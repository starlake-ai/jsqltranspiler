/**
 * Starlake.AI JSQLTranspiler is a SQL to DuckDB Transpiler.
 * Copyright (C) 2024 Starlake.AI
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

import ai.starlake.transpiler.schema.JdbcColumn;
import ai.starlake.transpiler.schema.JdbcMetaData;
import ai.starlake.transpiler.schema.JdbcResultSetMetaData;
import ai.starlake.transpiler.schema.JdbcTable;
import ai.starlake.transpiler.schema.treebuilder.TreeBuilder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A class for resolving the actual columns returned by a SELECT statement. Depends on virtual or
 * physical Database Metadata holding the schema and table information.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class JSQLColumResolver
    implements SelectVisitor<JdbcResultSetMetaData>, FromItemVisitor<JdbcResultSetMetaData> {
  public final static Logger LOGGER = Logger.getLogger(JSQLColumResolver.class.getName());


  private final JdbcMetaData metaData;
  private final JSQLExpressionColumnResolver expressionColumnResolver;


  /**
   * Instantiates a new JSQLColumnResolver for the provided Database Meta Data
   *
   * @param metaData the meta data
   */
  public JSQLColumResolver(JdbcMetaData metaData) {
    this.metaData = metaData;
    this.expressionColumnResolver = new JSQLExpressionColumnResolver(this);
  }


  /**
   * Instantiates a new JSQLColumnResolver for the provided simplified Meta Data, presented as an
   * Array of Tables and Column Names only.
   *
   * @param currentCatalogName the current catalog name
   * @param currentSchemaName the current schema name
   * @param metaDataDefinition the meta data definition as n Array of Tablename and Column Names
   */
  public JSQLColumResolver(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    this(new JdbcMetaData(currentCatalogName, currentSchemaName, metaDataDefinition));
  }

  /**
   * Instantiates a new JSQLColumnResolver for the provided simplified Meta Data with an empty
   * CURRENT_SCHEMA and CURRENT_CATALOG
   *
   * @param metaDataDefinition the meta data definition as n Array of Tablename and Column Names
   */
  public JSQLColumResolver(String[][] metaDataDefinition) {
    this("", "", metaDataDefinition);
  }


  /**
   * Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and
   * CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaData the Database Meta Data
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public static JdbcResultSetMetaData getResultSetMetaData(String sqlStr, JdbcMetaData metaData)
      throws JSQLParserException {

    if (sqlStr == null || sqlStr.trim().isEmpty()) {
      throw new JSQLParserException("The provided Statement must not be empty.");
    }

    JSQLColumResolver resolver = new JSQLColumResolver(metaData);

    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      PlainSelect select = (PlainSelect) st;
      return select.accept(resolver, JdbcMetaData.copyOf(metaData));

    } else {
      throw new RuntimeException(
          st.getClass().getSimpleName() + " Statements are not supported yet.");
    }
  }

  /**
   * Resolves the actual columns returned by a SELECT statement for a given CURRENT_CATALOG and
   * CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaDataDefinition the meta data definition as an array of Tables with Columns e.g. {
   *        TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
   * @param currentCatalogName the CURRENT_CATALOG name (which is the default catalog for accessing
   *        the schemas)
   * @param currentSchemaName the CURRENT_SCHEMA name (which is the default schema for accessing the
   *        tables)
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public static JdbcResultSetMetaData getResultSetMetaData(String sqlStr,
      String[][] metaDataDefinition, String currentCatalogName, String currentSchemaName)
      throws JSQLParserException {
    JdbcMetaData metaData =
        new JdbcMetaData(currentCatalogName, currentSchemaName, metaDataDefinition);
    return getResultSetMetaData(sqlStr, metaData);
  }

  /**
   * Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an
   * empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @param metaDataDefinition the meta data definition as an array of Tables with Columns e.g. {
   *        TABLE_NAME, COLUMN1, COLUMN2 ... COLUMN10 }
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public JdbcResultSetMetaData getResultSetMetaData(String sqlStr, String[][] metaDataDefinition)
      throws JSQLParserException {
    JdbcMetaData metaData = new JdbcMetaData("", "", metaDataDefinition);
    return getResultSetMetaData(sqlStr, metaData);
  }

  /**
   * Resolves the actual columns returned by a SELECT statement for an empty CURRENT_CATALOG and an
   * empty CURRENT_SCHEMA and wraps this information into `ResultSetMetaData`.
   *
   * @param sqlStr the `SELECT` statement text
   * @return the ResultSetMetaData representing the actual columns returned by the `SELECT`
   *         statement
   * @throws JSQLParserException when the `SELECT` statement text can not be parsed
   */
  public JdbcResultSetMetaData getResultSetMetaData(String sqlStr) throws JSQLParserException {

    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      Select select = (Select) st;
      return select.accept(this, JdbcMetaData.copyOf(metaData));
    } else {
      throw new RuntimeException("Unsupported Statement");
    }
  }


  /**
   * Gets the rewritten statement text with any AllColumns "*" or AllTableColumns "t.*" expression
   * resolved into the actual columns
   *
   * @param sqlStr the query statement string (using any AllColumns "*" or AllTableColumns "t.*"
   *        expression)
   * @return rewritten statement text with any AllColumns "*" or AllTableColumns "t.*" expression
   *         resolved into the actual columns
   * @throws JSQLParserException the exception when parsing the query statement fails
   */
  public String getResolvedStatementText(String sqlStr) throws JSQLParserException {
    StringBuilder builder = new StringBuilder();
    StatementDeParser deParser = new StatementDeParser(builder);

    Statement st = CCJSqlParserUtil.parse(sqlStr);
    if (st instanceof Select) {
      Select select = (Select) st;
      select.accept(this, JdbcMetaData.copyOf(metaData));
    }
    st.accept(deParser);
    return builder.toString();
  }

  public static <T> T getLineage(Class<? extends TreeBuilder<T>> treeBuilderClass, String sqlStr,
      Connection connection) throws JSQLParserException, NoSuchMethodException,
      InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {

    JdbcMetaData metaData = new JdbcMetaData(connection);
    JSQLColumResolver resolver = new JSQLColumResolver(metaData);
    JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(sqlStr);
    TreeBuilder<T> builder =
        treeBuilderClass.getConstructor(JdbcResultSetMetaData.class).newInstance(resultSetMetaData);
    return builder.getConvertedTree(resolver);
  }

  public static <T> T getLineage(Class<? extends TreeBuilder<T>> treeBuilderClass, String sqlStr,
      String[][] metaDataDefinition, String currentCatalogName, String currentSchemaName)
      throws JSQLParserException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException, SQLException {

    JdbcMetaData metaData =
        new JdbcMetaData(currentCatalogName, currentSchemaName, metaDataDefinition);
    JSQLColumResolver resolver = new JSQLColumResolver(metaData);

    JdbcResultSetMetaData resultSetMetaData = resolver.getResultSetMetaData(sqlStr);
    TreeBuilder<T> builder =
        treeBuilderClass.getConstructor(JdbcResultSetMetaData.class).newInstance(resultSetMetaData);
    return builder.getConvertedTree(resolver);
  }

  // for visiting Column Sub-Selects
  public <T> T getLineage(Class<? extends TreeBuilder<T>> treeBuilderClass, Select select)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
      IllegalAccessException, SQLException {
    JdbcResultSetMetaData resultSetMetaData = select.accept(this, JdbcMetaData.copyOf(metaData));
    TreeBuilder<T> builder =
        treeBuilderClass.getConstructor(JdbcResultSetMetaData.class).newInstance(resultSetMetaData);
    return builder.getConvertedTree(this);
  }


  public <T> T getLineage(Class<? extends TreeBuilder<T>> treeBuilderClass, String sqlStr)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException,
      IllegalAccessException, SQLException, JSQLParserException {
    JdbcResultSetMetaData resultSetMetaData = getResultSetMetaData(sqlStr);
    TreeBuilder<T> builder =
        treeBuilderClass.getConstructor(JdbcResultSetMetaData.class).newInstance(resultSetMetaData);
    return builder.getConvertedTree(this);
  }

  public static String getQualifiedTableName(String catalogName, String schemaName,
      String tableName) {
    StringBuilder builder = new StringBuilder();
    if (catalogName != null && !catalogName.isEmpty()) {
      builder.append(catalogName).append(".");
      builder.append(schemaName != null ? schemaName : "").append(".");
    } else if (schemaName != null && !schemaName.isEmpty()) {
      builder.append(schemaName).append(".");
    }
    builder.append(tableName);
    return builder.toString();
  }

  public static String getQualifiedColumnName(String catalogName, String schemaName,
      String tableName, String columName) {
    StringBuilder builder = new StringBuilder();
    if (tableName == null || tableName.isEmpty()) {
      return columName;
    } else if (catalogName != null && !catalogName.isEmpty()) {
      builder.append(catalogName).append(".");
      builder.append(schemaName != null ? schemaName : "").append(".");
    } else if (schemaName != null && !schemaName.isEmpty()) {
      builder.append(schemaName).append(".");
    }
    builder.append(tableName).append(".").append(columName);

    return builder.toString();
  }

  @Override
  public <S> JdbcResultSetMetaData visit(Table table, S context) {
    JdbcResultSetMetaData rsMetaData = new JdbcResultSetMetaData();

    if (table.getSchemaName() == null || table.getSchemaName().isEmpty()) {
      table.setSchemaName(metaData.getCurrentSchemaName());
    }

    if (table.getDatabase() == null) {
      table.setDatabaseName(metaData.getCurrentCatalogName());
    } else if (table.getDatabase().getDatabaseName() == null
        || table.getDatabase().getDatabaseName().isEmpty()) {
      table.getDatabase().setDatabaseName(metaData.getCurrentCatalogName());
    }

    for (JdbcColumn jdbcColumn : metaData.getTableColumns(table.getDatabase().getDatabaseName(),
        table.getSchemaName(), table.getName(), null)) {

      rsMetaData.add(jdbcColumn, null);
    }

    return rsMetaData;
  }

  public JdbcResultSetMetaData visit(ParenthesedSelect parenthesedSelect, JdbcMetaData context) {
    JdbcResultSetMetaData rsMetaData = null;
    Alias alias = parenthesedSelect.getAlias();
    JdbcTable t = new JdbcTable(metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName(),
        alias != null ? parenthesedSelect.getAlias().getName() : "");

    rsMetaData = parenthesedSelect.getSelect().accept((SelectVisitor<JdbcResultSetMetaData>) this,
        JdbcMetaData.copyOf(context));
    try {
      int columnCount = rsMetaData.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        t.add(t.tableCatalog, t.tableSchema, t.tableName,
            rsMetaData.getColumnLabel(i) != null && !rsMetaData.getColumnLabel(i).isEmpty()
                ? rsMetaData.getColumnLabel(i)
                : rsMetaData.getColumnName(i),
            rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
            rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
            "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
            rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
      }
      metaData.put(t);
    } catch (SQLException ex) {
      throw new RuntimeException("Error in WITH clause " + parenthesedSelect.toString(), ex);
    }
    return rsMetaData;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(ParenthesedSelect parenthesedSelect, S context) {
    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData1 = (JdbcMetaData) context;
      return visit(parenthesedSelect, JdbcMetaData.copyOf(metaData1));
    }
    return null;
  }

  @Override
  public void visit(ParenthesedSelect parenthesedSelect) {
    SelectVisitor.super.visit(parenthesedSelect);
  }


  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength"})
  public JdbcResultSetMetaData visit(PlainSelect select, JdbcMetaData metaData) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = select.getFromItem();
    List<Join> joins = select.getJoins();

    if (select.getWithItemsList() != null) {
      for (WithItem withItem : select.getWithItemsList()) {
        withItem.accept((SelectVisitor<JdbcResultSetMetaData>) this, metaData);
      }
    }

    if (fromItem instanceof Table) {
      Alias alias = fromItem.getAlias();
      Table t = (Table) fromItem;

      if (alias != null) {
        metaData.getFromTables().put(alias.getName().replaceAll("^\"|\"$", ""), (Table) fromItem);
      } else {
        metaData.getFromTables().put(t.getName().replaceAll("^\"|\"$", ""), (Table) fromItem);
      }
    } else if (fromItem != null) {
      Alias alias = fromItem.getAlias();
      JdbcTable t = new JdbcTable(metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName(),
          alias != null ? alias.getName() : "");

      JdbcResultSetMetaData rsMetaData = fromItem.accept(this, JdbcMetaData.copyOf(metaData));
      try {
        int columnCount = rsMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          t.add(t.tableCatalog, t.tableSchema, t.tableName,
              rsMetaData.getColumnLabel(i) != null && !rsMetaData.getColumnLabel(i).isEmpty()
                  ? rsMetaData.getColumnLabel(i)
                  : rsMetaData.getColumnName(i),
              rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
              rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
              "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
              rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
        }
        metaData.put(t);
        metaData.getFromTables().put(
            alias != null ? alias.getName().replaceAll("^\"|\"$", "")
                : t.tableName.replaceAll("^\"|\"$", ""),
            new Table(alias != null ? alias.getName() : t.tableName));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    if (joins != null) {
      for (Join join : joins) {
        if (join.isLeft() || join.isInner()) {
          metaData.addLeftUsingJoinColumns(join.getUsingColumns());
        } else if (join.isRight()) {
          metaData.addRightUsingJoinColumns(join.getUsingColumns());
        }

        if (join.getFromItem() instanceof Table) {
          Alias alias = join.getFromItem().getAlias();
          Table t = (Table) join.getFromItem();

          if (alias != null) {
            metaData.getFromTables().put(alias.getName().replaceAll("^\"|\"$", ""), t);
            if (join.isNatural()) {
              metaData.getNaturalJoinedTables().put(alias.getName().replaceAll("^\"|\"$", ""), t);
            }

          } else {
            metaData.getFromTables().put(t.getName().replaceAll("^\"|\"$", ""), t);
            if (join.isNatural()) {
              metaData.addNaturalJoinedTable(t);
            }
          }
        } else {
          Alias alias = join.getFromItem().getAlias();
          JdbcTable t = new JdbcTable(metaData.getCurrentCatalogName(),
              metaData.getCurrentSchemaName(), alias != null ? alias.getName() : "");

          JdbcResultSetMetaData rsMetaData =
              join.getFromItem().accept(this, JdbcMetaData.copyOf(metaData));
          try {
            int columnCount = rsMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
              t.add(t.tableCatalog, t.tableSchema, t.tableName, rsMetaData.getColumnName(i),
                  rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
                  rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i),
                  "", "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
                  rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
            }
            metaData.put(t);
            metaData.getFromTables().put(
                alias != null ? alias.getName().replaceAll("^\"|\"$", "")
                    : t.tableName.replaceAll("^\"|\"$", ""),
                new Table(alias != null ? alias.getName() : t.tableName));

            if (join.isNatural()) {
              metaData.getNaturalJoinedTables().put(alias != null ? alias.getName() : t.tableName,
                  new Table(alias != null ? alias.getName() : t.tableName));
            }
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    for (Table t : metaData.getFromTables().values()) {
      if (t.getSchemaName() == null || t.getSchemaName().isEmpty()) {
        t.setSchemaName(metaData.getCurrentSchemaName());
      }

      if (t.getDatabase() == null) {
        t.setDatabaseName(metaData.getCurrentCatalogName());
      } else if (t.getDatabase().getDatabaseName() == null
          || t.getDatabase().getDatabaseName().isEmpty()) {
        t.getDatabase().setDatabaseName(metaData.getCurrentCatalogName());
      }
    }

    for (Table t : metaData.getNaturalJoinedTables().values()) {
      if (t.getSchemaName() == null || t.getSchemaName().isEmpty()) {
        t.setSchemaName(metaData.getCurrentSchemaName());
      }

      if (t.getDatabase() == null) {
        t.setDatabaseName(metaData.getCurrentCatalogName());
      } else if (t.getDatabase().getDatabaseName() == null
          || t.getDatabase().getDatabaseName().isEmpty()) {
        t.getDatabase().setDatabaseName(metaData.getCurrentCatalogName());
      }
    }

    /* this is valid SQL:
    
    SELECT
        main.sales.salesid
    FROM main.sales
     */

    // column positions in MetaData start at 1
    ArrayList<SelectItem<?>> newSelectItems = new ArrayList<>();
    for (SelectItem<?> selectItem : select.getSelectItems()) {
      Alias alias = selectItem.getAlias();
      List<JdbcColumn> jdbcColumns =
          selectItem.getExpression().accept(expressionColumnResolver, metaData);

      for (JdbcColumn col : jdbcColumns) {
        resultSetMetaData.add(col, alias != null ? alias.withUseAs(false).getName() : null);
        Table t = new Table(col.tableCatalog, col.tableSchema, col.tableName);
        if (selectItem.getExpression() instanceof AllColumns
            || selectItem.getExpression() instanceof AllTableColumns) {
          newSelectItems.add(new SelectItem<>(
              new Column(t, col.columnName).withCommentText("Resolved Column"), alias));
        } else {
          newSelectItems.add(selectItem);
        }
      }

    }
    select.setSelectItems(newSelectItems);

    return resultSetMetaData;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(PlainSelect select, S context) {
    if (context instanceof JdbcMetaData) {
      return visit(select, (JdbcMetaData) context);
    } else {
      return null;
    }
  }

  @Override
  public void visit(PlainSelect plainSelect) {
    SelectVisitor.super.visit(plainSelect);
  }

  // for visiting Column Sub-Selects
  public JdbcResultSetMetaData visit(Select select) {
    return select.accept(this, JdbcMetaData.copyOf(metaData));
  }

  @Override
  public <S> JdbcResultSetMetaData visit(SetOperationList setOperationList, S context) {
    if (context instanceof JdbcMetaData) {
      return setOperationList.getSelect(0).accept(this, (JdbcMetaData) context);
    } else {
      return null;
    }
  }

  @Override
  public <S> JdbcResultSetMetaData visit(WithItem withItem, S context) {
    if (context instanceof JdbcMetaData) {
      JdbcMetaData metaData = (JdbcMetaData) context;

      JdbcTable t = new JdbcTable(metaData.getCurrentCatalogName(), metaData.getCurrentSchemaName(),
          withItem.getAlias().getName().replaceAll("^\"|\"$", ""));

      JdbcResultSetMetaData rsMetaData = withItem.getSelect()
          .accept((SelectVisitor<JdbcResultSetMetaData>) this, JdbcMetaData.copyOf(metaData));
      try {
        int columnCount = rsMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          t.add(t.tableCatalog, t.tableSchema, t.tableName,
              rsMetaData.getColumnLabel(i) != null && !rsMetaData.getColumnLabel(i).isEmpty()
                  ? rsMetaData.getColumnLabel(i).replaceAll("^\"|\"$", "")
                  : rsMetaData.getColumnName(i).replaceAll("^\"|\"$", ""),
              rsMetaData.getColumnType(i), rsMetaData.getColumnClassName(i),
              rsMetaData.getPrecision(i), rsMetaData.getScale(i), 10, rsMetaData.isNullable(i), "",
              "", rsMetaData.getColumnDisplaySize(i), i, "", rsMetaData.getCatalogName(i),
              rsMetaData.getSchemaName(i), rsMetaData.getTableName(i), null, "", "");
        }
        metaData.put(t);
      } catch (SQLException ex) {
        throw new RuntimeException("Error in WITH clause " + withItem.toString(), ex);
      }
    }

    return withItem.getSelect().accept((SelectVisitor<JdbcResultSetMetaData>) this, metaData);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(Values values, S context) {
    return null;
  }

  @Override
  public void visit(Values values) {
    SelectVisitor.super.visit(values);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(LateralSubSelect lateralSubSelect, S context) {
    return null;
  }

  @Override
  public void visit(LateralSubSelect lateralSubSelect) {
    SelectVisitor.super.visit(lateralSubSelect);
  }

  @Override
  public <S> JdbcResultSetMetaData visit(TableFunction tableFunction, S context) {
    return null;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(ParenthesedFromItem parenthesedFromItem, S context) {
    JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

    FromItem fromItem = parenthesedFromItem.getFromItem();
    try {
      resultSetMetaData.add(fromItem.accept(this, context));
    } catch (SQLException ex) {
      throw new RuntimeException("Failed on ParenthesedFromItem " + fromItem.toString(), ex);
    }

    List<Join> joins = parenthesedFromItem.getJoins();
    if (joins != null && !joins.isEmpty()) {
      for (Join join : joins) {
        try {
          resultSetMetaData.add(join.getFromItem().accept(this, context));
        } catch (SQLException ex) {
          throw new RuntimeException("Failed on Join " + join.getFromItem().toString(), ex);
        }
      }
    }
    return resultSetMetaData;
  }

  @Override
  public <S> JdbcResultSetMetaData visit(TableStatement tableStatement, S context) {
    return null;
  }

}
