

# This MIPS program should count the occurence of a word in a text block using MMIO

.data
#any any data you need be after this line 
inst:	.asciiz "\nType a text. Once done, hit <Enter>.\nQuick note, this program can deal with special characters in the text.\n"
inst2:	.asciiz "\nWrite the token. Only write numbers and letter.\n"
inst3:	.asciiz "\nPress <e> to try again or <q> to quit\n"
inst4:	.asciiz "\nExecution ended\n"
nl:	.asciiz "\n"
res:	.asciiz	"\nMatches: "
txt:	.space 600 	#Buffer for a string of 600 char max
token:	.space 64	#Buffer for token	

	.text
	.globl main

main:	# all subroutines you create must come below "main"
	la $s1, txt	#Load text and token buffers
	la $s2, token
	
	la $a0, inst	#Print the initial instructions
	jal print
	la $t8, inst	#Print the initial instructions to MMIO
	jal PrintLoop
	

poll:	jal Read		# reading and writing using MMIO in an infinite loop
	add $a0,$v0,$zero	# Passing the read char as arg for Write subroutine
	jal store		# subroutine that stores the char in a string if it's not <Enter>
	jal Write		# Write the char to the monitor
	j poll			#Loop back to keep reading
	
Read:  	lui $t0, 0xffff 	#ffff0000
Loop1:	lw $t1, 0($t0) 		#control
	andi $t1,$t1,0x0001
	beq $t1,$zero,Loop1
	lw $v0, 4($t0) 		#data	
	jr $ra

Write: 	lui $t0, 0xffff 	#ffff0000
Loop2: 	lw $t1, 8($t0) 		#control
	andi $t1,$t1,0x0001
	beq $t1,$zero,Loop2
	sw $a0, 12($t0) 	#data	
	jr $ra
	
store: 	li $t0, 10		#Comparing the char to <Enter>
	beq $t0, $a0, end
	sb $a0, ($s1)		#If it's anything but <Enter> -> store in buffer
	addi $s1, $s1, 1
	jr $ra

end: 	addi $t9, $0, '\0'	#null terminating a string
	#addi $s1, $s1, 1
	sb $t9, ($s1)

	la $a0, txt		#print the string
	li $v0, 4
	syscall
	
	la $a0, inst2		#Print the next guidelines
	jal print
	la $t8, inst2		#Print the next guidelines to MMIO
	jal PrintLoop
	
pollT:	jal ReadT		# reading and writing using MMIO in an infinite loop
	add $a0,$v0,$zero	# Passing the read char as arg for Write subroutine
	jal storeT		# subroutine that stores the char in a string if it's not <Enter>
	jal WriteT		# Write the char to the monitor
	j pollT			#Loop back to keep reading
	
ReadT: 	lui $t0, 0xffff 	#ffff0000
Loop1T:	lw $t1, 0($t0) 		#control
	andi $t1,$t1,0x0001
	beq $t1,$zero,Loop1T
	lw $v0, 4($t0) 		#data	
	jr $ra

WriteT:	lui $t0, 0xffff 	#ffff0000
Loop2T: lw $t1, 8($t0) 		#control
	andi $t1,$t1,0x0001
	beq $t1,$zero,Loop2T
	sw $a0, 12($t0) 	#data	
	jr $ra
	
storeT:	li $t0, 10		#Comparing the char to <Enter>
	beq $t0, $a0, endT
	sb $a0, ($s2)		#If it's anything but <Enter> -> store in buffer
	addi $s2, $s2, 1
	jr $ra

endT: 	addi $t9, $0, '\0'	#null terminating the token
	#addi $s2, $s2, 1
	sb $t9, ($s2)

	la $a0, token		#print the string
	li $v0, 4
	syscall
	
	j search		#Go to the search subroutine
	
print:	li $v0, 4		#print string subroutine
	syscall
	jr $ra
	
	
search:		la $s1, txt		#Load text buffer
		move $s7, $0		#Create and initialize match counter
	
txtLoop:	lb $t0, ($s1)		#Get char from the text

isNull:		li $t1, '\0'		#Compare to see if we have reached the end of the string
		beq $t0, $t1, isEnd	

isAlpha:	li $t1, 48		#If current char is a number
		li $t2, 57 
		blt $t0, $t1, isElse
		bgt $t0, $t2, isUpper	
		j isWord	
		
isUpper:	li $t1, 65		#If current char is an uppercase letter
		li $t2, 90 
		blt $t0, $t1, isElse
		bgt $t0, $t2, isLower
		j isWord		
		
isLower:	li $t1, 97		#If current char is a lowercase letter
		li $t2, 122 
		blt $t0, $t1, isElse
		bgt $t0, $t2, isElse
		j isWord		
		
isElse:		addi $s1, $s1, 1	#If it's anything else we don't care
		j txtLoop		#Go to the next char
		
		
		
isWord:		la $s2, token		#Load token buffer to prepare for comparison	
		
wordLoop:	lb $t0, ($s1)		#Get char from the text
		lb $t1, ($s2)		#Load byte from the token	

compareWord:	bne $t0, $t1, check	#Compare the char in txt and the token
					#If the curr char isn't alphanumerical and we haven't jumped to <nextWord>
					#subroutine; then we have found a match in the txt
					#Following tests ensure this and will increase a match counter if necessary	
						
isNull2:	li $t1, '\0'		#Compare to see if we have reached the end of the string
		beq $t0, $t1, matchFound						
					
isAlpha2:	li $t2, 57 		#If current char is a number
		bgt $t0, $t2, isUpper2
		j nextChar

isUpper2:	li $t2, 90 		#If current char is an uppercase letter
		bgt $t0, $t2, isLower2
		j nextChar
		
