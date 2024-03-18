/**
 * Manticore Projects JSQLTranspiler is a multiple SQL Dialect to DuckDB Translation Software.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manticore.transpiler;

import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Expression transpiler.
 */
public class ExpressionTranspiler extends ExpressionDeParser {
  private final JSQLTranspiler.Dialect inputDialect;

  public ExpressionTranspiler(SelectVisitor selectVisitor, StringBuilder buffer,
      JSQLTranspiler.Dialect inputDialect) {
    super(selectVisitor, buffer);
    this.inputDialect = inputDialect;
  }

  public static boolean isDatePart(Expression expression, JSQLTranspiler.Dialect dialect) {
    switch (dialect) {
        case GOOGLE_BIG_QUERY:
            return isDatePartBigQuery(expression);
        case DATABRICKS:
          return isDatePartDataBricks(expression);
        case SNOWFLAKE:
          return isDatePartSnowflake(expression);
        case AMAZON_REDSHIFT:
          return isDatePartRedshift(expression);
      default:
          return isDatePart(expression);
    }
  }

  private static boolean isDatePart(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
            "\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
            , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartBigQuery(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
            , Pattern.compile(
                    "\\b(DAY|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartDataBricks(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartSnowflake(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
            "\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
            , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDatePartRedshift(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePart(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartBigQuery(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
            , Pattern.compile(
            "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)"
            , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartDataBricks(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartSnowflake(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  private static boolean isDateTimePartRedshift(Expression expression) {
    Pattern[] patterns = {
            Pattern.compile(
                    "\\b(MICROSECOND|MILLISECOND|SECOND|MINUTE|HOUR|DAY|WEEK|ISOWEEK|MONTH|QUARTER|YEAR|ISOYEAR|DAYOFWEEK|DAYOFYEAR|DATE|TIME\n)"
                    , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
    };
    boolean isDatePart=false;
    for (Pattern p:patterns) {
      if (expression instanceof Column || expression instanceof Function) {
        Matcher matcher = p.matcher(expression.toString());
        isDatePart |= matcher.matches();
      } else if (expression instanceof StringValue) {
        StringValue stringValue = (StringValue) expression;
        Matcher matcher = p.matcher(stringValue.getValue());
        isDatePart |= matcher.matches();
      }
    }
    return isDatePart;
  }

  public static boolean isDateTimePart(Expression expression, JSQLTranspiler.Dialect dialect) {
    switch (dialect) {
      case GOOGLE_BIG_QUERY:
        return isDateTimePartBigQuery(expression);
      case DATABRICKS:
        return isDateTimePartDataBricks(expression);
      case SNOWFLAKE:
        return isDateTimePartSnowflake(expression);
      case AMAZON_REDSHIFT:
        return isDateTimePartRedshift(expression);
      default:
        return isDateTimePart(expression);
    }
  }

  private boolean parameterWEEK(ExpressionList<?> parameters, int index) {
    // Date Part "WEEK(MONDAY)" or "WEEK(SUNDAY)" seems to be a thing
    Pattern pattern = Pattern.compile(
            "WEEK(\\(\\b(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\))?"
            , Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    Expression p = parameters.get(index);
    if (p instanceof Column || p instanceof Function) {
      Matcher matcher = pattern.matcher(p.toString());
      return matcher.matches();
    } else {
      return false;
    }
  }



  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public void visit(Function function) {
    // @todo: figure out a better rewrite mechanism
    if (function.getName().equalsIgnoreCase("nvl")) {
      function.setName("Coalesce");
      super.visit(function);
    } else if (function.getName().equalsIgnoreCase("date")) {
      ExpressionList<?> parameters = function.getParameters();
      final CastExpression expression;
      switch (parameters.size()) {
        // DATE(DATETIME '2016-12-25 23:59:59') AS date_dt
        case 1:
          // DATE(TIMESTAMP '2016-12-25 05:30:00+07', 'America/Los_Angeles') AS date_tstz
        case 2:
          expression = new CastExpression("Cast").withLeftExpression(parameters.get(0))
              .withType(new ColDataType().withDataType("DATE"));
          super.visit(expression);
          break;
        case 3:
          function.setName("MAKE_DATE");
          super.visit(function);
          break;
      }
    } else if (function.getName().equalsIgnoreCase("date_diff")
        && inputDialect == JSQLTranspiler.Dialect.GOOGLE_BIG_QUERY) {
      ExpressionList<?> parameters = function.getParameters();
      switch (parameters.size()) {
        case 3:
          ExpressionList<Expression> reversedParameters = new ExpressionList<>();
          // Date Part "ISOWEEK" exists and is not supported on DuckDB
          if (parameterWEEK(parameters, 2)) {
            reversedParameters.add(new StringValue("WEEK"));
            buffer.append(" /*APPROXIMATION: WEEK*/ ");
          } else if (parameters.get(2) instanceof Column && ((Column) parameters.get(2)).toString()
              .replaceAll(" ", "").equalsIgnoreCase("ISOWEEK")) {
            reversedParameters.add(new StringValue("WEEK"));
          } else {
            // translate DAY into String 'DAY'
            reversedParameters.add(!(parameters.get(2) instanceof StringValue)
                ? new StringValue(parameters.get(2).toString())
                : parameters.get(2));
          }

          // enforce DATE casting
          reversedParameters.add(parameters.get(1) instanceof StringValue
              ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                  .withValue(((StringValue) parameters.get(1)).toString())
              : parameters.get(1));

          // enforce DATE casting
          reversedParameters.add(parameters.get(0) instanceof StringValue
              ? new DateTimeLiteralExpression().withType(DateTimeLiteralExpression.DateTime.DATE)
                  .withValue(((StringValue) parameters.get(0)).toString())
              : parameters.get(0));
          function.setParameters(reversedParameters);
        default:
          super.visit(function);
      }
    } else {
      super.visit(function);
    }
  }
}
