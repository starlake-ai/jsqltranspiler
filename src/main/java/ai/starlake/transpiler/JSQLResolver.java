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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.List;

public class JSQLResolver extends JSQLColumResolver {
    List<JdbcColumn> withColumns = new ArrayList<>();
    List<JdbcColumn> selectColumns = new ArrayList<>();
    List<JdbcColumn> whereColumns = new ArrayList<>();
    List<JdbcColumn> groupByColumns = new ArrayList<>();
    List<JdbcColumn> havingColumns = new ArrayList<>();
    List<JdbcColumn> joinedOnColumns = new ArrayList<>();

    public JSQLResolver(JdbcMetaData metaData) {
        super(metaData);
    }

    public JSQLResolver(String currentCatalogName, String currentSchemaName, String[][] metaDataDefinition) {
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
            JdbcTable t = metaData.put(rsMetaData, alias != null ? alias.getUnquotedName():"",
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


        // column positions in MetaData start at 1
        ArrayList<SelectItem<?>> newSelectItems = new ArrayList<>();
        for (SelectItem<?> selectItem : select.getSelectItems()) {
            Alias alias = selectItem.getAlias();
            List<JdbcColumn> jdbcColumns =
                    selectItem.getExpression().accept(expressionColumnResolver, metaData);

            for (JdbcColumn col : jdbcColumns) {
                resultSetMetaData.add(col, alias != null ? alias.getUnquotedName() : null);
                Table t = new Table(col.tableCatalog, col.tableSchema, col.tableName);
                if (selectItem.getExpression() instanceof AllColumns
                        || selectItem.getExpression() instanceof AllTableColumns) {
                    newSelectItems.add(new SelectItem<>(
                            new Column(t, col.columnName).withCommentText("Resolved Column"), alias));
                } else {
                    newSelectItems.add(selectItem);
                }
            }

        }
        select.setSelectItems(newSelectItems);

        // Join expressions
        if (joins!=null) {
            for (Join join: joins) {
                for (Column column : join.getUsingColumns()) {
                    joinedOnColumns.addAll(column.accept(expressionColumnResolver, select));
                }
                for (Expression expression : join.getOnExpressions()) {
                    joinedOnColumns.addAll(expression.accept(expressionColumnResolver, select));
                }
            }
        }

        // where expressions
        Expression whereExpression = select.getWhere();
        if (whereExpression!=null) {
            whereColumns.addAll(
                   whereExpression.accept(expressionColumnResolver, metaData));
        }

        // aggregate expressions
        GroupByElement groupBy = select.getGroupBy();
        if (groupBy!=null) {
            ExpressionList<Expression> expressionList = groupBy.getGroupByExpressionList();
            for (Expression expression:expressionList) {
                groupByColumns.addAll(
                        expression.accept(expressionColumnResolver, metaData));
            }

            List<ExpressionList<Expression>> groupingSets = groupBy.getGroupingSets();
            for (ExpressionList<Expression> expressions:groupingSets) {
                for (Expression e:expressions) {
                    groupByColumns.addAll(
                            e.accept(expressionColumnResolver, metaData));
                }
            }
        }

        return resultSetMetaData;
    }

    public List<JdbcColumn> getWhereColumns() {
        return whereColumns;
    }

    public JSQLResolver setWhereColumns(List<JdbcColumn> whereColumns) {
        this.whereColumns = whereColumns;
        return this;
    }

    /**
     * Resolves all the columns used at any clause of a SELECT statement for an empty CURRENT_CATALOG and an
     * empty CURRENT_SCHEMA.
     *
     * @param sqlStr the `SELECT` statement text
     * @throws net.sf.jsqlparser.JSQLParserException when the `SELECT` statement text can not be parsed
     */
    public void resolve(String sqlStr) throws JSQLParserException {

        Statement st = CCJSqlParserUtil.parse(sqlStr);
        if (st instanceof Select) {
            Select select = (Select) st;
            JdbcResultSetMetaData jdbcResultSetMetaData = select.accept((SelectVisitor<JdbcResultSetMetaData>) this,
                    JdbcMetaData.copyOf(metaData));
        } else {
            throw new RuntimeException("Unsupported Statement");
        }
    }
}
