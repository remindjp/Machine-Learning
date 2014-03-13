//Jingpeng Wu Machine Learning Viterbi Algorithm
//3/12/2014
//This program uses the viterbi algorithm to find the 
//optimal path given a series of observations
//Uses arrays when the length is known and an ArrayList
//otherwise.
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;


public class viterbi {
	
	public static int states;
	public static double[] initial;
	public static double[][] transitions;
	
	public static int outputs;
	public static ArrayList<String> alphabet;
	public static double[][] distributions;

	public static void main(String[] args) throws FileNotFoundException {
		parse(args[0], args[1]);
	}
	
	//Parses all the required fields
	public static void parse(String one, String two) throws FileNotFoundException {
		Scanner s = new Scanner(new File(one));
		states = Integer.parseInt(s.next());

		initial = new double[states];
		for(int i = 0; i< states; i++) {
			initial[i] = Double.parseDouble(s.next());
		}
		
		transitions = new double[states][states];
		for(int i = 0; i < states; i++) {
			for(int j = 0; j < states; j++) {
				transitions[i][j] = Double.parseDouble(s.next());
			}
		}
		
		outputs = Integer.parseInt(s.next());
		
		alphabet = new ArrayList<String>();
		s.nextLine();
		Scanner alphabetScan = new Scanner(s.nextLine());
		while(alphabetScan.hasNext()) {
			alphabet.add(alphabetScan.next());
		}
		
		distributions = new double[states][outputs];
		for(int i = 0; i < states; i++) {
			for(int j = 0; j < outputs; j++) {
				distributions[i][j] = Double.parseDouble(s.next());
			}
		}		

		//Observed data from args[1]
		Scanner oS = new Scanner(new File(two));
		
		//For each observation (line) compute & print using viterbi 
		while(oS.hasNextLine()) {
			ArrayList<String> observations = new ArrayList<String>();
			Scanner oSLine = new Scanner(oS.nextLine());
			while(oSLine.hasNext()) {
				observations.add(oSLine.next());
			}
			compute(observations);
		}
				
	}
	
	//Keeps track of 2 consecutive steps of the HMM to use for calculations
	public static void compute(ArrayList<String> observations) {

		int[] sequence = new int[observations.size()];
		double[][] computed = new double[observations.size()][states];
		//Finds which column of distributions to use
		int column = alphabet.indexOf(observations.get(0));
		//step one
		for(int i =0; i < states; i++) {
			computed[0][i] = initial[i] * distributions[i][column];
		}
		//For the M-1 remaining observations, compute and store the max
		for(int m =1; m < observations.size(); m++) {

			column = alphabet.indexOf(observations.get(m));
			//For all states for step M
			for(int i =0; i< states; i++) {
				//Compute the max of all states j entering state i
				double max = 0.0;
				for(int j=0; j< states; j++) {
					//value of j * transition from j to i 
					max = Math.max(max, computed[m-1][j] * transitions[j][i]); 
				}
				computed[m][i] = distributions[i][column] * max;	
			}
		}
		
		//backtrack last element
		double max = 0.0;
		int stateNum =-1;
		for(int i =0; i < states; i++) {
			if(computed[observations.size() - 1][i] > max) {
				max = computed[observations.size() - 1][i];
				stateNum = i;
			}
		}
		sequence[observations.size() -1] = stateNum;
		
		//other elements based on last element
		for(int j = observations.size() - 2; j >= 0; j--) {
			for(int i =0; i < states; i++) {
				if(computed[j][i] * transitions[i][stateNum] > max) {
					max = computed[j][i]* transitions[i][stateNum];
					stateNum = i;
				}	
			}
			sequence[j] = stateNum;
		}
		System.out.println("Most likely sequence: " + Arrays.toString(sequence));
	}
}
