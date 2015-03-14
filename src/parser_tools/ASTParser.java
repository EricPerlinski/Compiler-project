package parser_tools;

import java.util.ArrayList;

import model.TDS;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class ASTParser {
	
	private CommonTree c;
	private ArrayList<TDS> tableDesSymboles;
	
	/* Constructor */
	
	public ASTParser(CommonTree newC){
		setC(newC);
		setTableDesSymboles(new ArrayList<TDS>());
	}
	
	/* Methods */

	public String toStringTree(){
		return c.toStringTree();
	}
	
	public void ASTParse(){
		
		if(c.getToken().getText().equalsIgnoreCase("PROG") && c.getChildCount() == 2 && c.getChild(0).getText().equalsIgnoreCase("DECLARATIONS") && c.getChild(1).getText().equalsIgnoreCase("INSTRUCTIONS") ){
			
			TDS tDS = new TDS(0,1);
			tableDesSymboles.add(tDS);
			
			NodeParse(c.getChild(0)); // Parse du premier Noeud Declarations
			NodeParse(c.getChild(1)); // Parse du premier Noeud Instructions
			
		}
		
	}
	
	public void NodeParse(Tree t){
		
		System.out.println(t.getText());
		
		for(int i=0; i<t.getChildCount(); i++){
			NodeParse(t.getChild(i));
		}
	
		
	}
	
	/* Getters & Setters */
	
	public CommonTree getC() {
		return c;
	}

	public void setC(CommonTree c) {
		this.c = c;
	}

	public ArrayList<TDS> getTableDesSymboles() {
		return tableDesSymboles;
	}

	public void setTableDesSymboles(ArrayList<TDS> tableDesSymboles) {
		this.tableDesSymboles = tableDesSymboles;
	}

	@Override
	public String toString() {
		return "ASTParser [c=" + c + ", tableDesSymboles=" + tableDesSymboles
				+ "]";
	}
	
	
}
