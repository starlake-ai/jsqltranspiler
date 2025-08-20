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

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.delete.ParenthesedDelete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.insert.ParenthesedInsert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitorAdapter;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.ParenthesedUpdate;
import net.sf.jsqlparser.statement.update.Update;

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
            Table resolved = table.getResolvedTable();
            if (resolved != null) {
              resolved.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                  resolver.metaData.getCurrentSchemaName());

              if (replaceTables.containsKey(resolved.getFullyQualifiedName())) {
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
          fromItemVisitor, selectVisitor, mergeOperationVisitor) {
        @Override
        public <S> Void visit(Delete delete, S context) {
          Table t = delete.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          } else {
            for (Table t1 : delete.getTables()) {
              t1.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                  resolver.metaData.getCurrentSchemaName());

              if (replaceTables.containsKey(t1.getFullyQualifiedName())) {
                t1.setName(replaceTables.get(t1.getFullyQualifiedName()));
              }
            }
          }

          return super.visit(delete, context);
        }

        @Override
        public <S> Void visit(ParenthesedDelete delete, S context) {
          return visit(delete.getDelete(), context);
        }

        @Override
        public <S> Void visit(Update update, S context) {
          Table t = update.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          }
          return super.visit(update, context);
        }

        @Override
        public <S> Void visit(ParenthesedUpdate update, S context) {
          return visit(update.getUpdate(), context);
        }

        @Override
        public <S> Void visit(Insert insert, S context) {
          Table t = insert.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          }
          return super.visit(insert, context);
        }

        @Override
        public <S> Void visit(ParenthesedInsert insert, S context) {
          return visit(insert.getInsert(), context);
        }

        @Override
        public <S> Void visit(Drop drop, S context) {
          if ("TABLE".equalsIgnoreCase(drop.getType())) {
            Table t = drop.getName();
            if (t != null) {
              t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                  resolver.metaData.getCurrentSchemaName());

              if (replaceTables.containsKey(t.getFullyQualifiedName())) {
                t.setName(replaceTables.get(t.getFullyQualifiedName()));
              }
            }
          }
          return super.visit(drop, context);
        }

        @Override
        public <S> Void visit(Truncate truncate, S context) {
          Table t = truncate.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          } else {
            for (Table t1 : truncate.getTables()) {
              t1.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                  resolver.metaData.getCurrentSchemaName());

              if (replaceTables.containsKey(t1.getFullyQualifiedName())) {
                t1.setName(replaceTables.get(t1.getFullyQualifiedName()));
              }
            }
          }
          return super.visit(truncate, context);
        }

        @Override
        public <S> Void visit(CreateIndex createIndex, S context) {
          Table t = createIndex.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          }
          return super.visit(createIndex, context);
        }

        @Override
        public <S> Void visit(CreateTable createTable, S context) {
          return super.visit(createTable, context);
        }

        @Override
        public <S> Void visit(CreateView createView, S context) {
          return super.visit(createView, context);
        }

        @Override
        public <S> Void visit(Alter alter, S context) {
          return super.visit(alter, context);
        }

        @Override
        public <S> Void visit(Merge merge, S context) {
          Table t = merge.getTable();
          if (t != null) {
            t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                resolver.metaData.getCurrentSchemaName());

            if (replaceTables.containsKey(t.getFullyQualifiedName())) {
              t.setName(replaceTables.get(t.getFullyQualifiedName()));
            }
          }

          return super.visit(merge, context);
        }

        @Override
        public <S> Void visit(RenameTableStatement renameTableStatement, S context) {
          for (Entry<Table, Table> e : renameTableStatement.getTableNames()) {
            Table t = e.getKey();
            if (t != null) {
              t.setUnsetCatalogAndSchema(resolver.metaData.getCurrentCatalogName(),
                  resolver.metaData.getCurrentSchemaName());

              if (replaceTables.containsKey(t.getFullyQualifiedName())) {
                t.setName(replaceTables.get(t.getFullyQualifiedName()));
              }
            }
          }
          return super.visit(renameTableStatement, context);
        }
      };

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
