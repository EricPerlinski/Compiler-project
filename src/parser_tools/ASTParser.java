package parser_tools;

import java.util.ArrayList;
import java.util.Stack;

import model.Bound;
import model.Declarations;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class ASTParser {
	
	private CommonTree c;
	private ArrayList<TDS> tableDesSymboles;
	private Stack<TDS> stack;
	private TDS current; 
	private int currentReg=0;
	
	/* Constructor */
	
	public ASTParser(CommonTree newC){
		setC(newC);
		setTableDesSymboles(new ArrayList<TDS>());
		stack=new Stack<TDS>();
		current=new TDS(null,0,currentReg);
	}

	public TDS getCurrent(){
		return current;
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
			parse_variable(t);
		}else 
		/* Si c'est un bloc avec params et nom alors ajouter la TDS courante a la pile créé une nouvelle TDS et parcourir le bloc recursivement*/
		if(t.getText().equals("FUNCTION")){
			parse_bloc_function(t);
		}else if (t.getText().equals("PROCEDURE")){
			parse_bloc_procedure(t);
		}else 
		/* Si c'est un bloc anonyme faire comme une fonction mais sans les params, types, ...*/
		if(	t.getText().equals("BLOC") || t.getText().equals("IF_BLOC") || t.getText().equals("ELSE_BLOC") || t.getText().equals("FOR")){
			parse_bloc_anonyme(t);
			
			
		}
		
	}
	
	
	public void parse_variable(Tree t){
	
		
		
		if(t.getChild(0).getText().equalsIgnoreCase("integer")){
			
			for(int i = 1; i < t.getChildCount(); i++){
				//TODO Ajouter t.getChild(i).getText() à la TDS
				Declarations d = new Declarations(Type.integer,t.getChild(i).getText(), 0);
				System.out.println(d);
				 
			}
		}else if(t.getChild(0).getText().equalsIgnoreCase("boolean")){
			
			for(int i = 1; i < t.getChildCount(); i++){
				//TODO Ajouter t.getChild(i).getText() à la TDS
				Declarations d = new Declarations(Type.bool,t.getChild(i).getText(), 0);
				System.out.println(d);
			}
			
		}else if(t.getChild(0).getText().equalsIgnoreCase("array")){
			
				ArrayList<Bound> Bounds = new ArrayList<Bound>();
				if(t.getChild(0).getChild(0).getText().equalsIgnoreCase("BOUNDS")){
					
					for(int k = 0; k < t.getChild(0).getChild(0).getChildCount(); k++){
						Bound b = new Bound(Integer.parseInt(t.getChild(0).getChild(0).getChild(k).getChild(0).getText()),
								Integer.parseInt(t.getChild(0).getChild(0).getChild(k).getChild(1).getText()));
						Bounds.add(b);
					}
				}
				Declarations d = new Declarations(Type.array,t.getChild(1).getText(),0, Bounds);
				System.out.println(d);
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

	public void parse_bloc_anonyme(Tree t){
		//on cree un nouveau current, on push l'ancien
		stack.push(current);
		currentReg++;
		current=new TDS(current,current.getNbImb()+1,currentReg);
		(stack.peek()).addFils(current);

		
		//declarations
		NodeParse(t.getChild(0));
		
		//instructions
		NodeParse(t.getChild(1));

		//reset current
		current=stack.pop();
	}

	public void parse_bloc_function(Tree t){
		//on cree un nouveau current, on push l'ancien
		stack.push(current);
		currentReg++;
		current=new TDS(current,current.getNbImb()+1,currentReg);
		(stack.peek()).addFils(current);
		
		//prototype
		Tree t_proto = t.getChild(0);

		String type_ret = t_proto.getChild(0).getText(); //TODO utiliser ca plus tard pour la semantique
		current.setIdf(t_proto.getChild(1).getText());
		parse_params(t_proto.getChild(2));
		
		//declarations
		NodeParse(t.getChild(1));
		
		//instructions
		NodeParse(t.getChild(2));

		//reset current
		current=stack.pop();
	}
	
	public void parse_bloc_procedure(Tree t){
		//on cree un nouveau current, on push l'ancien
		stack.push(current);
		currentReg++;
		current=new TDS(current,current.getNbImb()+1,currentReg);
		(stack.peek()).addFils(current);

		//prototype
		Tree t_proto = t.getChild(0);

		current.setIdf(t_proto.getChild(0).getText());
		parse_params(t_proto.getChild(1));

		//declarations
		NodeParse(t.getChild(1));
		
		//instructions
		NodeParse(t.getChild(2));

		//reset current
		current=stack.pop();
	}

	public void parse_params(Tree t){
		
	}
	
}
