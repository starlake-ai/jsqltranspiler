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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class JdbcCatalog implements Comparable<JdbcCatalog> {

  public static final Logger LOGGER = Logger.getLogger(JdbcCatalog.class.getName());

  public String tableCatalog;
  public String catalogSeparator;

  public CaseInsensitiveLinkedHashMap<JdbcSchema> schemas = new CaseInsensitiveLinkedHashMap<>();

  public JdbcCatalog(String tableCatalog, String catalogSeparator) {
    this.tableCatalog = tableCatalog != null ? tableCatalog : "";
    this.catalogSeparator = catalogSeparator != null ? catalogSeparator : ".";
  }

  public JdbcCatalog() {}

  public static Collection<JdbcCatalog> getCatalogs(DatabaseMetaData metaData) throws SQLException {
    ArrayList<JdbcCatalog> jdbcCatalogs = new ArrayList<>();

    try (ResultSet rs = metaData.getCatalogs();) {

      String catalogSeparator = metaData.getCatalogSeparator();
      while (rs.next()) {
        String tableCatalog = JdbcUtils.getStringSafe(rs, "TABLE_CAT");
        if (tableCatalog != null && !tableCatalog.isEmpty()) {
          JdbcCatalog jdbcCatalog = new JdbcCatalog(tableCatalog, catalogSeparator);
          jdbcCatalogs.add(jdbcCatalog);
        }
      }
      // add <empty> catalog as some DBs don't have the concept of catalog for tables
      jdbcCatalogs.add(new JdbcCatalog("", "."));

    }
    return jdbcCatalogs;
  }

  public JdbcSchema put(JdbcSchema jdbcSchema) {
    return schemas.put(jdbcSchema.tableSchema.replaceAll("^\"|\"$", "").toUpperCase(), jdbcSchema);
  }

  public JdbcSchema get(String tableSchema) {
    return schemas.get(tableSchema.replaceAll("^\"|\"$", "").toUpperCase());
  }

  @Override
  public int compareTo(JdbcCatalog o) {
    return tableCatalog.compareToIgnoreCase(o.tableCatalog);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcCatalog)) {
      return false;
    }

    JdbcCatalog jdbcCatalog = (JdbcCatalog) o;

    return tableCatalog.equals(jdbcCatalog.tableCatalog);
  }

  @Override
  public int hashCode() {
    return tableCatalog.hashCode();
  }

  public JdbcSchema put(String key, JdbcSchema value) {
    return schemas.put(key, value);
  }

  public boolean containsValue(JdbcSchema value) {
    return schemas.containsValue(value);
  }

  public int size() {
    return schemas.size();
  }

  public JdbcSchema replace(String key, JdbcSchema value) {
    return schemas.replace(key, value);
  }

  public boolean isEmpty() {
    return schemas.isEmpty();
  }

  public JdbcSchema compute(String key,
      BiFunction<? super String, ? super JdbcSchema, ? extends JdbcSchema> remappingFunction) {
    return schemas.compute(key, remappingFunction);
  }

  public void putAll(Map<? extends String, ? extends JdbcSchema> m) {
    schemas.putAll(m);
  }

  public Collection<JdbcSchema> values() {
    return schemas.values();
  }

  public boolean replace(String key, JdbcSchema oldValue, JdbcSchema newValue) {
    return schemas.replace(key, oldValue, newValue);
  }

  public void forEach(BiConsumer<? super String, ? super JdbcSchema> action) {
    schemas.forEach(action);
  }

  public JdbcSchema getOrDefault(String key, JdbcSchema defaultValue) {
    return schemas.getOrDefault(key, defaultValue);
  }

  public boolean remove(String key, Object value) {
    return schemas.remove(key, value);
  }

  public JdbcSchema computeIfPresent(String key,
      BiFunction<? super String, ? super JdbcSchema, ? extends JdbcSchema> remappingFunction) {
    return schemas.computeIfPresent(key, remappingFunction);
  }

  public void replaceAll(
      BiFunction<? super String, ? super JdbcSchema, ? extends JdbcSchema> function) {
    schemas.replaceAll(function);
  }

  public JdbcSchema computeIfAbsent(String key,
      Function<? super String, ? extends JdbcSchema> mappingFunction) {
    return schemas.computeIfAbsent(key, mappingFunction);
  }

  public JdbcSchema putIfAbsent(JdbcSchema value) {
    return schemas.putIfAbsent(value.tableSchema, value);
  }

  public JdbcSchema merge(String key, JdbcSchema value,
      BiFunction<? super JdbcSchema, ? super JdbcSchema, ? extends JdbcSchema> remappingFunction) {
    return schemas.merge(key, value, remappingFunction);
  }

  public boolean containsKey(String key) {
    return schemas.containsKey(key);
  }

  public JdbcSchema remove(String key) {
    return schemas.remove(key);
  }

  public void clear() {
    schemas.clear();
  }

  public Set<Map.Entry<String, JdbcSchema>> entrySet() {
    return schemas.entrySet();
  }

  public Set<String> keySet() {
    return schemas.keySet();
  }

  public String getTableCatalog() {
    return tableCatalog;
  }

  public void setTableCatalog(String tableCatalog) {
    this.tableCatalog = tableCatalog;
  }

  public String getCatalogSeparator() {
    return catalogSeparator;
  }

  public void setCatalogSeparator(String catalogSeparator) {
    this.catalogSeparator = catalogSeparator;
  }

  public List<JdbcSchema> getSchemas() {
    return new ArrayList<JdbcSchema>(schemas.values());
  }

  public void setSchemas(List<JdbcSchema> schemas) {
    for (JdbcSchema item : schemas) {
      item.tableCatalog = this.tableCatalog;
      put(item);
    }
  }
}
