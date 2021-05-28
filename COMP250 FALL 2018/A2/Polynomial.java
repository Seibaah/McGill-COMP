
package assignments2018.a2template;

import java.math.BigInteger;
import java.util.Iterator;

public class Polynomial 
{
	private SLinkedList<Term> polynomial;
	public int size()
	{	
		return polynomial.size();
	}
	private Polynomial(SLinkedList<Term> p)
	{
		polynomial = p;
	}
	
	public Polynomial()
	{
		polynomial = new SLinkedList<Term>();
	}
	
	// Returns a deep copy of the object.
	public Polynomial deepClone()
	{	
		return new Polynomial(polynomial.deepClone());
	}
	
	/* 
	 * TODO: Add new term to the polynomial. Also ensure the polynomial is
	 * in decreasing order of exponent.
	 */
	
	// Hint: Notice that the function SLinkedList.get(index) method is O(n), 
	// so if this method were to call the get(index) 
	// method n times then the method would be O(n^2).
	// Instead, use a Java enhanced for loop to iterate through 
	// the terms of an SLinkedList.
	/*
	for (Term currentTerm: polynomial)
	{
		// The for loop iterates over each term in the polynomial!!
		// Example: System.out.println(currentTerm.getExponent()) should print the exponents of each term in the polynomial when it is not empty.  
	}
	*/
	
	public void addTerm(Term t)
	{		
		/**** ADD CODE HERE ****/ 
		/*
		 *Method that adds a term to a polynomial (this.)
		 *We run a for enhanced loop. In it first we test if the current term has the same exponent as the one we are adding.
		 *If so it will combine the two by adding the coefficients and keeping the current exponent. When this happens we 
		 *can exit the method with a return statement.
		 *
		 *If there isn't a term with the same exponent we need to find the spot where our term needs to go.
		 *The simplest way is that when we find an exponent that is smaller than "Term t" then we insert t at the current index.
		 *After that we can return.
		 *
		 *If none of the 2 conditions is met we increase our counter variable so that when we find where to add a term we 
		 *have the correct index.
		 *
		 *If the term we are adding has (x^0) our for loop will run entirely and add "Term t" at the last index of the polynomial
		 *which is conveniently stored by count.
		 */
		
			int count = 0;
			BigInteger tCoef, nCoef, sum;
			
			for (Term n: this.polynomial) {
				if (t.getExponent()==n.getExponent()) {
					tCoef=t.getCoefficient();
					nCoef=n.getCoefficient();
					sum=tCoef.add(nCoef);
					n.setCoefficient(sum);
					
					return;
				}
				if (t.getExponent()>n.getExponent()) {
					this.polynomial.add(count, t);
					return;
				}
				count++;
			}
			this.polynomial.add(count, t); 
	}
	
	public Term getTerm(int index)
	{
		return polynomial.get(index);
	}
	
