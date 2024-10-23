/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

  @Test
  void castDateTime2() {
    // general timezones are not accepted
    // Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T12:34:56.789+0000'",
    // JSQLExpressionTranspiler.castDateTime("2024-03-20 12:34:56.789 'Asia/Bangkok'").toString());

    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T12:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("2024-03-20 12:34:56.789+00:00").toString());
    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T05:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("2024-03-20 12:34:56.789+07:00").toString());
    Assertions.assertEquals("TIMESTAMP WITHOUT TIME ZONE '2024-03-20T12:34:56.789'",
        JSQLExpressionTranspiler.castDateTime("2024-03-20 12:34:56.789").toString());
    Assertions.assertEquals("TIMESTAMP WITHOUT TIME ZONE '2024-03-20T12:34:56.000'",
        JSQLExpressionTranspiler.castDateTime("2024-03-20 12:34:56").toString());

    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T12:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("20240320 12:34:56.789+00:00").toString());
    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T05:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("20240320 12:34:56.789+07:00").toString());
    Assertions.assertEquals("TIMESTAMP WITHOUT TIME ZONE '2024-03-20T12:34:56.789'",
        JSQLExpressionTranspiler.castDateTime("20240320 12:34:56.789").toString());

    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T12:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("20240320T123456.789+00:00").toString());
    Assertions.assertEquals("TIMESTAMP WITH TIME ZONE '2024-03-20T05:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("20240320T123456.789+07:00").toString());
    Assertions.assertEquals("TIMESTAMP WITHOUT TIME ZONE '2024-03-20T12:34:56.789'",
        JSQLExpressionTranspiler.castDateTime("20240320T123456.789").toString());

    Assertions.assertEquals("TIME WITHOUT TIME ZONE '12:34:56.789'",
        JSQLExpressionTranspiler.castDateTime("12:34:56.789").toString());
    Assertions.assertEquals("TIME WITH TIME ZONE '05:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("12:34:56.789+07:00").toString());
    Assertions.assertEquals("TIME WITHOUT TIME ZONE '12:34:56.789'",
        JSQLExpressionTranspiler.castDateTime("123456.789").toString());
    Assertions.assertEquals("TIME WITH TIME ZONE '05:34:56.789+0000'",
        JSQLExpressionTranspiler.castDateTime("123456.789+07:00").toString());

    Assertions.assertEquals("DATE '2024-03-20'",
        JSQLExpressionTranspiler.castDateTime("2024-03-20").toString());
    Assertions.assertEquals("DATE '2024-03-20'",
        JSQLExpressionTranspiler.castDateTime("20240320").toString());

    // Ordinal Dates
    Assertions.assertEquals("DATE '1981-04-05'",
        JSQLExpressionTranspiler.castDateTime("1981-095").toString());

    // collides
    // Assertions.assertEquals("DATE '1981-04-05'",
    // JSQLExpressionTranspiler.castDateTime("1981095").toString());

    // Week Dates
    Assertions.assertEquals("DATE '2008-12-29'",
        JSQLExpressionTranspiler.castDateTime("2009-W01-1").toString());
    Assertions.assertEquals("DATE '2008-12-29'",
        JSQLExpressionTranspiler.castDateTime("2009W011").toString());

    // @todo: make this work too
    // Assertions.assertEquals("DATE '2010-01-03'",
    // JSQLExpressionTranspiler.castDateTime("2009-W53-7").toString());
  }

  @Test
  @Disabled
  void castDateTime3() {
    // no support for micro-seconds
    Assertions.assertEquals("TIME WITHOUT TIME ZONE '12:34:56.789234'",
        JSQLExpressionTranspiler.castDateTime("12:34:56.789235").toString());
  }
}
