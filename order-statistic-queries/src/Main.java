import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/**
 * Main contains all necessary classes and determines the specified number of stores
 * located more closely to the given coordinates using the Order Statistic Query algorithm, Rand-Select algorithm, and more
 * 
 * UTSA CS 3343 - Project 2
 * Spring 2023
 * @author Isabella Talijancic (juu530)
 * @author Amalia Talijancic (fwn783)
 */
public class Main 
{
	//Conversion derived from Professor Gibson's video instructions
	static double radiusOfEarthInMiles = 3958.8;

    public static void main(String[] args) 
    {
        // Reading in individual store information and queries through CSVs
        ArrayList<Store> whataburgerStores = readInCSVStores( "src/data/WhataburgerData.csv" );
        ArrayList<Store> starbucksStores = readInCSVStores( "src/data/StarbucksData.csv" );

        ArrayList<Query> queries = readInCSVQueries( "src/data/Queries.csv" );
        
        // Completing the given queries specified while calculating the distance between each coordinate and stores
        for ( Query query : queries ) 
        {
            ArrayList<StoreDist> distances = new ArrayList<>();
            
            for ( Store store : whataburgerStores ) 
            {
                double distance = Haversine.calcDist( query.latitude, query.longitude, store.latitude, store.longitude );
                distances.add( new StoreDist( store, distance ) );
            }
            
            for ( Store store : starbucksStores ) 
            {
            	double distance = Haversine.calcDist( query.latitude, query.longitude, store.latitude, store.longitude );
                distances.add( new StoreDist( store, distance ) );
            }
            
            // Order Statistic Query algorithm, determining the stores that are closest (isCloser), and 
            // using Collection.Sort() to sort them by their distance
            int lookingAt;
            lookingAt = query.numStores;
            
            if ( lookingAt > distances.size( ) ) 
            {
            	
                lookingAt = distances.size( );
            
            }
       
            StoreDist isCloser = null;
            
            if ( !distances.isEmpty( ) ) 
            {
            	
                isCloser = randSelect( distances, 0, distances.size( ) - 1, lookingAt );
           
            }

           
            ArrayList<Store> nearestStores = new ArrayList<>();
            for ( StoreDist storeDist : distances )
            {
                if ( storeDist.distance <= isCloser.distance )
                {
                    nearestStores.add( storeDist.store );
                }
            }
            
            Collections.sort( nearestStores, new Comparator<Store>()
            {
                @Override
                public int compare( Store s1, Store s2 ) {
                    double d1 = Haversine.calcDist( query.latitude, query.longitude, s1.latitude, s1.longitude );
                    double d2 = Haversine.calcDist( query.latitude, query.longitude, s2.latitude, s2.longitude );
                    
                    return Double.compare(d1, d2);
               
                }
                
            } );
            
            // Print out nearest stores
            System.out.println( "The " + query.numStores + " closest Stores to (" + query.latitude + "," + query.longitude + "):" );
            for ( Store store : nearestStores )
            {
                double distance = Haversine.calcDist( query.latitude, query.longitude, store.latitude, store.longitude );
                String formattedDistance = String.format( "%.2f", distance );
                System.out.println( "Store #" + store.id + ". " + store.address + ", " + store.city + ", " + store.state +", " + store.zipCode + ". - " + formattedDistance + " miles)" );
            }
            System.out.println();
        }
    }
    
