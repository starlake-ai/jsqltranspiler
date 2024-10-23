/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
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

import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.snowflake.AsciiTreeBuilder;
import com.opencsv.CSVWriter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class AbstractColumnResolverTest extends JSQLTranspilerTest {

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
  void resolve(File f, int idx, SQLTest t) throws Exception {
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

    ResultSetMetaData resultSetMetaData =
        JSQLColumResolver.getResultSetMetaData(t.providedSqlStr, metaData);

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

  static String assertLineage(String[][] schemaDefinition, String sqlStr, String expected)
      throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, JSQLParserException {

    JSQLColumResolver resolver = new JSQLColumResolver(new JdbcMetaData(schemaDefinition));
    String actual = resolver.getLineage(AsciiTreeBuilder.class, sqlStr);
    Assertions.assertThat(actual).isEqualToIgnoringWhitespace(expected);

    return actual;
  }

  static String assertLineage(JdbcMetaData metaData, String sqlStr, String expected)
      throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, JSQLParserException {

    JSQLColumResolver resolver = new JSQLColumResolver(metaData);
    String actual = resolver.getLineage(AsciiTreeBuilder.class, sqlStr);
    Assertions.assertThat(actual).isEqualToIgnoringWhitespace(expected);

    return actual;
  }

  static String assertLineage(String sqlStr, String expected)
      throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, JSQLParserException {

    //@formatter:off
    String[][] schemaDefinition = {
        // Table A with Columns col1, col2, col3, colAA, colAB
        {"a", "col1", "col2", "col3", "colAA", "colAB"},

        // Table B with Columns col1, col2, col3, colBA, colBB
        {"b", "col1", "col2", "col3", "colBA", "colBB"}
    };
    //@formatter:on
    JdbcMetaData metaData = new JdbcMetaData(schemaDefinition);

    return assertLineage(metaData, sqlStr, expected);
  }

  String assertThatRewritesInto(String sqlStr, String expected)
      throws SQLException, JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3", "colAA", "colAB"},
        {"b", "col1", "col2", "col3", "colBA", "colBB"}};
    return assertThatRewritesInto(schemaDefinition, sqlStr, expected);
  }

  String assertThatRewritesInto(String[][] schemaDefinition, String sqlStr, String expected)
      throws SQLException, JSQLParserException {
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);
    String actual = resolver.getResolvedStatementText(sqlStr);
    Assertions.assertThat(sanitize(actual)).isEqualToIgnoringCase(sanitize(expected));
    return actual;
  }

  JdbcResultSetMetaData assertThatResolvesInto(String sqlStr, String[][] expectedColumns)
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

  JdbcResultSetMetaData assertThatResolvesInto(String[][] schemaDefinition, String sqlStr,
      String[][] expectedColumns) throws SQLException, JSQLParserException {
    JSQLColumResolver resolver = new JSQLColumResolver(schemaDefinition);

    JdbcResultSetMetaData res = resolver.getResultSetMetaData(sqlStr);

    assertThatResolvesInto(res, expectedColumns);

    return res;
  }

  void assertThatResolvesInto(ResultSetMetaData res, String[][] expectedColumns)
      throws SQLException {
    Assertions.assertThat(res.getColumnCount()).isEqualTo(expectedColumns.length);
    for (int i = 0; i < res.getColumnCount(); i++) {
      // No Label expected
      if (expectedColumns[i].length == 2) {
        Assertions.assertThat(new String[] {res.getTableName(i + 1), res.getColumnName(i + 1)})
            .isEqualTo(expectedColumns[i]);

        // catalog, schema, table, column, label
      } else if (expectedColumns[i].length == 5) {
        Assertions
            .assertThat(new String[] {res.getCatalogName(i + 1), res.getSchemaName(i + 1),
                res.getTableName(i + 1), res.getColumnName(i + 1), res.getColumnLabel(i + 1)})
            .isEqualTo(expectedColumns[i]);

        // Label is explicitly expected
      } else {
        Assertions.assertThat(new String[] {res.getTableName(i + 1), res.getColumnName(i + 1),
            res.getColumnLabel(i + 1)}).isEqualTo(expectedColumns[i]);
      }
    }
  }
}
