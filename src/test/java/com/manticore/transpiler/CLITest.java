package com.manticore.transpiler;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CLITest extends JSQLTranspilerTest {
  public final static String TEST_FOLDER_STR = "build/resources/test/com/manticore/transpiler";

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  @Disabled
  void transpile(File f, int idx, SQLTest t) throws Exception {

  }

  @Test
  void mainTest() throws IOException {
    String providedSqlStr = IOUtils.resourceToString(
        JSQLTranspilerTest.class.getCanonicalName().replaceAll("\\.", "/") + "_MainIn.sql",
        Charset.defaultCharset(), JSQLTranspilerTest.class.getClassLoader());

    String expectedSqlStr = IOUtils.resourceToString(
        JSQLTranspilerTest.class.getCanonicalName().replaceAll("\\.", "/") + "_MainOut.sql",
        Charset.defaultCharset(), JSQLTranspilerTest.class.getClassLoader());

    String inputFileStr = TEST_FOLDER_STR + "/JSQLTranspilerTest_MainIn.sql";
    File outputFile = File.createTempFile("any_transpiled_", ".sql");

    // Input file to Output file
    String[] cmdLine = {"-i", inputFileStr, "--any", "-o", outputFile.getAbsolutePath()};
    JSQLTranspiler.main(cmdLine);

    // Input file to STDOUT
    cmdLine = new String[] {"-i", inputFileStr, "--any"};
    JSQLTranspiler.main(cmdLine);

    // STDIN to STDOUT
    cmdLine = new String[] {"--any", providedSqlStr};
    JSQLTranspiler.main(cmdLine);

    // STDIN to Output file
    cmdLine = new String[] {"--any", "-o", outputFile.getAbsolutePath(), providedSqlStr};
    JSQLTranspiler.main(cmdLine);

    cmdLine = new String[] {"--help"};
    JSQLTranspiler.main(cmdLine);

    Assertions.assertThrows(RuntimeException.class, new Executable() {
      @Override
      public void execute() throws Throwable {
        String[] cmdLine = cmdLine = new String[] {"--unsupported-option"};
        JSQLTranspiler.main(cmdLine);
      }
    });
  }
}
