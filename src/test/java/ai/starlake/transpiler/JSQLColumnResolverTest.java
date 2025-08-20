/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI (hayssam.saleh@starlake.ai)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcCatalog;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcSchema;
import ai.starlake.transpiler.schema.JdbcTable;
import ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilder;
import ai.starlake.transpiler.schema.treebuilder.XmlTreeBuilder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
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
        .addTable("a", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"))
        .addTable("b", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"));

    ResultSetMetaData res = JSQLColumResolver.getResultSetMetaData("SELECT * FROM a, b", metaData);

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
    //@formatter:off
    String[][] expected = new String[][] {
            {"c", "col1"}
            , {"c", "col2"}
            , {"c", "col3"}
            , {"c", "colBA"}
            , {"c", "colBB"}
    };
    //@formatter:on
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

      // JdbcColumn points on its Expression
      Assertions.assertThat(column.getExpression()).isInstanceOf(Function.class);

      // JdbcColum has methods which can be used to traverse through the lineage
      // getChildren() provides a List of children
      for (JdbcColumn child : column.getChildren()) {

        // getParent() provides the parent
        Assertions.assertThat(child.getParent()).isInstanceOf(JdbcColumn.class);

        // JdbcColumn points on its Expression
        Assertions.assertThat(child.getExpression()).isInstanceOf(Addition.class);
      }
    }
  }

  @Test
  void testCaseExpression() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String sqlStr =
        "SELECT Case when Sum(colBA + colBB)=0 then c.col1 else a.col2 end AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
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
    //@formatter:off
    String[][] expected = new String[][] {
            {"c", "test", "test"}
            , {"c", "colBA", "colBA"}
    };
    //@formatter:on
    assertThatResolvesInto(sqlStr, expected);

    //@formatter:off
    String lineage =
            "SELECT\n"
            + " ├─c.test → b.test : Other\n"
            + " └─c.colBA → b.colBA : Other\n"
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


  @Test
  void testHayssam1() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData("", "");
    metaData.addTable("d", "a", new JdbcColumn("col1"), new JdbcColumn("col2"),
        new JdbcColumn("col3"));
    metaData.addTable("b", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"));


    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData("SELECT * FROM d.a, b", metaData);
    Assertions.assertThat(6).isEqualTo(res.getColumnCount());

    String[][] expected = new String[][] {{"a", "col1"}, {"a", "col2"}, {"a", "col3"},
        {"b", "col1"}, {"b", "col2"}, {"b", "col3"}};

    assertThatResolvesInto(res, expected);

  }

  @Test
  void testBigQuerySinglePairQuoting() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData("c", "s");
    JdbcCatalog catalog = new JdbcCatalog("c", ".");
    metaData.put(catalog);

    JdbcSchema schema = new JdbcSchema("s", "c");
    catalog.put(schema);

    JdbcTable table = new JdbcTable("c", "s", "t");
    table.add(new JdbcColumn("a"));
    table.add(new JdbcColumn("b"));
    schema.put(table);

    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData("SELECT * from `c.s.t`", metaData);

    String[][] expected = new String[][] {{"c", "s", "t", "a", "a"}, {"c", "s", "t", "b", "b"}};

    assertThatResolvesInto(res, expected);

  }

  @Test
  void testWithBQProjectIdAndQuotes()
      throws JSQLParserException, SQLException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    String sqlStr =
        "with mycte as (\n" + "    select o.amount, c.id, CURRENT_TIMESTAMP() as timestamp1\n"
            + "    from sales.orders o, sales.customers c\n" + "    where o.customer_id = c.id\n"
            + ")\n" + "select id, sum(amount) as sum, timestamp1\n" + "from mycte\n"
            + "group by mycte.id, mycte.timestamp1";


    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData(sqlStr, JdbcMetaData.copyOf(metaData));

    String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp1"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─mycte.id → sales.customers.id : Other\n" +
            " ├─sum AS Function sum\n" +
            " │  └─mycte.amount → sales.orders.amount : Other\n" +
            " └─mycte.timestamp1 AS TimeKeyExpression: CURRENT_TIMESTAMP()\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }

  @Test
  void testWithAllQuoted() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    //@formatter:off
    String sqlStr =
            "with \"mycte\" as (\n"
            + "    select \"o\".\"amount\", \"c\".\"id\", CURRENT_TIMESTAMP() as \"timestamp\"\n"
            + "    from \"sales\".\"orders\" \"o\", \"sales\".\"customers\" \"c\"\n"
            + "    where \"o\".\"customer_id\" = \"c\".\"id\"\n"
            + ")\n"
            + "select \"id\", sum(\"amount\") as sum, \"timestamp\"\n"
            + "from \"mycte\"\n"
            + "group by \"mycte\".\"id\", \"mycte\".\"timestamp\"";
    //@formatter:on

    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData(sqlStr, JdbcMetaData.copyOf(metaData));

    String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─mycte.id → sales.customers.id : Other\n" +
            " ├─sum AS Function sum\n" +
            " │  └─mycte.amount → sales.orders.amount : Other\n" +
            " └─mycte.timestamp AS TimeKeyExpression: CURRENT_TIMESTAMP()\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }

  @Test
  void testUnresolvableTable() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    //@formatter:off
    String sqlStr =
            "with \"mycte\" as (\n"
            + "    select invalidColumn, \"c\".\"id\", CURRENT_TIMESTAMP() as \"timestamp\"\n"
            + "    from nonExistingTable \"o\", \"sales\".\"customers\" \"c\"\n"
            + "    where \"o\".\"customer_id\" = \"c\".\"id\"\n"
            + ")\n"
            + "select \"id\", sum(\"amount\") as sum, \"timestamp\"\n"
            + "from \"mycte\"\n"
            + "group by \"mycte\".\"id\", \"mycte\".\"timestamp\"";
    //@formatter:on

    // STRICT MODE
    Assertions.assertThatException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
      @Override
      public void call() throws Throwable {
        JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
            JdbcMetaData.copyOf(metaData.setErrorMode(JdbcMetaData.ErrorMode.STRICT)));

        String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
        assertThatResolvesInto(res, expected);
      }
    });

    // LENIENT MODE
    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        JdbcMetaData.copyOf(metaData.setErrorMode(JdbcMetaData.ErrorMode.LENIENT)));

    String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
        "SELECT\n" +
        " ├─mycte.id → sales.customers.id : Other\n" +
        " ├─sum AS Function sum\n" +
        " │  └─amount : Unknown Not found in schema\n" +
        " └─mycte.timestamp AS TimeKeyExpression: CURRENT_TIMESTAMP()";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);

    // IGNORE
    res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        JdbcMetaData.copyOf(metaData.setErrorMode(JdbcMetaData.ErrorMode.IGNORE)));

    expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    lineage =
        "SELECT\n" +
        " ├─mycte.id → sales.customers.id : Other\n" +
        " ├─sum AS Function sum\n" +
        " └─mycte.timestamp AS TimeKeyExpression: CURRENT_TIMESTAMP()\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }



  @Test
  void testWithAllBackTickQuoted()
      throws JSQLParserException, SQLException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    //@formatter:off
    String sqlStr =
            "with `mycte` as (\n"
            + "    select `o`.`amount`, `c`.`id`, CURRENT_TIMESTAMP() as `timestamp`\n"
            + "    from `sales`.`orders` `o`, `sales`.`customers` `c`\n"
            + "    where `o`.`customer_id` = `c`.`id`\n"
            + ")\n"
            + "select `id`, sum(`amount`) as sum, `timestamp`\n"
            + "from `mycte`\n"
            + "group by `mycte`.`id`, `mycte`.`timestamp`";
    //@formatter:on

    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData(sqlStr, JdbcMetaData.copyOf(metaData));

    String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─mycte.id → sales.customers.id : Other\n" +
            " ├─sum AS Function sum\n" +
            " │  └─mycte.amount → sales.orders.amount : Other\n" +
            " └─mycte.timestamp AS TimeKeyExpression: CURRENT_TIMESTAMP()\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }

  @Test
  void testWithBigQuerySingleQuotePair()
      throws JSQLParserException, SQLException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    //@formatter:off
    String sqlStr =
            "with `mycte` as (\n"
            + "    select `o`.`amount`, `c`.`id`, CURRENT_TIMESTAMP() as `timestamp`\n"
            + "    from `sales.orders` `o`, `sales.customers` `c`\n"
            + "    where `o`.`customer_id` = `c`.`id`\n"
            + ")\n"
            + "select `id`, sum(`amount`) as sum, `timestamp`\n"
            + "from `mycte`\n"
            + "group by `mycte`.`id`, `mycte`.`timestamp`";
    //@formatter:on

    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData(sqlStr, JdbcMetaData.copyOf(metaData));

    String[][] expected = new String[][] {{"mycte", "id"}, {"", "sum"}, {"mycte", "timestamp"}};
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─mycte.id → sales.customers.id : Other\n" +
            " ├─sum AS Function sum\n" +
            " │  └─mycte.amount → sales.orders.amount : Other\n" +
            " └─mycte.timestamp AS TimeKeyExpression: CURRENT_TIMESTAMP()\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }

  @Test
  void testWithWith() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    JdbcMetaData metaData =
        new JdbcMetaData("", "").addTable("sales", "orders", new JdbcColumn("customer_id"),
            new JdbcColumn("order_id"), new JdbcColumn("amount"), new JdbcColumn("seller_id"))
            .addTable("sales", "customers", new JdbcColumn("id"), new JdbcColumn("signup"),
                new JdbcColumn("contact"), new JdbcColumn("birthdate"), new JdbcColumn("name1"),
                new JdbcColumn("name2"), new JdbcColumn("id1"));
    //@formatter:off
    String sqlStr =
            "WITH mycte AS (\n"
            + "        SELECT  o.amount\n"
            + "                , c.id\n"
            + "                , CURRENT_TIMESTAMP() AS timestamp1\n"
            + "                , o.amount AS amount2\n"
            + "        FROM sales.orders o\n"
            + "            , sales.customers c\n"
            + "        WHERE o.customer_id = c.id )\n"
            + "    , yourcte AS (\n"
            + "        SELECT *\n"
            + "        FROM mycte )\n"
            + "SELECT  id\n"
            + "        , Sum( amount ) AS sum\n"
            + "        , timestamp1\n"
            + "        , amount AS amount2\n"
            + "FROM yourcte\n"
            + "GROUP BY    yourcte.id\n"
            + "            , yourcte.timestamp1\n"
            + ";";
    //@formatter:on

    JdbcResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData(sqlStr, JdbcMetaData.copyOf(metaData));

    //@formatter:off
    String[][] expected = new String[][] {
            {"yourcte", "id", "id"}
            , {"", "Sum", "sum"}
            , {"yourcte", "timestamp1", "timestamp1"}
            , {"yourcte", "amount", "amount2"}
    };
    //@formatter:on
    assertThatResolvesInto(res, expected);

    //@formatter:off
    String lineage =
            "SELECT\n" +
            " ├─yourcte.id → sales.customers.id : Other\n" +
            " ├─sum AS Function Sum\n" +
            " │  └─yourcte.amount → sales.orders.amount : Other\n" +
            " ├─yourcte.timestamp1 AS TimeKeyExpression: CURRENT_TIMESTAMP()\n" +
            " └─amount2 AS yourcte.amount → sales.orders.amount : Other\n";
    //@formatter:on
    assertLineage(JdbcMetaData.copyOf(metaData), sqlStr, lineage);
  }

  @Test
  void testScopeColumn() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    String[][] schemaDefinition = {
        // Table a
        {"a", "id"},

        // Table b
        {"mytable", "id", "cola", "colb"}};

    String sqlStr = "select * from a, (select cola id, colb b1 from mytable) b where a.id=b.id";

    // get the List of JdbcColumns, each holding its lineage using the TreeNode interface
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);
    System.out.println(resolver.getLineage(AsciiTreeBuilder.class, sqlStr));
  }

  @Test
  void testWithAndFunctionClauseIssue41()
      throws JSQLParserException, SQLException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {

    //@formatter:off
    String[][] schemaDefinition = {
            {"a", "col1", "col2"}
    };

    String sqlStr =
          " WITH d AS (\n"
        + "        SELECT SUM(a.col1, a.col2) as colx\n"
        + "        FROM a )\n" + " SELECT *\n"
        + " FROM d\n"
        + ";"
        ;

    String expected =
          "SELECT\n"
        + " └─d.colx AS Function SUM\n"
        + "    ├─a.col1 : Other\n"
        + "    └─a.col2 : Other"
        ;
    //@formatter:on

    assertLineage(schemaDefinition, sqlStr, expected);
  }

  @Test
  void testDiscussion2096() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {

    // provide minimum schema information,
    // alternatively JSQLTranspiler can derive that from the Database itself when you provide a JDBC
    // connection
    String[][] schemaDefinition =
        {{"projects", "employee_id"}, {"tasks", "employee_id"}, {"employees", "name"}};

    // the SQL Query to resolve
    String sqlStr = " SELECT e.name, \n"
        + "       (SELECT COUNT(*) FROM projects AS p WHERE p.employee_id = e.employee_id) AS project_count,\n"
        + "       (SELECT COUNT(*) FROM tasks AS p WHERE p.employee_id = e.employee_id) AS task_count\n"
        + "FROM employees AS e";

    // The expected output in ASCII (alternatively JSON and XML is available)
    // @formatter:off
    String expected =
            "SELECT\n" +
            " ├─employees.name : Other\n" +
            " ├─project_count AS SELECT\n" +
            " │  └─Function COUNT\n" +
            " │     └─projects.employee_id : Other\n" +
            " │  └─projects.employee_id : Other\n" +
            " └─task_count AS SELECT\n" +
            "    └─Function COUNT\n" +
            "       └─tasks.employee_id : Other\n" +
            "    └─tasks.employee_id : Other\n";
    // @formatter:on
    assertLineage(schemaDefinition, sqlStr, expected);
  }

  /**
   * Test lenient mode for an aliased table and column, when table and column exist, but schema
   * mismatch.
   *
   * @throws JSQLParserException then exception when the SQL can't be parsed
   */
  @Test
  void testLenientSelectAliasSchemaMismatch() throws JSQLParserException {
    //@formatter:off
    String sqlStr =
        "SELECT alias_a.colaa\n" +
        "FROM a alias_a\n" +
        "    JOIN b alias_b\n" +
        "        ON alias_a.col1 = alias_b.col1";
    //@formatter:on

    JdbcMetaData jdbcMetadata = new JdbcMetaData("", "starbake");
    jdbcMetadata.addTable("", "", "a", new JdbcColumn("col1"), new JdbcColumn("colAA"));
    jdbcMetadata.addTable("", "", "b", new JdbcColumn("col1"));

    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        jdbcMetadata.setErrorMode(JdbcMetaData.ErrorMode.LENIENT));

    // `alias_a.colAA` --> derived from Table `a`
    Assertions.assertThat(res.getColumns().get(0).scopeTable).isEqualToIgnoringCase("a");
  }

  /**
   * Test lenient mode for an aliased table and column, when table exists, but no columns found and
   * schema mismatch.
   *
   * @throws JSQLParserException then exception when the SQL can't be parsed
   */
  @Test
  void testLenientSelectAliasSchemaAndColumnsMismatch() throws JSQLParserException, SQLException {
    //@formatter:off
    String sqlStr =
        "SELECT alias_a.colaa\n" +
        "FROM a alias_a\n" +
        "    JOIN b alias_b\n" +
        "        ON alias_a.col1 = alias_b.col1";
    //@formatter:on

    JdbcMetaData jdbcMetadata = new JdbcMetaData("", "starbake");
    jdbcMetadata.addTable("", "", "a", new JdbcColumn[0]);
    jdbcMetadata.addTable("", "", "b", new JdbcColumn[0]);

    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        jdbcMetadata.setErrorMode(JdbcMetaData.ErrorMode.LENIENT));

    // `alias_a.colAA` --> derived from Table `a`
    Assertions.assertThat(res.getColumns().get(0).scopeTable).isEqualToIgnoringCase("a");
  }

  @Test
  void testSelectCTEAlias() throws JSQLParserException, SQLException {
    //@formatter:off
    String sqlStr =
        "WITH order_details AS (\n" +
                "    SELECT  o.order_id\n" +
                "         , o.customer_id\n" +
                "         , List( p.name || ' (' || o.quantity || ')' ) AS purchased_items\n" +
                "         , Sum( o.quantity * p.price ) AS total_order_value\n" +
                "    FROM starbake.order_line o\n" +
                "             JOIN starbake.product p\n" +
                "                  ON o.product_id = p.product_id\n" +
                "    GROUP BY    o.order_id\n" +
                "           , o.customer_id )\n" +
                "SELECT  order_id\n" +
                "     , customer_id\n" +
                "     , purchased_items\n" +
                "     , total_order_value\n" +
                "FROM order_details\n" +
                "ORDER BY order_id";
    //@formatter:on

    JdbcMetaData jdbcMetadata = new JdbcMetaData("", "starbake");
    jdbcMetadata.addTable("", "starbake", "order_line", new JdbcColumn[0]);
    jdbcMetadata.addTable("", "starbake", "product", new JdbcColumn[0]);
    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        jdbcMetadata.setErrorMode(JdbcMetaData.ErrorMode.LENIENT));

    // `order_id` --> `order_details` derived from Table `order_line`
    Assertions.assertThat(res.getColumns().get(0).scopeTable).isEqualToIgnoringCase("order_line");

    // `customer_id` --> `order_details` derived from Table `order_line`
    Assertions.assertThat(res.getColumns().get(1).scopeTable).isEqualToIgnoringCase("order_line");

    // The last two columns also stop short of resolving to the source table name and instead
    // resolve to the alias name only.
    Assertions.assertThat(res.getColumns()).hasSize(4);
  }

  @Test
  void testIssue115() throws JSQLParserException, SQLException, InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    //@formatter:off
    String sqlStr =
            "WITH customer_metrics AS (\n"
            + "        SELECT  Count( DISTINCT customer_id ) AS total_customers\n"
            + "                , Avg( total_orders ) AS avg_orders_per_customer\n"
            + "                , Avg( total_spent ) AS avg_spent_per_customer\n"
            + "                , Min( first_order_date ) AS earliest_order_date\n"
            + "                , Max( last_order_date ) AS latest_order_date\n"
            + "                , Avg( days_since_first_order ) AS avg_customer_lifetime_days\n"
            + "                , Avg( Array_Length( purchased_categories, 1 ) ) AS avg_categories_per_customer\n"
            + "        FROM starbake_analytics.customer_purchase_history )\n"
            + "    , order_metrics AS (\n"
            + "        SELECT  Count( DISTINCT order_id ) AS total_orders\n"
            + "                , Sum( total_order_value ) AS total_revenue\n"
            + "                , Avg( total_order_value ) AS avg_order_value\n"
            + "                , Count( DISTINCT customer_id ) AS customers_with_orders\n"
            + "        FROM starbake_analytics.order_items_analysis )\n"
            + "SELECT  cm.total_customers\n"
            + "        , om.total_orders\n"
            + "        , om.total_revenue\n"
            + "        , om.avg_order_value\n"
            + "        , cm.avg_orders_per_customer\n"
            + "        , cm.avg_spent_per_customer\n"
            + "        , cm.earliest_order_date\n"
            + "        , cm.latest_order_date\n"
            + "        , cm.avg_customer_lifetime_days\n"
            + "        , cm.avg_categories_per_customer\n"
            + "        , om.customers_with_orders::FLOAT\n"
            + "             / cm.total_customers AS customer_order_rate\n"
            + "        , om.total_revenue\n"
            + "             / Nullif(  Cast( cm.latest_order_date AS DATE ) -  Cast( cm.earliest_order_date AS DATE ), 0 ) AS daily_revenue\n"
            + "        , om.total_orders::FLOAT\n"
            + "             / Nullif(  Cast( cm.latest_order_date AS DATE ) -  Cast( cm.earliest_order_date AS DATE ), 0 ) AS daily_order_rate\n"
            + "-- om.total_revenue / NULLIF(DATEDIFF('day', cm.earliest_order_date, cm.latest_order_date), 0) AS daily_revenue,\n"
            + " -- om.total_orders::FLOAT / NULLIF(DATEDIFF('day', cm.earliest_order_date, cm.latest_order_date), 0) AS daily_order_rate\n"
            + "FROM customer_metrics cm\n"
            + "    CROSS JOIN order_metrics om\n"
            + ";";
    //@formatter:on

    // schema with 2 tables, but w/o any columns
    JdbcMetaData jdbcMetadata = new JdbcMetaData("", "starbake_analytics");
    jdbcMetadata.addTable("starbake_analytics", "customer_purchase_history", List.of());
    jdbcMetadata.addTable("starbake_analytics", "order_items_analysis", List.of());

    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        jdbcMetadata.setErrorMode(JdbcMetaData.ErrorMode.LENIENT));

    Assertions.assertThat(res.getColumns()).hasSize(13);

    // The expected output in ASCII (alternatively JSON and XML is available)
    // @formatter:off
    String expected =
            "SELECT\n" +
            " ├─Function Count\n" +
            " │  └─customer_id : Unknown Not found in schema\n" +
            " ├─Function Count\n" +
            " │  └─order_id : Unknown Not found in schema\n" +
            " ├─Function Sum\n" +
            " │  └─total_order_value : Unknown Not found in schema\n" +
            " ├─Function Avg\n" +
            " │  └─total_order_value : Unknown Not found in schema\n" +
            " ├─Function Avg\n" +
            " │  └─total_orders : Unknown Not found in schema\n" +
            " ├─Function Avg\n" +
            " │  └─total_spent : Unknown Not found in schema\n" +
            " ├─Function Min\n" +
            " │  └─first_order_date : Unknown Not found in schema\n" +
            " ├─Function Max\n" +
            " │  └─last_order_date : Unknown Not found in schema\n" +
            " ├─Function Avg\n" +
            " │  └─days_since_first_order : Unknown Not found in schema\n" +
            " ├─Function Avg\n" +
            " │  └─.Array_Length AS Function Array_Length\n" +
            " │     ├─purchased_categories : Unknown Not found in schema\n" +
            " │     └─LongValue: 1\n" +
            " ├─customer_order_rate AS Division: om.customers_with_orders::FLOAT / cm.total_customers\n" +
            " │  ├─Function Count\n" +
            " │  │  └─customer_id : Unknown Not found in schema\n" +
            " │  └─Function Count\n" +
            " │     └─customer_id : Unknown Not found in schema\n" +
            " ├─daily_revenue AS Division: om.total_revenue / Nullif(Cast(cm.latest_order_date AS DATE) - Cast(cm.earliest_order_date AS DATE), 0)\n" +
            " │  ├─Function Sum\n" +
            " │  │  └─total_order_value : Unknown Not found in schema\n" +
            " │  └─.Nullif AS Function Nullif\n" +
                    " │     ├─Subtraction: Cast(cm.latest_order_date AS DATE) - Cast(cm.earliest_order_date AS DATE)\n" +
                    " │     │  ├─Function Max\n" +
                    " │     │  │  └─last_order_date : Unknown Not found in schema\n" +
                    " │     │  └─Function Min\n" +
                    " │     │     └─first_order_date : Unknown Not found in schema\n" +
                    " │     └─LongValue: 0\n" +
                    " └─daily_order_rate AS Division: om.total_orders::FLOAT / Nullif(Cast(cm.latest_order_date AS DATE) - Cast(cm.earliest_order_date AS DATE), 0)\n" +
                    "    ├─Function Count\n" +
                    "    │  └─order_id : Unknown Not found in schema\n" +
                    "    └─.Nullif AS Function Nullif\n" +
                    "       ├─Subtraction: Cast(cm.latest_order_date AS DATE) - Cast(cm.earliest_order_date AS DATE)\n" +
                    "       │  ├─Function Max\n" +
                    "       │  │  └─last_order_date : Unknown Not found in schema\n" +
                    "       │  └─Function Min\n" +
                    "       │     └─first_order_date : Unknown Not found in schema\n" +
                    "       └─LongValue: 0\n";
    // @formatter:on
    assertLineage(jdbcMetadata, sqlStr, expected);

  }


  @Test
  void testIssue119MissingSourceTable()
      throws JSQLParserException, SQLException, InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    // formatter:off
    String sqlStr = "WITH customer_orders AS (\n" + "        SELECT  o.customer_id\n"
        + "                , Count( DISTINCT o.order_id ) AS total_orders\n"
        + "                , Sum( o.quantity * p.price ) AS total_spent\n"
        + "                , Min( o.order_date ) AS first_order_date\n"
        + "                , Max( o.order_date ) AS last_order_date\n"
        + "                , Array_Agg( DISTINCT p.category ) AS purchased_categories\n"
        + "        FROM starbake.orders o\n" + "            JOIN starbake.products p\n"
        + "                ON o.product_id = p.product_id\n" + "        GROUP BY o.customer_id )\n"
        + "SELECT  co.customer_id\n"
        + "        , Concat( c.first_name, ' ', c.last_name ) AS customer_name\n"
        + "        , c.email\n" + "        , co.total_orders\n" + "        , co.total_spent\n"
        + "        , co.first_order_date\n" + "        , co.last_order_date\n"
        + "        , co.purchased_categories\n"
        + "        , (  Cast( co.last_order_date AS DATE ) -  Cast( co.first_order_date AS DATE ) ) AS days_since_first_order\n"
        + "FROM starbake.customers c\n" + "    LEFT JOIN customer_orders co\n"
        + "        ON c.id = co.customer_id\n" + "ORDER BY co.total_spent DESC NULLS LAST\n" + ";";

    String[][] expected =
        new String[][] {{"customer_orders", "customer_id"}, {"", "Concat"}, {"customers", "email"},
            {"customer_orders", "total_orders"}, {"customer_orders", "total_spent"},
            {"customer_orders", "first_order_date"}, {"customer_orders", "last_order_date"},
            {"customer_orders", "purchased_categories"}, {"", "ParenthesedExpressionList"}};
    // formatter:on

    JdbcMetaData meta = new JdbcMetaData(JSQLSchemaDiffTest.getStarlakeSchemas());
    JdbcResultSetMetaData res = JSQLColumResolver.getResultSetMetaData(sqlStr,
        meta.setErrorMode(JdbcMetaData.ErrorMode.LENIENT));
    assertThatResolvesInto(res, expected);

    Assertions.assertThat(res.getScopeTable(3)).isEqualToIgnoringCase("customers");

    // The expected output in ASCII (alternatively JSON and XML is available)
    // @formatter:off
    String expectedASCII =
            "SELECT\n" +
            " ├─customer_orders.customer_id → starbake.orders.customer_id : long\n" +
            " ├─customer_name AS .Concat AS Function Concat\n" +
            " │  ├─starbake.customers.first_name : string\n" +
            " │  ├─StringValue: ' '\n" +
            " │  └─starbake.customers.last_name : string\n" +
            " ├─starbake.customers.email : string\n" +
            " ├─Function Count\n" +
            " │  └─starbake.orders.order_id : long\n" +
            " ├─Function Sum\n" +
            " │  └─Multiplication: o.quantity * p.price\n" +
            " │     ├─starbake.orders.quantity : long\n" +
            " │     └─starbake.products.price : double\n" +
            " ├─Function Min\n" +
            " │  └─starbake.orders.order_date : date\n" +
            " ├─Function Max\n" +
            " │  └─starbake.orders.order_date : date\n" +
            " ├─Function Array_Agg\n" +
            " │  └─starbake.products.category : string\n" +
            " └─days_since_first_order AS ParenthesedExpressionList: (Cast(co.last_order_date AS DATE) - Cast(co.first_order_date AS DATE))\n" +
            "    └─Subtraction: Cast(co.last_order_date AS DATE) - Cast(co.first_order_date AS DATE)\n" +
            "       ├─Function Max\n" +
            "       │  └─starbake.orders.order_date : date\n" +
            "       └─Function Min\n" +
            "          └─starbake.orders.order_date : date\n";
    // @formatter:on
    assertLineage(meta, sqlStr, expectedASCII);
  }
}
