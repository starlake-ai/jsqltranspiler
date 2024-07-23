package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLExpressionColumnResolver extends ExpressionVisitorAdapter<List<JdbcColumn>>
    implements SelectVisitor<List<JdbcColumn>> {
  public final static Logger LOGGER =
      Logger.getLogger(JSQLExpressionColumnResolver.class.getName());
  private final JSQLColumResolver columResolver;

  public JSQLExpressionColumnResolver(JSQLColumResolver columResolver) {
    this.columResolver = columResolver;
  }

  private static JdbcColumn getJdbcColumn(JdbcMetaData metaData, Column column) {
    JdbcColumn jdbcColumn = null;
    String columnTableName = null;
    String columnSchemaName = null;
    String columnCatalogName = null;

    CaseInsensitiveLinkedHashMap<Table> fromTables = metaData.getFromTables();

    Table table = column.getTable();
    if (table != null) {
      columnTableName = table.getName();

      if (table.getSchemaName() != null) {
        columnSchemaName = table.getSchemaName();
      }

      if (table.getDatabase() != null) {
        columnCatalogName = table.getDatabase().getDatabaseName();
      }
    }

    if (columnTableName != null) {
      // column has a table name prefix, which could be the actual table name or the table's
      // alias
      String actualColumnTableName =
          fromTables.containsKey(columnTableName) ? fromTables.get(columnTableName).getName()
              : null;
      String actualColumnSchemaName =
          fromTables.containsKey(columnTableName) ? fromTables.get(columnTableName).getSchemaName()
              : columnSchemaName;
      String actualColumnCatalogName =
          fromTables.containsKey(columnTableName) ? fromTables.get(columnTableName).getCatalogName()
              : columnCatalogName;

      jdbcColumn = metaData.getColumn(actualColumnCatalogName, actualColumnSchemaName,
          actualColumnTableName, column.getColumnName());

    } else {
      // column has no table name prefix and we have to lookup in all tables of the scope
      for (Table t : fromTables.values()) {
        String tableSchemaName = t.getSchemaName();
        String tableCatalogName =
            t.getDatabase() != null ? t.getDatabase().getDatabaseName() : null;

        jdbcColumn = metaData.getColumn(tableCatalogName, tableSchemaName, t.getName(),
            column.getColumnName());
        if (jdbcColumn != null) {
          break;
        }
      }
    }
    return jdbcColumn;
  }

  @Override
  protected <S> List<JdbcColumn> visitExpression(Expression expression, S context) {
    JdbcColumn col = new JdbcColumn(expression.toString().replaceAll("[()]", ""), expression);
    return List.of(col);
  }

  @Override
  protected <S> List<JdbcColumn> visitExpressions(Expression expression, S context,
      Collection<Expression> subExpressions) {
    JdbcColumn col = new JdbcColumn(expression.getClass().getSimpleName(), expression);
    for (Expression e : subExpressions) {
      col.add(e.accept(this, context));
    }
    return List.of(col);
  }

  @Override
  public <S> List<JdbcColumn> visit(Function function, S context) {
    JdbcColumn col = new JdbcColumn(function.getName(), function);
    for (Expression expression : function.getParameters()) {
      List<JdbcColumn> subColumns = expression.accept(this, context);
      col.add(subColumns);
    }
    return List.of(col);
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  @Override
  public <S> List<JdbcColumn> visit(AllTableColumns allTableColumns, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) context;
      Table table = allTableColumns.getTable();

      HashSet<JdbcColumn> excepts = new HashSet<>();
      ExpressionList<Column> exceptColumns = allTableColumns.getExceptColumns();
      if (exceptColumns != null) {
        for (Column c : exceptColumns) {
          JdbcColumn jdbcColumn =
              getJdbcColumn(metaData, c.getTable() == null ? c.withTable(table) : c);
          if (jdbcColumn != null) {
            excepts.add(jdbcColumn);
          } else {
            LOGGER.warning("Could not resolve EXCEPT Column " + c.getFullyQualifiedName());
          }
        }
      }

      HashMap<JdbcColumn, Alias> replaceMap = new HashMap<>();
      List<SelectItem<Column>> replaceExpressions = allTableColumns.getReplaceExpressions();
      if (replaceExpressions != null) {
        for (SelectItem<Column> c : replaceExpressions) {
          JdbcColumn jdbcColumn = getJdbcColumn(metaData,
              c.getExpression().getTable() == null ? c.getExpression().withTable(table)
                  : c.getExpression());
          if (jdbcColumn != null) {
            replaceMap.put(jdbcColumn, c.getAlias());
          } else {
            LOGGER.warning(
                "Could not resolve REPLACE Column " + c.getExpression().getFullyQualifiedName());
          }
        }
      }

      /*
      -- invalid:
      select JSQLTranspilerTest.main.listing.*
      from  JSQLTranspilerTest.main.listing
      
      select main.listing.*
      from  main.listing
      
      -- valid:
      select listing.*
      from  JSQLTranspilerTest.main.listing
       */


      String columnTablename = null;
      if (table != null) {
        columnTablename = table.getName();
      }

      if (columnTablename != null) {
        // column has a table name prefix, which could be the actual table name or the table's
        // alias

        Table actualTable = metaData.getFromTables().get(columnTablename);
        if (actualTable == null) {
          throw new RuntimeException("Table " + columnTablename + " not found in tables "
              + Arrays.deepToString(metaData.getFromTables().keySet().toArray(new String[0])));
        }
        String tableSchemaName = actualTable.getSchemaName();
        String tableCatalogName =
            actualTable.getDatabase() != null ? actualTable.getDatabase().getDatabaseName() : null;

        for (JdbcColumn jdbcColumn : metaData.getTableColumns(tableCatalogName, tableSchemaName,
            actualTable.getName(), null)) {

          if (!excepts.contains(jdbcColumn)) {
            columns.add(jdbcColumn);
          }
        }
      }
    }
    return columns;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  @Override
  public <S> List<JdbcColumn> visit(AllColumns allColumns, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();

    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) context;

      HashSet<JdbcColumn> excepts = new HashSet<>();
      ExpressionList<Column> exceptColumns = allColumns.getExceptColumns();
      if (exceptColumns != null) {
        for (Column c : exceptColumns) {
          if (c.getTable() != null) {
            JdbcColumn jdbcColumn = getJdbcColumn(metaData, c);
            if (jdbcColumn != null) {
              excepts.add(jdbcColumn);
            } else {
              LOGGER.warning("Could not resolve EXCEPT Column " + c.getFullyQualifiedName());
            }
          } else {
            for (Table t : metaData.getFromTables().values()) {
              JdbcColumn jdbcColumn = getJdbcColumn(metaData, c.withTable(t));
              if (jdbcColumn != null) {
                excepts.add(jdbcColumn);
              } else {
                LOGGER.fine("Could not resolve EXCEPT Column " + c.getFullyQualifiedName());
              }
            }
          }
        }
      }

      HashMap<JdbcColumn, Alias> replaceMap = new HashMap<>();
      List<SelectItem<Column>> replaceExpressions = allColumns.getReplaceExpressions();
      if (replaceExpressions != null) {
        for (SelectItem<Column> c : replaceExpressions) {
          if (c.getExpression().getTable() != null) {
            JdbcColumn jdbcColumn = getJdbcColumn(metaData, c.getExpression());
            if (jdbcColumn != null) {
              replaceMap.put(jdbcColumn, c.getAlias());
            } else {
              LOGGER.warning(
                  "Could not resolve REPLACE Column " + c.getExpression().getFullyQualifiedName());
            }
          } else {
            for (Table t : metaData.getFromTables().values()) {
              JdbcColumn jdbcColumn = getJdbcColumn(metaData, c.getExpression().withTable(t));
              if (jdbcColumn != null) {
                replaceMap.put(jdbcColumn, c.getAlias());
              } else {
                LOGGER.warning("Could not resolve REPLACE Column "
                    + c.getExpression().getFullyQualifiedName());
              }
            }
          }
        }
      }

      for (Table t : metaData.getFromTables().values()) {
        String tableSchemaName = t.getSchemaName();
        String tableCatalogName =
            t.getDatabase() != null ? t.getDatabase().getDatabaseName() : null;

        for (JdbcColumn jdbcColumn : metaData.getTableColumns(tableCatalogName, tableSchemaName,
            t.getName(), null)) {
          boolean inserted = false;
          if (!excepts.contains(jdbcColumn)) {

            if (metaData.getNaturalJoinedTables().containsValue(t)) {
              for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).columnName.equalsIgnoreCase(jdbcColumn.columnName)) {
                  inserted = true;
                  break;
                }
              }
            }

            if (metaData.getLeftUsingJoinedColumns().containsKey(jdbcColumn.columnName)) {
              for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).columnName.equalsIgnoreCase(jdbcColumn.columnName)) {
                  inserted = true;
                  break;
                }
              }
            }

            if (metaData.getRightUsingJoinedColumns().containsKey(jdbcColumn.columnName)) {
              for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).columnName.equalsIgnoreCase(jdbcColumn.columnName)) {
                  columns.remove(i);
                  columns.add(i, jdbcColumn);
                  inserted = true;
                  break;
                }
              }
            }

            if (!inserted) {
              columns.add(jdbcColumn);
            }

          }
        }
      }
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(Column column, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();

    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) context;
      JdbcColumn jdbcColumn = getJdbcColumn(metaData, column);

      if (jdbcColumn == null) {
        throw new RuntimeException("Column " + column + " not found in tables "
            + Arrays.deepToString(metaData.getFromTables().values().toArray()));
      }

      columns.add(jdbcColumn);
    }

    return columns;
  }


  @Override
  public <S> List<JdbcColumn> visit(ParenthesedSelect select, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (select.getWithItemsList() != null) {
      for (WithItem item : select.getWithItemsList()) {
        columns.addAll(item.accept((SelectVisitor<JdbcResultSetMetaData>) columResolver, context)
            .getColumns());
      }
    }
    columns.addAll(
        select.accept((SelectVisitor<JdbcResultSetMetaData>) columResolver, context).getColumns());
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(Select select, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (select.getWithItemsList() != null) {
      for (WithItem item : select.getWithItemsList()) {
        for (JdbcColumn col : item
            .accept((SelectVisitor<JdbcResultSetMetaData>) columResolver, context).getColumns()) {
          columns.add(col.setExpression(select));
        }
      }
    }

    for (JdbcColumn col : select
        .accept((SelectVisitor<JdbcResultSetMetaData>) columResolver, context).getColumns()) {
      columns.add(col.setExpression(select));
    }

    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(PlainSelect plainSelect, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          plainSelect.accept(columResolver, JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(SetOperationList setOperationList, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          setOperationList.accept(columResolver, JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(WithItem withItem, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          withItem.accept((SelectVisitor<JdbcResultSetMetaData>) columResolver,
              JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(Values values, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          values.accept((SelectVisitor<JdbcResultSetMetaData>) columResolver,
              JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(LateralSubSelect lateralSubSelect, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          lateralSubSelect.accept((SelectVisitor<JdbcResultSetMetaData>) columResolver,
              JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }

  @Override
  public <S> List<JdbcColumn> visit(TableStatement tableStatement, S context) {
    ArrayList<JdbcColumn> columns = new ArrayList<>();
    if (context instanceof JdbcMetaData) {
      JdbcResultSetMetaData resultSetMetaData =
          tableStatement.accept(columResolver, JdbcMetaData.copyOf((JdbcMetaData) context));
      columns.addAll(resultSetMetaData.getColumns());
    }
    return columns;
  }
}
