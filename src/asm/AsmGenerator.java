package asm;

import java.util.Stack;

import model.TDS;

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
		//init du programme
		addCodeln("org 0x1000");
		addCodeln("start main_");
		//modification nom des registres
		addCodeln("//renomage registres");
		addCodeln("BP equ R14");
		addCodeln("SP equ R15");
		//creation pile
		addCodeln("stackSize equ 100");
		addCodeln("stack rsb stackSize");

		addCodeln("main_");
		addCodeln("LDW BP, SP");
		generateRec(tds, ast, -1);



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



	private void function(Tree ast, TDS tds){
		addCodeln("//fonction "+tds.getIdf());
		//etiquette de fonction
		addCodeln(tds.getIdf()+"_");

		//TODO ajouter var locale ici ??

		//sauvegarde de la base
		addCodeln("stw BP,-(SP)");
		addCodeln("ldw BP, SP");
		//sauvagarde du contexte
		for(int i=0;i<=13;i++){
			addCodeln("stw R"+i+",-(R15)");
		}
		//debut corps fonction
		addCodeln("//Cords de la fonction");
	}


	private void function_end(Tree as, TDS tds){
		//restauration du contexte
		for(int i=13;i>=0;i--){
			addCodeln("ldw R"+i+",(R15)+");
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
		addCodeln("STW R0, (BP)-"+depl);
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
			}else{
				//une variable, on le charge depuis la pile
				int depl = tds.getDeclarationOfVar(ast.getText()).getDeplacement();
				addCodeln("LDW R0, (BP)-"+depl);
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
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
				addCodeln("NEG R1, R"+(num_fils+1));
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}
		}


	}


	public void function_call(Tree ast, TDS tds){
		//empilage des params
		//execution fct
		//recuperation du resultat dans R0
		//depilage params
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
