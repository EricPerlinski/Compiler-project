package model;

import java.util.ArrayList;

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
		System.out.println(this);
		for(int i=0;i<fils.size();i++){
			fils.get(i).afficherTds();
		}
	}
	
	
	
	
	
}
