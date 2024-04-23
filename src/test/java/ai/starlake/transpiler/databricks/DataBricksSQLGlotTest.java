package ai.starlake.transpiler.databricks;

import ai.starlake.transpiler.JSQLTranspiler;
import ai.starlake.transpiler.SQLGlotTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

@Disabled
public class DataBricksSQLGlotTest extends SQLGlotTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/ai/starlake/transpiler/databricks";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void transpile(File f, int idx, SQLTest t) throws Exception {
    super.transpile(f, idx, t, "databricks");
  }

}