	//TODO: Add two polynomial without modifying either
	public static Polynomial add(Polynomial p1, Polynomial p2)
	{
		/**** ADD CODE HERE ****/  
		/*
		 * Method that adds two polynomials. The resultant polynomial must have terms exponents in decreasing order.
		 * 
		 * First we create a deepClone of each polynomial and create an empty one to store the sum.
		 * Then we create an iterator object for each to iterate through both deepCopies. We store the terms in variables.
		 * We create int variables to store the exponents.
		 * thanks to the iterators and .next()
		 * 
		 * We create a while loop that will run while at least one of the polynomials has terms left to add.
		 * In the while, at the beginning of each iteration we get the exponent of the current term.
		 * 
		 * First we'll test if the 1st polynomial doesn't have a next term. If so, then we iterate only through the
		 * other polynomial and we add it's terms to our sum polynomial. We don't need to check for exponents nd order as we
		 * know inside each poly the terms are ordered. When said polynomial doesn't have any next terms we exit the while.
		 * We add the current term (last) term for the 2nd polynomial and then do the same for poly1. Once done we break out of
		 * the loop and return the sum polynomial.
		 * 
		 * The next "else if block" does the same as stated before except that this time it's in case the p2 is shorter than p1.
		 * 
		 * Now, if our polynomials are of equal size or aren't at their last term we can start comparing. 
		 * 
		 * If their exponents are the same then we just add the coefficients and keep the current exponent. We add the new 
		 * term to our sum polynomial, get the next terms in each polynomial and keep them in "t1" and "t2" and use 
		 * continue to directly skip the rest of the current iteration.
		 * 
		 * If the exponent of t1 is bigger than t2 we add t1 to the sum polynomial, get the next term in p1 and use continue.
		 * Else it means t1Exp<t2Exp so we add t2 to sum poly, get the next term in p2 and use continue.
		 * 
		 * After adding all the terms we return the sum polynomial
		 * 
		 */
		Polynomial p1DC = p1.deepClone(), p2DC = p2.deepClone(), sum = new Polynomial();
		Iterator<Term> itr1 = p1DC.polynomial.iterator(), itr2 = p2DC.polynomial.iterator();
		Term t1 = itr1.next(), t2 = itr2.next();
		int t1Exp, t2Exp;
		
		while ((itr1.hasNext()) || (itr2.hasNext())) {
			t1Exp = t1.getExponent(); 
			t2Exp = t2.getExponent();
			
			if (!itr1.hasNext()) {	
				while (itr2.hasNext()) {
					sum.addTermLast(t2);
					t2 = itr2.next();
				}
				sum.addTerm(t2);
				sum.addTerm(t1);
				break;
			}
			if (!itr2.hasNext()) {	
				 while (itr1.hasNext()) {
				 	sum.addTermLast(t1);
				 	t1 = itr1.next();
				 }
				 sum.addTerm(t1);	
				 sum.addTerm(t2);
				 break;
					}
			
			if (t1Exp==t2Exp) {	
				
				BigInteger t1Coef = t1.getCoefficient(), t2Coef = t2.getCoefficient(), sumCoef = t1Coef.add(t2Coef);
				
				t1.setCoefficient(sumCoef);
				sum.addTermLast(t1);
				t1 = itr1.next();
				t2 = itr2.next();
				continue;
				
			}
			if (t1Exp > t2Exp) {		
				sum.addTermLast(t1);
				t1 = itr1.next();
				continue;
						
			} 
			if (t1Exp<t2Exp) {		
				sum.addTermLast(t2);
				t2 = itr2.next();
				continue;
								
			}
		}
		return sum;
	}
	
	//TODO: multiply this polynomial by a given term.
	private void multiplyTerm(Term t) 
	{	
		/**** ADD CODE HERE ****/
		/*
		 * Method that multiplies a this.polynomial by a term "t".
		 * 
		 * First we get the exponent and coefficient of "t". We also create a BigInteger equal to 0.
		 * 
		 * Even though it may not have been required we check if our term coefficient is 0. If so then we create an empty 
		 * zero polynomial and set the polynomial we were set to multiply equal to a deepClone of the new zeroPoly.
		 * 
		 * If we are not multiplying a poly by 0 then we create a for enhanced loop. First  we get the coefficient 
		 * of n and then we replace it by the multiplication of itself and the coefficient of t. Secondly, we set the 
		 * exponent of n to the multiplication of "n" and "t" exponents. 
		 * 
		 * Now for each term "n" in the polynomial we first test if the exponent is 0. 
		 * 
		 * If so then we set n's exponent to the exponent of t. Then we use break since this will only happen when we reach
		 * the end of this.polynomial (aka last possible term x^0).
		 * 
		 * If exp isn't 0 then we get n's exponent and then set it to the multiplication of n's and t's exponent.
		 * 
		 * This will multiply a full polynomial by a given term.
		 * 
		 */
		int tExp=t.getExponent();
		BigInteger tCoef=t.getCoefficient(), zeroBI= new BigInteger("0");
		
		if ((tCoef.compareTo(zeroBI))==0) {
			Polynomial zeroPoly= new Polynomial();
			this.polynomial=zeroPoly.polynomial.deepClone();
		} else {
			for (Term n: this.polynomial) {
				BigInteger nCoef=n.getCoefficient();
				n.setCoefficient(tCoef.multiply(nCoef));
				
				if (n.getExponent()==0) {
					n.setExponent(tExp);
					break;
				}
				
				int nExp=n.getExponent();
				n.setExponent(nExp+tExp);
			}
		}
	}
	
