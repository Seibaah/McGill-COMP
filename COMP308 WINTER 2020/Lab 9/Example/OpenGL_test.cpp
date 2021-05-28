// Based on NeHe(Jeff Molofee)'s code

#include <windows.h>		// Header File For Windows
#include <stdio.h>			// for sprintf()
#include <string>
#include <gl\gl.h>			// Header File For The OpenGL32 Library
#include <gl\glu.h>			// Header File For The GLu32 Library
#include "bmp.h"			// Header File For The Glaux replacement Library

/////////////////////////
//Extra variables declared here
int angle=0, rotations=0;
/////////////////////////

const float DEG2RAD = 3.14159f/180;

char*		appName = "OpenGL example";

HDC			hDC=NULL;		// Private GDI Device Context
HGLRC		hRC=NULL;		// Permanent Rendering Context
HWND		hWnd=NULL;		// Holds Our Window Handle
HINSTANCE	hInstance;		// Holds The Instance Of The Application

bool	keys[256];			// Array Used For The Keyboard Routine
bool	active = true;		// Window Active Flag Set To TRUE By Default
bool	fullscreen = true;	// Fullscreen Flag Set To Fullscreen Mode By Default

int		Xres = 1024;
int		Yres = 768;
int		Depth = 32;

GLfloat	uTx = 0.0f;
GLfloat	uTy = 0.0f;
GLfloat	uTz = 0.0f;
GLfloat	uRx = 0.0f;
GLfloat	uRy = 0.0f;
GLfloat	uRz = 0.0f;

GLfloat sunOffset = 0.0f;

GLuint	texture[1];

LRESULT	CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);	// Declaration For WndProc

AUX_RGBImageRec *LoadBMP(char *Filename)				// Loads A Bitmap Image
{
	FILE *File=NULL;									// File Handle

	if (!Filename)										// Make Sure A Filename Was Given
	{
		return NULL;									// If Not Return NULL
	}

	File=fopen(Filename,"r");							// Check To See If The File Exists

	if (File)											// Does The File Exist?
	{
		fclose(File);									// Close The Handle
		return auxDIBImageLoad(Filename);				// Load The Bitmap And Return A Pointer
	}

	return NULL;										// If Load Failed Return NULL
}

bool LoadGLTextures()									// Load Bitmaps And Convert To Textures
{
	AUX_RGBImageRec *TextureImage[1];					// Create Storage Space For The Texture

	memset(TextureImage,0,sizeof(void *)*1);           	// Set The Pointer To NULL

	// Load The Bitmap, Check For Errors, If Bitmap's Not Found Quit
	if (!(TextureImage[0]=LoadBMP("Data/image.bmp")))
		return false;

	glGenTextures(1, &texture[0]);						// Create One Textures

	// Create MipMapped Texture
	glBindTexture(GL_TEXTURE_2D, texture[0]);
	glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_NEAREST);
	gluBuild2DMipmaps(GL_TEXTURE_2D, 3, TextureImage[0]->sizeX, TextureImage[0]->sizeY, GL_RGB, GL_UNSIGNED_BYTE, TextureImage[0]->data);

	if (TextureImage[0])								// If Texture Exists	
		delete TextureImage[0];							// destroy it
	return true;										// Return The Status
}


GLvoid ReSizeGLScene(GLsizei width, GLsizei height)		// Resize And Initialize The GL Window
{
	if (height==0)										// Prevent A Divide By Zero By
		height=1;										// Making Height Equal One

	glViewport(0,0,width,height);						// Reset The Current Viewport

	glMatrixMode(GL_PROJECTION);						// Select The Projection Matrix
	glLoadIdentity();									// Reset The Projection Matrix

	// Calculate The Aspect Ratio Of The Window
	//gluPerspective(40.0f,(GLfloat)width/(GLfloat)height,1.0f,20.0f);
	glOrtho(0, width, height, 0, 0, 1);

	glMatrixMode(GL_MODELVIEW);							// Select The Modelview Matrix
	glLoadIdentity();									// Reset The Modelview Matrix
}

bool InitGL(GLvoid)										// All Setup For OpenGL Goes Here
{
	if (!LoadGLTextures())								// Jump To Texture Loading Routine
		return false;									// If Texture Didn't Load Return FALSE

	glEnable(GL_TEXTURE_2D);							// Enable Texture Mapping
	glShadeModel(GL_SMOOTH);							// Enable Smooth Shading
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);				// Black Background
	glClearDepth(1.0f);									// Depth Buffer Setup
	glDisable(GL_DEPTH_TEST);							// Disable Depth Testing (we're drawing in 2D)
	glDepthFunc(GL_LEQUAL);								// The Type Of Depth Testing To Do
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);	// Really Nice Perspective Calculations

	return true;
}

