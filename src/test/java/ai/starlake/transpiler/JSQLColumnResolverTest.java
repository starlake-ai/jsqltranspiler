/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilder;
import ai.starlake.transpiler.schema.treebuilder.XmlTreeBuilder;
import ai.starlake.transpiler.snowflake.AsciiTreeBuilder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

public class JSQLColumnResolverTest extends AbstractColumnResolverTest {

  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/schema";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @Test
  void testSimpleSchemaProvider() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData("", "")
        .addTable("d", "a", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"))
        .addTable("b", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"));

    ResultSetMetaData res = JSQLColumResolver.getResultSetMetaData("SELECT * FROM d.a, b", metaData);
    Assertions.assertThat(6).isEqualTo(res.getColumnCount());

    Assertions.assertThat(new String[] {"a", "col1"})
        .isEqualTo(new String[] {res.getTableName(1), res.getColumnName(1)});
    Assertions.assertThat(new String[] {"a", "col2"})
        .isEqualTo(new String[] {res.getTableName(2), res.getColumnName(2)});
    Assertions.assertThat(new String[] {"a", "col3"})
        .isEqualTo(new String[] {res.getTableName(3), res.getColumnName(3)});
    Assertions.assertThat(new String[] {"b", "col1"})
        .isEqualTo(new String[] {res.getTableName(4), res.getColumnName(4)});
    Assertions.assertThat(new String[] {"b", "col2"})
        .isEqualTo(new String[] {res.getTableName(5), res.getColumnName(5)});
    Assertions.assertThat(new String[] {"b", "col3"})
        .isEqualTo(new String[] {res.getTableName(6), res.getColumnName(6)});
  }

  @Test
  void testWithBQProjectIdAndQuotes() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData("", "")
            .addTable("sales", "orders", new JdbcColumn("customer_id"), new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"), new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"), new JdbcColumn("name2"), new JdbcColumn("id1"));
    String sqlStr = "with mycte as (\n" +
            "    select o.amount, c.id, CURRENT_TIMESTAMP() as timestamp\n" +
            "    from sales.orders o, sales.customers c\n" +
            "    where o.customer_id = c.id\n" +
            ")\n" +
            "select id, sum(amount) as sum, timestamp\n" +
            "from mycte\n" +
            "group by mycte.id, mycte.timestamp";
    ResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr, metaData);
  }
  
  @Test
  void testSimplerSchemaProvider() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData().addTable("a", "col1", "col2", "col3").addTable("b",
        "col1", "col2", "col3");

    ResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData("SELECT b.* FROM a, b", metaData);

    Assertions.assertThat(3).isEqualTo(res.getColumnCount());

    Assertions.assertThat(new String[] {"b", "col1"})
        .isEqualTo(new String[] {res.getTableName(1), res.getColumnName(1)});
    Assertions.assertThat(new String[] {"b", "col2"})
        .isEqualTo(new String[] {res.getTableName(2), res.getColumnName(2)});
    Assertions.assertThat(new String[] {"b", "col3"})
        .isEqualTo(new String[] {res.getTableName(3), res.getColumnName(3)});
  }

  @Test
  void testSimplestSchemaProvider() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"}};

    // allows for:
    // JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);

    String sqlStr = "SELECT b.* FROM a, b";

    String[][] expected = new String[][] {{"b", "col1"}, {"b", "col2"}, {"b", "col3"}};

    assertThatResolvesInto(schemaDefinition, sqlStr, expected);
  }

  @Test
  void testExcludeAllColumns() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * EXCEPT(col1, col2, colAB, colBB) FROM a, b";
    String[][] expected =
        new String[][] {{"a", "col3"}, {"a", "colAA"}, {"b", "col3"}, {"b", "colBA"}};
    assertThatResolvesInto(sqlStr, expected);


    sqlStr = "SELECT * EXCEPT(b.col1, b.col2, b.colBB) FROM a, b";
    expected = new String[][] {{"a", "col1"}, {"a", "col2"}, {"a", "col3"}, {"a", "colAA"},
        {"a", "colAB"}, {"b", "col3"}, {"b", "colBA"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testExcludeAllTableColumns() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT b.* EXCEPT(col1, col2, colBB) FROM a, b";
    String[][] expected = new String[][] {{"b", "col3"}, {"b", "colBA"}};
    assertThatResolvesInto(sqlStr, expected);


    sqlStr = "SELECT b.* EXCEPT(b.col1, b.col2, b.colBB) FROM a, b";
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  @Disabled
  void testReplaceAllTableColumns() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT b.* REPLACE(replacement1 AS col1, replacement2 AS col2) FROM a, b";
    String[][] expected = new String[][] {{"", "replacement1", "col1"},
        {"", "replacement2", "col2"}, {"b", "col3"}, {"b", "colA"}, {"b", "colB"}};
    assertThatResolvesInto(sqlStr, expected);


    sqlStr = "SELECT b.* REPLACE(b.col1 AS replacement1, b.col2 AS replacement2) FROM a, b";
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  @Disabled
  void testReplaceAllColumns() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * REPLACE(col1 AS replacement1, col2 AS replacement2) FROM a, b";
    String[][] expected =
        new String[][] {{"", "replacement1", "col1"}, {"", "replacement2", "col2"}, {"a", "col3"},
            {"a", "colA"}, {"a", "colB"}, {"", "replacement1", "col1"},
            {"", "replacement2", "col2"}, {"b", "col3"}, {"b", "colA"}, {"b", "colB"}};
    assertThatResolvesInto(sqlStr, expected);


    sqlStr = "SELECT * REPLACE(b.col1 AS replacement1, b.col2 AS replacement2) FROM a, b";
    expected = new String[][] {{"a", "col1"}, {"a", "col2"}, {"a", "col3"}, {"a", "colA"},
        {"a", "colB"}, {"b", "col1", "replacement1"}, {"b", "col2", "replacement2"}, {"b", "col3"},
        {"b", "colA"}, {"b", "colB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  @Disabled
  void testColumnsFunction() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT COLUMNS('b\\.col\\d+') FROM a, b";
    String[][] expected = new String[][] {{"b", "col1"}, {"b", "col2"}, {"b", "col3"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testWithClause() throws JSQLParserException, SQLException {
    String sqlStr = "WITH c AS (SELECT * FROM b) SELECT * FROM c";
    String[][] expected = new String[][] {{"c", "col1"}, {"c", "col2"}, {"c", "col3"},
        {"c", "colBA"}, {"c", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);


    // Nested With Statements
    //@formatter:off
    sqlStr =
            " WITH d AS (\n" +
            "        WITH c AS (\n" +
            "                SELECT *\n" +
            "                FROM b )\n" +
            "        SELECT *\n" +
            "        FROM c )\n" +
            " SELECT *\n" +
            " FROM d\n" + ";";

    expected = new String[][] {
            {"d", "col1"},
            {"d", "col2"},
            {"d", "col3"},
            {"d", "colBA"},
            {"d", "colBB"}
    };
    //@formatter:on
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testParenthesedSubSelect() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * FROM (SELECT * FROM b) c ";
    String[][] expected = new String[][] {{"c", "col1"}, {"c", "col2"}, {"c", "col3"},
        {"c", "colBA"}, {"c", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testParenthesedFromIten() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * FROM ( (SELECT * FROM b) c inner join a on c.col1 = a.col1 ) d ";
    String[][] expected = new String[][] {
        // from physical table b
        {"d", "col1"}, {"d", "col2"}, {"d", "col3"}, {"d", "colBA"}, {"d", "colBB"},

        // from physical table a
        {"d", "col1_1"}, {"d", "col2_1"}, {"d", "col3_1"}, {"d", "colAA"}, {"d", "colAB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testSubSelectJoin() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT colBA, colBB FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"c", "colBA"}, {"c", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testLeftJoinUsing() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * from a left join b using (col1, col2)";
    String[][] expected = new String[][] {{"a", "col1"}, {"a", "col2"}, {"a", "col3"},
        {"a", "colAA"}, {"a", "colAB"}, {"b", "col3"}, {"b", "colBA"}, {"b", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testRightJoinUsing() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * from a right join b using (col1, col2)";
    String[][] expected = new String[][] {{"b", "col1"}, {"b", "col2"}, {"a", "col3"},
        {"a", "colAA"}, {"a", "colAB"}, {"b", "col3"}, {"b", "colBA"}, {"b", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);
  }

  @Test
  void testNaturalJoin() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT * from a natural join b";
    String[][] expected = new String[][] {{"a", "col1"}, {"a", "col2"}, {"a", "col3"},
        {"a", "colAA"}, {"a", "colAB"}, {"b", "colBA"}, {"b", "colBB"}};
    assertThatResolvesInto(sqlStr, expected);
  }


  @Test
  void testFunction() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr =
        "SELECT Sum(colBA + colBB) AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"", "Sum", "total"}};
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " └─total AS Function Sum\n" +
            "    └─Addition: colBA + colBB\n" +
            "       ├─c.colBA → b.colBA : Other\n" +
            "       └─c.colBB → b.colBB : Other"
            ;
    //@formatter:on
    assertLineage(sqlStr, lineage);
  }

  @Test
  void testFunctionVerbose() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {
        // Table A with Columns col1, col2, col3, colAA, colAB
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table B with Columns col1, col2, col3, colBA, colBB
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};

    String sqlStr =
        "SELECT Sum(colBA + colBB) AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"", "Sum", "total"}};

    /*
    SELECT
     └─total AS Function: Sum(colBA + colBB)
        └─Addition: colBA + colBB
           ├─c.colBA → b.colBA : Other
           └─c.colBB → b.colBB : Other
     */

    // resolve statement against schema and return the column information as ResultSetMetaData
    JdbcResultSetMetaData resultSetMetaData =
        new JSQLColumResolver(schemaDefinition).getResultSetMetaData(sqlStr);

    // Column Index starts with 1
    for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
      // generic column properties of the ResultSetMetaData interface
      Assertions.assertThat(resultSetMetaData.getColumnName(i)).isEqualToIgnoringCase("Sum");
      Assertions.assertThat(resultSetMetaData.getColumnTypeName(i)).isEqualToIgnoringCase("Other");


      // specific column object can be accessed in a list
      JdbcColumn column = resultSetMetaData.getColumns().get(i - 1);

      // Optional Alias is stored in a list, can be NULL/empty
      String alias = resultSetMetaData.getLabels().get(i - 1);
      Assertions.assertThat(alias).isEqualToIgnoringCase("total");


      // JdbcColum implements the TreeNode interface which can be used to traverse through the
      // lineage
      Assertions.assertThat(column).isInstanceOf(TreeNode.class);

      // JdbcColumn points on its Expression
      Assertions.assertThat(column.getExpression()).isInstanceOf(Function.class);

      Iterator<JdbcColumn> iterator = column.children().asIterator();
      if (iterator.hasNext()) {
        JdbcColumn child = iterator.next();

        // JdbcColum implements the TreeNode interface which can be used to traverse through the
        // lineage
        Assertions.assertThat(child).isInstanceOf(TreeNode.class);

        // JdbcColumn points on its Expression
        Assertions.assertThat(child.getExpression()).isInstanceOf(Addition.class);
      }
    }
  }

  @Test
  void testCaseExpression() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr =
        "SELECT " +
                "Case when Sum(colBA + colBB)=0 " +
                "then c.col1 else a.col2 end AS total " +
                "FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"", "CaseExpression", "total"}};
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage = "SELECT\n" +
            " └─total AS CaseExpression: CASE WHEN Sum(colBA + colBB) = 0 THEN c.col1 ELSE a.col2 END\n" +
            "    ├─WhenClause: WHEN Sum(colBA + colBB) = 0 THEN c.col1\n" +
            "    │  ├─EqualsTo: Sum(colBA + colBB) = 0\n" +
            "    │  │  ├─Function Sum\n" +
            "    │  │  │  └─Addition: colBA + colBB\n" +
            "    │  │  │     ├─c.colBA → b.colBA : Other\n" +
            "    │  │  │     └─c.colBB → b.colBB : Other\n" +
            "    │  │  └─LongValue: 0\n" +
            "    │  └─c.col1 → b.col1 : Other\n" +
            "    └─a.col2 : Other";
    //@formatter:on

    assertLineage(sqlStr, lineage);
  }

  @Test
  void testWithLineage() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr = "WITH c AS (SELECT col1 AS test, colBA FROM b) SELECT * FROM c";
    String[][] expected = new String[][] {{"c", "col1", "col1"}, {"c", "colBA", "colBA"}};
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─c.col1 → b.col1 : Other\n" +
            " └─c.colBA → b.colBA : Other\n"
            ;
    //@formatter:on

    assertLineage(sqlStr, lineage);
  }

  @Test
  void testCurrentDateLineage() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr = "SELECT CURRENT_DATE()";
    String[][] expected = new String[][] {{"", "CURRENT_DATE"}};
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " └─TimeKeyExpression: CURRENT_DATE()";
    //@formatter:on

    assertLineage(sqlStr, lineage);
  }

  @Test
  void testSubSelectLineage() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr = "SELECT (SELECT col1 AS test FROM b) col2 FROM a";
    String[][] expected = new String[][] {{"b", "col1", "col2"}};
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " └─col2 AS SELECT\n" +
            "    └─test AS b.col1 : Other";
    //@formatter:on
    assertLineage(sqlStr, lineage);
  }

  @Test
  void lineage() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String[][] schemaDefinition = {
        // Table a
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table b
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};

    String sqlStr =
        "SELECT Sum(colBA + colBB) AS total, (SELECT col1 AS test FROM b) col2, CURRENT_TIMESTAMP() as col3 FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";

    // get the List of JdbcColumns, each holding its lineage using the TreeNode interface
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);
    JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(sqlStr);

    System.out.println(resolver.getLineage(AsciiTreeBuilder.class, sqlStr));

    System.out.println(resolver.getLineage(XmlTreeBuilder.class, sqlStr));

    System.out.println(resolver.getLineage(JsonTreeBuilder.class, sqlStr));
  }



  @Test
  void testRewriteSimple() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT * FROM ( (SELECT * FROM b) c inner join a on c.col1 = a.col1 ) d ";

    String expected = "SELECT  d.col1                 /* Resolved Column*/\n"
        + "        , d.col2               /* Resolved Column*/\n"
        + "        , d.col3               /* Resolved Column*/\n"
        + "        , d.colba              /* Resolved Column*/\n"
        + "        , d.colbb              /* Resolved Column*/\n"
        + "        , d.col1_1             /* Resolved Column*/\n"
        + "        , d.col2_1             /* Resolved Column*/\n"
        + "        , d.col3_1             /* Resolved Column*/\n"
        + "        , d.colaa              /* Resolved Column*/\n"
        + "        , d.colab              /* Resolved Column*/\n"
        + "FROM (  (   SELECT  b.col1     /* Resolved Column*/\n"
        + "                    , b.col2   /* Resolved Column*/\n"
        + "                    , b.col3   /* Resolved Column*/\n"
        + "                    , b.colba  /* Resolved Column*/\n"
        + "                    , b.colbb  /* Resolved Column*/\n" + "            FROM b ) c\n"
        + "            INNER JOIN a\n" + "                ON c.col1 = a.col1 ) d\n" + ";";

    assertThatRewritesInto(sqlStr, expected);
  }
}
