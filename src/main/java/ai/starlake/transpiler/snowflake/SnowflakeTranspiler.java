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

import ai.starlake.transpiler.JSQLTranspiler;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.StructType;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Values;

import java.util.ArrayList;
import java.util.List;

public class SnowflakeTranspiler extends JSQLTranspiler {
  public SnowflakeTranspiler() {
    super(SnowflakeExpressionTranspiler.class);
  }

  public void visit(Values values) {
    boolean wrapped = true;
    for (Expression expression : values.getExpressions()) {
      wrapped &= expression instanceof ExpressionList;
    }

    if (wrapped) {
      ExpressionList<StructType> structs = new ExpressionList<>();
      for (Expression expression : values.getExpressions()) {
        List<SelectItem<?>> selectItems = new ArrayList<>();

        int c = 0;
        for (Expression e : (ExpressionList<?>) expression) {
          selectItems
              .add(new SelectItem<>(SnowflakeExpressionTranspiler.castDateTime(e), "column" + ++c));
        }
        structs.add(new StructType(StructType.Dialect.DUCKDB, null, selectItems));
      }
      ArrayConstructor arr = new ArrayConstructor(structs, false);
      Function f = new Function("Unnest$$", arr,
          new OracleNamedFunctionParameter("recursive", new Column("True")));

      ParenthesedSelect select =
          new ParenthesedSelect().withSelect(new PlainSelect().addSelectItem(f));
      select.accept((FromItemVisitor) this);
    } else {
      super.visit(values);
    }
  }

  public void visit(TableFunction tableFunction) {
    String prefix = tableFunction.getPrefix();
    String name = tableFunction.getFunction().getName();

    if (name.equalsIgnoreCase("table")) {
      PlainSelect select = new PlainSelect();
      for (Expression expression : tableFunction.getFunction().getParameters()) {
        Alias alias = null;
        if (expression instanceof Function) {
          Function f = (Function) expression;
          String fName = f.getName().toUpperCase();

          if (fName.equalsIgnoreCase("SPLIT_TO_TABLE")
              || fName.equalsIgnoreCase("STRTOK_SPLIT_TO_TABLE")) {
            select.addSelectItem(new LongValue(0), new Alias("seq", true));
            select.addSelectItem(new LongValue(0), new Alias("index", true));

            select.addSelectItem(expression, alias);
            alias = new Alias("value", true);
          }
        }
        select.addSelectItem(expression, alias);
      }
      ParenthesedSelect parenthesedSelect =
          new ParenthesedSelect().withSelect(select).withAlias(tableFunction.getAlias());

      visit(parenthesedSelect);
    } else if (prefix.equalsIgnoreCase("lateral")) {
      PlainSelect select = new PlainSelect();
      if (name.equalsIgnoreCase("SPLIT_TO_TABLE")
          || name.equalsIgnoreCase("STRTOK_SPLIT_TO_TABLE")) {
        select.addSelectItem(new LongValue(0), new Alias("seq", true));
        select.addSelectItem(new LongValue(0), new Alias("index", true));

        select.addSelectItem(tableFunction.getFunction(), new Alias("value", true));
      } else {
        select.addSelectItem(tableFunction.getFunction());
      }
      LateralSubSelect s = new LateralSubSelect(select, tableFunction.getAlias());
      visit(s);
    } else {
      super.visit(tableFunction);
    }
  }
}
