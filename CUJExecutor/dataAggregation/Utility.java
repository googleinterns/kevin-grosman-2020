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
   static class clipInfo {
	String folder; //the folder where the clip is located (should be called "full_video.mp4)
	String start; //the time at which this clip begins in the video (in the format HH:MM:SS.sss)
	String end; //the time at which this clip ends in the video (in the format HH:MM:SS.sss)

	public clipInfo (String fldr, String timestamps) {
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
   * calculates the average of each column
   * ith entry of returned array is average of ith column of input
   */
    public static List<Integer> averageColumns(List<List<Integer>> mat) {
	if (mat.size() == 0) return null;
	List<Integer> averages = new ArrayList<>();
        for (int j = 0; j < mat.get(0).size(); j++) {
            int sum = 0;
            for (int i = 0; i < mat.size(); i++) {
                sum += mat.get(i).get(j);
            }
            averages.add(sum / mat.size());
        }
        return averages;
    }

    /**
     * Returns the median of the provided list
     * If list is even, returns the greater of the two middle elements
     * if list is empty, return null
     */
    public static Integer median(List<Integer> list) {
	    if (list.size() == 0) return null;
	    Collections.sort(list);
	    return list.get(list.size() / 2);
	    
	    /*if (list.size() % 2 == 1) {
	    } else {
		    return (list.get(list.size() / 2 - 1) + list.get(list.size() / 2)) / 2;
	    }*/
    }

   /**
     * calculates the median of each column
     * ith entry of returned array is median of ith column of input
     */
    public static List<Integer> medianColumns(List<List<Integer>> mat) {
        if (mat.size() == 0) return null;
	List<Integer> medians = new ArrayList<>();
        for (int j = 0; j < mat.get(0).size(); j++) {
            int[] column = new int[mat.size()];
            for (int i = 0; i < mat.size(); i++) {
                column[i] = mat.get(i).get(j);
            }
            Arrays.sort(column);
            medians.add(column[column.length / 2]);
            /*if (column.length % 2 == 1) {
            } else {
                medians.add((column[column.length / 2] + column[(column.length / 2) - 1]) / 2);
            }*/
        }
        return medians;
    }	
    
    /**
     * Prints str to the provided printStream in a column with the width specified by alignment
     */
    public static void printWithAlignment(PrintStream ps, int alignment, String str) {
	ps.printf("%-" + (alignment + 1) + "s", str);
    }




    /**
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
     * Parses the list specified durations and adds it to the allDurations list.
     * Also creates a clipInfo object for the CUJ iteration in question and maps the iteration's total duration to 
     * this clipInfo object in the passed totalDurationToClipInfo map.
     *
     * @param durations - a comma-seperated list of durations of each measured action for this particular iteration through the CUJ.
     * @param timestamps - a comma-seperated pair of timestamps specifying the beginning and end of this iteration through the CUJ in the full video of it (located at fullVideoFolder).
     * @param fullVideoFolder the folder holding the full video that contains the iteration in question (if such a recording exists)
     * @param allDurations[out] - a list duration-lists, to which the data held by durations is to be added.
     * @param totalDurationToClipInfo[out] - a map from total iteration durations to clipInfos of clips with that total duration. The data from the iteration in question is to be added to this map.
     *
     */
    public static void readDurationTimePair(String durations, String timestamps, String fullVideoFolder, List<List<Integer>> allDurations, Map<Integer, clipInfo> totalDurationToClipInfo) {
		String[] durationArr = durations.split("\\s*,\\s*");
		List<Integer> durationList = new ArrayList<>();
		for (String s : durationArr) {
			durationList.add(Integer.parseInt(s.trim()));
		}
		Integer totalDuration = sum(durationList);
		durationList.add(totalDuration);

		allDurations.add(durationList);
		clipInfo curClipInfo = new clipInfo(fullVideoFolder, timestamps);
		totalDurationToClipInfo.put(totalDuration, curClipInfo);
	}

	public static void trimClip(clipInfo clip, String destinationFile) {
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
