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
			System.out.println("Erreur d'affectation : "+tfg.toString()+" != "+tfd.toString());
			return false;
		}
	}
	
	// Contrôle le nombre de paramètres passés en argument lors de l'appel d'une fonction ou d'une procédure
	// Noeud racine FUNC_CALL
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
	// Noeud racince FUNC_CALL
	public static boolean check_func_call(Tree sub_tree, TDS tds) {
		TDS tdsCurrent = tds.getTdsOfFunction(sub_tree.getChild(0).getText());
		Type typeCurrent = null;
		boolean res = true;
		for (int i=1; i<sub_tree.getChildCount()-1; i++) {
			typeCurrent = getTypeOfExp(sub_tree.getChild(i), tds);
			if (typeCurrent != tdsCurrent.getParams().get(i-1).getType()) {
				System.out.println("Erreur d'appel de fonction : Le type du "+i+"eme paramètre est "+typeCurrent.toString()+" il devrait être de type "+tdsCurrent.getParams().get(i-1).getType().toString());
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
			System.out.println("Erreur de type de retour de fonction : La fonction "+sub_tree.getChild(0).getText()+" a pour type de retour "+typeDefined.toString()+" mais vous retournez un "+typeCurrent);
			res = false;
		}
		return res;
	}
	
	// Contrôle le type des conditions dans les structures if : doit être un boolean
	// Noeud racine CONDITION ou IF je sais plus
	public static boolean check_condition_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		if (getTypeOfExp(sub_tree.getChild(0), tds)!=Type.bool) {
			System.out.println("Erreur de type de condition : La condition n'est pas de type boolean");
			res = false;
		}
		return res;
	}
	
	// Contrôle le type des bornes lors des boucles for : doivent être entiers
	// Noeud racine FOR
	public static boolean check_forloop_type(Tree sub_tree, TDS tds) {
		boolean res = true;
		for (int i=1; i<sub_tree.getChildCount()-1; i++) {
			if (getTypeOfExp(sub_tree.getChild(i), tds)!=Type.integer) {
				System.out.println("Erreur de type de boucle : Le "+i+"ème paramètre n'est pas de type integer");
				res = false;
			}
		}
		return res;
	}
	
	// Contrôle si une fonction n'a pas 2 paramètres avec le même id et le même type
	// Noeud racine je sais pas encore je suis dessus
	public static boolean check_func_params(Tree sub_tree, TDS tds) {
		boolean res = true;
		
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
				System.out.println("Opération "+name+" avec un "+t1.toString()+" et un "+t2.toString());
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
	
	   // t = arbre avec comme racine le noeud ARRAY
    public static boolean isGoodTypesInArrayDimensions(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be the ARRAY node";
        boolean res = true;
        for (int i = 1; i < t.getChildCount(); i++) {
            Tree boundNode = t.getChild(i);
            if (getTypeOfExp(boundNode, tds) != Type.integer) {
                    System.out.println("Ligne " + boundNode.getLine() + ": Variable " + boundNode.getText() + ", type mismatch, must be an integer.");
                    res = false;
            }
        }
        return res;
    }
    // t = arbre avec comme racine le noeud ARRAY
    public static boolean isGoodNumberOfIndexesInArrayDimensions(Tree t, TDS tds) {
        assert !t.getText().equalsIgnoreCase("ARRAY") : "t must be the ARRAY node";
        Tree idfNode = t.getChild(0);
        int numberOfIndexes = t.getChildCount() - 1; //-1 pour enlever l'idf
        Declaration arrayDecl = tds.getDeclarationOfVar(idfNode.getText());
        if(arrayDecl != null && arrayDecl.getBounds().size() != numberOfIndexes) {
            System.out.println("Ligne " + t.getLine() + ": Variable " + idfNode.getText() + ", wrong number of index.");
            return false;
        }
        return true;
    }
}
