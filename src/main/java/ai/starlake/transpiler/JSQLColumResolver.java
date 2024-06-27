/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI
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

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * A class for resolving the actual columns returned by a SELECT statement. Depends on virtual or
 * physical Database Metadata holding the schema and table information.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLColumResolver
    implements SelectVisitor<JdbcResultSetMetaData>, FromItemVisitor<JdbcResultSetMetaData> {
  public final static Logger LOGGER = Logger.getLogger(JSQLColumResolver.class.getName());

  private final String currentCatalogName;
  private final String currentSchemaName;

  private final JdbcMetaData metaData;

  public JSQLColumResolver(String currentCatalogName, String currentSchemaName,
      JdbcMetaData metaData) {
    this.currentCatalogName = currentCatalogName;
    this.currentSchemaName = currentSchemaName;
    this.metaData = metaData;

  }

  public JSQLColumResolver(JdbcMetaData metaData) {
    this("", "", metaData);
  }

  public JSQLColumResolver(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    this(currentCatalogName, currentSchemaName, new JdbcMetaData(metaDataDefinition));
  }

  public JSQLColumResolver(String[][] metaDataDefinition) {
    this("", "", metaDataDefinition);
  }


  /**
   * Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and
   * CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaData the Database Meta Data
   * @param currentCatalogName the CURRENT_CATALOG name (which is the default catalog for accessing
   *        the schemas)
   * @param currentSchemaName the CURRENT_SCHEMA name (which is the default schema for accessing the
   *        tables)
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static ResultSetMetaData getResultSetMetaData(String sqlStr, JdbcMetaData metaData,
      String currentCatalogName, String currentSchemaName) throws JSQLParserException {
    if (sqlStr == null || sqlStr.trim().isEmpty()) {
      throw new JSQLParserException("The provided Statement must not be empty.");
    }

    JSQLColumResolver resolver =
        new JSQLColumResolver(currentCatalogName, currentSchemaName, metaData);

    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      PlainSelect select = (PlainSelect) st;
      return select.accept(resolver, metaData);

    } else {
      throw new RuntimeException(
          st.getClass().getSimpleName() + " Statements are not supported yet.");
    }
  }

  /**
   * Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and
   * CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaDataDefinition the meta data definition as an array of Tables with Columns e.g. {
   *        TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
   * @param currentCatalogName the CURRENT_CATALOG name (which is the default catalog for accessing
   *        the schemas)
   * @param currentSchemaName the CURRENT_SCHEMA name (which is the default schema for accessing the
   *        tables)
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public static ResultSetMetaData getResultSetMetaData(String sqlStr, String[][] metaDataDefinition,
      String currentCatalogName, String currentSchemaName) throws JSQLParserException {
    JdbcMetaData metaData =
        new JdbcMetaData(currentCatalogName, currentSchemaName, metaDataDefinition);
    return getResultSetMetaData(sqlStr, metaData, currentCatalogName, currentSchemaName);
  }

  /**
   * Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and and
   * empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaDataDefinition the meta data definition as an array of Tables with Columns e.g. {
   *        TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public ResultSetMetaData getResultSetMetaData(String sqlStr, String[][] metaDataDefinition)
      throws JSQLParserException {
    JdbcMetaData metaData =
        new JdbcMetaData(currentCatalogName, currentSchemaName, metaDataDefinition);
    return getResultSetMetaData(sqlStr, metaData, "", "");
  }

  private static JdbcColumn getJdbcColumn(JdbcMetaData metaData, Column column,
      CaseInsensitiveLinkedHashMap<Table> fromTables) {
    JdbcColumn jdbcColumn = null;
    String columnTableName = null;
    String columnSchemaName = null;
    String columnCatalogName = null;

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
      jdbcColumn = metaData.getColumn(columnCatalogName, columnSchemaName, actualColumnTableName,
          column.getColumnName());

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

  /**
   * Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an
   * empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public ResultSetMetaData getResultSetMetaData(String sqlStr) throws JSQLParserException {

    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      Select select = (Select) st;
      return select.accept(this, metaData);
    } else {
      throw new RuntimeException("Unsupported Statement");
    }
  }


  @Override
  public <S> JdbcResultSetMetaData visit(Table table, S s) {
    JdbcResultSetMetaData rsMetaData = new JdbcResultSetMetaData();

    if (table.getSchemaName() == null || table.getSchemaName().isEmpty()) {
      table.setSchemaName(currentSchemaName);
    }

    if (table.getDatabase() == null) {
      table.setDatabase(new Database(currentCatalogName));
    } else if (table.getDatabase().getDatabaseName() == null
        || table.getDatabase().getDatabaseName().isEmpty()) {
      table.getDatabase().setDatabaseName(currentCatalogName);
    }

    for (JdbcColumn jdbcColumn : metaData.getTableColumns(table.getDatabase().getDatabaseName(),
        table.getSchemaName(), table.getName(), null)) {

      rsMetaData.add(jdbcColumn, null);
    }

    return rsMetaData;
  }

  @Override
  public void visit(Table tableName) {
    FromItemVisitor.super.visit(tableName);
  }

  public JdbcResultSetMetaData visit(ParenthesedSelect parenthesedSelect, JdbcMetaData parameters) {
    JdbcResultSetMetaData rsMetaData = null;
    Alias alias = parenthesedSelect.getAlias();
    JdbcTable t = new JdbcTable(currentCatalogName, currentSchemaName,
        alias != null ? parenthesedSelect.getAlias().getName() : "");

    rsMetaData = parenthesedSelect.getSelect().accept((SelectVisitor<JdbcResultSetMetaData>) this,
        JdbcMetaData.copyOf(parameters));
    try {
      int columnCount = rsMetaData.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        t.add(t.tableCatalog, t.tableSchema, t.tableName, rsMetaData.getColumnName(i),
            rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
            rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
            "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
            rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
      }
      metaData.put(t);
    } catch (SQLException ex) {
      throw new RuntimeException("Error in WITH clause " + parenthesedSelect.toString(), ex);
    }
    return rsMetaData;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(ParenthesedSelect parenthesedSelect, S parameters) {
    if (parameters instanceof JdbcMetaData) {
      JdbcMetaData metaData1 = (JdbcMetaData) parameters;
      return visit(parenthesedSelect, JdbcMetaData.copyOf(metaData1));
    }
    return null;
  }

  @Override
  public void visit(ParenthesedSelect parenthesedSelect) {
    SelectVisitor.super.visit(parenthesedSelect);
  }


  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public JdbcResultSetMetaData visit(PlainSelect select, JdbcMetaData metaData) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = select.getFromItem();
    List<Join> joins = select.getJoins();

    CaseInsensitiveLinkedHashMap<Table> fromTables = new CaseInsensitiveLinkedHashMap<>();

    if (select.getWithItemsList() != null) {
      for (WithItem withItem : select.getWithItemsList()) {
        withItem.accept((SelectVisitor<JdbcResultSetMetaData>) this, metaData);
      }
    }

    if (fromItem instanceof Table) {
      Alias alias = fromItem.getAlias();
      Table t = (Table) fromItem;

      if (alias != null) {
        fromTables.put(alias.getName(), (Table) fromItem);
      } else {
        fromTables.put(t.getName(), (Table) fromItem);
      }
    } else if (fromItem != null) {
      Alias alias = fromItem.getAlias();
      JdbcTable t = new JdbcTable(currentCatalogName, currentSchemaName,
          alias != null ? alias.getName() : "");

      JdbcResultSetMetaData rsMetaData = fromItem.accept(this, JdbcMetaData.copyOf(metaData));
      try {
        int columnCount = rsMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          t.add(t.tableCatalog, t.tableSchema, t.tableName, rsMetaData.getColumnName(i),
              rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
              rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
              "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
              rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
        }
        metaData.put(t);
        fromTables.put(alias != null ? alias.getName() : t.tableName,
            new Table(alias != null ? alias.getName() : t.tableName));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    if (joins != null) {
      for (Join join : joins) {
        if (join.getFromItem() instanceof Table) {
          Alias alias = join.getFromItem().getAlias();
          Table t = (Table) join.getFromItem();

          if (alias != null) {
            fromTables.put(alias.getName(), t);
          } else {
            fromTables.put(t.getName(), t);
          }
        } else {
          Alias alias = join.getFromItem().getAlias();
          JdbcTable t = new JdbcTable(currentCatalogName, currentSchemaName,
              alias != null ? alias.getName() : "");

          JdbcResultSetMetaData rsMetaData =
              join.getFromItem().accept(this, JdbcMetaData.copyOf(metaData));
          try {
            int columnCount = rsMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
              t.add(t.tableCatalog, t.tableSchema, t.tableName, rsMetaData.getColumnName(i),
                  rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
                  rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i),
                  "", "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
                  rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
            }
            metaData.put(t);
            fromTables.put(alias != null ? alias.getName() : t.tableName,
                new Table(alias != null ? alias.getName() : t.tableName));
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    for (Table t : fromTables.values()) {
      if (t.getSchemaName() == null || t.getSchemaName().isEmpty()) {
        t.setSchemaName(currentSchemaName);
      }

      if (t.getDatabase() == null) {
        t.setDatabase(new Database(currentCatalogName));
      } else if (t.getDatabase().getDatabaseName() == null
          || t.getDatabase().getDatabaseName().isEmpty()) {
        t.getDatabase().setDatabaseName(currentCatalogName);
      }
    }

    /* this is valid SQL:
    
    SELECT
        main.sales.salesid
    FROM main.sales
     */

    // column positions in MetaData start at 1
    for (SelectItem<?> selectItem : select.getSelectItems()) {

      if (selectItem.getExpression() instanceof Column) {
        Column column = (Column) selectItem.getExpression();
        Alias alias = selectItem.getAlias();
        JdbcColumn jdbcColumn = getJdbcColumn(metaData, column, fromTables);

        if (jdbcColumn == null) {
          throw new RuntimeException("Column " + column + " not found in tables "
              + Arrays.deepToString(fromTables.values().toArray()));
        } else {
          resultSetMetaData.add(jdbcColumn,
              alias != null ? alias.withUseAs(false).toString() : null);
        }
      } else if (selectItem.getExpression() instanceof AllTableColumns) {
        AllTableColumns allTableColumns = (AllTableColumns) selectItem.getExpression();
        Table table = allTableColumns.getTable();

        HashSet<JdbcColumn> excepts = new HashSet<>();
        ExpressionList<Column> exceptColumns = allTableColumns.getExceptColumns();
        if (exceptColumns != null) {
          for (Column c : exceptColumns) {
            JdbcColumn jdbcColumn =
                getJdbcColumn(metaData, c.getTable() == null ? c.withTable(table) : c, fromTables);
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
                    : c.getExpression(),
                fromTables);
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

          Table actualTable = fromTables.get(columnTablename);
          if (actualTable == null) {
            throw new RuntimeException("Table " + columnTablename + " not found in tables "
                + Arrays.deepToString(fromTables.keySet().toArray(new String[0])));
          }
          String tableSchemaName = actualTable.getSchemaName();
          String tableCatalogName =
              actualTable.getDatabase() != null ? actualTable.getDatabase().getDatabaseName()
                  : null;

          for (JdbcColumn jdbcColumn : metaData.getTableColumns(tableCatalogName, tableSchemaName,
              actualTable.getName(), null)) {

            if (!excepts.contains(jdbcColumn)) {
              Alias alias = replaceMap.get(jdbcColumn);
              resultSetMetaData.add(jdbcColumn, alias != null ? alias.getName() : null);
            }
          }
        }
      } else if (selectItem.getExpression() instanceof AllColumns) {
        AllColumns allColumns = (AllColumns) selectItem.getExpression();

        HashSet<JdbcColumn> excepts = new HashSet<>();
        ExpressionList<Column> exceptColumns = allColumns.getExceptColumns();
        if (exceptColumns != null) {
          for (Column c : exceptColumns) {
            if (c.getTable() != null) {
              JdbcColumn jdbcColumn = getJdbcColumn(metaData, c, fromTables);
              if (jdbcColumn != null) {
                excepts.add(jdbcColumn);
              } else {
                LOGGER.warning("Could not resolve EXCEPT Column " + c.getFullyQualifiedName());
              }
            } else {
              for (Table t : fromTables.values()) {
                JdbcColumn jdbcColumn = getJdbcColumn(metaData, c.withTable(t), fromTables);
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
              JdbcColumn jdbcColumn = getJdbcColumn(metaData, c.getExpression(), fromTables);
              if (jdbcColumn != null) {
                replaceMap.put(jdbcColumn, c.getAlias());
              } else {
                LOGGER.warning("Could not resolve REPLACE Column "
                    + c.getExpression().getFullyQualifiedName());
              }
            } else {
              for (Table t : fromTables.values()) {
                JdbcColumn jdbcColumn =
                    getJdbcColumn(metaData, c.getExpression().withTable(t), fromTables);
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


        for (Table t : fromTables.values()) {
          String tableSchemaName = t.getSchemaName();
          String tableCatalogName =
              t.getDatabase() != null ? t.getDatabase().getDatabaseName() : null;

          for (JdbcColumn jdbcColumn : metaData.getTableColumns(tableCatalogName, tableSchemaName,
              t.getName(), null)) {

            if (!excepts.contains(jdbcColumn)) {
              Alias alias = replaceMap.get(jdbcColumn);
              resultSetMetaData.add(jdbcColumn, alias != null ? alias.getName() : null);
            }

          }
        }
      }
    }
    return resultSetMetaData;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(PlainSelect select, S parameters) {
    if (parameters instanceof JdbcMetaData) {
      return visit(select, (JdbcMetaData) parameters);
    } else {
      return null;
    }
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    SelectVisitor.super.visit(plainSelect);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(SetOperationList setOperationList, S parameters) {
    return null;
  }

  @Override
  public void visit(SetOperationList setOpList) {
    SelectVisitor.super.visit(setOpList);
  }


  @Override
  public <S> JdbcResultSetMetaData visit(WithItem withItem, S parameters) {
    if (parameters instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) parameters;

      JdbcTable t =
          new JdbcTable(currentCatalogName, currentSchemaName, withItem.getAlias().getName());

      JdbcResultSetMetaData md = withItem.getSelect()
          .accept((SelectVisitor<JdbcResultSetMetaData>) this, JdbcMetaData.copyOf(metaData));
      try {
        int columnCount = md.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          t.add(t.tableCatalog, t.tableSchema, t.tableName, md.getColumnName(i),
              md.getColumnType(i), md.getColumnClassName(i), md.getPrecision(i), md.getScale(i), 10,
              md.isNullable(i), "", "", md.getColumnDisplaySize(i), i, "", md.getCatalogName(i),
              md.getSchemaName(i), md.getTableName(i), null, "", "");
        }
        metaData.put(t);
      } catch (SQLException ex) {
        throw new RuntimeException("Error in WITH clause " + withItem.toString(), ex);
      }
    }

    return withItem.getSelect().accept((SelectVisitor<JdbcResultSetMetaData>) this, metaData);
  }

  @Override
  public void visit(WithItem withItem) {
    SelectVisitor.super.visit(withItem);
  }


  @Override
  public <S> JdbcResultSetMetaData visit(Values values, S parameters) {
    return null;
  }

  @Override
  public void visit(Values values) {
    SelectVisitor.super.visit(values);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(LateralSubSelect lateralSubSelect, S parameters) {
    return null;
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    SelectVisitor.super.visit(lateralSubSelect);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(TableFunction tableFunction, S parameters) {
    return null;
  }

  @Override
  public void visit(TableFunction tableFunction) {
    FromItemVisitor.super.visit(tableFunction);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(ParenthesedFromItem parenthesedFromItem, S parameters) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = parenthesedFromItem.getFromItem();
    try {
      resultSetMetaData.add(fromItem.accept(this, parameters));
    } catch (SQLException ex) {
      throw new RuntimeException("Failed on ParenthesedFromItem " + fromItem.toString(), ex);
    }

    List<Join> joins = parenthesedFromItem.getJoins();
    if (joins != null && !joins.isEmpty()) {
      for (Join join : joins) {
        try {
          resultSetMetaData.add(join.getFromItem().accept(this, parameters));
        } catch (SQLException ex) {
          throw new RuntimeException("Failed on Join " + join.getFromItem().toString(), ex);
        }
      }
    }
    return resultSetMetaData;
  }

  @Override
  public void visit(ParenthesedFromItem parenthesedFromItem) {
    FromItemVisitor.super.visit(parenthesedFromItem);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(TableStatement tableStatement, S parameters) {
    return null;
  }

  @Override
  public void visit(TableStatement tableStatement) {
    SelectVisitor.super.visit(tableStatement);
  }

}
