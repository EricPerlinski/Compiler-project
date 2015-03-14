package parser_tools;

import org.antlr.runtime.tree.CommonTree;

public class ASTParser {
	
	private CommonTree c;
	
	
	/* Constructor */
	public ASTParser(CommonTree newC){
		setC(newC);
	}
	
	/* Methods */

	public String toStringTree(){
		return c.toStringTree();
	}
	
	/* Getters & Setters */
	
	public CommonTree getC() {
		return c;
	}

	public void setC(CommonTree c) {
		this.c = c;
	}
	
	
}
