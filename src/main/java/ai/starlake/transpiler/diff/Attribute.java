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
    this.status = AttributeStatus.UNCHANGED;
  }

  public Attribute(String name, Class<?> type) {
    this.name = name;
    this.type = type.getSimpleName();
    this.isArray = false;
    this.attributes = null;
    this.status = AttributeStatus.UNCHANGED;
  }

  public Attribute(String name, String type, AttributeStatus status) {
    this.name = name;
    this.type = type;
    this.isArray = false;
    this.attributes = null;
    this.status = status;
  }

  public Attribute(String name, Class<?> type, AttributeStatus status) {
    this.name = name;
    this.type = type.getSimpleName();
    this.isArray = false;
    this.attributes = null;
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public AttributeStatus getStatus() {
    return status;
  }

  public Attribute setStatus(AttributeStatus status) {
    this.status = status;
    return this;
  }

  public ArrayList<Attribute> getAttributes() {
    return attributes;
  }

  public Attribute setAttributes(ArrayList<Attribute> attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Attribute)) {
      return false;
    }

    Attribute attribute = (Attribute) o;
    return isArray == attribute.isArray && name.equals(attribute.name)
        && type.equals(attribute.type);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + Boolean.hashCode(isArray);
    return result;
  }

  @Override
  public String toString() {
    return "Attribute{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", isArray=" + isArray
        + ", status=" + status + ", attributes=" + attributes + '}';
  }
}
