package ai.starlake.transpiler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

public class AmazonRedshiftTranspilerTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/ai/starlake/transpiler/amazon_redshift";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.AMAZON_REDSHIFT, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  void transpile(File f, int idx, SQLTest t) throws Exception {
    super.transpile(f, idx, t);
  }

}
