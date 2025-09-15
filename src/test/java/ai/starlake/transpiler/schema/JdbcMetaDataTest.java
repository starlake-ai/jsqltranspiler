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
package ai.starlake.transpiler.schema;

import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

class JdbcMetaDataTest {

  Connection conn;

  @BeforeEach
  void createDatabase() throws SQLException, ClassNotFoundException {

    // Wrap an H2 in memory DB
    Driver driver = DriverManager.getDriver("jdbc:h2:mem:");

    Properties info = new Properties();
    info.put("username", "SA");
    info.put("password", "");

    conn = driver.connect("jdbc:h2:mem:", info);
    try (Statement st = conn.createStatement()) {
      st.executeUpdate("CREATE SCHEMA IF NOT EXISTS test;");
    }
  }

  @AfterEach
  void closeDatabase() throws SQLException {
    conn.close();
  }

  @Test
  void testUpdateSchema() throws SQLException {
    JdbcMetaData metaData = new JdbcMetaData(conn);
    try (Statement st = conn.createStatement()) {
      st.executeUpdate("CREATE TABLE IF NOT EXISTS test.b (c VARCHAR(40));");
    }

    metaData.updateTable(conn, new Table("test.b"));

    Assertions.assertEquals(1, metaData.get("UNNAMED").get("test").get("b").columns.size());
  }
}