	//TODO: multiply two polynomials
	public static Polynomial multiply(Polynomial p1, Polynomial p2)
	{
		/**** ADD CODE HERE ****/
		/*
		 * This method will multiply 2 polynomials. We created 2 helper private methods to achieve this in fast enough
		 * to meet the time complexity requirements. In the directory for Assignment 2 you may see older versions and 
		 * how they compare to each other.
		 * 
		 * First, we create some variables: a counter, a condition to test which poly is longer and a lot of polynomial obj.
		 * We create a poly for the shortest and longest arguments; a resultant and a temporary poly.
		 * 
		 * Using our bool variable we test to see which is the longest argument and store a deepclone each one in their 
		 * respective polynomial obj.
		 * 
		 * Using a for enhanced loop we iterate through the longest polynomial. Each time we loop we store in a temporary
		 * poly the multiplied polynomial returned by my multiply helper method (shortestPoly*current term in longest poly). 
		 * Main difference between my helper method and the stock multiplyTerm is that this one returns an modified poly.
		 * 
		 * Now, the 1st time we loop, since our resultant polynomial is empty, we can't use the normal add method as it will
		 * produce an exception. So we use a 2nd private helper method (addEmpty) that will add the terms to our empty list
		 * without producing an exception. We used the counter variable to trigger this the 1st time we loop. We increase the 
		 * counter to never use the method again.
		 * 
		 * Now that our array isn't  empty we can call the stock add method to add the contents or res and the new terms 
		 * in temp.
		 * 
		 * Once we finish looping through the longestPoly we return the resultant list.
		 * 
		 */
		int count=0;
		boolean cond=(p1.size()>p2.size());
		Polynomial longestDC, shortestDC, res= new Polynomial(), temp= new Polynomial();
		
		if (cond) {
			longestDC=p1.deepClone(); 
			shortestDC=p2.deepClone();
		} else { 
			longestDC=p2.deepClone(); 
			shortestDC=p1.deepClone();
		}
		for (Term nl: longestDC.polynomial) {
			 	temp=shortestDC.multiplyTerm_v2(nl);
				if (count==0) {
					res=res.addEmpty(res, temp);
					count++;
				} else {
					res=res.add(res, temp);
				}
		}
		return res;
	}
	
	private Polynomial multiplyTerm_v2(Term t) 
	{	
		//Does the same as multiplyTerm, except it does it on a polynomial that it returns. Use newPoly instead of this.
		
		int tExp=t.getExponent();
		BigInteger tCoef=t.getCoefficient(), zeroBI= new BigInteger("0");
		Polynomial newPoly= this.deepClone();
		
		if ((tCoef.compareTo(zeroBI))==0) {
			Polynomial zeroPoly= new Polynomial();
			newPoly.polynomial=zeroPoly.polynomial.deepClone();
		} else {
			for (Term n: newPoly.polynomial) {
				BigInteger nCoef=n.getCoefficient();
				n.setCoefficient(tCoef.multiply(nCoef));
				
				if (n.getExponent()==0) {
					n.setExponent(tExp);
					break;
				}
				
				int nExp=n.getExponent();
				n.setExponent(nExp+tExp);
			}
		}
		return newPoly;
	}
	
	
	private Polynomial addEmpty (Polynomial basket, Polynomial p1) {
		//This method adds the terms in p1 to an empty "basket" polynomial. Only called once.
						
		for (Term x: p1.polynomial) {
				basket.addTerm(x);
		}
		return basket;
	}
	
	//TODO: evaluate this polynomial.
	// Hint:  The time complexity of eval() must be order O(m), 
	// where m is the largest degree of the polynomial. Notice 
	// that the function SLinkedList.get(index) method is O(m), 
	// so if your eval() method were to call the get(index) 
	// method m times then your eval method would be O(m^2).
	// Instead, use a Java enhanced for loop to iterate through 
	// the terms of an SLinkedList.

