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

import java.util.Map;

interface SchemaProvider {
  /**
   * Get all tables in the schema
   * 
   * @return Map of tables with schema name and table name as key and map of field name and field
   *         type as value
   */
  Map<String, Map<String, String>> getTables();


  /**
   * Get all fields in the table
   * 
   * @param schemaName schema name
   * @param tableName table name
   * @return Map of field name and field type
   */
  Map<String, String> getTable(String schemaName, String tableName);

  /**
   * Get table regardless of schema name
   * 
   * @param tableName table name
   * @return Map of schema name where the table is found and map of field name and field type.
   *         Returning more than one key means the table is found in multiple schemas and the
   *         resolution is ambiguous. In the future, resolution may be done by jsqltranspiler based
   *         on the context.
   */
  Map<String, Map<String, String>> getTables(String tableName);

}

