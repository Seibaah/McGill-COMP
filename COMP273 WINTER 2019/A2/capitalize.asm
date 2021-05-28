# This program illustrates an exercise of capitalizing a string.
# The test string is hardcoded. The program should capitalize the input string
# Comment your work

	.data	
		prompt: 	.asciiz "Enter a string:\n"
		in_space:	.space 1024
		out_space:	.space 1024


	.text
	.globl main
	
main:	li $v0, 4              #Print promt
    	la $a0, prompt         
    	syscall  
    	
    	li $v0, 8              #Read user's string
    	la $a0, in_space   	
    	li $a1, 1024           
    	syscall 
    	
    	la $s1, out_space	#Loading buffers
    	la $s0, in_space
	
ogstr:	li $v0, 4		#Print the original string
	la $a0, in_space
	syscall
	 
loop:	lb $t2, 0($s0)		#Getting the 1st byte of the original string
	beqz $t2, print		#If it's = 0, this is the end of the string
	
test1:	li $t1, 'a'		#Getting 'a' to compare char value
	bge $t2, $t1, test2	#If the value is bigger or equal than 'a'
	j write			#Else capitalization doesn't apply
	
test2:	li $t1, 'z'		#Getting 'z' to compare char value
	ble $t2, $t1, upper	#If the byte is a lowercase, go to upper label
	j write			
	
next:	addi $s0, $s0, 1	#Go to next char in the original string
	j loop
	
write: 	sb $t2, 0($s1)		#Store the unconverted byte in the string
	addi $s1, $s1, 1	#Move the new string pointer
	j next
	
upper:	subi $t2, $t2, 32	#Converting the byte to it's lowercase version by subtracting 32 (see ascii table)
	sb $t2, 0($s1)		#Store the converted byte in the string
	addi $s1, $s1, 1	#Move the second string pointer
	j next
	
print:	li $v0, 4		#Loop ended, print the modified string
	la $a0, out_space
	syscall
	
exit: nop
	

