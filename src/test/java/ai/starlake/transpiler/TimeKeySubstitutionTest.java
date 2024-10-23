/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
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

import net.sf.jsqlparser.JSQLParserException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class TimeKeySubstitutionTest {

  @BeforeEach
  void setUp() {
    System.setProperty("CURRENT_DATE", "2024-06-09");
    System.setProperty("CURRENT_TIME", "16:24:23.123");
    System.setProperty("CURRENT_TIMESTAMP", "2024-06-09 16:24:23.123");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("CURRENT_DATE");
    System.clearProperty("CURRENT_TIME");
    System.clearProperty("CURRENT_TIMESTAMP");
  }

  @Test
  void testCurrentDate() throws JSQLParserException, InterruptedException {
    String expected = "SELECT DATE '2024-06-09'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_DATE", JSQLTranspiler.Dialect.ANY,
        Collections.emptyMap());

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCurrentTime() throws JSQLParserException, InterruptedException {
    String expected = "SELECT TIME WITHOUT TIME ZONE '16:24:23.123'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIME", JSQLTranspiler.Dialect.ANY,
        Collections.emptyMap());

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCurrentTimestamp() throws JSQLParserException, InterruptedException {
    String expected = "SELECT TIMESTAMP WITHOUT TIME ZONE '2024-06-09T16:24:23.123'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIMESTAMP",
        JSQLTranspiler.Dialect.ANY, Collections.emptyMap());

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCurrentDateParameter() throws JSQLParserException, InterruptedException {
    String expected = "SELECT DATE '2024-06-10'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_DATE", JSQLTranspiler.Dialect.ANY,
        Map.of("CURRENT_DATE", "2024-06-10"));

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCurrentTimeParameter() throws JSQLParserException, InterruptedException {
    String expected = "SELECT TIME WITHOUT TIME ZONE '17:24:23.123'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIME", JSQLTranspiler.Dialect.ANY,
        Map.of("CURRENT_TIME", "17:24:23.123"));

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCurrentTimestampParameter() throws JSQLParserException, InterruptedException {
    String expected = "SELECT TIMESTAMP WITHOUT TIME ZONE '2024-06-10T17:24:23.123'";
    String actual = JSQLTranspiler.transpileQuery("SELECT CURRENT_TIMESTAMP",
        JSQLTranspiler.Dialect.ANY, Map.of("CURRENT_TIMESTAMP", "2024-06-10T17:24:23.123"));

    Assertions.assertThat(actual).isEqualTo(expected);
  }
}
