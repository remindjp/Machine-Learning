/* Jingpeng Wu 
 * Machine Learning HW#3 Sigmoid
 * This program trains a sigmoid node using the gradient descent algorithm 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Sigmoid {
	public static double[] weights;
	public static int attrSize;
	//Takes train file, test file, learning rate and iterations through args[]
	public static void main(String[] args) throws FileNotFoundException {
		train(args[0], Double.parseDouble(args[2]), Integer.parseInt(args[3])); //test file and iterations
		test(args[1]);
	}
	
	//Uses the given scanner to put input into a usable format
	public static ArrayList<int[]> read(Scanner scan) {
		ArrayList<int[]> data = new ArrayList<int[]>();
		
		while(scan.hasNextLine()) {
			int[] temp = new int[attrSize + 1];
			String line = scan.nextLine();
			Scanner lineScan = new Scanner(line);
			if(line.equals("")) { //if there are extra spaces
				break;
			}
			for(int i = 0 ; i < attrSize + 1; i++) {
				temp[i] = lineScan.nextInt();
			}
			data.add(temp);
		}			
		
//		for(int i =0; i < data.size(); i++) {
//			System.out.println(Arrays.toString(data.get(i)));
//		}
		return data;
	}
	//Trains the sigmoid on the input data. Assumes valid imput format
	public static void train(String trainData, double rate, int iterations) throws FileNotFoundException {
		Scanner trainScan = new Scanner(new File(trainData));
		
		//Finds attrSize based on header
		String header = trainScan.nextLine();
		Scanner headScan = new Scanner(header);
		while(headScan.hasNext()) {
			attrSize++;
			headScan.next(); //discard
		}
		ArrayList<int[]> data = read(trainScan);
		weights = new double[attrSize]; //all weights = 0 default		
		for(int i = 0; i < iterations; i++) { //for each instance, compute..
			int ii = i;
			if(iterations > data.size()) { //repeat data
				ii = i % data.size();
			}
			//compute in
			int[] instance = data.get(ii);
			double in = inComp(instance);
			
			double gIn = 1.0 / (1 + Math.exp( -1 * in));
			double err = instance[attrSize] -gIn;
			double gPrime = gIn * ( 1 - gIn);
			
			for(int j = 0; j< weights.length; j++) {
				weights[j] += rate * err * gPrime * instance[j];
			}
		}
	}
	
	public static double inComp(int[] instance) {
		double in = 0.0;
		for(int j = 0 ; j < attrSize; j++) {
			 in += weights[j] * instance[j];
		}
		return in;
	}
	
	//Tests the data based on the trained sigmoid
	public static void test(String testData) throws FileNotFoundException {
		Scanner testScan = new Scanner(new File(testData));
		testScan.nextLine();
		
		ArrayList<int[]> data = read(testScan);
		int counts = data.size();
		int correct = 0;
		for(int i = 0; i < counts; i++) {
			//compute in
			int[] instance = data.get(i);
			double in = inComp(instance);
			double gIn = 1.0 / (1 + Math.exp( -1 * in));
			double err = instance[attrSize] - gIn;
			if(err <= .5) { //.5 threshold
				correct++;
			}
		}
		
		//prints accuracy
		double accuracy = (double) correct / counts;
		System.out.print("Accuracy on test set (" + counts + " instances):");
		System.out.printf("%.1f", accuracy * 100);
		System.out.println("%");
	}
	
	
}
