/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI (hayssam.saleh@starlake.ai)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.treebuilder.JsonTreeBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * The column lineage of set operations ({@code UNION}, {@code INTERSECT}, {@code EXCEPT}), with
 * schema-qualified tables, as a caller that declares its tables by schema builds them.
 */
public class JSQLColumResolverSetOperationTest {

  private static JdbcMetaData metaData() {
    return new JdbcMetaData("", "")
        .addTable("", "sales", "orders",
            Arrays.asList(new JdbcColumn("id"), new JdbcColumn("amount")))
        .addTable("", "sales", "refunds",
            Arrays.asList(new JdbcColumn("order_id"), new JdbcColumn("refund")))
        .addTable("", "sales", "adjustments",
            Arrays.asList(new JdbcColumn("ref"), new JdbcColumn("delta")));
  }

  private static final String TWO_BRANCHES =
      "SELECT id, amount FROM sales.orders UNION ALL SELECT order_id, refund FROM sales.refunds";

  @Test
  void theStaticResolutionAcceptsASetOperation() throws Exception {
    JdbcResultSetMetaData resultSetMetaData =
        JSQLColumResolver.getResultSetMetaData(TWO_BRANCHES, metaData());

    Assertions.assertThat(resultSetMetaData.getColumnCount()).isEqualTo(2);
    Assertions.assertThat(resultSetMetaData.getColumnName(1)).isEqualTo("id");
    Assertions.assertThat(resultSetMetaData.getColumnName(2)).isEqualTo("amount");
  }

  @Test
  void theCommonTableExpressionsInFrontOfASetOperationAreResolved() throws Exception {
    String sqlStr = "WITH recent AS (SELECT id, amount FROM sales.orders) "
        + "SELECT r.id, r.amount FROM recent r "
        + "UNION ALL SELECT order_id, refund FROM sales.refunds";

    Map<String, Set<String>> lineage = new JSQLColumResolver(metaData()).getLineage(sqlStr);

    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id");
    Assertions.assertThat(lineage.get("amount")).containsExactly("sales.orders.amount");
  }

  @Test
  void theFirstBranchOnlyByDefault() throws Exception {
    JSQLColumResolver resolver = new JSQLColumResolver(metaData());
    Assertions.assertThat(resolver.isAllSetOperationBranches()).isFalse();

    JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(TWO_BRANCHES);
    Assertions.assertThat(resultSetMetaData.getTableName(1)).isEqualTo("orders");

    Map<String, Set<String>> lineage = resolver.getLineage(TWO_BRANCHES);
    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id");
    Assertions.assertThat(lineage.get("amount")).containsExactly("sales.orders.amount");
  }

