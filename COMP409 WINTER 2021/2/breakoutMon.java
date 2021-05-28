import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class breakoutMon {
	//n->simulation time, k->waiting sleep time, w->breakout room sleep time
	public static int n, k, w;
    public static ArrayList<Thread> threads;
	
	public static void main(String[] args) {
		
		n = Integer.parseInt(args[0]); k = Integer.parseInt(args[1]); w = Integer.parseInt(args[2]);
		
		long t0=System.currentTimeMillis();
		
		//initialize threads and monitor
		Monitor mon=new Monitor(n, k, w, t0);
		threads = new ArrayList<Thread>();
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Science, mon, i)));
		}
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Art, mon, i+4)));
		}
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Engineering, mon, i+8)));
		}
		
		//shuffle to adjust for spawn order bias
		Collections.shuffle(threads);
		
		//start threads
		for (Thread t : threads) {
			t.start();
		}
		
		//execute simulation timing code
		mon.main();
	}
}

class Monitor {
	//cmd line parameters
	public int n, k, w;
	
	//simulation start time and run condition
	public long t0;
    public volatile boolean simulationOn;
	
	//rooms owning and previous owning faculty
    public volatile Faculty fac;
    public volatile Faculty lastFac;
    
    /*
     * waitCount indicates a student not from the current room faculty is waiting
     * roomCount keeps track of the number of students in the room
     */
    public volatile int waitCount;
    public volatile int roomCount;
    
    public Monitor(int nn, int kk, int ww, long t) {
    	n=nn; k=kk; w=ww; t0=t; waitCount=0; roomCount=0;
    	simulationOn=true;
    }
    
    //keeps the simulation going for n seconds
    public void main() {
		while (System.currentTimeMillis()-t0<n*1000) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		simulationOn=false;
    }
    
    /*
     * if the room's (faculty is null) or (if it's their faculty and no other faculty is
     * waiting) then students can go right in the room. When a student from a different 
     * faculty arrives they wait in (2) after increasing a wait counter. From now on all
     * students go wait in (3) until the room empties. When this happens a notifyAll is 
     * issued and a random thread is woken up. At this point in time 1 waiting thread is 
     * in (2) and the rest are in (3). The thread in (2) was the first to wait and therefore
     * we want it to be the first to get the room. 2 things can happen:
     * -If it is woken up then it resets the wait counter back to 0 and proceeds into the room.
     * 		On the way out it notify another thread to wake up. 
     * -A thread in (3) is woken up. If this happens when a thread in (2) is waiting we put it
     * 		back to sleep and keep waking up threads until the one in (2) is chosen.
     * After the thread in (2) is cleared the threads in (3) wake up 1 by 1 and break out of 
     * their loop. They are sent back at (1) where the process described above starts again.
     * This mechanism also prevents spurious wakeups to skip ahead in the line and get in early
     */
	public synchronized void getIn(Student st) {
		boolean b=false;
		while (true) {//(1)
			b=false;
			if (waitCount==0) {
				while (fac!=st.fac && fac!=null) {//(2)
					try {
						waitCount++;
						wait();
						waitCount--;
						b=true;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			else {
				while (true) {//(3)
					b=false;
					try {
						wait();
						if (waitCount==1) {
							notifyAll();
							wait();
							continue;
						}
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//update room details
		roomCount++;
		if (fac!=st.fac) {
			System.out.println(st.fac);
			fac=st.fac;		
		}
		
		if (b==true) {
			notify();
		}
	}
	
	/*
	 * When a student wants to get out the decrease the room occupancy counter
	 * After that if the room is empty then the student relinquishes the faculty's
	 * ownership of the room and notifies all waiting threads
	 */
	public synchronized void getOut(Student st) {
		roomCount--;
		if (waitCount>=0 && roomCount==0) {
			System.out.println("empty");
			lastFac=st.fac;
			fac=null;
			notifyAll();
		}
	}
}

class Student implements Runnable{
	//student attributes
	public int id;
	public Faculty fac;
	//monitor reference
	public Monitor mon;
	
	Student (Faculty f, Monitor m, int n){
		fac=f; mon=m; id=n;
	}
	
	@Override
	/*
	 * Each student tries to get in the room, sleep, get out and then sleep again
	 * before repeating the process
	 */
	public void run() {
		while (mon.simulationOn==true) {
			
			goToSleep(breakoutMon.k, 11);
			mon.getIn(this);
			goToSleep(breakoutMon.w, 11);
			mon.getOut(this);
		}
	}
	
	//sleep a random bounded amount of time
	public void goToSleep(int n, int m) {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(n, m*n));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

enum Faculty{
	Science,
	Art, 
	Engineering
}
