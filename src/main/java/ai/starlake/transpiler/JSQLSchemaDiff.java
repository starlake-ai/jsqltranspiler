package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class JSQLSchemaDiff {
  private final JdbcMetaData meta;

  public JSQLSchemaDiff(DBSchema schema) {
    meta = new JdbcMetaData(schema.getCatalogName(), schema.getSchemaName());
    for (Entry<String, Collection<Attribute>> entry : schema.getTables().entrySet()) {
      ArrayList<JdbcColumn> jdbcColumns = new ArrayList<>();
      for (Attribute attribute : entry.getValue()) {
        JdbcColumn jdbcColumn =
            new JdbcColumn(schema.getCatalogName(), schema.getSchemaName(), entry.getKey(),
                attribute.getName(), Types.OTHER, attribute.getType(), 0, 0, 0, "", null);
        jdbcColumns.add(jdbcColumn);
      }
      meta.addTable(entry.getKey(), jdbcColumns);
    }
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
