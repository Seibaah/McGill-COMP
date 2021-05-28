/* FILE: A3_solutions.c is where you will code your answers for Assignment 3.
 * 
 * Each of the functions below can be considered a start for you. They have 
 * the correct specification and are set up correctly with the header file to
 * be run by the tester programs.  
 *
 * You should leave all of the code as is, especially making sure not to change
 * any return types, function name, or argument lists, as this will break
 * the automated testing. 
 *
 * Your code should only go within the sections surrounded by
 * comments like "REPLACE EVERTHING FROM HERE... TO HERE.
 *
 * The assignment document and the header A3_solutions.h should help
 * to find out how to complete and test the functions. Good luck!
 *
 */


/*
*Progress: missing padding and storing in an array with malloc the img data
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "A3_provided_functions.h"

unsigned char*
bmp_open( char* bmp_filename,        unsigned int *width, 
          unsigned int *height,      unsigned int *bits_per_pixel, 
          unsigned int *padding,     unsigned int *data_size, 
          unsigned int *data_offset                                  )
{
  unsigned char *img_data=NULL;
  	// REPLACE EVERYTHING FROM HERE
	
	//Opening a file stream and checking if it was succesful
	FILE *bmpfile = fopen( bmp_filename, "rb" );
	if (bmpfile==NULL) {
	  printf("I was unable to open the file. \n");
	  return NULL;
	}

	//Read the first 2 bytes of the files to test if it's a BMP file (precaution)
	char b, m;			
	fread (&b, 1, 1, bmpfile);
	fread (&m, 1, 1, bmpfile);
	if ((b!='B') || (m!='M')){
		printf("BM");
		return NULL;
	}
	
	//Reading the total filesize
	unsigned int fileSize;	
	fread (&fileSize, sizeof(unsigned int), 1, bmpfile);
		
	//Reading the reserved bytes (useless for assignment)
	unsigned int reserved;
	fread (&reserved, sizeof(unsigned int), 1, bmpfile);
	
	//Reading pixel data offset
	fread (data_offset, sizeof(unsigned int), 1, bmpfile);

	//Reading the DIB header
	unsigned int DIBheader;
	fread (&DIBheader, sizeof(unsigned int), 1, bmpfile);
	
	//Reading width and height
	fread (width, 4, 1, bmpfile);
	fread (height, 4, 1, bmpfile);
	
	//Reading planes bytes (useless for assignment)
	unsigned int planes;
	fread (&planes, 2, 1, bmpfile);
	
	//Reading bits_per_pixel in a short int to avoid getting incorrect 
	// data then passing it to the unsigned int pointer
	short int bpp;
	fread (&bpp, 2, 1, bmpfile);
	*bits_per_pixel=bpp;
	
	//Reading compression bytes (useless for assignment)
	unsigned int compression;
	fread (&compression, 4, 1, bmpfile);

	//Reading image size
	fread (data_size, 4, 1, bmpfile);
 	
	//Computing padding (formula from discussion boardd
	*padding=((bpp*(*width)+31)/32)%4; 

	//Rewinding to the beginning of the file
	rewind(bmpfile);

	//Using malloc to allocate memory for img_data and storing the all the file data in it
	img_data=(unsigned char*)malloc(sizeof(unsigned char) * fileSize);
	if( fread(img_data, 1, fileSize, bmpfile ) != fileSize ){
	  printf( "I was unable to read the requested %d bytes.\n", fileSize );
	  return NULL;
	}	

	//Closing bmpfile stream
	fclose(bmpfile);

  	// TO HERE!  
  return img_data;  
}

void 
bmp_close( unsigned char **img_data )
{
  // REPLACE EVERYTHING FROM HERE

	//Releasing memory and setting img_data to NULL. Ready for reuse.
	if (*img_data!=NULL){
	free(*img_data);
	*img_data=NULL;
	}

  // TO HERE!  
}

unsigned char***  
bmp_scale( unsigned char*** pixel_array, unsigned char* header_data, unsigned int header_size,
           unsigned int* width, unsigned int* height, unsigned int num_colors,
           float scale )
{
  unsigned char*** new_pixel_array = NULL; 
  // REPLACE EVERYTHING FROM HERE
	
	//Getting bpp and data size from header data, initializing padding and setting img_data to NULL
	unsigned int bits_per_pixel=*(unsigned short*)(header_data+28);
	unsigned int data_size=*(unsigned short*)(header_data+2);
	unsigned int padding;
	
	//Calculating new width and height and allocating memory for a new scaled pixel array + check for failure
	int new_width=(*width)*scale, new_height=(*height)*scale;
	new_pixel_array=(unsigned char***)malloc( sizeof(unsigned int**) * (new_height));
	if(new_pixel_array == NULL ){
		printf( "Error: bmp_to_3D_array failed to allocate memory for image of height %d.\n", (new_height) );
		return NULL;
	}
	//Modifying header data 
	*(unsigned short*)(header_data+2) = new_width*new_height*num_colors+header_size;
	*(unsigned short*)(header_data+18) = new_width;
	*(unsigned short*)(header_data+22) = new_height;

	//Calculating the num of colors per pixel (ARGB vs RGB)
	num_colors=bits_per_pixel/8;
	
	//Code copied from A3_provided_functions.c that malloc's memory in the 3D array
	for( int row=0; row<new_height; row++ ){
		new_pixel_array[row] = (unsigned char**)malloc( sizeof(unsigned char*) * (new_width) );
		for( int col=0; col<new_width; col++ ){
			new_pixel_array[row][col] = (unsigned char*)malloc( sizeof(unsigned char) * (num_colors) );
    		}
	}

	/*
	*	Code copied from A3_provided.functions.c that sets every pixel at 
	*	[row][col] in the scaled image to the value at
	*	[row/scale][col/scale] in the old image.
	*/
	for( int row=0; row<new_height; row++ )
		for( int col=0; col<new_width; col++ )
			for( int color=0; color<num_colors; color++ ) {
				float y_float=row/scale, x_float=col/scale;
				unsigned int y=(unsigned int) y_float, x=(unsigned int) x_float; 
        		new_pixel_array[row][col][color] = pixel_array[y][x][color];
			}	

	/*	
	*	As requested by the PARAMS in A3_solutions.h we set the old 
	*	width and height to the new scaled versions (doesn't work without
	*	this for some reason...).
	*/
	*width=new_width;
	*height=new_height;

	//Releasing the memory and setting the old pixel array to NULL
	free(**pixel_array);
	**pixel_array=NULL;

  // TO HERE! 
  return new_pixel_array;
}         