isLower2:	li $t2, 122 		#If current char is a lowercase letter
		bgt $t0, $t2, matchFound
		j nextChar			
		
matchFound:	addi $s7, $s7, 1	#Increase match counter
		addi $s1, $s1, 1	#Incr txt ptr
		j txtLoop
		
		
nextChar:	addi $s1, $s1, 1	#Incr ptr for txt
		addi $s2, $s2, 1	#Incr ptr for word
		j wordLoop
		
		
		
nextWord:	addi $s1, $s1, 1	#Incr txt ptr to look for next word or txt end.
		lb $t0, ($s1)		#Unlike before this time we ignore the alphanumeric chars
					#We know the current word isn't a match so we look for
					#the first non alpha numeric char to reach the end of the current word.
					#Once found we can resume normal word search and comparison.
					
isNull3:	li $t1, 0		#Compare to see if we have reached the end of the string
		beq $t0, $t1, isEnd	

isAlpha3:	li $t1, 48		#If current char is a number go to nextWord and keep iterating
		li $t2, 57 		#If it's not a number it could be a letter or sth else. Branch accordingly
		blt $t0, $t1, isElse3
		bgt $t0, $t2, isUpper3	
		j nextWord
		
isUpper3:	li $t1, 65		#If current char is an uppercase letter go to nextWord and keep iterating
		li $t2, 90 		#If it's not a number it could be a letter or sth else. Branch accordingly
		blt $t0, $t1, isElse3
		bgt $t0, $t2, isLower3
		j nextWord				
		
isLower3:	li $t1, 97		#If current char is a lowercase letter go to nextWord and keep iterating
		li $t2, 122 		#If it's not then we have reached the end of the no match word. Go to isElse3
		blt $t0, $t1, isElse3
		bgt $t0, $t2, isElse3
		j nextWord		
		
isElse3:	addi $s1, $s1, 1	#If it's not an alphanumeric char we have skipped the no-match word.
		j isWord		#Go up top and restart looking for the next valid word
		
isEnd:		la $a0, res		#Print results message
		jal print
		la $t8, res		#Print the results message to MMIO
		jal PrintLoop
		
		li $v0, 1		#Print match counter
    		move $a0, $s7
    		syscall
    		
    		jal PrintChar
    		
redo:		j pollRedo		
    		
check:		#t0->bit txt
		#t1->bit token

isAlpha4:	li $t2, 48		#Evaluating if the txt bit is a number
		li $t3, 57 
		blt $t0, $t2, tokenEnd	#If char is less than 48 it can't be a number nor any letter. Check if token has ended.
		bgt $t0, $t3, isUpper4	#If char is greater than 57 it can't be a number, skip to next tests
		j nextWord		#If boves branches above don't get triggered curr char is a number. No match
		
isUpper4:	li $t2, 65		#Evaluating if the txt bit is an upper case letter
		li $t3, 90 
		blt $t0, $t2, tokenEnd	#If char is less than 65 it can't be a letter. If we are here we also know it's not a number. Check if token has ended.
		bgt $t0, $t3, isLower4	#If char is greater than 90 it can't be an upper case letter, skip to next tests
		j nextWord		#If boves branches above don't get triggered curr char is an uppercase letter. No match
		
isLower4:	li $t2, 97		#Evaluating if the txt bit is a lower case letter
		li $t3, 122 
		blt $t0, $t2, tokenEnd	#At this point if the char triggers this condition we know it not a number nor a letter. Check if token has ended.
		bgt $t0, $t3, tokenEnd	#If it triggers this condition instead we know it's not a letter nor a number	
		j nextWord

tokenEnd:	li $t2, '\0'
		beq $t2, $t1, matchFound	
		j txtLoop
		
pollRedo:	la $a0, inst3	#Print the Redo instruction
		jal print
		la $t8, inst3	#Print the Redo instructions to MMIO
		jal PrintLoop
redoRead:	jal ReadF		#Read from MMIO
		add $a0,$v0,$zero	#Pass input to $a0
		jal Repeat		#Check user input
	
#Standard read code for MMIO
ReadF:  lui $t0, 0xffff 	#ffff0000
LoopF:	lw $t1, 0($t0) 		#control
	andi $t1,$t1,0x0001
	beq $t1,$zero,LoopF
	lw $v0, 4($t0) 		#data	
	jr $ra		
		
Repeat:		#Checking if the user wants to repeat the search or quit

isQ:		li $t1, 113		#Compare to see if user wants to quit
		beq $a0, $t1, EndOfWorld

isE:		li $t1, 101		#Compare to see if user wants to repeat
		beq $a0, $t1, main
		
isElseFinal:	j redoRead		#Give the user a second chance at selecting a valid choice
		
EndOfWorld:	la $t8, inst4		#Print the END message to MMIO
		jal PrintLoop
		
		li $v0, 10		#End of program
		syscall

PrintLoop:	lb $a0, 0($t8)		#Prints a message to MMIO
WritePrint:	lui $t0, 0xffff 	#ffff0000
		sb $a0, 12($t0) 	#data
		addi $t8, $t8, 1
			
		li, $t2, '\0'		#When we reach the null termination char stop printing to MMIO
		beq, $t2, $a0, return	#else keep printing
		j PrintLoop	
		
return:		jr $ra	

PrintChar:	addi $a0, $a0, 48	#Prints an int converted to ASCII to MMIO
		lui $t0, 0xffff 	#ffff0000
		sb $a0, 12($t0) 	#data
		addi $t8, $t8, 1	
		jr $ra	
		
		
		
		
		
		
		
		
