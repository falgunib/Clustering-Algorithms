package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;


public class Hierarchical {
	public static void formCluster(double[][] geneMat, int m, int n) throws NumberFormatException, IOException {
	
		ArrayList<ArrayList<Double>> minDist = new ArrayList<ArrayList<Double>>();
		int miniDist[] = new int[m];
		

		ArrayList<Double> item = new ArrayList<Double>(m);
		for (int i = 0; i <= m; i++) {
			item.add((double) i);
		}
		minDist.add(item);
				
		for (int i = 0; i < m; i++) {
			ArrayList<Double> nextItem = new ArrayList<Double>(m);
			nextItem.add((double) (i + 1));
			for (int j = 0; j < m; j++) {
				nextItem.add(0.0);
			}
			minDist.add(nextItem);
		}
		

		Double[][] distanceMatrix = new Double[m][m];
		for (int i = 0; i < m; i++) {
			for (int j = i+1; j < m; j++) {
				double distance = 0d;
				if (i != j)
					for (int l = 2; l < n; l++) {
						distance += Math.pow(geneMat[i][l] - geneMat[j][l], 2);
						distanceMatrix[i][j] = Math.sqrt(distance);
					}
				else {
					distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
				}

				minDist.get(i + 1).set(j + 1, distance);
				minDist.get(j + 1).set(i + 1, distance);
				
			}
		}
		
		
		int size = m;
		int k = 5;
		//System.out.println("Enter number of clusters");
		//BufferedReader br = new BufferedReader(br);
		//k = Integer.parseInt(br.readLine());

		
		while (minDist.size() > k + 1) {

			int min_i = 0, min_j = 1, iMin = (int) (double) minDist.get(0).get(0),
					jMin = (int) (double) minDist.get(1).get(0);
			Double minX = 100d; //Double.POSITIVE_INFINITY;

			for (int i = 1; i < size; i++)
				for (int j = i + 1; j < size + 1; j++)
					if (minDist.get(j).get(i) < minX) {
						iMin = (int) ((double) minDist.get(i).get(0)) - 1;
						jMin = (int) ((double) minDist.get(j).get(0)) - 1;
						min_i = i;
						min_j = j;
						minX = minDist.get(i).get(j);

					}
			
			for (int i = 0; i < m; i++){
				if (geneMat[i][n] == jMin){
					geneMat[i][n] = (double) iMin;
				}
			}

			for (int i = 1; i <= size; i++) {
				Double min = 0.0;
			if(minDist.get(min_i).get(i) < minDist.get(min_j).get(i)){
				min = minDist.get(min_i).get(i);
			}
				
			else{
				min = minDist.get(min_j).get(i);
			}
				
			minDist.get(min_i).set(i, min);
			minDist.get(i).set(min_i, min);
				//System.out.println("test:" + minDist.get(i).size());
			}
			

			for (int i = 0; i <= size; i++){
				minDist.get(i).remove(min_j);
			}
				
			minDist.remove(min_j);
			size--;
		}
		
		// Calculate Jaccard coefficient
		
	
			int[][] getCluster = new int[m][m];
			int[][] groundTruth = new int[m][m];
			
			double jaccard = 0.0;
			for (int i = 0; i < m; i++)
				for (int j = 0; j < m; j++) {
					if (geneMat[i][n] == geneMat[j][n]) {
						getCluster[i][j] = getCluster[j][i] = 1;
					} 
					else {
						getCluster[i][j] = getCluster[j][i] = 0;
					}

					if (geneMat[i][1] == geneMat[j][1] && geneMat[i][1] != -1) {// removing outliers
						groundTruth[i][j] = groundTruth[j][i] = 1;
					} 
					else {
						groundTruth[i][j] = groundTruth[j][i] = 0;
					}
				}
			
			// The Jaccard similarity coefficient, J, is given as
			// J = m11 / (m01 + m10 + m11) 
			
			int m11 = 0, m10 = 0, m01 = 0;
			
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < m; j++) {
					if (getCluster[i][j] == groundTruth[i][j]) {
						if (getCluster[i][j] == 1)
							m11++;
					} else {
						if (getCluster[i][j] == 1 && groundTruth[i][j] == 0)
							m10++;
						else if (getCluster[i][j] == 0 && groundTruth[i][j] == 1)
							m01++;
					}
				}
			}
			jaccard = m11 / (double) (m11 + m10 + m01);
			System.out.println("Jaccard coefficient is : " + jaccard);
			
			// PCA Visualization
			
	
			   
			  /* ******************* */
			  /* for (int i = 0; i < m; i++)
					System.out.println(miniDist[i]);

				// count of each cluster
				HashMap<ArrayList<Double>, Integer> count = new HashMap<ArrayList<Double>, Integer>();
				for (int i = 0; i < m; i++) {

					if (count.get(minDist.get(i)) != 0){
						//count.put(minDist.get(i), 1);
					//else {
						int val = count.get(miniDist[i]);
						count.put(minDist.get(i), ++val);
					}

				}
			   
				try {
					FileWriter fw=new FileWriter("make_new_file.txt");
					BufferedWriter bw=new BufferedWriter(fw);
					for(int i=0;i < m;i++)
					{
						bw.write(String.valueOf(minDist.get(i)));
					bw.newLine();
					}
					
					
					bw.close();
					fw.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Cannot open");
					e.printStackTrace();
				}			  */
			
			
			List<int[]> cluster = new ArrayList<int[]>(k); 
			List<Integer> cluster_ids = new ArrayList<Integer>(k);
			for (int i = 0; i < k; i++) {
				int current_id = 0;
				for (int j = 0; j < m; j++) {
					if ((!cluster_ids.contains((int)geneMat[j][n])) && current_id == 0)
					{
						current_id = (int) geneMat[j][n];
						cluster_ids.add((int)current_id);
						
						cluster.addAll(new ArrayList<int[]>());
						int[] point = new int[n-2];
						for (int h = 0; h < n-2; h++)
							point[h] = (int) geneMat[j][h+2];
						cluster.add(point);
						//System.out.println(cluster.add(point));
					}
					else if( geneMat[j][n] == current_id)
					{
						int[] x = new int[n-2];
						for (int h = 0; h < n-2; h++)
							x[h] = (int) geneMat[j][h+2];
						cluster.add(x);
						//System.out.println(cluster.add(x));
					}
				}
			}
			
		/*	Iterator itr =  (Iterator) cluster.iterator();
			 while (itr.hasNext()) {
			        point = (Integer[])it.next();  
			        for (int i = 0; i < 3; i++) {
			            System.out.print(point[i] + ",");
			        } 
			            System.out.println(); 
			        }
			*/
			
			//List<int[][]> pca = new ArrayList<int[][]>();
			   
		//	for(int[] l:cluster){
			  //  double[][] array= new double[l.length][n-2];
			   // pca.add();
			    
		//	}
	}
}
