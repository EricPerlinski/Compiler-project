package plic;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import model.TDS;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;

import asm.AsmGenerator;

import parser_tools.SemanticChecker;
import parser_tools.TdsBuilder;
import dot.Tds2img;
import dot.Tree2img;

public class Test {

    public static void main(String[] args) throws Exception {

        /*
         * ANALYSE SYNTAXIQUE
         */

        // Stream à modifier plus tard par System.in
        FileInputStream stream = null;
        String name = null;
        if (args.length == 1) {
            stream = new FileInputStream(args[0]);
            String tmp[] = args[0].split("/");
            name = tmp[tmp.length - 1];
            // name=(name.split("."))[0];
        } else {
            stream = new FileInputStream("test/correct/test-sujet-simplifie.plic");
            name = "test-sujet-simplifie";
        }

        ANTLRInputStream input = new ANTLRInputStream(stream);

        // Mise en place du plicLexer et plicParser
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);

        PlicParser.root_return r = parser.root();

        int nbError = parser.getNumberOfSyntaxErrors();
        if (nbError > 0) {
            System.out.println("\033[31m*****  Il y a " + nbError + " erreur" + (nbError > 1 ? "s" : "")
                    + " *****\033[0m");
            return;
        }

        // recuperation de l'AST
        CommonTree ast = (CommonTree) r.getTree();
        Tree2img.run(ast, name);

        // Création du parseur d'AST pour la création des TDS
        TdsBuilder tdsBuilder = new TdsBuilder(ast);
        // recuperation de la TDS
        TDS tds = tdsBuilder.getTds();
        tdsBuilder.ASTParse();

        // TEST
        Tds2img.dotWrite(tdsBuilder.getCurrent().toDot(), name);
        // System.out.println(tdsBuilder.toString());

        // tdsBuilder.getCurrent().afficherTds();

        // ANALYSE SEMANTIQUE
        boolean errCheck = SemanticChecker.check(ast, tds);
        if(errCheck){
        	int nbErr = SemanticChecker.getNbErrors();
        	int nbWarn = SemanticChecker.getNbWarn();
        	System.out.println(SemanticChecker.getErrors());
        	System.out.print("===> Please correct your ");
        	if(nbErr>0){
        		System.out.print("\033[31m"+nbErr+"\033[0m Errors ");
        	}
        	if(nbWarn>0){
        		System.out.print("\033[33m"+nbErr+"\033[0m Warnings");
        	}
        	System.out.println(".");
        	System.exit(1);
        }
        
        
        //ici le code est bon :D
        //Lancement de la generation de code
        
        AsmGenerator asm = new AsmGenerator(name, ast, tds);
        boolean errGen = asm.generate();
        if(errGen){
        	System.out.println("Erreur de generation de code");
        }else{
        	//System.out.println(asm.getCode());
        	writeAsm(name, asm.getCode().toString());
        }    
    }
    
    
    
    private static void writeAsm(String name, String code){
    	BufferedWriter outputFile;
		try {
			outputFile = new BufferedWriter(new FileWriter("asm/src/"+name+".s"));
			outputFile.write(code);
	    	outputFile.close();
		} catch (IOException e) {
			System.out.println("Erreur ecriture fichier assembleur");
			e.printStackTrace();
		}
    	
    }

}
