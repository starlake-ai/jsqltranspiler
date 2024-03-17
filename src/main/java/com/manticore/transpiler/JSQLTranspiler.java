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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * The type Jsql transpiler.
 */
public class JSQLTranspiler extends SelectDeParser {
  /**
   * The constant LOGGER.
   */
  public final static Logger LOGGER = Logger.getLogger(JSQLTranspiler.class.getName());
  private final ExpressionTranspiler expressionTranspiler;
  private final StringBuilder resultBuilder;

  private final Dialect inputDialect;

  private final Dialect outputDialect;

  /**
   * Instantiates a new Jsql transpiler.
   */
  public JSQLTranspiler(Dialect inputDialect, Dialect outputDialect) {
    this.inputDialect = inputDialect;
    this.outputDialect = outputDialect;
    this.resultBuilder = new StringBuilder();
    this.setBuffer(resultBuilder);

    expressionTranspiler =
        new ExpressionTranspiler(this, this.resultBuilder, inputDialect, outputDialect);
    this.setExpressionVisitor(expressionTranspiler);
  }

  /**
   * Resolves the absolute File from a relative filename, considering $HOME variable and "~"
   *
   * @param filename the relative filename
   * @return the resolved absolute file
   */
  public static File getAbsoluteFile(String filename) {
    String homePath = new File(System.getProperty("user.home")).toURI().getPath();

    String _filename = filename.replaceFirst("~", Matcher.quoteReplacement(homePath))
        .replaceFirst("\\$\\{user.home}", Matcher.quoteReplacement(homePath));

    File f = new File(_filename);
    if (!f.isAbsolute()) {
      Path basePath = Paths.get("").toAbsolutePath();

      Path resolvedPath = basePath.resolve(filename);
      Path absolutePath = resolvedPath.normalize();
      f = absolutePath.toFile();
    }
    return f;
  }

  /**
   * Resolves the absolute File Name from a relative filename, considering $HOME variable and "~"
   *
   * @param filename the relative filename
   * @return the resolved absolute file name
   */
  public static String getAbsoluteFileName(String filename) {
    return getAbsoluteFile(filename).getAbsolutePath();
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static void main(String[] args) {
    Options options = new Options();
    OptionGroup inputDialectOptions = new OptionGroup();
    inputDialectOptions.addOption(Option.builder("d").longOpt("input-dialect").hasArg()
        .type(Dialect.class)
        .desc(
            "The SQL dialect to parse.\n[ANY*, GOOGLE_BIG_QUERY, DATABRICKS, SNOWFLAKE, AMAZON_REDSHIFT]")
        .build());
    inputDialectOptions.addOption(Option.builder(null).longOpt("any").hasArg(false)
        .desc("Interpret the SQL as Generic Dialect [DEFAULT].").build());
    inputDialectOptions.addOption(Option.builder(null).longOpt("bigquery").hasArg(false)
        .desc("Interpret the SQL as Google BigQuery Dialect.").build());
    inputDialectOptions.addOption(Option.builder(null).longOpt("databricks").hasArg(false)
        .desc("Interpret the SQL as DataBricks Dialect.").build());
    inputDialectOptions.addOption(Option.builder(null).longOpt("snowflake").hasArg(false)
        .desc("Interpret the SQL as Snowflake Dialect.").build());
    inputDialectOptions.addOption(Option.builder(null).longOpt("redshift").hasArg(false)
        .desc("Interpret the SQL as Amazon Snowflake Dialect.").build());

    options.addOptionGroup(inputDialectOptions);


    OptionGroup outputDialectOptions = new OptionGroup();
    outputDialectOptions.addOption(Option.builder("D").longOpt("output-dialect").hasArg()
        .desc("The SQL dialect to write.\n[DUCKDB*]").build());
    outputDialectOptions.addOption(Option.builder(null).longOpt("duckdb").hasArg(false)
        .desc("Write the SQL in the Duck DB Dialect [DEFAULT].").build());
    options.addOptionGroup(outputDialectOptions);

    options.addOption("i", "inputFile", true,
        "The input SQL file or folder.\n  - Read from STDIN when no input file provided.");
    options.addOption("o", "outputFile", true,
        "The out SQL file for the formatted statements.\n  - Create new SQL file when folder provided.\n  - Append when existing file provided.\n  - Write to STDOUT when no output file provided.");

    options.addOption("h", "help", false, "Print the help synopsis.");

    // create the parser
    CommandLineParser parser = new DefaultParser();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help") || line.getOptions().length == 0 && line.getArgs().length == 0) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);

        String startupCommand =
            System.getProperty("java.vm.name").equalsIgnoreCase("Substrate VM") ? "./JSQLTranspiler"
                : "java -jar JSQLTranspiler.jar";

        formatter.printHelp(startupCommand, options, true);
        return;
      }

      Dialect dialect = Dialect.ANY;
      if (line.hasOption("d")) {
        dialect = (Dialect) line.getParsedOptionValue("d");
      } else if (line.hasOption("bigquery")) {
        dialect = Dialect.GOOGLE_BIG_QUERY;
      } else if (line.hasOption("databricks")) {
        dialect = Dialect.DATABRICKS;
      } else if (line.hasOption("snowflake")) {
        dialect = Dialect.SNOWFLAKE;
      } else if (line.hasOption("redshift")) {
        dialect = Dialect.AMAZON_REDSHIFT;
      }

      File outputFile = null;
      if (line.hasOption("outputFile")) {
        outputFile = getAbsoluteFile(line.getOptionValue("outputFile"));

        // if an existing folder was provided, create a new file in it
        if (outputFile.exists() && outputFile.isDirectory()) {
          outputFile = File.createTempFile(dialect.name() + "_transpiled_", ".sql", outputFile);
        }
      }

      File inputFile = null;
      if (line.hasOption("inputFile")) {
        inputFile = getAbsoluteFile(line.getOptionValue("inputFile"));

        if (!inputFile.canRead()) {
          throw new IOException(
              "Can't read the specified INPUT-FILE " + inputFile.getAbsolutePath());
        }

        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
          String sqlStr = IOUtils.toString(inputStream, Charset.defaultCharset());
          transpile(sqlStr, dialect, Dialect.DUCK_DB, outputFile);
        } catch (IOException ex) {
          throw new IOException(
              "Can't read the specified INPUT-FILE " + inputFile.getAbsolutePath(), ex);
        } catch (JSQLParserException ex) {
          throw new RuntimeException("Failed to parse the provided SQL.", ex);
        }
      }

