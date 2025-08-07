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
package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import ai.starlake.transpiler.schema.TypeMappingSystem;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSQLSchemaDiff {
  public final static Logger LOGGER = Logger.getLogger(JSQLSchemaDiff.class.getName());
  private final JdbcMetaData meta;

  public JSQLSchemaDiff(Collection<DBSchema> schemas) {
    meta = new JdbcMetaData(schemas);
  }

  public JSQLSchemaDiff(DBSchema... schemas) {
    this(Arrays.asList(schemas));
  }

  public List<Attribute> getDiff(String sqlStr, String qualifiedTargetTableName)
      throws SQLException, JSQLParserException {
    return getDiff(JSQLTranspiler.Dialect.SNOWFLAKE, sqlStr, qualifiedTargetTableName);
  }

  public List<Attribute> getDiff(JSQLTranspiler.Dialect dialect, String sqlStr,
      String qualifiedTargetTableName) throws JSQLParserException, SQLException {
    JSQLColumResolver resolver = new JSQLResolver(meta);
    final JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(sqlStr);
    List<Attribute> attributes = new ArrayList<>();

    JSQLTranspiler.Dialect intoDialect = null;
    try (Connection conn = prepareConnection();) {
      try {
        if (conn.getMetaData().getDriverName().toLowerCase().contains("duck")) {
          intoDialect = JSQLTranspiler.Dialect.DUCK_DB;
        }
        sqlStr = rewriteQuery(dialect, sqlStr, intoDialect);
      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "Failed to rewrite the query:\n" + sqlStr, ex);
      }


      final JdbcTable table = meta.getTable(new Table(qualifiedTargetTableName));
      int c = 1;
      for (JdbcColumn column : resultSetMetaData.getColumns()) {
        String columnName = resultSetMetaData.getColumnLabel(c);
        String typeName = getDataType(conn, sqlStr, c);
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
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public Connection prepareConnection()
      throws SQLException, ClassNotFoundException, JSQLParserException {
    String ddlStr = meta.getDDLStr("");
    LOGGER.fine(ddlStr);

    String urlStr = "jdbc:h2:mem:";
    // try to get a DuckDB connection first
    try {
      Class.forName("org.duckdb.DuckDBDriver");
      urlStr = "jdbc:duckdb:";
      LOGGER.log(Level.INFO, "Found a DuckDB JDBC driver.");
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "No DuckDB JDBC driver found, will use H2 instead.", ex);
      Class.forName("org.h2.Driver");
    }

    // Wrap an H2 in memory DB
    Driver driver = DriverManager.getDriver(urlStr);

    Properties info = new Properties();
    info.put("username", "SA");
    info.put("password", "");

    Connection conn = driver.connect(urlStr, info);
    try (java.sql.Statement st = conn.createStatement()) {
      for (Statement stmt : CCJSqlParserUtil.parseStatements(ddlStr)) {
        try {
          st.executeUpdate(stmt.toString());
        } catch (Exception ex) {
          LOGGER.log(Level.WARNING, "Failed to execute DDL:\n" + stmt, ex);
        }
      }

      return conn;
    }
  }

  public String rewriteQuery(JSQLTranspiler.Dialect dialect, String sqlStr,
      JSQLTranspiler.Dialect intoDialect) throws JSQLParserException {

    // transpile only when DuckDB is used
    if (JSQLTranspiler.Dialect.DUCK_DB.equals(intoDialect)) {
      try {
        sqlStr = JSQLTranspiler.transpileQuery(sqlStr, dialect);
      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "Failed to transpile query:\n" + sqlStr, ex);
      }
    }

    boolean hasWildcards = false;
    PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
    final int size = select.getSelectItems().size();
    for (int i = 0; i < size; i++) {
      Expression expression = select.getSelectItems().get(i).getExpression();
      if (expression instanceof AllColumns) {
        hasWildcards = true;
        break;
      }
    }

    if (hasWildcards) {
      JSQLColumResolver resolver = new JSQLColumResolver(meta);
      return resolver.getResolvedStatementText(sqlStr);
    } else {
      return sqlStr;
    }
  }

  public String getDataType(Connection conn, String sqlStr, int columIndex) {
    String typeName = "";
    try {
      PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

      // test if any wildcards included
      final int size = select.getSelectItems().size();
      for (int i = size; i > 0; i--) {
        if (i != columIndex) {
          select.getSelectItems().remove(i - 1);
        }
      }
      try (PreparedStatement pst = conn.prepareStatement(select.toString());) {
        ResultSetMetaData resultSetMetaData = pst.getMetaData();
        typeName = TypeMappingSystem.mapResultSetToTypeName(resultSetMetaData, 1,
            conn.getMetaData().getDriverName().toLowerCase().contains("duck") ? "duckdb" : "h2");
        typeName = typeName.toLowerCase();
      } catch (SQLException ex) {
        LOGGER.log(Level.WARNING, "Failed execute the query:\n" + select, ex);
      }
    } catch (JSQLParserException ex) {
      LOGGER.log(Level.WARNING, "Failed to parse:\n" + sqlStr, ex);
    }
    return typeName;
  }
}
