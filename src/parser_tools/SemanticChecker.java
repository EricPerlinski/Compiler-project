package parser_tools;

import model.Declaration;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class SemanticChecker{
	
	private static StringBuffer errorsBuff=null;
	private static int nbErrors=0;
	private static int nbWarn=0;
	
	public static StringBuffer getErrors(){
		return errorsBuff;
	}
	
	public static int getNbErrors(){
		return nbErrors;
	}
	
	public static int getNbWarn(){
		return nbWarn;
	}
	
	private static void addError(String err){
		nbErrors++;
		errorsBuff.append("\n\033[31m");
		errorsBuff.append(err);
		errorsBuff.append("\033[0m\n");
	}
	
	@SuppressWarnings("unused")
	private static void addWarning(String warning){
		nbWarn++;
		errorsBuff.append("\n\033[33m");
		errorsBuff.append(warning);
		errorsBuff.append("\033[0m\n");
	}
	
	
	
	
	
	
	public static boolean check(Tree ast, TDS tds){
		nbErrors=0;
		nbWarn=0;
		errorsBuff=new StringBuffer();
		checkRec(ast, tds, -1);
		
		return nbErrors>0 || nbWarn>0 ;
	}
	
	public static int checkRec(Tree ast, TDS tds, int truc){
		boolean bloc=false;;
		switch(ast.getType()){
		case PlicParser.BLOC:
		case PlicParser.FUNCTION:
		case PlicParser.PROCEDURE:
			truc++;
			bloc=true;
			tds = tds.getFils().get(truc);
			break;
		}
		//Tester ici avec les fct semantiques
		// faire un mega switch case :D 
	
		
		switch(ast.getType()){
		case PlicParser.AFFECTATION:
			check_aff(ast, tds);
			break;
		case PlicParser.FUNC_CALL:
		case PlicParser.PROC_CALL:
			check_nbparams_func_call(ast, tds);
			check_func_call(ast, tds);
			break;
		case PlicParser.RETURN:
			check_return_type(ast, tds);
			break;
		case PlicParser.CONDITION:			
			check_condition_type(ast, tds);
			break;
		case PlicParser.FOR:
			areVarAndExprInForIntegers(ast, tds);
			break;
		case PlicParser.PARAMS:
			check_func_params(ast, tds);
			break;
		case PlicParser.ARRAY:
			if(ast.getParent().getType()!=PlicParser.PROTOTYPE){
			    boolean isRealArray = isARealArrayType(ast, tds);
			    if(isRealArray) {
			        isGoodNumberOfIndexesInArrayDimensions(ast, tds);
			        isGoodTypesInArrayDimensions(ast, tds);
			    }
			}
		    break;
		    
		}
		
		
		//fin test
		
		//boolean bloc=false;
		int res=truc;
		if(bloc){
			res=-1;
		}
		for(int i=0;i<ast.getChildCount();i++){	
			res = SemanticChecker.checkRec(ast.getChild(i), tds,res);
		}
		if(bloc){
			return truc;
		}else{
			return res;
		}
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
	// Noeud racine AFFECTATION
	public static boolean check_aff(Tree sub_tree, TDS tds)
	{
		Tree fg = sub_tree.getChild(0);
		Tree fd = sub_tree.getChild(1);
		
		Type tfg = getTypeOfExp(fg.getChild(0), tds);
		Type tfd = getTypeOfExp(fd.getChild(0), tds);
		
		if (tfg == tfd) {
			return true;
		} else {
			//System.out.println(tds.toString());
			if(tfg==null){
				addError("Line "+sub_tree.getLine()+": "+fg.getChild(0).getText()+" n'existe pas");

			}
			if(tfd==null){
				if(fd.getChild(0).getType()==PlicParser.FUNC_CALL){
					addError("Line "+sub_tree.getLine()+": error function "+fd.getChild(0).getChild(0).getText()+" does not exist");
				}else{
					addError("Line "+sub_tree.getLine()+": "+fd.getChild(0).getText()+" does not exist");
				}
			}
			if(tfd!=null && tfg!=null){
				addError("Line "+sub_tree.getLine()+": variable « " + fg.getChild(0).getText() + " », " +tfg.toString()+" is different from "+tfd.toString());
			}

			return false;
		}
	}
	
	// Contrôle le nombre de paramètres passés en argument lors de l'appel d'une fonction ou d'une procédure
	// Noeud racine FUNC_CALL
	public static boolean check_nbparams_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		boolean res = true;
		if(tdsCurrent!=null){
			if (tdsCurrent.getParams().size()!=sub_tree.getChildCount()-1) {
				addError("Line "+sub_tree.getLine()+": wrong call of the function « " + sub_tree.getChild(0).getText() + " » -> Wrong number of parameters");
				res = false;
			}
		}
		return res;
	}
	
	// Contrôle la cohérence des types des paramètres à l'appel des fonctions et des procédures
	// Noeud racince FUNC_CALL
	public static boolean check_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		if(tdsCurrent==null){
			return false;
		}
		Type typeCurrent = null;
		boolean res = true;
		for (int i=1; i<sub_tree.getChildCount(); i++) {
			typeCurrent = getTypeOfExp(sub_tree.getChild(i), tds);
			if(i-1<tdsCurrent.getParams().size()){
				if (typeCurrent != tdsCurrent.getParams().get(i-1).getType()) {
					addError("Line "+sub_tree.getLine()+": wrong call of the function -> Wrong type of the parameter « " + (sub_tree.getChild(i).getType()!=PlicParser.FUNC_CALL ? sub_tree.getChild(i).getText() : sub_tree.getChild(i).getChild(0).getText() ) + " », it should be "+tdsCurrent.getParams().get(i-1).getType().toString());
					res = false;
				}
			}
		}
		return res;
	}
	
	// Contrôle la cohérence entre le type de retour d'une fonction et ce qui est effectivement retourné
	// Noeud racine RETURN
	public static boolean check_return_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		Type typeCurrent = getTypeOfExp(sub_tree.getChild(0),tds);
