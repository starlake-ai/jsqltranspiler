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
import com.opencsv.CSVWriter;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.StringWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class JSQLColumnResolverTest extends JSQLTranspilerTest {

  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/schema";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  @Disabled
  protected void transpile(File f, int idx, SQLTest t) throws Exception {

  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void resolve(File f, int idx, SQLTest t) throws Exception {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    if (t.prologue != null && !t.prologue.isEmpty()) {
      try (Statement st = connDuck.createStatement();) {
        st.executeUpdate("set timezone='Asia/Bangkok'");
        for (net.sf.jsqlparser.statement.Statement statement : CCJSqlParserUtil
            .parseStatements(t.prologue, executorService, parser -> {
            })) {
          st.executeUpdate(statement.toString());
        }
      }
    }

    ResultSetMetaData resultSetMetaData = JSQLColumResolver.getResultSetMetaData(t.providedSqlStr,
        metaData, "JSQLTranspilerTest", "main");

    StringWriter stringWriter = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);

    csvWriter.writeNext(new String[] {"#", "label", "name", "table", "schema", "catalog", "type",
        "type name", "precision", "scale", "display size"

        /* we can skip these for now
        , "auto increment"
        , "case-sensitive"
        , "searchable"
        , "currency"
        , "nullable"
        , "signed"
        , "readonly"
        , "writable"
        */
    }, true);

    final int maxI = resultSetMetaData.getColumnCount();
    for (int i = 1; i <= maxI; i++) {
      csvWriter.writeNext(new String[] {String.valueOf(i), resultSetMetaData.getColumnLabel(i),
          resultSetMetaData.getColumnName(i), resultSetMetaData.getTableName(i),
          resultSetMetaData.getSchemaName(i), resultSetMetaData.getCatalogName(i),
          JdbcMetaData.getTypeName(resultSetMetaData.getColumnType(i)),
          resultSetMetaData.getColumnTypeName(i), String.valueOf(resultSetMetaData.getPrecision(i)),
          String.valueOf(resultSetMetaData.getScale(i)),
          String.valueOf(resultSetMetaData.getColumnDisplaySize(i))

          /* we can skip these for now
          , "auto increment"
          , "case-sensitive"
          , "searchable"
          , "currency"
          , "nullable"
          , "signed"
          , "readonly"
          , "writable"
          */
      }, true);
    }
    csvWriter.flush();
    csvWriter.close();


    Assertions.assertThat(stringWriter.toString().trim()).isEqualToIgnoringCase(t.expectedResult);

  }

  @Test
  void testSimpleSchemaProvider() throws JSQLParserException, SQLException {
    JdbcMetaData metaData = new JdbcMetaData("", "")
        .addTable("a", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"))
        .addTable("b", new JdbcColumn("col1"), new JdbcColumn("col2"), new JdbcColumn("col3"));

    ResultSetMetaData res =
        JSQLColumResolver.getResultSetMetaData("SELECT * FROM a, b", metaData, "", "");

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
        JSQLColumResolver.getResultSetMetaData("SELECT b.* FROM a, b", metaData, "", "");

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
    sqlStr = "WITH d AS (\n" + "        WITH c AS (\n" + "                SELECT *\n"
        + "                FROM b )\n" + "        SELECT *\n" + "        FROM c )\n" + "SELECT *\n"
        + "FROM d\n" + ";";
    expected = new String[][] {{"d", "col1"}, {"d", "col2"}, {"d", "col3"}, {"d", "colBA"},
        {"d", "colBB"}};
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

  public static SimpleTreeNode translateNode(TreeNode node, String alias) {
    SimpleTreeNode simpleTreeNode = new SimpleTreeNode(
        ((alias != null && !alias.isEmpty()) ? alias + " AS " : "") + node.toString());
    Enumeration<? extends TreeNode> children = node.children();
    while (children.hasMoreElements()) {
      simpleTreeNode.addChild(translateNode(children.nextElement(), ""));
    }
    return simpleTreeNode;
  }

  public static String assertLineage(JdbcResultSetMetaData resultSetMetaData, String expected)
      throws SQLException {
    // Define your own Tree based on your own TreeNode interface
    SimpleTreeNode rootNode = new SimpleTreeNode("SELECT");
    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {

      // Add each columns lineage tree as node to the root with a translation from Swing's TreeNode
      rootNode.addChild(translateNode(resultSetMetaData.getColumns().get(i),
          resultSetMetaData.getLabels().get(i)));
    }

    String actual = new ListingTreePrinter().stringify(rootNode);
    Assertions.assertThat(actual).isEqualToIgnoringWhitespace(expected);

    return actual;
  }

  @Test
  void testFunction() throws JSQLParserException, SQLException {
    String sqlStr =
        "SELECT Sum(colBA + colBB) AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"", "Sum", "total"}};
    JdbcResultSetMetaData resultSetMetaData = assertThatResolvesInto(sqlStr, expected);

    String lineage = "SELECT\n" + "   └─total AS Function: Sum(colBA + colBB)\n"
        + "      └─Addition: colBA + colBB\n" + "         ├─c.colBA → b.colBA : Other\n"
        + "         └─c.colBB → b.colBB : Other";
    assertLineage(resultSetMetaData, lineage);
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
  void testFunction2() throws JSQLParserException, SQLException {
    String sqlStr =
        "SELECT Case when Sum(colBA + colBB)=0 then c.col1 else a.col2 end AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";
    String[][] expected = new String[][] {{"", "CaseExpression", "total"}};
    JdbcResultSetMetaData resultSetMetaData = assertThatResolvesInto(sqlStr, expected);

    String lineage = "SELECT\n"
        + " └─total AS CaseExpression: CASE WHEN Sum(colBA + colBB) = 0 THEN c.col1 ELSE a.col2 END\n"
        + "    ├─WhenClause: WHEN Sum(colBA + colBB) = 0 THEN c.col1\n"
        + "    │  ├─EqualsTo: Sum(colBA + colBB) = 0\n"
        + "    │  │  └─Function: Sum(colBA + colBB)\n" + "    │  │     └─Addition: colBA + colBB\n"
        + "    │  │        ├─c.colBA → b.colBA : Other\n"
        + "    │  │        └─c.colBB → b.colBB : Other\n" + "    │  └─c.col1 → b.col1 : Other\n"
        + "    └─a.col2 : Other\n";
    assertLineage(resultSetMetaData, lineage);
  }

  @Test
  void testWithLineage() throws JSQLParserException, SQLException {
    String sqlStr = "WITH c AS (SELECT col1 AS test, colBA FROM b) SELECT * FROM c";
    String[][] expected = new String[][] {{"c", "col1", "col1"}, {"c", "colBA", "colBA"}};
    JdbcResultSetMetaData resultSetMetaData = assertThatResolvesInto(sqlStr, expected);

    String lineage = "SELECT\n" + " ├─c.col1 → b.col1 : Other\n" + " └─c.colBA → b.colBA : Other\n";
    assertLineage(resultSetMetaData, lineage);
  }

  @Test
  void testSubSelectLineage() throws JSQLParserException, SQLException {
    String sqlStr = "SELECT (SELECT col1 AS test FROM b) col2 FROM a";
    String[][] expected = new String[][] {{"b", "col1", "col2"}};
    JdbcResultSetMetaData resultSetMetaData = assertThatResolvesInto(sqlStr, expected);

    String lineage = "SELECT\n" + " └─col2 AS b.col1 : Other\n";
    assertLineage(resultSetMetaData, lineage);
  }

  void lineage() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {
        // Table a
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table b
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};

    String sqlStr =
        "SELECT Sum(colBA + colBB) AS total FROM a INNER JOIN (SELECT * FROM b) c ON a.col1 = c.col1";

    // get the List of JdbcColumns, each holding its lineage using the TreeNode interface
    JdbcResultSetMetaData resultSetMetaData =
        new JSQLColumResolver(schemaDefinition).getResultSetMetaData(sqlStr);


    // Define your own Tree based on your own TreeNode interface
    SimpleTreeNode rootNode = new SimpleTreeNode("SELECT");
    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {

      // Add each columns lineage tree as node to the root
      // JdbcColum implements the Swing's TreeNode interface
      // thus a translation between the different TreeNode interfaces may be needed
      JdbcColumn column = resultSetMetaData.getColumns().get(i);
      String alias = resultSetMetaData.getLabels().get(i);
      rootNode.addChild(translateNode(column, alias));
    }
    new ListingTreePrinter().print(rootNode);
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

  private String assertThatRewritesInto(String sqlStr, String expected)
      throws SQLException, JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3", "colAA", "colAB"},
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};
    return assertThatRewritesInto(schemaDefinition, sqlStr, expected);
  }

  private String assertThatRewritesInto(String[][] schemaDefinition, String sqlStr, String expected)
      throws SQLException, JSQLParserException {
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);
    String actual = resolver.getResolvedStatementText(sqlStr);
    Assertions.assertThat(JSQLTranspilerTest.sanitize(actual))
        .isEqualToIgnoringCase(JSQLTranspilerTest.sanitize(expected));
    return actual;
  }

  private JdbcResultSetMetaData assertThatResolvesInto(String sqlStr, String[][] expectedColumns)
      throws SQLException, JSQLParserException {
    String[][] schemaDefinition = {
        // Table A with Columns col1, col2, col3, colAA, colAB
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table B with Columns col1, col2, col3, colBA, colBB
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};

    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);

    JdbcResultSetMetaData res = resolver.getResultSetMetaData(sqlStr);

    assertThatResolvesInto(res, expectedColumns);

    return res;
  }

  private JdbcResultSetMetaData assertThatResolvesInto(String[][] schemaDefinition, String sqlStr,
      String[][] expectedColumns) throws SQLException, JSQLParserException {
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);

    JdbcResultSetMetaData res = resolver.getResultSetMetaData(sqlStr);

    assertThatResolvesInto(res, expectedColumns);

    return res;
  }

  private void assertThatResolvesInto(ResultSetMetaData res, String[][] expectedColumns)
      throws SQLException {
    Assertions.assertThat(res.getColumnCount()).isEqualTo(expectedColumns.length);
    for (int i = 0; i < expectedColumns.length; i++) {
      // No Label expected
      if (expectedColumns[i].length == 2) {
        Assertions.assertThat(new String[] {res.getTableName(i + 1), res.getColumnName(i + 1)})
            .isEqualTo(expectedColumns[i]);

        // Label is explicitly expected
      } else {
        Assertions.assertThat(new String[] {res.getTableName(i + 1), res.getColumnName(i + 1),
            res.getColumnLabel(i + 1)}).isEqualTo(expectedColumns[i]);
      }
    }
  }
}
