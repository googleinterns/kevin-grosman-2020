package dataAggregation;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.*;
import java.io.*;


class Utility {

   static class clipInfo {
	String folder;
	String timeStamps;
	String startTime;
	String endTime;

	public clipInfo (String f, String t) {
		folder = f;
		timeStamps = t;
		String[] startEnd = timeStamps.split("\\s*,\\s*");
		startTime = startEnd[0];
		endTime = startEnd[1];
	}
	public String getFolder() {
		return folder;
	}
	public String getStart() {
		return startTime;
	}

	public String getEnd() {
		return endTime;
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

    public static void printWithAlignment(PrintStream ps, int alignment, String str) {
	ps.printf("%-" + (alignment + 1) + "s", str);
    }





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



    //Return true if read was successful
    public static void readDurationTimePair(String durations, String timestamps, String fullVideoFolder, List<List<Integer>> allData, Map<Integer, clipInfo> totalDurationToClipInfo) {
		String[] durationArr = durations.split("\\s*,\\s*");
		List<Integer> durationList = new ArrayList<>();
		for (String s : durationArr) {
			durationList.add(Integer.parseInt(s.trim()));
		}
		Integer totalDuration = sum(durationList);
		durationList.add(totalDuration);

		allData.add(durationList);
		clipInfo curClipInfo = new clipInfo(fullVideoFolder, timestamps);
		totalDurationToClipInfo.put(totalDuration, curClipInfo);
	}

	public static void trimVideoWithMedianTotalDuration(List<Integer> medianActionDurations, Map<Integer, clipInfo> totalDurationToClipInfo, String destinationFile) {
		if (medianActionDurations != null) {
			Integer medianTotalDuration = medianActionDurations.get(medianActionDurations.size() - 1);
			clipInfo medianClipInfo = totalDurationToClipInfo.get(medianTotalDuration);
			String fullClipLoc = medianClipInfo.getFolder() + "/full_video.mp4";
			File fullClip = new File(fullClipLoc);
			System.out.println("Picked with length " + medianTotalDuration + " (starting at " + medianClipInfo.getStart() + " and ending at " + medianClipInfo.getEnd());
			if (fullClip.exists()) {
				splitVideo(fullClipLoc, medianClipInfo.getStart(), medianClipInfo.getEnd(), destinationFile);
			}
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

	// -- Windows --

	// Run a command
	//processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");
	
	// Run a bat file
	//processBuilder.command("C:\\Users\\mkyong\\hello.bat");

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


    public static void splitVideo(String source, String from, String to, String destination) {
	executeShellCommand("ffmpeg -i " + source + " -force_key_frames:v " + from + "," + to + " -acodec copy -map 0 -f segment -segment_times " + from + "," + to + " -reset_timestamps 1 -y temp%d.mp4");
	executeShellCommand("mv temp1.mp4 " + destination);
    }




}
