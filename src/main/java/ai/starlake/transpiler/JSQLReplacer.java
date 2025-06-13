package ai.starlake.transpiler;

import ai.starlake.transpiler.schema.CaseInsensitiveLinkedHashMap;
import ai.starlake.transpiler.schema.JdbcMetaData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PivotVisitorAdapter;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

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
   * Instantiates a new JSQLReplacer for a given Database MetaData for an empty default Catalog and Schema.
   *
   * @param metaData the database meta data
   */
  public JSQLReplacer(JdbcMetaData metaData) {
    resolver = new JSQLResolver(metaData);
  }

  /**
   * Instantiates a new JSQLReplacer for a given Database MetaData.
   *
   * @param currentCatalogName the current catalog name
   * @param currentSchemaName  the current schema name
   * @param metaDataDefinition the meta data definition
   */
  public JSQLReplacer(String currentCatalogName, String currentSchemaName,
                      String[][] metaDataDefinition) {
    resolver = new JSQLResolver(currentCatalogName, currentSchemaName, metaDataDefinition);
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
   * @return the replace tables
   */
  public CaseInsensitiveLinkedHashMap<String> getReplaceTables() {
    return replaceTables;
  }

  /**
   * Put a map of table names into the map of tables to be replaced.
   *
   * @param replaceTables the replace tables
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
   * @param replacementName    the replacement name
   * @return the jsql replacer
   */
  public JSQLReplacer putReplacementTable(String qualifiedTableName, String replacementName) {
    this.replaceTables.put(qualifiedTableName, replacementName);
    return this;
  }

  private final ExpressionVisitorAdapter<Void> expressionVisitor = new ExpressionVisitorAdapter<>() {
    @Override
    public <S> Void visit(Column column, S context) {
      Table table = column.getTable();
      if (table.getResolvedTable() != null
          && replaceTables.containsKey(table.getResolvedTable().getFullyQualifiedName())) {
        String replacementName = replaceTables.get(table.getResolvedTable().getFullyQualifiedName());
        table.setName(replacementName);
      }
      return null;
    }
  };

  private final FromItemVisitorAdapter<Void> fromItemVisitor = new FromItemVisitorAdapter<>() {
    @Override
    public <S> Void visit(Table table, S context) {
      if (table.getResolvedTable() != null
          && replaceTables.containsKey(table.getResolvedTable().getFullyQualifiedName())) {
        table.setName(replaceTables.get(table.getResolvedTable().getFullyQualifiedName()));
      }
      return null;
    }
  };

  private final PivotVisitorAdapter<Void> pivotVisitorAdapter =
      new PivotVisitorAdapter<>(expressionVisitor);

  private final SelectItemVisitorAdapter<Void> selectItemVisitor =
      new SelectItemVisitorAdapter<>(expressionVisitor);

  private final SelectVisitor<Void> selectVisitor = new SelectVisitorAdapter<>(expressionVisitor,
                                                                               pivotVisitorAdapter, selectItemVisitor, fromItemVisitor);

  private final StatementVisitor<Void> statementVisitor = new StatementVisitorAdapter<>();

  {
    expressionVisitor.setSelectVisitor(selectVisitor);
    fromItemVisitor.setSelectVisitor(selectVisitor);
  }

  /**
   * Replace physically existing table names in a given statement.
   *
   * @param st                the statement
   * @param replacementTables the replacement tables
   * @return the modified statement with the replaced table names
   */
  public Statement replace(Statement st, Map<String, String> replacementTables) {
    this.replaceTables.clear();
    for (Entry<String, String> entry : replacementTables.entrySet()) {
      this.replaceTables.put(entry.getKey(), entry.getValue());
    }

    resolver.resolve(st);

    if (st instanceof Select) {
      Select select = (Select) st;
      select.accept(selectVisitor, null);
    } else {
      st.accept(statementVisitor);
    }

    return st;
  }

  /**
   * Replace physically existing table names in a given query.
   *
   * @param sqlStr            the query text
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
