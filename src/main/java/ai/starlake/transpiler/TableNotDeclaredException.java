package ai.starlake.transpiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableNotDeclaredException extends RuntimeException {
  String tableName;
  Set<String> tableNames = new HashSet<>();

  public TableNotDeclaredException(String tableName, Collection<String> tableNames,
      Throwable cause) {
    super("Table " + tableName + " not declared in " + Arrays.deepToString(tableNames.toArray()),
        cause);
    this.tableName = tableName;
    this.tableNames.addAll(tableNames);
  }

  public TableNotDeclaredException(String tableName, Collection<String> tableNames) {
    super("Table " + tableName + " not declared in " + Arrays.deepToString(tableNames.toArray()));
    this.tableName = tableName;
    this.tableNames.addAll(tableNames);
  }

  public String getTableName() {
    return tableName;
  }
}
