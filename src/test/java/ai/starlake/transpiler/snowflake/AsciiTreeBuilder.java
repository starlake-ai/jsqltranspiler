package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.treebuilder.TreeBuilder;
import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter;

import java.sql.SQLException;
import java.util.Enumeration;

public class AsciiTreeBuilder extends TreeBuilder<String> {
  public AsciiTreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  public static SimpleTreeNode translateNode(JdbcColumn node, String alias) {
    String nodeContent =
        ((alias != null && !alias.isEmpty()) ? alias + " AS " : "") + node.toString();

    SimpleTreeNode simpleTreeNode = new SimpleTreeNode(nodeContent);

    Enumeration<JdbcColumn> children = node.children();
    while (children.hasMoreElements()) {
      simpleTreeNode.addChild(translateNode(children.nextElement(), ""));
    }
    return simpleTreeNode;
  }

  @Override
  public String getConvertedTree() throws SQLException {

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
