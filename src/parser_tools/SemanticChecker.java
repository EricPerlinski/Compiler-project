package parser_tools;

import model.Declarations;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class SemanticChecker{



	public static boolean check(Tree ast, TDS tds){
		boolean result=true;
		



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
	
	public static boolean check_aff(Tree sub_tree, TDS tds)
	{
		Tree fg = sub_tree.getChild(0);
		Tree fd = sub_tree.getChild(1);
		
		Type tfg = tds.getTypeOfVar(fg.getChild(0).getText());
		Type tfd = null;
		
		if (fd.getChild(0).getType()==PlicParser.FUNC_CALL) {
			tfd = tds.getTypeOfFunction(fd.getChild(0).getText());
		} else {
			tfd = tds.getTypeOfVar(fd.getChild(0).getText());
		}
		
		if (tfg == tfd) {
			return true;
		} else {
			System.out.println("Erreur d'affectation : "+tfg.toString()+" != "+tfd.toString());
			return false;
		}
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