package ai.starlake.transpiler;

public class SchemaNotFoundException extends RuntimeException {
  String SchemaName;

  public SchemaNotFoundException(String SchemaName, Throwable cause) {
    super("Schema not found: " + SchemaName, cause);
    this.SchemaName = SchemaName;
  }

  public SchemaNotFoundException(String SchemaName) {
    super("Schema not found: " + SchemaName);
    this.SchemaName = SchemaName;
  }

  public String getSchemaName() {
    return SchemaName;
  }
}
