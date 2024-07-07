package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;

import java.sql.SQLException;
import java.util.Enumeration;

public class XmlTreeBuilder extends TreeBuilder<String> {
  StringBuilder xmlBuilder = new StringBuilder();

  public XmlTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    xmlBuilder.append("  ".repeat(Math.max(0, indent)));
  }

  private void convertNodeToXml(JdbcColumn node, String alias, int indent) {
    addIndentation(indent);
    xmlBuilder.append("<Column");

    if (alias != null && !alias.isEmpty()) {
      xmlBuilder.append(" alias='").append(alias).append("'");
    }
    xmlBuilder.append(" name='").append(node.toString()).append("'");


    if (!node.isLeaf()) {
      xmlBuilder.append(">\n");

      addIndentation(indent + 1);
      xmlBuilder.append("<Lineage>\n");

      Enumeration<JdbcColumn> children = node.children();
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
  public String getConvertedTree() throws SQLException {
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