// Draws a filled square at position (x,y)
void drawSquare( GLfloat x, GLfloat y, float size, bool useTexture )
{
	glBegin( GL_QUADS );
		if ( useTexture ) glTexCoord2f(1.0f,1.0f);
		glVertex2f( x, y );
		if ( useTexture ) glTexCoord2f(0.0f,1.0f);
		glVertex2f( x + size, y );
		if ( useTexture ) glTexCoord2f(0.0f,0.0f);
		glVertex2f( x + size, y + size );
		if ( useTexture ) glTexCoord2f(1.0f,0.0f);
		glVertex2f( x, y + size );
	glEnd();
}

// Draws a filled circle at position (x,y)
void drawCircle( GLfloat x, GLfloat y, float radius )
{
   glBegin(GL_TRIANGLE_FAN);
 
   for (int i=0; i < 360; i++)
   {
      float degInRad = i*DEG2RAD;
      glVertex2f( x + cos(degInRad) * radius, y + sin(degInRad) * radius );
   }
 
   glEnd();
}

int DrawGLScene(GLvoid)									// Here's Where We Do All The Drawing
{
	
	glClearColor(0.2f, 0.6f, 0.9f, 0.0f);				// Select background color
	glClear(GL_COLOR_BUFFER_BIT);						// Clear Screen
	glLoadIdentity();									// Reset The Current Modelview Matrix

	// send vertices to GPU

	// Draw house body
	glColor3f(0.4f,0.4f,0.0f);

	glBegin(GL_QUADS);
		glVertex2f(300, 400);
		glVertex2f(600, 400);
		glVertex2f(600, 750);
		glVertex2f(300, 750);
	glEnd();

	// Draw roof
	glColor3f(0.6f,0.5f,0.2f);

	glBegin(GL_TRIANGLE_FAN);
		glVertex2f(300, 400);
		glVertex2f(450, 250);
		glVertex2f(600, 400);
	glEnd();

	// Draw door
	glColor3f(0.4f,0.2f,0.0f);

	glBegin(GL_QUADS);
		glVertex2f(320, 620);
		glVertex2f(380, 620);
		glVertex2f(380, 730);
		glVertex2f(320, 730);
	glEnd();

	// Draw windows ( 4 x 2 )
	glColor3f(0.7f,0.7f,0.7f);

	glBindTexture(GL_TEXTURE_2D, texture[0]);			// use our texture
	for ( int i = 0; i < 2; i++ )
	{
		for( int j = 0; j < 4; j++ )
			drawSquare( 320.0f + j*70.0f, 420.0f + i*90.0f, 50.0f, true );
	}

	// Draw sun
	glColor3f(1.0f,1.0f,0.0f);

	drawCircle( 800 - sunOffset, 150, 80 );

	// update sun position
	if ( sunOffset < 650 )
		sunOffset += 1.0f;

	/////////////////
	//Denis Racicot Morales
	//ID: 260799301
	/////////////////

	//vertices and center coordinates. Extra Variables declared up top
	float x0=700,y0=300,x1=700,y1=600,x2=900,y2=300;
	float cx=(x0+x1+x2)/3, cy=(y0+y1+y2)/3;

	//Apply if triangle hasn't been rotated 3 times yet
	if(rotations<3){

		//Sequence necessary to rotate through the center of the triangle instead of the origin
		glTranslatef(cx, cy, 0.0);
		glRotatef(angle++, 0.0, 0.0, 1.0);
		glTranslatef(-cx, -cy, 0.0);
	}
	//Draw triangle and wait a bit to fake animation
	glColor3f(0.2f,0.2f,0.2f);
	glBegin(GL_TRIANGLE_FAN);
		glVertex2f(x0, y0);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		Sleep(10);
	glEnd();
	//Each time the angle hits 360 a rotation has been completed
	if (angle>=360) {
		angle=0;
		rotations++;
	}
	////////////////////

	return true;
}

