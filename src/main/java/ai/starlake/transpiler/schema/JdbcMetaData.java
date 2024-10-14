/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI
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

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.starlake.transpiler.schema.JdbcUtils.DatabaseSpecific;

/**
 * The type Jdbc metadata.
 */
@JsonPropertyOrder({"databaseType","currentCatalog","currentSchema","catalogSeparator","catalogs"})
@JsonIncludeProperties({"currentCatalog","currentSchema","catalogSeparator","databaseType","catalogs"})
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public final class JdbcMetaData implements DatabaseMetaData {
  public final static Logger LOGGER = Logger.getLogger(JdbcMetaData.class.getName());
  public static final Map<Integer, String> SQL_TYPE_NAME_MAP = new HashMap<>();

  private CaseInsensitiveLinkedHashMap<JdbcCatalog> catalogs =
      new CaseInsensitiveLinkedHashMap<>();
  private String currentCatalogName;
  private String currentSchemaName;
  private String catalogSeparator = ".";

  @JsonIgnore 
  private final CaseInsensitiveLinkedHashMap<Table> fromTables =
      new CaseInsensitiveLinkedHashMap<>() {};

  @JsonIgnore 
  private final CaseInsensitiveLinkedHashMap<Table> naturalJoinedTables =
      new CaseInsensitiveLinkedHashMap<>();
  @JsonIgnore 
  private final CaseInsensitiveLinkedHashMap<Column> leftUsingJoinedColumns =
      new CaseInsensitiveLinkedHashMap<>();
  @JsonIgnore 
  private final CaseInsensitiveLinkedHashMap<Column> rightUsingJoinedColumns =
      new CaseInsensitiveLinkedHashMap<>();

  private DatabaseSpecific databaseType = DatabaseSpecific.OTHER;
  
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
   * @param con the physical database connection
   * @throws SQLException when the database fails to return CURRENT_CATALOG or CURRENT_SCHEMA
   */
  public JdbcMetaData(Connection con) throws SQLException {
	DatabaseMetaData metaData = con.getMetaData();
	this.databaseType = JdbcUtils.DatabaseSpecific.getType(metaData.getDatabaseProductName());
	
    try (Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(this.databaseType.getCurrentSchemaQuery())) {
      rs.next();
      currentCatalogName = rs.getString(1);
      currentSchemaName = rs.getString(2);
    }
    
    for (JdbcCatalog jdbcCatalog : JdbcCatalog.getCatalogs(metaData)) {
      put(jdbcCatalog);
    }

    for (JdbcSchema jdbcSchema : JdbcSchema.getSchemas(metaData)) {
      put(jdbcSchema);
    }

    for (JdbcTable jdbcTable : JdbcTable.getTables(metaData,this.currentCatalogName,this.currentSchemaName)) {
      put(jdbcTable);
      jdbcTable.getColumns(metaData);
      if (jdbcTable.tableType.contains("TABLE")) {
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
    /*different DBs don't return correct catalog+schema info/hierarchy in getSchemas()
     *it is fixed here by adding missing catalogs and/or schemas
     */
	  
	JdbcCatalog jdbcCatalog = catalogs.get(jdbcTable.tableCatalog.toUpperCase());
    if (jdbcCatalog==null) {
    	jdbcCatalog = new JdbcCatalog(jdbcTable.tableCatalog, null);
    	catalogs.put(jdbcCatalog.tableCatalog, jdbcCatalog);
    }
    JdbcSchema jdbcSchema = jdbcCatalog.get(jdbcTable.tableSchema.toUpperCase());
    if (jdbcSchema==null) {
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
        t.add(t.tableCatalog, t.tableSchema, t.tableName,
            rsMetaData.getColumnLabel(i) != null && !rsMetaData.getColumnLabel(i).isEmpty()
                ? rsMetaData.getColumnLabel(i)
                : rsMetaData.getColumnName(i),
            rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
            rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
            "", rsMetaData.getColumnDisplaySize(i), i, "", 
            rsMetaData.getScopeCatalog(i) !=null && !rsMetaData.getScopeCatalog(i).isEmpty() ? rsMetaData.getScopeCatalog(i) : rsMetaData.getCatalogName(i),
            rsMetaData.getScopeSchema(i) != null && !rsMetaData.getScopeSchema(i).isEmpty() ? rsMetaData.getScopeSchema(i) : rsMetaData.getSchemaName(i),
            rsMetaData.getScopeTable(i) !=null && !rsMetaData.getScopeTable(i).isEmpty() ? rsMetaData.getScopeTable(i) : rsMetaData.getTableName(i),
            rsMetaData.getColumnName(i),
            null, "", "");
        /*
         * String tableCatalog, String tableSchema, String tableName,
      String columnName, Integer dataType, String typeName, Integer columnSize,
      Integer decimalDigits, Integer numericPrecisionRadix, Integer nullable, String remarks,
      String columnDefinition, Integer characterOctetLength, Integer ordinalPosition,
      String isNullable, String scopeCatalog, String scopeSchema, String scopeTable, String scopeColumn,
      Short sourceDataType, String isAutomaticIncrement, String isGeneratedColumn)
         */
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
      LOGGER.info(
          "Available catalogues: " + Arrays.deepToString(catalogs.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Catalog " + catalogName + " does not exist in the DatabaseMetaData.");
    }

    JdbcSchema jdbcSchema =
        jdbcCatalog.get(schemaName == null || schemaName.isEmpty() ? currentSchemaName
            : schemaName.replaceAll("^\"|\"$", ""));
    if (jdbcSchema == null) {
      LOGGER.info("Available schema: "
          + Arrays.deepToString(jdbcCatalog.schemas.keySet().toArray(new String[0])));
      throw new RuntimeException(
          "Schema " + schemaName + " does not exist in the given Catalog " + catalogName);
    }

    if (tableName != null && !tableName.isEmpty()) {
      JdbcTable jdbcTable = jdbcSchema.get(tableName.replaceAll("^\"|\"$", ""));

      if (jdbcTable == null) {
        LOGGER.info("Available tables: "
            + Arrays.deepToString(jdbcSchema.tables.keySet().toArray(new String[0])));
        // throw new RuntimeException(
        // "Table " + tableName + " does not exist in the given Schema " + schemaName);
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
    return DatabaseMetaData.sqlStateSQL;
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

  private static JdbcTable getJdbcTable(JdbcTable table) {
    JdbcTable table1 = new JdbcTable(table.tableCatalog, table.tableSchema, table.tableName,
        table.tableType, table.remarks, table.typeCatalog, table.typeSchema, table.typeName,
        table.selfReferenceColName, table.referenceGeneration);
    for (JdbcColumn column : table.columns.values()) {
      JdbcColumn column1 = new JdbcColumn(column.tableCatalog, column.tableSchema, column.tableName,
          column.columnName, column.dataType, column.typeName, column.columnSize,
          column.decimalDigits, column.numericPrecisionRadix, column.nullable, column.remarks,
          column.columnDefinition, column.characterOctetLength, column.ordinalPosition,
          column.isNullable, column.scopeCatalog, column.scopeSchema, column.scopeTable, column.scopeColumn,
          column.sourceDataType, column.isAutomaticIncrement, column.isGeneratedColumn,
          column.getExpression());
      table1.add(column1);
    }
    return table1;
  }

  @JsonProperty("currentCatalog") 
  public String getCurrentCatalogName() {
    return currentCatalogName;
  }

  @JsonProperty("currentSchema") 
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

  @JsonProperty("databaseType") 
  public String getDatabaseType() {
	  return databaseType.name();
  }

  public void setDatabaseType(String databaseType) {
	  this.databaseType = DatabaseSpecific.valueOf(databaseType);
  }

  @JsonProperty("catalogs") 
  public List<JdbcCatalog> getCatalogsList(){
	  return new ArrayList<JdbcCatalog>(this.catalogs.values());
  }

  public void setCatalogsList(List<JdbcCatalog> catalogs) {
	  for(JdbcCatalog item:catalogs) {
		  this.put(item);
	  }
  }


  public void setCurrentCatalogName(String currentCatalogName) {
	  this.currentCatalogName = currentCatalogName;
  }

  public void setCurrentSchemaName(String currentSchemaName) {
	  this.currentSchemaName = currentSchemaName;
  }
  
  /**
   * Serialize this MetaData object (and all its children) to JSON
   * 
   * @return String containing JSON
   */
  public String toJson() {
	  // Use Jackson's ObjectMapper to serialize to JSON
	  ObjectMapper objectMapper = new ObjectMapper();
	  // Serialize the department object to JSON
	  return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);


  }
  

  /**
   *  Serialize this MetaData object (and all its children) to JSON
   * 
   * @param writer the Writer object through which the JSON should be output
   */
  public void toJson(Writer writer) {
	  ObjectMapper objectMapper = new ObjectMapper();
	  objectMapper.writeValue(writer, this);
  }


  /**
   * Read/deserialize JdbcMetaData object from JSON
   * 
   * @param jsonString representing the metadata
   * @return
   */
  public static JdbcMetaData fromJson(String jsonString) {
	  ObjectMapper objectMapper = new ObjectMapper();
	  return objectMapper.readValue(jsonString, JdbcMetaData.class);
  }

  /**
   * Read/deserialize JdbcMetaData object from JSON
   * 
   * @param json the Reader object through which to read JSON data
   * @return
   */
  public static JdbcMetaData fromJson(Reader json) {
	  ObjectMapper objectMapper = new ObjectMapper();
	  return objectMapper.readValue(json, JdbcMetaData.class);
  }

  
}
