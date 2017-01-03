import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.util.Tool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.fs.FileSystem;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
//import WriteToFile.class.*;


public class KMeansMR extends Configured implements Tool{

	public static HashMap<Integer,ArrayList<Double>> hm = new HashMap<Integer,ArrayList<Double>>();
	public static TreeMap<Integer,ArrayList<Double>> previousCentroids = new TreeMap<Integer,ArrayList<Double>>();
	static ArrayList<Integer> groundTruthVal = new ArrayList<Integer>();
	public static int totalRecords = 0;
	public static int totalCol = 0;
	public static int k = -1;
	public static int flag = 0;
	public static boolean notConverged = true;
	public static int iterations = 0;
	public static List<Integer> oldC = new ArrayList<Integer>();
	public static List<Integer> newC = new ArrayList<Integer>();
	public static int[] centroids = {1,5,10,15,20};
	static Configuration config = new Configuration();
	static ArrayList<Integer> resultingCluster=new ArrayList<Integer>();
	static TreeMap<Integer, ArrayList<Double>> geneInCluster = new TreeMap<Integer,ArrayList<Double>>();
	static HashMap<ArrayList<Double>,Integer> clusterIndex = new HashMap<ArrayList<Double>,Integer>();
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int exitWhen = ToolRunner.run(new Configuration(), new KMeansMR(), args);
		System.exit(exitWhen);
	}
	public int run(String[] args) throws Exception{
		String inputFile = args[0];
		k = Integer.parseInt(args[2]);
		FileSystem fs = FileSystem.get(config);
	    DataInputStream is = new DataInputStream(fs.open(new Path(inputFile)));
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    String in;
	    int j = 1;
	    //System.out.println("Adding in hashmap & config change this part");
	    hm.clear();
	    hm.put(0, null);
	    while ((in = br.readLine()) != null) {
	      config.set(Integer.toString(j), in);
	      //System.out.println(in);
	      String col[] = in.split("\t");
	      groundTruthVal.add(Integer.parseInt(col[1]));
	      totalCol = col.length;
	      ArrayList<Double> genes = new ArrayList<Double>();
	      for (int i = 2 ; i < col.length ; i++) {
				genes.add(Double.parseDouble(col[i]));	
			}
	      totalRecords++;
	    	hm.put(j,genes);
	      j++;
	    }
	    is.close();
	    for(Integer jd: hm.keySet()){
	    	  System.out.println(jd+"Haha "+hm.get(jd));
	      }
	    
	    //config.set("iterationNumber", (iterations+1)+"");
	    
	    Job job1 = Job.getInstance(config, "KMeans Clustering using Map Reduce!");
		
		job1.setJarByClass(KMeansMR.class);
	    job1.setMapperClass(InitialMap.class);
	    job1.setMapOutputKeyClass(IntWritable.class);
	    job1.setMapOutputValueClass(Text.class);
	    //job1.setCombinerClass(KMeansReduce.class);
	    //job1.setReducerClass(KMeansReduce.class);
	    job1.setOutputKeyClass(Text.class);
	    job1.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job1, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job1, new Path(args[1]+iterations+1));
	    job1.waitForCompletion(true);
	    
	    while(iterations < 15 && flag < 5){
			Job job = Job.getInstance(config, "KMeans Clustering using Map Reduce!");
			
			job.setJarByClass(KMeansMR.class);
		    job.setMapperClass(KMeansMapper.class);
		    job.setMapOutputKeyClass(IntWritable.class);
		    job.setMapOutputValueClass(Text.class);
		    //job.setCombinerClass(KMeansReducer.class);
		    job.setReducerClass(KMeansReducer.class);
		    job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(IntWritable.class);
		    FileInputFormat.addInputPath(job, new Path(args[0]));
		    FileOutputFormat.setOutputPath(job, new Path(args[1]+iterations));
		    
		    config.setInt("K",k);
		    config.set("inputFile",args[0]);
		    	    
		    //Running the job
		    job.waitForCompletion(true);
		    iterations++;
		    //int xyz = checkIfSame(config);
		    
			}
	    iterations--;
	    WriteToFile.writeToFile(args[1]+iterations+"/part-r-00000", hm, previousCentroids, groundTruthVal);
	    //System.out.println("Done done done");
	    //System.out.println(geneInCluster.size());
	    //finalclustersList();
	    //addInFile();
	    
		
		return 0;
	}
	
	
	public static class InitialMap extends Mapper<LongWritable, Text, IntWritable, Text>{
		IntWritable k1 = new IntWritable();
		Text res = new Text();
		public void setup(Context context) throws IOException, InterruptedException{
			super.setup(context);
			//Configuration config = context.getConfiguration();
			System.out.println("NO!");
			Random r = new Random();
		    flag = 1;
			int z;
			//oldC.clear();
			for(int i = 0; i < k; i++){
				z = r.nextInt(totalRecords);
				//System.out.println(z);
				centroids[i] = z;
				oldC.add(z);
				previousCentroids.put(z, hm.get(z));
				
			
			}
			for(Integer e: previousCentroids.keySet()){
				System.out.println("Key: "+e+" Centroid values "+previousCentroids.get(e));
			}
			//createCentroid(config.get("iterationCount"));
			
			
		}
		
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//k1.set(Integer.parseInt(key.toString()));
			String[] itr = value.toString().split("\t");
			k1.set(Integer.parseInt(itr[0].trim()));
			//System.out.println("K value "+k1);
			String s = "";
			for(int i = 1; i<itr.length;i++){
				s=s+"\t" + itr[i];
			}
			
			//System.out.println("In initial Map "+s);
			context.write(k1,  new Text(s));
		
		}
		
	}

	public static class KMeansMapper extends Mapper<LongWritable, Text, IntWritable, Text>{
	
		List<Double> clustersList = new ArrayList<Double>();
		private final static IntWritable clId = new IntWritable();
		public double getDistance(ArrayList<Double> list1, ArrayList<Double> list2){
		 	
			double distance = 0.0;
			//System.out.println(list1.size()+" "+list2.size());
	    	for(int i = 0 ; i < list2.size() ; i++) {
	    		distance += (Math.pow((list2.get(i) - list1.get(i)), 2));
	    	}
	    	return Math.sqrt(distance);
		}

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
        {
			String[] itr = value.toString().split("\t");
            ArrayList<Double> dim = new ArrayList<Double>();
            //System.out.println("In mapper "+itr[0]);
            //adding dimension values to arraylist
            for(int i=2;i<itr.length;i++){
                dim.add(Double.parseDouble(itr[i].trim()));
            }
            int geneId = Integer.parseInt(itr[0].trim());
            //System.out.println("Size of dim "+dim.size());
            
            //adding each gene and its dimension to the hashmap hm
            
                double min=-1;
                int index = -1;
                
                //checking which centroid the gene is closest to
                for(int i: previousCentroids.keySet()){
                	ArrayList<Double> a2 = previousCentroids.get(i);
                	//System.out.println(a2.size());
                	double sqrt = getDistance(a2,dim);
                	if(sqrt<min || min==-1){
                        min = sqrt;
                        index = i;
                    }
                }
                
                clId.set(index);
                //oldC.add(centroids[index]);
                context.write(clId,new Text(Integer.toString(geneId))); 
            
        }
		
	}
	public static class KMeansReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
		double[] average = new double[18];
		ArrayList<Double> temp = new ArrayList<Double>();
		List<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
		
		private final static IntWritable clId = new IntWritable();
		int total = 0;
		public void setup(Context context) throws IOException, InterruptedException{
			
			//oldC.clear();
		}
		@Override
		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			System.out.println("Reduce starts");
			System.out.println("Key "+key.toString());
			//oldC.add(Integer.parseInt(key.toString()));
			for(int a = 0; a < 18; a++){
				average[a] = 0;
			}
			
			//System.out.println("avg done ");
			//int cId = Integer.parseInt(key.toString());
			String s = "";
			//if(values.toString().contains(",")) return;
			for(Text val: values){
				//temp.clear();
				if(val.toString().contains(",")) {
					System.out.println("writing to context: "+key);
					
					context.write(key, val);
					return;
				}
				s = val.toString() + ", " + s;
				//System.out.println((val.toString()));
				temp = hm.get(Integer.parseInt(val.toString()));
				list.add(temp);
				//System.out.println("Now checking temp size: ");
				//System.out.println("TS"+temp.size());
				if(temp!=null){
				for(int a = 0; a <temp.size(); a++){
					average[a] += temp.get(a);
				}}
				//System.out.println("avg in for loop");
				total++;	
			}
			//System.out.println("for done");
			ArrayList<Double> m= new ArrayList<Double>();
			for(int a = 0; a < totalCol-2; a++){
				average[a] = (average[a]/total);
				//System.out.println("Avg"+average[a]);
				m.add(average[a]);
			}
			//System.out.println("avg 2 done");
			//for(Double m1:m)
			//	System.out.print(m1+" ");
			for(Integer i: previousCentroids.keySet()){
				if(previousCentroids.get(i).equals(m))
					flag ++;
			}
			//if(flag >= 5)
				//iterations = 15;
			previousCentroids.put(totalRecords++, m);
			//System.out.println(previousCentroids.size());
			previousCentroids.pollFirstEntry();
			for(Integer e: previousCentroids.keySet()){
				System.out.println("Key: "+e+" Centroid values "+previousCentroids.get(e));
			}
			//newC.add(totalRecords);
			
			s=s.substring(0, s.length()-1);
			clId.set(totalRecords);
			//System.out.println("S is "+hm.size());
			
			context.write(clId, new Text(s));
			return;
		}
	}
}
