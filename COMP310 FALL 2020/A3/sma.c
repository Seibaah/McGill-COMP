/*
 * =====================================================================================
 *
 *	Filename:  		sma.c
 *
 *  Description:	Base code for Assignment 3 for ECSE-427 / COMP-310
 *
 *  Version:  		1.0
 *  Created:  		6/11/2020 9:30:00 AM
 *  Revised:  		-
 *  Compiler:  		gcc
 *
 *  Author:  		Mohammad Mushfiqur Rahman
 *      
 *  Instructions:   Please address all the "TODO"s in the code below and modify 
 * 					them accordingly. Feel free to modify the "PRIVATE" functions.
 * 					Don't modify the "PUBLIC" functions (except the TODO part), unless
 * 					you find a bug! Refer to the Assignment Handout for further info.
 * =====================================================================================
 */

/* Includes */
#include "sma.h"	// Please add any libraries you plan to use inside this file

/* Definitions*/
#define MAX_TOP_FREE (128 * 1024)								// Max top free block size = 128 Kbytes
#define FREE_BLOCK_LIST_POINTER_SIZE 2 * sizeof(long)				// Size of the Header in a free memory block
#define FREE_BLOCK_BOUNDARY_TAG_SIZE 2 * sizeof(int)			// Size of the Footer in a free memory block

typedef enum
{
	WORST,
	NEXT
} Policy;

char* sma_malloc_error;
void *freeListHead = NULL;
void *freeListTail = NULL;
unsigned long totalAllocatedSize = 0;
unsigned long totalFreeSize = 0;
Policy currentPolicy = WORST;
//Add any global variables here
void *heap_start=NULL, *iterator=NULL;
int used=1, notUsed=0, nomergecount=0, mergeabovecount=0, mergebelowcount=0, iter=0, freeListCount=0, iter2=0;
/*
 * =====================================================================================
 *	Public Functions for SMA
 * =====================================================================================
 */

//TODO: Check stats in merge above and below

/*
 *	Funcation Name: sma_malloc
 *	Input type:		int
 * 	Output type:	void*
 * 	Description:	Allocates a memory block of input size from the heap, and returns a 
 * 					pointer pointing to it. Returns NULL if failed and sets a global error.
 */

void *sma_malloc(int size)
{
	void *pMemory = NULL;

	// Checks if the free list is empty
	if (freeListHead == NULL)
	{
		// Allocate memory by increasing the Program Break
		pMemory = allocate_pBrk(size);
	}
	// If free list is not empty
	else
	{
		// Allocate memory from the free memory list
		pMemory = allocate_freeList(size);

		// If a valid memory could NOT be allocated from the free memory list
		if (pMemory == (void *)-2)
		{
			// Allocate memory by increasing the Program Break
			pMemory = allocate_pBrk(size);
		}
	}

	// Validates memory allocation
	if (pMemory < 0 || pMemory == NULL)
	{
		sma_malloc_error = "Error: Memory allocation failed!";
		return NULL;
	}

	// Updates SMA Info
	totalAllocatedSize += size;

	return pMemory;
}

/*
 *	Funcation Name: sma_free
 *	Input type:		void*
 * 	Output type:	void
 * 	Description:	Deallocates the memory block pointed by the input pointer
 */
void sma_free(void *ptr)
{
	//	Checks if the ptr is NULL
	if (ptr == NULL)
	{
		puts("Error: Attempting to free NULL!\n");
	}
	//	Checks if the ptr is beyond Program Break
	else if (ptr > sbrk(0))
	{
		puts("Error: Attempting to free unallocated space!\n");
	}
	else
	{
		//	Adds the block to the free memory list
		add_block_freeList(ptr);
	}
}

/*
 *	Funcation Name: sma_mallopt
 *	Input type:		int
 * 	Output type:	void
 * 	Description:	Specifies the memory allocation policy
 */
void sma_mallopt(int policy)
{
	// Assigns the appropriate Policy
	if (policy == 1)
	{
		currentPolicy = WORST;
	}
	else if (policy == 2)
	{
		currentPolicy = NEXT;
	}
}

/*
 *	Funcation Name: sma_mallinfo
 *	Input type:		void
 * 	Output type:	void
 * 	Description:	Prints statistics about the memory allocation by SMA so far.
 */
