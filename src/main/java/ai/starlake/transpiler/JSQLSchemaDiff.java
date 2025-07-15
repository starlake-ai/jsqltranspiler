package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import ai.starlake.transpiler.schema.JdbcCatalog;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcSchema;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class JSQLSchemaDiff {
  private final JdbcMetaData meta;

  public JSQLSchemaDiff(Collection<DBSchema> schemas) {
    meta = new JdbcMetaData();
    for (DBSchema schema : schemas) {
      JdbcCatalog jdbcCatalog = meta.get(schema.getCatalogName());
      if (jdbcCatalog == null) {
        jdbcCatalog = new JdbcCatalog(schema.getCatalogName(), ".");
        meta.put(jdbcCatalog);
      }

      JdbcSchema jdbcSchema = jdbcCatalog.get(schema.getSchemaName());
      if (jdbcSchema == null) {
        jdbcSchema = new JdbcSchema(schema.getSchemaName(), schema.getCatalogName());
        jdbcCatalog.put(jdbcSchema);
      }

      for (Entry<String, Collection<Attribute>> entry : schema.getTables().entrySet()) {
        JdbcTable jdbcTable = new JdbcTable(jdbcCatalog, jdbcSchema, entry.getKey());
        jdbcSchema.put(jdbcTable);

        for (Attribute attribute : entry.getValue()) {
          JdbcColumn jdbcColumn =
              new JdbcColumn(schema.getCatalogName(), schema.getSchemaName(), entry.getKey(),
                  attribute.getName(), Types.OTHER, attribute.getType(), 0, 0, 0, "", null);
          jdbcTable.put(jdbcColumn.columnName, jdbcColumn);
        }
      }
    }
  }

  public JSQLSchemaDiff(DBSchema... schemas) {
    this(Arrays.asList(schemas));
  }

  public List<Attribute> getDiff(String sqlStr, String qualifiedTargetTableName)
      throws JSQLParserException, SQLException {
    JSQLColumResolver resolver = new JSQLResolver(meta);
    final JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(sqlStr);

    ArrayList<Attribute> attributes = new ArrayList<>();

    // test if target table exists
    final JdbcTable table = meta.getTable(new Table(qualifiedTargetTableName));
    int c = 1;
    for (JdbcColumn column : resultSetMetaData.getColumns()) {
      AttributeStatus status = AttributeStatus.UNCHANGED;
      if (table == null || !table.columns.containsKey(column.columnName)) {
        status = AttributeStatus.ADDED;
      }
      Attribute attribute =
          new Attribute(resultSetMetaData.getColumnLabel(c++), column.typeName, status);
      attributes.add(attribute);
    }

    if (table != null) {
      for (JdbcColumn column : table.getColumns()) {
        boolean found = false;
        for (JdbcColumn column1 : resultSetMetaData.getColumns()) {
          if (column.columnName.equalsIgnoreCase(column1.columnName)) {
            found = true;
            break;
          }
        }

        if (!found) {
          Attribute attribute =
              new Attribute(column.columnName, column.typeName, AttributeStatus.REMOVED);
          attributes.add(attribute);
        }
      }
    }
    return attributes;
  }
}
