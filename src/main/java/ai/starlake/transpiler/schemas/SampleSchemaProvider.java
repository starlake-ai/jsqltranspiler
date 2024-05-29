package ai.starlake.transpiler.schemas;


import java.util.Map;

public class SampleSchemaProvider implements SchemaProvider {
    public Map<String, Map<String, String>> getTables()  {
        return Map.of(
                    "schema1.table1", Map.of(
                        "field1", "type1",
                        "field2", "type2"
                    ),
                    "schema2.table1", Map.of(
                        "field1", "type1",
                        "field2", "type2"
                    )
                );
            }
    public Map<String, String> getTable(String schemaName, String tableName) {
        return Map.of(
                    "field1", "type1",
                    "field2", "type2"
                );
    }
    public Map<String, Map<String, String>> getTables(String tableName) {
        return Map.of(
                    "schema1", Map.of(
                        "field1", "type1",
                        "field2", "type2"
                    ),
                    "schema2", Map.of(
                        "field1", "type1",
                        "field2", "type2"
                    )
                );
    }
}
