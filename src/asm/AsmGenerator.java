package asm;

import model.TDS;

import org.antlr.runtime.tree.Tree;

public class AsmGenerator {
	
	private String name;
	private Tree ast;
	private TDS tds;
	
	private StringBuffer asmBuff;
	
	public AsmGenerator(String name,Tree ast, TDS tds){
		this.ast=ast;
		this.tds=tds;
		this.name=name;
		asmBuff=new StringBuffer();
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
		return false;
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
