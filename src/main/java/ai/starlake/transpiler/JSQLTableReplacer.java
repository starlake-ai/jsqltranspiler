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

public class JSQLTableReplacer {
  private final CaseInsensitiveLinkedHashMap<String> replaceTables =
      new CaseInsensitiveLinkedHashMap<>();
  private JSQLResolver resolver;

  public JSQLTableReplacer(JdbcMetaData metaData) {
    resolver = new JSQLResolver(metaData);
  }

  public JSQLTableReplacer(String currentCatalogName, String currentSchemaName,
      String[][] metaDataDefinition) {
    resolver = new JSQLResolver(currentCatalogName, currentSchemaName, metaDataDefinition);
  }

  public JSQLTableReplacer(String[][] metaDataDefinition) {
    resolver = new JSQLResolver(metaDataDefinition);
  }

  public void clearReplaceTables() {
    replaceTables.clear();
  }

  public CaseInsensitiveLinkedHashMap<String> getReplaceTables() {
    return replaceTables;
  }

  public JSQLTableReplacer putReplaceTables(Map<String, String> replaceTables) {
    this.replaceTables.putAll(replaceTables);
    return this;
  }

  public JSQLTableReplacer putReplacementTable(String qualifiedTableName, String replacementName) {
    this.replaceTables.put(qualifiedTableName, replacementName);
    return this;
  }

  private ExpressionVisitorAdapter<Void> expressionVisitor = new ExpressionVisitorAdapter<>() {
    public <S> Void visit(Column column, S context) {
      Table table = column.getTable();
      if (table.getResolvedTable() != null
          && replaceTables.containsKey(table.getResolvedTable().getFullyQualifiedName())) {
        table.setName(replaceTables.get(table.getResolvedTable().getFullyQualifiedName()));
      }
      return null;
    }
  };

  private FromItemVisitorAdapter<Void> fromItemVisitor = new FromItemVisitorAdapter<>() {
    @Override
    public <S> Void visit(Table table, S context) {
      if (table.getResolvedTable() != null
          && replaceTables.containsKey(table.getResolvedTable().getFullyQualifiedName())) {
        table.setName(replaceTables.get(table.getResolvedTable().getFullyQualifiedName()));
      }
      return null;
    }
  };

  private PivotVisitorAdapter<Void> pivotVisitorAdapter =
      new PivotVisitorAdapter<>(expressionVisitor);

  private SelectItemVisitorAdapter<Void> selectItemVisitor =
      new SelectItemVisitorAdapter<>(expressionVisitor);

  private SelectVisitor<Void> selectVisitor = new SelectVisitorAdapter<>(expressionVisitor,
      pivotVisitorAdapter, selectItemVisitor, fromItemVisitor);

  private StatementVisitor<Void> statementVisitor = new StatementVisitorAdapter<>();

  {
    expressionVisitor.setSelectVisitor(selectVisitor);
  }

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

  public Statement replace(String sqlStr, Map<String, String> replacementTables)
      throws JSQLParserException {
    Statement st = CCJSqlParserUtil.parse(sqlStr);
    return replace(st, replacementTables);
  }

}
