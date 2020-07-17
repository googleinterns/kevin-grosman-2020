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
				//Read all duration/timestamp pairs
				Utility.readDurationTimePair(line, myReader.nextLine(), ".", actionDurations, totalDurationToClipInfo); //Two consequtive lines containing a duration list and a time start/end
			}
		}
		myReader.close();
	}

	/**
	 * Formats a table which includes the duration for each action (plus the total duration) for each iteration, as well as averages and medians over all iterations.
	 * If there is no data to enter in a table entry, "N/A" is entered.
	 *  
	 * @param postCUJ - an array of Strings representing the post
	 * @param actionDurations - list of lists, each of which holds durations for each of the actions in the measured-actions list, as well as a final total duration
	 * @param averageActionDurations - the average duration for each action (may contain nulls if no data is recorded for that action)
	 * @param medianActionDurations - the mediane duration for each action (may contain nulls if no data is recorded for that action)
	 *
	 */
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
		for (int i = 0; i < postCUJ.length; i++) { //don't include termination action
			String averageActionDuration = (averageActionDurations == null) ? "N/A" : String.valueOf(averageActionDurations.get(i));
			averageRow.add(averageActionDuration);
		}
		formattedTable.add(averageRow);

		//Add with median row
		List<String> medianRow = new ArrayList<>();
		medianRow.add("MEDIAN:");
		for (int i = 0; i < postCUJ.length; i++) { //don't include termination action
			String medianActionDuration = (medianActionDurations == null) ? "N/A" : String.valueOf(medianActionDurations.get(i));
			medianRow.add(medianActionDuration);
		}
		formattedTable.add(medianRow);

		return formattedTable;
	}



	/**
	 * parses the passed inputfile and prints a summary of the data, which includes the total number of iterations executed,
	 * and a table with the durations of each action (and the total) for each iteration, as well as averages and medians for
	 * each action (and the total). Each column is also labeled by the token used in the file passed to ./executeCUJ and
	 * a note is printed detailing how many iterations of data are missing (if any are).
	 *
	 *
	 * @param inputFile - location of a file that contains the data from each iteration through the CUJ executed by ./executeCuj
	 * 		      file must be in the following form:
	 * 		     
	 * 		      d1
	 *	              t1
	 *	              d2
	 *	              t2
	 *
	 *	              .
	 *	              .
	 *	              .
	 *
	 *	              dn
	 *	              tn
	 *
	 *	              Where the executeCuj call successfully iterated through the CUJ n times. Each di must be a comma-seperated
	 *	              list of integers, where each integer corresponds to the duration of an action in the CUJ (passed sequentially).
	 *	              Each ti must consist of two comma-seperated times in the form HH:MM:SS.sss, where the first corresponds to the time 
	 *	              when this iteration started and the second time corresponds to the time when this iteration ended (relative to the 
	 *	              beginning of the video containing every iteration). An example file is provided at 
	 *	              dataAggregation/mockFiles/mockExecuteCujOutput.txt.
	 *	             
	 * @param outputStream - the location of the file where we should dump the summary table produced by parsing the given data
	 * @param postCUJ - a comma-seperated list of actions of the form specified by the ./executeCuj script
	 * @param totalIters - the total number of iterations expected to have been executed
	 * @param clipDestinationFolder - the folder where we should store the output clip if one is produced
	 * @param trim - "true" or "false", telling us whether we should produce a trimmed clip
	 */
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
			ps.printf("\nNOTE: " + (totalIters - actionDurations.size()) + " iterations of data are missing\n");
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
