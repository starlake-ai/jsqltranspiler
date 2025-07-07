package ai.starlake.transpiler.diff;

import java.util.ArrayList;


public class Attribute {
  final String name;
  final String type;
  final boolean isArray;
  AttributeStatus status;

  ArrayList<Attribute> attributes; // present only if typ is "struct"

  public boolean isNestedField() {
    assert type.equalsIgnoreCase("struct");
    return attributes != null && !attributes.isEmpty();
  }

  public boolean isArray() {
    return isArray;
  }

  public Attribute(String name, String type, boolean array, ArrayList<Attribute> attributes,
      AttributeStatus status) {
    this.name = name;
    this.type = type;
    this.isArray = array;
    this.attributes = attributes;
    this.status = status;
  }

  public Attribute(String name, String type) {
    this.name = name;
    this.type = type;
    this.isArray = false;
    this.attributes = null;
    this.status = null;
  }

  public Attribute(String name, Class<?> type) {
    this.name = name;
    this.type = type.getSimpleName();
    this.isArray = false;
    this.attributes = null;
    this.status = null;
  }
}
