package ai.starlake.transpiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableNotFoundException extends RuntimeException {
  String tableName;
  Set<String> schemaNames = new HashSet<>();

  public TableNotFoundException(String tableName, Collection<String> schemaNames, Throwable cause) {
    super(
        "Table " + tableName + " not found in schema " + Arrays.deepToString(schemaNames.toArray()),
        cause);
    this.tableName = tableName;
    this.schemaNames.addAll(schemaNames);
  }

  public TableNotFoundException(String tableName, Collection<String> schemaNames) {
    super("Table " + tableName + " not found in schema "
        + Arrays.deepToString(schemaNames.toArray()));
    this.tableName = tableName;
    this.schemaNames.addAll(schemaNames);
  }

  public String getTableName() {
    return tableName;
  }
}
