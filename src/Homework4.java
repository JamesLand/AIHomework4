import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Homework4 {
	/**
	 * Contains code for Part 1 of HW 4 Parses the csv files, computes LMS of
	 * year 2015, then predicts values for 2016
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void regression() throws FileNotFoundException, UnsupportedEncodingException {
		// Read in two years
		LMSYear a = new LMSYear("2015.csv");
		LMSYear b = new LMSYear("2016.csv");

		// LMS for 1000 iterations
		for (int i = 0; i < 1000; i++) {
			a.LMS();
		}
		// Transfer weights
		b.setWeights(a.getWeights());

		// Create writers for output
		PrintWriter writer = new PrintWriter("2016predict.txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter("2016actual.txt", "UTF-8");
		PrintWriter writer3 = new PrintWriter("sse.txt", "UTF-8");

		// Write the predicted values and actual values
		for (int i = 0; i < b.getTmaxs().size(); i++) {
			writer.println(b.dotProduct(i));
			writer2.println(b.getTmaxs().get(i));
			System.out.println(b.dotProduct(i));
		}

		// Write the error
		for (int i = 0; i < a.getSSE().size(); i++) {
			writer3.println(a.getSSE().get(i));
		}

		// Close writers
		writer.close();
		writer2.close();
		writer3.close();
	}

	/**
	 * Contains the code for Part 2 of HW 4 Clusters the data from the input for
	 * k values ranging from 2 to 7 Writes output to text files
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void clustering() throws FileNotFoundException, UnsupportedEncodingException {
		// Create writer for error
		PrintWriter writer = new PrintWriter("KclustersSSE.txt", "UTF-8");

		// For different values of k
		for (int k = 2; k <= 7; k++) {
			// Read in input
			ClusterYear a = new ClusterYear("2015.csv");
			ClusterYear b = new ClusterYear("2016.csv");

			// Cluster the data from 2015 given k clusters
			a.cluster(k);

			// Predict the values for year 2016 and write the error
			writer.println(a.predict(b));
		}
		// Close the writer
		writer.close();
	}

	/**
	 * Main method that calls regression and clustering methods
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		regression();
		clustering();
	}
}

/**
 * Class to hold data for each year when doing LMS
 */
class LMSYear {

	// Alpha value
	private static final double ALPHA = 0.00001;

	// List of max temperatures
	private Vector<Double> tmaxs = new Vector<Double>();

	// List of minimum temperatures
	private Vector<Double> tmins = new Vector<Double>();

	// List of average temperatures
	private Vector<Double> tavgs = new Vector<Double>();

	// List of Sum squared errors
	private Vector<Double> sse = new Vector<Double>();

	// Array of weights
	private double[] weights;

	/**
	 * Parses a csv file and adds relevant information to class variables
	 * 
	 * @param loc
	 * @throws FileNotFoundException
	 */
	public LMSYear(String loc) throws FileNotFoundException {
		//set up file reader
		Scanner scanner = new Scanner(new File(loc));
		scanner.useDelimiter(",|\n");
		scanner.nextLine();
		int columnCount = 0;
		
		//Iterate through the file
		while (scanner.hasNext()) {
			double val;
			String s = scanner.next();
			//Attempt to convert the input to a double
			try {
				val = Double.parseDouble(s);
			} catch (Exception e) {
				continue;
			}
			//Store the input value based on which column we are on
			switch (columnCount) {
			case 3:
				tmaxs.add(val);
				columnCount++;
				break;
			case 4:
				tmins.add(val);
				columnCount++;
				break;
			case 5:
				tavgs.add(val);
				columnCount = 0;
				break;
			default:
				columnCount++;
			}
		}
		scanner.close();
		
		//initialize our weights
		weights = new double[3];
		for (int i = 0; i < 3; i++) {
			weights[i] = 1.0;
		}
	}

	// Iterate through each data row and adjust weights while calculating the
	// error
	public void LMS() {
		double sse = 0.0;
		for (int i = 0; i < tmaxs.size(); i++) {
			double error = tmaxs.get(i) - dotProduct(i);
			sse += Math.pow(error, 2);
			weights[0] = weights[0] + ALPHA * error;
			weights[1] = weights[1] + ALPHA * error * tmins.get(i);
			weights[2] = weights[2] + ALPHA * error * tavgs.get(i);
		}
		// Adds the new sse value to the vector
		this.sse.add(sse);
	}

	// Calculate the dot product of weights, minimum and average temperatures of
	// a given index
	public double dotProduct(int i) {
		return weights[0] + weights[1] * tmins.get(i) + weights[2] * tavgs.get(i);
	}

	/*
	 * Below are getters and setters for our class data
	 */
	public Vector<Double> getTmaxs() {
		return this.tmaxs;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	public Vector<Double> getSSE() {
		return this.sse;
	}

}

/**
 * Class to hold data for each year when doing K means clustering
 *
 */
class ClusterYear {
	//number of clusters
	private int k;

	//List of max temperatures
	private Vector<Double> tmaxs = new Vector<Double>();

	//List of average temperatures
	private Vector<Double> tavgs = new Vector<Double>();

	//List of minimum temperatures
	private Vector<Double> tmins = new Vector<Double>();

	//List of centers
	private Vector<Double> centers = new Vector<Double>();

	//List of clusters
	private Vector<Set<Integer>> clusters;

	/**
	 * Constructor method
	 * Parses through data and fills in appropriate class variables
	 * @param loc
	 * @throws FileNotFoundException
	 */
	public ClusterYear(String loc) throws FileNotFoundException {
		//Set up file input stream
		Scanner scanner = new Scanner(new File(loc));
		scanner.useDelimiter(",|\n");
		scanner.nextLine();
		int columnCount = 0;
		
		//Iterate through the stream
		while (scanner.hasNext()) {
			double val;
			String s = scanner.next();
			//attempt to parse the input as a double
			try {
				val = Double.parseDouble(s);
			} catch (Exception e) {
				continue;
			}
			
			//Allocate the value based on the column we are on
			switch (columnCount) {
			case 3:
				tmaxs.add(val);
				columnCount++;
				break;
			case 4:
				tmins.add(val);
				columnCount++;
				break;
			case 5:
				tavgs.add(val);
				columnCount = 0;
				break;
			default:
				columnCount++;
			}
		}
		scanner.close();
	}

	/**
	 * Cluster the data with k clusters
	 * @param k
	 */
	public void cluster(int k) {
		this.k = k;
		Random rand = new Random();
		clusters = new Vector<Set<Integer>>();
		
		//initialize clusters based on k
		for (int i = 0; i < k; i++) {
			clusters.add(new HashSet<Integer>());
		}

		//Initialize cluster centers randomly
		for (int i = 0; i < k; i++) {
			int randIndex = 0;
			//Loop to prevent duplicate centers
			do {
				randIndex = rand.nextInt(tmaxs.size());
			} while (centers.contains(distance(tavgs.get(randIndex), tmins.get(randIndex))));
			
			centers.add(distance(tavgs.get(randIndex), tmins.get(randIndex)));
			clusters.get(i).add(randIndex);
			System.out.println(("Initial center: " + centers.get(i)));
		}
		
		//Add data to clusters based on the closest center to the data value
		for (int i = 0; i < tavgs.size(); i++) {
			clusters.get(getClosestCenterIndex(distance(tavgs.get(i), tmins.get(i)))).add(i);
		}

		//Create queues that will hold data that needs moving
		//the first integer is the source, the second is the value
		Vector<HashMap<Integer, Integer>> queues = new Vector<HashMap<Integer, Integer>>();
		for (int i = 0; i < k; i++) {
			queues.add(new HashMap<Integer, Integer>());
		}
		//Track movements
		int movements = 0;
		int iterations = 0;
		
		//Recompute Centers and move data appropriately until convergence or 1000 iterations
		do {
			movements = 0;
			iterations++;
			recomputeCenters();
			for (int i = 0; i < k; i++) {
				for (Integer a : clusters.get(i)) {
					int index = getClosestCenterIndex(distance(tavgs.get(a),tmins.get(a)));
					//If the index of clusters are not equal, this means a change of clusters
					if (index != i) {
						queues.get(index).put(i, a);
						movements++;
					}
				}
			}
			
			//Move any data that needs to change clusters
			for (int clusterNum = 0; clusterNum < k; clusterNum++) {
				for (Integer originalCluster : queues.get(clusterNum).keySet()) {
					clusters.get(originalCluster).remove(queues.get(clusterNum).get(originalCluster));
					clusters.get(clusterNum).add(queues.get(clusterNum).get(originalCluster));
				}
				queues.get(clusterNum).clear();
			}

		} while (movements > 0 || iterations > 1000);

	}

	/**
	 * Finds the index of the closest center that matches the given value
	 * @param in
	 * @return the index of the closest center
	 */
	public int getClosestCenterIndex(double in) {
		double minDistance = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < centers.size(); i++) {
			double distance = Math.abs(in - centers.get(i));
			if (distance < minDistance) {
				minDistance = distance;
				index = i;
			}
		}
		return index;
	}

	/**
	 * Iterates through all centers and recalculates them based on the average
	 */
	public void recomputeCenters() {
		for (int i = 0; i < centers.size(); i++) {
			double sum = 0;
			for (Integer a : clusters.get(i)) {
				sum += distance(tavgs.get(a), tmins.get(a));
			}
			centers.set(i, sum / clusters.get(i).size());
		}
	}

	/**
	 * Averages the max temperatures given a cluster index
	 * @param index
	 * @return the average MAX temperature of the cluster
	 */
	public double averageCluster(int index) {
		double sum = 0;
		for (Integer a : clusters.get(index)) {
			sum += tmaxs.get(a);
		}
		return sum / clusters.get(index).size();
	}

	/**
	 * Predict values for another year based on this year's cluster data
	 * Writes the predicted and actual values to a text file
	 * @param b The year we are predicting for
	 * @return the sum squared error
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public double predict(ClusterYear b) throws FileNotFoundException, UnsupportedEncodingException {
		double sse = 0.0;
		PrintWriter writer = new PrintWriter("KclusterPredict-" + Integer.toString(k) + ".txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter("KclusterActual-" + Integer.toString(k) + ".txt", "UTF-8");
		for (int i = 0; i < b.getTmaxs().size(); i++) {
			int index = getClosestCenterIndex(distance(b.getTavgs().get(i), b.getTmins().get(i)));
			sse += Math.pow(b.getTmaxs().get(i) - averageCluster(index), 2);
			writer.println(averageCluster(index));
			writer2.println(b.getTmaxs().get(i));
		}
		writer.close();
		writer2.close();
		return sse;
	}
	
	/**
	 * Calculates the euclidean distance between two numbers
	 * @param a
	 * @param b
	 * @return
	 */
	public double distance(double a, double b){
		return Math.sqrt(Math.pow(a, 2) - Math.pow(b, 2));
	}
	
	/*
	 * Below are getters for class variables
	 */

	public Vector<Double> getTmaxs() {
		return this.tmaxs;
	}

	public Vector<Double> getTavgs() {
		return this.tavgs;
	}
	
	public Vector<Double> getTmins() {
		return this.tmins;
	}

	public Vector<Set<Integer>> getClusters() {
		return clusters;
	}

}