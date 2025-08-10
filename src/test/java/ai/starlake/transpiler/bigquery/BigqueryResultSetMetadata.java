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

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;

import java.sql.ResultSetMetaData;
import java.sql.Types;

public class BigqueryResultSetMetadata implements ResultSetMetaData {
  private final Schema schema;

  public BigqueryResultSetMetadata(Schema schema) {
    this.schema = schema;
  }

  @Override
  public int getColumnCount() {
    return this.schema.getFields().size();
  }

  @Override
  public boolean isAutoIncrement(int column) {
    return false;
  }

  @Override
  public boolean isCaseSensitive(int column) {
    return false;
  }

  @Override
  public boolean isSearchable(int column) {
    return false;
  }

  @Override
  public boolean isCurrency(int column) {
    return false;
  }

  @Override
  public int isNullable(int column) {
    if (schema.getFields().get(column - 1).getMode() == Field.Mode.REQUIRED) {
      return columnNoNulls;
    } else {
      return columnNullable;
    }
  }

  @Override
  public boolean isSigned(int column) {
    return true;
  }

  @Override
  public int getColumnDisplaySize(int column) {
    return Math.toIntExact(schema.getFields().get(column - 1).getMaxLength());
  }

  @Override
  public String getColumnLabel(int column) {
    return schema.getFields().get(column - 1).getName();
  }

  @Override
  public String getColumnName(int column) {
    return schema.getFields().get(column - 1).getName();
  }

  @Override
  public String getSchemaName(int column) {
    return "";
  }

  @Override
  public int getPrecision(int column) {
    return Math.toIntExact(schema.getFields().get(column - 1).getPrecision());
  }

  @Override
  public int getScale(int column) {
    return Math.toIntExact(schema.getFields().get(column - 1).getScale());
  }

  @Override
  public String getTableName(int column) {
    return "";
  }

  @Override
  public String getCatalogName(int column) {
    return "";
  }

  @Override
  public int getColumnType(int column) {
    int columnType;
    if (schema.getFields().get(column - 1).getMode() == Field.Mode.REPEATED) {
      columnType = Types.ARRAY;
    } else {
      switch (schema.getFields().get(column - 1).getType().getStandardType()) {
        case BOOL:
          columnType = Types.BOOLEAN;
          break;
        case INT64:
          columnType = Types.INTEGER;
          break;
        case FLOAT64:
          columnType = Types.DOUBLE;
          break;
        case NUMERIC:
          columnType = Types.NUMERIC;
          break;
        case BIGNUMERIC:
          columnType = Types.DECIMAL;
          break;
        case STRING:
          columnType = Types.VARCHAR;
          break;
        case BYTES:
          columnType = Types.VARBINARY;
          break;
        case STRUCT:
          columnType = Types.STRUCT;
          break;
        case ARRAY:
          columnType = Types.ARRAY;
          break;
        case TIMESTAMP:
        case DATETIME:
          columnType = Types.TIMESTAMP;
          break;
        case DATE:
          columnType = Types.DATE;
          break;
        case TIME:
          columnType = Types.TIME;
          break;
        case GEOGRAPHY:
        case JSON:
        case INTERVAL:
        case RANGE:
          columnType = Types.OTHER;
          break;
        default:
          throw new RuntimeException(schema.getFields().get(column - 1).getType().getStandardType()
              + " not handled during type conversion");
      }
    }
    return columnType;
  }

  @Override
  public String getColumnTypeName(int column) {
    return schema.getFields().get(column - 1).getType().getStandardType().name();
  }

  @Override
  public boolean isReadOnly(int column) {
    return true;
  }

  @Override
  public boolean isWritable(int column) {
    return false;
  }

  @Override
  public boolean isDefinitelyWritable(int column) {
    return false;
  }

  @Override
  public String getColumnClassName(int column) {
    return "";
  }

  @Override
  public <T> T unwrap(Class<T> iface) {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }
}
