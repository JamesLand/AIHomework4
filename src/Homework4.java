import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Vector;



public class Homework4 {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		Year a = new Year("2015.csv");
		Year b = new Year("2016.csv");		
		
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

}

class Year{
	
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
	
	public Year(String loc) throws FileNotFoundException{
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