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
package ai.starlake.transpiler.bigquery;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.JSQLTranspilerTest;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BigQueryTranspilerTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/ai/starlake/transpiler/bigquery";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void transpile(File f, int idx, SQLTest t) throws Exception {
    super.transpile(f, idx, t);
  }

  public static Object[][] getQueryResults(String query) throws SQLException {
    try (Statement statement = connDuck.createStatement();
        ResultSet resultSet = statement.executeQuery(query)) {

      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();

      // Use an ArrayList to build rows dynamically
      List<Object[]> data = new ArrayList<>();

      // Add column headers as the first row
      Object[] headers = new Object[columnCount];
      for (int i = 0; i < columnCount; i++) {
        headers[i] = metaData.getColumnLabel(i + 1);
      }
      data.add(headers);

      // Add each row of data
      while (resultSet.next()) {
        Object[] row = new Object[columnCount];
        for (int colIndex = 0; colIndex < columnCount; colIndex++) {
          row[colIndex] = resultSet.getObject(colIndex + 1);
        }
        data.add(row);
      }

      // Convert List to Object[][]
      return data.toArray(new Object[0][]);
    }
  }

  @Test
  void testRegex() {
    String input = "\"Replace\" and \"Repl\"\"\"ace\"";
    String expected = "'Replace' and 'Repl\"\"\"ace'";

    // Pattern to match starting and ending double quotes unless enclosed in double or single quotes
    Pattern pattern =
        Pattern.compile("(?<=^|[^\"'])(\"(?!.*\").*?\"|\".*?(?<![\"'])(\"))(?![\"'])");
    Matcher matcher = pattern.matcher(input);

    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String match = matcher.group();
      String replaced = match.replaceAll("^\"|\"$", "'");
      matcher.appendReplacement(sb, replaced);
    }
    matcher.appendTail(sb);


    Assertions.assertEquals(expected, sb.toString());

  }

  @Test
  void testGeoModeGeometryUsingProperty()
      throws JSQLParserException, InterruptedException, SQLException {
    System.setProperty("GEO_MODE", "GEOMETRY");

    String expected =
        "SELECT ST_Area(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area";
    String actual = JSQLTranspiler.transpileQuery(
        "select ST_Area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;",
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY);

    Assertions.assertEquals(expected, actual);

    Assertions.assertEquals(1.0, getQueryResults(actual)[1][0]);
  }

  @Test
  void testGeoModeGeometryUsingParameterMap()
      throws JSQLParserException, InterruptedException, SQLException {
    String expected =
        "SELECT ST_Area(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area";
    String actual = JSQLTranspiler.transpileQuery(
        "select ST_Area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;",
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, Map.of("GEO_MODE", "GEOMETRY"));

    Assertions.assertEquals(expected, actual);

    Assertions.assertEquals(1.0, getQueryResults(actual)[1][0]);
  }

  @Test
  void testGeoModeGeographyUsingProperty()
      throws JSQLParserException, InterruptedException, SQLException {
    System.setProperty("GEO_MODE", "GEOGRAPHY");

    String expected =
        "SELECT ST_Area_Spheroid(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area";
    String actual = JSQLTranspiler.transpileQuery(
        "select st_area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;",
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY);
    Assertions.assertEquals(expected, actual);
    Assertions.assertEquals(12308778361.469452, getQueryResults(actual)[1][0]);
  }

  @Test
  void testGeoModeGeographyUsingParameterMap()
      throws JSQLParserException, InterruptedException, SQLException {
    String expected =
        "SELECT ST_Area_Spheroid(ST_GEOMFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) AS area";
    String actual = JSQLTranspiler.transpileQuery(
        "select st_area(ST_GEOGFROMTEXT('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))')) as area;",
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, Map.of("GEO_MODE", "GEOGRAPHY"));
    Assertions.assertEquals(expected, actual);
    Assertions.assertEquals(12308778361.469452, getQueryResults(actual)[1][0]);
  }
}
