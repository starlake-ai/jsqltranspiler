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
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;

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
    DATE_FROM_PARTS, DATEFROMPARTS, TIME_FROM_PARTS, TIMEFROMPARTS, TIMESTAMP_FROM_PARTS, TIMESTAMPFROMPARTS, TIMESTAMP_TZ_FROM_PARTS, TIMESTAMPTZFROMPARTS, TIMESTAMP_LTZ_FROM_PARTS, TIMESTAMPLTZFROMPARTS, TIMESTAMP_NTZ_FROM_PARTS, TIMESTAMPNTZFROMPARTS, DATE_PART, DAYNAME, LAST_DAY, MONTHNAME, ADD_MONTHS, DATEADD, DATEDIFF

    , TO_DATE, TO_TIME, TO_TIMESTAMP;
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
                CastExpression castExpression = new CastExpression(new Parenthesis(BinaryExpression
                    .concat(parameters.get(2), new StringValue("."), parameters.get(3))), "DOUBLE");
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
                rewrittenExpression = new CastExpression(new Parenthesis(BinaryExpression
                    .add(castDateTime(parameters.get(0)), castDateTime(parameters.get(1)))),
                    "TIMESTAMP");
                break;

              // TIMESTAMP_FROM_PARTS( <year>, <month>, <day>, <hour>, <minute>, <second> [,
              // <nanosecond> ] [, <time_zone> ] )
              // make_timestamp(bigint, bigint, bigint, bigint, bigint, double)
              case 8:
                rewrittenExpression = new TimezoneExpression(function, parameters.get(7));
              case 7:
                CastExpression castExpression = new CastExpression(new Parenthesis(BinaryExpression
                    .concat(parameters.get(5), new StringValue("."), parameters.get(6))), "DOUBLE");
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
        case TO_DATE:
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression = new CastExpression(parameters.get(0), "DATE");
                break;
            }
          }
          break;
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
          if (hasParameters) {
            switch (parameters.size()) {
              case 1:
                rewrittenExpression = new CastExpression(parameters.get(0),
                    hasTimeZoneInfo(parameters.get(0)) ? "TIMESTAMP WITH TIME ZONE" : "TIMESTAMP");
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
                  new Parenthesis(
                      BinaryExpression.concat(parameters.get(1), new StringValue(" MONTH"))),
                  "INTERVAL"));
          break;
        case DATEADD:
          // DATEADD(year, 2, TO_DATE('2013-05-08')
          if (hasParameters && parameters.size() == 3) {
            function.setName("date_add");
            function.setParameters(
                rewriteDateLiteral(parameters.get(2), DateTimeLiteralExpression.DateTime.TIMESTAMP),
                new CastExpression(new Parenthesis(
                    BinaryExpression.concat(parameters.get(1), toDateTimePart(parameters.get(0)))),
                    "INTERVAL"));
          }
          break;
        case DATEDIFF:
          if (hasParameters && parameters.size() == 3) {
            function.setParameters(toDateTimePart(parameters.get(0)),
                castDateTime(parameters.get(1)), castDateTime(parameters.get(2)));
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
    if (f != null) {
      switch (f) {
      }
    }
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
    }
    return super.rewriteType(colDataType);
  }
}
