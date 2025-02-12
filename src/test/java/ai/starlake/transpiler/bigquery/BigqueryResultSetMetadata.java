package ai.starlake.transpiler.bigquery;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class BigqueryResultSetMetadata implements ResultSetMetaData {
    private final Schema schema;

    public BigqueryResultSetMetadata(Schema schema) {
        this.schema = schema;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.schema.getFields().size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        if(schema.getFields().get(column - 1).getMode() == Field.Mode.REQUIRED){
            return ResultSetMetaData.columnNoNulls;
        } else {
            return ResultSetMetaData.columnNullable;
        }
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return true;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return Math.toIntExact(schema.getFields().get(column - 1).getMaxLength());
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return schema.getFields().get(column - 1).getName();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return schema.getFields().get(column - 1).getName();
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return Math.toIntExact(schema.getFields().get(column - 1).getPrecision());
    }

    @Override
    public int getScale(int column) throws SQLException {
        return Math.toIntExact(schema.getFields().get(column - 1).getScale());
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        int columnType;
        if(schema.getFields().get(column -1).getMode() == Field.Mode.REPEATED) {
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
                    columnType = Types.TIMESTAMP;
                    break;
                case DATE:
                    columnType = Types.DATE;
                    break;
                case TIME:
                    columnType = Types.TIME;
                    break;
                case DATETIME:
                    columnType = Types.TIMESTAMP;
                    break;
                case GEOGRAPHY:
                    columnType = Types.OTHER;
                    break;
                case JSON:
                    columnType = Types.OTHER;
                    break;
                case INTERVAL:
                    columnType = Types.OTHER;
                    break;
                case RANGE:
                    columnType = Types.OTHER;
                    break;
                default:
                    throw new RuntimeException(schema.getFields().get(column - 1).getType().getStandardType() + " not handled during type conversion");
            }
        }
        return columnType;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return schema.getFields().get(column - 1).getType().getStandardType().name();
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return "";
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}