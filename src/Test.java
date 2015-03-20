import java.io.FileInputStream;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import parser_tools.*;
import java.io.*;
import dot.*;
import model.*;


public class Test {
	
	public static void main(String[] args) throws Exception {
	
        /*
            ANALYSE SYNTAXIQUE
        */

		// Stream à modifier plus tard par System.in
		FileInputStream stream=null;
        String name=null;
        if(args.length==1){
            stream=new FileInputStream(args[0]);
            String tmp[]=args[0].split("/");
            name=tmp[tmp.length-1];
            //name=(name.split("."))[0];
        }else{
            stream=new FileInputStream("test/correct/test2.plic");    
        }
        
        ANTLRInputStream input = new ANTLRInputStream(stream);
        
        // Mise en place du plicLexer et plicParser
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();

        // recuperation de l'AST
        CommonTree ast = (CommonTree)r.getTree();
        Tree2img.run(ast,name);

        // Création du parseur d'AST pour la création des TDS
        TdsBuilder tdsBuilder = new TdsBuilder(ast);
        //recuperation de la TDS
        TDS tds = tdsBuilder.getTds();
        tdsBuilder.ASTParse();
        
        // TEST
        Tds2img.dotWrite(tdsBuilder.getCurrent().toDot(),name);
        //System.out.println(tdsBuilder.toString());
        
        //tdsBuilder.getCurrent().afficherTds();




        // ANANLYSE SEMANTIQUE

    }

   
}
