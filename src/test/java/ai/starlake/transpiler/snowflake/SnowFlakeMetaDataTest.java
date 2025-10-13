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
package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.schema.JdbcMetaData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Execution(ExecutionMode.SAME_THREAD)
@Disabled
public class SnowFlakeMetaDataTest {
  private final static Logger LOGGER = Logger.getLogger(SnowFlakeMetaDataTest.class.getName());
  public static Connection conn;

  @BeforeAll
  static void init() {
    try {
      Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    String account = System.getenv("SNOWFLAKE_ACCOUNT");
    String user = System.getenv("SNOWFLAKE_USER");
    String password = System.getenv("SNOWFLAKE_PASSWORD");
    String warehouse = System.getenv("SNOWFLAKE_WAREHOUSE");
    String database = System.getenv("SNOWFLAKE_DATABASE");
    String schema = System.getenv("SNOWFLAKE_SCHEMA");

    if (account == null || user == null || password == null) {
      throw new IllegalStateException(
          "Missing required Snowflake environment variables! Please adjust");
    }

    String url = String.format(
        "jdbc:snowflake://%s.snowflakecomputing.com/?user=%s&password=%s&warehouse=%s&db=%s&schema=%s",
        account, user, password, warehouse, database, schema);
    Driver driver = null;
    try {
      driver = DriverManager.getDriver(url);
      Properties info = new Properties();
      conn = driver.connect(url, info);
    } catch (SQLException ex) {
      LOGGER.log(Level.SEVERE, "Failed to connect to DB.", ex);
      throw new RuntimeException(ex);
    }
  }

  @Test
  void testSelect() throws SQLException {
    long startNanoTime = System.nanoTime();
    new JdbcMetaData(conn);

    long endNanoTime = System.nanoTime();

    long durationMillis = (endNanoTime - startNanoTime) / 1_000_000;
    long minutes = durationMillis / 60_000;
    long seconds = durationMillis % 60_000 / 1_000;
    long millis = durationMillis % 1_000;

    System.out.printf("Duration: %d:%02d.%03d%n", minutes, seconds, millis);
  }
}
