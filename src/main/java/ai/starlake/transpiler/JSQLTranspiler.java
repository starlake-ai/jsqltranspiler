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
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.update.Update;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type JSQLTranspiler.
 */
public class JSQLTranspiler extends StatementDeParser {
  public static final Logger LOGGER = Logger.getLogger(JSQLTranspiler.class.getName());

  /**
   * The constant parser TIMEOUT in seconds.
   */
  public static final int TIMEOUT = 6;

  protected final JSQLExpressionTranspiler expressionTranspiler;
  protected final JSQLSelectTranspiler selectTranspiler;
  protected final JSQLInsertTranspiler insertTranspiler;
  protected final JSQLUpdateTranspiler updateTranspiler;
  protected final JSQLDeleteTranspiler deleteTranspiler;
  protected final JSQLMergeTranspiler mergeTranspiler;

  protected final HashMap<String, Object> parameters = new LinkedHashMap<>();

  private final static Pattern SINGLE_TICKS_PATTERN =
      Pattern.compile("(?=(?:[^']*'[^']*')*[^']*$)`");

  protected JSQLTranspiler(Class<? extends JSQLSelectTranspiler> selectTranspilerClass,
      Class<? extends JSQLExpressionTranspiler> expressionTranspilerClass)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException,
      IllegalAccessException {
    super(expressionTranspilerClass, selectTranspilerClass);

    this.expressionTranspiler = expressionTranspilerClass.cast(this.getExpressionDeParser());
    this.selectTranspiler = selectTranspilerClass.cast(this.getSelectDeParser());

    this.insertTranspiler =
        new JSQLInsertTranspiler(this.expressionTranspiler, this.selectTranspiler, buffer);

    this.updateTranspiler = new JSQLUpdateTranspiler(this.expressionTranspiler, buffer);

    this.deleteTranspiler = new JSQLDeleteTranspiler(this.expressionTranspiler, buffer);

    this.mergeTranspiler =
        new JSQLMergeTranspiler(this.expressionTranspiler, this.selectTranspiler, buffer);

  }

