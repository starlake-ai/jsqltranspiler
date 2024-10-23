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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class JdbcSchema implements Comparable<JdbcSchema> {

  public static final Logger LOGGER = Logger.getLogger(JdbcSchema.class.getName());

  public String tableSchema;
  public String tableCatalog;

  public CaseInsensitiveLinkedHashMap<JdbcTable> tables = new CaseInsensitiveLinkedHashMap<>();

  public JdbcSchema(String tableSchema, String tableCatalog) {
    this.tableSchema = tableSchema != null ? tableSchema : "";
    this.tableCatalog = tableCatalog != null ? tableCatalog : "";
  }

  public JdbcSchema() {}

  public static Collection<JdbcSchema> getSchemas(DatabaseMetaData metaData) throws SQLException {
    ArrayList<JdbcSchema> jdbcSchemas = new ArrayList<>();

    try (ResultSet rs = metaData.getSchemas();) {

      while (rs.next()) {
        // TABLE_SCHEM String => schema name
        String tableSchema = JdbcUtils.getStringSafe(rs, "TABLE_SCHEM");
        // TABLE_CATALOG String => catalog name (may be null)
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CATALOG", "");
        if (tableSchema != null && !tableSchema.isBlank()) {
          JdbcSchema jdbcSchema = new JdbcSchema(tableSchema, tableCatalog);
          jdbcSchemas.add(jdbcSchema);
        }
      }
      // add <empty> schema as some DBs don't have the concept of schema for tables
      jdbcSchemas.add(new JdbcSchema("", ""));

    }
    return jdbcSchemas;
  }

  public JdbcTable put(JdbcTable jdbcTable) {
    return tables.put(jdbcTable.tableName.toUpperCase(), jdbcTable);
  }

  public JdbcTable get(String tableName) {
    return tables.get(tableName.replaceAll("^\"|\"$", "").toUpperCase());
  }

  @Override
  public int compareTo(JdbcSchema o) {
    int compareTo = tableCatalog.compareToIgnoreCase(o.tableCatalog);

    if (compareTo == 0) {
      compareTo = tableSchema.compareToIgnoreCase(o.tableSchema);
    }

    return compareTo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcSchema)) {
      return false;
    }

    JdbcSchema jdbcSchema = (JdbcSchema) o;

    if (!tableSchema.equals(jdbcSchema.tableSchema)) {
      return false;
    }
    if (!Objects.equals(tableCatalog, jdbcSchema.tableCatalog)) {
      return false;
    }
    return Objects.equals(tables, jdbcSchema.tables);
  }

  @Override
  public int hashCode() {
    int result = tableSchema.hashCode();
    result = 31 * result + (tableCatalog != null ? tableCatalog.hashCode() : 0);
    result = 31 * result + (tables != null ? tables.hashCode() : 0);
    return result;
  }

  public JdbcTable put(String key, JdbcTable value) {
    return tables.put(key, value);
  }

  public boolean containsValue(JdbcTable value) {
    return tables.containsValue(value);
  }

  public int size() {
    return tables.size();
  }

  public JdbcTable replace(String key, JdbcTable value) {
    return tables.replace(key, value);
  }

  public boolean isEmpty() {
    return tables.isEmpty();
  }

  public JdbcTable compute(String key,
      BiFunction<? super String, ? super JdbcTable, ? extends JdbcTable> remappingFunction) {
    return tables.compute(key, remappingFunction);
  }

  public void putAll(Map<? extends String, ? extends JdbcTable> m) {
    tables.putAll(m);
  }

  public Collection<JdbcTable> values() {
    return tables.values();
  }

  public boolean replace(String key, JdbcTable oldValue, JdbcTable newValue) {
    return tables.replace(key, oldValue, newValue);
  }

  public void forEach(BiConsumer<? super String, ? super JdbcTable> action) {
    tables.forEach(action);
  }

  public JdbcTable getOrDefault(String key, JdbcTable defaultValue) {
    return tables.getOrDefault(key, defaultValue);
  }

  public boolean remove(String key, JdbcTable value) {
    return tables.remove(key, value);
  }

  public JdbcTable computeIfPresent(String key,
      BiFunction<? super String, ? super JdbcTable, ? extends JdbcTable> remappingFunction) {
    return tables.computeIfPresent(key, remappingFunction);
  }

  public void replaceAll(
      BiFunction<? super String, ? super JdbcTable, ? extends JdbcTable> function) {
    tables.replaceAll(function);
  }

  public JdbcTable computeIfAbsent(String key,
      Function<? super String, ? extends JdbcTable> mappingFunction) {
    return tables.computeIfAbsent(key, mappingFunction);
  }

  public JdbcTable putIfAbsent(JdbcTable value) {
    return tables.putIfAbsent(value.tableName, value);
  }

  public JdbcTable merge(String key, JdbcTable value,
      BiFunction<? super JdbcTable, ? super JdbcTable, ? extends JdbcTable> remappingFunction) {
    return tables.merge(key, value, remappingFunction);
  }

  public boolean containsKey(String key) {
    return tables.containsKey(key);
  }

  public JdbcTable remove(String key) {
    return tables.remove(key);
  }

  public void clear() {
    tables.clear();
  }

  public Set<Map.Entry<String, JdbcTable>> entrySet() {
    return tables.entrySet();
  }

  public Set<String> keySet() {
    return tables.keySet();
  }

  /*
   * following for JSON (de)serialization
   */

  public List<JdbcTable> getTables() {
    return new ArrayList<JdbcTable>(this.tables.values());
  }

  public void setTables(List<JdbcTable> tables) {
    for (JdbcTable item : tables) {
      item.tableCatalog = this.tableCatalog;
      item.tableSchema = this.tableSchema;
      put(item);
    }
  }

  public String getSchemaName() {
    return this.tableSchema;
  }

  public void setSchemaName(String schemaName) {
    this.tableSchema = schemaName;
  }

}
