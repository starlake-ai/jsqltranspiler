/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI (hayssam.saleh@starlake.ai)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler.schema;

import ai.starlake.transpiler.CatalogNotFoundException;
import ai.starlake.transpiler.SchemaNotFoundException;
import ai.starlake.transpiler.TableNotFoundException;
import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.DBSchema;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import ai.starlake.transpiler.schema.JdbcUtils.DatabaseSpecific;

/**
 * The type Jdbc metadata.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public final class JdbcMetaData implements DatabaseMetaData {
  public final static Logger LOGGER = Logger.getLogger(JdbcMetaData.class.getName());
  public static final Map<Integer, String> SQL_TYPE_NAME_MAP = new HashMap<>();

  private final CaseInsensitiveLinkedHashMap<JdbcCatalog> catalogs =
      new CaseInsensitiveLinkedHashMap<>();
  private String currentCatalogName;
  private String currentSchemaName;
  private String catalogSeparator = ".";

  private final CaseInsensitiveLinkedHashMap<Table> fromTables =
      new CaseInsensitiveLinkedHashMap<>() {};

  private final CaseInsensitiveLinkedHashMap<Table> naturalJoinedTables =
      new CaseInsensitiveLinkedHashMap<>();
  private final CaseInsensitiveLinkedHashMap<Column> leftUsingJoinedColumns =
      new CaseInsensitiveLinkedHashMap<>();
  private final CaseInsensitiveLinkedHashMap<Column> rightUsingJoinedColumns =
      new CaseInsensitiveLinkedHashMap<>();

  private DatabaseSpecific databaseType = DatabaseSpecific.OTHER;

  public String getDDLStr(String catalogName) {
    StringBuilder builder = new StringBuilder();

    JdbcCatalog catalog = catalogs.get(catalogName);
    if (catalog != null) {
      for (JdbcSchema schema : catalog.schemas.values()) {
        if (!schema.tableSchema.isEmpty()) {
          builder.append("DROP SCHEMA IF EXISTS ").append(schema.tableSchema).append(" CASCADE;\n");
          builder.append("CREATE SCHEMA ").append(schema.tableSchema).append(";\n");
        }

        for (JdbcTable table : schema.tables.values()) {
          if (!table.tableName.isEmpty() && !table.getColumns().isEmpty()) {
            builder.append(TypeMappingSystem.generateCreateTableDDL(table, "h2",
                !schema.tableSchema.isEmpty()));
          }
        }

      }
    }

    return builder.toString();
  }

  /**
   * Generates a CREATE TABLE DDL statement for this JdbcTable
   * 
   * @param includeSchema whether to include schema in table name
   * @return DDL CREATE TABLE statement
   */
  public static String generateCreateTableDDL(JdbcTable table, boolean includeSchema) {
    StringBuilder ddl = new StringBuilder();

    // Table name with optional schema
    String fullTableName =
        includeSchema && table.tableSchema != null && !table.tableSchema.isEmpty()
            ? table.tableSchema + "." + table.tableName
            : table.tableName;

    ddl.append("DROP TABLE IF EXISTS ").append(fullTableName).append(";\n");
    ddl.append("CREATE TABLE ").append(fullTableName).append(" (\n");

    // Column definitions
    StringJoiner columnJoiner = new StringJoiner(",\n  ", "  ", "");

    for (JdbcColumn column : table.getColumns()) {
      String columnDef = generateColumnDefinition(column);
      columnJoiner.add(columnDef);
    }

    ddl.append(columnJoiner);

    // Primary key constraint
    if (table.primaryKey != null && !table.primaryKey.columnNames.isEmpty()) {
      ddl.append(",\n  ");
      ddl.append("CONSTRAINT ")
          .append(table.primaryKey.primaryKeyName != null ? table.primaryKey.primaryKeyName
              : "pk_" + table.tableName);
      ddl.append(" PRIMARY KEY (");
      ddl.append(String.join(", ", table.primaryKey.columnNames));
      ddl.append(")");
    }

    ddl.append("\n);\n");

    return ddl.toString();
  }

  /**
   * Generates column definition string for a JdbcColumn
   */
  private static String generateColumnDefinition(JdbcColumn column) {
    StringBuilder colDef = new StringBuilder();

    colDef.append(column.columnName).append(" ");
    colDef.append(
        mapTypeToH2(column.dataType, column.typeName, column.columnSize, column.decimalDigits));


    // Nullable constraint
    if (column.nullable != null && column.nullable == 0) {
      colDef.append(" NOT NULL");
    }

    // Default value
    if (column.columnDefinition != null && !column.columnDefinition.trim().isEmpty()) {
      colDef.append(" DEFAULT ").append(column.columnDefinition);
    }


    return colDef.toString();
  }

  /**
   * Maps both Java class types and JDBC types to H2 column types
   */
  private static String mapTypeToH2(Integer jdbcType, String typeName, Integer columnSize,
      Integer decimalDigits) {
    // First try Java class type mapping
    String s =
        columnSize != null && columnSize > 0 ? "VARCHAR(" + columnSize + ")" : "VARCHAR(255)";
    if (typeName != null) {
      String upperType = typeName.toUpperCase();
      switch (upperType) {
        case "LONG":
        case "JAVA.LANG.LONG":
          return "BIGINT";
        case "STRING":
        case "JAVA.LANG.STRING":
          return s;
        case "OBJECT":
        case "JAVA.LANG.OBJECT":
          return "JSON";
        case "INTEGER":
        case "INT":
        case "JAVA.LANG.INTEGER":
          return "INTEGER";
        case "DOUBLE":
        case "JAVA.LANG.DOUBLE":
          return "DOUBLE";
        case "BOOLEAN":
        case "JAVA.LANG.BOOLEAN":
          return "BOOLEAN";
        case "BYTE[]":
        case "BYTES":
          return columnSize != null && columnSize > 0 ? "VARBINARY(" + columnSize + ")" : "BLOB";
        case "DATE":
        case "JAVA.SQL.DATE":
          return "DATE";
        case "TIMESTAMP":
        case "JAVA.SQL.TIMESTAMP":
          return "TIMESTAMP";
      }
    }

    // Fall back to JDBC type mapping
    if (jdbcType != null) {
      switch (jdbcType) {
        case Types.BIGINT:
          return "BIGINT";
        case Types.INTEGER:
          return "INTEGER";
        case Types.SMALLINT:
          return "SMALLINT";
        case Types.TINYINT:
          return "TINYINT";
        case Types.DOUBLE:
          return "DOUBLE";
        case Types.REAL:
          return "REAL";
        case Types.DECIMAL:
        case Types.NUMERIC:
          if (columnSize != null && decimalDigits != null && decimalDigits > 0) {
            return "DECIMAL(" + columnSize + "," + decimalDigits + ")";
          }
          return "DECIMAL";
        case Types.VARCHAR:
          return s;
        case Types.CHAR:
          return columnSize != null && columnSize > 0 ? "CHAR(" + columnSize + ")" : "CHAR(1)";
        case Types.CLOB:
          return "CLOB";
        case Types.BLOB:
          return "BLOB";
        case Types.BOOLEAN:
          return "BOOLEAN";
        case Types.DATE:
          return "DATE";
        case Types.TIME:
          return "TIME";
        case Types.TIMESTAMP:
          return "TIMESTAMP";
      }
    }

    return "VARCHAR(255)"; // Default fallback
  }


  public enum ErrorMode {
    /**
     * STRICT error mode will fail when an object can't be resolved against schema.
     */
    STRICT,
    /**
     * LENIENT error mode will show a virtual column, when the physical column can't be resolved.
     */
    LENIENT,
    /**
     * IGNORE error mode will ignore an object which can't be resolved.
     */
    IGNORE
  }

  private final Set<String> unresolvedObjects = CaseInsensitiveConcurrentSet.newSet();
  private ErrorMode errorMode = ErrorMode.STRICT;

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

  /**
   * Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty
   * CURRENT_SCHEMA and creates tables from the provided definition.
   *
   * @param schemaDefinition the schema definition of tables and columns
   */
  public JdbcMetaData(String[][] schemaDefinition) {
    this("", "");
    for (String[] tableDefinition : schemaDefinition) {
      addTable(tableDefinition[0], Arrays.copyOfRange(tableDefinition, 1, tableDefinition.length));
    }
  }


  /**
   * Instantiates a new JDBC MetaData object from the starlake schema api. Empty CURRENT_CATALOG and
   * empty CURRENT_SCHEMA.
   *
   * @param schemas the schemas
   */
  public JdbcMetaData(Collection<DBSchema> schemas) {
    this("", "");
    for (DBSchema schema : schemas) {
      JdbcCatalog jdbcCatalog = get(schema.getCatalogName());
      if (jdbcCatalog == null) {
        jdbcCatalog = new JdbcCatalog(schema.getCatalogName(), ".");
        put(jdbcCatalog);
      }

      JdbcSchema jdbcSchema = jdbcCatalog.get(schema.getSchemaName());
      if (jdbcSchema == null) {
        jdbcSchema = new JdbcSchema(schema.getSchemaName(), schema.getCatalogName());
        jdbcCatalog.put(jdbcSchema);
      }

      for (Map.Entry<String, Collection<Attribute>> entry : schema.getTables().entrySet()) {
        JdbcTable jdbcTable = new JdbcTable(jdbcCatalog, jdbcSchema, entry.getKey());
        jdbcSchema.put(jdbcTable);

        for (Attribute attribute : entry.getValue()) {
          int dataType = Types.OTHER;
          String typeName = attribute.getType();

          if (attribute.isNestedField()) {
            dataType = Types.STRUCT;
            StringBuilder builder = new StringBuilder("STRUCT( ");
            int i = 0;
            for (Attribute a : attribute.getAttributes()) {
              if (i++ > 0) {
                builder.append(", ");
              }
              builder.append(
                  TypeMappingSystem.generateColumnDefinition(a.getName(), a.getType(), "duckdb"));
            }
            builder.append(")");
            typeName = builder.toString();

          } else if (attribute.isArray()) {
            dataType = Types.ARRAY;
            typeName = attribute.getType() + "[]";
          }

          JdbcColumn jdbcColumn = new JdbcColumn(schema.getCatalogName(), schema.getSchemaName(),
              entry.getKey(), attribute.getName(), dataType, typeName, 0, 0, 0, "", null);
          jdbcTable.put(jdbcColumn.columnName, jdbcColumn);
        }
      }
    }
  }

  /**
   * Instantiates a new virtual JDBC MetaData object for the given CURRENT_CATALOG and
   * CURRENT_SCHEMA and creates tables from the provided definition.
   *
   * @param catalogName the CURRENT_CATALOG
   * @param schemaName the CURRENT_SCHEMA
   * @param schemaDefinition the schema definition of tables and columns
   */
  public JdbcMetaData(String catalogName, String schemaName, String[][] schemaDefinition) {
    this(catalogName, schemaName);
    for (String[] tableDefinition : schemaDefinition) {
      for (String columnName : Arrays.copyOfRange(tableDefinition, 1, tableDefinition.length)) {
        addTable(catalogName, schemaName, tableDefinition[0], new JdbcColumn(columnName));
      }
    }
  }

  public static String getTypeName(int sqlType) {
    return SQL_TYPE_NAME_MAP.getOrDefault(sqlType, "UNKNOWN");
  }

  /**
   * Instantiates a new virtual JDBC MetaData object with a given CURRENT_CATALOG and
   * CURRENT_SCHEMA.
   *
   * @param catalogName the CURRENT_CATALOG to set
   * @param schemaName the CURRENT_SCHEMA to set
   */
  public JdbcMetaData(String catalogName, String schemaName) {
    currentCatalogName = catalogName;
    currentSchemaName = schemaName;

    JdbcCatalog catalog = new JdbcCatalog(catalogName, catalogSeparator);
    put(catalog);

    catalog.put(new JdbcSchema(schemaName, catalogName));
  }

  /**
   * Instantiates a new virtual JDBC MetaData object with an empty CURRENT_CATALOG and an empty
   * CURRENT_SCHEMA.
   *
   */
  public JdbcMetaData() {
    this("", "");
  }

  /**
   * Derives JDBC MetaData object from a physical database connection.
   *
   * @param conn the physical database connection
   * @throws SQLException when the database fails to return CURRENT_CATALOG or CURRENT_SCHEMA
   */
  public JdbcMetaData(Connection conn) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    this.databaseType = JdbcUtils.DatabaseSpecific.getType(metaData.getDatabaseProductName());

    try (Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(this.databaseType.getCurrentSchemaQuery())) {
      if (rs.next()) {
        currentCatalogName = JdbcUtils.getStringSafe(rs, 1, "");
        currentSchemaName = JdbcUtils.getStringSafe(rs, 2, "");
      } else {
        throw new SQLException();
      }
    } catch (SQLException ex) {
      currentCatalogName = "";
      currentSchemaName = "";
    }

    try {
      for (JdbcCatalog jdbcCatalog : JdbcCatalog.getCatalogsFromInformationSchema(conn)) {
        put(jdbcCatalog);
      }
    } catch (SQLException ex) {
      LOGGER.warning("Failed get Catalogs from INFORMATION_SCHEMA, use DatabaseMetaData now.");
      for (JdbcCatalog jdbcCatalog : JdbcCatalog.getCatalogs(metaData)) {
        put(jdbcCatalog);
      }
    }

    try {
      for (JdbcSchema jdbcSchema : JdbcSchema.getSchemasFromInformationSchema(conn)) {
        put(jdbcSchema);
      }
    } catch (SQLException ex) {
      LOGGER.warning("Failed get Schemas from INFORMATION_SCHEMA, use DatabaseMetaData now.");
      for (JdbcSchema jdbcSchema : JdbcSchema.getSchemas(metaData)) {
        put(jdbcSchema);
      }
    }

    for (JdbcTable jdbcTable : JdbcTable.getTables(metaData, null, null)) {
      String tableCatalog = jdbcTable.tableCatalog;
      String tableSchema = jdbcTable.tableSchema;
      JdbcCatalog catalog = catalogs.get(tableCatalog);
      if (catalog != null) {
        JdbcSchema schema = catalog.get(tableSchema);
        if (schema != null) {
          schema.put(jdbcTable);
        }
      }
    }

    for (JdbcColumn column : JdbcTable.getColumns(metaData)) {
      String tableCatalog = column.tableCatalog;
      String tableSchema = column.tableSchema;
      String tableName = column.tableName;

      JdbcCatalog catalog = catalogs.get(tableCatalog);
      if (catalog != null) {
        JdbcSchema schema = catalog.get(tableSchema);
        if (schema != null) {
          JdbcTable table = schema.get(tableName);
          if (table != null) {
            table.columns.put(column.columnName, column);
          }
        }
      }
    }
  }

  public void updateTable(Connection conn, Table t) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    this.databaseType = JdbcUtils.DatabaseSpecific.getType(metaData.getDatabaseProductName());

    try (Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(this.databaseType.getCurrentSchemaQuery())) {
      if (rs.next()) {
        currentCatalogName = JdbcUtils.getStringSafe(rs, 1, "");
        currentSchemaName = JdbcUtils.getStringSafe(rs, 2, "");
      } else {
        throw new SQLException();
      }
    } catch (SQLException ex) {
      currentCatalogName = "";
      currentSchemaName = "";
    }

    String catalogName = t.getUnquotedCatalogName();
    if (catalogName == null || catalogName.isEmpty()) {
      catalogName = currentCatalogName;
    }
    if (!catalogs.containsKey(catalogName)) {
      catalogs.put(catalogName, new JdbcCatalog(catalogName, "."));
    }
    JdbcCatalog jdbcCatalog = catalogs.get(catalogName.toUpperCase());

    String schemaName = t.getUnquotedSchemaName();
    if (schemaName == null || schemaName.isEmpty()) {
      schemaName = currentSchemaName;
    }
    if (!jdbcCatalog.containsKey(schemaName)) {
      jdbcCatalog.put(new JdbcSchema(schemaName, catalogName));
    }
    JdbcSchema schema = jdbcCatalog.get(schemaName.toUpperCase());

    for (JdbcTable jdbcTable : JdbcTable.getTables(metaData, null, null,
        t.getUnquotedName().toUpperCase())) {
      schema.put(jdbcTable);

      for (JdbcColumn column : JdbcTable.getColumns(metaData, jdbcTable.tableCatalog,
          jdbcTable.tableSchema, jdbcTable.tableName)) {
        jdbcTable.columns.put(column.columnName, column);
      }
    }
  }

  public void dropTable(Table t) throws SQLException {
    String catalogName = t.getUnquotedCatalogName();
    if (catalogName == null || catalogName.isEmpty()) {
      catalogName = currentCatalogName;
    }
    if (!catalogs.containsKey(catalogName)) {
      catalogs.put(catalogName, new JdbcCatalog(catalogName, "."));
    }
    JdbcCatalog jdbcCatalog = catalogs.get(catalogName.toUpperCase());

    String schemaName = t.getUnquotedSchemaName();
    if (schemaName == null || schemaName.isEmpty()) {
      schemaName = currentSchemaName;
    }
    if (!jdbcCatalog.containsKey(schemaName)) {
      jdbcCatalog.put(new JdbcSchema(schemaName, catalogName));
    }
    JdbcSchema schema = jdbcCatalog.get(schemaName.toUpperCase());

    if (schema.containsKey(t.getFullyQualifiedName())) {
      schema.remove(t.getFullyQualifiedName());
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
    /*
     * different DBs don't return correct catalog+schema info/hierarchy in
     * getSchemas() it is fixed here by adding missing catalogs and/or schemas
     */

    JdbcCatalog jdbcCatalog = catalogs.get(jdbcTable.tableCatalog.toUpperCase());
    if (jdbcCatalog == null) {
      jdbcCatalog = new JdbcCatalog(jdbcTable.tableCatalog, null);
      catalogs.put(jdbcCatalog.tableCatalog, jdbcCatalog);
    }
    JdbcSchema jdbcSchema = jdbcCatalog.get(jdbcTable.tableSchema.toUpperCase());
    if (jdbcSchema == null) {
      jdbcSchema = new JdbcSchema(jdbcTable.tableSchema, jdbcCatalog.tableCatalog);
      jdbcCatalog.put(jdbcSchema);
    }
    return jdbcSchema.put(jdbcTable);
  }

  public JdbcTable put(JdbcResultSetMetaData rsMetaData, String name, String errorMessage) {
    try {
      JdbcTable t = new JdbcTable(currentCatalogName, currentSchemaName, name);
      int columnCount = rsMetaData.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        String finalColumnName =
            rsMetaData.getColumnLabel(i) != null && !rsMetaData.getColumnLabel(i).isEmpty()
                ? rsMetaData.getColumnLabel(i)
                : rsMetaData.getColumnName(i);

        JdbcColumn col = t.add(t.tableCatalog, t.tableSchema, t.tableName, finalColumnName,
            rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
            rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
            "", rsMetaData.getColumnDisplaySize(i), i, "",
            rsMetaData.getScopeCatalog(i) != null && !rsMetaData.getScopeCatalog(i).isEmpty()
                ? rsMetaData.getScopeCatalog(i)
                : rsMetaData.getCatalogName(i),
            rsMetaData.getScopeSchema(i) != null && !rsMetaData.getScopeSchema(i).isEmpty()
                ? rsMetaData.getScopeSchema(i)
                : rsMetaData.getSchemaName(i),
            rsMetaData.getScopeTable(i) != null && !rsMetaData.getScopeTable(i).isEmpty()
                ? rsMetaData.getScopeTable(i)
                : rsMetaData.getTableName(i),
            rsMetaData.getColumnName(i), null, "", "");

        // add the Lineage Information, 0-Indexed
        col.add(rsMetaData.columns.get(i - 1).getChildren());
        col.setExpression(rsMetaData.columns.get(i - 1).getExpression());
      }
      put(t);
      return t;
    } catch (SQLException ex) {
      throw new RuntimeException(errorMessage, ex);
    }
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
        switch (errorMode) {
          case STRICT:
            throw new RuntimeException(
                "Table " + tableName + " does not exist in the given Schema " + schemaName);
          case LENIENT:
          case IGNORE:
            addUnresolved(tableName);
        }
      } else {
        // @todo: implement a GLOB based column name filter
        for (JdbcColumn column : jdbcTable.columns.values()) {
          // column.tableCatalog = jdbcCatalog.tableCatalog;
          // column.tableSchema = jdbcSchema.tableSchema;
          column.tableName = jdbcTable.tableName;

          if (column.scopeCatalog == null || column.scopeCatalog.isEmpty()) {
            column.scopeCatalog = jdbcCatalog.tableCatalog;
          }
          if (column.scopeSchema == null || column.scopeSchema.isEmpty()) {
            column.scopeSchema = jdbcSchema.tableSchema;
          }
          if (column.scopeTable == null || column.scopeTable.isEmpty()) {
            column.scopeTable = jdbcTable.tableName;
          }

          jdbcColumns.add(column);
        }
      }
    } else {
      for (JdbcTable jdbcTable : jdbcSchema.tables.values()) {
        // @todo: implement a GLOB based column name filter
        for (JdbcColumn column : jdbcTable.columns.values()) {
          column.tableCatalog = jdbcCatalog.tableCatalog;
          column.tableSchema = jdbcSchema.tableSchema;
          column.tableName = jdbcTable.tableName;
          if (column.scopeCatalog == null || column.scopeCatalog.isEmpty()) {
            column.scopeCatalog = jdbcCatalog.tableCatalog;
          }
          if (column.scopeSchema == null || column.scopeSchema.isEmpty()) {
            column.scopeSchema = jdbcSchema.tableSchema;
          }
          if (column.scopeTable == null || column.scopeTable.isEmpty()) {
            column.scopeTable = jdbcTable.tableName;
          }
          jdbcColumns.add(column);
        }
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
      switch (errorMode) {
        case STRICT:
          throw new CatalogNotFoundException(catalogName);
        case LENIENT:
          LOGGER.warning("Available catalogues: "
              + Arrays.deepToString(catalogs.keySet().toArray(new String[0])));
          break;
        case IGNORE:
          LOGGER.fine("Available catalogues: "
              + Arrays.deepToString(catalogs.keySet().toArray(new String[0])));
          break;
      }
    }

    JdbcSchema jdbcSchema =
        jdbcCatalog.get(schemaName == null || schemaName.isEmpty() ? currentSchemaName
            : schemaName.replaceAll("^\"|\"$", ""));
    if (jdbcSchema == null) {
      switch (errorMode) {
        case STRICT:
          throw new SchemaNotFoundException(schemaName);
        case LENIENT:
          LOGGER.warning("Available schema: "
              + Arrays.deepToString(jdbcCatalog.schemas.keySet().toArray(new String[0])));
          break;
        case IGNORE:
          LOGGER.fine("Available schema: "
              + Arrays.deepToString(jdbcCatalog.schemas.keySet().toArray(new String[0])));
          break;
      }
    }

    if (tableName != null && !tableName.isEmpty()) {
      JdbcTable jdbcTable = jdbcSchema.get(tableName.replaceAll("^\"|\"$", ""));

      if (jdbcTable == null) {
        switch (errorMode) {
          case STRICT:
            throw new TableNotFoundException(tableName, jdbcSchema.tables.keySet());
          case LENIENT:
            LOGGER.warning("Available tables: "
                + Arrays.deepToString(jdbcSchema.tables.keySet().toArray(new String[0])));
            break;
          case IGNORE:
            LOGGER.finer("Available tables: "
                + Arrays.deepToString(jdbcSchema.tables.keySet().toArray(new String[0])));
            break;
        }

      } else {
        jdbcColumn = jdbcTable.columns.get(columnName.replaceAll("^\"|\"$", ""));
      }
    } else {
      for (JdbcTable jdbcTable : jdbcSchema.tables.values()) {
        jdbcColumn = jdbcTable.columns.get(columnName.replaceAll("^\"|\"$", ""));
        if (jdbcColumn != null) {
          break;
        }
      }
    }
    return jdbcColumn;
  }

  public JdbcCatalog put(String key, JdbcCatalog value) {
    return catalogs.put(key, value);
  }

  public boolean containsValue(JdbcCatalog value) {
    return catalogs.containsValue(value);
  }

  public int size() {
    return catalogs.size();
  }

  public JdbcCatalog replace(String key, JdbcCatalog value) {
    return catalogs.replace(key, value);
  }

  public boolean isEmpty() {
    return catalogs.isEmpty();
  }

  public JdbcCatalog compute(String key,
      BiFunction<? super String, ? super JdbcCatalog, ? extends JdbcCatalog> remappingFunction) {
    return catalogs.compute(key, remappingFunction);
  }

  public void putAll(Map<? extends String, ? extends JdbcCatalog> m) {
    catalogs.putAll(m);
  }

  public Collection<JdbcCatalog> values() {
    return catalogs.values();
  }

  public boolean replace(String key, JdbcCatalog oldValue, JdbcCatalog newValue) {
    return catalogs.replace(key, oldValue, newValue);
  }

  public void forEach(BiConsumer<? super String, ? super JdbcCatalog> action) {
    catalogs.forEach(action);
  }

  public JdbcCatalog getOrDefault(String key, JdbcCatalog defaultValue) {
    return catalogs.getOrDefault(key, defaultValue);
  }

  public boolean remove(String key, JdbcCatalog value) {
    return catalogs.remove(key, value);
  }

  public JdbcCatalog computeIfPresent(String key,
      BiFunction<? super String, ? super JdbcCatalog, ? extends JdbcCatalog> remappingFunction) {
    return catalogs.computeIfPresent(key, remappingFunction);
  }

  public void replaceAll(
      BiFunction<? super String, ? super JdbcCatalog, ? extends JdbcCatalog> function) {
    catalogs.replaceAll(function);
  }

  public JdbcCatalog computeIfAbsent(String key,
      Function<? super String, ? extends JdbcCatalog> mappingFunction) {
    return catalogs.computeIfAbsent(key, mappingFunction);
  }

  public JdbcCatalog putIfAbsent(JdbcCatalog value) {
    return catalogs.putIfAbsent(value.tableCatalog, value);
  }

  public JdbcCatalog merge(String key, JdbcCatalog value,
      BiFunction<? super JdbcCatalog, ? super JdbcCatalog, ? extends JdbcCatalog> remappingFunction) {
    return catalogs.merge(key, value, remappingFunction);
  }

  public JdbcCatalog get(String key) {
    return catalogs.get(key);
  }

  public boolean containsKey(String key) {
    return catalogs.containsKey(key);
  }

  public JdbcCatalog remove(String key) {
    return catalogs.remove(key);
  }

  public void clear() {
    catalogs.clear();
  }

  public Set<Map.Entry<String, JdbcCatalog>> entrySet() {
    return catalogs.entrySet();
  }

  public Set<String> keySet() {
    return catalogs.keySet();
  }

  public JdbcSchema addSchema(String schemaName) {
    JdbcCatalog catalog = catalogs.getOrDefault(currentCatalogName,
        new JdbcCatalog(currentCatalogName, catalogSeparator));
    putIfAbsent(catalog);

    JdbcSchema schema =
        catalog.getOrDefault(schemaName, new JdbcSchema(schemaName, catalog.tableCatalog));
    catalog.putIfAbsent(schema);

    return schema;
  }

  public JdbcMetaData addTable(String catalogName, String schemaName, String tableName,
      Collection<JdbcColumn> columns) {
    JdbcCatalog catalog =
        catalogs.getOrDefault(catalogName, new JdbcCatalog(catalogName, catalogSeparator));
    putIfAbsent(catalog);

    JdbcSchema schema =
        catalog.getOrDefault(schemaName, new JdbcSchema(schemaName, catalog.tableCatalog));
    catalog.putIfAbsent(schema);

    JdbcTable table = schema.getOrDefault(tableName, new JdbcTable(catalog, schema, tableName));
    schema.putIfAbsent(table);

    int ordinalPosition = table.columns.size();
    for (JdbcColumn column : columns) {
      column.tableCatalog = catalog.tableCatalog;
      column.scopeCatalog = catalog.tableCatalog;
      column.tableSchema = schema.tableSchema;
      column.scopeSchema = schema.tableSchema;
      column.tableName = table.tableName;
      column.scopeTable = table.tableName;
      column.ordinalPosition = ordinalPosition;
      table.add(column);
    }
    return this;
  }

  public JdbcMetaData addTable(String catalogName, String schemaName, String tableName,
      JdbcColumn... columns) {
    return addTable(catalogName, schemaName, tableName, Arrays.asList(columns));
  }

  public JdbcMetaData addTable(String schemaName, String tableName,
      Collection<JdbcColumn> columns) {
    return addTable(currentCatalogName, schemaName, tableName, columns);
  }

  public JdbcMetaData addTable(String schemaName, String tableName, JdbcColumn... columns) {
    return addTable(schemaName, tableName, Arrays.asList(columns));
  }

  public JdbcMetaData addTable(String tableName, Collection<JdbcColumn> columns) {
    return addTable(currentCatalogName, currentSchemaName, tableName, columns);
  }

  public JdbcMetaData addTable(String tableName, JdbcColumn... columns) {
    return addTable(tableName, Arrays.asList(columns));
  }

  public JdbcMetaData addTable(String tableName, String... columnNames) {
    LinkedHashSet<JdbcColumn> columns = new LinkedHashSet<>();
    for (String columnName : columnNames) {
      columns.add(new JdbcColumn(currentCatalogName, currentSchemaName, tableName, columnName,
          new Column(columnName)));
    }

    return addTable(tableName, List.copyOf(columns));
  }

  public JdbcTable addColumns(String tableName, Collection<JdbcColumn> columns) {
    JdbcCatalog catalog = catalogs.getOrDefault(currentCatalogName,
        new JdbcCatalog(currentCatalogName, catalogSeparator));
    putIfAbsent(catalog);

    JdbcSchema schema = catalog.getOrDefault(currentSchemaName,
        new JdbcSchema(currentSchemaName, catalog.tableCatalog));
    catalog.putIfAbsent(schema);

    JdbcTable table = schema.getOrDefault(tableName, new JdbcTable(catalog, schema, tableName));
    schema.putIfAbsent(table);

    int ordinalPosition = table.columns.size();
    for (JdbcColumn column : columns) {
      column.tableCatalog = catalog.tableCatalog;
      column.tableSchema = schema.tableSchema;
      column.tableName = table.tableName;
      column.ordinalPosition = ordinalPosition;
      table.add(column);
    }

    return table;
  }

  public JdbcTable addColumns(String tableName, JdbcColumn... columns) {
    return addColumns(tableName, Arrays.asList(columns));
  }

  @Override
  public boolean allProceduresAreCallable() {
    return false;
  }

  @Override
  public boolean allTablesAreSelectable() {
    return true;
  }

  @Override
  public String getURL() {
    return "";
  }

  @Override
  public String getUserName() {
    return "";
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public boolean nullsAreSortedHigh() {
    return true;
  }

  @Override
  public boolean nullsAreSortedLow() {
    return false;
  }

  @Override
  public boolean nullsAreSortedAtStart() {
    return true;
  }

  @Override
  public boolean nullsAreSortedAtEnd() {
    return false;
  }

  @Override
  public String getDatabaseProductName() {
    return "";
  }

  @Override
  public String getDatabaseProductVersion() {
    return "";
  }

  @Override
  public String getDriverName() {
    return "";
  }

  @Override
  public String getDriverVersion() {
    return "";
  }

  @Override
  public int getDriverMajorVersion() {
    return 0;
  }

  @Override
  public int getDriverMinorVersion() {
    return 0;
  }

  @Override
  public boolean usesLocalFiles() {
    return false;
  }

  @Override
  public boolean usesLocalFilePerTable() {
    return false;
  }

  @Override
  public boolean supportsMixedCaseIdentifiers() {
    return true;
  }

  @Override
  public boolean storesUpperCaseIdentifiers() {
    return false;
  }

  @Override
  public boolean storesLowerCaseIdentifiers() {
    return false;
  }

  @Override
  public boolean storesMixedCaseIdentifiers() {
    return true;
  }

  @Override
  public boolean supportsMixedCaseQuotedIdentifiers() {
    return true;
  }

  @Override
  public boolean storesUpperCaseQuotedIdentifiers() {
    return false;
  }

  @Override
  public boolean storesLowerCaseQuotedIdentifiers() {
    return false;
  }

  @Override
  public boolean storesMixedCaseQuotedIdentifiers() {
    return true;
  }

  @Override
  public String getIdentifierQuoteString() {
    return "\"";
  }

  @Override
  public String getSQLKeywords() {
    return "";
  }

  @Override
  public String getNumericFunctions() {
    return "";
  }

  @Override
  public String getStringFunctions() {
    return "";
  }

  @Override
  public String getSystemFunctions() {
    return "";
  }

  @Override
  public String getTimeDateFunctions() {
    return "";
  }

  @Override
  public String getSearchStringEscape() {
    return "";
  }

  @Override
  public String getExtraNameCharacters() {
    return "";
  }

  @Override
  public boolean supportsAlterTableWithAddColumn() {
    return false;
  }

  @Override
  public boolean supportsAlterTableWithDropColumn() {
    return false;
  }

  @Override
  public boolean supportsColumnAliasing() {
    return true;
  }

  @Override
  public boolean nullPlusNonNullIsNull() {
    return false;
  }

  @Override
  public boolean supportsConvert() {
    return false;
  }

  @Override
  public boolean supportsConvert(int fromType, int toType) {
    return false;
  }

  @Override
  public boolean supportsTableCorrelationNames() {
    return false;
  }

  @Override
  public boolean supportsDifferentTableCorrelationNames() {
    return false;
  }

  @Override
  public boolean supportsExpressionsInOrderBy() {
    return true;
  }

  @Override
  public boolean supportsOrderByUnrelated() {
    return false;
  }

  @Override
  public boolean supportsGroupBy() {
    return true;
  }

  @Override
  public boolean supportsGroupByUnrelated() {
    return false;
  }

  @Override
  public boolean supportsGroupByBeyondSelect() {
    return false;
  }

  @Override
  public boolean supportsLikeEscapeClause() {
    return true;
  }

  @Override
  public boolean supportsMultipleResultSets() {
    return false;
  }

  @Override
  public boolean supportsMultipleTransactions() {
    return false;
  }

  @Override
  public boolean supportsNonNullableColumns() {
    return false;
  }

  @Override
  public boolean supportsMinimumSQLGrammar() {
    return true;
  }

  @Override
  public boolean supportsCoreSQLGrammar() {
    return true;
  }

  @Override
  public boolean supportsExtendedSQLGrammar() {
    return true;
  }

  @Override
  public boolean supportsANSI92EntryLevelSQL() {
    return true;
  }

  @Override
  public boolean supportsANSI92IntermediateSQL() {
    return true;
  }

  @Override
  public boolean supportsANSI92FullSQL() {
    return true;
  }

  @Override
  public boolean supportsIntegrityEnhancementFacility() {
    return false;
  }

  @Override
  public boolean supportsOuterJoins() {
    return true;
  }

  @Override
  public boolean supportsFullOuterJoins() {
    return true;
  }

  @Override
  public boolean supportsLimitedOuterJoins() {
    return false;
  }

  @Override
  public String getSchemaTerm() {
    return "SCHEMA";
  }

  @Override
  public String getProcedureTerm() {
    return "";
  }

  @Override
  public String getCatalogTerm() {
    return "CATALOG";
  }

  @Override
  public boolean isCatalogAtStart() {
    return true;
  }

  @Override
  public String getCatalogSeparator() {
    return catalogSeparator;
  }

  @Override
  public boolean supportsSchemasInDataManipulation() {
    return true;
  }

  @Override
  public boolean supportsSchemasInProcedureCalls() {
    return false;
  }

  @Override
  public boolean supportsSchemasInTableDefinitions() {
    return true;
  }

  @Override
  public boolean supportsSchemasInIndexDefinitions() {
    return false;
  }

  @Override
  public boolean supportsSchemasInPrivilegeDefinitions() {
    return false;
  }

  @Override
  public boolean supportsCatalogsInDataManipulation() {
    return true;
  }

  @Override
  public boolean supportsCatalogsInProcedureCalls() {
    return false;
  }

  @Override
  public boolean supportsCatalogsInTableDefinitions() {
    return true;
  }

  @Override
  public boolean supportsCatalogsInIndexDefinitions() {
    return false;
  }

  @Override
  public boolean supportsCatalogsInPrivilegeDefinitions() {
    return false;
  }

  @Override
  public boolean supportsPositionedDelete() {
    return false;
  }

  @Override
  public boolean supportsPositionedUpdate() {
    return false;
  }

  @Override
  public boolean supportsSelectForUpdate() {
    return false;
  }

  @Override
  public boolean supportsStoredProcedures() {
    return false;
  }

  @Override
  public boolean supportsSubqueriesInComparisons() {
    return true;
  }

  @Override
  public boolean supportsSubqueriesInExists() {
    return true;
  }

  @Override
  public boolean supportsSubqueriesInIns() {
    return true;
  }

  @Override
  public boolean supportsSubqueriesInQuantifieds() {
    return true;
  }

  @Override
  public boolean supportsCorrelatedSubqueries() {
    return true;
  }

  @Override
  public boolean supportsUnion() {
    return true;
  }

  @Override
  public boolean supportsUnionAll() {
    return true;
  }

  @Override
  public boolean supportsOpenCursorsAcrossCommit() {
    return false;
  }

  @Override
  public boolean supportsOpenCursorsAcrossRollback() {
    return false;
  }

  @Override
  public boolean supportsOpenStatementsAcrossCommit() {
    return false;
  }

  @Override
  public boolean supportsOpenStatementsAcrossRollback() {
    return false;
  }

  @Override
  public int getMaxBinaryLiteralLength() {
    return 0;
  }

  @Override
  public int getMaxCharLiteralLength() {
    return 0;
  }

  @Override
  public int getMaxColumnNameLength() {
    return 0;
  }

  @Override
  public int getMaxColumnsInGroupBy() {
    return 0;
  }

  @Override
  public int getMaxColumnsInIndex() {
    return 0;
  }

  @Override
  public int getMaxColumnsInOrderBy() {
    return 0;
  }

  @Override
  public int getMaxColumnsInSelect() {
    return 0;
  }

  @Override
  public int getMaxColumnsInTable() {
    return 0;
  }

  @Override
  public int getMaxConnections() {
    return 0;
  }

  @Override
  public int getMaxCursorNameLength() {
    return 0;
  }

  @Override
  public int getMaxIndexLength() {
    return 0;
  }

  @Override
  public int getMaxSchemaNameLength() {
    return 0;
  }

  @Override
  public int getMaxProcedureNameLength() {
    return 0;
  }

  @Override
  public int getMaxCatalogNameLength() {
    return 0;
  }

  @Override
  public int getMaxRowSize() {
    return 0;
  }

  @Override
  public boolean doesMaxRowSizeIncludeBlobs() {
    return false;
  }

  @Override
  public int getMaxStatementLength() {
    return 0;
  }

  @Override
  public int getMaxStatements() {
    return 0;
  }

  @Override
  public int getMaxTableNameLength() {
    return 0;
  }

  @Override
  public int getMaxTablesInSelect() {
    return 0;
  }

  @Override
  public int getMaxUserNameLength() {
    return 0;
  }

  @Override
  public int getDefaultTransactionIsolation() {
    return 0;
  }

  @Override
  public boolean supportsTransactions() {
    return false;
  }

  @Override
  public boolean supportsTransactionIsolationLevel(int level) {
    return false;
  }

  @Override
  public boolean supportsDataDefinitionAndDataManipulationTransactions() {
    return false;
  }

  @Override
  public boolean supportsDataManipulationTransactionsOnly() {
    return false;
  }

  @Override
  public boolean dataDefinitionCausesTransactionCommit() {
    return false;
  }

  @Override
  public boolean dataDefinitionIgnoredInTransactions() {
    return false;
  }

  @Override
  public ResultSet getProcedures(String catalog, String schemaPattern,
      String procedureNamePattern) {
    return null;
  }

  @Override
  public ResultSet getProcedureColumns(String catalog, String schemaPattern,
      String procedureNamePattern, String columnNamePattern) {
    return null;
  }

  @Override
  public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern,
      String[] types) {
    return null;
  }

  @Override
  public ResultSet getSchemas() {
    return null;
  }

  @Override
  public ResultSet getCatalogs() {
    return null;
  }

  @Override
  public ResultSet getTableTypes() {
    return null;
  }

  @Override
  public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
      String columnNamePattern) {
    return null;
  }

  @Override
  public ResultSet getColumnPrivileges(String catalog, String schema, String table,
      String columnNamePattern) {
    return null;
  }

  @Override
  public ResultSet getTablePrivileges(String catalog, String schemaPattern,
      String tableNamePattern) {
    return null;
  }

  @Override
  public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope,
      boolean nullable) {
    return null;
  }

  @Override
  public ResultSet getVersionColumns(String catalog, String schema, String table) {
    return null;
  }

  @Override
  public ResultSet getPrimaryKeys(String catalog, String schema, String table) {
    return null;
  }

  @Override
  public ResultSet getImportedKeys(String catalog, String schema, String table) {
    return null;
  }

  @Override
  public ResultSet getExportedKeys(String catalog, String schema, String table) {
    return null;
  }

  @Override
  public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
      String foreignCatalog, String foreignSchema, String foreignTable) {
    return null;
  }

  @Override
  public ResultSet getTypeInfo() {
    return null;
  }

  @Override
  public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique,
      boolean approximate) {
    return null;
  }

  @Override
  public boolean supportsResultSetType(int type) {
    return false;
  }

  @Override
  public boolean supportsResultSetConcurrency(int type, int concurrency) {
    return false;
  }

  @Override
  public boolean ownUpdatesAreVisible(int type) {
    return false;
  }

  @Override
  public boolean ownDeletesAreVisible(int type) {
    return false;
  }

  @Override
  public boolean ownInsertsAreVisible(int type) {
    return false;
  }

  @Override
  public boolean othersUpdatesAreVisible(int type) {
    return false;
  }

  @Override
  public boolean othersDeletesAreVisible(int type) {
    return false;
  }

  @Override
  public boolean othersInsertsAreVisible(int type) {
    return false;
  }

  @Override
  public boolean updatesAreDetected(int type) {
    return false;
  }

  @Override
  public boolean deletesAreDetected(int type) {
    return false;
  }

  @Override
  public boolean insertsAreDetected(int type) {
    return false;
  }

  @Override
  public boolean supportsBatchUpdates() {
    return false;
  }

  @Override
  public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern,
      int[] types) {
    return null;
  }

  @Override
  public Connection getConnection() {
    return null;
  }

  @Override
  public boolean supportsSavepoints() {
    return false;
  }

  @Override
  public boolean supportsNamedParameters() {
    return true;
  }

  @Override
  public boolean supportsMultipleOpenResults() {
    return false;
  }

  @Override
  public boolean supportsGetGeneratedKeys() {
    return false;
  }

  @Override
  public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) {
    return null;
  }

  @Override
  public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) {
    return null;
  }

  @Override
  public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
      String attributeNamePattern) {
    return null;
  }

  @Override
  public boolean supportsResultSetHoldability(int holdability) {
    return false;
  }

  @Override
  public int getResultSetHoldability() {
    return 0;
  }

  @Override
  public int getDatabaseMajorVersion() {
    return 0;
  }

  @Override
  public int getDatabaseMinorVersion() {
    return 0;
  }

  @Override
  public int getJDBCMajorVersion() {
    return 0;
  }

  @Override
  public int getJDBCMinorVersion() {
    return 0;
  }

  @Override
  public int getSQLStateType() {
    return sqlStateSQL;
  }

  @Override
  public boolean locatorsUpdateCopy() {
    return false;
  }

  @Override
  public boolean supportsStatementPooling() {
    return false;
  }

  @Override
  public RowIdLifetime getRowIdLifetime() {
    return null;
  }

  @Override
  public ResultSet getSchemas(String catalog, String schemaPattern) {
    return null;
  }

  @Override
  public boolean supportsStoredFunctionsUsingCallSyntax() {
    return false;
  }

  @Override
  public boolean autoCommitFailureClosesAllResultSets() {
    return false;
  }

  @Override
  public ResultSet getClientInfoProperties() {
    return null;
  }

  @Override
  public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) {
    return null;
  }

  @Override
  public ResultSet getFunctionColumns(String catalog, String schemaPattern,
      String functionNamePattern, String columnNamePattern) {
    return null;
  }

  @Override
  public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
      String columnNamePattern) {
    return null;
  }

  @Override
  public boolean generatedKeyAlwaysReturned() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> iface) {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }

  public CaseInsensitiveLinkedHashMap<Table> getFromTables() {
    return fromTables;
  }

  public JdbcMetaData addFromTables(Collection<Table> fromTables) {
    for (Table t : fromTables) {
      this.fromTables.put(t.getName(), t);
    }
    return this;
  }

  public JdbcMetaData addFromTables(Table... fromTables) {
    for (Table t : fromTables) {
      this.fromTables.put(t.getName(), t);
    }
    return this;
  }

  public CaseInsensitiveLinkedHashMap<Column> getLeftUsingJoinedColumns() {
    return leftUsingJoinedColumns;
  }

  public JdbcMetaData addLeftUsingJoinColumns(Collection<Column> columns) {
    for (Column column : columns) {
      this.leftUsingJoinedColumns.put(column.getFullyQualifiedName(), column);
    }
    return this;
  }

  public CaseInsensitiveLinkedHashMap<Column> getRightUsingJoinedColumns() {
    return rightUsingJoinedColumns;
  }

  public JdbcMetaData addRightUsingJoinColumns(Collection<Column> columns) {
    for (Column column : columns) {
      this.rightUsingJoinedColumns.put(column.getFullyQualifiedName(), column);
    }
    return this;
  }

  public CaseInsensitiveLinkedHashMap<Table> getNaturalJoinedTables() {
    return naturalJoinedTables;
  }

  public JdbcMetaData addNaturalJoinedTable(Table t) {
    naturalJoinedTables.put(t.getName(), t);
    return this;
  }

  public static JdbcMetaData copyOf(JdbcMetaData metaData,
      CaseInsensitiveLinkedHashMap<Table> fromTables) {
    JdbcMetaData metaData1 =
        new JdbcMetaData(metaData.currentCatalogName, metaData.currentSchemaName);
    metaData1.getFromTables().putAll(fromTables);

    for (JdbcCatalog catalog : metaData.catalogs.values()) {
      JdbcCatalog catalog1 = new JdbcCatalog(catalog.tableCatalog, metaData.catalogSeparator);
      for (JdbcSchema schema : catalog.schemas.values()) {
        JdbcSchema schema1 = new JdbcSchema(schema.tableSchema, schema.tableCatalog);
        for (JdbcTable table : schema.tables.values()) {
          JdbcTable table1 = getJdbcTable(table);
          // @todo: add indices and reference
          schema1.put(table1);
        }
        schema1.synonyms.putAll(schema.synonyms);
        schema1.droppedTables.putAll(schema.droppedTables);

        catalog1.put(schema1);
      }
      metaData1.put(catalog1);
    }

    metaData1.errorMode = metaData.getErrorMode();

    return metaData1;
  }

  public static JdbcMetaData copyOf(JdbcMetaData metaData) {
    return copyOf(metaData, new CaseInsensitiveLinkedHashMap<Table>());
  }

  public JdbcMetaData copyOf() {
    return copyOf(this);
  }

  private static JdbcTable getJdbcTable(JdbcTable table) {
    JdbcTable table1 = new JdbcTable(table.tableCatalog, table.tableSchema, table.tableName,
        table.tableType, table.remarks, table.typeCatalog, table.typeSchema, table.typeName,
        table.selfReferenceColName, table.referenceGeneration);
    for (JdbcColumn column : table.columns.values()) {
      JdbcColumn column1 = new JdbcColumn(column.tableCatalog, column.tableSchema, column.tableName,
          column.columnName, column.dataType, column.typeName, column.columnSize,
          column.decimalDigits, column.numericPrecisionRadix, column.nullable, column.remarks,
          column.columnDefinition, column.characterOctetLength, column.ordinalPosition,
          column.isNullable, column.scopeCatalog, column.scopeSchema, column.scopeTable,
          column.scopeColumn, column.sourceDataType, column.isAutomaticIncrement,
          column.isGeneratedColumn, column.getExpression());
      table1.add(column1);
    }
    return table1;
  }

  public String getCurrentCatalogName() {
    return currentCatalogName;
  }

  public String getCurrentSchemaName() {
    return currentSchemaName;
  }

  public JdbcMetaData setCatalogSeparator(String catalogSeparator) {
    this.catalogSeparator = catalogSeparator;
    return this;
  }

  /**
   * Add the name of an unresolvable column or table to the list.
   *
   * @param unquotedQualifiedName the unquoted qualified name of the table or column
   */
  public void addUnresolved(String unquotedQualifiedName) {
    unresolvedObjects.add(unquotedQualifiedName);
  }

  /**
   * Gets unresolved column or table names, not existing in the schema
   *
   * @return the unresolved column or table names
   */
  public Set<String> getUnresolvedObjects() {
    return unresolvedObjects;
  }

  /**
   * Gets the error mode.
   *
   * @return the error mode
   */
  public ErrorMode getErrorMode() {
    return errorMode;
  }

  /**
   * Sets the error mode.
   *
   * @param errorMode the error mode
   * @return the error mode
   */
  public JdbcMetaData setErrorMode(ErrorMode errorMode) {
    this.errorMode = errorMode;
    return this;
  }

  public String getDatabaseType() {
    return databaseType.name();
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = DatabaseSpecific.valueOf(databaseType);
  }

  public List<JdbcCatalog> getCatalogsList() {
    return new ArrayList<JdbcCatalog>(this.catalogs.values());
  }

  public void setCatalogsList(List<JdbcCatalog> catalogs) {
    for (JdbcCatalog item : catalogs) {
      this.put(item);
    }
  }

  public void setCurrentCatalogName(String currentCatalogName) {
    this.currentCatalogName = currentCatalogName;
  }

  public void setCurrentSchemaName(String currentSchemaName) {
    this.currentSchemaName = currentSchemaName;
  }

  public boolean hasTable(String catalogName, String schemaName, String tableName) {
    final JdbcCatalog jdbcCatalog = catalogs.get(catalogName);
    if (jdbcCatalog != null) {
      final JdbcSchema jdbcSchema = jdbcCatalog.get(schemaName);
      if (jdbcSchema != null) {
        return jdbcSchema.containsKey(tableName);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean hasTable(Table t) {
    final JdbcCatalog jdbcCatalog =
        catalogs.getOrDefault(t.getUnquotedCatalogName(), catalogs.get(currentCatalogName));
    if (jdbcCatalog != null) {
      final JdbcSchema jdbcSchema =
          jdbcCatalog.getOrDefault(t.getUnquotedSchemaName(), jdbcCatalog.get(currentSchemaName));
      if (jdbcSchema != null) {
        return jdbcSchema.containsKey(t.getUnquotedName());
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public JdbcTable getTable(String catalogName, String schemaName, String tableName) {
    final JdbcCatalog jdbcCatalog =
        catalogs.getOrDefault(catalogName, catalogs.get(currentCatalogName));
    if (jdbcCatalog != null) {
      final JdbcSchema jdbcSchema =
          jdbcCatalog.getOrDefault(schemaName, jdbcCatalog.get(currentSchemaName));
      if (jdbcSchema != null) {
        return jdbcSchema.get(tableName);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public JdbcTable getTable(Table t) {
    Table fullyQualifiedTable = new Table(t.getFullyQualifiedName())
        .setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);

    final JdbcCatalog jdbcCatalog = catalogs.get(fullyQualifiedTable.getUnquotedCatalogName());
    if (jdbcCatalog != null) {
      final JdbcSchema jdbcSchema = jdbcCatalog.get(fullyQualifiedTable.getUnquotedSchemaName());
      if (jdbcSchema != null) {
        return jdbcSchema.get(fullyQualifiedTable.getUnquotedName());
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public boolean hasTableColumn(String catalogName, String schemaName, String tableName,
      String columnName) {
    final JdbcCatalog jdbcCatalog =
        catalogs.getOrDefault(catalogName, catalogs.get(currentCatalogName));
    if (jdbcCatalog != null) {
      final JdbcSchema jdbcSchema =
          jdbcCatalog.getOrDefault(schemaName, jdbcCatalog.get(currentSchemaName));
      if (jdbcSchema != null) {
        JdbcTable jdbcTable = jdbcSchema.get(tableName);
        if (jdbcTable != null) {
          return jdbcTable.columns.containsKey(columnName);
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public void addSynonym(String fromTableName, String toTableName) {
    if (fromTableName == null || fromTableName.isEmpty()) {
      throw new RuntimeException("Table name must not be empty!");
    } else if (toTableName == null || toTableName.isEmpty()) {
      throw new RuntimeException("Table name must not be empty!");
    }

    Table fromTable =
        new Table(fromTableName).setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);
    Table toTable =
        new Table(toTableName).setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);
    JdbcTable jdbcTable = getTable(toTable);
    if (jdbcTable == null) {
      throw new TableNotFoundException(toTable.getFullyQualifiedName(), List.of());
    }

    JdbcCatalog fromCatalog = catalogs.get(fromTable.getUnquotedCatalogName());
    if (fromCatalog != null) {
      JdbcSchema fromSchema = fromCatalog.get(fromTable.getUnquotedSchemaName());
      if (fromSchema != null) {
        fromSchema.synonyms.put(fromTable.getUnquotedName(), jdbcTable);
      } else {
        throw new SchemaNotFoundException(fromTable.getUnquotedSchemaName());
      }
    } else {
      throw new CatalogNotFoundException(fromTable.getUnquotedCatalogName());
    }
  }

  public void dropSynonym(String fromTableName, String toTableName) {
    if (fromTableName == null || fromTableName.isEmpty()) {
      throw new RuntimeException("Table name must not be empty!");
    } else if (toTableName == null || toTableName.isEmpty()) {
      throw new RuntimeException("Table name must not be empty!");
    }

    Table fromTable =
        new Table(fromTableName).setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);
    Table toTable =
        new Table(toTableName).setUnsetCatalogAndSchema(currentCatalogName, currentSchemaName);
    JdbcTable jdbcTable = getTable(toTable);
    if (jdbcTable == null) {
      throw new TableNotFoundException(toTable.getFullyQualifiedName(), List.of());
    }

    JdbcCatalog fromCatalog = catalogs.get(fromTable.getUnquotedCatalogName());
    if (fromCatalog != null) {
      JdbcSchema fromSchema = fromCatalog.get(fromTable.getUnquotedSchemaName());
      if (fromSchema != null) {
        fromSchema.synonyms.remove(fromTable.getUnquotedName(), jdbcTable);
        fromSchema.remove(fromTable.getUnquotedName(), jdbcTable);
      } else {
        throw new SchemaNotFoundException(fromTable.getUnquotedSchemaName());
      }
    } else {
      throw new CatalogNotFoundException(fromTable.getUnquotedCatalogName());
    }

    JdbcCatalog toCatalog = catalogs.get(toTable.getUnquotedCatalogName());
    if (toCatalog != null) {
      JdbcSchema toSchema = toCatalog.get(toTable.getUnquotedSchemaName());
      if (toSchema != null) {
        toSchema.remove(fromTable.getUnquotedName(), jdbcTable);
      } else {
        throw new SchemaNotFoundException(fromTable.getUnquotedSchemaName());
      }
    } else {
      throw new CatalogNotFoundException(fromTable.getUnquotedCatalogName());
    }
  }
}
