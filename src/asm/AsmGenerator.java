package asm;

import java.util.Stack;

import model.Declaration;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import parser_tools.SemanticChecker;
import plic.PlicParser;

public class AsmGenerator {

	private String name;
	private Tree ast;
	private TDS tds;

	private StringBuffer asmBuff;

	private int uniqId;

	
	
	private static final int INT_SIZE = 2;
	private static final int BOOL_SIZE = 2;

	public AsmGenerator(String name,Tree ast, TDS tds){
		this.ast=ast;
		this.tds=tds;
		this.name=name;
		this.uniqId=0;
		asmBuff=new StringBuffer();
	}

	public int getUniqId(){
		return uniqId++;
	}

	public StringBuffer getCode(){
		return asmBuff;
	}

	private void addCode(String code){
		asmBuff.append(code);
	}

	private void addCodeln(String code){
		addCode(code);
		asmBuff.append("\n");
	}

	public boolean generate(){
		addCodeln("//Prog "+name);
		//modification nom des registres
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
		//init du programme
		addCodeln("ORG LOAD_ADRS");
		addCodeln("START main_");
		//creation pile
		addCodeln("stackSize equ 100");
		addCodeln("stack rsb stackSize");

		addCodeln("\n\nmain_");

		
		addCodeln("LDW SP, #STACK_ADRS");
		//addCodeln("LDW BP, SP");
		
		//0 dans RO
		addCodeln("LDW R0, #0");
		//on met 0 pour l'@ de retour
		addCodeln("STW R0, -(SP)");
		//on met 0 pour le DYN
		addCodeln("STW R0, -(SP)");
		//on charge BP sur DYN
		addCodeln("LDW BP, SP");
		//on met 0 pour STATIC
		addCodeln("STW R0, -(SP)");
				
		
		generateRec(tds, ast, -1);
		
		//TODO a mettre au debut du main
		
		

		//debut du programme
		//addCodeln("main");
		//init de la pile
		//addCodeln("ldw R15,#(stack+stackSize)");



		//fin du programme
		addCodeln("fin");


		//fin du programme
		addCodeln("trp #64");
		return false;
	}

	public int generateRec(TDS tds, Tree ast, int truc){
		//System.out.println(ast.getText());
		boolean bloc=false;

		// NE PAS MODIFIER
		switch(ast.getType()){
		case PlicParser.BLOC:
		case PlicParser.FUNCTION:
		case PlicParser.PROCEDURE:
			truc++;
			bloc=true;
			tds = tds.getFils().get(truc);
			break;
		}
		// FIN DE NE PAS MODIFIER



		// Generer de l'ASM suivant le type
		switch(ast.getType()){
		case PlicParser.FUNCTION:
			function(ast,tds);
			break;
		case PlicParser.VARIABLE:
			variable(ast, tds);
			break;
		case PlicParser.AFFECTATION:
			affectaction(ast,tds);
			break;
		case PlicParser.FUNC_CALL:
			function_call(ast, tds);
			break;
		}
		//fin génération

		//boolean bloc=false;
		int res=(bloc?-1:truc);

		for(int i=0;i<ast.getChildCount();i++){	
			res = generateRec(tds,ast.getChild(i), res);
		}


		switch(ast.getType()){
		case PlicParser.FUNCTION:
			function_end(ast,tds);
			break;
		}

		return (bloc?truc:res);
	}


	
	public void function_call(Tree ast, TDS tds){
		
		//Sauvegarde registres
		for(int i=1;i<=10;i++){
			addCodeln("stw R"+i+",-(SP)");
		}
		
		
		// Preparation de l'environnement du programme principal 
		// empile les parametres de la fonction 
		int currentdeplacement = tds.getSizeOfParams();
		for (int i = tds.getParams().size()-1; i == 0 ; i--){
			addCodeln("LDW R1, (BP)-"+currentdeplacement);
			addCodeln("STW R1, -(SP)");
			currentdeplacement += tds.getParams().get(i).getSize();
		}
		
		//execution fct
		
		addCodeln("JSR @"+ast.getChild(0).getText()+"_");
		
	}


	private void function(Tree ast, TDS tds){
		addCodeln("//fonction "+tds.getIdf());
		//etiquette de fonction
		
		addCodeln(tds.getIdf()+"_ LDQ "+tds.getSizeOfVar()+",R1"); // R1 = taille donnees locales de fonction appelee
			
		//sauvegarde de la base
		addCodeln("stw BP,-(SP)");
		addCodeln("ldw BP, SP");
		
		addCodeln("SUB SP, R1, SP"); //reserve R1 octets sur la pile pour les variables locales
		
		//debut corps fonction
		addCodeln("//Corps de la fonction");
		
		
		//fin de la fonction
		addCodeln ("LDW SP, BP"); // charge SP avec contenu de BP: abandon infos locales
		addCodeln ("LDW BP, (SP)"); // charge BP avec ancien BP
		addCodeln ("ADQ 2,SP"); // supprime l'ancien BP de la pile
		
		// RTS // retour au programme appelant:
		addCodeln ("LDW WR, (SP)"); // charge WR avec l'adresse de retour
		addCodeln ("ADQ 2,SP"); // incremente le pointeur de pile SP
		addCodeln ("JEA (WR)"); // saute a l'instruction d'adresse absolue dans WR
		
		
	}


