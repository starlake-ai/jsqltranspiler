package com.manticore.transpiler;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionTranspilerTest {

  @Test
  void isDatePart() {
    Column column = new Column("YEAR");
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    column = new Column("WEEK");
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    Function function = new Function().withName("week").withParameters(new Column("monday"));
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));
    Assertions
        .assertFalse(ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.DATABRICKS));
    Assertions
        .assertFalse(ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.SNOWFLAKE));
    Assertions.assertFalse(
        ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.AMAZON_REDSHIFT));

  }

  @Test
  void isDateTimePart() {
    Column column = new Column("YEAR");
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    column = new Column("WEEK");
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(column, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));

    Function function = new Function().withName("week").withParameters(new Column("monday"));
    Assertions.assertTrue(
        ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY));
    Assertions
        .assertFalse(ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.DATABRICKS));
    Assertions
        .assertFalse(ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.SNOWFLAKE));
    Assertions.assertFalse(
        ExpressionTranspiler.isDatePart(function, JSQLTranspiler.Dialect.AMAZON_REDSHIFT));
  }

  @Test
  void hasTimeZoneInfoTest() {
    Assertions.assertTrue(ExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789+00:00"));
    Assertions.assertTrue(ExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789+00"));
    Assertions.assertFalse(ExpressionTranspiler.hasTimeZoneInfo("2024-03-20 12:34:56.789"));
    Assertions.assertTrue(ExpressionTranspiler.hasTimeZoneInfo("2008-12-25 15:30:00+00"));
    Assertions.assertTrue(ExpressionTranspiler.hasTimeZoneInfo("2008-12-25 15:30:00+00"));
  }
}
