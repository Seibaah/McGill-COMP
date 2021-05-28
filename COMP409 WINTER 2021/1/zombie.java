public class zombie {
	
	//# of door threads and zombie killer thread
	static int k;
	static Thread zk;
	
	//thread and thread object arrays
	static Thread[] door;
	volatile static doorThread[] doorObj;
	
	//zombie limit and current head count
	volatile static long zombiesLimit;
	volatile static long zombiesInTheRoom;
	
	volatile static boolean overflow;
	
	public static void main(String[] args) {
		
		start(args);

		//wait for zombie killer to be done
		try {
			zk.join();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//initial setup for the game execution
	public static void start(String args[]) {
		
		//parsing arguments
		if (args.length==2) {
			k = Integer.parseInt(args[0]);
			zombiesLimit = Integer.parseInt(args[1]);
		}
		else {
        	System.out.println("2 arguments required: # of doors, zombie limit");
        	System.out.println("Running default settings: 8, 60");
        	k=8; zombiesLimit=60;
        }	
		
		overflow=false;
		
		//create specialized threads and objects
		zk = new Thread(new zombieKiller());
		door = new Thread[k];
		doorObj = new doorThread[k];
		
		//start specialized threads
		for (int i=0; i<k; i++) {
			doorObj[i]= new doorThread(i);
			door[i]= new Thread(doorObj[i]);
			door[i].start();
		}
		
		zk.start();
	}

	//adds the number of zombies let in by a thread and closes its door
	public synchronized static void updateZombieCount(int id, int n) {
		addZombiesInTheRoom(n);
		doorObj[id].closeDoor();
	}
	
	public synchronized static long getZombiesInTheRoom() {
		return zombiesInTheRoom;
	}
	
	public synchronized static void setZombiesInTheRoom(long n) {
		zombiesInTheRoom=n;
	}
	
	public synchronized static void addZombiesInTheRoom(long n) {
		zombiesInTheRoom+=n;
	}
	
	public synchronized static doorThread getDoor(int i) {
		return doorObj[i];
	}
	
	public synchronized static boolean getOverflow() {
		return overflow;
	}
	
	public synchronized static void setOverflow(boolean b) {
		overflow=b;
	}
	
	//Every second tallies the kills and updates the number of zombies in the room
	//Only accessed when all doors are closed
	public synchronized static void updateKillCount(int n, long t) {

		long tempCount=getZombiesInTheRoom();
		
		//updates the number of zombies alive
		setZombiesInTheRoom(Math.max(0, getZombiesInTheRoom()-n));
		
		//Display throughput
		System.out.println("Throughput " + (tempCount-getZombiesInTheRoom()) + " z/s");
		
		//if there is no overflow 
		if (!getOverflow()) {
			//open the doors if the total is under the limit
			if (getZombiesInTheRoom()<zombiesLimit) {
				//System.out.println("Doors open");
				for (int i=0; i<k; i++) {
					getDoor(i).openDoor();
				}
			}
			//otherwise don't open the doors
			else {
				//System.out.println("Doors close");
				setOverflow(true);
			}
		}
		//if there is overflow
		else {
			//open the doors only is under limit/2
			if (getZombiesInTheRoom()<(zombiesLimit/2)) {
				//System.out.println("Doors open");
				for (int i=0; i<k; i++) {
					getDoor(i).openDoor();
				}
			}
			else {
				//System.out.println("Doors close");
				setOverflow(true);
			}
		}
		
	}
	
	//returns true if all the doors are closed
	public synchronized static boolean waitForDoors() {
		boolean b=false;
		for (int i=0; i<k; i++) {
			b=!getDoor(i).getDoorStatus();
		}
		return b;
	}
}

class zombieKiller implements Runnable {
	
	volatile int kills;
	volatile long runTime;
	
	//a zombie has a 40% chance of being killed every 10ms
	public void run() {
		
		try {
			
			while (true) {
				
				double x = Math.random();
				
				if (0.4f >= x) {
					addKills(1);					
				}
				
				Thread.sleep(10);
				
				runTime+=10;
				if (runTime%1000==0) {
					
					System.out.println("Time " + (runTime/1000) + "s");
					
					//wait for all doors to be closed
					while(zombie.waitForDoors());
					
					/*for (int i=0; i<8; i++) {
						System.out.println("id " + zombie.getDoor(i).getDoorStatus());
						System.out.println("id " + zombie.getDoor(i).getZombies());
					}*/
					
					//resolve kill and zombie deltas in a thread safe environment
					zombie.updateKillCount(kills, runTime);
					
					//reset timesplit kill counter
					setKills(0);
				}
			}
		}
		catch (Exception e) {
			System.out.println("Killer thread failed");
			e.printStackTrace();
		}
	}
	
	public synchronized void setKills(int n) {
		kills=n;
	}
	
	public synchronized void addKills(int n) {
		kills+=n;
	}
}

class doorThread implements Runnable {

	int id;
	volatile int zombieCount;
	volatile long runTime;
	volatile boolean doorClosed;
	
	//constructor
	doorThread(int n){
		id=n;
		doorClosed=false;
	}
	
	//thread safe close door operation
	public synchronized void closeDoor() {
		doorClosed = true;
	}
	
	//thread safe open door operation
	public synchronized void openDoor() {
		doorClosed = false;
	}
	
	//zombies have a 10% spawning chance every 10 ms
	public void run() {
		
		try {
			
			while (true) {
				
				double x = Math.random();
				
				if (0.1f >= x) {
					addZombies(1);
				}
				
				Thread.sleep(10);
				
				//every 1s the doors tally their spawn numbers 
				runTime+=10;
				if (runTime%1000==0) {
					
					//add to the total number of zombies. Door is closed
					zombie.updateZombieCount(id, zombieCount);
					
					//wait until your door is opened to repeat the process
					while (getDoorStatus()==true);
					
					//reset local counter
					setZombies(0);
				}
			}
		}
		catch (Exception e) {
			System.out.println("Door thread failed");
			e.printStackTrace();
		}
		
	}
	
	public synchronized boolean getDoorStatus() {
		return doorClosed;
	}
	
	public synchronized int getZombies() {
		return zombieCount;
	}
	
	public synchronized void addZombies(int n) {
		zombieCount+=n;
	}
	
	public synchronized void setZombies(int n) {
		zombieCount=n;
	}
	
	public synchronized void syncTimer(long t) {
		runTime=t;
	}
}

