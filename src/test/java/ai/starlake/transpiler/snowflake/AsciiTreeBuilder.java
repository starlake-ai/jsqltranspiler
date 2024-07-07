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

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;

public class AsciiTreeBuilder extends TreeBuilder<String> {
  private JSQLColumResolver resolver;

  public AsciiTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }


  private String getNodeText(JdbcColumn column, String alias) {
    StringBuilder b = new StringBuilder();
    if (alias != null && !alias.isEmpty()) {
      b.append(alias).append(" AS ");
    }

    Expression expression = column.getExpression();
    if (expression instanceof Function) {
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

      if (column.scopeTable != null && !column.scopeTable.isEmpty()) {
        b.append(" → ");
        if (column.scopeCatalog != null && !column.scopeCatalog.isEmpty()) {
          b.append(column.scopeCatalog).append(".")
              .append(column.scopeSchema != null ? column.scopeSchema : "").append(".");
        } else if (column.scopeSchema != null && !column.scopeSchema.isEmpty()) {
          b.append(column.scopeSchema).append(".");
        }
        b.append(column.scopeTable).append(".").append(column.columnName);
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
    } else {
      b.append(expression.getClass().getSimpleName()).append(": ").append(expression);
    }

    return b.toString();
  }

  public SimpleTreeNode translateNode(JdbcColumn column, String alias) {
    String nodeContent = getNodeText(column, alias);

    SimpleTreeNode simpleTreeNode = new SimpleTreeNode(nodeContent);

    Enumeration<JdbcColumn> children = column.children();
    while (children.hasMoreElements()) {
      simpleTreeNode.addChild(translateNode(children.nextElement(), ""));
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
