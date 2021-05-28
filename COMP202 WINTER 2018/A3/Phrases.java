
public class Phrases {
	public static void main(String[] args) {
		String firstString="My name is Jeff from the Overwatch team", secondString = "Overwatch nerf team with another Hero downgrade for Mercy";	//The main method is filled with test cases followed by a call to the combineStrings method to fuse the strings at their common word
		System.out.println(combineStrings(firstString, secondString));
		String thirdString="Hello there General Kenobi You fool", fourthString = "General Wasabi You fool! I've been trained by Count Dooku in your Jedi Arts!";
		System.out.println(combineStrings(thirdString, fourthString));
		String fifthString="I wish my grades stayed in a galaxy far", sixthString = "Paris is far far away";
		System.out.println(combineStrings(fifthString,sixthString));
		String seventhString="Roses are red, violets are blue, I suck at poetry", eightString = "Barcelona's uniform is has blue, Madrid is dead, Cristiano sucks too";
		System.out.println(combineStrings(seventhString,eightString));
		String ninthString="I have run out of ideas", tenthString = "Maybe you can come up with some";
		System.out.println(combineStrings(ninthString,tenthString));
	}
	
	public static String join(String[] combinedStringArray) {		//Method join that takes a String array  and converts it into a String
		String reconstruction="";									//Declaring and initializing a string to perform the conversion
		for (int i=0; i<combinedStringArray.length; i++) {			//For loop to iterate over the different Strings in the array
			reconstruction+=combinedStringArray[i];					//We add the String in the i-th place to our String 
			reconstruction+=" ";									//We add the spacing between words
		}
		return reconstruction;										//Returning the converted String
	}
	
	public static int findInStringArray(String searchString, String[] secondStringArray) {		//Method to find a specific word from a String in a String array
		String searchStringCopy=searchString.toLowerCase(), tempArrayString;					//Creating a lower case copy of the word we are looking for and initializing a temporary String array
		for (int i=0; i<secondStringArray.length; i++) {										//For loop to iterate over the elements of the String array
			tempArrayString=secondStringArray[i].toLowerCase();									//Storing in the temporary String array the element in the i-th position and converting it to lower case. We do this to ignore capitalization in the words.
			if(searchStringCopy.equals(tempArrayString)) {										//Comparing the String and the temporary String Array element
				return i;																		//If they are equal we return the position in which the array element is
			}
		}
		return -1;																				//Otherwise we return -1 to let know that there isn't a word in common 
	}
	
	public static int wordCount(String str) {													//Creating a method to count the words in a String
		int count=0;																			//Initializing a counter variable
		for (int i=0; i<str.length(); i++) {													//For loop to iterate over the String
			char space= ' ', current=str.charAt(i);												//Initialize a space variable char and a current variable that stores the character in the i-th position in the string
			if (current==space) {																//We compare the two previously created variables
				count++;																		//If they are equal counter increases
			}
		}
		count++;																				//Since counter represents the spaces between words we add 1 to have the number of words in the String
		return count;																			//We return the number of words in the String
	}
	
	public static String[] tokenize(String str) {												//Method to divide a String into individual elements that correspond to words and store them in a String Array
		int size=wordCount(str), start=0;														//We create a variable to count the words in a String and initialize it with the method wordCount; we also create a start variable and set it to 0
		String strArray[]=new String [size];													//We create a String Array and define it's size with the variable that stored the wordCount of the String
		for (int i=0; i<strArray.length; i++) {													//For loop to iterate over the array
			String tempWord="";																	//Creating a temporary word String to store the individual words one at a time
			for (int j=start; j<str.length(); j++) {											//For loop to iterate over the String, starts iterating at start value so we can skip previous words
				if (str.charAt(j)!=' ') {														//If statement that is true if the character at the j-th position is not equal to ' ' (i.e we are still going through the individual characters of a word)
					char current=str.charAt(j);													//We create and store in a char variable the character at the j-th position										
					tempWord+=Character.toString(current);										//We convert the character in the j-th position to a String and add it to the temporary word String
					start++;																	//Counter start increases
				}else break;																	//If the character at the j-th position is equal to ' ' (i.e we have finished going through a word)
			}
			start++;																			//Counter start increases
			strArray[i]=tempWord;																//We store the word in the i-th position of the array
		}
		return strArray;																		//Returning the array with String elements
	}
	
	public static String[] combineArray(String[] firstStringArray, String[] secondStringArray, int posFirstArray, int posSecondArray) {		//Method to combine 2 arrays at their common word
		String combinedStringArray[] = new String[(posFirstArray)+(secondStringArray.length-posSecondArray)];								//We create a string array to store the combination of arrays, it's length is the combination of position of the first common word and the remianing words from the scond array
		int counter=0;																														//We declare a counter variable
		for (int i=0; i<=posFirstArray; i++) {																								//For loop to iterate over the first Array
			combinedStringArray[i]=firstStringArray[i];																						//We store the words of the first Array in the combined array until we hit the common word(inclusive)
			counter++;																														//Counter increases
		}
		for (int j=posSecondArray+1; j<secondStringArray.length; j++) {																		//For loop to iterate over the second Array, starts at the position of the common word +1 to avoid repetition
			combinedStringArray[counter]=secondStringArray[j];																				//We store in the combined Array at the counter position the words from the second String starting at the j-th position
			counter++;																														//Counter increases
		}
		return combinedStringArray;																											//We return the combined Array
	}
	
	public static String combineStrings(String firstString, String secondString) {															//Method that organizes the combination progress of Strings
		String[] firstStringArray=tokenize(firstString), secondStringArray=tokenize(secondString), combinedStringArray= {""};				//We declare 2 String Arrays and store in them the converted parameter Strings
		int posSecondArray=-1;																												//Declare a variable that stores the position of the common word in the 2nd String, it's -1 if the word hasn't been found
		for (int i=0; i<firstStringArray.length; i++) {																						//For loop to iterate over the first Array
			String searchString=firstStringArray[i];																						//We store the word in the i-th position in the variable searchString. This the word we will look for in the second String
			posSecondArray=findInStringArray(searchString, secondStringArray);																//We call the method findInStringArray to check if the word is present and store it's return value 					
			if (posSecondArray!=-1) {																										//If the previously obtained value is not -1 we found a common word
				int posFirstArray=i;																										//We store in an integer the position in the first String of said word 
				combinedStringArray=combineArray(firstStringArray, secondStringArray, posFirstArray, posSecondArray);						//We call the method combineArray with the 2 String Arrays and 2 positions and store the returned string in an Array
				break;																														//break out of the loop if a word is found, we won't check if further words are repeated between the 2 strings
			}
		}
		if (posSecondArray==-1) {																											//If the position of the second word is still -1 this is true
			throw new IllegalArgumentException("There is no common word between the two strings. Please check the input Strings.");			//Thus as asked in the exercise we throw and Illegal Argument Exception with an informative text
		}
		String finalString=join(combinedStringArray);																						//We finally call the method join the convert the Array String into a String, we store the returned value in a String
		return finalString;																													//We return to the main method the final String
	}
}