void sma_mallinfo()
{
	//	Finds the largest Contiguous Free Space (should be the largest free block)
	int largestFreeBlock = get_largest_freeBlock();
	char str[60];

	//	Prints the SMA Stats
	sprintf(str, "Total number of bytes allocated: %lu", totalAllocatedSize);
	puts(str);
	sprintf(str, "Total free space: %lu", totalFreeSize);
	puts(str);
	sprintf(str, "Size of largest contigious free space (in bytes): %d", largestFreeBlock);
	puts(str);
}

/*
 *	Funcation Name: sma_realloc
 *	Input type:		void*, int
 * 	Output type:	void*
 * 	Description:	Reallocates memory pointed to by the input pointer by resizing the
 * 					memory block according to the input size.
 */
void *sma_realloc(void *ptr, int size)
{
	// TODO: 	Should be similar to sma_malloc, except you need to check if the pointer address
	//			had been previously allocated.
	// Hint:	Check if you need to expand or contract the memory. If new size is smaller, then
	//			chop off the current allocated memory and add to the free list. If new size is bigger
	//			then check if there is sufficient adjacent free space to expand, otherwise find a new block
	//			like sma_malloc

	void *dummy=ptr, *copy_ptr, *newBlock;
	int *t=dummy;
	int block_size=*t;	//usable space if the block
	t--;
	int block_status=*t, excessSize=0;

	if (block_status!=1 || ptr==NULL){
		puts("Error. Pointer doesn't point to an allocated block!");
		return ptr;
	}

	//min usable size for a block is 48
	if (size<48){
		size=48;
	}

	dummy=(char*)dummy + block_size + FREE_BLOCK_BOUNDARY_TAG_SIZE;	//move dummy to the top of the block
	copy_ptr=(char*)dummy-FREE_BLOCK_BOUNDARY_TAG_SIZE;

	if(size<block_size){	//verify later
		//need to chop block
		excessSize=block_size-size;
		allocate_block(dummy, size, excessSize, 0);

		dummy=(char*)dummy-block_size+FREE_BLOCK_BOUNDARY_TAG_SIZE;
		newBlock=dummy;
	}
	else if (size>block_size){
		newBlock=sma_malloc(size);
	}
	else {
		puts("Error. Resizing to same size!");
		return ptr;
	}

	//copy the old block data
	memcpy((char*)newBlock+block_size, (char*)ptr, block_size);

	//free the old block
	sma_free(ptr);

	return newBlock;
	
}

/*
 * =====================================================================================
 *	Private Functions for SMA
 * =====================================================================================
 */

//TODO: Implement all your helper functions here (You need to declare them in helper.h)

/*
 *	Funcation Name: allocate_pBrk
 *	Input type:		int
 * 	Output type:	void*
 * 	Description:	Allocates memory by increasing the Program Break
 */
void *allocate_pBrk(int size)
{
	void *newBlock;
	int excessSize;

	//save the heap start location
	if(heap_start==NULL){
		heap_start=sbrk(0);
	}

	//calculates the new pbrk value
	//partition is free block + used block
	//TODO min size = 32 and maybe pad to a multiple of 16
	int partition = size + 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE + MAX_TOP_FREE;

	if (partition%16!=0){	//to preserve memory alignment
		partition+=partition%16;
	}

	excessSize=MAX_TOP_FREE;
	
	newBlock = sbrk(partition);
	newBlock = sbrk(0);
	
	//	Allocates the Memory Block
	allocate_block(newBlock, size, excessSize, 0);

	newBlock=(char*)newBlock - excessSize - size - FREE_BLOCK_BOUNDARY_TAG_SIZE;	//point the block to the bottom

	return newBlock;
}

/*
 *	Funcation Name: allocate_freeList
 *	Input type:		int
 * 	Output type:	void*
 * 	Description:	Allocates memory from the free memory list
 */
void *allocate_freeList(int size)
{
	void *pMemory;

	if (currentPolicy == WORST)
	{
		// Allocates memory using Worst Fit Policy
		pMemory = allocate_worst_fit(size);
	}
	else if (currentPolicy == NEXT)
	{
		// Allocates memory using Next Fit Policy
		pMemory = allocate_next_fit(size);
	}
	else
	{
		return NULL;
	}

	return pMemory;
}

