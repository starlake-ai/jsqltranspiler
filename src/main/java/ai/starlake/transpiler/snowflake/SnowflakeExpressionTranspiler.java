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
package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.util.ArrayList;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class SnowflakeExpressionTranspiler extends RedshiftExpressionTranspiler {

  public SnowflakeExpressionTranspiler(SelectDeParser deParser, StringBuilder buffer) {
    super(deParser, buffer);
  }

  enum DatePart {
    //@formatter:off
    year, y, yy, yyy, yyyy, yr, years, yrs, month, mm, mon, mons, months, day, d, dd, days, dayofmonth, dayofweek
    , weekday, dow, dw, dayofweekiso, weekday_iso, dow_iso, dw_iso, dayofyear, yearday, doy, dy, week, w, wk, weekofyear
    , woy, wy, weekiso, week_iso, weekofyeariso, weekofyear_iso, quarter, q, qtr, qtrs, quarters, yearofweek, yearofweekiso
    //@formatter:on
  }

  enum TimePart {
    //@formatter:off
    hour, h, hh, hr, hours, hrs, minute, m, mi, min, minutes, mins, second, s, sec, seconds, secs, millisecond, ms
    , msec, milliseconds, microsecond, us, usec, microseconds, nanosecond, ns, nsec, nanosec, nsecond, nanoseconds
    , nanosecs, nseconds, epoch_second, epoch, epoch_seconds, epoch_millisecond, epoch_milliseconds, epoch_microsecond
    , epoch_microseconds, epoch_nanosecond, epoch_nanoseconds, timezone_hour, tzh, timezone_minute, tzm
    //@formatter:on
  }

  enum TranspiledFunction {
    //@formatter:off
    DATE_FROM_PARTS, DATEFROMPARTS, TIME_FROM_PARTS, TIMEFROMPARTS, TIMESTAMP_FROM_PARTS, TIMESTAMPFROMPARTS
    , TIMESTAMP_TZ_FROM_PARTS, TIMESTAMPTZFROMPARTS, TIMESTAMP_LTZ_FROM_PARTS, TIMESTAMPLTZFROMPARTS
    , TIMESTAMP_NTZ_FROM_PARTS, TIMESTAMPNTZFROMPARTS, DATE_PART, DAYNAME, LAST_DAY, MONTHNAME, ADD_MONTHS, DATEADD
    , TIMEADD, TIMESTAMPADD, DATEDIFF, TIMEDIFF, TIMESTAMPDIFF, TIME_SLICE, TRUNC, DATE, TIME, TO_TIMESTAMP_LTZ
    , TO_TIMESTAMP_NTZ, TO_TIMESTAMP_TZ, CONVERT_TIMEZONE, TO_DATE, TO_TIME, TO_TIMESTAMP
    , REGEXP_COUNT, REGEXP_EXTRACT_ALL, REGEXP_SUBSTR_ALL, REGEXP_INSTR, REGEXP_SUBSTR, REGEXP_LIKE, REGEXP_REPLACE
    , BIT_LENGTH, OCTET_LENGTH, CHAR, INSERT, RTRIMMED_LENGTH, SPACE, SPLIT_TO_TABLE, STRTOK_SPLIT_TO_TABLE, STRTOK
    , STRTOK_TO_ARRAY, UUID_STRING, CHARINDEX, POSITION, EDITDISTANCE, ENDSWITH, STARTSWITH, JAROWINKLER_SIMILARITY
    , ARRAYAGG, ARRAY_CONSTRUCT, ARRAY_COMPACT, ARRAY_CONSTRUCT_COMPACT, ARRAY_CONTAINS, ARRAY_EXCEPT
    , ARRAY_FLATTEN, ARRAY_GENERATE_RANGE, ARRAY_INSERT, ARRAY_INTERSECTION, ARRAY_MAX, ARRAY_MIN, ARRAY_POSITION
    , ARRAY_PREPEND, ARRAY_REMOVE, ARRAY_REMOVE_AT, ARRAY_SIZE, ARRAY_SLICE, ARRAY_SORT, ARRAY_TO_STRING, ARRAYS_OVERLAP
    , VARIANCE_POP, VARIANCE_SAMP, BITAND_AGG, BITOR_AGG, BITXOR_AGG, BOOLAND_AGG, BOOLOR_AGG, BOOLXOR_AGG, SKEW
    , GROUPING_ID, TO_VARCHAR, TO_BINARY, TRY_TO_BINARY, TO_DECIMAL, TO_NUMBER, TO_NUMERIC, TRY_TO_DECIMAL
    , TRY_TO_NUMBER, TRY_TO_NUMERIC, TO_DOUBLE, TRY_TO_DOUBLE, TO_BOOLEAN, TRY_TO_BOOLEAN, TRY_TO_DATE, TRY_TO_TIME
    , TRY_TO_TIMESTAMP, TRY_TO_TIMESTAMP_LTZ, TRY_TO_TIMESTAMP_NTZ, TRY_TO_TIMESTAMP_TZ
    , RANDOM, DIV0, DIV0NULL, ROUND, SQUARE
    , CHECK_JSON, TRY_PARSE_JSON, GET_PATH, OBJECT_KEYS, STRIP_NULL_VALUE
    , ST_GEOMFROMWKB , ST_GEOMETRYFROMEWKB , ST_GEOMFROMEWKB, ST_GEOMETRYFROMWKB, ST_GEOMFROMWKT , ST_GEOMETRYFROMEWKT
    , ST_GEOMFROMEWKT , ST_GEOMETRYFROMTEXT , ST_GEOMFROMTEXT, ST_GEOMETRYFROMWKT, TO_GEOMETRY, TRY_TO_GEOMETRY
    ;
    //@formatter:on


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
    CRC32, DIFFERENCE, INITCAP, SOUNDEX, STRTOL, NEXT_DAY, PARSE_IP, PARSE_URL, SOUNDEX_P123, ARRAY_UNION_AGG, ARRAY_UNIQUE_AGG, BITMAP_BIT_POSITION, BITMAP_BUCKET_NUMBER, BITMAP_COUNT, BITMAP_CONSTRUCT_AGG, BITMAP_OR_AGG, BOOLXOR_AGG, HASH_AGG, OBJECT_AGG

    , REGR_AVGX, REGR_AVGY, REGR_COUNT, REGR_INTERCEPT, REGR_R2, REGR_SLOPE, REGR_SXX, REGR_SXY, REGR_SYY

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

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public <S> StringBuilder visit(Function function, S params) {
    String functionName = function.getName().toUpperCase();
    boolean hasParameters = hasParameters(function);
    int paramCount = hasParameters ? function.getParameters().size() : 0;

    if (UnsupportedFunction.from(function) != null) {
      throw new RuntimeException(
          "Unsupported: " + functionName + " is not supported by DuckDB (yet).");
    } else if (functionName.endsWith("$$")) {
      // work around for transpiling already transpiled functions twice
      // @todo: figure out a better way to achieve that

      // careful: we must not strip the $$ PREFIX here since SUPER will call
      // JSQLExpressionTranspiler
      // function.setName(functionName.substring(0, functionName.length() - 2));
      super.visit(function, params);
      return null;
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
        case TRY_TO_DATE:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(
                functionName.startsWith("TRY") ? "Try_Cast" : null, parameters.get(0), "DATE");
          }
          break;
        case TIME:
        case TO_TIME:
        case TRY_TO_TIME:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(
                functionName.startsWith("TRY") ? "Try_Cast" : null, parameters.get(0), "TIME");
          }
          break;
        case TO_TIMESTAMP:
        case TO_TIMESTAMP_NTZ:
        case TRY_TO_TIMESTAMP:
        case TRY_TO_TIMESTAMP_NTZ:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(
                functionName.startsWith("TRY") ? "Try_Cast" : null, parameters.get(0), "TIMESTAMP");
          }
          break;
        case TO_TIMESTAMP_LTZ:
        case TO_TIMESTAMP_TZ:
        case TRY_TO_TIMESTAMP_LTZ:
        case TRY_TO_TIMESTAMP_TZ:
          if (paramCount == 1) {
            rewrittenExpression =
                new CastExpression(functionName.startsWith("TRY") ? "Try_Cast" : null,
                    parameters.get(0), "TIMESTAMP WITH TIME ZONE");
          }
          break;
        case DATE_PART:
          if (paramCount == 2) {
            function.setParameters(toDateTimePart(parameters.get(0)), parameters.get(1));
          }
          break;
        case LAST_DAY:
          if (paramCount == 2) {
            throw new RuntimeException("LAST_DATE with DatePart is not supported.");
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
            function.setName(functionName + "$$");
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
        case BIT_LENGTH:
          if (hasParameters && parameters.size() == 1) {
            CaseExpression caseExpression = new CaseExpression(
                new Function("Octet_Length$$",
                    new CastExpression("Try_Cast", parameters.get(0), "BLOB")),
                new WhenClause(new StringValue("VARCHAR"),
                    new Function("Octet_Length$$",
                        new Function("Encode",
                            new CastExpression("Try_Cast", parameters.get(0), "VARCHAR")))))
                .withSwitchExpression(new Function("TypeOf", parameters.get(0)));
            rewrittenExpression = BinaryExpression.multiply(new LongValue(8), caseExpression);
          }
          break;
        case OCTET_LENGTH:
          // case typeof('abc')
          // when 'VARCHAR' then OCTET_LENGTH(encode('abc'))
          // else octet_length( try_cast('abc' AS BLOB) ) end
          if (hasParameters && parameters.size() == 1) {
            rewrittenExpression = new CaseExpression(
                new Function("Octet_Length$$",
                    new CastExpression("Try_Cast", parameters.get(0), "BLOB")),
                new WhenClause(new StringValue("VARCHAR"),
                    new Function("Octet_Length$$",
                        new Function("Encode",
                            new CastExpression("Try_Cast", parameters.get(0), "VARCHAR")))))
                .withSwitchExpression(new Function("TypeOf", parameters.get(0)));
          }
          break;
        case CHAR:
          function.setName("Chr");
          break;
        case INSERT:
          // INSERT('abcdef', 3, 2, 'zzz')
          // substr('abcdef',0,3) || 'zzz' || substr('abcdef', 2+3)
          rewrittenExpression = BinaryExpression.concat(
              new Function("SubStr", parameters.get(0), new LongValue(0), parameters.get(1)),
              parameters.get(3), new Function("SubStr", parameters.get(0),
                  BinaryExpression.add(parameters.get(1), parameters.get(2))));
          break;
        case RTRIMMED_LENGTH:
          // LEN(RTRIM(' ABCD '))
          if (hasParameters && parameters.size() == 1) {
            function.setName("Len$$");
            function.setParameters(new Function("RTrim", parameters.get(0)));
            break;
          }
        case SPACE:
          if (hasParameters && parameters.size() == 1) {
            function.setName("Repeat");
            function.setParameters(new StringValue(" "), parameters.get(0));
            break;
          }
        case SPLIT_TO_TABLE:
          if (hasParameters && parameters.size() == 2) {
            function.setName("regexp_split_to_table");
            function.setParameters(parameters.get(0),
                new Function("regexp_escape", parameters.get(1)));
          }
          break;
        case STRTOK_SPLIT_TO_TABLE:
          if (hasParameters && parameters.size() == 2) {

            // list_aggregate( list_transform( str_split_regex('hello␣world. 42', ''), x ->
            // regexp_escape(x)), 'string_agg', '|')
            Function splitParameter = new Function("list_aggregate",
                new Function("list_transform",
                    new Function("str_split_regex", parameters.get(1), new StringValue("")),
                    new LambdaExpression("x", new Function("regexp_escape", new Column("x")))),
                new StringValue("string_agg"), new StringValue("|"));

            function.setName("regexp_split_to_table");
            function.setParameters(parameters.get(0), splitParameter);
          }
          break;
        case STRTOK:
          if (hasParameters && parameters.size() == 3) {
            Function splitParameter = new Function("list_aggregate",
                new Function("list_transform",
                    new Function("str_split_regex", parameters.get(1), new StringValue("")),
                    new LambdaExpression("x", new Function("regexp_escape", new Column("x")))),
                new StringValue("string_agg"), new StringValue("|"));

            function.setName("Str_Split_Regex");
            function.setParameters(parameters.get(0), splitParameter);

            rewrittenExpression = new ArrayExpression(function, parameters.get(2));
          }
          break;
        case STRTOK_TO_ARRAY:
          if (hasParameters && parameters.size() == 2) {
            Function splitParameter = new Function("list_aggregate",
                new Function("list_transform",
                    new Function("str_split_regex", parameters.get(1), new StringValue("")),
                    new LambdaExpression("x", new Function("regexp_escape", new Column("x")))),
                new StringValue("string_agg"), new StringValue("|"));

            function.setName("Str_Split_Regex");
            function.setParameters(parameters.get(0), splitParameter);
          }
          break;
        case UUID_STRING:
          function.setName("UUID");
          break;
        case CHARINDEX:
        case POSITION:
          if (hasParameters) {
            switch (parameters.size()) {
              case 2:
                function.setName("InStr");
                function.setParameters(parameters.get(1), parameters.get(0));
                break;
              case 3:
                function.setName("InStr");
                function.setParameters(new Function("SubStr", parameters.get(1), parameters.get(2)),
                    parameters.get(0));
                break;
            }
          }
          break;
        case EDITDISTANCE:
          if (hasParameters) {
            switch (parameters.size()) {
              case 2:
                function.setName("editdist3");
                function.setParameters(parameters.get(0), parameters.get(1));
                break;
              case 3:
                function.setName("Least");
                function.setParameters(
                    new Function("editdist3", parameters.get(0), parameters.get(1)),
                    parameters.get(2));
                break;
            }
          }
          break;
        case ENDSWITH:
          if (hasParameters && parameters.size() == 2) {
            function.setName("Ends_With");
          }
          break;
        case STARTSWITH:
          if (hasParameters && parameters.size() == 2) {
            function.setName("Starts_With");
          }
          break;
        case JAROWINKLER_SIMILARITY:
          if (hasParameters && parameters.size() == 2) {
            function.setName("JARO_WINKLER_SIMILARITY");
          }
          break;
        case ARRAYAGG:
          function.setName("ARRAY_AGG");
          break;
        case ARRAY_CONSTRUCT:
          rewrittenExpression = new ArrayConstructor(parameters, true);
          break;
        case ARRAY_COMPACT:
          if (hasParameters && parameters.size() == 1) {
            function.setName("list_filter");
            function.setParameters(parameters.get(0),
                new LambdaExpression("x", new IsNullExpression(new Column("x")).withNot(true)));
          }
          break;
        case ARRAY_CONSTRUCT_COMPACT:
          if (hasParameters) {
            function.setName("list_filter");
            function.setParameters(new ArrayConstructor(parameters, true),
                new LambdaExpression("x", new IsNullExpression(new Column("x")).withNot(true)));
          }
          break;
        case ARRAY_CONTAINS:
          if (paramCount == 2) {
            function.setParameters(parameters.get(1), parameters.get(0));
          }
          break;
        case ARRAY_EXCEPT:
          // list_filter(['A', 'B', 'C'], x -> not list_contains(['B', 'C'],x) )
          if (paramCount == 2) {
            function.setName("List_Filter");
            function.setParameters(parameters.get(0), new LambdaExpression("x", new NotExpression(
                new Function("List_Contains", parameters.get(1), new Column("x")))));
          }
          break;
        case ARRAY_FLATTEN:
          function.setName("Flatten");
          break;
        case ARRAY_GENERATE_RANGE:
          function.setName("Range");
          break;
        case ARRAY_INSERT:
          // [0,1,2,3][0:2] || ['hello'] || [0,1,2,3][2+1:]
          if (paramCount == 3) {
            rewrittenExpression = BinaryExpression.concat(
                new ArrayExpression(parameters.get(0), new LongValue(0), parameters.get(1)),
                new ArrayConstructor(new ExpressionList<Expression>(parameters.get(2)), false),
                new ArrayExpression(parameters.get(0),
                    BinaryExpression.add(parameters.get(1), new LongValue(1)), null));
          }
          break;
        case ARRAY_INTERSECTION:
          function.setName("Array_Intersect");
          break;
        case ARRAY_MAX:
          // list_reverse_sort(list_filter([20, 0, NULL, 10, NULL], x -> x is not null))[1]
          if (paramCount == 1) {
            rewrittenExpression = new ArrayExpression(
                new Function("list_reverse_sort",
                    new Function("list_filter", parameters.get(0),
                        new LambdaExpression("x",
                            new IsNullExpression(new Column("x")).withNot(true)))),
                new LongValue(1));
          }
          break;
        case ARRAY_MIN:
          if (paramCount == 1) {
            rewrittenExpression = new ArrayExpression(
                new Function("list_sort",
                    new Function("list_filter", parameters.get(0),
                        new LambdaExpression("x",
                            new IsNullExpression(new Column("x")).withNot(true)))),
                new LongValue(1));
          }
          break;
        case ARRAY_POSITION:
          // nullif(ARRAY_POSITION(array['hello', 'hi'], 'hi'::varchar)-1, -1)
          if (paramCount == 2) {
            function.setName("NullIf");
            function.setParameters(BinaryExpression.subtract(
                new Function("Array_Position$$", parameters.get(1), parameters.get(0)),
                new LongValue(1)), new LongValue(-1));
          }
          break;
        case ARRAY_PREPEND:
          if (paramCount == 2) {
            function.setParameters(parameters.get(1), parameters.get(0));
          }
          break;
        case ARRAY_REMOVE:
          // list_filter([1, 5, 5.00, 5.00::DOUBLE, '5', 5, NULL], x -> x!=5)
          if (paramCount == 2) {
            function.setName("List_Filter");
            function.setParameters(parameters.get(0),
                new LambdaExpression("x", new NotEqualsTo(new Column("x"), parameters.get(1))));
          }
          break;
        case ARRAY_REMOVE_AT:
          // [2, 5, 7][:0] || [2, 5, 7][0+2:]
          if (paramCount == 2) {
            warning("Negative positions not supported");
            rewrittenExpression = BinaryExpression.concat(
                new ArrayExpression(parameters.get(0), null, parameters.get(1)),
                new ArrayExpression(parameters.get(0),
                    BinaryExpression.add(parameters.get(1), new LongValue(2)), null));
          }
          break;
        case ARRAY_SIZE:
          if (paramCount == 1) {
            function.setName("Len$$");
          }
          break;
        case ARRAY_SLICE:
          if (paramCount == 3) {
            warning("Negative position not supported");
            function.setParameters(parameters.get(0),
                BinaryExpression.add(parameters.get(1), new LongValue(1)), parameters.get(2));
          }
          break;
        case ARRAY_SORT:
          switch (paramCount) {
            case 3:
              function.setParameters(parameters.get(0),
                  new Function("If", parameters.get(1), new StringValue("ASC"),
                      new StringValue("DESC")),
                  new Function("If", parameters.get(2), new StringValue("NULLS FIRST"),
                      new StringValue("NULLS LAST")));
              break;
            case 2:
              function.setParameters(parameters.get(0), new Function("If", parameters.get(1),
                  new StringValue("ASC"), new StringValue("DESC")));
              break;
          }
          break;
        case ARRAY_TO_STRING:
          function.setName("Array_To_String$$");
          break;
        case ARRAYS_OVERLAP:
          // len(Array_Intersect(ARRAY['hello', 'aloha'],
          // array['hello', 'hi', 'hey']))>0
          if (paramCount == 2) {
            rewrittenExpression = new GreaterThan(
                new Function("Len$$",
                    new Function("Array_Intersect", parameters.get(0), parameters.get(1))),
                new LongValue(0));
          }
          break;
        case VARIANCE_POP:
          function.setName("Var_Pop");
          break;
        case VARIANCE_SAMP:
          function.setName("Var_Samp");
          break;
        case BITAND_AGG:
          if (paramCount == 1) {
            function.setName("Bit_And");
            function.setParameters(new CastExpression(parameters.get(0), "INT"));
            break;
          }
        case BITOR_AGG:
          if (paramCount == 1) {
            function.setName("Bit_Or");
            function.setParameters(new CastExpression(parameters.get(0), "INT"));
            break;
          }
        case BITXOR_AGG:
          if (paramCount == 1) {
            function.setName("Bit_Xor");
            function.setParameters(new CastExpression(parameters.get(0), "INT"));
            break;
          }
        case BOOLAND_AGG:
          function.setName("Bool_And");
          break;
        case BOOLOR_AGG:
          function.setName("Bool_Or");
          break;
        case BOOLXOR_AGG:
          function.setName("Bool_Xor");
          break;
        case SKEW:
          function.setName("Skewness");
          break;
        case GROUPING_ID:
          function.setName("Grouping");
          break;
        case TO_VARCHAR:
          function.setName("To_Char");
          break;
        case TO_BINARY:
          function.setName("Encode");
          break;
        case TRY_TO_BINARY:
          if (paramCount == 1) {
            function.setName("Encode");
            function.setParameters(new CastExpression("Try_Cast", parameters.get(0), "VARCHAR"));
          }
          break;
        case TO_DECIMAL:
        case TO_NUMBER:
        case TO_NUMERIC:
        case TRY_TO_DECIMAL:
        case TRY_TO_NUMBER:
        case TRY_TO_NUMERIC:
          // TO_DECIMAL( <expr> [, '<format>' ] [, <precision> [, <scale> ] ] )
          // list_aggregate(regexp_extract_all('-1,000.00', '[\+|\-\d|\.]'),'string_agg',
          // '')::NUMERIC

          Function f1 = new Function("List_Aggregate",
              new Function("Regexp_Extract_All", parameters.get(0),
                  new StringValue("[\\+|\\-\\d|\\.]")),
              new StringValue("string_agg"), new StringValue(""));
          f1 = new Function("If",
              new EqualsTo(new Function("TypeOf", parameters.get(0)), new StringValue("VARCHAR")),
              f1, parameters.get(0));

          switch (paramCount) {
            case 4:
              warning("Format Parameter not supported.");
              if (parameters.get(2) instanceof LongValue
                  && parameters.get(3) instanceof LongValue) {
                String typeStr = "DECIMAL(" + ((LongValue) parameters.get(2)).getValue() + ", "
                    + ((LongValue) parameters.get(3)).getValue() + ")";
                rewrittenExpression = new CastExpression(
                    functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, typeStr);
              }
              break;
            case 3:
              if (parameters.get(1) instanceof StringValue
                  && parameters.get(2) instanceof LongValue) {
                warning("Format Parameter not supported.");
                String typeStr = "DECIMAL(" + ((LongValue) parameters.get(2)).getValue() + ")";
                rewrittenExpression = new CastExpression(
                    functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, typeStr);
              } else if (parameters.get(1) instanceof LongValue
                  && parameters.get(2) instanceof LongValue) {
                String typeStr = "DECIMAL(" + ((LongValue) parameters.get(1)).getValue() + ", "
                    + ((LongValue) parameters.get(2)).getValue() + ")";
                rewrittenExpression = new CastExpression(
                    functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, typeStr);
              }
              break;
            case 2:
              if (parameters.get(1) instanceof StringValue) {
                warning("Format Parameter not supported.");
                rewrittenExpression = new CastExpression(
                    functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, "DECIMAL(12,0)");
              } else if (parameters.get(1) instanceof LongValue) {
                String typeStr = "DECIMAL(" + ((LongValue) parameters.get(1)).getValue() + ")";
                rewrittenExpression = new CastExpression(
                    functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, typeStr);
              }
              break;
            case 1:
              rewrittenExpression = new CastExpression(
                  functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, "DECIMAL(12,0)");
              break;
          }
          break;
        case TO_DOUBLE:
        case TRY_TO_DOUBLE:
          switch (paramCount) {
            case 2:
              warning("Format parameter not supported");
            case 1:
              rewrittenExpression =
                  new CastExpression(functionName.startsWith("TRY") ? "Try_Cast" : "Cast",
                      parameters.get(0), "DOUBLE");
          }
          break;
        case TO_BOOLEAN:
        case TRY_TO_BOOLEAN:
          switch (paramCount) {
            case 2:
              warning("Format parameter not supported");
            case 1:
              rewrittenExpression =
                  new CastExpression(functionName.startsWith("TRY") ? "Try_Cast" : "Cast",
                      parameters.get(0), "BOOLEAN");
          }
          break;
        case RANDOM:
          if (paramCount == 1) {
            warning("SEED parameter not supported");
          }
          // ((random() - 0.5) * 1E19)::int64
          rewrittenExpression = new CastExpression("Cast",
              BinaryExpression.multiply(
                  new ParenthesedExpressionList<>(
                      BinaryExpression.subtract(new Function("Random$$"), new DoubleValue(0.5d))),
                  new DoubleValue("1E19")),
              "INT64");
          break;
        case DIV0:
        case DIV0NULL:
          if (paramCount == 2) {
            function.setName("Coalesce");
            function.setParameters(new Function("Divide", parameters.get(0), parameters.get(1)),
                new LongValue(0));
          }
          break;
        case ROUND:
          switch (paramCount) {
            case 3:
              warning("Limited support for rounding mode");
              if ("'HALF_TO_EVEN'".equalsIgnoreCase(parameters.get(2).toString())) {
                function.setName("Round_Even");
              }
            case 2:
              function.setParameters(parameters.get(0), parameters.get(1));
              break;
          }
          break;
        case SQUARE:
          if (paramCount == 1) {
            function.setName("Power");
            function.setParameters(parameters.get(0), new LongValue(2));
          }
          break;
        case CHECK_JSON:
          function.setName("JSon_Valid");
          break;
        case TRY_PARSE_JSON:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression("Try_Cast", parameters.get(0), "JSON");
          }
          break;
        case GET_PATH:
          function.setName("Json_Value");
          break;
        case OBJECT_KEYS:
          function.setName("Json_Keys");
          break;
        case STRIP_NULL_VALUE:
          if (paramCount == 1) {
            rewrittenExpression = parameters.get(0);
          }
          break;
        case ST_GEOMETRYFROMWKB:
        case ST_GEOMFROMWKB:
        case ST_GEOMETRYFROMEWKB:
        case ST_GEOMFROMEWKB:
          if (paramCount > 1) {
            warning("SRID and ALLOW_INVALID are not supported.");
          }
          function.setName("If");
          function.setParameters(
              new Function("REGEXP_MATCHES", parameters.get(0), new StringValue("^[0-9A-Fa-f]+$")),
              new Function("ST_GeomFromHEXEWKB$$", parameters.get(0)),
              new Function("ST_GeomFromWKB$$", new CastExpression(parameters.get(0), "BLOB")));
          break;
        case ST_GEOMFROMWKT:
        case ST_GEOMETRYFROMEWKT:
        case ST_GEOMFROMEWKT:
        case ST_GEOMETRYFROMTEXT:
        case ST_GEOMFROMTEXT:
        case ST_GEOMETRYFROMWKT:
          function.setName("ST_GEOMFROMTEXT$$");
          if (paramCount > 1) {
            warning("SRID and ALLOW_INVALID are not supported.");
          }
          if (parameters.get(0) instanceof StringValue) {
            String regex = "(?i)SRID=\\d+;";
            String s = ((StringValue) parameters.get(0)).getValue();
            if (s.toUpperCase().contains("SRID")) {
              warning("SRID is not supported");
              function.setParameters(new StringValue(s.replaceAll(regex, "")));
            } else {
              function.setParameters(parameters.get(0));
            }
          } else {
            function.setParameters(parameters.get(0));
          }
          break;
        case TO_GEOMETRY:
          if (paramCount > 1) {
            warning("SRID and ALLOW_INVALID are not supported.");
          }
          rewrittenExpression = new CastExpression("Cast", parameters.get(0), "GEOMETRY");
          break;
        case TRY_TO_GEOMETRY:
          if (paramCount > 1) {
            warning("SRID and ALLOW_INVALID are not supported.");
          }
          rewrittenExpression = new CastExpression("Try_Cast", parameters.get(0), "GEOMETRY");
          break;
      }
    }
    if (rewrittenExpression == null) {
      super.visit(function, params);
    } else {
      rewrittenExpression.accept(this, null);
    }
    return null;
  }

  @Override
  public <S> StringBuilder visit(AnalyticExpression function, S params) {
    String functionName = function.getName();

    if (UnsupportedFunction.from(function) != null) {
      throw new RuntimeException(
          "Unsupported: " + functionName + " is not supported by DuckDB (yet).");
    } else if (functionName.endsWith("$$")) {
      // work around for transpiling already transpiled functions twice
      // @todo: figure out a better way to achieve that
      function.setName(functionName.substring(0, functionName.length() - 2));
      super.visit(function, params);
      return null;
    }

    if (function.getNullHandling() != null && function.isIgnoreNullsOutside()) {
      function.setIgnoreNullsOutside(false);
    }

    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case ARRAYAGG:
          function.setName("ARRAY_AGG");
          break;
        case VARIANCE_POP:
          function.setName("Var_Pop");
          break;
        case VARIANCE_SAMP:
          function.setName("Var_Samp");
          break;
        case BITAND_AGG:
          function.setName("Bit_And");
          function.setExpression(new CastExpression(function.getExpression(), "INT"));
          break;
        case BITOR_AGG:
          function.setName("Bit_Or");
          function.setExpression(new CastExpression(function.getExpression(), "INT"));
          break;
        case BITXOR_AGG:
          function.setName("Bit_Xor");
          function.setExpression(new CastExpression(function.getExpression(), "INT"));
          break;
        case BOOLAND_AGG:
          function.setName("Bool_And");
          break;
        case BOOLOR_AGG:
          function.setName("Bool_Or");
          break;
        case BOOLXOR_AGG:
          function.setName("Bool_Xor");
          break;
        case SKEW:
          function.setName("Skewness");
          break;
        case GROUPING_ID:
          function.setName("Grouping");
          break;
      }
    }

    super.visit(function, params);
    return null;
  }

  @Override
  public <S> StringBuilder visit(Column column, S params) {
    if (column.getColumnName().equalsIgnoreCase("SYSDATE")) {
      column.setColumnName("CURRENT_DATE");
    }
    super.visit(column, params);
    return null;
  }

  @Override
  public <S> StringBuilder visit(HexValue hexValue, S params) {
    CastExpression castExpression = new CastExpression("$$", hexValue.getBlob(), "BLOB");
    super.visit(castExpression, params);
    return null;
  }

  @Override
  public <S> StringBuilder visit(LikeExpression likeExpression, S params) {
    ArrayList<LikeExpression> likes = new ArrayList<>();
    Expression l = likeExpression.getLeftExpression();
    Expression r = likeExpression.getRightExpression();


    if (r instanceof Function) {
      Function f = (Function) r;
      String name = f.getName().toUpperCase();

      if (name.equals("ALL")) {
        for (Expression e : f.getParameters()) {
          likes.add(new LikeExpression().withLeftExpression(l).withRightExpression(e)
              .withEscape(likeExpression.getEscape())
              .setLikeKeyWord(likeExpression.getLikeKeyWord()));
        }
        BinaryExpression.and(likes.toArray(new LikeExpression[0])).accept(this, null);
        return null;
      } else if (name.equals("ANY")) {
        for (Expression e : f.getParameters()) {
          likes.add(new LikeExpression().withLeftExpression(l).withRightExpression(e)
              .withEscape(likeExpression.getEscape())
              .setLikeKeyWord(likeExpression.getLikeKeyWord()));
        }
        BinaryExpression.or(likes.toArray(new LikeExpression[0])).accept(this, null);
        return null;
      }
    }

    super.visit(likeExpression, params);
    return null;
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
    } else if (colDataType.getDataType().equalsIgnoreCase("VARIANT")) {
      colDataType.setDataType("VARCHAR");
    }
    return super.rewriteType(colDataType);
  }
}
