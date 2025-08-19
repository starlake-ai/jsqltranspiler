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

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SessionStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.export.Export;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeDelete;
import net.sf.jsqlparser.statement.merge.MergeInsert;
import net.sf.jsqlparser.statement.merge.MergeOperation;
import net.sf.jsqlparser.statement.merge.MergeUpdate;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JSQLResolver extends JSQLColumResolver {
  List<JdbcColumn> withColumns = new ArrayList<>();
  List<JdbcColumn> selectColumns = new ArrayList<>();
  List<JdbcColumn> deleteColumns = new ArrayList<>();
  List<JdbcColumn> updateColumns = new ArrayList<>();
  List<JdbcColumn> insertColumns = new ArrayList<>();
  List<JdbcColumn> whereColumns = new ArrayList<>();
  List<JdbcColumn> groupByColumns = new ArrayList<>();
  List<JdbcColumn> havingColumns = new ArrayList<>();
  List<JdbcColumn> joinedOnColumns = new ArrayList<>();
  List<JdbcColumn> orderByColumns = new ArrayList<>();
  List<Expression> functions = new ArrayList<>();

  public JSQLResolver(JdbcMetaData metaData) {
    super(metaData);
  }

  public JSQLResolver(Connection connection) throws SQLException {
    super(connection);
  }

  public JSQLResolver(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    super(currentCatalogName, currentSchemaName, metaDataDefinition);
  }

  public JSQLResolver(String[][] metaDataDefinition) {
    super(metaDataDefinition);
  }

  /**
   * @param commentFlag the comment columns flag
   */
  public JSQLResolver setCommentFlag(boolean commentFlag) {
    super.setCommentFlag(commentFlag);
    return this;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(WithItem<?> withItem, S context) {
    JdbcResultSetMetaData rsMetaData = null;
    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) context;

      ParenthesedStatement st = withItem.getParenthesedStatement();
      if (st instanceof ParenthesedSelect) {
        rsMetaData = withItem.getSelect().accept((SelectVisitor<JdbcResultSetMetaData>) this,
            metaData.copyOf());
        metaData.put(rsMetaData, withItem.getUnquotedAliasName(),
            "Error in WITH clause " + withItem);
      } else if (st instanceof ParenthesedDelete) {
        withItem.getDelete().getDelete().accept(new StatementResolver());

        rsMetaData = withItem.getDelete().getDelete().getTable().accept(this, metaData.copyOf());
        metaData.put(rsMetaData, withItem.getUnquotedAliasName(),
            "Error in WITH clause " + withItem);

      } else if (st instanceof ParenthesedInsert) {
        withItem.getDelete().getDelete().accept(new StatementResolver());

        rsMetaData = withItem.getInsert().getInsert().getTable().accept(this, metaData.copyOf());
        metaData.put(rsMetaData, withItem.getUnquotedAliasName(),
            "Error in WITH clause " + withItem);
      } else if (st instanceof ParenthesedUpdate) {
        rsMetaData = withItem.getUpdate().getUpdate().getTable().accept(this, metaData.copyOf());
        metaData.put(rsMetaData, withItem.getUnquotedAliasName(),
            "Error in WITH clause " + withItem);
      }
    }
    return rsMetaData;
  }


  @Override
  public JdbcResultSetMetaData visit(PlainSelect select, JdbcMetaData metaData) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = select.getFromItem();
    List<Join> joins = select.getJoins();

    // `WITH` shadows base tables
    if (select.getWithItemsList() != null) {
      for (WithItem<?> withItem : select.getWithItemsList()) {
        Alias alias = withItem.getAlias();
        JdbcResultSetMetaData rsMetaData = withItem.accept(this, metaData.copyOf());
        for (JdbcColumn c : rsMetaData.getColumns()) {
          c.tableName = alias.getName();
          c.tableSchema = metaData.getCurrentSchemaName();
          c.tableCatalog = metaData.getCurrentCatalogName();
          if (c.getExpression() instanceof Column) {
            Column column = (Column) c.getExpression();
            if (column.getTable() != null) {
              column.getTable().setResolvedTable(null);
            }
            column.setResolvedTable(null);
          }
        }
        final JdbcTable t =
            metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
        t.setTableType("VIRTUAL TABLE");
      }
    }

    // `WITH` shadows base tables so base tables need to get resolved first
    if (fromItem instanceof Table) {
      Alias alias = fromItem.getAlias();
      Table t = (Table) fromItem;

      Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
          metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

      JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
      if (jdbcTable != null) {
        if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
          t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
        }
      } else {
        throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
      }

      if (alias != null) {
        metaData.getFromTables().put(alias.getName(), t);
      } else {
        metaData.getFromTables().put(t.getName(), t);
      }

    } else if (fromItem != null) {
      Alias alias = fromItem.getAlias();
      JdbcResultSetMetaData rsMetaData = fromItem.accept(this, JdbcMetaData.copyOf(metaData));
      for (JdbcColumn c : rsMetaData.getColumns()) {
        c.tableName = alias.getName();
        c.tableSchema = metaData.getCurrentSchemaName();
        c.tableCatalog = metaData.getCurrentCatalogName();
        if (c.getExpression() instanceof Column) {
          Column column = (Column) c.getExpression();
          if (column.getTable() != null) {
            column.getTable().setResolvedTable(null);
          }
          column.setResolvedTable(null);
        }
      }
      JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
          "Error in FromItem " + fromItem);
      t.setTableType("VIRTUAL TABLE");
      metaData.getFromTables().put(t.tableName, new Table(t.tableName));
    }

    if (joins != null) {
      for (Join join : joins) {
        if (join.isLeft() || join.isInner()) {
          metaData.addLeftUsingJoinColumns(join.getUsingColumns());
        } else if (join.isRight()) {
          metaData.addRightUsingJoinColumns(join.getUsingColumns());
        }

        if (join.getFromItem() instanceof Table) {
          Alias alias = join.getFromItem().getAlias();
          Table t = (Table) join.getFromItem();

          Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
              metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

          JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
          if (jdbcTable != null) {
            if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
              t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
            }
          } else {
            throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
          }

          if (alias != null) {
            metaData.getFromTables().put(alias.getUnquotedName(), t);
            if (join.isNatural()) {
              metaData.getNaturalJoinedTables().put(alias.getUnquotedName(), t);
            }

          } else {
            metaData.getFromTables().put(t.getName(), t);
            if (join.isNatural()) {
              metaData.addNaturalJoinedTable(t);
            }
          }
        } else {
          Alias alias = join.getFromItem().getAlias();
          JdbcResultSetMetaData rsMetaData =
              join.getFromItem().accept(this, JdbcMetaData.copyOf(metaData));
          for (JdbcColumn c : rsMetaData.getColumns()) {
            c.tableName = alias.getName();
            c.tableSchema = metaData.getCurrentSchemaName();
            c.tableCatalog = metaData.getCurrentCatalogName();
            if (c.getExpression() instanceof Column) {
              Column column = (Column) c.getExpression();
              if (column.getTable() != null) {
                column.getTable().setResolvedTable(null);
              }
              column.setResolvedTable(null);
            }
          }

          JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
              "Error in FromItem " + fromItem);
          t.setTableType("VIRTUAL TABLE");
          metaData.getFromTables().put(t.tableName, new Table(t.tableName));

          if (join.isNatural()) {
            metaData.getNaturalJoinedTables().put(t.tableName, new Table(t.tableName));
          }
        }
      }
    }

    // column positions in MetaData start at 1
    ArrayList<SelectItem<?>> newSelectItems = new ArrayList<>();
    for (SelectItem<?> selectItem : select.getSelectItems()) {
      Alias alias = selectItem.getAlias();
      List<JdbcColumn> jdbcColumns =
          selectItem.getExpression().accept(expressionColumnResolver, metaData);

      for (JdbcColumn col : jdbcColumns) {
        resultSetMetaData.add(col, alias != null ? alias.getUnquotedName() : null);

        if (selectItem.getExpression() instanceof AllColumns
            || selectItem.getExpression() instanceof AllTableColumns) {

          Table t = metaData.getFromTables().get(col.tableName);
          Table clonedTable = new Table(t.getFullyQualifiedName()).withAlias(t.getAlias());
          clonedTable.setResolvedTable(t.getResolvedTable() != null ? t.getResolvedTable() : null);

          Column column = new Column(clonedTable, col.columnName);
          // if (isCommentFlag()) {
          // column.setCommentText("Resolved Column");
          // }
          newSelectItems.add(new SelectItem<>(column, alias));

          selectColumns.add(new JdbcColumn(t.getUnquotedName(), col.columnName));
        } else {
          newSelectItems.add(selectItem);

          selectColumns.add(col);
        }
      }
    }
    select.setSelectItems(newSelectItems);

    // if (select.getWithItemsList() != null) {
    // for (WithItem<?> withItem : select.getWithItemsList()) {
    // withColumns.addAll(withItem.accept(expressionColumnResolver, metaData));
    // }
    // }
    //
    // // column positions in MetaData start at 1
    // for (SelectItem<?> selectItem : select.getSelectItems()) {
    // selectColumns.addAll(selectItem.getExpression().accept(expressionColumnResolver, metaData));
    // }

    // Join expressions
    if (joins != null) {
      for (Join join : joins) {
        for (Column column : join.getUsingColumns()) {
          joinedOnColumns.addAll(column.accept(expressionColumnResolver, metaData));
        }
        for (Expression expression : join.getOnExpressions()) {
          joinedOnColumns.addAll(expression.accept(expressionColumnResolver, metaData));
        }
      }
    }

    // where expressions
    Expression whereExpression = select.getWhere();
    if (whereExpression != null) {
      whereColumns.addAll(whereExpression.accept(expressionColumnResolver, metaData));
    }

    // aggregate expressions
    GroupByElement groupBy = select.getGroupBy();
    if (groupBy != null) {
      ExpressionList<Expression> expressionList = groupBy.getGroupByExpressionList();
      for (Expression expression : expressionList) {
        groupByColumns.addAll(expression.accept(expressionColumnResolver, metaData));
      }

      List<ExpressionList<Expression>> groupingSets = groupBy.getGroupingSets();
      for (ExpressionList<Expression> expressions : groupingSets) {
        for (Expression e : expressions) {
          groupByColumns.addAll(e.accept(expressionColumnResolver, metaData));
        }
      }
    }

    if (select.getHaving() != null) {
      havingColumns.addAll(select.getHaving().accept(expressionColumnResolver, metaData));
    }

    List<OrderByElement> orderByElements = select.getOrderByElements();
    if (orderByElements != null) {
      for (OrderByElement orderByElement : orderByElements) {
        orderByElement.getExpression().accept(expressionColumnResolver, metaData);
      }
    }

    return resultSetMetaData;
  }

  public List<JdbcColumn> getWhereColumns() {
    return whereColumns;
  }

  public Set<JdbcColumn> getFlattendedWhereColumns() {
    return flatten(whereColumns);
  }


  public JSQLResolver setWhereColumns(List<JdbcColumn> whereColumns) {
    this.whereColumns = whereColumns;
    return this;
  }

  public List<JdbcColumn> getWithColumns() {
    return withColumns;
  }

  public List<JdbcColumn> getSelectColumns() {
    return selectColumns;
  }

  public List<JdbcColumn> getDeleteColumns() {
    return deleteColumns;
  }

  public List<JdbcColumn> getUpdateColumns() {
    return updateColumns;
  }

  public List<JdbcColumn> getInsertColumns() {
    return insertColumns;
  }

  public List<JdbcColumn> getGroupByColumns() {
    return groupByColumns;
  }

  public List<JdbcColumn> getHavingColumns() {
    return havingColumns;
  }

  public List<JdbcColumn> getJoinedOnColumns() {
    return joinedOnColumns;
  }

  public Set<JdbcColumn> getFlattenedJoinedOnColumns() {
    return flatten(joinedOnColumns);
  }

  public List<JdbcColumn> getOrderByColumns() {
    return orderByColumns;
  }

  public List<Expression> getFunctions() {
    return functions;
  }

  public Set<String> getFlatFunctionNames() {
    Set<String> functionNames = new LinkedHashSet<>();
    for (Expression e : functions) {
      if (e instanceof Function) {
        Function function = (Function) e;
        functionNames.add(function.getName());
      } else if (e instanceof JsonFunction) {
        JsonFunction function = (JsonFunction) e;
        functionNames.add(function.getType().name());
      } else if (e instanceof JsonAggregateFunction) {
        JsonAggregateFunction function = (JsonAggregateFunction) e;
        functionNames.add(function.getType().name());
      } else if (e instanceof TableFunction) {
        TableFunction function = (TableFunction) e;
        functionNames.add(function.getFunction().getName());
      }
    }
    return functionNames;
  }

  public Set<JdbcColumn> flatten(Collection<JdbcColumn> columns) {
    LinkedHashSet<JdbcColumn> flattenedSet = new LinkedHashSet<>();
    for (JdbcColumn column : columns) {
      if (column.getExpression() instanceof Column) {
        String catalogName =
            column.scopeCatalog != null && !column.scopeCatalog.isEmpty() ? column.scopeCatalog
                : column.tableCatalog;
        String schemaName =
            column.scopeSchema != null && !column.scopeSchema.isEmpty() ? column.scopeSchema
                : column.tableSchema;
        String tableName =
            column.scopeTable != null && !column.scopeTable.isEmpty() ? column.scopeTable
                : column.tableName;
        String columnName =
            column.scopeColumn != null && !column.scopeColumn.isEmpty() ? column.scopeColumn
                : column.columnName;
        flattenedSet.add(
            new JdbcColumn(catalogName, schemaName, tableName, columnName, column.getExpression()));
      } else {
        flattenedSet.addAll(flatten(column.getChildren()));
      }
    }
    return flattenedSet;
  }


  /**
   * Resolves all the columns used at any clause of a SELECT, INSERT, UPDATE or DELETE statement for
   * an empty CURRENT_CATALOG and an empty CURRENT_SCHEMA.
   *
   * @param st the (parsed) `SELECT`, `INSERT`, `UPDATE` or `DELETE` statement
   * @return the set of all affected columns (with table information)
   */
  public Set<JdbcColumn> resolve(Statement st) {
    expressionColumnResolver.clearFunctions();

    if (st instanceof Select) {
      Select select = (Select) st;
      select.accept((SelectVisitor<JdbcResultSetMetaData>) this, JdbcMetaData.copyOf(metaData));
    } else {
      st.accept(new StatementResolver(), JdbcMetaData.copyOf(metaData));
    }
    functions = expressionColumnResolver.getFunctions();

    Set<JdbcColumn> allColumns = new HashSet<>();
    allColumns.addAll(flatten(withColumns));
    allColumns.addAll(flatten(selectColumns));
    allColumns.addAll(flatten(deleteColumns));
    allColumns.addAll(flatten(joinedOnColumns));
    allColumns.addAll(flatten(whereColumns));
    allColumns.addAll(flatten(groupByColumns));
    allColumns.addAll(flatten(havingColumns));
    allColumns.addAll(flatten(orderByColumns));
    return allColumns;
  }

  /**
   * Resolves all the columns used at any clause of a SELECT statement for an empty CURRENT_CATALOG
   * and an empty CURRENT_SCHEMA.
   *
   * @param sqlStr the `SELECT` statement text
   * @return the set of all affected columns (with table information)
   * @throws net.sf.jsqlparser.JSQLParserException when the `SELECT` statement text can not be
   *         parsed
   */
  public Set<JdbcColumn> resolve(String sqlStr) throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(sqlStr);
    return resolve(st);
  }

  /**
   * Resolves all the actual physical tables used at any clause of a statement for an empty
   * CURRENT_CATALOG and an empty CURRENT_SCHEMA.
   *
   * @param sqlStr the statement text
   * @return the statement with `resolvedTable` filled for all (physical) tables and columns
   *         referencing physical tables
   * @throws net.sf.jsqlparser.JSQLParserException when the `SELECT` statement text can not be
   *         parsed
   */
  public Statement resolveTables(String sqlStr) throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(sqlStr);
    expressionColumnResolver.clearFunctions();

    if (st instanceof Select) {
      Select select = (Select) st;
      select.accept((SelectVisitor<JdbcResultSetMetaData>) this, JdbcMetaData.copyOf(metaData));
    } else if (st instanceof Delete) {
      Delete delete = (Delete) st;
      delete.accept(new StatementResolver(), JdbcMetaData.copyOf(metaData));
    } else if (st instanceof Insert) {
      Insert insert = (Insert) st;
      insert.accept(new StatementResolver(), JdbcMetaData.copyOf(metaData));
    } else if (st instanceof Update) {
      Update update = (Update) st;
      update.accept(new StatementResolver(), JdbcMetaData.copyOf(metaData));
    }

    return st;
  }

  class StatementResolver implements StatementVisitor<JdbcResultSetMetaData> {

    @Override
    public <S> JdbcResultSetMetaData visit(Analyze analyze, S s) {
      return null;
    }

    @Override
    public void visit(Analyze analyze) {
      StatementVisitor.super.visit(analyze);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(SavepointStatement savepointStatement, S s) {
      return null;
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
      StatementVisitor.super.visit(savepointStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(RollbackStatement rollbackStatement, S s) {
      return null;
    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
      StatementVisitor.super.visit(rollbackStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Comment comment, S s) {
      return null;
    }

    @Override
    public void visit(Comment comment) {
      StatementVisitor.super.visit(comment);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Commit commit, S s) {
      return null;
    }

    @Override
    public void visit(Commit commit) {
      StatementVisitor.super.visit(commit);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Delete delete, S s) {
      JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

      Table fromItem = delete.getTable();
      List<Join> joins = delete.getJoins();

      if (delete.getWithItemsList() != null) {
        for (WithItem<?> withItem : delete.getWithItemsList()) {
          Alias alias = withItem.getAlias();
          JdbcResultSetMetaData rsMetaData = withItem.accept(JSQLResolver.this, metaData);
          metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
        }
      }

      if (fromItem != null) {
        Alias alias = fromItem.getAlias();
        Table t = fromItem;
        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), fromItem);
        } else {
          metaData.getFromTables().put(t.getName(), fromItem);
        }
      }

      if (joins != null) {
        for (Join join : joins) {
          if (join.isLeft() || join.isInner()) {
            metaData.addLeftUsingJoinColumns(join.getUsingColumns());
          } else if (join.isRight()) {
            metaData.addRightUsingJoinColumns(join.getUsingColumns());
          }

          if (join.getFromItem() instanceof Table) {
            Alias alias = join.getFromItem().getAlias();
            Table t = (Table) join.getFromItem();

            if (alias != null) {
              metaData.getFromTables().put(alias.getUnquotedName(), t);
              if (join.isNatural()) {
                metaData.getNaturalJoinedTables().put(alias.getUnquotedName(), t);
              }

            } else {
              metaData.getFromTables().put(t.getName(), t);
              if (join.isNatural()) {
                metaData.addNaturalJoinedTable(t);
              }
            }
          } else {
            Alias alias = join.getFromItem().getAlias();
            JdbcResultSetMetaData rsMetaData =
                join.getFromItem().accept(JSQLResolver.this, JdbcMetaData.copyOf(metaData));

            JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
                "Error in FromItem " + fromItem);
            metaData.getFromTables().put(t.tableName, new Table(t.tableName));

            if (join.isNatural()) {
              metaData.getNaturalJoinedTables().put(t.tableName, new Table(t.tableName));
            }
          }
        }
      }

      for (Table t : metaData.getFromTables().values()) {
        if (t.getSchemaName() == null || t.getSchemaName().isEmpty()) {
          t.setSchemaName(metaData.getCurrentSchemaName());
        }

        if (t.getDatabase() == null) {
          t.setDatabaseName(metaData.getCurrentCatalogName());
        } else if (t.getDatabase().getDatabaseName() == null
            || t.getDatabase().getDatabaseName().isEmpty()) {
          t.getDatabase().setDatabaseName(metaData.getCurrentCatalogName());
        }
      }

      for (Table t : metaData.getNaturalJoinedTables().values()) {
        if (t.getSchemaName() == null || t.getSchemaName().isEmpty()) {
          t.setSchemaName(metaData.getCurrentSchemaName());
        }

        if (t.getDatabase() == null) {
          t.setDatabaseName(metaData.getCurrentCatalogName());
        } else if (t.getDatabase().getDatabaseName() == null
            || t.getDatabase().getDatabaseName().isEmpty()) {
          t.getDatabase().setDatabaseName(metaData.getCurrentCatalogName());
        }
      }


      if (delete.getWithItemsList() != null) {
        for (WithItem<?> withItem : delete.getWithItemsList()) {
          withColumns.addAll(withItem.accept(expressionColumnResolver, delete));
        }
      }

      // column positions in MetaData start at 1
      deleteColumns.addAll(new AllColumns().accept(expressionColumnResolver, metaData));

      // Join expressions
      if (joins != null) {
        for (Join join : joins) {
          for (Column column : join.getUsingColumns()) {
            joinedOnColumns.addAll(column.accept(expressionColumnResolver, metaData));
          }
          for (Expression expression : join.getOnExpressions()) {
            joinedOnColumns.addAll(expression.accept(expressionColumnResolver, metaData));
          }
        }
      }

      // where expressions
      Expression whereExpression = delete.getWhere();
      if (whereExpression != null) {
        whereColumns.addAll(whereExpression.accept(expressionColumnResolver, metaData));
      }

      return resultSetMetaData;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Update update, S s) {
      JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();


      {
        Table t = update.getTable();
        Alias alias = t.getAlias();

        Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
            metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

        JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
        if (jdbcTable != null) {
          if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
            t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
          }
        } else {
          throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
        }

        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), t);
        } else {
          metaData.getFromTables().put(t.getName(), t);
        }
      }

      FromItem fromItem = update.getFromItem();
      List<Join> joins = update.getJoins();

      if (update.getWithItemsList() != null) {
        for (WithItem<?> withItem : update.getWithItemsList()) {
          Alias alias = withItem.getAlias();
          JdbcResultSetMetaData rsMetaData = withItem.accept(JSQLResolver.this, metaData);
          metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
        }
      }

      if (fromItem instanceof Table) {
        Alias alias = fromItem.getAlias();
        Table t = (Table) fromItem;

        Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
            metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

        JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
        if (jdbcTable != null) {
          if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
            t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
          }
        } else {
          throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
        }

        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), t);
        } else {
          metaData.getFromTables().put(t.getName(), t);
        }
      } else if (fromItem != null) {
        Alias alias = fromItem.getAlias();
        JdbcResultSetMetaData rsMetaData =
            fromItem.accept(JSQLResolver.this, JdbcMetaData.copyOf(metaData));
        for (JdbcColumn c : rsMetaData.getColumns()) {
          c.tableName = alias.getName();
          c.tableSchema = metaData.getCurrentSchemaName();
          c.tableCatalog = metaData.getCurrentCatalogName();
          if (c.getExpression() instanceof Column) {
            Column column = (Column) c.getExpression();
            if (column.getTable() != null) {
              column.getTable().setResolvedTable(null);
            }
            column.setResolvedTable(null);
          }
        }
        JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
            "Error in FromItem " + fromItem);
        t.setTableType("VIRTUAL TABLE");
        metaData.getFromTables().put(t.tableName, new Table(t.tableName));
      }

      if (joins != null) {
        for (Join join : joins) {
          if (join.isLeft() || join.isInner()) {
            metaData.addLeftUsingJoinColumns(join.getUsingColumns());
          } else if (join.isRight()) {
            metaData.addRightUsingJoinColumns(join.getUsingColumns());
          }

          if (join.getFromItem() instanceof Table) {
            Alias alias = join.getFromItem().getAlias();
            Table t = (Table) join.getFromItem();

            Table fullyQualifiedTable =
                new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
                    metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

            JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
            if (jdbcTable != null) {
              if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
                t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
              }
            } else {
              throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
            }

            if (alias != null) {
              metaData.getFromTables().put(alias.getUnquotedName(), t);
              if (join.isNatural()) {
                metaData.getNaturalJoinedTables().put(alias.getUnquotedName(), t);
              }

            } else {
              metaData.getFromTables().put(t.getName(), t);
              if (join.isNatural()) {
                metaData.addNaturalJoinedTable(t);
              }
            }
          } else {
            Alias alias = join.getFromItem().getAlias();
            JdbcResultSetMetaData rsMetaData =
                join.getFromItem().accept(JSQLResolver.this, JdbcMetaData.copyOf(metaData));
            for (JdbcColumn c : rsMetaData.getColumns()) {
              c.tableName = alias.getName();
              c.tableSchema = metaData.getCurrentSchemaName();
              c.tableCatalog = metaData.getCurrentCatalogName();
              if (c.getExpression() instanceof Column) {
                Column column = (Column) c.getExpression();
                if (column.getTable() != null) {
                  column.getTable().setResolvedTable(null);
                }
                column.setResolvedTable(null);
              }
            }

            JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
                "Error in FromItem " + fromItem);
            t.setTableType("VIRTUAL TABLE");
            metaData.getFromTables().put(t.tableName, new Table(t.tableName));

            if (join.isNatural()) {
              metaData.getNaturalJoinedTables().put(t.tableName, new Table(t.tableName));
            }
          }
        }
      }

      for (Table t : metaData.getFromTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }

      for (Table t : metaData.getNaturalJoinedTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }


      if (update.getWithItemsList() != null) {
        for (WithItem<?> withItem : update.getWithItemsList()) {
          withColumns.addAll(withItem.accept(expressionColumnResolver, update));
        }
      }

      // column positions in MetaData start at 1
      for (UpdateSet updateSet : update.getUpdateSets()) {
        for (Column column : updateSet.getColumns()) {
          updateColumns.addAll(column.accept(expressionColumnResolver, metaData));
        }
        for (Expression expression : updateSet.getValues()) {
          selectColumns.addAll(expression.accept(expressionColumnResolver, metaData));
        }
      }

      // Join expressions
      if (joins != null) {
        for (Join join : joins) {
          for (Column column : join.getUsingColumns()) {
            joinedOnColumns.addAll(column.accept(expressionColumnResolver, metaData));
          }
          for (Expression expression : join.getOnExpressions()) {
            joinedOnColumns.addAll(expression.accept(expressionColumnResolver, metaData));
          }
        }
      }

      // where expressions
      Expression whereExpression = update.getWhere();
      if (whereExpression != null) {
        whereColumns.addAll(whereExpression.accept(expressionColumnResolver, metaData));
      }

      return resultSetMetaData;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Insert insert, S s) {
      JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

      Table table = insert.getTable();

      if (insert.getWithItemsList() != null) {
        for (WithItem<?> withItem : insert.getWithItemsList()) {
          Alias alias = withItem.getAlias();
          JdbcResultSetMetaData rsMetaData = withItem.accept(JSQLResolver.this, metaData);
          metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
        }
      }

      if (table != null) {
        Alias alias = table.getAlias();
        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), table);
        } else {
          metaData.getFromTables().put(table.getName(), table);
        }
      }

      for (Table t : metaData.getFromTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }

      for (Table t : metaData.getNaturalJoinedTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }


      if (insert.getWithItemsList() != null) {
        for (WithItem<?> withItem : insert.getWithItemsList()) {
          withColumns.addAll(withItem.accept(expressionColumnResolver, insert));
        }
      }

      // column positions in MetaData start at 1
      insertColumns.addAll(new AllColumns().accept(expressionColumnResolver, metaData));

      return resultSetMetaData;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Drop drop, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Truncate truncate, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateIndex createIndex, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateSchema createSchema, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateTable createTable, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateView createView, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(AlterView alterView, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(
        RefreshMaterializedViewStatement refreshMaterializedViewStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Alter alter, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Statements statements, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Execute execute, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(SetStatement setStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ResetStatement resetStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ShowColumnsStatement showColumnsStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ShowIndexStatement showIndexStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ShowTablesStatement showTablesStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Merge merge, S s) {
      JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

      // `WITH` shadows base tables
      if (merge.getWithItemsList() != null) {
        for (WithItem<?> withItem : merge.getWithItemsList()) {
          Alias alias = withItem.getAlias();
          JdbcResultSetMetaData rsMetaData = withItem.accept(this, metaData.copyOf());
          for (JdbcColumn c : rsMetaData.getColumns()) {
            c.tableName = alias.getName();
            c.tableSchema = metaData.getCurrentSchemaName();
            c.tableCatalog = metaData.getCurrentCatalogName();
            if (c.getExpression() instanceof Column) {
              Column column = (Column) c.getExpression();
              if (column.getTable() != null) {
                column.getTable().setResolvedTable(null);
              }
              column.setResolvedTable(null);
            }
          }
          final JdbcTable t =
              metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
          t.setTableType("VIRTUAL TABLE");
        }
      }

      {
        Table t = merge.getTable();
        Alias alias = t.getAlias();

        Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
            metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

        JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
        if (jdbcTable != null) {
          if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
            t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
          }
        } else {
          throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
        }

        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), t);
        } else {
          metaData.getFromTables().put(t.getName(), t);
        }
      }

      FromItem fromItem = merge.getFromItem();
      if (fromItem instanceof Table) {
        Alias alias = fromItem.getAlias();
        Table t = (Table) fromItem;

        Table fullyQualifiedTable = new Table(t.getFullyQualifiedName()).setUnsetCatalogAndSchema(
            metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName());

        JdbcTable jdbcTable = metaData.getTable(fullyQualifiedTable);
        if (jdbcTable != null) {
          if (!jdbcTable.tableType.equals("VIRTUAL TABLE")) {
            t.setResolvedTable(fullyQualifiedTable.withAlias(alias));
          }
        } else {
          throw new TableNotFoundException(t.getFullyQualifiedName(), List.of());
        }

        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), t);
        } else {
          metaData.getFromTables().put(t.getName(), t);
        }

      } else if (fromItem != null) {
        Alias alias = fromItem.getAlias();
        JdbcResultSetMetaData rsMetaData =
            fromItem.accept(JSQLResolver.this, JdbcMetaData.copyOf(metaData));
        for (JdbcColumn c : rsMetaData.getColumns()) {
          c.tableName = alias.getName();
          c.tableSchema = metaData.getCurrentSchemaName();
          c.tableCatalog = metaData.getCurrentCatalogName();
          if (c.getExpression() instanceof Column) {
            Column column = (Column) c.getExpression();
            if (column.getTable() != null) {
              column.getTable().setResolvedTable(null);
            }
            column.setResolvedTable(null);
          }
        }
        JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
            "Error in FromItem " + fromItem);
        t.setTableType("VIRTUAL TABLE");
        metaData.getFromTables().put(t.tableName, new Table(t.tableName));
      }


      for (Table t : metaData.getFromTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }

      for (Table t : metaData.getNaturalJoinedTables().values()) {
        t.setUnsetCatalogAndSchema(metaData.getCurrentCatalogName(),
            metaData.getCurrentSchemaName());
      }

      merge.getOnCondition().accept(expressionColumnResolver, metaData);

      if (merge.getWithItemsList() != null) {
        for (WithItem<?> withItem : merge.getWithItemsList()) {
          withColumns.addAll(withItem.accept(expressionColumnResolver, metaData));
        }
      }

      // column positions in MetaData start at 1
      deleteColumns.addAll(new AllColumns().accept(expressionColumnResolver, metaData));

      for (MergeOperation operation : merge.getOperations()) {
        if (operation instanceof MergeInsert) {
          MergeInsert insert = (MergeInsert) operation;

          for (Column column : insert.getColumns()) {
            column.accept(expressionColumnResolver, metaData);
          }

          for (Expression expression : insert.getValues()) {
            expression.accept(expressionColumnResolver, metaData);
          }

          Expression expression = insert.getAndPredicate();
          if (expression != null) {
            expression.accept(expressionColumnResolver, metaData);
          }

          expression = insert.getWhereCondition();
          if (expression != null) {
            expression.accept(expressionColumnResolver, metaData);
          }
        } else if (operation instanceof MergeUpdate) {
          MergeUpdate update = (MergeUpdate) operation;

          for (UpdateSet updateSet : update.getUpdateSets()) {
            for (Column column : updateSet.getColumns()) {
              column.accept(expressionColumnResolver, metaData);
            }

            for (Expression expression : updateSet.getValues()) {
              expression.accept(expressionColumnResolver, metaData);
            }
          }

          Expression expression = update.getAndPredicate();
          if (expression != null) {
            expression.accept(expressionColumnResolver, metaData);
          }

          expression = update.getWhereCondition();
          if (expression != null) {
            expression.accept(expressionColumnResolver, metaData);
          }
        } else if (operation instanceof MergeDelete) {
          MergeDelete delete = (MergeDelete) operation;

          Expression expression = delete.getAndPredicate();
          if (expression != null) {
            expression.accept(expressionColumnResolver, metaData);
          }
        }

      }

      return resultSetMetaData;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Select select, S s) {
      return select.accept((SelectVisitor<JdbcResultSetMetaData>) JSQLResolver.this, s);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Upsert upsert, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(UseStatement useStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Block block, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(DescribeStatement describeStatement, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ExplainStatement explainStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ShowStatement showStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(DeclareStatement declareStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(Grant grant, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateSequence createSequence, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(AlterSequence alterSequence, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateFunctionalStatement createFunctionalStatement,
        S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(CreateSynonym createSynonym, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(AlterSession alterSession, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(IfElseStatement ifElseStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(RenameTableStatement renameTableStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(PurgeStatement purgeStatement, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(AlterSystemStatement alterSystemStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(UnsupportedStatement unsupportedStatement, S s) {
      return null;
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedInsert parenthesedInsert, S s) {
      return parenthesedInsert.getInsert().accept(this, s);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedUpdate parenthesedUpdate, S s) {
      return parenthesedUpdate.getUpdate().accept(this, s);
    }


    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedDelete parenthesedDelete, S s) {
      return parenthesedDelete.getDelete().accept(this, s);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(SessionStatement sessionStatement, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Import imprt, S context) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Export export, S context) {
      return null;
    }
  }
}