GLvoid KillGLWindow(GLvoid)								// Properly kill the window
{
	if (fullscreen)										// Are We In Fullscreen Mode?
	{
		ChangeDisplaySettings(NULL,0);					// If So Switch Back To The Desktop
		ShowCursor(true);								// Show Mouse Pointer
	}

	if (hRC)											// Do We Have A Rendering Context?
	{
		if (!wglMakeCurrent(NULL,NULL))					// Are We Able To Release The DC And RC Contexts?
			MessageBox(NULL,"Release of DC and RC failed", appName,MB_OK | MB_ICONINFORMATION);

		if (!wglDeleteContext(hRC))						// Are We Able To Delete The RC?
			MessageBox(NULL,"Release of rendering context failed.",appName,MB_OK | MB_ICONINFORMATION);
		hRC=NULL;										// Set RC To NULL
	}

	if (hDC && !ReleaseDC(hWnd,hDC))					// Are We Able To Release The DC
	{
		MessageBox(NULL,"Release of device context failed.", appName,MB_OK | MB_ICONINFORMATION);
		hDC=NULL;										// Set DC To NULL
	}

	if (hWnd && !DestroyWindow(hWnd))					// Are We Able To Destroy The Window?
	{
		MessageBox(NULL,"Could not release window handle.", appName,MB_OK | MB_ICONINFORMATION);
		hWnd=NULL;										// Set hWnd To NULL
	}

	if (!UnregisterClass("OpenGL",hInstance))			// Are We Able To Unregister Class
	{
		MessageBox(NULL,"Could not unregister class.", appName,MB_OK | MB_ICONINFORMATION);
		hInstance=NULL;									// Set hInstance To NULL
	}
}

/*	This Code Creates Our OpenGL Window.  Parameters Are:					*
 *	title			- Title To Appear At The Top Of The Window				*
 *	width			- Width Of The GL Window Or Fullscreen Mode				*
 *	height			- Height Of The GL Window Or Fullscreen Mode			*
 *	bits			- Number Of Bits To Use For Color (8/16/24/32)			*
 *	fullscreenflag	- Use Fullscreen Mode (TRUE) Or Windowed Mode (FALSE)	*/
 
