
package dataAggregation;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;


public class AggregateExecuteCujTest {
	@Test
	public void parseInputFileTest() throws Exception {
		File inputFile = new File("dataAggregation/mockFiles/mockExecuteCujOutput.txt");
		Scanner myReader = new Scanner(inputFile);
		List<List<Integer>> actionDurations = new ArrayList<>();
		Map<Integer, Utility.clipInfo> totalDurationToClipInfo = new HashMap<>();
			
		List<Integer> durations1 = new ArrayList<>(Arrays.asList(1234, 4321));
		int sum1 = durations1.get(0) + durations1.get(1);
		durations1.add(sum1);
		String start1 = "12:34:56.789";
		String end1 = "98:76:54.321";


		List<Integer> durations2 = new ArrayList<>(Arrays.asList(1000, 2500));
		int sum2 = durations2.get(0) + durations2.get(1);
		durations2.add(sum2);
		String start2 = "00:00:24.806";
		String end2 = "00:00:31.190";

		List<Integer> durations3 = new ArrayList<> (Arrays.asList(4500, 50));
		int sum3 = durations3.get(0) + durations3.get(1);
		durations3.add(sum3);
		String start3 = "00:19:37.591";
		String end3 = "00:00:44.063";

		AggregateExecuteCuj.parseInputFile(myReader, actionDurations, totalDurationToClipInfo);


		//check actionDurations list
		assertEquals(3, actionDurations.size());
		assertTrue(actionDurations.contains(durations1));
		assertTrue(actionDurations.contains(durations2));
		assertTrue(actionDurations.contains(durations3));


		//check totalDurationToClipInfo
		assertEquals(3, totalDurationToClipInfo.size());
		assertTrue(totalDurationToClipInfo.containsKey(sum1));
		assertTrue(totalDurationToClipInfo.containsKey(sum2));
		assertTrue(totalDurationToClipInfo.containsKey(sum3));


		//check ClipInfos
		Utility.clipInfo clip1 = totalDurationToClipInfo.get(sum1);
		assertEquals(start1, clip1.getStart());
		assertEquals(end1, clip1.getEnd());
		assertEquals(".", clip1.getFolder());

		Utility.clipInfo clip2 = totalDurationToClipInfo.get(sum2);
		assertEquals(start2, clip2.getStart());
		assertEquals(end2, clip2.getEnd());
		assertEquals(".", clip2.getFolder());

		Utility.clipInfo clip3 = totalDurationToClipInfo.get(sum3);
		assertEquals(start3, clip3.getStart());
		assertEquals(end3, clip3.getEnd());
		assertEquals(".", clip3.getFolder());

	}



}
