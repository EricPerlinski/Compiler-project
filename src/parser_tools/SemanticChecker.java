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
	
	// Contrôle la cohérence des types lors des affectations
	public static boolean check_aff(Tree sub_tree, TDS tds)
	{
		Tree fg = sub_tree.getChild(0);
		Tree fd = sub_tree.getChild(1);
		
		Type tfg = getTypeExp(fg.getChild(0), tds);
		Type tfd = getTypeExp(fd.getChild(0), tds);
		
		if (tfg == tfd) {
			return true;
		} else {
			System.out.println("Erreur d'affectation : "+tfg.toString()+" != "+tfd.toString());
			return false;
		}
	}
	
	// Contrôle le nombre de paramètres passés en argument lors de l'appel d'une fonction ou d'une procédure
	public static boolean check_nbparams_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		boolean res = true;
		if (tds.getParams().size()!=sub_tree.getChildCount()-1) {
			System.out.println("Erreur d'appel de fonction : Mauvais nombre de paramètres");
			res = false;
		}
		return res;
	}
	
	// Contrôle la cohérence des types des paramètres à l'appel des fonctions et des procédures
	public static boolean check_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		Type typeCurrent = null;
		boolean res = true;
		for (int i=1; i<sub_tree.getChildCount()-1; i++) {
			typeCurrent = getTypeExp(sub_tree.getChild(i), tds);
			if (typeCurrent != tdsCurrent.getParams().get(i-1).getType()) {
				System.out.println("Erreur d'appel de fonction : Le type du "+i+"eme paramètre est "+typeCurrent.toString()+" il devrait être de type "+tdsCurrent.getParams().get(i-1).getType().toString());
				res = false;
			}
		}
		return res;
	}
	
	// Contrôle la cohérence entre le type de retour d'une fonction et ce qui est effectivement retourné
	public static boolean check_return_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		Type typeCurrent = getTypeExp(sub_tree.getChild(0),tds);
		Type typeDefined = TDS.str2type((tds.getTypeRet()));
		if (typeCurrent!=typeDefined) {
			System.out.println("Erreur de type de retour de fonction : La fonction "+sub_tree.getChild(0).getText()+" a pour type de retour "+typeDefined.toString()+" mais vous retournez un "+typeCurrent);
			res = false;
		}
		return res;
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