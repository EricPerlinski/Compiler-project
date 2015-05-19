package asm;

import model.Declaration;
import model.TDS;
import model.Type;

import org.antlr.runtime.tree.Tree;

import plic.PlicParser;

public class AsmGenerator {

	private String name;
	private Tree ast;
	private TDS tds;

	private StringBuffer asmBuff;

	private int uniqId;
	private int tab=0;



	private static final int INT_SIZE = 2;
	private static final int BOOL_SIZE = 2;

	public AsmGenerator(String name,Tree ast, TDS tds){
		this.ast=ast;
		this.tds=tds;
		this.name=name;
		this.uniqId=0;
		asmBuff=new StringBuffer();
	}
	
	private void emptyLine(){
		addCode("\n");
	}
	
	private void addTab(){
		tab++;
	}
	
	private void removeTab(){
		tab--;
	}

	public int getUniqId(){
		return uniqId++;
	}

	public StringBuffer getCode(){
		return asmBuff;
	}

	private void addCode(String code){
		for(int i=0;i<tab;i++){
			asmBuff.append("\t");
		}
		asmBuff.append(code);
	}

	private void addCodeln(String code){
		addCode(code);
		asmBuff.append("\n");
	}

	public boolean generate(){
		addCodeln("//Prog "+name);
		//modification nom des registres
		addCodeln("//renomage registres");
		addCodeln("SP EQU R15");
		addCodeln("WR EQU R14");
		addCodeln("BP EQU R13");
		addCodeln("EXIT_EXC EQU 64");
		addCodeln("READ_EXC EQU 65");
		addCodeln("WRITE_EXC EQU 66");
		addCodeln("STACK_ADRS EQU 0x1000");
		addCodeln("LOAD_ADRS EQU 0xF000");
		addCodeln("NIL EQU 0");
		//init du programme
		addCodeln("ORG LOAD_ADRS");
		addCodeln("START Root_");
		//creation pile
		addCodeln("stackSize equ 100");
		addCodeln("stack rsb stackSize");

		addCodeln("Root_");


		addCodeln("LDW SP, #STACK_ADRS");
		//addCodeln("LDW BP, SP");

		//0 dans R0
		addCodeln("LDW R0, #0");
		//on met 0 pour l'@ de retour
		addCodeln("STW R0, -(SP)");
		//on met 0 pour le DYN
		addCodeln("STW R0, -(SP)");
		//on charge BP sur DYN
		addCodeln("LDW BP, SP");
		//on met 0 pour STATIC
		addCodeln("STW R0, -(SP)");

		//Generation du corps du programme
		generateRec(tds, ast, -1);

		//fin du programme
		addCodeln("fin");

		//fin du programme
		addCodeln("trp #64");
		return false;
	}

	public int generateRec(TDS tds, Tree ast, int truc){
		//System.out.println(ast.getText());
		boolean bloc=false;
		
		// Label unique nécessaire pour les if
		String label = null;

		// NE PAS MODIFIER
		switch(ast.getType()){
		case PlicParser.BLOC:
		case PlicParser.FUNCTION:
		case PlicParser.PROCEDURE:
			truc++;
			bloc=true;
			tds = tds.getFils().get(truc);
			break;
		}
		// FIN DE NE PAS MODIFIER



		// Generer de l'ASM suivant le type
		switch(ast.getType()){
		case PlicParser.FUNCTION:
			function(ast,tds);
			break;
		case PlicParser.VARIABLE:
			variable(ast, tds);
			break;
		case PlicParser.AFFECTATION:
			affectaction(ast,tds);
			break;
		case PlicParser.RETURN:
			retourne(ast,tds);
			break;
		case PlicParser.IF:
			label = "end_if_"+getUniqId()+"_";
			if_start(ast, tds, label);
			break;
		}
		//fin génération

		//boolean bloc=false;
		int res=(bloc?-1:truc);

		for(int i=0;i<ast.getChildCount();i++){	
			res = generateRec(tds,ast.getChild(i), res);
		}


		switch(ast.getType()){
		case PlicParser.FUNCTION:
			function_end(ast,tds);
			break;
		case PlicParser.IF:
			if_end(ast, tds, label);
			break;
		}

		return (bloc?truc:res);
	}



