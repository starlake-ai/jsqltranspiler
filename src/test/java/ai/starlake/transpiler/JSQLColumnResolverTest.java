package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcMetaData;
import com.opencsv.CSVWriter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.StringWriter;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class JSQLColumnResolverTest extends JSQLTranspilerTest {

  public final static String TEST_FOLDER_STR = "build/resources/test/ai/starlake/transpiler/schema";

  static Stream<Arguments> getSqlTestMap() {
    return unrollParameterMap(getSqlTestMap(new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER),
        JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY, JSQLTranspiler.Dialect.DUCK_DB));
  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  @Disabled
  protected void transpile(File f, int idx, SQLTest t) throws Exception {

  }

  @ParameterizedTest(name = "{index} {0} {1}: {2}")
  @MethodSource("getSqlTestMap")
  protected void resolve(File f, int idx, SQLTest t) throws Exception {
    ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    if (t.prologue != null && !t.prologue.isEmpty()) {
      try (Statement st = connDuck.createStatement();) {
        st.executeUpdate("set timezone='Asia/Bangkok'");
        for (net.sf.jsqlparser.statement.Statement statement : CCJSqlParserUtil
            .parseStatements(t.prologue, executorService, parser -> {
            })) {
          st.executeUpdate(statement.toString());
        }
      }
    }

    // update the meta data after prologue has been executed
    JdbcMetaData metaData = new JdbcMetaData(connDuck);
    metaData.build();

    ResultSetMetaData resultSetMetaData = JSQLColumResolver.getResultSetMetaData(t.providedSqlStr,
        metaData, "JSQLTranspilerTest", "main");

    StringWriter stringWriter = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);

    csvWriter.writeNext(new String[] {"#", "label", "name", "table", "schema", "catalog", "type",
        "type name", "precision", "scale", "display size"

        /* we can skip these for now
        , "auto increment"
        , "case sensitive"
        , "searchable"
        , "currency"
        , "nullable"
        , "signed"
        , "readonly"
        , "writable"
        */
    }, true);

    final int maxI = resultSetMetaData.getColumnCount();
    for (int i = 1; i <= maxI; i++) {
      csvWriter.writeNext(new String[] {String.valueOf(i), resultSetMetaData.getColumnLabel(i),
          resultSetMetaData.getColumnName(i), resultSetMetaData.getTableName(i),
          resultSetMetaData.getSchemaName(i), resultSetMetaData.getCatalogName(i),
          JdbcMetaData.getTypeName(resultSetMetaData.getColumnType(i)),
          resultSetMetaData.getColumnTypeName(i), String.valueOf(resultSetMetaData.getPrecision(i)),
          String.valueOf(resultSetMetaData.getScale(i)),
          String.valueOf(resultSetMetaData.getColumnDisplaySize(i))

          /* we can skip these for now
          , "auto increment"
          , "case sensitive"
          , "searchable"
          , "currency"
          , "nullable"
          , "signed"
          , "readonly"
          , "writable"
          */
      }, true);
    }
    csvWriter.flush();
    csvWriter.close();


    Assertions.assertThat(stringWriter.toString().trim()).isEqualToIgnoringCase(t.expectedResult);

  }
}
