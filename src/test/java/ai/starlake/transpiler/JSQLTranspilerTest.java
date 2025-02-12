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

import ai.starlake.transpiler.bigquery.BigqueryResultSet;
import ai.starlake.transpiler.schema.JdbcMetaData;
import com.google.cloud.bigquery.*;
import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.net.URISyntaxException;
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
import java.util.HashMap;
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
      return filename.endsWith(".sql") && !filename.contains("disabled");
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

    public Map<String, Object> parameters = new HashMap<>();

    SQLTest(JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect,
        Map<String, Object> parameters) {
      this.inputDialect = inputDialect;
      this.outputDialect = outputDialect;
      this.parameters.putAll(parameters);
    }

    @Override
    public String toString() {
      return providedSqlStr;
    }
  }

  protected static Stream<Arguments> getInputQueries(File inputFile, FilenameFilter filenameFilter) {
    return Arrays.stream(Objects.requireNonNull(inputFile.listFiles(filenameFilter))).flatMap(sqlFile -> {
      try {
        return Stream.generate(new InputQuerySupplier(sqlFile)).takeWhile(Objects::nonNull);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    });
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

        JSQLExpressionTranspiler.GeoMode geoMode =
            file.getName().toLowerCase().contains("geography")
                ? JSQLExpressionTranspiler.GeoMode.GEOGRAPHY
                : JSQLExpressionTranspiler.GeoMode.GEOMETRY;

        SQLTest test = new SQLTest(inputDialect, outputDialect, Map.of("GEO_MODE", geoMode.name()));
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
                || token.startsWith("return") || token.startsWith("epilog")
                || token.startsWith("output")) {

              if (previousToken.startsWith("prolog")) {
                test.prologue = stringBuilder.toString();
              } else if (previousToken.startsWith("provid")) {
                test.providedSqlStr = stringBuilder.toString();
              } else if (previousToken.startsWith("output")) {
                //
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

                geoMode = file.getName().toLowerCase().contains("geography")
                    ? JSQLExpressionTranspiler.GeoMode.GEOGRAPHY
                    : JSQLExpressionTranspiler.GeoMode.GEOMETRY;

                test = new SQLTest(inputDialect, outputDialect, Map.of("GEO_MODE", geoMode.name()));
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
        } else if (token.startsWith("output")) {
          // ignore
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

      LOGGER.info("Preparing H3 Community Extension");
      try (Statement st = connDuck.createStatement()) {
        for (String s : new String[] {"INSTALL h3 FROM community;", "LOAD h3;"}) {
          LOGGER.fine("execute: " + s);
          st.execute(s);
        }
      } catch (Exception ex) {
        LOGGER.log(Level.FINE, "Failed to INSTALL/LOAD the SPATIAL extension", ex);
      }

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
            String fileName = EXTRACTION_PATH + File.separator + entry.getName();
            if (!entry.isDirectory() && entry.getName().endsWith(".txt")) {
              // Extract the text file
              File outputFile = new File(extractionPathFolder, entry.getName());
              try (OutputStream out = new FileOutputStream(outputFile)) {
                IOUtils.copy(zipIn, out);

                // remove some silly '\N' entries since
                List<String> lines =
                    Files.readAllLines(outputFile.toPath(), StandardCharsets.UTF_8);
                lines.replaceAll(s -> s.replace("\\N", ""));
                Files.write(outputFile.toPath(), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);

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
                String copyCommand = entry.getName().contains("tab") ? "COPY " + tableName
                    + " FROM '" + fileName
                    + "' (FORMAT CSV, AUTO_DETECT true, DELIMITER '\t', TIMESTAMPFORMAT '%m/%d/%Y %I:%M:%S', IGNORE_ERRORS true);"
                    : "COPY " + tableName + " FROM '" + fileName + "';";

                try (Statement st = connDuck.createStatement()) {
                  LOGGER.fine("execute: " + copyCommand);
                  st.execute(copyCommand);
                }
              } catch (IOException e) {
                throw new RuntimeException(e);
              } finally {
                File f = new File(fileName);
                if (f.exists()) {
                  f.delete();
                }
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

      LOGGER.info("Download Amazon Redshift Geo-Spatial examples");
      URL[] urls = {
          JSQLTranspilerTest.class.getClassLoader()
              .getResource("ai/starlake/transpiler/accommodations.csv"),
          JSQLTranspilerTest.class.getClassLoader()
              .getResource("ai/starlake/transpiler/zipcode.csv")};
      for (URL url1 : urls) {
        assert url1 != null;
        // remove some silly '\N' entries since
        File outputFile = new File(extractionPathFolder, FilenameUtils.getName(url1.getFile()));
        try {
          List<String> lines = Files.readAllLines(Path.of(url1.toURI()), StandardCharsets.UTF_8);
          lines.replaceAll(s -> s.replace("\\N", ""));
          Files.write(outputFile.toPath(), lines, StandardCharsets.UTF_8,
              StandardOpenOption.CREATE_NEW);

          // @todo: find a better way to map the file names to the actual table names
          String tableName = FilenameUtils.getBaseName(url1.getFile());
          String copyCommand = "COPY " + tableName + " FROM '" + outputFile.getAbsolutePath()
              + "' (FORMAT CSV, AUTO_DETECT true, TIMESTAMPFORMAT '%m/%d/%Y %I:%M:%S', IGNORE_ERRORS true);";

          try (Statement st = connDuck.createStatement()) {
            LOGGER.fine("execute: " + copyCommand);
            st.execute(copyCommand);
          }
        } catch (IOException | URISyntaxException e) {
          throw new RuntimeException(e);
        } finally {
          if (outputFile.exists()) {
            outputFile.delete();
          }
        }
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

    // For any JSON related test we want to distinguish the SQL NULL, while for anything else it
    // does not matter
    executeTest(connDuck, t, transpiledSqlStr,
        f.getName().toLowerCase().contains("json") ? JSQLResultSetHelperService.DEFAULT_NULL_VALUE : "");
  }

  public static void executeTest(Connection connDuck, SQLTest t, String transpiledSqlStr,
      String nullValue) throws SQLException, IOException, JSQLParserException {
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
      String outputResult = executeJdbcQuery(connDuck, transpiledSqlStr, nullValue);
      Assertions.assertThat(outputResult).isEqualToIgnoringCase(t.expectedResult);
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

  @Test
  void testUnPipe() throws JSQLParserException, InterruptedException {
    //@formatter:off
    String providedStr =
            "FROM customer\n" +
            "|> LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%unusual%packages%'\n" +
            "|> AGGREGATE COUNT(o_orderkey) c_count GROUP BY c_custkey\n" +
            "|> AGGREGATE COUNT(*) AS custdist GROUP BY c_count\n" +
            "|> ORDER BY custdist DESC, c_count DESC;\n";

    String expectedStr =
            "SELECT  Count( * ) AS custdist\n" +
            "        , c_count\n" +
            "FROM (  SELECT  Count( o_orderkey ) c_count\n" +
            "                , c_custkey\n" +
            "        FROM customer\n" +
            "            LEFT OUTER JOIN orders\n" +
            "                ON c_custkey = o_custkey\n" +
            "                    AND o_comment NOT LIKE '%unusual%packages%'\n" +
            "        GROUP BY c_custkey )\n" +
            "GROUP BY c_count\n" +
            "ORDER BY    custdist DESC\n" +
            "            , c_count DESC\n" +
            ";";
    //@formatter:on

    // only rewrite the `FromQuery` and the `PipedOperators` but don't transpile expressions or
    // functions
    String transpiledSqlStr = JSQLTranspiler.unpipe(providedStr);

    // compare output ignoring white space
    Assertions.assertThat(sanitize(transpiledSqlStr, true)).isEqualTo(sanitize(expectedStr, true));
  }

  private static String executeJdbcQuery(Connection conn, String transpiledSqlStr) throws SQLException, IOException {
    return executeJdbcQuery(conn, transpiledSqlStr, JSQLResultSetHelperService.DEFAULT_NULL_VALUE);
  }

  private static String executeJdbcQuery(Connection conn, String transpiledSqlStr, String nullValue) throws SQLException, IOException {
    try (Statement st = conn.createStatement();) {
      st.executeUpdate("set timezone='Asia/Bangkok'");
      try (ResultSet rs = st.executeQuery(transpiledSqlStr)){
        return formatAsCSV(rs, nullValue);
      }
    }
  }

  private static String executeQuery(JSQLTranspiler.Dialect dialect, String query) throws InterruptedException, SQLException, IOException {
    String result = "";
    if(dialect == JSQLTranspiler.Dialect.DUCK_DB){
      result = executeJdbcQuery(connDuck, query);
    }else if(dialect == JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY){
      result = executeBQQuery(query);
    } else {
      String dbJdbcURL = System.getenv(dialect.name().toUpperCase() + "_JDBC_URL");
      String dbUserName = System.getenv(dialect.name().toUpperCase() + "_USERNAME");
      String dbPassword = System.getenv(dialect.name().toUpperCase() + "_PASSWORD");
      try(Connection jdbcConnection = DriverManager.getConnection(dbJdbcURL, dbUserName, dbPassword)) {
        result = executeJdbcQuery(jdbcConnection, query);
      }
    }
    return result;
  }

  private static String executeBQQuery(String sqlStr) throws InterruptedException, SQLException, IOException {
    // Initialize BigQuery service
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

    // Configure the query
    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sqlStr).setUseLegacySql(false).build();
    JobId jobId = JobId.newBuilder().build();
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

    // Wait for the query to complete.
    queryJob = queryJob.waitFor();
    if (queryJob == null) {
      throw new RuntimeException("Job no longer exists");
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw new RuntimeException(queryJob.getStatus().getError().toString());
    }
    // Execute the query and retrieve results
    TableResult results = queryJob.getQueryResults();
    return formatAsCSV(new BigqueryResultSet(results));
  }

  private static String formatAsCSV(ResultSet rs) throws SQLException, IOException {
    return formatAsCSV(rs, JSQLResultSetHelperService.DEFAULT_NULL_VALUE);
  }

  private static String formatAsCSV(ResultSet rs, String nullValue) throws SQLException, IOException {
    DecimalFormat floatingPointFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
    floatingPointFormat.setGroupingUsed(false);
    floatingPointFormat.setMaximumFractionDigits(9);
    floatingPointFormat.setMinimumFractionDigits(1);
    floatingPointFormat.setMinimumIntegerDigits(1);
    StringWriter stringWriter = new StringWriter();
    try(CSVWriter csvWriter = new CSVWriter(stringWriter)){
      // enforce SQL compliant format
      ResultSetHelperService resultSetHelperService = new JSQLResultSetHelperService(nullValue);
      resultSetHelperService.setDateFormat("yyyy-MM-dd");
      resultSetHelperService.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.S");
      resultSetHelperService.setFloatingPointFormat(floatingPointFormat);
      csvWriter.setResultService(resultSetHelperService);

      csvWriter.writeAll(rs, true, false, true);
      return stringWriter.toString().trim();
    }
  }

  protected static void generateTestCase(JSQLTranspiler.Dialect inputDialect, String inputQuery, String outputFilePath, int queryIndex, boolean supported) throws IOException {
    generateTestCase(inputDialect, JSQLTranspiler.Dialect.DUCK_DB, inputQuery, outputFilePath, queryIndex, supported);
  }

  protected static void generateTestCase(JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect, String inputQuery, String outputFilePath, int queryIndex, boolean supported) throws IOException {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    File outputFile = new File(outputFilePath);
    File parentDir = outputFile.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      parentDir.mkdirs(); // Create missing directories
    }
    try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, queryIndex > 1))){
      if (!supported) {
        writer.write("/*\n");
      }
      // Write the provided input to the file
      writer.write("--provided\n");
      writer.write(inputQuery.trim() + "\n\n");

      // Transpile the input
      boolean transpilationSuccess = true;
      String expectedSqlStr = "";
      try {
        expectedSqlStr = JSQLTranspiler.transpileQuery(inputQuery, inputDialect,
                Collections.emptyMap(), executorService, parser -> {
                });
      } catch (JSQLParserException e) {
        transpilationSuccess = false;
        expectedSqlStr = "UNSUPPORTED" + e.getMessage();
      }

      // Write the transpiled string to the file
      writer.write("--expected\n");
      writer.write(expectedSqlStr + "\n\n");
      if (transpilationSuccess) {
        writer.write("--output\n");
        try {
          String transpilationOutput = executeQuery(outputDialect, expectedSqlStr);
          writer.write(transpilationOutput + "\n\n");
        } catch (Exception e) {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          e.printStackTrace(pw);
          writer.write("INVALID_TRANSLATION " + e.getMessage() + "\n" + sw + "\n\n");
        }
      } else {
        writer.write("--output\n");
        writer.write("NOT TRANSPILED" + "\n\n");
      }
      writer.write("--result\n");
      try {
        String output = executeQuery(inputDialect, inputQuery);
        writer.write(output + "\n\n");
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        writer.write("INVALID_INPUT_QUERY " + e.getMessage() + "\n" + sw + "\n\n");
      }
      if (!supported) {
        writer.write("*/\n");
      }
      // Flush the writer to ensure data is saved
      writer.flush();
    } finally {
      executorService.shutdown();
    }
  }
}
