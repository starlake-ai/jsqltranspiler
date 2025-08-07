package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.AttributeStatus;
import ai.starlake.transpiler.diff.DBSchema;
import net.sf.jsqlparser.JSQLParserException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
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
            new Attribute("order_id", "string")
            , new Attribute("order_date", "timestamp")
            , new Attribute("total_revenue", "double", AttributeStatus.ADDED)
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
            new Attribute("order_id", "string")
            , new Attribute("order_date", "timestamp")
            , new Attribute("total_revenue", "double", AttributeStatus.ADDED)
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
            , new Attribute("sale_price", Double.class)
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
            new Attribute("order_id", "long")
            , new Attribute("order_date", "timestamp")
            , new Attribute("total_revenue", "double", AttributeStatus.ADDED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(schema1, schema2, schema3);
    List<Attribute> actual = diff.getDiff(sqlStr, "kpi.revenue_summary");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  /*
  ---
  - schemaName: "starbake_analytics"
  tables:
    customer_purchase_history:
    - name: "customer_id"
      type: "long"
    - name: "purchase_date"
      type: "date"
  - schemaName: "starbake"
  tables:
    customers:
    - name: "id"
      type: "int"
    - name: "first_name"
      type: "string"
    - name: "last_name"
      type: "string"
    - name: "email"
      type: "string"
    - name: "join_date"
      type: "date"
    orders:
    - name: "customer_id"
      type: "long"
    - name: "order_date"
      type: "date"
    - name: "order_id"
      type: "long"
    - name: "product_id"
      type: "long"
    - name: "quantity"
      type: "long"
    products:
    - name: "category"
      type: "string"
    - name: "cost"
      type: "double"
    - name: "description"
      type: "string"
    - name: "name"
      type: "string"
    - name: "price"
      type: "double"
    - name: "product_id"
      type: "int"
  - schemaName: "auditing"
  tables:
  audit_kpi:
    name: "order_id"
    type: "long"
    status: "UNCHANGED"
    array: false
    nestedField: false
    name: "order_date"
    type: "date"
    status: "UNCHANGED"
    array: false
    nestedField: false
    name: "customer_id"
    type: "long"
    status: "UNCHANGED"
    array: false
    nestedField: false
    name: "purchased_items"
    type: "string"
    status: "UNCHANGED"
    array: false
    nestedField: false
    name: "total_order_value"
    type: "decimal"
    status: "UNCHANGED"
    array: false
    nestedField: false
  
   */

  public static Collection<DBSchema> getStarlakeSchemas() {
    //@formatter:off
    DBSchema schema1 = new DBSchema(
            ""
            , "starbake"
            , "orders"
            , new Attribute("customer_id", "long")
            , new Attribute("order_date", "date")
            , new Attribute("order_id", "long")
            , new Attribute("product_id", "long")
            , new Attribute("quantity", "long")
    );
    DBSchema schema2 = new DBSchema(
            ""
            , "starbake"
            , "products"
            , new Attribute("category", "string")
            , new Attribute("cost", "double")
            , new Attribute("description", "string")
            , new Attribute("name", "string")
            , new Attribute("price", "double")
            , new Attribute("product_id", "int")
    );
    DBSchema schema3 = new DBSchema(
            ""
            , "starbake"
            , "customers"
            , new Attribute("id", "int")
            , new Attribute("first_name", "string")
            , new Attribute("last_name", "string")
            , new Attribute("email", "string")
            , new Attribute("join_date", "date")
    );

    DBSchema schema4 = new DBSchema(
            ""
            , "starbake_analytics"
            , "customer_purchase_history"
            , new Attribute("customer_id", "long")
            , new Attribute("purchase_date", "date")
    );

    DBSchema schema5 = new DBSchema(
            ""
            , "audit"
            , "audit_kpi"
            , new Attribute("order_id", "long")
            , new Attribute("order_date", "date")
            , new Attribute("customer_id", "long")
            , new Attribute("purchased_items", "string")
            , new Attribute("total_order_value", "decimal")
    );

    DBSchema schema6 = new DBSchema(
            ""
            , "audit"
            , "audit"
            , new Attribute("JOBID", "string")
            , new Attribute("PATHS", "string")
            , new Attribute("SCHEMA", "string")
            , new Attribute("SUCCESS", "boolean")
            , new Attribute("COUNT", "long")
            , new Attribute("COUNTACCEPTED", "long")
            , new Attribute("COUNTREJECTED", "long")
            , new Attribute("TIMESTAMP", "timestamp")
            , new Attribute("DURATION", "long")
            , new Attribute("MESSAGE", "string")
            , new Attribute("STEP", "string")
            , new Attribute("DATABASE", "string")
            , new Attribute("TENANT", "string")
    );
    //@formatter:off

    return List.of(schema1, schema2, schema3, schema4, schema5, schema6);
  }

  @Test
  void testIssue114() throws JSQLParserException, SQLException {
    String sqlStr =
            "WITH customer_orders AS (\n"
            + "        SELECT  o.customer_id\n"
            + "                , Count( DISTINCT o.order_id ) AS total_orders\n"
            + "                , Sum( o.quantity * p.price ) AS total_spent\n"
            + "                , Min( o.order_date ) AS first_order_date\n"
            + "                , Max( o.order_date ) AS last_order_date\n"
            + "                , array_agg( DISTINCT p.category ) AS purchased_categories\n"
            + "        FROM starbake.orders o\n"
            + "            JOIN starbake.products p\n"
            + "                ON o.product_id = p.product_id\n"
            + "        GROUP BY o.customer_id )\n"
            + "SELECT  co.customer_id\n"
            + "        , Concat( c.first_name, ' ', c.last_name ) AS customer_name\n"
            + "        , c.email\n"
            + "        , co.total_orders\n"
            + "        , co.total_spent\n"
            + "        , co.first_order_date\n"
            + "        , co.last_order_date\n"
            + "        , co.purchased_categories\n"
            + "        , Datediff( 'day', co.first_order_date, co.last_order_date ) AS days_since_first_order\n"
            + "FROM starbake.customers c\n"
            + "    LEFT JOIN customer_orders co\n"
            + "        ON c.id = co.customer_id\n"
            + "ORDER BY co.total_spent DESC NULLS LAST\n"
            + ";";

    List<Attribute> expected = List.of(
            new Attribute("customer_id", "long")
            , new Attribute("customer_name", "string", AttributeStatus.ADDED)
            , new Attribute("email", "string", AttributeStatus.ADDED)
            , new Attribute("total_orders", "long", AttributeStatus.ADDED)
            , new Attribute("total_spent", "double", AttributeStatus.ADDED)
            , new Attribute("first_order_date", "date", AttributeStatus.ADDED)
            , new Attribute("last_order_date", "date", AttributeStatus.ADDED)
            , new Attribute("purchased_categories", "string", AttributeStatus.ADDED)
            , new Attribute("days_since_first_order", "long", AttributeStatus.ADDED)
            , new Attribute("purchase_date", "date", AttributeStatus.REMOVED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(getStarlakeSchemas());
    List<Attribute> actual = diff.getDiff(sqlStr, "starbake_analytics.customer_purchase_history");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void testIssue118() throws JSQLParserException, SQLException {
    //@formatter:off
    String sqlStr =
          "WITH order_details AS (\n" +
                  "        SELECT  o.order_id\n" +
                  "                , o.order_date\n" +
                  "                , o.customer_id\n" +
                  "                , Array_Agg( p.name || ' (' || o.quantity || ')' ) AS purchased_items\n" +
                  "                , Sum( o.quantity * p.price ) AS total_order_value\n" +
                  "                , p.cost\n" +
                  "        FROM starbake.orders o\n" +
                  "            JOIN starbake.products p\n" +
                  "                ON o.product_id = p.product_id\n" +
                  "        GROUP BY    o.order_id\n" +
                  "                    , o.order_date\n" +
                  "                    , o.customer_id\n" +
                  "                    , cost )\n" +
                  "SELECT  order_id\n" +
                  "        , order_date\n" +
                  "        , customer_id\n" +
                  "        , purchased_items\n" +
                  "        , total_order_value\n" +
                  "        , cost\n" +
                  "FROM order_details\n" +
                  "ORDER BY order_id\n" +
                  ";";

    List<Attribute> expected = List.of(
            new Attribute("order_id", "long")
            , new Attribute("order_date", "date", AttributeStatus.UNCHANGED)
            , new Attribute("customer_id", "long", AttributeStatus.ADDED)
            , new Attribute("purchased_items", "string", AttributeStatus.ADDED)
            , new Attribute("total_order_value", "double", AttributeStatus.ADDED)
            , new Attribute("cost", "double", AttributeStatus.ADDED)
            , new Attribute("purchase_date", "date", AttributeStatus.REMOVED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(getStarlakeSchemas());
    List<Attribute> actual = diff.getDiff(sqlStr, "starbake_analytics.customer_purchase_history");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void testIssue116() throws JSQLParserException, SQLException {
    //@formatter:off
    String sqlStr =
            "select * from audit.audit";

    List<Attribute> expected = List.of(
            new Attribute("JOBID", "string")
            , new Attribute("PATHS", "string", AttributeStatus.UNCHANGED)
            , new Attribute("SCHEMA", "string", AttributeStatus.UNCHANGED)
            , new Attribute("SUCCESS", "boolean", AttributeStatus.UNCHANGED)
            , new Attribute("COUNT", "long", AttributeStatus.UNCHANGED)
            , new Attribute("COUNTACCEPTED", "long", AttributeStatus.UNCHANGED)
            , new Attribute("COUNTREJECTED", "long", AttributeStatus.UNCHANGED)
            , new Attribute("TIMESTAMP", "timestamp", AttributeStatus.UNCHANGED)
            , new Attribute("DURATION", "long", AttributeStatus.UNCHANGED)
            , new Attribute("MESSAGE", "string", AttributeStatus.UNCHANGED)
            , new Attribute("STEP", "string", AttributeStatus.UNCHANGED)
            , new Attribute("DATABASE", "string", AttributeStatus.UNCHANGED)
            , new Attribute("TENANT", "string", AttributeStatus.UNCHANGED)
    );
    //@formatter:on

    JSQLSchemaDiff diff = new JSQLSchemaDiff(getStarlakeSchemas());
    List<Attribute> actual = diff.getDiff(sqlStr, "audit.audit");

    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }
}
