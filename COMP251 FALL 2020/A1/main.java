import java.io.*;
import java.util.*;
 
public class main {     
 
     
    public static void main(String[] args) {
    	// TODO:build the hash table and insert keys using the insertKeyArray function
        int [] list1={70, 54, 19, 58, 46, 14, 67, 80, 3, 93, 47, 50, 74, 72, 85, 95, 86, 91, 81, 90},
    			list2={79, 13, 45, 64, 32, 95, 67, 27, 78, 18, 41, 69, 15, 29, 72, 57, 81, 50, 60, 14};
    	int A1=1023, A2=590;
    	 
    	Chaining chainMap1 = new Chaining(10, 0, A1);
    	Chaining chainMap2 = new Chaining(10, 0, A2);
    	int col0 = chainMap1.insertKeyArray(list1), col00 = chainMap2.insertKeyArray(list2),
    			col1=chainMap1.insertKey(66), col2=chainMap2.chain(99);
    	
    	System.out.println("Collision insert key 66 chain map 1: "+col1);
    	System.out.println("Collision insert key 99 chain map 2: "+col2);
    	
    	Open_Addressing openMap1 = new Open_Addressing(10, 0, A1);
    	Open_Addressing openMap2 = new Open_Addressing(10, 0, A2);
    	Open_Addressing openMap3 = new Open_Addressing(10, 0, -1);
    	int col3 = openMap1.insertKeyArray(list1), col4 = openMap2.insertKeyArray(list2),
    			col5=openMap1.removeKey(66), col6=openMap1.removeKey(70),
    			col7=openMap3.insertKey(0), col8=openMap3.removeKey(9);
    	
    	System.out.println("Collision insert key array open map 1: "+col3);
    	System.out.println("Collision insert key array open map 2: "+col4);
    	System.out.println("Open Map1 size: "+openMap1.m);
    	System.out.println("Collision remove key 66 open map 1: "+col5);
    	System.out.println("Collision remove key 70 open map 1: "+col6);
    	System.out.println("Collision insert key 0 open map 3: "+col7);
    	System.out.println("Collision remove key 9 open map 3: "+col8);
    }
 
    
}