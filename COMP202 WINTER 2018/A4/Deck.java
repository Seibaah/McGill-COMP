

import java.util.Random;

public class Deck {
	private Card[] deck;										//Creating an attribute named deck of type Card[]
	private final String[] suits = {"hearts", "spades",			//Creating an immutable string containing the names of the card suits
			"clubs", "diamonds" };
	
	public Deck() {												//Constructor
		this.deck= new Card[52];								//deck will store 52 instances of the object Card
		for (int j=0; j<4; j++) {								//For loop to go through the 4 suits of cards
			for (int k=0; k<13; k++) {							//For loop to go through the 13 possible values for the cards per family
				Card newCard= new Card(k+1, suits[j]);			//We create a newCard object with the corresponding parameters; k+1 is the value (we add 1 because 0 isn't a valid value) 
				deck[k+(13*j)]=newCard;							//We store the newCard in its corresponding position in deck (k+(13*j)) serves this purpose
			}
		}
	}
	
	public void shuffle() {												//Shuffle method
		int seed=123;													//Initializing a seed
		Random num = new Random(seed);									//Creating an object random
		for (int i=0; i<1000; i++) {									//For loop that will switch 2 random cards' place inside the deck. This happens 1000 times.
			int rand_1=num.nextInt(52), rand_2=num.nextInt(52);
			Card temp;
			temp=deck[rand_1];											//Using a temporary Card object to perform the switch
			deck[rand_1]=deck[rand_2];
			deck[rand_2]=temp;
		}
	}
	
	public Card[] dealHand(int handSize, int player_id) {								//Method to distribute the cards to each player			
		Card[] hand = new Card[handSize];												//Creating a hand object of type Card[] with size defined by handSize
		int count=0;																	//Counter variable that designates where the card from the deck will be stores in the hand
		if ((52-handSize*player_id)<handSize) {											//If statement to test if there will be enough cards for every hand, if not we throw an exception
			throw new IllegalArgumentException ("There arent enough cards "
					+ "in the deck to distribute this hand");
		}
		for (int i=handSize*player_id; i<52-(handSize*(4-1-player_id)); i++) {			//For loop that starts on the first card corresponding to the new hand (ie starting point for players vary, 0 for player 1, 13 for player 2, etc.
				hand[count]=deck[i];													//We assign the card in deck[i] to the hand in position count
				count++;																//count increases so next time the card goes to the next available spot on the hand
		}
		return hand;																	//We return the distributed hand
	}
}
