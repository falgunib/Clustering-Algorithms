import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
//import KMeansMR.class;

public class WriteToFile {
	static ArrayList<Integer> resultingCluster=new ArrayList<Integer>();
	static TreeMap<Integer, ArrayList<Double>> geneInCluster = new TreeMap<Integer,ArrayList<Double>>();
	static HashMap<ArrayList<Double>,Integer> clusterIndex = new HashMap<ArrayList<Double>,Integer>();
	static ArrayList<Integer> groundTruthVal = new ArrayList<Integer>();
	public static int totalRecords = 16;
	
	public static void writeToFile(String path, HashMap<Integer,ArrayList<Double>> hm,TreeMap<Integer,ArrayList<Double>> pc, ArrayList<Integer> gt) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(path));
		groundTruthVal = gt;
		ArrayList<Double> gene=null;
		try {
		    
		    String line;

		    while ((line = br.readLine())!= null) {
		    	gene=new ArrayList<Double>();
		    	//count++;
		    	String columns[]=line.split("\t");
		    	//ground_truth.add(Integer.parseInt(columns[1]));
		    	String parts[] = columns[1].split(",");
		    	for(int i = 0; i < parts.length;i++){
		    		geneInCluster.put(Integer.parseInt(parts[i].trim()), pc.get(Integer.parseInt(columns[0].trim())-1));
		    		System.out.println("ID: "+Integer.parseInt(parts[i].trim())+" Centroid: "+pc.get(Integer.parseInt(columns[0].trim())-1));
		    	}
		    	
		    
		    }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finalclustersList();
		addInFile();
		
	}
	public static void finalclustersList(){
		ArrayList<Double> clusterExpVal;
		int clusterID = 1;
		System.out.println("GG "+geneInCluster.size());
		for(int j:geneInCluster.keySet()){
			clusterExpVal=geneInCluster.get(j);
			if(!clusterIndex.containsKey(clusterExpVal)){
				clusterIndex.put(clusterExpVal, clusterID);
				clusterID++;
			}
		}
		System.out.println("GG "+clusterIndex.size()); 
	}
	public static void addInFile() throws IOException{
		Writer w;
		w = new FileWriter("/Users/falgunibharadwaj/Downloads/iyer_result_mr.txt");
		for(int k: geneInCluster.keySet()){
			System.out.println("K is "+k+" hoho "+geneInCluster.get(k));
			resultingCluster.add(clusterIndex.get(geneInCluster.get(k)));
			w.write(String.valueOf(clusterIndex.get(geneInCluster.get(k))));
			w.write("\r\n");
		}
		w.close();
		calculateSimilarity();
	}
	
	public static void calculateSimilarity(){
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
	public static double jaccard_coeff(int[][] groundMatrix,int[][] clusterMatrix, int n) {
		
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
}
