package ai.starlake.transpiler.schemas;

import java.util.Map;

interface SchemaProvider {
  /**
   * Get all tables in the schema
   * 
   * @return Map of tables with schema name and table name as key and map of field name and field
   *         type as value
   */
  Map<String, Map<String, String>> getTables();


  /**
   * Get all fields in the table
   * 
   * @param schemaName schema name
   * @param tableName table name
   * @return Map of field name and field type
   */
  Map<String, String> getTable(String schemaName, String tableName);

  /**
   * Get table regardless of schema name
   * 
   * @param tableName table name
   * @return Map of schema name where the table is found and map of field name and field type.
   *         Returning more than one key means the table is found in multiple schemas and the
   *         resolution is ambiguous. In the future, resolution may be done by jsqltranspiler based
   *         on the context.
   */
  Map<String, Map<String, String>> getTables(String tableName);

}

