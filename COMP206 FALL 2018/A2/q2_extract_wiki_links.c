#include<stdio.h>
#include<string.h>
#include<stdlib.h>

int main(int argc, char *argv[]){

	int len;
	char line[200], *result_1, *result_2, *result_3, *titlePage, *token;
	const char needle_1[]="<a href=\"/wiki/", needle_2[]="title=\"", needle_3[]="</a>", needle_4[]="\"", s[2]="\"";
	if (argc != 2){
		printf("ERROR: Wrong number of arguments. One required.\n");
		return -1;
	}
	char *link = argv[1];
	//printf("%s", link);
	FILE *f=fopen (link, "r");
	if (f == NULL){
		printf("Can't open the file\n");
	} else {
		while (!feof(f)){
			fgets(line, 200, f);
			//printf("%s",line);
			//puts(line);
			result_1 = strstr(line, needle_1);
			if (result_1 != NULL){
				//printf("One\n");
				//printf("%s\n", result_1);
				result_2 = strstr(result_1, needle_2);
				//printf("%s\n", result_2);
				if (result_2 != NULL){
					//printf("Two\n");
					result_3=strstr(result_2, needle_3);				
					if (result_3 != NULL){
						result_2+=7;
						token = strtok (result_2, s);
						printf("%s\n", token);
					} //else printf("String not found\n");
				} //else printf("String not found\n");
			} //else printf("String not found\n");
		}
	fclose(f);
	}
return 0;
}
