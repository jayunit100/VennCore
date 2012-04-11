/**
 * 
 */
package services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import legacy.bioinformatics.BlastBean;

/**
 * @author jayunit100
 *
 */
public class VennHomologSet 
{
	String parent;
	List<BlastBean> children=new ArrayList<BlastBean>();
	
	public VennHomologSet ()
	{
		
	}
	/**
	 * @return the parent
	 */
	public String getParent() {
		return this.parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}
	/**
	 * @return the children
	 */
	public List<BlastBean> getChildren() {
		return this.children;
	}

	public Hashtable<String,BlastBean> getMap()
	{
		Hashtable<String,BlastBean> t = new Hashtable<String,BlastBean>();
		for(BlastBean b : this.children)
		t.put(b.getId(), b);
		return t;
	}
	/**
	 * @param children the children to set
	 */
	public void setChildren(List<BlastBean> children) 
	{
		this.children = children;
	}
	
	public String debug()
	{
		String s = "Debug of venn homolog set ";
		for(BlastBean b: this.children)
		{
			s+=("\thomolog:"+b.getId()+"="+b.getSequence())+"\n";
		}
		return s;
	}
	
	/**
	 * @param parent
	 * @param children
	 */
	public VennHomologSet(String parent, List<BlastBean> children) {
		super();
		this.parent = parent;
		this.children = children;
	}
	
	public String toString()
	{
		return parent + "[homologs= " + children.size() +"]";
	}
}
