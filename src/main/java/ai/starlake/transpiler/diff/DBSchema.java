package ai.starlake.transpiler.diff;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;

import java.util.Arrays;
import java.util.Collection;

public class DBSchema {
  String catalogName;
  String schemaName;
  CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables;

  public DBSchema(String catalogName, String schemaName) {
    this.catalogName = catalogName == null ? "" : catalogName;
    this.schemaName = schemaName == null ? "" : schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
  }

  public DBSchema(String catalogName, String schemaName,
      CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables) {
    this.catalogName = catalogName == null ? "" : catalogName;
    this.schemaName = schemaName == null ? "" : schemaName;
    this.tables = tables;
  }

  public DBSchema(String catalogName, String schemaName, String tableName,
      Collection<Attribute> attributes) {
    this.catalogName = catalogName == null ? "" : catalogName;
    this.schemaName = schemaName == null ? "" : schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
    this.tables.put(tableName, attributes);
  }

  public DBSchema(String catalogName, String schemaName, String tableName,
      Attribute... attributes) {
    this.catalogName = catalogName == null ? "" : catalogName;
    this.schemaName = schemaName == null ? "" : schemaName;
    this.tables = new CaseInsensitiveLinkedHashMap<>();
    this.tables.put(tableName, Arrays.asList(attributes));
  }

  public DBSchema put(String tableName, Attribute... attributes) {
    this.tables.put(tableName, Arrays.asList(attributes));
    return this;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public DBSchema setCatalogName(String catalogName) {
    this.catalogName = catalogName;
    return this;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public DBSchema setSchemaName(String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

  public CaseInsensitiveLinkedHashMap<Collection<Attribute>> getTables() {
    return tables;
  }

  public DBSchema setTables(CaseInsensitiveLinkedHashMap<Collection<Attribute>> tables) {
    this.tables = tables;
    return this;
  }
}
