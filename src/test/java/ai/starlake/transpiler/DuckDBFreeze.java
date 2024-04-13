package ai.starlake.transpiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DuckDBFreeze {

  @Test
  void testFreeze() throws SQLException {
    String sqlString = "SELECT LENGTH(42) AS L";

    Properties info = new Properties();

    // crashes
    // info.put("old_implicit_casting", true);

    // works
    info.put("old_implicit_casting", "true");

    try (Connection connDuck = DriverManager.getConnection("jdbc:duckdb:", info);
        Statement st = connDuck.createStatement();
        ResultSet rs = st.executeQuery(sqlString);) {
      Assertions.assertTrue(rs.next());

      Assertions.assertEquals(2, rs.getInt(1));
    }
  }
}