//		Type typeDefined = tds.getTypeOfFunction((sub_tree.getParent().getParent().getChild(0).getChild(1).getText()));
		Type typeDefined = TDS.str2type(tds.getTypeRet());
		if (typeCurrent!=typeDefined) {
			addError("Line "+sub_tree.getLine()+": wrong type of return -> The function « "+sub_tree.getParent().getParent().getChild(0).getChild(1).getText()+" » must return a "+(typeDefined!=null ? typeDefined.toString() : "null" )+" but it actually returns a "+typeCurrent);
			res = false;
		}
		return res;
	}
	
	// Contrôle le type des conditions dans les structures if : doit être un boolean
	// Noeud racine CONDITION ou IF je sais plus
	public static boolean check_condition_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		if (getTypeOfExp(sub_tree.getChild(0), tds)!=Type.bool) {
			addError("Line "+sub_tree.getLine()+": wrong type of the condition, it should be of boolean type");
			res = false;
		}
		return res;
	}
	
	// Contrôle si une fonction n'a pas 2 paramètres avec le même id et le même type
	// Noeud racine PARAMS
	public static boolean check_func_params(Tree sub_tree, TDS tds) {
		boolean res = true;
		for (int i=0; i<sub_tree.getChildCount()-1; i++) {
			String current = sub_tree.getChild(i).getChild(1).getText();
			for (int j=i+1; j<sub_tree.getChildCount(); j++) {
				if (current.equals(sub_tree.getChild(j).getChild(1).getText())) {
					addError("Line "+sub_tree.getLine()+": wrong prototype of the function -> At least two parameters have the same identifier");
					res = false;
				}
			}
		}
		return res;
	}
	
	// Retourne la TDS de définition de l'identifiant demandé afin de faciliter les contrôles sémantiques
	public static TDS getTdsOfDef(String idf, TDS tds) {
		TDS current = tds;
		do {
			for (Declaration e : tds.getVar()) {
				if (e.getIdf()==idf) {
					return current;
				}
			}
			for (Declaration e : tds.getParams()) {
				if (e.getIdf()==idf) {
					return current;
				}
			}
			current = current.getPere();
		} while(current!=null);
		return null;
	}

	public static Type getTypeOfExp(Tree t, TDS tds){
		Type res=null;
		String name=t.getText();
		if(name.equals("+") || name.equals("-") || name.equals("*")){
			Type t1 = getTypeOfExp(t.getChild(0), tds);
			Type t2 = getTypeOfExp(t.getChild(1), tds);
			if(t1!=null && t2!=null && t1==t2){
				res=t1;
			}else{
				addError("Line "+t.getLine()+": operator "+name+" applied to a "+ (t1==null ? "null" : t1.toString()) +" and a "+(t2==null ? "null" : t2.toString()));
			}
		}else if(name.equals("==") || name.equals("!=")|| name.equals("<")|| name.equals("<=")|| name.equals(">")|| name.equals(">=")){
			Type t1 = getTypeOfExp(t.getChild(0), tds);
			Type t2 = getTypeOfExp(t.getChild(1), tds);
			if( t1!=null && t2!=null && t1==t2 && (t1!=Type.bool || ( t1==Type.bool && (name.equals("==") || name.equals("!="))))){
				res=Type.bool;
			}
		}else if(name.equals("UNAIRE")){
			res = getTypeOfExp(t.getChild(0), tds);
		}else if(name.equals("ARRAY")){
			res = Type.integer;
		}else if(name.equals("FUNC_CALL")){
			res = tds.getTypeOfFunction(t.getChild(0).getText());
		}else if(name.equals("true") || name.equals("false")){
			res = Type.bool;
		}else if(name.matches("^\\p{Digit}+$")){
			res=Type.integer;
		}else if((res=tds.getTypeOfVar(name))!=null){
			res=tds.getTypeOfVar(name);
		}else{
			addError("Line "+t.getLine()+": error name : "+name);
			res=null;
		}
		
		return res;
	}
	
    //Ne pas oublier d'appeler isARealArrayType
	// t = arbre avec comme racine le noeud ARRAY
    public static boolean isGoodTypesInArrayDimensions(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be a ARRAY node";
        boolean res = true;
        for (int i = 1; i < t.getChildCount(); i++) {
            Tree boundNode = t.getChild(i);
            if (getTypeOfExp(boundNode, tds) != Type.integer) {
            	addError("Line " + boundNode.getLine() + ": variable « " + boundNode.getText() + " », type mismatch, must be an integer.");
                    res = false;
            }
        }
        return res;
    }
    //Ne pas oublier d'appeler isARealArrayType
    // t = arbre avec comme racine le noeud ARRAY
    public static boolean isGoodNumberOfIndexesInArrayDimensions(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be a ARRAY node";
        Tree idfNode = t.getChild(0);
        int numberOfIndexes = t.getChildCount() - 1; //-1 pour enlever l'idf
        Declaration arrayDecl = tds.getDeclarationOfVar(idfNode.getText());
        if(arrayDecl != null && arrayDecl.getBounds().size() != numberOfIndexes) {
        	addError("Line " + t.getLine() + ": variable « " + idfNode.getText() + " », wrong number of index.");
            return false;
        }
        return true;
    }
    
    //À appeler impérativement sur un noeud array avant tout autre contrôle concernant les array
    // t = arbre avec comme racine le noeud ARRAY
    public static boolean isARealArrayType(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be a ARRAY node";
        Tree idfNode = t.getChild(0);
        if (idfNode.getText().equalsIgnoreCase("BOUNDS")) {
            idfNode = t.getParent().getChild(1);
        }
        if (getTypeOfExp(idfNode, tds) != Type.array) {
        	addError("Line " + t.getLine() + ": variable « " + idfNode.getText() + " », the type of the expression must be an array type.");
            return false;
        }
        return true;
    }
    
    //t = arbre avec comme racine le noeud FOR
    public static boolean areVarAndExprInForIntegers(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("FOR") : "t must be a FOR node";
        boolean res = true;
        Tree varNode = t.getChild(0);
        if(tds.getTypeOfVar(varNode.getText()) != Type.integer) {
        	addError("Line " + t.getLine() + ": the variable « " + varNode.getText() + " » in for must be an integer.");
            res = false;
        }
        for(int i = 1 ; i <= 2 ; i++) {
        Tree expNode = t.getChild(i);
            if(getTypeOfExp(expNode, tds) != Type.integer) {
            	addError("Line " + t.getLine() + ": the expression « " + expNode.getText() + " » in for must be an integer.");
                res = false;
            }
        }
        return res;
    }
}
