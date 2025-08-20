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
package ai.starlake.transpiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableNotFoundException extends RuntimeException {
  String tableName;
  Set<String> schemaNames = new HashSet<>();

  public TableNotFoundException(String tableName, Collection<String> schemaNames, Throwable cause) {
    super(
        "Table " + tableName + " not found in schema " + Arrays.deepToString(schemaNames.toArray()),
        cause);
    this.tableName = tableName;
    this.schemaNames.addAll(schemaNames);
  }

  public TableNotFoundException(String tableName, Collection<String> schemaNames) {
    super("Table " + tableName + " not found in schema "
        + Arrays.deepToString(schemaNames.toArray()));
    this.tableName = tableName;
    this.schemaNames.addAll(schemaNames);
  }

  public String getTableName() {
    return tableName;
  }
}