/*
 *	Funcation Name: allocate_worst_fit
 *	Input type:		int
 * 	Output type:	void*
 * 	Description:	Allocates memory using Worst Fit from the free memory list
 */
void *allocate_worst_fit(int size)
{
	void *worstBlock, *block=freeListHead;
	int excessSize, block_size, worst_block_size=size;
	int blockFound = 0;
	long *l, x;
	
	iter=0;
	while(1){
		block_size=get_blockSize(block);

		if (block_size>=worst_block_size){
			worst_block_size=block_size;
			worstBlock=block;
			blockFound=1;
		}

		if (block==freeListTail){
			break;
		}
		else{
			l=block;
			l--; l--; l--; l--;
			block = (void*)*l;	//move dummy to the written address
		}
		iter++;
	}

	excessSize=worst_block_size-size;

	//	Checks if appropriate found is found.
	if (blockFound)
	{
		//	Allocates the Memory Block
		allocate_block(worstBlock, size, excessSize, 1);
		worstBlock=(char*)worstBlock-excessSize-size-FREE_BLOCK_BOUNDARY_TAG_SIZE;
		//need to point to the bottom of the block
	}
	else
	{
		//	Assigns invalid valid
		worstBlock = (void *)-2;
	}

	return worstBlock;
}

/*
 *	Funcation Name: allocate_next_fit
 *	Input type:		int
 * 	Output type:	void*
 * 	Description:	Allocates memory using Next Fit from the free memory list
 */
void *allocate_next_fit(int size)
{
	void *nextBlock, *dummy, *iterator_start;
	int excessSize;
	int blockFound = 0, block_size;
	long *l;
	iter2=0;
	
	if (freeListHead==NULL){
		nextBlock = (void *)-2;
	}

	if (iterator==NULL){
		iterator=freeListHead;
	}

	iterator_start=iterator;	//save the starting location of the search

	while(1){
		block_size=get_blockSize(iterator);

		if (block_size>=size){
			nextBlock=iterator;
			excessSize = block_size - size;
			blockFound=1;

			l=iterator;
			l--; l--; l--; l--;
			iterator=(void*)*l;	//go to next block

			break;
		}

		l=iterator;
		l--; l--; l--; l--;
		iterator=(void*)*l;	//go to next block

		
		//no block can fit the request
		if(iterator==iterator_start || iterator==freeListTail){
			break;
		}

		//test
		iter2++;
	}

	//	Checks if appropriate found is found.
	if (blockFound)
	{
		//	Allocates the Memory Block
		allocate_block(nextBlock, size, excessSize, 1);
		nextBlock=(char*)nextBlock -size -excessSize -FREE_BLOCK_BOUNDARY_TAG_SIZE;
	}
	else
	{
		//	Assigns invalid valid
		nextBlock = (void *)-2;
	}

	return nextBlock;
}

/*
 *	Funcation Name: allocate_block
 *	Input type:		void*, int, int, int
 * 	Output type:	void
 * 	Description:	Allocates memory using Next Fit from the free memory list
 */
