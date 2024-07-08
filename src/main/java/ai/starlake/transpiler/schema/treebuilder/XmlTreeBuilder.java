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

public class XmlTreeBuilder extends TreeBuilder<String> {
  private final StringBuilder xmlBuilder = new StringBuilder();
  private JSQLColumResolver resolver;

  public XmlTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  private void addIndentation(int indent) {
    xmlBuilder.append("  ".repeat(Math.max(0, indent)));
  }

  private StringBuilder addIndentation(String input, int indentLevel) {
    StringBuilder result = new StringBuilder();
    String[] lines = input.split("\n");
    if (lines.length <= 1) {
      return result; // No lines or only one line which is removed
    }

    String indent = " ".repeat(indentLevel);
    // skip the first line of the XML declaration
    for (int i = 1; i < lines.length; i++) {
      result.append(indent).append(lines[i]).append("\n");
    }

    // Remove the last newline character
    if (result.length() > 0) {
      result.setLength(result.length() - 1);
    }

    return result;
  }

  private void convertNodeToXml(JdbcColumn column, String alias, int indent) {
    addIndentation(indent);
    xmlBuilder.append("<Column");

    if (alias != null && !alias.isEmpty()) {
      xmlBuilder.append(" alias='").append(alias).append("'");
    }
    xmlBuilder.append(" name='").append(column.columnName).append("'");

    if (column.getExpression() instanceof Column) {
      xmlBuilder.append(" table='").append( JSQLColumResolver.getQualifiedTableName(column.tableCatalog, column.tableSchema, column.tableName) ).append("'");
      if (column.scopeTable!=null && ! column.scopeTable.isEmpty()) xmlBuilder.append(" scope='").append(JSQLColumResolver.getQualifiedColumnName(column.scopeCatalog, column.scopeSchema, column.scopeTable, column.columnName)).append("'");
      xmlBuilder.append(" dataType='java.sql.Types.").append(JdbcMetaData.getTypeName(column.dataType)).append("'");
      xmlBuilder.append(" typeName='").append( column.typeName ).append("'");
      xmlBuilder.append(" columnSize='").append( column.columnSize ).append("'");
      xmlBuilder.append(" decimalDigits='").append( column.decimalDigits ).append("'");
      xmlBuilder.append(" nullable='").append( column.isNullable ).append("'");
    }

    Expression expression = column.getExpression();
    if (expression instanceof Select) {
      xmlBuilder.append(">\n");

      Select select = (Select) expression;
      try {
        xmlBuilder.append(addIndentation(resolver.getLineage(this.getClass(), select), indent + 2)).append("\n");
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
               | IllegalAccessException | SQLException e) {
        throw new RuntimeException(e);
      }
      addIndentation(indent);
      xmlBuilder.append("</Column>\n");
    } else if (!column.isLeaf()) {
      xmlBuilder.append(">\n");
      addIndentation(indent + 1);
      xmlBuilder.append("<ColumnSet>\n");

      Enumeration<JdbcColumn> children = column.children();
      while (children.hasMoreElements()) {
        JdbcColumn child = children.nextElement();
        convertNodeToXml(child, "", indent + 2);
      }

      addIndentation(indent + 1);
      xmlBuilder.append("</ColumnSet>\n");

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
