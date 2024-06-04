package ai.starlake.transpiler.bigquery;

import ai.starlake.transpiler.JSQLTranspiler;

import java.lang.reflect.InvocationTargetException;

public class BigQueryTranspiler extends JSQLTranspiler {
  public BigQueryTranspiler() throws InvocationTargetException, NoSuchMethodException,
      InstantiationException, IllegalAccessException {
    super(BigQuerySelectTranspiler.class, BigQueryExpressionTranspiler.class);
  }
}
