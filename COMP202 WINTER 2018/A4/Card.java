

public class Card {
	private String suit;																		//Our card needs a suit attribute
	private int value;																			//It also needs a value attribute
	
	public Card(int myVal, String mySuit) {														//Constructor
		final String h="hearts", s="spades", c="clubs", d="diamonds";							//Immutable string with suit names for the cards
		if (myVal<1||myVal>13) {																//If statement that prevents the assigning of an invalid card value to a card object, we throw an exception if this happens
			throw new IllegalArgumentException ("This isn't a valid card");
		}else this.value=myVal;																	//If the value is valid we assign it to the card
		if ((mySuit.toLowerCase().equals(h))||(mySuit.toLowerCase().equals(s))					//If statement that checks if the suit value is valid, if it is we proceed with the assigning, otherwise we throw an exception
				||(mySuit.toLowerCase().equals(c))||(mySuit.toLowerCase().equals(d))) {			//toLowerCase() is used to bypass capitalization discrepancies
			this.suit=mySuit;
		}else throw new IllegalArgumentException ("This isn't a valid card suit"); 
	}
	public String getSuit() {						//Getter method to get the Card suit
		return suit;
	}
	
	public int getValue() {							//Getter method to get the Card value
		return value;
	}
	
	public void print() {							//Method to print a Card value	
		System.out.print(value);
	}
}
