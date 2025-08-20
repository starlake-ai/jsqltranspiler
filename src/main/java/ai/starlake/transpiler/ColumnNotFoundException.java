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

public class ColumnNotFoundException extends RuntimeException {
  String columnName;
  Set<String> tableNames = new HashSet<>();

  public ColumnNotFoundException(String columnName, Collection<String> tableNames,
      Throwable cause) {
    super("Column " + columnName + " not found in " + Arrays.deepToString(tableNames.toArray()),
        cause);
    this.columnName = columnName;
    this.tableNames.addAll(tableNames);
  }

  public ColumnNotFoundException(String columnName, Collection<String> tableNames) {
    super("Column " + columnName + " not found in " + Arrays.deepToString(tableNames.toArray()));
    this.columnName = columnName;
    this.tableNames.addAll(tableNames);
  }

  public String getColumnName() {
    return columnName;
  }

  public Set<String> getTableNames() {
    return tableNames;
  }
}
