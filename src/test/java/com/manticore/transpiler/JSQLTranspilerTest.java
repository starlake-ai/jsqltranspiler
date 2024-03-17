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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class JSQLTranspilerTest {
  private final static Logger LOGGER = Logger.getLogger(JSQLTranspilerTest.class.getName());
  private final static String EXTRACTION_PATH =
      System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
  private static Connection connDuck;
  private static boolean isInitialised = false;

  private static final Pattern SQL_COMMENT_PATTERN =
      Pattern.compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

  private static final Pattern SQL_SANITATION_PATTERN =
      Pattern.compile("(\\s+)", Pattern.MULTILINE);

  // Assure SPACE around Syntax Characters
  private static final Pattern SQL_SANITATION_PATTERN2 =
      Pattern.compile("\\s*([!/,()=+\\-*|\\]<>:])\\s*", Pattern.MULTILINE);

  public final static String TEST_FOLDER_STR = "build/resources/test/com/manticore/transpiler";

  public static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".sql");
    }
  };

  static class SQLTest {
    final JSQLTranspiler.Dialect inputDialect;
    final JSQLTranspiler.Dialect outputDialect;

    String providedSqlStr;
    String expectedSqlStr;

    int expectedTally = -1;

    String expectedResult = null;

      SQLTest(JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect) {
          this.inputDialect = inputDialect;
          this.outputDialect = outputDialect;
      }
  }

  static Stream<Map.Entry<File, List<SQLTest>>> getSqlTestMap() {
    return getSqlTestMap(new File(TEST_FOLDER_STR + "/any").listFiles(FILENAME_FILTER), JSQLTranspiler.Dialect.ANY, JSQLTranspiler.Dialect.DUCK_DB );
  }

  static Stream<Map.Entry<File, List<SQLTest>>> getSqlTestMap(File[] testFiles,  JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect) {
    LinkedHashMap<File, List<SQLTest>> sqlMap = new LinkedHashMap<>();
    List<SQLTest> tests = new ArrayList<>();

    for (File file : Objects.requireNonNull(testFiles)) {
      boolean startContent = false;
      boolean endContent;

      StringBuilder stringBuilder = new StringBuilder();
      String line;
      String k = "";
      int j = 0;

      try (FileReader fileReader = new FileReader(file);
          BufferedReader bufferedReader = new BufferedReader(fileReader)) {
        SQLTest test = new SQLTest(inputDialect, outputDialect);
        while ((line = bufferedReader.readLine()) != null) {
          if (!startContent && line.startsWith("--") && !line.startsWith("-- @")) {
            k = line.substring(3).trim().toLowerCase();
          }

          if (line.toLowerCase().startsWith("-- provided")) {
            if (test.providedSqlStr!=null && test.expectedSqlStr!=null && (test.expectedTally>=0 || test.expectedResult!=null)) {
              LOGGER.info("Found multiple test descriptions in " + file.getName());
              tests.add(test);

              test = new SQLTest(inputDialect, outputDialect);
            }
          }

          startContent = startContent
              || (!line.startsWith("--") || line.startsWith("-- @")) && !line.trim().isEmpty();
          endContent = startContent && !line.startsWith("--")
              && (line.trim().endsWith(";")
                  || (k.equalsIgnoreCase("count") || k.equalsIgnoreCase("tally")) && line.isEmpty()
                  || k.startsWith("result") && line.isEmpty());

          if (startContent) {
            stringBuilder.append(line).append("\n");
          }

          if (endContent) {
            if (k.equalsIgnoreCase("provided")) {
              test.providedSqlStr = sanitize(stringBuilder.toString());
            } else if (k.equalsIgnoreCase("expected")) {
              test.expectedSqlStr = sanitize(stringBuilder.toString());
            } else if (k.equalsIgnoreCase("count") || k.equalsIgnoreCase("tally")) {
              test.expectedTally = Integer.parseInt(stringBuilder.toString().trim());
            } else if (k.startsWith("result")) {
              test.expectedResult = stringBuilder.toString();
            }
            stringBuilder.setLength(0);
            startContent = false;
          }

        }
        tests.add(test);

        sqlMap.put(file, tests);
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, "Failed to read " + file.getAbsolutePath(), ex);
      }
    }

    return sqlMap.entrySet().stream();
  }

  @BeforeAll
  // setting up a TEST Database according to
  // https://docs.aws.amazon.com/redshift/latest/dg/c_sampledb.html
  static void init() throws SQLException, IOException, JSQLParserException {
    File extractionPathFolder = new File(EXTRACTION_PATH);
    boolean mkdirs = extractionPathFolder.mkdirs();

    // Currently, Duck DB Home resolution in Java seems broken
    File fileDuckDB =
        new File(EXTRACTION_PATH, JSQLTranspilerTest.class.getSimpleName() + ".duckdb");
    connDuck = DriverManager.getConnection("jdbc:duckdb:" + fileDuckDB.getAbsolutePath());

    if (!isInitialised) {
      String sqlStr = IOUtils.resourceToString(
          JSQLTranspilerTest.class.getCanonicalName().replaceAll("\\.", "/") + "_DDL.sql",
          Charset.defaultCharset(), JSQLTranspilerTest.class.getClassLoader());
      Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);

      LOGGER.info("Create the DuckDB Table with Indices");
      try (Statement st = connDuck.createStatement()) {
        for (net.sf.jsqlparser.statement.Statement statement : statements) {
          LOGGER.fine("execute: " + statement.toString());
          st.execute(statement.toString());
        }
      }

      LOGGER.info("Download Amazon Redshift `TickitDB` example");
      // Download the ZIP file
      URL url = new URL("https://docs.aws.amazon.com/redshift/latest/gsg/samples/tickitdb.zip");
      try (InputStream in = url.openStream()) {
        // Extract the ZIP file
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in)) {
          ZipArchiveEntry entry;
          while ((entry = zipIn.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".txt")) {
              // Extract the text file
              File outputFile = new File(extractionPathFolder, entry.getName());
              try (OutputStream out = new FileOutputStream(outputFile)) {
                IOUtils.copy(zipIn, out);
              }

              // remove some silly '\N' entries since
              // @todo: find a better way to clean those CSVs
              try {
                ProcessBuilder processBuilder =
                    new ProcessBuilder("sed", "-i", "s/\\\\N//g", outputFile.getAbsolutePath());
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }

              // @todo: find a better way to map the file names to the actual table names
              String tableName = entry.getName().replace(".txt", "").replace("_pipe", "")
                  .replace("_tab", "").replace("all", "").replace("2008", "");

              if (tableName.equals("events")) {
                tableName = "event";
              }
              if (tableName.equals("listings")) {
                tableName = "listing";
              }

              // Execute the copyCommand with DuckDB
              String fileName = EXTRACTION_PATH + File.separator + entry.getName();
              String copyCommand = entry.getName().contains("tab")
                  ? "COPY " + tableName + " FROM '" + fileName
                      + "' WITH(timestampformat '%m/%d/%Y %I:%M:%S', ignore_errors 'true');"
                  : "COPY " + tableName + " FROM '" + fileName + "';";

              try (Statement st = connDuck.createStatement()) {
                LOGGER.fine("execute: " + copyCommand);
                st.execute(copyCommand);
              }

              File f = new File(fileName);
              if (f.exists()) {
                boolean delete = f.delete();
              }
            }
          }
        }
      }

      LOGGER.info("Finished preparation.");
      isInitialised = true;
    }
  }

  @AfterAll
  static void shutdown() throws IOException {
    // delete the DuckDB
    // @todo: maybe we should delete only when all the tests have ran successfully?
    Path folderPath = Paths.get(EXTRACTION_PATH);
    Files.walk(folderPath).sorted((p1, p2) -> -p1.compareTo(p2)).forEach(p -> {
      try {
        Files.delete(p);
      } catch (IOException ex) {
        LOGGER.log(Level.WARNING, "Failed to delete " + p, ex);
      }
    });
  }

  public static String sanitize(final String originalSql) {
    return sanitize(originalSql, true);
  }

  public static String sanitize(final String originalSql, boolean laxDeparsingCheck) {
    if (laxDeparsingCheck) {
      // remove comments
      String sanitizedSqlStr = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");

      // redundant white space
      sanitizedSqlStr = SQL_SANITATION_PATTERN.matcher(sanitizedSqlStr).replaceAll(" ");

      // assure spacing around Syntax Characters
      sanitizedSqlStr = SQL_SANITATION_PATTERN2.matcher(sanitizedSqlStr).replaceAll("$1");

      sanitizedSqlStr = sanitizedSqlStr.trim().toLowerCase();

      // Rewrite statement separators "/" and "GO"
      if (sanitizedSqlStr.endsWith("/") || sanitizedSqlStr.endsWith(";")) {
        sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 1).trim();
      } else if (sanitizedSqlStr.endsWith("go")) {
        sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 2).trim();
      }

      return sanitizedSqlStr;

    } else {
      // remove comments only
      return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
    }
  }

  @ParameterizedTest(name = "{index} {0}: {1}")
  @MethodSource("getSqlTestMap")
  void transpile(Map.Entry<File, List<SQLTest>> entry) throws Exception {
    for (SQLTest t: entry.getValue()) {
      // Expect this query to fail since DuckDB does not support `TOP <integer>`
      Assertions.assertThrows(java.sql.SQLException.class, new Executable() {
        @Override
        public void execute() throws Throwable {
          try (Statement st = connDuck.createStatement();
               ResultSet rs = st.executeQuery(t.providedSqlStr)) {
            rs.next();
          }
        }
      });

      // Expect a rewritten query
      String expectedSqlStr = sanitize("select qtysold, sellerid\n" + "from sales\n"
              + "order by qtysold desc, sellerid\n" + "limit 10");

      String transpiledSqlStr =
              JSQLTranspiler.transpileQuery(t.providedSqlStr, t.inputDialect);
      Assertions.assertEquals(t.expectedSqlStr, sanitize(transpiledSqlStr));

      // Expect this transpiled query to succeed since DuckDB does not support `TOP <integer>`
      if (t.expectedTally >= 0) {
        int i = 0;
        try (Statement st = connDuck.createStatement();
             ResultSet rs = st.executeQuery(transpiledSqlStr);) {
          while (rs.next()) {
            i++;
          }
        }
        // Expect 10 records
        Assertions.assertEquals(t.expectedTally, i);
      }


      if (t.expectedResult!=null && !t.expectedResult.isEmpty()) {
        // Compare output
        try (Statement st = connDuck.createStatement();
             ResultSet rs = st.executeQuery(transpiledSqlStr);
             StringWriter stringWriter = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(stringWriter)) {
          csvWriter.writeAll(rs, true, true, true);
          csvWriter.flush();
          stringWriter.flush();
          Assertions.assertEquals(t.expectedResult, stringWriter.toString().trim());
        }
      }
    }
  }
}
