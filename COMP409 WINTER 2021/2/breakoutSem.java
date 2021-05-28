import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Semaphore;

public class breakoutSem {
	//n->simulation time, k->waiting sleep time, w->breakout room sleep time
	public static int n, k, w;
    public static ArrayList<Thread> threads;
    
    //s-> next in line semaphore, facS-> faculty semaphore, roomS->room capacity semaphore
    public static Semaphore s;
    public static Semaphore facS;
    public static Semaphore roomS;
    
    //current faculty owning the room
    public volatile static Faculty fac;
    
    //conditional on the simulation termination
    public static volatile boolean simulationOn;
	
	public static void main(String[] args) {
		
		n = Integer.parseInt(args[0]); k = Integer.parseInt(args[1]); w = Integer.parseInt(args[2]);
		
		//initialization step
		//starting state for the room
		long t0=System.currentTimeMillis();
		simulationOn=true;
		fac=null;
		
		//semaphores creation
		s=new Semaphore(1, true);
		facS=new Semaphore(1, true);
		roomS=new Semaphore(4, true);
		
		//threads creation
		threads = new ArrayList<Thread>();
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Science, i)));
		}
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Art, i+4)));
		}
		for (int i=0; i<4; i++) {
			threads.add(new Thread(new Student(Faculty.Engineering, i+8)));
		}
		
		//shuffle to adjust for spawn order bias
		Collections.shuffle(threads);
		
		//start the threads
		for (Thread t : threads) {
			t.start();
		}
		
		//simulation time based termination code
		while (System.currentTimeMillis()-t0<n*1000) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		simulationOn=false;
	}
}

class Student implements Runnable{
	//student attributes
	public int id;
	public Faculty fac;
	
	Student (Faculty f, int i){
		fac=f; id=i;
	}
	
	@Override
	/*
	 * Each student tries to get in the room, sleep, get out and then sleep again
	 * before repeating the process
	 */
	public void run() {
		while (breakoutSem.simulationOn==true) {
			
			goToSleep(breakoutSem.k, 11);
			
			/*
			 * student tries to acquire a semaphore that simulates 
			 * being at the head of the queue. Enforces fifo ordering
			 */
			try {
				breakoutSem.s.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*
			 * If the faculty owning the room isn't yours then wait on
			 * a faculty semaphore. You acquire it when the room is 
			 * relinquished by the previous owners
			 */
			if (breakoutSem.fac!=fac) {
				try {
					breakoutSem.facS.acquire();
					breakoutSem.fac=fac;
					System.out.println(fac);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			/*
			 * Try to get in the room. It has capacity four.
			 */
			try {
				breakoutSem.roomS.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*
			 * Once in let the next student in line try to get in and go to sleep
			 */
			breakoutSem.s.release();
			goToSleep(breakoutSem.w, 11);

			/*
			 * If you are the last student leaving the relinquish ownership
			 * of the room by releasing the faculty semaphore.
			 */
			if (breakoutSem.roomS.availablePermits()==3) {
				System.out.println("empty");
				breakoutSem.facS.release();
			}
			
			//Release a count on the room capacity semaphore
			breakoutSem.roomS.release();
					
		}
	}
	
	//sleep a thread for a random bounded time
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
