

public class Mountains {
	public static void main(String[] args) {												//Main method
		String mountainSymbols= "#*H";														//String containing symbols to draw the mountains
		int pointsNumber=(int)((Math.random()*50)+1);										//Creating an integer variable with random value between 1 and 50 to set our mountain extension
		double steepness=((Math.random()*4)+1), maxHeight=((Math.random()*35 ))+ 1;			//Creating a double steepness variable between 1 and 4; and a maxHeight between 1 and 35
		double[] allHeightsCopy=generateMountains(pointsNumber, steepness, maxHeight);		//Creating a double Array that stores all the points heights with 3 parameters. This invokes the method generateMountains
		drawMountains(allHeightsCopy, mountainSymbols);										//Calling the method drawMountains, to draw them on the screen
	}
	
	public static double getNextPoint(double previousHeight, double steepness, double absoluteMaxHeight) {		//Method to generate the next point of the mountain
		double newHeight, newMinHeight=previousHeight-steepness, newMaxHeight=previousHeight+steepness;			//Initializing variables for the calculated height and its max and min possible values
		if (newMinHeight<0) {																					//If the min height dips below 0 we set it to 0
			newMinHeight=0;
		}
		if (newMaxHeight>absoluteMaxHeight) {																	//If the max height goes past our absolute max Height we set it equal to the latter
			newMaxHeight=absoluteMaxHeight;
		}
		newHeight=(Math.random()*2*steepness)+newMinHeight;														//We calculate the point height using Math.random(). We multiply times twice the steepness to cover the whole range and add the min height because it is our baseline
		return newHeight;																						//We return the new Height value
	}
	
	public static double[] generateMountains(int pointsNumber, double steepness, double maxHeight) {			//Method that stores the data of the mountains; i.e the heights
		double allHeights[]=new double[pointsNumber];															//Creating an array of the length calculated in main
		allHeights[0]=maxHeight/2;																				//As instructed the first point is half the max height
		for (int i=1; i<pointsNumber; i++) {																	//For loop to start filling the array with the heights
			double previousHeight=allHeights[i-1];																//We store in a variable the previous height as it affects the next point's height
			allHeights[i]=getNextPoint(previousHeight, steepness, maxHeight);									//Using the method getNextPoint we get the height for the i-th point
		}
		return allHeights;																						//Returning the array with all the heights	
	}
	
	public static double findMaxHeight(double[] allHeights) {													//Method to find the highest point in the mountain/array
		double realMaxHeight=0;																					//We create a variable to store the highest point and set it to 0
		for (int i=0; i<allHeights.length; i++) {																//For loop to iterate over the array containing all the heights
			if (allHeights[i]>=realMaxHeight) {																	//If statement to test if the height in the i-th position is bigger or equal to the current value of the variable just created
				realMaxHeight=allHeights[i];																	//If this is the case then we replace the value in the variable for the new highest point
			}
		}
		return realMaxHeight;																					//We return the highest point
	}
	
	public static void drawMountains(double[] allHeights, String symbols) {										//Method that draws the mountains using the generated data
		int tallestPoint=(int)findMaxHeight(allHeights)+1, randomSymbol=(int)(Math.random()*3);					//We define our tallest point and store its value; we also randomly generate a number that defines what symbol will be used to draw the mountain body. This is following the instructions that asked to allow mountains to be drawn with different symbols.
		for (int y=tallestPoint; y>=0; y--) {																	//For loop that starts from the tallest point and goes down to draw the mountains; "y" will be our current height
			for (int x=0; x<allHeights.length; x++ ) {															//Nested for loop that reads the values in the array from left to right
				if (y<1) {																						//If the current height is smaller than 1 we draw the ground symbol
					System.out.print("-");																		//Printing the ground symbol
					continue;																					//If the condition is true we don't need to test the other ones so we skip to the next iteration of the loop. This mesure could be replaced by using else in the other if statements. Both options work equally as well.
				}
				if (y>allHeights[x]) {																			//If the current height is bigger than the height of the x-th point stored in the array we draw a blank space
					System.out.print(" ");																		//Printing a blank space
					continue;																					//If the condition is true we don't need to test the other ones so we skip to the next iteration of the loop.
				}
				if ((y-allHeights[x]<1) && (y-allHeights[x]>-1)) {												//If the current height minus the value in the x-th position in the array is between -1 and 1 we draw the mountain top
					System.out.print("^");																		//Printing the mountain top 
					continue;																					//If the condition is true we don't need to test the other ones so we skip to the next iteration of the loop.
				}
				if (y<allHeights[x]) {																			//If the current height is smaller than the value in the x-th position in the array we draw the mountain's body symbol
					System.out.print(symbols.charAt(randomSymbol));												//Printing the randomly selected mountain symbol
					continue;																					//If the condition is true we don't need to test the other ones so we skip to the next iteration of the loop.
				}
				
			}
			System.out.println();																				//Print statement to skip to a new line																		
		}
	}
}
