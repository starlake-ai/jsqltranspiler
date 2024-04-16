package ai.starlake.transpiler.redshift;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedshiftExpressionTranspilerTest {

  @Test
  void toFormat() {
    String provided = "9,999.99";
    String expected = "%g";

    Assertions.assertEquals(expected, RedshiftExpressionTranspiler.toFormat(provided));
  }
}
