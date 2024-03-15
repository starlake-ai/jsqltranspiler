package com.manticore.transpiler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Map;
import java.util.stream.Stream;

public class GoogleBigQueryTranspilerTest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR =
      "build/resources/test/com/manticore/transpiler/google_bigquery";

  static Stream<Map.Entry<File, SQLTest>> getSqlTestMap() {
    return getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER));
  }

  @ParameterizedTest(name = "{index} {0}: {1}")
  @MethodSource("getSqlTestMap")
  void transpile(Map.Entry<File, SQLTest> entry) throws Exception {
    super.transpile(entry);
  }

}
