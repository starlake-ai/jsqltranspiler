package ai.starlake.transpiler;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class JSQLAbstractReplacerTest {
  @Test
  void testWithShadowing() throws JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}};

    // WITH item `a` shadows the base table and does not contain a column `col1`
    String sqlStr = "with a as (select 1 from a) SELECT a.col1 from a;";
    JSQLReplacer replacer = new JSQLReplacer(schemaDefinition);

    // So we expect a Column Not Found exception
    Assertions.assertThatExceptionOfType(ColumnNotFoundException.class).isThrownBy(() -> {
      PlainSelect st = (PlainSelect) replacer.replace(sqlStr, Map.of("a", "test"));

      // only physical BASE TABLE would get replaced
      Assertions.assertThat(st.toString())
          .isEqualToIgnoringCase("with a as (select 1 from test) SELECT a.col1 from a;");
    });

    // only physical BASE TABLE would get replaced
    String sqlStr2 = "with a as (select a.col2 AS col1 from a) SELECT a.col1 from a;";
    String expected = "with a as (select test.col2 AS col1 from test) SELECT a.col1 from a";

    assertThatRewritesInto(sqlStr2, Map.of("a", "test"), expected);
  }

  @Test
  void testBaseTableResolution() throws JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}};

    // base table contains a column `col1`
    String sqlStr = "SELECT a.col1 from a;";
    JSQLReplacer replacer = new JSQLReplacer(schemaDefinition);

    // So we expect no exception
    Assertions.assertThatNoException().isThrownBy(() -> {
      PlainSelect st = (PlainSelect) replacer.replace(sqlStr, Map.of("a", "test"));

      // only physical BASE TABLE would get replaced
      Assertions.assertThat(st.toString()).isEqualToIgnoringCase("SELECT test.col1 from test");
    });
  }

  @Test
  void testAliasShadowing() throws JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "cola", "colb", "colc"}};

    // base table contains a column `col1`
    String sqlStr = "SELECT b.col1 from a AS b;";
    JSQLReplacer resolver = new JSQLReplacer(schemaDefinition);

    // So we expect no exception
    Assertions.assertThatNoException().isThrownBy(() -> {
      PlainSelect select = (PlainSelect) resolver.replace(sqlStr, Map.of("b", "test"));

      // table column is shadowed by alias, so table must not get resolved
      Column c = select.getSelectItem(0).getExpression(Column.class);
      Assertions.assertThat(c.getResolvedTable()).isNull();
    });
  }

  @Test
  void testSwapTablenames() throws JSQLParserException {
    // @formatter:off
    String sqlStr =
        "SELECT a.*\n"
        + "FROM (  SELECT  a.col3\n"
        + "                , Sum( a.col2 )\n"
        + "        FROM a inner join b on a.col1=b.col1\n"
        + "        WHERE a.col1 = b.col1\n"
        + "        GROUP BY a.col3\n"
        + "        HAVING Sum( a.col2 ) > 0 ) AS a";
    // @formatter:on

    Map<String, String> replacements = Map.of("a", "b", "b", "a");

    // @formatter:off
    String expected =
        "SELECT a.col3, a.sum \n"
        + "FROM (  SELECT  b.col3\n"
        + "                , Sum( b.col2 )\n"
        + "        FROM b inner join a on b.col1=a.col1\n"
        + "        WHERE b.col1 = a.col1\n"
        + "        GROUP BY b.col3\n"
        + "        HAVING Sum( b.col2 ) > 0 ) AS a";
    // @formatter:on

    assertThatRewritesInto(sqlStr, replacements, expected);
  }

  Statement assertThatRewritesInto(String sqlStr, Map<String, String> replacements, String expected)
      throws JSQLParserException {
    String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"},
        {"test", "col1", "col2", "col3"}};
    return assertThatRewritesInto(schemaDefinition, sqlStr, replacements, expected);
  }

  Statement assertThatRewritesInto(String[][] schemaDefinition, String sqlStr,
      Map<String, String> replacements, String expected) throws JSQLParserException {
    JSQLReplacer replacer = new JSQLReplacer(schemaDefinition);
    Statement st = replacer.replace(sqlStr, replacements);
    Assertions.assertThat(JSQLColumnResolverTest.sanitize(st.toString()))
        .isEqualToIgnoringCase(JSQLColumnResolverTest.sanitize(expected));
    return st;
  }
}
