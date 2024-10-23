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

import java.util.LinkedList;
import java.util.Objects;

public class JdbcPrimaryKey {

  String tableCatalog;
  String tableSchema;
  String tableName;
  String primaryKeyName;

  LinkedList<String> columnNames = new LinkedList<>();

  public JdbcPrimaryKey(String tableCatalog, String tableSchema, String tableName,
      String primaryKeyName) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.primaryKeyName = primaryKeyName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcPrimaryKey)) {
      return false;
    }

    JdbcPrimaryKey that = (JdbcPrimaryKey) o;

    if (!Objects.equals(tableCatalog, that.tableCatalog)) {
      return false;
    }
    if (!Objects.equals(tableSchema, that.tableSchema)) {
      return false;
    }
    if (!tableName.equals(that.tableName)) {
      return false;
    }
    if (!primaryKeyName.equals(that.primaryKeyName)) {
      return false;
    }
    return columnNames.equals(that.columnNames);
  }

  @Override
  public int hashCode() {
    int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
    result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
    result = 31 * result + tableName.hashCode();
    result = 31 * result + primaryKeyName.hashCode();
    result = 31 * result + columnNames.hashCode();
    return result;
  }
}
