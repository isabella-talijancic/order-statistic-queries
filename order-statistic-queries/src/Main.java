import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Main is a Java class.
 * 
 * @author Isabella Talijancic (juu530)
 * @author Amalia Talijancic (fwn783)
 * UTSA CS 3343 - Project 2
 * Spring 2023
 */

//StoreLocator Class
public class Main {
    
    private static final int EARTH_RADIUS = 6371; // km
    
    public static void main(String[] args) {
        // Read in store data
        ArrayList<Store> whataburgerStores = readStoresFromFile("src/data/WhataburgerData.csv");
        ArrayList<Store> starbucksStores = readStoresFromFile("src/data/StarbucksData.csv");
        //"C:\Users\bella\git\order-statistic-queries\order-statistic-queries\src\data\StarbucksData.csv"
        
        // Read in query points
        ArrayList<Query> queries = readQueriesFromFile("src/data/Queries.csv");
        
        // Perform queries
        for (Query query : queries) {
            ArrayList<StoreDistance> distances = new ArrayList<>();
            // Compute distances from query point to all stores
            for (Store store : whataburgerStores) {
                double distance = computeHaversineDistance(query.latitude, query.longitude, store.latitude, store.longitude);
                distances.add(new StoreDistance(store, distance));
            }
            for (Store store : starbucksStores) {
                double distance = computeHaversineDistance(query.latitude, query.longitude, store.latitude, store.longitude);
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
                    double d1 = computeHaversineDistance(query.latitude, query.longitude, s1.latitude, s1.longitude);
                    double d2 = computeHaversineDistance(query.latitude, query.longitude, s2.latitude, s2.longitude);
                    return Double.compare(d1, d2);
                }
            });
            // Print close stores
            System.out.println("Query: " + query.latitude + ", " + query.longitude + ", " + query.numStores);
            for (Store store : closeStores) {
                double distance = computeHaversineDistance(query.latitude, query.longitude, store.latitude, store.longitude);
                System.out.println(store.name + " - " + store.address + " (" + distance + " km)");
            }
            System.out.println();
        }
    }
    
    private static ArrayList<Store> readStoresFromFile(String filename) {
        ArrayList<Store> stores = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String id = parts[0];
                String name = parts[1];
                String address = parts[2];
                double latitude = 0;
                double longitude = 0;
                try {
                    latitude = Double.parseDouble(parts[3]);
                    longitude = Double.parseDouble(parts[4]);
                } catch (NumberFormatException e) {
                    // Skip this line if latitude or longitude is not a valid double
                    continue;
                }
                stores.add(new Store(id, name, address, latitude, longitude));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stores;
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

    
    private static double computeHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;
        return distance;
    }
    
    private static StoreDistance randSelect(ArrayList<StoreDistance> distances, int left, int right, int k) {
        if (left == right) {
            return distances.get(left);
        }
        int pivotIndex = randPartition(distances, left, right);
        int pivotDistance = pivotIndex - left + 1;
        if (k == pivotDistance) {
            return distances.get(pivotIndex);
        } else if (k < pivotDistance) {
            return randSelect(distances, left, pivotIndex - 1, k);
        } else {
            return randSelect(distances, pivotIndex + 1, right, k - pivotDistance);
        }
    }

    private static int randPartition(ArrayList<StoreDistance> distances, int left, int right) {
        int pivotIndex = (int) (Math.random() * (right - left + 1) + left);
        double pivotValue = distances.get(pivotIndex).distance;
        swap(distances, pivotIndex, right);
        int storeIndex = left;
        for (int i = left; i < right; i++) {
            if (distances.get(i).distance <= pivotValue) {
                swap(distances, i, storeIndex);
                storeIndex++;
            }
        }
        swap(distances, storeIndex, right);
        return storeIndex;
    }

    private static void swap(ArrayList<StoreDistance> distances, int i, int j) {
        StoreDistance temp = distances.get(i);
        distances.set(i, distances.get(j));
        distances.set(j, temp);
    }

	}

	class Store {
		public String id;
		public String name;
		public String address;
		public double latitude;
		public double longitude;
		
		public Store(String id, String name, String address, double latitude, double longitude) {
		    this.id = id;
		    this.name = name;
		    this.address = address;
		    this.latitude = latitude;
		    this.longitude = longitude;
		}

	}

	class Query {
	public double latitude;
	public double longitude;
	public int numStores;
	
	public Query(double latitude, double longitude, int numStores) {
	    this.latitude = latitude;
	    this.longitude = longitude;
	    this.numStores = numStores;
	}

	}
	
	/**
	 * Represents a store and its distance from a query point.
	 */
	/*public*/class StoreDistance {

	    public final Store store;
	    public final double distance;

	    public StoreDistance(Store store, double distance) {
	        this.store = store;
	        this.distance = distance;
	    }

	//}

	
	private static int randPartition(ArrayList<StoreDistance> distances, int left, int right) {
	    int pivotIndex = new Random().nextInt(right - left + 1) + left;
	    double pivot = distances.get(pivotIndex).distance;
	    swap(distances, pivotIndex, right);
	    int storeIndex = left;
	    for (int i = left; i < right; i++) {
	        if (distances.get(i).distance < pivot) {
	            swap(distances, i, storeIndex);
	            storeIndex++;
	        }
	    }
	    swap(distances, storeIndex, right);
	    return storeIndex;
	}

	private static void swap(ArrayList<StoreDistance> distances, int i, int j) {
	    StoreDistance temp = distances.get(i);
	    distances.set(i, distances.get(j));
	    distances.set(j, temp);
	}
	}