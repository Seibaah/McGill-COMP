

public class Game {								
	public static void main (String[] args) {									//Main method
		Deck newDeck= new Deck();												//Creating an object Deck
		newDeck.shuffle();														//Calling shuffle method on object Deck
		System.out.println();
		for (int i=0; i<4; i++) {												//For to iterate through the 4 players
			Card[] hand = new Card[13];											//Creating an object hand of type Card[] with 13 slots for the cards
			hand=newDeck.dealHand(13, i);										//Using dealHand we give each player their hand
			System.out.println("Player " + (i+1) 
					+ " was dealt the following hand of cards: ");
			for (int j=0; j<13; j++) {											//For loop to iterate through the cards in the hand
			hand[j].print();
			System.out.print(" of ");
			System.out.print(hand[j].getSuit()+ ", ");
			}
			System.out.println();
			int pts=BridgeUtilities.countPoints(hand);							//Calling on countPoints in Class Bridge Utilities to get the total pts of the hand
			System.out.println("Their hand is worth "+ pts + " points");
			System.out.println();
		}
	}

}