bool CreateGLWindow(char* title, int width, int height, int bits, bool fullscreenflag)
{
	GLuint		PixelFormat;			// Holds The Results After Searching For A Match
	WNDCLASS	wc;						// Windows Class Structure
	DWORD		dwExStyle;				// Window Extended Style
	DWORD		dwStyle;				// Window Style
	RECT		WindowRect;				// Grabs Rectangle Upper Left / Lower Right Values
	WindowRect.left=(long)0;			// Set Left Value To 0
	WindowRect.right=(long)width;		// Set Right Value To Requested Width
	WindowRect.top=(long)0;				// Set Top Value To 0
	WindowRect.bottom=(long)height;		// Set Bottom Value To Requested Height

	fullscreen=fullscreenflag;			// Set The Global Fullscreen Flag

	hInstance			= GetModuleHandle(NULL);				// Grab An Instance For Our Window
	wc.style			= CS_HREDRAW | CS_VREDRAW | CS_OWNDC;	// Redraw On Size, And Own DC For Window.
	wc.lpfnWndProc		= (WNDPROC) WndProc;					// WndProc Handles Messages
	wc.cbClsExtra		= 0;									// No Extra Window Data
	wc.cbWndExtra		= 0;									// No Extra Window Data
	wc.hInstance		= hInstance;							// Set The Instance
	wc.hIcon			= LoadIcon(NULL, IDI_WINLOGO);			// Load The Default Icon
	wc.hCursor			= LoadCursor(NULL, IDC_ARROW);			// Load The Arrow Pointer
	wc.hbrBackground	= NULL;									// No Background Required For GL
	wc.lpszMenuName		= NULL;									// We Don't Want A Menu
	wc.lpszClassName	= "OpenGL";								// Set The Class Name

	if (!RegisterClass(&wc))									// Attempt To Register The Window Class
	{
		MessageBox(NULL,"Failed to register the window class.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;											// Return FALSE
	}
	
	if (fullscreen)												// Attempt Fullscreen Mode?
	{
		DEVMODE dmScreenSettings;								// Device Mode
		memset(&dmScreenSettings,0,sizeof(dmScreenSettings));	// Makes Sure Memory's Cleared
		dmScreenSettings.dmSize=sizeof(dmScreenSettings);		// Size Of The Devmode Structure
		dmScreenSettings.dmPelsWidth = width;					// Selected Screen Width
		dmScreenSettings.dmPelsHeight = height;					// Selected Screen Height
		dmScreenSettings.dmBitsPerPel = bits;					// Selected Bits Per Pixel
		dmScreenSettings.dmFields=DM_BITSPERPEL|DM_PELSWIDTH|DM_PELSHEIGHT;

		// Try To Set Selected Mode And Get Results.  NOTE: CDS_FULLSCREEN Gets Rid Of Start Bar.
		if (ChangeDisplaySettings(&dmScreenSettings,CDS_FULLSCREEN)!=DISP_CHANGE_SUCCESSFUL)
		{
			// If The Mode Fails, Offer Two Options.  Quit Or Use Windowed Mode.
			if (MessageBox(NULL,"The requested fullscreen mode is not supported by\nyour video card. Use windowed mode instead?", appName,MB_YESNO|MB_ICONEXCLAMATION)==IDYES)
				fullscreen = false;		// Windowed Mode Selected
			else
			{
				// Pop Up A Message Box Letting User Know The Program Is Closing.
				MessageBox(NULL,"Program will now close.", appName,MB_OK|MB_ICONSTOP);
				return false;									// Return FALSE
			}
		}
	}
	if (fullscreen)												// Are We Still In Fullscreen Mode?
	{
		dwExStyle=WS_EX_APPWINDOW;								// Window Extended Style
		dwStyle=WS_POPUP;										// Windows Style
		ShowCursor(false);										// Hide Mouse Pointer
	}
	else
	{
		dwExStyle=WS_EX_APPWINDOW | WS_EX_WINDOWEDGE;			// Window Extended Style
		dwStyle=WS_OVERLAPPEDWINDOW;							// Windows Style
	}

	AdjustWindowRectEx(&WindowRect, dwStyle, false, dwExStyle);		// Adjust Window To True Requested Size

	// Create The Window
	if (!(hWnd=CreateWindowEx(	dwExStyle,							// Extended Style For The Window
								"OpenGL",							// Class Name
								title,								// Window Title
								dwStyle |							// Defined Window Style
								WS_CLIPSIBLINGS |					// Required Window Style
								WS_CLIPCHILDREN,					// Required Window Style
								0, 0,								// Window Position
								WindowRect.right-WindowRect.left,	// Calculate Window Width
								WindowRect.bottom-WindowRect.top,	// Calculate Window Height
								NULL,								// No Parent Window
								NULL,								// No Menu
								hInstance,							// Instance
								NULL)))								// Dont Pass Anything To WM_CREATE
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Window creation error.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	static	PIXELFORMATDESCRIPTOR pfd=				// pfd Tells Windows How We Want Things To Be
	{
		sizeof(PIXELFORMATDESCRIPTOR),				// Size Of This Pixel Format Descriptor
		1,											// Version Number
		PFD_DRAW_TO_WINDOW |						// Format Must Support Window
		PFD_SUPPORT_OPENGL |						// Format Must Support OpenGL
		PFD_DOUBLEBUFFER,							// Must Support Double Buffering
		PFD_TYPE_RGBA,								// Request An RGBA Format
		bits,										// Select Our Color Depth
		0, 0, 0, 0, 0, 0,							// Color Bits Ignored
		0,											// No Alpha Buffer
		0,											// Shift Bit Ignored
		0,											// No Accumulation Buffer
		0, 0, 0, 0,									// Accumulation Bits Ignored
		16,											// 16Bit Z-Buffer (Depth Buffer)  
		0,											// No Stencil Buffer
		0,											// No Auxiliary Buffer
		PFD_MAIN_PLANE,								// Main Drawing Layer
		0,											// Reserved
		0, 0, 0										// Layer Masks Ignored
	};
	
	if (!(hDC=GetDC(hWnd)))							// Did We Get A Device Context?
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Cannot create a GL device context.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	if (!(PixelFormat=ChoosePixelFormat(hDC,&pfd)))	// Did Windows Find A Matching Pixel Format?
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Cannot find a suitable pixel format.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	if(!SetPixelFormat(hDC,PixelFormat,&pfd))		// Are We Able To Set The Pixel Format?
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Cannot set pixel format.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	if (!(hRC=wglCreateContext(hDC)))				// Are We Able To Get A Rendering Context?
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Cannot create a GL rendering context.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	if(!wglMakeCurrent(hDC,hRC))					// Try To Activate The Rendering Context
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Cannot activate the GL rendering context.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}

	ShowWindow(hWnd,SW_SHOW);						// Show The Window
	SetForegroundWindow(hWnd);						// Slightly Higher Priority
	SetFocus(hWnd);									// Sets Keyboard Focus To The Window
	ReSizeGLScene(width, height);					// Set Up Our Perspective GL Screen

	if (!InitGL())									// Initialize Our Newly Created GL Window
	{
		KillGLWindow();								// Reset The Display
		MessageBox(NULL,"Initialization failed.", appName,MB_OK|MB_ICONEXCLAMATION);
		return false;								// Return FALSE
	}
	return true;									// Success
}

