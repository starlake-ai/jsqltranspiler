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

import ai.starlake.transpiler.JSQLTranspiler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class SnowflakeVariantModeTest {

  @Test
  void snowflakeDefaultsToVariantBracketAccess() throws Exception {
    Assertions
        .assertThat(
            JSQLTranspiler.transpileQuery("SELECT v:a.b FROM t", JSQLTranspiler.Dialect.SNOWFLAKE))
        .isEqualToIgnoringWhitespace("SELECT v['a']['b'] FROM t");

    Assertions
        .assertThat(JSQLTranspiler.transpileQuery("SELECT v:arr[0] FROM t",
            JSQLTranspiler.Dialect.SNOWFLAKE))
        .isEqualToIgnoringWhitespace("SELECT v['arr'][1] FROM t");

    Assertions
        .assertThat(JSQLTranspiler.transpileQuery("SELECT x::VARIANT FROM t",
            JSQLTranspiler.Dialect.SNOWFLAKE))
        .isEqualToIgnoringWhitespace("SELECT x::VARIANT FROM t");
  }

  @Test
  void jsonModeOnRequest() throws Exception {
    Map<String, Object> params = Map.of("VARIANT_MODE", "JSON");

    Assertions
        .assertThat(JSQLTranspiler.transpileQuery("SELECT v:a.b::string FROM t",
            JSQLTranspiler.Dialect.SNOWFLAKE, params))
        .isEqualToIgnoringWhitespace("SELECT (v->'a'->>'b')::string FROM t");

    Assertions
        .assertThat(JSQLTranspiler.transpileQuery("SELECT x::VARIANT FROM t",
            JSQLTranspiler.Dialect.SNOWFLAKE, params))
        .isEqualToIgnoringWhitespace("SELECT x::VARCHAR FROM t");
  }
}
