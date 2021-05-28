package assignment_1;

public class Customer {
		
	private String name;
	private int balance;
	private Basket trolley;
	
	public Customer(String name, int b) {
		trolley = new Basket();
		this.name=name;
		this.balance=b;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getBalance() {
		return this.balance;
	}
	
	public Basket getBasket() {
		return this.trolley;
	}
	
	public int addFunds(int add) {
		if (add<0) {
			throw new IllegalArgumentException ("Can't add negative funds");
		} 
		else {
			this.balance+=add;
			return this.balance;
		}
	}
	
	public void addToBasket(MarketProduct item) {
		this.trolley.add(item);
	}
	
	public Boolean removeFromBasket(MarketProduct item) {
		return this.trolley.remove(item);
	}
	
	public String checkOut() {
		String bill = this.trolley.toString();
		if (this.balance<this.trolley.getTotalCost()) {
			throw new IllegalStateException();
		} else {
			this.balance-=this.trolley.getTotalCost();
			return bill;
		}
	}
}
