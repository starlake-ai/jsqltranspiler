package ai.starlake.transpiler;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Stream;

public abstract class SQLGlotTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/ai/starlake/transpiler/any";

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
