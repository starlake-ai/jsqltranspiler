/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI <hayssam.saleh@starlake.ai>
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

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

// The purpose of this facility is to run Pipe
public class PipedTest extends JSQLTranspilerTest {
  public final static String INPUT_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/piped/generation_input";
  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/piped/generated";
  public final static String GENERATED_TEST_FOLDER_STR =
      "src/test/resources/ai/starlake/transpiler/piped/generated";

  public static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return !name.toLowerCase().contains("disabled") && name.toLowerCase().contains("piped")
          && name.toLowerCase().endsWith(".sql");
    }
  };

  static Stream<Arguments> getInputs() {
    return getInputQueries(new File(INPUT_FOLDER_STR), FILENAME_FILTER);
  }

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void transpile(File f, int idx, SQLTest t) throws Exception {
    super.transpile(f, idx, t);
  }

  @ParameterizedTest(name = "{index} {0} {1} {2}: {3}")
  @MethodSource("getInputs")
  @Disabled
  protected void generate(File f, int idx, boolean supported, String inputQuery)
      throws IOException {
    generateTestCase(JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY,
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, inputQuery,
        Path.of(GENERATED_TEST_FOLDER_STR, f.getName()).toString(), idx, supported);
  }

  @Test
  void testTransparentJDBCQuery() throws SQLException, JSQLParserException, InterruptedException {
    //@formatter:off
    String sql =
              "(\n"
            + "  SELECT '000123' AS id, 'apples' AS item, 2 AS sales\n"
            + "  UNION ALL\n"
            + "  SELECT '000456' AS id, 'bananas' AS item, 5 AS sales\n"
            + ") AS sales_table\n"
            + "|> AGGREGATE SUM(sales) AS total_sales GROUP BY id, item\n"
            + "|> AS t1\n"
            + "|> JOIN (SELECT 456 AS id, 'yellow' AS color) AS t2\n"
            + "   ON CAST(t1.id AS INT64) = t2.id\n"
            + "|> SELECT t2.id, total_sales, color;";
    //@formatter:on

    try (Statement st = connDuck.createStatement();
        ResultSet rs =
            st.executeQuery(JSQLTranspiler.transpileQuery(sql, JSQLTranspiler.Dialect.ANY));) {
      ResultSetMetaData resultSetMetaData = rs.getMetaData();
      Assertions.assertEquals(3, resultSetMetaData.getColumnCount());
      Assertions.assertEquals("id", resultSetMetaData.getColumnLabel(1));
      Assertions.assertEquals("total_sales", resultSetMetaData.getColumnLabel(2));
      Assertions.assertEquals("color", resultSetMetaData.getColumnLabel(3));

      if (rs.next()) {
        Assertions.assertEquals(456, rs.getInt(1));
        Assertions.assertEquals(5, rs.getInt(2));
        Assertions.assertEquals("yellow", rs.getString(3));
      }
    }
  }
}