  public JSQLTranspiler(Map<String, Object> parameters) throws InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    this(JSQLSelectTranspiler.class, JSQLExpressionTranspiler.class);
    this.parameters.putAll(parameters);
    this.expressionTranspiler.parameterMap.putAll(parameters);
  }

  public JSQLTranspiler() throws InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    this(JSQLSelectTranspiler.class, JSQLExpressionTranspiler.class);
  }


  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @param parameters the map of substitution key/value pairs (can be empty)
   * @param executorService the ExecutorService to use for running and observing JSQLParser
   * @param consumer the parser configuration to use for the parsing
   * @return the transformed query string
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public static String transpileQuery(String qryStr, Dialect dialect,
      Map<String, Object> parameters, ExecutorService executorService,
      Consumer<CCJSqlParser> consumer) throws JSQLParserException {
    Statement st;

    switch (dialect) {
      case GOOGLE_BIG_QUERY:
        // Replace Double quoted string literals with single quoted string literals
        StringBuilder sb = new StringBuilder();
        boolean inSingleQuote = false; // Tracks if we're inside single quotes
        boolean inDoubleQuote = false; // Tracks if we're inside double quotes

        for (int i = 0; i < qryStr.length(); i++) {
          char c = qryStr.charAt(i);

          // Toggle state for single quotes
          if (c == '\'' && !inDoubleQuote) {
            inSingleQuote = !inSingleQuote;
            sb.append(c); // Append the single quote as-is
          } else /* Toggle state for double quotes */ if (c == '\"' && !inSingleQuote) {
            inDoubleQuote = !inDoubleQuote;
            sb.append('\''); // Replace outer double quotes with single quotes
          } else /*Replace inner single quotes with double quotes if inside double-quoted context */ if (c == '\'') {
            sb.append('"');
          } else /* Append everything else as-is */ {
            sb.append(c);
          }
        }

        // Replace single quoted identifiers with double-quoted identifiers
        Matcher matcher = SINGLE_TICKS_PATTERN.matcher(sb.toString());
        sb = new StringBuilder();
        while (matcher.find()) {
          matcher.appendReplacement(sb, matcher.group().replaceAll("^`|`$", "\""));
        }
        matcher.appendTail(sb);

        st = CCJSqlParserUtil.parse(sb.toString(), executorService, consumer);
        return transpileBigQuery(st, parameters);
      case DATABRICKS:
        st = CCJSqlParserUtil.parse(qryStr, executorService, consumer);
        return transpileDatabricks(st, parameters);
      case SNOWFLAKE:
        st = CCJSqlParserUtil.parse(qryStr, executorService, consumer);
        return transpileSnowflake(st, parameters);
      case AMAZON_REDSHIFT:
        st = CCJSqlParserUtil.parse(qryStr, executorService, consumer);
        return transpileAmazonRedshift(st, parameters);
      default:
        st = CCJSqlParserUtil.parse(qryStr, executorService, consumer);
        return transpile(st, parameters);
    }
  }

  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @param parameters the map of substitution key/value pairs (can be empty)
   * @return the transformed query string
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time-out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static String transpileQuery(String qryStr, Dialect dialect,
      Map<String, Object> parameters) throws JSQLParserException, InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    String result = transpileQuery(qryStr, dialect, parameters, executorService, parser -> {
    });

    executorService.shutdown();
    boolean wasTerminated = executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    LOGGER.log(Level.FINE, "Exceutor Service terminated: " + wasTerminated);

    return result;
  }

  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @return the transformed query string
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time-out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static String transpileQuery(String qryStr, Dialect dialect)
      throws JSQLParserException, InterruptedException {
    return transpileQuery(qryStr, dialect, Collections.emptyMap());
  }

  /**
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT. Using the provided Executor Service for observing the parser.
   *
   * @param sqlStr the original query string
   * @param parameters the map of substitution key/value pairs (can be empty)
   * @param outputFile the output file, writing to STDOUT when not defined
   * @param executorService the ExecutorService to use for running and observing JSQLParser
   * @param consumer the parser configuration to use for the parsing
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public static void transpile(String sqlStr, Map<String, Object> parameters, File outputFile,
      ExecutorService executorService, Consumer<CCJSqlParser> consumer) throws JSQLParserException {
    try {
      JSQLTranspiler transpiler = new JSQLTranspiler(parameters);

      // @todo: we may need to split this manually to salvage any not parseable statements
      Statements statements = CCJSqlParserUtil.parseStatements(sqlStr, executorService, consumer);
      for (Statement st : statements) {
        st.accept(transpiler);
        transpiler.getBuffer().append("\n;\n\n");
      }

      String transpiledSqlStr = transpiler.getBuffer().toString();
      LOGGER.fine("-- Transpiled SQL:\n" + transpiledSqlStr);

      // write to STDOUT when there is no OUTPUT File
      if (outputFile == null) {
        System.out.println(transpiledSqlStr);
      } else {
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
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should not really be possible
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT.
   *
   *
   * @param sqlStr the original query string
   * @param parameters the map of substitution key/value pairs (can be empty)
   * @param outputFile the output file, writing to STDOUT when not defined
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time-out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static boolean transpile(String sqlStr, Map<String, Object> parameters, File outputFile)
      throws JSQLParserException, InterruptedException {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    transpile(sqlStr, parameters, outputFile, executorService, parser -> {
      parser.withErrorRecovery().withTimeOut(6000).withAllowComplexParsing(true)
          .withUnsupportedStatements();
    });
    executorService.shutdown();
    return executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
  }

  /**
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT.
   *
   * @param sqlStr the original query string
   * @param outputFile the output file, writing to STDOUT when not defined
   * @throws JSQLParserException a parser exception when the statement can't be parsed
   * @throws InterruptedException a time-out exception, when the statement can't be parsed within 6
   *         seconds (hanging parser)
   */
  public static boolean transpile(String sqlStr, File outputFile)
      throws JSQLParserException, InterruptedException {
    return transpile(sqlStr, Collections.emptyMap(), outputFile);
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
    LOGGER.log(Level.FINE, "Executor Service terminated: " + wasTerminated);

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
   * Rewrite a given SQL Statement into a text representation.
   *
   * @param statement the statement
   * @return the string
   */
  public static String transpile(Statement statement, Map<String, Object> parameters) {
    try {
      JSQLTranspiler transpiler = new JSQLTranspiler(parameters);
      statement.accept(transpiler);

      return transpiler.getBuffer().toString();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should really never happen
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Rewrite a given BigQuery SQL Statement into a text representation.
   *
   * @param statement the statement
   * @return the string
   */
  public static String transpileBigQuery(Statement statement, Map<String, Object> parameters) {
    try {
      BigQueryTranspiler transpiler = new BigQueryTranspiler(parameters);
      statement.accept(transpiler);

      return transpiler.getBuffer().toString();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should really never happen
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Rewrite a given DataBricks SQL Statement into a text representation.
   *
   * @param statement the statement
   * @return the string
   */
  public static String transpileDatabricks(Statement statement, Map<String, Object> parameters) {
    try {
      DatabricksTranspiler transpiler = new DatabricksTranspiler(parameters);
      statement.accept(transpiler);

      return transpiler.getBuffer().toString();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should really never happen
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Rewrite a given Snowflake SQL Statement into a text representation.
   *
   * @param statement the statement
   * @return the string
   */
  public static String transpileSnowflake(Statement statement, Map<String, Object> parameters) {
    try {
      SnowflakeTranspiler transpiler = new SnowflakeTranspiler(parameters);
      statement.accept(transpiler);

      return transpiler.getBuffer().toString();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should really never happen
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  /**
   * Rewrite a given Redshift SQL Statement into a text representation.
   *
   * @param statement the statement
   * @return the string
   */
  public static String transpileAmazonRedshift(Statement statement,
      Map<String, Object> parameters) {
    try {
      RedshiftTranspiler transpiler = new RedshiftTranspiler(parameters);
      statement.accept(transpiler);

      return transpiler.getBuffer().toString();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
        | IllegalAccessException e) {
      // this should really never happen
      throw new RuntimeException("Failed to initiate the Transpiler Classes", e);
    }
  }

  public <S> StringBuilder visit(Select select, S context) {
    select.accept((SelectVisitor<StringBuilder>) selectTranspiler, context);
    return buffer;
  }

  public <S> StringBuilder visit(Insert insert, S context) {
    insertTranspiler.deParse(insert);
    return buffer;
  }

  public <S> StringBuilder visit(Update update, S context) {
    updateTranspiler.deParse(update);

    return buffer;
  }

  public <S> StringBuilder visit(Delete delete, S context) {
    deleteTranspiler.deParse(delete);
    return buffer;
  }


  public <S> StringBuilder visit(Merge merge, S context) {
    mergeTranspiler.deParse(merge);
    return buffer;
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
