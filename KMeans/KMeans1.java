import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.HashMap;

public class KMeans1 {
	
	static int totalRecords=0;//386/517
	int k = 0;
	static ArrayList<Integer> groundTruthVal = new ArrayList<Integer>();//Stores ground truth values
	ArrayList<Double> genes = null;
	String fileName = null;
	static List<ArrayList<Double>> clustersList = new ArrayList<ArrayList<Double>>();//Stores list of current cluster centroids, size: k
	BufferedReader br;
	static int totalColumns = 0;
	static int iterations = 0;
	static int[] a = new int[10];
	static int totalIterations = 80;
	static boolean haveIndexes = false;
	static TreeMap<Integer, ArrayList<Double>> geneInCluster = new TreeMap<Integer,ArrayList<Double>>(); //Stores <gene, centroid/cluster it belongs to>
	static HashMap<ArrayList<Double>,Integer> clusterIndex = new HashMap<ArrayList<Double>,Integer>();//Numbers each cluster <cluster ID, cluster list>
	static List<ArrayList<Double>> geneExpVal=new ArrayList<ArrayList<Double>>(); //Expression values of each gene
	static ArrayList<Integer> resultingCluster=new ArrayList<Integer>();
	static List<ArrayList<Double>> list=new ArrayList<ArrayList<Double>>();//Final values to be compared with groundTruthVal
	static HashMap<ArrayList<Double>, ArrayList<ArrayList<Double>>> clustersContainingGenes = new HashMap<ArrayList<Double>, ArrayList<ArrayList<Double>>>(); 
	//Final list contains <cluster, list of gene exp values belonging to that cluster>
	static TreeMap<Integer,HashMap<ArrayList<Double>,Integer>> clusterMap = new TreeMap<Integer,HashMap<ArrayList<Double>,Integer>>();
	
