package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
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
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
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
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

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
  List<JdbcColumn> whereColumns = new ArrayList<>();
  List<JdbcColumn> groupByColumns = new ArrayList<>();
  List<JdbcColumn> havingColumns = new ArrayList<>();
  List<JdbcColumn> joinedOnColumns = new ArrayList<>();
  List<JdbcColumn> orderByColumns = new ArrayList<>();

  public JSQLResolver(JdbcMetaData metaData) {
    super(metaData);
  }

  public JSQLResolver(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    super(currentCatalogName, currentSchemaName, metaDataDefinition);
  }

  public JSQLResolver(String[][] metaDataDefinition) {
    super(metaDataDefinition);
  }

  public JdbcResultSetMetaData visit(PlainSelect select, JdbcMetaData metaData) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = select.getFromItem();
    List<Join> joins = select.getJoins();

    if (select.getWithItemsList() != null) {
      for (WithItem<?> withItem : select.getWithItemsList()) {
        Alias alias = withItem.getAlias();
        JdbcResultSetMetaData rsMetaData = withItem.accept(this, metaData);
        metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
      }
    }

    if (fromItem instanceof Table) {
      Alias alias = fromItem.getAlias();
      Table t = (Table) fromItem;
      if (alias != null) {
        metaData.getFromTables().put(alias.getName(), (Table) fromItem);
      } else {
        metaData.getFromTables().put(t.getName(), (Table) fromItem);
      }
    } else if (fromItem != null) {
      Alias alias = fromItem.getAlias();
      JdbcResultSetMetaData rsMetaData = fromItem.accept(this, JdbcMetaData.copyOf(metaData));
      JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
          "Error in FromItem " + fromItem);
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


    if (select.getWithItemsList() != null) {
      for (WithItem<?> withItem : select.getWithItemsList()) {
        withColumns.addAll(withItem.accept(expressionColumnResolver, select));
      }
    }

    // column positions in MetaData start at 1
    for (SelectItem<?> selectItem : select.getSelectItems()) {
      selectColumns.addAll(selectItem.getExpression().accept(expressionColumnResolver, metaData));
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

  public List<JdbcColumn> getGroupByColumns() {
    return groupByColumns;
  }

  public List<JdbcColumn> getHavingColumns() {
    return havingColumns;
  }

  public List<JdbcColumn> getJoinedOnColumns() {
    return joinedOnColumns;
  }

  public List<JdbcColumn> getOrderByColumns() {
    return orderByColumns;
  }

  public Set<JdbcColumn> flatten(Collection<JdbcColumn> columns) {
    LinkedHashSet<JdbcColumn> flattenedSet = new LinkedHashSet<>();
    for (JdbcColumn column : columns) {
      if (column.getExpression() instanceof Column) {
        flattenedSet.add(column);
      } else {
        flattenedSet.addAll(flatten(column.getChildren()));
      }
    }
    return flattenedSet;
  }

  /**
   * Resolves all the columns used at any clause of a SELECT statement for an empty CURRENT_CATALOG
   * and an empty CURRENT_SCHEMA.
   *
   * @param sqlStr the `SELECT` statement text
   * @throws net.sf.jsqlparser.JSQLParserException when the `SELECT` statement text can not be
   *         parsed
   */
  public Set<JdbcColumn> resolve(String sqlStr) throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      Select select = (Select) st;
      select.accept((SelectVisitor<JdbcResultSetMetaData>) this, JdbcMetaData.copyOf(metaData));
    } else if (st instanceof Delete) {
      Delete delete = (Delete) st;
      delete.accept(new Statementresolver(), JdbcMetaData.copyOf(metaData));
    } else {
      throw new RuntimeException("Unsupported Statement");
    }

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

  private class Statementresolver implements StatementVisitor<JdbcResultSetMetaData> {

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

      FromItem fromItem = delete.getTable();
      List<Join> joins = delete.getJoins();

      if (delete.getWithItemsList() != null) {
        for (WithItem<?> withItem : delete.getWithItemsList()) {
          Alias alias = withItem.getAlias();
          JdbcResultSetMetaData rsMetaData = withItem.accept(JSQLResolver.this, metaData);
          metaData.put(rsMetaData, alias.getUnquotedName(), "Error in WithItem " + withItem);
        }
      }

      if (fromItem instanceof Table) {
        Alias alias = fromItem.getAlias();
        Table t = (Table) fromItem;
        if (alias != null) {
          metaData.getFromTables().put(alias.getName(), (Table) fromItem);
        } else {
          metaData.getFromTables().put(t.getName(), (Table) fromItem);
        }
      } else if (fromItem != null) {
        Alias alias = fromItem.getAlias();
        JdbcResultSetMetaData rsMetaData =
            fromItem.accept(JSQLResolver.this, JdbcMetaData.copyOf(metaData));
        JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName() : "",
            "Error in FromItem " + fromItem);
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
    public void visit(Delete delete) {
      StatementVisitor.super.visit(delete);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Update update, S s) {
      return null;
    }

    @Override
    public void visit(Update update) {
      StatementVisitor.super.visit(update);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Insert insert, S s) {
      return null;
    }

    @Override
    public void visit(Insert insert) {
      StatementVisitor.super.visit(insert);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Drop drop, S s) {
      return null;
    }

    @Override
    public void visit(Drop drop) {
      StatementVisitor.super.visit(drop);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Truncate truncate, S s) {
      return null;
    }

    @Override
    public void visit(Truncate truncate) {
      StatementVisitor.super.visit(truncate);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateIndex createIndex, S s) {
      return null;
    }

    @Override
    public void visit(CreateIndex createIndex) {
      StatementVisitor.super.visit(createIndex);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateSchema createSchema, S s) {
      return null;
    }

    @Override
    public void visit(CreateSchema createSchema) {
      StatementVisitor.super.visit(createSchema);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateTable createTable, S s) {
      return null;
    }

    @Override
    public void visit(CreateTable createTable) {
      StatementVisitor.super.visit(createTable);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateView createView, S s) {
      return null;
    }

    @Override
    public void visit(CreateView createView) {
      StatementVisitor.super.visit(createView);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(AlterView alterView, S s) {
      return null;
    }

    @Override
    public void visit(AlterView alterView) {
      StatementVisitor.super.visit(alterView);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(
        RefreshMaterializedViewStatement refreshMaterializedViewStatement, S s) {
      return null;
    }

    @Override
    public void visit(RefreshMaterializedViewStatement materializedView) {
      StatementVisitor.super.visit(materializedView);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Alter alter, S s) {
      return null;
    }

    @Override
    public void visit(Alter alter) {
      StatementVisitor.super.visit(alter);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Statements statements, S s) {
      return null;
    }

    @Override
    public void visit(Statements statements) {
      StatementVisitor.super.visit(statements);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Execute execute, S s) {
      return null;
    }

    @Override
    public void visit(Execute execute) {
      StatementVisitor.super.visit(execute);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(SetStatement setStatement, S s) {
      return null;
    }

    @Override
    public void visit(SetStatement set) {
      StatementVisitor.super.visit(set);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ResetStatement resetStatement, S s) {
      return null;
    }

    @Override
    public void visit(ResetStatement reset) {
      StatementVisitor.super.visit(reset);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ShowColumnsStatement showColumnsStatement, S s) {
      return null;
    }

    @Override
    public void visit(ShowColumnsStatement showColumns) {
      StatementVisitor.super.visit(showColumns);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ShowIndexStatement showIndexStatement, S s) {
      return null;
    }

    @Override
    public void visit(ShowIndexStatement showIndex) {
      StatementVisitor.super.visit(showIndex);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ShowTablesStatement showTablesStatement, S s) {
      return null;
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
      StatementVisitor.super.visit(showTables);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Merge merge, S s) {
      return null;
    }

    @Override
    public void visit(Merge merge) {
      StatementVisitor.super.visit(merge);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Select select, S s) {
      return null;
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Upsert upsert, S s) {
      return null;
    }

    @Override
    public void visit(Upsert upsert) {
      StatementVisitor.super.visit(upsert);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(UseStatement useStatement, S s) {
      return null;
    }

    @Override
    public void visit(UseStatement use) {
      StatementVisitor.super.visit(use);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Block block, S s) {
      return null;
    }

    @Override
    public void visit(Block block) {
      StatementVisitor.super.visit(block);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(DescribeStatement describeStatement, S s) {
      return null;
    }

    @Override
    public void visit(DescribeStatement describe) {
      StatementVisitor.super.visit(describe);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ExplainStatement explainStatement, S s) {
      return null;
    }

    @Override
    public void visit(ExplainStatement explainStatement) {
      StatementVisitor.super.visit(explainStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ShowStatement showStatement, S s) {
      return null;
    }

    @Override
    public void visit(ShowStatement showStatement) {
      StatementVisitor.super.visit(showStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(DeclareStatement declareStatement, S s) {
      return null;
    }

    @Override
    public void visit(DeclareStatement declareStatement) {
      StatementVisitor.super.visit(declareStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(Grant grant, S s) {
      return null;
    }

    @Override
    public void visit(Grant grant) {
      StatementVisitor.super.visit(grant);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateSequence createSequence, S s) {
      return null;
    }

    @Override
    public void visit(CreateSequence createSequence) {
      StatementVisitor.super.visit(createSequence);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(AlterSequence alterSequence, S s) {
      return null;
    }

    @Override
    public void visit(AlterSequence alterSequence) {
      StatementVisitor.super.visit(alterSequence);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateFunctionalStatement createFunctionalStatement,
        S s) {
      return null;
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
      StatementVisitor.super.visit(createFunctionalStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(CreateSynonym createSynonym, S s) {
      return null;
    }

    @Override
    public void visit(CreateSynonym createSynonym) {
      StatementVisitor.super.visit(createSynonym);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(AlterSession alterSession, S s) {
      return null;
    }

    @Override
    public void visit(AlterSession alterSession) {
      StatementVisitor.super.visit(alterSession);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(IfElseStatement ifElseStatement, S s) {
      return null;
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
      StatementVisitor.super.visit(ifElseStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(RenameTableStatement renameTableStatement, S s) {
      return null;
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
      StatementVisitor.super.visit(renameTableStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(PurgeStatement purgeStatement, S s) {
      return null;
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
      StatementVisitor.super.visit(purgeStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(AlterSystemStatement alterSystemStatement, S s) {
      return null;
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
      StatementVisitor.super.visit(alterSystemStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(UnsupportedStatement unsupportedStatement, S s) {
      return null;
    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
      StatementVisitor.super.visit(unsupportedStatement);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedInsert parenthesedInsert, S s) {
      return null;
    }

    @Override
    public void visit(ParenthesedInsert parenthesedInsert) {
      StatementVisitor.super.visit(parenthesedInsert);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedUpdate parenthesedUpdate, S s) {
      return null;
    }

    @Override
    public void visit(ParenthesedUpdate parenthesedUpdate) {
      StatementVisitor.super.visit(parenthesedUpdate);
    }

    @Override
    public <S> JdbcResultSetMetaData visit(ParenthesedDelete parenthesedDelete, S s) {
      return null;
    }

    @Override
    public void visit(ParenthesedDelete parenthesedDelete) {
      StatementVisitor.super.visit(parenthesedDelete);
    }
  }
}
