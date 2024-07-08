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
import java.util.Enumeration;

public class JsonTreeBuilder extends TreeBuilder<String> {
  private final StringBuilder jsonBuilder = new StringBuilder();
  private JSQLColumResolver resolver;

  public JsonTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    jsonBuilder.append(" ".repeat(Math.max(0, indent)));
  }

  private StringBuilder addIndentation(String input, int indentLevel) {
    StringBuilder result = new StringBuilder();
    String[] lines = input.split("\n");
    if (lines.length <= 1) {
      return result; // No lines or only one line which is removed
    }

    String indent = " ".repeat(indentLevel);
    // don't indent the first line
    result.append(lines[0]).append("\n");
    for (int i = 1; i < lines.length; i++) {
      result.append(indent).append(lines[i]).append("\n");
    }

    // Remove the last newline character
    if (result.length() > 0) {
      result.setLength(result.length() - 1);
    }

    return result;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  private void convertNodeToJson(JdbcColumn column, String alias, int indent) {
    addIndentation(indent);
    jsonBuilder.append("{\n");

    addIndentation(indent + 2);
    jsonBuilder.append("\"name\": \"").append(column.columnName).append("\"");

    if (alias != null && !alias.isEmpty()) {
      jsonBuilder.append(",\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"alias\": \"").append(alias).append("\"");
    }

    if (column.getExpression() instanceof Column) {
      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder
          .append("\"table\": \"").append(JSQLColumResolver
              .getQualifiedTableName(column.tableCatalog, column.tableSchema, column.tableName))
          .append("\",");

      if (column.scopeTable != null && !column.scopeTable.isEmpty()) {
        jsonBuilder.append("\n");
        addIndentation(indent + 2);
        jsonBuilder.append("\"scope\": \"")
            .append(JSQLColumResolver.getQualifiedColumnName(column.scopeCatalog,
                column.scopeSchema, column.scopeTable, column.columnName))
            .append("\",");
      }

      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"dataType\": \"java.sql.Types.")
          .append(JdbcMetaData.getTypeName(column.dataType)).append("\",");

      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"typeName\": \"").append(column.typeName).append("\",");

      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"columnSize\": ").append(column.columnSize).append(",");

      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"decimalDigits\": ").append(column.decimalDigits).append(",");

      jsonBuilder.append("\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"nullable\": ").append(column.isNullable);
    }

    Expression expression = column.getExpression();
    if (expression instanceof Select) {
      jsonBuilder.append(",\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"subquery\": ");

      Select select = (Select) expression;
      try {
        jsonBuilder
            .append(addIndentation(resolver.getLineage(this.getClass(), select), indent + 2));
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
          | IllegalAccessException | SQLException e) {
        throw new RuntimeException(e);
      }
      jsonBuilder.append("\n");
      addIndentation(indent);
    } else if (!column.isLeaf()) {
      jsonBuilder.append(",\n");
      addIndentation(indent + 2);
      jsonBuilder.append("\"columnSet\": [\n");

      Enumeration<JdbcColumn> children = column.children();
      while (children.hasMoreElements()) {
        JdbcColumn child = children.nextElement();
        convertNodeToJson(child, "", indent + 4);
        if (children.hasMoreElements()) {
          jsonBuilder.append(",");
        }
        jsonBuilder.append("\n");
      }

      addIndentation(indent + 2);
      jsonBuilder.append("]\n");
      addIndentation(indent);
    } else {
      jsonBuilder.append("\n");
      addIndentation(indent);
    }
    jsonBuilder.append("}");
  }

  @Override
  public String getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    jsonBuilder.append("{\n");
    jsonBuilder.append("  \"columnSet\": [\n");

    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      convertNodeToJson(resultSetMetaData.getColumns().get(i), resultSetMetaData.getLabels().get(i),
          4);
      if (i < resultSetMetaData.getColumnCount() - 1) {
        jsonBuilder.append(",");
      }
      jsonBuilder.append("\n");
    }

    jsonBuilder.append("  ]\n");
    jsonBuilder.append("}\n");

    return jsonBuilder.toString();
  }
}
