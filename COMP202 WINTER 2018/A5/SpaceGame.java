

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

public class SpaceGame {																							//Spacegame class with its attributes
	private Scanner scan;
	private Spaceship player_1;
	private Spaceship enemy;
	private static final int NUM_ARTIFACTS_WIN=5;
	
	public SpaceGame(String filename) {																			//Constructor that initializes the different aspects of the game
		this.scan=new Scanner(System.in);																		//Opening a scanner
		System.out.println("Welcome to SpaceGame! You must find 5 artifacts to win."+"\n");
		System.out.println("Loaded solar system from sol.txt: "+"\n");
		ArrayList<Planet> planet_list=FileIO.loadPlanets(filename);												//Loading the array list with planet objects
		Spaceship.setPlanets(planet_list);																		//Setting the possible planets for a spaceship
		player_1= FileIO.loadSpaceship("player.txt");															//Creating player 1 object
		enemy= FileIO.loadSpaceship("enemy.txt");																//Creating enemy object
		Planet starting_planet=planet_list.get(0), enemy_planet=planet_list.get(planet_list.size()-1);			//Copying the first and last planet objects in the array list
		String starting_planet_name=starting_planet.getName(), enemy_planet_name=enemy_planet.getName();		//Getting their names
		player_1.moveTo(starting_planet_name);																	//Moving the player to the first planet
		enemy.moveTo(enemy_planet_name);																		//Moving the enemy to the last planet
	}
	
	private int checkForDestroyed() {																			//Method that checks if the player and/or the enemy are still alive
		if (player_1.getHealth()<=0) {
			return 1;																							//Returns 1 if the player dies
		} else if (enemy.getHealth()<=0) {
			return 2;																							//Returns 2 if the enemy dies
		} else return 0;																						//Returns 0 if both are still alive																					
	}
	
	private int checkForWin() {																					//Method that checks if the player and/or the enemy have found 5 artifacts and thus won the game
		if (player_1.getArtifacts()>=NUM_ARTIFACTS_WIN) {
			return 1;																							//Returns 1 if the player found 5 artifacts
		} else if (enemy.getArtifacts()>=NUM_ARTIFACTS_WIN) {
			return 2;																							//Returns 2 if the enemy found 5 artifacts
		} else return 0;																						//Returns 0 if both haven't found 5 artifacts yet
	}
	
