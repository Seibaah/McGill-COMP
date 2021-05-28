 .286 
.model small
.stack 100h
.data
	coord dw 100, 100, 100, 60, 60, 60	;y0,x0 ; y1,x1 ; y2,x2 coordinates of the triangle
	fill_origin dw 90, 70			;y, x origin of fill procedure
.code

; draw a single pixel specific to Mode 13h (320x200 with 1 byte per color)
drawPixel:
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]

	push	bp
	mov	bp, sp

	push	bx
	push	cx
	push	dx
	push	es

	; set ES as segment of graphics frame buffer
	mov	ax, 0A000h
	mov	es, ax


	; BX = ( y1 * 320 ) + x1
	mov	bx, x1
	mov	cx, 320
	xor	dx, dx
	mov	ax, y1
	mul	cx
	add	bx, ax

	; DX = color
	mov	dx, color

	; plot the pixel in the graphics frame buffer
	mov	BYTE PTR es:[bx], dl

	pop	es
	pop	dx
	pop	cx
	pop	bx

	pop	bp

	ret	6
	
; merged draw function
drawLine_merge:
	
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]
	x2 EQU ss:[bp+10]
	y2 EQU ss:[bp+12]

	push bp
	mov bp, sp

	push    bx
	push    cx
	push	dx

	; BX keeps track of the X1 coordinate,
	; DX keeps track of the Y1 coordinate,
	mov	bx, x1
	mov	dx, y1

	; CX = number of pixels to draw horizontlly
	mov	cx, x2
	sub	cx, bx
	cmp cx, 0			;if cx!=0 then go to horizontal and diagonal loop
	jne nonVertical	
	jl revertVert
	jmp vertical
	
	revertVert:
		xor cx, 80h		;change sign 

	vertical:			;draws a vertical line
		mov	bx, y1
		mov	cx, y2
		sub	cx, bx		;if y1=y2 then both coords are the same, nothing to draw, exit program
		cmp cx, 0
		je exit
		dlv_5loop:
			push	bx
			push	x1
			push	color
			call	drawPixel
			cmp bx, y2	;cmp y1, y2
			jl down		; determines drawing dorection based on x1 and x2
			sub bx, 1
			jmp next5
		down:
			add	bx, 1
		next5:
			loopw	dlv_5loop
		dlv_5end:

		jmp exit

	nonVertical:		;draws horizontal and diagonal lines
		dld6_loop:
			push	dx
			push	bx
			push	color
			call	drawPixel
		
		compareY:	
			cmp WORD PTR y2, dx	;cmp y2, y1
			je horizontal
			jl diagUp
		diagDown:
			cmp bx, x2		;cmp x1, x2
			jl diagDownB
			sub	bx, 1		;draw diagonal R -> L and down
			add	dx, 1
			jmp continue
		diagDownB:			;else draw line L -> R and down
			add	bx, 1	
			add	dx, 1
			jmp continue	
		horizontal:	
			cmp bx, x2		;cmp x1, x2
			jl leftToRight
			sub bx, 1		;draw horizontal line R -> L
			jmp continue
		leftToRight:		;draw horizontal line L -> R
			add	bx, 1
			jmp continue
		diagUp:
			cmp bx, x2		;cmp x1, x2
			jl diagUpB		
			sub	bx, 1		;draws diagonal R -> L and up
			sub	dx, 1
			jmp continue
		diagUpB:			;else draws diagonal L -> R and up
			add	bx, 1
			sub	dx, 1
			jmp continue
		continue:			
			loopw	dld6_loop
		dld6_end:

	exit:
	pop		dx
	pop     cx
	pop     bx
	pop 	bp
	ret 8
