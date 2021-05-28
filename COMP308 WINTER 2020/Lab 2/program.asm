.model small
.stack 100h
.data
	sPrompt db "Enter a number: $"
	sHello db 10,13,"Hello World!$"
	.code
	start:
	; initialize data segment
	mov ax, @data
	mov ds, ax
	; print prompt text
	mov ah, 9
	mov dx, OFFSET sPrompt
	int 21h
	; input number (will be stored as an ASCII character in AL)
	mov ah, 1
	int 21h
	; set counter register to input number
	; (subtract '0' from input ASCII character to get a number)
	sub al, '0'
	xor cx, cx
	mov cl, al
	; label that will be used for our loop
printMore:
	; save our counter
	push cx
	; print our string
	mov ah, 9
	mov dx, OFFSET sHello
	int 21h
	; restore our counter
	pop cx
	; decrement counter and jump back if it's not 0
	dec cx
	jnz short printMore
	; terminate program
	mov ax, 4c00h
	int 21h
END start