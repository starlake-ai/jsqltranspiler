package ai.starlake.transpiler.diff;

public interface DBSchemaDiffApi {

    DBSchema diff(String sql, DBSchema existingSchema) throws Exception;

}
