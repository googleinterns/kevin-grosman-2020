package dataAggregation;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.*;
import java.io.*;

class AggregateExecuteCuj {
	

	public static void parseInputFile (Scanner myReader, List<List<Integer>> actionDurations,  Map<Integer, Utility.clipInfo> totalDurationToClipInfo) {
		//Parse Input file:
		while (myReader.hasNextLine()) {
			String line = myReader.nextLine();
			if (!line.equals("")) {
				//Read all duration entries
				Utility.readDurationTimePair(line, myReader.nextLine(), ".", actionDurations, totalDurationToClipInfo); //Two consequtive lines containing a duration list and a time start/end
			}
		}
		myReader.close();
	}

	public static List<List<String>> formatTable (String[] postCUJ, List<List<Integer>> actionDurations, List<Integer> averageActionDurations, List<Integer> medianActionDurations) {
		List<List<String>> formattedTable = new ArrayList<>();

		//Add with first row
		List<String> firstRow = new ArrayList<>();
		firstRow.add("");
		for (int i = 0; i < postCUJ.length - 1; i++) {
			String action = postCUJ[i];
			firstRow.add(String.valueOf(action));
		}
		firstRow.add("TOTAL");
		formattedTable.add(firstRow);
		
		//Add with raw data rows
		int iter = 1;
		for (List<Integer> iterationActionDurations: actionDurations) {
			List<String> row = new ArrayList<>();
			row.add("ITERATION " + iter++ + ": ");
			for (Integer time : iterationActionDurations) row.add(String.valueOf(time));
			formattedTable.add(row);
		}
		
		//Add empty row
		List<String> emptyRow = new ArrayList<>();
		for (int i = 0; i < firstRow.size(); i++) emptyRow.add("");
		formattedTable.add(emptyRow);

		//Add with average row
		List<String> averageRow = new ArrayList<>();
		averageRow.add("AVERAGE:");
		for (Integer time : averageActionDurations) averageRow.add(String.valueOf(time));
		formattedTable.add(averageRow);

		//Add with median row
		List<String> medianRow = new ArrayList<>();
		medianRow.add("MEDIAN:");
		for (Integer time : medianActionDurations) medianRow.add(String.valueOf(time));
		formattedTable.add(medianRow);

		return formattedTable;
	}




	public static void main (String[] args) throws Exception {

		//Prepare inputs/outputs
		File inputFile = new File(args[0]);
		Scanner myReader = new Scanner(inputFile);
		OutputStream outputStream = new FileOutputStream(args[1]);
		PrintStream ps = new PrintStream(outputStream);
		String headerFooter = "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";


		for (String s : args) {
			System.out.println(s);
		}

		String[] postCUJ = args[2].split("\\s*,\\s*");
		int totalIters = Integer.parseInt(args[3]);
		String clipDestinationFolder = args[4];
		boolean trim = Boolean.parseBoolean(args[5]); //tell us whether we want to trim the video

		List<List<Integer>> actionDurations = new ArrayList<>(totalIters);//2D list : j = test index, k = action index
		Map<Integer, Utility.clipInfo> totalDurationToClipInfo = new HashMap<>(); //Mapping from durations to videoclip infos for every flag

		//parse input file
		parseInputFile(myReader, actionDurations, totalDurationToClipInfo);
		

		//compute average/median durations and report missing data
		ps.printf("TOTAL ITERATIONS: " + totalIters + "\n");
		if (actionDurations.size() < totalIters) {
			ps.printf("\nNOTE: " + (totalIters - actionDurations.size()) + " iterations of data are missing");
		}
		List<Integer> averageActionDurations = Utility.averageColumns(actionDurations);
		List<Integer> medianActionDurations = Utility.medianColumns(actionDurations);


		
		//Print summary
		List<List<String>> formattedSummary = formatTable(postCUJ, actionDurations, averageActionDurations, medianActionDurations);
		Utility.printFormattedTable(formattedSummary, ps);

		ps.printf(headerFooter);


		//Trim and save all videos
		if (medianActionDurations != null && trim) {
			Integer medianTotalDuration = medianActionDurations.get(medianActionDurations.size() - 1);
			Utility.clipInfo medianClip = totalDurationToClipInfo.get(medianTotalDuration);
			Utility.trimClip(medianClip, clipDestinationFolder + "/median_clip.mp4");
		}
		

	
	}
}
