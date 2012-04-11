package standalones;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * Input is a genome file, and a sequence (the sequence to pre/post analyze), lines before, and lines after.
 * 
 * This is used to find the upstream and downstream promotor regions of a gene.
 * 
 * It takes a FULL dna file .  It was built for bacterial genomes.
 * 
 	  Warning : Returns the nucleotides before, after, and inside the first sequence found matching the input dna sequence. 
	  does not work if there are more than one versions of a sequence. 
	  
	  To accomodate this, modify the input argument to the getSubsequenceMethod accordingly.


 * @author vyas
 *
 */
public class GenomeBrowser {

	int lineSize;

	/**
	 * input is a whole fasta file, with only one entry (i.e. 
	 * a whole genome)
	 * @param fasta
	 * @return
	 */
	public char[] getNucleotideArray(String fasta)
	{
		String[] lines = fasta.split("\n");
		int lineLength = lines[0].length();
		int total = lines.length;
		char[] nuc = new char[total*lineLength];
		int p=0;
		for(int i =1; i < lines.length; i++)
		{
			for(char c : lines[i].toCharArray())
			{
				if(Character.isLetter(c))
				{
					nuc[p++]=c;
				}
			}
		}
		System.out.println("returning nucleotides : " + nuc.length + " " + nuc[0]+" to "+ (nuc.length-1) +"("+nuc[nuc.length-1]+")");
		return nuc;
	}
	
	/**
	 * Deliberately hard coded to only get the first version (i.e. notice
	 * how the indexOf method is used with argument 0!)
	 * @param nucs
	 * @param sequence
	 * @param bef
	 * @param aft
	 * @return
	 */
	public String getSubSequence(char[] nucs, int start, String sequence, int bef, int aft)
	{
		String nucString = new String(nucs);

		//usually is 0.
		int index = nucString.indexOf(sequence,start);
		
		String before = nucString.subSequence(index-100, index).toString();
		String after  = nucString.subSequence(index, index+100).toString();
	
		return before+sequence+after;
	}
	
	public String pre;
	public String match;
	public String post;

	/**
	 * @param startLine
	 * @param input
	 * @param cds
	 * @param before
	 * @param after
	 */
	public GenomeBrowser(int startLine, File input, String cds, int before, int after)
	{
		try
		{
			String s = FileUtils.readFileToString(input) ;
			s=s.replaceAll("\n", "");
			int index =s.indexOf(cds.toUpperCase());
			System.out.println("Match index (should be > 1) =" + index);
			pre = s.substring(index-before, index);

			match = s.substring(index, index+cds.length());
		    post = s.substring(index+cds.length(), index+cds.length()+after);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
		Alter the below arrays.  
	 * @param args
	 */
	
	public static File[] files=
	{
		new File("/raid/Users/vyas/old/firmicutes/full_genomes/Ali.fna"),
		//new File("/raid/Users/vyas/old/firmicutes/full_genomes/PJDR2.fna"),
		//new File("/raid/Users/vyas/old/firmicutes/full_genomes/PpoE6.fna"),
	};
	
	public static String[] sequences=
	{
		 //"small acid-soluble spore protein alpha/betatype" protein_id="ZP_03492866.1"
		"TTACGCGCGGCCCGCGAGGCTTTGCTCCGCGTACGCCACGAGGCGCTTCGTGATCTCGCCACCCACCGAGCCGTTCTGGCGGGAGGTGGTGTCCGGGCCAAGGTTGACCCCGAACTCCGTCGCGATCTCGTACTTCATCTGATCCAGCGCCTTCGAAGCCTGCGGAACCAGCGTGCGATTGCTACCCGAATTGTTTGCCAT",
		
	};
	
	public static void main(String[] args)
	{
		for(int i = 0 ; i < files.length ; i++)
		{
		    File species = files[i];	
			String dna = sequences[i].toUpperCase();
			System.out.println("Search="+dna);
			GenomeBrowser gg = new GenomeBrowser(1, species, dna,200,200);
	
			System.out.println("\tSpecies file : " + species.getName());
			System.out.println("\t\tpre 1-200 : " +gg.pre);
			System.out.println("\t\tmatch : " + gg.match);
			System.out.println("\t\tpost 1-200 : "+ gg.post);
		}
		
		
	}
	
}
