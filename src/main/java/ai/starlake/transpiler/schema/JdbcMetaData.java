/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com> on behalf of Starlake.AI
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class JdbcMetaData {

  private final TreeMap<String, JdbcCatalog> catalogs = new TreeMap<>();
  private final DatabaseMetaData metaData;

  public JdbcMetaData(Connection con) throws SQLException {
    this.metaData = con.getMetaData();
  }

  public void build() throws SQLException {
    for (JdbcCatalog jdbcCatalog : JdbcCatalog.getCatalogs(metaData)) {
      put(jdbcCatalog);
    }

    for (JdbcSchema jdbcSchema : JdbcSchema.getSchemas(metaData)) {
      put(jdbcSchema);
    }

    for (JdbcTable jdbcTable : JdbcTable.getTables(metaData)) {
      put(jdbcTable);
      jdbcTable.getColumns(metaData);

      // "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
      // "SYNONYM"
      if (jdbcTable.tableType.equals("TABLE") || jdbcTable.tableType.equals("SYSTEM TABLE")) {
        jdbcTable.getIndices(metaData, true);
        jdbcTable.getPrimaryKey(metaData);
      }
    }

  }

  public JdbcCatalog put(JdbcCatalog jdbcCatalog) {
    return catalogs.put(jdbcCatalog.tableCatalog.toUpperCase(), jdbcCatalog);
  }

  public Map<String, JdbcCatalog> getCatalogs() {
    return Collections.unmodifiableMap(catalogs);
  }

  public JdbcSchema put(JdbcSchema jdbcSchema) {
    JdbcCatalog jdbcCatalog = catalogs.get(jdbcSchema.tableCatalog.toUpperCase());
    return jdbcCatalog.put(jdbcSchema);
  }

  public JdbcTable put(JdbcTable jdbcTable) {
    JdbcCatalog jdbcCatalog = catalogs.get(jdbcTable.tableCatalog.toUpperCase());
    JdbcSchema jdbcSchema = jdbcCatalog.get(jdbcTable.tableSchema.toUpperCase());

    return jdbcSchema.put(jdbcTable);
  }

}
