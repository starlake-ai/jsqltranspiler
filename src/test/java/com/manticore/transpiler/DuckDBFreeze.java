package com.manticore.transpiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Disabled
public class DuckDBFreeze {

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  void testFreeze() throws SQLException {
    String sqlString =
        "WITH example AS\n" + "  (SELECT 'абвгд' AS characters, encode('абвгд') bytes)\n"
            + "SELECT\n" + "  characters,\n" + "  bit_length(characters) AS string_example,\n"
            + "  bytes,\n" + "  bit_length(bytes) AS bytes_example\n" + "FROM example;\n";

    try (Connection connDuck = DriverManager.getConnection("jdbc:duckdb:");
        Statement st = connDuck.createStatement();
        ResultSet rs = st.executeQuery(sqlString);) {
      Assertions.assertTrue(rs.next());
    }
  }

  @Test
  void testSalaaU() {
    final String regex = "ู";
    final String string = "ศูพรรณี";

    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(string);

    Assertions.assertTrue(matcher.find());
  }
}
