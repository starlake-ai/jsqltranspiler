package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;

import java.sql.SQLException;
import java.util.Enumeration;

public class JsonTreeBuilder extends TreeBuilder<String> {
  StringBuilder jsonBuilder = new StringBuilder();

  public JsonTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    jsonBuilder.append("  ".repeat(Math.max(0, indent)));
  }

  private void convertNodeToJson(JdbcColumn node, String alias, int indent) {
    addIndentation(indent);
    jsonBuilder.append("{\n");
    addIndentation(indent + 1);
    jsonBuilder.append("\"name\": \"").append(node.toString()).append("\"");

    if (!node.isLeaf()) {
      jsonBuilder.append(",\n");
      addIndentation(indent + 1);
      jsonBuilder.append("\"children\": [\n");

      Enumeration<JdbcColumn> children = node.children();
      while (children.hasMoreElements()) {
        JdbcColumn child = children.nextElement();
        convertNodeToJson(child, alias, indent + 2);
        if (children.hasMoreElements()) {
          jsonBuilder.append(",\n");
        }
      }
      jsonBuilder.append("\n");
      addIndentation(indent + 1);
      jsonBuilder.append("]");
    }

    jsonBuilder.append("\n");
    addIndentation(indent);
    jsonBuilder.append("}");
  }

  @Override
  public String getConvertedTree() throws SQLException {
    jsonBuilder.append("{ \"columnSet\": [\n");

    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      convertNodeToJson(resultSetMetaData.getColumns().get(i), resultSetMetaData.getLabels().get(i),
          1);
    }

    jsonBuilder.append("\n]}\n");

    return jsonBuilder.toString();
  }
}