	public void function_call(Tree ast, TDS tds){
		TDS tds_func = tds.getTdsOfFunction(ast.getChild(0).getText());
		
		//si le pere de la fct appelé c'est moi alors vrai
		boolean fils=tds_func.getPere().getIdf().equals(tds.getIdf());	
		emptyLine();
		addCodeln("//debut appel fonction "+tds_func.getIdf());
		
		//Sauvegarde registres
		for(int i=1;i<=10;i++){
			addCodeln("stw R"+i+",-(SP)");
		}


		// Preparation de l'environnement du programme principal 
		// empile les parametres de la fonction 
		emptyLine();
		for (int i = tds_func.getParams().size()-1; i >= 0 ; i--){
			addCodeln("//empile "+tds_func.getParams().get(i).getIdf());
			boolean b = tds_func.getParams().get(i).getType()==Type.bool;
			expr(ast.getChild(i+1),tds,b);
			addCodeln("STW R0, -(SP)");
		}
		
		emptyLine();
		addCodeln("//chainage static");
		if(fils){
			//le chainage static pointe sur moi
			addCodeln("//c est un fils, donc je serai sont chainage static");
			addCodeln("LDW R2, BP");
		}else{
			//le chainage static pointe sur notre pere
			addCodeln("//c est un frere, donc meme valeur de chainage static");
			addCodeln("LDW R2, (BP)-2");
		}

		//execution fct
		emptyLine();
		addCodeln("//appel fct");
		addCodeln("JSR @"+ast.getChild(0).getText()+"_");



		//Fin de la fonction 
		emptyLine();
		for (int i = 0; i<tds_func.getParams().size() ; i++){
			addCodeln("//depile "+tds_func.getParams().get(i).getIdf());
			addCodeln("LDW R1, (SP)+");
		}
		
		//equivalent au truc du dessus
		// nettoyage de la pile par le programme appelant 	
		//addCodeln("ADQ "+tds_func.getSizeOfParams()+",SP");
		
		//restauration des registres
		emptyLine();
		for(int i=10;i>0;i--){
			addCodeln("ldw R"+i+",(SP)+");
		}
		emptyLine();
		addCodeln("//fin appel fonction "+tds_func.getIdf());

	}


	private void function(Tree ast, TDS tds){
		addTab();
		emptyLine();
		addCodeln("//fonction "+tds.getIdf());
		//saut à la fin de la fct
		addCodeln("JMP #end_"+tds.getIdf()+"_ -$-2");
		//etiquette de fonction
		addCodeln(tds.getIdf()+"_"); 
		
		//sauvegarde de la base / DYN 
		addCodeln("STW BP,-(SP)");
		addCodeln("LDW BP, SP");
		
		// chainage static, R2 est definie par l'appelant
		addCodeln("STW R2, -(SP)");

		//la methode variable() fait ça normalement
		//addCodeln("LDQ "+tds.getSizeOfVar()+",R1"); // R1 = taille donnees locales de fonction appelee
		//addCodeln("SUB SP, R1, SP"); //reserve R1 octets sur la pile pour les variables locales

		//debut corps fonction
		emptyLine();
		addCodeln("//Corps de la fonction");

	}


	private void function_end(Tree as, TDS tds){
		emptyLine();
		addCodeln("//fin corps de la fonction");
		
		//fin de la fonction
		addCodeln ("LDW SP, BP"); // charge SP avec contenu de BP: abandon infos locales
		addCodeln ("LDW BP, (SP)"); // charge BP avec ancien BP
		addCodeln ("ADQ 2,SP"); // supprime l'ancien BP de la pile

		addCodeln("RTS"); // retour au programme appelant
		//label de fin de fct
		addCodeln("end_"+tds.getIdf()+"_");
		removeTab();
		emptyLine();
	}

