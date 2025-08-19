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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

class JSQLReplacerTest {
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
  void testSimpleAlias() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT b.b  from a as b";
    String expected = "SELECT b.b from test.c as b";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  @Disabled
  void testShadowingAlias() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT a.b  from a as a";
    String expected = "SELECT a.b from test.c as a";
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

  @Test
  void testSelect() throws SQLException, JSQLParserException {
    String sqlStr = "SELECT b FROM a;";
    String expected = "SELECT b FROM test.c;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testInsert() throws SQLException, JSQLParserException {
    String sqlStr = "INSERT INTO a VALUES('1');";
    String expected = "INSERT INTO test.c VALUES('1');";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testUpdate1() throws SQLException, JSQLParserException {
    String sqlStr = "UPDATE a set b=1;";
    String expected = "UPDATE test.c set b=1;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testUpdate2() throws SQLException, JSQLParserException {
    String sqlStr = "UPDATE a set a.b=1;";
    String expected = "UPDATE test.c set c.b=1;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  @Disabled
  void testUpdateWithShadowingAlias() throws SQLException, JSQLParserException {
    String sqlStr = "UPDATE a as a set a.b=1;";
    String expected = "UPDATE test.c as a set a.b=1;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testDelete() throws SQLException, JSQLParserException {
    String sqlStr = "DELETE FROM a;";
    String expected = "DELETE FROM test.c;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testTruncate() throws SQLException, JSQLParserException {
    String sqlStr = "TRUNCATE TABLE a;";
    String expected = "TRUNCATE TABLE test.c;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testDrop() throws SQLException, JSQLParserException {
    String sqlStr = "DROP TABLE a;";
    String expected = "DROP TABLE test.c;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testCreateIndex() throws SQLException, JSQLParserException {
    String sqlStr = "CREATE INDEX a_idx1 ON a(b);";
    String expected = "CREATE INDEX a_idx1 ON test.c(b);";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testMerge1() throws SQLException, JSQLParserException {
    String sqlStr = "MERGE INTO a USING a s ON (a.b=s.b) WHEN MATCHED THEN UPDATE SET a.b=s.b";
    String expected =
        "MERGE INTO test.c USING test.c s ON (c.b=s.b) WHEN MATCHED THEN UPDATE SET c.b=s.b;";
    assertThatRewritesInto(sqlStr, expected);
  }

  @Test
  void testMerge2() throws SQLException, JSQLParserException {
    String sqlStr =
        "MERGE INTO a t USING (select * from a) s ON (t.b=s.b) WHEN MATCHED THEN UPDATE SET t.b=s.b";
    String expected =
        "MERGE INTO test.c t USING (select c.b from test.c) s ON (t.b=s.b) WHEN MATCHED THEN UPDATE SET t.b=s.b;";
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