  @Test
  void everyBranchOfASetOperationWhenAsked() throws Exception {
    JSQLColumResolver resolver = new JSQLColumResolver(metaData()).setAllSetOperationBranches(true);

    JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(TWO_BRANCHES);
    Assertions.assertThat(resultSetMetaData.getColumnCount()).isEqualTo(2);
    Assertions.assertThat(resultSetMetaData.getColumnName(1)).isEqualTo("id");
    Assertions.assertThat(resultSetMetaData.getTableName(1)).isEmpty();
    Assertions.assertThat(resultSetMetaData.getColumns().get(0).getChildren()).hasSize(2);

    Map<String, Set<String>> lineage = resolver.getLineage(TWO_BRANCHES);
    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id",
        "sales.refunds.order_id");
    Assertions.assertThat(lineage.get("amount")).containsExactly("sales.orders.amount",
        "sales.refunds.refund");
  }

  @Test
  void everyBranchOfASetOperationInACommonTableExpression() throws Exception {
    String sqlStr = "WITH movements AS (" + "SELECT id, amount FROM sales.orders "
        + "UNION ALL SELECT order_id, refund FROM sales.refunds "
        + "UNION ALL SELECT ref, delta FROM sales.adjustments) "
        + "SELECT m.id AS order_ref, Sum(m.amount) AS total FROM movements m GROUP BY m.id";

    Map<String, Set<String>> lineage =
        new JSQLColumResolver(metaData()).setAllSetOperationBranches(true).getLineage(sqlStr);

    Assertions.assertThat(lineage.get("order_ref")).containsExactly("sales.orders.id",
        "sales.refunds.order_id", "sales.adjustments.ref");
    Assertions.assertThat(lineage.get("total")).containsExactly("sales.orders.amount",
        "sales.refunds.refund", "sales.adjustments.delta");
  }

  @Test
  void everyBranchOfASetOperationOfStarsOverCommonTableExpressions() throws Exception {
    String sqlStr = "WITH previous AS (SELECT id, amount FROM sales.orders), "
        + "latest AS (SELECT order_id, refund FROM sales.refunds) "
        + "SELECT * FROM previous WHERE id IN (SELECT order_id FROM latest) "
        + "UNION ALL SELECT * FROM latest";

    Map<String, Set<String>> lineage =
        new JSQLColumResolver(metaData()).setAllSetOperationBranches(true).getLineage(sqlStr);

    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id",
        "sales.refunds.order_id");
    Assertions.assertThat(lineage.get("amount")).containsExactly("sales.orders.amount",
        "sales.refunds.refund");
  }

  private static Map<String, Set<String>> allBranches(String sqlStr) throws Exception {
    return new JSQLColumResolver(metaData()).setAllSetOperationBranches(true).getLineage(sqlStr);
  }

  @Test
  void theDerivedColumnsOfACommonTableExpressionReadByABranch() throws Exception {
    String sqlStr =
        "WITH totals AS (SELECT id, Sum(amount) AS total FROM sales.orders GROUP BY id) "
            + "SELECT id, total FROM totals UNION ALL SELECT order_id, refund FROM sales.refunds";

    Assertions.assertThat(new JSQLColumResolver(metaData()).getLineage(sqlStr).get("total"))
        .containsExactly("sales.orders.amount");
    Map<String, Set<String>> lineage = allBranches(sqlStr);
    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id",
        "sales.refunds.order_id");
    Assertions.assertThat(lineage.get("total")).containsExactly("sales.orders.amount",
        "sales.refunds.refund");
  }

  @Test
  void aSetOperationInACommonTableExpressionReadByABranch() throws Exception {
    Map<String, Set<String>> lineage = allBranches("WITH moves AS ("
        + "SELECT id FROM sales.orders UNION ALL SELECT order_id FROM sales.refunds) "
        + "SELECT id FROM moves UNION ALL SELECT ref FROM sales.adjustments");

    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id",
        "sales.refunds.order_id", "sales.adjustments.ref");
  }

  @Test
  void aRecursiveCommonTableExpression() throws Exception {
    Map<String, Set<String>> lineage = allBranches("WITH RECURSIVE chain AS ("
        + "SELECT id FROM sales.orders UNION ALL SELECT c.id FROM chain c) SELECT id FROM chain");

    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id");
  }

  @Test
  void theRightSideOfExceptGivesNoValue() throws Exception {
    Assertions.assertThat(
        allBranches("SELECT id FROM sales.orders EXCEPT SELECT order_id FROM sales.refunds")
            .get("id"))
        .containsExactly("sales.orders.id");
    Assertions.assertThat(
        allBranches("SELECT id FROM sales.orders INTERSECT SELECT order_id FROM sales.refunds")
            .get("id"))
        .containsExactly("sales.orders.id", "sales.refunds.order_id");
  }

  @Test
  void nestedParenthesedBranches() throws Exception {
    Map<String, Set<String>> lineage = allBranches("(SELECT id FROM sales.orders) UNION ALL "
        + "(SELECT order_id FROM sales.refunds UNION ALL SELECT ref FROM sales.adjustments)");

    Assertions.assertThat(lineage.get("id")).containsExactly("sales.orders.id",
        "sales.refunds.order_id", "sales.adjustments.ref");
  }

  @Test
  void aBranchWithoutColumnsAndTheLabelOfTheFirstBranch() throws Exception {
    Map<String, Set<String>> lineage =
        allBranches("SELECT id AS order_ref, amount FROM sales.orders UNION ALL VALUES (1, 2)");

    Assertions.assertThat(lineage.get("order_ref")).containsExactly("sales.orders.id");
    Assertions.assertThat(lineage.get("amount")).containsExactly("sales.orders.amount");
  }

  @Test
  void theJsonTreeOfASetOperationIsValid() throws Exception {
    String json = new JSQLColumResolver(metaData()).setAllSetOperationBranches(true)
        .getLineage(JsonTreeBuilder.class, TWO_BRANCHES);

    // two children of a column are separated by a comma
    Assertions.assertThat(json).doesNotContainPattern("\\}\\s*\\{");
    Assertions.assertThat(json).contains("sales").contains("refunds");
  }
}
