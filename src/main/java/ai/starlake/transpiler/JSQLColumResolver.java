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
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class JSQLColumResolver {
  public final static Logger LOGGER = Logger.getLogger(JSQLColumResolver.class.getName());

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static ResultSetMetaData getResultSetMetaData(String sqlStr, JdbcMetaData metaData,
      String currentCatalogName, String currentSchemaName) throws JSQLParserException {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    Statement st = CCJSqlParserUtil.parse(sqlStr);

    if (st instanceof PlainSelect) {
      PlainSelect select = (PlainSelect) st;
      FromItem fromItem = select.getFromItem();
      List<Join> joins = select.getJoins();

      CaseInsensitiveLinkedHashMap<Table> fromTables = new CaseInsensitiveLinkedHashMap<>();
      if (fromItem instanceof Table) {
        Alias alias = fromItem.getAlias();
        Table t = (Table) fromItem;

        if (alias != null) {
          fromTables.put(alias.getName(), (Table) fromItem);
        } else {
          fromTables.put(t.getName(), (Table) fromItem);
        }
      }

      if (joins != null) {
        for (Join join : joins) {
          if (join.getFromItem() instanceof Table) {
            Alias alias = fromItem.getAlias();
            Table t = (Table) join.getFromItem();

            if (alias != null) {
              fromTables.put(alias.getName(), t);
            } else {
              fromTables.put(t.getName(), t);
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
          JdbcColumn jdbcColumn = null;

          Column column = (Column) selectItem.getExpression();
          Alias alias = selectItem.getAlias();

          String columnTablename = null;
          String columnSchemaName = null;
          String columnCatalogName = null;

          Table table = column.getTable();
          if (table != null) {
            columnTablename = table.getName();

            if (table.getSchemaName() != null) {
              columnSchemaName = table.getSchemaName();
            }

            if (table.getDatabase() != null) {
              columnCatalogName = table.getDatabase().getDatabaseName();
            }
          }

          if (columnTablename != null) {
            // column has a table name prefix, which could be the actual table name or the table's
            // alias
            String actualColumnTableName =
                fromTables.containsKey(columnTablename) ? fromTables.get(columnTablename).getName()
                    : null;
            jdbcColumn = metaData.getColumn(columnCatalogName, columnSchemaName,
                actualColumnTableName, column.getColumnName());

            if (jdbcColumn == null) {
              throw new RuntimeException("Column " + column + " not found in tables "
                  + Arrays.deepToString(fromTables.values().toArray()));
            } else {
              resultSetMetaData.add(jdbcColumn,
                  alias != null ? alias.withUseAs(false).toString() : null);
            }
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
            if (jdbcColumn == null) {
              throw new RuntimeException("Column " + column + " not found in tables "
                  + Arrays.deepToString(fromTables.values().toArray()));
            } else {
              resultSetMetaData.add(jdbcColumn,
                  alias != null ? alias.withUseAs(false).toString() : null);
            }
          }
        } else if (selectItem.getExpression() instanceof AllColumns) {
          for (Table t : fromTables.values()) {
            String tableSchemaName = t.getSchemaName();
            String tableCatalogName =
                t.getDatabase() != null ? t.getDatabase().getDatabaseName() : null;

            for (JdbcColumn jdbcColumn : metaData.getTableColumns(tableCatalogName, tableSchemaName,
                t.getName(), null)) {
              resultSetMetaData.add(jdbcColumn, null);
            }
          }
        }
      }

    } else {
      throw new RuntimeException(
          st.getClass().getSimpleName() + " Statements are not supported yet");
    }

    return resultSetMetaData;
  }
}
