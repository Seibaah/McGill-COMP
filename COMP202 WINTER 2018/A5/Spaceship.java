

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Spaceship {		//Spaceship class, an instance will have the following attributes
	private String name;
	private double health;
	private double maxHealth;
	private int artifacts;
	private int wins;
	private Planet currPlanet;
	private static ArrayList<Planet> planet_list = new ArrayList<Planet>();
	
	public Spaceship(String name, double maxHealth, int wins) {			//Constructor to create a spaceship object with 3 attributes
		this.setName(name);												//Using setters to set the attributes
		this.setMaxHealth(maxHealth);
		this.setWins(wins);
	}
	
	private void setName(String name) {		//Setter method to set name
		this.name=name;
	}
	
	private void setMaxHealth(double maxHealth) {	//Setter method to set maxHealth, we also set the starting help to the same value
		this.maxHealth=maxHealth;
		this.health=maxHealth;
	}
	
	private void setWins(int wins) {	//Setter method to set number of wins
		this.wins=wins;
	}
	
	public String toString() {															//toString method that returns a printable statement with the ship's values
		DecimalFormat df= new DecimalFormat("#.##");									//Added a decimal format object to round the remaining health to 2 decimal places, so it doesn't show 15 decimal places in the screen
		df.setRoundingMode(RoundingMode.DOWN);
		return ("Name: " + this.name + " Health: " + df.format(this.getHealth()) + 
				" Artifacts: " + this.artifacts + "\n");
	}
	
	public static void setPlanets(ArrayList<Planet> planet_list_2) {	//Setter method thats sets the possible destinations for a ship
		Planet planet_ith;
		for (int i=0; i<planet_list_2.size(); i++) {					
			planet_ith=planet_list_2.get(i);
			planet_list.add(planet_list_2.get(i));
			planet_ith.toString();
		}
	}
	
	public void moveTo(String planet_name) {														//Method that moves the ship to a specific location
		int i=Planet.findPlanet(planet_name, planet_list);											//By calling findPlanet we get the index of the planet we want to go to
		if (i!=-1) {																				//If said planet is found we change the ship currPlanet attribute to the new planet
			this.currPlanet=planet_list.get(i);
			System.out.println("The " + this.name + " moved to " + planet_name+"\n");
			
		} else System.out.println("The " + this.name + " tried to move to " + planet_name + 		//Otherwise we show an error message
				", but that planet isn't in the solar system!"+"\n");
	}
	
	public void moveIn() {																							//Method to move in to the planet in the next lower position in the array list
		String location=currPlanet.getName();																		//We get our current location
		int i=Planet.findPlanet(location, planet_list);																//We get its index
		if (i>0) {																									//If our current planet isn't the first in the array list we basically change the ship's currPlanet attribute to the new planet
			i-=1;
			Planet destination=planet_list.get(i);
			String newLocation=destination.getName();
			moveTo(newLocation);
		} else System.out.println("The " + this.name + " couldn't move in. No planet is closer in.");				//If we are in the lowest index of the array list we print a warning
	}
	
	public void moveOut() {																							//Same principle as the last method but we are trying to move in the upper indexes of the array list
		String location=currPlanet.getName();
		int i=Planet.findPlanet(location, planet_list);
		if (i<planet_list.size()-1) {																				//If we aren't in the last planet of the array we change the ship's attributes to move planets
			i+=1;
			Planet destination=planet_list.get(i);
			String newLocation=destination.getName();
			moveTo(newLocation);
		} else System.out.println("The " + this.name + " couldn't move out. No planet is farther out.");			//If it's not possible we show a warning
	}
	
	public void increaseWins() {	//Method to increase a ship's wins
		this.wins+=1;
	}

	public double getHealth() {		//Getter method to get the current health of the ship
		return this.health;
	}
	
	public double getMaxHealth() {		//Getter method to get the max health of a ship
		return this.maxHealth;
	}
	
	public int getWins() {		//Getter method to get the wins of a ship
		return this.wins;
	}
	
	public String getName() {	//Getter method to get the name of a ship
		return this.name;
	}
	
	public double getArtifacts() {	//Getter method to get how many artifacts a ship has
		return this.artifacts;
	}
	
	public void setHealth(double damage) {		//Setter method to set the health after having taken damage
		this.health-=damage;
	}
	
	public Planet getLocation() {		//Getter method to get the ship's location
		return this.currPlanet;
	}

	public void doSearch() {													//Method that allows a ship to search a planet for an artifact
		boolean cond=this.currPlanet.searchForArtifact();						//We get from the searchForArtifact method if we found or not an artifact (T/F)
		if (cond==true) {														//If we did we add an artifact to the ship and print a message
			System.out.println(this.name + " found an artifact!");
			this.artifacts++;
		} else System.out.println(this.name + " didn't find an artifact!");		//Else we print a message
		double damage=this.currPlanet.getDamageTaken();							//Regardless of that we now calculate the damage the ship took and we use a setter to set the ships new reduced health
		this.setHealth(damage);
		String damageStr= String.format("%1$.2f", damage);						//Formating the damage output display
		System.out.println(this.name + " took " + damageStr + " while searching for an artifact on " + this.currPlanet.getName());		//Printing the results of the search
	}
}
