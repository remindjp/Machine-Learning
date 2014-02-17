import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

//Jingpeng Wu
//Machine Learning HW1
/*
 	This program takes training and test data from String[] args in the 
command line and tries to create a tree that accurately matches future 
classes based on their attributes in the test data.
	The functionality of each method and program flow is described in the header. 
Datatypes used are ArrayLists when the length is uncertain, arrays when 
the length is certain and HashSets when no order is needed.
	Due to time constraints, the attributes field must be manually adjusted
if applicable (all provided files have attributes = 6).

Special Notes:
Data is read columnwise
Outputs: Text to the command line including accuracy %'s and trees in the 
specified format.
 */






//Accepts two args, a training and test file from the command line
public class DTLearningAlgorithm {
	
	public static Node root;
	public static ArrayList<ArrayList<Integer>> values;
	public static ArrayList<Integer> classes;
	public static int TI;
	public static final int attributes = 6;  ////////////////////////Depends

	public static int[] vPas;
	public static int shift;
	
	public static ArrayList<ArrayList<Integer>> datas;
	public static ArrayList<Integer> dataClasses;

 	public static void main(String[] args) throws FileNotFoundException {

		//Reads the data from the training file
		Scanner trainScan = new Scanner(new File(args[0]));
		reader(trainScan);
		
		//Reads the data from the test file
		Scanner dataScan = new Scanner(new File(args[1]));
		tester(dataScan);
	}
	
 	//Tester that scans the test data and makes predictions
 	//contains duplicate code that might be changed
 	public static void tester(Scanner dataScan) {

		int accuracyNum =0;
		int accuracyDen =0;
		
 		//discard
 		dataScan.nextLine();
		datas = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < attributes; i++) {
			datas.add(new ArrayList<Integer>());
		}
 		dataClasses = new ArrayList<Integer>();
		int j = 0;
		int instances = 0;
		while(dataScan.hasNext()) {
			//put last column in classes
			int next = dataScan.nextInt();
			if(j == attributes) {
				dataClasses.add(next);
			} else {
				datas.get(j).add(next);
			}

			instances++;
			j = instances % (attributes + 1);
		}
		accuracyDen = instances / (attributes + 1);

		//Navigate the tree
		Node node = root;
		for(int y =0; y < accuracyDen; y++) {

			while(node.children.size()!=0) {
				int path = node.splitBy; //which attribute to look at
				//which attribute value to look at (= branch)
				int nextAttr = datas.get(path).get(y); 
				node = node.children.get(nextAttr - 1 + shift);
			}
			if(node.classLabel == dataClasses.get(y)) {
				accuracyNum++;
			}
				node = root;
		}
		double accuracy = (double) accuracyNum / accuracyDen;

