package asm;

import java.util.Stack;

import model.Declaration;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class AsmGenerator {

    private String name;
    private Tree ast;
    private TDS tds;

    private StringBuffer asmBuff;

    private int uniqId;
    private int tab = 0;

    private static final int INT_SIZE = 2;
    private static final int BOOL_SIZE = 2;
    
    private Stack<Integer> stackIf;

    public AsmGenerator(String name, Tree ast, TDS tds) {
        this.ast = ast;
        this.tds = tds;
        this.name = name;
        this.uniqId = 0;
        asmBuff = new StringBuffer();
        stackIf=new Stack<Integer>();
    }

    private void emptyLine() {
        addCode("\n");
    }

    private void addTab() {
        tab++;
    }

    private void removeTab() {
        tab--;
    }

    public int getUniqId() {
        return uniqId++;
    }

    public StringBuffer getCode() {
        return asmBuff;
    }

    private void addCode(String code) {
        for (int i = 0; i < tab; i++) {
            asmBuff.append("\t");
        }
        asmBuff.append(code);
    }

    private void addCodeln(String code) {
        addCode(code);
        asmBuff.append("\n");
    }

    public boolean generate() {
        addCodeln("//Prog " + name);
        // modification nom des registres
        addCodeln("//renomage registres");
        addCodeln("SP EQU R15");
        addCodeln("WR EQU R14");
        addCodeln("BP EQU R13");
        addCodeln("EXIT_EXC EQU 64");
        addCodeln("READ_EXC EQU 65");
        addCodeln("WRITE_EXC EQU 66");
        addCodeln("STACK_ADRS EQU 0x1000");
        addCodeln("LOAD_ADRS EQU 0xF000");
        addCodeln("NIL EQU 0");
        // init du programme
        addCodeln("ORG LOAD_ADRS");
        addCodeln("START Root_");
        // creation pile
        addCodeln("stackSize equ 100");
        addCodeln("stack rsb stackSize");

        // caractere retour ligne
        

        write_number();
        
        addCodeln("Root_");

        addCodeln("n_ rsw 1");
        addCodeln("LDW R0, #n_");
        addCodeln("LDW R1, #2560");
        addCodeln("STW R1, (R0)");
        
        addCodeln("LDW SP, #STACK_ADRS");
        // addCodeln("LDW BP, SP");

        // 0 dans R0
        addCodeln("LDW R0, #0");
        // on met 0 pour l'@ de retour
        addCodeln("STW R0, -(SP)");
        // on met 0 pour le DYN
        addCodeln("STW R0, -(SP)");
        // on charge BP sur DYN
        addCodeln("LDW BP, SP");
        // on met 0 pour STATIC
        addCodeln("STW R0, -(SP)");

        // Generation du corps du programme
        generateRec(tds, ast, -1);

        // fin du programme
        addCodeln("fin");

        // fin du programme
        addCodeln("trp #64");
        return false;
    }

    public int generateRec(TDS tds, Tree ast, int truc) {
        // System.out.println(ast.getText());
        boolean bloc = false;

        // Label unique nécessaire pour les if et les boucles
        String label = null;
        int labelID = 0;
        int id;

        // NE PAS MODIFIER
        switch (ast.getType()) {
        case PlicParser.BLOC:
        case PlicParser.FUNCTION:
        case PlicParser.PROCEDURE:
            truc++;
            bloc = true;
            tds = tds.getFils().get(truc);
            break;
        }
        // FIN DE NE PAS MODIFIER

        // Generer de l'ASM suivant le type
        switch (ast.getType()) {
        case PlicParser.FUNCTION:
        case PlicParser.PROCEDURE:
            function(ast, tds);
            break;
        case PlicParser.BLOC:
            bloc(ast, tds);
            break;
        case PlicParser.VARIABLE:
            variable(ast, tds);
            break;
        case PlicParser.AFFECTATION:
            affectaction(ast, tds);
            break;
        case PlicParser.RETURN:
            retourne(ast, tds);
            break;
        case PlicParser.IF:
        	id=getUniqId();
        	stackIf.push(id);
        	if(ast.getChildCount()==2){
        		label = "end_if_" + id + "_";
                if_start(ast, tds, label);
        	}else if(ast.getChildCount()==3){
        		label = "else_" + id + "_";
                if_start(ast, tds, label);
        	}
            break;
        case PlicParser.ELSE_BLOC:
        	System.out.println("ELSE BLOCK");
        	id = (int)stackIf.peek();
        	else_bloc(ast,tds,id);
        	break;
        case PlicParser.FOR:
            labelID = getUniqId();
            boucle_start(ast, tds, labelID);
            break;
        case PlicParser.PROC_CALL:
            function_call(ast, tds);
            break;
        case PlicParser.WRITE:
            write(ast, tds);
            break;
        }
        // fin génération

        // boolean bloc=false;
        int res = (bloc ? -1 : truc);

        for (int i = 0; i < ast.getChildCount(); i++) {
            res = generateRec(tds, ast.getChild(i), res);
        }

        switch (ast.getType()) {
        case PlicParser.FUNCTION:
        case PlicParser.PROCEDURE:
            function_end(ast, tds);
            break;
        case PlicParser.BLOC:
            bloc_end(ast, tds);
            break;
        case PlicParser.IF:
        	id = (int)stackIf.pop();
        	label = "end_if_" + id + "_";
        	if_end(ast, tds, label);
            break;
        case PlicParser.FOR:
            boucle_end(ast, tds, labelID);
            break;
        }

        return (bloc ? truc : res);
    }

    public void function_call(Tree ast, TDS tds) {
        TDS tds_func = tds.getTdsOfFunction(ast.getChild(0).getText());

        // si le pere de la fct appelé c'est moi alors vrai
        boolean fils = tds_func.getPere().getIdf().equals(tds.getIdf());
        emptyLine();
        addCodeln("//debut appel fonction " + tds_func.getIdf());

        // Sauvegarde registres
        for (int i = 1; i <= 10; i++) {
            addCodeln("stw R" + i + ",-(SP)");
        }

        // Preparation de l'environnement du programme principal
        // empile les parametres de la fonction
        emptyLine();
        for (int i = tds_func.getParams().size() - 1; i >= 0; i--) {
            addCodeln("//empile " + tds_func.getParams().get(i).getIdf());
            boolean b = tds_func.getParams().get(i).getType() == Type.bool;
            expr(ast.getChild(i + 1), tds, b);
            addCodeln("STW R0, -(SP)");
        }

        emptyLine();
        addCodeln("//chainage static");
        if (fils) {
            // le chainage static pointe sur moi
            addCodeln("//c est un fils, donc je serai sont chainage static");
            addCodeln("LDW R2, BP");
        } else {
            // le chainage static pointe sur notre pere
            addCodeln("//c est un frere, donc meme valeur de chainage static");
            addCodeln("LDW R2, (BP)-2");
        }

        // execution fct
        emptyLine();
        addCodeln("//appel fct");
        addCodeln("JSR @" + ast.getChild(0).getText() + "_");

        // Fin de la fonction
        emptyLine();
        for (int i = 0; i < tds_func.getParams().size(); i++) {
            addCodeln("//depile " + tds_func.getParams().get(i).getIdf());
            addCodeln("LDW R1, (SP)+");
        }

        // equivalent au truc du dessus
        // nettoyage de la pile par le programme appelant
        // addCodeln("ADQ "+tds_func.getSizeOfParams()+",SP");

        // restauration des registres
        emptyLine();
        for (int i = 10; i > 0; i--) {
            addCodeln("ldw R" + i + ",(SP)+");
        }
        emptyLine();
        addCodeln("//fin appel fonction " + tds_func.getIdf());

    }

    private void bloc(Tree ast, TDS tds) {
        addCodeln("//debut bloc");
        // Sauvegarde registres
        for (int i = 1; i <= 10; i++) {
            addCodeln("stw R" + i + ",-(SP)");
        }

        emptyLine();
        addCodeln("//chainage static");
        // sauvegarde de la base / DYN
        addCodeln("STW BP,-(SP)");
        // chainage static
        addCodeln("STW BP,-(SP)");
        addCodeln("LDW BP, SP");

        addCodeln("//corps bloc");
    }

    private void bloc_end(Tree ast, TDS tds) {
        emptyLine();
        addCodeln("//fin corps bloc");
        addCodeln("LDW SP, BP"); // charge SP avec contenu de BP: abandon infos
                                 // locales
        addCodeln("LDW BP, (SP)"); // charge BP avec ancien BP
        addCodeln("ADQ 2,SP"); // supprime l'ancien BP de la pile

        // restauration des registres
        emptyLine();
        for (int i = 10; i > 0; i--) {
            addCodeln("ldw R" + i + ",(SP)+");
        }
        emptyLine();
        addCodeln("//fin bloc");
    }

    private void function(Tree ast, TDS tds) {
        addTab();
        emptyLine();
        if (tds.getTypeRet().equalsIgnoreCase("void")) {
            addCodeln("//procedure " + tds.getIdf());
        } else {
            addCodeln("//fonction " + tds.getIdf());
        }
        // saut à la fin de la fct
        addCodeln("JMP #end_" + tds.getIdf() + "_ -$-2");
        // etiquette de fonction
        addCodeln(tds.getIdf() + "_");

        // sauvegarde de la base / DYN
        addCodeln("STW BP,-(SP)");
        addCodeln("LDW BP, SP");

        // chainage static, R2 est definie par l'appelant
        addCodeln("STW R2, -(SP)");

        // la methode variable() fait ça normalement
        // addCodeln("LDQ "+tds.getSizeOfVar()+",R1"); // R1 = taille donnees
        // locales de fonction appelee
        // addCodeln("SUB SP, R1, SP"); //reserve R1 octets sur la pile pour les
        // variables locales

        // debut corps fonction
        emptyLine();
        addCodeln("//Corps de la fonction");

    }

    private void function_end(Tree as, TDS tds) {
        addCodeln("end_" + tds.getIdf() + "_RTS_");

        emptyLine();
        if (tds.getTypeRet().equalsIgnoreCase("void")) {
            addCodeln("//fin corps de la procedure");
        } else {
            addCodeln("//fin corps de la fonction");
        }

        // fin de la fonction
        addCodeln("LDW SP, BP"); // charge SP avec contenu de BP: abandon infos
                                 // locales
        addCodeln("LDW BP, (SP)"); // charge BP avec ancien BP
        addCodeln("ADQ 2,SP"); // supprime l'ancien BP de la pile

        addCodeln("RTS"); // retour au programme appelant
        // label de fin de fct
        addCodeln("end_" + tds.getIdf() + "_");
        removeTab();
        emptyLine();
    }

    private void variable(Tree ast, TDS tds) {
        if (ast.getChild(0).getText().equalsIgnoreCase("integer")) {
            emptyLine();
            addCodeln("//integer");
            for (int i = 1; i < ast.getChildCount(); i++) {
                addCodeln("ADQ -" + INT_SIZE + ", SP //var " + ast.getChild(i).getText());
            }
        } else if (ast.getChild(0).getText().equalsIgnoreCase("boolean")) {
            emptyLine();
            addCodeln("//boolean");
            for (int i = 1; i < ast.getChildCount(); i++) {
                addCodeln("ADQ -" + BOOL_SIZE + ", SP //var " + ast.getChild(i).getText());
            }
        } else if (ast.getChild(0).getText().equalsIgnoreCase("array")) {
            emptyLine();
            addCodeln("//array");
            Declaration decl = tds.getDeclarationOfVar(ast.getChild(1).getText());
            addCodeln("LDW R1, #"+decl.getSize());
            addCodeln("SUB SP, R1, SP //var array " + ast.getChild(1).getText());
        }
    }
    
    private void pushVarOnR0(Tree ast, TDS tds, boolean adr){
    	String idf;

        if (ast.getText().equalsIgnoreCase("array")) { // array
            idf = ast.getChild(0).getText();
        } else { // int ou bool
            idf = ast.getText();
        }
        Declaration decl = tds.getDeclarationOfVar(idf);
        
        
        int deep = tds.getDeepOfVar(idf);
        // une variable, on le charge depuis la pile
        int depl = decl.getDeplacement();
        // chainage statique
        addCodeln("LDW WR, BP");
        while (deep > 0) {
            // on parcours le chainage static
            addCodeln("LDW WR, (WR)");
            deep--;
        }
        
        if (decl.getType() == Type.bool || decl.getType() == Type.integer) {
        
        	if(!adr){
        		addCodeln("LDW R0, (WR)" + depl+" //"+decl.getIdf());
        	}else{
        		addCodeln("ADQ "+depl+", WR //"+decl.getIdf());
        		addCodeln("STW WR, R0");
        	}
        } else {
        	
            addCodeln("//Array Depl");
            //addCodeln("LDW R9, WR"); // R0 <- tête du tableau
            addCodeln("ADQ "+depl+", WR");
            addCodeln("LDW R2, #0"); // Mettre 0 dans R2
            for (int i = 1; i < ast.getChildCount() - 1; i++) {
            	addCodeln("STW WR, -(SP)");
                expr(ast.getChild(i), tds, false); // borne i dans R0
                addCodeln("LDW WR, (SP)+");
                int dim =1;
                for(int j=i;j< ast.getChildCount() - 1;j++){
                	dim *= decl.getBound(j).getDim();
                }
                
                addCodeln("LDW R1, #"+decl.getBound(i-1).getLb());
                addCodeln("SUB R0, R1, R0");
                
                addCodeln("LDW R1, #" + dim);
                addCodeln("MUL R1, R0, R1");
                addCodeln("SUB WR, R1, WR");
                addCodeln("SUB WR, R1, WR");
                
            }
            addCodeln("STW WR, -(SP)");
            addCodeln("//debut expr");
            expr(ast.getChild(ast.getChildCount() - 1), tds, false); // dernière borne dans R0
            addCodeln("//fin expr");
            addCodeln("LDW WR, (SP)+");
            addCodeln("LDW R1, #"+decl.getBound(decl.getBounds().size()-1).getLb());
            addCodeln("SUB R0, R1, R0");
            
            addCodeln("SUB WR, R0, WR");
            addCodeln("SUB WR, R0, WR");
            addCodeln("ADQ -2, WR");
            if(!adr){
        		addCodeln("LDW R0, (WR)");
        	}else{
        		addCodeln("LDW R0, WR");
        	}
            addCodeln("//fin Array Depl");
        }
    }

    private void affectaction(Tree ast, TDS tds) {
        emptyLine();
        addCodeln("//****** affectation");
        Tree left = ast.getChild(0).getChild(0);
        Tree right = ast.getChild(1).getChild(0);
        
        Declaration decl;
		if (left.getText().equalsIgnoreCase("array")) { // array
            decl = tds.getDeclarationOfVar(left.getChild(0).getText());
        } else { // int ou bool
            decl = tds.getDeclarationOfVar(left.getText());
        }
        boolean bool = (decl.getType() == Type.bool);
        
        expr(right, tds, bool);
        
        addCodeln("STW R0, -(SP)");
        pushVarOnR0(left, tds, true);
        addCodeln("LDW R1, (SP)+");
        addCodeln("STW R1, (R0)");
    }

    /*
     * Permet de calculer le resultat d'une expression bool/arithmetique.
     * On lui donne le noeud le plus haut dans l'expression, la tds
     * correspondante
     * Le parametre bool doit être à vrai si le resultat est mis dans un boolean
     * (une variable, un parametre de fct, un if/while, ...)
     * si bool est a vrai il transformera systematiquement tout ce qui est
     * different de 0 en 1. Et le 0 reste 0 :D
     * 
     */
    private void expr(Tree ast, TDS tds, boolean bool) {
        // met le resultat de l'exp dans R0
        expr_rec(ast, tds, -1, bool);
    }

    private void expr_rec(Tree ast, TDS tds, int num_fils, boolean bool) {
        if (ast.getChildCount() == 0) {
            // bas de l'arbre
            if (ast.getText().equalsIgnoreCase("true") || ast.getText().equalsIgnoreCase("false")) {
                boolean b = ast.getText().equalsIgnoreCase("true");
                addCodeln("LDW R0, #" + (b ? "1" : "0"));
                if (num_fils != -1) {
                    addCodeln("STW R0, -(SP)");
                }
            } else if (ast.getText().matches("^\\p{Digit}+$")) {
                // un nombre on le met dans le registre directement
                addCodeln("LDW R0, #" + ast.getText());
                if (num_fils != -1) {
                    addCodeln("STW R0, -(SP)");
                }
            } else if (tds.getDeclarationOfVar(ast.getText()) != null || ast.getText().equalsIgnoreCase("array")) {
            	pushVarOnR0(ast,tds,false);
                if (num_fils != -1) {
                    addCodeln("STW R0, -(SP)");
                }
            } else {
                // erreur ?
                System.out.println("ERREUR");
            }
        } else {
            if (ast.getType() == PlicParser.FUNC_CALL) {
                // generation de l appel de fonction
                function_call(ast, tds);
                if (num_fils != -1) {
                    addCodeln("STW R0, -(SP)");
                }
            } else if(ast.getType() == PlicParser.ARRAY){
            	pushVarOnR0(ast,tds,false);
                if (num_fils != -1) {
                    addCodeln("STW R0, -(SP)");
                }
        	}else{
                for (int i = 0; i < ast.getChildCount(); i++) {
                    // parcours recursif sur chacun des fils
                    expr_rec(ast.getChild(i), tds, i, bool);
                }

                // Si c'est une operation +/-/*
                if (ast.getText().equalsIgnoreCase("+") || ast.getText().equalsIgnoreCase("-") || ast.getText().equalsIgnoreCase("*")) {
                    char c = ast.getText().charAt(0);
                    // ternaire de la mort :P
                    String opp = (c == '+' ? "ADD" : (c == '-' ? "SUB" : "MUL"));
                    addCodeln("LDW R1, (SP)+");
                    addCodeln("LDW R2, (SP)+");
                    addCodeln(opp + " R2, R1, R" + (num_fils + 1));
                    if (bool) {
                        // si c'est un bool on met le resultat à 0 ou 1
                        astuceBool(num_fils);
                    }
                    if (num_fils != -1) {
                        addCodeln("STW R" + (num_fils + 1) + ", -(SP)");
                    }

                    // unaire
                } else if (ast.getText().equalsIgnoreCase("unaire")) {
                	addCodeln("//UNAIRE");
                    if (bool) {
                        // si c'est un bool on met le resultat à 0 ou 1
                        addCodeln("LDW R" + (num_fils + 1) + ", (SP)+");
                        // on enleve un (0->-1, 1->0)
                        addCodeln("ADI R" + (num_fils + 1) + ", R" + (num_fils + 1) + ", #-1");
                        // on met le resultat à 0 ou 1
                        astuceBool(num_fils);
                    } else {
                        addCodeln("LDW R1, (SP)+");
                        addCodeln("NEG R1, R" + (num_fils + 1));
                    }
                    if (num_fils != -1) {
                        addCodeln("STW R0, -(SP)");
                    }
                    addCodeln("//fin unaire");

                    // comparaison
                } else if (ast.getText().equalsIgnoreCase("==") || ast.getText().equalsIgnoreCase("!=") || ast.getText().equalsIgnoreCase(">")
                        || ast.getText().equalsIgnoreCase(">=") || ast.getText().equalsIgnoreCase("<") || ast.getText().equalsIgnoreCase("<=")) {
                    String opp = "";
                    if (ast.getText().equalsIgnoreCase("==")) {
                        opp = "JEQ";
                    } else if (ast.getText().equalsIgnoreCase("!=")) {
                        opp = "JNE";
                    } else if (ast.getText().equalsIgnoreCase(">")) {
                        opp = "JGT";
                    } else if (ast.getText().equalsIgnoreCase(">=")) {
                        opp = "JGE";
                    } else if (ast.getText().equalsIgnoreCase("<")) {
                        opp = "JLW";
                    } else if (ast.getText().equalsIgnoreCase("<=")) {
                        opp = "JLE";
                    }
                    addCodeln("LDW R1, (SP)+");
                    addCodeln("LDW R2, (SP)+");
                    addCodeln("CMP R2, R1");
                    int id = getUniqId();
                    String label = "equals_" + id + "_";
                    addCodeln(opp + " #" + label + " -$-2");
                    addCodeln("LDW R0, #0");
                    addCodeln("JMP #end_" + label + " -$-2");
                    addCodeln(label);
                    addCodeln("LDW R0, #1");
                    addCodeln("end_" + label);
                }
            }
        }
    }

    private void astuceBool(int numR) {
        // permet de mettre le registre numR+1 à 1 si != 0
        addCodeln("//astuce pour les boolean, si c est different de zero alors on met à 1");
        String label = "bool_" + getUniqId() + "_";
        addCodeln("STW R3, -(SP)");
        addCodeln("LDW R3, #0");
        addCodeln("CMP R" + (numR + 1) + ", R3");
        addCodeln("JEQ #" + label + " -$-2");
        addCodeln("LDW R" + (numR + 1) + ", #1");
        addCodeln("LDW R3, (SP)+");
        addCodeln(label);
        addCodeln("//fin de petite astuce");
    }

    private void retourne(Tree ast, TDS tds) {
        if(TDS.str2type(tds.getTypeRet()) == Type.bool) {
            expr(ast.getChild(0), tds, true);
        } else if (TDS.str2type(tds.getTypeRet()) == Type.integer) {
            expr(ast.getChild(0), tds, false);
        }
        addCodeln("JMP #end_" + tds.getIdf() + "_RTS_ -$-2");
    }

    public void if_start(Tree ast, TDS tds, String label) {
    	
        emptyLine();
        addCodeln("// Structure IF");
        addTab();
        expr(ast.getChild(0), tds, true);
        // addCodeln("STW R1, -(SP)");
        addCodeln("LDW R1, #0");
        addCodeln("CMP R0, R1");
        // addCodeln("LDW R1, (SP)+");
        addCodeln("JEQ #" + label + " -$-2");
    }
    
    private void else_bloc(Tree ast, TDS tds, int id){
    	addCodeln("// Structure ELSE");
    	addCodeln("JMP #end_if_"+id+"_ -$-2");
    	removeTab();
    	addCodeln("else_"+id+"_");
    	addTab();
    	
    }

    public void if_end(Tree ast, TDS tds, String label) {
    	removeTab();
        addCodeln(label);
    }

    public void boucle_start(Tree ast, TDS tds, int labelID) {
    	addTab();
        emptyLine();
        addCodeln("// Structure de boucle FOR");
        // On affecte la valeur d'initialisation à la variable d'incrémentation
        expr(ast.getChild(1), tds, false);
        addCodeln("STW R0, R6");
        pushVarOnR0(ast.getChild(0),tds,true);
        addCodeln("STW R6, (R0)");
        // On stocke la valeur de la variable d'incrément dans R1
        addCodeln("start_boucle_" + labelID + "_");
        // On récupète la valeur de la borne supérieure de la boucle
        expr(ast.getChild(2), tds, false);
        addCodeln("STW R0, R6");
        pushVarOnR0(ast.getChild(0),tds,false);
        addCodeln("CMP R0, R6");
        addCodeln("JGT #end_boucle_" + labelID + "_ -$-2");
    }

    public void boucle_end(Tree ast, TDS tds, int labelID) {
    	addCodeln("//fin boucle");
    	pushVarOnR0(ast.getChild(0), tds, true);
        addCodeln("LDW R1, (R0)");
        addCodeln("ADQ 1, R1");
        addCodeln("STW R1, (R0)");
        addCodeln("JMP #start_boucle_" + labelID + "_ -$-2");
        addCodeln("end_boucle_" + labelID + "_");
        removeTab();
    }

    private void write(Tree ast, TDS tds) {
        if (ast.getChild(0).getText().charAt(0) == '"') {
            // c'est une cst string
            String str = ast.getChild(0).getText();
            String name = "str_" + getUniqId() + "_";
            addCodeln(name + " string " + str);
            addCodeln("LDW R0, #" + name);
            addCodeln("TRP #66");
        } else {
            // c'est une expr -> atoi ^^
            addCodeln("ldw r0, #10 ");      // charge 10 (pour base décimale) dans r0
            addCodeln("stw r0, -(sp)");     // empile contenu de r0 (paramètre b)
            addCodeln("adi bp, r0, #-8 ");  // r0 = bp - 8 = adresse du tableau text
            addCodeln("stw r0, -(sp) ");    // empile contenu de r0 (paramètre p)
            expr(ast, tds, false);
            addCodeln("stw r0, -(sp)  ");   // empile contenu de r0 (paramètre i)
            addCodeln("jsr @itoa_     ");   // appelle fonction itoa d'adresse itoa_
            addCodeln("adi sp, sp, #6 ");
            addCodeln("TRP #66");
        }
        addCodeln("LDW R0, #n_");
        addCodeln("TRP #66");
    }

    private void write_number() {
        // FONCTIONS PRé-DéFINIES EN LANAGAGE D'ASSEMBLAGE

        // char *itoa(int i, char *p, int b);
        //
        // i entier à convertir
        // p pointeur du tampon déjà alloué en mémoire où copier la chaîne de
        // caractères
        // b base de numération utilisée (de 2 à 36 inclus car il n'y a que 36
        // chiffres; par exemple: 2, 4, 8, 10, 16)
        //
        // Convertit un entier en chaîne de caractères codée en ASCII
        // (cette fonction fait partie de la bibliothèque standard portable C
        // stdlib et est normalement écrite en C).
        // Limitation ici: b doit être pair.
        // Retourne le pointeur sur la chaîne de caractère
        //
        // Ce programme terminera automatiquement la chaîne de caractères par
        // NUL;
        // le tampon devrait avoir une taille suffisante (par exemple
        // sizeof(int)*8+1 octets pour b=2)
        // Si la base = 10 et que l'entier est négatif la chaîne de caractères
        // est précédée d'un signe moins (-);
        // pour toute autre base, la valeur i est considérée non signée.
        // Les 36 chiffres utilisables sont dans l'ordre: 0, 1, 2,..., 9, A, B,
        // C, ... , Z .
        // Aucune erreur n'est gérée.
    	emptyLine();
    	emptyLine();
    	addCodeln("sp          equ r15");
    	addCodeln("wr          equ r14");
    	addCodeln("bp          equ r13");
    	addCodeln("NUL         equ  0 ");
    	
    	
    	
    	
    	
        addCodeln("ITOA_I      equ 4");      // offset du paramètre i
        addCodeln("ITOA_P      equ 6");      // offset du paramètre p
        addCodeln("ITOA_B      equ 8 ");     // offset du paramètre b

        addCodeln("ASCII_MINUS equ 45 ");    // code ASCII de -
        addCodeln("ASCII_PLUS  equ 43 ");    // code ASCII de +
        addCodeln("ASCII_SP    equ 32");     // code ASCII d'espace SP
        addCodeln("ASCII_0     equ 48 ");    // code ASCII de zéro (les autres
                                          // chiffres jusqu'à 9 suivent dans
                                          // l'ordre)
        addCodeln("ASCII_A     equ 65");     // code ASCII de A (les autres lettres
                                         // jusqu'à Z suivent dans l'ordre
                                         // alphabétique)

        // LNK: crée environnement du main pour permettre des variables locales
        // mais sans encore les réserver
        addCodeln("itoa_   stw bp, -(sp)");
        addCodeln("ldw bp, sp");

        // récupération des paramètres depuis pile vers registres
        addCodeln("ldw r0, (bp)ITOA_I    // r0 = i    ");
        addCodeln("ldw r1, (bp)ITOA_B    // r1 = b");

        // gère le signe: normalement itoa gère des int c'est à dire des entiers
        // signés,
        // mais en fait seulement pour b=10;
        // dans ce cas calcule le signe dans r3 et charge r0 avec la valeur
        // absolue de i
        addCodeln("ldq ASCII_SP, r3 ");     // code ASCII de espace (SPace) -> r3
        addCodeln("ldq 10, wr          ");  // 10 -> wr
        addCodeln("cmp r1, wr");            // charge les indicateurs de b - 10
        addCodeln("bne NOSIGN-$-2        ");// si non égal (donc si b != 10)
                                            // saute en NOSIGN, sinon calcule
                                            // signe
        addCodeln("ldq ASCII_PLUS, r3");    // charge le code ASCII du signe plus +
                                         // dans r3
        addCodeln("tst r0               "); // charge les indicateurs de r0 et
                                            // donc de i
        addCodeln("bge POSIT-$-2       ");  // saute en POSIT si i >= 0
        addCodeln("neg r0, r0           "); // change le signe de r0
        addCodeln("ldq ASCII_MINUS, r3");   // charge le code ASCII du signe moins
                                          // - dans r3
        addCodeln("POSIT   NOP ");                  // r3 = code ASCII de signe: SP pour aucun, -
                                   // ou +

        // convertit l'entier i en chiffres et les empile de droite à gauche
        addCodeln("NOSIGN  ldw r2, r0 ");           // r2 <- r0
        addCodeln("CNVLOOP ldw r0, r2      ");      // r0 <- r2

        // effectue "créativement" la division par b supposé pair (car
        // l'instruction div est hélas signée ...)
        // d=2*d' , D = d * q + r , D = 2*D'+r" , D' = d' * q + r' => r =
        // 2*r'+r"
        // un bug apparaît avec SRL R0, R0 avec R0 = 2 : met CF à 1 !!
        addCodeln("srl r1, r1           "); // r1 = b/2
        addCodeln("ani r0, r4, #1      ");  // ANd Immédiate entre r0 et 00...01
                                           // vers r4:
        // bit n°0 de r0 -> r4; r4 = reste" de r0/2
        addCodeln("srl r0, r0           "); // r0 / 2 -> r0
        addCodeln("div r0, r1, r2        ");// quotient = r0 / r1 -> r2, reste'
                                            // = r0 % r1 -> r0
        addCodeln("shl r0, r0            ");// r0 = 2 * reste'
        addCodeln("add r0, r4, r0        ");// r0 = reste = 2 * reste' + reste"
                                            // => r0 = chiffre
        addCodeln("shl r1, r1            ");// r1 = b

        addCodeln("adq -10, r0     ");      // chiffre - 10 -> r0
        addCodeln("bge LETTER-$-2       "); // saute en LETTER si chiffre >= 10
        addCodeln("adq 10+ASCII_0, r0");    // ajoute 10 => r0 = chiffre, ajoute
                                         // code ASCII de 0
        // => r0 = code ASCII de chiffre
        addCodeln("bmp STKCHR-$-2");        // saute en STKCHR

        addCodeln("LETTER  adq ASCII_A, r0");       // r0 = ASCII(A) pour chiffre =
                                              // 10, ASCII(B) pour 11 ...
        // ajoute code ASCII de A => r = code ASCII de chiffre
        addCodeln("STKCHR  stw r0, -(sp)  ");       // empile code ASCII du chiffre
        // (sur un mot complet pour pas désaligner pile)
        addCodeln("tst r2              ");  // charge les indicateurs en fonction
                                           // du quotient ds r2)
        addCodeln("bne CNVLOOP-$-2  ");     // boucle si quotient non nul; sinon sort

        // les caractères sont maintenant empilés : gauche en haut et droit en
        // bas

        // recopie les caractères dans le tampon dans le bon ordre: de gauche à
        // droite
        addCodeln("ldw r1, (bp)ITOA_P ");   // r1 pointe sur le début du tampon
                                          // déjà alloué
        addCodeln("stb r3, (r1)+        "); // copie le signe dans le tampon
        addCodeln("CPYLOOP ldw r0, (sp)+   ");      // dépile code du chiffre gauche
                                               // (sur un mot) dans r0
        addCodeln("stb r0, (r1)+        "); // copie code du chiffre dans un
                                            // Byte du tampon de gauche à droite
        addCodeln("cmp sp, bp        ");    // compare sp et sa valeur avant
                                         // empilement des caractères qui était
                                         // bp
        addCodeln("bne CPYLOOP-$-2 ");      // boucle s'il reste au moins un chiffre
                                       // sur la pile
        addCodeln("ldq NUL, r0     ");      // charge le code du caractère NUL dans
                                       // r0
        addCodeln("stb r0, (r1)+  ");       // sauve code NUL pour terminer la chaîne
                                      // de caractères

        // termine
        addCodeln("ldw r0, (bp)ITOA_P");    // retourne le pointeur sur la chaîne
                                         // de caractères

        // UNLINK: fermeture de l'environnement de la fonction itoa
        addCodeln("ldw sp, bp ");           // sp <- bp : abandonne infos locales; sp
                                  // pointe sur ancinne valeur de bp
        addCodeln("ldw bp, (sp)+ ");        // dépile ancienne valeur de bp dans bp; sp
                                     // pointe sur adresse de retour

        addCodeln("rts ");                  // retourne au programme appelant
        
        emptyLine();
        emptyLine();
        // -----------------------------------------------------------------------------------------------------

    }
}