	private void variable(Tree ast, TDS tds){
		if(ast.getChild(0).getText().equalsIgnoreCase("integer")){
			emptyLine();
			addCodeln("//integer");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln("ADQ -"+INT_SIZE+", SP //var "+ast.getChild(i).getText());
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("boolean")){
			emptyLine();
			addCodeln("//boolean");
			for(int i=1;i<ast.getChildCount();i++){
				addCodeln("ADQ -"+BOOL_SIZE+", SP //var "+ast.getChild(i).getText());
			}
		}else if(ast.getChild(0).getText().equalsIgnoreCase("array")){
			emptyLine();
			addCodeln("//array");
			//TODO
			addCodeln("//TODO");
		}
	}

	private void affectaction(Tree ast, TDS tds){
		emptyLine();
		addCodeln("//****** affectation");
		Tree left = ast.getChild(0).getChild(0);
		Tree right = ast.getChild(1).getChild(0);
		Declaration decl = tds.getDeclarationOfVar(left.getText());
		boolean bool = (decl.getType()==Type.bool);
		expr(right, tds,bool);
		addCodeln("STW R0, (BP)"+decl.getDeplacement());
	}


	/*
	 * Permet de calculer le resultat d'une expression bool/arithmetique.
	 * On lui donne le noeud le plus haut dans l'expression, la tds correspondante
	 * Le parametre bool doit être à vrai si le resultat est mis dans un boolean (une variable, un parametre de fct, un if/while, ...)
	 * si bool est a vrai il transformera systematiquement tout ce qui est different de 0 en 1. Et le 0 reste 0 :D
	 * 
	 * TODO ajouter les tableau dedans.
	 */
	private void expr(Tree ast, TDS tds, boolean bool){
		// met le resultat de l'exp dans R0
		expr_rec(ast, tds,-1,bool);
	}

