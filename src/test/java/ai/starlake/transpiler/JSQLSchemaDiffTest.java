package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

class JSQLSchemaDiffTest {
  @Test
  void getDiff() {
    //@formatter:off
    DBSchema schema = new DBSchema(
            null
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
            new Attribute("starbake.orders.order_id", String.class)
            , new Attribute("starbake.orders.order_date", String.class)
            , new Attribute("total_revenue", Float.class, AttributeStatus.ADDED)
            , new Attribute("starbake.orders.id", Long.class, AttributeStatus.REMOVED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(schema);
    List<Attribute> actual = diff.getDiff(sqlStr, "starbake.orders");

    // Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }
}
