

public class LemonadeStand
{
    public static void main(String[] args) {
    	int days=4, sales=60;						//introducing 2 integer variables that will store the values for the parameters numDays and numSales in the method standStats
		double priceTag=8;							//introducing 1 double variable that will store the value for the parameter price in the method standStats
    	for (int i=0; i<5; i++) {					//creating a for loop that will repeat itself 5 times
    		standStats(days, sales, priceTag);		//calling the methods standStats
    		sales-=10;								//reducing the value of sales by 10
    		days--;									//decreasing the value of days by 1
    		priceTag+=0.5;							//increasing the value of priceTag by 0.5
    												//the point of this for loop is to have 5 test cases with different values without having to write more code
    	}
    }
    
    public static double printTotalSales(int numSales, double price){		//initializing method printTotalSales
        Double finalSale=numSales*price;									//calculating the amount of money of the total sale
        return finalSale;													//returning the previously calculated amount
    }
    
    public static int division(int a, int b){													//initializing the method division
        int result;																				//initializing the variable result without assigning a value
        if (b==0){																				//this if statement tests if b is 0
            result=0;																			//sets the result to 0 according to the instructions
            System.out.println("It is not possible to sell lemonade over 0 days. Error!");		//printing an error message
        } else result=a/b;																		//if b isn't 0 the division is executed 
        return result;																			//returning the variable result
    }
    
    public static int getMaximum(int a2, int b2) {		//initializing the method getMaximum
    	if(a2>b2) {										//this if a2 is larger than b2
    		return a2;									//if the statement above is true then it returns the bigger number, a2
    	}else return b2;								//otherwise either b2 is the largest of the two numbers or they are both equal. In that case we may return whichever number. We choose b2.
    }
    
    public static void standStats(int numDays, int numSales, double price) {																			//initializing the method standStats
    	System.out.println("We have sold " + numSales + " units at $" + price + " each, which totals $" + printTotalSales(numSales, price) +".");		//combined an output with a method invocation 
    	int salesPerDay=division(numSales, numDays);																									//assigning to salesPerDay the return value from the method division
    	System.out.println("With " + numSales + " sales over " + numDays + " days, the sales per days were " + salesPerDay + ".");						//combined an output with a method invocation
    	int targetSales=getMaximum(salesPerDay, 10);																									//assigning to targetSales the return value from the method getMaximum
    	System.out.println("The target sales are now: " + targetSales + ".");																			//combined an output with a method invocation
    	System.out.println("");																															//used to separate the outputs from the different reiterations of the test cases
    }
}
