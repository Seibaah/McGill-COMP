

public class BridgeUtilities {
	
	private BridgeUtilities() {				//Private constructor to prevent the creation of objects of type BridgeUtilities outside this class
	}
	
	 private static int countValue(Card[] hand, int cardValue) {		//Method to count how many cards of a certain value are in a hand
		 int count=0;													//Counter variable
		 for (int i=0; i<hand.length; i++) {							//For loop to search through the hand for the specific card
			 if (hand[i].getValue()==cardValue) {						//If we find it the counter increases by one
				 count++;
			 }
		 }
		 return count;													//We return the counter
	 }
	 
	 private static int countSuits(Card[] hand, String suit) {			//Method to count how many cards of a certain suit are in the hand
		 int quantity=0;												//Counter variable
		 for (int i=0; i<hand.length; i++) {							//For loop to search through the hand for the specific card suit
			 String currentSuit=(hand[i].getSuit()).toLowerCase();		//If we find it the counter increases by one; toLowerCase() is used to bypass capitalization discrepancies
				if(currentSuit.equals(suit.toLowerCase())) {
					quantity++;
				}
		 }
		 return quantity;												//We return the counter
	 }
	 
	 public static int countPoints(Card[] hand) {										//Method to count the points of the hand
		 final String h="hearts", s="spades", c="clubs", d="diamonds";					//Immutable string with suit names for the cards
		 int numAces=countValue(hand, 1), ptsAces=numAces*4;							//Lines 33 to 36 have many integer variables that store the quantity of valuable cards in the hand
		 int numKings=countValue(hand, 13), ptsKings=numKings*3;						//Then the points are calculated with a multiplication and are stored in a new variable
		 int numQueens=countValue(hand, 12), ptsQueens=numQueens*2;						//We used the countValue method
		 int numJacks=countValue(hand, 11), ptsJacks=numJacks*1;
		 int numHearts=countSuits(hand, h), numSpades=countSuits(hand, s), 				//In lines 37-38 we store how many suits of a single type there are in a hand
				 numClubs=countSuits(hand, c), numDiamonds=countSuits(hand, d);			//We used the countSuits method
		 int totalPts=ptsAces+ptsKings+ptsQueens+ptsJacks;								//Total points are calculated
		 if (numHearts>4) {																//The next 4 if statements test if there is more than 4 of the suit per hand
			 totalPts+=(numHearts-4);													//If it's the case we add the remaining points to the total as required by the game's rule
		 }
		 if (numSpades>4) {
			 totalPts+=(numSpades-4);
		 }
		 if (numClubs>4) {
			 totalPts+=(numClubs-4);
		 }
		 if (numDiamonds>4) {
			 totalPts+=(numDiamonds-4);
		 }
		 return totalPts;																//We return the total points of the hand
	 }
}
