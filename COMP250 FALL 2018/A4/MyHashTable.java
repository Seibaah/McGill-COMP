package assignment4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class MyHashTable<K,V> implements Iterable<HashPair<K,V>>{
    // num of entries to the table
    private int numEntries;
    // num of buckets 
    private int numBuckets;
    // load factor needed to check for rehashing 
    private static final double MAX_LOAD_FACTOR = 0.75;
    // ArrayList of buckets. Each bucket is a LinkedList of HashPair
    private ArrayList<LinkedList<HashPair<K,V>>> buckets; 
    
    // constructor
    public MyHashTable(int initialCapacity) {
        // ADD YOUR CODE BELOW THIS
    	/*
    	 * We set the parameters, and within each slot of the ArrayList we create a linkedList
    	 */
    	this.numBuckets=initialCapacity;
    	numEntries=0;
    	buckets = new ArrayList<LinkedList<HashPair<K,V>>>(initialCapacity);
    	for (int i=0; i<initialCapacity; i++) {
    		buckets.add(i, new LinkedList<HashPair<K,V>>());
    	}
        //ADD YOUR CODE ABOVE THIS
    }
    
    public int size() {
        return this.numEntries;
    }
    
    public int numBuckets() {
        return this.numBuckets;
    }
    
    /**
     * Returns the buckets variable. Useful for testing  purposes.
     */
    public ArrayList<LinkedList< HashPair<K,V> > > getBuckets(){
        return this.buckets;
    }
    /**
     * Given a key, return the bucket position for the key. 
     */
    public int hashFunction(K key) {
        int hashValue = Math.abs(key.hashCode())%this.numBuckets;
        return hashValue;
    }
    /**
     * Takes a key and a value as input and adds the corresponding HashPair
     * to this HashTable. Expected average run time  O(1)
     */
    public V put(K key, V value) {
        //  ADD YOUR CODE BELOW HERE
    	//Getting the compressed address
        int bucketAddress=hashFunction(key);
        
        //Creating a new pair
        HashPair<K,V> KVpair= new HashPair<K,V>(key, value);
        
        /*We iterate through each pair in each linkedList in each slot of the arrayList
         * If we find a match, we overwrite and return the old one.
         * If we never find a duplicate we just add the new element and update the number 
         * of entries and return null.
         */
        for (HashPair<K,V> h: this.buckets.get(bucketAddress)) {
        	if (h.getKey().equals(key)) {
        		V temp=h.getValue();
        		h.setValue(value);
        		return temp;
        	}
        }
        this.buckets.get(bucketAddress).add(KVpair);
        this.numEntries++;
        float currLoadFactor=(this.numEntries)/(this.numBuckets);
        if (currLoadFactor>0.75) {
        	this.rehash();
        }
        return null;
        //  ADD YOUR CODE ABOVE HERE
    }
    
    
    /**
     * Get the value corresponding to key. Expected average runtime = O(1)
     */
    
    public V get(K key) {
        //ADD YOUR CODE BELOW HERE
    	int hashAddress=hashFunction(key);
    	
    	//We iterate through all the pairs in the linkedList and return a match is there is one, else null
    	for (HashPair<K, V> h: this.buckets.get(hashAddress)) {
    		if (h.getKey().equals(key)) {
    			return (V) h.getValue();
    		}
    	}
    	return null;
        //ADD YOUR CODE ABOVE HERE
    }
    
    /**
     * Remove the HashPair corresponding to key . Expected average runtime O(1) 
     */
    public V remove(K key) {
        //ADD YOUR CODE BELOW HERE
    	int hashAddress=hashFunction(key);
    	
    	/*
    	 * With an iterator we go through each pair in a linkedList. If we find a match
    	 * we remove it, return it and update the number of entries.
    	 * Else return null.
    	 */
        Iterator<HashPair<K, V>> itr = this.buckets.get(hashAddress).iterator();
    	while (itr.hasNext()) {
    		HashPair<K, V> curr=itr.next();
    		if (curr.getKey().equals(key)) {
    			itr.remove();
    			this.numEntries--;
    			return curr.getValue();
    		}
    	}
    	return null;
        //ADD YOUR CODE ABOVE HERE
    }
    
    // Method to double the size of the hashtable if load factor increases
    // beyond MAX_LOAD_FACTOR.
    // Made public for ease of testing.
    
    public void rehash() {
        //ADD YOUR CODE BELOW HERE
    	/*
    	 * Create a new arrayList with the new size, update the number of buckets of *this and add a linkedList
    	 * in each slot of the new arrayList
    	 */
        int newSize=2*(this.numBuckets());
        ArrayList<LinkedList<HashPair<K,V>>> newBuckets = new ArrayList<LinkedList<HashPair<K,V>>>(newSize);
        this.numBuckets=newSize;
        for (int i=0; i<newSize; i++) {
			newBuckets.add(i, new LinkedList<HashPair<K,V>>());
		}
        
        //Create 2 nested iterator objects. It'll visit each linkedList and each element in them subsequently.
        Iterator<LinkedList<HashPair<K, V>>> bucketsItr = this.buckets.iterator();
        while (bucketsItr.hasNext()) {
        	LinkedList<HashPair<K, V>> currBucket=bucketsItr.next();
            Iterator<HashPair<K, V>> pairItr = currBucket.iterator();
            while (pairItr.hasNext()) {
            	
            	//Using a private helper method we rehash the pairs in a new bigger array.
            	HashPair<K, V> currPair=pairItr.next();
            	//pairItr.remove();
            	this.numEntries--;
            	K currKey=currPair.getKey();
            	V currVal=currPair.getValue();
            	this.put2(currKey, currVal, newBuckets);//maybe put2 should ret the modded array. Remember in case of problems.
            }

        }
        
        //Replacing the old array with the rehashed
        this.buckets=newBuckets;
    	//ADD YOUR CODE ABOVE HERE
    }
    
    /*
     * Private method, works same as put but takes an arrayList as extra input. 
     * Puts the formed pair in it instead
     */
    private void put2(K key, V value, ArrayList<LinkedList<HashPair<K,V>>> newBuckets) {
        
        int bucketAddress=hashFunction(key);
        HashPair<K,V> KVpair= new HashPair<K,V>(key, value);
        for (HashPair<K,V> h: newBuckets.get(bucketAddress)) {//crash
        	if (h.getKey().equals(key)) {
        		h.setValue(value);
        	}
        }
        newBuckets.get(bucketAddress).add(KVpair);
        this.numEntries++;
        }
    
    
    /**
     * Return a list of all the keys present in this hashtable.
     */
    
    public ArrayList<K> keys() {
        //ADD YOUR CODE BELOW HERE
    	//Creating an array to store all the keys
    	ArrayList<K> keysList= new ArrayList<K>(this.numEntries);
    	
    	//Create 2 nested iterator objects. It'll visit each linkedList and each element in them subsequently.
        Iterator<LinkedList<HashPair<K, V>>> bucketsItr = this.buckets.iterator();
        while (bucketsItr.hasNext()) {
        	LinkedList<HashPair<K, V>> currBucket=bucketsItr.next();
            Iterator<HashPair<K, V>> pairItr = currBucket.iterator();
            while (pairItr.hasNext()) {
            	
            	//We get each HashPair's key and store it in an arrayList
            	HashPair<K, V> currPair=pairItr.next();
            	K currKey=currPair.getKey();
            	keysList.add(currKey);
            }

        }
        return keysList;
        //ADD YOUR CODE ABOVE HERE
    }
    
    /**
     * Returns an ArrayList of unique values present in this hashtable.
     * Expected average runtime is O(n)
     */
    public ArrayList<V> values() {
        //ADD CODE BELOW HERE
    	//Creating an array to store all the values
    	ArrayList<V> valuesList= new ArrayList<V>(this.numEntries);
    	
    	//Create 2 nested iterator objects. It'll visit each linkedList and each element in them subsequently.
        Iterator<LinkedList<HashPair<K, V>>> bucketsItr = this.buckets.iterator();
        while (bucketsItr.hasNext()) {
        	LinkedList<HashPair<K, V>> currBucket=bucketsItr.next();
            Iterator<HashPair<K, V>> pairItr = currBucket.iterator();
            while (pairItr.hasNext()) {
            	
            	//We get each HashPair's key and store it in an arrayList
            	HashPair<K, V> currPair=pairItr.next();
            	V currValue=currPair.getValue();
            	valuesList.add(currValue);
            }

        }
        return valuesList;
        //ADD CODE ABOVE HERE
    }
    
    
    @Override
    public MyHashIterator iterator() {
        return new MyHashIterator();
    }
    
    
    private class MyHashIterator implements Iterator<HashPair<K,V>> {
        private LinkedList<HashPair<K,V>> entries;
        
        private MyHashIterator() {
            //ADD YOUR CODE BELOW HERE
            
            //ADD YOUR CODE ABOVE HERE
        }
        
        @Override
        public boolean hasNext() {
            //ADD YOUR CODE BELOW HERE
            return false;// remove
            //ADD YOUR CODE ABOVE HERE
        }
        
        @Override
        public HashPair<K,V> next() {
            //ADD YOUR CODE BELOW HERE
            return null;//remove
            //ADD YOUR CODE ABOVE HERE
        }
        
    }
}
