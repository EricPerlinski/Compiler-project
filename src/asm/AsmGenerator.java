package asm;

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
		addCodeln("start debut");
		//creation pile
		addCodeln("stackSize equ 100");
		addCodeln("stack rsb stackSize");
		
		addCodeln("debut");

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
		boolean bloc=false;;
		switch(ast.getType()){
		case PlicParser.BLOC:
		case PlicParser.FUNCTION:
		case PlicParser.PROCEDURE:
			truc++;
			bloc=true;
			tds = tds.getFils().get(truc);
			break;
		}
		// Generer de l'ASM suivant le type
		switch(ast.getType()){
		case PlicParser.FUNCTION:
			function(ast,tds);
			break;
		case PlicParser.VARIABLE:
			variable(ast, tds);
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
		addCodeln(tds.getIdf());
		
		//TODO ajouter var locale ici ??
		
		//sauvegarde de la base
		addCodeln("stw R14,-(R15)");
		addCodeln("ldw R14, R15");
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
		addCodeln("ldw R14,(R15)+");
		addCodeln("//fin fonction "+tds.getIdf());
	}
	
	private void variable(Tree ast, TDS tds){
		if(ast.getChild(0).getText().equalsIgnoreCase("integer")){
			addCodeln("//integer");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln(ast.getChild(i).getText()+" rsw 2");
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("boolean")){
			addCodeln("//boolean");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln(ast.getChild(i).getText()+" rsw 2");
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("array")){
			addCodeln("//array");
			//TODO
			addCodeln("//TODO");
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
