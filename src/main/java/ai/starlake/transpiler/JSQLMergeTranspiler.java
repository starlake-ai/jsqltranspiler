package ai.starlake.transpiler;

import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.MergeDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class JSQLMergeTranspiler extends MergeDeParser {
    public JSQLMergeTranspiler(ExpressionDeParser expressionDeParser,
                               SelectDeParser selectDeParser,
                               StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }
}