	public void playGame() {																					//Method that runs the game
		do {																									//do while loop
			this.scan=new Scanner (System.in);																	//Opening a scanner
			Planet p1_turn_start_state=player_1.getLocation();													//The next 4 lines store starting values of the player: location, location's name, health if the player has performed a search
			String p1_turn_start_location=p1_turn_start_state.getName();
			double p1_turn_start_health=player_1.getHealth();
			boolean player_searched=false;
			System.out.println("You can type the following options: moveIn, moveOut, moveTo, search" + "\n");	//Showing available commands to the user
			String line=this.scan.nextLine();																	//We get the input					
			String line2=line.toLowerCase();																	//Lower case it to make easier comparisons
			if (line2.equals("movein")) {																		//Lines 55-66 compare the input and determine what actions to perform based on the input
				player_1.moveIn();
			} else if (line2.equals("moveout")) {
				player_1.moveOut();
			} else if (line2.equals("moveto")) {
				System.out.println("Which planet do you want to move: ");
				String input=this.scan.nextLine();
				player_1.moveTo(input);
			} else if (line2.equals("search")) {
				player_1.doSearch();
				player_searched=true;																			//If a search is performed we change the player searched value to true
			} else System.out.println("The input wasn't recognized. Please try again.");
			Planet p1_turn_end_state=player_1.getLocation();													//In the next 3 lines we store in different variables the ending values of the player
			String p1_turn_end_location=p1_turn_end_state.getName();
			double p1_turn_end_health=player_1.getHealth();
			boolean player_action=false;																		//Creating a boolean variable to determine if the player turn has been completed
			if (!p1_turn_start_location.equals(p1_turn_end_location)) {											//Lines 71-77 are a bunch of conditions that try to determine if the player has completed its turn
				player_action=true;																				//Based on location change, health decrease or if the player performed a search in a planet
			} else if (p1_turn_start_health!=p1_turn_end_health) {												//Without this, if we were to input an invalid command the AI would proceed with it's turn, this would be unfair 
				player_action=true;																				//So until the player completes it's turn the AI turn will skip, to provide a fair game experience
			} else if (player_searched==true) {
				player_action=true;
			}
			if (player_action==true) {									//If the player completed his turn the AI can play
				System.out.println(player_1.toString());				//We print the player after turn state here since this is the only part that only executes after his turn ended
				Random num=new Random();								//The AI actions are random, we randomly generate an integer between 0 and 3 and based on the results the AI performs an action
				int move=num.nextInt(3);
				if (move==0) {
					enemy.doSearch();
				} else if (move==1) {
					enemy.moveIn();
				} else if (move==2) {
					enemy.moveOut();
				}
				System.out.println(enemy.toString());					//We print the AI after turn state		
			}
			
		} while ((checkForDestroyed()==0)&&(checkForWin()==0));			//Do while condition, game will run only if both players are alive and haven't found 5 artifacts yet
		
		scan.close();													//Closing scanner
		if (checkForDestroyed()!=1) {																						//If the game has ended and the player is still alive + 
			
			if (checkForWin()==1) {																							//+If the player found 5 artifacts
				System.out.println(player_1.getName() + " won by finding 5 artifacts! " + enemy.getName() + " lost!");		//Printing the game results accordingly
				player_1.increaseWins();																					//Increase player wins
				System.out.println(player_1.getName() + " has won " + player_1.getWins() + " times!");						//Printing the number of wins the player has now
				FileIO.saveSpaceship(player_1, "player.txt");																//Save new player wins value in a file
				
			} else if (checkForWin()==2) {																					//+If the AI found 5 artifacts
				System.out.println(enemy.getName() + " won by finding 5 artifacts! " + player_1.getName() + " lost!");		//Printing the game results accordingly
				enemy.increaseWins();																						//Increase AI wins
				System.out.println(enemy.getName() + " has won " + enemy.getWins() + " times!");							//Printing the number of wins the AI has now
				FileIO.saveSpaceship(enemy, "enemy.txt");																	//Save new AI wins value in a file
				
			} else if (checkForDestroyed()==2) {																			//+If the AI was destroyed
				System.out.println(player_1.getName() + " won! " + enemy.getName() + " exploded!");							//Printing the game results accordingly
				player_1.increaseWins();																					//Increase player wins
				System.out.println(player_1.getName() + " has won " + player_1.getWins() + " times!");						//Printing the number of wins the player has now
				FileIO.saveSpaceship(player_1, "player.txt");																//Save new player wins value in a file
			}
			
		} else if (checkForDestroyed()!=2) {																				//If the game has ended and the AI is still alive + 
			
			if (checkForWin()==2) {																							//+If the AI found 5 artifacts
				System.out.println(enemy.getName() + " won by finding 5 artifacts! " + player_1.getName() + " lost!");		//Printing the game results accordingly
				enemy.increaseWins();																						//Increase AI wins
				System.out.println(enemy.getName() + " has won " + enemy.getWins() + " times!");							//Printing the number of wins the AI has now
				FileIO.saveSpaceship(enemy, "enemy.txt");																	//Save new AI wins value in a file
				
			} else if (checkForWin()==1) {																					//+If the player found 5 artifacts
				System.out.println(player_1.getName() + " won by finding 5 artifacts! " + enemy.getName() + " lost!");		//Printing the game results accordingly
				player_1.increaseWins();																					//Increase player wins
				System.out.println(player_1.getName() + " has won " + player_1.getWins() + " times!");						//Printing the number of wins the player has now
				FileIO.saveSpaceship(player_1, "player.txt");																//Save new player wins value in a file
				
			} else if (checkForDestroyed()==1) {																			//+If the player was destroyed
				System.out.println(enemy.getName() + " won! " + player_1.getName() + " exploded!");							//Printing the game results accordingly
				enemy.increaseWins();																						//Increase AI wins
				System.out.println(enemy.getName() + " has won " + enemy.getWins() + " times!");							//Printing the number of wins the AI has now
				FileIO.saveSpaceship(enemy, "enemy.txt");																	//Save new AI wins value in a file
			}
		}
	}
}
