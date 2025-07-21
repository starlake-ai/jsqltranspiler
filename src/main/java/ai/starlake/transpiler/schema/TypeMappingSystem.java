/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI <hayssam.saleh@starlake.ai>
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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Type mapping system that handles: 1. JDBC Schema types to DDL column types (using YAML config) 2.
 * ResultSetMetaData back to type names (reverse mapping)
 */
public class TypeMappingSystem {

  // Forward mapping: TypeName -> DDL for specific database
  private static final Map<String, String> TYPE_TO_H2_DDL = new HashMap<>();
  private static final Map<String, String> TYPE_TO_DUCKDB_DDL = new HashMap<>();
  private static final Map<String, String> TYPE_TO_POSTGRES_DDL = new HashMap<>();

  // Reverse mapping: DDL/JDBC Type -> TypeName
  private static final Map<String, String> H2_DDL_TO_TYPE = new HashMap<>();
  private static final Map<String, String> DUCKDB_DDL_TO_TYPE = new HashMap<>();
  private static final Map<String, String> POSTGRES_DDL_TO_TYPE = new HashMap<>();
  private static final Map<Integer, String> JDBC_TYPE_TO_TYPE = new HashMap<>();

  static {
    initializeMappings();
  }

  /**
   * Initialize all mappings based on the YAML configuration All keys are stored in lowercase for
   * case-insensitive lookup
   */
  private static void initializeMappings() {
    // string
    putCaseInsensitive("string", "VARCHAR", "VARCHAR", "varchar(8000)");

    // variant
    putCaseInsensitive("variant", "JSON", "JSON", "JSONB");

    // integer/int
    putCaseInsensitive("integer", "INTEGER", "INTEGER", "INT");
    putCaseInsensitive("int", "INTEGER", "INTEGER", "INT");

    // byte
    putCaseInsensitive("byte", "TINYINT", "TINYINT", "INT");

    // double
    putCaseInsensitive("double", "DOUBLE", "DOUBLE", "DOUBLE PRECISION");

    // long
    putCaseInsensitive("long", "BIGINT", "BIGINT", "INT");

    // short
    putCaseInsensitive("short", "SMALLINT", "SMALLINT", "smallint");

    // boolean
    putCaseInsensitive("boolean", "BOOLEAN", "BOOLEAN", "BOOLEAN");

    // timestamp (all ISO variants map to same DDL)
    String[] timestampTypes = {"timestamp", "basic_iso_date", "iso_local_date", "iso_offset_date",
        "iso_date", "iso_local_date_time", "iso_offset_date_time", "iso_zoned_date_time",
        "iso_date_time", "iso_ordinal_date", "iso_week_date", "iso_instant", "rfc_1123_date_time"};
    for (String tsType : timestampTypes) {
      putCaseInsensitive(tsType, "TIMESTAMP WITH TIME ZONE", "TIMESTAMPTZ", "TIMESTAMP");
    }

    // decimal
    putCaseInsensitive("decimal", "DECIMAL", "DECIMAL", "DECIMAL");

    // date
    putCaseInsensitive("date", "DATE", "DATE", "DATE");

    // Initialize reverse mappings
    initializeReverseMappings();
    initializeJdbcTypeMappings();
  }

  /**
   * Helper method to add case-insensitive mappings for all databases
   */
  private static void putCaseInsensitive(String typeName, String h2Type, String duckdbType,
      String postgresType) {
    String lowerType = typeName.toLowerCase();
    TYPE_TO_H2_DDL.put(lowerType, h2Type);
    TYPE_TO_DUCKDB_DDL.put(lowerType, duckdbType);
    TYPE_TO_POSTGRES_DDL.put(lowerType, postgresType);
  }

  /**
   * Initialize reverse mappings from DDL types back to type names All keys stored in uppercase for
   * case-insensitive lookup
   */
  private static void initializeReverseMappings() {
    // H2 reverse mappings
    putReverseCaseInsensitive("VARCHAR", "string");
    putReverseCaseInsensitive("JSON", "variant");
    putReverseCaseInsensitive("INTEGER", "integer");
    putReverseCaseInsensitive("TINYINT", "byte");
    putReverseCaseInsensitive("DOUBLE", "double");
    putReverseCaseInsensitive("BIGINT", "long");
    putReverseCaseInsensitive("SMALLINT", "short");
    putReverseCaseInsensitive("BOOLEAN", "boolean");
    putReverseCaseInsensitive("TIMESTAMP WITH TIME ZONE", "timestamp");
    putReverseCaseInsensitive("TIMESTAMPTZ", "timestamp");
    putReverseCaseInsensitive("TIMESTAMP", "timestamp");
    putReverseCaseInsensitive("DECIMAL", "decimal");
    putReverseCaseInsensitive("DATE", "date");

    // DuckDB reverse mappings (similar to H2 but some differences)
    DUCKDB_DDL_TO_TYPE.putAll(H2_DDL_TO_TYPE);

    // PostgreSQL reverse mappings
    putPostgresReverseCaseInsensitive("varchar(8000)", "string");
    putPostgresReverseCaseInsensitive("VARCHAR", "string");
    putPostgresReverseCaseInsensitive("JSONB", "variant");
    putPostgresReverseCaseInsensitive("INT", "integer");
    putPostgresReverseCaseInsensitive("INTEGER", "integer");
    putPostgresReverseCaseInsensitive("DOUBLE PRECISION", "double");
    putPostgresReverseCaseInsensitive("BIGINT", "long");
    putPostgresReverseCaseInsensitive("smallint", "short");
    putPostgresReverseCaseInsensitive("SMALLINT", "short");
    putPostgresReverseCaseInsensitive("BOOLEAN", "boolean");
    putPostgresReverseCaseInsensitive("TIMESTAMP", "timestamp");
    putPostgresReverseCaseInsensitive("DECIMAL", "decimal");
    putPostgresReverseCaseInsensitive("DATE", "date");
  }

