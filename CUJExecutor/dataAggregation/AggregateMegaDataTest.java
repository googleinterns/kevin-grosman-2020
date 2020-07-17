

package dataAggregation;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;


public class AggregateMegaDataTest {
	private class iterationInfo {
		int flagIdx;
		int size;
		String folder;
		List<Integer> durations;
		int totalDuration;
		String start;
		String end;

		public iterationInfo(int fi, int si, String fo, List<Integer> du, String st, String en) {
			flagIdx = fi;
			size = si;
			folder = fo;
			durations = new ArrayList<>(du); //so we can modify it
			start = st;
			end = en;

			totalDuration = 0;
			for (Integer n : du) totalDuration += n;
			durations.add(totalDuration);
		}

		
		public String toString() {
			return "flagIdx: " + flagIdx + " size: " + size + " folder: " + folder + " durations: " + durations + " totalDuration : " + totalDuration + " start: " + start + " end: " + end;
		}
	}

	@Test
	public void parseInputFileGeneralCase() throws Exception {
		File inputFile = new File("dataAggregation/mockFiles/mockMegaTestOutput.txt");
		Scanner myReader = new Scanner(inputFile);
		List<List<List<Integer>>> actionDurations = new ArrayList<>();
		List<List<Integer>> sizeData = new ArrayList<>();
		List<Map<Integer, Utility.clipInfo>> totalDurationToClipInfo = new ArrayList<>();
		Integer flagCount = 2;


		List<iterationInfo> iterations = new ArrayList<>();

		iterations.add(new iterationInfo(0, 45080, "flag0/iter0", Arrays.asList(1234567, 76543210), "12:34:56.789", "98:76:54.321"));
		iterations.add(new iterationInfo(0, 45080, "flag0/iter0", Arrays.asList(1938, 3044), "00:00:40.488", "00:00:47.970"));
		iterations.add(new iterationInfo(1, 170908, "flag1/iter0", Arrays.asList(1575, 2337), "00:00:25.233", "00:00:31.645"));
		iterations.add(new iterationInfo(1, 170908, "flag1/iter0", Arrays.asList(1505, 2339), "00:00:38.711", "00:00:45.055"));
		iterations.add(new iterationInfo(0, 45081, "flag0/iter1", Arrays.asList(1949, 2786), "00:00:26.097", "00:00:33.332"));
		iterations.add(new iterationInfo(0, 45081, "flag0/iter1", Arrays.asList(1911, 2918), "00:00:39.975", "00:00:47.304"));
		iterations.add(new iterationInfo(1, 170909, "flag1/iter1", Arrays.asList(1635, 2316), "00:00:25.765", "00:00:32.216"));
		iterations.add(new iterationInfo(1, 170909, "flag1/iter1", Arrays.asList(1580, 2339), "00:00:39.301", "00:00:45.720"));


		for (int i = 0; i < flagCount; i++) {
			actionDurations.add(new ArrayList<>());
			sizeData.add(new ArrayList<>());
			totalDurationToClipInfo.add(new HashMap<>());
		}

		AggregateMegaData.parseInputFile(myReader, flagCount, actionDurations, sizeData, totalDurationToClipInfo);



		//check actionDurations list
		assertEquals(2, actionDurations.size());
		for (iterationInfo info : iterations) {
			assertTrue("failed on iteration with the following data: " + info.toString(), actionDurations.get(info.flagIdx).contains(info.durations));
		}

		//check sizeData list
		assertEquals(2, sizeData.size());
		for (iterationInfo info : iterations) {
			assertTrue("failed on iteration with the following data: " + info.toString(), sizeData.get(info.flagIdx).contains(info.size));
		}
	
		//check totalDurationToClipInfo
		assertEquals(2, totalDurationToClipInfo.size());
		for (iterationInfo info : iterations) {
			assertTrue("failed on iteration with the following data: " + info.toString(), totalDurationToClipInfo.get(info.flagIdx).containsKey(info.totalDuration));
			Utility.clipInfo clip = totalDurationToClipInfo.get(info.flagIdx).get(info.totalDuration);
			assertEquals("failed on iteration with the following data: " + info.toString(), info.start, clip.getStart());
			assertEquals("failed on iteration with the following data: " + info.toString(), info.end, clip.getEnd());
			assertEquals("failed on iteration with the following data: " + info.toString(), info.folder, clip.getFolder());
		}


	}
}




