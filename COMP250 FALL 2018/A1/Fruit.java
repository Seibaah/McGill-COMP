package assignment_1;

public class Fruit extends MarketProduct {
	
	private double weight;
	private int priceKilo;
	
	public Fruit(String name, double w, int price) {
		super(name);
		this.weight=w;
		this.priceKilo=price;
	}
	
	public int getCost() {
		double cost=(this.priceKilo)*(this.weight);
		return (int) cost;
	}
	
	private double getExactWeight() {
		return this.weight;
	}

	public boolean equals(Object item) {
		if (item instanceof Fruit) {
			double eW=((Fruit) item).getExactWeight();
			if (this.weight == eW) {
				if (((Fruit) item).priceKilo == this.priceKilo) {
					if (this.getName().equals(((Fruit) item).getName())) {
						return true;
					} else return false;
				} else return false;
			} else return false;
		} else return false;
	}
}