	private void function_end(Tree as, TDS tds){
		// nettoyage de la pile par le programme appelant 	
		addCode("ADQ "+tds.getSizeOfParams()+",SP");
		//restauration des registres
		for(int i=10;i>=0;i--){
			addCodeln("ldw R"+i+",(SP)+");
		}
		//restauration base
		addCodeln("ldw BP,(SP)+");
		addCodeln("//fin fonction "+tds.getIdf());
	}

	private void variable(Tree ast, TDS tds){
		if(ast.getChild(0).getText().equalsIgnoreCase("integer")){
			addCodeln("//integer");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln("ADQ -"+INT_SIZE+", SP //var "+ast.getChild(i).getText());
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("boolean")){
			addCodeln("//boolean");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln("ADQ -"+BOOL_SIZE+", SP //var "+ast.getChild(i).getText());
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("array")){
			addCodeln("//array");
			//TODO
			addCodeln("//TODO");
		}
	}

	private void affectaction(Tree ast, TDS tds){
		addCodeln("//******");
		Tree left = ast.getChild(0).getChild(0);
		Tree right = ast.getChild(1).getChild(0);
		expr(right, tds);
		int depl = tds.getDeclarationOfVar(left.getText()).getDeplacement();
		addCodeln("STW R0, (BP)"+depl);
	}

	private void expr(Tree ast, TDS tds){
		// met le resultat de l'exp dans R0
		expr_rec(ast, tds,-1);
	}

	private void expr_rec(Tree ast, TDS tds, int num_fils){

		if(ast.getChildCount()==0){
			//bas de l'arbre
			if(ast.getText().matches("^\\p{Digit}+$")){
				//un nombre on le met dans le registre directement
				addCodeln("LDW R0, #"+ast.getText());
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else if(tds.getDeclarationOfVar(ast.getText())!=null){
				Declaration decl = tds.getDeclarationOfVar(ast.getText());
				int deep = tds.getDeepOfVar(ast.getText());
				//une variable, on le charge depuis la pile
				int depl = decl.getDeplacement();
				
				addCodeln("LDW WR, BP");
				while(deep>0){
					//on parcours le chainage static
					addCodeln("LDW WR, (WR)");
					deep--;
				}
				
				addCodeln("LDW R0, (WR)"+depl);
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else if(tds.getTdsOfFunction(ast.getText())!=null){
				//TODO faire appel de fct
				
			}else{
				//erreur ? 
				System.out.println("ERREUR");
			}
		}else{
			for(int i=0;i<ast.getChildCount();i++){
				if(ast.getType()==PlicParser.FUNC_CALL){
					//TODO appel fonction
				}else{
					expr_rec(ast.getChild(i), tds, i); 
				}
			}
			if(ast.getText().equalsIgnoreCase("+")){
				addCodeln("LDW R1, (SP)+");
				addCodeln("LDW R2, (SP)+");
				addCodeln("ADD R1, R2, R"+(num_fils+1));
				if(num_fils!=-1){
					addCodeln("STW R"+(num_fils+1)+", -(SP)");
				}

			}else if(ast.getText().equalsIgnoreCase("-")){
				addCodeln("LDW R1, (SP)+");
				addCodeln("LDW R2, (SP)+");
				addCodeln("SUB R2, R1, R"+(num_fils+1));
				if(num_fils!=-1){
					addCodeln("STW R"+(num_fils+1)+", -(SP)");
				}
			}else if(ast.getText().equalsIgnoreCase("*")){
				addCodeln("LDW R1, (SP)+");
				addCodeln("LDW R2, (SP)+");
				addCodeln("MUL R1, R2, R"+(num_fils+1));
				if(num_fils!=-1){
					addCodeln("STW R"+(num_fils+1)+", -(SP)");
				}
			}else if(ast.getText().equalsIgnoreCase("unaire")){
				addCodeln("LDW R1, (SP)+");
				addCodeln("NEG R1, R"+(num_fils+1));
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}
		}


	}

	public void accessLocaleVar(String var){
		addCodeln("LEA ("+Integer.toString(tds.getDeclarationOfLocaleVar(var).getDeplacement())+",A0),A1)");
	}

	public void accessNeitherGlobalNorLocaleVar(String var){
		addCodeln("MOVE #(Nx-Ny),DO");// TODO A modifier et à ajouter une fonction permettant de retrouver le niveau d'imbrication
		addCodeln("MOVE A0,A2");
		addCodeln("BOUCLE: MOVE(depl_stat,A2),A2"); // TODO A modifier
		addCodeln("SUB #1,D0");
		addCodeln("BNE BOUCLE");
		addCodeln("LEA (depl,A2),A1");
	}

	public void stackValue(){
		addCodeln("MOVE (A1),-(A7)");
	}

	public void accessAddressParam(String param){
		addCodeln("MOVE ("+Integer.toString(tds.getDeclarationOfParam(param).getDeplacement())+",A0),A1");
	}

	public void accessValParam(String param){
		addCodeln("LEA ("+Integer.toString(tds.getDeclarationOfParam(param).getDeplacement())+",A0),A1");
	}

	public void accessRoutine(){

	}

}
