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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.sql.DatabaseMetaData.columnNullableUnknown;

public class JdbcColumn implements Comparable<JdbcColumn> {

  public String tableCatalog;
  public String tableSchema;
  public String tableName;
  public String columnName;
  public Integer dataType;
  public String typeName;
  public Integer columnSize;
  public Integer decimalDigits;
  public Integer numericPrecisionRadix;
  public Integer nullable;
  public String remarks;
  public String columnDefinition;
  public Integer characterOctetLength;
  public Integer ordinalPosition;
  public String isNullable;
  public String scopeCatalog;
  public String scopeSchema;
  public String scopeTable;
  public String scopeColumn;
  public Short sourceDataType;
  public String isAutomaticIncrement;
  public String isGeneratedColumn;

  JdbcColumn parent = null;
  List<JdbcColumn> childNodes = new ArrayList<>();

  private Expression expression;

  /* Each column description has the following columns:
  
  TABLE_CAT String => table catalog (may be null)
  TABLE_SCHEM String => table schema (may be null)
  TABLE_NAME String => table name
  COLUMN_NAME String => column name
  DATA_TYPE int => SQL type from java.sql.Types
  TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
  COLUMN_SIZE int => column size.
  BUFFER_LENGTH is not used.
  DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
  NUM_PREC_RADIX int => Radix (typically either 10 or 2)
  NULLABLE int => is NULL allowed.
      columnNoNulls - might not allow NULL values
      columnNullable - definitely allows NULL values
      columnNullableUnknown - nullability unknown
  REMARKS String => comment describing column (may be null)
  COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
  SQL_DATA_TYPE int => unused
  SQL_DATETIME_SUB int => unused
  CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
  ORDINAL_POSITION int => index of column in table (starting at 1)
  IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
      YES --- if the column can include NULLs
      NO --- if the column cannot include NULLs
      empty string --- if the nullability for the column is unknown
  SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
  SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
  SCOPE_TABLE String => table name that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
  SCOPE_COLUMN String => column name (original) within the SCOPE_TABLE
  SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
  IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
      YES --- if the column is auto incremented
      NO --- if the column is not auto incremented
      empty string --- if it cannot be determined whether the column is auto incremented
  IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
      YES --- if this a generated column
      NO --- if this not a generated column
      empty string --- if it cannot be determined whether this is a generated column
   */

  public JdbcColumn(String tableCatalog, String tableSchema, String tableName, String columnName,
      Integer dataType, String typeName, Integer columnSize, Integer decimalDigits,
      Integer numericPrecisionRadix, Integer nullable, String remarks, String columnDefinition,
      Integer characterOctetLength, Integer ordinalPosition, String isNullable, String scopeCatalog,
      String scopeSchema, String scopeTable, String scopeColumn, Short sourceDataType,
      String isAutomaticIncrement, String isGeneratedColumn, Expression expression) {
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
    this.scopeColumn = scopeColumn;
    this.sourceDataType = sourceDataType;
    this.isAutomaticIncrement = isAutomaticIncrement;
    this.isGeneratedColumn = isGeneratedColumn;
    this.expression = expression;
  }

  public JdbcColumn(String tableCatalog, String tableSchema, String tableName, String columnName,
      Integer dataType, String typeName, Integer columnSize, Integer decimalDigits,
      Integer nullable, String remarks, Expression expression) {
    this(tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize,
        decimalDigits, 10, nullable, remarks, "", 0, 0, "", tableCatalog, tableSchema, tableName,
        columnName, (short) 0, "", "", expression);
  }

  public JdbcColumn(String columnName, Integer dataType, String typeName, Integer columnSize,
      Integer decimalDigits, Integer nullable, String remarks, Expression expression) {
    this("", "", "", columnName, dataType, typeName, columnSize, decimalDigits, 10, nullable,
        remarks, "", 0, 0, "", "", "", "", "", (short) 0, "", "", expression);
  }

  public JdbcColumn(String tableCatalog, String tableSchema, String tableName, String columnName,
      Expression expression) {
    this(tableCatalog, tableSchema, tableName, columnName, Types.OTHER, "Other", 0, 0, 10,
        columnNullableUnknown, "", "", 0, 0, "", tableCatalog, tableSchema, tableName, columnName,
        (short) 0, "", "", expression);
  }

  public JdbcColumn(String columnName, Expression expression) {
    this("", "", "", columnName, Types.OTHER, "Other", 0, 0, 10, columnNullableUnknown, "", "", 0,
        0, "", "", "", "", "", (short) 0, "", "", expression);
  }

  public JdbcColumn(String columnName) {
    this(columnName, new Column(columnName));
  }

  @Override
  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JdbcColumn)) {
      return false;
    }

    JdbcColumn column = (JdbcColumn) o;
    return Objects.equals(tableCatalog, column.tableCatalog)
        && Objects.equals(tableSchema, column.tableSchema) && tableName.equals(column.tableName)
        && columnName.equals(column.columnName);
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

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  @Override
  public String toString() {
    if (expression instanceof Function) {
      Function f = (Function) expression;
      return "Function: " + f.toString();
    } else if (expression instanceof Column) {

      StringBuilder b = new StringBuilder();
      if (tableCatalog != null && !tableCatalog.isEmpty()) {
        b.append(tableCatalog).append(".").append(tableSchema != null ? tableSchema : "")
            .append(".");
      } else if (tableSchema != null && !tableSchema.isEmpty()) {
        b.append(tableSchema).append(".");
      }
      b.append(tableName).append(".").append(columnName);

      if (scopeTable != null && !scopeTable.isEmpty()) {
        b.append(" → ");
        if (scopeCatalog != null && !scopeCatalog.isEmpty()) {
          b.append(scopeCatalog).append(".").append(scopeSchema != null ? scopeSchema : "")
              .append(".");
        } else if (scopeSchema != null && !scopeSchema.isEmpty()) {
          b.append(scopeSchema).append(".");
        }
        b.append(scopeTable).append(".").append(scopeColumn);
      }

      b.append(" : ").append(typeName);

      if (columnSize > 0) {
        b.append("(").append(columnSize);
        if (decimalDigits > 0) {
          b.append(", ").append(decimalDigits);
        }
        b.append(")");
      }

      return b.toString();
    } else {
      return expression.getClass().getSimpleName() + ": " + expression.toString();
    }
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
    result = 31 * result + (scopeColumn != null ? scopeColumn.hashCode() : 0);
    result = 31 * result + (sourceDataType != null ? sourceDataType.hashCode() : 0);
    result = 31 * result + (isAutomaticIncrement != null ? isAutomaticIncrement.hashCode() : 0);
    result = 31 * result + (isGeneratedColumn != null ? isGeneratedColumn.hashCode() : 0);
    return result;
  }

  public JdbcColumn getParent() {
    return parent;
  }

  public List<JdbcColumn> getChildren() {
    return childNodes;
  }

  public JdbcColumn add(Collection<JdbcColumn> children) {
    if (children != null) {
      for (JdbcColumn child : children) {
        child.parent = this;
        childNodes.add(child);
      }
    }
    return this;
  }

  public JdbcColumn add(JdbcColumn... children) {
    return add(Arrays.asList(children));
  }

  public Expression getExpression() {
    return expression;
  }

  public JdbcColumn setExpression(Expression expression) {
    this.expression = expression;
    return this;
  }

}
