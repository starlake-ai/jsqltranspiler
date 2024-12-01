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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Stream;

public abstract class SQLGlotTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/any";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.ANY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  protected void transpile(File f, int idx, SQLTest t, String dialect) throws Exception {

    StringBuilder script = new StringBuilder();
    script.append("#!/usr/bin/python\n\n");
    script.append("import sqlglot\n");
    script.append("print(sqlglot.transpile(\"");
    script.append(t.providedSqlStr.replaceAll("(\\s\\s+)|\\n+", " "));
    script.append("\", read=\"").append(dialect).append("\", write=\"duckdb\")[0])\n");

    File pythonFile = File.createTempFile("sqlglot_", ".py");
    pythonFile.deleteOnExit();
    try (FileWriter writer = new FileWriter(pythonFile)) {
      writer.write(script.toString());
    }

    StringBuilder output = new StringBuilder();
    // transpile via extern python/sqlglot
    // Build the command to run Python
    ProcessBuilder pb = new ProcessBuilder("python", pythonFile.getAbsolutePath());
    pb.redirectErrorStream(true); // Redirect error stream to the input stream

    // Start the process
    Process process = pb.start();

    // Read the output
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      output.append(line).append("\n");
    }

    // Wait for the process to complete
    int exitCode = process.waitFor();
    Assertions.assertThat(exitCode).isEqualTo(0).as("Failed to run the SQLGlot python script.");

    // Expect this transpiled query to succeed since DuckDB does not support `TOP <integer>`
    if (t.expectedTally >= 0) {
      int i = 0;
      try (Statement st = connDuck.createStatement();) {
        st.executeUpdate("set timezone='Asia/Bangkok'");

        try (ResultSet rs = st.executeQuery(output.toString());) {
          while (rs.next()) {
            i++;
          }
        }
      }
      Assertions.assertThat(i).isEqualTo(t.expectedTally).as("Returned records do not tally.");
    }

    // For any JSON related test we want to distinguish the SQL NULL, while for anything else it
    // does not matter
    executeTest(connDuck, t, output.toString(),
        f.getName().toLowerCase().contains("json") ? "JSQL_NULL" : "");
  }

}
