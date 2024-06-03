package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.InsertDeParser;

public class JSQLInsertTranspiler extends InsertDeParser {

    JSQLInsertTranspiler(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.setExpressionVisitor(expressionVisitor);
        this.setSelectVisitor(selectVisitor);
    }
}
