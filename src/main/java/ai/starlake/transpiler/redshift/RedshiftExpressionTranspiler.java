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
package ai.starlake.transpiler.redshift;

import ai.starlake.transpiler.JSQLExpressionTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.util.AbstractMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class RedshiftExpressionTranspiler extends JSQLExpressionTranspiler {
  public static final Pattern NUMBER_FORMAT_PATTERN =
      Pattern.compile("((?<!(%))(?<!(%-))(?i:[09D.G,]+))");

  public RedshiftExpressionTranspiler(SelectDeParser deParser, StringBuilder buffer) {
    super(deParser, buffer);
  }

  enum TranspiledFunction {
    // @formatter:off
    BPCHARCMP, BTRIM, BTTEXT_PATTERN_CMP, CHAR_LENGTH, CHARACTER_LENGTH, TEXTLEN, LEN, CHARINDEX, STRPOS, COLLATE
    , OCTETINDEX, REGEXP_COUNT, REGEXP_INSTR, REGEXP_REPLACE, REGEXP_SUBSTR, REPLICATE, ADD_MONTHS, CONVERT_TIMEZONE
    , DATE_CMP, DATE_CMP_TIMESTAMP, DATE_CMP_TIMESTAMPTZ, DATEADD, DATEDIFF, DATE_PART, DATE_PART_YEAR, DATE_TRUNC
    , GETDATE, INTERVAL_CMP, MONTHS_BETWEEN, SYSDATE, TIMEOFDAY, TIMESTAMP_CMP, TIMESTAMP_CMP_DATE
    , TIMESTAMP_CMP_TIMESTAMPTZ, TIMESTAMPTZ_CMP, TIMESTAMPTZ_CMP_DATE, TIMESTAMPTZ_CMP_TIMESTAMP, TIMEZONE
    , TO_TIMESTAMP, ARRAY, ARRAY_FLATTEN, GET_ARRAY_LENGTH, SPLIT_TO_ARRAY, SUBARRAY, DEXP, DLOG1, DLOG10, LOG
    , TRUNC, TO_CHAR, TO_NUMBER, CONVERT, APPROXIMATE_PERCENTILE_DISC, APPROXIMATE_COUNT, GEOMETRYTYPE, ST_GEOMFROMTEXT
    , ST_GEOGFROMTEXT, ST_ASEWKB, ST_ASEWKT, ST_ASBINARY, ST_ASGEOJSON, ST_ASHEXEWKB, ST_ASTEXT, ST_BUFFER, ST_COLLECT
    , ST_DISTANCESPHERE, ST_FORCE3D, ST_GEOGFROMWKB, ST_GEOMFROMWKB, ST_GEOMFROMEWKB, ST_GEOMFROMEWKT, ST_LENGTHSPHERE
    , ST_LENGTH2D, ST_MAKEPOINT, ST_NDIMS, ST_PERIMETER2D, ST_POLYGON
    , JSON_PARSE, CAN_JSON_PARSE, IS_VALID_JSON, IS_VALID_JSON_ARRAY, JSON_EXTRACT_ARRAY_ELEMENT_TEXT, JSON_EXTRACT_PATH_TEXT;
    // @formatter:on


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


  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public <S> StringBuilder visit(Function function, S params) {
    String functionName = function.getName();
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
      super.visit(function, null);
      return null;
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
        case BPCHARCMP:
        case BTTEXT_PATTERN_CMP:
          rewrittenExpression = new CaseExpression(new LongValue(0),
              new WhenClause(new GreaterThan(parameters.get(0), parameters.get(1)),
                  new LongValue(1)),
              new WhenClause(new MinorThan(parameters.get(0), parameters.get(1)),
                  new LongValue(-1)));
          break;
        case BTRIM:
          function.setName("TRIM");
          break;
        case CHAR_LENGTH:
        case CHARACTER_LENGTH:
        case LEN:
        case TEXTLEN:
          function.setName("LENGTH");
          break;
        case CHARINDEX:
        case STRPOS:
          if (parameters != null && parameters.size() == 2) {
            function.setName("InStr$$");
            function.setParameters(new CastExpression(parameters.get(1), "VARCHAR"),
                parameters.get(0));
          }
          break;
        case COLLATE:
          // 'en-u-kf-upper-kn-true' specifies the English locale (en) with case-insensitive
          // collation (kf-upper-kn-true)
          // 'en-u-kf-upper' specifies the English locale (en) with a case-sensitive collation
          // (kf-upper)

          // 'ICU; [caseLevel=yes]'
          function.setName("icu_sort_key");
          if (parameters.get(1).toString().equals("case_sensitive")) {
            function.setParameters(parameters.get(0), new StringValue("und:cs"));
          } else {
            function.setParameters(parameters.get(0), new StringValue("und:ci"));
          }
          break;
        case OCTETINDEX:
          // SELECT octet_length(encode(substr('Άμαζον Amazon Redshift',0 , instr('Άμαζον Amazon
          // Redshift', 'Redshift')+1))) as index;

          Expression instr = new Addition()
              .withLeftExpression(new Function("Instr", parameters.get(1), parameters.get(0)))
              .withRightExpression(new LongValue(1));
          Function substrFunction =
              new Function("SubStr", parameters.get(1), new LongValue(0), instr);

          function.setName("Octet_Length$$");
          function.setParameters(new Function("Encode", substrFunction));
          break;
        case REGEXP_COUNT:
          function.setName("Length$$");
          function.setParameters(
              new Function("regexp_split_to_array", parameters.get(0), parameters.get(1)));
          rewrittenExpression =
              new Subtraction().withLeftExpression(function).withRightExpression(new LongValue(1));
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
        case REGEXP_REPLACE:
          // REGEXP_REPLACE( source_string, pattern [, replace_string [ , position [, parameters ] ]
          // ] )
          switch (paramCount) {
            case 4:
              warning("Position Parameter unsupported");
              parameters.remove(3);
              break;
            case 5:
              if (parameters.get(4).toString().contains("p")) {
                warning("PCRE unsupported");
              }
              warning("Position Parameter unsupported");
              parameters.remove(3);
              function.setName(functionName + "$$");
              break;
          }
          break;
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
        case REPLICATE:
          function.setName("Repeat");
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
        case CONVERT_TIMEZONE:
          if (parameters != null) {
            switch (parameters.size()) {
              case 2:
                rewrittenExpression = new TimezoneExpression(
                    rewriteDateLiteral(parameters.get(1),
                        DateTimeLiteralExpression.DateTime.TIMESTAMP),
                    new StringValue("UTC"), parameters.get(0));
                break;
              case 3:
                rewrittenExpression = new TimezoneExpression(
                    rewriteDateLiteral(parameters.get(2),
                        DateTimeLiteralExpression.DateTime.TIMESTAMP),
                    parameters.get(0), parameters.get(1));
                break;
            }
          }
          break;
        case TRUNC:
          // case typeof(expr)
          // WHEN 'TIMESTAMP' THEN date_trunc('day', expr))
          // ELSE ROUND( Try_cast(expr AS DECIMAL), 3) <-- this does not work!
          // END

          warning("Strictly interpreted as DATE_TRUNC");

          WhenClause whenClause = new WhenClause(new StringValue("TIMESTAMP"),
              new Function("Date_Trunc", new StringValue("Day"), parameters.get(0)));
          rewrittenExpression = new CaseExpression(whenClause)
              .withSwitchExpression(new Function("TypeOf", parameters.get(0)));
          break;
        case DATE_CMP:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression = cmp(parameters.get(0), "DATE", parameters.get(1), "DATE");
          }
          break;
        case DATE_CMP_TIMESTAMP:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression = cmp(parameters.get(0), "DATE", parameters.get(1), "TIMESTAMP");
          }
          break;
        case DATE_CMP_TIMESTAMPTZ:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression = cmp(parameters.get(0), "DATE", parameters.get(1), "TIMESTAMPTZ");
          }
          break;
        case DATEADD:
          if (parameters != null && parameters.size() == 3) {
            // date_add(caldate, (30 ||' day')::INTERVAL)
            function.setName("date_add$$");
            function.setParameters(parameters.get(2),
                new CastExpression(
                    new ParenthesedExpressionList<>(BinaryExpression.concat(parameters.get(1),
                        new StringValue(" " + parameters.get(0).toString()))),
                    "INTERVAL"));
          }
          break;
        case DATEDIFF:
          if (parameters != null && parameters.size() == 3) {
            function.setParameters(new StringValue(parameters.get(0).toString()),
                castDateTime(parameters.get(1)), castDateTime(parameters.get(2)));
          }
          break;
        case DATE_PART:
          if (parameters != null && parameters.size() == 2) {
            function.setName("date_part$$");
            function.setParameters(new StringValue(parameters.get(0).toString()),
                castDateTime(parameters.get(1)));
          }
          break;
        case DATE_PART_YEAR:
          if (parameters != null && parameters.size() == 1) {
            function.setName("date_part$$");
            function.setParameters(new StringValue("YEAR"), castDateTime(parameters.get(0)));
          }
          break;
        case DATE_TRUNC:
          function.setName("DATE_TRUNC$$");
          function.setParameters(parameters.get(0), castDateTime(parameters.get(1)));
          break;
        case GETDATE:
          function.setName("Get_Current_Timestamp");
          break;
        case INTERVAL_CMP:
          // case
          // when INTERVAL '3 days' > INTERVAL '1 year' then 1
          // when INTERVAL '3 days' < INTERVAL '1 year' then -1
          // else 0 end as compare

          rewrittenExpression = new CaseExpression(new LongValue(0),
              new WhenClause(
                  new GreaterThan(castInterval(parameters.get(0)), castInterval(parameters.get(1))),
                  new LongValue(1)),
              new WhenClause(
                  new MinorThan(castInterval(parameters.get(0)), castInterval(parameters.get(1))),
                  new LongValue(-1)));
          break;
        case MONTHS_BETWEEN:
          warning("Fraction based on days unsupported.");
          if (parameters != null && parameters.size() == 2) {
            function.setName("Date_Diff$$");
            function.setParameters(new StringValue("MONTH"), castDateTime(parameters.get(1)),
                castDateTime(parameters.get(0)));
          }
          break;
        case TIMEOFDAY:
          // Thu Sep 19 22:53:50.333525 2013 UTC
          function.setName("strftime");
          function.setParameters(new Column("CURRENT_TIMESTAMP"),
              new StringValue("%a %b %-d %H:%M:%S.%n %Y %Z"));
          break;
        case TIMESTAMP_CMP:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression =
                cmp(parameters.get(0), "TIMESTAMP", parameters.get(1), "TIMESTAMP");
          }
          break;
        case TIMESTAMP_CMP_DATE:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression = cmp(parameters.get(0), "TIMESTAMP", parameters.get(1), "DATE");
          }
          break;
        case TIMESTAMP_CMP_TIMESTAMPTZ:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression =
                cmp(parameters.get(0), "TIMESTAMP", parameters.get(1), "TIMESTAMPTZ");
          }
          break;
        case TIMESTAMPTZ_CMP:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression =
                cmp(parameters.get(0), "TIMESTAMPTZ", parameters.get(1), "TIMESTAMPTZ");
          }
          break;
        case TIMESTAMPTZ_CMP_DATE:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression = cmp(parameters.get(0), "TIMESTAMPTZ", parameters.get(1), "DATE");
          }
          break;
        case TIMESTAMPTZ_CMP_TIMESTAMP:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression =
                cmp(parameters.get(0), "TIMESTAMPTZ", parameters.get(1), "TIMESTAMP");
          }
          break;
        case TIMEZONE:
          if (parameters != null && parameters.size() == 2) {
            rewrittenExpression =
                new TimezoneExpression(castDateTime(parameters.get(1)), parameters.get(0));
          }
          break;
        case TO_TIMESTAMP:
          if (parameters != null) {
            switch (parameters.size()) {
              case 3:
                warning("IS_STRICT not supported.");
              case 2:
                if (parameters.get(1) instanceof StringValue) {
                  StringValue stringValue = (StringValue) parameters.get(1);
                  stringValue = new StringValue(toFormat(stringValue.getValue()));

                  function.setName("strptime");
                  function.setParameters(parameters.get(0), stringValue);

                  rewrittenExpression = new TimezoneExpression(
                      new CastExpression(function, "TIMESTAMP"), new StringValue("UTC"));
                } else {
                  throw new RuntimeException(
                      "TO_TIMESTAMP can't be transpiled when FORMAT parameter is not a static string.");
                }
                break;
            }
          }
          break;
        case TO_CHAR:
          switch (paramCount) {
            case 1:
              rewrittenExpression = new CastExpression(parameters.get(0), "VARCHAR");
              break;
            case 2:
              StringValue stringValue = (StringValue) parameters.get(1);
              String formatStr = toFormat(stringValue.getValue());

              stringValue = new StringValue(formatStr);

              // this is totally whack, but I did not see any other way to decide
              // when to used which function
              // @todo: submit PR to DuckDB
              function.setName(formatStr.contains("%g") ? "printf" : "strftime");
              function.setParameters(parameters.get(0), stringValue);
          }
          break;
        case TO_NUMBER:
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
            case 2:
              warning("Format Parameter not supported.");
              rewrittenExpression = new CastExpression(
                  functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, "NUMERIC");
              break;
            case 1:
              rewrittenExpression = new CastExpression(
                  functionName.startsWith("TRY") ? "Try_Cast" : "Cast", f1, "NUMERIC");
              break;
          }
          break;
        case ARRAY:
          rewrittenExpression = new ArrayConstructor(parameters, false);
          break;
        case ARRAY_FLATTEN:
          function.setName("Flatten");
          break;
        case GET_ARRAY_LENGTH:
          function.setName("Len$$");
          break;
        case SPLIT_TO_ARRAY:
          if (parameters != null && parameters.size() == 2) {
            function.setName("regexp_split_to_array");
            function.setParameters(parameters.get(0),
                new Function("regexp_escape", parameters.get(1)));
          }
          break;
        case SUBARRAY:
          function.setName("list_slice");
          break;
        case DEXP:
          function.setName("Exp$$");
          break;
        case DLOG1:
          function.setName("Ln$$");
          break;
        case DLOG10:
        case LOG:
          function.setName("Log$$");
          break;

        case APPROXIMATE_PERCENTILE_DISC:
          warning("without APPROXIMATE");
          function.setName("PERCENTILE_DISC");
          break;
        case APPROXIMATE_COUNT:
          function.setName("approx_count_distinct");
          break;
        case GEOMETRYTYPE:
          function.setName("ST_GeometryType");
          break;
        case ST_GEOGFROMTEXT:
        case ST_GEOMFROMTEXT:
        case ST_GEOMFROMEWKT:
          function.setName("ST_GEOMFROMTEXT$$");
          if (paramCount == 2) {
            warning("SRID is not supported");
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
        case ST_ASBINARY:
          rewrittenExpression = new CastExpression("$$", function.withName("ST_AsWKB$$"), "BLOB");
          break;
        case ST_ASEWKB:
          function.setName("ST_AsWKB$$");
          break;
        case ST_ASEWKT:
          if (paramCount == 2) {
            warning("PRECISION is not supported");
          }
          function.setName("ST_AsText$$");
          function.setParameters(parameters.get(0));
          break;
        case ST_ASGEOJSON:
        case ST_ASTEXT:
          if (paramCount == 2) {
            warning("PRECISION is not supported");
            function.setParameters(parameters.get(0));
          }
          break;
        case ST_ASHEXEWKB:
          function.setName("ST_ASHEXWKB");
          break;
        case ST_BUFFER:
          function.setName("ST_BUFFER$$");
          break;
        case ST_COLLECT:
          switch (paramCount) {
            case 1:
              function.setParameters(new Function("List", parameters));
              break;
            case 2:
              function.setParameters(new Function("Array_value", parameters));
              break;
          }
          break;
        case ST_DISTANCESPHERE:
          warning("Returns wrong results.");
          function.setName("ST_DISTANCE_SPHERE");
          break;
        case ST_FORCE3D:
          function.setName("ST_FORCE3DZ");
          break;
        case ST_GEOMFROMWKB:
        case ST_GEOGFROMWKB:
          function.setName("ST_GEOMFROMHEXWKB");
          break;
        case ST_GEOMFROMEWKB:
          function.setName("ST_GEOMFROMHEXEWKB");
          break;
        case ST_LENGTHSPHERE:
          warning("Results differ");
          function.setName("ST_LENGTH_SPHEROID");
          break;
        case ST_LENGTH2D:
          function.setName("ST_LENGTH");
          break;
        case ST_MAKEPOINT:
          function.setName("ST_POINT");
          break;
        case ST_NDIMS:
          warning("Produces wrong result");
          function.setName("ST_DIMENSION");
          break;
        case ST_PERIMETER2D:
          function.setName("ST_PERIMETER");
          break;
        case ST_POLYGON:
          if (paramCount == 2) {
            warning("SRID is not supported");
          }
          function.setName("ST_MakePolygon");
          function.setParameters(parameters.get(0));
          break;
        case JSON_PARSE:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(parameters.get(0), "JSON");
          }
          break;
        case CAN_JSON_PARSE:
          if (paramCount == 1) {
            rewrittenExpression =
                new IsNullExpression(new CastExpression("Try_Cast", parameters.get(0), "JSON"))
                    .withNot(true);
          }
          break;
        case IS_VALID_JSON:
          if (paramCount == 1) {
            // json_valid(json_strings) AND json_type(try_cast(json_strings AS JSON))!='ARRAY'
            function.setName("Json_Valid");
            rewrittenExpression = new AndExpression(function,
                new NotEqualsTo(
                    new Function("Json_type",
                        new CastExpression("Try_cast", parameters.get(0), "JSON")),
                    new StringValue("ARRAY")));
          }
          break;
        case IS_VALID_JSON_ARRAY:
          if (paramCount == 1) {
            // json_valid(json_strings) AND json_type(try_cast(json_strings AS JSON))='ARRAY'
            function.setName("Json_Valid");
            rewrittenExpression = new AndExpression(function,
                new EqualsTo(
                    new Function("Json_type",
                        new CastExpression("Try_cast", parameters.get(0), "JSON")),
                    new StringValue("ARRAY")));
          }
          break;
        case JSON_EXTRACT_ARRAY_ELEMENT_TEXT:
          if (paramCount == 2) {
            // SELECT ('[111,112,113]'::JSON)[2] e;
            rewrittenExpression = new ArrayExpression(
                new CastExpression("Try_Cast", parameters.get(0), "JSON"), parameters.get(1));
          }
          break;
        case JSON_EXTRACT_PATH_TEXT:
          if (paramCount > 1) {
            rewrittenExpression = new CastExpression(parameters.get(0), "JSON");
            for (int i = 1; i < paramCount; i++) {
              rewrittenExpression = new JsonExpression(rewrittenExpression,
                  List.of(new AbstractMap.SimpleEntry<>(parameters.get(i), "->")));
            }
          }
      }
    }
    if (rewrittenExpression == null) {
      super.visit(function, null);
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

    Expression rewrittenExpression = null;
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case APPROXIMATE_PERCENTILE_DISC:
          warning("without APPROXIMATE");
          function.setName("PERCENTILE_DISC");
          break;
        case APPROXIMATE_COUNT:
          function.setName("approx_count_distinct");
          break;
        case ST_COLLECT:
          rewrittenExpression = new Function("ST_COLLECT$$", function.withName("Array_Agg"));
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

  private static CaseExpression cmp(Expression expr1, String type1, Expression expr2,
      String type2) {
    return new CaseExpression(new LongValue(0),
        new WhenClause(new MinorThan(new CastExpression(castDateTime(expr1), type1),
            new CastExpression(castDateTime(expr2), type2)), new LongValue(-1)),
        new WhenClause(new GreaterThan(new CastExpression(castDateTime(expr1), type1),
            new CastExpression(castDateTime(expr2), type2)), new LongValue(1)));
  }

  public <S> StringBuilder visit(Column column, S params) {
    if (column.getColumnName().equalsIgnoreCase("SYSDATE")) {
      column.setColumnName("CURRENT_DATE");
    }
    super.visit(column, params);
    return null;
  }

  final static String[][] REPLACEMENT =
      {{"YYYY", "%Y"}, {"YYY", "%Y"}, {"YY", "%y"}, {"IYYY", "%G"}, {"MONTH", "%B "},
          {"Month", "%B "}, {"month", "%B "}, {"MON", "%b"}, {"Mon", "%b"}, {"mon", "%b"},
          {"MM", "%m"}, {"WW", "%U"}, {"IW", "%V"}, {"DAY", "%A "}, {"Day", "%A "}, {"day", "%A "},
          {"DY", "%a"}, {"Dy", "%a"}, {"dy", "%a"}, {"DDD", "%j"}, {"DD", "%d"}, {"ID", "%u"},
          {"HH24", "%H"}, {"HH12", "%I"}, {"HH", "%I"}, {"MI", "%M"}, {"SS", "%S"}, {"MS", "%g"},
          {"US", "%f"}, {"AM", "%p"}, {"PM", "%p"}, {"TZ", "%Z"}, {"OF", "%z"}, {"Y", "%-y"}};

  public static String toFormat(final String s) {
    String replacedFormatStr = s;
    for (String[] r : REPLACEMENT) {
      // replace any occurrence except when preceded by "%" or "%-"
      replacedFormatStr = replacedFormatStr.replaceAll("(?<!(%))(?<!(%-))(?i:" + r[0] + ")", r[1]);
    }

    // "SELECT PRINTF('%010.2f', 125.8) AS chars;";
    // 0000125.80
    if (s.equals(replacedFormatStr)) {
      Matcher matcher = NUMBER_FORMAT_PATTERN.matcher(replacedFormatStr);
      while (matcher.find()) {
        String found = matcher.group(1);
        // boolean zeroPadded = found.startsWith("0");

        String replacement = "%g";
        replacedFormatStr = replacedFormatStr.replace(found, replacement);
      }
    }


    return replacedFormatStr;
  }

  public ColDataType rewriteType(ColDataType colDataType) {
    if (colDataType.getDataType().equalsIgnoreCase("FLOAT")) {
      colDataType.setDataType("FLOAT8");
    } else if (colDataType.getDataType().equalsIgnoreCase("DEC")) {
      colDataType.setDataType("DECIMAL");
    }

    return super.rewriteType(colDataType);
  }
}
