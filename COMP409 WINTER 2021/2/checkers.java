import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.*;

public class checkers {
	//t -> number of threads; k -> sleep milliseconds; n -> number of moves 
	public static int t, k, n, color;
    public static ArrayList<Thread> threads;
    
    //board and piece reference data structures
    public static Tile board[][];
    public static ArrayList<Piece> pieces;
    
	public static void main(String[] args) {

		t = Integer.parseInt(args[0]); k = Integer.parseInt(args[1]); n = Integer.parseInt(args[2]);

		//initialization step
		//board setup
		board = new Tile[8][8];
		fillBoard();
		updateAdjTiles();
		
		//spawn pieces
		pieces = new ArrayList<Piece>();
		threads = new ArrayList<Thread>();
		spawnPieces();
		
		//start the game
		startThreads();
		
		//wait for all piece threads to be done
		for (Thread th : threads) {
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//starts the threads for each piece
	public static void startThreads() {
		for (Thread th : threads) {
			th.start();
		}
	}
	
	//spawns the pieces on the tiles of the same color on the board
	public static void spawnPieces() {
		Random rand =  new Random();
		
		color = rand.nextInt(2);
		
		while (t>0) {
			int px = rand.nextInt(8);
			int py = rand.nextInt(8);
			
			//pieces spawn only on the same color and if the tile isn't already occupied
			if (color==0 && board[py][px].col==Color.red && board[py][px].tLock.lock.get()==false) {
				//update board
				board[py][px].tLock.lock.set(true);
				board[py][px].tLock.sub = new Piece(px, py, board[py][px], n, t);
				//update top level data structures
				pieces.add(board[py][px].tLock.sub);
				threads.add(new Thread(board[py][px].tLock.sub));
				
				t--;
			}
			else if (color==1 && board[py][px].col==Color.black && board[py][px].tLock.lock.get()==false) {
				//update board
				board[py][px].tLock.lock.set(true);
				board[py][px].tLock.sub = new Piece(px, py, board[py][px], n, t);
				//update top level data structures
				pieces.add(board[py][px].tLock.sub);
				threads.add(new Thread(board[py][px].tLock.sub));
				
				t--;
			}
			
		}
	}

	//builds an adjacency list for every tile
	public static void updateAdjTiles() {
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				addNeighbors(board[i][j]);
			}
		}
	}
	
	//test if tiles can be added to the adjacency list
	public static void addNeighbors(Tile t) {
		if (t.coord.x-1>=0 && t.coord.y-1>=0) {
			t.adjList.add(board[t.coord.y-1][t.coord.x-1]);
		}
		if (t.coord.x-1>=0 && t.coord.y+1<8) {
			t.adjList.add(board[t.coord.y+1][t.coord.x-1]);
		}
		if (t.coord.x+1<8 && t.coord.y-1>=0) {
			t.adjList.add(board[t.coord.y-1][t.coord.x+1]);
		}
		if (t.coord.x+1<8 && t.coord.y+1<8) {
			t.adjList.add(board[t.coord.y+1][t.coord.x+1]);
		}
	}
	
	//initializes every tile on the board
	public static void fillBoard() {
		
		boolean isRed=true;
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				if (isRed == true) {
					board[i][j] = new Tile(isRed, j, i);
				}
				else {
					board[i][j] = new Tile(isRed, j, i);
				}
				isRed = !isRed;
			}
			isRed = !isRed;
		}
	}
}

class Tile {
	//position and color of the tile
	Pair coord;
	Color col;
	
	//special lock that holds a tile's state
	TileLock tLock;
	
	//a list of references this tile diagonally adjacent tiles
	ArrayList<Tile> adjList;
	
	Tile(boolean b, int x, int y){
		coord = new Pair(x, y);
		if (b == true) {
			col = Color.red;
		}
		else {
			col = Color.black;
		}
		
		tLock=new TileLock(false);
		
		adjList = new ArrayList<Tile>();
	}
	
