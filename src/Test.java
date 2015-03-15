import java.io.FileInputStream;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import parser_tools.ASTParser;


public class Test {
	
	public static void main(String[] args) throws Exception {
	
		// Stream à modifier plus tard par System.in
		FileInputStream stream=null;
        String name=null;
        if(args.length==1){
            stream=new FileInputStream(args[0]);
            String tmp[]=args[0].split("/");
            name=tmp[tmp.length-1];
            //name=(name.split("."))[0];
        }else{
            stream=new FileInputStream("test/correct/test-sujet.plic");    
        }
        
        ANTLRInputStream input = new ANTLRInputStream(stream);
        
        // Mise en place du plicLexer et plicParser
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();

        CommonTree t = (CommonTree)r.getTree();
        Tree2img.run(t.toStringTree(),name);

        // Création du parseur d'AST pour la création des TDS
        ASTParser astParser = new ASTParser(t);
        astParser.ASTParse();
        
        // TEST
        astParser.getCurrent().afficherTds();
        //System.out.println(astParser.toString());
    }
}