void allocate_block(void *newBlock, int size, int excessSize, int fromFreeList)
{
	void *excessFreeBlock;
	int addFreeBlock;
	int *temp;

	// 	Checks if excess free size is big enough to be added to the free memory list
	//	Helps to reduce external fragmentation
	addFreeBlock = excessSize > 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE + 2 * FREE_BLOCK_LIST_POINTER_SIZE;

	//	If excess free size is big enough
	if (addFreeBlock)
	{
		//update excess size to usable free size
		excessSize -= 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;

		excessFreeBlock=newBlock;
		newBlock=(char*)newBlock - 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE - excessSize;

		//Setting the allocated block
		void *dummy = newBlock;
		//upper tag: Allocated int | Size of block
		temp = (int*)dummy;
		*temp=used;
		dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy;
		*temp=size;
		//lower tag: Size of block | Allocated int
		dummy = (char*)newBlock - FREE_BLOCK_BOUNDARY_TAG_SIZE - size;
		temp = (int*)dummy;
		*temp=size;
		dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy;
		*temp=used;

		//Setting the free block
		void *dummy2 = excessFreeBlock;
		//upper tag: Allocated int | Size of block
		temp = (int*)dummy2;
		*temp=notUsed;
		dummy2 = (char*)dummy2 - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy2;
		*temp=excessSize;
		//lower tag: Size of block | Allocated int
		dummy2 = (char*)excessFreeBlock - FREE_BLOCK_BOUNDARY_TAG_SIZE - excessSize;
		temp = (int*)dummy2;
		*temp=excessSize;
		dummy2 = (char*)dummy2 - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy2;
		*temp=notUsed;

		//	Checks if the new block was allocated from the free memory list
		if (fromFreeList)
		{
			//	Removes new block and adds excess free block to the free list
			replace_block_freeList(newBlock, excessFreeBlock);
		}
		else
		{
			//	Adds excess free block to the free list
			excessFreeBlock=(char*)excessFreeBlock - excessSize - FREE_BLOCK_BOUNDARY_TAG_SIZE;
			add_block_freeList(excessFreeBlock);
		}
	}
	//	Otherwise add the excess memory to the new block
	else
	{
		//excess size not big enough to make a free block so pass it to the user
		size+=excessSize;
		
		//Fill the used block top boundary tag
		void *dummy = newBlock;
		temp = (int*)dummy;
		*temp = used;
		dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy;
		*temp = size;

		//Fill the bottom tag
		dummy = (char*)newBlock - size - FREE_BLOCK_BOUNDARY_TAG_SIZE;
		temp = (int*)dummy;
		*temp = size;
		dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy;
		*temp = used;

		//	Checks if the new block was allocated from the free memory list
		if (fromFreeList)
		{
			//	Removes that block from the free Add excessSize to size and assign ilist
			remove_block_freeList(newBlock);
			
		}
	}
}

/*
 *	Funcation Name: replace_block_freeList
 *	Input type:		void*, void*
 * 	Output type:	void
 * 	Description:	Replaces old block with the new block in the free list
 */
void replace_block_freeList(void *oldBlock, void *newBlock)
{
	//Not used by my implementation, its work is done elsewhere
	//	Updates SMA info
	totalAllocatedSize += (get_blockSize(oldBlock) - get_blockSize(newBlock));
}

/*
 *	Funcation Name: add_block_freeList
 *	Input type:		void*
 * 	Output type:	void
 * 	Description:	Adds a memory block from the the free memory list
 */
void add_block_freeList(void *block)
{
	int *temp;
	int b_cur_size, b_above_size, b_below_size, b_cur_status=0, b_above_status=-1, b_below_status=-1;
	void *b_above, *b_cur, *b_below, *pbrk, *dummy=block;
	pbrk=sbrk(0);

	//get the current free block size, to be added to the free list
	dummy = (char*)dummy;
	temp = (int*)dummy;
	b_cur_size=*temp;

	block=(char*)block + b_cur_size + FREE_BLOCK_BOUNDARY_TAG_SIZE;
	b_cur=block;

	if (pbrk>b_cur){
		//there is a block above
		//get status of the block above in memory
		dummy = (char*)b_cur + FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
		temp = (int*)dummy;
		b_above_status = *temp;

		if(b_above_status==0){
			//get the size if the block is free
			dummy = (char*)dummy + FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
			temp = (int*)dummy;
			b_above_size = *temp;

			//set the block above reference 
			dummy = (char*)b_cur + b_above_size + 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;
			b_above=dummy;
		}
	}
	dummy = (char*)b_cur - b_cur_size - 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;
	if (dummy>heap_start){
		//there is a block below in memory
		dummy = (char*)b_cur - b_cur_size - 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;
		temp = (int*)dummy;
		b_below_status = *temp;

		if(b_below_status==0){
			//get the size if the block is free
			dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
			temp = (int*)dummy;
			b_below_size = *temp;

			//set the block below reference
			dummy = (char*)dummy + FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
			b_below=dummy;
		}
	}
	if ((b_above_status == 1 || b_above_status == -1) &&
		b_below_status ==0){
			//no_merge(b_cur);
			merge_below(b_cur, b_cur_size, b_below, b_below_size);	//call merge below*/
	}
	else if ((b_below_status == 1 || b_below_status == -1) &&
		b_above_status ==0){
			merge_above(b_cur, b_cur_size, b_above, b_above_size);	//call merge above
			b_cur=b_above;
	} 
	else if ((b_below_status == 1 || b_below_status == -1) &&
		(b_above_status == 1 || b_above_status == -1)){
			no_merge(b_cur);	//no merge
	} 
	else {
		no_merge(b_cur);
		merge_above(b_cur, b_cur_size, b_above, b_above_size);
		b_cur=b_above;
		b_cur_size=get_blockSize(b_cur);
		detach_from_freeList(b_below, b_below_size);
		merge_above(b_below, b_below_size, b_cur, b_cur_size);
	};

	block=b_cur;
	
	//	Updates SMA info
	totalAllocatedSize -= get_blockSize(block);
	totalFreeSize += get_blockSize(block);
	}

