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

import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.StructType;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Expression transpiler.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLExpressionTranspiler extends ExpressionDeParser {
  enum TranspiledFunction {
    // @FORMATTER:OFF
    CURRENT_DATE, CURRENT_DATETIME, CURRENT_TIME, CURRENT_TIMESTAMP

    , DATE, DATETIME, TIME, TIMESTAMP

    , DATE_ADD, DATETIME_ADD, TIME_ADD, TIMESTAMP_ADD

    , DATE_DIFF, DATETIME_DIFF, TIME_DIFF, TIMESTAMP_DIFF

    , DATE_SUB, DATETIME_SUB, TIME_SUB, TIMESTAMP_SUB

    , DATE_TRUNC, DATETIME_TRUNC, TIME_TRUNC, TIMESTAMP_TRUNC

    , EXTRACT

    , FORMAT_DATE, FORMAT_DATETIME, FORMAT_TIME, FORMAT_TIMESTAMP

    , LAST_DAY

    , PARSE_DATE, PARSE_DATETIME, PARSE_TIME, PARSE_TIMESTAMP, DATE_FROM_UNIX_DATE, UNIX_DATE, TIMESTAMP_MICROS, TIMESTAMP_MILLIS, TIMESTAMP_SECONDS, UNIX_MICROS, UNIX_MILLIS, UNIX_SECONDS

    , STRING, BYTE_LENGTH, CHAR_LENGTH, CHARACTER_LENGTH, CODE_POINTS_TO_BYTES, CODE_POINTS_TO_STRING, COLLATE, CONTAINS_SUBSTR, EDIT_DISTANCE, FORMAT, INSTR, LENGTH, LPAD, NORMALIZE, NORMALIZE_AND_CASEFOLD, OCTET_LENGTH, REGEXP_CONTAINS, REGEXP_EXTRACT, REGEXP_EXTRACT_ALL, REGEXP_INSTR, REGEXP_REPLACE, REGEXP_SUBSTR, REPEAT, REPLACE, REVERSE, RPAD, SAFE_CONVERT_BYTES_TO_STRING, TO_CODE_POINTS, TO_HEX, UNICODE

    , DIV, IEEE_DIVIDE, IS_INF, IS_NAN, LOG, RAND, RANGE_BUCKET, ROUND

    , SAFE_ADD, SAFE_DIVIDE, SAFE_MULTIPLY, SAFE_NEGATE, SAFE_SUBTRACT, TRUNC

    , ARRAY_CONCAT_AGG, COUNTIF, LOGICAL_AND, LOGICAL_OR, ARRAY, ARRAY_CONCAT, ARRAY_TO_STRING, GENERATE_ARRAY, GENERATE_DATE_ARRAY, GENERATE_TIMESTAMP_ARRAY

    , FIRST_VALUE, LAST_VALUE, PERCENTILE_CONT, PERCENTILE_DISC

    , GENERATE_UUID

    , NVL, UNNEST;
    // @FORMATTER:ON


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
    ASINH, ACOSH, COSH, SINH, COTH, COSINE_DISTANCE, CSC, CSCH, EUCLIDEAN_DISTANCE, SEC, SECH

    , APPROX_QUANTILES, APPROX_TOP_COUNT, APPROX_TOP_SUM

    , SEARCH, VECTOR_SEARCH, APPENDS, EXTERNAL_OBJECT_TRANSFORM, GAP_FILL

    ;

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

    public static UnsupportedFunction from(AnalyticExpression f) {
      return from(f.getName());
    }
  }

  public JSQLExpressionTranspiler(JSQLTranspiler transpiler, StringBuilder buffer) {
    super(transpiler, buffer);
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
    return pattern.matcher(timestampStr.replaceAll("'", "")).find();
  }

  public static boolean hasTimeZoneInfo(Expression timestamp) {
    if (timestamp instanceof DateTimeLiteralExpression) {
      // @todo: improve JSQLParser so `getValue()` will return the unquoted String
      return hasTimeZoneInfo(((DateTimeLiteralExpression) timestamp).getValue());
    } else if (timestamp instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) timestamp;
      return (castExpression.isTimeStamp() || castExpression.isTime())
          && hasTimeZoneInfo(castExpression.getLeftExpression());
    } else if (timestamp instanceof StringValue) {
      return hasTimeZoneInfo(((StringValue) timestamp).getValue());
    } else {
      return false;
    }
  }

  public static Expression rewriteDateLiteral(Expression p,
      DateTimeLiteralExpression.DateTime dateTimeType) {

    if (p instanceof StringValue && dateTimeType != null) {
      StringValue stringValue = (StringValue) p;

      if (dateTimeType == DateTimeLiteralExpression.DateTime.TIMESTAMP
          && hasTimeZoneInfo(stringValue)) {
        // convert to TIMESTAMPTZ when time zone info is present
        return new DateTimeLiteralExpression()
            .withType(DateTimeLiteralExpression.DateTime.TIMESTAMPTZ)
            .withValue(stringValue.toString());
      } else {
        return new DateTimeLiteralExpression().withType(dateTimeType)
            .withValue(stringValue.toString());
      }
    } else if (p instanceof DateTimeLiteralExpression) {
      DateTimeLiteralExpression dateTimeLiteralExpression = (DateTimeLiteralExpression) p;
      if (dateTimeLiteralExpression.getType() == DateTimeLiteralExpression.DateTime.TIMESTAMP
          && hasTimeZoneInfo(dateTimeLiteralExpression)) {
        dateTimeLiteralExpression.setType(DateTimeLiteralExpression.DateTime.TIMESTAMPTZ);
      }
      return dateTimeLiteralExpression;
    } else if (p instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) p;
      if (castExpression.getColDataType().getDataType().equalsIgnoreCase("TIMESTAMP")
          && hasTimeZoneInfo(castExpression.getLeftExpression())) {
        castExpression.getColDataType().setDataType("TIMESTAMPTZ");
      }
      return castExpression;
    } else {
      return p;
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

    if (function.getMultipartName().size() > 1
        && function.getMultipartName().get(0).equalsIgnoreCase("SAFE")) {
      warning("SAFE prefix is not supported.");
      function.getMultipartName().remove(0);
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
          if (parameters != null) {
            switch (parameters.size()) {
              case 1:
                // CURRENT_DATE(timezone)
                // CURRENT_DATETIME(timezone)
                rewrittenExpression =
                    new TimezoneExpression(function.withParameters(), parameters.get(0));
                break;
            }
          }
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
        case TIMESTAMP_ADD:
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
        case TIMESTAMP_DIFF:
          rewriteDateDiffFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIMESTAMP);
          break;
        case DATE_SUB:
        case DATETIME_SUB:
        case TIME_SUB:
        case TIMESTAMP_SUB:
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
        case TIMESTAMP_TRUNC:
          rewrittenExpression = rewriteDateTruncFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIMESTAMP);
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
        case FORMAT_TIMESTAMP:
          rewriteFormatDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIMESTAMP);
          break;
        case STRING:
          rewriteStringFunction(function, parameters);
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
        case PARSE_TIMESTAMP:
          rewrittenExpression = rewriteParseDateFunction(function, parameters,
              DateTimeLiteralExpression.DateTime.TIMESTAMP);
          break;
        case UNIX_DATE:
          rewriteUnixDateFunction(function, parameters);
          break;
        case DATE_FROM_UNIX_DATE:
          rewriteDateFromUnixFunction(function, parameters);
          break;
        case TIMESTAMP_MICROS:
          function.setName("MAKE_TIMESTAMP");
          break;
        case TIMESTAMP_MILLIS:
          function.setName("EPOCH_MS");
          break;
        case TIMESTAMP_SECONDS:
          // work around:
          CastExpression castExpression = new CastExpression().withLeftExpression(parameters.get(0))
              .withType(new ColDataType("INT64"));

          function.setName("EPOCH_MS");
          function.setParameters(new ExpressionList<Expression>(new Multiplication()
              .withLeftExpression(castExpression).withRightExpression(new LongValue(1000))));
          break;
        case UNIX_MICROS:
          function.setName("EPOCH_US");
          break;
        case UNIX_MILLIS:
          function.setName("EPOCH_MS");
          break;
        case UNIX_SECONDS:
          function.setName("EPOCH");
          break;
        case NVL:
          function.setName("Coalesce");
          break;
        case BYTE_LENGTH:
        case OCTET_LENGTH:
          // OCTET_LENGTH( CASE TypeOf('français') WHEN 'VARCHAR' THEN encode('français') ELSE
          // Try_Cast('français' AS BLOB) END ) AS bytes
          CaseExpression caseExpression =
              new CaseExpression(new CastExpression("Try_Cast", parameters.get(0), "BLOB"),
                  new WhenClause(new StringValue("VARCHAR"),
                      new Function("Encode", new CastExpression(parameters.get(0), "VARCHAR"))))
                  .withSwitchExpression(new Function("TypeOf", parameters.get(0)));
          function.setName("OCTET_LENGTH");
          function.setParameters(caseExpression);
          break;
        case CHAR_LENGTH:
        case CHARACTER_LENGTH:
          function.setName("Length");
          break;
        case CODE_POINTS_TO_BYTES:
          rewrittenExpression = rewriteCodePointsToBytes(parameters);
          break;
        case CODE_POINTS_TO_STRING:
          rewrittenExpression = rewriteCodePointsToString(parameters);
          break;
        case COLLATE:
          function.setName("icu_sort_key");
          break;
        case CONTAINS_SUBSTR:
          rewrittenExpression = rewriteContainsSubStr(parameters);
        case EDIT_DISTANCE:
          function.setName("levenshtein");
          break;
        case FORMAT:
          function.setName("printf");
          // flags not working:
          // %t the string representation of the value, e.g. '2023-12-31'
          // %T the TYPE STRING representation of the value, e.g. DATE '2023-12-31'
          if (parameters.get(0) instanceof StringValue) {
            String s = ((StringValue) parameters.get(0)).getValue();
            if (s.contains("%t")) {
              warning("Format %t is not supported");
              s = s.replaceAll("%t", "%s");
            }
            if (s.contains("%T")) {
              warning("Format %T is not supported");
              s = s.replaceAll("%T", "%s");
            }

            function.setParameters(new ExpressionList<>(new StringValue(s), parameters.get(1)));
          }
          break;
        case INSTR:
          if (parameters != null && parameters.size() == 2) {
            // pass through
            break;
          } else {
            throw new RuntimeException(
                "`INSTR` does not support the parameters `position` or `occurrence` yet.");
          }
        case LENGTH:
          rewrittenExpression = rewriteLength(parameters);
          break;
        case LPAD:
        case RPAD:
          rewrittenExpression = rewritePad(function, parameters);
          break;
        case NORMALIZE:
          if (parameters != null && parameters.size() == 2
              && !"NFC".equalsIgnoreCase(parameters.get(1).toString())) {
            warning("NORMALIZE only supported for NFC, but not for NFKC, NFD, NFKD yet.");
          }
          if (parameters != null && parameters.size() == 2) {
            parameters.remove(1);
          }
          function.setName("NFC_NORMALIZE");
          break;
        case NORMALIZE_AND_CASEFOLD:
          if (parameters != null && parameters.size() == 2
              && !"NFC".equalsIgnoreCase(parameters.get(1).toString())) {
            warning(
                "NORMALIZE_AND_CASEFOLD only supported for NFC, but not for NFKC, NFD, NFKD yet.");
          }
          if (parameters != null && parameters.size() == 2) {
            parameters.remove(1);
          }
          function.setName("NFC_NORMALIZE");
          function.setParameters(new Function("lower", parameters));
          break;
        case REGEXP_CONTAINS:
          function.setName("REGEXP_MATCHES");
          break;
        case REGEXP_EXTRACT:
        case REGEXP_SUBSTR:
          if (parameters != null && parameters.size() > 2) {
            warning("REGEXP_EXTRACT supports only 2 parameters.");
            while (parameters.size() > 2) {
              parameters.remove(parameters.size() - 1);
            }
          }
          function.setName("REGEXP_EXTRACT");
          break;
        case REGEXP_EXTRACT_ALL:
          // pass through
          break;
        case REGEXP_INSTR:
          if (parameters != null && parameters.size() > 2) {
            warning("REGEXP_INSTR supports only 2 parameters.");
            while (parameters.size() > 2) {
              parameters.remove(parameters.size() - 1);
            }
          }
          /*
          CASE
                WHEN Regexp_Matches( source_value, reg_exp )
                    THEN Instr( source_value, Regexp_Extract( source_value, reg_exp ) )
                ELSE 0
            END AS instr
           */
          WhenClause when =
              new WhenClause(new Function("REGEXP_MATCHES", parameters.get(0), parameters.get(1)),
                  new Function("INSTR", parameters.get(0),
                      new Function("REGEXP_EXTRACT", parameters.get(0), parameters.get(1))));
          rewrittenExpression = new CaseExpression(new LongValue(0), when);
          break;
        case REGEXP_REPLACE:
          // pass through
          break;
        case UNNEST:
          if (parameters != null) {
            switch (parameters.size()) {
              case 1:
                boolean recursive = false;
                if (parameters.get(0) instanceof ArrayConstructor) {
                  ArrayConstructor arrayConstructor = (ArrayConstructor) parameters.get(0);
                  for (Expression e : arrayConstructor.getExpressions()) {
                    if (e instanceof StructType || e instanceof ParenthesedExpressionList) {
                      recursive = true;
                      break;
                    }
                  }
                }

                if (recursive) {
                  function.setParameters(parameters.get(0),
                      new OracleNamedFunctionParameter("recursive", new Column("TRUE")));
                }
            }
          }
          break;
        case SAFE_CONVERT_BYTES_TO_STRING:
          warning("SAFE_CONVERT_BYTES_TO_STRING is not supported");
          function.setName("decode");
          break;
        case TO_CODE_POINTS:
          // TO_CODE_POINTS(word) as code_points
          //
          // list_transform( split(word, ''), x -> unicode(x) ) as code_points

          function.setName("List_Transform");
          function.setParameters(new Function("Split", parameters.get(0), new StringValue("")),
              new LambdaExpression(Arrays.asList("x"), new Function("Unicode$$", new Column("x"))));
          break;
        case TO_HEX:
          function.setParameters(new Function("Decode", parameters.get(0)));
          break;
        case UNICODE:
          rewrittenExpression = new Function("If",
              new EqualsTo(new Function("Length$$", parameters.get(0)), new LongValue(0)),
              new LongValue(0), function.withName(function.getName() + "$$"));
          break;
        case DIV:
        case IEEE_DIVIDE:
          function.setName("Divide");
          break;
        case IS_INF:
          function.setName("IsInf");
          break;
        case IS_NAN:
          function.setName("IsNan");
          break;
        case LOG:
          if (parameters != null) {
            switch (parameters.size()) {
              case 1:
                function.setName("Ln");
                break;
              case 2:
                function.setName("Divide");
                function.setParameters(new Function("Ln", parameters.get(0)),
                    new Function("Ln", parameters.get(1)));
                break;
            }
          }
          break;
        case RAND:
          function.setName("Random");
          break;
        case RANGE_BUCKET:
          if (parameters != null && parameters.size() == 2) {
            // Len( List_Filter( [0, 10, 20, 30, 40], x -> x <= 20 ) ) a
            Function filter = new Function("List_Filter", parameters.get(1),
                new LambdaExpression("x", new MinorThanEquals(new Column("x"), parameters.get(0))));
            function.setName("Len");
            function.setParameters(filter);
          }
          break;
        case ROUND:
          if (parameters != null) {
            switch (parameters.size()) {
              case 1:
                function.setParameters(parameters.get(0), new LongValue(0));
                break;
              case 3:
                if (parameters.get(2).toString().toUpperCase().contains("ROUND_HALF_EVEN")) {
                  function.setName("Round_Even");
                }
                function.setParameters(parameters.get(0), parameters.get(1));
                break;
            }
          }
          break;
        case SAFE_ADD:
        case SAFE_DIVIDE:
        case SAFE_MULTIPLY:
        case SAFE_SUBTRACT:
          warning("SAFE variant not supported");
          function.setName(functionName.substring("SAFE_".length()));
          break;
        case SAFE_NEGATE:
          warning("SAFE variant not supported");
          function.setName("Multiply");
          function.setParameters(parameters.get(0), new LongValue(-1));
          break;
        case TRUNC:
          if (parameters != null) {
            switch (parameters.size()) {
              case 2:
                function.setName("Round");
                break;
            }
          }
          break;
        case ARRAY_CONCAT_AGG:
          // list_sort(flatten(list(x)), 'ASC', 'NULLS FIRST'))
          function.setName("List_Sort");
          function.setParameters(new Function("Flatten", new Function("List", parameters.get(0))),
              new StringValue("ASC"), new StringValue("NULLS FIRST"));
          break;
        case COUNTIF:
          // COUNT(IF(x < 0, x, NULL))

          final Set<Column> expressionColumns = new HashSet<>();
          parameters.get(0).accept(new ExpressionVisitorAdapter() {
            public void visit(Column column) {
              expressionColumns.add(column);
            }
          });
          // @todo: clarify if there can be only exactly 1 column
          // else, what do to on None or many?
          assert expressionColumns.size() == 1;
          Column column = expressionColumns.toArray(new Column[0])[0];

          warning("Different NULL handling");
          function.setName("Count");
          function.setParameters(new Function("If", parameters.get(0), column, new NullValue()));

          break;
        case LOGICAL_AND:
          function.setName("Bool_And");
          break;
        case LOGICAL_OR:
          function.setName("Bool_Or");
          break;

        case ARRAY:
          function.setName("List_Sort");
          function.setParameters(new Function("Array$$", parameters));
          break;

        case ARRAY_CONCAT:
          rewrittenExpression =
              Concat.concat(parameters.toArray(new Expression[parameters.size()]));
          break;

        case ARRAY_TO_STRING:
          if (parameters != null) {
            switch (parameters.size()) {
              case 3:
                Expression p1 = parameters.get(0);
                Expression p2 = parameters.get(1);

                // turn it into a Lambda replacing the NULL values with 3rd parameter
                p1 = new Function("List_Transform", p1, new LambdaExpression("x",
                    new Function("Coalesce", new Column("x"), parameters.get(2))));
                function.setParameters(p1, p2);
            }
          }
          break;

        case GENERATE_ARRAY:
          function.setName("Generate_Series");
          break;

        case GENERATE_DATE_ARRAY:
          function.setName("Generate_Series");
          function.setParameters(new CastExpression(parameters.get(0), "DATE"),
              new CastExpression(parameters.get(1), "DATE"),
              new CastExpression(parameters.get(2), "INTERVAL"));
          rewrittenExpression = new CastExpression(function, "DATE[]");
          break;

        case GENERATE_TIMESTAMP_ARRAY:
          function.setName("Generate_Series");
          function.setParameters(new CastExpression(parameters.get(0), "TIMESTAMP"),
              new CastExpression(parameters.get(1), "TIMESTAMP"),
              new CastExpression(parameters.get(2), "INTERVAL"));
          break;

        case GENERATE_UUID:
          function.setName("UUID");
          break;
      }
    }
    if (rewrittenExpression == null) {
      super.visit(function);
    } else {
      rewrittenExpression.accept(this);
    }
  }

  public void visit(AnalyticExpression function) {
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
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case COUNTIF:
          // COUNT(IF(x < 0, x, NULL))

          final Set<Column> expressionColumns = new HashSet<>();
          function.getExpression().accept(new ExpressionVisitorAdapter() {
            public void visit(Column column) {
              expressionColumns.add(column);
            }
          });
          // @todo: clarify if there can be only exactly 1 column
          // else, what do to on None or many?
          assert expressionColumns.size() == 1;
          Column column = expressionColumns.toArray(new Column[0])[0];

          warning("Different NULL handling");
          function.setName("Count");
          function
              .setExpression(new Function("If", function.getExpression(), column, new NullValue()));

          break;
        case LOGICAL_AND:
          function.setName("Bool_And");
          break;
        case LOGICAL_OR:
          function.setName("Bool_Or");
          break;
        case FIRST_VALUE:
          function.setName("First");
          break;
        case LAST_VALUE:
          function.setName("First");
          break;
        case PERCENTILE_CONT:
          function.setName("Quantile_Cont");
          break;
        case PERCENTILE_DISC:
          function.setName("Quantile_Disc");
          break;
      }
    }
    if (rewrittenExpression == null) {
      super.visit(function);
    } else {
      rewrittenExpression.accept(this);
    }
  }

  private Expression rewriteLength(ExpressionList<?> parameters) {
    if (parameters != null) {
      switch (parameters.size()) {
        case 1:
          /*
          case typeof(encode('français'))
            when 'BLOB' then octet_length( try_cast(encode('français') AS BLOB))
            when 'VARCHAR' then length(try_cast(encode('français') AS VARCHAR))
            end as bytes
          */

          WhenClause whenChar = new WhenClause().withWhenExpression(new StringValue("VARCHAR"))
              .withThenExpression(new Function("Length$$")
                  .withParameters(new CastExpression("Try_Cast$$", parameters.get(0), "VARCHAR")));
          WhenClause whenBLOB = new WhenClause().withWhenExpression(new StringValue("BLOB"))
              .withThenExpression(new Function("octet_length$$")
                  .withParameters(new CastExpression("Try_Cast$$", parameters.get(0), "BLOB")));

          CaseExpression caseExpression = new CaseExpression(whenChar, whenBLOB)
              .withSwitchExpression(new Function("typeOf", parameters.get(0)));

          return caseExpression;
      }
    }
    return null;
  }

  private Expression rewritePad(Function function, ExpressionList<?> parameters) {
    if (parameters != null) {
      Expression padding = parameters.size() == 3 ? parameters.get(2) : new StringValue(" ");
      switch (parameters.size()) {
        case 2:
        case 3:
          WhenClause whenChar =
              new WhenClause().withWhenExpression(new StringValue("VARCHAR"))
                  .withThenExpression(new Function(function.getName() + "$$").withParameters(
                      new CastExpression(parameters.get(0), "VARCHAR"), parameters.get(1),
                      padding));
          // @todo: support bytes
          // WhenClause whenBLOB = new WhenClause()
          // .withWhenExpression(new StringValue("BLOB"))
          // .withThenExpression(new Function("octet_length").withParameters(new
          // CastExpression(parameters.get(0), "BLOB")));

          CaseExpression caseExpression = new CaseExpression(whenChar)
              .withSwitchExpression(new Function("typeOf", parameters.get(0)));

          return caseExpression;
      }
    }
    return null;
  }

  private Expression rewriteContainsSubStr(ExpressionList<?> parameters) {
    if (parameters != null) {
      switch (parameters.size()) {
        case 2:

          Concat concat = new Concat().withLeftExpression(new StringValue("%"))
              .withRightExpression(parameters.get(1));

          concat =
              new Concat().withLeftExpression(concat).withRightExpression(new StringValue("%"));

          LikeExpression like = new LikeExpression().setLikeKeyWord("ILIKE")
              .withLeftExpression(new Function("nfc_normalize", parameters.get(0)))
              .withRightExpression(new Function("nfc_normalize", concat));

          return like;
        case 3:
          throw new RuntimeException(
              "Function `CONTAINS_SUBST with `JSON_SCOPE` is not supported yet.");
      }
    }
    return null;
  }

  private Expression rewriteCodePointsToBytes(ExpressionList<?> parameters) {
    // select encode(string_agg(a, '')) bytes from (select chr(unnest([65, 98, 67, 100])) a)

    Function chr = new Function("Chr", new Function("UnNest", parameters.get(0)));
    PlainSelect select = new PlainSelect().withSelectItems(new SelectItem<Function>(chr, "a"));

    Function encode =
        new Function("Encode", new Function("String_Agg", new Column("a"), new StringValue("")));

    select = new PlainSelect().withSelectItems(new SelectItem<Function>(encode, "bytes"))
        .withFromItem(new ParenthesedSelect().withSelect(select));

    Parenthesis p = new Parenthesis(select);

    return p;
  }

  private Expression rewriteCodePointsToString(ExpressionList<?> parameters) {
    // select string_agg(a, '') characters from (select chr(unnest([65, 255, 513, 1024])) a)

    Function chr = new Function("Chr", new Function("UnNest", parameters.get(0)));
    PlainSelect select = new PlainSelect().withSelectItems(new SelectItem<Function>(chr, "a"));

    Function stringAgg = new Function("String_Agg", new Column("a"), new StringValue(""));

    select = new PlainSelect().withSelectItems(new SelectItem<Function>(stringAgg, "characters"))
        .withFromItem(new ParenthesedSelect().withSelect(select));

    Parenthesis p = new Parenthesis(select);

    return p;
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
              "Unsupported: LAST_DATE(date, part) is not supported by DuckDB.");
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
    Expression dateTimeExpression;
    switch (parameters.size()) {
      case 2:
        dateTimeExpression = rewriteDateLiteral(parameters.get(1), dateTimeType);

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
        break;
      case 3:
        dateTimeExpression = rewriteDateLiteral(parameters.get(1), dateTimeType);

        dateTimeExpression = new TimezoneExpression(dateTimeExpression, parameters.get(2));
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
        break;
    }
  }

  private static void rewriteStringFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 1:
        newParameters.add(
            rewriteDateLiteral(parameters.get(0), DateTimeLiteralExpression.DateTime.TIMESTAMP));
      case 2:
        TimezoneExpression timezoneExpression = new TimezoneExpression(
            rewriteDateLiteral(parameters.get(0), DateTimeLiteralExpression.DateTime.TIMESTAMPTZ),
            parameters.get(1));
        newParameters.add(timezoneExpression);
      default:
        newParameters.add(new StringValue("%c%z"));
        function.setName("StrfTime");
        function.setParameters(newParameters);

    }
  }

  private Expression rewriteDateTruncFunction(Function function, ExpressionList<?> parameters,
      DateTimeLiteralExpression.DateTime dateTimeType) {
    ExpressionList<Expression> reversedParameters = new ExpressionList<>();
    Expression dateTimeExpression;
    switch (parameters.size()) {
      case 2:
        // Date Part "ISOWEEK" exists and is not supported on DuckDB
        if (parameterWEEK(parameters, 1)) {
          reversedParameters.add(new StringValue("WEEK"));
          warning("WEEK is not distinct");
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
        dateTimeExpression = rewriteDateLiteral(parameters.get(0), dateTimeType);
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
        if (DateTimeLiteralExpression.DateTime.TIME == dateTimeType
            || DateTimeLiteralExpression.DateTime.TIMESTAMP == dateTimeType) {
          // flag the function so it would not be transpiled again
          function.setName("DATE_TRUNC$$");
          CastExpression castExpression = new CastExpression().withLeftExpression(function)
              .withType(new ColDataType(dateTimeType.name()));
          return castExpression;
        }
        break;
      case 3:
        // Date Part "ISOWEEK" exists and is not supported on DuckDB
        if (parameterWEEK(parameters, 1)) {
          reversedParameters.add(new StringValue("WEEK"));
          warning("WEEK is not distinct");
        } else if (parameters.get(1) instanceof Column && ((Column) parameters.get(1)).toString()
            .replaceAll(" ", "").equalsIgnoreCase("ISOWEEK")) {
          reversedParameters.add(new StringValue("WEEK"));
        } else {
          // translate DAY into String 'DAY'
          reversedParameters.add(!(parameters.get(1) instanceof StringValue)
              ? new StringValue(parameters.get(1).toString())
              : parameters.get(1));
        }

        // enforce casting to TimeZone
        TimezoneExpression timezoneExpression = new TimezoneExpression(
            rewriteDateLiteral(parameters.get(0), dateTimeType), parameters.get(2));
        reversedParameters.add(timezoneExpression);

        function.setName("DATE_TRUNC");
        function.setParameters(reversedParameters);

        // flag the function so it would not be transpiled again
        function.setName("DATE_TRUNC$$");
        CastExpression castExpression = new CastExpression().withLeftExpression(function)
            .withType(new ColDataType(DateTimeLiteralExpression.DateTime.TIMESTAMPTZ.name()));
        return castExpression;
    }
    return null;
  }

  private static void rewriteDateSubFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:
        newParameters
            .add(rewriteDateLiteral(parameters.get(0), (DateTimeLiteralExpression.DateTime) null));

        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          String negatedParameter =
              interval.getParameter().startsWith("-") ? interval.getParameter().substring(1)
                  : "-" + interval.getParameter();
          interval
              .setExpression(new StringValue(negatedParameter + " " + interval.getIntervalType()));
          interval.setIntervalType(null);

          newParameters.add(interval);
        } else {
          newParameters.add(
              rewriteDateLiteral(parameters.get(1), (DateTimeLiteralExpression.DateTime) null));
        }
        function.setName("DATE_ADD");
        function.setParameters(newParameters);
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
          warning("WEEK is not distinct");
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
        reversedParameters.add(rewriteDateLiteral(parameters.get(1), dateTimeType));

        // enforce DATE casting
        reversedParameters.add(rewriteDateLiteral(parameters.get(0), dateTimeType));
        function.setParameters(reversedParameters);
        function.setName("DATE_DIFF");
    }
  }

  private static void rewriteDateAddFunction(Function function, ExpressionList<?> parameters) {
    ExpressionList<Expression> newParameters = new ExpressionList<>();
    switch (parameters.size()) {
      case 2:
        newParameters
            .add(rewriteDateLiteral(parameters.get(0), (DateTimeLiteralExpression.DateTime) null));

        if (parameters.get(1) instanceof IntervalExpression) {
          IntervalExpression interval = (IntervalExpression) parameters.get(1);
          interval.setExpression(
              new StringValue(interval.getParameter() + " " + interval.getIntervalType()));
          interval.setIntervalType(null);
          newParameters.add(interval);
        } else {
          newParameters.add(
              rewriteDateLiteral(parameters.get(1), (DateTimeLiteralExpression.DateTime) null));
        }
        function.setName("DATE_ADD");
        function.setParameters(newParameters);
    }
  }

  private Expression rewriteDateFunction(Function function, ExpressionList<?> parameters) {
    CastExpression castExpression = null;
    switch (parameters.size()) {
      case 1:
        // DATE(DATETIME '2016-12-25 23:59:59') AS date_dt
        castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
            .withType(new ColDataType().withDataType("DATE"));
        break;
      case 2:
        // DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz
        // --> CAST(TIMESTAMPTZ '2016-12-25 05:30:00+07' AT TIME ZONE 'America/Los_Angeles' AS DATE)
        // AS date_tstz;
        castExpression = new CastExpression("Cast")
            .withLeftExpression(new TimezoneExpression(parameters.get(0), parameters.get(1)))
            .withType(new ColDataType().withDataType("DATE"));
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
        Function dateFunction = new Function().withName("MAKE_DATE")
            .withParameters(parameters.get(0), parameters.get(1), parameters.get(2));

        Function timeFunction = new Function().withName("MAKE_TIME")
            .withParameters(parameters.get(3), parameters.get(4), parameters.get(5));
        Addition add =
            new Addition().withLeftExpression(dateFunction).withRightExpression(timeFunction);
        castExpression = new CastExpression("Cast").withLeftExpression(add)
            .withType(new ColDataType().withDataType("DATETIME"));
        break;
      case 2:
        if (parameters.get(0) instanceof CastExpression
            && ((CastExpression) parameters.get(0)).isDate()
            && parameters.get(1) instanceof CastExpression
            && ((CastExpression) parameters.get(1)).isTime()) {
          add = new Addition().withLeftExpression(parameters.get(0))
              .withRightExpression(parameters.get(1));
          castExpression = new CastExpression("Cast").withLeftExpression(add)
              .withType(new ColDataType().withDataType("DATETIME"));
        } else if (parameters.get(0) instanceof CastExpression
            && ((CastExpression) parameters.get(0)).isTimeStamp()
            && parameters.get(1) instanceof StringValue) {
          castExpression = new CastExpression("Cast")
              .withLeftExpression(new TimezoneExpression(parameters.get(0), parameters.get(1)))
              .withType(new ColDataType().withDataType("DATETIME"));
        } else {
          // @todo: verify if this needs to be amended
          throw new RuntimeException(
              "Unsupported: DATETIME(" + parameters.get(0).getClass().getName() + ", "
                  + parameters.get(1).getClass().getName() + ") is not supported yet.");
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
        castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
            .withType(new ColDataType().withDataType("TIME"));
        break;
      case 2:
        // TIME(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz
        castExpression = new CastExpression("Cast")
            .withLeftExpression(new TimezoneExpression(parameters.get(0), parameters.get(1)))
            .withType(new ColDataType().withDataType("TIME"));
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
          return castExpression;
        case 2:
          castExpression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
              .withType(new ColDataType().withDataType(timestampType));
          TimezoneExpression timezoneExpression =
              new TimezoneExpression(castExpression, parameters.get(1));
          return timezoneExpression;
      }
    }
    return null;
  }

  public void visit(ExtractExpression extractExpression) {
    // @todo: JSQLParser Extract Expression must support `WEEK(MONDAY) .. WEEK(SUNDAY)`

    if (extractExpression.getName().equalsIgnoreCase("WEEK")) {
      warning("WEEK is not distinct");
      extractExpression.setName("WEEK");
    } else if (extractExpression.getName().equalsIgnoreCase("ISOWEEK")) {
      extractExpression.setName("WEEK");
    }

    extractExpression.setExpression( castDateTime(extractExpression.getExpression()) );

    // DuckkDB returns "Sub Minute" units for millis and micros
    if (Set.of("microseconds", "microsecond", "us", "usec", "usecs", "usecond", "useconds").contains( extractExpression.getName().toLowerCase() )) {
      BinaryExpression.modulo( extractExpression.withName("us$$"), new LongValue(1000000)).accept(this);
    } else if (Set.of("milliseconds", "millisecond", "ms", "msec", "msecs", "msecond", "mseconds").contains( extractExpression.getName().toLowerCase() )) {
      BinaryExpression.modulo( extractExpression.withName("ms$$"), new LongValue(1000)).accept(this);;
    } else if (extractExpression.getName().endsWith("$$")) {
      String name = extractExpression.getName();
      super.visit(extractExpression.withName( name.substring(0, name.length()-2) ));
    } else {
      super.visit(extractExpression);
    }
  }

  public void visit(StringValue stringValue) {
    if ("b".equalsIgnoreCase(stringValue.getPrefix())) {
      stringValue
          .setValue(JSQLExpressionTranspiler.convertByteStringToUnicode(stringValue.getValue()));

      Function encode = new Function("encode", stringValue.withPrefix(""));
      visit(encode);
      return;
    }

    if (!"r".equalsIgnoreCase(stringValue.getPrefix())) {
      // DuckDB does not use/allow "\" for escaping, so "\\" would count as 2
      stringValue.setValue(stringValue.getValue().replaceAll("\\\\\\\\", "\\\\"));
    }

    stringValue.setValue(convertUnicode(stringValue.getValue()));

    if (stringValue.getValue().equalsIgnoreCase("+inf")) {
      stringValue.setValue("+Infinity");
    } else if (stringValue.getValue().equalsIgnoreCase("-inf")) {
      stringValue.setValue("-Infinity");
    }

    super.visit(stringValue.withPrefix(null));
  }

  public void visit(HexValue hexValue) {
    super.visit(hexValue.getLongValue());
  }

  public static String convertUnicode(String input) {
    StringBuilder builder = new StringBuilder();
    int i = 0;
    while (i < input.length()) {
      char currentChar = input.charAt(i);
      if (currentChar == '\\' && i + 1 < input.length()
          && (input.charAt(i + 1) == 'u' || input.charAt(i + 1) == 'U')) {
        // Found an escaped Unicode character
        try {
          String unicodeStr = input.substring(i + 2, i + 6);
          char unicodeChar = (char) Integer.parseInt(unicodeStr, 16);
          builder.append(unicodeChar);
          i += 6;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
          // Invalid Unicode escape sequence, append as is
          builder.append(currentChar);
          i++;
        }
      } else {
        // Not an escaped Unicode character, append as is
        builder.append(currentChar);
        i++;
      }
    }
    return builder.toString();
  }

  public void visit(CastExpression castExpression) {
    // same cast
    if (castExpression.getLeftExpression() instanceof CastExpression ) {
      CastExpression leftExpression = (CastExpression) castExpression.getLeftExpression();
      if ( castExpression.isOf( leftExpression )
          || castExpression.isOf(CastExpression.DataType.TIMESTAMP,CastExpression.DataType.TIMESTAMP_WITHOUT_TIME_ZONE) && leftExpression.isOf(CastExpression.DataType.TIMESTAMP, CastExpression.DataType.TIMESTAMP_WITHOUT_TIME_ZONE)
           || castExpression.isOf(CastExpression.DataType.TIMESTAMPTZ,CastExpression.DataType.TIMESTAMP_WITH_TIME_ZONE) && leftExpression.isOf(CastExpression.DataType.TIMESTAMPTZ,CastExpression.DataType.TIMESTAMP_WITH_TIME_ZONE)
           || castExpression.isOf(CastExpression.DataType.TIME,CastExpression.DataType.TIME_WITHOUT_TIME_ZONE) && leftExpression.isOf(CastExpression.DataType.TIME, CastExpression.DataType.TIME_WITHOUT_TIME_ZONE)
      ) {

        castExpression.getLeftExpression().accept(this);
        return;
      }
    }

    if (castExpression.isOf(CastExpression.DataType.TIMESTAMP)
        && hasTimeZoneInfo(castExpression.getLeftExpression())) {
      castExpression.getColDataType().setDataType("TIMESTAMPTZ");
    }

    // call Encode when it looks like a String cast to BLOB
    if ((castExpression.keyword == null || !castExpression.keyword.endsWith("$$"))
        && castExpression.isBLOB()
        && (castExpression.getLeftExpression() instanceof StringValue
            || castExpression.getLeftExpression() instanceof Concat
            || castExpression.getLeftExpression() instanceof Function && !castExpression
                .getLeftExpression(Function.class).getName().equalsIgnoreCase("encode"))) {
      Function f = new Function("Encode$$", castExpression.getLeftExpression());
      f.accept(this);

      return;
    }

    if (castExpression.keyword != null && castExpression.keyword.endsWith("$$")) {
      castExpression.keyword =
          castExpression.keyword.substring(0, castExpression.keyword.length() - 2);
    }

    if (castExpression.isImplicitCast()) {
      this.buffer.append(rewriteType(castExpression.getColDataType()));
      this.buffer.append(" ");
      castExpression.getLeftExpression().accept(this);
    } else if (castExpression.isUseCastKeyword()) {
      this.buffer.append(castExpression.keyword).append("(");
      castExpression.getLeftExpression().accept(this);
      this.buffer.append(" AS ");
      this.buffer.append(castExpression.getColumnDefinitions().size() > 1
          ? "ROW(" + Select.getStringList(castExpression.getColumnDefinitions()) + ")"
          : rewriteType(castExpression.getColDataType()).toString());
      this.buffer.append(")");
    } else {
      castExpression.getLeftExpression().accept(this);
      this.buffer.append("::");
      this.buffer.append(rewriteType(castExpression.getColDataType()));
    }
  }

  public void visit(StructType structType) {
    if (structType.getArguments() != null && !structType.getArguments().isEmpty()) {
      buffer.append("{ ");
      int i = 0;
      for (SelectItem<?> e : structType.getArguments()) {
        if (0 < i) {
          buffer.append(",");
        }
        if (e.getAlias() != null) {
          buffer.append(e.getAlias().getName());
        } else if (structType.getParameters() != null && i < structType.getParameters().size()) {
          buffer.append(structType.getParameters().get(i).getKey());
        }

        buffer.append(":");
        buffer.append(e.getExpression());

        i++;
      }
      buffer.append(" }");
    }

    if (structType.getParameters() != null && !structType.getParameters().isEmpty()) {
      buffer.append("::STRUCT( ");
      int i = 0;
      for (Map.Entry<String, ColDataType> e : structType.getParameters()) {
        if (0 < i++) {
          buffer.append(",");
        }
        buffer.append(e.getKey()).append(" ");
        buffer.append(e.getValue());
      }
      buffer.append(")");
    }
  }


  // @todo: complete the data type mapping
  // implement an Enum on Big Query allowed data types
  public final static ColDataType rewriteType(ColDataType colDataType) {
    if (CastExpression.isOf(colDataType, CastExpression.DataType.BYTES,
        CastExpression.DataType.VARBYTE)) {
      colDataType.setDataType("BLOB");
    } else if (colDataType.getDataType().equalsIgnoreCase("FLOAT64")) {
      colDataType.setDataType("FLOAT");
    }
    return colDataType;
  }

  public final void warning(String s) {
    buffer.append("/* Approximation: ").append(s).append(" */ ");
  }

  public static String convertByteStringToUnicode(String byteString) {
    StringBuilder unicodeBuilder = new StringBuilder();
    for (int i = 0; i < byteString.length(); i++) {
      if (byteString.charAt(i) == '\\' && i + 1 < byteString.length()) {
        if (byteString.charAt(i + 1) == 'x' && i + 3 < byteString.length()) {
          // Extract and convert the hexadecimal escape sequence
          String hex = byteString.substring(i + 2, i + 4);
          char unicodeChar = (char) Integer.parseInt(hex, 16);
          unicodeBuilder.append(unicodeChar);
          i += 3; // Move the index to skip the escape sequence
        } else {
          // Append the character following the backslash as-is
          unicodeBuilder.append(byteString.charAt(i + 1));
          i++; // Move the index to skip the character
        }
      } else {
        // Append normal characters as-is
        unicodeBuilder.append(byteString.charAt(i));
      }
    }

    return unicodeBuilder.toString();
  }

  private static String formatDate(Date date, String pattern, String tzID) {
    SimpleDateFormat f = new SimpleDateFormat(pattern);
    f.setTimeZone(TimeZone.getTimeZone(tzID));
    return f.format(date);
  }

  public static Expression castDateTime(String expression) {
    return castDateTime(new StringValue(expression));
  }

  public static Expression castDateTime(Expression expression) {
    if (expression instanceof StringValue) {
      return castDateTime((StringValue) expression);
    } else if (expression instanceof CastExpression) {
      return castDateTime((CastExpression) expression);
    } else if (expression instanceof DateTimeLiteralExpression) {
      return castDateTime((DateTimeLiteralExpression) expression);
    } else {
      return expression;
    }
  }

  // Unfortunately "yyyyDDD" will collide with "yyyyMMdd"
  // because number of the Digits is not enforced when parsing
  // Also, General Time Zones like `Asia/Bangkok` are not accepted when parsing
  private final static String[] dateFormats =
          {"", "yyyy-MM-dd", "yyyyMMdd", "YYYY-'W'ww-u", "YYYY'W'wwu", "yyyy-DDD", "yyyyDDD"};
  private final static  String[] timeFormats = {"HH:mm:ss.SSS", "HHmmss.SSS", "HH:mm:ss", "HHmmss"};
  private final static  String[] timeSeparators = {"", "'T'", " "};
  private final static String[] zones = {"z", "zz", "zzzz", "Z", "X", "XXX"};

  @SuppressWarnings({"PMD.EmptyCatchBlock"})
  public static Expression castDateTime(DateTimeLiteralExpression expression) {
    SimpleDateFormat f = new SimpleDateFormat();
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    f.setLenient(false);

    String s = expression.getValue();
    Date date = null;

    // test for Timestamp with time zone
    for (String df : dateFormats) {
      for (String tf : timeFormats) {
        for (String separator : timeSeparators) {

          for (String z : zones) {
            f.applyPattern(df + separator + tf + z);
            try {
              date = f.parse(s);
              return df.isEmpty()
                     ? expression.withValue( formatDate(date, "HH:mm:ss.SSS" + "Z", "UTC"))
                     : expression.withValue( formatDate(date, "yyyy-MM-dd'T'" + "HH:mm:ss.SSS" + "Z", "UTC"));
            } catch (Exception ignore) {
              // nothing to do here
            }
          }

          f.applyPattern(df + separator + tf);
          try {
            date = f.parse(s);
            return df.isEmpty()
                   ? expression.withValue(formatDate(date, "HH:mm:ss.SSS", "UTC"))
                   : expression.withValue(formatDate(date, "yyyy-MM-dd'T'" + "HH:mm:ss.SSS", "UTC"));
          } catch (Exception ignore) {
            // nothing to do here
          }

        }
      }

      if (!df.isEmpty()) {
        f.applyPattern(df);
        try {
          date = f.parse(s);
          return expression.withValue(formatDate(date, "yyyy-MM-dd", "UTC"));
        } catch (Exception ignore) {
          // nothing to do here
        }
      }
    }
    return expression;
  }

  @SuppressWarnings({"PMD.EmptyCatchBlock"})
  public static Expression castDateTime(CastExpression expression) {
    if (! (expression.isDate() || expression.isTime() || expression.isTimeStamp())) {
      return expression;
    }

    if (! (expression.getLeftExpression() instanceof StringValue)) {
      return expression;
    }

    SimpleDateFormat f = new SimpleDateFormat();
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    f.setLenient(false);

    Expression expression1 = castDateTime( (StringValue) expression.getLeftExpression() );
    if (expression1 instanceof CastExpression) {
      CastExpression autoCast = (CastExpression) expression1;
      if (autoCast.isOf(expression)) {
        return expression.withLeftExpression( autoCast.getLeftExpression() );
      } else if (autoCast.isOf(CastExpression.DataType.TIME_WITH_TIME_ZONE )
                               && expression.isOf(CastExpression.DataType.TIME, CastExpression.DataType.TIME_WITHOUT_TIME_ZONE)) {
        return autoCast;
      } else if (autoCast.isOf(CastExpression.DataType.TIMESTAMP_WITH_TIME_ZONE )
                 && expression.isOf(CastExpression.DataType.TIMESTAMP, CastExpression.DataType.TIMESTAMP_WITHOUT_TIME_ZONE, CastExpression.DataType.TIMESTAMPTZ)) {
        return autoCast;
      } else if (autoCast.isOf(CastExpression.DataType.TIME_WITHOUT_TIME_ZONE )
                 && expression.isOf(CastExpression.DataType.TIME, CastExpression.DataType.TIME_WITHOUT_TIME_ZONE)) {
        return expression.withLeftExpression( autoCast.getLeftExpression() );
      } else if (autoCast.isOf(CastExpression.DataType.TIMESTAMP_WITHOUT_TIME_ZONE )
                 && expression.isOf(CastExpression.DataType.TIMESTAMP, CastExpression.DataType.TIMESTAMP_WITHOUT_TIME_ZONE)) {
        return expression.withLeftExpression( autoCast.getLeftExpression() );
      } else {
        return expression.setImplicitCast(false).withLeftExpression(autoCast);
      }
    }

    return expression;
  }

  @SuppressWarnings({"PMD.EmptyCatchBlock"})
  public static Expression castDateTime(StringValue expression) {
    SimpleDateFormat f = new SimpleDateFormat();
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    f.setLenient(false);

    String s = ((StringValue) expression).getValue();
    Date date = null;

    // test for Timestamp with time zone
    for (String df : dateFormats) {
      for (String tf : timeFormats) {
        for (String separator : timeSeparators) {

          for (String z : zones) {
            f.applyPattern(df + separator + tf + z);
            try {
              date = f.parse(s);
              return df.isEmpty()
                  ? new CastExpression("TIME WITH TIME ZONE",
                      formatDate(date, "HH:mm:ss.SSS" + "Z", "UTC"))
                  : new CastExpression("TIMESTAMP WITH TIME ZONE",
                      formatDate(date, "yyyy-MM-dd'T'" + "HH:mm:ss.SSS" + "Z", "UTC"));
            } catch (Exception ignore) {
              // nothing to do here
            }
          }

          f.applyPattern(df + separator + tf);
          try {
            date = f.parse(s);
            return df.isEmpty()
                ? new CastExpression("TIME WITHOUT TIME ZONE",
                    formatDate(date, "HH:mm:ss.SSS", "UTC"))
                : new CastExpression("TIMESTAMP WITHOUT TIME ZONE",
                    formatDate(date, "yyyy-MM-dd'T'" + "HH:mm:ss.SSS", "UTC"));
          } catch (Exception ignore) {
            // nothing to do here
          }

        }
      }

      if (!df.isEmpty()) {
        f.applyPattern(df);
        try {
          date = f.parse(s);
          return new CastExpression("DATE", formatDate(date, "yyyy-MM-dd", "UTC"));
        } catch (Exception ignore) {
          // nothing to do here
        }
      }
    }
    return expression;
  }

  public static Expression castInterval(String expression) {
    return castInterval(new StringValue(expression));
  }

  public static Expression castInterval(Expression expression) {
    if (expression instanceof StringValue) {
      return castInterval((StringValue) expression);
    } else if (expression instanceof CastExpression) {
      return castInterval((CastExpression) expression);
    } else if (expression instanceof IntervalExpression) {
      return castInterval((IntervalExpression) expression);
    } else {
      return expression;
    }
  }

  public static Expression castInterval(StringValue expression) {
    return new CastExpression("INTERVAL", expression.getValue());
  }

  public static Expression castInterval(CastExpression expression) {
    return expression;
  }

  public static Expression castInterval(IntervalExpression expression) {
    return expression;
  }

}
