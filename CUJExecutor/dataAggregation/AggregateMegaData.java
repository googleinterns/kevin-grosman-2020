package dataAggregation;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.*;
import java.io.*;

class AggregateMegaData {
	


	/**
	 *
	 *
	 */
	public static void parseInputFile (Scanner myReader, int flagCount, List<List<List<Integer>>> actionDurations, List<List<Integer>> sizeData, List<Map<Integer, Utility.clipInfo>> totalDurationToClipInfo) {
		//Parse Input file:
		int flagIdx = 0;
		while (myReader.hasNextLine()) {
			String line = myReader.nextLine();
			if (!line.equals("")) {
				int size = Integer.parseInt(line);
				sizeData.get(flagIdx).add(size);

				String fullVideoFolder = myReader.nextLine();
				
				//Read all duration/timestamp pairs/ 
				while (!(line = myReader.nextLine()).equals("")) {
					Utility.readDurationTimePair(line, myReader.nextLine(), fullVideoFolder, actionDurations.get(flagIdx), totalDurationToClipInfo.get(flagIdx)); //Two consequtive lines containing a duration list and a time start/end
				}
				flagIdx = (flagIdx + 1) % flagCount;
			}
		}
		myReader.close();
	}

	/**
	 * Formats a table where the columns, in order, are: compilationFlags, Sizes, col 1 of actionDurations, col 2 of actionDurations, ..., col n of actionDurations.
	 * Each column is labeled by the data in that column (COMPILATION FLAG, APP SIZE, or in the case of an actionDuration column, the corresponding token in the postCUJ array).
	 * If there is no data to place in a table entry, "N/A" is entered.
	 * This is intended to be helper method for printing measures of central tendency for our data (where the entries in sizes and actionDurations are actually averages, medians, or some other statistic calculated over raw data).
	 *  
	 * @param postCUJ - an array of Strings representing the post
	 * @param compilationFlags - a list of the compilation flags used for this test
	 * @param sizes - a list of size compilationFlags.length. (could contain nulls if no data is recorded for that flag)
	 * @param actionDurations - list of size compilationFlags.length, where each non-null sublist has length postCUJ.length (could contain nulls if no data is recorded for that flag)
	 *
	 */
	public static List<List<String>> formatTable(String[] postCUJ, String[] compilationFlags, List<Integer> sizes, List<List<Integer>> actionDurations) {
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
			for (int action = 0; action < postCUJ.length; action++) {
				row.add(actionDurations.get(flagIdx) == null ? "N/A" : String.valueOf(actionDurations.get(flagIdx).get(action)));
			}
			formattedTable.add(row); 
		}
		return formattedTable;
    	}


	/**
         * parses the passed inputfile and prints a summary of the data, which includes the total number of iterations executed,
         * and a table with the durations of each action (and the total) for each iteration, as well as averages and medians for
         * each action (and the total). Each column is also labeled by the token used in the file passed to ./executeCUJ and
         * a note is printed detailing what data is missing (if any is).
         *
         *
         * @param inputFile - location of a file that contains the data from each iteration through the CUJ executed by ./executeCuj
         *                    file must be in the following form:
         *                   
         *                    s
	 *                    f1
	 *                    d1
	 *                    t1
	 *
	 *                    s2
	 *                    f2
	 *                    d2
	 *                    t2
	 *
	 *		      .
	 *		      .
	 *		      .
	 *
	 *                    sn
	 *                    fn
	 *                    dn
	 *                    tn
	 *
         *
         *                    Where the executeCuj call successfully iterated through the CUJ n times. Each di must be a comma-seperated
         *                    list of integers, where each integer corresponds to the duration of an action in the CUJ (passed sequentially).
         *                    Each ti must consist of two comma-seperated times in the form HH:MM:SS.sss, where the first corresponds to the time 
         *                    when this iteration started and the second time corresponds to the time when this iteration ended (relative to the 
         *                    beginning of the video containing every iteration). An example file is provided at 
         *                    dataAggregation/mockFiles/mockExecuteCujOutput.txt.
         *                   
         * @param outputStream - the location of the file where we should dump the summary table produced by parsing the given data
         * @param postCUJ - a comma-seperated list of actions of the form specified by the ./executeCuj script
         * @param totalIters - the total number of iterations expected to have been executed
         * @param clipDestinationFolder - the folder where we should store the output clip if one is produced
         * @param compilationFlags - "true" or "false", telling us whether we should produce a trimmed clip
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
		
		String[] postCUJ = args[2].split("\\s*,\\s*");
		int totalIters = Integer.parseInt(args[3]);
		String clipDestinationFolder = args[4];
		String[] compilationFlags = args[5].split(" ");

		int flagCount = compilationFlags.length;
		int actionCount = postCUJ.length - 1;

		List<List<List<Integer>>> actionDurations = new ArrayList<>(flagCount);//3D list : i = flag, j = test index, k = action index
		List<List<Integer>> sizeData = new ArrayList<>(flagCount); //3D list : i = flag, j = test index
		List<Map<Integer, Utility.clipInfo>> totalDurationToClipInfo = new ArrayList<>(flagCount); //Mapping from durations to videoclip infos for every flag

		for (int i = 0; i < flagCount; i++) {
			actionDurations.add(new ArrayList<>());
			sizeData.add(new ArrayList<>());
			totalDurationToClipInfo.add(new HashMap<>());
		}

		//parse input file
		parseInputFile(myReader, flagCount, actionDurations, sizeData, totalDurationToClipInfo);

		
		//compute average/median durations and report missing data
		List<List<Integer>> averageActionDurations = new ArrayList<>(flagCount); //i = flag, j = action index
		List<List<Integer>> medianActionDurations = new ArrayList<>(flagCount);	//i = flag, j = action index

		ps.printf("TOTAL ITERATIONS PER FLAG: " + totalIters);
		for (int flagIdx = 0; flagIdx < flagCount; flagIdx++) {
			List<List<Integer>> flagData = actionDurations.get(flagIdx);
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
