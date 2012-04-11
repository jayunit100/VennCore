package standalones;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import legacy.bioinformatics.BioinformaticsUtilities;
import legacy.bioinformatics.BlastBean;
import legacy.util.Utilities.Counter;
import legacy.util.Utilities.FileUtils;

/**
 * Calculates residue enrichment
 * 
 * inputs 
 * 1: a fasta file (proteome);
 * 2: a protein sequence file;
 * 
 * result : finds all proteins in file 1 which match the enrichment of proteins in file 2. 
 * 
 * See main method for implementation example.
 * 
 * @author vyas
 *
 */
public class ResidueEnrichmentTool 
{
	//initialization data for reference proteome stats.
	
	//using paenabacillus but similar results when using alicyclebacillus 
	//public static File refProteome=new File("/raid/Users/vyas/Downloads/paenibacillusProteome.fasta");
	public static File refProteome= new File("/raid/Users/vyas/old/firmicutes/all.faa/Alicyclobacillus_acidocaldarius_DSM_446/proteome.faa");
	
//	public static File sspes = new File("/raid/Users/vyas/Desktop/sspe.fasta");
//	public static File kinases= new File("/raid/Users/vyas/Desktop/kinases.fasta");
//	public static File polymerases = new File("/raid/Users/vyas/Desktop/polymerases.fasta");
//	public static File tuscia = new File("/raid/Users/vyas/Desktop/tusciae.fasta");

	
	static TreeMap<Character,Float> proteomeE = new TreeMap<Character,Float>();
	static Counter<Character> proteome;
	static int proteomeTotal ;
	
	static
	{
		proteome = count(refProteome);
		proteomeTotal = total(proteome);
	
		for(Character c1 : proteome.keySet())
		{
			proteomeE.put(c1, (float) proteome.get(c1)/(float)proteomeTotal);
		}
		System.out.println("Done making reference residue enrichment map : " + proteomeE);
	}

	public static Counter<Character> count(File fasta)
	{
		List<BlastBean> beans = BioinformaticsUtilities.parseBeansFromFasta(FileUtils.fileToString(fasta));
		return count(beans);
	}
	
	public static Counter<Character> count(List<BlastBean> beans)
	{
		Counter<Character> c11 = new Counter<Character>(Character.class);
		for(BlastBean s : beans)
		{
			for(Character c1:s.getSequence().toCharArray())
			{
				c11.increment(new Character(c1));
			}
		}
		return c11;
	}
	
	
	
	public static Counter<Character> count(String s)
	{
		Counter<Character> c11 = new Counter<Character>(Character.class);
		for(Character c1:s.toCharArray())
		{
			c11.increment(new Character(c1));
		}
		return c11;
	}
	
	public static int total(Counter<Character> count)
	{
		int t = 0;
		for(Character c : count.keySet())
		{
			t+=count.get(c);
		}
		return t;
	}
	
	public ResidueEnrichmentTool(String protein)
	{
		Counter<Character> input = count(protein);
		int inputTotal = total(input);
		
		TreeMap<Character,Float> inputE = new TreeMap<Character,Float>();

		//calcualte percentages for each.
		for(Character c1 : input.keySet())
		{
			 
			inputE.put(c1, (float) input.get(c1)/inputTotal);
			
			//calculate the fold enrichment (remove the proteome divisor to simply calculate percent enrichment).
			foldE.put(c1,inputE.get(c1));
			
			//System.out.println(c1 + "," + formatter.format(inputE.get(c1)) + "," + formatter.format(proteomeE.get(c1)));
		}
	}

	public TreeMap<Character,Float> foldE = new TreeMap<Character,Float>();
	public static NumberFormat formatter = new DecimalFormat("#0.00");
	/**
	 * For a fasta file of orthologs, this method calculates
	 * residue fold enrichment, as compared to paenaebacillus reference proteome.
	 * 
	 * 
	 * It basicaly creats an amino-profile .  used in conjunction with 
	 * compare enrichment.
	 * @param orthologs
	 * @return
	 */
	public static TreeMap<String,TreeMap<Character,Float>> calculateEnrichment(File orthologs)
	{
    	TreeMap<String,TreeMap<Character,Float>> outputs = new TreeMap<String,TreeMap<Character,Float>>();
		//for each protein in sspe file
		for(BlastBean b : BioinformaticsUtilities.parseBeansFromFasta(FileUtils.fileToString(orthologs)))
		{
			outputs.put(b.getName(),new ResidueEnrichmentTool(b.getSequence()).foldE);
		}
		
		//print species names
		System.out.print("Amino,");
		for(String s : outputs.keySet())
		for(String aa:BioinformaticsUtilities.AA_RESIDUES)
		{
			Float asum = 0f;
			float acount = 0f;
			for(String n : outputs.keySet())
			{
				char aac = aa.charAt(0);
				Float  f = outputs.get(s).get(aac);
				if (f==null) f=0f;
				asum+=f;
				acount++;
				//you might want to print something here. 
			}
			System.out.print(",avg="+asum/acount);
			System.out.println();
		}
		
		return outputs;
	}
	/**
	 * Compares a profile to a sequence. 
	 * returns a map of the distances.
	 * @param sequence
	 * @param refGene
	 * @return
	 */
	public static float compareEnrichment(String sequence, TreeMap<Character,Float> refGene)
	{
		TreeMap<Character,Float> distances = new TreeMap<Character,Float>();
	
		Counter<Character> chars = count(sequence);
		
		for(Character a : chars.keySet())
		{
			float enrchment = (float)chars.get(a)/(float) sequence.length();
			if(refGene.containsKey(a))
				distances.put(a, Math.abs(enrchment-refGene.get(a))); 
			else
				distances.put(a, enrchment);
		}
		return avg(distances.values());
	}
	
	public static Float avg(Collection<Float> f)
	{
		float ff=0;
		for(Float g : f)
		{
			ff+=g;
		}
		
		return ff/f.size();
	}
	
	public static void main(String[] args)
	{
		//TreeMap<String,TreeMap<Character,Float>> outputs = calculateEnrichment(ResidueEnrichmentTool.refProteome);

		/**
		 * This code, if an SSPE's file exists, can be used to generate sspe differences.
		 * 
		//sspe enrichment
		TreeMap<String,TreeMap<Character,Float>> outputs = calculateEnrichment(sspes);
		
		System.out.println(""+outputs);
		float best = 1000000f;

		List<BlastBean> all = BioinformaticsUtilities.parseBeansFromFasta(FileUtils.fileToString(refProteome));
		for(BlastBean b : all)
		{
				float avgdistance = compareEnrichment(b.getSequence(),outputs.get(outputs.firstKey()));
				b.setScore(1/avgdistance);
				
				if(b.getSequence().length()>150)
					b.setScore(-1000);
		}
		
		Collections.sort(all,Collections.reverseOrder());
		
		System.out.println("best/worst " + all.get(0).getScore()+ " / " + all.get(all.size()-1).getScore());
		System.out.println(all.get(0).getName()+"/"+all.get(0).getSequence());
		int total = 50;
		for(BlastBean b : all)
		{
			if(total>0)
				System.out.println("score="+(b.getScore().intValue()) + " " + b.getName()+ b.getSequence());
			
			total--;
		}
		**/
	}
	
}
