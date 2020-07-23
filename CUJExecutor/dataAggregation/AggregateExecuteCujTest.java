
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
		List<Utility.IterationInfo> allIterationInfos = new ArrayList<>();
			
		List<Integer> durations1 = new ArrayList<>(Arrays.asList(1234, 4321));
		int sum1 = durations1.get(0) + durations1.get(1);
		String start1 = "12:34:56.789";
		String end1 = "98:76:54.321";


		List<Integer> durations2 = new ArrayList<>(Arrays.asList(1000, 2500));
		int sum2 = durations2.get(0) + durations2.get(1);
		String start2 = "00:00:24.806";
		String end2 = "00:00:31.190";

		List<Integer> durations3 = new ArrayList<> (Arrays.asList(4500, 50));
		int sum3 = durations3.get(0) + durations3.get(1);
		String start3 = "00:19:37.591";
		String end3 = "00:00:44.063";

		AggregateExecuteCuj.parseInputFile(myReader, allIterationInfos);


		//check allIterationInfos
		Utility.IterationInfo iteration1 = allIterationInfos.get(0);	
		assertEquals(durations1, iteration1.getDurations());
		assertEquals((Integer) sum1, iteration1.getTotalDuration());
		Utility.ClipInfo clip1 = iteration1.getClipInfo();
		assertEquals(start1, clip1.getStart());
		assertEquals(end1, clip1.getEnd());
		assertEquals(".", clip1.getFolder());

		Utility.IterationInfo iteration2 = allIterationInfos.get(1);	
		assertEquals(durations2, iteration2.getDurations());
		assertEquals((Integer) sum2, iteration2.getTotalDuration());
		Utility.ClipInfo clip2 = iteration2.getClipInfo();
		assertEquals(start2, clip2.getStart());
		assertEquals(end2, clip2.getEnd());
		assertEquals(".", clip2.getFolder());

		Utility.IterationInfo iteration3 = allIterationInfos.get(2);	
		assertEquals(durations3, iteration3.getDurations());
		assertEquals((Integer) sum3, iteration3.getTotalDuration());
		Utility.ClipInfo clip3 = iteration3.getClipInfo();
		assertEquals(start3, clip3.getStart());
		assertEquals(end3, clip3.getEnd());
		assertEquals(".", clip3.getFolder());
	}
}
