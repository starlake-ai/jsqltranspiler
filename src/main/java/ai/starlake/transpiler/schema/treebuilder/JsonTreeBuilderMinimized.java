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
package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * Concise/minimized version of output generated by JsonTreeBuilder. Useful when the output needs to
 * be transported somewhere or parsed back into POJO.
 * 
 * @see ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilder
 * 
 * 
 */
public class JsonTreeBuilderMinimized extends TreeBuilder<String> {
  private final StringBuilder jsonBuilder = new StringBuilder();
  private JSQLColumResolver resolver;

  public JsonTreeBuilderMinimized(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  private void convertNodeToJson(JdbcColumn column, String alias) {
    jsonBuilder.append("{");

    jsonBuilder.append("\"name\":\"").append(column.columnName).append("\",");

    if (alias != null && !alias.isEmpty()) {
      jsonBuilder.append(",");
      jsonBuilder.append("\"alias\":\"").append(alias).append("\"");
    }

    if (column.getExpression() instanceof Column) {
      jsonBuilder
          .append("\"table\":\"").append(JSQLColumResolver
              .getQualifiedTableName(column.tableCatalog, column.tableSchema, column.tableName))
          .append("\",");

      if (column.scopeTable != null && !column.scopeTable.isEmpty()) {
        jsonBuilder.append("\"scope\":\"")
            .append(JSQLColumResolver.getQualifiedColumnName(column.scopeCatalog,
                column.scopeSchema, column.scopeTable, column.scopeColumn))
            .append("\",");
      }

      jsonBuilder.append("\"dataType\":\"java.sql.Types.")
          .append(JdbcMetaData.getTypeName(column.dataType)).append("\",");

      jsonBuilder.append("\"typeName\":\"").append(column.typeName).append("\",");

      jsonBuilder.append("\"columnSize\":").append(column.columnSize).append(",");

      jsonBuilder.append("\"decimalDigits\":").append(column.decimalDigits).append(",");

      jsonBuilder.append("\"nullable\":").append(column.isNullable.equalsIgnoreCase("YES"));
    }

    Expression expression = column.getExpression();
    if (expression instanceof Select) {
      jsonBuilder.append(",\"subquery\": ");

      Select select = (Select) expression;
      try {
        jsonBuilder.append(resolver.getLineage(this.getClass(), select));
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
          | IllegalAccessException | SQLException e) {
        throw new RuntimeException(e);
      }
    } else if (!column.getChildren().isEmpty()) {
      jsonBuilder.append(",\"columnSet\":[");

      boolean first = true;
      for (JdbcColumn child : column.getChildren()) {
        convertNodeToJson(child, "");
        if (!first) {
          first = false;
          jsonBuilder.append(",");
        }
      }
      jsonBuilder.append("]");
    }
    jsonBuilder.append("}");
  }

  @Override
  public String getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    jsonBuilder.append("{\"columnSet\":[");

    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      convertNodeToJson(resultSetMetaData.getColumns().get(i),
          resultSetMetaData.getLabels().get(i));
      if (i < resultSetMetaData.getColumnCount() - 1) {
        jsonBuilder.append(",");
      }
    }

    jsonBuilder.append("]}");

    return jsonBuilder.toString();
  }
}
