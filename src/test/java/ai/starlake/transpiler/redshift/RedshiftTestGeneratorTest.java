/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler.redshift;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.JSQLTranspilerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class RedshiftTestGeneratorTest extends JSQLTranspilerTest {
    public final static String INPUT_FOLDER_STR =
            "build/resources/test/ai/starlake/test_generator/redshift";
    public final static String TEST_FOLDER_STR =
            "src/test/resources/ai/starlake/transpiler/redshift";

    private static final JSQLTranspiler.Dialect INPUT_DIALECT = JSQLTranspiler.Dialect.AMAZON_REDSHIFT;

    static Stream<Arguments> getInputs() throws FileNotFoundException {
        return getInputQueries(new File(INPUT_FOLDER_STR));
    }

    @ParameterizedTest(name = "{index} {0} {1}: {2}")
    @MethodSource("getSqlTestMap")
    @Disabled
    protected void transpile(File f, int idx, SQLTest t) throws Exception {
    }

    @ParameterizedTest(name = "{index} {0} {1} {2}: {3}")
    @MethodSource("getInputs")
    protected void generate(File f, int idx, boolean supported, String inputQuery) throws IOException {
        generateTestCase(INPUT_DIALECT, inputQuery, Path.of(TEST_FOLDER_STR, f.getName()).toString(), idx, supported);
    }
}
