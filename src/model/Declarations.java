package model;

import java.util.ArrayList;

public class Declarations {
	
	
	/* Attributs de la classe Declarations */
	
	private Type type;
	private String idf;
	private boolean parAdresse;
	private int deplacement;
	private ArrayList<Bound> bounds = new ArrayList<Bound>();

	
	
	
	
	/* Constuctors */ 
	
	public Declarations(Type newType, String newIdf, int newDeplacement){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = new ArrayList<Bound>();
		this.parAdresse = false;
	}
	
	public Declarations(Type newType, String newIdf, int newDeplacement, ArrayList<Bound> newBounds){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = newBounds;
		this.parAdresse = false;
	}
	
	public Declarations(Type newType, String newIdf, int newDeplacement, boolean newParAdresse){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = new ArrayList<Bound>();
		this.parAdresse = newParAdresse;
	}
	
	public Declarations(Type newType, String newIdf, int newDeplacement, ArrayList<Bound> newBounds, boolean newParAdresse){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = newBounds;
		this.parAdresse = newParAdresse;
	}
	
	
	
	/* Getters & Setters */

	public Type getType() {
		return type;
	}

	public void setType(Type newType) {
		this.type = newType;
	}

	public String getIdf() {
		return idf;
	}

	public void setIdf(String newIdf) {
		this.idf = newIdf;
	}

	public int getDeplacement() {
		return deplacement;
	}

	public void setDeplacement(int newDeplacement) {
		this.deplacement = newDeplacement;
	}

	public ArrayList<Bound> getBounds() {
		return bounds;
	}

	public void setBounds(ArrayList<Bound> newBounds) {
		this.bounds = newBounds;
	}
	
	
	/* Bounds Methods */
	
	public Bound getBound(int i){
		return bounds.get(i);
	}
	
	public void addBound(Bound newBound){
		this.bounds.add(newBound);
	}
	
	public void removeBound(int i){
		this.bounds.remove(i);
	}
	
	public void removeBound(Bound boundToRemove){
		this.bounds.remove(boundToRemove);
	}

	public String toString(){
		String str="";
		str += "DECLARATION { \n type : "+type+"\n idf : "+idf+"\n deplacement : "+deplacement+"\n	bounds : ";
		for(int i=0;i<bounds.size();i++){
			str += "\tdim"+i+":"+bounds.get(i).toString()+",\n";
		}
		str+="\n}";
		return str;
	}
	

}
