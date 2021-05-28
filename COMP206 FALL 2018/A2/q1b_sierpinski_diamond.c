#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<stdbool.h>

//int test[][]={{1},{1,1},{1,0,0,1},{1,1,1,1}};
//bool test[][]={{true},{true,true},{true,false,false,true},{true,true,true,true}};
//matrix[x][y]

int createTrigPresetHeight(int y2, int tRows){
	return y2/tRows;
}


int main (int argc, char *argv[]){
	
	int test[4][4]={{1},{1,1},{1,0,0,1},{1,1,1,1}};
	if (argc != 3){
		printf("ERROR: Wrong numbers of arguments. Two required.\n"); 
		return -1; 
	}
	char *s = argv[1], *s2 = argv[2];
	int size = atoi(s), level = atoi(s2);
	if (size%2 != 1){
		printf("ERROR: Bad argument. Height must be positive odd integer.\n");
		return -1;
	}//Still gotta check for the unique condition to this problem
		else if ((argc = 3)&&((size%2)!=0)){ 
			if (level==1){

			//Copy paste algo from Q1A

			} else {
				int y=size+1, trigRows=pow(2, (level-1)), baseTrigs=trigRows;
				int h=createTrigPresetHeight(y, trigRows), baseSingleTrig=h;
				char matrix[baseTrigs][y], *mat=matrix;
				int posX=(baseTrigs+1)/2, posY=0;
				bool skip=false;
				for (int i=0; i<sizeof(test); i++){
					for (int j=0; j<sizeof(test[i]); j++){
						if (test[i][j]==true){
							*matrix=drawTrig(h, matrix, posX, posY);
						}else {
							if (!skip){
								*matrix=drawFakeTrig(h, matrix, posX, posY);
							}
							skip=true;
						}
						posX-=h;
					}
					posY+=h;
				}
			 
			}	
		}	
return 0;
}

char drawTrig (int height, char[][] m, int copyPosX, int copyPosY){
	int rX=1, rY=1, copy_2PosX=copyPosX;
	for (int i=0; i<height; i++){
		for (int j=0; j<rX; j++){
			m[copyPosX][copyPosY]=='*';
			copyPosX++;
		}
		copyPosX-=rX;
		copyPosY+=rY;
		rX+=2;
	}
return m;
}


char drawFakeTrig (int height, char[][] m, int copyPosX, int copyPosY){
	int rX=1, rY=1, copy_2PosX=copyPosX;
	for (int i=0; i<height; i++){
		for (int j=0; j<rX; j++){
			m[copyPosX][copyPosY]=='A';
			copyPosX++;
		}
		copyPosX-=rX;
		copyPosY+=rY;
		rX+=2;
	}
return m;
}
