
#include <iostream>
#include <string>
#include <stdlib.h> 
#include <time.h>
#include <algorithm>

using namespace std;

struct ticket {
	unsigned int numbers[6];
	ticket* next;
};

class SuperDraw {
	public :
	SuperDraw();
	SuperDraw(int);
	SuperDraw(const SuperDraw&);
	~SuperDraw();
	void newTicket(int);
	void printAllTicketNumbers();
	void verifySequence(int[]);
	void deleteSequence(int[]);
	private :
	ticket* ticketListHead;
	ticket* ticketListTail;
};

//Constructor. By specification head and tail pointers are set to NULL 
SuperDraw::SuperDraw(){
	ticketListHead=NULL;
	ticketListTail=NULL;
}

//Constructor. By specification generates n tickets where n>=0 is an int parameter. The tickets are not printed.
SuperDraw::SuperDraw(int n){
	if (n<0) {
		throw "Invalid input. Enter a positive integer.";
	} else {
		ticketListHead=NULL;
		ticketListTail=NULL;
		while (n>0) {
			newTicket(0);
			n--;
		}
	}
}

//Copy Constructor. By specification it produces a deepcopy of the passed object.
SuperDraw::SuperDraw(const SuperDraw &sd) {
	if (sd.ticketListHead==NULL) {
		ticketListHead=NULL;
		ticketListTail=NULL;
	} else {

		//Deep copy head
		ticket *newTic = new ticket;
		for (int i=0; i<6; i++) {
			newTic->numbers[i]=sd.ticketListHead->numbers[i];
		}
		newTic->next=NULL;
		ticketListHead=newTic;
		ticketListTail=newTic;

		//Deep copy the following tickets
		//Get current node from the referenced obj
		ticket *curr=sd.ticketListHead->next;
		while (curr!=NULL){
			
			//Create new ticket and copy each number one by one
			ticket *newTic=new ticket;
			for (int i=0; i<6; i++) {
				newTic->numbers[i]=curr->numbers[i];
			}

			//Update linking and go to the next ticket to be copied
			newTic->next=NULL;
			ticketListTail->next=newTic;
			ticketListTail=ticketListTail->next;
			curr=curr->next;
		}
	}
}

//Destructor. By specification each node is deleted
SuperDraw::~SuperDraw(){
	ticket* curr=ticketListHead;
	ticket* next;

	while (curr!=NULL) {
		next=curr->next;
		delete curr;
		curr=next;
	}
}

/*
Generates 6 pseudo random numbers with a ctime seed. 
To avoid duplicates the method checks back after each generation its array. 
If a match beside the number itself is found the method regenerates the number.
When 6 different numbers have been generated, they are sorted by in ascending order.
If verbose is set to 1 (0 by default) the ticket numbers are printed.
Finally, the new ticket is added at the end of the linked list.
*/
void SuperDraw::newTicket(int verbose=0) {
	int a[6]={0,0,0,0,0,0}, count;
	ticket *newTic = new ticket;

	//Generates random numbers and puts them in an array
	for (int i=0; i<6; i++) {
		int r=(rand()%49)+1;
		a[i]=r;
		count=0;

		//Checks back if said number was already generated in the storing array. If so said slot is re-generated.
		for (int j=0; j<6; j++) {
			if (a[j]==r)  {
				count++;
			}
			if (count>1) {
				i--;
				break;
			}
		}
	}

	//Sorting in ascending order
	sort(begin(a), end(a));

	//Copying the finalized sequence to the ticket obj
	for (int k=0; k<6; k++) {
		newTic->numbers[k]=a[k];
		if (verbose==1) {
			cout<<a[k]<<" ";
			if (k==5) {
				cout<<endl;
			}
		}
	}

	//Updating the list pointers
	newTic->next=NULL;

	if (ticketListHead==NULL) {
		ticketListHead=newTic;
		ticketListTail=newTic;
	} else {
		ticketListTail->next=newTic;
		ticketListTail=ticketListTail->next;
	}
	
}

//Method that prints all the tickets currently in the list.
void SuperDraw::printAllTicketNumbers(){
	ticket* curr=ticketListHead;
	while(curr!=NULL) {
		for(int i=0; i<6; i++){
			cout<<curr->numbers[i]<<" ";
		}
		cout<<endl;
		curr=curr->next;
	}
}

//Method that searches for a match to an input array in the generated tickets by comparing each number individually for each ticket.
void SuperDraw::verifySequence(int a[]){
	ticket* curr=ticketListHead;
	int c;
	bool found=false;
	while(curr!=NULL) {
		c=0;
		for(int i=0; i<6; i++){
			if (curr->numbers[i]!=a[i]){
				break;
			} else { c++; }
		}
		if (c==6) {
			cout<<"The provided sequence of numbers was already generated."<<endl;
			found=true;
		}
		curr=curr->next;
	}
	if (found==false) {
		cout<<"The provided sequence of numbers was never generated before"<<endl;
	}
}

//Method that deletes a ticket of the list if it matches the input array numbers.
void SuperDraw::deleteSequence(int a[]){
	ticket* curr=ticketListHead;
	int c, pos=-1;
	bool found=false;

	//Loops through the all the tickets and compare the lottery numbers
	while(curr!=NULL) {
		c=0;
		pos++;
		for(int i=0; i<6; i++){
			if (curr->numbers[i]!=a[i]){
				break;
			} else { c++; }
		}
		//Code to delete a node if a match is found
		if (c==6) {
			ticket *node=ticketListHead;

			//If the match is the head
			if (pos==0) {
				ticketListHead=node->next;
				free(node);
				cout<<"The provided sequence of numbers was successfully deleted."<<endl;
				return;
			}

			//Find the node that comes before the match
			for(int i=0; node!=NULL && i<pos-1; i++) {
				node=node->next;
			}

			//node->net is the mtch to be deleted
			ticket *next=node->next->next;

			//Unlinking the match from the list
			free(node->next);
			node->next=next;

			//Updating curr node for the next iteration of the search.
			curr=node;
			cout<<"The provided sequence of numbers was successfully deleted."<<endl;
		} else {
			//Updating curr node for the next iteration of the search. Only happens if this ticket isn't a match
			curr=curr->next;
		}
	}
	if (found==false) {
		cout<<"The provided sequence of numbers was never generated before"<<endl;
	}
}

int main() {
	//Initializing random seed
	srand((float)time(NULL));

	//Creating 6 tickets
	SuperDraw sd(2);
	sd.newTicket(0);
	sd.newTicket(0);
	sd.newTicket(0);
	sd.newTicket(0);

	//Print all tickets in sd
	sd.printAllTicketNumbers();
	cout<<endl;
	
	//Creating a deepcopy of sd using the copy constructor and printing it
	SuperDraw sd2(sd);
	sd2.printAllTicketNumbers();
	cout<<endl;

	//Looking for a specific array match in the tickets. If a match is found it will be deleted.
	int myNumbers[6] = {2, 4, 17, 29, 31, 34};
	sd.verifySequence(myNumbers);
	sd.deleteSequence(myNumbers);

	//Adding new ticket to sd
	sd.newTicket(0);
	cout<<endl;

	//Printing sd and sd2 to see that the list are independent and now differ
	sd.printAllTicketNumbers();
	cout<<endl;
	sd2.printAllTicketNumbers();
	return 0;
}