	/*
	 * compares a tile's state with an expected state and returns the 
	 * comparison results
	 */
	public synchronized BoolTriple getTileInfo(TileLock expected) {
		boolean a=false, b=false, c=false;
		if (tLock.lock.get()==expected.lock.get()) {
			a=true;
		}
		
		if (tLock.owner==expected.owner) {
			b=true;
		}
		
		if (tLock.sub==expected.sub) {
			c=true;
		}
		
		return (new BoolTriple(a, b, c));
	}
	
	/*
	 * if the tile's state matches the expected values then lock the tile
	 * for a move command issue by the caller. Returns success or failure of
	 * the operation
	 */
	public synchronized boolean lockMove(TileLock expected, Piece caller) {
		boolean a=false, b=false, c=false;
		if (tLock.lock.get()==expected.lock.get()) {
			a=true;
		}
		if (tLock.owner==expected.owner) {
			b=true;
		}
		if (tLock.sub==expected.sub) {
			c=true;
		}
		if (a==true&&b==true&&c==true) {
			tLock.lock.set(true);
			tLock.owner=caller;
			return true;
		}
		return false;
	}
	
	/*
	 * if the tile's state matches the expected values then lock the tile
	 * for a capture command issue by the caller. Returns success or failure of
	 * the operation
	 */
	public synchronized boolean lockCapture(TileLock expected, Piece caller) {
		boolean a=false, b=false, c=false;
		if (tLock.lock.get()==expected.lock.get()) {
			a=true;
		}
		if (tLock.owner==expected.owner) {
			b=true;
		}
		if (tLock.sub!=expected.sub) {
			c=true;
		}
		if (a==true&&b==true&&c==true) {
			tLock.lock.set(true);
			tLock.owner=caller;
			return true;
		}
		return false;
	}
	
	/*
	 * resets the tile to its default starting state (i.e unnocupied state)
	 */
	public synchronized void resetTile() {
			tLock.lock.set(false);
			tLock.owner=null;
			tLock.sub=null;
	}
	
	/*
	 * updates the target tile's info to make the caller move
	 * updates the caller position and tile reference too
	 * and its moves left to do 
	 */
	public synchronized void moveTo(Piece caller) {
		tLock.lock.set(true);
		tLock.owner=caller;
		tLock.sub=caller;
		
		caller.coord.x=coord.x;
		caller.coord.y=coord.y;
		caller.t=this;
		caller.movesLeft--;
	}
	
	/*
	 * restores a tile's state if a move attempt couldn't go through
	 */
	public synchronized void revertMoveAttempt() {
		tLock.lock.set(true);
		tLock.owner=null;
	}
	
	/*
	 * restores a tile's state if a capture attempt couldn't go through
	 */
	public synchronized void revertCaptureAttempt() {
		tLock.lock.set(false);
		tLock.owner=null;
	}
}

class Piece implements Runnable{
	int id;
	//tile state variables
	int movesLeft;
	volatile Pair coord;
	volatile Tile t;
	volatile boolean wasCaptured;
	
	//specialized moves data structures
	ArrayList<Action> simples;
	ArrayList<Action> captures;

	Piece(int x, int y, Tile tl, int n, int i){
		coord = new Pair(x, y);
		t=tl; movesLeft=n; id=i;
		simples=new ArrayList<Action>();
		captures=new ArrayList<Action>();
		wasCaptured=false;
	}

	@Override
	public void run() {
		
		while (movesLeft>0) {
			/*
			 * computes all possible moves at this point in time and stores them 
			 * in specialized data structures
			 */
			computeMoves();

			//randomizes the moves order
			Collections.shuffle(captures, ThreadLocalRandom.current());
			Collections.shuffle(simples, ThreadLocalRandom.current());
			
			//tries to move in one of the available ways
			move();
			
			//tests whether or not we were captured
			if (wasCaptured==true) {
				//if we were we go to sleep and then respawn for a random a time
				goToSleep(ThreadLocalRandom.current().nextInt(2, 5));
				respawn();
			}
			else {
				//if we weren't then we go to sleep
				goToSleep(1);
			}
			
			//reset action data structures
			simples.clear();
			captures.clear();
			
			//unlock your own tile
			t.revertMoveAttempt();
			
		}
		
		//take your piece off the board before thread termination
		t.resetTile();
	}
	
