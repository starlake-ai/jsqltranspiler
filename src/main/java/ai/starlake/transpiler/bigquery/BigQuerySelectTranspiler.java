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
package ai.starlake.transpiler.bigquery;

import ai.starlake.transpiler.JSQLSelectTranspiler;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.StructType;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BigQuerySelectTranspiler extends JSQLSelectTranspiler {
  public BigQuerySelectTranspiler(Class<? extends ExpressionDeParser> expressionDeparserClass,
      StringBuilder builder) throws NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {
    super(expressionDeparserClass, builder);
  }

  @Override
  public <S> StringBuilder visit(PlainSelect select, S params) {
    if (select.getBigQuerySelectQualifier() != null) {
      switch (select.getBigQuerySelectQualifier()) {
        case AS_VALUE:
          select.setBigQuerySelectQualifier(null);
          Alias alias = select.getSelectItems().get(0).getAlias();

          SelectItem<?> newSelectItem =
              alias != null ? new SelectItem<>(new AllTableColumns(new Table(alias.getName())))
                  : new SelectItem<>(new AllColumns());

          PlainSelect select1 = new PlainSelect(new ParenthesedSelect().withSelect(select))
              .withSelectItems(newSelectItem);
          super.visit(select1, params);
          return null;
        case AS_STRUCT:
          select.setBigQuerySelectQualifier(null);
          StructType structType =
              new StructType(StructType.Dialect.DUCKDB, List.copyOf(select.getSelectItems()));
          select.setSelectItems(List.of(new SelectItem<>(structType, "VALUE_TABLE")));
          break;
      }
    }
    super.visit(select, params);
    return null;
  }
}
