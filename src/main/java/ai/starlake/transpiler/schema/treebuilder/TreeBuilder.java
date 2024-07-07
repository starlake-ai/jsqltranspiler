package ai.starlake.transpiler.schema.treebuilder;

import ai.starlake.transpiler.schema.JdbcResultSetMetaData;

import java.sql.SQLException;

public abstract class TreeBuilder<T> {
  public JdbcResultSetMetaData resultSetMetaData;

  public TreeBuilder(JdbcResultSetMetaData resultSetMetaData) {
    this.resultSetMetaData = resultSetMetaData;
  }

  public abstract T getConvertedTree() throws SQLException;
}
