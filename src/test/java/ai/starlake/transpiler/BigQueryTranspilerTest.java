package ai.starlake.transpiler;

import com.manticore.jsqlformatter.JSQLFormatter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BigQueryTranspilerTest {
  @Test
  void testComplexQueryWithSubStr() throws Exception {
    String sqlStr = IOUtils.resourceToString("/ai/starlake/transpiler/BigQueryTranspilerTest.sql",
        Charset.defaultCharset());

    ExecutorService e = Executors.newSingleThreadExecutor();

    // transpile
    String output =
        JSQLTranspiler.transpileQuery(sqlStr, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, Map.of(), e,p -> p.withTimeOut(6000) );

    // format
    output = JSQLFormatter.format(output);

    // reparse
    Statement st = CCJSqlParserUtil.parse(output, e, p -> p.withTimeOut(6000));

    e.shutdown();
    e.awaitTermination(1, TimeUnit.DAYS);

    Assertions.assertThat(st).isNotNull();
  }
}
