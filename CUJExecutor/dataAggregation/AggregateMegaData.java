package dataAggregation;
import java.io.File;  
import java.io.FileNotFoundException;  
import java.util.Scanner; 
import java.util.*;
import java.io.*;

class AggregateMegaData {
	


	/**
	 * Parses the test given by myReader, which is formatted as specified by the JAVADOC on the main function of this class.
	 * Parses each empty-line-seperated paragraph as corresponding to a flag in a cyclic fashion (according to order and flagCount).
	 * Then, indexes into the lists for that flag and adds the parsed iterations
	 * (We are assuming that the size and fullVideoFolder data will always be present, even if the actual execution of the CUJ fails. Thus, we can assume the cyclic ordering will always hold.)
	 * 	
	 *
	 */
	public static void parseInputFile (Scanner myReader, int flagCount, List<List<Utility.IterationInfo>> allIterationInfos) {
		//Parse Input file:
		int flagIdx = 0;
		while (myReader.hasNextLine()) {
			String line = myReader.nextLine();
			if (!line.equals("")) {
				int size = Integer.parseInt(line);
				String fullVideoFolder = myReader.nextLine();
				
				//Read all duration/timestamp pairs
				while (!(line = myReader.nextLine()).equals("")) {
					Utility.readDurationTimePair(line, myReader.nextLine(), size, fullVideoFolder, allIterationInfos.get(flagIdx)); //Two consequtive lines containing a duration list and a time start/end
				}
				flagIdx = (flagIdx + 1) % flagCount;
			}
		}
		myReader.close();
	}

	/**
	 * Formats a table where the columns, in order, are: compilationFlags, Sizes, col 1 of actionDurations, col 2 of actionDurations, ..., col n of actionDurations.
	 * Each column is labeled by the data in that column (COMPILATION FLAG, APP SIZE, or in the case of an actionDuration column, the corresponding token in the measuredCUJ array).
	 * If there is no data to place in a table entry, "N/A" is entered.
	 * This is intended to be helper method for printing measures of central tendency for our data (where the entries in sizes and actionDurations are actually averages, medians, or some other statistic calculated over raw data).
	 *  
	 * @param measuredCUJ - an array of Strings representing the measured
	 * @param compilationFlags - a list of the compilation flags used for this test
	 * @param sizes - a list of size compilationFlags.length. (could contain nulls if no data is recorded for that flag)
	 * @param actionDurations - list of size compilationFlags.length, where each non-null sublist has length measuredtCUJ.length (inner lists could be null if no data is recorded for that flag)
	 *
	 */
	public static List<List<String>> formatTable(String[] measuredCUJ, String[] compilationFlags, List<Integer> sizes, List<List<Integer>> actionDurations) {
		List<List<String>> formattedTable = new ArrayList<>();

		//Add first row
		List<String> firstRow = new ArrayList<>();
		firstRow.add("COMPILATION FLAG");
		firstRow.add("APP SIZE");
		for (int i = 0; i < measuredCUJ.length; i++) {
                        String action = measuredCUJ[i];
                        firstRow.add(String.valueOf(action));
                }
		firstRow.add("TOTAL");
		formattedTable.add(firstRow);


		//Add data rows
		for (int flagIdx = 0; flagIdx < compilationFlags.length; flagIdx++) {
			List<String> row = new ArrayList<>();
			row.add(compilationFlags[flagIdx]);
			row.add(sizes.get(flagIdx) == null ? "N/A" : String.valueOf(sizes.get(flagIdx)));
			for (int action = 0; action < measuredCUJ.length; action++) { //Don't have data for termination action
				row.add(actionDurations.get(flagIdx) == null ? "N/A" : String.valueOf(actionDurations.get(flagIdx).get(action)));
			}
			row.add(actionDurations.get(flagIdx) == null ? "N/A" : String.valueOf(Utility.sum(actionDurations.get(flagIdx))));
			formattedTable.add(row); 
		}
		return formattedTable;
    	}


