package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import net.sf.jsqlparser.JSQLParserException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

class JSQLSchemaDiffTest {
  @Test
  void getDiffSingleSchema() throws JSQLParserException, SQLException {
    //@formatter:off
    DBSchema schema = new DBSchema(
            ""
            , "starbake"
            , "orders"
            , new Attribute("id", Long.class)
            , new Attribute("order_id", String.class)
            , new Attribute("order_date", Timestamp.class)
    );
    schema.put(
            "order_lines"
            , new Attribute("id", Long.class)
            , new Attribute("quantity", Long.class)
            , new Attribute("sale_price", Float.class)
    );

    String sqlStr =
            "SELECT  o.order_id\n"
            + "        , o.order_date\n"
            + "        , Sum( ol.quantity * ol.sale_price ) AS total_revenue\n"
            + "FROM starbake.orders o\n"
            + "    INNER JOIN starbake.order_lines ol\n"
            + "            USING ( id )\n"
            + "GROUP BY    o.order_id\n"
            + "            , o.order_date\n"
            + ";";

    List<Attribute> expected = List.of(
            new Attribute("order_id", String.class)
            , new Attribute("order_date", Timestamp.class)
            , new Attribute("total_revenue", "Other", AttributeStatus.ADDED)
            , new Attribute("id", Long.class, AttributeStatus.REMOVED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(schema);
    List<Attribute> actual = diff.getDiff(sqlStr, "starbake.orders");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void getDiffMultiSchema() throws JSQLParserException, SQLException {
    //@formatter:off
    DBSchema schema1 = new DBSchema(
            ""
            , "starbake"
            , "orders"
            , new Attribute("id", Long.class)
            , new Attribute("order_id", String.class)
            , new Attribute("order_date", Timestamp.class)
    );
    DBSchema schema2 = new DBSchema(
            ""
            , "starlord"
            , "order_lines"
            , new Attribute("id", Long.class)
            , new Attribute("quantity", Long.class)
            , new Attribute("sale_price", Float.class)
    );

    String sqlStr =
            "SELECT  o.order_id\n"
            + "        , o.order_date\n"
            + "        , Sum( ol.quantity * ol.sale_price ) AS total_revenue\n"
            + "FROM starbake.orders o\n"
            + "    INNER JOIN starlord.order_lines ol\n"
            + "            USING ( id )\n"
            + "GROUP BY    o.order_id\n"
            + "            , o.order_date\n"
            + ";";

    List<Attribute> expected = List.of(
            new Attribute("order_id", String.class)
            , new Attribute("order_date", Timestamp.class)
            , new Attribute("total_revenue", "Other", AttributeStatus.ADDED)
            , new Attribute("id", Long.class, AttributeStatus.REMOVED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(schema1, schema2);
    List<Attribute> actual = diff.getDiff(sqlStr, "starbake.orders");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void getDiffMultiSchema2() throws JSQLParserException, SQLException {
    //@formatter:off
    DBSchema schema1 = new DBSchema(
            ""
            , "starbake"
            , "orders"
            , new Attribute("order_id", Long.class)
            , new Attribute("timestamp", Timestamp.class)
    );
    DBSchema schema2 = new DBSchema(
            ""
            , "starbake"
            , "order_lines"
            , new Attribute("order_id", Long.class)
            , new Attribute("quantity", Long.class)
            , new Attribute("sale_price", Float.class)
    );
    DBSchema schema3 = new DBSchema(
            ""
            , "kpi"
            , "revenue_summary"
            , new Attribute("order_id", Long.class)
            , new Attribute("order_date", Timestamp.class)
            , new Attribute("total_revenue", Double.class)
    );

    String sqlStr =
            "SELECT  o.order_id\n"
            + "        , o.timestamp AS order_date\n"
            + "        , Sum( ol.quantity * ol.sale_price ) AS total_revenue\n"
            + "FROM starbake.orders o\n"
            + "    JOIN starbake.order_lines ol\n"
            + "        ON o.order_id = ol.order_id\n"
            + "GROUP BY    o.order_id\n"
            + "            , o.timestamp\n"
            + ";";

    List<Attribute> expected = List.of(
            new Attribute("order_id", Long.class)
            , new Attribute("order_date", Timestamp.class)
            , new Attribute("total_revenue", "Other", AttributeStatus.ADDED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(schema1, schema2, schema3);
    List<Attribute> actual = diff.getDiff(sqlStr, "kpi.revenue_summary");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }
}
