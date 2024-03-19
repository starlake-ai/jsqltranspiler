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

import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Expression transpiler.
 */
public class ExpressionTranspiler extends ExpressionDeParser {
  enum DateFunction {
   CURRENT_DATE
    , DATE
    , DATE_ADD
    , DATE_DIFF
    , DATE_SUB
    , DATE_TRUNC
    , EXTRACT
    , FORMAT_DATE;

    public static ExpressionTranspiler.DateFunction from(String name) {
      ExpressionTranspiler.DateFunction function = null;
      try {
        function = Enum.valueOf(ExpressionTranspiler.DateFunction.class, name.toUpperCase());
      } catch (Exception ignore) {

      }
      return function;
    }

    public static ExpressionTranspiler.DateFunction from(Function f) {
      return from(f.getName());
    }

  }
  public static boolean isDateFunction(Function f) {
    return DateFunction.from(f.getName())!=null;
  }

  enum TranspiledFunction {
    CURRENT_DATE
    , DATE
    , DATE_ADD
    , DATE_DIFF
    , DATE_SUB
    , DATE_TRUNC
    , EXTRACT
    , FORMAT_DATE

    , NVL;

    public static TranspiledFunction from(String name) {
      TranspiledFunction function = null;
      try {
        function = Enum.valueOf(TranspiledFunction.class, name.toUpperCase());
      } catch (Exception ignore) {

      }
      return function;
    }

    public static TranspiledFunction from(Function f) {
      return from(f.getName());
    }
  }

  enum UnsupportedFunction {
    DATE_FROM_UNIX_DATE
    ;

    public static UnsupportedFunction from(String name) {
      UnsupportedFunction function = null;
      try {
        function = Enum.valueOf(UnsupportedFunction.class, name.toUpperCase());
      } catch (Exception ignore) {

      }
      return function;
    }

    public static UnsupportedFunction from(Function f) {
      return from(f.getName());
    }
  }

  private final JSQLTranspiler.Dialect inputDialect;

  public ExpressionTranspiler(SelectVisitor selectVisitor, StringBuilder buffer,
      JSQLTranspiler.Dialect inputDialect) {
    super(selectVisitor, buffer);
    this.inputDialect = inputDialect;
  }

  public static boolean isDatePart(Expression expression, JSQLTranspiler.Dialect dialect) {
    switch (dialect) {
      case GOOGLE_BIG_QUERY:
        return isDatePartBigQuery(expression);
      case DATABRICKS:
        return isDatePartDataBricks(expression);
      case SNOWFLAKE:
        return isDatePartSnowflake(expression);
      case AMAZON_REDSHIFT:
        return isDatePartRedshift(expression);
      default:
        return isDatePart(expression);
    }
  }

