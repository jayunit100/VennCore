package legacy.bioinformatics;


import org.biojava.bio.structure.Group;


public class AminoAcidGroup
{
	public int index;
	public Group g;
	private char name;
	
	public char getName()
	{
		return name;
	}
	
	public AminoAcidGroup(int ind, Group gg, char n)
	{
		//System.out.println("CREATING AA GROUP " + ind + " " + gg + ">"+n);
		index=ind;
		g=gg;
		name=n;
		if(g != null)
		{
			assert GPdbUtils.getCharForAminoAcid(gg.getPDBName())==name;
		}
		assert Character.isLetter(name);
	}
	
	
	public String toString()
	{
		return ">"+getName() + "@"+ index +":grp="+g;
	}
}