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
import ai.starlake.transpiler.schema.TypeMappingSystem;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class JSQLSchemaDiff {
  public final static Logger LOGGER = Logger.getLogger(JSQLSchemaDiff.class.getName());
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
    List<Attribute> attributes = new ArrayList<>();

    try {
      attributes = getDiffH2(sqlStr, qualifiedTargetTableName);
    } catch (Exception ex) {
      // test if target table exists
      final JdbcTable table = meta.getTable(new Table(qualifiedTargetTableName));
      int c = 1;
      for (JdbcColumn column : resultSetMetaData.getColumns()) {
        String columnName = resultSetMetaData.getColumnLabel(c++);
        AttributeStatus status = AttributeStatus.UNCHANGED;
        if (table == null || !table.columns.containsKey(columnName)) {
          status = AttributeStatus.ADDED;
        } else if (!column.typeName.equalsIgnoreCase(table.columns.get(columnName).typeName)) {
          status = AttributeStatus.MODIFIED;
        }
        Attribute attribute = new Attribute(columnName, column.typeName, status);
        attributes.add(attribute);
      }

      // Any removed columns
      if (table != null) {
        for (JdbcColumn column : table.getColumns()) {
          boolean found = false;
          ArrayList<JdbcColumn> columns = resultSetMetaData.getColumns();
          for (int i = 0; i < columns.size(); i++) {
            String columnName = resultSetMetaData.getColumnLabel(i + 1);
            if (column.columnName.equalsIgnoreCase(columnName)) {
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
    }
    return attributes;
  }

  public List<Attribute> getDiffH2(String sqlStr, String qualifiedTargetTableName)
      throws ClassNotFoundException, SQLException, JSQLParserException {
    ArrayList<Attribute> attributes = new ArrayList<>();

    String ddlStr = meta.getDDLStr("");
    LOGGER.fine(ddlStr);

    Class.forName("org.h2.Driver");

    // Wrap an H2 in memory DB
    Driver driver = DriverManager.getDriver("jdbc:h2:mem:");

    Properties info = new Properties();
    info.put("username", "SA");
    info.put("password", "");

    try (Connection conn = driver.connect("jdbc:h2:mem:", info);
        java.sql.Statement st = conn.createStatement();) {

      for (Statement stmt : CCJSqlParserUtil.parseStatements(ddlStr)) {
        st.executeUpdate(stmt.toString());
      }

      PreparedStatement pst = conn.prepareStatement(sqlStr);
      ResultSetMetaData resultSetMetaData = pst.getMetaData();

      // test if target table exists
      final JdbcTable table = meta.getTable(new Table(qualifiedTargetTableName));

      // Create case-insensitive lookup map of existing columns
      Map<String, JdbcColumn> existingColumnsMap = new HashMap<>();
      if (table != null) {
        for (JdbcColumn column : table.getColumns()) {
          existingColumnsMap.put(column.columnName.toLowerCase(), column);
        }
      }

      for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
        String columnName = resultSetMetaData.getColumnLabel(i + 1);

        // Convert to lowercase to match expected output format
        String normalizedColumnName = columnName.toLowerCase();

        AttributeStatus status = AttributeStatus.UNCHANGED;

        // Case-insensitive check for existing column
        if (table == null || !existingColumnsMap.containsKey(normalizedColumnName)) {
          status = AttributeStatus.ADDED;
        }

        String columnTypeName =
            TypeMappingSystem.mapResultSetToTypeName(resultSetMetaData, i + 1, "h2");

        // Convert type name to match expected format (capitalize first letter)
        String normalizedTypeName = capitalizeFirstLetter(columnTypeName);

        Attribute attribute = new Attribute(normalizedColumnName, normalizedTypeName, status);
        attributes.add(attribute);
      }

      // Any removed columns - case-insensitive comparison
      if (table != null) {
        // Create case-insensitive set of result set column names
        Set<String> resultSetColumns = new HashSet<>();
        for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
          String columnName = resultSetMetaData.getColumnLabel(i + 1);
          resultSetColumns.add(columnName.toLowerCase());
        }

        for (JdbcColumn column : table.getColumns()) {
          // Case-insensitive check if column exists in result set
          if (!resultSetColumns.contains(column.columnName.toLowerCase())) {
            // Keep original column name and type for removed columns
            Attribute attribute =
                new Attribute(column.columnName, column.typeName, AttributeStatus.REMOVED);
            attributes.add(attribute);
          }
        }
      }
    }

    return attributes;
  }

  /**
   * Helper method to capitalize first letter of type name
   */
  private String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
