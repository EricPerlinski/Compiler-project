import java.io.FileInputStream;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import parser_tools.ASTParser;
import java.io.*;


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
            stream=new FileInputStream("test/correct/test2.plic");    
        }
        
        ANTLRInputStream input = new ANTLRInputStream(stream);
        
        // Mise en place du plicLexer et plicParser
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();

        CommonTree t = (CommonTree)r.getTree();
        Tree2img.run(t,name);

        // Création du parseur d'AST pour la création des TDS
        ASTParser astParser = new ASTParser(t);
        astParser.ASTParse();
        
        // TEST
        dotWrite(astParser.getCurrent().toDot(),name);
        //System.out.println(astParser.toString());
        
        //astParser.getCurrent().afficherTds();
    }

    private static void dotWrite(String src, String name){
        FileWriter fw =null;
        BufferedWriter outputFile=null;
        try{
            String name_output=null;
            if(name==null){
                name_output="tds/input/output.dot";
            }else{
                name_output="tds/input/"+name+".dot";
            }

            fw = new FileWriter(name_output, false);         
            outputFile = new BufferedWriter(fw);
            outputFile.write(src);
            outputFile.flush();
            outputFile.close();
            System.out.println("fichier TDS "+name_output+" créé");
            fw.close();
            outputFile.close();
        }
        catch(Exception ioe){
            System.out.print("Erreur : ");
            ioe.printStackTrace();
        }
    }
}
