

# This MIPS program should sort a set of numbers using the quicksort algorithm
# The program should use MMIO

.data
#any any data you need be after this line 
welcome:	.asciiz "Welcome to quicksort\n\0"
blankspace:	.asciiz " \0"
newLine:	.asciiz "\n\0"
sorted:		.asciiz "\nThe sorted array is: \0"
cleared:	.asciiz "\nThe array has been cleared.\n\0"
ended:		.asciiz "\nEnded execution\n\0"
array:		.word 10


	.text
	.globl main

main:	# all subroutines you create must come below "main"

pre:	la $t1, welcome		#Load welcome string, printed once at the beginning
	jal PrintLoop		#Go to print sub routine

load:	la $s0, array		#Load buffer for array in $s0

	move $s1, $0		#Initialize counter that keeps track of the number of elements in the array
	move $s2, $0		#Initialize counter that keeps track of the digits of the user input
	
exec:	jal Read0		#Read input from MMIO
	add $a0,$v0,$0		#Pass input to $a0
			
	jal assemble		#Subroutine that constructs the number
	jal Store		#Subroutine that stores the number in the array
	
	addi $t0, $0, 10	#Through our array elements counter we allow the user to enter a max of 10 elements
	blt $s1, $t0, exec	#If less than 10 elements have been entered the user can still type more numbers

#After filling the array or when the user entered <s>. Executes before sorting
mid:	jal printArray		#Prints array contents to the MARS console. For testing
	j preSort		#Jumps to label that precedes and prepares the recursive sorting
	
return:	jr $ra			#Standard return label used in many parts in the code

#This label executes if the user types <q>
end:	la $t1, ended		#Prints to MMIO the execution end message
	jal PrintLoop
	li $v0, 10		#Stop program
    	syscall

#Method that prints a fixed string to MMIO.
#String ptr is in $t1
PrintLoop:	lb $a0, 0($t1)		
Write: 		lui $t0, 0xffff 	#ffff0000
		sb $a0, 12($t0) 	#data
		addi $t1, $t1, 1
			
		li, $t2, '\0'		#When we reach the null termination char stop printing to MMIO
		beq, $t2, $a0, return	#else keep printing
		j PrintLoop
		
#Subroutine that reads from the MMIO display
Read0:  	lui $t0, 0xffff 	#reads input bit
Loop0:		lw $t1, 0($t0) 		#control bit
		andi $t1,$t1,0x0001
		beq $t1,$zero,Loop0
		lb $v0, 4($t0) 		#data
		
#Following labels test the input bit to decide wheter or not it should be echo'd to the MMIO display
isSpace0:	li $t1, ' '		#If entered char is a space -> write it to MMIO
		beq $v0, $t1, Write0	

isAlpha0:	li $t1, 48		#If current char is a number -> write it to MMIO
		li $t2, 57 
		blt $v0, $t1, return	#return to exec if the char should not be echo'd
		bgt $v0, $t2, return	

#Subroutine that writes to the MMIO display
Write0:		lui $t0, 0xffff 	#ffff0000
Loop00:		lw $t1, 8($t0) 		#control
		andi $t1,$t1,0x0001
		beq $t1,$zero,Loop0
		sw $v0, 12($t0) 	#data	
		jr $ra

printInt:	li $v0, 1		#Print number to MARS console - for testing
    		syscall
    		jr $ra

#Subroutine that constructs a number by getting it's digits. In the process special keys are evaluated and garbage entries are filtered out
assemble: 	li $t0, ' '
		beq $t0, $a0, finalize		#If current input bit is a <space> finalize the last number and prepare for next number	
		
		li $t0, 's'
		beq $t0, $a0, finalize2	#If current input bit is an <s> then finalize the last number and quicksort the actual array
		
		li $t0, 'c'
		beq $t0, $a0, ClearArr		#If current input bit is a <c> clear the the array and return to exec
		
		li $t0, 'q'
		beq $t0, $a0, end		#If current input bit is a <q> stop execution	
		
isAlpha2:	li $t1, 48		#If current bit is a number we can go to the subroutines that convert ascii to multi digit numbers
		li $t2, 57 
		blt $a0, $t1, exec	#Else we ignore and go back to poll for input
		bgt $a0, $t2, exec	
		
		
		beqz $s2, Digit1		#Current char could be the 1st or 2nd digit, go to the respective sub routine to make the number
		j Digit2

Digit1:		addi $a0, $a0, -48		#Creates single digit number
		move $s3, $a0			#move digit to saved $s3 register
		addi $s2, $s2, 1		#Increase digit counter
		jr $ra	
		
Digit2:		addi $a0, $a0, -48		#Creates the second digit in a number

		addi $t3, $0, 10		#Prepares the previously created digit from Digit1
		mul $s3, $s3, $t3
		
		add $s3, $a0, $s3		#Assemble the number together and put it in $s3
		jr $ra
	
finalize:	add $s2, $0, $0		#reset digit counter
		addi $s1, $s1, 1	#Increase number counter
		addi $t0, $0, 10	#compare to see if user has given have 10 numbers 
		blt $s1, $t0, exec
		j mid
		
