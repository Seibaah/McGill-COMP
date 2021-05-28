#include <CONIO.H>

int main(){
    int times=5;	//loop var 

    while (times>0){	//code will execute 5 times

        delay(500);		//delay command to pace the code execution

        gotoxy(5,5);	//start coordinate of the wait wheel
        printf("|");	//print char
        delay(500);		//delay command to give animation sensation

		//same as described abovce but with a different char at a different position
        gotoxy(5,4);
        printf("/");
        delay(500);

        gotoxy(6,4);
        printf("-");
        delay(500);

        gotoxy(7,4);
		printf("\\");
        delay(500);

        gotoxy(7,5);
        printf("|");
        delay(500);

        gotoxy(7,6);
        printf("/");
        delay(500);

        gotoxy(6,6);
        printf("-");
        delay(500);

        gotoxy(5,6);
		printf("\\");
        delay(500);

        times--;	//reduce times variable
        clrscr();	//clear screen to start a new wheel
    }
return 0;
}