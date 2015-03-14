import java.io.FileInputStream;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import parser_tools.ASTParser;

public class Test {
public static void main(String[] args) throws Exception {
		FileInputStream stream=new FileInputStream("test/correct/test-sujet.plic");
        ANTLRInputStream input = new ANTLRInputStream(stream);
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();
        ASTParser astparser = new ASTParser((CommonTree)r.getTree());
        
        System.out.println(astparser.toStringTree());
        
        
    }
}
