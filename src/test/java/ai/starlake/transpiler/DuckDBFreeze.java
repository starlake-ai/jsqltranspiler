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
package ai.starlake.transpiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DuckDBFreeze {

  @Test
  @Disabled
  void testFreeze() throws SQLException {
    String sqlString = "SELECT PRINTF('%010.2f', 125.8) AS chars;";

    Properties info = new Properties();

    // crashes
    // info.put("old_implicit_casting", true);

    // works
    // info.put("old_implicit_casting", "true");

    try (Connection connDuck = DriverManager.getConnection("jdbc:duckdb:", info);
        Statement st = connDuck.createStatement();
        ResultSet rs = st.executeQuery(sqlString);) {
      Assertions.assertTrue(rs.next());

      Assertions.assertEquals("21", rs.getString(1));
    }
  }

  @Test
  void testUpcast() {
    // SELECT 345349 * POWER((1+Cast(7 AS FLOAT)/100/12),120) qty2010;

    Object c = 1 + (float) 0.07 / 12;

    System.out.println(c.getClass());


    System.out.println(345349 * Math.pow(1 + (float) 0.07 / 12, 120));

  }
}
