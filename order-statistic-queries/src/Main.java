import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

/**
 * Main is a Java class.
 * 
 * UTSA CS 3343 - Project 2
 * Spring 2023
 * @author Isabella Talijancic (juu530)
 * @author Amalia Talijancic (fwn783)
 */
public class Main {

    private static final int EARTH_RADIUS = 6371; // km

    public static void main(String[] args) {
        // Read in store data
        ArrayList<Store> whataburgerStores = readStoresFromFile("src/data/WhataburgerData.csv");
        ArrayList<Store> starbucksStores = readStoresFromFile("src/data/StarbucksData.csv");

        // Read in query points
        ArrayList<Query> queries = readQueriesFromFile("src/data/Queries.csv");
        
        // Perform queries
        for (Query query : queries) {
            ArrayList<StoreDistance> distances = new ArrayList<>();
            // Compute distances from query point to all stores
            for (Store store : whataburgerStores) {
                double distance = Haversine.calculateDistance(query.latitude, query.longitude, store.latitude, store.longitude);
                distances.add(new StoreDistance(store, distance));
            }
            for (Store store : starbucksStores) {
                double distance = Haversine.calculateDistance(query.latitude, query.longitude, store.latitude, store.longitude);
                distances.add(new StoreDistance(store, distance));
            }
            // Use order statistic query to find farthest store we care about
            int k = query.numStores;
            if (k > distances.size()) {
                k = distances.size();
            }
       
            StoreDistance kthClosest = null;
            if (!distances.isEmpty()) {
                kthClosest = randSelect(distances, 0, distances.size() - 1, k);
            }

            // Find all stores that are at least as close as kthClosest
            ArrayList<Store> closeStores = new ArrayList<>();
            for (StoreDistance storeDistance : distances) {
                if (storeDistance.distance <= kthClosest.distance) {
                    closeStores.add(storeDistance.store);
                }
            }
            // Sort close stores by distance from query point
            Collections.sort(closeStores, new Comparator<Store>() {
                @Override
                public int compare(Store s1, Store s2) {
                    double d1 = Haversine.calculateDistance(query.latitude, query.longitude, s1.latitude, s1.longitude);
                    double d2 = Haversine.calculateDistance(query.latitude, query.longitude, s2.latitude, s2.longitude);
                    return Double.compare(d1, d2);
                }
            });
            // Print close stores
            System.out.println("The " + query.numStores + " closest Stores to (" + query.latitude + "," + query.longitude + "):");
            for (Store store : closeStores) {
                double distance = Haversine.calculateDistance(query.latitude, query.longitude, store.latitude, store.longitude);
                System.out.println("Store #" + store.id + ". " + store.address + ", " + store.city + ", " + store.state +", " + store.zipCode + ". - " + distance + " km)");
            }
            System.out.println();
        }
    }
    
    private static ArrayList<Query> readQueriesFromFile(String filename) {
        ArrayList<Query> queries = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                try {
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    int numStores = Integer.parseInt(parts[2]);
                    queries.add(new Query(latitude, longitude, numStores));
                } catch (NumberFormatException e) {
                    // Skip this line if latitude or longitude is not a valid double
                    continue;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return queries;
    }
    
    private static ArrayList<Store> readStoresFromFile(String filename) {
        ArrayList<Store> stores = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String id = parts[0];
                String address = parts[1];
                String city = parts[2];
                String state = parts[3];
                double zipCode = 0;
                try {
                    zipCode = Double.parseDouble(parts[4]);
                } catch (NumberFormatException e) {
                    // Skip this line if zipCode is not a valid double
                    continue;
                }
                double latitude = Double.parseDouble(parts[5]);
                double longitude = Double.parseDouble(parts[6]);
                stores.add(new Store(id, address, city, state, zipCode, latitude, longitude));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stores;
    }

            private static StoreDistance randSelect(ArrayList<StoreDistance> arr, int left, int right, int k) {
                if (left == right) {
                    return arr.get(left);
                }
                int pivotIndex = randomPartition(arr, left, right);
                int pivotDist = pivotIndex - left + 1;
                if (k == pivotDist) {
                    return arr.get(pivotIndex);
                } else if (k < pivotDist) {
                    return randSelect(arr, left, pivotIndex - 1, k);
                } else {
                    return randSelect(arr, pivotIndex + 1, right, k - pivotDist);
                }
            }

            private static int randomPartition(ArrayList<StoreDistance> arr, int left, int right) {
                int pivotIndex = new Random().nextInt(right - left + 1) + left;
                StoreDistance pivotValue = arr.get(pivotIndex);
                swap(arr, pivotIndex, right);
                int storeIndex = left;
                for (int i = left; i < right; i++) {
                    if (arr.get(i).distance < pivotValue.distance) {
                        swap(arr, i, storeIndex);
                        storeIndex++;
                    }
                }
                swap(arr, storeIndex, right);
                return storeIndex;
            }

            private static void swap(ArrayList<StoreDistance> arr, int i, int j) {
                StoreDistance temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
        }
        
        //Store Class
        class Store {
        	String id;
        	String address;
        	String city;
        	String state;
        	double zipCode;
        	double latitude;
        	double longitude;
        
        	public Store(String id, String address, String city, String state, double zipCode, double latitude, double longitude){
	        	this.id = id;
	        	this.address = address;
	        	this.city = city;
	        	this.state = state;
	        	this.zipCode = zipCode;
	        	this.latitude = latitude;
	        	this.longitude = longitude;
        	}
        }
        
        //Query Class
        class Query {
        	double latitude;
        	double longitude;
        	int numStores;
        	
        	public Query(double latitude, double longitude, int numStores) {
        		this.latitude = latitude;
        		this.longitude = longitude;
        		this.numStores = numStores;
        	}
        }
        
        //StoreDistance Class
        class StoreDistance{
        	Store store;
        	double distance;
        	
        	public StoreDistance(Store store, double distance) {
        		this.store = store;
        		this.distance = distance;
        	}
        }
        
        //Haversine Class
        class Haversine{
        	private static final int EARTH_RADIUS = 6371; // km

        	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        	    double dLat = Math.toRadians(lat2 - lat1);
        	    double dLon = Math.toRadians(lon2 - lon1);
        	    double lat1Rad = Math.toRadians(lat1);
        	    double lat2Rad = Math.toRadians(lat2);
        	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        	               Math.sin(dLon / 2) * Math.sin(dLon / 2) *
        	               Math.cos(lat1Rad) * Math.cos(lat2Rad);
        	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        	    return EARTH_RADIUS * c;
        	}
        }