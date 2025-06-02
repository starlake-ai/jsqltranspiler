/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI <hayssam.saleh@starlake.ai>
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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


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
    return builder;
  }

  @Override
  public void visit(Top top) {
    // get the parent SELECT
    Node node = (Node) top.getASTNode().jjtGetParent();
    while (node.jjtGetValue() == null) {
      node = (Node) node.jjtGetParent();
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
      parenthesedSelect.accept((FromItemVisitor<StringBuilder>) this, params);
    } else {
      super.visit(tableFunction, params);
    }
    return builder;
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
    return builder;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public <S> StringBuilder visit(ParenthesedSelect select, S params) {
    List<WithItem<?>> withItemsList = select.getWithItemsList();
    if (withItemsList != null && !withItemsList.isEmpty()) {
      this.builder.append("WITH ");

      for (WithItem<?> withItem : withItemsList) {
        withItem.accept(this, params);
        this.builder.append(" ");
      }
    }

    if (select.getSampleClause() != null) {
      SampleClause sampleClause = select.getSampleClause();
      sampleClause.setKeyword(SampleClause.SampleKeyword.USING_SAMPLE);
    }

    this.builder.append("(");
    select.getSelect().accept((SelectVisitor<StringBuilder>) this, params);
    this.builder.append(")");

    Alias alias = select.getAlias();
    if (alias != null) {
      this.builder.append(alias);
    }

    Pivot pivot = select.getPivot();
    if (pivot != null) {
      pivot.accept(this, params);
    }

    UnPivot unpivot = select.getUnPivot();
    if (unpivot != null) {
      unpivot.accept(this, params);
    }

    if (select.getOrderByElements() != null) {
      new OrderByDeParser(this.getExpressionVisitor(), this.builder)
          .deParse(select.isOracleSiblings(), select.getOrderByElements());
    }

    if (select.getLimit() != null) {
      new LimitDeparser(this.getExpressionVisitor(), this.builder).deParse(select.getLimit());
    }

    if (select.getOffset() != null) {
      this.visit(select.getOffset());
    }

    if (select.getFetch() != null) {
      this.visit(select.getFetch());
    }

    if (select.getIsolation() != null) {
      this.builder.append(select.getIsolation().toString());
    }

    return this.builder;
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

    if (table.getSampleClause() != null) {
      SampleClause sampleClause = table.getSampleClause();
      sampleClause.setKeyword(SampleClause.SampleKeyword.USING_SAMPLE);
    }

    super.visit(table, params);
    return builder;
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
      builder.append(selectItem.getAlias().toString());
    }

    return builder;
  }

  @Override
  public <S> StringBuilder visit(FromQuery fromQuery, S context) {
    PlainSelect select = fromQuery.accept(new JSQLFromQueryTranspiler(), null);
    select.accept(this);
    return builder;
  }

  public PlainSelect visit(SelectPipeOperator selectPipeOperator, PlainSelect select) {
    if (selectPipeOperator.getOperatorName().equalsIgnoreCase("SELECT")) {
      List<SelectItem<?>> selectItems = select.getSelectItems();

      if (selectItems.isEmpty()) {
        selectItems.addAll(selectPipeOperator.getSelectItems());
      } else {
        select = new PlainSelect(new ParenthesedSelect().withSelect(select))
            .withSelectItems(selectPipeOperator.getSelectItems());
      }
    }

    return select;
  };

}
