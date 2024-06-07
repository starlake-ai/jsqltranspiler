/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Andreas Reichel <andreas@manticore-projects.com> on behalf of Starlake.AI
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

public class JdbcColumn implements Comparable<JdbcColumn> {

  String tableCatalog;
  String tableSchema;
  String tableName;
  String columnName;
  Integer dataType;
  String typeName;
  Integer columnSize;
  Integer decimalDigits;
  Integer numericPrecisionRadix;
  Integer nullable;
  String remarks;
  String columnDefinition;
  Integer characterOctetLength;
  Integer ordinalPosition;
  String isNullable;
  String scopeCatalog;
  String scopeSchema;
  String scopeTable;
  Short sourceDataType;
  String isAutomaticIncrement;
  String isGeneratedColumn;

  public JdbcColumn(String tableCatalog, String tableSchema, String tableName, String columnName,
      Integer dataType, String typeName, Integer columnSize, Integer decimalDigits,
      Integer numericPrecisionRadix, Integer nullable, String remarks, String columnDefinition,
      Integer characterOctetLength, Integer ordinalPosition, String isNullable, String scopeCatalog,
      String scopeSchema, String scopeTable, Short sourceDataType, String isAutomaticIncrement,
      String isGeneratedColumn) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.columnName = columnName;
    this.dataType = dataType;
    this.typeName = typeName;
    this.columnSize = columnSize;
    this.decimalDigits = decimalDigits;
    this.numericPrecisionRadix = numericPrecisionRadix;
    this.nullable = nullable;
    this.remarks = remarks;
    this.columnDefinition = columnDefinition;
    this.characterOctetLength = characterOctetLength;
    this.ordinalPosition = ordinalPosition;
    this.isNullable = isNullable;
    this.scopeCatalog = scopeCatalog;
    this.scopeSchema = scopeSchema;
    this.scopeTable = scopeTable;
    this.sourceDataType = sourceDataType;
    this.isAutomaticIncrement = isAutomaticIncrement;
    this.isGeneratedColumn = isGeneratedColumn;
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public int compareTo(JdbcColumn o) {
    int compareTo = tableCatalog == null && o.tableCatalog == null ? 0
        : tableCatalog != null ? tableCatalog.compareToIgnoreCase(o.tableCatalog)
            : -o.tableCatalog.compareToIgnoreCase(tableCatalog);

    if (compareTo == 0) {
      compareTo = tableSchema == null && o.tableSchema == null ? 0
          : tableSchema != null ? tableSchema.compareToIgnoreCase(o.tableSchema)
              : -o.tableSchema.compareToIgnoreCase(tableSchema);
    }

    if (compareTo == 0) {
      compareTo = tableName.compareToIgnoreCase(o.tableName);
    }

    if (compareTo == 0) {
      compareTo = ordinalPosition.compareTo(o.ordinalPosition);
    }

    return compareTo;
  }

  @Override
  public String toString() {
    return tableCatalog + "." + tableSchema + "." + tableName + "." + columnName + "\t" + typeName
        + " (" + columnSize + ", " + decimalDigits + ")";
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcColumn)) {
      return false;
    }

    JdbcColumn jdbcColumn = (JdbcColumn) o;

    if (!Objects.equals(tableCatalog, jdbcColumn.tableCatalog)) {
      return false;
    }
    if (!Objects.equals(tableSchema, jdbcColumn.tableSchema)) {
      return false;
    }
    if (!tableName.equals(jdbcColumn.tableName)) {
      return false;
    }
    if (!columnName.equals(jdbcColumn.columnName)) {
      return false;
    }
    if (!dataType.equals(jdbcColumn.dataType)) {
      return false;
    }
    if (!Objects.equals(typeName, jdbcColumn.typeName)) {
      return false;
    }
    if (!columnSize.equals(jdbcColumn.columnSize)) {
      return false;
    }
    if (!Objects.equals(decimalDigits, jdbcColumn.decimalDigits)) {
      return false;
    }
    if (!Objects.equals(numericPrecisionRadix, jdbcColumn.numericPrecisionRadix)) {
      return false;
    }
    if (!Objects.equals(nullable, jdbcColumn.nullable)) {
      return false;
    }
    if (!Objects.equals(remarks, jdbcColumn.remarks)) {
      return false;
    }
    if (!Objects.equals(columnDefinition, jdbcColumn.columnDefinition)) {
      return false;
    }
    if (!Objects.equals(characterOctetLength, jdbcColumn.characterOctetLength)) {
      return false;
    }
    if (!Objects.equals(ordinalPosition, jdbcColumn.ordinalPosition)) {
      return false;
    }
    if (!Objects.equals(isNullable, jdbcColumn.isNullable)) {
      return false;
    }
    if (!Objects.equals(scopeCatalog, jdbcColumn.scopeCatalog)) {
      return false;
    }
    if (!Objects.equals(scopeSchema, jdbcColumn.scopeSchema)) {
      return false;
    }
    if (!Objects.equals(scopeTable, jdbcColumn.scopeTable)) {
      return false;
    }
    if (!Objects.equals(sourceDataType, jdbcColumn.sourceDataType)) {
      return false;
    }
    if (!Objects.equals(isAutomaticIncrement, jdbcColumn.isAutomaticIncrement)) {
      return false;
    }
    return Objects.equals(isGeneratedColumn, jdbcColumn.isGeneratedColumn);
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public int hashCode() {
    int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
    result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
    result = 31 * result + tableName.hashCode();
    result = 31 * result + columnName.hashCode();
    result = 31 * result + dataType.hashCode();
    result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
    result = 31 * result + columnSize.hashCode();
    result = 31 * result + (decimalDigits != null ? decimalDigits.hashCode() : 0);
    result = 31 * result + (numericPrecisionRadix != null ? numericPrecisionRadix.hashCode() : 0);
    result = 31 * result + (nullable != null ? nullable.hashCode() : 0);
    result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
    result = 31 * result + (columnDefinition != null ? columnDefinition.hashCode() : 0);
    result = 31 * result + (characterOctetLength != null ? characterOctetLength.hashCode() : 0);
    result = 31 * result + (ordinalPosition != null ? ordinalPosition.hashCode() : 0);
    result = 31 * result + (isNullable != null ? isNullable.hashCode() : 0);
    result = 31 * result + (scopeCatalog != null ? scopeCatalog.hashCode() : 0);
    result = 31 * result + (scopeSchema != null ? scopeSchema.hashCode() : 0);
    result = 31 * result + (scopeTable != null ? scopeTable.hashCode() : 0);
    result = 31 * result + (sourceDataType != null ? sourceDataType.hashCode() : 0);
    result = 31 * result + (isAutomaticIncrement != null ? isAutomaticIncrement.hashCode() : 0);
    result = 31 * result + (isGeneratedColumn != null ? isGeneratedColumn.hashCode() : 0);
    return result;
  }
}
