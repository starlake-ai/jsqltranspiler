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

import ai.starlake.transpiler.schema.JdbcCatalog;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcSchema;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.sql.ResultSetMetaData;
import java.util.List;

public class JSQLColumResolver {

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static ResultSetMetaData getResultSetMetaData(String sqlStr, JdbcMetaData metaData,
      String currentCatalogName, String currentSchemaName) throws JSQLParserException {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    Statement st = CCJSqlParserUtil.parse(sqlStr);

    if (st instanceof PlainSelect) {
      PlainSelect select = (PlainSelect) st;
      FromItem fromItem = select.getFromItem();
      List<Join> joins = select.getJoins();

      // column positions in MetaData start at 1
      int position = 1;
      for (SelectItem<?> selectItem : select.getSelectItems()) {

        if (selectItem.getExpression() instanceof Column) {
          Column column = (Column) selectItem.getExpression();
          Alias alias = selectItem.getAlias();

          String tablename = null;
          String schemaName = currentSchemaName;
          String catalogName = currentCatalogName;

          Table table = column.getTable();
          if (table != null) {
            tablename = table.getName();

            if (table.getSchemaName() != null) {
              schemaName = table.getSchemaName();
            }

            if (table.getDatabase() != null) {
              catalogName = table.getDatabase().getDatabaseName();
            }
          }

          JdbcCatalog jdbcCatalog = metaData.getCatalogs().get(catalogName);
          if (jdbcCatalog == null) {
            throw new RuntimeException(
                "Catalog " + catalogName + " does not exist in the DatabaseMetaData.");
          }

          JdbcSchema jdbcSchema = jdbcCatalog.get(schemaName);
          if (jdbcSchema == null) {
            throw new RuntimeException(
                "Schema " + schemaName + " does not exist in the given Catalog " + catalogName);
          }

          if (tablename != null) {
            JdbcTable jdbcTable = jdbcSchema.get(tablename);
            if (jdbcTable == null) {
              throw new RuntimeException(
                  "Table " + tablename + " does not exist in the given Schema " + schemaName);
            } else {
              JdbcColumn jdbcColumn = jdbcTable.jdbcColumns.get(column.getColumnName());
              resultSetMetaData.add(jdbcColumn,
                  alias != null ? alias.withUseAs(false).toString() : null);
              throw new RuntimeException(
                  "Column " + column + " does not exist in the given Table " + tablename);
            }
          }

          String tableCatalog = "";
          String tableSchema = "";
          String tableName = "";
          String columnName = "";
          Integer dataType = 0;
          String typeName = "";
          Integer columnSize = 0;
          Integer decimalDigits = 0;
          Integer numericPrecisionRadix = 0;
          Integer nullable = ResultSetMetaData.columnNullable;
          String remarks = "";
          String columnDefinition = "";
          Integer characterOctetLength = 0;
          Integer ordinalPosition = ++position;
          String isNullable = "YES";
          String scopeCatalog = "";
          String scopeSchema = "";
          String scopeTable = "";
          Short sourceDataType = 0;
          String isAutomaticIncrement = "NO";
          String isGeneratedColumn = "NO";

          JdbcColumn jdbcColumn =
              new JdbcColumn(tableCatalog, tableSchema, tableName, columnName, dataType, typeName,
                  columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks,
                  columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog,
                  scopeSchema, scopeTable, sourceDataType, isAutomaticIncrement, isGeneratedColumn);

          resultSetMetaData.add(jdbcColumn, alias != null ? alias.getName() : null);
        }
      }

    } else {
      throw new RuntimeException(
          st.getClass().getSimpleName() + " Statements are not supported yet");
    }

    return resultSetMetaData;
  }
}
