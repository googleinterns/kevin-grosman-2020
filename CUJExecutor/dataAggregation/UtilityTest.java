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
        public void averageActionDurationsGeneralCase() {
		Utility.IterationInfo iteration0 = new Utility.IterationInfo(null, Arrays.asList(1,2,8), null);
		Utility.IterationInfo iteration1 = new Utility.IterationInfo(null, Arrays.asList(3,3,3), null);
		Utility.IterationInfo iteration2 = new Utility.IterationInfo(null, Arrays.asList(4,5,1), null);
		Utility.IterationInfo iteration3 = new Utility.IterationInfo(null, Arrays.asList(-3,2,5), null);

		List<Utility.IterationInfo> list = Arrays.asList(iteration0, iteration1, iteration2, iteration3);

                assertEquals(Arrays.asList(1, 3, 4), Utility.averageActionDurations(list));
	}


        @Test
        public void averageSizeGeneralCase() {
		Utility.IterationInfo iteration0 = new Utility.IterationInfo(null, Arrays.asList(1,2,8), 1000);
		Utility.IterationInfo iteration1 = new Utility.IterationInfo(null, Arrays.asList(3,3,3), 2000);
		Utility.IterationInfo iteration2 = new Utility.IterationInfo(null, Arrays.asList(4,5,1), 3000);
		Utility.IterationInfo iteration3 = new Utility.IterationInfo(null, Arrays.asList(-3,2,5), 10000);

		List<Utility.IterationInfo> list = Arrays.asList(iteration0, iteration1, iteration2, iteration3);

                assertEquals((Integer) 4000, Utility.averageSize(list));
	}





	/*
	 * Medianing
	 */

	@Test
        public void medianActionDurationsGeneralCase() {
		Utility.IterationInfo iteration0 = new Utility.IterationInfo(null, Arrays.asList(1,2,8), null);
		Utility.IterationInfo iteration1 = new Utility.IterationInfo(null, Arrays.asList(3,3,3), null);
		Utility.IterationInfo iteration2 = new Utility.IterationInfo(null, Arrays.asList(4,5,1), null);
		Utility.IterationInfo iteration3 = new Utility.IterationInfo(null, Arrays.asList(-3,2,5), null);

		List<Utility.IterationInfo> list = Arrays.asList(iteration0, iteration1, iteration2, iteration3);

		assertEquals(Arrays.asList(3, 3, 5), Utility.medianActionDurations(list));
        }
		

	@Test
        public void iterationWithMedianTotalDurationGeneralCase() {
		Utility.IterationInfo iteration0 = new Utility.IterationInfo(null, Arrays.asList(1,2,8), null);
		Utility.IterationInfo iteration1 = new Utility.IterationInfo(null, Arrays.asList(3,3,3), null);
		Utility.IterationInfo iteration2 = new Utility.IterationInfo(null, Arrays.asList(4,5,1), null);
		Utility.IterationInfo iteration3 = new Utility.IterationInfo(null, Arrays.asList(-3,2,5), null);

		List<Utility.IterationInfo> list = Arrays.asList(iteration0, iteration1, iteration2, iteration3);

		assertEquals(iteration2, Utility.iterationWithMedianTotalDuration(list));

        }

	@Test
        public void iterationWithMedianSizeGeneralCase() {
		Utility.IterationInfo iteration0 = new Utility.IterationInfo(null, Arrays.asList(1,2,8), 123);
		Utility.IterationInfo iteration1 = new Utility.IterationInfo(null, Arrays.asList(3,3,3), 456);
		Utility.IterationInfo iteration2 = new Utility.IterationInfo(null, Arrays.asList(4,5,1), 1000);
		Utility.IterationInfo iteration3 = new Utility.IterationInfo(null, Arrays.asList(-3,2,5), 7890);

		List<Utility.IterationInfo> list = Arrays.asList(iteration0, iteration1, iteration2, iteration3);

		assertEquals((Integer) 1000, Utility.medianSize(list));

        }



	/*
	 * Reading Duration/Time pairs
	 */

	@Test
	public void readDurationTimePairGeneralCase() {
		String durations = "1, 23, 4, 56789";
		Integer sum = 1 + 23 + 4 + 56789;
		Integer size = 9000;
		String timestamps = "12:34:56.789,  98:76:54.321";
		String fullVideoFolder = "/bestFolder";
		List<Utility.IterationInfo> allIterationInfos = new ArrayList<>();

		Utility.readDurationTimePair(durations, timestamps, size, fullVideoFolder, allIterationInfos);

		//Check allIterationInfos list
		assertEquals(1, allIterationInfos.size());
		assertEquals(Arrays.asList(1, 23, 4, 56789), allIterationInfos.get(0).getDurations());
		assertEquals(sum, allIterationInfos.get(0).getTotalDuration());
		assertEquals((Integer) 9000, allIterationInfos.get(0).getSize());
		Utility.ClipInfo clip = allIterationInfos.get(0).getClipInfo();
		assertEquals("12:34:56.789", clip.getStart());
		assertEquals("98:76:54.321", clip.getEnd());
		assertEquals(fullVideoFolder, clip.getFolder());
	}
}
