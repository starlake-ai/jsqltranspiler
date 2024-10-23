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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
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
}
