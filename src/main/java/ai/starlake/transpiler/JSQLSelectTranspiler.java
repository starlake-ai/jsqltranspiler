package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.SelectDeParser;


public class JSQLSelectTranspiler extends SelectDeParser {
    /**
     * The Expression transpiler.
     */
    protected JSQLExpressionTranspiler expressionTranspiler;


    /**
     * Instantiates a new transpiler.
     */
    protected JSQLSelectTranspiler(JSQLExpressionTranspiler expressionTranspiler, StringBuilder resultBuilder) {
        super(expressionTranspiler, resultBuilder);
    }


    /**
     * Gets result builder.
     *
     * @return the result builder
     */
    public StringBuilder getResultBuilder() {
        return getBuffer();
    }

    public void visit(Top top) {
        // get the parent SELECT
        SimpleNode node = (SimpleNode) top.getASTNode().jjtGetParent();
        while (node.jjtGetValue() == null) {
            node = (SimpleNode) node.jjtGetParent();
        }
        PlainSelect select = (PlainSelect) node.jjtGetValue();

        // rewrite the TOP into a LIMIT
        select.setTop(null);
        select.setLimit(new Limit().withRowCount(top.getExpression()));
    }

    public void visit(TableFunction tableFunction) {
        String name = tableFunction.getFunction().getName();
        if (name.equalsIgnoreCase("unnest")) {
            PlainSelect select = new PlainSelect()
                                         .withSelectItems(new SelectItem<>(tableFunction.getFunction(), tableFunction.getAlias()));

            ParenthesedSelect parenthesedSelect =
                    new ParenthesedSelect().withSelect(select).withAlias(tableFunction.getAlias());

            visit(parenthesedSelect);
        } else {
            super.visit(tableFunction);
        }
    }

    public void visit(PlainSelect plainSelect) {
        // remove any DUAL pseudo tables
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            if (table.getName().equalsIgnoreCase("dual")) {
                plainSelect.setFromItem(null);
            }
        }
        super.visit(plainSelect);
    }

    public void visit(Table table) {
        String name = table.getName().toLowerCase();
        String aliasName = table.getAlias() != null ? table.getAlias().getName() : null;

        for (String[] keyword : JSQLExpressionTranspiler.KEYWORDS) {
            if (keyword[0].equals(name)) {
                table.setName("\"" + table.getName() + "\"");
                name = null;
                if (aliasName == null) {
                    break;
                }
            }

            if (keyword[0].equals(aliasName)) {
                table.getAlias().setName("\"" + table.getAlias().getName() + "\"");
                aliasName = null;
                if (name == null) {
                    break;
                }
            }
        }

        super.visit(table);
    }

    public void visit(SelectItem selectItem) {
        if (selectItem.getAlias() != null) {
            String aliasName = selectItem.getAlias().getName().toLowerCase();
            for (String[] keyword : JSQLExpressionTranspiler.KEYWORDS) {
                if (keyword[0].equals(aliasName)) {
                    selectItem.getAlias().setName("\"" + selectItem.getAlias().getName() + "\"");
                    break;
                }
            }
        }
        super.visit(selectItem);
    }
}
