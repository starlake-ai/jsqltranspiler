package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;

public class JsonTreeBuilder extends TreeBuilder<String> {
  private StringBuilder jsonBuilder = new StringBuilder();
  private JSQLColumResolver resolver;

  public JsonTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    jsonBuilder.append("  ".repeat(Math.max(0, indent)));
  }

  private void convertNodeToJson(JdbcColumn column, String alias, int indent) {
    addIndentation(indent);
    jsonBuilder.append("{\n");
    addIndentation(indent + 1);
    jsonBuilder.append("\"name\": \"").append(column.columnName).append("\"");

    Expression expression = column.getExpression();
    if (expression instanceof Select) {
      Select select = (Select) expression;
      try {
        jsonBuilder.append(resolver.getLineage(this.getClass(), select));
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
          | IllegalAccessException | SQLException e) {
        throw new RuntimeException(e);
      }
    }

    if (!column.isLeaf()) {
      jsonBuilder.append(",\n");
      addIndentation(indent + 1);
      jsonBuilder.append("\"children\": [\n");

      Enumeration<JdbcColumn> children = column.children();
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
  public String getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    jsonBuilder.append("{ \"columnSet\": [\n");

    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      convertNodeToJson(resultSetMetaData.getColumns().get(i), resultSetMetaData.getLabels().get(i),
          1);
    }

    jsonBuilder.append("\n]}\n");

    return jsonBuilder.toString();
  }
}