      List<String> argsList = line.getArgList();
      if (argsList.isEmpty() && !line.hasOption("inputFile")) {
        throw new IOException("No SQL statements provided for formatting.");
      } else {
        for (String sqlStr : argsList) {
          try {
            transpile(sqlStr, dialect, Dialect.DUCK_DB, outputFile);
          } catch (JSQLParserException ex) {
            throw new RuntimeException("Failed to parse the provided SQL.", ex);
          }
        }
      }

    } catch (ParseException ex) {
      LOGGER.log(Level.FINE, "Parsing failed.  Reason: " + ex.getMessage(), ex);

      HelpFormatter formatter = new HelpFormatter();
      formatter.setOptionComparator(null);
      formatter.printHelp("java -jar JSQLTranspiler.jar", options, true);

      throw new RuntimeException("Could not parse the Command Line Arguments.", ex);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Transpile a query string in the defined dialect into DuckDB compatible SQL.
   *
   * @param qryStr the original query string
   * @param dialect the dialect of the query string
   * @return the transformed query string
   * @throws Exception the exception
   */
  public static String transpileQuery(String qryStr, Dialect dialect) throws Exception {
    Statement st = CCJSqlParserUtil.parse(qryStr);
    if (st instanceof PlainSelect) {
      PlainSelect select = (PlainSelect) st;

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
   * Transpile a query string from a file or STDIN and write the transformed query string into a
   * file or STDOUT.
   *
   *
   * @param sqlStr the original query string
   * @param inputDialect the input dialect
   * @param outputDialect the output dialect (DuckDB)
   * @param outputFile the output file, writing to STDOUT when not defined
   * @throws JSQLParserException the jsql parser exception
   */
  public static void transpile(String sqlStr, Dialect inputDialect, Dialect outputDialect,
      File outputFile) throws JSQLParserException {
    JSQLTranspiler transpiler = new JSQLTranspiler(inputDialect, outputDialect);

    // @todo: we may need to split this manually to salvage any not parseable statements
    Statements statements = CCJSqlParserUtil.parseStatements(sqlStr, parser -> {
      parser.setErrorRecovery(true);
      // parser.withTimeOut(60000);
      // parser.withAllowComplexParsing(true);
    });
    for (Statement st : statements) {
      if (st instanceof PlainSelect) {
        PlainSelect select = (PlainSelect) st;
        transpiler.visit(select);

        transpiler.getResultBuilder().append("\n;\n\n");
      } else {
        LOGGER.log(Level.SEVERE,
            st.getClass().getSimpleName() + " is not supported yet:\n" + st.toString());
      }
    }

    String transpiledSqlStr = transpiler.getResultBuilder().toString();
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
  }

  /**
   * Transpile string.
   *
   * @param select the select
   * @return the string
   * @throws Exception the exception
   */
  public static String transpile(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler(Dialect.ANY, Dialect.DUCK_DB);
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  /**
   * Transpile google big query string.
   *
   * @param select the select
   * @return the string
   * @throws Exception the exception
   */
  public static String transpileGoogleBigQuery(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler(Dialect.GOOGLE_BIG_QUERY, Dialect.DUCK_DB);
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  /**
   * Transpile databricks query string.
   *
   * @param select the select
   * @return the string
   * @throws Exception the exception
   */
  public static String transpileDatabricksQuery(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler(Dialect.DATABRICKS, Dialect.DUCK_DB);
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  /**
   * Transpile snowflake query string.
   *
   * @param select the select
   * @return the string
   * @throws Exception the exception
   */
  public static String transpileSnowflakeQuery(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler(Dialect.SNOWFLAKE, Dialect.DUCK_DB);
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  /**
   * Transpile amazon redshift query string.
   *
   * @param select the select
   * @return the string
   * @throws Exception the exception
   */
  public static String transpileAmazonRedshiftQuery(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler(Dialect.AMAZON_REDSHIFT, Dialect.DUCK_DB);
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  /**
   * Gets expression transpiler.
   *
   * @return the expression transpiler
   */
  public ExpressionTranspiler getExpressionTranspiler() {
    return expressionTranspiler;
  }

  /**
   * Gets result builder.
   *
   * @return the result builder
   */
  public StringBuilder getResultBuilder() {
    return resultBuilder;
  }

  public void visit(Top top) {
    // get the parent SELECT
    SimpleNode node = (SimpleNode) top.getASTNode().jjtGetParent();
    while (node.jjtGetValue() == null) {
      node = (SimpleNode) node.jjtGetParent();
    }
    PlainSelect select = (PlainSelect) node.jjtGetValue();

    // rewrite the TOP into a LIMIT
    select.setTop(null);
    select.setLimit(new Limit().withRowCount(top.getExpression()));
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
