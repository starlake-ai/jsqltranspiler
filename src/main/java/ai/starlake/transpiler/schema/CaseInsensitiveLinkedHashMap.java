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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A Case insensitive linked hash map preserving the original spelling of the keys. It can be used
 * for looking up a database's schemas, tables, columns, indices and constraints.
 *
 * @param <V> the type parameter
 */
public class CaseInsensitiveLinkedHashMap<V> extends LinkedHashMap<String, V> {
  private final Map<String, String> originalKeys = new LinkedHashMap<>();
  private static final Pattern LEADING_TRAILING_QUOTES_PATTERN =
      Pattern.compile("^[\"\\[`]+|[\"\\]`]+$");

  /**
   * Removes leading and trailing quotes from a SQL quoted identifier
   *
   * @param quotedIdentifier the quoted identifier
   * @return the pure identifier without quotes
   */
  public static String unquote(String quotedIdentifier) {
    return LEADING_TRAILING_QUOTES_PATTERN.matcher(quotedIdentifier).replaceAll("");
  }

  @Override
  public V put(String key, V value) {
    String lowerCaseKey = unquote(key.toLowerCase());
    originalKeys.put(lowerCaseKey, unquote(key));
    return super.put(lowerCaseKey, value);
  }

  @Override
  public V get(Object key) {
    if (key instanceof String) {
      String unquotedLowerCaseKey = unquote(((String) key).toLowerCase());
      return super.get(unquotedLowerCaseKey);
    }
    return null;
  }

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String) {
      String unquotedLowerCaseKey = unquote(((String) key).toLowerCase());
      return super.containsKey(unquotedLowerCaseKey);
    }
    return false;
  }

  @Override
  public V remove(Object key) {
    if (key instanceof String) {
      String unquotedLowerCaseKey = unquote(((String) key).toLowerCase());
      originalKeys.remove(unquotedLowerCaseKey);
      return super.remove(unquotedLowerCaseKey);
    }
    return null;
  }

  @Override
  public void clear() {
    originalKeys.clear();
    super.clear();
  }

  @Override
  public Set<Map.Entry<String, V>> entrySet() {
    Set<Map.Entry<String, V>> originalEntrySet = new LinkedHashSet<>();
    for (Map.Entry<String, V> entry : super.entrySet()) {
      String originalKey = originalKeys.get(entry.getKey());
      originalEntrySet.add(new AbstractMap.SimpleEntry<>(originalKey, entry.getValue()));
    }
    return originalEntrySet;
  }

  @Override
  public Set<String> keySet() {
    Set<String> originalKeySet = new LinkedHashSet<>();
    for (String key : super.keySet()) {
      originalKeySet.add(originalKeys.get(key));
    }
    return originalKeySet;
  }
}