#In case s is pressed save the last number and go to sort		
finalize2:	beqz $s2, f2Skip
		addi $s1, $s1, 1	#Increase number counter
f2Skip:		jal Store		#Store the last number
		add $s2, $0, $0		#reset digit counter
		j mid
											
#Subroutine that stores the number in tha array											
Store:		addi $t0, $s1, 0
		sll $t0, $t0, 2		#update index
    		
		sw $s3, array($t0)	#Store int in array buffer
		jr $ra	

#Subroutine that prints the array to the MARS console - for testing only
printArray:	add $t0, $0, $0		#Create array ptr
		add $t1, $0, $0		#Create loop counter
ArrLoop:	lw $a0, array($t0)	#Load array element at index $t0
		
		li $v0, 1		#Print number (for testing)
    		syscall
    		
    		li $v0, 11
    		addi $a0, $0, ' '
    		syscall
    		
    		addi $t0, $t0, 4	#Increase array ptr
    		addi $t1, $t1, 1	#Increase loop counter
    		beq $t1, $s1, stopPrint
    		j ArrLoop
    		
stopPrint:	jr $ra

#Subroutine that clears the contents of the array. Executes when <c> is pressed 
ClearArr:	add $t0, $0, $0		#Create array ptr
		add $t1, $0, $0		#Create loop counter
ClearLoop:	sw $0, array($t0)	#Load array element at index $t0
		
    		
    		addi $t0, $t0, 4	#Increase array ptr
    		addi $t1, $t1, 1	#Increase loop counter
    		beq $t1, $s1, ClearStop
    		j ClearLoop
    		
ClearStop:	j reset			 		

#Subroutines that reset current counters. Prepares a fresh start.	
reset:		jal printArray		#Print the array after clearing to MARS console - for testing
		move $s1, $0		#Reinitialize counter that keeps track of the number of elements in the array
		move $s2, $0		#Reinitialize counter that keeps track of the digits of the user input
		la $t1, cleared		#Print to MMIO the "array was cleared" message
		jal PrintLoop		#
		j exec			#Go back to execution start so the user can reneter a fresh array
	
#Subroutine that prepares quickSort
preSort:	#s0 -> ptr to array buffer
		addi $s5, $s1, -1	#hi -> number of elements -1 (s1 is # elem in array)
		add $s3, $0, $0		#Initialize low -> 0
		add $s4, $0, $0		#Initialize pivot -> 0
		add $t0, $0, $0		#$t0 will store a chosen pivot, local use only	

#Subroutine outside of the recursion calls.	
sortExec:	jal stackSave		#Jumps into the quicksort
		jal printArrayMMIO	#After recursion is done print the sorted array to MMIO
		la $t1, newLine		#Print a newline for formatting purposes in MMIO
		jal PrintLoop		#
		j exec			#Go back to execution start so the user can reenter a fresh array
				
########################################################################################################
#Recursion starts here
########################################################################################################
stackSave:	addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address

quickSort:	ble $s5, $s3, return	#if hi <= low 
		jal partition		#Jump to subroutine that partitons the array around the pivot. Returns the pivot.
		jal printArray4		#Print the array after each partition call
		
		#stack save
stackSave2:	addi $sp, $sp, -12
		sw $v1, 0($sp)		#store p_pos in the stack
		sw $s3, 4($sp)		#store low in the stack
		sw $s5, 8($sp)		#store hi in the stack
		#modify passed values
		addi $s5 ,$v1, -1	#pivot - 1 = hi after recursive call
		jal stackSave		#<-----------Recursive call
		#stack restore
		addi $sp, $sp, 4	#pop out old address
		lw $v1, 0($sp)		#store p_pos from the stack
		lw $s3, 4($sp)		#store low from the stack
		lw $s5, 8($sp)		#store hi from the stack		
		#modify passed values
		addi $s3 ,$v1, 1	#pivot +1 = lo after recursive call
		jal stackSave		#<-----------Recursive call
		#stack restore
		addi $sp, $sp, 16	#and release stack
		lw $ra, 0($sp)		#restore proper addr
		jr $ra

#Subroutine that puts the element smaller than a pivot to it's left and the bigger ones to it's right
partition:	add $t1, $0, $s3	#p_pos -> low
		sll $t2, $t1, 2		#Adjusting by 4x p_pos index
		lw $t0, array($t2)	#loading pivot
		
		addi $t4, $s3, 1	#Initialize loop index i=low+1
swapLoop:	sll $t5, $t4, 2		#adjusted loop index (4x)
		lw $t3, array($t5)	#Load the i-th element of the array in $t3
		bgt $t4, $s5, postLoop	#loop while i<=hi
		bge $t3, $t0, next	#if a[i] < pivot prepare swap

swapPrep:	#s0 ->  ptr to array
		#s3 -> low
		addi $t1, $t1, 1	#p_pos++
		