  private static boolean isDatePart(Expression expression) {
    Pattern[] patterns = {Pattern.compile("\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartBigQuery(Expression expression) {
    Pattern[] patterns = {
        Pattern.compile(
            "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(DAY|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartDataBricks(Expression expression) {
    Pattern[] patterns = {Pattern.compile("\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartSnowflake(Expression expression) {
    Pattern[] patterns = {Pattern.compile("\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartRedshift(Expression expression) {
    Pattern[] patterns = {Pattern.compile("\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePart(Expression expression) {
    Pattern[] patterns = {Pattern.compile("\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartBigQuery(Expression expression) {
    Pattern[] patterns = {
        Pattern.compile(
            "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE),
        Pattern.compile(
            "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartDataBricks(Expression expression) {
    Pattern[] patterns = {Pattern.compile(
        "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartSnowflake(Expression expression) {
    Pattern[] patterns = {Pattern.compile(
        "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartRedshift(Expression expression) {
    Pattern[] patterns = {Pattern.compile(
        "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)};
    boolean isDatePart = false;
    for (Pattern p : patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  public static boolean isDateTimePart(Expression expression, JSQLTranspiler.Dialect dialect) {
    switch (dialect) {
      case GOOGLE_BIG_QUERY:
        return isDateTimePartBigQuery(expression);
      case DATABRICKS:
        return isDateTimePartDataBricks(expression);
      case SNOWFLAKE:
        return isDateTimePartSnowflake(expression);
      case AMAZON_REDSHIFT:
        return isDateTimePartRedshift(expression);
      default:
        return isDateTimePart(expression);
    }
  }

  private boolean parameterWEEK(ExpressionList<?> parameters, int index) {
    // Date Part "WEEK(MONDAY)" or "WEEK(SUNDAY)" seems to be a thing
    Pattern pattern = Pattern.compile(
        "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    Expression p = parameters.get(index);
    if (p instanceof Column || p instanceof Function) {
      Matcher matcher = pattern.matcher(p.toString());
      return matcher.matches();
    } else {
      return false;
    }
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public void visit(Function function) {
    if (UnsupportedFunction.from(function)!=null) {
      throw new RuntimeException("Unsupported: " + function.getName() + " is not supported by DuckDB (yet).");
    }

    Expression rewrittenExpression = null;
    ExpressionList<?> parameters = function.getParameters();
    TranspiledFunction f = TranspiledFunction.from(function.getName());
    if (f!=null) {
      switch (f) {
          case CURRENT_DATE:
            rewriteCurrentDateFunction(parameters);
            break;
          case DATE:
            rewrittenExpression = rewriteDateFunction(function, parameters);
            break;
          case DATE_ADD:
            rewriteDateAddFunction(parameters);
            break;
          case DATE_DIFF:
            rewriteDateDiffFunction(function, parameters);
            break;
          case DATE_SUB:
            // Google BigQuery DATE_SUB means Subtract interval from Date
            // shall be translated to DATE_ADD
            rewriteDateSubFunction(function, parameters);
            break;
          case DATE_TRUNC:
            rewriteDateTruncFunction(function, parameters);
            break;
          case EXTRACT:
            // Extract is a specific `ExtractExpression`
            break;
          case FORMAT_DATE:
            rewriteDateFormatFunction(function, parameters);
            break;
          case NVL:
            function.setName("Coalesce");
            break;
      }
    }
    if (rewrittenExpression==null) {
      super.visit(function);
    }
  }

  private static void rewriteDateFormatFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> reversedParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:
        // enforce DATE casting
        reversedParameters.add(parameters.get(1) instanceof StringValue
                               ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                                       .withValue(parameters.get(1).toString())
                               :parameters.get(1));

        // pass through the format parameter string
        // @todo: parse and replace those parameters where necessary
        reversedParameters.add(parameters.get(0));

        function.setName("StrfTime");
        function.setParameters(reversedParameters);
    }
  }

  private void rewriteDateTruncFunction(Function function, ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 2:
        ExpressionList<Expression> reversedParameters = new ExpressionList<>();

        // Date Part "ISOWEEK" exists and is not supported on DuckDB
        if (parameterWEEK(parameters, 1)) {
          reversedParameters.add(new StringValue("WEEK"));
          buffer.append(" /*APPROXIMATION: WEEK*/ ");
        } else if (parameters.get(1) instanceof Column && ((Column) parameters.get(1)).toString()
                .replaceAll(" ", "").equalsIgnoreCase("ISOWEEK")) {
          reversedParameters.add(new StringValue("WEEK"));
        } else {
          // translate DAY into String 'DAY'
          reversedParameters.add(!(parameters.get(1) instanceof StringValue)
                                 ? new StringValue(parameters.get(1).toString())
                                 :parameters.get(1));
        }

        // enforce DATE casting
        reversedParameters.add(parameters.get(0) instanceof StringValue
                               ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                                       .withValue(((StringValue) parameters.get(0)).toString())
                               :parameters.get(0));

        function.setParameters(reversedParameters);
    }
  }

  private static void rewriteDateSubFunction(Function function, ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 2:
        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          String negatedParameter =
                  interval.getParameter().startsWith("-") ? interval.getParameter().substring(1)
                                                          : "-" + interval.getParameter();
          interval.setExpression(
                  new StringValue(negatedParameter + " " + interval.getIntervalType()));
          interval.setIntervalType("");
          function.setName("DATE_ADD");
        }
    }
  }

  private void rewriteDateDiffFunction(Function function, ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 3:
        ExpressionList<Expression> reversedParameters = new ExpressionList<>();
        // Date Part "ISOWEEK" exists and is not supported on DuckDB
        if (parameterWEEK(parameters, 2)) {
          reversedParameters.add(new StringValue("WEEK"));
          buffer.append(" /*APPROXIMATION: WEEK*/ ");
        } else if (parameters.get(2) instanceof Column && ((Column) parameters.get(2)).toString()
                .replaceAll(" ", "").equalsIgnoreCase("ISOWEEK")) {
          reversedParameters.add(new StringValue("WEEK"));
        } else {
          // translate DAY into String 'DAY'
          reversedParameters.add(!(parameters.get(2) instanceof StringValue)
                                 ? new StringValue(parameters.get(2).toString())
                                 :parameters.get(2));
        }

        // enforce DATE casting
        reversedParameters.add(parameters.get(1) instanceof StringValue
                               ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                                       .withValue(((StringValue) parameters.get(1)).toString())
                               :parameters.get(1));

        // enforce DATE casting
        reversedParameters.add(parameters.get(0) instanceof StringValue
                               ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                                       .withValue(((StringValue) parameters.get(0)).toString())
                               :parameters.get(0));
        function.setParameters(reversedParameters);
    }
  }

  private static void rewriteDateAddFunction(ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 2:
        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          interval.setExpression(
                  new StringValue(interval.getParameter() + " " + interval.getIntervalType()));
          interval.setIntervalType("");
        }
    }
  }

  private Expression rewriteDateFunction(Function function, ExpressionList<?> parameters) {
    CastExpression castExpression = null;
    switch (parameters.size()) {
      case 1:
        // DATE(DATETIME '2016-12-25 23:59:59') AS date_dt
      case 2:
        // DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz
        buffer.append(" /*APPROXIMATION: timezone not supported*/ ");
        castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
                .withType(new ColDataType().withDataType("DATE"));
        visit(castExpression);
        break;
      case 3:
        function.setName("MAKE_DATE");
        break;
    }
    return castExpression;
  }

  private void rewriteCurrentDateFunction(ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 1:
        // CURRENT_DATE(timezone) is not supported in DuckDB
        buffer.append(" /*APPROXIMATION: timezone not supported*/ ");
        parameters.clear();
    }
  }

  public void visit(ExtractExpression extractExpression) {
    //@todo: JSQLParser Extract Expression must support `WEEK(MONDAY) .. WEEK(SUNDAY)`

    if (extractExpression.getName().equalsIgnoreCase("WEEK")) {
      buffer.append(" /*APPROXIMATION: WEEK*/ ");
      extractExpression.setName("WEEK");
    } else if (extractExpression.getName().equalsIgnoreCase("ISOWEEK")) {
      extractExpression.setName("WEEK");
    }

    if (extractExpression.getExpression() instanceof StringValue) {
      extractExpression.setExpression(
              new DateTimeLiteralExpression()
                      .withType(DateTimeLiteralExpression.DateTime.DATE)
                      .withValue(extractExpression.toString())
      );
    }
    super.visit(extractExpression);
  }
}
