package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.JSQLColumResolver;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FlattenedColumnBuilder extends TreeBuilder<Map<String, Set<String>>> {
  private JSQLColumResolver resolver;

  public FlattenedColumnBuilder(JdbcResultSetMetaData resultSetMetaData) {
    super(resultSetMetaData);
  }

  /**
   * Recursively collects all leaf node column names from the dependency tree. A leaf node is a
   * column from a physical table (base table).
   *
   * @param column The column to traverse
   * @param dependencies List to collect the leaf column names
   */
  private void collectLeafDependencies(JdbcColumn column, LinkedHashSet<String> dependencies)
      throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException,
      IllegalAccessException {
    Expression expression = column.getExpression();

    // If it's a subquery, resolve it and collect its dependencies
    if (expression instanceof Select) {
      Select select = (Select) expression;
      JdbcResultSetMetaData subqueryMetaData = resolver.getResultSetMetaData(select);

      // Recursively collect dependencies from all columns in the subquery
      for (JdbcColumn subColumn : subqueryMetaData.getColumns()) {
        collectLeafDependencies(subColumn, dependencies);
      }
    } else if (!column.getChildren().isEmpty()) {
      for (JdbcColumn child : column.getChildren()) {
        collectLeafDependencies(child, dependencies);
      }
    } else {
      // Leaf nodes have expression == null and have scopeTable set to the actual base table
      // The scopeTable contains the real table, while tableName might be a CTE/derived table
      if (column.scopeTable != null && !column.scopeTable.isEmpty()) {
        // Use scopeCatalog, scopeSchema, scopeTable for the actual base table reference
        String qualifiedColumnName = JSQLColumResolver.getQualifiedColumnName(column.scopeCatalog,
            column.scopeSchema, column.scopeTable, column.columnName);

        // Only add if not empty and not already present (avoid duplicates)
        if (qualifiedColumnName != null && !qualifiedColumnName.isEmpty()
            && !dependencies.contains(qualifiedColumnName)) {
          dependencies.add(qualifiedColumnName);
        }
      }
    }
  }

  @Override
  public Map<String, Set<String>> getConvertedTree(JSQLColumResolver resolver) throws SQLException {
    this.resolver = resolver;

    Map<String, Set<String>> dependencyMap = new LinkedHashMap<>();

    // Process each column in the result set
    for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
      JdbcColumn column = resultSetMetaData.getColumns().get(i);
      String columnLabel = resultSetMetaData.getLabels().get(i);

      // Use the alias/label as the key, fall back to column name if no label
      String key =
          (columnLabel != null && !columnLabel.isEmpty()) ? columnLabel : column.columnName;

      LinkedHashSet<String> dependencies = new LinkedHashSet<>();

      try {
        collectLeafDependencies(column, dependencies);
      } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
          | IllegalAccessException e) {
        throw new RuntimeException("Failed to collect dependencies for column: " + key, e);
      }

      dependencyMap.put(key, dependencies);
    }

    return dependencyMap;
  }
}
