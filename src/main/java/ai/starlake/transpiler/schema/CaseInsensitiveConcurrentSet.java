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
package ai.starlake.transpiler.schema;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CaseInsensitiveConcurrentSet {
  private final ConcurrentMap<String, Boolean> map;

  private CaseInsensitiveConcurrentSet() {
    this.map = new ConcurrentHashMap<>();
  }

  public static Set<String> newSet() {
    return Collections.newSetFromMap(new CaseInsensitiveConcurrentSet().map);
  }

  public boolean add(String s) {
    return map.putIfAbsent(s.toLowerCase(), Boolean.TRUE) == null;
  }

  public boolean contains(String s) {
    return map.containsKey(s.toLowerCase());
  }

  public boolean remove(String s) {
    return map.remove(s.toLowerCase()) != null;
  }

}
