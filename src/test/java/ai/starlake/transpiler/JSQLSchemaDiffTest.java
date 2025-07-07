package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.DBSchema;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

class JSQLSchemaDiffTest {
  @Test
  void getDiff() {
    DBSchema schema = new DBSchema(null, "starbake", "orders", new Attribute("id", Long.class),
        new Attribute("order_id", String.class), new Attribute("order_date", Timestamp.class));

    schema.put("order_lines", new Attribute("id", Long.class),
        new Attribute("quantity", Long.class), new Attribute("price", Float.class));
  }
}
