package ai.starlake.transpiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ColumnNotFoundException extends RuntimeException {
  String columnName;
  Set<String> tableNames = new HashSet<>();

  public ColumnNotFoundException(String columnName, Collection<String> tableNames,
      Throwable cause) {
    super("Column " + columnName + " not found in " + Arrays.deepToString(tableNames.toArray()),
        cause);
    this.columnName = columnName;
    this.tableNames.addAll(tableNames);
  }

  public ColumnNotFoundException(String columnName, Collection<String> tableNames) {
    super("Column " + columnName + " not found in " + Arrays.deepToString(tableNames.toArray()));
    this.columnName = columnName;
    this.tableNames.addAll(tableNames);
  }

  public String getColumnName() {
    return columnName;
  }

  public Set<String> getTableNames() {
    return tableNames;
  }
}
