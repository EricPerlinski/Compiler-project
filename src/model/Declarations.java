package model;

import java.util.ArrayList;

public class Declarations {
	
	
	/* Attributs de la classe Declarations */
	
	private Type type;
	private String idf;
	private int deplacement;
	private ArrayList<Bound> bounds = new ArrayList<Bound>();
	
	
	
	/* Constuctors */ 
	
	public Declarations(Type newType, String newIdf, int newDeplacement){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = new ArrayList<Bound>();
	}
	
	public Declarations(Type newType, String newIdf, int newDeplacement, ArrayList<Bound> newBounds){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = newBounds;
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
	

}
