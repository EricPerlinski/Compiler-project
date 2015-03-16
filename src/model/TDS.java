package model;

import java.util.*;

public class TDS {

	private ArrayList<Declarations> var; // Liste de déclarations pour les variables
	private ArrayList<Declarations> params; // Liste de déclarations pour les paramètres
	private ArrayList<TDS> fils; // lien vers les TDS des fils
	private TDS pere; // lien vers la TDS du père
	private int nbImb; // numero d'imbrication
	private int nbReg; // numero de region
	private String idf; //idf du bloc si fonction ou procedure
	
	
	/* Constructors */
	
	public TDS (ArrayList<Declarations> newVar, ArrayList<Declarations> newParams, ArrayList<TDS> newFils, TDS newPere, int newNbImb, int newNbReg){
		var = newVar;
		params = newParams;
		fils = newFils;
		pere = newPere;
		nbImb = newNbImb;
		nbReg = newNbReg;
	}
	
	public TDS (TDS newPere, int newNbImb, int newNbReg){
		var = new ArrayList<Declarations>();
		params = new ArrayList<Declarations>();
		fils = new ArrayList<TDS>();
		pere = newPere;
		nbImb = newNbImb;
		nbReg = newNbReg;
	}
	
	public TDS (int newNbImb, int newNbReg){
		var = new ArrayList<Declarations>();
		params = new ArrayList<Declarations>();
		fils = new ArrayList<TDS>();
		pere = null;
		nbImb = newNbImb;
		nbReg = newNbReg;
	}
	
	/* Methods */
	
	public void addParam (Declarations newParam){
		this.params.add(newParam);
	}

	public void addFils (TDS tds){
		this.fils.add(tds);
	}
	
	public void addVar (Declarations newVar){
		this.var.add(newVar);
	}

	/* Getters & Setters */ 
	
	public ArrayList<Declarations> getVar() {
		return var;
	}

	public void setVar(ArrayList<Declarations> var) {
		this.var = var;
	}

	public ArrayList<Declarations> getParams() {
		return params;
	}

	public void setParams(ArrayList<Declarations> params) {
		this.params = params;
	}

	public ArrayList<TDS> getFils() {
		return fils;
	}

	public void setFils(ArrayList<TDS> fils) {
		this.fils = fils;
	}

	public TDS getPere() {
		return pere;
	}

	public void setPere(TDS pere) {
		this.pere = pere;
	}

	public int getNbImb() {
		return nbImb;
	}

	public void setNbImb(int nbImb) {
		this.nbImb = nbImb;
	}

	public int getNbReg() {
		return nbReg;
	}

	public void setNbReg(int nbReg) {
		this.nbReg = nbReg;
	}

	public void setIdf(String idf){
		this.idf=idf;
	}

	public String getIdf(){
		return this.idf;
	}


	/* ToString */
	
	@Override
	public String toString() {
		String str= "\nTDS [\nidf=" +idf+  "\nvar=\n"; 
		for(int i=0;i<var.size();i++){
			str+=var.get(i).toString()+",\n";
		}
		str+= "\nparams=";
		for(int i=0;i<params.size();i++){
			str+=params.get(i).toString()+",\n";
		}
		str += "\nnbImb=" + nbImb + ", nbReg=" + nbReg+ "\n]\n";
		return str;
	}

	public void afficherTds(){
		System.out.println(this.toString());
		for(int i=0;i<fils.size();i++){
			fils.get(i).afficherTds();
		}
	}

	public String toDot(){
		StringBuffer str=new StringBuffer();
		str.append("digraph {rankdir = TB ; node[shape=none]; edge[tailclip=false];\n");

		Stack<Integer> stack = new Stack<Integer>();
		str.append(tds2dot(0));
		stack.push(new Integer(0));
		int current=0;
		afficherTdsDotRec(str,stack,1);

		str.append("\n}");
		return str.toString();
	}

	public void afficherTdsDot(){
		
		System.out.println(toDot());
	}

	public int afficherTdsDotRec(StringBuffer str,Stack<Integer> stack,int max){
		
		for(int i=0;i<fils.size();i++){
			max++;
			Integer currentO=(stack.peek());
			int current = ((int)currentO)+1;
			str.append("\nelement_"+stack.peek()+":2 -> element_"+max+":port\n");
			str.append(fils.get(i).tds2dot(max));
			stack.push(new Integer(max));
			max = fils.get(i).afficherTdsDotRec(str,stack,max+1);
			stack.pop();
		}
		return max;
		
	}


	public String tds2dot(int index){
		StringBuffer str = new StringBuffer();
		str.append("element_"+index+"[label=<\n"+
			"<TABLE BORDER=\"0\" CELLSPACING=\"0\">\n"+
			"<TR><TD PORT=\"port\" BORDER=\"1\" WIDTH=\"100\"></TD></TR>\n"+
			"<TR><TD BORDER=\"1\" WIDTH=\"100\">"+idf+"</TD></TR>\n");

		str.append("<TR><TD BORDER=\"1\" WIDTH=\"100\">nbReg : "+nbReg+"</TD></TR>\n");
		str.append("<TR><TD BORDER=\"1\" WIDTH=\"100\">nbImb : "+nbImb+"</TD></TR>\n");

		for(int i=0;i<params.size();i++){
			str.append("<TR><TD BORDER=\"1\" WIDTH=\"100\">Param_"+i+" : "+params.get(i).toDot()+"</TD></TR>\n");
		}

		for(int i=0;i<var.size();i++){
			str.append("<TR><TD BORDER=\"1\" WIDTH=\"100\">var_"+i+" : "+var.get(i).toDot()+"</TD></TR>\n");
		}

		str.append("<TR><TD PORT=\"2\" BORDER=\"1\" WIDTH=\"40\"> </TD></TR></TABLE>>]\n");
		return str.toString();
	}
	
	
	
	
	
}
