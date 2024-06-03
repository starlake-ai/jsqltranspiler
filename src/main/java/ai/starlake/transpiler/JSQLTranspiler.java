/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com> on behalf of Starlake.AI
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

import ai.starlake.transpiler.bigquery.BigQueryTranspiler;
import ai.starlake.transpiler.databricks.DatabricksTranspiler;
import ai.starlake.transpiler.redshift.RedshiftTranspiler;
import ai.starlake.transpiler.snowflake.SnowflakeTranspiler;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type JSQLtranspiler.
 */
public class JSQLTranspiler extends StatementDeParser {
  public static final Logger LOGGER = Logger.getLogger(JSQLTranspiler.class.getName());

  /**
   * The constant parser TIMEOUT in seconds.
   */
  public static final int TIMEOUT = 6;

  private final JSQLExpressionTranspiler expressionTranspiler;
  private final JSQLSelectTranspiler selectTranspiler;
  private final JSQLInsertTranspiler insertTranspiler;
  private final JSQLUpdateTranspiler updateTranspiler;
  private final JSQLDeleteTranspiler deleteTranspiler;
  private final JSQLMergeTranspiler mergeTranspiler;

  public JSQLTranspiler(Class<? extends JSQLExpressionTranspiler> expressionTranspilerClass, Class<? extends JSQLSelectTranspiler> selectTranspilerClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    super(expressionTranspilerClass, selectTranspilerClass);

    this.expressionTranspiler = expressionTranspilerClass.cast( this.getExpressionDeParser() );
    this.selectTranspiler = selectTranspilerClass.cast(this.getSelectDeParser());

    this.insertTranspiler = new JSQLInsertTranspiler(this.expressionTranspiler, this.selectTranspiler, buffer);

    this.updateTranspiler = new JSQLUpdateTranspiler(this.expressionTranspiler, buffer);

    this.deleteTranspiler = new JSQLDeleteTranspiler(this.expressionTranspiler, buffer);

    this.mergeTranspiler = new JSQLMergeTranspiler(this.expressionTranspiler, this.selectTranspiler, buffer);

  }

