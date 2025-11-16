package ai.starlake.transpiler;

import com.manticore.jsqlformatter.JSQLFormatter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Map;

public class BigQueryTranspilerTest {
  @Test
  void testComplexQueryWithSubStr() throws Exception {
    String sqlStr = IOUtils.resourceToString("/ai/starlake/transpiler/BigQueryTranspilerTest.sql",
        Charset.defaultCharset());

    // transpile
    String output =
        JSQLTranspiler.transpileQuery(sqlStr, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, Map.of());

    // format
    output = JSQLFormatter.format(output);

    // reparse
    Statement st = CCJSqlParserUtil.parse(output);

    Assertions.assertThat(st).isNotNull();
  }
}
