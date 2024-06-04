package ai.starlake.transpiler.redshift;

import ai.starlake.transpiler.JSQLTranspiler;

import java.lang.reflect.InvocationTargetException;

public class RedshiftTranspiler extends JSQLTranspiler {
  public RedshiftTranspiler() throws InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    super(RedshiftSelectTranspiler.class, RedshiftExpressionTranspiler.class);
  }
}
