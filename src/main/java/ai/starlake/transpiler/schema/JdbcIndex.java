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

import java.util.Objects;
import java.util.TreeMap;

public class JdbcIndex {

  String tableCatalog;
  String tableSchema;
  String tableName;
  Boolean nonUnique;
  String indexQualifier;
  String indexName;
  Short type;

  TreeMap<Short, JdbcIndexColumn> columns = new TreeMap<>();

  public JdbcIndex(String tableCatalog, String tableSchema, String tableName, Boolean nonUnique,
      String indexQualifier, String indexName, Short type) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.nonUnique = nonUnique;
    this.indexQualifier = indexQualifier;
    this.indexName = indexName;
    this.type = type;
  }

  public JdbcIndexColumn put(Short ordinalPosition, String columnName, String ascOrDesc,
      Long cardinality, Long pages, String filterCondition) {
    JdbcIndexColumn column = new JdbcIndexColumn(ordinalPosition, columnName, ascOrDesc,
        cardinality, pages, filterCondition);
    return columns.put(ordinalPosition, column);
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcIndex)) {
      return false;
    }

    JdbcIndex jdbcIndex = (JdbcIndex) o;

    if (!Objects.equals(tableCatalog, jdbcIndex.tableCatalog)) {
      return false;
    }
    if (!Objects.equals(tableSchema, jdbcIndex.tableSchema)) {
      return false;
    }
    if (!tableName.equals(jdbcIndex.tableName)) {
      return false;
    }
    if (!nonUnique.equals(jdbcIndex.nonUnique)) {
      return false;
    }
    if (!indexQualifier.equals(jdbcIndex.indexQualifier)) {
      return false;
    }
    if (!indexName.equals(jdbcIndex.indexName)) {
      return false;
    }
    if (!type.equals(jdbcIndex.type)) {
      return false;
    }
    return columns.equals(jdbcIndex.columns);
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public int hashCode() {
    int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
    result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
    result = 31 * result + tableName.hashCode();
    result = 31 * result + nonUnique.hashCode();
    result = 31 * result + indexQualifier.hashCode();
    result = 31 * result + indexName.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + columns.hashCode();
    return result;
  }
}
