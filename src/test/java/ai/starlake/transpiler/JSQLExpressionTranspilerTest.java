package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JSQLExpressionTranspilerTest {

  @Test
  void isDatePart() {
    Column column = new Column("YEAR");
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    column = new Column("WEEK");
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    Function function = new Function().withName("week").withParameters(new Column("monday"));
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.DATABRICKS));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.SNOWFLAKE));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.AMAZON_REDSHIFT));

  }

  @Test
  void isDateTimePart() {
    Column column = new Column("YEAR");
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    column = new Column("WEEK");
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    Function function = new Function().withName("week").withParameters(new Column("monday"));
    Assertions.assertTrue(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.DATABRICKS));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.SNOWFLAKE));
    Assertions.assertFalse(
        JSQLExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.AMAZON_REDSHIFT));
  }

  @Test
  void hasTimeZoneInfoTest() {
    Assertions
        .assertTrue(JSQLExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789+00:00"));
    Assertions.assertTrue(JSQLExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789+00"));
    Assertions.assertFalse(JSQLExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789"));
    Assertions.assertTrue(JSQLExpressionTranspiler.hasTimeZoneInfo("2008-12-25 15:30:00+00"));
    Assertions.assertTrue(JSQLExpressionTranspiler.hasTimeZoneInfo("2008-12-25 15:30:00+00"));
  }

  @Test
  void testBytesStringToUnicode() {
    StringValue stringValue = new StringValue("\\x61\\x62c").withPrefix("b");

    stringValue
        .setValue(JSQLExpressionTranspiler.convertByteStringToUnicode(stringValue.getValue()));
    stringValue.setPrefix(null);

    Assertions.assertEquals("abc", stringValue.getValue());
  }
}
