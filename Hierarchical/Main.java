package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int m = 0, n = 0;
		String input = "";
		
			System.out.println("Enter your file name:");
			input = reader.readLine();
			List<String> fileName = null;
		
		/*if (!(!input.isEmpty() || input.equals("cho.txt") || input.equals("iyer.txt") || input.equals("new_dataset_1.txt") || input.equals("new_dataset_2.txt"))){
			System.out.println("Enter valid file name:");
			input = reader.readLine();
		}
		else{
			System.out.println("File not found");
			throw new FileNotFoundException();
		}*/
			
			//} while(!input.isEmpty());
			
		try {
			fileName = Files.readAllLines(
					Paths.get("C:\\Users\\debik\\Documents\\UB\\3rd Sem\\CSE601\\proj2\\" + input),
					StandardCharsets.UTF_8);

			// count rows and columns in input file
			m = fileName.size();
			n = fileName.get(0).split("\t").length;
			double[][] geneMat = new double[m][n + 1];
			System.out.println("m = " + m + " and n = " + n);

			for (int i = 0; i < m; i++) {
				String[] strArr = fileName.get(i).split("\t");
				for (int j = 0; j < n; j++) {
					geneMat[i][j] = Double.parseDouble(strArr[j]);
				}
				geneMat[i][n] = i;
			}

			FileWriter fw = new FileWriter("input_data_pca.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < m; i++) {
				for (int j = 2; j < n; j++) {
					bw.write(String.valueOf(geneMat[i][j]));
					if (j < n - 1)
						bw.write("\t");
				}
				bw.newLine();
			}
			bw.flush();
			bw.close();

			// Normalize the values processed

			for (int i = 2; i < n; i++) {
				double min = Double.POSITIVE_INFINITY;
				double max = Double.NEGATIVE_INFINITY;
				for (int j = 0; j < m; j++) {
					if (geneMat[j][i] > max)
						max = geneMat[j][i];
					if (geneMat[j][i] < min)
						min = geneMat[j][i];
				}
				for (int j = 0; j < m; j++) {
					geneMat[j][i] = (geneMat[j][i] - min) / (max - min);
				}

			}

			Hierarchical.formCluster(geneMat, m, n);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
