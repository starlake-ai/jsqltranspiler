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
package ai.starlake.transpiler;

import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.lang.reflect.InvocationTargetException;


public class JSQLSelectTranspiler extends SelectDeParser {
  /**
   * The Expression transpiler.
   */
  protected JSQLExpressionTranspiler expressionTranspiler;


  /**
   * Instantiates a new transpiler.
   */
  protected JSQLSelectTranspiler(JSQLExpressionTranspiler expressionTranspiler,
      StringBuilder resultBuilder) {
    super(expressionTranspiler, resultBuilder);
  }

  public JSQLSelectTranspiler(Class<? extends ExpressionDeParser> expressionDeparserClass,
      StringBuilder builder) throws NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {
    super(expressionDeparserClass, builder);
    this.setExpressionVisitor(expressionDeparserClass
        .getConstructor(SelectDeParser.class, StringBuilder.class).newInstance(this, builder));
  }


  /**
   * Gets result builder.
   *
   * @return the result builder
   */
  public StringBuilder getResultBuilder() {
    return getBuffer();
  }

  @Override
  public void visit(Top top) {
    // get the parent SELECT
    SimpleNode node = (SimpleNode) top.getASTNode().jjtGetParent();
    while (node.jjtGetValue() == null) {
      node = (SimpleNode) node.jjtGetParent();
    }
    PlainSelect select = (PlainSelect) node.jjtGetValue();

    // rewrite the TOP into a LIMIT
    select.setTop(null);
    select.setLimit(new Limit().withRowCount(top.getExpression()));
  }

  @Override
  public <S> StringBuilder visit(TableFunction tableFunction, S params) {
    String name = tableFunction.getFunction().getName();
    if (name.equalsIgnoreCase("unnest")) {
      PlainSelect select = new PlainSelect()
          .withSelectItems(new SelectItem<>(tableFunction.getFunction(), tableFunction.getAlias()));

      ParenthesedSelect parenthesedSelect =
          new ParenthesedSelect().withSelect(select).withAlias(tableFunction.getAlias());

      visit(parenthesedSelect, params);
    } else {
      super.visit(tableFunction, params);
    }
    return buffer;
  }

  public <S> StringBuilder visit(PlainSelect plainSelect, S params) {
    // remove any DUAL pseudo tables
    FromItem fromItem = plainSelect.getFromItem();
    if (fromItem instanceof Table) {
      Table table = (Table) fromItem;
      if (table.getName().equalsIgnoreCase("dual")) {
        plainSelect.setFromItem(null);
      }
    }
    super.visit(plainSelect, params);
    return buffer;
  }

  @Override
  public <S> StringBuilder visit(Table table, S params) {
    String name = table.getName().toLowerCase();
    String aliasName = table.getAlias() != null ? table.getAlias().getName() : null;

    for (String[] keyword : JSQLExpressionTranspiler.KEYWORDS) {
      if (keyword[0].equals(name)) {
        table.setName("\"" + table.getName() + "\"");
        name = null;
        if (aliasName == null) {
          break;
        }
      }

      if (keyword[0].equals(aliasName)) {
        table.getAlias().setName("\"" + table.getAlias().getName() + "\"");
        aliasName = null;
        if (name == null) {
          break;
        }
      }
    }

    super.visit(table, params);
    return buffer;
  }

  @Override
  public <S> StringBuilder visit(SelectItem<?> selectItem, S context) {
    if (selectItem.getAlias() != null) {
      String aliasName = selectItem.getAlias().getName().toLowerCase();
      for (String[] keyword : JSQLExpressionTranspiler.KEYWORDS) {
        if (keyword[0].equals(aliasName)) {
          selectItem.getAlias().setName("\"" + selectItem.getAlias().getName() + "\"");
          break;
        }
      }
    }

    selectItem.getExpression().accept(this.getExpressionVisitor(), context);
    if (selectItem.getAlias() != null) {
      buffer.append(selectItem.getAlias().toString());
    }

    return buffer;
  }
}
