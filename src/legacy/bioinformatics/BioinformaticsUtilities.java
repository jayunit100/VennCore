package legacy.bioinformatics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import legacy.util.Utilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import uk.ac.ebi.kraken.model.blast.parameters.MaxNumberResultsOptions;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniRefQueryService;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastInput;

public class BioinformaticsUtilities 
{
	static String[] nonmotifs = new String[]{"DNA","RNA","NLS","AND"};
	static String[] AAS = "x,X,Gly,G,Ala,A,Val,V,Leu,L,Ile,I,Met,M,Phe,P,Trp,W,Pro,P,Ser,S,Thr,T,Cys,C,Tyr,Y,Asn,N,Gln,Q,Asp,D,Glu,E,Lys,K,Arg,R,His,H".split(",");
	public static String AAS_REGEX ="((([GAVLMFWPSTCYNQDEKRHYIPpXx])|(Gly)|(Ala)|(Val)|(Leu)|(Ile)|(Met)|(Phe)|(Trp)|(Pro)|(Ser)|(Thr)|(Cys)|(Tyr)|(Asn)|(Gln)|(Lys)|(Arg)|(His)|(Asp)|(Glu)|(Lys)|(Thr)|(Trp)|\\p{Punct}|)-?){3,15}";
	public static String DYNEIN_PDB="MSDRKAVIKNADMSEDMQQDAVDCATQAMEKYNIEKDIAAYIKKEFDKKYNPTWHCIVGRNFGSYVTHETKHFIYFYLGQVAILLFKSG";
	public static String DYNEIN_HEAVY_TRNA="DLKRLRQEPEVFHRAIREKGVALDLEALLAVDEQLHKQQEVIADKQMSVKEDLDKVEPAVIEAQNAVKSIKKQHLVEVRSMANPPAAVKLALESIALLLGESTTDWKQIRSIIMRENFIPTIVNFSAEEISDAIREKMKKNYMSNPSYNYEIVNRASLAAGPMVKWAIAQLNYADMLKRVEPLRNELQKLEDDAKDNQQKLEALLLQVPLPPWPGAPVGGEEANREIKRVGGPPEFSFPPLDHVALMEKNGWWEPRISQVSGSRSYALKGDLALYELALLRFAMDFMARRGFLPMTLPSYAREKAFLGTGHFPAYRDQVWAIAETDLYLTGTAEVVLNALHSGEILPYEALPLRYAGYAPAFRSEAGSFGKDVRGLMRVHQFHKVEQYVLTEASLEASDRAFQELLENAEEILRLLELPYRLVEVATGDMGPGKWRQVDIEVYLPSEGRYRETHSCSALLDWQARRANLRYRDPEGRVRYAYTLNNTALATPRILAMLLENHQLQDGRVRVPQALIPYMGKEVLEPG";
	public static String[] AA_RESIDUES="A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y".split(",");
	
	//NMR Residue Properties.
	public static String Kay_LABELLED_ILV = "HG22,HG23,HG21,HD11,HD12,HD13,HD23,HD21,HD22,HG11,HG12,HG13" ;
	public static enum RESIDUE
	{
		ALA("",'A'),
		ARG("HH21,HH22,HH11,HH12",'R'),
		ASN("HD21,HD22",'N'),
		ASP("",'D'),
		CYS("",'C'),
		GLN("HE21,HE22",'Q'),
		GLU("",'E'),
		GLY("",'G'),
		HIS("HD1,HD1",'H'),
		ILE(Kay_LABELLED_ILV,'I'),
		LEU(Kay_LABELLED_ILV,'L'),
		LYS("",'K'),
		MET("",'M'),
		PHE("",'F'),
		PRO("",'P'),
		SER("",'S'),
		THR("",'T'),
		TRP("HE1,HE1",'W'),
		TYR("",'Y'),
		VAL(Kay_LABELLED_ILV,'V');
		
