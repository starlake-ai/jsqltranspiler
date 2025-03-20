package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcMetaData.ErrorMode;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


class JSQLResolverTest extends AbstractColumnResolverTest {

  @Test
  void testSimpleSelect() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"}};

    String sqlStr = "SELECT sum(b.col1) FROM a, b where a.col2='test' group by b.col3;";

    // all involved columns with tables
    String[][] expectedColumns = {{"b", "col1"}, {"a", "col2"}, {"b", "col3"}};

    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    Set<JdbcColumn> actualColumns = resolver.resolve(sqlStr);

    assertThatTableAndColumnsMatch(actualColumns, expectedColumns);

  }

  @Test
  void testSimpleDelete() throws JSQLParserException, SQLException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"}};

    String sqlStr = "DELETE FROM a, b where a.col2='test' AND b.col3=1;";

    // all involved columns with tables
    String[][] expectedColumns = {{"a", "col2"}, {"b", "col3"}};

    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    resolver.resolve(sqlStr);

    Set<JdbcColumn> actualColumns = resolver.getFlattendedWhereColumns();

    assertThatTableAndColumnsMatch(actualColumns, expectedColumns);

  }

  void assertThatTableAndColumnsMatch(Set<JdbcColumn> columns, String[][] expectedColumns) {
    ArrayList<String[]> actual = new ArrayList<>();
    for (JdbcColumn column : columns) {
      actual.add(new String[] {column.tableName, column.columnName});
    }
    Assertions.assertThatList(actual).containsExactlyInAnyOrder(expectedColumns);
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
  void testUnresolvableIdentifiersIssue48() throws JSQLParserException {

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

  @Test
  void testUnresolvableIdentifiersIssue82() throws JSQLParserException {

    //@formatter:off
    String[][] schemaDefinition = {
            {"foo", "id", "name"},
            {"fooFact", "id", "value"}
    };
    //@formatter:on

    // missing table fooFact1
    String sqlStr = "select * from foo where foo.id in (select fooFact1.id from fooFact1)";
    testMissingTable(schemaDefinition, sqlStr, "fooFact1");

    // undeclared table foo1 (undeclared has priority over missing!)
    sqlStr = "select avg(foo1.id) from foo group by foo.name";
    testMissingDeclaration(schemaDefinition, sqlStr, "foo1");

    // undeclared table foo1 (undeclared has priority over missing!)
    sqlStr = "select sum(foo.id) from foo group by foo.name having foo1.name = 'tets'";
    testMissingDeclaration(schemaDefinition, sqlStr, "foo1");

    // missing column foo.name1
    sqlStr = "select sum(foo.id) from foo group by foo.name having foo.name1 = 'tets'";
    testMissingColumn(schemaDefinition, sqlStr, "foo.name1");
  }

  private String guard(String sqlStr, JdbcMetaData metaData) throws JSQLParserException {

    JSQLResolver resolver = new JSQLResolver(metaData);

    // this does not really work, when there are comments
    if (sqlStr == null || sqlStr.trim().isEmpty()) {
      throw new RuntimeException("Statement is empty");
    }

    try {
      final Set<JdbcColumn> resolve = resolver.resolve(sqlStr);

      // select columns should not be empty
      final List<JdbcColumn> selectColumns = resolver.getSelectColumns();
      if (selectColumns.isEmpty()) {
        throw new RuntimeException("Nothing was selected.");
      }

      // any update columns must be empty
      final List<JdbcColumn> deleteColumns = resolver.getDeleteColumns();
      if (!deleteColumns.isEmpty()) {
        throw new RuntimeException("DELETE is not allowed.");
      }

      final List<JdbcColumn> updateColumns = resolver.getUpdateColumns();
      if (!deleteColumns.isEmpty()) {
        throw new RuntimeException("UPDATE is not allowed.");
      }

      final List<JdbcColumn> insertColumns = resolver.getInsertColumns();
      if (!deleteColumns.isEmpty()) {
        throw new RuntimeException("INSERT is not allowed.");
      }

    } catch (CatalogNotFoundException | ColumnNotFoundException | SchemaNotFoundException | TableNotDeclaredException | TableNotFoundException ex) {
      throw new RuntimeException("Unresolvable Statement ", ex);
    }

    // return statement when all checks have passed
    return sqlStr;
  }

  @Test
  void testGuardFailingOnMissingTable() throws JSQLParserException {
    //@formatter:off
    String[][] schemaDefinition = {
            {"foo", "id", "name"},
            {"fooFact", "id", "value"}
    };
    final JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);
    //@formatter:on

    // missing table fooFact1
    String sqlStr = "select * from foo where foo.id in (select fooFact1.id from fooFact1)";
    RuntimeException exception =
            Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
              guard(sqlStr, jdbcMetaData);
            }).actual();
    Assertions.assertThat(((TableNotFoundException) exception.getCause()).tableName).isEqualTo("fooFact1");
  }

  @Test
  void testGuardSucceedingOnSelect() throws JSQLParserException {
    //@formatter:off
    String[][] schemaDefinition = {
            {"foo", "id", "name"},
            {"fooFact", "id", "value"}
    };
    final JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);
    //@formatter:on

    // missing table fooFact1
    String sqlStr = "select * from foo where foo.id in (select fooFact1.id from fooFact fooFact1)";
    Assertions.assertThat(guard(sqlStr, jdbcMetaData)).isEqualTo(sqlStr);

    // get the resolved select items
    JSQLResolver resolver = new JSQLResolver(jdbcMetaData);
    resolver.resolve(sqlStr);

    Assertions
            .assertThat(resolver.getSelectColumns())
            .containsExactlyInAnyOrder(
                    new JdbcColumn("foo", "id")
                    , new JdbcColumn("foo", "name")
                    , new JdbcColumn("fooFact", "id")
            );
  }

  @Test
  void testGuardFailingOnDeleteSimple() throws JSQLParserException {
    //@formatter:off
    String[][] schemaDefinition = {
            {"foo", "id", "name"},
            {"fooFact", "id", "value"}
    };
    final JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);
    //@formatter:on

    // invalid query, deletes columns
    String sqlStr = "delete from foo where foo.id in (select fooFact1.id from fooFact fooFact1)";
    RuntimeException exception =
            Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
              guard(sqlStr, jdbcMetaData);
            }).actual();
    Assertions.assertThat(exception.getMessage()).isEqualTo("DELETE is not allowed.");
  }

  @Test
  void testGuardFailingOnDeleteTricky() throws JSQLParserException {
    //@formatter:off
    String[][] schemaDefinition = {
            {"foo", "id", "name"},
            {"fooFact", "id", "value"}
    };
    final JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);
    //@formatter:on

    // that's actually a valid SELECT statement!
    // which should be blocked because its DELETING/altering data
    String sqlStr = "with t as (delete from foo returning id) select * from t";

    JSQLResolver resolver = new JSQLResolver(schemaDefinition);
    resolver.resolve(sqlStr);
    Assertions.assertThat(resolver.getDeleteColumns()).isNotEmpty();

    RuntimeException exception =
            Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
              guard(sqlStr, jdbcMetaData);
            }).actual();
    Assertions.assertThat(exception.getMessage()).isEqualTo("DELETE is not allowed.");
  }

}
