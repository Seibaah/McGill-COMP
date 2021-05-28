package assignment_1;

public class SeasonalFruit extends Fruit {

	public SeasonalFruit(String name, double w, int price) {
		super(name, w, price);
	}
	
	public int getCost() {
		double cost=super.getCost();
		cost*=0.85;
		int roundedCost=(int) cost;
		return roundedCost;
	}
}
