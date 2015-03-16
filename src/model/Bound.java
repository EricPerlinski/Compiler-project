package model;

public class Bound {

	private int lb;
	private int ub;
	
	public Bound(int newLb, int newUb){
		this.lb = newLb;
		this.ub = newUb;
	}
	
	public String toString() { 
		return "lowerbound = "+lb+" - upperbound = "+ub;
	}

	public int getLb() {
		return lb;
	}

	public void setLb(int newLb) {
		this.lb = newLb;
	}

	public int getUb() {
		return ub;
	}

	public void setUb(int newUb) {
		this.ub = newUb;
	};
	
	public int getDim() {
		return this.ub-this.lb+1;
	}
	
}
