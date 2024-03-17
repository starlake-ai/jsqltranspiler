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

          // Date Part "WEEK(MONDAY)" or "WEEK(SUNDAY)" seems to be a thing
          // Date Part "ISOWEEK" exists and is not supported on DuckDB
          if (parameters.get(2) instanceof Function && ((Function) parameters.get(2)).toString()
              .replaceAll(" ", "").equalsIgnoreCase("WEEK(MONDAY)")) {
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
