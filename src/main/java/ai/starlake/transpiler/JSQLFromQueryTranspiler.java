package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.piped.AggregatePipeOperator;
import net.sf.jsqlparser.statement.piped.AsPipeOperator;
import net.sf.jsqlparser.statement.piped.CallPipeOperator;
import net.sf.jsqlparser.statement.piped.DropPipeOperator;
import net.sf.jsqlparser.statement.piped.ExtendPipeOperator;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.FromQueryVisitor;
import net.sf.jsqlparser.statement.piped.JoinPipeOperator;
import net.sf.jsqlparser.statement.piped.LimitPipeOperator;
import net.sf.jsqlparser.statement.piped.OrderByPipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperatorVisitor;
import net.sf.jsqlparser.statement.piped.PivotPipeOperator;
import net.sf.jsqlparser.statement.piped.RenamePipeOperator;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.statement.piped.SetOperationPipeOperator;
import net.sf.jsqlparser.statement.piped.SetPipeOperator;
import net.sf.jsqlparser.statement.piped.TableSamplePipeOperator;
import net.sf.jsqlparser.statement.piped.UnPivotPipeOperator;
import net.sf.jsqlparser.statement.piped.WherePipeOperator;
import net.sf.jsqlparser.statement.piped.WindowPipeOperator;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.List;

