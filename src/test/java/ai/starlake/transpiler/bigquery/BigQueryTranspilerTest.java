package ai.starlake.transpiler.bigquery;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.JSQLTranspilerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

public class BigQueryTranspilerTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/ai/starlake/transpiler/bigquery";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void transpile(File f, int idx, SQLTest t) throws Exception {
    super.transpile(f, idx, t);
  }

}
