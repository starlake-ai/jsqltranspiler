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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

public class JdbcSchema implements Comparable<JdbcSchema> {

  public static final Logger LOGGER = Logger.getLogger(JdbcSchema.class.getName());

  String tableSchema;
  String tableCatalog;

  public CaseInsensitiveLinkedHashMap<JdbcTable> tables = new CaseInsensitiveLinkedHashMap<>();

  public JdbcSchema(String tableSchema, String tableCatalog) {
    this.tableSchema = tableSchema != null ? tableSchema : "";
    this.tableCatalog = tableCatalog != null ? tableCatalog : "";
  }

  public static Collection<JdbcSchema> getSchemas(DatabaseMetaData metaData) throws SQLException {
    ArrayList<JdbcSchema> jdbcSchemas = new ArrayList<>();

    try (ResultSet rs = metaData.getSchemas();) {

      while (rs.next()) {
        // TABLE_SCHEM String => schema name
        String tableSchema = rs.getString("TABLE_SCHEM");
        // TABLE_CATALOG String => catalog name (may be null)
        String tableCatalog = rs.getString("TABLE_CATALOG");
        JdbcSchema jdbcSchema = new JdbcSchema(tableSchema, tableCatalog);

        jdbcSchemas.add(jdbcSchema);
      }
      if (jdbcSchemas.isEmpty()) {
        jdbcSchemas.add(new JdbcSchema("", "."));
      }

    }
    return jdbcSchemas;
  }

  public JdbcTable put(JdbcTable jdbcTable) {
    return tables.put(jdbcTable.tableName.toUpperCase(), jdbcTable);
  }

  public JdbcTable get(String tableName) {
    return tables.get(tableName.toUpperCase());
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
}
