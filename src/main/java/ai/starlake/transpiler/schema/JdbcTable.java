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

import net.sf.jsqlparser.schema.Column;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JdbcTable implements Comparable<JdbcTable> {
  public static final Logger LOGGER = Logger.getLogger(JdbcTable.class.getName());

  public String tableCatalog;
  public String tableSchema;
  public String tableName;
  public String tableType;
  public String remarks;
  public String typeCatalog;
  public String typeSchema;
  public String typeName;
  public String selfReferenceColName;
  public String referenceGeneration;

  public CaseInsensitiveLinkedHashMap<JdbcColumn> columns = new CaseInsensitiveLinkedHashMap<>();
  public CaseInsensitiveLinkedHashMap<JdbcIndex> indices = new CaseInsensitiveLinkedHashMap<>();
  public JdbcPrimaryKey primaryKey = null;

  public JdbcTable(String tableCatalog, String tableSchema, String tableName, String tableType,
      String remarks, String typeCatalog, String typeSchema, String typeName,
      String selfReferenceColName, String referenceGeneration) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.tableType = tableType;
    this.remarks = remarks;
    this.typeCatalog = typeCatalog;
    this.typeSchema = typeSchema;
    this.typeName = typeName;
    this.selfReferenceColName = selfReferenceColName;
    this.referenceGeneration = referenceGeneration;
  }

  public JdbcTable(String tableCatalog, String tableSchema, String tableName) {
    this(tableCatalog, tableSchema, tableName, "TABLE", "Virtually generated", tableCatalog,
        tableSchema, "TABLE", "", "");
  }

  public JdbcTable(JdbcCatalog catalog, JdbcSchema schema, String tableName) {
    this(catalog.tableCatalog, schema.tableSchema, tableName);
  }

  public JdbcTable() {}

  public static Collection<JdbcTable> getTables(DatabaseMetaData metaData, String currentCatalog,
      String currentSchema) throws SQLException {
    ArrayList<JdbcTable> jdbcTables = new ArrayList<>();

    JdbcUtils.DatabaseSpecific dbSpecific =
        JdbcUtils.DatabaseSpecific.getType(metaData.getDatabaseProductName());

    try (ResultSet rs = metaData.getTables(null, null, "%", dbSpecific.tableTypes);) {
      while (rs.next()) {
        // TABLE_CATALOG String => catalog name (may be null)
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CAT", currentCatalog);

        // TABLE_SCHEM String => schema name
        String tableSchema = JdbcUtils.getStringSafe(rs, "TABLE_SCHEM", currentSchema);

        // Should this schema be included or ignored ?
        if (!dbSpecific.processSchema(tableSchema)) {
          continue;
        }

        // TABLE_NAME String => table name
        String tableName = JdbcUtils.getStringSafe(rs, "TABLE_NAME");

        // TABLE_TYPE String => table type. Typical
        // types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL
        // TEMPORARY",
        // "ALIAS", "SYNONYM".
        String tableType = JdbcUtils.getStringSafe(rs, "TABLE_TYPE");

        // REMARKS String => explanatory comment on the table(may be null)
        String remarks = JdbcUtils.getStringSafe(rs, "REMARKS");

        // TYPE_CAT String => the types catalog (may be null)
        String typeCatalog = JdbcUtils.getStringSafe(rs, "TYPE_CAT");

        // TYPE_SCHEM String => the types schema (may be null)
        String typeSchema = JdbcUtils.getStringSafe(rs, "TYPE_SCHEM");

        // TYPE_NAME String => type name (may be null)
        String typeName = JdbcUtils.getStringSafe(rs, "TYPE_NAME");

        // SELF_REFERENCING_COL_NAME
        // String => name of the designated "identifier" column of a typed table (may be
        // null)
        String selfReferenceColName = JdbcUtils.getStringSafe(rs, "SELF_REFERENCING_COL_NAME");

        // REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME
        // are created.
        // Values are "SYSTEM", "USER", "DERIVED". (may be null)
        String referenceGeneration = JdbcUtils.getStringSafe(rs, "REF_GENERATION");

        JdbcTable jdbcTable = new JdbcTable(tableCatalog, tableSchema, tableName, tableType,
            remarks, typeCatalog, typeSchema, typeName, selfReferenceColName, referenceGeneration);

        jdbcTables.add(jdbcTable);
      }

    }
    return jdbcTables;
  }

  public void getColumns(DatabaseMetaData metaData) throws SQLException {
    try (ResultSet rs = metaData.getColumns(tableCatalog, tableSchema, tableName, "%");) {
      while (rs.next()) {
        // TABLE_CATALOG String => catalog name (may be null)
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CAT", "");

        // TABLE_SCHEM String => schema name
        String tableSchema = JdbcUtils.getStringSafe(rs, "TABLE_SCHEM", "");

        // TABLE_NAME String => table name
        String tableName = JdbcUtils.getStringSafe(rs, "TABLE_NAME");

        // COLUMN_NAME String => column name
        String columnName = JdbcUtils.getStringSafe(rs, "COLUMN_NAME");

        // DATA_TYPE int => SQL type from java.sql.Types
        Integer dataType = JdbcUtils.getIntSafe(rs, "DATA_TYPE");

        // TYPE_NAME String => Data source dependent type name, for a UDT the type name
        // is fully
        // qualified
        String typeName = JdbcUtils.getStringSafe(rs, "TYPE_NAME");

        // COLUMN_SIZE int => column size.
        Integer columnSize = JdbcUtils.getIntSafe(rs, "COLUMN_SIZE");

        // DECIMAL_DIGITS int => the number of fractional digits.
        // Null is returned for data types where DECIMAL_DIGITS is not applicable.
        Integer decimalDigits = JdbcUtils.getIntSafe(rs, "DECIMAL_DIGITS");

        // NUM_PREC_RADIX int => Radix (typically either 10 or 2)
        Integer numericPrecicionRadix = JdbcUtils.getIntSafe(rs, "NUM_PREC_RADIX");

        // NULLABLE int => is NULL allowed.
        Integer nullable = JdbcUtils.getIntSafe(rs, "NULLABLE");

        // REMARKS String => comment describing column (may be null)
        String remarks = JdbcUtils.getStringSafe(rs, "REMARKS");

        // COLUMN_DEF String => default value for the column, which should be
        // interpreted as a string when the value is enclosed in single quotes (may be
        // null)
        String columnDefinition = JdbcUtils.getStringSafe(rs, "COLUMN_DEF");

        // CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the
        // column
        Integer characterOctetLength = JdbcUtils.getIntSafe(rs, "CHAR_OCTET_LENGTH");

        // ORDINAL_POSITION int => index of column in table (starting at 1)
        Integer ordinalPosition = JdbcUtils.getIntSafe(rs, "ORDINAL_POSITION");

        // IS_NULLABLE String => ISO rules are used to determine the nullability for a
        // column.
        String isNullable = JdbcUtils.getStringSafe(rs, "IS_NULLABLE");

        // SCOPE_CATALOG String => catalog of table that is the scope of a reference
        // attribute
        // (null if DATA_TYPE isn't REF)
        String scopeCatalog = JdbcUtils.getStringSafe(rs, "SCOPE_CATALOG");

        // SCOPE_SCHEMA String => schema of table that is the scope of a reference
        // attribute
        // (null if the DATA_TYPE isn't REF)
        String scopeSchema = JdbcUtils.getStringSafe(rs, "SCOPE_SCHEMA");

        // SCOPE_TABLE String => table name that this the scope of a reference attribute
        // (null if the DATA_TYPE isn't REF)
        String scopeTable = JdbcUtils.getStringSafe(rs, "SCOPE_TABLE");

        // SCOPE_COLUMNT String => table name that this the scope of a reference
        // attribute
        // (null if the DATA_TYPE isn't REF)
        String scopeColumn = JdbcUtils.getStringSafe(rs, "SCOPE_COLUMN");

        // SOURCE_DATA_TYPE short => source type of a distinct type or user-generated
        // Ref type,
        // SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or
        // user-generated REF)
        Short sourceDataType = JdbcUtils.getShortSafe(rs, "SOURCE_DATA_TYPE");

        // IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
        String isAutoIncrement = JdbcUtils.getStringSafe(rs, "IS_AUTOINCREMENT");

        // IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
        String isGeneratedColumn = JdbcUtils.getStringSafe(rs, "IS_GENERATEDCOLUMN");

        JdbcColumn jdbcColumn = new JdbcColumn(tableCatalog, tableSchema, tableName, columnName,
            dataType, typeName, columnSize, decimalDigits, numericPrecicionRadix, nullable, remarks,
            columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog,
            scopeSchema, scopeTable, scopeColumn, sourceDataType, isAutoIncrement,
            isGeneratedColumn, new Column(columnName));

        columns.put(jdbcColumn.columnName, jdbcColumn);
      }

    }
  }

  public void getIndices(DatabaseMetaData metaData, boolean approximate) throws SQLException {
    try (ResultSet rs =
        metaData.getIndexInfo(tableCatalog, tableSchema, tableName, false, approximate);) {
      LOGGER.fine(tableCatalog + "." + tableSchema + "." + tableName);

      while (rs.next()) {
        // TABLE_CATALOG String => catalog name(may be null)
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CAT", "");

        // TABLE_SCHEM String => schema name
        String tableSchema = JdbcUtils.getStringSafe(rs, "TABLE_SCHEM", "");

        // TABLE_NAME String => table name
        String tableName = JdbcUtils.getStringSafe(rs, "TABLE_NAME");

        // NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is
        // tableIndexStatistic
        Boolean nonUnique = JdbcUtils.getBooleanSafe(rs, "NON_UNIQUE");

        // INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is
        // tableIndexStatistic
        String indexQualifier = JdbcUtils.getStringSafe(rs, "INDEX_QUALIFIER");

        // INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
        String indexName = JdbcUtils.getStringSafe(rs, "INDEX_NAME", "");

        // TYPE short => index type:
        Short type = JdbcUtils.getShortSafe(rs, "TYPE");

        // ORDINAL_POSITION short => column sequence number within index; zero when TYPE
        // is
        // tableIndexStatistic
        Short ordinalPosition = JdbcUtils.getShortSafe(rs, "ORDINAL_POSITION");

        // COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
        String columnName = JdbcUtils.getStringSafe(rs, "COLUMN_NAME");

        // ASC_OR_DESC String => column sort sequence,
        // "A" => ascending,
        // "D" => descending,
        // may be null if sort sequence is not supported;
        // null when TYPE is tableIndexStatistic
        String ascOrDesc = JdbcUtils.getStringSafe(rs, "ASC_OR_DESC");

        // CARDINALITY long => When TYPE is
        // tableIndexStatistic, then this
        // is the number of rows in the table; otherwise, it is the number
        // of unique values in the index.
        Long cardinality = rs.getLong("CARDINALITY");

        // PAGES long => When TYPE is tableIndexStatistic then
        // this is the number
        // of pages used for the table, otherwise it is the number of pages used
        // for the current index.
        Long pages = rs.getLong("PAGES");

        // FILTER_CONDITION String => Filter condition, if any. (may be null)
        String filterCondition = JdbcUtils.getStringSafe(rs, "FILTER_CONDITION");

        JdbcIndex jdbcIndex = indices.get(indexName.toUpperCase());
        if (jdbcIndex == null) {
          jdbcIndex = new JdbcIndex(tableCatalog, tableSchema, tableName, nonUnique, indexQualifier,
              indexName, type);
          indices.put(jdbcIndex.indexName, jdbcIndex);
        }
        jdbcIndex.put(ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition);
      }

    } catch (SQLFeatureNotSupportedException ex1) {
      LOGGER.fine("This database does not support Index Information yet.");
    }
  }

  public void getPrimaryKey(DatabaseMetaData metaData) throws SQLException {
    try (ResultSet rs = metaData.getPrimaryKeys(tableCatalog, tableSchema, tableName);) {
      TreeMap<Short, String> columnNames = new TreeMap<>();

      while (rs.next()) {
        // TABLE_CATALOG String => catalog name (may be null)
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CAT");

        // TABLE_SCHEM String => schema name
        String tableSchema = JdbcUtils.getStringSafe(rs, "TABLE_SCHEM");

        // TABLE_NAME String => table name
        String tableName = JdbcUtils.getStringSafe(rs, "TABLE_NAME");

        // COLUMN_NAME String => column name
        String columnName = JdbcUtils.getStringSafe(rs, "COLUMN_NAME");

        // KEY_SEQ short => sequence number within
        // primary key(a value of 1 represents the first column of the primary key, a
        // value of 2
        // would
        // represent the second column within the primary key).
        Short keySequence = JdbcUtils.getShortSafe(rs, "KEY_SEQ");

        // PK_NAME String => primary key name (may be null)
        String primaryKeyName = JdbcUtils.getStringSafe(rs, "PK_NAME");

        if (primaryKey == null) {
          primaryKey = new JdbcPrimaryKey(tableCatalog, tableSchema, tableName, primaryKeyName);
        }

        columnNames.put(keySequence, columnName);
      }

      for (Entry<Short, String> e : columnNames.entrySet()) {
        primaryKey.columnNames.add(e.getValue());
      }

    }
  }

  @Override
  public int compareTo(JdbcTable o) {
    int compareTo = tableCatalog.compareToIgnoreCase(o.tableCatalog);

    if (compareTo == 0) {
      compareTo = tableSchema.compareToIgnoreCase(o.tableSchema);
    }

    if (compareTo == 0) {
      compareTo = tableName.compareToIgnoreCase(o.tableName);
    }

    return compareTo;
  }

  public JdbcColumn add(JdbcColumn jdbcColumn) {
    int i = 0;
    while (columns.containsKey(jdbcColumn.columnName + (i > 0 ? "_" + i : ""))) {
      i++;
    }

    if (i > 0) {
      jdbcColumn.columnName = jdbcColumn.columnName + "_" + i;

    }
    columns.put(jdbcColumn.columnName, jdbcColumn);
    return jdbcColumn;
  }

  public JdbcColumn add(String tableCatalog, String tableSchema, String tableName,
      String columnName, Integer dataType, String typeName, Integer columnSize,
      Integer decimalDigits, Integer numericPrecisionRadix, Integer nullable, String remarks,
      String columnDefinition, Integer characterOctetLength, Integer ordinalPosition,
      String isNullable, String scopeCatalog, String scopeSchema, String scopeTable,
      String scopeColumn, Short sourceDataType, String isAutomaticIncrement,
      String isGeneratedColumn) {
    JdbcColumn jdbcColumn = new JdbcColumn(tableCatalog, tableSchema, tableName, columnName,
        dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks,
        columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog,
        scopeSchema, scopeTable, scopeColumn, sourceDataType, isAutomaticIncrement,
        isGeneratedColumn, new Column(columnName));
    return add(jdbcColumn);
  }

  public boolean containsKey(String columnName) {
    return columns.containsKey(columnName);
  }

  public boolean contains(JdbcColumn jdbcColumn) {
    return columns.containsKey(jdbcColumn.columnName);
  }

  public JdbcIndex put(JdbcIndex jdbcIndex) {
    return indices.put(jdbcIndex.indexName.toUpperCase(), jdbcIndex);
  }

  public boolean containsIndexKey(String indexName) {
    return indices.containsKey(indexName.toUpperCase());
  }

  public JdbcIndex get(String indexName) {
    return indices.get(indexName.toUpperCase());
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcTable)) {
      return false;
    }

    JdbcTable jdbcTable = (JdbcTable) o;

    if (!Objects.equals(tableCatalog, jdbcTable.tableCatalog)) {
      return false;
    }
    if (!Objects.equals(tableSchema, jdbcTable.tableSchema)) {
      return false;
    }
    if (!tableName.equals(jdbcTable.tableName)) {
      return false;
    }
    if (!tableType.equals(jdbcTable.tableType)) {
      return false;
    }
    if (!Objects.equals(remarks, jdbcTable.remarks)) {
      return false;
    }
    if (!Objects.equals(typeCatalog, jdbcTable.typeCatalog)) {
      return false;
    }
    if (!Objects.equals(typeSchema, jdbcTable.typeSchema)) {
      return false;
    }
    if (!Objects.equals(typeName, jdbcTable.typeName)) {
      return false;
    }
    if (!Objects.equals(selfReferenceColName, jdbcTable.selfReferenceColName)) {
      return false;
    }
    if (!Objects.equals(referenceGeneration, jdbcTable.referenceGeneration)) {
      return false;
    }
    if (!columns.equals(jdbcTable.columns)) {
      return false;
    }
    if (!Objects.equals(indices, jdbcTable.indices)) {
      return false;
    }
    return Objects.equals(primaryKey, jdbcTable.primaryKey);
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public int hashCode() {
    int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
    result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
    result = 31 * result + tableName.hashCode();
    result = 31 * result + tableType.hashCode();
    result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
    result = 31 * result + (typeCatalog != null ? typeCatalog.hashCode() : 0);
    result = 31 * result + (typeSchema != null ? typeSchema.hashCode() : 0);
    result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
    result = 31 * result + (selfReferenceColName != null ? selfReferenceColName.hashCode() : 0);
    result = 31 * result + (referenceGeneration != null ? referenceGeneration.hashCode() : 0);
    result = 31 * result + columns.hashCode();
    result = 31 * result + (indices != null ? indices.hashCode() : 0);
    result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0);
    return result;
  }

  public JdbcColumn put(String key, JdbcColumn value) {
    return columns.put(key, value);
  }

  public boolean containsValue(JdbcColumn value) {
    return columns.containsValue(value);
  }

  public int size() {
    return columns.size();
  }

  public JdbcColumn replace(String key, JdbcColumn value) {
    return columns.replace(key, value);
  }

  public boolean isEmpty() {
    return columns.isEmpty();
  }

  public JdbcColumn compute(String key,
      BiFunction<? super String, ? super JdbcColumn, ? extends JdbcColumn> remappingFunction) {
    return columns.compute(key, remappingFunction);
  }

  public void putAll(Map<? extends String, ? extends JdbcColumn> m) {
    columns.putAll(m);
  }

  public Collection<JdbcColumn> values() {
    return columns.values();
  }

  public boolean replace(String key, JdbcColumn oldValue, JdbcColumn newValue) {
    return columns.replace(key, oldValue, newValue);
  }

  public void forEach(BiConsumer<? super String, ? super JdbcColumn> action) {
    columns.forEach(action);
  }

  public JdbcColumn getOrDefault(String key, JdbcColumn defaultValue) {
    return columns.getOrDefault(key, defaultValue);
  }

  public boolean remove(String key, JdbcColumn value) {
    return columns.remove(key, value);
  }

  public JdbcColumn computeIfPresent(String key,
      BiFunction<? super String, ? super JdbcColumn, ? extends JdbcColumn> remappingFunction) {
    return columns.computeIfPresent(key, remappingFunction);
  }

  public void replaceAll(
      BiFunction<? super String, ? super JdbcColumn, ? extends JdbcColumn> function) {
    columns.replaceAll(function);
  }

  public JdbcColumn computeIfAbsent(String key,
      Function<? super String, ? extends JdbcColumn> mappingFunction) {
    return columns.computeIfAbsent(key, mappingFunction);
  }

  public JdbcColumn putIfAbsent(String key, JdbcColumn value) {
    return columns.putIfAbsent(key, value);
  }

  public JdbcColumn merge(String key, JdbcColumn value,
      BiFunction<? super JdbcColumn, ? super JdbcColumn, ? extends JdbcColumn> remappingFunction) {
    return columns.merge(key, value, remappingFunction);
  }

  public JdbcColumn remove(String key) {
    return columns.remove(key);
  }

  public void clear() {
    columns.clear();
  }

  public Set<Entry<String, JdbcColumn>> entrySet() {
    return columns.entrySet();
  }

  public Set<String> keySet() {
    return columns.keySet();
  }

  /*
   * following for JSON (de)serialization
   */


  public List<JdbcColumn> getColumns() {
    return new ArrayList<JdbcColumn>(this.columns.values());
  }

  public void setColumns(List<JdbcColumn> columns) {
    for (JdbcColumn item : columns) {
      item.tableName = this.tableName;
      item.tableCatalog = this.tableCatalog;
      item.tableSchema = this.tableSchema;
      add(item);
    }
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }



}
