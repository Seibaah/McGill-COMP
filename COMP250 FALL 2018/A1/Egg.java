package assignment_1;

public class Egg extends MarketProduct {

	private  int numEggs;
	private int dozenPrice;
	
	public Egg(String name, int num, int price) {
		super(name);
		this.numEggs=num;
		this.dozenPrice=price;
	}
	
	public int getCost() {
		double x=this.dozenPrice;
		double cost=(x/12.0)*(this.numEggs);
		return (int) cost;
	}
	
	public boolean equals(Object item) {
		if (item instanceof Egg) {
			if (((Egg) item).numEggs == this.numEggs) {
				if (((Egg) item).dozenPrice == this.dozenPrice) {
					if (this.getName().equals(((Egg) item).getName())) {
						return true;
					} else return false;
				} else return false;
			} else return false;
		}else return false;
	}
}
