package ai.starlake.transpiler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

/**
 * Standalone Java class for deserializing GEOMETRY BLOBs into Well-Known Text (WKT) format.
 * Supports DuckDB SPATIAL extension format and common WKB formats.
 *
 * Standalone Java class for deserializing GEOMETRY BLOBs into Well-Known Text (WKT) format.
 * Supports common geometry types: POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING,
 * MULTIPOLYGON.
 *
 * Handles both Well-Known Binary (WKB) and database-specific binary formats.
 */
public class GeometryBlobDeserializerTest {

  // WKB Geometry Types
  private static final int WKB_POINT = 1;
  private static final int WKB_LINESTRING = 2;
  private static final int WKB_POLYGON = 3;
  private static final int WKB_MULTIPOINT = 4;
  private static final int WKB_MULTILINESTRING = 5;
  private static final int WKB_MULTIPOLYGON = 6;
  private static final int WKB_GEOMETRYCOLLECTION = 7;

  // Byte order constants
  private static final byte WKB_XDR = 0; // Big Endian
  private static final byte WKB_NDR = 1; // Little Endian

  /**
   * Main method to deserialize a GEOMETRY BLOB to WKT string
   *
   * @param blob The GEOMETRY BLOB from JDBC
   * @return WKT representation of the geometry
   * @throws SQLException if blob cannot be read
   * @throws IllegalArgumentException if geometry format is not supported
   */
  public static String deserializeToWKT(Blob blob) throws SQLException {
    if (blob == null) {
      return null;
    }

    byte[] binaryData = blob.getBytes(1, (int) blob.length());
    return deserializeToWKT(binaryData);
  }

  /**
   * Deserialize byte array to WKT string
   *
   * @param binaryData The binary geometry data
   * @return WKT representation of the geometry
   * @throws IllegalArgumentException if geometry format is not supported
   */
  public static String deserializeToWKT(byte[] binaryData) {
    if (binaryData == null || binaryData.length == 0) {
      return null;
    }

    ByteBuffer buffer = ByteBuffer.wrap(binaryData);

    // Check if this is DuckDB SPATIAL format first
    if (isDuckDBSpatialFormat(binaryData)) {
      return parseDuckDBSpatial(buffer);
    }

    // Check if this is WKB format (starts with byte order marker)
    if (binaryData[0] == WKB_XDR || binaryData[0] == WKB_NDR) {
      return parseWKB(buffer);
    }

    // Try to parse as database-specific format
    return parseProprietaryFormat(buffer);
  }

  /**
   * Check if binary data uses DuckDB SPATIAL format
   */
  private static boolean isDuckDBSpatialFormat(byte[] data) {
    if (data.length < 8)
      return false;

    // DuckDB SPATIAL format starts with:
    // [geometry_type:1 byte][flags:1 byte][unused:2 bytes][padding:4 bytes]

    // First byte should be geometry type (0-6)
    int geometryType = data[0] & 0xFF;
    if (geometryType <= 6) {
      return true;
    }

    // Fallback: if no clear WKB markers, assume DuckDB
    boolean hasWKBMarker = false;
    for (int i = 0; i < Math.min(data.length, 20); i++) {
      if (data[i] == WKB_XDR || data[i] == WKB_NDR) {
        hasWKBMarker = true;
        break;
      }
    }

    return !hasWKBMarker;
  }

  /**
   * Parse DuckDB SPATIAL format Based on DBeaver's DuckDBGeometryConverter implementation
   */
  private static String parseDuckDBSpatial(ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.position(0);

    // DuckDB SPATIAL format header:
    // [geometry_type:1 byte][flags:1 byte][unused:2 bytes][padding:4 bytes]

    int geometryType = buffer.get() & 0xFF; // unsigned byte
    int flags = buffer.get() & 0xFF; // unsigned byte
    buffer.getShort(); // unused 2 bytes
    buffer.getInt(); // padding 4 bytes

    boolean hasZ = (flags & 0x01) != 0;
    boolean hasM = (flags & 0x02) != 0;
    boolean hasBBox = (flags & 0x04) != 0;
    int dimensions = 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);

