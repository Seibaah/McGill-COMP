

import java.util.ArrayList;
import java.util.Random;

public class Planet {		//Planet class with 3 attributes
	private String name;
	private double chance;
	private double damage;
	
	public Planet(String name, double chance, double damage) {		//Constructor that creates a Planet object and through setter set its name, artifact chance and max damage characteristics
		setName(name);
		if ((chance>=0)&&(chance<1)) {								//Will only set the chance of the planet if it's in the valid range, otherwise we throw an exception
			setChance(chance);
		} else throw new IllegalArgumentException ("Chance value is invalid. Must be between 0 and 1.");
		if (damage>=0) {																						//Same logic as before, will only set the damage if it's in the valid range							
			setDamage(damage);
		} else throw new IllegalArgumentException ("Damage value is invalid. Must be greater than 0.");
		System.out.println(this.toString());						//Printing the new Planet object and its attributes
		
	}
	
	private void setName (String name) {	//Setter method to set name
		this.name=name;
	}
	
	private void setChance (double chance) {	//Setter method to set chance of artifact
		this.chance=chance;
	}
	
	private void setDamage (double damage) {	//Setter method to set damage of planet
		this.damage=damage;
	}
	
	public String getName() {	//Getter method to get the name of the planet
		return this.name;
	}
	
	public String toString() {											//toString method that returns a printable String of the instance and its attributes
		return ("Name: " + this.name + " Artifact chance:" + 
						(this.chance*100) + "% " + " Possible damage: " + this.damage);
	}
	
	public static int findPlanet(String name, ArrayList<Planet> planet_list) {		//Static method that search and finds for a planet in an array list with a specific name
		String planet_1=name.toLowerCase(), planet_2;								//Whatever name we are looking for we convert it to lower case to ignore casing
		Planet planet_2_obj;
		for (int i=0; i<planet_list.size(); i++) {									//For loop to iterate over the array list
			planet_2_obj=planet_list.get(i);										//We get the i-th object in the list
			planet_2=(planet_2_obj.name).toLowerCase();								//Get its name attribute and lower case it
			if ((planet_1).equals(planet_2)) {										//Compare it to the name of the planet we are looking for
				return i;															//If we find it we return its index
			}
		}
		return -1;																	//We return -1 if said planet isnt in the array
	}
	
	public boolean searchForArtifact() {			//Method that randomly decides if the ship found or not an artifact in a planet
		if(this.chance!=0) {						//If the chance setting of the planet isn't 0 we proceed, otherwise we skip at the last return statement
			Random num=new Random();				//Creating a random object
			double rand=num.nextDouble();			//Getting a random number
			if (rand<this.chance) {					//If the number is smaller than the chance value we found an artifact
				return true;
			} else return false;					//If not we didn't find an artifact and we return false
		} else return false;				
	}
	
	public double getDamageTaken() {			//Method that returns how much damage our ship took in the planet
		Random num=new Random();
		double damage=(num.nextDouble())*(this.damage);		//We generate a random number with Random class and get a random amount of damage
		return damage;
	}
}
