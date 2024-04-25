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
package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.JSQLExpressionTranspiler;
import ai.starlake.transpiler.JSQLTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class SnowflakeExpressionTranspiler extends JSQLExpressionTranspiler {
  public SnowflakeExpressionTranspiler(JSQLTranspiler transpiler, StringBuilder buffer) {
    super(transpiler, buffer);
  }

  enum DatePart {
    year, y, yy, yyy, yyyy, yr, years, yrs, month, mm, mon, mons, months, day, d, dd, days, dayofmonth, dayofweek, weekday, dow, dw, dayofweekiso, weekday_iso, dow_iso, dw_iso, dayofyear, yearday, doy, dy, week, w, wk, weekofyear, woy, wy, weekiso, week_iso, weekofyeariso, weekofyear_iso, quarter, q, qtr, qtrs, quarters, yearofweek, yearofweekiso
  }

  enum TimePart {
    hour, h, hh, hr, hours, hrs, minute, m, mi, min, minutes, mins, second, s, sec, seconds, secs, millisecond, ms, msec, milliseconds, microsecond, us, usec, microseconds, nanosecond, ns, nsec, nanosec, nsecond, nanoseconds, nanosecs, nseconds, epoch_second, epoch, epoch_seconds, epoch_millisecond, epoch_milliseconds, epoch_microsecond, epoch_microseconds, epoch_nanosecond, epoch_nanoseconds, timezone_hour, tzh, timezone_minute, tzm
  }

  enum TranspiledFunction {
    // @FORMATTER:OFF
    DATE_FROM_PARTS, DATEFROMPARTS, TIME_FROM_PARTS, TIMEFROMPARTS, TIMESTAMP_FROM_PARTS, TIMESTAMPFROMPARTS, TIMESTAMP_TZ_FROM_PARTS, TIMESTAMPTZFROMPARTS, TIMESTAMP_LTZ_FROM_PARTS, TIMESTAMPLTZFROMPARTS, TIMESTAMP_NTZ_FROM_PARTS, TIMESTAMPNTZFROMPARTS, DATE_PART, DAYNAME, LAST_DAY, MONTHNAME, ADD_MONTHS, DATEADD, TIMEADD, TIMESTAMPADD, DATEDIFF, TIMEDIFF, TIMESTAMPDIFF, TIME_SLICE, TRUNC, DATE, TIME, TO_TIMESTAMP_LTZ, TO_TIMESTAMP_NTZ, TO_TIMESTAMP_TZ, CONVERT_TIMEZONE, TO_DATE, TO_TIME, TO_TIMESTAMP

    , REGEXP_COUNT, REGEXP_EXTRACT_ALL, REGEXP_SUBSTR_ALL, REGEXP_INSTR, REGEXP_SUBSTR, REGEXP_LIKE, REGEXP_REPLACE;
    // @FORMATTER:ON


    @SuppressWarnings({"PMD.EmptyCatchBlock"})
    public static TranspiledFunction from(String name) {
      TranspiledFunction function = null;
      try {
        function = Enum.valueOf(TranspiledFunction.class, name.replaceAll(" ", "_").toUpperCase());
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
    CRC32, DIFFERENCE, INITCAP, SOUNDEX, STRTOL, NEXT_DAY

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

  public Expression toDateTimePart(Expression expression) {
    return toDateTimePart(expression, JSQLTranspiler.Dialect.SNOWFLAKE);
  }

  public Expression castInterval(Expression e1, Expression e2) {
    return castInterval(e1, e2, JSQLTranspiler.Dialect.SNOWFLAKE);
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public void visit(Function function) {
    String functionName = function.getName();
    boolean hasParameters = hasParameters(function);

    if (UnsupportedFunction.from(function) != null) {
      throw new RuntimeException(
          "Unsupported: " + functionName + " is not supported by DuckDB (yet).");
    } else if (functionName.endsWith("$$")) {
      // work around for transpiling already transpiled functions twice
      // @todo: figure out a better way to achieve that

      // careful: we must not strip the $$ PREFIX here since SUPER will call
      // JSQLExpressionTranspiler
      // function.setName(functionName.substring(0, functionName.length() - 2));
      super.visit(function);
      return;
    }

    if (function.getMultipartName().size() > 1
        && function.getMultipartName().get(0).equalsIgnoreCase("SAFE")) {
      warning("SAFE prefix is not supported.");
      function.getMultipartName().remove(0);
    }

    Expression rewrittenExpression = null;
    ExpressionList<? extends Expression> parameters = function.getParameters();
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case DATE_FROM_PARTS:
        case DATEFROMPARTS:
          warning("Negative arguments not supported.");
          function.setName("MAKE_DATE");
          break;
        case TIME_FROM_PARTS:
        case TIMEFROMPARTS:
          if (hasParameters) {
            switch (parameters.size()) {
              case 4:
                // select make_time(12, 34, (56 || '.' || 987654321)::DOUBLE ) AS time;
                CastExpression castExpression = new CastExpression(
                    new ParenthesedExpressionList<>(BinaryExpression.concat(parameters.get(2),
                        new StringValue("."), parameters.get(3))),
                    "DOUBLE");
                function.setParameters(parameters.get(0), parameters.get(1), castExpression);
              case 3:
                function.setName("MAKE_TIME");
                break;
            }
          }
          break;
        case TIMESTAMP_TZ_FROM_PARTS:
        case TIMESTAMPTZFROMPARTS:

        case TIMESTAMP_FROM_PARTS:
        case TIMESTAMPFROMPARTS:
        case TIMESTAMP_LTZ_FROM_PARTS:
        case TIMESTAMPLTZFROMPARTS:
        case TIMESTAMP_NTZ_FROM_PARTS:
        case TIMESTAMPNTZFROMPARTS:
          if (hasParameters) {
            switch (parameters.size()) {
              // TIMESTAMP_FROM_PARTS( <date_expr>, <time_expr> )
              case 2:
                rewrittenExpression = new CastExpression(
                    new ParenthesedExpressionList<>(BinaryExpression
                        .add(castDateTime(parameters.get(0)), castDateTime(parameters.get(1)))),
                    "TIMESTAMP");
                break;

              // TIMESTAMP_FROM_PARTS( <year>, <month>, <day>, <hour>, <minute>, <second> [,
              // <nanosecond> ] [, <time_zone> ] )
              // make_timestamp(bigint, bigint, bigint, bigint, bigint, double)
              case 8:
                rewrittenExpression = new TimezoneExpression(function, parameters.get(7));
              case 7:
                CastExpression castExpression = new CastExpression(
                    new ParenthesedExpressionList<>(BinaryExpression.concat(parameters.get(5),
                        new StringValue("."), parameters.get(6))),
                    "DOUBLE");
                function.setParameters(parameters.get(0), parameters.get(1), parameters.get(2),
                    parameters.get(3), parameters.get(4), castExpression);

              case 6:
                function.setName("Make_Timestamp");
                break;
            }
          }
          break;
        case DAYNAME:
          // dayname return only the 3 char abbreviation
          if (hasParameters && parameters.size() == 1) {
            function.setName("strftime");
            function.setParameters(parameters.get(0), new StringValue("%a"));
          }
          break;
        case MONTHNAME:
          if (hasParameters && parameters.size() == 1) {
            function.setName("strftime");
            function.setParameters(parameters.get(0), new StringValue("%b"));
          }
          break;
        case DATE:
        case TO_DATE:
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression = new CastExpression(parameters.get(0), "DATE");
                break;
            }
          }
          break;
        case TIME:
        case TO_TIME:
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression = new CastExpression(parameters.get(0), "TIME");
                break;
            }
          }
          break;
        case TO_TIMESTAMP:
        case TO_TIMESTAMP_NTZ:
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression = new CastExpression(parameters.get(0), "TIMESTAMP");
                break;
            }
          }
          break;
        case TO_TIMESTAMP_LTZ:
        case TO_TIMESTAMP_TZ:
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression =
                    new CastExpression(parameters.get(0), "TIMESTAMP WITH TIME ZONE");
                break;
            }
          }
          break;
        case DATE_PART:
          if (hasParameters) {
            switch (parameters.size()) {
              case 2:
                function.setParameters(toDateTimePart(parameters.get(0)), parameters.get(1));
                break;
            }
          }
          break;
        case LAST_DAY:
          if (hasParameters) {
            switch (parameters.size()) {
              case 2:
                throw new RuntimeException("LAST_DATE with DatePart is not supported.");
            }
          }
          break;
        case ADD_MONTHS:
          // date_add(TIMESTAMP '2008-01-01 05:07:30', (1 || ' MONTH')::INTERVAL )
          warning("Different Ultimo handling");
          function.setName("date_add");
          function.setParameters(
              rewriteDateLiteral(parameters.get(0), DateTimeLiteralExpression.DateTime.TIMESTAMP),
              new CastExpression(
                  new ParenthesedExpressionList<>(
                      BinaryExpression.concat(parameters.get(1), new StringValue(" MONTH"))),
                  "INTERVAL"));
          break;
        case TIMEADD:
        case TIMESTAMPADD:
          function.setName("DateAdd");
        case DATEADD:
          // DATEADD(year, 2, TO_DATE('2013-05-08')
          if (hasParameters && parameters.size() == 3) {
            function.setName("date_add");
            function.setParameters(
                rewriteDateLiteral(parameters.get(2), DateTimeLiteralExpression.DateTime.TIMESTAMP),
                new CastExpression(new ParenthesedExpressionList<>(
                    BinaryExpression.concat(parameters.get(1), toDateTimePart(parameters.get(0)))),
                    "INTERVAL"));
          }
          break;
        case TIMEDIFF:
        case TIMESTAMPDIFF:
          function.setName("DateDiff");
        case DATEDIFF:
          if (hasParameters && parameters.size() == 3) {
            function.setParameters(toDateTimePart(parameters.get(0)),
                castDateTime(parameters.get(1)), castDateTime(parameters.get(2)));
          }
          break;
        case TIME_SLICE:
          if (hasParameters) {
            switch (parameters.size()) {
              // TIME_SLICE(billing_date, 2, 'WEEK', 'START')
              // time_bucket(INTERVAL '2 WEEK', billing_date) + INTERVAL '2 WEEK'
              case 4:
                if (parameters.get(3) instanceof StringValue
                    && ((StringValue) parameters.get(3)).getValue().equalsIgnoreCase("END")) {
                  rewrittenExpression = BinaryExpression.add(function,
                      castInterval(parameters.get(1), parameters.get(2)));
                }
              case 3:
                function.setName("Time_Bucket");
                function.setParameters(castInterval(parameters.get(1), parameters.get(2)),
                    castDateTime(parameters.get(0)));
            }
          }
          break;
        case TRUNC:
          if (hasParameters && parameters.size() == 2) {
            function.setName("Date_Trunc$$");
            function.setParameters(toDateTimePart(parameters.get(1)),
                castDateTime(parameters.get(0)));
          }
          break;
        case CONVERT_TIMEZONE:
          if (hasParameters) {
            switch (parameters.size()) {
              case 2:
                rewrittenExpression =
                    new TimezoneExpression(castDateTime(parameters.get(1)), parameters.get(0));
                break;
              case 3:
                rewrittenExpression = new TimezoneExpression(
                    new TimezoneExpression(castDateTime(parameters.get(2)), parameters.get(1)),
                    parameters.get(0));
                break;
            }
          }
          break;
        case REGEXP_COUNT:
          function.setName("Length$$");
          function.setParameters(
              new Function("regexp_split_to_array", parameters.get(0), parameters.get(1)));
          rewrittenExpression =
              new Subtraction().withLeftExpression(function).withRightExpression(new LongValue(1));
          break;
        case REGEXP_SUBSTR_ALL:
          function.setName("Regexp_Extract_All");
        case REGEXP_EXTRACT_ALL:
          // REGEXP_SUBSTR_ALL( <subject> , <pattern> [ , <position> [ , <occurrence> [ ,
          // <regex_parameters> [ , <group_num> ] ] ] ] )
          // regexp_extract_all(string, regex[, group = 0])
          if (hasParameters) {
            switch (parameters.size()) {
              case 6:
              case 5:
                parameters.remove(4);
              case 4:
                parameters.remove(3);
              case 3:
                warning("unsupported parameters");
                parameters.remove(2);
            }
          }
          break;
        case REGEXP_INSTR:
          // case when len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$'))>1 then
          // len(REGEXP_SPLIT_TO_ARRAY(venuename,'[cC]ent(er|re)$')[1])+1 else 0 end
          if (parameters != null) {
            while (parameters.size() > 2) {
              parameters.remove(parameters.size() - 1);
            }

            Expression whenExpr = new GreaterThan(
                new Function("Length$$",
                    new Function("REGEXP_SPLIT_TO_ARRAY", parameters.get(0), parameters.get(1))),
                new LongValue(1));
            Expression thenExpr = BinaryExpression.add(new Function("Length$$",
                new ArrayExpression(
                    new Function("REGEXP_SPLIT_TO_ARRAY", parameters.get(0), parameters.get(1)),
                    new LongValue(1), null, null)),
                new LongValue(1));

            rewrittenExpression =
                new CaseExpression(new LongValue(0), new WhenClause(whenExpr, thenExpr));
          }
        case REGEXP_SUBSTR:
          // REGEXP_SUBSTR( source_string, pattern [, position [, occurrence [, parameters ] ] ] )
          // REGEXP_SUBSTR skips the first occurrence -1 matches. The default is 1.
          if (parameters != null) {
            function.setName("Regexp_Extract$$");
            switch (parameters.size()) {
              case 2:
                function.setParameters(parameters.get(0), parameters.get(1), new LongValue(0));
                break;
              case 3:
                warning("Position Parameter unsupported");
                parameters.remove(2);

                function.setParameters(parameters.get(0), parameters.get(1), new LongValue(0));
                break;

              case 4:
                warning("Position Parameter unsupported");

                if (parameters.get(3) instanceof LongValue) {
                  LongValue longValue = (LongValue) parameters.get(3);
                  longValue.setValue(longValue.getValue() - 1);
                  function.setParameters(parameters.get(0), parameters.get(1), longValue);
                } else {
                  function.setParameters(parameters.get(0), parameters.get(1),
                      BinaryExpression.subtract(parameters.get(3), new LongValue(1)));
                }
                break;
              case 5:
                warning("Position Parameter unsupported");

                if (parameters.get(4).toString().contains("p")) {
                  warning("PCRE unsupported");
                }
                if (parameters.get(4).toString().contains("e")) {
                  warning("Sub-Expression");
                }

                if (parameters.get(3) instanceof LongValue) {
                  LongValue longValue = (LongValue) parameters.get(3);
                  longValue.setValue(longValue.getValue() - 1);
                  function.setParameters(parameters.get(0), parameters.get(1), longValue,
                      parameters.get(4));
                } else {
                  function.setParameters(parameters.get(0), parameters.get(1),
                      BinaryExpression.subtract(parameters.get(3), new LongValue(1)),
                      parameters.get(4));
                }

                break;
            }
          }
          break;
        case REGEXP_LIKE:
          function.setName("Regexp_Matches");
          break;
        case REGEXP_REPLACE:
          // REGEXP_REPLACE( <subject> , <pattern> [ , <replacement> , <position> , <occurrence> ,
          // <parameters> ] )
          if (hasParameters) {
            switch (parameters.size()) {
              case 6:
              case 5:
                parameters.remove(4);
              case 4:
                warning("unsupported parameters");
                parameters.remove(3);
              case 3:
                // add the "global" flag
                if (parameters.size() == 3) {
                  function.setParameters(parameters.get(0), parameters.get(1), parameters.get(2),
                      new StringValue("g"));
                } else {
                  function.setParameters(parameters.get(0), parameters.get(1), parameters.get(2),
                      BinaryExpression.concat(parameters.get(3), new StringValue("g")));
                }
                break;

              case 2:
                function.setParameters(parameters.get(0), parameters.get(1), new StringValue(""),
                    new StringValue("g"));
                break;
            }
          }
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

    if (function.getNullHandling() != null && function.isIgnoreNullsOutside()) {
      function.setIgnoreNullsOutside(false);
    }

    Expression rewrittenExpression = null;
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (rewrittenExpression == null) {
      super.visit(function);
    } else {
      rewrittenExpression.accept(this);
    }
  }

  public void visit(Column column) {
    if (column.getColumnName().equalsIgnoreCase("SYSDATE")) {
      column.setColumnName("CURRENT_DATE");
    }
    super.visit(column);
  }

  public ColDataType rewriteType(ColDataType colDataType) {
    if (colDataType.getDataType().equalsIgnoreCase("FLOAT")) {
      colDataType.setDataType("FLOAT8");
    } else if (colDataType.getDataType().equalsIgnoreCase("DEC")) {
      colDataType.setDataType("DECIMAL");
    } else if (colDataType.getDataType().equalsIgnoreCase("TIMESTAMP_NTZ")) {
      colDataType.setDataType("TIMESTAMP");
    } else if (colDataType.getDataType().equalsIgnoreCase("TIMESTAMP_LTZ")) {
      colDataType.setDataType("TIMESTAMPTZ");
    } else if (colDataType.getDataType().equalsIgnoreCase("TIMESTAMP_TZ")) {
      colDataType.setDataType("TIMESTAMPTZ");
    } else if (colDataType.getDataType().equalsIgnoreCase("NUMBER")) {
      colDataType.setDataType("NUMERIC");
    }
    return super.rewriteType(colDataType);
  }
}
