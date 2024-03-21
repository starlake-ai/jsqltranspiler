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
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
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
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class ExpressionTranspiler extends ExpressionDeParser {
  enum TranspiledFunction {
    CURRENT_DATE, CURRENT_DATETIME, CURRENT_TIME, CURRENT_TIMESTAMP

    , DATE, DATETIME, TIME, TIMESTAMP, DATE_ADD, DATETIME_ADD, TIME_ADD, DATE_DIFF, DATETIME_DIFF, TIME_DIFF, DATE_SUB, DATETIME_SUB, TIME_SUB, DATE_TRUNC, DATETIME_TRUNC, TIME_TRUNC, EXTRACT, FORMAT_DATE, FORMAT_DATETIME, FORMAT_TIME, LAST_DAY,

    PARSE_DATE, PARSE_DATETIME, PARSE_TIME, DATE_FROM_UNIX_DATE, UNIX_DATE

    , NVL;


    @SuppressWarnings({"PMD.EmptyCatchBlock"})
    public static TranspiledFunction from(String name) {
      TranspiledFunction function = null;
      try {
        function = Enum.valueOf(TranspiledFunction.class, name.toUpperCase());
      } catch (Exception ignore) {
        // nothing to do here
      }
      return function;
    }

    public static TranspiledFunction from(Function f) {
      return from(f.getName());
    }
  }

  enum UnsupportedFunction {
    NOTHING;

    @SuppressWarnings({"PMD.EmptyCatchBlock"})
    public static UnsupportedFunction from(String name) {
      UnsupportedFunction function = null;
      try {
        function = Enum.valueOf(UnsupportedFunction.class, name.toUpperCase());
      } catch (Exception ignore) {
        // nothing to do here
      }
      return function;
    }

    public static UnsupportedFunction from(Function f) {
      return from(f.getName());
    }
  }

  public ExpressionTranspiler(SelectVisitor selectVisitor, StringBuilder buffer) {
    super(selectVisitor, buffer);
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

  public static boolean hasTimeZoneInfo(String timestampStr) {
    // Regular expression to match timezone offset with optional minutes part
    final Pattern pattern = Pattern.compile("\\+\\d{2}(:\\d{2})?$");
    // If the string matches the regular expression, it contains timezone information
    return pattern.matcher(timestampStr.replaceAll("\\'", "")).find();
  }

  public static boolean hasTimeZoneInfo(Expression timestamp) {
    if (timestamp instanceof DateTimeLiteralExpression) {
      // @todo: improve JSQLParser so `getValue()` will return the unquoted String
      return hasTimeZoneInfo(((DateTimeLiteralExpression) timestamp).getValue());
    } else if (timestamp instanceof StringValue) {
      return hasTimeZoneInfo(((StringValue) timestamp).getValue());
    } else {
      throw new RuntimeException(
          "Only StringValue or DateTimeLiteralExpression can be tested for TimeZoneInfo.");
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

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public void visit(Function function) {
    String functionName = function.getName();

    if (UnsupportedFunction.from(function) != null) {
      throw new RuntimeException(
          "Unsupported: " + functionName + " is not supported by DuckDB (yet).");
    } else if (functionName.endsWith("$$")) {
      // work around for transpiling already transpiled functions twice
      // @todo: figure out a better way to achieve that
      function.setName(functionName.substring(0, functionName.length() - 2));
      super.visit(function);
      return;
    }

    Expression rewrittenExpression = null;
    ExpressionList<?> parameters = function.getParameters();
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case CURRENT_DATE:
        case CURRENT_DATETIME:
        case CURRENT_TIME:
        case CURRENT_TIMESTAMP:
          rewriteCurrentDateFunction(parameters);
          break;
        case DATE:
          rewrittenExpression = rewriteDateFunction(function, parameters);
          break;
        case DATETIME:
          rewrittenExpression = rewriteDateTimeFunction(parameters);
          break;
        case TIME:
          rewrittenExpression = rewriteTimeFunction(function, parameters);
          break;
        case TIMESTAMP:
          rewrittenExpression = rewriteTimestampFunction(parameters);
          break;
        case DATE_ADD:
        case DATETIME_ADD:
        case TIME_ADD:
          rewriteDateAddFunction(function, parameters);
          break;
        case DATE_DIFF:
          rewriteDateDiffFunction(function, parameters, DateTimeLiteralExpression.DateTime.DATE);
          break;
        case DATETIME_DIFF:
          rewriteDateDiffFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.DATETIME);
          break;
        case TIME_DIFF:
          rewriteDateDiffFunction(function, parameters, DateTimeLiteralExpression.DateTime.TIME);
          break;
        case DATE_SUB:
        case DATETIME_SUB:
        case TIME_SUB:
          // Google BigQuery DATE_SUB means Subtract interval from Date
          // shall be translated to DATE_ADD
          rewriteDateSubFunction(function, parameters);
          break;
        case DATE_TRUNC:
          rewriteDateTruncFunction(function, parameters, DateTimeLiteralExpression.DateTime.DATE);
          break;
        case DATETIME_TRUNC:
          rewriteDateTruncFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.DATETIME);
          break;
        case TIME_TRUNC:
          rewrittenExpression = rewriteDateTruncFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIME);
          break;
        case EXTRACT:
          // Extract is a specific `ExtractExpression`
          break;
        case FORMAT_DATE:
          rewriteFormatDateFunction(function, parameters, DateTimeLiteralExpression.DateTime.DATE);
          break;
        case FORMAT_DATETIME:
          rewriteFormatDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.DATETIME);
          break;
        case FORMAT_TIME:
          rewriteFormatDateFunction(function, parameters, DateTimeLiteralExpression.DateTime.TIME);
          break;
        case LAST_DAY:
          rewriteLastDayFunction(function, parameters);
          break;
        case PARSE_DATE:
          rewrittenExpression = rewriteParseDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.DATE);
          break;
        case PARSE_DATETIME:
          rewrittenExpression = rewriteParseDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.DATETIME);
          break;
        case PARSE_TIME:
          rewrittenExpression = rewriteParseDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIME);
          break;
        case UNIX_DATE:
          rewriteUnixDateFunction(function, parameters);
          break;
        case DATE_FROM_UNIX_DATE:
          rewriteDateFromUnixFunction(function, parameters);
          break;
        case NVL:
          function.setName("Coalesce");
          break;
      }
    }
    if (rewrittenExpression == null) {
      super.visit(function);
    }
  }


  private void rewriteDateFromUnixFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 1:
        // Epoch
        newParameters.add(new DateTimeLiteralExpression()
            .withType(DateTimeLiteralExpression.DateTime.DATE).withValue("'1970-01-01'"));

        // INTERVAL " 1 DAY"
        newParameters.add(new IntervalExpression()
            .withExpression(new StringValue(parameters.get(0).toString() + " DAY")));

        function.setParameters(newParameters);
        function.setName("DATE_ADD");
    }
  }

  private void rewriteUnixDateFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 1:
        // Date Part
        newParameters.add(new StringValue("DAY"));

        // Epoch
        newParameters.add(new DateTimeLiteralExpression()
            .withType(DateTimeLiteralExpression.DateTime.DATE).withValue("'1970-01-01'"));

        // enforce DATE casting
        newParameters.add(parameters.get(0) instanceof StringValue
            ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                .withValue(parameters.get(0).toString())
            : parameters.get(0));

        function.setParameters(newParameters);
        function.setName("DATE_DIFF");
    }
  }

  private Expression rewriteParseDateFunction(Function function, ExpressionList<?> parameters,
      DateTimeLiteralExpression.DateTime dateTimeType) {
    ExpressionList<Expression> reversed = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:
        /* translate format parameters
          %e --> %-d
        */
        reversed.add(parameters.get(1));

        if (parameters.get(0) instanceof StringValue) {
          reversed.add(translateFormatStr((StringValue) parameters.get(0)));
        } else {
          reversed.add(parameters.get(0));
        }
        function.setName("strptime");
        function.setParameters(reversed);

        CastExpression castExpression = new CastExpression().withLeftExpression(function)
            .withType(new ColDataType(dateTimeType.name()));
        visit(castExpression);

        return castExpression;
      default:
        return function;
    }
  }

  private static StringValue translateFormatStr(StringValue formatStringValue) {
    String formatStr = formatStringValue.getValue();
    // The day of month as a decimal number (1-31); single digits are preceded by a space.
    formatStr = formatStr.replaceAll("%e", "%-d");

    // The hour (24-hour clock) as a decimal number (0-23); single digits are preceded by a
    // space.
    formatStr = formatStr.replaceAll("%k", "%-H");

    // The hour (12-hour clock) as a decimal number (1-12); single digits are preceded by a
    // space.
    formatStr = formatStr.replaceAll("%l", "%-I");

    // The date and time representation (English). Example: Wed Jan 20 21:47:00 2021
    formatStr = formatStr.replaceAll("%c", "%a %b %-d %-H:%M:%S %Y");

    // The time in the format %H:%M. Example: 21:47
    formatStr = formatStr.replaceAll("%R", "%H:%M");
    return new StringValue(formatStr);
  }

  private void rewriteLastDayFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:
        if ("MONTH".equalsIgnoreCase(parameters.get(1).toString())) {
          parameters.remove(1);
        } else {
          // todo: check if we can rewrite for YEAR, QUARTER and WEEK
          throw new RuntimeException(
              "Unsupported: LAST_DAT(date, part) is not supported by DuckDB.");
        }
      case 1:
        // enforce DATE casting
        newParameters.add(parameters.get(0) instanceof StringValue
            ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                .withValue(parameters.get(0).toString())
            : parameters.get(0));
        function.setParameters(newParameters);
    }
  }

  private static void rewriteFormatDateFunction(Function function, ExpressionList<?> parameters,
      DateTimeLiteralExpression.DateTime dateTimeType) {
    ExpressionList<Expression> reversedParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:

        Expression dateTimeExpression =
            parameters.get(1) instanceof StringValue ? new DateTimeLiteralExpression()
                .withType(dateTimeType).withValue(parameters.get(1).toString()) : parameters.get(1);

        // DuckDB does not support "StrFTime( TIME expression, format)", see
        // https://github.com/duckdb/duckdb/discussions/11263
        if (DateTimeLiteralExpression.DateTime.TIME == dateTimeType) {
          dateTimeExpression =
              new Addition().withLeftExpression(new Function().withName("CURRENT_DATE"))
                  .withRightExpression(dateTimeExpression);
        }
        // enforce DATE casting
        reversedParameters.add(dateTimeExpression);

        // try to rewrite the formatting parameters
        if (parameters.get(0) instanceof StringValue) {
          reversedParameters.add(translateFormatStr((StringValue) parameters.get(0)));
        } else {
          reversedParameters.add(parameters.get(0));
        }

        function.setName("StrfTime");
        function.setParameters(reversedParameters);
    }
  }

  private Expression rewriteDateTruncFunction(Function function, ExpressionList<?> parameters,
      DateTimeLiteralExpression.DateTime dateTimeType) {
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
              : parameters.get(1));
        }

        // DuckDB does not support TRUNC on TIME, see
        // https://github.com/duckdb/duckdb/discussions/11264
        Expression dateTimeExpression =
            parameters.get(0) instanceof StringValue ? new DateTimeLiteralExpression()
                .withType(dateTimeType).withValue(parameters.get(0).toString()) : parameters.get(0);

        if (DateTimeLiteralExpression.DateTime.TIME == dateTimeType) {
          dateTimeExpression =
              new Addition().withLeftExpression(new Function().withName("CURRENT_DATE"))
                  .withRightExpression(dateTimeExpression);
        }

        // enforce DATE casting
        reversedParameters.add(dateTimeExpression);

        function.setName("DATE_TRUNC");
        function.setParameters(reversedParameters);

        // DuckDB does not support TRUNC on TIME, see
        // https://github.com/duckdb/duckdb/discussions/11264
        // so rewrite into a CastExpression which will be visited instead of the function
        if (DateTimeLiteralExpression.DateTime.TIME == dateTimeType) {
          // flag the function so it would not be transpiled again
          function.setName("DATE_TRUNC$$");
          CastExpression castExpression = new CastExpression().withLeftExpression(function)
              .withType(new ColDataType(dateTimeType.name()));
          visit(castExpression);
          return castExpression;
        }
    }
    return null;
  }

  private static void rewriteDateSubFunction(Function function, ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 2:
        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          String negatedParameter =
              interval.getParameter().startsWith("-") ? interval.getParameter().substring(1)
                  : "-" + interval.getParameter();
          interval
              .setExpression(new StringValue(negatedParameter + " " + interval.getIntervalType()));
          interval.setIntervalType("");
          function.setName("DATE_ADD");
        }
    }
  }

  private void rewriteDateDiffFunction(Function function, ExpressionList<?> parameters,
      DateTimeLiteralExpression.DateTime dateTimeType) {
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
              : parameters.get(2));
        }

        // enforce DATE casting
        reversedParameters
            .add(
                parameters.get(1) instanceof StringValue
                    ? new DateTimeLiteralExpression().withType(dateTimeType)
                        .withValue(((StringValue) parameters.get(1)).toString())
                    : parameters.get(1));

        // enforce DATE casting
        reversedParameters
            .add(
                parameters.get(0) instanceof StringValue
                    ? new DateTimeLiteralExpression().withType(dateTimeType)
                        .withValue(((StringValue) parameters.get(0)).toString())
                    : parameters.get(0));
        function.setParameters(reversedParameters);
        function.setName("DATE_DIFF");
    }
  }

  private static void rewriteDateAddFunction(Function function, ExpressionList<?> parameters) {
    switch (parameters.size()) {
      case 2:
        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          interval.setExpression(
              new StringValue(interval.getParameter() + " " + interval.getIntervalType()));
          interval.setIntervalType("");
        }
        function.setName("DATE_ADD");
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

  private Expression rewriteDateTimeFunction(ExpressionList<?> parameters) {
    CastExpression castExpression = null;
    switch (parameters.size()) {
      case 6:
        Function dateFuncttion = new Function().withName("MAKE_DATE")
            .withParameters(parameters.get(0), parameters.get(1), parameters.get(2));

        Function timeFuncttion = new Function().withName("MAKE_TIME")
            .withParameters(parameters.get(3), parameters.get(4), parameters.get(5));
        Addition add =
            new Addition().withLeftExpression(dateFuncttion).withRightExpression(timeFuncttion);
        castExpression = new CastExpression("Cast").withLeftExpression(add)
            .withType(new ColDataType().withDataType("DATETIME"));
        visit(castExpression);
        break;
      case 2:
        if (parameters.get(0) instanceof DateTimeLiteralExpression
            && parameters.get(1) instanceof DateTimeLiteralExpression) {
          add = new Addition().withLeftExpression(parameters.get(0))
              .withRightExpression(parameters.get(1));
          castExpression = new CastExpression("Cast").withLeftExpression(add)
              .withType(new ColDataType().withDataType("DATETIME"));
          visit(castExpression);
        } else if (parameters.get(0) instanceof DateTimeLiteralExpression
            && ((DateTimeLiteralExpression) parameters.get(0))
                .getType() == DateTimeLiteralExpression.DateTime.TIMESTAMP
            && parameters.get(1) instanceof StringValue) {

          buffer.append(" /*APPROXIMATION: timezone not supported*/ ");
          castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
              .withType(new ColDataType().withDataType("DATETIME"));
          visit(castExpression);
        } else {
          // @todo: veryify if this needs to be ammended
          throw new RuntimeException("Unsupported: DATETIME(string, string) is not supported yet.");
        }
        break;
    }
    return castExpression;
  }

  private Expression rewriteTimeFunction(Function function, ExpressionList<?> parameters) {
    CastExpression castExpression = null;
    switch (parameters.size()) {
      case 1:
        // TIME(DATETIME '2016-12-25 23:59:59') AS date_dt
      case 2:
        // TIME(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz
        buffer.append(" /*APPROXIMATION: timezone not supported*/ ");
        castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
            .withType(new ColDataType().withDataType("TIME"));
        visit(castExpression);
        break;
      case 3:
        function.setName("MAKE_TIME");
        break;
    }
    return castExpression;
  }

  private Expression rewriteTimestampFunction(ExpressionList<?> parameters) {
    /*
    TIMESTAMP(string_expression[, time_zone])
    TIMESTAMP(date_expression[, time_zone])
    TIMESTAMP(datetime_expression[, time_zone])
    */

    if (parameters != null && !parameters.isEmpty()) {
      CastExpression castExpression;
      String timestampType = (parameters.get(0) instanceof StringValue
          || parameters.get(0) instanceof DateTimeLiteralExpression)
          && hasTimeZoneInfo(parameters.get(0)) ? "TIMESTAMPTZ" : "TIMESTAMP";
      switch (parameters.size()) {
        case 1:
          castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
              .withType(new ColDataType().withDataType(timestampType));
          visit(castExpression);
          return castExpression;
        case 2:
          castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
              .withType(new ColDataType().withDataType(timestampType));
          TimezoneExpression timezoneExpression =
              new TimezoneExpression(castExpression, parameters.get(1));
          visit(timezoneExpression);
          return timezoneExpression;
      }
    }
    return null;
  }

  private void rewriteCurrentDateFunction(ExpressionList<?> parameters) {
    if (parameters != null) {
      switch (parameters.size()) {
        case 1:
          // CURRENT_DATE(timezone) is not supported in DuckDB
          // CURRENT_DATETIME(timezone) is not supported in DuckDB
          buffer.append(" /*APPROXIMATION: timezone not supported*/ ");
          parameters.clear();
      }
    }
  }

  public void visit(ExtractExpression extractExpression) {
    // @todo: JSQLParser Extract Expression must support `WEEK(MONDAY) .. WEEK(SUNDAY)`

    if (extractExpression.getName().equalsIgnoreCase("WEEK")) {
      buffer.append(" /*APPROXIMATION: WEEK*/ ");
      extractExpression.setName("WEEK");
    } else if (extractExpression.getName().equalsIgnoreCase("ISOWEEK")) {
      extractExpression.setName("WEEK");
    }

    if (extractExpression.getExpression() instanceof StringValue) {
      extractExpression.setExpression(
          new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
              .withValue(extractExpression.toString()));
    }
    super.visit(extractExpression);
  }
}