		System.out.print("Accuracy using training set (" + TI + " instances):");
		System.out.printf("%.1f", accuracy * 100);
		System.out.println("%");
 	}
 	
 	
	//This method uses the given Scanner to read dat files for making a tree
	public static void reader(Scanner trainScan) {
		vPas = new int[attributes];	//values per attribute
		
		//1st line, grab number of values per attribute
		for(int i =0; i< (attributes) * 2 ; i+=2) {
			trainScan.next(); 	//discard
			vPas[i/2] = trainScan.nextInt();
		}
	
		//creates a list for each attribute
		values = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < attributes; i++) {
			values.add(new ArrayList<Integer>());
		}
		//List of classes
		classes = new ArrayList<Integer>();
		int j = 0;
		int instances = 0;
		while(trainScan.hasNext()) {
			//put last column in classes
			int next = trainScan.nextInt();
			if(j == attributes) {
				classes.add(next);

			} else {
				values.get(j).add(next);
				if(next == 0) {
					shift = 1;
				}
			}
			instances++;
			j = instances % (attributes + 1);
		}
		
		TI= instances / (attributes + 1);
		
		root = new Node();	
		for(int i =0; i < classes.size(); i++) {
			root.data.add(i);
		}
		//add attributes (all present initially to root)
		for (int i =0; i < attributes; i++) {
			root.attsLeft.add(i);
		}
		
		
		//split once.
		TreeCreator(root, minCalc(root));
		
		//split remaining nodes
		for(int i = 0; i < root.children.size(); i++) {
			Node child = root.children.get(i);
			for (int k = 0; k < child.children.size(); k++) {
				Node childChild = child.children.get(k);
				TreeCreator(childChild, minCalc(childChild));
			}
		}
		
		//print tree
		treePrinter(root, -1);
		System.out.println();
	}
	
	//This method prints the tree recursively.
	public static void treePrinter(Node node, int level) {
		level++;
		//isLeaf
		if(node.children.size()==0) {
			System.out.println(node.classLabel + node.data.toString());
		} else {
			System.out.println();
			for(int i =0; i < node.children.size(); i++) { //for each child
				//print |'s
				for(int k=0; k<level; k++) {  //print indent
					System.out.print("| ");
				}
				//0 based indexing for attributes with 1 based indexing
				System.out.print("attr" + node.splitBy + " = " + (i + 1 + shift) + ": ");
				treePrinter(node.children.get(i), level);
			}
		}
	}
	
	//Returns att# with lowest entropy (-1 flag if no IG)
	public static int minCalc(Node node) {
		double minEntropy = 9.9;
		int min = -1;
		double[] nodeEntropies = entropy(node);
		//for each available attribute
		for(int i : node.attsLeft) {
			double entropyForAtt = nodeEntropies[i];
			if( entropyForAtt < minEntropy && entropyForAtt < 1.0 ) { // H < 1.0 for IG
				minEntropy = entropyForAtt;
				min = i;
			}
		}
		return min;
	}	
	
	//Splits the given node at attribute i
	public static void TreeCreator (Node node, int split){
		//flag for 0 IG, may lead to low accuracies.
		if(split == -1 || node.isPure) {
			return;
		}
		//Attribute has been used. Remove from future split candidates.
		node.attsLeft.remove(split);
		node.splitBy = split;
		//Adds 'branches' to this node for each attribute's potential splitting. 
		//Then passes down all attributes left to children
		for(int i = 0; i < vPas[split]; i++) {
			Node branch = new Node();
			branch.attsLeft.addAll(node.attsLeft);
			node.children.add(branch);
		}
		
		ArrayList<Integer> attribute = values.get(split);
		//adds the index of a class to the proper branch after splitting (branch#=attr#)
		for(int i =0; i < attribute.size(); i++) {
			Node child = node.children.get(attribute.get(i) - 1 + shift);
			//Only adds if class is part of the current node and available for splitting
			if(node.data.contains(i)) {
				child.data.add(i);
				if(classes.get(i) == 0) {
					child.aCounts++;
				} else {
					child.bCounts++;
				}
			}			
		}

		//puritycheck, puts all elements of the node into a set, if pure then size = 1
		//also assigns classLabel based on aCounts and bCounts
		for(int i =0; i < node.children.size(); i++) {
			Node child = node.children.get(i);
			HashSet<Integer> types = new HashSet<Integer>();
			
			for(int j : child.data) {
				types.add(classes.get(j));
			}
			if(types.size()==1 || child.data.size()==0) {
				child.isPure = true;
			}
			
			//label

			if(!(child.aCounts ==0 && child.bCounts ==0)) {
				
				if(child.aCounts > child.bCounts) {
					child.classLabel = 0;
				} else if (child.aCounts < child.bCounts) {
					child.classLabel = 1;
				} else {
					//equal entropy
					Random rand = new Random();
					child.classLabel = rand.nextInt(2);
				}
			}
		}
		//Recursively creates the tree
		for(int i =0; i < node.children.size(); i++) {
			Node child = node.children.get(i);
			if(!child.isPure && child.attsLeft.size() >0) {
				TreeCreator(child, minCalc(child));
			}
		}
	}

	//Calculates the entropies for a node for all att splits. 
	public static double[] entropy(Node node) {
		
		double[] entropyArray = new double[values.size()];
		for(int i : node.attsLeft) {
			ArrayList<Integer> attrValues = values.get(i);
			int attrSize = vPas[i];
			int[] countA = new int[attrSize]; //class 0
			int[] countB = new int[attrSize]; //class 1

			//for each class at this node... do {...} 
			for(int j : node.data) {
				int value = attrValues.get(j) - 1 + shift; //value of attribute

				//Count the # of each attribute value for the two classes A and B.
				if(classes.get(j) == 0) {
					countA[value]++;
				} else { // ==1
					countB[value]++;
				}
			}	
			//Find total # of attribute values regardless of class type
			int[] sums = new int[attrSize]; 
			for(int j =0; j <attrSize; j++) {
				sums[j] = countA[j] + countB[j];
			}
			
			//entropy of split
			double entropy = 0.0;
			for (int j = 0; j < attrSize; j++) {
				//Probabilities for each after split
				if (sums[j] == 0) {
					sums[j] = 1;
				}
				double aP = (double) countA[j] / sums[j];
				double bP = (double) countB[j] / sums[j];
				if (aP ==0) {
					aP = 1;
				}
				if (bP ==0) {
					bP = 1;
				}		
				//Add entropy from this portion of the split
				entropy -= ((double)sums[j] / node.data.size()) *
						(aP * Math.log(aP) / Math.log(2) + bP * Math.log(bP) / Math.log(2));
			} 
			entropyArray[i] = entropy;

		}		
		return entropyArray;
	}
	
	//Node class for a tree with an arbitrary number of children
	public static class Node {
		//indices of the classes in this node
		public HashSet<Integer> data;
		public ArrayList<Node> children; //Branches
		public HashSet<Integer> attsLeft; //Atts left to be used on data at this node
		public boolean isPure;
		public int splitBy; //Attribute used to split to this node
		public int classLabel;
		public int aCounts; //# of class A instances
		public int bCounts; //# of class B instances
		
		public Node (){
			data = new HashSet<Integer>();
			children = new ArrayList<Node>();
			isPure = false;
			attsLeft = new HashSet<Integer>();
			splitBy = -1; //Didnt split yet
			classLabel = -1; //-1 for unassigned / empty
		}
		
		//toString()
		public String toString() {	
			String str = "[";
			for(int s : data) {
				str += s + " ";
			}
			str += "]";
			return str;			
		}
	}	
}
