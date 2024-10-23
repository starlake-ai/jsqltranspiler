/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
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
package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.treebuilder.TreeBuilder;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class AsciiTreeBuilder extends TreeBuilder<String> {
  private JSQLColumResolver resolver;

  public AsciiTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }


  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  private String getNodeText(JdbcColumn column, String alias) {
    StringBuilder b = new StringBuilder();
    if (alias != null && !alias.isEmpty()) {
      b.append(alias).append(" AS ");
    }

    Expression expression = column.getExpression();
    if (expression instanceof Function) {
      if (!StringUtils.isEmpty(column.columnName) && column.getChildren().size() > 1) {
        if (column.tableCatalog != null && !column.tableCatalog.isEmpty()) {
          b.append(column.tableCatalog).append(".")
              .append(column.tableSchema != null ? column.tableSchema : "").append(".");
        } else if (column.tableSchema != null && !column.tableSchema.isEmpty()) {
          b.append(column.tableSchema).append(".");
        }
        b.append(column.tableName).append(".").append(column.columnName);
        b.append(" AS ");
      }

      Function f = (Function) expression;
      b.append("Function ").append(f.getName());
    } else if (expression instanceof Select) {
      Select select = (Select) expression;
      try {
        b.append(resolver.getLineage(this.getClass(), select));
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
          | IllegalAccessException | SQLException e) {
        throw new RuntimeException(e);
      }

    } else if (expression instanceof Column) {

      if (column.tableCatalog != null && !column.tableCatalog.isEmpty()) {
        b.append(column.tableCatalog).append(".")
            .append(column.tableSchema != null ? column.tableSchema : "").append(".");
      } else if (column.tableSchema != null && !column.tableSchema.isEmpty()) {
        b.append(column.tableSchema).append(".");
      }
      b.append(column.tableName).append(".").append(column.columnName);

      if (!StringUtils.equals(column.tableCatalog, column.scopeCatalog)
          || !StringUtils.equals(column.tableSchema, column.scopeSchema)
          || !StringUtils.equals(column.tableName, column.scopeTable)) {
        b.append(" → ");
        if (column.scopeCatalog != null && !column.scopeCatalog.isEmpty()) {
          b.append(column.scopeCatalog).append(".")
              .append(column.scopeSchema != null ? column.scopeSchema : "").append(".");
        } else if (column.scopeSchema != null && !column.scopeSchema.isEmpty()) {
          b.append(column.scopeSchema).append(".");
        }

        if (!StringUtils.isEmpty(column.scopeTable)) {
          b.append(column.scopeTable).append(".");
        }

        b.append(column.columnName);
      }

      b.append(" : ").append(column.typeName);

      if (column.columnSize > 0) {
        b.append("(").append(column.columnSize);
        if (column.decimalDigits > 0) {
          b.append(", ").append(column.decimalDigits);
        }
        b.append(")");
      }

      return b.toString();
    } else if (expression != null) {
      if (!StringUtils.isEmpty(column.tableName)) {
        if (column.tableCatalog != null && !column.tableCatalog.isEmpty()) {
          b.append(column.tableCatalog).append(".")
              .append(column.tableSchema != null ? column.tableSchema : "").append(".");
        } else if (column.tableSchema != null && !column.tableSchema.isEmpty()) {
          b.append(column.tableSchema).append(".");
        }
        b.append(column.tableName).append(".").append(column.columnName);
        b.append(" AS ");
      }

      b.append(expression.getClass().getSimpleName()).append(": ").append(expression);
    } else {
      b.append("unresolvable");
    }

    return b.toString();
  }

  public SimpleTreeNode translateNode(JdbcColumn column, String alias) {
    String nodeContent = getNodeText(column, alias);

    SimpleTreeNode simpleTreeNode = new SimpleTreeNode(nodeContent);

    for (JdbcColumn child : column.getChildren()) {
      simpleTreeNode.addChild(translateNode(child, ""));
    }
    return simpleTreeNode;
  }

  @Override
  public String getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    // Define your own Tree based on your own TreeNode interface
    SimpleTreeNode rootNode = new SimpleTreeNode("SELECT");
    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {

      // Add each columns lineage tree as node to the root with a translation from Swing's TreeNode
      rootNode.addChild(translateNode(resultSetMetaData.getColumns().get(i),
          resultSetMetaData.getLabels().get(i)));
    }

    return new ListingTreePrinter().stringify(rootNode);
  }

}
