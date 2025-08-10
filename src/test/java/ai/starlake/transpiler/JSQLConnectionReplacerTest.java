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
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

class JSQLConnectionReplacerTest {
  Connection conn;
  JdbcMetaData metaData;

  Map<String, String> replacements = Map.of("a", "test.c");

  @BeforeEach
  void beforeEach() throws SQLException {
    Driver driver = DriverManager.getDriver("jdbc:h2:mem:");
    Properties info = new Properties();
    info.put("username", "SA");
    info.put("password", "");

    conn = driver.connect("jdbc:h2:mem:", info);
    try (java.sql.Statement st = conn.createStatement()) {
      st.executeUpdate("CREATE SCHEMA IF NOT EXISTS test;");
      st.executeUpdate("CREATE TABLE test.c(b VARCHAR(1));");

      // add a synonym from "public.a" to "test.a"
      metaData = new JdbcMetaData(conn);
      metaData.addSynonym("a", "test.c");
    }
  }

  @AfterEach()
  void afterEach() throws SQLException {
    if (!conn.isClosed()) {
      conn.close();
    }
  }

  @Test
  void testMetaData() {
    Assertions.assertThat(metaData.getCurrentCatalogName()).isEqualToIgnoringCase("UNNAMED");
    Assertions.assertThat(metaData.getCurrentSchemaName()).isEqualToIgnoringCase("PUBLIC");
    Assertions.assertThat(metaData.hasTable(new Table("public.a"))).isTrue();
  }

  @Test
  void testSimple() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT * from a";
    String expected = "SELECT c.b from test.c";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testSubSelect() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT a.b from (select * from a) a";
    String expected = "SELECT a.b from (SELECT c.b from test.c) a";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testSimpleWith() throws SQLException, JSQLParserException {
    String sqlStr = "with a as (select b from a) SELECT b from a";
    String expected = "with a as (select b from test.c) SELECT b from a";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testSimpleWithAllColumns() throws SQLException, JSQLParserException {
    String sqlStr = "with a as (select * from a) SELECT * from a";
    String expected = "with a as (select c.b from test.c) SELECT a.b from a";
    assertThatRewritesInto(sqlStr, expected);
  }


  @Test
  void testWithAndSubSelect() throws SQLException, JSQLParserException {
    String sqlStr = "with a as (SELECT a.b from (select * from a) a) SELECT * from a";
    String expected = "with a as (SELECT a.b from (select c.b from test.c) a) SELECT a.b from a";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testWithAndSubSelectJoin() throws SQLException, JSQLParserException {
    String sqlStr =
        "with a as (SELECT a.b from (select * from a) a) SELECT * from a inner join a using(b)";
    String expected =
        "with a as (SELECT a.b from (select c.b from test.c) a) SELECT a.b from a inner join a using(b)";
    assertThatRewritesInto(sqlStr, expected);
  }

  Statement assertThatRewritesInto(String sqlStr, String expected)
      throws JSQLParserException, SQLException {
    JSQLReplacer replacer = new JSQLReplacer(metaData);
    Statement st = replacer.replace(sqlStr, replacements);
    Assertions.assertThat(JSQLColumnResolverTest.sanitize(st.toString()))
        .isEqualToIgnoringCase(JSQLColumnResolverTest.sanitize(expected));
    return st;
  }
}
