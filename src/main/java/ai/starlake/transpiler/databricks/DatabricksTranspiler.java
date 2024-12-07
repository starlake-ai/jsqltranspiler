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
package ai.starlake.transpiler.databricks;

import ai.starlake.transpiler.JSQLTranspiler;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DatabricksTranspiler extends JSQLTranspiler {

  public DatabricksTranspiler(Map<String, Object> parameters) throws InvocationTargetException,
      NoSuchMethodException, InstantiationException, IllegalAccessException {
    super(DatabricksSelectTranspiler.class, DatabricksExpressionTranspiler.class);
    this.parameters.putAll(parameters);
    this.expressionTranspiler.parameterMap.putAll(parameters);
  }
}