  /**
   * Helper method to add case-insensitive reverse mappings for H2 and DuckDB
   */
  private static void putReverseCaseInsensitive(String ddlType, String typeName) {
    String upperDDL = ddlType.toUpperCase();
    H2_DDL_TO_TYPE.put(upperDDL, typeName);
    DUCKDB_DDL_TO_TYPE.put(upperDDL, typeName);
  }

  /**
   * Helper method to add case-insensitive reverse mappings for PostgreSQL
   */
  private static void putPostgresReverseCaseInsensitive(String ddlType, String typeName) {
    String upperDDL = ddlType.toUpperCase();
    POSTGRES_DDL_TO_TYPE.put(upperDDL, typeName);
  }

  /**
   * Initialize JDBC type code to type name mappings
   */
  private static void initializeJdbcTypeMappings() {
    JDBC_TYPE_TO_TYPE.put(Types.VARCHAR, "string");
    JDBC_TYPE_TO_TYPE.put(Types.CHAR, "string");
    JDBC_TYPE_TO_TYPE.put(Types.LONGVARCHAR, "string");
    JDBC_TYPE_TO_TYPE.put(Types.CLOB, "string");
    JDBC_TYPE_TO_TYPE.put(Types.NVARCHAR, "string");
    JDBC_TYPE_TO_TYPE.put(Types.NCHAR, "string");

    JDBC_TYPE_TO_TYPE.put(Types.INTEGER, "integer");
    JDBC_TYPE_TO_TYPE.put(Types.TINYINT, "byte");
    JDBC_TYPE_TO_TYPE.put(Types.SMALLINT, "short");
    JDBC_TYPE_TO_TYPE.put(Types.BIGINT, "long");
    JDBC_TYPE_TO_TYPE.put(Types.DOUBLE, "double");
    JDBC_TYPE_TO_TYPE.put(Types.FLOAT, "double");
    JDBC_TYPE_TO_TYPE.put(Types.REAL, "double");
    JDBC_TYPE_TO_TYPE.put(Types.DECIMAL, "decimal");
    JDBC_TYPE_TO_TYPE.put(Types.NUMERIC, "decimal");

    JDBC_TYPE_TO_TYPE.put(Types.BOOLEAN, "boolean");
    JDBC_TYPE_TO_TYPE.put(Types.BIT, "boolean");

    JDBC_TYPE_TO_TYPE.put(Types.DATE, "date");
    JDBC_TYPE_TO_TYPE.put(Types.TIMESTAMP, "timestamp");
    JDBC_TYPE_TO_TYPE.put(Types.TIMESTAMP_WITH_TIMEZONE, "timestamp");
    JDBC_TYPE_TO_TYPE.put(Types.TIME, "timestamp");
    JDBC_TYPE_TO_TYPE.put(Types.TIME_WITH_TIMEZONE, "timestamp");

    // JSON/Object types
    JDBC_TYPE_TO_TYPE.put(Types.OTHER, "variant");
    JDBC_TYPE_TO_TYPE.put(Types.JAVA_OBJECT, "variant");
  }

  /**
   * Maps type name to DDL column type for specific database
   */
  public static String mapTypeToDDL(String typeName, String database, Integer columnSize,
      Integer decimalDigits) {
    if (typeName == null) {
      return "VARCHAR(255)";
    }

    String normalizedType = typeName.toLowerCase().trim();
    Map<String, String> mapping;

    switch (database.toLowerCase()) {
      case "h2":
        mapping = TYPE_TO_H2_DDL;
        break;
      case "duckdb":
        mapping = TYPE_TO_DUCKDB_DDL;
        break;
      case "postgres":
      case "postgresql":
        mapping = TYPE_TO_POSTGRES_DDL;
        break;
      default:
        mapping = TYPE_TO_H2_DDL; // Default to H2
    }

    String ddlType = mapping.get(normalizedType);
    if (ddlType == null) {
      return "VARCHAR(255)"; // Fallback
    }

    // Apply size/precision for certain types
    return applyPrecisionAndScale(ddlType, columnSize, decimalDigits);
  }

