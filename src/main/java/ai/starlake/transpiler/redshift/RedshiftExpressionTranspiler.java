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
package ai.starlake.transpiler.redshift;

import ai.starlake.transpiler.JSQLExpressionTranspiler;
import ai.starlake.transpiler.JSQLTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;

public class RedshiftExpressionTranspiler extends JSQLExpressionTranspiler {
  public RedshiftExpressionTranspiler(JSQLTranspiler transpiler, StringBuilder buffer) {
    super(transpiler, buffer);
  }

  enum TranspiledFunction {
    // @FORMATTER:OFF
    BPCHARCMP, BTRIM, BTTEXT_PATTERN_CMP, CHAR_LENGTH, CHARACTER_LENGTH, TEXTLEN, LEN, CHARINDEX, STRPOS, COLLATE, OCTETINDEX

    , REGEXP_COUNT, REGEXP_INSTR, REGEXP_REPLACE, REGEXP_SUBSTR, REPLICATE

    ;
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
    CRC32, DIFFERENCE, INITCAP, SOUNDEX, STRTOL

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


  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public void visit(Function function) {
    String functionName = function.getName();

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
          if (parameters != null) {
            switch (parameters.size()) {
              case 2:
                function.setParameters(parameters.get(0), parameters.get(1), new StringValue(""));
                break;
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
                break;
            }
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

      }
    }
    if (rewrittenExpression == null) {
      super.visit(function);
    } else {
      rewrittenExpression.accept(this);
    }
  }
}