	//prints the piece last move
	public void printMove(String s) {
		System.out.println(s);
	}
	
	/*
	 * after a capture respawn in a new location of the same color as the initial spawn
	 */
	private void respawn() {
		
		while (true) {
			int px=ThreadLocalRandom.current().nextInt(8);
			int py=ThreadLocalRandom.current().nextInt(8);
			
			if(checkers.color==0 && checkers.board[py][px].col==Color.red) {
				boolean c=checkers.board[py][px].lockMove(new TileLock(false, null, null), this);
						
				if (c==true){
					checkers.board[py][px].moveTo(this);
					break;
				}
			}
			else if(checkers.color==1 && checkers.board[py][px].col==Color.black) {
				boolean c=checkers.board[py][px].lockMove(new TileLock(false, null, null), this);

				if (c==true){
					checkers.board[py][px].moveTo(this);
					break;
				}
			}
		}
		printMove("T" + id + ": respawning at" + coord.toString());
		wasCaptured=false;
	}
	
	//puts a thread to sleep for some time
	private void goToSleep(int n) {
		try {
			Thread.sleep(n*checkers.k);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * executes an available move if possible
	 */
	private void move() {
		boolean canMove=false;
		boolean c1=t.lockCapture(new TileLock(true, null, null), this);

		//tile tries to set itself as invincible
		if (c1==true) {
			canMove=true;
		}
		else { 
			/*
			 * if we fail to set ourselves as invincible then we were captured
			 * take the piece off the board
			 */
			t.resetTile();
			wasCaptured=true;
			t=null;
			coord.x=-1; coord.y=-1;
			
			printMove("T" + id + ": captured");
		}
		
		//if we can move try to perform a capture
		while (canMove) {
			//test if there possible captures were computed
			if (captures.size()>0) {
				Action ac = captures.get(0);
				//try to lock the tile were we land on a capture
				boolean c2=ac.path.get(1).lockMove(new TileLock(false, null, null), this);
				
				//if we succeeded proceed
				if (c2==true) {
					//try to lock the tile where the captured piece is
					boolean c3=ac.path.get(0).lockCapture(new TileLock(true, null, null), this);
					
					//if we succeed then we have performed a complete capture
					if (c3==true) {	
						
						//move to the new tile and save your old tile reference
						Tile prevT=t;
						ac.path.get(1).moveTo(this);
						
						//unlock your old tile
						prevT.resetTile();
						
						//don't need to try moves anymore
						canMove=false;
						
						printMove("T" + id + ": captures "+ prevT.coord.toString());
						printMove("T" + id + ": moves to " + t.coord.toString());


					}
					else {
						/*
						 * we couldn't capture a piece in the last step
						 * so we unlock the tile where we were supposed to move to
						 */
						ac.path.get(1).revertCaptureAttempt();
						
						//we remove this capture action from our list of available actions
						captures.remove(0);
					}
				}
				else {
					/*
					 * we couldn't lock a move to perform a capture
					 * we remove this capture action from our list of available actions
					 */
					captures.remove(0);
				}
				
			}
			else {
				//we have no more capture actions left to attempt so we break
				break;
			}
		}
		
		//if we still can move then we try to perform a simple move
		while (canMove) {
			//test if we have available simple moves to perform
			if (simples.size()>0) {
				Action ac = simples.get(0);
				//try to lock the tile were we land on a simple move
				boolean c2=ac.path.get(0).lockMove(new TileLock(false, null, null), this);

				//if we succeeded then we can move
				if (c2==true) {
					//move to the new tile and save your old tile reference
					Tile prevT=t;
					ac.path.get(0).moveTo(this);
					
					//unlock your old tile
					prevT.resetTile();
					
					//don't need to try moves anymore
					canMove=false;
					
					printMove("T" + id + ": moves to " + t.coord.toString());
				}
				else { 
					/*
					 * we couldn't lock a tile to perform a simple move
					 * we remove this capture action from our list of available actions
					 */
					simples.remove(0);
				}
			}
			else {
				//we have no more simple move actions left to attempt so we break
				break;
			}
		}
	}
	
	//computes all the possible moves available to us
	private void computeMoves() {
		//iterate through the tile's adjacency list
		for (int i=0; i<t.adjList.size(); i++) {
			//test a tile state
			BoolTriple c1=t.adjList.get(i).getTileInfo(new TileLock(true, null, null)); 
			
			//if the tile holds a tile
			if (c1.l==true && c1.o==true && c1.s==false) {
				
				//get the landing tile for a capture move
				Tile landingPt=getCapMoveTile(t.adjList.get(i));
				
				//disregard the move if said tile is out of bounds
				if (landingPt==null) {
					continue;
				}
				
				/*
				 * test if the next tile in the current diagonal is free to move to
				 * to complete a capture
				 */
				BoolTriple c2=landingPt.getTileInfo(new TileLock(false, null, null));
				
				//if state allows for it then add the capture as a possible move
				if (c2.l==true && c2.o==true && c2.s==true) {
					
					Action ac=new Action();
					
					//create capture path
					ac.path.add(t.adjList.get(i));
					ac.path.add(landingPt);
					
					//add to capture moves data structure
					captures.add(ac);
				}
			}
			/*
			 * can't perform a capture but test the state to see if we can move to 
			 * it instead
			 */
			else if (c1.l==false && c1.o==true && c1.s==true) {
				//can perform a simple move
				Action ac=new Action();
				
				//create move path
				ac.path.add(t.adjList.get(i));
				
				//add to simple moves data structure
				simples.add(ac);
			}
		}
	}
	
	//returns the tile to land on after a capture or null if impossible
	private Tile getCapMoveTile(Tile tl) {
		int x1=tl.coord.x, y1=tl.coord.y, xf, yf;
		int dx=coord.x-x1, dy=coord.y-y1;
		
		if (dx==1) {
			if (dy==1) { //upper left
				 xf=coord.x-2; yf=coord.y-2;
			}
			else { //down left
				xf=coord.x-2; yf=coord.y+2;
			}
		}
		else {
			if (dy==1) { //upper right
				xf=coord.x+2; yf=coord.y-2;
			}
			else { //down right
				xf=coord.x+2; yf=coord.y+2;
			}
		}
		
		if (xf>=0 && xf<8 && yf>=0 && yf<8) {
			return checkers.board[yf][xf];
		}
		else return null;
	}
}

/*
 * custom lock that holds the state of a tile
 * lock-> if a piece is on the tile or not
 * owner-> the piece that owns and whose operations can't be interrupted for that tile
 * sub-> piece on the tile but that can be interrupted or captured
 */
class TileLock {
	volatile AtomicBoolean lock;
	volatile Piece owner;
	volatile Piece sub;
	
	TileLock(boolean b){
		lock=new AtomicBoolean(b);
		owner=null;
		sub=null;
	}
	
	TileLock(boolean b, Piece op, Piece s){
		lock=new AtomicBoolean(b);
		owner=op;
		sub=s;
	}
}

/*
 * triple set of booleans to test thread safely a tile's state
 */
class BoolTriple {
	volatile boolean l;
	volatile boolean o;
	volatile boolean s;
	
	BoolTriple (boolean a, boolean b, boolean c){
		l=a; o=b; s=c;
	}
}

/*
 * class that holds the tiles that are involved in an action
 * simple moves have a path of length 1
 * capture moves have a path of length 2
 */
class Action {
	ArrayList<Tile> path;
	
	Action(){
		path = new ArrayList<Tile>();
	}
	
	public String toString() {
		String s="";
		for (int i=0; i<path.size(); i++) {
			s=s.concat(" to "+path.get(i).coord.toString());
		}
		return s;
	}
}

/*
 * Pair class for coordinates on the board
 */
class Pair {
	int x;
	int y;
	
	Pair(int a, int b){
		x=a;
		y=b;
	}
	
	public String toString() {
		return ("("+(7-y)+","+x+")");
	}
}

//color of the tile
enum Color {
	red,
	black
}