  /**
   * Apply precision and scale to DDL types where applicable
   */
  private static String applyPrecisionAndScale(String ddlType, Integer columnSize,
      Integer decimalDigits) {
    switch (ddlType.toUpperCase()) {
      case "VARCHAR":
        if (columnSize != null && columnSize > 0) {
          return "VARCHAR(" + columnSize + ")";
        }
        return "VARCHAR(255)";
      case "CHAR":
        if (columnSize != null && columnSize > 0) {
          return "CHAR(" + columnSize + ")";
        }
        return "CHAR(1)";
      case "DECIMAL":
        if (columnSize != null && decimalDigits != null && decimalDigits > 0) {
          return "DECIMAL(" + columnSize + "," + decimalDigits + ")";
        } else if (columnSize != null) {
          return "DECIMAL(" + columnSize + ")";
        }
        return "DECIMAL";
      case "FLOAT":
        if (columnSize != null && columnSize > 0) {
          return "FLOAT(" + columnSize + ")";
        }
        return "FLOAT";
      case "TIMESTAMP":
        if (decimalDigits != null && decimalDigits > 0) {
          return "TIMESTAMP(" + decimalDigits + ")";
        }
        return "TIMESTAMP";
      case "TIMESTAMP WITH TIME ZONE":
      case "TIMESTAMPTZ":
        if (decimalDigits != null && decimalDigits > 0) {
          return ddlType + "(" + decimalDigits + ")";
        }
        return ddlType;
      default:
        return ddlType;
    }
  }

  /**
   * Maps ResultSetMetaData back to type name
   */
  public static String mapResultSetToTypeName(ResultSetMetaData metaData, int columnIndex,
      String database) throws SQLException {
    int jdbcType = metaData.getColumnType(columnIndex);
    String typeName = metaData.getColumnTypeName(columnIndex);

    // First try direct type name mapping
    if (typeName != null) {
      String mappedType = mapDDLToTypeName(typeName, database);
      if (mappedType != null) {
        return mappedType;
      }
    }

    // Fall back to JDBC type mapping
    String typeFromJdbc = JDBC_TYPE_TO_TYPE.get(jdbcType);
    if (typeFromJdbc != null) {
      return typeFromJdbc;
    }

    // Ultimate fallback
    return "string";
  }

  /**
   * Maps DDL type name back to our type system
   */
  private static String mapDDLToTypeName(String ddlTypeName, String database) {
    if (ddlTypeName == null) {
      return null;
    }

    // Normalize - remove size/precision info
    String normalizedDDL = ddlTypeName.toUpperCase().replaceAll("\\(.*?\\)", "").trim();

    Map<String, String> reverseMapping;
    switch (database.toLowerCase()) {
      case "h2":
        reverseMapping = H2_DDL_TO_TYPE;
        break;
      case "duckdb":
        reverseMapping = DUCKDB_DDL_TO_TYPE;
        break;
      case "postgres":
      case "postgresql":
        reverseMapping = POSTGRES_DDL_TO_TYPE;
        break;
      default:
        reverseMapping = H2_DDL_TO_TYPE;
    }

    return reverseMapping.get(normalizedDDL);
  }

  /**
   * Enhanced column definition generator using the type mapping system
   */
  public static String generateColumnDefinition(JdbcColumn column, String database) {
    StringBuilder colDef = new StringBuilder();

    colDef.append(column.columnName).append(" ");

    // Use type mapping system
    String ddlType;
    if (column.typeName != null) {
      // Use type name if available
      ddlType = mapTypeToDDL(column.typeName, database, column.columnSize, column.decimalDigits);
    } else {
      // Fall back to JDBC type
      String typeName = JDBC_TYPE_TO_TYPE.get(column.dataType);
      ddlType = mapTypeToDDL(typeName, database, column.columnSize, column.decimalDigits);
    }

    colDef.append(ddlType);

    // Nullable constraint
    if (column.nullable != null && column.nullable == 0) {
      colDef.append(" NOT NULL");
    }

    // Default value
    if (column.columnDefinition != null && !column.columnDefinition.trim().isEmpty()) {
      colDef.append(" DEFAULT ").append(column.columnDefinition);
    }

    // Auto increment
    // if ("YES".equalsIgnoreCase(column.isAutoIncrement)) {
    // colDef.append(" AUTO_INCREMENT");
    // }

    return colDef.toString();
  }

  /**
   * Example usage for creating table DDL
   */
  public static String generateCreateTableDDL(JdbcTable table, String database,
      boolean includeSchema) {
    StringBuilder ddl = new StringBuilder();

    String fullTableName =
        includeSchema && table.tableSchema != null && !table.tableSchema.isEmpty()
            ? table.tableSchema + "." + table.tableName
            : table.tableName;

    ddl.append("CREATE TABLE ").append(fullTableName).append(" (\n");

    // Column definitions
    String[] columnDefs = table.getColumns().stream()
        .map(col -> "  " + generateColumnDefinition(col, database)).toArray(String[]::new);

    ddl.append(String.join(",\n", columnDefs));

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
}
