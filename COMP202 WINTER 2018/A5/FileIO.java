

import java.io.*;
import java.util.ArrayList;

public class FileIO {
	
	public static Spaceship loadSpaceship(String filename) {					//Method that reads the ships values from a file and calls a constructor to create a ship object
		try {																	//Try and catch blocks to handle the file/IO exceptions
			FileReader fr=new FileReader(filename);								//Creating file and buffered readers
			BufferedReader br=new BufferedReader(fr);
			String name=br.readLine();											//Store a file's line under name
			double maxHealth=Double.parseDouble(br.readLine());					//Read and store the ship's maxHealth in an integer
			int wins=Integer.parseInt(br.readLine());							//We do the same for wins
			Spaceship player_proto =new Spaceship(name, maxHealth, wins);		//Call the spaceship constructor
			br.close();															//Closing file and buffered readers
			fr.close();
			return player_proto;												//Return the ship object
		} catch (FileNotFoundException e) {										//Catching exceptions
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("File error");
		}
		return null;															//Return statement in case the try block fails
	}
	
	public static ArrayList<Planet> loadPlanets(String filename){				//Method that load Planets in an array list
		ArrayList<Planet> planet_list_local= new ArrayList<Planet>();			//Creating a list
		try {																	//Try and catch blocks to handle the file/IO exceptions
			FileReader fr=new FileReader(filename);								//Creating file and buffered readers
			BufferedReader br=new BufferedReader(fr);
			String currentLine=br.readLine();
			do {
				String line=currentLine;										//Store a file's line under name
				String[] data=line.split(" ");									//Split the line at every blank space and store each part in an array
				String name=data[0];											//The first index of the array stores the planet name
				Double chance=Double.parseDouble(data[1]);						//The second position stores the artifact spawn chance
				Double damage=Double.parseDouble(data[2]);						//The third contains the damage of the planet
				planet_list_local.add(new Planet(name, chance, damage));		//Calling the planet constructor to create a planet
				currentLine=br.readLine();										//We read the next line
			} while(currentLine!=null);											//do While that keeps reading and loading planets while the next file line isn't null
			br.close();															//Closing file and buffered readers
			fr.close();
			return planet_list_local;											//Returning the loaded array list
		} catch (FileNotFoundException e) {										//Catching exceptions
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("File error");
		}
		return null;															//Return statement in case the try block fails
	}
	
	public static void saveSpaceship(Spaceship winner, String filename) {		//Method that writes the winner's spaceship data into a file to record the wins
		try {																	//Try and catch blocks to handle the file/IO exceptions
			FileWriter fw=new FileWriter(filename);								//Creating file and buffered writers
			BufferedWriter bw=new BufferedWriter(fw);
			String name=winner.getName();										//In the following 3 lines we store the winner's name, maxHealth and number of wins in variables
			double maxHealth=winner.getMaxHealth();
			int wins=winner.getWins();
			bw.write(name+"\n");												//In the next 3 lines we write in a file those values
			bw.write(maxHealth+"\n");
			bw.write(wins+"\n");
			bw.close();															//Closing file and buffered writers
			fw.close();
		} catch (FileNotFoundException e) {										//Catching exceptions
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("File error");
		}
	}
}
