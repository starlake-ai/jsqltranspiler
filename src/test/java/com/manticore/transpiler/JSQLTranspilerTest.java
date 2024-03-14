package com.manticore.transpiler;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.util.PerformanceTest;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

  @BeforeAll
  static void init() throws SQLException, IOException, JSQLParserException {
    File extractionPathFolder = new File(EXTRACTION_PATH);
    extractionPathFolder.mkdirs();

    // Currently, Duck DB Home resolution in Java seems broken
    File fileDuckDB =
        new File(EXTRACTION_PATH, JSQLTranspilerTest.class.getSimpleName() + ".duckdb");
    connDuck = DriverManager.getConnection("jdbc:duckdb:" + fileDuckDB.getAbsolutePath());

    if (!isInitialised) {
      String sqlStr = IOUtils.resourceToString(
          JSQLTranspilerTest.class.getCanonicalName().replaceAll("\\.", "/") + "_DDL.sql",
          Charset.defaultCharset(), PerformanceTest.class.getClassLoader());
      Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);

      LOGGER.info("Create the DuckDB Table with Indices");
      try (Statement st = connDuck.createStatement()) {
        for (net.sf.jsqlparser.statement.Statement statement : statements.getStatements()) {
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
      if (sanitizedSqlStr.endsWith("/")) {
        sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 1) + ";";
      } else if (sanitizedSqlStr.endsWith("go")) {
        sanitizedSqlStr = sanitizedSqlStr.substring(0, sanitizedSqlStr.length() - 2) + ";";
      }

      return sanitizedSqlStr;

    } else {
      // remove comments only
      return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
    }
  }

  @Test
  void main() {}

  @Test
  void transpileQuery() {}

  @Test
  void transpile() throws Exception {
    String sqlStr =
        "select top 10 qtysold, sellerid\n" + "from sales\n" + "order by qtysold desc, sellerid;";

    // Expect this query to fail since DuckDB does not support `TOP <integer>`
    Assertions.assertThrows(java.sql.SQLException.class, new Executable() {
      @Override
      public void execute() throws Throwable {
        try (Statement st = connDuck.createStatement(); ResultSet rs = st.executeQuery(sqlStr);) {
          rs.next();
        }
      }
    });

    // Expect a rewritten query
    String expectedSqlStr = sanitize("select qtysold, sellerid\n" + "from sales\n"
        + "order by qtysold desc, sellerid\n" + "limit 10");

    String transpiledSqlStr = JSQLTranspiler.transpileQuery(sqlStr, JSQLTranspiler.Dialect.ANY);
    Assertions.assertEquals(expectedSqlStr, sanitize(transpiledSqlStr));

    // Expect this transpiled query to succeed since DuckDB does not support `TOP <integer>`
    int i = 0;
    try (Statement st = connDuck.createStatement();
        ResultSet rs = st.executeQuery(transpiledSqlStr);) {
      while (rs.next()) {
        i++;
      }
    }
    // Expect 10 records
    Assertions.assertEquals(10, i);
  }

  @Test
  void transpileGoogleBigQuery() {}

  @Test
  void transpileDatabricksQuery() {}

  @Test
  void transpileSnowflakeQuery() {}

  @Test
  void transpileAmazonRedshiftQuery() {}
}
