import java.util.HashSet;
import java.util.Vector;

import services.AlignmentUtil;
import services.VennMatrix;


public class TestAlignmentUtil {

	@Test
	public void test()
	{
		String main = "AMAALAAAAALAAALAAAAA";
		AlignmentUtil a =
			new AlignmentUtil
			(		
					new VennMatrix().matrix,
					-3,
					main, 
					new HashSet<Character>(),
					new String[]  
					        {
								"AMAAMAAAALAAAAAAAAAAAM", 
					            "AAMAAAAAAAALAAAAAAAAA"
							}
					
			);
		
		Vector<Float> scores=a.getScoresOrderedByResidue();
		System.out.println("Done " + scores.size());

		for(int i = 0 ; i < scores.size();i++)
		{
			System.out.println(i + " " + scores.get(i));
		}
		System.out.println(a);
	}
}
