package dbscan;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.lang.*;

public class dbscan {
	
	static HashMap<Integer,ArrayList<Double>> hm = new HashMap<Integer,ArrayList<Double>>();
	static HashMap<Integer,ArrayList<Integer>> clusters = new HashMap<Integer,ArrayList<Integer>>();
	static HashMap<Integer,Integer> visit = new HashMap<Integer,Integer>();
	static HashMap<Integer,Integer> gtv = new HashMap<Integer,Integer>();
	static HashMap<Integer,Integer> atv = new HashMap<Integer,Integer>();
	static ArrayList<Integer> noise = new ArrayList<Integer>();
	static HashMap<Integer,Integer> forPCA = new HashMap<Integer,Integer>();
	
	public static void main(String[] args){
	
		try{
			
			double eps = 2;
			int minpts = 200;
			int clusterNo = 0;
			
			BufferedReader br = new BufferedReader(new FileReader("/Users/mtappeta/Desktop/cho.csv"));
			String line = null;
			
			while((line = br.readLine())!=null){
				
				String[] sep = line.split(",");
				ArrayList<Double> values = new ArrayList<Double>();
				
				for(int i=2;i<14;i++){
					
					values.add(Double.parseDouble(sep[i]));
				}
				
				//STORING GENEID AS KEY AND TRUE(1) OR FALSE(0) AS VALUES FOR VISITED
				visit.put(Integer.parseInt(sep[0]), 0);
				
				//STORING EACH GENEID AS KEY AND ITS POINTS FROM DATASET AS VALUES
				hm.put(Integer.parseInt(sep[0]), values);
				
				//STORING GROUND TRUTH VALUE
				gtv.put(Integer.parseInt(sep[0]), Integer.parseInt(sep[1]));
				
			}
			
	
			//ITERATING THROUGH EACH GENE IN hm
			for(int j=1; j<=hm.size();j++){						
		
				//CHECKING IF NODE IS UNVISITED
				if(visit.get(j)!=1){	
					
					//MARK P AS VISITED
					visit.put(j, 1);			
					
					//FINDING P's NEIGHBOURS
					ArrayList<Integer> neighbours = new ArrayList<Integer>();
					neighbours = regionQuery(j,eps);	//find all neighbours of point j
					
					if(neighbours.size() >= minpts){
						
						//IF NO. OF NEIGHBOURS IF >= MINPTS START CLUSTERING
						clusterNo++;
						
						expandCluster(j, eps, neighbours, minpts, clusterNo);
					}else noise.add(j);
					
				}
			}

			double jaccard = calJaccard();
	
		
			clusterNo++;
			Writer w;
			w = new FileWriter("/Users/mtappeta/Desktop/cho_result.txt");
			for(int f=1;f<=hm.size();f++){
				if(forPCA.get(f)==null){
					w.write(String.valueOf(clusterNo));
					w.write("\r\n");
				}else{
					w.write(String.valueOf(forPCA.get(f)));
					w.write("\r\n");
				}
					
			}
			w.close();
			System.out.println("No of clusters :"+clusters.size());
			System.out.println("Jaccard Coefficient :"+ jaccard);
			
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException s){
				s.printStackTrace();
			}
	}
	
	public static ArrayList<Integer> regionQuery(int point, double dist){
		
		ArrayList<Double> pointDim = new ArrayList<Double>(hm.get(point));
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
		
		for(int k=1; k<=hm.size(); k++){
			
			double sum=0;		
			//STORING DIMENION POINTS OF EACH GENE IN dimension 
			ArrayList<Double> dimension = new ArrayList<Double>(hm.get(k));
			
			for(int ele=0; ele<dimension.size(); ele++){
				
				//CALCULATING EUCLIDEAN DISTANCE
				double diff = dimension.get(ele) - pointDim.get(ele);
				double square = diff*diff;
				sum = sum + square;
			}
			
			double sqrt = Math.sqrt(sum);
			//IF DISTANCE <= eps THEN ADD POINT TO NEIGHBOUR LIST
			
			if(sqrt <= dist && sqrt>-1 )
				neighbours.add(k);
			
		}
		
		
		return neighbours;
	}
	
	public static double calJaccard(){
		int dim = hm.size()+1;
		int [][]  groundTruth = new int[dim][dim];
		int [][]  actualValue = new int[dim][dim];
		
		//CREATING GROUND TRUTH VALUE MATRIX
		for(int r=1;r<=hm.size();r++){
			for(int s=1;s<=hm.size();s++){
				if(gtv.get(r) == gtv.get(s) && gtv.get(r)!=null && gtv.get(s)!=null)
					groundTruth[r][s] = 1;
				else
					groundTruth[r][s] = 0;
			}
		}
		
		//CREATING ACTUAL VALUE MATIRX
		for(int r=1;r<=hm.size();r++){
			for(int s=1;s<=hm.size();s++){
				if(atv.get(r) == atv.get(s) && atv.get(r)!=null && atv.get(s)!=null )
					actualValue[r][s] = 1;
				else
					actualValue[r][s] = 0;
			}
		}
		int pos = 0, neg=0;
		for(int r=1;r<=atv.size();r++){
			
			for(int s=1;s<=atv.size();s++){
				
				if(groundTruth[r][s] == 1 && actualValue[r][s] == 1)
					pos++;
				
				if(!(groundTruth[r][s] == 0 && actualValue[r][s] == 0))
					neg++;
			}
		}
	
		double jacc = (double)(pos)/(double)(neg);
		return jacc;
	}
	
	public static void expandCluster(int point, double dist, ArrayList<Integer> neighbour, int minpts, int CNo){
		
		ArrayList<Integer> newcluster = new ArrayList<Integer>();	
		
		//ADD P TO C
		if(!newcluster.contains(point)){
			newcluster.add(point);
			forPCA.put(point, CNo);
		}
		
		for(int y=0; y<neighbour.size(); y++){
		
			//ITERATE OVER EACH NEIGHBOUR
			if(visit.get(neighbour.get(y)) == 0 && (!noise.contains(neighbour.get(y)) )){
				
				//IF NEIGHBOUR IS NOT VISITED THEN MARK AS VISITED
				visit.put(neighbour.get(y), 1);
				ArrayList<Integer> neighboursNeighbour = new ArrayList<Integer>();
				
				//FIND ALL NEIGHBOURS N' OF NEIGHBOUR N
				neighboursNeighbour = regionQuery(neighbour.get(y),dist);
				
				if(neighboursNeighbour.size() >= minpts){
					
					//IF N' COUNT IS >=MINPTS THEN ADD ALL N' TO N
					for(int x=0; x<neighboursNeighbour.size(); x++){
						if(!neighbour.contains(neighboursNeighbour.get(x)))
						neighbour.add(neighboursNeighbour.get(x));
					}
				}
				
			}
			
			// CHECKING IF N IS ALREADY PRESENT IN ANY C, IF NOT ADD N TO C
			int containsGene = 0;
				
				for(int q=0;q<clusters.size();q++){
					
					if(clusters.get(q)!=null && clusters.get(q).contains(neighbour.get(y)))
							containsGene = 1;
				}
				if(containsGene == 0 && (!newcluster.contains(neighbour.get(y)) ) )
				{
					newcluster.add(neighbour.get(y));
					forPCA.put(neighbour.get(y), CNo);
					atv.put(neighbour.get(y), CNo);
				}
				
		}
		
		clusters.put(CNo, newcluster);
		
	}

}
