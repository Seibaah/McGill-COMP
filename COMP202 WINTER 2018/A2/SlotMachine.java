

public class SlotMachine
{
    public static void main(String[] args) {
    	double money=Double.parseDouble(args[0]), bet=Double.parseDouble(args[1]), goal=Double.parseDouble(args[2]); 	//Initializing 3 variables and assigning them to arguments in Run Config
    	System.out.println("Welcome to Royal Ca$ino!");																	//Welcome print statement
    	playMachine(money, bet, goal);																					//Calling the method playMachine with 3 double parameters
    }
    
    public static void playMachine (double playerMoney, double playerBet, double playerGoal) {							//Method playMachine, simulating the function of a casino machine
  		int i=1, multiplier;																							//Initializing 2 variables: i to use as a counter to print the rounds and multiplier to add money to the player's balance if they get the right combination
      	while (canPlay(playerMoney, playerBet) && goalReached(playerMoney, playerGoal)==false) {						//The game will play if canPlay returns true (i.e has enough money to bet) and goalReached returns false (i.e money goal hasn't been achieved)
      		System.out.println("");																						//Print statement for format purposes
      		System.out.println("You are playing round " + i);															//Print statement to display rounds using i
      		i++;																										//i increases for the next round
      		String icon_1=getSymbol(), icon_2=getSymbol(), icon_3=getSymbol();											//Initializing 3 string variables. Each one call getSymbol to get its random symbol
          	displaySymbols(icon_1, icon_2, icon_3);																		//Calling method displaySymbol to print the symbols combination
         		multiplier=getMultiplier(icon_1, icon_2, icon_3);														//Calling method getMultiplier to check if the player won money, got his bet back or lost money depending on the symbols combination
         		playerMoney-=playerBet;																					//Taking off the bet from the playerMoney balance
         		playerMoney+=(playerBet*multiplier);																	//Updating the new playerMoney balance for the next round, if they got lucky the won money back
         		System.out.println("You now have: "+ formatMoney(playerMoney));											//Displaying the player's balance after the round
      	}
      	if (canPlay(playerMoney, playerBet)==false) {																	//If canPlay returns false the the player can't play, thus this code executes and the game ends
      		System.out.println("");																																		//Print statement for format purposes
      		System.out.println("Sorry, you don't have enough money to keep playing! You now have " + formatMoney(playerMoney) + ". Come back another day!");			//Print statement to let the player know why they can't play anymore
      	}
      	if (goalReached(playerMoney, playerGoal)==true) {																												//If the condition goalReached returns true then the game ends
      		System.out.println("");																																		//Print statement for format purposes
      		System.out.println("You're winner! You have reached your daily goal of " + formatMoney(playerGoal) +". You now have " + formatMoney(playerMoney) + ".");	//Print statement to let the player know why they can't play anymore
      	}
      }
    
    public static int diceRoll(){																						//Method diceRoll, generates a random number between 1 and 6(inclusive)			
        double randNum=Math.random();																					//Initializing variable that stores a random number by calling method Math.random()
        randNum *=7;																									//Since the value of Math.random is 0<x<1, we multiply the value by 7 to get it between 1 and 7
        if(randNum<0.6){																								//If the number is still too small after the multiplication we execute this code
            if (randNum*10<=6){																							//If the number *10 is less than 6 the condition is true
                randNum *=10;																							//We multiply said number by 10	
            } 
        }
        if(randNum<1){																									//If after all those operations the number is still too small the if condition is true 
            randNum++;																									//We just add 1 to said number
        }
        int randInt=(int) randNum ;																						//We convert the number we got to an integer
        return randInt ;																								//Returning said integer
    }
    
    public static String getSymbol(){																					//Method getSymbol, depending on what random number is generated we print it's associated symbol
        int index=diceRoll();																							//We store in the variable index the number returned by the method diceRoll
        String [][] symbolDataBase ={																					//We initialize a String with the symbols stored in it
        	{"Cherries", "Oranges", "Plums", "Bells", "Melons", "Bars"}
        };
        return(symbolDataBase[0][index-1]);																				//We return the string associated with the random number-1, because String indexing starts at 0 and our random number generator starts at 1
    }
    
    public static int getMultiplier(String symbol_1, String symbol_2, String symbol_3) {								//Method getMultiplier with 3 string parameters, if a combination of symbols is attained a multiplier greater than 0 will be used to retrun a certain amount of money to the player
    	if (symbol_1==symbol_2 && symbol_1==symbol_3) {																	//If 3 symbols are the same the condition is true
    		if (symbol_1=="Bells") {																					//On top of that if the symbols are bells the multiplier will be higher
    			return 5;																								//Returning 5 as both if statements are true
    		}else return 3;																								//Return 3 since only the 1st if is true
    	}else if (((symbol_1==symbol_2) && (symbol_1!=symbol_3)) || ((symbol_1==symbol_3) && (symbol_1!=symbol_2)) || ((symbol_2==symbol_3) && (symbol_2!=symbol_1)) ) {	//If 2 symbols are the same the condition is true
    		return 2;																																						//Return 2
    	}else return 0;																									//If all symbols are different from each other then 0 is returned, the player will not win his money back
    }
    
    public static boolean canPlay(double playerMoney, double roundBet) {												//Method canPlay, checks if the player has money left to bet. If false the game ends.
    	if (playerMoney>=roundBet && roundBet>0) {																		//If with 2 conditions to check if the player's balance is superior to the bet amount (i.e. if he can afford 1 more bet) and if the bet amount is bigger than 0
    		return true;																								//If both conditions are true then we return true, thus allowing the game to proceed
    	}else return false;																								//If one condition is false the game ends
    }
    
    public static boolean goalReached(double playerMoney, double moneyGoal) {											//Method goalReached, checks if the player reached the amount of money set as goal, if true we retrun true and the game ends
    	if(moneyGoal>playerMoney) {																						//If checking the previously mentioned condition
    		return false;																								//If the money goal hasn't been achieved we return false and the game continues
    	}else return true;																								//Otherwise we return true and the game ends
    }
    
    public static void displaySymbols(String symbol_1, String symbol_2, String symbol_3) {								//Method displaySymbols, takes 3 string parameters and will display them
    	int totalLength=symbol_1.length()+symbol_2.length()+symbol_3.length()+10;										//To manually create a nice format we store in a variable the total length of the symbols +8 to account for extra symbols that are visible in the next print statement
    	for (int l=0; l<totalLength; l++) {																				//For loop to print a line of "-" according to length stored in totalLength
    		System.out.print("-");																						//Printing "-"
    	}
    	System.out.println("");																							//Print statement for format purposes
    	System.out.println("| " + symbol_1 + " | " + symbol_2 + " | " + symbol_3 + " |");								//Printing the symbols with proper spacing
    	for (int l=0; l<totalLength; l++) {																				//For loop to print a line of "-" according to length stored in totalLength
    		System.out.print("-");																						//Printing "-"
    	}
    	System.out.println("");																							//Print statement for format purposes
    }
    
    public static String formatMoney(double money) {																	//Method formatMoney, to display money nicely. Called every time money appears on screen
    	return String.format("$%.2f", money);																			//Print statement with the proper code to modify how money is displayed
    }
        
}
