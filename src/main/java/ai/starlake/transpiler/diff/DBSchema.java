package ai.starlake.transpiler.diff;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;

import java.util.Arrays;
import java.util.Collection;

public class DBSchema {
  String catalogName;
  String schemaName;
  CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables;

  public DBSchema(String catalogName, String schemaName) {
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
  }

  public DBSchema(String catalogName, String schemaName,
      CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables) {
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.tables = tables;
  }

  public DBSchema(String catalogName, String schemaName, String tableName,
      Collection<Attribute> attributes) {
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
    this.tables.put(tableName, attributes);
  }

  public DBSchema(String catalogName, String schemaName, String tableName,
      Attribute... attributes) {
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
    this.tables.put(tableName, Arrays.asList(attributes));
  }

  public DBSchema put(String tableName, Attribute... attributes) {
    this.tables.put(tableName, Arrays.asList(attributes));
    return this;
  }
}
