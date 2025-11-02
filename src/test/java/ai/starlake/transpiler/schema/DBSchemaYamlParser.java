package ai.starlake.transpiler.schema;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DBSchemaYamlParser {

  /**
   * Reads a YAML file and converts it to a list of DBSchema objects.
   *
   * @param yamlFilePath the path to the YAML file
   * @return a list of DBSchema objects
   * @throws Exception if there's an error reading or parsing the file
   */
  public static List<DBSchema> readYamlToDBSchemas(String yamlFilePath) throws Exception {
    try (InputStream inputStream = new FileInputStream(yamlFilePath)) {
      return readYamlToDBSchemas(inputStream);
    }
  }

  /**
   * Reads a YAML input stream and converts it to a list of DBSchema objects.
   *
   * @param inputStream the YAML input stream
   * @return a list of DBSchema objects
   */
  public static List<DBSchema> readYamlToDBSchemas(InputStream inputStream) {
    Yaml yaml = new Yaml();
    List<Map<String, Object>> schemasList = yaml.load(inputStream);

    List<DBSchema> dbSchemas = new ArrayList<>();

    for (Map<String, Object> schemaMap : schemasList) {
      DBSchema dbSchema = parseSchema(schemaMap);
      dbSchemas.add(dbSchema);
    }

    return dbSchemas;
  }

  /**
   * Parses a single schema map into a DBSchema object.
   */
  private static DBSchema parseSchema(Map<String, Object> schemaMap) {
    String schemaName = (String) schemaMap.get("schemaName");
    Map<String, List<Map<String, Object>>> tablesMap =
        (Map<String, List<Map<String, Object>>>) schemaMap.get("tables");

    CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables =
        new CaseInsensitiveLinkedHashMap<>();

    if (tablesMap != null) {
      for (Map.Entry<String, List<Map<String, Object>>> tableEntry : tablesMap.entrySet()) {
        String tableName = tableEntry.getKey();
        List<Map<String, Object>> attributesList = tableEntry.getValue();

        Collection<Attribute> attributes = parseAttributes(attributesList);
        tables.put(tableName, attributes);
      }
    }

    return new DBSchema(null, schemaName, tables);
  }

  /**
   * Parses a list of attribute maps into a collection of Attribute objects.
   */
  private static Collection<Attribute> parseAttributes(List<Map<String, Object>> attributesList) {
    List<Attribute> attributes = new ArrayList<>();

    for (Map<String, Object> attrMap : attributesList) {
      Attribute attribute = parseAttribute(attrMap);
      attributes.add(attribute);
    }

    return attributes;
  }

  /**
   * Parses a single attribute map into an Attribute object.
   */
  private static Attribute parseAttribute(Map<String, Object> attrMap) {
    String name = (String) attrMap.get("name");
    String type = (String) attrMap.get("type");
    Boolean array = (Boolean) attrMap.get("array");
    boolean isArray = array != null && array;

    String statusStr = (String) attrMap.get("status");
    AttributeStatus status =
        statusStr != null ? AttributeStatus.valueOf(statusStr) : AttributeStatus.UNCHANGED;

    // Handle nested fields if present
    Boolean nestedField = (Boolean) attrMap.get("nestedField");
    Collection<Attribute> nestedAttributes = null;

    if (nestedField != null && nestedField) {
      List<Map<String, Object>> nestedAttrList =
          (List<Map<String, Object>>) attrMap.get("attributes");
      if (nestedAttrList != null) {
        nestedAttributes = parseAttributes(nestedAttrList);
      }
    }

    return new Attribute(name, type, isArray, nestedAttributes, status);
  }

  /**
   * Example usage
   */
  public static void main(String[] args) {
    try {
      List<DBSchema> schemas = readYamlToDBSchemas("JSQLResolverTest.yml");

      System.out.println("Loaded " + schemas.size() + " schemas");

      for (DBSchema schema : schemas) {
        System.out.println("\nSchema: " + schema.getSchemaName());
        System.out.println("Tables: " + schema.getTables().size());

        for (Map.Entry<String, Collection<Attribute>> entry : schema.getTables().entrySet()) {
          System.out.println(
              "  Table: " + entry.getKey() + " (" + entry.getValue().size() + " attributes)");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
