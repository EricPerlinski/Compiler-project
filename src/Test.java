import java.io.FileInputStream;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import parser_tools.ASTParser;

public class Test {
	
	public static void main(String[] args) throws Exception {
	
		// Stream à modifier plus tard par System.in
		FileInputStream stream=new FileInputStream("test/correct/test-sujet.plic");
        ANTLRInputStream input = new ANTLRInputStream(stream);
        
        // Mise en place du plicLexer et plicParser
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();
        
        // Création du parseur d'AST pour la création des TDS
        ASTParser astParser = new ASTParser((CommonTree)r.getTree());
        astParser.ASTParse();
        
        // TEST
        System.out.println(astParser.toString());
    }
}
