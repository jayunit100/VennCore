package datatype;

import org.junit.Test;

import datatypes.VennRestraint;

/** 
 * Used in conjunction with CS-Rosetta for extracting interatomic distance restraints from
 * an ab initio foldded protein set.
 * @author vyas
 *
 */
public class TestVennRestraint {
	
	@Test
	public void test(){
		VennRestraint vr = new VennRestraint();
		vr.setRes1atom("HA");
		vr.setRes2atom("HB");
		System.out.println(vr);
		System.out.println(vr.toStringXplor());
	}
	
}