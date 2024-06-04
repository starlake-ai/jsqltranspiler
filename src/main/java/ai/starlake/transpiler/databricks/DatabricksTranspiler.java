package ai.starlake.transpiler.databricks;

import ai.starlake.transpiler.JSQLTranspiler;

import java.lang.reflect.InvocationTargetException;

public class DatabricksTranspiler extends JSQLTranspiler {

  public DatabricksTranspiler() throws InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    super(DatabricksSelectTranspiler.class, DatabricksExpressionTranspiler.class);
  }
}
