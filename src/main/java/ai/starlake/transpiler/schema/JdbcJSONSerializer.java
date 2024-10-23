/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler.schema;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JdbcJSONSerializer {

  public static void toJson(JdbcMetaData metadata, Writer out, int indent) {
    toJson(metadata).write(out, indent, 0);
  }

  public static void toJson(JdbcMetaData metadata, Writer out) {
    toJson(metadata).write(out);
  }

  public static JSONObject toJson(JdbcMetaData metadata) {
    JSONObject metadataObject = new JSONObject();
    metadataObject.put("databaseType", metadata.getDatabaseType());
    metadataObject.put("currentCatalog", metadata.getCurrentCatalogName());
    metadataObject.put("currentSchema", metadata.getCurrentSchemaName());
    metadataObject.put("catalogSeparator", metadata.getCatalogSeparator());

    JSONArray catalogsArray = new JSONArray();

    for (JdbcCatalog catalog : metadata.getCatalogsList()) {
      catalogsArray.put(toJson(catalog));
    }
    metadataObject.put("catalogs", catalogsArray);

    return metadataObject;

  }

  public static JdbcMetaData fromJson(Reader in) {
    JSONObject json = new JSONObject(new JSONTokener(in));

    JdbcMetaData metadata =
        new JdbcMetaData(json.getString("currentCatalog"), json.getString("currentSchema"));
    metadata.setDatabaseType(json.getString("databaseType"));
    metadata.setCatalogSeparator(json.getString("catalogSeparator"));

    JSONArray jsonCatalogs = json.getJSONArray("catalogs");

    List<JdbcCatalog> catalogs = new ArrayList<JdbcCatalog>(jsonCatalogs.length());
    for (int i = 0; i < jsonCatalogs.length(); i++) {
      catalogs.add(fromJsonCatalog(jsonCatalogs.getJSONObject(i)));
    }

    metadata.setCatalogsList(catalogs);
    return metadata;

  }

  protected static JSONObject toJson(JdbcCatalog catalog) {
    JSONObject catalogObject = new JSONObject();
    catalogObject.put("name", catalog.tableCatalog);
    catalogObject.put("separator", catalog.catalogSeparator);

    JSONArray schemasArray = new JSONArray();

    for (JdbcSchema schema : catalog.schemas.values()) {
      schemasArray.put(toJson(schema));
    }
    catalogObject.put("schemas", schemasArray);

    return catalogObject;
  }

  protected static JSONObject toJson(JdbcSchema schema) {
    JSONObject schemaObject = new JSONObject();
    schemaObject.put("name", schema.tableSchema);

    JSONArray tablesArray = new JSONArray();

    for (JdbcTable table : schema.tables.values()) {
      tablesArray.put(toJson(table));
    }

    schemaObject.put("tables", tablesArray);

    return schemaObject;

  }

  protected static JSONObject toJson(JdbcTable table) {
    JSONObject tableObject = new JSONObject();
    tableObject.put("name", table.getTableName());
    tableObject.put("type", table.getTableType());

    JSONArray columnsArray = new JSONArray();

    for (JdbcColumn column : table.columns.values()) {
      columnsArray.put(toJson(column));
    }

    tableObject.put("columns", columnsArray);

    return tableObject;
  }

  protected static JSONObject toJson(JdbcColumn column) {
    JSONObject columnObject = new JSONObject();
    columnObject.put("name", column.columnName);
    columnObject.put("type", column.typeName);
    columnObject.put("typeID", column.dataType);
    columnObject.put("size", column.columnSize);
    if (column.decimalDigits != null) {
      columnObject.put("decimalDigits", column.decimalDigits);
    }
    columnObject.put("isNullable", column.isNullable.equalsIgnoreCase("YES"));

    return columnObject;
  }

  protected static JdbcCatalog fromJsonCatalog(JSONObject json) {
    final String catalogName = json.getString("name");
    JdbcCatalog catalog = new JdbcCatalog(catalogName, json.getString("separator"));

    JSONArray jsonSchemas = json.getJSONArray("schemas");

    List<JdbcSchema> schemas = new ArrayList<JdbcSchema>(jsonSchemas.length());
    for (int i = 0; i < jsonSchemas.length(); i++) {
      schemas.add(fromJsonSchema(jsonSchemas.getJSONObject(i)));
    }
    catalog.setSchemas(schemas);
    return catalog;
  }

  protected static JdbcSchema fromJsonSchema(JSONObject json) {
    JdbcSchema schema = new JdbcSchema();
    schema.setSchemaName(json.getString("name"));

    JSONArray jsonTables = json.getJSONArray("tables");

    List<JdbcTable> tables = new ArrayList<JdbcTable>(jsonTables.length());
    for (int i = 0; i < jsonTables.length(); i++) {
      tables.add(fromJsonTable(jsonTables.getJSONObject(i)));
    }

    schema.setTables(tables);

    return schema;

  }

  protected static JdbcTable fromJsonTable(JSONObject json) {
    JdbcTable table = new JdbcTable();
    table.setTableName(json.getString("name"));
    table.setTableType(json.getString("type"));

    JSONArray jsonColumns = json.getJSONArray("columns");

    List<JdbcColumn> columns = new ArrayList<JdbcColumn>(jsonColumns.length());
    for (int i = 0; i < jsonColumns.length(); i++) {
      columns.add(fromJsonColumn(jsonColumns.getJSONObject(i)));
    }

    table.setColumns(columns);

    return table;

  }

  protected static JdbcColumn fromJsonColumn(JSONObject json) {
    JdbcColumn column = new JdbcColumn(json.getString("name"));
    column.typeName = json.getString("type");
    column.dataType = json.getInt("typeID");
    column.columnSize = json.getInt("size");
    column.isNullable = json.getBoolean("isNullable") ? "YES" : "NO";
    if (json.has("decimalDigits")) {
      column.decimalDigits = json.getInt("decimalDigits");
    }

    return column;
  }

}
