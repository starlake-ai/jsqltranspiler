package com.manticore.transpiler;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public class JSQLTranspiler extends SelectDeParser {
  public final static Logger LOGGER = Logger.getLogger(JSQLTranspiler.class.getName());

  public enum Dialect {
    GOOGLE_BIG_QUERY, DATABRICKS, SNOWFLAKE, AMAZON_REDSHIFT, ANY
  }

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

  public static String getAbsoluteFileName(String filename) {
    return getAbsoluteFile(filename).getAbsolutePath();
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public static void main(String[] args) {
    System.out.println("Hello world");

    Options options = new Options();

    options.addOption("i", "inputFile", true, "The input SQL file or folder.");
    options.addOption("o", "outputFile", true, "The out SQL file for the formatted statements.");


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

      File inputFile = null;
      if (line.hasOption("inputFile")) {
        inputFile = getAbsoluteFile(line.getOptionValue("inputFile"));

        if (!inputFile.canRead()) {
          throw new IOException(
              "Can't read the specified INPUT-FILE " + inputFile.getAbsolutePath());
        }

        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
          String sqlStr = IOUtils.toString(inputStream, Charset.defaultCharset());
          // @todo: do something useful here
          System.out.println(sqlStr);
        } catch (IOException ex) {
          throw new IOException(
              "Can't read the specified INPUT-FILE " + inputFile.getAbsolutePath(), ex);
        }
      }

      List<String> argsList = line.getArgList();
      if (argsList.isEmpty() && !line.hasOption("input-file")) {
        throw new IOException("No SQL statements provided for formatting.");
      } else {
        for (String s : argsList) {
          // @todo: do something useful here
          System.out.println(s);
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

  public static String transpile(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler();
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  public static String transpileGoogleBigQuery(PlainSelect select) throws Exception {
    JSQLTranspiler transpiler = new JSQLTranspiler();
    transpiler.visit(select);

    return transpiler.getResultBuilder().toString();
  }

  public static String transpileDatabricksQuery(PlainSelect select) throws Exception {
    throw new SQLFeatureNotSupportedException("Dialect not implemented yet");
  }

  public static String transpileSnowflakeQuery(PlainSelect select) throws Exception {
    throw new SQLFeatureNotSupportedException("Dialect not implemented yet");
  }

  public static String transpileAmazonRedshiftQuery(PlainSelect select) throws Exception {
    throw new SQLFeatureNotSupportedException("Dialect not implemented yet");
  }

  public ExpressionTranspiler getExpressionTranspiler() {
    return expressionTranspiler;
  }

  public StringBuilder getResultBuilder() {
    return resultBuilder;
  }

  private final ExpressionTranspiler expressionTranspiler;
  private final StringBuilder resultBuilder;

  public JSQLTranspiler() {
    expressionTranspiler = new ExpressionTranspiler();
    resultBuilder = new StringBuilder();

    this.setExpressionVisitor(expressionTranspiler);
    this.setBuffer(resultBuilder);

    expressionTranspiler.setSelectVisitor(this);
    expressionTranspiler.setBuffer(resultBuilder);
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

}
