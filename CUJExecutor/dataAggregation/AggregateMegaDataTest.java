

package dataAggregation;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;


public class AggregateMegaDataTest {
	private class TestIterationInfo {
		int flagIdx;
		int iterIdx;
		int size;
		String folder;
		List<Integer> durations;
		int totalDuration;
		String start;
		String end;

		public TestIterationInfo(int fi, int it, int si, String fo, List<Integer> du, String st, String en) {
			flagIdx = fi;
			iterIdx = it;
			size = si;
			folder = fo;
			durations = new ArrayList<>(du); //so we can modify it
			start = st;
			end = en;

			totalDuration = 0;
			for (Integer n : du) totalDuration += n;
		}

		
		public String toString() {
			return "flagIdx: " + flagIdx + " size: " + size + " folder: " + folder + " durations: " + durations + " totalDuration : " + totalDuration + " start: " + start + " end: " + end;
		}
	}

	@Test
	public void parseInputFileGeneralCase() throws Exception {
		File inputFile = new File("dataAggregation/mockFiles/mockMegaTestOutput.txt");
		Scanner myReader = new Scanner(inputFile);
		Integer flagCount = 2;


		List<TestIterationInfo> testIterations = new ArrayList<>();

		testIterations.add(new TestIterationInfo(0, 0, 45080, "flag0/iter0", Arrays.asList(1234567, 76543210), "12:34:56.789", "98:76:54.321"));
		testIterations.add(new TestIterationInfo(0, 1, 45080, "flag0/iter0", Arrays.asList(1938, 3044), "00:00:40.488", "00:00:47.970"));
		testIterations.add(new TestIterationInfo(1, 0, 170908, "flag1/iter0", Arrays.asList(1575, 2337), "00:00:25.233", "00:00:31.645"));
		testIterations.add(new TestIterationInfo(1, 1, 170908, "flag1/iter0", Arrays.asList(1505, 2339), "00:00:38.711", "00:00:45.055"));
		testIterations.add(new TestIterationInfo(0, 2, 45081, "flag0/iter1", Arrays.asList(1949, 2786), "00:00:26.097", "00:00:33.332"));
		testIterations.add(new TestIterationInfo(0, 3, 45081, "flag0/iter1", Arrays.asList(1911, 2918), "00:00:39.975", "00:00:47.304"));
		testIterations.add(new TestIterationInfo(1, 2, 170909, "flag1/iter1", Arrays.asList(1635, 2316), "00:00:25.765", "00:00:32.216"));
		testIterations.add(new TestIterationInfo(1, 3, 170909, "flag1/iter1", Arrays.asList(1580, 2339), "00:00:39.301", "00:00:45.720"));



		List<List<Utility.IterationInfo>> allIterationInfos = new ArrayList<>();
		for (int i = 0; i < flagCount; i++) {
			allIterationInfos.add(new ArrayList<>());
		}

		AggregateMegaData.parseInputFile(myReader, flagCount, allIterationInfos);


		

	
		//check totalDurationToClipInfo
		

		String errorStr = "failed on iteration with the following data: ";

		for (TestIterationInfo testInfo : testIterations) {
			Utility.IterationInfo iterationInfo = allIterationInfos.get(testInfo.flagIdx).get(testInfo.iterIdx); 
			assertEquals(errorStr + testInfo.toString(), (Integer) testInfo.size, iterationInfo.getSize());
			assertEquals(errorStr + testInfo.toString(), testInfo.durations, iterationInfo.getDurations());
			assertEquals(errorStr + testInfo.toString(), (Integer) testInfo.totalDuration, iterationInfo.getTotalDuration());
			assertEquals(errorStr + testInfo.toString(), testInfo.folder, iterationInfo.getClipInfo().getFolder());	
			assertEquals(errorStr + testInfo.toString(), testInfo.start, iterationInfo.getClipInfo().getStart());	
			assertEquals(errorStr + testInfo.toString(), testInfo.end, iterationInfo.getClipInfo().getEnd());	
		}


	}
}




