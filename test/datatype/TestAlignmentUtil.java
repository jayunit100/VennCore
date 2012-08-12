package datatype;

import java.util.HashSet;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

import services.AlignmentUtil;
import services.VennMatrix;

public class TestAlignmentUtil {

	@Test
	public void main() {
		String main = "AMAALAAAAALAAALAAAAA";
		AlignmentUtil a = new AlignmentUtil(new VennMatrix().matrix, -3, main,
				new HashSet<Character>(), new String[] {
						"AMAAMAAAALAAAAAAAAAAAM", "AAMAAAAAAAALAAAAAAAAA" }

		);

		Vector<Float> scores = a.getScoresOrderedByResidue();
		System.out.println("Done " + scores.size());

		Assert.assertEquals(20, scores.size());
		Assert.assertEquals(new Float(1.0), scores.get(18));

	}
}
