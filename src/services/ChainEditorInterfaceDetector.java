package services;

import java.util.ArrayList;
import java.util.List;

import legacy.bioinformatics.GPdbUtils;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;

public class ChainEditorInterfaceDetector 
{
	int angs=4 ;
	public Structure newStructure;
	public ChainEditorInterfaceDetector(final Structure s,int ang) throws Exception
	{
		angs=ang;
		
		newStructure = s.clone();
		
		for(Chain c : s.getChains())
		{
			Chain cNew = newStructure.getChainByPDB(c.getName());
			cNew.getAtomGroups().clear();
			for(Chain c2 : s.getChains())
			{
				if(isProtein(c) && isProtein(c2) && (c!=c2))
				{
					List<Group> g = getInterface(c,c2);
					cNew.getAtomGroups().addAll(g);
				}
			}
		}

		//write new structure out to disk.
		System.out.println("Done finding interface ; #chains =  " + newStructure.getChains().size() );
		for(Chain c : newStructure.getChains())
		{
			System.out.println(c.getName() + " :  " + c.getAtomLength() + "  " + c.getAtomSequence());
		}
	}
	
	public int closeOnes=0;
	public boolean isClose(Atom a, Atom b) throws Exception 
	{
		double close = Calc.getDistance(a,b);
		
		System.out.println(a + " -> " + b + " : " + close + " so far " + closeOnes);

		if(close < angs )
		{
			System.out.println("\t these atoms are close ! "+closeOnes++);
			return true;
		}
		else
			return false;
	}
	
	public List<Group> getInterface(Chain c1, Chain c2) throws Exception
	{
		List<Group> g = new ArrayList<Group>();
		for(Group g1 : c1.getAtomGroups())
		{
			Atom a  = g1.getAtom("CA");

			inner:
			for(Group g2 : c2.getAtomGroups())
			{
				Atom b = g2.getAtom("CA");
	
				if(isClose(a,b))
				{
					if(! g.contains(g1))
					{
						g.add(g1);
						break inner;
					}
				}
				
			}
		}
		return g;
	}
		
	public static boolean isProtein(Chain c)
	{
		return c.getAtomSequence() != null && c.getAtomSequence().length()>0;
	}
	
	public static void main(String[] args)
	{
		try
		{
			//8 is the perfect number.
			String pdbId="1AZG";
			Structure s = GPdbUtils.getStructure(pdbId);
			new ChainEditorInterfaceDetector(s,8);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	}