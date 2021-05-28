	.data
		prompt:		.asciiz "Enter a number: "
		positive:	.asciiz " is congruent.\n"
		nothing:	.asciiz "No congruences were found.\n"
		error:		.asciiz "Second input number can't be 0. Please input valid numbers.\n"
	.text
	.globl main
pre:		j input
		add $s3, $0, $0		#Initializing a counter to keep track of how many congruences we find

main:		jal algo		#algo is the "method that does the heavy lifting
		j post			
		
algo:		#This is program uses the excludent method to find the congruences. It relies on 
		#the fact that finding the congruences is equivalent to solving "c^2 = a % b"; for
		#each value of c. We can observe too that according to this b can never be 0. 
		mult $s2, $s2		#c^2 = LO
		mflo $t0		#LO = t0
		sub $t1, $t0, $s0	#t0 - a = t1
		rem $t2, $t1, $s1	#t1 % s1 = t2
		sne $t3, $t2, $0	
		bne $t3, $0, skip 	#If there is a remainder, this value of c isn't a congruence of a % b, go to skip
		addi $s3, $s3, 1	#Else increase counter
		
		li $v0, 1		#If c holds for the congruence, print c
		move $a0, $s2
		syscall	 
		li $v0, 4		#Printing the positive message
		la $a0, positive
		syscall
		
skip:		addi $s2, $s2, -1	#c--	
		bgt  $s2, $0, algo  	#Repeat the loop if c>0
		jr $ra			#Else go back to main

input:		#This label gets user input and checks for invalid entries.
		#If the user enters b=0, they have to re-input valid numbers.
		jal print		
		li $v0, 5		
		syscall
		move $s0, $v0		#a is in s0

		jal print
		li $v0, 5	
		syscall
		move $s1, $v0		#b is in s1
		
		jal print
		li $v0, 5		
		syscall
		move $s2, $v0		#c is in s2
		
		beq $s1, $0, redo	#If b=0, prompt the user to re input new valid numbers
		j main			#Else keep executing
		
redo:		li $v0, 4		#Printing the redo message
		la $a0, error
		syscall
		j input

print:		li $v0, 4		#Printing the prompt message
		la $a0, prompt
		syscall
		jr $ra		
		
post: 		bne $s3, $0, end	#If congruences were found, go to end label
	
		li $v0, 4		#Else print this message
		la $a0, nothing
		syscall

end:		nop