  public JSQLTranspiler() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
      this(JSQLExpressionTranspiler.class, JSQLSelectTranspiler.class);
  }


  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @param executorService the ExecutorService to use for running and observing JSQLParser
   * @param consumer the parser configuration to use for the parsing
   * @return the transformed query string
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public static String transpileQuery(String qryStr, Dialect dialect,
      ExecutorService executorService, Consumer<CCJSqlParser> consumer) throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(qryStr, executorService, consumer);
    if (st instanceof Select) {
      Select select = (Select) st;

      switch (dialect) {
        case GOOGLE_BIG_QUERY:
          return transpileGoogleBigQuery(select);
        case DATABRICKS:
          return transpileDatabricksQuery(select);
        case SNOWFLAKE:
          return transpileSnowflakeQuery(select);
        case AMAZON_REDSHIFT:
          return transpileAmazonRedshiftQuery(select);
        default:
          return transpile(select);
      }
    } else {
      throw new RuntimeException("The " + st.getClass().getName()
          + " is not supported yet. Only `PlainSelect` is supported right now.");
    }
  }

  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @return the transformed query string
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static String transpileQuery(String qryStr, Dialect dialect)
      throws JSQLParserException, InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    String result = transpileQuery(qryStr, dialect, executorService, parser -> {
    });

    executorService.shutdown();
    boolean wasTerminated = executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    LOGGER.log(Level.FINE, "Exceutor Service terminated: " + wasTerminated);

    return result;
  }

  /**
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT. Using the provided Executor Service for observing the parser.
   *
   * @param sqlStr the original query string
   * @param outputFile the output file, writing to STDOUT when not defined
   * @param executorService the ExecutorService to use for running and observing JSQLParser
   * @param consumer the parser configuration to use for the parsing
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   */
  public static void transpile(String sqlStr, File outputFile, ExecutorService executorService,
      Consumer<CCJSqlParser> consumer) throws JSQLParserException {
    try {
      JSQLTranspiler transpiler = new JSQLTranspiler();

      // @todo: we may need to split this manually to salvage any not parseable statements
      Statements statements = CCJSqlParserUtil.parseStatements(sqlStr, executorService, consumer);
      for (Statement st : statements) {
        if (st instanceof Select) {
          Select select = (Select) st;
          select.accept(transpiler);

          transpiler.getBuffer().append("\n;\n\n");
        }
        else {
          LOGGER.log(Level.SEVERE, st.getClass().getSimpleName() + " is not supported yet:\n" + st);
        }
      }

      String transpiledSqlStr = transpiler.getBuffer().toString();
      LOGGER.fine("-- Transpiled SQL:\n" + transpiledSqlStr);

      // write to STDOUT when there is no OUTPUT File
      if (outputFile == null) {
        System.out.println(transpiledSqlStr);
      }
      else {
        if (!outputFile.exists() && outputFile.getParentFile() != null) {
          boolean mkdirs = outputFile.getParentFile().mkdirs();
          if (mkdirs) {
            LOGGER.fine("Created all the necessary folders.");
          }
        }

        try (FileWriter writer = new FileWriter(outputFile, Charset.defaultCharset(), true)) {
          writer.write(transpiledSqlStr);
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Failed to write to " + outputFile.getAbsolutePath());
        }
      }
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
        //  this should not really be possible
        throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT.
   *
   *
   * @param sqlStr the original query string
   * @param outputFile the output file, writing to STDOUT when not defined
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static boolean transpile(String sqlStr, File outputFile)
      throws JSQLParserException, InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    transpile(sqlStr, outputFile, executorService, parser -> {
      parser.withErrorRecovery().withTimeOut(6000).withAllowComplexParsing(true)
          .withUnsupportedStatements();
    });
    executorService.shutdown();
    return executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
  }

  /**
   * Read the text content from a resource file.
   *
   *
   * @param url the URL of the resource file
   * @return the text content
   * @throws IOException when the resource file can't be read
   */
  public static String readResource(URL url) throws IOException {
    URLConnection connection = url.openConnection();
    StringBuilder content = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
        content.append(System.lineSeparator());
      }
    }
    return content.toString();
  }

  /**
   * Read the text content from a resource file relative to a particular class' suffix
   *
   *
   * @param clazz the Class which defines the classpath URL of the resource file
   * @param suffix the Class Name suffix used for naming the resource file
   * @return the text content
   * @throws IOException when the resource file can't be read
   */
  public static String readResource(Class<?> clazz, String suffix) throws IOException {
    URL url = JSQLTranspiler.class
        .getResource("/" + clazz.getCanonicalName().replaceAll("\\.", "/") + suffix);
    assert url != null;
    return readResource(url);
  }

  /**
   * Get the Macro `CREATE FUNCTION` statements as a list of text, using the provided
   * ExecutorService to monitor the parser
   *
   * @param executorService the ExecutorService to use for running and observing JSQLParser
   * @param consumer the parser configuration to use for the parsing
   * @return the list of statement texts
   * @throws IOException when the Macro resource file can't be read
   * @throws JSQLParserException when statements in the Macro resource file can't be parsed
   */
  public static Collection<String> getMacros(ExecutorService executorService,
      Consumer<CCJSqlParser> consumer) throws IOException, JSQLParserException {
    ArrayList<String> macroStrList = new ArrayList<>();
    String sqlStr = readResource(JSQLTranspiler.class, "Macro.sql");

    Statements statements = CCJSqlParserUtil.parseStatements(sqlStr, executorService, consumer);
    for (Statement statement : statements) {
      macroStrList.add(statement.toString());
    }
    return macroStrList;
  }

  /**
   * Get the Macro `CREATE FUNCTION` statements as a list of text
   *
   *
   * @return the list of statement texts
   * @throws IOException when the Macro resource file can't be read
   * @throws JSQLParserException when statements in the Macro resource file can't be parsed
   * @throws InterruptedException when the parser does not return a result with 6 seconds
   */
  public static Collection<String> getMacros()
      throws IOException, JSQLParserException, InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    Collection<String> macroStrList = getMacros(executorService, parser -> {
    });

    executorService.shutdown();
    boolean wasTerminated = executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    LOGGER.log(Level.FINE, "Exceutor Service terminated: " + wasTerminated);

    return macroStrList;
  }

  /**
   * Get the Macro `CREATE FUNCTION` statements as an Array of text
   *
   *
   * @return the array of statement texts
   * @throws IOException when the Macro resource file can't be read
   * @throws JSQLParserException when statements in the Macro resource file can't be parsed
   * @throws InterruptedException when the parser does not return a result with 6 seconds
   */
  public static String[] getMacroArray()
      throws IOException, JSQLParserException, InterruptedException {
    return getMacros().toArray(getMacros().toArray(new String[0]));
  }


  /**
   * Create the Macros in a given JDBC connection
   *
   *
   * @throws IOException when the Macro resource file can't be read
   * @throws JSQLParserException when statements in the Macro resource file can't be parsed
   * @throws SQLException when statements can't be executed
   * @throws InterruptedException when the parser does not return a result with 6 seconds
   */
  public static void createMacros(Connection conn)
      throws SQLException, IOException, JSQLParserException, InterruptedException {

    LOGGER.info("Create the DuckDB Macros");
    try (java.sql.Statement st = conn.createStatement()) {
      for (String sqlStr : getMacros()) {
        LOGGER.fine("execute: " + sqlStr);
        st.execute(sqlStr);
      }
    }
  }

  /**
   * Transpile string.
   *
   * @param select the select
   * @return the string
   */
  public static String transpile(Select select) {
    JSQLTranspiler transpiler = new JSQLTranspiler();
    select.accept(transpiler);

    return transpiler.getBuffer().toString();
  }

  /**
   * Transpile google big query string.
   *
   * @param select the select
   * @return the string
   */
  public static String transpileGoogleBigQuery(Select select) {
    BigQueryTranspiler transpiler = new BigQueryTranspiler();
    select.accept(transpiler);

    return transpiler.getBuffer().toString();
  }

  /**
   * Transpile databricks query string.
   *
   * @param select the select
   * @return the string
   */
  public static String transpileDatabricksQuery(Select select) {
    DatabricksTranspiler transpiler = new DatabricksTranspiler();
    select.accept(transpiler);

    return transpiler.getBuffer().toString();
  }

  /**
   * Transpile snowflake query string.
   *
   * @param select the select
   * @return the string
   */
  public static String transpileSnowflakeQuery(Select select) {
    SnowflakeTranspiler transpiler = new SnowflakeTranspiler();
    select.accept(transpiler);

    return transpiler.getBuffer().toString();
  }

  /**
   * Transpile amazon redshift query string.
   *
   * @param select the select
   * @return the string
   */
  public static String transpileAmazonRedshiftQuery(Select select) {
    RedshiftTranspiler transpiler = new RedshiftTranspiler();
    select.accept(transpiler);

    return transpiler.getBuffer().toString();
  }

  public void visit(Select select) {
    select.accept(selectTranspiler);
  }

  public void visit(Insert insert) {
    insertTranspiler.deParse(insert);
  }

  public void visit(Update update) {
    updateTranspiler.deParse(update);

  }

  public void visit(Delete delete) {
    deleteTranspiler.deParse(delete);
  }


  public void visit(Merge merge) {
    mergeTranspiler.deParse(merge);
  }

  /**
   * The enum Dialect.
   */
  public enum Dialect {
    /**
     * Google big query dialect.
     */
    GOOGLE_BIG_QUERY,
    /**
     * Databricks dialect.
     */
    DATABRICKS,
    /**
     * Snowflake dialect.
     */
    SNOWFLAKE,
    /**
     * Amazon redshift dialect.
     */
    AMAZON_REDSHIFT,
    /**
     * Any dialect.
     */
    ANY,
    /**
     * Duck db dialect.
     */
    DUCK_DB
  }

}
