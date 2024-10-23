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
package ai.starlake.transpiler.databricks;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.redshift.RedshiftExpressionTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnalyticType;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.util.List;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class DatabricksExpressionTranspiler extends RedshiftExpressionTranspiler {

  public DatabricksExpressionTranspiler(SelectDeParser selectDeParser, StringBuilder buffer) {
    super(selectDeParser, buffer);
  }

  enum TranspiledFunction {
    // @FORMATTER:OFF
    DATE_FROM_PARTS, BINARY, BITMAP_COUNT, BTRIM, CHAR, CHAR_LENGTH, CHARACTER_LENGTH, CHARINDEX, ENDSWITH, STARTSWITH

    , FIND_IN_SET, LEVENSHTEIN, LOCATE, LTRIM, RTRIM, POSITION, REGEXP_REGEX, REGEXP_LIKE, REGEXP_EXTRACT, REGEXP_SUBSTR

    , SHA2, SPACE, SPLIT, STRING, SUBSTR, SUBSTRING_INDEX, TRY_TO_BINARY, TO_BINARY, UNBASE64, ENCODE, DECODE

    , ARRAY

    , GETDATE, NOW, CURDATE, CURRENT_TIMEZONE, DATEADD, DATE_ADD, DATEDIFF, DATE_DIFF, DATE_FORMAT, DATE_FROM_UNIX_DATE, DATE_SUB

    , DAY, DAYOFMONTH, DAYOFWEEK, DAYOFYEAR, HOUR, LAST_DAY, MINUTE, MONTH, QUARTER, SECOND, WEEKDAY, WEEKOFYEAR, YEAR

    , FROM_UNIXTIME, TO_UNIX_TIMESTAMP, MAKE_TIMESTAMP, TIMESTAMP, TO_TIMESTAMP

    , ANY, APPROX_PERCENTILE, ARRAY_AGG, COLLECT_LIST, COLLECT_SET, COUNT, COUNT_IF, FIRST, FIRST_VALUE, LAST, LAST_VALUE

    , PERCENTILE, PERCENTILE_APPROX, REGR_INTERCEPT, REGR_SLOPE, KURTOSIS, SKEWNESS, STD, NTH_VALUE

    , TRY_AVG, TRY_SUM, PERCENT_RANK

    , ARRAY_APPEND, ARRAY_COMPACT, ARRAY_EXCEPT

    ;
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
    return toDateTimePart(expression, JSQLTranspiler.Dialect.DATABRICKS);
  }

  public Expression castInterval(Expression e1, Expression e2) {
    return castInterval(e1, e2, JSQLTranspiler.Dialect.DATABRICKS);
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
      super.visit(function, params);
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
        case DATE_FROM_PARTS:
          break;
        case BINARY:
          function.setName("Encode");
          break;
        case BITMAP_COUNT:
          if (paramCount == 1) {
            function.setName("Bit_Count");
            function.setParameters(new CastExpression(parameters.get(0), "BIT"));
          }
          break;
        case BTRIM:
          function.setName("Trim");
          break;
        case CHAR:
          function.setName("Chr");
          break;
        case CHAR_LENGTH:
        case CHARACTER_LENGTH:
          function.setName("Len$$");
          break;
        case CHARINDEX:
        case LOCATE:
        case POSITION:
          switch (paramCount) {
            case 2:
              function.setName("InStr");
              function.setParameters(parameters.get(1), parameters.get(0));
              break;
            case 3:
              // ifplus( instr(substr('abcbarbar', 5), 'bar'), 0, 5-1)
              function.setName("IfPlus");
              function.setParameters(new Function("InStr",
                  new Function("SubStr", parameters.get(1), parameters.get(2)), parameters.get(0)),
                  new LongValue(0), BinaryExpression.subtract(parameters.get(2), new LongValue(1)));
              break;
          }
          break;
        case ARRAY:
          // see fixed issue #12252
          // function.setName("Array_Value");
          rewrittenExpression = new ArrayConstructor(parameters, false);
          break;
        case ENDSWITH:
          function.setName("Ends_With");
          break;
        case STARTSWITH:
          function.setName("Starts_With");
          break;
        case FIND_IN_SET:
          // list_position(str_split_regex('abc,b,ab,c,def', ','), 'ab')
          if (paramCount == 2) {
            function.setName("List_position");
            function.setParameters(
                new Function("Str_Split_Regex", parameters.get(1), new StringValue(",")),
                parameters.get(0));
          }
          break;
        case LEVENSHTEIN:
          if (paramCount == 3) {
            function.setName("Least");
            function.setParameters(
                new Function("Levenshtein", parameters.get(0), parameters.get(1)),
                parameters.get(2));
          }
          break;
        case LTRIM:
        case RTRIM:
          if (paramCount == 2) {
            function.setParameters(parameters.get(1), parameters.get(0));
          }
          break;
        case REGEXP_REGEX:
        case REGEXP_LIKE:
          function.setName("Regexp_Matches");
          break;
        case REGEXP_SUBSTR:
          function.setName("Regexp_Extract");
        case REGEXP_EXTRACT:
          switch (paramCount) {
            case 4:
              warning("Position parameter is not supported");
              parameters.remove(3);
            case 3:
              warning("Only first occurrence");
              function.setName(functionName + "$$");
              break;
          }
          break;
        case SHA2:
          if (paramCount == 2) {
            warning("Only 256bits supported.");
            function.setName("Sha256");
            function.setParameters(parameters.get(0));
          }
          break;
        case SPACE:
          if (paramCount == 1) {
            function.setName("Repeat");
            function.setParameters(new StringValue(" "), parameters.get(0));
          }
          break;
        case SPLIT:
          switch (paramCount) {
            case 3:
              warning("LIMIT parameter is not supported");
              parameters.remove(2);
            case 2:
              function.setName("Regexp_Split_To_Array");
              function.setParameters(parameters.get(0), parameters.get(1));
          }
          break;
        case STRING:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(parameters.get(0), "VARCHAR");
          }
          break;
        case SUBSTR:
          function.setName("SubString");
          break;
        case SUBSTRING_INDEX:
          // substring_index('www.apache.org', '.', 2)
          // list_aggregate(regexp_split_to_array('www.apache.org', regexp_escape('.'))[1:2],
          // 'string_agg', '.')

          if (paramCount == 3) {
            function.setName("List_aggregate");
            function.setParameters(new ArrayExpression(
                new Function("RegExp_Split_To_Array", parameters.get(0),
                    new Function("RegExp_Escape", parameters.get(1))),
                new LongValue(1), parameters.get(2)), new StringValue("string_agg"),
                parameters.get(1));
          }
          break;
        case TRY_TO_BINARY:
          warning("TRY is not supported.");
        case TO_BINARY:
          switch (paramCount) {
            case 2:
              String p = parameters.get(1).toString().toLowerCase();
              if (p.equals("'hex'")) {
                function.setName("UnHex");
                function.setParameters(parameters.get(0));
              } else if (p.equals("'base64'")) {
                function.setName("From_Base64");
                function.setParameters(parameters.get(0));
              } else if (p.equals("'utf-8'")) {
                function.setName("Encode");
                function.setParameters(parameters.get(0));
              }
              break;
            case 1:
              function.setName("UnHex");
          }
          break;
        case ENCODE:
        case DECODE:
          if (paramCount == 2) {
            warning("CHARSET parameter not supported.");
            function.setParameters(parameters.get(0));
          }
          break;
        case UNBASE64:
          function.setName("From_Base64");
          break;
        case GETDATE:
        case CURDATE:
          rewrittenExpression = new TimeKeyExpression("CURRENT_DATE");
          break;
        case NOW:
          rewrittenExpression = new TimeKeyExpression("CURRENT_TIMESTAMP");
          break;
        case DATEADD:
        case DATE_ADD:
          switch (paramCount) {
            case 2:
              function.setName("DATE_ADD$$");
              function.setParameters(castDateTime(parameters.get(0)), parameters.get(1));
              break;
            case 3:
              function.setName("DATE_ADD$$");
              function
                  .setParameters(castDateTime(parameters.get(2)),
                      new CastExpression(new ParenthesedExpressionList<>(BinaryExpression
                          .concat(parameters.get(1), toDateTimePart(parameters.get(0)))),
                          "INTERVAL"));
              break;
          }
          break;
        case DATE_SUB:
          if (paramCount == 2) {
            function.setName("DATE_ADD$$");
            function.setParameters(castDateTime(parameters.get(0)),
                BinaryExpression.multiply(new LongValue(-1), parameters.get(1)));
            break;
          }
        case DATEDIFF:
          if (paramCount == 2) {
            rewrittenExpression = BinaryExpression.subtract(castDateTime(parameters.get(0)),
                castDateTime(parameters.get(1)));
          }
          break;
        case DATE_DIFF:
          if (paramCount == 3) {
            function.setName("DATE_DIFF$$");
            function.setParameters(toDateTimePart(parameters.get(0)),
                castDateTime(parameters.get(1)), castDateTime(parameters.get(2)));
          }
          break;
        case DATE_FORMAT:
          if (paramCount == 2) {
            function.setName("StrFTime");
            function.setParameters(castDateTime(parameters.get(0)), parameters.get(1));
          }
          break;
        case DATE_FROM_UNIX_DATE:
          if (paramCount == 1) {
            rewrittenExpression = BinaryExpression.add(new CastExpression("DATE", "1970-01-01"),
                new CastExpression(
                    new ParenthesedExpressionList<>(
                        BinaryExpression.concat(parameters.get(0), new StringValue("DAY"))),
                    "INTERVAL"));
          }
          break;
        case DAY:
        case DAYOFMONTH:
        case DAYOFWEEK:
        case DAYOFYEAR:
        case HOUR:
        case LAST_DAY:
        case MINUTE:
        case MONTH:
        case QUARTER:
        case SECOND:
        case WEEKDAY:
        case WEEKOFYEAR:
        case YEAR:
          if (paramCount == 1) {
            function.setParameters(castDateTime(parameters.get(0)));
          }
          break;
        case FROM_UNIXTIME:
          switch (paramCount) {
            case 2:
              warning("FORMAT parameter is not supported yet.");
            case 1:
              rewrittenExpression =
                  BinaryExpression.add(new CastExpression("TIMESTAMP", "1969-12-31T16:00:00.000"),
                      new CastExpression(new ParenthesedExpressionList<>(
                          BinaryExpression.concat(parameters.get(0), new StringValue("SECOND"))),
                          "INTERVAL"));
          }
          break;
        case TO_UNIX_TIMESTAMP:
          switch (paramCount) {
            case 2:
              warning("FORMAT parameter is not supported yet.");
            case 1:
              function.setName("Epoch");
              function.setParameters(castDateTime(parameters.get(0)));
          }
          break;
        case MAKE_TIMESTAMP:
          switch (paramCount) {
            case 7:
              function.setParameters(parameters.get(0), parameters.get(1), parameters.get(2),
                  parameters.get(3), parameters.get(4), parameters.get(5));
              rewrittenExpression = new TimezoneExpression(function, parameters.get(6));
              break;
          }
          break;
        case TO_TIMESTAMP:
          switch (paramCount) {
            case 2:
              warning("FORMAT parameter not supported yet.");
            case 1:
              rewrittenExpression = new CastExpression(parameters.get(0), "TIMESTAMP");
          }
          break;
        case TIMESTAMP:
          if (paramCount == 1) {
            rewrittenExpression = new CastExpression(parameters.get(0), "TIMESTAMP");
          }
          break;
        case ANY:
          function.setName("Any_Value");
          break;
        case APPROX_PERCENTILE:
          function.setName("Approx_Quantile");
          if (paramCount == 3) {
            warning("PRECISION parameter not supported");
            parameters.remove(2);
          }
          break;
        case ARRAY_AGG:
        case COLLECT_LIST:
        case COLLECT_SET:
          // enforce an AnalyticExpression to get access to the aggregate function syntax
          rewrittenExpression = new AnalyticExpression(function).withType(AnalyticType.FILTER_ONLY);
          // preserve the position in the AST
          rewrittenExpression.setASTNode(function.getASTNode());
          break;

        case COUNT:
          // @todo: add support for multiple columns
          // @todo: NULL suppression
          if (paramCount > 1) {
            warning("Only one column supported.");
            while (parameters.size() > 1) {
              parameters.remove(parameters.size() - 1);
            }
          }
          break;
        case COUNT_IF:
          if (function.isDistinct()) {
            warning("DISTINCT is not supported (for Macro function).");
            function.setDistinct(false);
          }
          break;
        case FIRST_VALUE:
          function.setName("First");
        case FIRST:
          // @todo: NULL suppression
          if (paramCount > 1) {
            warning("Ignore NULLs is not supported.");
            while (parameters.size() > 1) {
              parameters.remove(parameters.size() - 1);
            }
          }
          break;
        case LAST_VALUE:
          function.setName("Last");
        case LAST:
          // @todo: NULL suppression
          if (paramCount > 1) {
            warning("Ignore NULLs is not supported.");
            while (parameters.size() > 1) {
              parameters.remove(parameters.size() - 1);
            }
          }
          break;
        case PERCENTILE:
          function.setName("Quantile_Cont");
          if (paramCount == 3) {
            warning("FREQUENCY not supported");
            parameters.remove(2);
          }
          break;
        case PERCENTILE_APPROX:
          function.setName("Approx_Quantile");
          if (paramCount == 3) {
            warning("ACCURACY not supported");
            parameters.remove(2);
          }
          break;
        case REGR_INTERCEPT:
        case REGR_SLOPE:
        case KURTOSIS:
        case SKEWNESS:
          warning("Unreliable, results may differ.");
          break;
        case STD:
          function.setName("StdDev");
          break;
        case TRY_AVG:
          warning("TRY error handling not supported.");
          function.setName("Avg");
          break;
        case TRY_SUM:
          warning("TRY error handling not supported.");
          function.setName("Sum");
          break;
        case ARRAY_APPEND:
          if (paramCount == 2) {
            rewrittenExpression =
                BinaryExpression.concat(parameters.get(0), new ArrayConstructor(parameters.get(1)));
          }
          break;
        case ARRAY_COMPACT:
          if (paramCount == 1) {
            function.setName("List_Filter");
            function.setParameters(parameters.get(0),
                new LambdaExpression("x", new IsNullExpression("x", true)));
          }
          break;
        case ARRAY_EXCEPT:
          if (paramCount == 2) {
            // LIST_DISTINCT(LIST_FILTER([1,2,2,3],X->NOT
            // ARRAY_CONTAINS(LIST_INTERSECT([1,2,2,3],[1,1,3,5]),X)))

            NotExpression notExpression = new NotExpression(new Function("Array_Contains",
                new Function("List_Intersect", parameters.get(0), parameters.get(1)),
                new Column("x")));

            function.setName("List_Distinct");
            function.setParameters(new Function("List_Filter", parameters.get(0),
                new LambdaExpression("x", notExpression)));
          }
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

    Expression rewrittenExpression = null;
    TranspiledFunction f = TranspiledFunction.from(functionName);
    if (f != null) {
      switch (f) {
        case ANY:
          function.setName("Any_Value");
          break;
        case ARRAY_AGG:
          if (isEmpty(function.getFuncOrderBy())) {
            function.setFuncOrderBy(
                List.of(new OrderByElement().withExpression(function.getExpression())));
          }
          break;
        case COLLECT_LIST:
          // todo: add FILTER( column IS NOT NULL)
          function.setName("List");
          if (isEmpty(function.getFuncOrderBy())) {
            function.setFuncOrderBy(
                List.of(new OrderByElement().withExpression(function.getExpression())));
          }
          break;
        case COLLECT_SET:
          // todo: add FILTER( column IS NOT NULL)
          function.setDistinct(true);
          function.setName("List");
          if (isEmpty(function.getFuncOrderBy())) {
            function.setFuncOrderBy(
                List.of(new OrderByElement().withExpression(function.getExpression())));
          }
          break;
        case REGR_INTERCEPT:
        case REGR_SLOPE:
        case KURTOSIS:
        case SKEWNESS:
          warning("Unreliable, results may differ.");
          break;
        case STD:
          function.setName("StdDev");
          break;
        case TRY_AVG:
          warning("TRY error handling not supported.");
          function.setName("Avg");
          break;
        case TRY_SUM:
          warning("TRY error handling not supported.");
          function.setName("Sum");
          break;
        case NTH_VALUE:
          // , ignoreNulls
          if (function.getDefaultValue() != null) {
            if (function.getDefaultValue().toString().equalsIgnoreCase("TRUE")) {
              function.setNullHandling(Function.NullHandling.IGNORE_NULLS);
            } else if (function.getDefaultValue().toString().equalsIgnoreCase("FALSE")) {
              function.setNullHandling(Function.NullHandling.RESPECT_NULLS);
            }
            warning("ignoreNulls parameter not supported, use IGNORE/RESPECT NULLS instead.");
            function.setDefaultValue(null);
          }
          break;
        case PERCENT_RANK:
          if (function.getExpression() != null) {
            warning("PERCENT_RANK needs 0 parameters, got 1");
            function.setExpression(null);
          }
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
  public <S> StringBuilder visit(Column column, S params) {
    if (column.getColumnName().equalsIgnoreCase("SYSDATE")) {
      column.setColumnName("CURRENT_DATE");
    }
    super.visit(column, params);
    return null;
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
