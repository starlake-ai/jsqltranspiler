/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI <hayssam.saleh@starlake.ai>
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

import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;

import java.util.stream.Collectors;

public class BigqueryObjectSerializer {
  public static Object serialize(Object object) {
    if (object == null) {
      return null;
    } else if (object instanceof FieldValueList) {
      FieldValueList list = (FieldValueList) object;
      return list.stream().map(BigqueryObjectSerializer::serialize).collect(Collectors.toList());
    } else if (object instanceof FieldValue) {
      FieldValue fv = (FieldValue) object;
      if (fv.getValue() instanceof FieldValueList) {
        return serialize(fv.getValue());
      } else {
        return String.valueOf(fv.getValue());
      }
    } else {
      return String.valueOf(object);
    }
  }
}
