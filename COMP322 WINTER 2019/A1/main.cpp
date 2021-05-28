
#include <iostream>
#include <string>
#include <stdlib.h> 

using namespace std;

void countLetter();
void convertPhonetic();
void factorial();
unsigned int tailFactorial(unsigned int n, unsigned int a);
void enhancedFactorial();
unsigned int tailEnhancedFactorial(unsigned int n, unsigned int a, int (&f)[6]);


int main()
{
    countLetter();
    
    convertPhonetic();
    
	factorial();
    
    enhancedFactorial();
    
    return 0;
}

/*
This method takes no input from main. It requests a sentence and a letter from the user.
The letter is searched throughout the sentence.

To count both upper and lower case encounters we convert the input letter to lower case.
We do the same for the sentence.

We call the public member function ::find on the string and subsequent substrings. 
::find takes 2 parameters: the char we are looking for and the starting position.
::find return 1 if it found an occurence or 0 otherwise. 
When the method returns 1 we increase an int counter and replace the current string with a 
substring to find the values after the position of the match we just found.
*/
 void countLetter(){
     int count=0, pos=0;
     string s;
     char c;
     cout<<"Please enter a sentence: "<<endl;
     getline (cin, s);
     cout<<"Please enter a letter: "<<endl;
     cin>>c;
	 c=tolower(c);

	 for (int i=0; i<s.length(); i++){
		 s[i]=tolower(s[i]);
	 }

     while (s.find(c, 0) != -1){
		pos=s.find(c, 0);
		if (pos != -1){
		count++;
		s=s.substr(pos+1, s.length());
		}
	 }
	 cout<<"The letter "<<c<<" is repeated "<<count<<" times in the sentence"<<endl;
 }

 /*
 This method takes no input from main. It requests a word input from the user to convert it to NATO code.

 The NATO code is stored in a 2d string array.

 The method iterates over each character of the word, converts it to lower case and gets its int value (see ASCII table). 
 The value must fall within an adjusted range to verify it is an alphabetical character, if not it'll be ignored.
 If the input is valid he method displays the coresponding coded word to the terminal.
 */
 void convertPhonetic(){
	 string s, code[26] = {"Alfa", "Bravo", "Charlie",
			"Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
			"Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "Xray", "Yankee", "Zulu"};

	 cout<<"Please enter a word. Non alphabetic characters will be ignored!: "<<endl;
     cin>>s;

	 for (int i=0; i<s.length(); i++){
		 int value=(char) tolower(s[i])-97;
		 if ((value>0) && (value<26)){
			 cout<<code[value]<<" ";
		 }
	 }
	 cout<<endl;
 }

 /*
 This method takes no input from main. It prompts the user to enter a number to calculate its factorial.

 It calls a tail recursive method that will do the calculation and return the result.
 */
 void factorial(){
	unsigned int n, res=0;
	cout<<"Please enter a number"<<endl;
	cin>>n; 
	res=tailFactorial(n, 1);
	cout<<"The factorial of "<<n<<" is "<<res<<endl;
 }

 /*
 This method takes 2 parameters: 2 unsigned ints where ~n~ is the number that we are calculating the factorial of; it's value decreases as we do
 recursive calls as per tail recursion Modus Operandi. The 2nd value is the previous recursive call multiplication of factors.
 This is a tail recursive function.

 The base case is reached when n=0, in which case the method returns a number that corresponds to the previous multiplication of factors.
 Otherwise we pass a decreasing n and the multiplication of factors in the recursive call.
 */
 unsigned int tailFactorial(unsigned int n, unsigned int a) {
	 if (n==0) return a;
	 else {
		 return tailFactorial(n-1, n*a);
	 }
 }

 /*
 This method takes no input from main. It prompts the user to enter a number to calculate its factorial.
 It has a local factorial array that stores the first 6 factorials results.

 It calls a tail recursive method that will do the calculation and return the result.
 */
 void enhancedFactorial(){
	 int factorials[] = {1, 2, 6, 24, 120, 720};
	 unsigned int n, res=0;
	 cout<<"Please enter a number"<<endl;
	 cin>>n; 
	 res=tailEnhancedFactorial(n, 1, factorials);
	 cout<<"The factorial of "<<n<<" is "<<res<<endl;
 }

 /*
 This method takes 3 parameters: 2 unsigned ints and a reference to an array. ~n~ is the number that we are calculating the factorial of; it's value decreases as we do
 recursive calls as per tail recursion Modus Operandi. The 2nd value is the previous recursive call multiplication of factors. The array contains 
 the first 6 factorials results.

 The base case is reached when the value ~n~ which we are calculating the factorial is <=6 OR when n has been reduced enough
 times to ==6. In both cases we return the known result stored in the array thus saving some calculation time. 
 Otherwise we pass a decreasing n and the multiplication of factors in the recursive call.
 */
 unsigned int tailEnhancedFactorial(unsigned int n, unsigned int a, int (&f)[6]){
	  if (n<=6) return f[n-1];
	 else {
		 return tailFactorial(n-1, n*a);
	 }
 }

 /*
 Q3:
 Reursion works by breaking a complicated problem into smaller easily solvable parts.
 However as the recursive calls go the stack gets filled. Unnoticeable in smalls programs
 it poses a problem for bigger programs. 
 Tail recursion solves this. In a tail recursive method the computations are done first
 so that the last thing left to do is the value return. Optimized compiler recognize this
 and instead of creating more Stack frames they reuse the current one thus saving space (tail
 call elimination). 
 A drawback however is that it makes debugging harder in the Stack.

 Not all recursions can be designed as tail recursive, however they can be designed as
 "continuation passing style" which is similar. CPS has tail calls that are not recursive.
 A CPS function takes an extra parameter, a callback or continuation, which is a function. Instead of 
 returning a result it calls the callback on the result.

 Sources:
 http://wiki.c2.com/?TailRecursion
 https://cs.stackexchange.com/questions/41768/can-any-recursion-implementation-be-written-as-tail-recursion
 https://en.wikipedia.org/wiki/Continuation-passing_style#Tail_calls

 */