public class JSQLFromQueryTranspiler implements FromQueryVisitor<PlainSelect, PlainSelect>,
    PipeOperatorVisitor<PlainSelect, PlainSelect> {
  public JSQLFromQueryTranspiler() {}

  @Override
  public PlainSelect visit(FromQuery fromQuery, PlainSelect plainSelect) {
    if (plainSelect == null) {
      plainSelect = new PlainSelect().withFromItem(fromQuery.getFromItem());
    } else {
      plainSelect.setFromItem(fromQuery.getFromItem());
    }

    for (PipeOperator operator : fromQuery.getPipeOperators()) {
      plainSelect = operator.accept(this, plainSelect);
    }

    if (plainSelect.getSelectItems() == null || plainSelect.getSelectItems().isEmpty()) {
      plainSelect.addSelectItem(new AllColumns());
    }

    return plainSelect;
  }

  @Override
  public PlainSelect visit(AggregatePipeOperator aggregatePipeOperator, PlainSelect plainSelect) {
    if (plainSelect.getSelectItems() == null || plainSelect.getSelectItems().isEmpty()) {
      plainSelect.setSelectItems(aggregatePipeOperator.getSelectItems());
    } else {
      plainSelect = plainSelect.withFromItem(plainSelect)
          .withSelectItems(aggregatePipeOperator.getSelectItems());
    }

    if (aggregatePipeOperator.getGroupItems() != null
        && !aggregatePipeOperator.getGroupItems().isEmpty()) {
      if (plainSelect.getGroupBy() == null) {
        plainSelect.setGroupByElement(new GroupByElement());
      }
      ExpressionList expressions = new ExpressionList<>();
      for (SelectItem<?> selectItem : aggregatePipeOperator.getGroupItems()) {
        plainSelect.getSelectItems().add(selectItem);
        expressions.add(selectItem.getExpression());
      }
      plainSelect.getGroupBy().setGroupByExpressions(expressions);
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(AsPipeOperator asPipeOperator, PlainSelect plainSelect) {
    if ((plainSelect.getSelectItems() == null || plainSelect.getSelectItems().isEmpty())
        && plainSelect.getFromItem().getAlias() == null) {
      plainSelect.getFromItem().setAlias(asPipeOperator.getAlias());
    } else {
      return new PlainSelect()
          .withFromItem(
              new ParenthesedSelect().withSelect(plainSelect).withAlias(asPipeOperator.getAlias()))
          .addSelectItem(new AllColumns());
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(CallPipeOperator callPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(DropPipeOperator dropPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(ExtendPipeOperator extendPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(JoinPipeOperator joinPipeOperator, PlainSelect plainSelect) {
    if (plainSelect.getJoins() == null) {
      plainSelect.setJoins(List.of(joinPipeOperator.getJoin()));
    } else {
      plainSelect.getJoins().add(joinPipeOperator.getJoin());
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(LimitPipeOperator limitPipeOperator, PlainSelect plainSelect) {
    if (plainSelect.getLimit() == null) {
      plainSelect.setLimit(new Limit().withRowCount(limitPipeOperator.getLimitExpression())
          .withOffset(limitPipeOperator.getOffsetExpression()));
    } else {
      plainSelect = new PlainSelect().withFromItem(new ParenthesedSelect().withSelect(plainSelect))
          .addSelectItem(new AllColumns());
      plainSelect.setLimit(new Limit().withRowCount(limitPipeOperator.getLimitExpression())
          .withOffset(limitPipeOperator.getOffsetExpression()));
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(OrderByPipeOperator orderByPipeOperator, PlainSelect plainSelect) {
    if (plainSelect.getOrderByElements() == null || plainSelect.getOrderByElements().isEmpty()) {
      plainSelect.setOrderByElements(orderByPipeOperator.getOrderByElements());
    } else {
      plainSelect.getOrderByElements().addAll(orderByPipeOperator.getOrderByElements());
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(PivotPipeOperator pivotPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(RenamePipeOperator renamePipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(SelectPipeOperator selectPipeOperator, PlainSelect plainSelect) {
    if (selectPipeOperator.getOperatorName().equalsIgnoreCase("SELECT")) {
      if (plainSelect.getSelectItems() == null || plainSelect.getSelectItems().isEmpty()
          || plainSelect.getSelectItems().size() == 1
              && plainSelect.getSelectItem(0).getExpression() instanceof AllColumns) {
        plainSelect.setSelectItems(selectPipeOperator.getSelectItems());
      } else {
        return new PlainSelect().withFromItem(new ParenthesedSelect().withSelect(plainSelect))
            .withSelectItems(selectPipeOperator.getSelectItems());
      }
    } else if (selectPipeOperator.getOperatorName().equalsIgnoreCase("EXTEND")) {
      if (plainSelect.getSelectItems() == null || plainSelect.getSelectItems().isEmpty()) {
        plainSelect.addSelectItems(new AllColumns())
            .addSelectItems(selectPipeOperator.getSelectItems());
      } else {
        return plainSelect.addSelectItems(selectPipeOperator.getSelectItems());
      }
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(SetPipeOperator setPipeOperator, PlainSelect plainSelect) {
    List<SelectItem<?>> selectItems = plainSelect.getSelectItems();

    if (selectItems == null || selectItems.isEmpty()) {
      AllColumns allColumns = new AllColumns();
      setAllColumnsReplace(allColumns, setPipeOperator);
      plainSelect.addSelectItem(allColumns);
    } else {
      boolean allColumnsFound = false;
      for (SelectItem<?> selectItem : selectItems) {
        if (selectItem.getExpression() instanceof AllColumns) {
          AllColumns allColumns = (AllColumns) selectItem.getExpression();
          setAllColumnsReplace(allColumns, setPipeOperator);
          allColumnsFound = true;
          break;
        }
      }
      if (!allColumnsFound) {
        AllColumns allColumns = new AllColumns();
        setAllColumnsReplace(allColumns, setPipeOperator);

        plainSelect =
            new PlainSelect().withFromItem(new ParenthesedSelect().withSelect(plainSelect))
                .addSelectItem(allColumns);
      }
    }

    return plainSelect;
  }

  private static void setAllColumnsReplace(AllColumns allColumns, SetPipeOperator setPipeOperator) {
    if (allColumns.getReplaceExpressions() == null
        || allColumns.getReplaceExpressions().isEmpty()) {
      allColumns.setReplaceExpressions(new ArrayList<>());
    }

    for (UpdateSet updateSet : setPipeOperator.getUpdateSets()) {
      for (int i = 0; i < updateSet.getColumns().size(); i++) {
        Column column = updateSet.getColumn(i);
        Expression value = updateSet.getValue(i);
        allColumns.getReplaceExpressions()
            .add(new SelectItem<>(value, new Alias(column.getColumnName(), true)));
      }
    }
  }

  @Override
  public PlainSelect visit(TableSamplePipeOperator tableSamplePipeOperator,
      PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(SetOperationPipeOperator setOperationPipeOperator,
      PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(UnPivotPipeOperator unPivotPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }

  @Override
  public PlainSelect visit(WherePipeOperator wherePipeOperator, PlainSelect plainSelect) {
    if (plainSelect.getWhere() == null) {
      plainSelect.setWhere(wherePipeOperator.getExpression());
    } else {
      plainSelect
          .setWhere(new AndExpression(plainSelect.getWhere(), wherePipeOperator.getExpression()));
    }
    return plainSelect;
  }

  @Override
  public PlainSelect visit(WindowPipeOperator windowPipeOperator, PlainSelect plainSelect) {
    return plainSelect;
  }
}
