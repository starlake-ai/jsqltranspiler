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

import java.util.Objects;

public class JdbcIndexColumn implements Comparable<JdbcIndexColumn> {

  Short ordinalPosition;
  String columnName;
  String ascOrDesc;
  Long cardinality;
  Long pages;
  String filterCondition;

  public JdbcIndexColumn(Short ordinalPosition, String columnName, String ascOrDesc,
      Long cardinality, Long pages, String filterCondition) {
    this.ordinalPosition = ordinalPosition;
    this.columnName = columnName;
    this.ascOrDesc = ascOrDesc;
    this.cardinality = cardinality;
    this.pages = pages;
    this.filterCondition = filterCondition;
  }

  @Override
  public int compareTo(JdbcIndexColumn o) {
    return ordinalPosition.compareTo(o.ordinalPosition);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcIndexColumn)) {
      return false;
    }

    JdbcIndexColumn that = (JdbcIndexColumn) o;

    if (!ordinalPosition.equals(that.ordinalPosition)) {
      return false;
    }
    if (!columnName.equals(that.columnName)) {
      return false;
    }
    if (!Objects.equals(ascOrDesc, that.ascOrDesc)) {
      return false;
    }
    if (!Objects.equals(cardinality, that.cardinality)) {
      return false;
    }
    if (!Objects.equals(pages, that.pages)) {
      return false;
    }
    return Objects.equals(filterCondition, that.filterCondition);
  }

  @Override
  public int hashCode() {
    int result = ordinalPosition.hashCode();
    result = 31 * result + columnName.hashCode();
    result = 31 * result + (ascOrDesc != null ? ascOrDesc.hashCode() : 0);
    result = 31 * result + (cardinality != null ? cardinality.hashCode() : 0);
    result = 31 * result + (pages != null ? pages.hashCode() : 0);
    result = 31 * result + (filterCondition != null ? filterCondition.hashCode() : 0);
    return result;
  }
}