	public BigInteger eval(BigInteger x)
	{
		/**** ADD CODE HERE ****/
		/*Prepare yourself for a ride
		 * We are writing a function to apply Horner's method to evaluate a polynomial that might have some
		 * missing terms, however all the terms are already in descending exponent order. ex: 5x^5 + 3x^2 + 1
		 * 
		 * We divided the Honer's method in 2 steps, the multiplication and the addition
		 *
		 * Some definitions: We'll call a skip is the exponent of the polynomial goes from, let's say 
		 * for example, 5 -> 2. It skipped exponent 4 and 3. What this means for our calculation is that
		 * only the multiplication has to occur and we have to skip the addition process of Honer's method.
		 * In this scenario the multiplication happens 5-2 times. In our case, due to how we organized our code, it'd 
		 * happens (5-2)-1 times since we have a multiplication execution outside of the if statement that checks 
		 * for a skip. Why? Simply because no matter what, there is always at least multiplication that happens 
		 * per cycle. This can probably be done better...
		 * 
		 * At the end, we have a while loop. This is in case the polynomial doesn't have the term with x^0. Without
		 * this it would miss the last possible skip at the end of the polynomial. By testing what was our last 
		 * exponent in prevExp we can calculate how many cycle mandatory multiplications we have left and perform 
		 * them in the loop without affecting our time execution substantially.
		 * 
		 */
		
		BigInteger sumRes= new BigInteger("0") ; 		//creating a partial sum variable big Int
		int cond=0, prevExp=0; 							//Creating a one time condition var and a prevExp var
		
		for (Term curr: polynomial) { 					//Enhanced for loop
			
			sumRes=(sumRes.multiply(x));				//Mandatory multiplication per cycle
			
			if (cond==1) {									//if that will only trigger after the first cycle
				if (prevExp!=curr.getExponent()+1) {		//testing if there is a skip but comparing exponent difference
					
					int dif=prevExp-curr.getExponent()-1;	//storing the skip difference-1 in a var
					
					while (dif>0) {							//while loop to do the multiplication dif times
						sumRes=(sumRes.multiply(x));		//multiplication execution
						dif--;								//reducing dif counter
					}
					
					sumRes=(sumRes.add(curr.getCoefficient())); //after all the multiplications have been done we do the addition operation
					dif=0;										//we reset the difference between exponents
					prevExp=curr.getExponent();					//we set the previous Exponent to the exp of the current term in preparation for the next iteration
					
					continue;									//we skip the rest of the loop iteration
				} else {										
					prevExp=curr.getExponent();					//we set the previous Exponent to the exp of the current term in preparation for the next iteration
				}
			}
			if (cond==0) {							//if that will only trigger in the first iteration
				prevExp=curr.getExponent();			//we set the previous exponent value to the current one, since there can't be a skip on the first term this process works slightly differently for i=0
				cond=1;								//we set cond=1 so the previous chunk is always evaluated from now on instead of this
			}
			sumRes=(sumRes.add(curr.getCoefficient())); //If there is no skip we do the mandatory addition
		}
		
		while (prevExp>0) {							//while to see if the polynomial didn't end in an x^0 term
			sumRes=(sumRes.multiply(x));			//if the polynomial didn't end in x^0 we gotta perform extra multiplications
			prevExp--;								//reducing the prevExp until it hits 0, and no multiplication is left to be done
		}
	
		return sumRes;								//returning the partialsum
	}
	
	// Checks if this polynomial is same as the polynomial in the argument.
	// Used for testing whether two polynomials have same content but occupy disjoint space in memory.
	// Do not change this code, doing so may result in incorrect grades.
	public boolean checkEqual(Polynomial p)
	{	
		// Test for null pointer exceptions!!
		// Clearly two polynomials are not same if they have different number of terms
		if (polynomial == null || p.polynomial == null || size() != p.size())
			return false;
		
		int index = 0;
		// Simultaneously traverse both this polynomial and argument. 
		for (Term term0 : polynomial)
		{
			// This is inefficient, ideally you'd use iterator for sequential access.
			Term term1 = p.getTerm(index);
			
			if (term0.getExponent() != term1.getExponent() || // Check if the exponents are not same
				term0.getCoefficient().compareTo(term1.getCoefficient()) != 0 || // Check if the coefficients are not same
				term1 == term0) // Check if the both term occupy same memory location.
					return false;
			
			index++;
		}
		return true;
	}
	
	// This method blindly adds a term to the end of LinkedList polynomial. 
	// Avoid using this method in your implementation as it is only used for testing.
	// Do not change this code, doing so may result in incorrect grades.
	public void addTermLast(Term t)
	{	
		polynomial.addLast(t);
	}
	
	// This is used for testing multiplyTerm.
	// Do not change this code, doing so may result in incorrect grades.
	public void multiplyTermTest(Term t)
	{
		multiplyTerm(t);
	}
	
	@Override
	public String toString()
	{	
		if (polynomial.size() == 0) return "0";
		return polynomial.toString();
	}
}
