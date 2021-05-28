
    
public class Calculator
{

	public static void main(String[] args)
	{
		System.out.println("Welcome to the Calculator program!");

		if (args.length < 3)
		{
			System.out.println("You need to enter three arguments to this program. Try typing 'run Calculator 5 5 1' in Dr. Java, or 'java Calculator 5 5 1' on the command line.");
			return;
		}

		int a = getIntegerNumber(args[0]);
		int b = getIntegerNumber(args[1]);
		double c = getDoubleNumber(args[2]);

		System.out.println("The first argument a is: " + a);
		System.out.println("The second argument b is: " + b);
		System.out.println("The third argument c is: " + c);

    //========================
    //Enter your code below
		
		int sum_ab, product_ab, division_ab;		//Initializing integer variables and naming them accordingly with the operations and variables they use
		double division_ac; 						//Initializing the variable for the division between a and c
		boolean larger_than=false, odd=true;		//Initializing 2 boolean variables to evaluate if a is larger than b; and if a is an odd number
		sum_ab=a+b; 								//Assigning to sum_ab the result of a+b
		product_ab=a*b; 							//the product of a times b is stored in the variable product_ab
		division_ab=a/b;							//the integer division of a by b is stored in the variable division_ab
		division_ac=a/c;							//the division of a by c is stored in the variable division_ac
		if (a>b) {									//testing with an if statement if a is larger tan b
			larger_than=true;						//the variable larger_than will be true only if a>b, otherwise it stays false
		}
		if (a%2==0) {								//testing with an if statement if a is odd with the modulus(%) operator
			odd=false;								//the variable odd will be set to false if the rest of a/2 is 0, otherwise it stays true
		}
		//the next 6 lines combine concatenation of a string plus a variable to show the results of the operations we made
		System.out.println("Sum of a and b: " + sum_ab);
		System.out.println("Product of a and b: " + product_ab);
		System.out.println("Dividing a by b: " + division_ab);
		System.out.println("Dividing a by c: " + division_ac);
		System.out.println("Is a larger than b: " + larger_than);
		System.out.println("Is a odd: " + odd);
		
    //Enter your code above
    //========================
	}

	public static int getIntegerNumber(String arg)
	{
		try
		{
			return Integer.parseInt(arg);
		}catch(NumberFormatException e)
		{
			System.out.println("ERROR: " + e.getMessage() + " This argument must be an integer!");
		}
    	
    	//error, return 1
		return 1;
	}

	public static double getDoubleNumber(String arg)
	{
		try
		{
			return Double.parseDouble(arg);
		}catch(NumberFormatException e)
		{
			System.out.println("ERROR: " + e.getMessage() + " This argument must be a double!");
		}
    	
    	//error, return 1.0
		return 1.0;
	}
}
