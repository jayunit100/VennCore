/**
 * 
 */
package services;

import java.util.List;

import legacy.bioinformatics.BlastBean;
import legacy.bioinformatics.GPdbUtils;

import org.biojava.bio.structure.Structure;

/**
 * @author jayunit100
 *
 */
public class VennScript {
	
	public String structure;
	public String script;
	public List<BlastBean> homologs;
	/**
	 * @param structure
	 * @param script
	 */
	public VennScript(String structure, String script, List<BlastBean> hom) {
		super();
		
		
		System.out.println("VennScript, Trying to create a structure... just to validate....");
		Structure s = GPdbUtils.parseStructure(structure);
		System.out.println("Validation worked.");
		this.structure = structure;
			
		this.script = script;

		this.homologs=hom;
	}
	
}
