package assignment_1;

public class Basket {
	
	private MarketProduct[] cart;
	
	public Basket() {
		cart = new MarketProduct[4];
	}
	
	public MarketProduct[] getProducts() {
		MarketProduct[] sCopy= new MarketProduct[this.cart.length];
		for (int i=0; i<this.cart.length; i++) {
			sCopy[i]=this.cart[i];
		}
		return sCopy;
	}
	
	public void add(MarketProduct item) {
		int amount=this.getAmountOfProducts();
		if (amount>=this.cart.length) {
			this.cart=this.resize();
		}
		//this.cart[amount]=item;
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i]==null) {
				cart[i]=item;
				break;
			}
		}
	}
	
	public boolean remove(MarketProduct item) {
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i]==null) {
				continue;
			}
			if ((this.cart[i]).equals(item)){
				this.cart[i]=null;
				this.shiftCart(i);
				return true;
			}
		}
		return false;
	}
	
	private void shiftCart(int start) {
		for (int i=start; i<this.cart.length-1; i++) {
				this.cart[i]=this.cart[i+1];
		}
		this.cart[this.cart.length-1]=null;
	}
	
	public void clear() {
		for (int i=0; i<this.cart.length; i++) {
			this.cart[i]=null;
		}
	}
	
	public int getNumOfProducts() {
		return getAmountOfProducts();
	}
	
	private int getAmountOfProducts() {
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i]==null) {
				return i;
			}
		}
		return this.cart.length;
	}
	
	public int getSubTotal() {
		int pSum=0;
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i]==null) {
				continue;
			}
			int temp=this.cart[i].getCost();
			pSum=pSum+temp;
		}
		return pSum;
	}
	
	public int getTotalTax() {
		int pSum=0;
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i] instanceof Jam) {
				pSum+=this.cart[i].getCost();
			}
		}
		double taxSum=(pSum*0.15);
		return (int) taxSum;
	}
	
	public int getTotalCost() {
		int finalCost=(this.getSubTotal())+(this.getTotalTax());
		return finalCost;
	}
	
	public String toString() {
		String finalReceipt="", fCost;
		for (int i=0; i<this.cart.length; i++) {
			if (this.cart[i]==null) {
				continue;
			}
			String name=this.cart[i].getName();
			double cost=(this.cart[i].getCost())/100.0;
			if (cost<=0) {
				fCost="-";			
			} else {
				fCost=String.format("%.2f", cost);
			}
			finalReceipt+=name+ "\t" +fCost+ "\n";
		}
		double subT=(this.getSubTotal())/100.0;
		String s=String.format("%.2f", subT);
		finalReceipt+="\nSubtotal \t"+ s+"\n";
		
		double subTax=(this.getTotalTax())/100.0;
		String s2=String.format("%.2f", subTax);		
		finalReceipt+="Total Tax \t"+ s2 +"\n";
		
		double totC=(this.getTotalCost()/100.0);
		String s4=String.format("%.2f", totC);
		finalReceipt+="\nTotal Cost \t"+ s4;
		
		return finalReceipt;
		}
	
	private MarketProduct[] resize() {
		int newSize=(this.cart.length)*2, oldSize=this.cart.length;
		MarketProduct[] cartCopy= new MarketProduct[newSize];
		for (int i=0; i<oldSize; i++) {
			cartCopy[i]=this.cart[i];
		}
		return cartCopy;
	}
}
