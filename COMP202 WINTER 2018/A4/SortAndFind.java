
import java.util.Random;

public class SortAndFind {						
	public static void main(String[] args) {
		int y=6, x=8, n=51, n_2=42, n_3=11;				//Initializing matrix size and 3 test cases  to find
		int[] coord = new int [2];						//Creating an array to store the coordinates of he element to find
		int [][] matrix=generateRandomArray(y,x);		//Creating a matrix with random entries using method generateRandomArray
		displayMatrix(matrix);							//With he displayMatrix method we show the randomized matrix
		System.out.println();
		sortMatrix(matrix);								//We call the sortMatrix method to sort the matrix
		coord=findElement(matrix, n);					//Next lines are 3 test cases to find an element in the matrix
		printPos(coord, n);								//Using a helper method to display the array with the coordinates
		coord=findElement(matrix, n_2);
		printPos(coord, n_2);
		coord=findElement(matrix, n_3);
		printPos(coord, n_3);
	}
	
	public static void printPos(int[] coord, int n) {						//Helper method to display the array with the coordinates
		System.out.print("The element " + n + " is in the position ");
		for (int i=0; i<coord.length; i++) {
			System.out.print(coord[i]+ " ");
		}
		System.out.println();
	}
	
	public static int[][] generateRandomArray(int y, int x) {			//Method to randomize the numbers in the matrix
		int seed=123;													//Creating a seed 
		int [][] matrix= new int [y][x];								//Creating a matrix
		Random num= new Random(seed);									//The block from lines 34 to 40 iterates through the array and fills it with randomized numbers
		for (int i=0; i<x; i++) {
			for (int j=0; j<y; j++) {
				int rand=num.nextInt(50);
				matrix[j][i]=rand;
			}
		}
		return matrix;													//Returning the filled matrix
	}
	
	public static void displayMatrix(int [][] matrix) {					//Method to display the matrix
		for (int i=0; i<matrix.length; i++) {							//Block from lines 45 to 50 iterates through the matrix to display it using \t for spacing
			for (int j=0; j<matrix[i].length; j++) {
				System.out.print(matrix[i][j]+ "\t");
			}
			System.out.println();
		}
	}
	
	public static void sortMatrix (int[][] matrix) {					//Method to sort the matrix
		for (int i=0; i<matrix.length; i++) {							//For to iterate through the rows of the matrix
			int line=i;													//Saving the current row to a variable
			int [] row=matrixRowTransform(matrix, line);				//Using a helper method to transform the current line of the matrix into a 1-D array
			row=sortOneRow(row);										//Calling sortMethod into this 1-D array
			matrix=matrixRewrite(row, matrix, line);					//Using a helper method to change the row on the matrix with the new sorted one, copying the 1-D array into the 2-D one
		}
		for (int i=0; i<matrix[0].length; i++) {						//The following for calls the sortOneColumn method to sort the columns of the matrix
				sortOneColumn(matrix, i);
		}
		displayMatrix(matrix);											//Using the display method to show the sorted matrix
	}
	
	public static int[] matrixRowTransform(int[][] matrix, int line) {			//Helper method to transform a line of the matrix into a 1-D array
		for (int j=line; j<matrix.length; j++) {								//The block of code from line 67 to 73 goes through the line in the matrix and copies it to a 1-D array
			int [] row= new int [matrix[j].length];
			for (int i=0; i<matrix[j].length; i++) {
				row[i]=matrix[j][i];
			}
			return row;
		}
		return null;															//Return statement to avoid errors, the code will never return null if properly done
	}
	
	public static int[] sortOneRow (int [] row) {						//SortOneRow method
		for (int i=0; i<row.length; i++) {								//For iterating through our 1-D array
			int smallestNum=row[i], indexSmall=i, temp;					//Initializing variables to perform comparisons and switch values around, the smallest value starts as the 1st entry of the "sorted" part of the array
			for (int j=i+1; j<row.length; j++) {						//Nested for loop to go through the "unsorted" part of the array
				int num_2=row[j];										//Creating a variable for the comparison process					
				if (smallestNum>num_2) {								//If the smallestNum is > than the entry in num_2 we change our smallestValue and store its index, the process repeats until the end of the array
					smallestNum=num_2;
					indexSmall=j;
				}
			}
			temp=row[i];												//Once the array read has been done we perform the switch using a temporary variable
			row[i]=smallestNum;
			row[indexSmall]=temp;
		}
		return row;														//Returning the sorted row
	}
	
	public static int[][] matrixRewrite (int[] row, int [][] matrix, int line){			//Helper method to patch the sorted 1-D array over the respective line in the 2-D matrix
			for (int i=0; i<matrix[line].length; i++) {
				matrix[line][i]=row[i];
			}
			return matrix;																//Returning the updated matrix
	}
	
	public static void sortOneColumn(int [][] matrix, int column){				//Method with same algorithm of sortOneRow applied to a 2-D matrix sorting the columns instead of rows
		for (int i=0; i<matrix.length; i++) {									//For iterating through our matrix columns
			int smallestNum=matrix[i][column], indexSmall=i, temp;				//Initializing variables to perform comparisons and switch values around, the smallest value starts as the 1st entry of the "sorted" part of the array
			for (int j=i+1; j<matrix.length; j++) {								//Nested for loop to go through the "unsorted" part of the column
				int num_2=matrix[j][column];									//Creating a variable for the comparison process
				if (smallestNum>num_2) {										//If the smallestNum is > than the entry in num_2 we change our smallestValue and store its index, the process repeats until the end of the column
					smallestNum=num_2;
					indexSmall=j;
				}
			}
			temp=matrix[i][column];												////Once the column read has been done we perform the switch using a temporary variable
			matrix[i][column]=smallestNum;
			matrix[indexSmall][column]=temp;
		}
	}
	
	public static int[] findElement(int [][] matrix, int n){											//method to find a number in the sorted array
		int pos_y=matrix.length-1, pos_x=0;																//Initializing starting position at the bottom left corner
		int [] coord= {-1, -1};																			//Initializing an array storing default -1,-1 coordinates
		while (((pos_y<matrix.length)&&(pos_y>-1))&&((pos_x<matrix[0].length)&&(pos_x>-1))) {			//Huge while loop condition that basically stops running the search if its about to go out of bounds
			if (n==matrix[pos_y][pos_x]) {																//If the number is found we return an array with the current position
				int [] coord_2= {pos_y, pos_x};
				return coord_2;
			}
			if(n>matrix[pos_y][pos_x]) {																//If the number at pos_y, pox_x is smaller than what we are looking for we move to the right
				pos_x++;
			}
			else if(n<matrix[pos_y][pos_x]) {															//If the number at pos_y, pox_x is bigger than what we are looking for we move upwards
				pos_y--;
			}
		}
		return coord;																					//If the number isn't in the matrix we return our default coordinates array
	}
}
