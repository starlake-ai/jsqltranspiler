package ai.starlake.transpiler.schema;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CaseInsensitiveConcurrentSet {
  private final ConcurrentMap<String, Boolean> map;

  private CaseInsensitiveConcurrentSet() {
    this.map = new ConcurrentHashMap<>();
  }

  public static Set<String> newSet() {
    return Collections.newSetFromMap(new CaseInsensitiveConcurrentSet().map);
  }

  public boolean add(String s) {
    return map.putIfAbsent(s.toLowerCase(), Boolean.TRUE) == null;
  }

  public boolean contains(String s) {
    return map.containsKey(s.toLowerCase());
  }

  public boolean remove(String s) {
    return map.remove(s.toLowerCase()) != null;
  }

}