	//Initialising
	public KMeans1(String fileName, int clustersNum){
		this.fileName = fileName;
		k = clustersNum;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void startClustering() throws IOException{
		addData();
		createclustersList();
		/*Display centroids
		for(int i = 0;i<k;i++){
			System.out.println(clustersList.get(i));
		}*/
		kMeansCalculation(0);
		List<ArrayList<Double>> oldClusters = new ArrayList<ArrayList<Double>>();
		int start = 1;
		//System.out.println(clustersList.size());
		while(!(oldClusters.equals(clustersList)) && iterations < totalIterations){
			oldClusters=new ArrayList<ArrayList<Double>>();
			oldClusters.addAll(clustersList);
			reCalculateCentroids();
			kMeansCalculation(start++);
			iterations++;
		}
		
		finalclustersList();
		/*
		for(int j: geneInCluster.keySet()){
			System.out.println(j+" , "+geneInCluster.get(j));
		}
		*/
		System.out.println("Clusters "+clustersContainingGenes.size());
		//Writing result to file
		Writer w;
		w = new FileWriter("/Users/falgunibharadwaj/Downloads/cho_result.txt");
		for(int k: resultingCluster){
			
			w.write(String.valueOf(k));
			w.write("\r\n");
		}
		w.close();
		calculateSimilarity();
	}
	
	//Adding data to lists
	public void addData(){	
		try {    
			    String in;
			    while ((in = br.readLine())!= null) {
			    	genes = new ArrayList<Double>();
			    	
			    	String col[] = in.split("\t");
			    	//System.out.println(col.length);
			    	groundTruthVal.add(Integer.parseInt(col[1]));
			    	totalRecords++;
			    	totalColumns = col.length-2;
			    
			    	for (int i = 2 ; i < col.length ; i++) {
						genes.add(Double.parseDouble(col[i]));	
					}
			    	geneExpVal.add(genes);
			    }
			    br.close();
			    System.out.println("Total records: "+totalRecords);
			    
			} catch(IOException e){
				e.printStackTrace();
			}
				
	}
	
	public double minimumDistance(ArrayList<Double> list1, ArrayList<Double> list2){
	 	
		double distance = 0.0;
		//System.out.println(list1.size()+" "+list2.size());
    	for(int i = 0 ; i < list1.size() ; i++) {
    		distance += (Math.pow((list2.get(i) - list1.get(i)), 2));
    	}
    	return Math.sqrt(distance);
	}
	
	public void createclustersList(){
		if(haveIndexes==false){
			Random r = new Random();
			int centroid; 
			
			for (int i = 0 ; i < k ; i++) {
				centroid = r.nextInt(totalRecords);
				
				if (!clustersList.contains(geneExpVal.get(centroid))) {
					clustersList.add(geneExpVal.get(centroid));
				}
			}
		}
		else{
			int centroid;
			for(int i = 0 ; i < k ; i++){
				centroid = a[i];
				if (!clustersList.contains(geneExpVal.get(centroid))) {
					clustersList.add(geneExpVal.get(centroid));
				}
			}
			
		}
		
	}
	
	public void kMeansCalculation(int sv){
		
		//System.out.println("KMeans Iteration number: "+sv);
		clustersContainingGenes.clear();
		
		for(int i = 0 ; i < geneExpVal.size() ; i++){
			
			//System.out.println("In KMeans Calculation "+i);
			double min = Double.MAX_VALUE;
			ArrayList<Double> minCentroid = null;
			for(int j = 0 ; j < clustersList.size() ; j++){
				//System.out.println("Size of j: "+clustersList.get(j).size());
				double dist = minimumDistance(clustersList.get(j),geneExpVal.get(i));
				//System.out.println("Distance: "+dist+" , Minimum: "+min);
				if(dist < min){
					min = dist;
					minCentroid = clustersList.get(j);
					
					
				}
			}
			
			//System.out.println("In KMeans Calculation "+i+"  Minimum: "+minCentroid);
			geneInCluster.put(i,minCentroid);//Adding centroid cluster for each gene
			//Populating clustersContainingGenes
			if (!clustersContainingGenes.containsKey(minCentroid)) {
				ArrayList<ArrayList<Double>> temp = new ArrayList<ArrayList<Double>>();
				temp.add(geneExpVal.get(i));
				clustersContainingGenes.put(minCentroid, temp);
			} else {
				ArrayList<ArrayList<Double>> temp = clustersContainingGenes.get(minCentroid);
				temp.add(geneExpVal.get(i));
				clustersContainingGenes.put(minCentroid, temp);
			}
		}
	}
	
	public void reCalculateCentroids(){
		clustersList.clear();
		
		double[] avg=new double[totalColumns];
	    double count;
		for(ArrayList<Double> genes_key: clustersContainingGenes.keySet()){
			for(int i = 0 ; i < avg.length ; i++) {
        		avg[i] = 0;
        		
        	}
        	//System.out.println(genes_key);
        	//System.out.println(clustersContainingGenes.get(genes_key));
        	count = 0;
            for(ArrayList<Double> ccg:clustersContainingGenes.get(genes_key)) {
            	//System.out.println(ccg);
            	//System.out.println(ccg.get(0));
            	for(int j = 0 ; j < ccg.size() ; j++) {
            		
            		avg[j] = avg[j] + ccg.get(j);
            	}
            	count++;
            	//System.out.println(v.size());
             } 
            ArrayList<Double> average = new ArrayList<Double>();
            for(int i = 0 ; i < avg.length ; i++) {
            	
            	avg[i] = avg[i]/count;
            	average.add(avg[i]);	
            }
            //System.out.println("average"+average.size());//16
            //System.out.println(count+" !!!!!!!");
            clustersList.add(average);   

		}
		
	}
	
	public void finalclustersList(){
		ArrayList<Double> clusterExpVal;
		int clusterID = 1;
		//System.out.println("GG "+geneInCluster.size());
		for(int j:geneInCluster.keySet()){
			clusterExpVal = geneInCluster.get(j);
			if(!clusterIndex.containsKey(clusterExpVal)){
				clusterIndex.put(clusterExpVal, clusterID);
				clusterID++;
			}
			list.add(geneInCluster.get(j));
			
		}
		//System.out.println("CI "+list.size()); 
		for(int k: geneInCluster.keySet()){
			//System.out.println(geneInCluster.size());
			resultingCluster.add(clusterIndex.get(geneInCluster.get(k)));
		}
	}
	
	public void calculateSimilarity(){
		int[][] groundMatrix=new int[totalRecords][totalRecords];
		int[][] clusterMatrix=new int[totalRecords][totalRecords];
		//System.out.println("It comes here!");
		for (int i = 0; i < totalRecords; i++) {
			
            for (int j = i; j < totalRecords; j++) {
            	
            	if (groundTruthVal.get(i) == groundTruthVal.get(j)){
                    groundMatrix[i][j] = 1;
                    groundMatrix[j][i] = 1;
                }
                else{
                	groundMatrix[i][j] = 0;
                	groundMatrix[j][i] = 0;
                }
            	
                if (resultingCluster.get(i) == resultingCluster.get(j)){
                    clusterMatrix[i][j] = 1;
                    clusterMatrix[j][i] = 1;
                }
                else{
                    clusterMatrix[i][j] = 0;
                    clusterMatrix[j][i] = 0;
                }
            }
        }
		
		double jaccard = jaccard_coeff(groundMatrix,clusterMatrix,totalRecords);
		System.out.println("External Validation: Jaccard Coefficient is "+jaccard);
	}
	public double jaccard_coeff(int[][] groundMatrix,int[][] clusterMatrix, int n) {
		
		double jaccard = 0;
		//System.out.println("Jaccard! It comes here!");
		int positive = 0, notNegative = 0;
		
		for(int i = 0 ; i < n ; i++) {
			
			for(int j = 0 ; j < n ; j++) {
				
				if(clusterMatrix[i][j] == 1 && groundMatrix[i][j] == 1) 
					positive++;
				
				if(!(clusterMatrix[i][j]==0 && groundMatrix[i][j]==0))
					notNegative++;
					
			}
		}
		jaccard = (double)(positive)/(double)(notNegative);
		return jaccard;
	}
	
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		String fileName = "/Users/falgunibharadwaj/Downloads/iyer.txt";
		System.out.println("Enter file path: ");
		Scanner in = new Scanner(System.in);
		fileName = in.next();
		System.out.println("Enter k of clustersList: ");
		in = new Scanner(System.in);
		int k = in.nextInt();
		System.out.println("Enter number of iterations");
		in = new Scanner(System.in);
		totalIterations = in.nextInt();
		//static?
		System.out.println("Do you want to enter initial centroid ids? (y/n)");
		in = new Scanner(System.in);
		String y = in.next();
		System.out.println(y);
		if(y.equals("y")){
			haveIndexes = true;
			for(int i = 0; i< k;i++){
			System.out.println("Enter id"+i);
			in = new Scanner(System.in);
			a[i]=in.nextInt();
			}
		}
		KMeans1 k_means = new KMeans1(fileName,k);
		k_means.startClustering();
		
	}
	
}
