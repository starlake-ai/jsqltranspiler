/**
 * Manticore Projects JSQLTranspiler is a multiple SQL Dialect to DuckDB Translation Software.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manticore.transpiler;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Stream;

@Disabled
public class GoogleBigQuerySQLGlotTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/com/manticore/transpiler/google_bigquery";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  void transpile(File f, int idx, SQLTest t) throws Exception {

    StringBuilder script = new StringBuilder();
    script.append("#!/usr/bin/python\n\n");
    script.append("import sqlglot\n");
    script.append("print(sqlglot.transpile(\"");
    script.append(t.providedSqlStr.replaceAll("(\\s\\s+)|\\n+", " "));
    script.append("\", read=\"bigquery\", write=\"duckdb\")[0])\n");

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
      try (Statement st = connDuck.createStatement();
          ResultSet rs = st.executeQuery(output.toString());) {
        while (rs.next()) {
          i++;
        }
      }
      // Expect 10 records
      // Assertions.assertEquals(t.expectedTally, i);
      Assertions.assertThat(i).isEqualTo(t.expectedTally).as("Returned records do not tally.");
    }


    if (t.expectedResult != null && !t.expectedResult.isEmpty()) {
      // Compare output
      try (Statement st = connDuck.createStatement();
          ResultSet rs = st.executeQuery(output.toString());
          StringWriter stringWriter = new StringWriter();
          CSVWriter csvWriter = new CSVWriter(stringWriter)) {

        // enforce SQL compliant format
        ResultSetHelperService resultSetHelperService = new ResultSetHelperService();
        resultSetHelperService.setDateFormat("yyyy-MM-dd");
        resultSetHelperService.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        csvWriter.setResultService(resultSetHelperService);

        csvWriter.writeAll(rs, true, true, true);
        csvWriter.flush();
        stringWriter.flush();
        Assertions.assertThat(stringWriter.toString().trim())
            .isEqualToIgnoringCase(t.expectedResult);
      }
    }
  }

}
