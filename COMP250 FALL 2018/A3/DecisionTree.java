
import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf



		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}



		// this method takes in a datalist (ArrayList of type datum) and a minSizeInClassification (int) and returns
		// the calling DTNode object as the root of a decision tree trained using the datapoints present in the
		// datalist variable
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			//YOUR CODE HERE
			
			boolean sameLabel=true;
			if (datalist.size()>=minSizeDatalist) {
				
				//check if all items have the same label, if not -> false and break
				for (int i=0; i<datalist.size()-1; i++) {
					if ((datalist.get(i).y != datalist.get(i+1).y)) {
						sameLabel=false;
						//break;
					}
				}
				
				//if true set label, leaf status and return
				if (sameLabel) {
					DTNode newNode = new DTNode();
					newNode.label=datalist.get(0).y;
					newNode.leaf=true;
					newNode.left=null;
					newNode.right=null;
					return newNode;
				}
				else {
					double best_avg_entropy=Double.MAX_VALUE, best_threshold=-1 
							,leftE, rightE, wL, wR, curr_avg_entropy;
					int best_attr=-1;
					ArrayList<Datum> bestL=null, bestR=null;
					//for each attribute
					for (int i=0; i<2; i++)	{
						
						//for each data point in the array list
						for (int j=0; j<datalist.size(); j++) {
							
							//setting current point as test threshold(brute force)
							double thresholdTemp=datalist.get(j).x[i];
							
							//creating a left and right subset to divide tree
							ArrayList<Datum> leftSet= new ArrayList<>(), rightSet= new ArrayList<>();
							
							//for each point in datalist testing with current temp threshold
							for (int k=0; k<datalist.size(); k++) {
								if(datalist.get(k).x[i]<thresholdTemp) {
									leftSet.add(datalist.get(k));
								} else rightSet.add(datalist.get(k)); 
							}
								
							//After separating into L and R subsets calc the entropy, without cast it doesn't work (idk why)
							wL=(double)leftSet.size()/(double)datalist.size();
							wR=(double)rightSet.size()/(double)datalist.size();
							leftE=calcEntropy(leftSet);
							rightE=calcEntropy(rightSet);
							curr_avg_entropy=(wL*leftE)+(wR*rightE);
																
							//If entropy is lower we save it as best
							if ((best_avg_entropy>curr_avg_entropy)) {
								best_avg_entropy=curr_avg_entropy;
								best_attr=i;
								best_threshold=thresholdTemp;
								bestL=leftSet;
								bestR=rightSet;
							}
						}
					}
					
					//Create and set new node
					DTNode newNode = new DTNode();
					newNode.leaf=false;
					newNode.attribute=best_attr;
					newNode.threshold=best_threshold;
					//recursive call
					newNode.left=fillDTNode(bestL);
					newNode.right=fillDTNode(bestR);
					return newNode;
				
				}
			}
			//if datalist is too small, set label in function of the majority
			else {
				DTNode newNode = new DTNode();
				newNode.label = findMajority(datalist);
				return newNode;
			}
		}


		//This is a helper method. Given a datalist, this method returns the label that has the most
		// occurences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist)
		{
			int l = datalist.get(0).x.length;
			int [] votes = new int[l];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			int max = -1;
			int max_index = -1;
			//find the label with the max occurrences
			for (int i = 0 ; i < l ;i++)
			{
				if (max<votes[i])
				{
					max = votes[i];
					max_index = i;
				}
			}
			return max_index;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			
			//YOUR CODE HERE
			//if this. node is a leaf return it's label
			if (this.leaf) {
				return this.label;
			}
			//if not, compare data point in correct axis to the threshold
			else {
				double threshold=this.threshold;
				//if smaller recursive return call on the left child, else on the right side (this condition is weird tbh) 
				if(xQuery[this.attribute]<threshold) {
					return this.left.classifyAtNode(xQuery);
				} else return this.right.classifyAtNode(xQuery);
			}
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{

			//YOUR CODE HERE
//			if ((this==null) && (dt2==null)) {
//				return true;
//			}
			//If neither nodes are null
			if ((this!=null) && (dt2!=null)) {
				//if both are leaves, return if their labels are equal
				if ((this.leaf) && (((DTNode) dt2).leaf)) {
					return ((this.label==((DTNode) dt2).label));
				}
				//if both aren't leaves
				if ((!this.leaf) && (!((DTNode) dt2).leaf)) {
					//if their attributes and threshold are the same
					if ((this.attribute==((DTNode) dt2).attribute)
							&& (this.threshold==((DTNode) dt2).threshold)) {
						//recursive call on equal for both the left and right childs
						boolean c1=this.left.equals(((DTNode) dt2).left);
						boolean c2=this.right.equals(((DTNode) dt2).right);
						//if left and right subtrees are equal ret true, else false
						if (c1 && c2) {
							return true;
						} else return false;
					}
				}
			}
			//One node empty, one not -> trees different, auto false
			return false;
		}
	}



	//Given a dataset, this retuns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist)
	{
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		DTNode node = this.rootDTNode;
		return node.classifyAtNode( xQuery );
	}

    // Checks the performance of a DecisionTree on a dataset
    //  This method is provided in case you would like to compare your
    //results with the reference values provided in the PDF in the Data
    //section of the PDF

    String checkPerformance( ArrayList<Datum> datalist)
	{
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