//add the block to the free list when no merge is required
void no_merge(void *b_cur){
	void *dummy=b_cur;
	int *t;

	//update top tag
	t = (int*)dummy;
	*t = notUsed;
	dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	t = (int*)dummy;
	int size = *t;
	//update lower tag
	dummy = (char*)b_cur - size - FREE_BLOCK_BOUNDARY_TAG_SIZE;
	t = (int*)dummy;
	*t = size;
	dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	t = (int*)dummy;
	*t = notUsed;
/*
	if (freeListHead==NULL){
		freeListHead=b_cur;
		freeListTail=b_cur;
	}


	//set the .next of the free list tail to the current block
	long x = (long)b_cur;	//x stores a base 10 address of b_cur
	long *l = freeListTail;
	l--; l--; l--; l--;	//move the next long pointer tag to the next field
	*l = x; 

	//set the .prev of the current block to the free list tail 
	x = (long)freeListTail;	//x stores a base 10 address of freeListTail
	l = b_cur;
	l--; l--;
	*l = x;

	//set the .next of the current block to itself 
	x = (long)b_cur;	//x stores a base 10 address of freeListTail
	l--; l--;
	*l = x;
	

	//set the tail to the new block to be added to the free list
	freeListTail=b_cur;*/

	//insert in the free list
	sorted_insert(b_cur, (long)b_cur);

	//test
	nomergecount++;
	freeListCount++;
}