    // Skip bounding box if present (dimensions * float size * 2 for min/max)
    if (hasBBox) {
      buffer.position(buffer.position() + dimensions * Float.BYTES * 2);
    }

    return deserializeRecursive(buffer, hasZ, hasM);
  }

  /**
   * Deserialize geometry recursively - matches DBeaver implementation exactly
   */
  private static String deserializeRecursive(ByteBuffer buffer, boolean hasZ, boolean hasM) {
    int type = buffer.getInt();
    int count = buffer.getInt();

    switch (type) {
      case 0: // POINT
        return readPoint(buffer, count, hasZ, hasM);

      case 1: // LINESTRING
        return readLineString(buffer, count, hasZ, hasM);

      case 2: // POLYGON
        return readPolygon(buffer, count, hasZ, hasM);

      case 3: // MULTI_POINT
        StringBuilder mpSb = new StringBuilder("MULTIPOINT (");
        for (int i = 0; i < count; i++) {
          if (i > 0)
            mpSb.append(", ");
          String point = deserializeRecursive(buffer, hasZ, hasM);
          // Extract coordinates from "POINT (x y)" format
          String coords = point.substring(point.indexOf('(') + 1, point.indexOf(')'));
          mpSb.append("(").append(coords).append(")");
        }
        mpSb.append(")");
        return mpSb.toString();

      case 4: // MULTI_LINESTRING
        StringBuilder mlsSb = new StringBuilder("MULTILINESTRING (");
        for (int i = 0; i < count; i++) {
          if (i > 0)
            mlsSb.append(", ");
          String lineString = deserializeRecursive(buffer, hasZ, hasM);
          // Extract coordinates from "LINESTRING (coords)" format
          String coords =
              lineString.substring(lineString.indexOf('(') + 1, lineString.indexOf(')'));
          mlsSb.append("(").append(coords).append(")");
        }
        mlsSb.append(")");
        return mlsSb.toString();

      case 5: // MULTI_POLYGON
        StringBuilder mpolySb = new StringBuilder("MULTIPOLYGON (");
        for (int i = 0; i < count; i++) {
          if (i > 0)
            mpolySb.append(", ");
          String polygon = deserializeRecursive(buffer, hasZ, hasM);
          // Extract coordinates from "POLYGON (coords)" format
          String coords = polygon.substring(polygon.indexOf('(') + 1, polygon.lastIndexOf(')'));
          mpolySb.append("(").append(coords).append(")");
        }
        mpolySb.append(")");
        return mpolySb.toString();

      case 6: // MULTI_GEOMETRY / GEOMETRYCOLLECTION
        StringBuilder gcSb = new StringBuilder("GEOMETRYCOLLECTION (");
        for (int i = 0; i < count; i++) {
          if (i > 0)
            gcSb.append(", ");
          gcSb.append(deserializeRecursive(buffer, hasZ, hasM));
        }
        gcSb.append(")");
        return gcSb.toString();

      default:
        throw new IllegalArgumentException("Unknown DuckDB geometry type: " + type);
    }
  }

  private static String readPoint(ByteBuffer buffer, int count, boolean hasZ, boolean hasM) {
    // Point should have exactly 1 coordinate
    if (count != 1) {
      throw new IllegalArgumentException("Point should have exactly 1 coordinate, got: " + count);
    }

    double x = buffer.getDouble();
    double y = buffer.getDouble();
    if (hasZ)
      buffer.getDouble(); // skip Z
    if (hasM)
      buffer.getDouble(); // skip M

    return String.format("POINT (%s %s)", formatCoordinate(x), formatCoordinate(y));
  }

  private static String readLineString(ByteBuffer buffer, int count, boolean hasZ, boolean hasM) {
    StringBuilder sb = new StringBuilder("LINESTRING (");

    for (int i = 0; i < count; i++) {
      if (i > 0)
        sb.append(", ");

      double x = buffer.getDouble();
      double y = buffer.getDouble();
      if (hasZ)
        buffer.getDouble(); // skip Z
      if (hasM)
        buffer.getDouble(); // skip M

      sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
    }

    sb.append(")");
    return sb.toString();
  }

  private static String readPolygon(ByteBuffer buffer, int ringCount, boolean hasZ, boolean hasM) {
    StringBuilder sb = new StringBuilder("POLYGON (");

    // Read ring count with padding (like DBeaver implementation)
    int paddedRingCount = ringCount + (ringCount % 2 == 1 ? 1 : 0);

    // Read ring sizes
    int[] ringSizes = new int[paddedRingCount];
    for (int i = 0; i < paddedRingCount; i++) {
      ringSizes[i] = buffer.getInt();
    }

    // Read rings
    for (int ring = 0; ring < ringCount; ring++) {
      if (ring > 0)
        sb.append(", ");
      sb.append("(");

      int ringSize = ringSizes[ring];
      for (int i = 0; i < ringSize; i++) {
        if (i > 0)
          sb.append(", ");

        double x = buffer.getDouble();
        double y = buffer.getDouble();
        if (hasZ)
          buffer.getDouble(); // skip Z
        if (hasM)
          buffer.getDouble(); // skip M

        sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
      }

      sb.append(")");
    }

    sb.append(")");
    return sb.toString();
  }

  /**
   * Parse Well-Known Binary (WKB) format - fallback for standard WKB
   */
  private static String parseWKB(ByteBuffer buffer) {
    // Read byte order
    byte byteOrder = buffer.get();
    if (byteOrder == WKB_XDR) {
      buffer.order(ByteOrder.BIG_ENDIAN);
    } else if (byteOrder == WKB_NDR) {
      buffer.order(ByteOrder.LITTLE_ENDIAN);
    } else {
      throw new IllegalArgumentException("Invalid WKB byte order: " + byteOrder);
    }

    // Read geometry type
    int geometryType = buffer.getInt();

    // Handle extended geometry types (with Z, M, or ZM dimensions)
    int baseType = geometryType & 0xFF; // Get lower 8 bits
    if (baseType == 0) {
      baseType = (geometryType >> 8) & 0xFF;
    }
    if (baseType == 0) {
      baseType = geometryType & 0x1F; // Lower 5 bits
    }

    switch (baseType) {
      case 0:
        return "GEOMETRYCOLLECTION EMPTY";
      case WKB_POINT:
        return parseWKBPoint(buffer, geometryType);
      case WKB_LINESTRING:
        return parseWKBLineString(buffer);
      case WKB_POLYGON:
        return parseWKBPolygon(buffer);
      case WKB_MULTIPOINT:
        return parseWKBMultiPoint(buffer);
      case WKB_MULTILINESTRING:
        return parseWKBMultiLineString(buffer);
      case WKB_MULTIPOLYGON:
        return parseWKBMultiPolygon(buffer);
      default:
        throw new IllegalArgumentException(
            "Unsupported geometry type: " + geometryType + " (base type: " + baseType + ")");
    }
  }

  private static String parseWKBPoint(ByteBuffer buffer, int geometryType) {
    // Check if this is an extended geometry type (3D, measured, etc.)
    boolean hasZ =
        (geometryType & 0x80000000) != 0 || (geometryType >= 1000 && geometryType < 2000);
    boolean hasM =
        (geometryType & 0x40000000) != 0 || (geometryType >= 2000 && geometryType < 3000);

    double x = buffer.getDouble();
    double y = buffer.getDouble();

    // Skip Z coordinate if present
    if (hasZ) {
      buffer.getDouble(); // Z coordinate - skip for now
    }

    // Skip M coordinate if present
    if (hasM) {
      buffer.getDouble(); // M coordinate - skip for now
    }

    return String.format("POINT (%s %s)", formatCoordinate(x), formatCoordinate(y));
  }

  private static String parseWKBLineString(ByteBuffer buffer) {
    int numPoints = buffer.getInt();
    StringBuilder sb = new StringBuilder("LINESTRING (");

    for (int i = 0; i < numPoints; i++) {
      if (i > 0)
        sb.append(", ");
      double x = buffer.getDouble();
      double y = buffer.getDouble();
      sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
    }

    sb.append(")");
    return sb.toString();
  }

  private static String parseWKBPolygon(ByteBuffer buffer) {
    int numRings = buffer.getInt();
    StringBuilder sb = new StringBuilder("POLYGON (");

    for (int ring = 0; ring < numRings; ring++) {
      if (ring > 0)
        sb.append(", ");
      sb.append("(");

      int numPoints = buffer.getInt();
      for (int i = 0; i < numPoints; i++) {
        if (i > 0)
          sb.append(", ");
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
      }

      sb.append(")");
    }

    sb.append(")");
    return sb.toString();
  }

  private static String parseWKBMultiPoint(ByteBuffer buffer) {
    int numPoints = buffer.getInt();
    StringBuilder sb = new StringBuilder("MULTIPOINT (");

    for (int i = 0; i < numPoints; i++) {
      if (i > 0)
        sb.append(", ");

      // Skip byte order and geometry type for each point
      buffer.get(); // byte order
      buffer.getInt(); // geometry type

      double x = buffer.getDouble();
      double y = buffer.getDouble();
      sb.append("(").append(formatCoordinate(x)).append(" ").append(formatCoordinate(y))
          .append(")");
    }

    sb.append(")");
    return sb.toString();
  }

  private static String parseWKBMultiLineString(ByteBuffer buffer) {
    int numLineStrings = buffer.getInt();
    StringBuilder sb = new StringBuilder("MULTILINESTRING (");

    for (int i = 0; i < numLineStrings; i++) {
      if (i > 0)
        sb.append(", ");

      // Skip byte order and geometry type for each linestring
      buffer.get(); // byte order
      buffer.getInt(); // geometry type

      sb.append("(");
      int numPoints = buffer.getInt();
      for (int j = 0; j < numPoints; j++) {
        if (j > 0)
          sb.append(", ");
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
      }
      sb.append(")");
    }

    sb.append(")");
    return sb.toString();
  }

  private static String parseWKBMultiPolygon(ByteBuffer buffer) {
    int numPolygons = buffer.getInt();
    StringBuilder sb = new StringBuilder("MULTIPOLYGON (");

    for (int i = 0; i < numPolygons; i++) {
      if (i > 0)
        sb.append(", ");

      // Skip byte order and geometry type for each polygon
      buffer.get(); // byte order
      buffer.getInt(); // geometry type

      sb.append("(");
      int numRings = buffer.getInt();
      for (int ring = 0; ring < numRings; ring++) {
        if (ring > 0)
          sb.append(", ");
        sb.append("(");

        int numPoints = buffer.getInt();
        for (int j = 0; j < numPoints; j++) {
          if (j > 0)
            sb.append(", ");
          double x = buffer.getDouble();
          double y = buffer.getDouble();
          sb.append(formatCoordinate(x)).append(" ").append(formatCoordinate(y));
        }

        sb.append(")");
      }
      sb.append(")");
    }

    sb.append(")");
    return sb.toString();
  }

  /**
   * Parse database-specific proprietary format
   */
  private static String parseProprietaryFormat(ByteBuffer buffer) {
    // Try Oracle SDO_GEOMETRY format
    String result = tryOracleFormat(buffer);
    if (result != null)
      return result;

    // Try PostGIS extended format
    result = tryPostGISFormat(buffer);
    if (result != null)
      return result;

    // Try SQL Server format
    result = trySQLServerFormat(buffer);
    if (result != null)
      return result;

    // Generic approach: scan for WKB markers
    return scanForWKBData(buffer);
  }

  private static String tryOracleFormat(ByteBuffer buffer) {
    buffer.position(0);
    if (buffer.remaining() >= 32) {
      buffer.position(32);
      if (buffer.remaining() >= 5) {
        byte b = buffer.get(buffer.position());
        if (b == WKB_XDR || b == WKB_NDR) {
          try {
            return parseWKB(buffer);
          } catch (Exception e) {
            // Continue with other attempts
          }
        }
      }
    }
    return null;
  }

  private static String tryPostGISFormat(ByteBuffer buffer) {
    buffer.position(0);
    if (buffer.remaining() >= 9) {
      buffer.position(4);
      byte b = buffer.get(buffer.position());
      if (b == WKB_XDR || b == WKB_NDR) {
        try {
          return parseWKB(buffer);
        } catch (Exception e) {
          // Continue
        }
      }
    }
    return null;
  }

  private static String trySQLServerFormat(ByteBuffer buffer) {
    buffer.position(0);
    int[] offsets = {0, 6, 8, 16, 20, 24};

    for (int offset : offsets) {
      if (buffer.remaining() > offset + 5) {
        buffer.position(offset);
        byte b = buffer.get(buffer.position());
        if (b == WKB_XDR || b == WKB_NDR) {
          try {
            return parseWKB(buffer);
          } catch (Exception e) {
            // Continue
          }
        }
      }
    }
    return null;
  }

  private static String scanForWKBData(ByteBuffer buffer) {
    buffer.position(0);

    for (int i = 0; i < Math.min(buffer.remaining() - 5, 50); i++) {
      buffer.position(i);
      if (buffer.remaining() < 5)
        break;

      byte b = buffer.get();
      if (b == WKB_XDR || b == WKB_NDR) {
        buffer.position(i);
        try {
          return parseWKB(buffer);
        } catch (Exception e) {
          // Continue searching
        }
      }
    }

    throw new IllegalArgumentException("Unable to parse geometry format. Data length: "
        + buffer.capacity() + " bytes. First 20 bytes: " + bytesToHex(buffer.array(), 20));
  }

  /**
   * Check if a coordinate value seems reasonable
   */
  private static boolean isReasonableCoordinate(double coord) {
    return !Double.isNaN(coord) && !Double.isInfinite(coord) && Math.abs(coord) < 1e6
        && Math.abs(coord) > 1e-50; // Tighter bounds
  }

  /**
   * Format coordinate value, removing unnecessary decimal places and handling floating-point
   * precision
   */
  private static String formatCoordinate(double coord) {
    // Handle floating-point precision issues by rounding to reasonable precision
    double rounded = Math.round(coord * 1e10) / 1e10; // Round to 10 decimal places

    // Check if it's very close to an integer
    if (Math.abs(rounded - Math.round(rounded)) < 1e-10) {
      return String.valueOf(Math.round(rounded));
    }

    // Format with minimal decimal places, removing trailing zeros
    String formatted = String.format("%.10f", rounded);

    // Remove trailing zeros
    if (formatted.contains(".")) {
      formatted = formatted.replaceAll("0+$", "");
      formatted = formatted.replaceAll("\\.$", "");
    }

    return formatted;
  }

  /**
   * Helper method to convert bytes to hex string for debugging
   */
  private static String bytesToHex(byte[] bytes, int maxLength) {
    StringBuilder sb = new StringBuilder();
    int length = Math.min(bytes.length, maxLength);
    for (int i = 0; i < length; i++) {
      sb.append(String.format("%02X ", bytes[i]));
    }
    return sb.toString();
  }

  /**
   * Add debugging method for DuckDB format analysis
   */
  public static void debugDuckDBFormat(Blob blob) throws SQLException {
    System.out.println("=== DuckDB SPATIAL Format Analysis ===");

    if (blob == null) {
      System.out.println("Blob is null");
      return;
    }

    byte[] data = blob.getBytes(1, (int) blob.length());
    debugDuckDBFormat(data);
  }

  public static void debugDuckDBFormat(byte[] data) {
    if (data == null || data.length == 0) {
      System.out.println("Data is null or empty");
      return;
    }

    System.out.println("=== DuckDB SPATIAL Format Analysis ===");
    System.out.println("Length: " + data.length + " bytes");
    System.out.println("Raw hex: " + bytesToHex(data, Math.min(data.length, 60)));

    if (data.length >= 8) {
      ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

      // Parse DuckDB header
      int geometryType = buffer.get() & 0xFF;
      int flags = buffer.get() & 0xFF;
      short unused = buffer.getShort();
      int padding = buffer.getInt();

      System.out.println("\nDuckDB Header:");
      System.out.println("  Geometry Type: " + geometryType + " ("
          + getDuckDBGeometryTypeName(geometryType) + ")");
      System.out.println("  Flags: 0x" + Integer.toHexString(flags));

      boolean hasZ = (flags & 0x01) != 0;
      boolean hasM = (flags & 0x02) != 0;
      boolean hasBBox = (flags & 0x04) != 0;

      System.out.println("    Has Z: " + hasZ);
      System.out.println("    Has M: " + hasM);
      System.out.println("    Has BBox: " + hasBBox);
      System.out.println("  Unused: 0x" + Integer.toHexString(unused));
      System.out.println("  Padding: 0x" + Integer.toHexString(padding));

      int dimensions = 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);

      if (hasBBox) {
        System.out.println("  Skipping BBox (" + (dimensions * 4 * 2) + " bytes)");
        buffer.position(buffer.position() + dimensions * 4 * 2);
      }

      // Show next few values
      System.out.println("\nGeometry Data at offset " + buffer.position() + ":");
      if (buffer.remaining() >= 8) {
        int nextType = buffer.getInt();
        int count = buffer.getInt();
        System.out.println("  Next Type: " + nextType);
        System.out.println("  Count: " + count);

        if (buffer.remaining() >= 16) {
          double x = buffer.getDouble();
          double y = buffer.getDouble();
          System.out.println("  First coordinate: X=" + x + ", Y=" + y);
        }
      }
    }
  }

  private static String getDuckDBGeometryTypeName(int type) {
    switch (type) {
      case 0:
        return "POINT";
      case 1:
        return "LINESTRING";
      case 2:
        return "POLYGON";
      case 3:
        return "MULTI_POINT";
      case 4:
        return "MULTI_LINESTRING";
      case 5:
        return "MULTI_POLYGON";
      case 6:
        return "MULTI_GEOMETRY";
      default:
        return "UNKNOWN";
    }
  }

  private static Connection connection;

  @BeforeAll
  static void setUp() throws SQLException {
    // Set up DuckDB connection and spatial extension
    connection = DriverManager.getConnection("jdbc:duckdb:");
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("INSTALL spatial;");
      stmt.executeUpdate("LOAD spatial;");
    }
  }

  /**
   * Test data structure for queries with multiple columns
   */
  public static class GeometryTestCase {
    public final String sql;
    public final String[] expectedWkt;

    public GeometryTestCase(String sql, String... expectedWkt) {
      this.sql = sql;
      this.expectedWkt = expectedWkt;
    }
  }

  /**
   * Provides test data: SQL query, geometry column name, and expected WKT result
   */
  static Stream<Arguments> geometryTestCases() {
    //@formatter:off
    return Stream.of(
            // Points - single column (backwards compatibility)
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromGeoJSON('{\"type\": \"Point\", \"coordinates\": [30.0, 10.0]}') as geom",
                    "POINT (30 10)")),
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('POINT(1 2)') as geom",
                    "POINT (1 2)")),
            Arguments.of(new GeometryTestCase(
                    "select ST_Point(5.5, -3.2) as geom",
                    "POINT (5.5 -3.2)")),

            // LineStrings
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('LINESTRING(0 0, 1 1, 2 2)') as geom",
                    "LINESTRING (0 0, 1 1, 2 2)")),
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromGeoJSON('{\"type\": \"LineString\", \"coordinates\": [[0,0], [1,1], [2,2]]}') as geom",
                    "LINESTRING (0 0, 1 1, 2 2)")),

            // Polygons
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))') as geom",
                    "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))")),
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromGeoJSON('{\"type\": \"Polygon\", \"coordinates\": [[[0,0], [0,1], [1,1], [1,0], [0,0]]]}') as geom",
                    "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))")),

            // MultiPoint
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('MULTIPOINT((1 2), (3 4))') as geom",
                    "MULTIPOINT ((1 2), (3 4))")),

            // MultiLineString
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('MULTILINESTRING((0 0, 1 1), (2 2, 3 3))') as geom",
                    "MULTILINESTRING ((0 0, 1 1), (2 2, 3 3))")),

            // MultiPolygon
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)), ((2 2, 2 3, 3 3, 3 2, 2 2)))') as geom",
                    "MULTIPOLYGON (((0 0, 0 1, 1 1, 1 0, 0 0)), ((2 2, 2 3, 3 3, 3 2, 2 2)))")),

            // Edge cases
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('POINT(0 0)') as geom",
                    "POINT (0 0)")),
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('POINT(-180 -90)') as geom",
                    "POINT (-180 -90)")),
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('POINT(180 90)') as geom",
                    "POINT (180 90)")),

            // Multiple columns example - specify which column contains the geometry
            Arguments.of(new GeometryTestCase(
                    "WITH wkb_data AS (" +
                    "  SELECT '010200000002000000feffffffffffef3f000000000000f03f01000000000008400000000000000040' geo" +
                    ") " +
                    "SELECT " +
                    "  If(Regexp_Matches(geo, '^[0-9A-Fa-f]+$'), St_Geomfromhexewkb(geo), St_Geomfromwkb(geo::BLOB)) AS from_planar, " +
                    "  If(Regexp_Matches(geo, '^[0-9A-Fa-f]+$'), St_Geomfromhexewkb(geo), St_Geomfromwkb(geo::BLOB)) AS from_geodesic " +
                    "FROM wkb_data",
                    "LINESTRING (1 1, 3 2)", "LINESTRING (1 1, 3 2)")),

            // Another multi-column example
            Arguments.of(new GeometryTestCase(
                    "SELECT ST_GeomFromText('POINT(10 20)') as geometry"
                    , "POINT (10 20)")),
            // GEOMETRYCOLLECTION tests
            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('GEOMETRYCOLLECTION(POINT(1 2), LINESTRING(0 0, 1 1))') as geom",
                    "GEOMETRYCOLLECTION (POINT (1 2), LINESTRING (0 0, 1 1))")),

            Arguments.of(new GeometryTestCase(
                    "select ST_GeomFromText('GEOMETRYCOLLECTION(POINT(10 20), POLYGON((0 0, 0 1, 1 1, 1 0, 0 0)), MULTIPOINT((5 5), (6 6)))') as geom",
                    "GEOMETRYCOLLECTION (POINT (10 20), POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0)), MULTIPOINT ((5 5), (6 6)))")),

            Arguments.of(new GeometryTestCase(
                    "WITH wkb_data AS (\n"
                    + "        SELECT '010200000002000000feffffffffffef3f000000000000f03f01000000000008400000000000000040' geo  )\n"
                    + "SELECT  /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/ If( Regexp_Matches( geo, '^[0-9A-Fa-f]+$' ), St_Geomfromhexewkb( geo ), St_Geomfromwkb( geo::BLOB ) ) AS from_planar\n"
                    + "        , /*Warning: ORIENTED, PLANAR, MAKE_VALID parameters unsupported.*/  If( Regexp_Matches( geo, '^[0-9A-Fa-f]+$' ), St_Geomfromhexewkb( geo ), St_Geomfromwkb( geo::BLOB ) ) AS from_geodesic\n"
                    + "FROM wkb_data\n"
                    + ";"
                    , "LINESTRING (1 1, 3 2)","LINESTRING (1 1, 3 2)"
            )),

            Arguments.of(new GeometryTestCase(
                    "WITH Geographies AS\n"
                    + " (SELECT ST_GEOMFROMTEXT('MULTIPOINT(2 11, 4 12, 0 15, 1 9, 1 12)') AS g)\n"
                    + "SELECT\n"
                    + "  g AS input_geography,\n"
                    + "  ST_CONVEXHULL(g) AS convex_hull\n"
                    + "FROM Geographies;"
                    , "MULTIPOINT (2 11, 4 12, 0 15, 1 9, 1 12)","POLYGON ((1 9, 0 15, 4 12, 1 9))"
            )),

            Arguments.of(new GeometryTestCase(
                    "SELECT\n"
                    + "  ST_DIFFERENCE(\n"
                    + "      ST_GEOMFROMTEXT('POLYGON((0 0, 10 0, 10 10, 0 0))'),\n"
                    + "      ST_GEOMFROMTEXT('POLYGON((4 2, 6 2, 8 6, 4 2))')\n"
                    + "  ) diff;"
                    , "POLYGON ((10 10, 10 0, 0 0, 10 10), (6 2, 8 6, 4 2, 6 2))"
            )),

            Arguments.of(new GeometryTestCase(
                    "WITH example AS (\n"
                    + "  SELECT ST_GEOMFROMTEXT('GEOMETRYCOLLECTION(POINT(0 0), LINESTRING(1 2, 2 1))') AS geography)\n"
                    + "SELECT\n"
                    + "  geography AS original_geography,\n"
                    + "  ST_DUMP(geography) AS dumped_geographies\n"
                    + "FROM example;"
                    , "GEOMETRYCOLLECTION (POINT (0 0), LINESTRING (1 2, 2 1))","[{geom=POINT (0 0), path=[1]}, {geom=LINESTRING (1 2, 2 1), path=[2]}]"
              )
            )
    );
    //@formatter:on
  }

  @ParameterizedTest(name = "Test {index}: {0}")
  @MethodSource("geometryTestCases")
  void testGeometryDeserialization(GeometryTestCase testCase) throws SQLException {
    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(testCase.sql)) {

      Assertions.assertThat(rs.next()).as("Query should return at least one row").isTrue();

      int c = 1;
      for (String s : testCase.expectedWkt) {
        Blob blob = (Blob) rs.getObject(c++);
        Assertions.assertThat(blob).as("Geometry BLOB should not be null").isNotNull();
        String actualWkt = GeometryBlobDeserializerTest.deserializeToWKT(blob);

        Assertions.assertThat(actualWkt).as("Deserialized WKT should match expected result")
            .isEqualToIgnoringCase(s);
      }
    }
  }

  /**
   * Alternative method for testing with custom test data
   */
  void testCustomGeometry(String sql, String expectedWkt) throws SQLException {
    testGeometryDeserialization(new GeometryTestCase(sql, expectedWkt));
  }

  /**
   * Alternative method for testing with custom test data and specific column
   */
  void testCustomGeometry(String sql, String geometryColumnName, String expectedWkt)
      throws SQLException {
    testGeometryDeserialization(new GeometryTestCase(sql, geometryColumnName, expectedWkt));
  }

  /**
   * Helper method for debugging failed test cases
   */
  void debugGeometry(String sql) throws SQLException {
    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        Blob blob = (Blob) rs.getObject(1);
        System.out.println("=== Debugging SQL: " + sql + " ===");
        debugDuckDBFormat(blob);

        try {
          String wkt = GeometryBlobDeserializerTest.deserializeToWKT(blob);
          System.out.println("Parsed WKT: " + wkt);
        } catch (Exception e) {
          System.err.println("Parsing failed: " + e.getMessage());
        }
      }
    }
  }
}
