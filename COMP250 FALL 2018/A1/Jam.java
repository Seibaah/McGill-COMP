package assignment_1;

public class Jam extends MarketProduct {

	private int numJams;
	private int priceJar;
	
	public Jam(String name, int n, int price) {
		super(name);
		this.numJams=n;
		this.priceJar=price;
	}
	
	public int getCost() {
		int cost=(this.numJams)*(this.priceJar);
		return cost;
	}
	
	public boolean equals(Object item) {
		if (item instanceof Jam) {
			if (((Jam) item).numJams == this.numJams) {
				if (((Jam) item).priceJar == this.priceJar) {
					if (this.getName().equals(((Jam) item).getName())) {
						return true;
					} else return false;
				} else return false;
			} else return false;
		}else return false;
	}
}