		public static TreeSet<String> OMNIVISIBLE= new TreeSet<String>(Arrays.asList("H,HN,CA,CB,C,O,N,CB,CG,NE,CZ,NH2,NH1,ND2,SG,CD,CD2,NE1,NE2,OE2,ND1,CE,SD,CE,NZ,CD2,CE2,CD1,OG1,CH2,CZ3,CZ2".split(",")));
		public TreeSet<String> kayDuetrationVisible;
		public char oneLetter;
		RESIDUE(String visibleAfterDeuteration, char c)
		{
			oneLetter=c;
			kayDuetrationVisible = new TreeSet<String>(Arrays.asList(visibleAfterDeuteration.split(",")));
		}
		
		public static List<RESIDUE> get(char... vals)
		{
			List<RESIDUE> res = new ArrayList<RESIDUE>();
			for(RESIDUE r1 : RESIDUE.values())
			{
				for(char oneL : vals)
				if(r1.oneLetter==oneL)
				{
					res.add(r1);
				}
			}
			return res;
		}
	};

	public static boolean isValidProteinSequence(String s)
	{
		return s.matches("[QWERTYUIOPASDFGHJKLZCVBNM]{1,10000}");
	}
	
	public static String getMatrixFromNCBI(String name)
	{
		try
		{
			return Utilities.FileUtils.read(new URL("ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/"+name));
		}
		catch(Exception e)
		{
			System.err.println("not found " + name );
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static Vector<String> getMatchingRegions(String protein,String regex)
	{
		if(protein==null || regex==null || protein.length() < 1 || regex.length() < 1)
		{
			lg.warn("Protein has no content returning empty vector : Did you mix up the protein sequence and regex sequence?");
			if(regex==null || protein== null)
				lg.warn("null inputs : " + regex + " protein sequence : " + protein);
				
			else if(regex.length()>protein.length())
				lg.warn( regex +" is longer than the protein sequence! "+protein);
			return new Vector<String>();
		}

		Vector<Integer> regions=Utilities.Regex.getMatchIndices(protein, Pattern.compile(regex), regex.length());
		Vector<String> s=new Vector<String>();
		for(Integer l :regions)
		{
			s.add(protein.substring(l,l+regex.length()));
			System.out.println("BioinformaticsUtilities:added " + s.lastElement() + " of regions ("+regions.size()+")");
		}
		
		return s;
	}
	
	public static class Alignment
	{
		public String a,b;
		public Alignment(String aa,String bb)
		{
			a=aa;b=bb;
		}
		
	}

	/**
	public static Alignment getAlignedSequence(String parent, String  s)
	{
 	        GappedSequence gss[]=new GappedSequence[2 ];
 	        int i=0;
    		try
    		{gss[0]=ProteinTools.createGappedProteinSequence(parent,"s"+0);
    		gss[1]=ProteinTools.createGappedProteinSequence(s,"s"+1);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
	        SequenceAligner aligner=new charite.christo.strap.extensions.MultipleAlignerMuscle();
	        
	        StrapBiojavaAlignmentTools.alignAminoAcidSequences(gss,aligner);
	        
	        return new Alignment(gss[0].seqString(),gss[1].seqString());
	}
	**/
	
	public static String GOURL="http://amigo.geneontology.org/cgi-bin/amigo/term-details.cgi?term=<0>&format=obo";
	
	
	public static HashSet<String> hasMotif(String s)
    {
		String content = " " + s + " ";
    	//lg.info("\tSearching " + content) ;
    	//not used but is a good regex ! 
    	HashSet<String> motifs = new HashSet<String>();

    	String[] words = content.split(" ");
			for(String test : words)
			{	
				test=Utilities.removeNonAlphaNumerics(test,new char[]{'-'});
				if(test.length()>=3 && Pattern.matches(AAS_REGEX , test))
				{
					motifs.add(test);
				}
			}
		for(String s2 : nonmotifs)
		{
			motifs.remove(s2);
		}
    	return motifs;
    }
	//create a substitution matrix...  adopted to biojava3
    static SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();	
    
    public static enum BioDataType{Refseq,Swissprot,OtherUniparc,DNA_sequence,Amino_sequence};
	
	public static BioDataType getDataType(String gene)
	{
		if(gene.matches(REGEX_REFSEQ))
			return BioDataType.Refseq;
		else if (gene.matches("[UGTAC]{4,}+"))
			return BioDataType.DNA_sequence;
		else if (gene.matches("[ACDEFGHIKLMNPQRSTVWYX]{4,}+"))
			return BioDataType.Amino_sequence;
		else if(gene.matches("[PQ][1234567890]{1,}+"))
			return BioDataType.Swissprot;
		else
			return BioDataType.OtherUniparc;

		
		
	}
	
	
	public static Logger lg = Logger.getLogger(BioinformaticsUtilities.class);
	
	public static String REFSEQ="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&id=<GI>&rettype=fasta";
	public static String REFSEQ_META="http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&id=<GI>&rettype=fasta";
	public static String UNIPROT_TEMPLATE="http://www.uniprot.org/<0>/<1>.<2>";
	public static String REGEX_REFSEQ="[NX][MP]_[0-9]+\\.?[0-9]+";
	
	/**
	 * For use with biojava.  If this method is slow, optimize it... by finding out (more directly) 
	 * how to create a symbol.
	 * For now it should work effectively.
	 * @param c
	 * @return
	 */
	public static Symbol getSymbol(char c)
	{
		try
		{
			return ProteinTools.createProtein(c+"").symbolAt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		lg.warn("no symbol for["+c+"]");
		return null;
	}
	
	public static boolean validateRefseq(String s)
	{
		return Utilities.Regex.match(REGEX_REFSEQ, s).size()>0;
	}
	
   /**
	 * Calculates the Mass of the peptide in Daltons. Using the average Isotope
	 * Mass
	 * @param protein the peptide
	 * @throws IllegalSymbolException if <code>protein</code> is not a protein
	 * @return the mass
	 */
  public static double getMass(String protein)
  {
    double mass = 0.0;
    MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS, true);
    
    try
    {
    	if(protein.contains("X"))
    		lg.warn("WARNING : REPLACING variable x's with leucine to calculate weight !!!");
    	protein=protein.replaceAll("X", "L");
    	mass = mc.getMass(ProteinTools.createProtein(protein));
    }catch(Exception e)
    {
    	lg.warn("BAD SEQUENCE:"+protein);
    	e.printStackTrace();
    }
    return mass;
  }
 
	  /**
	   * Calculates the isoelectric point assuming a free NH and COOH
	   * @param protein the peptide
	   * @throws IllegalAlphabetException if <code>protein</code> is not a peptide
	   * @throws BioException
	   * @return double the PI
	   */
	  public static double getPI(String protein)
	  {
	    double pI = 0.0;
	    IsoelectricPointCalc ic = new IsoelectricPointCalc();
	    try{
	    	pI = ic.getPI(ProteinTools.createProtein(protein), true, true);
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return pI;
	  }

	
	/**
	 * Create  A LIST of blast beans from a generic OR EBI fasta record.
	 * For generic, just put the name of the blast bean = to the header. 
	 * @param fastaFile
	 * @return
	 */
	public static Vector<BlastBean> parseBeansFromFasta(String fastaFile)
	{
		Vector<BlastBean> bb = new Vector<BlastBean>();
		
		String[] fastaRecords = fastaFile.split(">");
		
		for(int i = 1; i < fastaRecords.length; i++)
		{
				String fa = fastaRecords[i];
				String sequence = parseSequenceFromFasta(">"+fa).toUpperCase();

				String header=fa.split("\n")[0];
				BlastBean b;
				//if it is EBI formatted.
				if(header.contains("OS=") || header.contains("GN="))
				{
					b =BioinformaticsUtilities.parseBlastBeanFromEBIFastaRecord(">"+header+"\n"+sequence);
				}
				//otherwise set the header equal to the name. And set the sequence manually.
				else
				{
					b = new BlastBean();
					b.setName(header);
					b.setId("v"+i+"-"+System.currentTimeMillis());
					//make sure and replace new lines, since some ncbi resources artificially
				}
					
				//alot of cleaning here, just to make sure there are NO newline characters.
				b.setSequence(Utilities.removeNonAlphaNumerics(sequence,new char[]{}));
				b.setSequence( b.getSequence().replace("\n", "") );
				b.setSequence( b.getSequence().replace("\\n", "") );
				b.setSequence( b.getSequence().replace("\\\\n", "") );
				
				System.out.println("Just finished replaceing new lines " + b.getSequence() + " " + b.getSequence().split("\\n").length + " are number of newlines left ");
				bb.add(b);
		}
		return bb;
		
	}
	
	
	public static void setFastaProperties(BlastBean b, String header)
	{
		//try ncbi splitting
	}
	
	  
	public static String parseSequenceFromFasta(String fasta)
	{
		lg.info("parsing"+fasta);
		assert fasta.contains(">");
		assert fasta.contains("\n");
		String[] lines = fasta.split("\n");
		String retVal="";
		for(int i = 1; i < lines.length; i++)
		{
			retVal+=lines[i];
		}
		
		//watch out for new line characters.
		//remove them one last time.
		retVal=retVal.replaceAll("\n", "");
		return retVal;
	}

	/**
	 * This method is to be used by jaymol so users can enter refseqs.
	 * not tested
	 * @param ids
	 * @return
	 */
	public static List<BlastBean> getBlastBeansForRefseqOrUniprots(String... ids)
	{
		ArrayList<BlastBean> bbs=new ArrayList<BlastBean>();
		
		for(String s : ids)
		{
			if(s.matches("[XNZ]P"))
			{
				BlastBean bean = BioinformaticsUtilities.getRefseq(s);
				bbs.add(bean);
			}
			else
			{
				System.err.println("Bioinformatics Utilities : Unsupported id " + s);
				
			}
		}
		return bbs;
	}
	
	public static BlastBean getRefseqOrUniref(String accession)
	{
		BlastBean b= BioinformaticsUtilities.getRefseq(accession);
		if( b == null )
			b=BioinformaticsUtilities.getUniref(accession);
		
		return b;
	}
	
   /**
	* Input is either a refseq or GI number.
	* Goes to ncbi and gets all the refseq's data.
	* @param i
	* @return
	*/
	public static BlastBean getRefseq(String i)
	{
		if(i.contains("."))
		{
			lg.info("removing version decimal");
			i=StringUtils.substringBefore(i, ".");
		}
		
		BlastBean e =new BlastBean();

		Vector<String> s = new Vector<String>();			
		boolean isValid=false;

		try 
		{
			System.out.println("BioinformaticsUtilities:accessing link to "+i);
			URL url = new URL( REFSEQ.replaceAll("<GI>",i));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));

			int line=0;
			String str;
			e.setId(i);

			while ((str = in.readLine()) != null) 
			{
			
				lg.info("sequencedata:"+str);
				if(line++==0)
				{
					Vector<String> refseq=Utilities.Regex.getMatches("[NX]P_[0-9]+\\.[0-9]+", str.split("\\|"));
					Vector<String> name=Utilities.Regex.getMatches("\\[.*", str.split("\\|"));
					Vector<String> geneId=Utilities.Regex.getMatches("[0-9]+", str.split("\\|"));
					
					if(!refseq.isEmpty() && !name.isEmpty() && !geneId.isEmpty())
					{
						String r=refseq.firstElement();
						String n= name.firstElement();
						String gg= geneId.firstElement();
						
						e.setName(n);
						e.setId(r);
						//e.set(gg);
						isValid=true;
					}
				}
				else					
					e.setSequence((e.getSequence()==null ? "" : e.getSequence())+str);
				
			}
			
			in.close();
		} catch (MalformedURLException exx) {
		} catch (IOException ex) {
		}

		if(! isValid)
			System.err.println("BioinformaticsUtilities : Invalid blast bean. ");
		return e;
	}
	
	public static BlastBean parseBlastBeanFromEBIFastaRecord(String fastaLine)
	{
		String[] fastaLines = fastaLine.split("\n");
			if(fastaLines.length==0)
				return null;

			BlastBean b = new BlastBean();

			String meta = fastaLines[0];
			System.out.println("Fasta lines " + fastaLines.length);
			String seq="";
			for(int i = 1 ; i < fastaLines.length; i++)
			{
					seq+=fastaLines[i];
			}
			seq=seq.replaceAll(">", "").trim();
			String[] prefixes = {"sp","tr"};

			b.setSequence(seq);
			for(String s : prefixes)
			{
				if(b.getId()==null && meta.contains("|"))
					b.setId(StringUtils.substringBetween(meta,s+"|", "|"));
				//sp|B91000| or tr|B10412|
			}

			//untested, should work 
			b.setName(StringUtils.substringBetween(meta,"Name=", ";"));
			if(b.getName()==null && meta.contains("SubName"))
			{
				b.setName(StringUtils.substringBetween(meta,"SubName:", ";").trim());
			}
			if(meta.contains("OS"))
				b.setTaxonomy(StringUtils.substringBetween(meta,"OS=","GN="));

			//Added to parse gene names for venn blank name bug 
			if(meta.contains("GN"))
				b.setName(StringUtils.substringBetween(meta,"GN=","PE="));

			//System.out.println(b + " tax="+b.getTaxonomy());

			if(b.getSequence()==null || b.getSequence().length()==0)
			{
				System.out.println("Error parsing blast bean.");
				return null;
			}
			if(b.getName()==null)
				b.setName("?");
			return b;
	}
	
	/**
	 * 
	 * Works with either uniref or uniprots. Uses EBIFAsta.parse* utility methods.
	 * 
	 * @param id
	 * @return
	 */
	public static BlastBean getUniref(String id)
	{
		String type="fasta";
		if(id.contains("_"))
			id=id.split("_")[1];
		
		try
		{
			//System.out.println("Going to uniref " + UNIPROT_TEMPLATE + " / " + id + " " + type );
			URL uniprot = new URL(Utilities.StringUt.replaceParameters(UNIPROT_TEMPLATE,"uniprot",id,type));
			URL uniparc = new URL(Utilities.StringUt.replaceParameters(UNIPROT_TEMPLATE,"uniparc",id,type));
			
			//this could be done more elegantly.
			//first, try to get it as a uniprot file.
			
			String fasta ;
			//uniparc sequences have a "UPI" prefix
			if(id.contains("UPI"))
			{
				System.out.println("Going to " + uniparc.getFile());
				fasta = Utilities.FileUtils.read(uniparc);
			}
			else
			{
				System.out.println("Going to " + uniprot.getFile());
				fasta = Utilities.FileUtils.read(uniprot);
			}

			if(fasta == null)
			{
				lg.info("Couldnt acquire url for this uniprot/uniparc entry :" + id);
				return null;
			}
			System.out.println("Parsing fasta file : " + fasta);
			BlastBean b1 = BioinformaticsUtilities.parseBlastBeanFromEBIFastaRecord(fasta);
			b1.setId(id);
			lg.info("blast bean acquired "+b1);
			return b1;
		}	
		catch(Exception e)
		{
			System.out.println("Bioiformatics Utilities Exception :  " + e.getMessage());
			return null;
		}
	}

	public Document getDoc(String xml)
	{
		Document doc = null;
		try {
	        DocumentBuilderFactory dbf =
	            DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));

	         doc = db.parse(is);
	        NodeList nodes = doc.getElementsByTagName("employee");

	        // iterate the employees
	        for (int i = 0; i < nodes.getLength(); i++) {
	           Element element = (Element) nodes.item(i);

	           NodeList name = element.getElementsByTagName("name");
	           Element line = (Element) name.item(0);
	           System.out.println("Name: " + getCharacterDataFromElement(line));

	           NodeList title = element.getElementsByTagName("title");
	           line = (Element) title.item(0);
	           System.out.println("Title: " + getCharacterDataFromElement(line));
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	
	    return doc;
	}

	  public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	       CharacterData cd = (CharacterData) child;
	       return cd.getData();
	    }
	    return "?";
	  }
	public static String NEF=
		"GDLRQRLLRARGETYGRLLGEVEDGYSQSLGGLDKGLSSLSCEGQKYNQGQYMNTPWRNPAEEREKLAYRKQNMDDVDEEDDDLVGVPVMPRVPLRTMSYKLAVDMSHFIKEKGGLEGIYYSARRHRILEKEEGIIPDWQDYTSGPGIRYPKTFGWLWKLVPVNVSDEAQEDEKHYLMQPAQTSKWDDPWGEVLAWKFDPTLAYTYEAYVRYPEEFGSKSGLPEEEVRRRLAARGLLNMADKKETR";
	
	public static enum BLAST_RESULT_SET{ALL_MATCHES,CLOSE,SPREAD,TOP_TEN};
	
	/**
	 * Gets the top 50 blast results for a protein.
	 * 
	 * @param name (unimportant, just a name for the web service, use anything)
	 * @param seq
	 * @return
	 */
	public static List<BlastBean> getHomologousProteinsViaEBI(String name, String seq, Enum... e )
	{ 
		System.out.println("invoking blast ");
		if(seq.length()>100)
			seq = seq.substring(0,99);
		if(TESTING)
			return new UniProtBlast("vyasj@student.uchc.edu", seq,  MaxNumberResultsOptions.FIFTY).getBlastHits();
		else
			return new UniProtBlast("vyasj@student.uchc.edu", seq,  MaxNumberResultsOptions.FIVE).getBlastHits();

	}
	
	
	
	public static boolean TESTING=true;
	public static void testProteins()
	{

		String[] ids = "P06493".split(",");
		for(String s : ids)
		{BlastBean b1=BioinformaticsUtilities.getUniref(s);
		lg.info("\nname="+b1.getName());
		lg.info("\nscore="+b1.getScore());
		lg.info("\nseq="+b1.getSequence());
		lg.info("\ntax="+b1.getTaxonomy());
		//lg.info("\nGO="+Utilities.collapse("\n",b1.getGoTerms().toArray()));

		}
	}
	
	public static String testUniprotConnection()
	{
		try
		{
			return UniProtBlast.testConn();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static String testBlast()
	{
		System.out.println("testing blast ");
		List<BlastBean> b=BioinformaticsUtilities.getHomologousProteinsViaEBI("JAX", BioinformaticsUtilities.NEF);
		String results = "";
		for(BlastBean b1 : b)
		{
			results+=b1.getId()+"-"+b1.getSequence();
			//System.out.println(b1.getName()+ " "+b1.getSequence()+ Utilities.collapse("\n\t",b1.getGoTerms()));
		}
		System.out.println("acquired = " + b.size());
		return results;
	}
	
	public static void main(String[] args)
	{
		System.out.println("TEsting uniref access .. ");
		BlastBean b = BioinformaticsUtilities.getUniref(("A7ZVT5"));
		System.out.println("uniref protein result : " + b.getName()+" "+b.sequence);

		//System.out.println("Testing blast " + b.sequence);
		//List<BlastBean> bbl = BioinformaticsUtilities.getHomologousProteinsViaEBI("j1", b.sequence );
		//System.out.println("Done testing blast proteins = " + bbl.size() + " -> " + StringUtils.join(bbl.toArray()));

	}
}
