package standalones;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import legacy.bioinformatics.GPdbUtils;
import legacy.bioinformatics.GPdbUtils.ATOM;
import legacy.util.Utilities;

import org.apache.commons.lang.StringUtils;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.StructureAlignmentFactory;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.ce.CeParameters;
import org.biojava.bio.structure.align.helper.AlignTools;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.jama.Matrix;

/**
 * Given a structure and a directory, calculates similarities. sorts them.
 * @author vyas
 *
 */
public class StructureFilter 
{
	public StructureFilter(Structure correct, File PdbDir)
	{
		//the "correct" structure, which is a cyana bundle.... needs to have same number of atoms as 
		//the targets, which will be single chain, single model rosetta structures for the purposes of the 
		//main goal of this program, which is to compare thousands of rosetta outputs with a known 
		//correct "needle "structure.  Thus, we get the minimized mean ...
		try
		{
			correct=GPdbUtils.getMinimizedMeanStructure(correct);
		
		System.out.println("protein lenght is " + correct.getChain(0).getAtomLength() + " " + correct.getChain(0).getAtomSequence());
		System.out.println(PdbDir.getAbsolutePath()+ " ... " + PdbDir.exists());
		//nmr/cyana/benchmark/AI_15270/rescue/csrosetta500/
		
		go(correct,PdbDir,ATOM.CA);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		String root = "/raid/Users/vyas/nmr/cyana/benchmark/A_16790/";
		Structure correct = GPdbUtils.getStructure(new File(root+"final.pdb"));
		File dir = new File(root+"/csrosetta20k/output/");
		//nmr/cyana/benchmark/AI_15270/rescue/csrosetta500/
		
		new StructureFilter(correct,dir);
	}

	
	/**
	 * The preferred alignment algorithm.
	 * Takes in QUICK,CA,HEAVY,BACKBONE, or ALL.
	 * @param s1
	 * @param s2
	 * @return
	 * @throws Exception
	 */
	public static float alignCE(Structure s1, Structure s2, ATOM type) throws Exception
	{
		Atom[] ca1 = GPdbUtils.getAtoms(s1, type);
        Atom[] ca2 = GPdbUtils.getAtoms(s2, type);

         // get default parameters
         CeParameters params = new CeParameters ();
        
         // set the maximum gap size to unlimited 
         //params.setMaxGapSize(-1);
         StructureAlignment algorithm  = StructureAlignmentFactory.getAlgorithm(CeMain.algorithmName );
         
         // The results are stored in an AFPChain object           
         AFPChain afpChain = algorithm.align(ca1,ca2,params);            

         afpChain.setName1("A");
         afpChain.setName2("B");

         if(total++ % 500 ==0)
        	 System.out.println("VennSF-CE structure alignmeng "+total+" difference in angstroms : "+afpChain.getChainRmsd());
         return (float)afpChain.getChainRmsd();
	}
	
	public static float calc(Matrix m1, Matrix m2)
	{
		//System.out.println(m1.getArray().length+"/"+m2.getArray().length);
		double[][] aa=m1.getArray();
		double[][] bb=m2.getArray();

		//System.out.println(aa.length + " / " + bb.length);
		double s=0d;
		
		for(int i =0; i < aa.length-1; i++)
		{	for(int j=0; j < aa[i].length-1; j++)
			{
				s += Math.abs( aa[i][j] - bb[i][j] ) ;
			}
		}
		
		return (float) s/(aa.length*aa.length);
	}
	
	static int total=0;
	
	public static float alignDM(Structure s1, Structure s2) throws Exception
	{
        // calculate structure superimposition for two complete structures
        StructurePairAligner aligner = new StructurePairAligner();
        Matrix m1 =AlignTools.getDistanceMatrix(StructureTools.getAtomCAArray(s1), StructureTools.getAtomCAArray(s1));    
        Matrix m2 =AlignTools.getDistanceMatrix(StructureTools.getAtomCAArray(s2), StructureTools.getAtomCAArray(s2));        	
       return calc(m1,m2);
        //System.out.println(calc(m1,m2));
	}
	
	
	public TreeMap<Integer,List<File>> strucsDMAT = new TreeMap<Integer,List<File>>();
	public TreeMap<Integer,List<File>> strucsRMSD = new TreeMap<Integer,List<File>>();
	public  void go(Structure structure1, File pdbdir, ATOM type)
	{
		try 
		{
			int total = pdbdir.listFiles().length;
			int soFar = 0 ; 
			for(File f : pdbdir.listFiles())
			{  
				System.out.println( ++soFar +" out of " + total);
				if(f.getPath().contains(".pdb") && f.length() > 2000 )
				{
					System.out.println("found a pdb " + f.getAbsolutePath()+" " + f.length());
					
					boolean isSplit = true;
	
					Structure structure2 = GPdbUtils.getStructure(f);

					//System.out.println("Structure 2 : length in aminos is "+structure2.getChain(0).getAtomLength() +" " +structure2.getChain(0).getAtomSequence());
					//align the structure, put the rmsd in a rounded number, so 1.5 -> 150.  
					int rmsd=Math.round(alignDM(structure1,structure2)*100);
					if( ! strucsDMAT.containsKey(rmsd) )
						strucsDMAT.put(rmsd, new ArrayList<File>());
					strucsDMAT.get(rmsd).add(f);
				
					if(strucsDMAT.size()%100==0)
					{	
						System.out.println("range " + strucsDMAT.firstKey()+" in " + StringUtils.join(strucsDMAT.firstEntry().getValue().toArray())+" to " + strucsDMAT.lastKey());
						System.out.println("Just finished comparing to Protein in File : " + f.getAbsolutePath());
					}
				}
				else
				{
					 System.out.println("Skip " + f.getAbsolutePath());
				}
			}
			System.out.println(Utilities.debugHashtable(strucsDMAT));
			
			//now, start calculating alignments for the best dmat structures. 
			for(Integer key : strucsDMAT.keySet())
			{
				Structure sK = GPdbUtils.getStructure(strucsDMAT.get(key).get(0));
				float alignment= StructureFilter.alignCE(structure1, sK, ATOM.CA);
				if(alignment < 3)
				System.out.println(key + ","+alignment+","+strucsDMAT.get(key).get(0).getAbsolutePath());
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return;
		}
	}
}
