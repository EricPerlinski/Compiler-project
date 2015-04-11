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

}
