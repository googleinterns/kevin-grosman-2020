package dataAggregation;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.*;
import java.io.*;


class Utility {

	/**
	 * Provides the info necessary to identify a section of a video (a clip)
	 */
   static class ClipInfo {
	private String folder; 			//the folder where the clip is located (should be called "full_video.mp4)
	private String start; 			//the time at which this clip begins in the video (in the format HH:MM:SS.sss)
	private String end; 			//the time at which this clip ends in the video (in the format HH:MM:SS.sss)

	public ClipInfo (String fldr, String timestamps) {
		folder = fldr;
		String[] startEnd = timestamps.split("\\s*,\\s*");
		start = startEnd[0];
		end = startEnd[1];
	}
	public String getFolder() {
		return folder;
	}
	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}
   }

   /*
    * Stores all the information relevant to an iteration through a CUJ
    */
   static class IterationInfo {
	private ClipInfo clipInfo; 		//Information regarding the clip of this CUJ
	private List<Integer> durations; 	//The duration of the measurement actions executed
	private Integer totalDuration; 		//The total duration of this iteration
	private Integer size; 			//The size of the app when this iteration was run

	public IterationInfo(ClipInfo iterationClipInfo, List<Integer> iterationDurations, Integer iterationSize) {
		clipInfo = iterationClipInfo;
		durations = iterationDurations;
		totalDuration = sum(durations);
		size = iterationSize;
	}

	public ClipInfo getClipInfo() {
		return clipInfo;
	}

	public List<Integer> getDurations() {
		return durations;
	}

	public Integer getTotalDuration() {
		return totalDuration;
	}

	public Integer getSize() {
		return size;
	}
	
   }
   


   public static Integer sum(List<Integer> list) {
	   int sum = 0;
	   for (int n : list) {
		   sum += n;
	   }
	   return sum;
   }

   public static Integer average(List<Integer> list) {
	   if (list.size() == 0) return null;
	   return (sum(list) / list.size());
   }


	/**
	 *
	 * Given a list of IterationInfos containing "durations" lists of the same length (data for the same number of actions), returns a list of the average duration for each action
	 * if allIterationInfos is empty, returns null
	 *
	 */
    public static List<Integer> averageActionDurations (List<IterationInfo> allIterationInfos) {
	if (allIterationInfos.size() == 0) return null;
	List<Integer> averageDurations = new ArrayList<>();
	int actionCount = allIterationInfos.get(0).getDurations().size();
	for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
		List<Integer> actionDurations = new ArrayList<>();
		for (IterationInfo iter : allIterationInfos) {
			actionDurations.add(iter.getDurations().get(actionIdx));
		}
		averageDurations.add(average(actionDurations));
	}	
	return averageDurations;
    }


    /*
     * Returns the average size field for a list of IterationInfos
     * if passed list is empty, returns null
     */
    public static Integer averageSize(List<IterationInfo> allIterationInfos) {
	    if (allIterationInfos.size() == 0) return null;
	    List<Integer> sizes = new ArrayList<>();
	    for (IterationInfo curIterationInfo : allIterationInfos) {
		    sizes.add(curIterationInfo.getSize());
	    }
	    return average(sizes);
    }




    /*
     * Returns the median (with a lean towards the higher value if size is even) in a list of IterationInfos given a comparator
     */
    private static IterationInfo medianIterationInfo(List<IterationInfo> list, Comparator<IterationInfo> comparator) {
	    if (list.size() == 0) return null;
	    Collections.sort(list, comparator);
	    return list.get(list.size() / 2);
    }

    /*
     * Returns the iterationInfo with the median duration for the action specified by idx
     */
    public static IterationInfo iterationWithMedianDurationAtIdx(List<IterationInfo> allIterationInfos, int idx) {
	    return medianIterationInfo(allIterationInfos,  new Comparator<IterationInfo>() {
			@Override
			public int compare(IterationInfo a, IterationInfo b) {
				return a.getDurations().get(idx).compareTo(b.getDurations().get(idx));
			}});
    }

    /*
     *  Returns the iterationInfo with the median total duration
     */
    public static IterationInfo iterationWithMedianTotalDuration(List<IterationInfo> allIterationInfos) {
	    return medianIterationInfo(allIterationInfos, new Comparator<IterationInfo>() {
		    @Override
		    public int compare(IterationInfo a, IterationInfo b) {
			    return a.getTotalDuration().compareTo(b.getTotalDuration());
		    }});
    }
    
    /*
     * Returns the median corresponding app size
     */
    public static IterationInfo iterationWithMedianSize(List<IterationInfo> allIterationInfos) {
	    return medianIterationInfo(allIterationInfos, new Comparator<IterationInfo>() {
		    @Override
		    public int compare(IterationInfo a, IterationInfo b) {
			    return a.getSize().compareTo(b.getSize());
		    }});
    }

    


	
    /*
     * Given a list of IterationInfos containing "durations" lists of the same length (data for the same number of actions), returns a list of the median duration for each action
     * if allIterationInfos is empty, returns null
     */

    public static List<Integer> medianActionDurations (List<IterationInfo> allIterationInfos) {
	if (allIterationInfos.size() == 0) return null;
	int actionCount = allIterationInfos.get(0).getDurations().size();
	List<Integer> medianDurations = new ArrayList<>();
	for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
		IterationInfo medianForIdx = iterationWithMedianDurationAtIdx(allIterationInfos, actionIdx);		 	    
		medianDurations.add(medianForIdx.getDurations().get(actionIdx));
	}
	return medianDurations;
    }

    /*
     * Returns the median app size out of all of the passed iterations
     * if the passed list is empty, returns null
     */
    public static Integer medianSize(List<IterationInfo> allIterationInfos) {
	if (allIterationInfos.size() == 0) return null;
	return iterationWithMedianSize(allIterationInfos).getSize();
    }



    /*
     * Prints str to the provided printStream in a column with the width specified by alignment
     */
    public static void printWithAlignment(PrintStream ps, int alignment, String str) {
	ps.printf("%-" + (alignment + 1) + "s", str);
    }




    /*
     * prints the provided (possible non-rectangular) list of lists specified by table in a grid, such that
     * the width of each column is 1 greater than the longest string in that column (and each row has a height of 1)
     */
    public static void printFormattedTable(List<List<String>> table, PrintStream ps) {
	    	//calculate number of columns
		int cols = 0;
		for (List<String> row : table) cols = Math.max(cols, row.size());

		//Calculate widest string in each column
		int[] colAlignment = new int[cols];
		Arrays.fill(colAlignment, 0);
		for (int i = 0; i < table.size(); i++) {
			for (int j = 0; j < table.get(i).size(); j++) {
				colAlignment[j] = Math.max(colAlignment[j], table.get(i).get(j).length());
			}
		}

		for (int i = 0; i < table.size(); i++) {
			ps.append('\n');
			for (int j = 0; j < table.get(i).size(); j++) {
				printWithAlignment(ps, colAlignment[j], table.get(i).get(j));
			}
		}


    }



    /**
     * Parses the passed data into an IterationInfo object holding a clipInfo object and then adds it to the allIterationInfos list
     *
     * @param durations - a comma-seperated list of durations of each measured action for this particular iteration through the CUJ.
     * @param timestamps - a comma-seperated pair of timestamps specifying the beginning and end of this iteration through the CUJ in the full video of it (located at fullVideoFolder).
     * @param size - the size of the app on this particular run through the CUJ
     * @param fullVideoFolder the folder holding the full video that contains the iteration in question (if such a recording exists)
     * @param allIterationInfos[out] - a list of IterationInfo objects
     *
     */
    	public static void readDurationTimePair(String durations, String timestamps, Integer size, String fullVideoFolder, List<IterationInfo> allIterationInfos) {
		String[] durationArr = durations.split("\\s*,\\s*");
		List<Integer> durationList = new ArrayList<>();
		for (String s : durationArr) {
			durationList.add(Integer.parseInt(s.trim()));
		}
		ClipInfo curClipInfo = new ClipInfo(fullVideoFolder, timestamps);
		allIterationInfos.add(new IterationInfo(curClipInfo, durationList, size));
	}

	public static void trimClip(ClipInfo clip, String destinationFile) {
		String fullClipLoc = clip.getFolder() + "/full_video.mp4";
		File fullClip = new File(fullClipLoc);
		System.out.println("Picked with length starting at " + clip.getStart() + " and ending at " + clip.getEnd());
		if (fullClip.exists()) {
			executeFFmpegTrim(fullClipLoc, clip.getStart(), clip.getEnd(), destinationFile);
		}
	}


    public static void executeShellCommand(String str) {
	ProcessBuilder processBuilder = new ProcessBuilder();

	// -- Linux --

	// Run a shell command
	String[] args = str.split("\\s+");
	System.out.println(Arrays.toString(args));

	processBuilder.command(Arrays.asList(args));
	// Run a shell script
	//processBuilder.command("path/to/hello.sh");


	try {

	Process process = processBuilder.start();

	StringBuilder output = new StringBuilder();

	BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream()));

	String line;
	while ((line = reader.readLine()) != null) {
		output.append(line + "\n");
	}

	int exitVal = process.waitFor();
	if (exitVal == 0) {
		System.out.println("Success!");
		System.out.println(output);
		//System.exit(0);
	} else {
		//abnormal...
	}

	} catch (IOException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    }


    public static void executeFFmpegTrim(String source, String from, String to, String destination) {
	executeShellCommand("ffmpeg -i " + source + " -force_key_frames:v " + from + "," + to + " -acodec copy -map 0 -f segment -segment_times " + from + "," + to + " -reset_timestamps 1 -y temp%d.mp4");
	executeShellCommand("mv temp1.mp4 " + destination);
    }




}
