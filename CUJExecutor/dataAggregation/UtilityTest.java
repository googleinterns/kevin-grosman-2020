
package dataAggregation;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;


public class UtilityTest {

	@Test
	public void sumGeneralCase() {
		List<Integer> list = Arrays.asList(1,2,3);

		assertEquals((Integer) 6, Utility.sum(list));
	}

	/*
	 * Averaging
	 */
        @Test
        public void averageGeneralCase() {
                List<Integer> list = Arrays.asList(1,2,6);

                assertEquals((Integer) 3, Utility.average(list));
        }

        @Test
        public void averageEmptyCase() {
                List<Integer> list = new ArrayList<>();

                assertEquals(null, Utility.average(list));
        }

        @Test
        public void averageColumnsGeneralCase() {
                List<List<Integer>> list =  Arrays.asList(Arrays.asList(1,2,6), Arrays.asList(2, 3, 5), Arrays.asList(4, 5, 1));
		List<Integer> expected = Arrays.asList(2, 3, 4);
		
                assertEquals(expected, Utility.averageColumns(list));
	}

	/*
	 * Medianing
	 */

	@Test
        public void medianOddLengthCase() {
                List<Integer> list  = Arrays.asList(1,2,6);

                assertEquals((Integer) 2, Utility.median(list));
        }

	@Test
        public void medianEvenLengthCase() {
                List<Integer> list = Arrays.asList(1,2,6,200);

                assertEquals((Integer) 6, Utility.median(list));
        }

	@Test
        public void medianEmptyLengthCase() {
                List<Integer> list = new ArrayList<>();

                assertEquals(null, Utility.median(list));
        }

        @Test
        public void medianColumnsGeneralCase() {
                List<List<Integer>> list =  Arrays.asList(Arrays.asList(1,2,6), Arrays.asList(2, 3, 5), Arrays.asList(4, 5, 1));
		List<Integer> expected = Arrays.asList(2, 3, 5);
		
                assertEquals(expected, Utility.medianColumns(list));
	}


	@Test
	public void readDurationTimePairGeneralCase() {
		String durations = "1, 23, 4, 56789";
		Integer sum = 1 + 23 + 4 + 56789;
		String timestamps = "12:34:56.789,  98:76:54.321";
		String fullVideoFolder = "/bestFolder";
		List<List<Integer>> allDurations = new ArrayList<>();
		Map<Integer, Utility.clipInfo> totalDurationToClipInfo = new HashMap<>();

		Utility.readDurationTimePair(durations, timestamps, fullVideoFolder, allDurations, totalDurationToClipInfo);

		//Check allDurations list
		assertEquals(1, allDurations.size());
		assertTrue(allDurations.contains(Arrays.asList(1, 23, 4, 56789, sum)));



		//Check totalDurationToClupInfo map
		assertEquals(1, totalDurationToClipInfo.size());
		assertTrue(totalDurationToClipInfo.containsKey(sum));
		Utility.clipInfo clip = totalDurationToClipInfo.get(sum);
		assertEquals("12:34:56.789", clip.getStart());
		assertEquals("98:76:54.321", clip.getEnd());
		assertEquals(fullVideoFolder, clip.getFolder());
	}



}
