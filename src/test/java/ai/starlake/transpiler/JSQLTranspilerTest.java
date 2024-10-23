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
import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLTranspilerTest {
  final static Logger LOGGER = Logger.getLogger(JSQLTranspilerTest.class.getName());
  private final static String EXTRACTION_PATH =
      System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
  public static Connection connDuck;
  public static JdbcMetaData metaData;
  private static boolean isInitialised = false;

  private static final Pattern SQL_COMMENT_PATTERN =
      Pattern.compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

  private static final Pattern SQL_SANITATION_PATTERN =
      Pattern.compile("(\\s+)", Pattern.MULTILINE);

  // Assure SPACE around Syntax Characters
  private static final Pattern SQL_SANITATION_PATTERN2 =
      Pattern.compile("\\s*([!/,()=+\\-*|{}\\[\\]<>:])\\s*", Pattern.MULTILINE);

  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/any";

  public static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      String filename = name.toLowerCase().trim();
      return filename.endsWith(".sql") && !filename.startsWith("disabled");
    }
  };

  public static class SQLTest {
    final JSQLTranspiler.Dialect inputDialect;
    final JSQLTranspiler.Dialect outputDialect;

    public int line = 0;
    public String prologue = null;
    public String epilogue = null;
    public String providedSqlStr;
    public String expectedSqlStr;

    public int expectedTally = -1;

    public String expectedResult = null;

    SQLTest(JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect) {
      this.inputDialect = inputDialect;
      this.outputDialect = outputDialect;
    }

    @Override
    public String toString() {
      return providedSqlStr;
    }
  }

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.ANY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  protected static Stream<Arguments> unrollParameterMap(Map<File, List<SQLTest>> map) {
    Set<Map.Entry<File, List<SQLTest>>> entries = map.entrySet();

    ArrayList<Object[]> data = new ArrayList<>();
    for (Map.Entry<File, List<SQLTest>> e : entries) {
      for (SQLTest t : e.getValue()) {
        data.add(new Object[] {e.getKey(), t.line, t});
      }
    }

    // Create a Stream<Arguments> from the array
    return Arrays.stream(data.toArray(new Object[0][]))
        .map(row -> Arguments.of(row[0], row[1], row[2]));
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  protected static Map<File, List<SQLTest>> getSqlTestMap(File[] testFiles,
      JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect) {
    LinkedHashMap<File, List<SQLTest>> sqlMap = new LinkedHashMap<>();

    if (testFiles == null) {
      return sqlMap;
    }

    for (File file : Objects.requireNonNull(testFiles)) {
      List<SQLTest> tests = new ArrayList<>();

      StringBuilder stringBuilder = new StringBuilder();
      String line;
      String token = "";
      String previousToken = null;

      try (FileReader fileReader = new FileReader(file);
          BufferedReader bufferedReader = new BufferedReader(fileReader)) {
        SQLTest test = new SQLTest(inputDialect, outputDialect);
        int r = 0;
        while ((line = bufferedReader.readLine()) != null) {
          r++;
          String trimmedLine = line.toLowerCase().replaceAll("\\s", "");

          if (line.startsWith("--") && !line.startsWith("--@")) {
            previousToken = token;
            token = trimmedLine.substring(2).trim().toLowerCase();

            if (token.startsWith("prolog") || token.startsWith("provid")
                || token.startsWith("expect") || token.startsWith("count")
                || token.startsWith("tally") || token.startsWith("result")
                || token.startsWith("return") || token.startsWith("epilog")) {

              if (previousToken.startsWith("prolog")) {
                test.prologue = stringBuilder.toString();
              } else if (previousToken.startsWith("provid")) {
                test.providedSqlStr = stringBuilder.toString();
              } else if (previousToken.startsWith("expect")) {
                test.expectedSqlStr = stringBuilder.toString();
              } else if (previousToken.startsWith("count") || token.startsWith("tally")) {
                test.expectedTally = Integer.parseInt(stringBuilder.toString().trim());
              } else if (previousToken.startsWith("result") || token.startsWith("return")) {
                test.expectedResult = stringBuilder.toString().trim();
              } else if (previousToken.startsWith("epilog")) {
                test.epilogue = stringBuilder.toString();
              }

              if ((token.startsWith("prolog") || token.startsWith("provid"))
                  && test.providedSqlStr != null
                  && (test.expectedTally >= 0 || test.expectedResult != null)) {

                LOGGER.fine("Found multiple test descriptions in " + file.getName());
                // pass through tests not depending on transpiling
                if (test.expectedSqlStr == null || test.expectedSqlStr.isEmpty()) {
                  test.expectedSqlStr = test.providedSqlStr;
                }
                tests.add(test);

                test = new SQLTest(inputDialect, outputDialect);
                test.line = r;
              }

              stringBuilder.setLength(0);
              continue;
            }
          }

          if (!line.isEmpty()) {
            stringBuilder.append(line).append("\n");
          }
        }

        // pass through tests not depending on transpiling
        if (token.startsWith("prolog")) {
          test.prologue = stringBuilder.toString();
        } else if (token.startsWith("provid")) {
          test.providedSqlStr = stringBuilder.toString();
        } else if (token.startsWith("expect")) {
          test.expectedSqlStr = stringBuilder.toString();
        } else if (token.startsWith("count") || token.startsWith("tally")) {
          test.expectedTally = Integer.parseInt(stringBuilder.toString().trim());
        } else if (token.startsWith("result") || token.startsWith("return")) {
          test.expectedResult = stringBuilder.toString().trim();
        } else if (token.startsWith("epilog")) {
          test.epilogue = stringBuilder.toString();
        }

        if (test.expectedSqlStr == null || test.expectedSqlStr.isEmpty()) {
          test.expectedSqlStr = test.providedSqlStr;
        }
        tests.add(test);

        sqlMap.put(file, tests);
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, "Failed to read " + file.getAbsolutePath(), ex);
      }
    }

    return sqlMap;
  }

  @BeforeAll
  // setting up a TEST Database according to
  // https://docs.aws.amazon.com/redshift/latest/dg/c_sampledb.html
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  static synchronized void init()
      throws SQLException, IOException, JSQLParserException, InterruptedException {
    File extractionPathFolder = new File(EXTRACTION_PATH);
    extractionPathFolder.mkdirs();

    // Currently, Duck DB Home resolution in Java seems broken
    File fileDuckDB =
        new File(EXTRACTION_PATH, JSQLTranspilerTest.class.getSimpleName() + ".duckdb");
    Properties info = new Properties();
    info.put("old_implicit_casting", "true");
    info.put("default_null_order", "NULLS FIRST");
    info.put("default_order", "ASC");
    info.put("memory_limit", "250M");
    connDuck = DriverManager.getConnection("jdbc:duckdb:" + fileDuckDB.getAbsolutePath(), info);

    if (!isInitialised) {
      JSQLTranspiler.createMacros(connDuck);

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
      // Download the ZIP file via Gradle Download task in order to enable caching
      // URL url = new URL("https://docs.aws.amazon.com/redshift/latest/gsg/samples/tickitdb.zip");
      URL url = JSQLTranspilerTest.class.getClassLoader()
          .getResource("ai/starlake/transpiler/tickitdb.zip");
      assert url != null;
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
              try {
                List<String> lines =
                    Files.readAllLines(outputFile.toPath(), StandardCharsets.UTF_8);
                lines.replaceAll(s -> s.replace("\\N", ""));
                Files.write(outputFile.toPath(), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
              } catch (IOException e) {
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
              String copyCommand = entry.getName().contains("tab") ? "COPY " + tableName + " FROM '"
                  + fileName
                  + "' (FORMAT CSV, AUTO_DETECT true, DELIMITER '\t', TIMESTAMPFORMAT '%m/%d/%Y %I:%M:%S', IGNORE_ERRORS true);"
                  : "COPY " + tableName + " FROM '" + fileName + "';";

              try (Statement st = connDuck.createStatement()) {
                LOGGER.fine("execute: " + copyCommand);
                st.execute(copyCommand);
              }

              File f = new File(fileName);
              if (f.exists()) {
                f.delete();
              }
            }
          }
        }
      }

      // used by the Snowflake examples
      // see https://duckdb.org/docs/extensions/tpch
      LOGGER.info("Preparing TPCH with load factor 0.2");
      try (Statement st = connDuck.createStatement()) {
        for (String s : new String[] {"INSTALL tpch;", "LOAD tpch;", "CALL dbgen(sf = 0.2);"}) {
          LOGGER.fine("execute: " + s);
          st.execute(s);
        }
      }

      LOGGER.info("Preparing JSON Extension");
      try (Statement st = connDuck.createStatement()) {
        for (String s : new String[] {"INSTALL json;", "LOAD json;"}) {
          LOGGER.fine("execute: " + s);
          st.execute(s);
        }
      } catch (Exception ex) {
        LOGGER.log(Level.FINE, "Failed to INSTALL/LOAD the JSON extension", ex);
      }

      LOGGER.info("Preparing Spatial Extension");
      try (Statement st = connDuck.createStatement()) {
        for (String s : new String[] {"INSTALL spatial;", "LOAD spatial;"}) {
          LOGGER.fine("execute: " + s);
          st.execute(s);
        }
      } catch (Exception ex) {
        LOGGER.log(Level.FINE, "Failed to INSTALL/LOAD the SPATIAL extension", ex);
      }

      LOGGER.info("Fetching the MetaData");
      metaData = new JdbcMetaData(connDuck);

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

  private static String upperCaseExceptEnclosed(String input) {
    StringBuilder result = new StringBuilder();
    boolean inQuotes = false;
    boolean escaped = false;

    for (char c : input.toCharArray()) {
      if (escaped) {
        result.append(Character.toUpperCase(c));
        escaped = false;
      } else if (c == '\\') {
        result.append(c);
        escaped = true;
      } else if (c == '\'') {
        inQuotes = !inQuotes;
        result.append(c);
      } else {
        result.append(inQuotes ? c : Character.toUpperCase(c));
      }
    }

    return result.toString();
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

      sanitizedSqlStr = sanitizedSqlStr.trim();
      sanitizedSqlStr = upperCaseExceptEnclosed(sanitizedSqlStr);

      // Rewrite statement separators "/" and "GO"
      if (sanitizedSqlStr.endsWith("/") || sanitizedSqlStr.endsWith(";")) {
        sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 1).trim();
      }
      // @todo: "SELECT 1 AS ago" would trigger this wrongly, so we need to do this more
      // in a more sophisticated way
      // else if (sanitizedSqlStr.endsWith("go")) {
      // sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 2).trim();
      // }

      return sanitizedSqlStr;

    } else {
      // remove comments only
      return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
    }
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void transpile(File f, int idx, SQLTest t) throws Exception {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // ONLY if expected differs from provided:
    // Expect this query to fail since DuckDB does not support `TOP <integer>`
    // if (!t.expectedSqlStr.equals(t.providedSqlStr)) {
    // Assertions.assertThrows(java.sql.SQLException.class, new Executable() {
    // @Override
    // public void execute() throws Throwable {
    // try (Statement st = connDuck.createStatement();
    // ResultSet rs = st.executeQuery(t.providedSqlStr)) {
    // rs.next();
    // }
    // }
    // });
    // }

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

    // Assertions.assertNotNull(t.expectedSqlStr);
    String transpiledSqlStr = JSQLTranspiler.transpileQuery(t.providedSqlStr, t.inputDialect,
        Collections.emptyMap(), executorService, parser -> {
        });
    Assertions.assertThat(transpiledSqlStr).isNotNull();
    Assertions.assertThat(sanitize(transpiledSqlStr, true))
        .isEqualTo(sanitize(t.expectedSqlStr, true));

    executeTest(connDuck, t, transpiledSqlStr);
  }

  public static void executeTest(Connection connDuck, SQLTest t, String transpiledSqlStr)
      throws SQLException, IOException, JSQLParserException {
    // Expect this transpiled query to succeed since DuckDB does not support `TOP <integer>`
    if (t.expectedTally >= 0) {
      int i = 0;
      try (Statement st = connDuck.createStatement()) {
        st.executeUpdate("set timezone='Asia/Bangkok'");

        if (t.inputDialect == JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY) {
          st.executeUpdate("set default_null_order='NULLS FIRST'");
          st.executeUpdate("set default_order='ASC'");
        }

        try (ResultSet rs = st.executeQuery(transpiledSqlStr);) {
          while (rs.next()) {
            i++;
          }
        }
      }
      Assertions.assertThat(i).isEqualTo(t.expectedTally);
    }

    DecimalFormat floatingPointFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
    floatingPointFormat.setGroupingUsed(false);
    floatingPointFormat.setMaximumFractionDigits(9);
    floatingPointFormat.setMinimumFractionDigits(1);
    floatingPointFormat.setMinimumIntegerDigits(1);

    if (t.expectedResult != null && !t.expectedResult.isEmpty()) {
      // Compare output
      try (Statement st = connDuck.createStatement();) {
        st.executeUpdate("set timezone='Asia/Bangkok'");

        try (ResultSet rs = st.executeQuery(transpiledSqlStr);
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);) {

          // enforce SQL compliant format
          ResultSetHelperService resultSetHelperService = new JSQLResultSetHelperService();
          resultSetHelperService.setDateFormat("yyyy-MM-dd");
          resultSetHelperService.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.S");
          resultSetHelperService.setFloatingPointFormat(floatingPointFormat);
          csvWriter.setResultService(resultSetHelperService);

          csvWriter.writeAll(rs, true, false, true);
          Assertions.assertThat(stringWriter.toString().trim())
              .isEqualToIgnoringCase(t.expectedResult);
        }
      }
    }

    if (t.epilogue != null && !t.epilogue.isEmpty()) {
      try (Statement st = connDuck.createStatement();) {
        for (net.sf.jsqlparser.statement.Statement statement : CCJSqlParserUtil
            .parseStatements(t.epilogue)) {
          st.executeUpdate(statement.toString());
        }
      }
    }
  }
}
