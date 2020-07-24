package dataAggregation;
import java.io.File;  
import java.io.FileNotFoundException;  
import java.util.Scanner; 
import java.util.*;
import java.io.*;

class AggregateExecuteCuj {
	

	/**
	 * Parses the test given by myReader, which is formatted as specified {@link #Main()} by the JAVADOC on the main function of this class.
	 * Adds each parsed iteration to the allIterationInfos list
	 *
	 */
	public static void parseInputFile (Scanner myReader, List<Utility.IterationInfo> allIterationInfos) {
		//Parse Input file:
		while (myReader.hasNextLine()) {
			String line = myReader.nextLine();
			if (!line.equals("")) {
				//Read all duration/timestamp pairs
				Utility.readDurationTimePair(line, myReader.nextLine(), null, ".", allIterationInfos); //Two consequtive lines containing a duration list and a time start/end
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
	 * @param averageActionDurations - the average duration for each action (may be null if no data is recorded)
	 * @param medianActionDurations - the median duration for each action (may be null if no data is recorded)
	 *
	 */
	public static List<List<String>> formatTable (String[] postCUJ, List<Utility.IterationInfo> allIterationInfos, List<Integer> averageActionDurations, List<Integer> medianActionDurations) {
		List<List<String>> formattedTable = new ArrayList<>();

		//Add with first row
		List<String> firstRow = new ArrayList<>();
		firstRow.add("");
		for (int i = 0; i < postCUJ.length - 1; i++) { // Don't include the termination action!
			String action = postCUJ[i];
			firstRow.add(String.valueOf(action));
		}
		firstRow.add("TOTAL");
		formattedTable.add(firstRow);
		
		//Add with raw data rows
		int iter = 1;
		for (Utility.IterationInfo curIterationInfo : allIterationInfos) {
			List<String> row = new ArrayList<>();
			row.add("ITERATION " + iter++ + ": ");
			for (Integer duration : curIterationInfo.getDurations()) {
				row.add(String.valueOf(duration));
			}
			row.add(String.valueOf(curIterationInfo.getTotalDuration()));
			formattedTable.add(row);
		}
		
		//Add empty row
		List<String> emptyRow = new ArrayList<>();
		for (int i = 0; i < firstRow.size(); i++) {
			emptyRow.add("");
		}
		formattedTable.add(emptyRow);

		//Add with average row
		List<String> averageRow = new ArrayList<>();
		averageRow.add("AVERAGE:");
		for (int i = 0; i < postCUJ.length - 1; i++) { //Don't include termination action
			String averageActionDuration = (averageActionDurations == null) ? "N/A" : String.valueOf(averageActionDurations.get(i));
			averageRow.add(averageActionDuration);
		}
		averageRow.add((averageActionDurations == null) ? "N/A" : String.valueOf(Utility.sum(averageActionDurations)));
		formattedTable.add(averageRow);

		//Add with median row
		List<String> medianRow = new ArrayList<>();
		medianRow.add("MEDIAN:");
		for (int i = 0; i < postCUJ.length - 1; i++) { //Don't include termination action
			String medianActionDuration = (medianActionDurations == null) ? "N/A" : String.valueOf(medianActionDurations.get(i));
			medianRow.add(medianActionDuration);
		}
		medianRow.add((medianActionDurations == null) ? "N/A" : String.valueOf(Utility.sum(medianActionDurations)));
		formattedTable.add(medianRow);

		return formattedTable;
	}



	/**
	 * Parses the passed inputfile and prints a summary of the data, which includes the total number of iterations executed,
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
		boolean trim = Boolean.parseBoolean(args[5]); //Tell us whether we want to trim the video

		List<Utility.IterationInfo> allIterationInfos = new ArrayList<>();

		//Parse input file
		parseInputFile(myReader, allIterationInfos);
		

		//Compute average/median durations and report missing data
		ps.printf("TOTAL ITERATIONS: " + totalIters + "\n");
		if (allIterationInfos.size() < totalIters) {
			ps.printf("\nNOTE: " + (totalIters - allIterationInfos.size()) + " iterations of data are missing\n");
		}
		List<Integer> averageActionDurations = Utility.averageActionDurations(allIterationInfos);
		List<Integer> medianActionDurations = Utility.medianActionDurations(allIterationInfos);

		
		//Print summary
		List<List<String>> formattedSummary = formatTable(postCUJ, allIterationInfos, averageActionDurations, medianActionDurations);
		Utility.printFormattedTable(formattedSummary, ps);

		ps.printf(headerFooter);


		//Trim and save all videos
		if (allIterationInfos.size() != 0 && trim) {
			Utility.ClipInfo medianClip = Utility.iterationWithMedianTotalDuration(allIterationInfos).getClipInfo();
			Utility.trimClip(medianClip, clipDestinationFolder + "/median_clip.mp4");
		}
	}
}
