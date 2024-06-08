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
import java.util.logging.Logger;

public class JdbcCatalog implements Comparable<JdbcCatalog> {

  public static final Logger LOGGER = Logger.getLogger(JdbcCatalog.class.getName());

  String tableCatalog;
  String catalogSeparator;

  public CaseInsensitiveLinkedHashMap<JdbcSchema> schemas = new CaseInsensitiveLinkedHashMap<>();

  public JdbcCatalog(String tableCatalog, String catalogSeparator) {
    this.tableCatalog = tableCatalog != null ? tableCatalog : "";
    this.catalogSeparator = catalogSeparator != null ? catalogSeparator : ".";
  }

  public static Collection<JdbcCatalog> getCatalogs(DatabaseMetaData metaData) throws SQLException {
    ArrayList<JdbcCatalog> jdbcCatalogs = new ArrayList<>();

    try (ResultSet rs = metaData.getCatalogs();) {

      String catalogSeparator = metaData.getCatalogSeparator();
      while (rs.next()) {
        String tableCatalog = rs.getString("TABLE_CAT");
        JdbcCatalog jdbcCatalog = new JdbcCatalog(tableCatalog, catalogSeparator);

        jdbcCatalogs.add(jdbcCatalog);
      }
      if (jdbcCatalogs.isEmpty()) {
        jdbcCatalogs.add(new JdbcCatalog("", "."));
      }

    }
    return jdbcCatalogs;
  }

  public JdbcSchema put(JdbcSchema jdbcSchema) {
    return schemas.put(jdbcSchema.tableSchema.toUpperCase(), jdbcSchema);
  }

  public JdbcSchema get(String tableSchema) {
    return schemas.get(tableSchema.toUpperCase());
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
}
