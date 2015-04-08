package plic;

import java.io.FileInputStream;

import model.TDS;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import parser_tools.SemanticChecker;
import parser_tools.TdsBuilder;
import plic.PlicLexer;
import plic.PlicParser;
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
            stream = new FileInputStream("test/correct/test2.plic");
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
        System.out.println("--------------------------------");
        //System.out.println("\n\033[31m");
        boolean semCheck = SemanticChecker.check(ast, tds);
        //System.out.println("\033[0m");

    }

}
