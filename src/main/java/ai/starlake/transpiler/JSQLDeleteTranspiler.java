package ai.starlake.transpiler;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.util.deparser.DeleteDeParser;

public class JSQLDeleteTranspiler extends DeleteDeParser {
    JSQLDeleteTranspiler(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.setExpressionVisitor(expressionVisitor);
    }
}
