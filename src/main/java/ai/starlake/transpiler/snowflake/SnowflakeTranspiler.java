package ai.starlake.transpiler.snowflake;

import ai.starlake.transpiler.JSQLTranspiler;

import java.lang.reflect.InvocationTargetException;

public class SnowflakeTranspiler extends JSQLTranspiler {
  public SnowflakeTranspiler() throws InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    super(SnowflakeSelectTranspiler.class, SnowflakeExpressionTranspiler.class);
  }
}
