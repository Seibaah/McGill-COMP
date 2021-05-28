#include<stdio.h>
#include<stdlib.h>

int main (int argc, char *argv[]){
	if (argc != 2){
		printf("ERROR: Wrong numbers of arguments. One required.\n"); 
		return -1; 
	}
	char *s = argv[1];
	int size = atoi(s);
	if (size%2 != 1){
		printf("ERROR: Bad argument. Height must be positive odd integer.\n");
		return -1;
	}
	else if ((argc = 2)&&((size%2)!=0)){ 
		int i=size;
		int r=0;
		char matrix[i][i];
		for (int j=0; j<i; j++){
			for (int k=0; k<i; k++){
				if ((k>=(i/2)-r)&&(k<=(i/2)+r)){
					matrix[j][k]='*';
					printf("%c ",matrix[j][k]);
				}
				else {
					matrix [j][k]=' ';
					printf("%c ",matrix[j][k]);
				}	
			}	
			if (j<(i/2)){
				r++;
			} else if (j>=(i/2)){
				r--;
			}
			printf("\n");
		}
	}	
return 0;
}


