package parser_tools;

import java.util.ArrayList;
import java.util.Stack;

import model.TDS;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class ASTParser {
	
	private CommonTree c;
	private ArrayList<TDS> tableDesSymboles;
	private Stack<TDS> stack;
	
	/* Constructor */
	
	public ASTParser(CommonTree newC){
		setC(newC);
		setTableDesSymboles(new ArrayList<TDS>());
		stack=new Stack<TDS>();
	}
	
	/* Methods */

	public String toStringTree(){
		return c.toStringTree();
	}
	
	public void ASTParse(){
		
		if(c.getToken().getText().equalsIgnoreCase("PROG") && c.getChildCount() == 2 && c.getChild(0).getText().equalsIgnoreCase("DECLARATIONS") && c.getChild(1).getText().equalsIgnoreCase("INSTRUCTIONS") ){
			
			TDS tDS = new TDS(0,1);
			tableDesSymboles.add(tDS);
			
			// Parse du premier Noeud Declarations
			for(int i=0;i<c.getChild(0).getChildCount();i++){
				NodeParse(c.getChild(0).getChild(i));
			}
			
			// Parse du premier Noeud Instructions
			for(int j=0;j<c.getChild(1).getChildCount();j++){
				NodeParse(c.getChild(1).getChild(j));
			}
			
			
			
		}
		
	}
	
	public void NodeParse(Tree t){
		

		System.out.println(t.getText());
		/* 
			Si c'est une variable, explorer le sous arbre pour recuperer les infos
			Faire une fonction qui parse un noeud VARIABLE
		*/
		if(t.getText().equals("VARIABLE")){
			//TODO creer parse_variable(t)
			System.out.println("Entrée dans un noeud variable");
			for(int i = 0; i<t.getChildCount(); i++){
				System.out.println("Variable::Type:::"+t.getChild(i).getText());
				parse_variable(t.getChild(i));
			}
		}else 
		/* Si c'est un bloc avec params et nom alors ajouter la TDS courante a la pile
		créé une nouvelle TDS et parcourir le bloc recursivement*/
		if(t.getText().equals("FUNCTION")){
			//TODO creer parse_fonction(t)
		}else if (t.getText().equals("PROCEDURE")){
			//TODO creer parse_proc(t)
		}else 
		/* Si c'est un bloc anonyme faire comme une fonction mais sans les params, types, ...*/
		if(	t.getText().equals("BLOC") || 
					t.getText().equals("IF_BLOC") || 
					t.getText().equals("ELSE_BLOC") || 
					t.getText().equals("FOR")){
			//TODO parse_bloc_anonyme(t) // il s'agit peut etre de cette fonction
			// je propose : 
			//
			//	for(int i=0; i<t.getChildCount(); i++){
			//		NodeParse(t.getChild(i));
			//	}
			
		}
		
	}
	
	
	public void parse_variable(Tree t){
		
		if(t.getText().equalsIgnoreCase("integer")){
			for(int i = 0; i < t.getChildCount(); i++){
				//TODO Ajouter t.getChild(i).getText() à la TDS
				System.out.println("integer ::::: "+t.getChild(i).getText());
				
			}
		}else if(t.getText().equalsIgnoreCase("boolean")){
			for(int i = 0; i < t.getChildCount(); i++){
				//TODO Ajouter t.getChild(i).getText() à la TDS
				System.out.println("boolean ::::: "+t.getText());
			}
			
		}else if(t.getText().equalsIgnoreCase("array")){
			for(int i = 0; i < t.getChildCount(); i++){
				System.out.println("array ::::: "+t.getText());
				parse_array(t.getChild(i));
			}
		}
		
	}
	
	
	
	public void parse_array(Tree t){
		for(int i = 0; i < t.getChildCount(); i++){
			
		}
	}
	
	public void parse_fonction(Tree t){
		
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
