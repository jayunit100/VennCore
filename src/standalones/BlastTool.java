package standalones;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import legacy.bioinformatics.BioinformaticsUtilities;
import legacy.bioinformatics.BlastBean;
import legacy.util.Comparison;

import org.apache.commons.io.FileUtils;

/**
 * Input file 1 : A proteome fasta file.
 * Input 2 : A Sequence. 
 * 
 * Finds top 50 proteins in the file matching the input sequence.
 * 
 * @author vyas
 *
 */
public class BlastTool {

	public static File refProteome=new File("/raid/Users/vyas/Downloads/paenibacillusProteome.fasta");

	public Vector<BlastBean> results = new Vector<BlastBean>();
	
	public BlastTool(File input, String sequence)
	{
		try
		{
			List<BlastBean> beans = BioinformaticsUtilities.parseBeansFromFasta(FileUtils.readFileToString(refProteome));

			for(BlastBean b : beans)
			{
				b.setScore(Comparison.compareStrings(b.getSequence(),sequence));
				if(b.getScore().floatValue()>.32f)
					results.add(b);
			}
		
			Collections.sort(results);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args)
	{
		BlastTool b = new BlastTool(refProteome,"MNEANQGRSRRSNNLVVPQANNALQQLKYEAAQELGITIPADGYYGDMPSREAGSLGGYITKRLVQLAEQQLSGRSGQ");
		
		Iterator<BlastBean> beans = b.results.iterator();
		while(beans.hasNext())
		{
			BlastBean bb = beans.next();
			
			System.out.println(bb.getScore() + " -> " + bb.getName() + bb.getSequence() );
		}
			
		
	}
}
