package model;

import java.util.ArrayList;

public class Declaration {
	
	
	/* Attributs de la classe Declarations */
	
	private Type type;
	private String idf;
	private boolean parAdresse;
	private int deplacement;
	private ArrayList<Bound> bounds = new ArrayList<Bound>();

	
	
	
	
	/* Constuctors */ 
	
	public Declaration(Type newType, String newIdf, int newDeplacement){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = new ArrayList<Bound>();
		this.parAdresse = false;
	}
	
	public Declaration(Type newType, String newIdf, int newDeplacement, ArrayList<Bound> newBounds){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = newBounds;
		this.parAdresse = false;
	}
	
	public Declaration(Type newType, String newIdf, int newDeplacement, boolean newParAdresse){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = new ArrayList<Bound>();
		this.parAdresse = newParAdresse;
	}
	
	public Declaration(Type newType, String newIdf, int newDeplacement, ArrayList<Bound> newBounds, boolean newParAdresse){
		this.type = newType;
		this.idf = newIdf;
		this.deplacement = newDeplacement;
		this.bounds = newBounds;
		this.parAdresse = newParAdresse;
	}
	
	
	
	/* Getters & Setters */

	public boolean getParAdresse(){
		return parAdresse;
	}
	
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
		String parad = parAdresse?"\npasse par adresse\n":"";
		str += "DECLARATION { \n type : "+type+"\n idf : "+idf
				+parad
				+"\n deplacement : "+deplacement+"\n	bounds : ";
		for(int i=0;i<bounds.size();i++){
			str += "\tdim"+i+":"+bounds.get(i).toString()+",\n";
		}
		str+="\n}";
		return str;
	}

	public String toDot(){
		StringBuffer str = new StringBuffer();

		str.append(""+(parAdresse?"adr ":"")+type+":<B>"+idf+"</B>");	
		for(int i=0;i<bounds.size();i++){
			str.append("["+bounds.get(i).getLb()+".."+bounds.get(i).getUb()+"]");
		}
		str.append(" # "+deplacement+"");	

		return str.toString();
	}
	
	/* Methods */
	
	public int getSize() {
		switch (this.getType()) {
			case integer:
				return 2;
			case bool:
				return 2;
			case array:
				int size = 1;
				for (int i=0; i<bounds.size(); i++) {
					size *= bounds.get(i).getDim();
				}
				return size*2;
			default:
				return 0;			
		}
	}
	

}
