package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import net.sf.jsqlparser.JSQLParserException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;


class JSQLResolverTest extends AbstractColumnResolverTest {

  @Test
  void testSimplestSchemaProvider() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"}};

    // allows for:
    // JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);

    String sqlStr = "SELECT sum(b.col1) FROM a, b where a.col2='test' group by b.col3;";

    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    resolver.resolve(sqlStr);

    for (JdbcColumn column : resolver.whereColumns) {
      System.out.println(Arrays.asList(column.getChildren().toArray()));
    }

    for (JdbcColumn column : resolver.groupByColumns) {
      System.out.println(Arrays.asList(column.getChildren().toArray()));
    }

  }

  public void testMissingTable(String[][] schemaDefinition, String sqlStr,
      String missingTableName) {
    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    TableNotFoundException exception =
        Assertions.assertThatExceptionOfType(TableNotFoundException.class).isThrownBy(() -> {
          resolver.resolve(sqlStr);
        }).actual();

    Assertions.assertThat(exception.getTableName()).isEqualToIgnoringCase(missingTableName);
  }

  public void testMissingDeclaration(String[][] schemaDefinition, String sqlStr,
      String missingTableName) {
    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    TableNotDeclaredException exception =
        Assertions.assertThatExceptionOfType(TableNotDeclaredException.class).isThrownBy(() -> {
          resolver.resolve(sqlStr);
        }).actual();

    Assertions.assertThat(exception.getTableName()).isEqualToIgnoringCase(missingTableName);
  }

  public void testMissingColumn(String[][] schemaDefinition, String sqlStr,
      String missingColumnName) {
    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    ColumnNotFoundException exception =
        Assertions.assertThatExceptionOfType(ColumnNotFoundException.class).isThrownBy(() -> {
          resolver.resolve(sqlStr);
        }).actual();

    Assertions.assertThat(exception.getColumnName()).endsWith(missingColumnName);
  }


  @Test
  void testIssue48() throws JSQLParserException {

    //@formatter:off
    String[][] schemaDefinition = {
        {"t1exist", "t1c1exist", "t1c2exist", "id"},
        {"fooFact", "t2c1exist", "t21c2exist", "t2c3exist"}
    };
    //@formatter:on

    // we are expecting exception if `t2miss` absent in describing
    String sqlStr =
        "select * from fooFact, t1exist where t1exist.id in (select t2miss.t2c1exist from t2miss)";
    testMissingTable(schemaDefinition, sqlStr, "t2miss");


    // we are expecting exception if t1miss table absent in describing. it relates any aggregation
    // functions (max, min, avg, count and e.c.t.)
    sqlStr = "select sum(t1miss.t1c1exist) from t1exist group by t1exist.t1c2exist";
    testMissingDeclaration(schemaDefinition, sqlStr, "t1miss");

    // we are expecting exception if t1miss table absent in describing. but "select
    // trim(t1exist.t1c2miss) from t1exist" is working. we have exception when we use wrong column
    // name.
    sqlStr = "select trim(t1miss.t1c2exist) from t1exist";
    testMissingDeclaration(schemaDefinition, sqlStr, "t1miss");

    // we are expecting exception if t1miss table absent in describing.
    sqlStr =
        "select sum(t1exist.t1c1exist) FROM t1exist GROUP BY t1exist.t1c2exist HAVING sum(t1miss.t1c1exist) > 5";
    testMissingDeclaration(schemaDefinition, sqlStr, "t1miss");

    // we are expecting exception if t1miss table absent in describing.
    sqlStr =
        "select sum(t1exist.t1c1exist) from t1exist group by t1exist.t1c2exist having t1miss.t1c2exist = 'test'";
    testMissingDeclaration(schemaDefinition, sqlStr, "t1miss");

    // we are expecting exception if column with t1c2miss absent in describing.
    sqlStr =
        "select sum(t1exist.t1c1exist) from t1exist group by t1exist.t1c2exist having t1exist.t1c2miss = 'test'";
    testMissingColumn(schemaDefinition, sqlStr, "t1c2miss");


    // we are expecting exception if table t2miss absent in describing.
    sqlStr = "select max(select t2c1exist from t2miss), t1exist.t1c2exist. from t1exist";
    testMissingTable(schemaDefinition, sqlStr, "t2miss");

    // we are expecting exception if table t1miss absent in describing.
    sqlStr = "select max(select t1c2exist from t1exist), t1miss.t1c2exist from t1exist";
    testMissingDeclaration(schemaDefinition, sqlStr, "t1miss");

  }
}
