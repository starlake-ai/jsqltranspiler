package ai.starlake.transpiler;

import ai.starlake.transpiler.diff.Attribute;
import ai.starlake.transpiler.diff.DBSchema;

import java.util.ArrayList;
import java.util.List;

public class JSQLSchemaDiff {
  DBSchema schema;

  public JSQLSchemaDiff(DBSchema schema) {
    this.schema = schema;
  }

  public List<Attribute> getDiff(String sqlStr, String qualifiedTargetTableName) {
    ArrayList<Attribute> attributes = new ArrayList<>();

    return attributes;
  }
}
