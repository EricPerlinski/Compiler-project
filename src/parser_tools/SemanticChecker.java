package parser_tools;

import model.Declaration;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class SemanticChecker{

	public static boolean check(Tree ast, TDS tds){
		boolean result=false;
		
		result = checkRec(ast, tds);
		
		return result;
	}
	
	public static boolean checkRec(Tree ast, TDS tds){
		System.out.println(ast.getText()+"("+tds.getNbImb()+":"+tds.getNbReg()+")");
		
		
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
		case PlicParser.FUNCTION:
		case PlicParser.PROCEDURE:
			check_func_params(ast, tds);
		}
		
		
		
		//fin test
		
		
		boolean res=false;
		TDS currentTDS = tds;
		int nbTds=-1;
		boolean bloc=false;
		for(int i=0;i<ast.getChildCount();i++){
			switch(ast.getChild(i).getType()){
			case PlicParser.FUNCTION:
			case PlicParser.PROCEDURE:
			case PlicParser.BLOC:
				nbTds++;
				bloc=true;
				break;
			default:
				bloc=false;
			}
			
			SemanticChecker.checkRec(ast.getChild(i), (bloc ? currentTDS.getFils().get(nbTds) : currentTDS));
			bloc=false;
		}
		
		return res;
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
			if(tfg==null){
				System.out.println(fg.getChild(0).getText()+" n'existe pas");

			}
			if(tfd==null){
				if(fd.getChild(0).getType()==PlicParser.FUNC_CALL){
					System.out.println("Ligne "+sub_tree.getLine()+": erreur fonction "+fd.getChild(0).getChild(0).getText()+" n'existe pas");
				}else{
					System.out.println("Ligne "+sub_tree.getLine()+": "+fd.getChild(0).getText()+" n'existe pas");
				}
			}
			if(tfd!=null && tfg!=null){
				System.out.println("Ligne "+sub_tree.getLine()+": erreur d'affectation : "+tfg.toString()+" != "+tfd.toString());
			}

			return false;
		}
	}
	
	// Contrôle le nombre de paramètres passés en argument lors de l'appel d'une fonction ou d'une procédure
	// Noeud racine FUNC_CALL
	public static boolean check_nbparams_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		boolean res = true;
		if (tdsCurrent.getParams().size()!=sub_tree.getChildCount()-1) {
			System.out.println("Ligne "+sub_tree.getLine()+": erreur d'appel de fonction -> Mauvais nombre de paramètres");
			res = false;
		}
		return res;
	}
	
	// Contrôle la cohérence des types des paramètres à l'appel des fonctions et des procédures
	// Noeud racince FUNC_CALL
	public static boolean check_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		Type typeCurrent = null;
		boolean res = true;
		for (int i=1; i<sub_tree.getChildCount()-1; i++) {
			typeCurrent = getTypeOfExp(sub_tree.getChild(i), tds);
			if (typeCurrent != tdsCurrent.getParams().get(i-1).getType()) {
				System.out.println("Ligne "+sub_tree.getLine()+": erreur d'appel de fonction -> Le type du "+i+"eme paramètre est "+typeCurrent.toString()+" il devrait être de type "+tdsCurrent.getParams().get(i-1).getType().toString());
				res = false;
			}
		}
		return res;
	}
	
	// Contrôle la cohérence entre le type de retour d'une fonction et ce qui est effectivement retourné
	// Noeud racine RETURN
	public static boolean check_return_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		Type typeCurrent = getTypeOfExp(sub_tree.getChild(0),tds);
		Type typeDefined = TDS.str2type((tds.getTypeRet()));
		if (typeCurrent!=typeDefined) {
			System.out.println("Ligne "+sub_tree.getLine()+": erreur de type de retour de fonction -> La fonction "+sub_tree.getChild(0).getText()+" a pour type de retour "+typeDefined.toString()+" mais vous retournez un "+typeCurrent);
			res = false;
		}
		return res;
	}
	
	// Contrôle le type des conditions dans les structures if : doit être un boolean
	// Noeud racine CONDITION ou IF je sais plus
	public static boolean check_condition_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		if (getTypeOfExp(sub_tree.getChild(0), tds)!=Type.bool) {
			System.out.println("Ligne "+sub_tree.getLine()+": erreur de type de condition : La condition n'est pas de type boolean");
			res = false;
		}
		return res;
	}
	
	// Contrôle si une fonction n'a pas 2 paramètres avec le même id et le même type
	// Noeud racine PARAMS
	public static boolean check_func_params(Tree sub_tree, TDS tds) {
		boolean res = true;
		for (int i=0; i<sub_tree.getChildCount()-2; i++) {
			String current = sub_tree.getChild(i).getChild(1).getText();
			for (int j=i+1; j<sub_tree.getChildCount()-1; j++) {
				if (current==sub_tree.getChild(j).getChild(1).getText()) {
					System.out.println("Ligne "+sub_tree.getLine()+": erreur de prototypage de fonction -> 2 paramètres ont le même nom");
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
				System.out.println("Opération "+name+" avec un "+ (t1==null ? "null" : t1.toString()) +" et un "+(t2==null ? "null" : t2.toString()));
			}
		}else if(name.equals("==") || name.equals("!=")|| name.equals("<")|| name.equals("<=")|| name.equals(">")|| name.equals(">=")){
			Type t1 = getTypeOfExp(t.getChild(0), tds);
			Type t2 = getTypeOfExp(t.getChild(1), tds);
			if(t1!=null && t2!=null && t1==t2 /*&& t1==Type.bool*/){
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
			System.out.println("Erreur name : "+name);
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
                    System.out.println("Line " + boundNode.getLine() + ": Variable " + boundNode.getText() + ", type mismatch, must be an integer.");
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
            System.out.println("Line " + t.getLine() + ": Variable " + idfNode.getText() + ", wrong number of index.");
            return false;
        }
        return true;
    }
    
    //À appeler impérativement sur un noeud array avant tout autre contrôle concernant les array
    // t = arbre avec comme racine le noeud ARRAY
    public static boolean isARealArrayType(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be a ARRAY node";
        Tree idfNode = t.getChild(0);
        if (getTypeOfExp(idfNode, tds) != Type.array) {
            System.out.println("Line " + t.getLine() + ": Variable " + idfNode.getText() + ", the type of the expression must be an array type.");
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
            System.out.println("Line " + t.getLine() + ": the variable " + varNode.getText() + " in for must be an integer.");
            return false;
        }
        for(int i = 1 ; i <= 3 ; i++) {
        Tree expNode = t.getChild(i);
            if(getTypeOfExp(expNode, tds) != Type.integer) {
                System.out.println("Line " + t.getLine() + ": the expression " + expNode.getText() + " in for must be an integer.");
                res = false;
            }
        }
        return res;
    }
}
