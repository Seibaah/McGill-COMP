package assignment4;

import java.util.ArrayList;

public class MusicStore {
    //ADD YOUR CODE BELOW HERE
	//Creating 3 hashtables for appropriate search functions
	MyHashTable<String,Song> byTitle;
	MyHashTable<String, ArrayList<Song>> byArtist;
	MyHashTable<Integer ,ArrayList<Song>> byYear;
	
	ArrayList<String> artistList;
	
    //ADD YOUR CODE ABOVE HERE
    
    
    public MusicStore(ArrayList<Song> songs) {
        //ADD YOUR CODE BELOW HERE
    	//Creating 3 hashtables for year, artist and title keys
    	this.byTitle = new MyHashTable<String,Song>(songs.size());
    	this.byArtist = new MyHashTable<String, ArrayList<Song>>(songs.size());
        this.byYear = new MyHashTable<Integer, ArrayList<Song>>(songs.size());
        
        //We iterate through each song in the list we are given
        for (Song s: songs) {
        	
        	//Initializing the keys for ease of use later
        	String songKey=s.getTitle(), artistKey=s.getArtist();
        	int yearKey=s.getYear();
        	
        	//Adding to the title hashtable
        	this.byTitle.put(songKey, s);
        	
        	/*
        	 * Adding to the artist hashtables. If it's the artist's first song that we are adding we create a new array
        	 * List, otherwise we just add it to the existing one
        	 */
        	if (this.byArtist.get(artistKey)==null) {
        		ArrayList<Song> artistSongs = new ArrayList<Song>();
        		artistSongs.add(s);
        		this.byArtist.put(artistKey, artistSongs);
        	} else {
        		this.byArtist.get(artistKey).add(s);
        	}
        	
        	//Same principle as byArtist hashtable
        	if (this.byYear.get(yearKey)==null) {
        		ArrayList<Song> yearSongs = new ArrayList<Song>();
        		yearSongs.add(s);
        		this.byYear.put(yearKey, yearSongs);
        	} else {
        		this.byYear.get(yearKey).add(s);
        	}
        }
        //ADD YOUR CODE ABOVE HERE
    }
    
    /**
     * Add Song s to this MusicStore
     */
    public void addSong(Song s) {
        // ADD CODE BELOW HERE
    	/*
    	 * Same principle as the constructor, repetitive but required in case we add songs after
    	 * creating the music store for the 1st time.
    	 */
    	String songKey=s.getTitle(), artistKey=s.getArtist();
    	int yearKey=s.getYear();
    	this.byTitle.put(songKey, s);
    	
    	if (this.byArtist.get(artistKey)==null) {
    		ArrayList<Song> artistSongs = new ArrayList<Song>();
    		artistSongs.add(s);
    		this.byArtist.put(artistKey, artistSongs);
    	} else {
    		this.byArtist.get(artistKey).add(s);
    	}
    	
    	if (this.byYear.get(yearKey)==null) {
    		ArrayList<Song> yearSongs = new ArrayList<Song>();
    		yearSongs.add(s);
    		this.byYear.put(yearKey, yearSongs);
    	} else {
    		this.byYear.get(yearKey).add(s);
    	}
        // ADD CODE ABOVE HERE
    }
    
    /**
     * Search this MusicStore for Song by title and return any one song 
     * by that title 
     */
    public Song searchByTitle(String title) {
        //ADD CODE BELOW HERE
        return (Song) byTitle.get(title);
        //ADD CODE ABOVE HERE
    }
    
    /**
     * Search this MusicStore for song by `artist' and return an 
     * ArrayList of all such Songs.
     */
    public ArrayList<Song> searchByArtist(String artist) {
        //ADD CODE BELOW HERE
    	 return byArtist.get(artist);
    	 //ADD CODE ABOVE HERE
    }
    
    /**
     * Search this MusicSotre for all songs from a `year'
     *  and return an ArrayList of all such  Songs  
     */
    public ArrayList<Song> searchByYear(Integer year) {
        //ADD CODE BELOW HERE
        return byYear.get(year);
        //ADD CODE ABOVE HERE
        
    }
}
