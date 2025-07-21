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
import net.sf.jsqlparser.statement.select.PlainSelect;

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
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
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

    final JdbcTable table = meta.getTable(new Table(qualifiedTargetTableName));
    int c = 1;
    for (JdbcColumn column : resultSetMetaData.getColumns()) {
      String columnName = resultSetMetaData.getColumnLabel(c);
      String typeName = getDataType(sqlStr, c);
      c++;

      AttributeStatus status = AttributeStatus.UNCHANGED;
      if (table == null || !table.columns.containsKey(columnName)) {
        status = AttributeStatus.ADDED;
      } else if (!column.typeName.equalsIgnoreCase(table.columns.get(columnName).typeName)) {
        status = AttributeStatus.MODIFIED;
      }
      Attribute attribute = new Attribute(columnName, typeName, status);
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
    return attributes;
  }


  public String getDataType(String sqlStr, int columIndex) {
    String typeName = "";

    try {

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

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        final int size = select.getSelectItems().size();
        for (int i = size; i > 0; i--) {
          if (i != columIndex) {
            select.getSelectItems().remove(i - 1);
          }
        }

        PreparedStatement pst = conn.prepareStatement(select.toString());
        ResultSetMetaData resultSetMetaData = pst.getMetaData();

        typeName = TypeMappingSystem.mapResultSetToTypeName(resultSetMetaData, 1, "h2");
        typeName = typeName.toLowerCase();
      } catch (SQLException | JSQLParserException ex) {
        LOGGER.log(Level.FINE, "Failed to get Column Type", ex);
      }
    } catch (ClassNotFoundException | SQLException ex) {
      LOGGER.log(Level.FINE, "Failed to get Column Type", ex);
    }

    return typeName;
  }
}