int 
bmp_collage( char* background_image_filename,     char* foreground_image_filename, 
             char* output_collage_image_filename, int row_offset,                  
             int col_offset,                      float scale )
{
  // REPLACE EVERYTHING FROM HERE

	//Code copied from A3_provided_functions.c. We call bmp_to_3D_array on the background image
	unsigned char*   header_data;
	unsigned int     header_size, width, height, num_colors;
	unsigned char*** background_array = NULL;
	background_array=bmp_to_3D_array( background_image_filename, &header_data, 
										&header_size, &width, 
										&height, &num_colors   ); 
	if(background_array== NULL ){
		printf( "Error: bmp_to_3D_array failed for file %s.\n", background_image_filename);
		return -1;
	}


	//Code copied from A3_provided_functions.c. We call bmp_to_3D_array on the foreground image
	unsigned char*   header_data2;
	unsigned int     header_size2, width2, height2, num_colors2;
	unsigned char*** foreground_array = NULL;
	foreground_array=bmp_to_3D_array( foreground_image_filename, &header_data2, 
										&header_size2, &width2, 
										&height2, &num_colors2   ); 
	if(foreground_array== NULL ){
		printf( "Error: bmp_to_3D_array failed for file %s.\n", foreground_image_filename);
		return -1;
	}


	//We scale the foreground image and check if it was succesful
	unsigned char*** scaled_foreground_array= NULL;
	scaled_foreground_array = bmp_scale( foreground_array, header_data2, header_size2,
											&width2, &height2, num_colors2,  scale   );                                          
	if( scaled_foreground_array == NULL ){
			printf( "Error: Call to bmp_scale failed!\n" );
			return -1;
		}

	//Checking for the ASSUMPTIONS named in A3_solutions.h.
	if ((num_colors!=4) || (num_colors2!=4) || (width<width2) || (height<height2) || (width<(col_offset+width2)) || (height<(row_offset+height2)) ) {
		return -1;
	}

	//Writing over background image
	for( int row=0; row<height2; row++ ) {
		for( int col=0; col<width2; col++ ) {
			for( int color=0; color<num_colors2; color++ ) {
				if (scaled_foreground_array[row][col][0]!=0){
					background_array[row+row_offset][col+col_offset][color]=scaled_foreground_array[row][col][color];
				}
			}	
		}
	}

	//Code copied from A3_provided_functions.c. Creates a bmp file from a 3D array. Don't know how this works to be honest
	FILE *out_fp = fopen( output_collage_image_filename, "wb" );
	if( out_fp == NULL ){
		printf( "Error: bmp_from_3D_array could not open file %s for writing.\n", output_collage_image_filename );
		return -1;
	}
	int row_size = ((32*width+31)/32)*4;
	int raw_pixel_size = header_size + height*row_size;
	unsigned char* raw_pixels = (unsigned char*)malloc( raw_pixel_size );
	for( int row=0; row<height; row++ ){
		for( int col=0; col<width; col++ ){
			for( int color=0; color<num_colors; color++ ){
				raw_pixels[row*row_size + col*num_colors + color] = background_array[height-row-1][col][color];
			}
		}
	}
	fwrite( header_data, 1, header_size, out_fp );
	fwrite( raw_pixels, 1,  raw_pixel_size, out_fp );
	fclose( out_fp );
	
  // TO HERE! 
  return 0;
}              

