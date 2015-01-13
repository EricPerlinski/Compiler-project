import org.antlr.runtime.*;

public class Test {
    public static void main(String[] args) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(System.in);
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        parser.prog();
    }
}
