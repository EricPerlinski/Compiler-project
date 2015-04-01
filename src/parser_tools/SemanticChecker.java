package parser_tools;

import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

public class SemanticChecker{



	public static boolean check(Tree ast, TDS tds){
		boolean result=false;




		return result;
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