#Subroutine that swaps to elements in given positions in the array
swap:		#s0 -> ptr to array
		#t1 -> p_pos -> i 
		#t4 -> i -> j
		#t5 -> adjusted i -> adjusted j (4x)
		sll $t1, $t1, 2		#adjusting p_pos/local i
		
		lw $t6, array($t1)	#temp -> a[i]
		lw $t7, array($t5)	#temp2 -> a[j]
		sw $t7, array($t1)	#a[i] -> temp2
		sw $t6, array($t5)	#a[j] -> temp
		
		srl $t1, $t1, 2		#normalizing p_pos/local i	

next:		addi $t4, $t4, 1	#Else increase loop index
		j swapLoop		#And keep looping

postLoop:	#s0 -> ptr to array
		#t4 -> low -> i
		#t1 -> p_pos -> j
		sll $t4, $s3, 2		#adjust low (4x)
		sll $t1, $t1, 2		#adjust j (4x)
		
		lw  $t6, array($t4)
		lw $t7, array($t1)
		sw $t7, array($t4)
		sw $t6, array($t1)
		
		srl $t4, $t4, 2		#normalize low 
		srl $t1, $t1, 2		#normalize j
		move $v1, $t1		#Pas p_pos back in v1 (not v0 bc we have a lot of print syscalls)
		
		jr $ra			#return to jal	

#Printing the array after each partition step to the MARS console - for testing only
printArray4:	add $t0, $0, $0		#Create array ptr
		add $t1, $0, $0		#Create loop counter
		
		li $v0, 11       #Print newline 
		li $a0, '\n'    
		syscall
		
ArrLoop4:	lw $a0, array($t0)	#Load array element at index $t0
		
		li $v0, 1		#Print number (for testing)
    		syscall
    		
    		li $v0, 11
    		addi $a0, $0, ' '
    		syscall
    		
    		addi $t0, $t0, 4	#Increase array ptr
    		addi $t1, $t1, 1	#Increase loop counter
    		beq $t1, $s1, stopPrint4
    		j ArrLoop4
    		
stopPrint4:	jr $ra

#Subroutine that prints the sorted array to MMIO
printArrayMMIO:	la $t1, sorted

		addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address
		jal PrintLoop
		lw $ra, 0($sp)		#restore proper addr
		addi $sp, $sp, 4	#and release stack
		
		la $t1, newLine
		addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address
		jal PrintLoop
		lw $ra, 0($sp)		#restore proper addr
		addi $sp, $sp, 4	#and release stack
		
		add $t3, $0, $0		#Create array ptr
		add $t4, $0, $0		#Create loop counter for the array elements
		
ArrLoop5:	lw $a0, array($t3)	#Load array element at index $t3	

		li $t1, 10
		div $t0, $a0, $t1	#integer division on a[i], t0 = tens
		#print decenas here if not 0	
		beqz $t0, units		#don't print tens if = 0
		#print tens
		move $t8, $t0		#move num to t8
		addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address
		jal PrintChar
		lw $ra, 0($sp)		#restore proper addr
		addi $sp, $sp, 4	#and release stack

		
units:		mul $t1, $t1, $t0	#tens * 10
		sub $t2, $a0, $t1	#a[i]-(tens*10) -> units
		# print units here
		move $t8, $t2		#move num to t8
		addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address
		jal PrintChar
		lw $ra, 0($sp)		#restore proper addr
		addi $sp, $sp, 4	#and release stack

		
		#print space string here
		la $t8, blankspace
		addi $sp, $sp, -4	#making space on the stack (4 words)
		sw $ra, 0($sp)        # save return address
		jal PrintLoopAlt
		lw $ra, 0($sp)		#restore proper addr
		addi $sp, $sp, 4	#and release stack
		
pArrayMMIOLoop:	addi $t3, $t3, 4	#Increase array ptr
    		addi $t4, $t4, 1	#Increase loop counter
    		beq $t4, $s1, return
    		j ArrLoop5
    		
printInt2:	addi $a0, $a0, 48	#Prints an int converted to ASCII to MMIO
		lui $t0, 0xffff 	#ffff0000
		sb $a0, 12($t0) 	#data
		addi $t8, $t8, 1	
		jr $ra	

printString:	lb $a0, 0($t8)		#Prints a message to MMIO
WritePrint:	lui $t0, 0xffff 	#ffff0000
		sb $a0, 12($t0) 	#data
		addi $t8, $t8, 1
			
		li, $t2, '\0'		#When we reach the null termination char stop printing to MMIO
		beq, $t2, $a0, return	#else keep printing
		j WritePrint	

PrintChar:	addi $t8, $t8, 48	#Prints an int converted to ASCII to MMIO
		lui $t7, 0xffff 	#ffff0000
		sb $t8, 12($t7) 	#data
		jr $ra

#Only difference with original PrintLoops is that it uses $t8 as load from register 
PrintLoopAlt:	
LoopAlt:	lb $a0, 0($t8)		#Prints the welcome message to MMIO
WriteAlt: 	lui $t9, 0xffff 	#ffff0000
		sb $a0, 12($t9) 	#data
		addi $t8, $t8, 1
			
		li, $t9, '\0'		#When we reach the null termination char stop printing to MMIO
		beq, $t9, $a0, return	#else keep printing
		j LoopAlt











