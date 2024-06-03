package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;

public class JSQLUpdateTranspiler extends UpdateDeParser {

    JSQLUpdateTranspiler(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.setExpressionVisitor(expressionVisitor);
    }
}
