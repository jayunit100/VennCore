/**
 * 
 */
package services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import legacy.bioinformatics.BlastBean;
import legacy.util.Utilities;
import legacy.util.Utilities.Counter;

import org.apache.log4j.Logger;


/**
 * Calculates the Raw Sequence Conservation 
 * given two strings. 
 * 
 * Supports the notion of an equivalenceSet - residues which are taken as equivalent.
 * For example, you might add Ser/Thr to an equivalence set so that they show us as identical.
 * 
 * Does not weight using any probabilities, etc... 
 * 
 * Just send the main sequence and the comparison sequences in. 
 * 
 * The output is :
 * 
 * 1) A Vector of ints , corresponding to indices in the orderd main sequence (Constructor Arg 1)
 * 2) A Vector of alignments, corresponding to each comparison sequence. 
 * 
 * A Heat map is easily built from (1) .
 * 
 * @author jayunit100
 *
 */
public class AlignmentUtil 
{
	//records the char conservation (integers)
	//this is then normalized with respect to the size 
	//in the getScores method.
	Counter<Integer> chars=new Counter<Integer>(Integer.class);;

	//alignments are public so web page can access it...
	//the main sequence and target sequence are both stored.
	public Vector<String> mainAlignments = new Vector<String>();
	public Vector<String> targetAlignments = new Vector<String>();
	
	int[][] matrix = new VennMatrix().matrix;

	public String mainSequence;
	/**
	 * See other constructor for comments.
	 * @param main
	 * @param cCequences
	 * @param d
	 */
	public AlignmentUtil(int[][] ma, int p, String main, Set<Character> eq, String... cCequences)
	{
		this.equivalentResidues=eq;
		matrix=ma;
		List<String> s = new ArrayList<String>();
		for(String ss:cCequences)
			s.add(ss);
		//for external programs.
		mainSequence=main;
		go(main,s,p);
	
	}
	
	static List<String> getStrings(List<? extends BlastBean> bb)
	{
		ArrayList<String> ss = new ArrayList<String>();
		for(BlastBean b2 : bb)
			ss.add(b2.getSequence());
		return ss;
	}

	public AlignmentUtil(int[][] ma,  String main, Set<Character> eq, List<? extends BlastBean> cCequences,int p)
	{
		this(ma,p,main,eq, getStrings(cCequences).toArray(new String[] {}));
	}
	
	Set<Character> equivalentResidues = new HashSet<Character>();
	public boolean cequals(Character c, Character b)
	{
		boolean equal = c.equals(b) ;
		if(! equal)
		{
			boolean cc =equivalentResidues.contains(c);
			boolean bb = equivalentResidues.contains(b);
			//lg.info("equal was " + equal +" secondary equivalence test ... " + cc + " ?~? " + bb );
			//lg.info("result " + c + " " + b + "? " + equal );
			equal=cc&&bb;
		}
		
		return equal;
	}

	Logger lg = Logger.getLogger(AlignmentUtil.class);
	/**
	 * Takes in a gap penalty.  Use 0 for peptides in the interface miner, 
	 * and something like -5 for normal proteins.
	 * @param main
	 * @param cSequences
	 * @param d
	 */
	public void go(String main, List<String> cSequences,int penalty)
	{ 
		//initialize the character map to be the size of the main sequence.
		for(int i = 0 ; i < main.length(); i++)
		{
			this.chars.put(i,0);
		}
		//for each cSequence, make a pairwise alignment.
		//And tally the amount of matches.  
		//this is used by the getScores methods.
		for(String c : cSequences)
		{
			NeedlemanWunsch al = new NeedlemanWunsch(penalty,matrix);
			String[] vals = al.align(main, c);
			
			String main_a = vals[0];
			String comp_a = vals[1];
			//size of this vector = size of main protein sequence.
			//values are true/false if it is/isnot conserved in the alignment.
			System.out.println(main_a.toCharArray().length + " " + comp_a.toCharArray().length);
			Vector<Boolean> conserved=getGappedConservationVector(main_a.toCharArray(),comp_a.toCharArray(),false);
			
			mainAlignments.add(main_a);
			targetAlignments.add(comp_a);
			
			//System.out.println("vector size "+conserved.size());
			for(int i = 0 ; i < conserved.size();i++)
			{
				if(conserved.get(i))
					chars.increment(i);
			}
		}
		
	}
	/**
	 * Returns a longer vector, which represents the total alignment.
	 * Used for printing the text alignment with gaps.
	 * @param m
	 * @param c
	 * @return
	 */
	public Vector<Boolean> getGappedConservationVector(char[] m, char[] c, boolean gapped )
	{	
		if(m.length != c.length)
			System.err.println("Warning : strings \n" + new String(m) + "\n"+c.length +" are unequal in length.");
		Vector<Boolean> bools = new Vector<Boolean>();
		for(int i = 0 ; i < m.length ; i++)
		{
				if(gapped || Character.isLetter(m[i]))
				{
					if(this.cequals(m[i], c[i]))
						bools.add(true);
					else
						bools.add(false);
				}
		}
		return bools;
	}
	
	
	/**
	 * Returns an orderd list of indices in the input string
	 * and their conservation.
	 * @return
	 */
	public TreeMap<Integer,Float> getScoresAsIndexMap()
	{
		float sz=this.targetAlignments.size();
		System.out.println(this.chars);
		TreeMap<Integer,Float> scores = new TreeMap<Integer,Float>();
		for(Integer i : this.chars.keySet())
		{
			float cons = this.chars.get(i);
			scores.put(i, cons/sz);
		}

		return scores;
	}
	
	/**
	 * This is easy to use, it returns a vector here the nth element 
	 * corresponds to conservatoin of the nth residue in the protein.
	 * Solve thd problem that there are keys such as 1 2 4 5 6 that make
	 * iteration complicated if using the map method.
	 * However map method can be used for other purposes..... such as finding residue.
	 * @return
	 */
	public Vector<Float> getScoresOrderedByResidue()
	{
		TreeMap<Integer,Float> scores = this.getScoresAsIndexMap();
		Vector<Float> f = new Vector<Float>();
		Iterator<Integer> residues= scores.keySet().iterator();
		while(residues.hasNext())
		{
			Float sc = scores.get(residues.next());
			f.add(sc);
		}
		return f;
	}
	
	public String toString()
	{
		String s="";
		AlignmentUtil a = this;
		for(int i = 0 ; i < a.mainAlignments.size(); i++)
		{
			s+=(a.mainAlignments.get(i));
			s+="\n";
			s+=(a.targetAlignments.get(i));
			s+="\n";
		}
		return "//start alignmentutil summary\n{\n"+s+"\n}//end summary";
	}

	public static void main(String[] args)
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
