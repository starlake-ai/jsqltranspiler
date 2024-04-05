package ai.starlake.transpiler;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class JSQLTranspilerTest {
  final static Logger LOGGER = Logger.getLogger(JSQLTranspilerTest.class.getName());
  private final static String EXTRACTION_PATH =
      System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
  public static Connection connDuck;
  private static boolean isInitialised = false;

  private static final Pattern SQL_COMMENT_PATTERN =
      Pattern.compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

  private static final Pattern SQL_SANITATION_PATTERN =
      Pattern.compile("(\\s+)", Pattern.MULTILINE);

  // Assure SPACE around Syntax Characters
  private static final Pattern SQL_SANITATION_PATTERN2 =
      Pattern.compile("\\s*([!/,()=+\\-*|\\{\\}\\[\\]<>:])\\s*", Pattern.MULTILINE);

  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/any";

  public static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      String filename = name.toLowerCase().trim();
      return name.endsWith(".sql") && !name.startsWith("disabled");
    }
  };

  public static class SQLTest {
    final JSQLTranspiler.Dialect inputDialect;
    final JSQLTranspiler.Dialect outputDialect;

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
      int i = 0;
      for (SQLTest t : e.getValue()) {
        data.add(new Object[] {e.getKey(), ++i, t});
      }
    }

    // Create a Stream<Arguments> from the array
    return Arrays.stream(data.toArray(new Object[0][]))
        .map(row -> Arguments.of(row[0], row[1], row[2]));
  }

  protected static Map<File, List<SQLTest>> getSqlTestMap(File[] testFiles,
      JSQLTranspiler.Dialect inputDialect, JSQLTranspiler.Dialect outputDialect) {
    LinkedHashMap<File, List<SQLTest>> sqlMap = new LinkedHashMap<>();


    for (File file : Objects.requireNonNull(testFiles)) {
      List<SQLTest> tests = new ArrayList<>();
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
            k = line.substring(2).trim().toLowerCase();
          }

          if (line.toLowerCase().replaceAll("\\s", "").startsWith("--provid")) {
            if (test.providedSqlStr != null
                && (test.expectedTally >= 0 || test.expectedResult != null)) {
              LOGGER.fine("Found multiple test descriptions in " + file.getName());
              // pass through tests not depending on transpiling
              if (test.expectedSqlStr == null || test.expectedSqlStr.isEmpty()) {
                test.expectedSqlStr = test.providedSqlStr;
              }
              tests.add(test);

              test = new SQLTest(inputDialect, outputDialect);
            }
          }

          startContent = startContent
              || (!line.startsWith("--") || line.startsWith("-- @")) && !line.trim().isEmpty();
          endContent = startContent && !line.startsWith("--")
              && (line.trim().endsWith(";")
                  || (k.equalsIgnoreCase("count") || k.equalsIgnoreCase("tally")) && line.isEmpty()
                  || (k.startsWith("result") || k.startsWith("return")) && line.isEmpty());

          if (startContent && !line.isEmpty()) {
            stringBuilder.append(line).append("\n");
          }

          if (endContent) {
            if (k.startsWith("provid")) {
              test.providedSqlStr = stringBuilder.toString();
            } else if (k.startsWith("expect")) {
              test.expectedSqlStr = stringBuilder.toString();
            } else if (k.startsWith("count") || k.startsWith("tally")) {
              test.expectedTally = Integer.parseInt(stringBuilder.toString().trim());
            } else if (k.startsWith("result") || k.startsWith("return")) {
              test.expectedResult = stringBuilder.toString().trim();
            }
            stringBuilder.setLength(0);
            startContent = false;
          }

        }

        // pass through tests not depending on transpiling
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
        if (inQuotes && !escaped) {
          inQuotes = false;
        } else if (!inQuotes) {
          inQuotes = true;
        }
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
      // sophisticated
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

    // Assertions.assertNotNull(t.expectedSqlStr);
    String transpiledSqlStr = JSQLTranspiler.transpileQuery(t.providedSqlStr, t.inputDialect);
    Assertions.assertThat(transpiledSqlStr).isNotNull();
    Assertions.assertThat(sanitize(transpiledSqlStr, true))
        .isEqualTo(sanitize(t.expectedSqlStr, true));

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
      Assertions.assertThat(i).isEqualTo(t.expectedTally);
    }


    if (t.expectedResult != null && !t.expectedResult.isEmpty()) {
      // Compare output
      try (Statement st = connDuck.createStatement();
          ResultSet rs = st.executeQuery(transpiledSqlStr);
          StringWriter stringWriter = new StringWriter();
          CSVWriter csvWriter = new CSVWriter(stringWriter)) {

        // enforce SQL compliant format
        ResultSetHelperService resultSetHelperService = new ResultSetHelperService();
        resultSetHelperService.setDateFormat("yyyy-MM-dd");
        resultSetHelperService.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        csvWriter.setResultService(resultSetHelperService);

        csvWriter.writeAll(rs, true, false, true);
        csvWriter.flush();
        stringWriter.flush();
        Assertions.assertThat(stringWriter.toString().trim())
            .isEqualToIgnoringCase(t.expectedResult);
      }
    }
  }
}
