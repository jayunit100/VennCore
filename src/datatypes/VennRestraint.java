package datatypes;

import java.text.DecimalFormat;

/** 
 * Used in conjunction with CS-Rosetta for extracting interatomic distance restraints from
 * an ab initio foldded protein set.
 * @author vyas
 *
 */
public class VennRestraint {
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getColumns()
	 */
	public String[] getColumns()
	{
		return new String[]{"res1Num","res1Name","res1atom","res2Num","res2name","res2atom","udl"};
	}
	
	Integer res1Num;
	String res1Name;
	String res1atom;
	Integer res2Num; 
	String res2name; 
	String res2atom ;
	Float udl;
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes1Num()
	 */
	public Integer getRes1Num() {
		return res1Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes1Num(java.lang.Integer)
	 */
	public void setRes1Num(Integer res1Num) {
		this.res1Num = res1Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes2Num(java.lang.Integer)
	 */
	public void setRes2Num(Integer res2Num) {
		this.res2Num = res2Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setUdl(java.lang.Float)
	 */
	public void setUdl(Float udl) {
		this.udl = udl;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes1Num(int)
	 */
	public void setRes1Num(int res1Num) {
		this.res1Num = res1Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes1Name()
	 */
	public String getRes1Name() {
		return res1Name;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes1Name(java.lang.String)
	 */
	public void setRes1Name(String res1name) {
		this.res1Name = res1name;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes1atom()
	 */
	public String getRes1atom() {
		return res1atom;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes1atom(java.lang.String)
	 */
	public void setRes1atom(String res1atom) {
		this.res1atom = res1atom;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes2Num()
	 */
	public Integer getRes2Num() {
		return res2Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes2Num(int)
	 */
	public void setRes2Num(int res2Num) {
		this.res2Num = res2Num;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes2name()
	 */
	public String getRes2name() {
		return res2name;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes2name(java.lang.String)
	 */
	public void setRes2name(String res2name) {
		this.res2name = res2name;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getRes2atom()
	 */
	public String getRes2atom() {
		return res2atom;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#setRes2atom(java.lang.String)
	 */
	public void setRes2atom(String res2atom) {
		this.res2atom = res2atom;
	}
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#getUdl()
	 */
	public Float getUdl() {
		return udl;
	}
	
	/* (non-Javadoc)
	 * @see cyana.VennRestraint#toString()
	 */
	public String toString() 
	{
		return res1Num +" "+ res1Name +" "+ res1atom +" "+res2Num+" "+res2name +" "+res2atom +" " + new DecimalFormat("###.###").format(udl);
	}

	/* (non-Javadoc)
	 * @see cyana.VennRestraint#toStringXplor()
	 */
	public String toStringXplor()
	{
		return "DIST   "+res1Num+"   "+this.res1atom+"   "+this.res2Num+"   "+this.res2atom+"   "+udl;
	}
}