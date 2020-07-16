package dataAggregation;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.*;
import java.io.*;

class aggregateMegaData {
	

	public static void parseInputFile (Scanner myReader, int flagCount, List<List<List<Integer>>> durationData, List<List<Integer>> sizeData, List<Map<Integer, Utility.clipInfo>> totalDurationToClipInfo) {
		//Parse Input file:
		int flagIdx = 0;
		while (myReader.hasNextLine()) {
			String line = myReader.nextLine();
			if (!line.equals("")) {
				int size = Integer.parseInt(line);
				sizeData.get(flagIdx).add(size);

				String fullVideoFolder = myReader.nextLine();
				
				//Read all duration entries
				while (!(line = myReader.nextLine()).equals("")) {
					Utility.readDurationTimePair(line, myReader.nextLine(), fullVideoFolder, durationData.get(flagIdx), totalDurationToClipInfo.get(flagIdx)); //Two consequtive lines containing a duration list and a time start/end
				}
				flagIdx = (flagIdx + 1) % flagCount;
			}
		}
		myReader.close();
	}
	public static List<List<String>> formatTable(String[] postCUJ, String[] compilationFlags, List<Integer> sizes, List<List<Integer>> durations) {
		List<List<String>> formattedTable = new ArrayList<>();

		//Add first row
		List<String> firstRow = new ArrayList<>();
		firstRow.add("COMPILATION FLAG");
		firstRow.add("APP SIZE");
		for (int i = 0; i < postCUJ.length - 1; i++) {
                        String action = postCUJ[i];
                        firstRow.add(String.valueOf(action));
                }
		firstRow.add("TOTAL");
		formattedTable.add(firstRow);


		//Add data rows
		for (int flagIdx = 0; flagIdx < compilationFlags.length; flagIdx++) {
			List<String> row = new ArrayList<>();
			row.add(compilationFlags[flagIdx]);
			row.add(sizes.get(flagIdx) == null ? "N/A" : String.valueOf(sizes.get(flagIdx)));
			for (int action = 0; action < durations.get(flagIdx).size(); action++) {
				row.add(durations.get(flagIdx) == null ? "N/A" : String.valueOf(durations.get(flagIdx).get(action)));
			}
			formattedTable.add(row); 
		}
		return formattedTable;
    }





	public static void main (String[] args) throws Exception {

		//Prepare inputs/outputs
		File inputFile = new File(args[0]);
		Scanner myReader = new Scanner(inputFile);
		OutputStream outputStream = new FileOutputStream(args[1]);
		PrintStream ps = new PrintStream(outputStream);

		for (String s : args) {
			System.out.println(s);
		}
		
		String[] postCUJ = args[2].split("\\s*,\\s*");
		int totalIters = Integer.parseInt(args[3]);
		String clipDestinationFolder = args[4];
		String[] compilationFlags = args[5].split(" ");

		int flagCount = compilationFlags.length;
		int actionCount = postCUJ.length - 1;

		List<List<List<Integer>>> durationData = new ArrayList<>(flagCount);//3D list : i = flag, j = test index, k = action index
		List<List<Integer>> sizeData = new ArrayList<>(flagCount); //3D list : i = flag, j = test index
		List<Map<Integer, Utility.clipInfo>> totalDurationToClipInfo = new ArrayList<>(flagCount); //Mapping from durations to videoclip infos for every flag

		for (int i = 0; i < flagCount; i++) {
			durationData.add(new ArrayList<>());
			sizeData.add(new ArrayList<>());
			totalDurationToClipInfo.add(new HashMap<>());
		}

		//parse input file
		parseInputFile(myReader, flagCount, durationData, sizeData, totalDurationToClipInfo);

		
		//compute average/median durations and report missing data
		List<List<Integer>> averageActionDurations = new ArrayList<>(flagCount); //i = flag, j = action index
		List<List<Integer>> medianActionDurations = new ArrayList<>(flagCount);	//i = flag, j = action index

		ps.printf("TOTAL ITERATIONS PER FLAG: " + totalIters);
		for (int flagIdx = 0; flagIdx < flagCount; flagIdx++) {
			List<List<Integer>> flagData = durationData.get(flagIdx);
			if (flagData.size() < totalIters) {
				ps.printf("\nNOTE: " + (totalIters - flagData.size()) + " iterations of data are missing for flag " + compilationFlags[flagIdx]);
			}
			averageActionDurations.add(Utility.averageColumns(flagData));
			medianActionDurations.add(Utility.medianColumns(flagData));
		}

		//compute average/median app sizess and report missing data
		List<Integer> averageSizes = new ArrayList<>(flagCount);
		List<Integer> medianSizes = new ArrayList<>(flagCount);


		for (List<Integer> flagSize : sizeData) {
			averageSizes.add(Utility.average(flagSize));
			medianSizes.add(Utility.median(flagSize));
		}

		
		//Print summary
		ps.printf("\n\nAVERAGED DATA:");
		List<List<String>> averageFormattedSummary = formatTable(postCUJ, compilationFlags, averageSizes, averageActionDurations);
		Utility.printFormattedTable(averageFormattedSummary, ps);

		ps.printf("\n\nMEDIANED DATA:");
		List<List<String>> medianFormattedSummary = formatTable(postCUJ, compilationFlags, medianSizes, medianActionDurations);
		Utility.printFormattedTable(medianFormattedSummary, ps);


		//Trim and save all videos
		for (int flagIdx = 0; flagIdx < flagCount; flagIdx++) {
			List<Integer> flagMedianActionDurations = medianActionDurations.get(flagIdx);
			Map<Integer, Utility.clipInfo> flagTotalDurationToClipInfo = totalDurationToClipInfo.get(flagIdx);
			if (flagMedianActionDurations != null) {
				Integer flagMedianTotalDuration = flagMedianActionDurations.get(flagMedianActionDurations.size() - 1);
				Utility.clipInfo flagMedianClip = flagTotalDurationToClipInfo.get(flagMedianTotalDuration);
				Utility.trimClip(flagMedianClip, clipDestinationFolder + "/" + compilationFlags[flagIdx] + "_median_clip.mp4");
			}
		}



	
	}
}
