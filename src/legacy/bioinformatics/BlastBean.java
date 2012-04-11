package legacy.bioinformatics;

public class BlastBean implements Comparable
{
	public int compareTo(Object o) {
		// TODO Auto-generated method stub	
		Float f = ((BlastBean)o).getScore().floatValue();
		//return -1 times reverse comparison to preserve java compareTo conventions.
		return -1*f.compareTo(getScore().floatValue());
	}
	String taxonomy;

	public int taxid;
	public String getTaxonomy() {
		return taxonomy;
	}
	/**
	 * @return the taxid
	 */
	public int getTaxid() {
		return this.taxid;
	}
	/**
	 * @param taxid the taxid to set
	 */
	public void setTaxid(int taxid) {
		this.taxid = taxid;
	}
	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}
	protected String id,name,sequence;Number score;
	
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#getRefseq()
	 */
	public String getId() 
	{
		return id;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#setRefseq(java.lang.String)
	 */
	public void setId(String refseq) {
		this.id = refseq;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#getScore()
	 */
	public Number getScore() {
		return score;
	}
	/* (non-Javadoc)
	 * @see com.connjur.utils.bioinformatics.IBlastBean#setScore(int)
	 */
	public void setScore(Number line) {
		this.score = line;
	}
	public String toString()
	{
		return "("+id + ") " + name + " with score " + score;
	}
	
}