	private void expr_rec(Tree ast, TDS tds, int num_fils, boolean bool){
		if(ast.getChildCount()==0){
			//bas de l'arbre
			if(ast.getText().equalsIgnoreCase("true") || ast.getText().equalsIgnoreCase("false")){
				boolean b = ast.getText().equalsIgnoreCase("true");
				addCodeln("LDW R0, #"+(b?"1":"0"));
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else if(ast.getText().matches("^\\p{Digit}+$")){
				//un nombre on le met dans le registre directement
				addCodeln("LDW R0, #"+ast.getText());
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else if(tds.getDeclarationOfVar(ast.getText())!=null){
				Declaration decl = tds.getDeclarationOfVar(ast.getText());
				if(decl.getType()==Type.bool || decl.getType()==Type.integer){
					//int ou bool
					int deep = tds.getDeepOfVar(ast.getText());
					//une variable, on le charge depuis la pile
					int depl = decl.getDeplacement();
					addCodeln("LDW WR, BP");
					while(deep>0){
						//on parcours le chainage static
						addCodeln("LDW WR, (WR)");
						deep--;
					}
					addCodeln("LDW R0, (WR)"+depl);
				}else{
					//TODO array (à une vache près c'est pareil que int/bool)
				}
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else{
				//erreur ? 
				System.out.println("ERREUR");
			}
		}else{
			if(ast.getType()==PlicParser.FUNC_CALL){
				//generation de l appel de fonction
				function_call(ast, tds);
				if(num_fils!=-1){
					addCodeln("STW R0, -(SP)");
				}
			}else{
				for(int i=0;i<ast.getChildCount();i++){
					//parcours recursif sur chacun des fils
					expr_rec(ast.getChild(i), tds, i, bool); 
				}
				
				//Si c'est une operation +/-/* 
				if(ast.getText().equalsIgnoreCase("+") || ast.getText().equalsIgnoreCase("-") || ast.getText().equalsIgnoreCase("*")){
					char c = ast.getText().charAt(0);
					//ternaire de la mort :P
					String opp= (c=='+'?"ADD":(c=='-'?"SUB":"MUL"));
					addCodeln("LDW R1, (SP)+");
					addCodeln("LDW R2, (SP)+");
					addCodeln(opp+" R1, R2, R"+(num_fils+1));
					if(bool){
						//si c'est un bool on met le resultat à 0 ou 1
						astuceBool(num_fils);
					}
					if(num_fils!=-1){
						addCodeln("STW R"+(num_fils+1)+", -(SP)");
					}

				//unaire
				}else if(ast.getText().equalsIgnoreCase("unaire")){
					if(bool){
						//si c'est un bool on met le resultat à 0 ou 1
						addCodeln("LDW R"+(num_fils+1)+", (SP)+");
						//on enleve un (0->-1, 1->0)
						addCodeln("ADI R"+(num_fils+1)+", R"+(num_fils+1)+", #-1");
						//on met le resultat à 0 ou 1
						astuceBool(num_fils);
					}else{
						addCodeln("LDW R1, (SP)+");
						addCodeln("NEG R1, R"+(num_fils+1));
					}
					if(num_fils!=-1){
						addCodeln("STW R0, -(SP)");
					}
					
				//comparaison
				}else if(ast.getText().equalsIgnoreCase("==") || ast.getText().equalsIgnoreCase("!=") ||ast.getText().equalsIgnoreCase(">") ||ast.getText().equalsIgnoreCase(">=") ||ast.getText().equalsIgnoreCase("<") ||ast.getText().equalsIgnoreCase("<=")){
					String opp="";
					if(ast.getText().equalsIgnoreCase("==")){
						opp="JEQ";
					}else if(ast.getText().equalsIgnoreCase("!=")){
						opp="JNE";
					}else if(ast.getText().equalsIgnoreCase(">")){
						opp="JGT";
					}else if(ast.getText().equalsIgnoreCase(">=")){
						opp="JGE";
					}else if(ast.getText().equalsIgnoreCase("<")){
						opp="JLW";
					}else if(ast.getText().equalsIgnoreCase("<=")){
						opp="JLE";
					}
					addCodeln("LDW R1, (SP)+");
					addCodeln("LDW R2, (SP)+");
					addCodeln("CMP R1, R2");
					int id = getUniqId();
					String label = "equals_"+id+"_";
					addCodeln(opp+" #"+label);
					addCodeln("LDW R0, #0");
					addCodeln("LDW R0, #0");
					addCodeln(label);
					addCodeln("LDW R0, #1");
					addCodeln("end_"+label);
				}
			}
		}
	}
	
	private void astuceBool(int numR){
		//permet de mettre le registre numR+1 à 1 si != 0
		addCodeln("//astuce pour les boolean, si c est different de zero alors on met à 1");
		String label = "bool_"+getUniqId()+"_";
		addCodeln("STW R3, -(SP)");
		addCodeln("LDW R3, #0");
		addCodeln("CMP R"+(numR+1)+", R3");
		addCodeln("JEQ #"+label+" -$-2");
		addCodeln("LDW R"+(numR+1)+", #1");
		addCodeln("LDW R3, (SP)+");
		addCodeln(label);
		addCodeln("//fin de petite astuce");
	}
	
	
	private void retourne(Tree ast, TDS tds){
		//TODO code du return
	}
	
	public void if_start(Tree ast, TDS tds, String label){
		emptyLine();
		addCodeln("// Structure IF");
		expr(ast.getChild(0), tds, true);
		//addCodeln("STW R1, -(SP)");
		addCodeln("LDW R1, #0");
		addCodeln("CMP R0, R1");
		//addCodeln("LDW R1, (SP)+");
		addCodeln("JEQ #"+label+" -$-2");
	}

	public void if_end(Tree ast, TDS tds, String label){
		emptyLine();
		addCodeln(label);
	}
}