LRESULT CALLBACK WndProc(	HWND	hWnd,			// Handle For This Window
							UINT	uMsg,			// Message For This Window
							WPARAM	wParam,			// Additional Message Information
							LPARAM	lParam)			// Additional Message Information
{
	switch (uMsg)									// Check For Windows Messages
	{
		case WM_ACTIVATE:							// Watch For Window Activate Message
		{
			if (!HIWORD(wParam))					// Check Minimization State
				active = true;						// Program Is Active
			else
				active = false;						// Program Is No Longer Active

			return 0;								// Return To The Message Loop
		}

		case WM_SYSCOMMAND:							// Intercept System Commands
		{
			switch (wParam)							// Check System Calls
			{
				case SC_SCREENSAVE:					// Screensaver Trying To Start?
				case SC_MONITORPOWER:				// Monitor Trying To Enter Powersave?
				return 0;							// Prevent From Happening
			}
			break;									// Exit
		}

		case WM_CLOSE:								// Did We Receive A Close Message?
		{
			PostQuitMessage(0);						// Send A Quit Message
			return 0;								// Jump Back
		}

		case WM_KEYDOWN:							// Is A Key Being Held Down?
		{
			keys[wParam] = true;					// If So, Mark It As TRUE
			return 0;								// Jump Back
		}

		case WM_KEYUP:								// Has A Key Been Released?
		{
			keys[wParam] = false;					// If So, Mark It As FALSE
			return 0;								// Jump Back
		}

		case WM_SIZE:								// Resize The OpenGL Window
		{
			ReSizeGLScene(LOWORD(lParam),HIWORD(lParam));  // LoWord=Width, HiWord=Height
			return 0;								// Jump Back
		}
	}
	// Pass All Unhandled Messages To DefWindowProc
	return DefWindowProc(hWnd,uMsg,wParam,lParam);
}

int WINAPI WinMain(	HINSTANCE	hInstance,			// Instance
					HINSTANCE	hPrevInstance,		// Previous Instance
					LPSTR		lpCmdLine,			// Command Line Parameters
					int			nCmdShow)			// Window Show State
{
	MSG		msg;									// Windows Message Structure
	bool	done = false;							// Bool Variable To Exit Loop

	// Ask The User Which Screen Mode They Prefer
	if (MessageBox(NULL,"Start in fullscreen mode?",  appName,MB_YESNO|MB_ICONQUESTION)==IDNO)
		fullscreen = false;							// Windowed Mode

	// Create Our OpenGL Window
	if (!CreateGLWindow( appName,Xres,Yres,Depth,fullscreen))
		return 0;									// Quit If Window Was Not Created

	while(!done)									// Loop That Runs While done=FALSE
	{
		if (PeekMessage(&msg,NULL,0,0,PM_REMOVE))	// Is There A Message Waiting?
		{
			if (msg.message==WM_QUIT)				// Have We Received A Quit Message?
				done = true;						// If So done=TRUE
			else									// If Not, Deal With Window Messages
			{
				TranslateMessage(&msg);				// Translate The Message
				DispatchMessage(&msg);				// Dispatch The Message
			}
		}
		else										// If There Are No Messages
		{
			// Draw The Scene.  Watch For ESC Key And Quit Messages From DrawGLScene()
			if ((active && !DrawGLScene()) || keys[VK_ESCAPE])	// Active?  Was There A Quit Received?
				done = true;						// ESC or DrawGLScene Signalled A Quit
			else									// Not Time To Quit, Update Screen
				SwapBuffers(hDC);					// Swap Buffers (Double Buffering)

			if (keys[VK_F1])						// Is F1 Being Pressed?
			{
				keys[VK_F1] = false;				// If So Make Key FALSE
				KillGLWindow();						// Kill Our Current Window
				fullscreen=!fullscreen;				// Toggle Fullscreen / Windowed Mode
				// Recreate Our OpenGL Window
				if (!CreateGLWindow( appName,Xres,Yres,Depth,fullscreen))
					return 0;						// Quit If Window Was Not Created
			}
			if (keys['W'])			
				uTy -= 0.1f;
			if (keys['S'])			
				uTy += 0.1f; 
			if (keys[VK_LEFT])
				uTx += 0.1f;
			if (keys[VK_RIGHT])
				uTx -= 0.1f;
			if (keys[VK_UP])
				uTz += 0.1f;
			if (keys[VK_DOWN])
				uTz -= 0.1f;
			if (keys['A'])
				uRy += 2.0f;
			if (keys['D'])
				uRy -= 2.0f;
			if (keys['Z'])
				uRx += 2.0f;
			if (keys['C'])
				uRx -= 2.0f;
			if (keys['Q'])
				uRz -= 2.0f;
			if (keys['E'])
				uRz += 2.0f;
		}
	}
	// Shutdown
	KillGLWindow();									// Kill The Window
	return (msg.wParam);							// Exit The Program
}
