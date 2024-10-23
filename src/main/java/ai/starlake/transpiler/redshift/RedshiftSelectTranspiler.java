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
package ai.starlake.transpiler.redshift;

import ai.starlake.transpiler.JSQLSelectTranspiler;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.lang.reflect.InvocationTargetException;

public class RedshiftSelectTranspiler extends JSQLSelectTranspiler {

  public RedshiftSelectTranspiler(Class<? extends ExpressionDeParser> expressionDeparserClass,
      StringBuilder builder) throws NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {
    super(expressionDeparserClass, builder);
  }
}