	/**
         * Parses the passed inputfile and prints a summary of the data, which includes the total number of iterations executed,
         * and a table with the durations of each action (and the total) for each iteration, as well as averages and medians for
         * each action (and the total). Each column is also labeled by the token used in the file passed to ./executeCUJ and
         * a note is printed detailing what data is missing (if any is).
         *
         *
         * @param inputFile - location of a file that contains the data from each iteration through the CUJ executed by ./executeCuj
         *                    file must be in the following form:
         *                   
         *                    s1
	 *                    f1
	 *                    d1,1
	 *                    t1,1
	 *                    d1,2
	 *                    t1,2
	 *                    .
	 *                    .
	 *                    .
	 *                    d1,n
	 *                    t1,n
	 *        
	 *   		      s2
	 *                    f2
	 *                    d2,1
	 *                    t2,1
	 *                    d2,2
	 *                    t2,2
	 *                    .
	 *                    .
	 *                    .
	 *                    d2,n
	 *                    t2,n
	 *        
	 *
	 *        	      .
	 *	              .
	 *        	      .
	 *        	      .
	 *        	      .
	 *        	      .
	 *
	 *   		      sk
	 *                    fk
	 *                    dk,1
	 *                    tk,1
	 *                    dk,2
	 *                    tk,2
	 *                    .
	 *                    .
	 *                    .
	 *                    dk,n
	 *                    tk,n
         *
         *
         *                    Where each executeCuj call involved n iterations through the CUJ and k total calls were made to executeCUJ
	 *                    Each si must be an integer representing the size of Google Maps before the test
	 *                    Each fi must be a folder where the video recording of this iteration would be stored (if it exists).
	 *                    Each di must be a comma-seperated list of integers, where each integer corresponds to the duration of an action in the CUJ (passed sequentially).
         *                    Each ti must consist of two comma-seperated times in the form HH:MM:SS.sss, where the first corresponds to the time when this iteration started 
	 *                    	and the second time corresponds to the time when this iteration ended (both relative to the beginning of the video containing every iteration).
	 *
	 *                    Note that the entries cycle through the provided compilation flags (i.e. if there are c compilation flags, entries sk, fk, dk,i and tk,i all corespond to iterations
	 *                    that were executed using the flag compilationFlags[k % compilationFlags.length]). Further, it is expected that the size and folder will always be provided, but any number 
	 *                    of durations/timestamp pairs may be passed. (Might want to add compilationFlag as a line in each paragraph...)
	 *
	 *                    An example file is provided at dataAggregation/mockFiles/mockMegaTestOutput.txt.
         *                   
         * @param outputStream - the location of the file where we should dump the summary table produced by parsing the given data
         * @param measuredCUJ - a comma-seperated list of actions of the form specified by the ./executeCuj script
         * @param totalIters - the total number of iterations expected to have been executed for each flag
         * @param clipDestinationFolder - the folder where we should store the output clip if one is produced
         * @param compilationFlags - a comma-separated list of the compilation flags used in this megaTest
         */


	public static void main (String[] args) throws Exception {

		//Prepare inputs/outputs
		File inputFile = new File(args[0]);
		Scanner myReader = new Scanner(inputFile);
		OutputStream outputStream = new FileOutputStream(args[1]);
		PrintStream ps = new PrintStream(outputStream);
		for (String s : args) {
			System.out.println(s);
		}
		
		String[] measuredCUJ = args[2].split("\\s*,\\s*");
		int totalIters = Integer.parseInt(args[3]);
		String clipDestinationFolder = args[4];
		String[] compilationFlags = args[5].split(" ");

		int flagCount = compilationFlags.length;


		List<List<Utility.IterationInfo>> allIterationInfos = new ArrayList<>();

		for (int i = 0; i < flagCount; i++) {
			allIterationInfos.add(new ArrayList<>());
		}

		//Parse input file
		parseInputFile(myReader, flagCount, allIterationInfos);

		
		//Compute average/median durations and sizes. Report missing data
		List<List<Integer>> averageActionDurations = new ArrayList<>(flagCount); //i = flag, j = action index
		List<List<Integer>> medianActionDurations = new ArrayList<>(flagCount);	//i = flag, j = action index
		List<Integer> averageSizes = new ArrayList<>(flagCount);
		List<Integer> medianSizes = new ArrayList<>(flagCount);

		ps.printf("TOTAL ITERATIONS PER FLAG: " + totalIters);
		for (int flagIdx = 0; flagIdx < flagCount; flagIdx++) {
			List<Utility.IterationInfo> flagData = allIterationInfos.get(flagIdx);
			if (flagData.size() < totalIters) {
				ps.printf("\nNOTE: " + (totalIters - flagData.size()) + " iterations of data are missing for flag " + compilationFlags[flagIdx]);
			}
			averageActionDurations.add(Utility.averageActionDurations(flagData));
			medianActionDurations.add(Utility.medianActionDurations(flagData));
			averageSizes.add(Utility.averageSize(flagData));
			medianSizes.add(Utility.medianSize(flagData));
		}


		
		//Print summary
		ps.printf("\n\nAVERAGED DATA:");
		List<List<String>> averageFormattedSummary = formatTable(measuredCUJ, compilationFlags, averageSizes, averageActionDurations);
		Utility.printFormattedTable(averageFormattedSummary, ps);

		ps.printf("\n\nMEDIANED DATA:");
		List<List<String>> medianFormattedSummary = formatTable(measuredCUJ, compilationFlags, medianSizes, medianActionDurations);
		Utility.printFormattedTable(medianFormattedSummary, ps);


		//Trim and save all videos
		for (int flagIdx = 0; flagIdx < flagCount; flagIdx++) {
			List<Utility.IterationInfo> flagData = allIterationInfos.get(flagIdx);
			if (flagData.size() != 0) {
				Utility.ClipInfo flagMedianClip = Utility.iterationWithMedianTotalDuration(flagData).getClipInfo();
				Utility.trimClip(flagMedianClip, clipDestinationFolder + "/" + compilationFlags[flagIdx] + "_median_clip.mp4");
			}
		}
	}
}
