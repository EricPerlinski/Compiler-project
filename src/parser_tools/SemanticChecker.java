package parser_tools;

import model.Declarations;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class SemanticChecker{



	public static boolean check(Tree ast, TDS tds){
		boolean result=false;




		return result;
	}
	
	// On vérifie que la variable est bien définie dans le bloc courant ou ceux englobant
	public static boolean isDefined(String idf, TDS tds) {
		if (getTdsOfDef(idf, tds)==null) {
			return false;
		} else {
			return true;
		}
	}
	
	// Pas encore aboutie du tout ...
	public static boolean check_aff_left(Tree sub_tree, TDS tds)
	{
		// On regarde si le premier fils est de type Array ou non
		Tree fc = sub_tree.getChild(0);
		if (fc.getType()==PlicParser.ARRAY) {
			for (Declarations e : tds.getVar()) {
				if (fc.getChild(0).getText().equalsIgnoreCase(e.getIdf()) && e.getType()!=Type.array) {
					return false;
				}
			}
			for (Declarations e : tds.getParams()) {
				if (fc.getChild(0).getText().equalsIgnoreCase(e.getIdf()) && e.getType()!=Type.array) {
					return false;
				}
			}
		} else { // Sinon on vérifie dans la tds que le premier fils est bien du même type que le second 
			Declarations res = null;
			for (Declarations e : tds.getVar()) {
				if (fc.getText().equalsIgnoreCase(e.getIdf())) {
					res = e;
				}
			}
			for (Declarations e : tds.getParams()) {
				if (fc.getText().equalsIgnoreCase(e.getIdf())) {
					res = e;
				}
			}
		}
		return true;
	}
	
	// Retourne la TDS de définition de l'identifiant demandé afin de faciliter les contrôles sémantiques
	public static TDS getTdsOfDef(String idf, TDS tds) {
		TDS current = tds;
		do {
			for (Declarations e : tds.getVar()) {
				if (e.getIdf()==idf) {
					return current;
				}
			}
			for (Declarations e : tds.getParams()) {
				if (e.getIdf()==idf) {
					return current;
				}
			}
			current = current.getPere();
		} while(current!=null);
		return null;
	}

	public static Type getTypeExp(Tree t, TDS tds){
		Type res=null;
		String name=t.getText();
		if(name.equals("+") || name.equals("-") || name.equals("*")){
			Type t1 = getTypeExp(t.getChild(0), tds);
			Type t2 = getTypeExp(t.getChild(1), tds);
			if(t1!=null && t2!=null && t1==t2){
				res=t1;
			}else{
				System.out.println("Opération "+name+" avec un "+t1.toString()+" et un "+t2.toString());
			}
		}else if(name.equals("==") || name.equals("!=")|| name.equals("<")|| name.equals("<=")|| name.equals(">")|| name.equals(">=")){
			Type t1 = getTypeExp(t.getChild(0), tds);
			Type t2 = getTypeExp(t.getChild(1), tds);
			if(t1!=null && t2!=null && t1==t2 /*&& t1==Type.bool*/){
				res=Type.bool;
			}
		}else if(name.equals("UNAIRE")){
			res = getTypeExp(t.getChild(0), tds);
		}else if(name.equals("ARRAY")){
			res = Type.integer;
		}else if(name.equals("FUNC_CALL")){
			res = tds.getTypeOfFunction(name);
		}else if(name.equals("true") || name.equals("false")){
			res = Type.bool;
		}else if(name.matches("^\\p{Digit}+$")){
			res=Type.integer;
		}else if((res=tds.getTypeOfVar(name))!=null){
			res=tds.getTypeOfVar(name);
		}else{
			System.out.println("Erreur name : "+name);
			res=null;
		}
		
		return res;
	}




}