    /**
     * 
     * readInCSVStores() method takes in the variable 
     * @param filename to 
     * @return stores
     * in order to split and parse through for needed information 
     * (i.e. id, address, etc.) while including a try/catch for 
     * potentially any invalid zipCode, for instance 
     */
    private static ArrayList<Store> readInCSVStores( String filename ) {
        ArrayList<Store> stores = new ArrayList<>();
        try {
            Scanner scanner = new Scanner( new File( filename ) );
            while ( scanner.hasNextLine() )
            {
                String line = scanner.nextLine();
                //String[] parts = line.split(",");
                String[] parts = line.split( ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" );
                String id = parts[0];
                String address = parts[1];
                String city = parts[2];
                String state = parts[3];
                double zipCode = 0;
                try
                {
                    zipCode = Double.parseDouble( parts[4] );
                } catch ( NumberFormatException e )
                {
                    // In the case that zipCode is invalid, continue
                    continue;
                }
                double latitude = Double.parseDouble( parts[5] );
                double longitude = Double.parseDouble( parts[6] );
                stores.add( new Store( id, address, city, state, zipCode, latitude, longitude ) );
            }
            scanner.close();
        } catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        return stores;
    }
    
		    /**
		     * 
		     * readInCSVQueries() method takes in variables 
		     * @param filename to 
		     * @return queries 
		     * in order to split and parse through for needed information 
		     * (i.e. numStores, latitude, etc.) while including a try/catch for any 
		     * coordinate-values that may be invalid
		     */
		    private static ArrayList<Query> readInCSVQueries( String filename )
		    {
		        ArrayList<Query> queries = new ArrayList<>();
		        
		        try
		        {
		        	
		            Scanner scanner = new Scanner( new File( filename ) );
		            
		            while ( scanner.hasNextLine() )
		            {
		            	
		                String line = scanner.nextLine();
		                String[] parts = line.split(",");
		                
		                try
		                {
		                	
		                    double latitude = Double.parseDouble( parts[0] );
		                    double longitude = Double.parseDouble( parts[1] );
		                    int numStores = Integer.parseInt( parts[2] );
		                    
		                    queries.add( new Query( latitude, longitude, numStores ) );
		                
		                } 
		                
		                catch ( NumberFormatException e )
		                
		                {
		               
		                    continue;
		               
		                }
		            }
		            
		            scanner.close();
		        } 
		        
		        catch ( FileNotFoundException e )
		        
		        {
		        	
		            e.printStackTrace();
		      
		        }
		        
		        return queries;
		    }

		    /**
		     * randSelect() takes in variables 
		     * @param arr
		     * @param left
		     * @param right and
		     * @param k
		     * to
		     * @return randSelect()
		     * in order for the StoreDist ArrayList to be partitioned to 
		     * search and identify the "k-th smallest element"
		     */
            private static StoreDist randSelect( ArrayList<StoreDist> arr, int left, int right, int k )
            {
            	
                if ( left == right )
                {
                    
                	return arr.get( left );
               
                }
                int pivotIndex = randomPartition( arr, left, right );
                int pivotDist = pivotIndex - left + 1;
                
                if ( k == pivotDist )
                {
                	
                    return arr.get( pivotIndex );
               
                }
                
                else if ( k < pivotDist )
                {
                	
                    return randSelect( arr, left, pivotIndex - 1, k );
              
                }
                
                else 
                {
                	
                    return randSelect( arr, pivotIndex + 1, right, k - pivotDist );
               
                }
            }

            /**
             * 
             * randomPartition() method takes in variables 
             * @param arr
             * @param left and 
             * @param right
             * to 
             * @return storeIndex
             * in order for the array to be partitioned into two sub-arrays
             */
            private static int randomPartition( ArrayList<StoreDist> arr, int left, int right )
            {
                int pivotIndex = new Random().nextInt( right - left + 1 ) + left;  
                StoreDist pivotValue = arr.get( pivotIndex );
                
                swap( arr, pivotIndex, right );
                
                int storeIndex = left;
                
                for ( int i = left; i < right; i++ )
                {
                	
                    if ( arr.get(i).distance < pivotValue.distance )
                    {
                       
                    	swap( arr, i, storeIndex );
                        storeIndex++;
                   
                    }
                }
                
                swap( arr, storeIndex, right );
                
                return storeIndex;
            }

            /**
             * 
             * swap() is used to store variables 
             * @param arr
             * @param i and 
             * @param j 
             * to swap elements in the StoreDist ArrayList to be in order
             */
            private static void swap( ArrayList<StoreDist> arr, int i, int j )
            {
                StoreDist temp = arr.get( i );
                arr.set( i, arr.get( j ) );
                arr.set( j, temp );
            }
        }

		/**
		 * 
		 * Haversine Class is used here to calculate the distances between two specified points
		 * @param firstLat
		 * @param firstLong
		 * @param secondLat and 
		 * @param secondLong
		 * 
		 * @return radiusOfEarthInMiles * haver2
		 *
		 */
		class Haversine
		{
			static double radiusOfEarthInMiles = 3958.8;
		
			public static double calcDist( double firstLat, double firstLong, double secondLat, double secondLong ) 
			{
				
			    double diffLat = Math.toRadians(secondLat - firstLat);
			    double diffLong = Math.toRadians(secondLong - firstLong);
			    double firstLatRad = Math.toRadians(firstLat);
			    double secLatRad = Math.toRadians(secondLat);
			   
			    double haver1 = Math.sin( diffLat / 2 ) * Math.sin( diffLat / 2 ) + Math.sin( diffLong / 2 ) * Math.sin( diffLong / 2 ) * Math.cos( firstLatRad ) * Math.cos( secLatRad );
			    double haver2 = 2 * Math.atan2( Math.sqrt( haver1 ), Math.sqrt( 1 - haver1 ) );
			   
			    return radiusOfEarthInMiles * haver2;
			}
		}
        
        /**
         * 
         * Store Class used to store variables representing parts of each line in the Whataburger and Starbucks CSVs 
         * so as to organize information, notably the coordinates, and print all data relating to queries once finished
         * using variables 
         * @param id
         * @param address
         * @param city
         * @param state
         * @param zipCode
         * @param latitude
         * @param longitude
         * 
         * Referred to Professor Gibson's video instructions
         *
         */
        class Store 
        {	
        	String id;
        	String address;
        	String city;
        	String state;
        	double zipCode;
        	double latitude;
        	double longitude;  
        	
        	public Store( String id, String address, String city, String state, double zipCode, double latitude, double longitude )
        	{
	        	this.id = id;
	        	this.address = address;
	        	this.city = city;
	        	this.state = state;
	        	this.zipCode = zipCode;
	        	this.latitude = latitude;
	        	this.longitude = longitude;
        	}
        }
        
        /**
         * 
         * StoreDist Class is used to store the distance in variables 
         * @param store and 
         * @param distance
         *
         */
        class StoreDist
        {
        	Store store;
        	double distance;
        	
        	public StoreDist( Store store, double distance ) 
        	{
        		this.store = store;
        		this.distance = distance;
        	}
        }
        
        /**
         * 
         * Query Class is used to store the queries in variables 
         * @param latitude
         * @param longitude and 
         * @param numStores
         *
         */
        class Query 
        {
        	double latitude;
        	double longitude;
        	int numStores;
        	
        	public Query( double latitude, double longitude, int numStores ) 
        	{
        		this.latitude = latitude;
        		this.longitude = longitude;
        		this.numStores = numStores;
        	}
        }