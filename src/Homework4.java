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
	
	public static void regression() throws FileNotFoundException, UnsupportedEncodingException{
		LMSYear a = new LMSYear("2015.csv");
		LMSYear b = new LMSYear("2016.csv");		
		
		for (int i = 0; i< 1000;i++){
			a.LMS();
		}
		b.setWeights(a.getWeights());
		
		PrintWriter writer = new PrintWriter("2016predict.txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter("2016actual.txt", "UTF-8");
		PrintWriter writer3 = new PrintWriter("sse.txt", "UTF-8");
		
		for (int i = 0; i< b.getTmaxs().size();i++){
			writer.println(b.dotProduct(i));
			writer2.println(b.getTmaxs().get(i));
			System.out.println(b.dotProduct(i));
		}		
		
		for (int i = 0; i < a.getSSE().size(); i++){
			writer3.println(a.getSSE().get(i));
		}
		
		writer.close();
		writer2.close();
		writer3.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		//regression();
		PrintWriter writer = new PrintWriter("KclustersSSE.txt", "UTF-8");
		for (int k = 2; k <= 7; k++) {
			ClusterYear a = new ClusterYear("2015.csv");
			ClusterYear b = new ClusterYear("2016.csv");
			a.cluster(k);
			writer.println(a.predict(b));
		}
		writer.close();
		
	}

}

class LMSYear{
	
	private static final double ALPHA = 0.00001;
	
	private Vector<Double> tmaxs = new Vector<Double>();
	
	private Vector<Double> tmins = new Vector<Double>();
	
	private Vector<Double> tavgs = new Vector<Double>();
	
	private Vector<Double> sse = new Vector<Double>();

	private double[] weights;
	
	public Vector<Double> getTmaxs(){
		return this.tmaxs;
	}
	
	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	
	public Vector<Double> getSSE(){
		return this.sse;
	}
	
	public LMSYear(String loc) throws FileNotFoundException{
		 Scanner scanner = new Scanner(new File(loc));
	        scanner.useDelimiter(",|\n");
	        scanner.nextLine();
	        int columnCount = 0;
	        while(scanner.hasNext()){
	        	double val;
	        	String s = scanner.next();
	        	try{
	        		val = Double.parseDouble(s);
	        	} catch(Exception e){
	        		continue;
	        	}
	        	switch (columnCount){
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
	        weights = new double[3];
	        
	        for (int i = 0;i< 3;i++){
	        	weights[i] = 1.0;
	        }
	}
	
	public void LMS(){
		double sse = 0.0;
		for (int i = 0; i< tmaxs.size();i++){
			double error = tmaxs.get(i) - dotProduct(i);
			sse += Math.pow(error, 2);
			weights[0] = weights[0] + ALPHA*error;
			weights[1] = weights[1] + ALPHA*error*tmins.get(i);
			weights[2] = weights[2] + ALPHA*error*tavgs.get(i);
		}
		//Adds the new sse value to the vector
		this.sse.add(sse);
	}
	
	public double dotProduct(int i){
		return weights[0] + weights[1]*tmins.get(i) + weights[2]*tavgs.get(i);
	}
	
}

class ClusterYear{
	private int k;
	
	private Vector<Double> tmaxs = new Vector<Double>();
	
	private Vector<Double> tavgs = new Vector<Double>();
	
	private Vector<Double> sse = new Vector<Double>();
	
	private Vector<Double> centers = new Vector<Double>();
	
	private Vector<Set<Integer>> clusters;
	
	public Vector<Double> getTmaxs(){
		return this.tmaxs;
	}
	
	public Vector<Double> getTavgs(){
		return this.tavgs;
	}
	
	public Vector<Double> getSSE(){
		return this.sse;
	}
	
	public Vector<Set<Integer>> getClusters() {
		return clusters;
	}

	public void setClusters(Vector<Set<Integer>> clusters) {
		this.clusters = clusters;
	}
	
	public ClusterYear(String loc) throws FileNotFoundException{
		 Scanner scanner = new Scanner(new File(loc));
	        scanner.useDelimiter(",|\n");
	        scanner.nextLine();
	        int columnCount = 0;
	        while(scanner.hasNext()){
	        	double val;
	        	String s = scanner.next();
	        	try{
	        		val = Double.parseDouble(s);
	        	} catch(Exception e){
	        		continue;
	        	}
	        	switch (columnCount){
		        	case 3:
		        		tmaxs.add(val);
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
	
	public void cluster(int k){
		this.k = k;
		Random rand = new Random();

		clusters = new Vector<Set<Integer>>();
		
		for (int i = 0; i < k; i++) {
			clusters.add(new HashSet<Integer>());
		}
		
		for (int i = 0; i < k; i++){
			//TODO check for duplicates
			int randIndex = rand.nextInt(tmaxs.size());
			centers.add(tavgs.get(randIndex));
			clusters.get(i).add(randIndex);
			System.out.println(("Initial center: "+centers.get(i)));
		}
		for (int i = 0; i< tavgs.size();i++){
			clusters.get(getClosestCenterIndex(tavgs.get(i))).add(i);
		}
		
		//the first integer is the source, the second is the value
		Vector<HashMap<Integer, Integer>> queues = new Vector<HashMap<Integer, Integer>>();
		for (int i = 0; i < k;i++){
			queues.add(new HashMap<Integer, Integer>());
		}
		int movements = 0;
		do{
			movements = 0;
			recomputeCenters();
			for (int i = 0; i < k; i++){
				for (Integer a : clusters.get(i)){
					int index = getClosestCenterIndex(tavgs.get(a));
					if (index != i){
						queues.get(index).put(i, a);
						movements++;
					}
				}
			}
			
			for (int clusterNum = 0; clusterNum < k; clusterNum++){
				for (Integer originalCluster : queues.get(clusterNum).keySet()){
					clusters.get(originalCluster).remove(queues.get(clusterNum).get(originalCluster));
					clusters.get(clusterNum).add(queues.get(clusterNum).get(originalCluster));
				}
				queues.get(clusterNum).clear();
			}
			
		}while(movements > 0);
		
	}

	public int getClosestCenterIndex(double in){
		double minDistance = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < centers.size();i++){
			double distance = Math.abs(in-centers.get(i));
			if (distance < minDistance){
				minDistance = distance;
				index = i;
			} 
		}
		return index;
	}
	
	public void recomputeCenters(){
		for (int i = 0; i < centers.size();i++){ 
			double sum = 0;
			for (Integer a : clusters.get(i)){
				sum += tavgs.get(a);
			}
			centers.set(i, sum/clusters.get(i).size());
		}
	}
	
	public double averageCluster(int index){
		double sum = 0;
		for (Integer a : clusters.get(index)){
			sum += tmaxs.get(a);
		}
		return sum/clusters.get(index).size();
	}
	
	public double predict(ClusterYear b) throws FileNotFoundException, UnsupportedEncodingException{
		double sse = 0.0;
		PrintWriter writer = new PrintWriter("KclusterPredict-" + Integer.toString(k) + ".txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter("KclusterActual-" + Integer.toString(k) + ".txt", "UTF-8");
		for (int i = 0; i < b.getTmaxs().size();i++){
			int index = getClosestCenterIndex(b.getTavgs().get(i));
			System.out.println("Average: "+b.getTavgs().get(i) + ", Prediction: "+averageCluster(index));
			sse += Math.pow(b.getTmaxs().get(i) - averageCluster(index), 2);
			writer.println(averageCluster(index));
			writer2.println(b.getTmaxs().get(i));
		}
		writer.close();
		writer2.close();
		return sse;
	}
	
}