/**
 * 
 */
package services;

import legacy.bioinformatics.GPdbUtils;

import org.biojava.bio.structure.Structure;

/**
 * All structures are wrapped by this class.
 * 
 * The contents are stored as a PDB string.
 * 
 * The VennId is 0 for PDB structures.
 * VennIds are assigned to custom structures, as well.
 * 
 * Thus the true "key" for this class is both the pdbid and the venn id.
 * 
 * For example, we might have 1AVZ, and 1AVZ made by interfaceMiner.
 * 
 * In that case, the venn ids, respectively, would be 0 and 100210010, for example.
 * @author jayunit100
 *
 */
public class VennStructure
{
	  public String pdbContents;
	  
	  public Structure getStructure()
	  {
		  if(pdbContents==null)
		  {
			  System.out.println("VennStructure : Major error, getStructure called, but no contents. Could be a Corrupt database .");
			  return null;
		  }
		  else
		  {
			  return GPdbUtils.parseStructure(this.pdbContents);
		  }
	  }
	  
	  public String pdbid;
	  public Long vennId;
	
	public VennStructure()
	{
		
	}
	
	/**
	 * @param structure
	 * @param pdbid
	 */
	public VennStructure(String pdbContents, String pdbid, Long vennId) {
		super();
		//may be null
		this.pdbid = pdbid;
		
		//must not be null 
		this.pdbContents=pdbContents;
		//may be null, in which case, assign it a unique Timestamp id.
		//this is used as the lookup .
		if(vennId==null || vennId==0)
		{
			this.vennId=System.currentTimeMillis();
		}
		else
			this.vennId=vennId;
	
	}
	/**
	 * @return the vennId
	 */
	public long getVennId() {
		return this.vennId;
	}

	/**
	 * @param vennId the vennId to set
	 */
	public void setVennId(long vennId) {
		this.vennId = vennId;
	}

	/**
	 * @return the pdbid
	 */
	public String getPdbid() {
		return this.pdbid;
	}
	/**
	 * @param pdbid the pdbid to set
	 */
	public void setPdbid(String pdbid) {
		this.pdbid = pdbid;
	}
	
	public String toString()
	{
		return this.getPdbid()+" "+this.getVennId();
	}
}