//merges the new free list block to the one above
void merge_above(void *b_cur, int b_cur_size, void *b_above, int b_above_size){
	void *dummy;
	int *temp;

	//size of the resultant block
	int merged_size = b_cur_size + b_above_size + 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;

	//update the top boundary tag of the resultant block
	dummy=b_above;
	temp=(int*)dummy;
	*temp=notUsed;	//write used tag
	dummy=(char*)b_above - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp=(int*)dummy;
	*temp=merged_size;	//write updated size

	//list pointer tags stay at the same location and stay the same

	//update bottom tag
	dummy=b_above;
	dummy=(char*)b_above - FREE_BLOCK_BOUNDARY_TAG_SIZE - merged_size;
	temp=(int*)dummy;
	*temp=merged_size;
	dummy=(char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp=(int*)dummy;
	*temp=notUsed;

	//block below reference is not valid anymore as block below doesn't exist anymore
	b_cur=b_above;
	b_above=NULL;

	//test
	mergeabovecount++;
}

//merges the new free list block to the one above
void merge_below(void *b_cur, int b_cur_size, void *b_below, int b_below_size){
	void *dummy;
	int *temp;
	long next, prev, prev_next;
	long *l;

	//size of the resultant block
	int merged_size = b_cur_size + b_below_size + 2 * FREE_BLOCK_BOUNDARY_TAG_SIZE;

	//update the top boundary tag of the resultant block
	dummy=b_cur;
	temp=(int*)dummy;
	*temp=notUsed;	//write usage tag
	dummy=(char*)b_cur - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp=(int*)dummy;
	*temp=merged_size;	//write updated size

	//copy list pointer
	l=b_below;
	l--; l--;	//move l to prev field
	prev=*l;	//copy prev address
	l--; l--;	//move l to next field
	next=*l;	//copy next address

	if (b_below==freeListHead && b_below==freeListTail){	//there is only 1 elem if the free list, merging into head/tail
		l=b_cur;
		l--; l--;
		*l=(long)b_cur;
		l--; l--;
		*l=(long)b_cur;
		freeListHead=b_cur;
		freeListTail=b_cur;
	}
	else if (b_below==freeListHead){	//there are at least 2 elems in the free list, we are merging into the head
		l=b_cur;
		l--; l--;
		*l=(long)b_cur;
		l--; l--;
		*l=next;

		freeListHead=b_cur;

		//update .next.prev ref to b_cur
		dummy = (void*)*l;
		l=dummy;
		l--; l--;
		*l=(long)b_cur;
	}
	else if (b_below==freeListTail){	//there are at least 2 elems in the free list, we are merging into the tail
		l=b_cur;
		l--; l--;
		*l=prev;
		l--; l--;
		*l=(long)b_cur;

		freeListTail=b_cur;

		//update .prev.next ref to b_cur
		l++; l++;
		dummy = (void*)*l;
		l=dummy;
		l--; l--; l--; l--;
		*l=(long)b_cur;
	}
	else {		//we are merging in a block not at the extremes of the list
		l=b_cur;
		l--; l--;
		*l=prev;
		l--; l--;
		*l=next;

		//update surrounding block in the list
		l=b_cur;
		l--; l--;
		dummy=(void*)*l;
		l=dummy; l--; l--; l--; l--;
		*l=(long)b_cur;

		l=b_cur;
		l--; l--; l--; l--;
		dummy=(void*)*l;
		l=dummy; l--; l--;
		*l=(long)b_cur;
	}

	//update bottom tag
	dummy=b_cur;
	dummy=(char*)b_cur - FREE_BLOCK_BOUNDARY_TAG_SIZE - merged_size;
	temp=(int*)dummy;
	*temp=merged_size;	//write updated size
	dummy=(char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp=(int*)dummy;
	*temp=notUsed;	//write usage tag

	//block below reference is not valid anymore as block below doesn't exist anymore
	b_below=NULL;

	//test
	mergebelowcount++;
}

//handles detaching a block from the free list
void detach_from_freeList(void *block, int size){
	void *dummy, *dummy2;
	long *l, *l2;
	long x, prev, next;
	if (block==freeListHead && block==freeListTail){
		freeListHead=NULL;
		freeListTail=NULL;
	}
	else if (block==freeListHead){
		l=block;
		l--; l--; l--; l--;	//move l to the .next field
		dummy = (void*)*l;	//move dummy to the written address
		l=dummy;			//l points to Head.next now
		l--; l--;			//move l to the .prev field
		x=(long)dummy;		//save the address of the new head in a long
		*l=x;				//by convention we point head.prev to itself

		freeListHead=dummy;	//update global head pointer
	}
	else if (block==freeListTail){
		l=block;
		l--; l--;			//move l to the .prev field
		dummy = (void*)*l;	//move dummy to the written address
		l=dummy;			//l points to Tail.prev now
		l--; l--; l--; l--;	//move l to the .next field
		x=(long)dummy;		//save the address of the new tail in a long
		*l=x;				//by convention we point tail.next to itself

		freeListTail=dummy;	//update global head pointer
	}
	else{
		l=block;
		l--; l--;			//move l to the .prev field
		dummy = (void*)*l;	//move dummy to the written address
		l=dummy;			//l points to block.prev now
		l--; l--; l--; l--;	//move l to the .next field
		prev=(long)dummy;	//save the block.prev addr in a long

		l2=block;
		l2--; l2--; l2--; l2--;	//move l2 to the .next field
		dummy2 = (void*)*l2;	//move dummy2 to the written address
		l2=dummy2;				//l2 points to block.next now
		l2--; l2--;				//move l2 to the .prev field
		next=(long)dummy2;      //save the block.next addr in a long

		//detach the block from the free list
		*l=next;
		*l2=prev;
	}

	//keep the next_fit iterator valid
	if (iterator==block){
		l=iterator;
		l--; l--; l--; l--;
		*l=next;
	}

	//test
	freeListCount--;
}

//insert a block in the free list while keeping the list sorted by ascending memory location
void sorted_insert(void *block, long addr){
	void *cur, *prev;	
	long cur_a;
	long *l, *l2;
	int done=0;

	if (freeListHead==NULL){	//we have an empty list
		freeListHead=block;
		freeListTail=block;

		//set .prev to current block
		long *l = block;
		l--; l--;
		*l = (long)block;

		//set the .next to current block
		l--; l--;	
		*l = (long)block; 

		done=1;
	}
	else{
		cur=freeListHead;
		
		do{
			cur_a=(long)cur;	//get the bloc addr in memory

			if (freeListHead==freeListTail){	//list only has 1 block
				if (cur_a>addr){
					//insert at head on a list of 1
					freeListHead=block;

					l=block;
					l--; l--;	//go to block.prev
					*l=(long)block;	//point the new head to itself

					l--; l--;	//go to block.next
					*l=(long)freeListTail;	//point it to the tail

					l=freeListTail;
					l--; l--;
					*l=(long)freeListHead;

					done=1;
					break;
				}
				else{
					//insert at tail on a list of 1
					freeListTail=block;

					l=block;
					l--; l--;	//go to block.prev
					*l=(long)freeListHead;	//point the new head to itself

					l--; l--;	//go to block.next
					*l=(long)block;	//point it to the tail

					l=freeListHead;
					l--; l--; l--; l--;
					*l=(long)freeListTail;

					done=1;
					break;
				}
			}
			else{	//list has at least 2 elements
				if (cur_a>addr){
					if  (cur==freeListHead){
						break;
					}
					//insert between cur and cur.prev

					//get a ref to cur.prev
					l=cur;
					l--; l--;
					prev=(void*)*l;	//prev points now to cur.prev

					//make prev.next point to block
					l2=prev;
					l2--; l2--; l2--; l2--;
					*l2=addr;

					//make block.prev point to prev
					l2=block;
					l2--; l2--;
					*l2=(long)prev;

					//make block.next point to cur
					l2--; l2--;
					*l2=(long)cur;

					//make cur.prev point to block
					*l=(long)block;

					done=1;
					break;
				}
				else{
					//go to cur.next
					l=cur;
					l--; l--; l--; l--;
					cur=(void*)*l;	//go to next element in list
				}
			}
		} while(cur!=freeListTail);

		if (done==0){	//need to insert on a list with more than 1 element
			if(cur==freeListHead){
				//insert at head on a list of 1
				freeListHead=block;

				l=block;
				l--; l--;	//go to block.prev
				*l=(long)block;	//point the new head to itself

				l--; l--;	//go to block.next
				*l=(long)cur;	//point it to the tail

				l=cur;
				l--; l--;
				*l=(long)block;	//old head points prev ptr to new head
				done=1;
			}
			else if(cur==freeListTail){
				//insert at tail on a list with more than 1 element
				freeListTail=block;

				l=block;
				l--; l--;	//go to block.prev
				*l=(long)cur;	//point the new tail to the old tail

				l--; l--;	//go to block.next
				*l=(long)cur;	//point the new tail to itself

				l=cur;
				l--; l--; l--; l--;
				*l=(long)block;	//old tail points next ptr to new tail
				done=1;
			}
		}

	}
}

//deprecated in favour of using gdb
void prints(int n){
	char buf[60];
	sprintf(buf, "%d\n", n);
	puts(buf);
}

/*
 *	Funcation Name: remove_block_freeList
 *	Input type:		void*
 * 	Output type:	void
 * 	Description:	Removes a memory block from the the free memory list
 */
void remove_block_freeList(void *block)
{
	void *dummy=block;
	int *temp;
	dummy = (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp = (int*)dummy;
	int size=*temp;

	detach_from_freeList(block, size);

	//	Updates SMA info
	totalAllocatedSize += get_blockSize(block);
	totalFreeSize -= get_blockSize(block);
}

/*
 *	Funcation Name: get_blockSize
 *	Input type:		void*
 * 	Output type:	int
 * 	Description:	Extracts the Block Size
 */
int get_blockSize(void *ptr)
{	
	void *dummy=ptr;
	int *temp;
	dummy= (char*)dummy - FREE_BLOCK_BOUNDARY_TAG_SIZE/2;
	temp=(int*) dummy;
	int block_size=*temp;
	return block_size;
}

/*
 *	Funcation Name: get_largest_freeBlock
 *	Input type:		void
 * 	Output type:	int
 * 	Description:	Extracts the largest Block Size
 */
int get_largest_freeBlock()
{
	int largestBlockSize=0, s;
	void *block=freeListHead;
	int *t;
	long *l;

	//iterate through the list and return the biggest size
	do {
		if (block==NULL){
			break;	//no block on the free list
		}

		t=block;
		t--;
		s=*t;

		if(s>largestBlockSize){
			largestBlockSize=s;
		}

		l=block;
		l--; l--; l--; l--;
		block=(void*)*l;

	} while(block!=freeListTail);

	return largestBlockSize;
}
