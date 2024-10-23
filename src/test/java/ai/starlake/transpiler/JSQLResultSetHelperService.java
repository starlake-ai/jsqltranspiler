/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI <hayssam.saleh@starlake.ai>
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

import com.opencsv.ResultSetHelperService;
import org.apache.commons.text.TextStringBuilder;

import java.io.IOException;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TreeMap;

public class JSQLResultSetHelperService extends ResultSetHelperService {
  private static final String DEFAULT_VALUE = "";

  public TreeMap<Integer, NumberFormat> numberFormatters = null;

  @Override
  public String[] getColumnValues(ResultSet rs, boolean trim, String dateFormatString,
      String timeFormatString) throws SQLException, IOException {
    ResultSetMetaData metadata = rs.getMetaData();

    if (numberFormatters == null) {
      numberFormatters = new TreeMap<>();

      for (int i = 1; i <= metadata.getColumnCount(); i++) {
        int colType = metadata.getColumnType(i);
        if (colType == Types.BIGINT || colType == Types.DECIMAL || colType == Types.NUMERIC) {
          int scale = metadata.getScale(i);

          DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
          df.setParseBigDecimal(true);
          df.setMinimumFractionDigits(scale - 1);
          df.setMaximumFractionDigits(scale);
          df.setMinimumIntegerDigits(1);
          df.setGroupingUsed(false);

          numberFormatters.put(i, df);
        }
      }
    }

    String[] valueArray = new String[metadata.getColumnCount()];
    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      valueArray[i - 1] = getColumnValue(rs, metadata.getColumnType(i), i, trim, dateFormatString,
          timeFormatString);
    }
    return valueArray;
  }

  private String applyFormatter(NumberFormat formatter, Number value) {
    if (formatter != null && value != null) {
      return formatter.format(value);
    }
    return Objects.toString(value, DEFAULT_VALUE);
  }

  /**
   * retrieves the data from an VarChar in a result set
   *
   * @param rs - result set
   * @param colIndex - column location of the data in the result set
   * @param trim - should the value be trimmed before being returned
   * @return a string representing the VarChar from the result set
   * @throws SQLException
   */
  protected String handleVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
    String value;
    String columnValue = rs.getString(colIndex);
    if (trim && columnValue != null) {
      value = columnValue.trim();
    } else {
      value = columnValue;
    }
    return value;
  }

  /**
   * retrieves the data from an NVarChar in a result set
   *
   * @param rs - result set
   * @param colIndex - column location of the data in the result set
   * @param trim - should the value be trimmed before being returned
   * @return a string representing the NVarChar from the result set
   * @throws SQLException
   */
  protected String handleNVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
    String value;
    String nColumnValue = rs.getNString(colIndex);
    if (trim && nColumnValue != null) {
      value = nColumnValue.trim();
    } else {
      value = nColumnValue;
    }
    return value;
  }

  /**
   * retrieves a date from a result set
   *
   * @param rs - result set
   * @param colIndex - column location of the data in the result set
   * @param dateFormatString - desired format of the date
   * @return - a string representing the data from the result set in the format set in
   *         dateFomratString.
   * @throws SQLException
   */
  protected String handleDate(ResultSet rs, int colIndex, String dateFormatString)
      throws SQLException {
    String value = DEFAULT_VALUE;
    Date date = rs.getDate(colIndex);
    if (date != null) {
      SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
      value = df.format(date);
    }
    return value;
  }

  /**
   * retrieves the data out of a CLOB
   *
   * @param rs - result set
   * @param colIndex - column location of the data in the result set
   * @return the data in the Clob as a string.
   * @throws SQLException
   * @throws IOException
   */
  protected String handleClob(ResultSet rs, int colIndex) throws SQLException, IOException {
    String value = DEFAULT_VALUE;
    Clob c = rs.getClob(colIndex);
    if (c != null) {
      TextStringBuilder sb = new TextStringBuilder();
      sb.readFrom(c.getCharacterStream());
      value = sb.toString();
    }
    return value;
  }

  /**
   * retrieves the data out of a NCLOB
   *
   * @param rs - result set
   * @param colIndex - column location of the data in the result set
   * @return the data in the NCLOB as a string.
   * @throws SQLException
   * @throws IOException
   */
  protected String handleNClob(ResultSet rs, int colIndex) throws SQLException, IOException {
    String value = DEFAULT_VALUE;
    NClob nc = rs.getNClob(colIndex);
    if (nc != null) {
      TextStringBuilder sb = new TextStringBuilder();
      sb.readFrom(nc.getCharacterStream());
      value = sb.toString();
    }
    return value;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity"})
  private String getColumnValue(ResultSet rs, int colType, int colIndex, boolean trim,
      String dateFormatString, String timestampFormatString) throws SQLException, IOException {

    String value;

    switch (colType) {
      case Types.BOOLEAN:
        value = Objects.toString(rs.getBoolean(colIndex));
        break;
      case Types.NCLOB:
        value = handleNClob(rs, colIndex);
        break;
      case Types.CLOB:
        value = handleClob(rs, colIndex);
        break;
      case Types.BIGINT:
      case Types.DECIMAL:
      case Types.NUMERIC:
        value = applyFormatter(numberFormatters.get(colIndex), rs.getBigDecimal(colIndex));
        break;
      case Types.REAL:
      case Types.DOUBLE:
        value = applyFormatter(floatingPointFormat, rs.getDouble(colIndex));
        break;
      case Types.FLOAT:
        value = applyFormatter(floatingPointFormat, rs.getFloat(colIndex));
        break;
      case Types.INTEGER:
      case Types.TINYINT:
      case Types.SMALLINT:
        value = applyFormatter(integerFormat, rs.getInt(colIndex));
        break;
      case Types.DATE:
        value = handleDate(rs, colIndex, dateFormatString);
        break;
      case Types.TIME:
        value = Objects.toString(rs.getTime(colIndex), DEFAULT_VALUE);
        break;
      case Types.TIMESTAMP:
        value = handleTimestamp(rs.getTimestamp(colIndex), timestampFormatString);
        break;
      case Types.NVARCHAR:
      case Types.NCHAR:
      case Types.LONGNVARCHAR:
        value = handleNVarChar(rs, colIndex, trim);
        break;
      case Types.LONGVARCHAR:
      case Types.VARCHAR:
      case Types.CHAR:
        value = handleVarChar(rs, colIndex, trim);
        break;
      default:
        // This takes care of Types.BIT, Types.JAVA_OBJECT, and anything
        // unknown.
        value = Objects.toString(rs.getObject(colIndex), DEFAULT_VALUE);
    }


    if (rs.wasNull() || value == null) {
      value = DEFAULT_VALUE;
    }

    return value;
  }
}
