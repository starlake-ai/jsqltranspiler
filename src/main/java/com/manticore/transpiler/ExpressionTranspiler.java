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
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExpressionTranspiler extends ExpressionDeParser {
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
           expression =
                  new CastExpression("Cast")
                          .withLeftExpression(parameters.get(0))
                          .withType(new ColDataType().withDataType("DATE"));
          super.visit(expression);
          break;
        case 3:
          function.setName("MAKE_DATE");
          super.visit(function);
          break;
      }
    }
  }
}
