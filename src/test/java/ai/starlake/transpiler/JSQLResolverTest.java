package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;


class JSQLResolverTest extends AbstractColumnResolverTest {

    @Test
    void testSimplestSchemaProvider() throws JSQLParserException, SQLException {
        String[][] schemaDefinition = {{"a", "col1", "col2", "col3"}, {"b", "col1", "col2", "col3"}};

        // allows for:
        // JdbcMetaData jdbcMetaData = new JdbcMetaData(schemaDefinition);

        String sqlStr = "SELECT sum(b.col1) FROM a, b where a.col2='test' group by b.col3;";

        JSQLResolver resolver = new JSQLResolver(schemaDefinition);
        resolver.resolve(sqlStr);

        for (JdbcColumn column: resolver.whereColumns) {
            System.out.println(Arrays.asList(column.getChildren().toArray()));
        }

        for (JdbcColumn column: resolver.groupByColumns) {
            System.out.println(Arrays.asList(column.getChildren().toArray()));
        }

    }
}