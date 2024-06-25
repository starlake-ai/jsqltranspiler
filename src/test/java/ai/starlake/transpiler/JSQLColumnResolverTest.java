package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import com.opencsv.CSVWriter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.StringWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

  private ResultSetMetaData assertThatResolvesInto(String sqlStr, String[][] expectedColumns)
      throws SQLException, JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3", "colAA", "colAB"},
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};

    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);

    ResultSetMetaData res = resolver.getResultSetMetaData(sqlStr);

    assertThatResolvesInto(res, expectedColumns);

    return res;
  }

  private ResultSetMetaData assertThatResolvesInto(String[][] schemaDefinition, String sqlStr,
      String[][] expectedColumns) throws SQLException, JSQLParserException {
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);

    ResultSetMetaData res = resolver.getResultSetMetaData(sqlStr);

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
