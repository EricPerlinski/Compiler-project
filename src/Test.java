import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
public class Test {
    public static void main(String[] args) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(System.in);
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();
        CommonTree t = (CommonTree)r.getTree();
        System.out.println(t.toStringTree());
    }
}
