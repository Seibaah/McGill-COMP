.286 
.model small
.stack 100h
.data
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
	

; draw a horizontal line
drawLine_h:
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]
	X2 EQU ss:[bp+10]

	push bp
	mov bp, sp

	push    bx
	push    cx

	; BX keeps track of the X coordinate
	mov	bx, x1

	; CX = number of pixels to draw
	mov	cx, x2
	sub	cx, bx
	inc	cx
	dlh_loop:
		push	y1
		push	bx
		push	color
		call	drawPixel
		add	bx, 1
		loopw	dlh_loop
	dlh_end:

	pop     cx
	pop     bx

	pop bp

	ret 8


; draw a vertical line
drawLine_v:
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]
	y2 EQU ss:[bp+10]

	push bp
	mov bp, sp

	push    bx
	push    cx

	; BX keeps track of the Y coordinate
	mov	bx, y1

	; CX = number of pixels to draw
	mov	cx, y2
	sub	cx, bx
	inc	cx
	dlv_loop:
		push	bx
		push	x1
		push	color
		call	drawPixel
		add	bx, 1
		loopw	dlv_loop
	dlv_end:

	pop     cx
	pop     bx

	pop bp

	ret 8


; draw a right increasing diagonal line
drawLine_d1:
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]
	x2 EQU ss:[bp+10]

	push bp
	mov bp, sp

	push    bx
	push    cx
	push	dx

	; BX keeps track of the X coordinate,
	; DX keeps track of the Y coordinate
	mov	bx, x1
	mov	dx, y1

	; CX = number of pixels to draw
	mov	cx, x2
	sub	cx, bx
	inc	cx		;this doesn't seem to do much
	dld1_loop:
		push	dx
		push	bx
		push	color
		call	drawPixel
		add	bx, 1
		sub	dx, 1
		loopw	dld1_loop
	dld1_end:

	pop	dx
	pop     cx
	pop     bx

	pop bp

	ret 8


; draw a right decreasing diagonal line
drawLine_d2:
	color EQU ss:[bp+4]
	x1 EQU ss:[bp+6]
	y1 EQU ss:[bp+8]
	x2 EQU ss:[bp+10]

	push bp
	mov bp, sp

	push    bx
	push    cx
	push	dx

	; BX keeps track of the X coordinate,
	; DX keeps track of the Y coordinate
	mov	bx, x1
	mov	dx, y1

	; CX = number of pixels to draw
	mov	cx, x2
	sub	cx, bx
	inc	cx
	dld2_loop:
		push	dx
		push	bx
		push	color
		call	drawPixel
		add	bx, 1
		add	dx, 1
		loopw	dld2_loop
	dld2_end:

	pop	dx
	pop     cx
	pop     bx

	pop bp

	ret 8
	
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
	pop bp
	ret 8

;start has been modified to draw a house with the new merged draw function
;if you pay attention you'll see it draws every possible line from L to R and R to L
start:
	; initialize data segment
	mov ax, @data
	mov ds, ax

	; set video mode - 320x200 256-color mode
	mov ax, 4F02h
	mov bx, 13h
	int 10h

	; draw a house

	; left wall
	push WORD PTR 100	;y2
	push WORD PTR 80	;x2
	push WORD PTR 160	;y1
	push WORD PTR 80	;x1
	push 0001h
	call drawLine_merge
	
	; right wall
	push WORD PTR 160	;y2
	push WORD PTR 240	;x2
	push WORD PTR 100	;y1
	push WORD PTR 240	;x1
	push 0002h
	call drawLine_merge
	
	; floor
	push WORD PTR 160	;y2
	push WORD PTR 80	;x2
	push WORD PTR 160	;y1
	push WORD PTR 240	;x1
	push 0003h
	call drawLine_merge

	; ceiling
	push WORD PTR 100	;y2
	push WORD PTR 240	;x2
	push WORD PTR 100	;y1
	push WORD PTR 80	;x1
	push 0004h
	call drawLine_merge
	
	; roof left
	push WORD PTR 20	;y2
	push WORD PTR 160	;x2
	push WORD PTR 100	;y1
	push WORD PTR 80	;x1
	push 0005h
	call drawLine_merge
	
	; roof right
	push WORD PTR 100	;y2
	push WORD PTR 240	;x2
	push WORD PTR 20	;y1
	push WORD PTR 160	;x1
	push 0006h
	call drawLine_merge

	; diagonal down reverse
	push WORD PTR 110	;y2
	push WORD PTR 120	;x2
	push WORD PTR 140	;y1
	push WORD PTR 150	;x1
	push 0007h
	call drawLine_merge
	
	; diagonal up reverse
	push WORD PTR 140	;y2
	push WORD PTR 120	;x2
	push WORD PTR 110	;y1
	push WORD PTR 150	;x1
	push 0008h
	call drawLine_merge


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