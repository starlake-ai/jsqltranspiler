/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com> on behalf of Starlake.AI
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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class JdbcMetaData {
  public final static Logger LOGGER = Logger.getLogger(JdbcMetaData.class.getName());

  private final CaseInsensitiveLinkedHashMap<JdbcCatalog> catalogs =
      new CaseInsensitiveLinkedHashMap<>();
  private final DatabaseMetaData metaData;
  private final String currentCatalogName;
  private final String currentSchemaName;

  private static final Map<Integer, String> SQL_TYPE_NAME_MAP = new HashMap<>();

  static {
    for (Field field : Types.class.getFields()) {
      try {
        if (field.getType() == int.class) {
          SQL_TYPE_NAME_MAP.put(field.getInt(null), field.getName());
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Failed to initialize SQL type map", e);
      }
    }
  }

  public static String getTypeName(int sqlType) {
    return SQL_TYPE_NAME_MAP.getOrDefault(sqlType, "UNKNOWN");
  }

  public JdbcMetaData(Connection con) throws SQLException {
    // todo: customise this for various databases, e. g. Oracle would need a "FROM DUAL"
    try (Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("SELECT current_database(), current_schema()");) {
      rs.next();
      currentCatalogName = rs.getString(1);
      currentSchemaName = rs.getString(2);
    }
    this.metaData = con.getMetaData();
  }

  public void build() throws SQLException {
    for (JdbcCatalog jdbcCatalog : JdbcCatalog.getCatalogs(metaData)) {
      put(jdbcCatalog);
    }

    for (JdbcSchema jdbcSchema : JdbcSchema.getSchemas(metaData)) {
      put(jdbcSchema);
    }

    for (JdbcTable jdbcTable : JdbcTable.getTables(metaData)) {
      put(jdbcTable);
      jdbcTable.getColumns(metaData);

      // "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
      // "SYNONYM"
      if (jdbcTable.tableType.equals("TABLE") || jdbcTable.tableType.equals("SYSTEM TABLE")) {
        jdbcTable.getIndices(metaData, true);
        jdbcTable.getPrimaryKey(metaData);
      }
    }

  }

  public JdbcCatalog put(JdbcCatalog jdbcCatalog) {
    return catalogs.put(jdbcCatalog.tableCatalog.toUpperCase(), jdbcCatalog);
  }

  public Map<String, JdbcCatalog> getCatalogMap() {
    return Collections.unmodifiableMap(catalogs);
  }

  public JdbcSchema put(JdbcSchema jdbcSchema) {
    JdbcCatalog jdbcCatalog = catalogs.get(jdbcSchema.tableCatalog.toUpperCase());
    return jdbcCatalog.put(jdbcSchema);
  }

  public JdbcTable put(JdbcTable jdbcTable) {
    JdbcCatalog jdbcCatalog = catalogs.get(jdbcTable.tableCatalog.toUpperCase());
    JdbcSchema jdbcSchema = jdbcCatalog.get(jdbcTable.tableSchema.toUpperCase());

    return jdbcSchema.put(jdbcTable);
  }

  // @todo: implement a GLOB based column name filter
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public List<JdbcColumn> getTableColumns(String catalogName, String schemaName, String tableName,
      String columnName) {
    ArrayList<JdbcColumn> jdbcColumns = new ArrayList<>();
    JdbcCatalog jdbcCatalog = catalogs
        .get(catalogName == null || catalogName.isEmpty() ? currentCatalogName : catalogName);
    if (jdbcCatalog == null) {
      LOGGER.info(
          "Available catalogues: " + Arrays.deepToString(catalogs.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Catalog " + catalogName + " does not exist in the DatabaseMetaData.");
    }

    JdbcSchema jdbcSchema = jdbcCatalog
        .get(schemaName == null || schemaName.isEmpty() ? currentSchemaName : schemaName);
    if (jdbcSchema == null) {
      LOGGER.info("Available schema: "
          + Arrays.deepToString(jdbcCatalog.schemas.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Schema " + schemaName + " does not exist in the given Catalog " + catalogName);
    }

    if (tableName != null && !tableName.isEmpty()) {
      JdbcTable jdbcTable = jdbcSchema.get(tableName);
      if (jdbcTable == null) {
        LOGGER.info("Available tables: "
            + Arrays.deepToString(jdbcSchema.tables.keySet().toArray(new String[0])));
        throw new RuntimeException(
            "Table " + tableName + " does not exist in the given Schema " + schemaName);
      } else {
        // @todo: implement a GLOB based column name filter
        jdbcColumns.addAll(jdbcTable.columns.values());
      }
    } else {
      for (JdbcTable jdbcTable : jdbcSchema.tables.values()) {
        // @todo: implement a GLOB based column name filter
        jdbcColumns.addAll(jdbcTable.columns.values());
      }
    }
    return jdbcColumns;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public JdbcColumn getColumn(String catalogName, String schemaName, String tableName,
      String columnName) {
    JdbcColumn jdbcColumn = null;
    JdbcCatalog jdbcCatalog = catalogs
        .get(catalogName == null || catalogName.isEmpty() ? currentCatalogName : catalogName);
    if (jdbcCatalog == null) {
      LOGGER.info(
          "Available catalogues: " + Arrays.deepToString(catalogs.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Catalog " + catalogName + " does not exist in the DatabaseMetaData.");
    }

    JdbcSchema jdbcSchema = jdbcCatalog
        .get(schemaName == null || schemaName.isEmpty() ? currentSchemaName : schemaName);
    if (jdbcSchema == null) {
      LOGGER.info("Available schema: "
          + Arrays.deepToString(jdbcCatalog.schemas.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Schema " + schemaName + " does not exist in the given Catalog " + catalogName);
    }

    if (tableName != null && !tableName.isEmpty()) {
      JdbcTable jdbcTable = jdbcSchema.get(tableName);

      if (jdbcTable == null) {
        LOGGER.info("Available tables: "
            + Arrays.deepToString(jdbcSchema.tables.keySet().toArray(new String[0])));
        throw new RuntimeException(
            "Table " + tableName + " does not exist in the given Schema " + schemaName);
      } else {
        jdbcColumn = jdbcTable.columns.get(columnName);
        if (jdbcColumn == null) {
          LOGGER.info("Available columns: "
              + Arrays.deepToString(jdbcTable.columns.keySet().toArray(new String[0])));
          throw new RuntimeException(
              "Column " + columnName + " does not exist in the given Table " + tableName);
        }
      }
    } else {
      for (JdbcTable jdbcTable : jdbcSchema.tables.values()) {
        jdbcColumn = jdbcTable.columns.get(columnName);
        if (jdbcColumn != null) {
          break;
        }
      }
      if (jdbcColumn == null) {
        throw new RuntimeException(
            "Column " + columnName + " does not exist in the given Schema " + schemaName);
      }
    }
    return jdbcColumn;
  }

}
