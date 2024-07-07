package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;

public class XmlTreeBuilder extends TreeBuilder<String> {
  private StringBuilder xmlBuilder = new StringBuilder();
  private JSQLColumResolver resolver;

  public XmlTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    xmlBuilder.append("  ".repeat(Math.max(0, indent)));
  }

  private void convertNodeToXml(JdbcColumn column, String alias, int indent) {
    addIndentation(indent);
    xmlBuilder.append("<Column");

    if (alias != null && !alias.isEmpty()) {
      xmlBuilder.append(" alias='").append(alias).append("'");
    }
    xmlBuilder.append(" name='").append(column.columnName).append("'");

    if (!column.isLeaf()) {
      xmlBuilder.append(">\n");

      Expression expression = column.getExpression();
      if (expression instanceof Select) {
        Select select = (Select) expression;
        try {
          xmlBuilder.append(resolver.getLineage(this.getClass(), select));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
            | IllegalAccessException | SQLException e) {
          throw new RuntimeException(e);
        }
      }

      addIndentation(indent + 1);
      xmlBuilder.append("<Lineage>\n");

      Enumeration<JdbcColumn> children = column.children();
      while (children.hasMoreElements()) {
        JdbcColumn child = children.nextElement();
        convertNodeToXml(child, "", indent + 2);
      }

      addIndentation(indent + 1);
      xmlBuilder.append("</Lineage>\n");

      addIndentation(indent);
      xmlBuilder.append("</Column>\n");
    } else {
      xmlBuilder.append("/>\n");
    }


  }

  @Override
  public String getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xmlBuilder.append("<ColumnSet>\n");

    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      convertNodeToXml(resultSetMetaData.getColumns().get(i), resultSetMetaData.getLabels().get(i),
          1);
    }

    xmlBuilder.append("</ColumnSet>\n");
    return xmlBuilder.toString();
  }
}
