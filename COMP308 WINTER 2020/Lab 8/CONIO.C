#include <stdio.h>
#include <conio.h>

int main()
{
	int i, j, times;
	char c;

	// clear screen
	clrscr();

	// label first 10 lines
	for( i = 1; i <= 10; i++ )
	{
		gotoxy( 1, i );
		printf( "Line # %d", i );
	}

	// prompt for a digit, retrieve digit with getch()
	gotoxy(1,12);
	printf("Press any digit to continue...");
	do
	{
		gotoxy(1,13);
		c = getch();
	}
	while ( c < '0' || c > '9' );


	// append "..again" to every line up to 9 times
	times = (int) c - '0';
	for( j = 1; j <= times; j++ )
	{
		for( i = 1; i <= 10; i++ )
		{
			gotoxy( j*11, i );
			printf( "column #%d", j );
		}
	}

	// wait for a key press using khbit(), then exit
	gotoxy(1,13);
	printf("Press any key to continue...");
	while( !kbhit() );


	return 0;
}