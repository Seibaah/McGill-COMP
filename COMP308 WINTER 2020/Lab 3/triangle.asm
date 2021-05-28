.286
.model small
.stack 100h
.data 
    sPrompt db "Enter a number: $"
    xTriangle db 'X'
.code
start:
    ; initialize data segment
    mov ax, @data
    mov ds, ax
    
    ; print prompt message
    mov ah, 9
    mov dx, OFFSET sPrompt
    int 21h
    
    ;input number stored as ASCII, ah
    mov ah, 1
    int 21h

    ; set counter register to input number
    ; (subtract '0' from input ASCII character to get a number)
    sub al, '0'
    xor cx, cx
    mov cl, al
    
    ;cx is user input, bx is external counter
    mov bx, 0

    ;print a newline
    mov dl, 10
    mov ah, 02h
    int 21h
    mov dl, 13
    mov ah, 02h
    int 21h

    ;external print loop
    control:
    push cx     ;save user input on the stack
    push bx     ;save i-th counter for the loop
    cmp bx, cx  ;checking what loop iteration we are in
    je exit 
    xor cx, cx  ;resetting cx to make it a dynamic counter, like i in a loop
    add cx, 1   
    add cx, bx
    mov bx, 0   ;making the counter of x's to print per row 0 
    
    ;row print loop
    printMore:

    ; print our string
    mov dl, 'X'
    mov ah, 02h
    int 21h

    ; increment counter and jump back if it's not 0
    add bx, 1
    cmp bx,  cx
    jne printMore

    ;print newline between rows
    mov dl, 10
    mov ah, 02h
    int 21h
    mov dl, 13
    mov ah, 02h
    int 21h

    ;re-establishing counters
    pop bx
    pop cx
    add bx, 1
    cmp bx, cx
    jle control

exit:
    ; terminate program
    mov ax, 4c00h
    int 21h
    END start