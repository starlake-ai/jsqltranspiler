/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2025 Starlake.AI <hayssam.saleh@starlake.ai>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitorAdapter;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The JSQLReplacer class for replacing any occurrence of a table in a statement.
 */
public class JSQLReplacer {
  private final CaseInsensitiveLinkedHashMap<String> replaceTables =
      new CaseInsensitiveLinkedHashMap<>();
  private final JSQLResolver resolver;


  /**
   * Instantiates a new JSQLReplacer for a given Database MetaData for an empty default Catalog and
   * Schema.
   *
   * @param metaData the database meta data
   */
  public JSQLReplacer(JdbcMetaData metaData) {
    resolver = new JSQLResolver(metaData);
  }


  /**
   * Instantiates a new JSQLReplacer for a given open Database Connection.
   *
   * @param connection the open database connection
   * @throws SQLException when the Database MetaData can't be queried
   */
  public JSQLReplacer(Connection connection) throws SQLException {
    resolver = new JSQLResolver(connection).setCommentFlag(false);
  }

  /**
   * Instantiates a new JSQLReplacer for a given Database MetaData.
   *
   * @param currentCatalogName the current catalog name
   * @param currentSchemaName the current schema name
   * @param metaDataDefinition the meta data definition
   */
  public JSQLReplacer(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    resolver = new JSQLResolver(currentCatalogName, currentSchemaName, metaDataDefinition)
        .setCommentFlag(false);
  }

  /**
   * Instantiates a new JSQLReplacer for a given Database MetaData.
   *
   * @param metaDataDefinition the meta data definition
   */
  public JSQLReplacer(String[][] metaDataDefinition) {
    resolver = new JSQLResolver(metaDataDefinition).setCommentFlag(false);
  }

  /**
   * Clear the map of tables to be replaced.
   */
  public void clearReplaceTables() {
    replaceTables.clear();
  }

  /**
   * Get the map of tables to be replaced.
   *
   * @return the replacement tables
   */
  public CaseInsensitiveLinkedHashMap<String> getReplaceTables() {
    return replaceTables;
  }

  /**
   * Put a map of table names into the map of tables to be replaced.
   *
   * @param replaceTables the replacement tables
   * @return the jsql replacer
   */
  public JSQLReplacer putReplaceTables(Map<String, String> replaceTables) {
    this.replaceTables.putAll(replaceTables);
    return this;
  }

  /**
   * Put a table name into the map of tables to be replaced.
   *
   * @param qualifiedTableName the qualified table name
   * @param replacementName the replacement name
   * @return the jsql replacer
   */
  public JSQLReplacer putReplacementTable(String qualifiedTableName, String replacementName) {
    this.replaceTables.put(qualifiedTableName, replacementName);
    return this;
  }

  private final ExpressionVisitorAdapter<Void> expressionVisitor =
      new ExpressionVisitorAdapter<>() {
        @Override
        public <S> Void visit(Column column, S context) {
          if (column.getTable() != null) {
            Table table = column.getTable();
            if (table.getResolvedTable() != null
                && replaceTables
                    .containsKey(
                        table.getResolvedTable()
                            .setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                                resolver.metaData.getCurrentSchemaName())
                            .getFullyQualifiedName())) {

              String replacementName =
                  replaceTables.get(table.getResolvedTable().getFullyQualifiedName());

              if (table.getDatabaseName() != null) {
                table.setName(replacementName);
              } else {
                Table replacementTable = new Table(replacementName);

                if (table.getSchemaName() != null) {
                  table.setSchemaName(replacementTable.getSchemaName());
                }
                table.setName(replacementTable.getName());
              }
            }
          }
          return null;
        }
      };

  private final FromItemVisitorAdapter<Void> fromItemVisitor = new FromItemVisitorAdapter<>() {
    @Override
    public <S> Void visit(Table table, S context) {
      if (table.getResolvedTable() != null && replaceTables.containsKey(table.getResolvedTable()
          .setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
              resolver.metaData.getCurrentSchemaName())
          .getFullyQualifiedName())) {

        String replacementName =
            replaceTables.get(table.getResolvedTable().getFullyQualifiedName());
        table.setName(replacementName);
      }
      return null;
    }
  };

  private final PivotVisitorAdapter<Void> pivotVisitorAdapter =
      new PivotVisitorAdapter<>(expressionVisitor);

  private final SelectItemVisitorAdapter<Void> selectItemVisitor =
      new SelectItemVisitorAdapter<>(expressionVisitor);

  private final SelectVisitorAdapter<Void> selectVisitor = new SelectVisitorAdapter<>(
      expressionVisitor, pivotVisitorAdapter, selectItemVisitor, fromItemVisitor);
  {
    expressionVisitor.setSelectVisitor(selectVisitor);
    fromItemVisitor.setSelectVisitor(selectVisitor);
  }

  private final MergeOperationVisitorAdapter<Void> mergeOperationVisitor =
      new MergeOperationVisitorAdapter<>(selectVisitor);

  private final StatementVisitorAdapter<Void> statementVisitor =
      new StatementVisitorAdapter<>(expressionVisitor, pivotVisitorAdapter, selectItemVisitor,
          fromItemVisitor, selectVisitor, mergeOperationVisitor);

  /**
   * Replace physically existing table names in a given statement.
   *
   * @param st the statement
   * @param replacementTables the replacement tables
   * @return the modified statement with the replaced table names
   */
  public Statement replace(Statement st, Map<String, String> replacementTables) {
    this.replaceTables.clear();
    for (Entry<String, String> entry : replacementTables.entrySet()) {
      Table t = new Table(entry.getKey()).setUnsetCatalogAndSchema(
          resolver.metaData.getCurrentCatalogName(), resolver.metaData.getCurrentSchemaName());
      this.replaceTables.put(t.getFullyQualifiedName(), entry.getValue());
    }

    resolver.resolve(st);
    st.accept(statementVisitor, null);

    return st;
  }

  /**
   * Replace physically existing table names in a given statement.
   *
   * @param st the statement
   * @param replacementTables the replacement tables
   * @return the modified statement with the replaced table names
   */
  public Statement replace(Statement st, CaseInsensitiveLinkedHashMap<Table> replacementTables) {
    CaseInsensitiveLinkedHashMap<String> replacementTableNames =
        new CaseInsensitiveLinkedHashMap<>();
    for (Entry<String, Table> entry : replacementTables.entrySet()) {
      replacementTableNames.put(entry.getKey(), entry.getValue().getFullyQualifiedName());
    }
    return replace(st, replacementTableNames);
  }

  /**
   * Replace physically existing table names in a given query.
   *
   * @param sqlStr the query text
   * @param replacementTables the replacement tables
   * @return the statement
   * @throws JSQLParserException the jsql parser exception
   */
  public Statement replace(String sqlStr, Map<String, String> replacementTables)
      throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(sqlStr);
    return replace(st, replacementTables);
  }

}
