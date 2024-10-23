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

import java.util.LinkedList;
import java.util.Objects;

public class JdbcReference {

  String pkTableCatalog;
  String pkTableSchema;
  String pkTableName;
  String fkTableCatalog;
  String fkTableSchema;
  String fkTableName;
  Short updateRule;
  Short deleteRule;
  String fkName;
  String pkName;
  Short deferrability;

  LinkedList<String[]> columns = new LinkedList<>();

  public JdbcReference(String pkTableCatalog, String pkTableSchema, String pkTableName,
      String fkTableCatalog, String fkTableSchema, String fkTableName, Short updateRule,
      Short deleteRule, String fkName, String pkName, Short deferrability) {
    this.pkTableCatalog = pkTableCatalog;
    this.pkTableSchema = pkTableSchema;
    this.pkTableName = pkTableName;
    this.fkTableCatalog = fkTableCatalog;
    this.fkTableSchema = fkTableSchema;
    this.fkTableName = fkTableName;
    this.updateRule = updateRule;
    this.deleteRule = deleteRule;
    this.fkName = fkName;
    this.pkName = pkName;
    this.deferrability = deferrability;
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcReference)) {
      return false;
    }

    JdbcReference jdbcReference = (JdbcReference) o;

    if (!Objects.equals(pkTableCatalog, jdbcReference.pkTableCatalog)) {
      return false;
    }
    if (!Objects.equals(pkTableSchema, jdbcReference.pkTableSchema)) {
      return false;
    }
    if (!pkTableName.equals(jdbcReference.pkTableName)) {
      return false;
    }
    if (!Objects.equals(fkTableCatalog, jdbcReference.fkTableCatalog)) {
      return false;
    }
    if (!Objects.equals(fkTableSchema, jdbcReference.fkTableSchema)) {
      return false;
    }
    if (!fkTableName.equals(jdbcReference.fkTableName)) {
      return false;
    }
    if (!Objects.equals(updateRule, jdbcReference.updateRule)) {
      return false;
    }
    if (!Objects.equals(deleteRule, jdbcReference.deleteRule)) {
      return false;
    }
    if (!fkName.equals(jdbcReference.fkName)) {
      return false;
    }
    if (!pkName.equals(jdbcReference.pkName)) {
      return false;
    }
    if (!Objects.equals(deferrability, jdbcReference.deferrability)) {
      return false;
    }
    return Objects.equals(columns, jdbcReference.columns);
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public int hashCode() {
    int result = pkTableCatalog != null ? pkTableCatalog.hashCode() : 0;
    result = 31 * result + (pkTableSchema != null ? pkTableSchema.hashCode() : 0);
    result = 31 * result + pkTableName.hashCode();
    result = 31 * result + (fkTableCatalog != null ? fkTableCatalog.hashCode() : 0);
    result = 31 * result + (fkTableSchema != null ? fkTableSchema.hashCode() : 0);
    result = 31 * result + fkTableName.hashCode();
    result = 31 * result + (updateRule != null ? updateRule.hashCode() : 0);
    result = 31 * result + (deleteRule != null ? deleteRule.hashCode() : 0);
    result = 31 * result + fkName.hashCode();
    result = 31 * result + pkName.hashCode();
    result = 31 * result + (deferrability != null ? deferrability.hashCode() : 0);
    result = 31 * result + (columns != null ? columns.hashCode() : 0);
    return result;
  }
}