;fill method, subdivided into 4 directional procedures
fill:
	fill_up:
		lea si, fill_origin		;loads into si the starting filling point

		;save y and x in registers respectively
		mov bx, [si+0]
		mov dx, [si+2]
		
		;go through the rows above the starting point, first left and then right
		loop_up:
			call read_color	;gets current pixel's color
			cmp al, 0001h	;cmp with border color
			je fill_down	;if equal then we can't keep going up, reset to starting position and go to fill down

			;prepare stack for drawPixel
			push bx		;y
			push dx		;x
			push 0005h	;fill color

			call drawPixel
			;fill pixels to the right until hit border
			move_right:
				inc dx
				call read_color	
				cmp al, 0001h 		
				je reset_x		;if equal then we hit border, reset back to starting x else keep filling to the right
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_right
			reset_x:
				mov dx, [si+2]		;reset dx register to x origin
			move_left:			;fill pixels to the left until hit border
				dec dx
				call read_color
				cmp al, 0001h
				je go_up		;if equal then we hit border, go to next line above else keep filling to the left
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_left
			go_up:					;update y coordinate to go up 1 row, reset x to starting point
				mov dx, [si+2]		;x
				dec bx 				;y
				jmp loop_up
	fill_down:
		mov bx, [si+0]	;y
		mov dx, [si+2]	;x
		;go through the rows under the starting point, first left and then right
		loop_down:
			call read_color ;gets current pixel's color
			cmp al, 0001h	;cmp with border color
			je fill_left	;if equal then we can't keep going down, reset to starting position and go to fill left

			;prepare stack for drawPixel
			push bx		;y
			push dx		;x
			push 0005h	;fill color

			call drawPixel
			;fill pixels to the right until hit border
			move_right2:
				inc dx
				call read_color	
				cmp al, 0001h 		
				je reset_x2		;if equal then we hit border, reset back to starting x else keep filling to the right
				;prepare stack for drawPixel		
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_right2
			reset_x2:
				mov dx, [si+2]		;reset dx to x origin
			move_left2:			;fill pixels to the left until hit border
				dec dx
				call read_color
				cmp al, 0001h
				je go_down			;if equal then we hit border, go to next line under else keep filling to the left
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_left2
			go_down:				;update y coordinate to go down 1 row, reset x to starting point
				mov dx, [si+2]		;x
				inc bx 				;y
				jmp loop_down
			
	fill_left:
		mov bx, [si+0]	;y
		mov dx, [si+2]	;x
		;go through the rows left of the starting point, first up and then down
		loop_left:
			call read_color	;gets current pixel's color
			cmp al, 0001h	;cmp with border color
			je fill_right	;if equal then we can't keep going left, reset to starting position and go to fill right

			;prepare stack for drawPixel
			push bx		;y
			push dx		;x
			push 0005h	;fill color

			call drawPixel
			;fill pixels up until hit border
			move_up:
				dec bx
				call read_color	
				cmp al, 0001h 		
				je reset_y		;if equal then we hit border, reset back to starting y else keep filling up
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_up
			reset_y:				
				mov bx, [si]		;reset bx to y origin
			move_down:				;fill pixels down until hit border
				inc bx
				call read_color
				cmp al, 0001h
				je go_left			;if equal then we hit border, go to next column to the left else keep filling down
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_down
			go_left:				;update x coordinate to go left 1 col, reset y to starting point
				mov bx, [si+0]		;y
				dec dx				;x
				jmp loop_left		
		
	fill_right:
		mov bx, [si+0]	;y
		mov dx, [si+2]	;x
		loop_right:
			call read_color	;gets current pixel's color
			cmp al, 0001h		;cmp with border color
			je exit2			;if equal then we can't keep going right, done filling shape

			;prepare stack for drawPixel
			push bx		;y
			push dx		;x
			push 0005h		;fill color

			call drawPixel
			;fill pixels down until hit border
			move_up2:
				dec bx
				call read_color	
				cmp al, 0001h 		
				je reset_y2		;if equal then we hit border, reset back to starting y else keep filling down
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h		;fill color
				call drawPixel
				jmp move_up2
			reset_y2:
				mov bx, [si]		;reset dx to x origin
			move_down2:			;fill pixels down until hit border
				inc bx
				call read_color
				cmp al, 0001h
				je go_right2		;if equal then we hit border, go to next column to the right else keep filling down
				;prepare stack for drawPixel
				push bx		;y
				push dx		;x
				push 0005h	;fill color
				call drawPixel
				jmp move_down2
			go_right2:				;update x coordinate to go right 1 col, reset y to starting point
				mov bx, [si+0]		;y
				inc dx				;x
				jmp loop_right

		exit2:		;exit
		ret

;reading current pixel color
read_color:
 	;save current bx, dx onto the stack
	push dx		;x
	push bx		;y

 	;interrupt 0Dh
	mov ah, 0Dh		
	mov cx, dx		;coords of pixel cx=x ; dx=y
	mov dx, bx
	mov bh, 0		;set page to 0 in bh
	int 10H			;al = color

	;restore the registers
	pop bx
	pop dx

	ret 

start:
	; initialize data segment
	mov ax, @data
	mov ds, ax

	; set video mode - 320x200 256-color mode
	mov ax, 4F02h
	mov bx, 13h
	int 10h
	
	; processing 3 coordinates, works for triangles with 45 deg angles
	lea si, coord
	push [si+0]		;y0
	push [si+2]		;x0
	push [si+4]		;y1
	push [si+6]		;x1
	push 0001h
	call drawLine_merge
	
	lea si, coord
	push [si+4]		;y1
	push [si+6]		;x1
	push [si+8]		;y2
	push [si+10]	;x2
	push 0001h
	call drawLine_merge
	
	lea si, coord
	push [si+0]		;y0
	push [si+2]		;x0
	push [si+8]		;y2
	push [si+10]	;x2
	push 0001h
	call drawLine_merge

	;modify traingle vertices and starting positions in the .data section
	call fill
	
	; prompt for a key
	mov ah, 0
	int 16h

	; switch back to text mode
	mov ax, 4f02h
	mov bx, 3
	int 10h

	mov ax, 4C00h
	int 21h

END start