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
package ai.starlake.transpiler.databricks;

import ai.starlake.transpiler.JSQLExpressionTranspiler;
import ai.starlake.transpiler.JSQLTranspiler;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class DatabricksExpressionTranspiler extends JSQLExpressionTranspiler {
  public DatabricksExpressionTranspiler(JSQLTranspiler transpiler, StringBuilder buffer) {
    super(transpiler, buffer);
  }

  enum TranspiledFunction {
    // @FORMATTER:OFF
    DATE_FROM_PARTS

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
        case DATE_FROM_PARTS:
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

    if (function.getNullHandling()!=null && function.isIgnoreNullsOutside()) {
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
    }
    return super.rewriteType(colDataType);
  }
}
