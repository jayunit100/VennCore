package legacy.bioinformatics;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import legacy.util.Utilities;
import legacy.util.Utilities.Maths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.io.PDBFileParser;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.jama.Matrix;

import standalones.StructureFilter;
import datatypes.VennRestraint;



public class GPdbUtils {

	//used for alignments. 
	public static enum ATOM
	{
		CA("CA"),
		HEAVY("CA","C","N","O"),
		BACKBONE("CA","C","N"),
		ALL("C,CA,CB,CD,CD1,CD2,CE,CE1,CE2,CE3,CG,CG1,CG2,CH2,CZ,CZ2,CZ3,H,HA,HA2,HA3,HB,HB1,HB2,HB3,HD1,HD11,HD12,HD13,HD2,HD21,HD22,HD23,HD3,HE,HE1,HE2,HE21,HE22,HE3,HG,HG1,HG11,HG12,HG13,HG2,HG21,HG22,HG23,HG3,HH,HH11,HH12,HH2,HH21,HH22,HZ,HZ1,HZ2,HZ3,N,ND1,ND2,NE,NE1,NE2,NH1,NH2,NZ,O,OD1,OD2,OE1,OE2,OG,OG1,OH,SD");
		public String[] atoms;
		ATOM(String... values)
		{
			atoms = values;	
		}
	};
	
	public static Structure getCleanStructure(Structure inp)
	{
		StructureImpl i = new StructureImpl();
		for(Chain c : inp.getChains())
		{
			if(c.getAtomGroups().size()>1)
				i.addChain(c);
		}

		return i;
	}
	
	public static boolean isValidPdb(File c)
	{
		try
		{
			if(FileUtils.readFileToString(c).contains("ATOM"))
				return true;
			else
				System.err.println("Failed validating " + c.getAbsolutePath());
		}
		catch(Exception e)
		{
			return false;
		}
		return false;
	}
	
	/**
	 * Creates a map of file paths to structure objects. 
	 * Convenience method.
	 * @param csrDirectory
	 * @return
	 */
	public static TreeMap<File,Structure> readPdbDirectory(File csrDirectory, List<File> allF, int limit) throws Exception
	{
		TreeMap<File,Structure> m = new TreeMap<File,Structure>();

		Iterator<File> all=null;
		if(allF!=null)
			all=allF.iterator();
		else
			all = FileUtils.iterateFiles(csrDirectory, new String[]{"pdb"}, false);
		if(allF != null)
		lg.info( allF.size() + " " + allF.get(0));
		
		
		while(all.hasNext() && m.size()<limit)
		{
			File f = all.next();
			if(isValidPdb(f))
			{
				System.out.println("valid");
				m.put(f, GPdbUtils.getModel(GPdbUtils.getStructure(f),0));
			}
			else
				System.out.println("invalid");
		}
	
		return m;
	}
	
	/**
	 * returns atom which has coords of X Y Z = standard deviation of x, y ,z .
	 * @param allAtoms
	 * @return
	 */
	public static Atom getStandardDeviation(Atom[] allAtoms)
	{
		Vector<Double> xs=new Vector<Double>();
		Vector<Double> ys=new Vector<Double>();
		Vector<Double> zs=new Vector<Double>();

		for(Atom a : allAtoms)
		{
			xs.add(a.getX());
			ys.add(a.getX());
			zs.add(a.getX());
		}
		
		double xstd=Utilities.Maths.stddev(xs.toArray(new Double[]{}));
		double ystd=Utilities.Maths.stddev(ys.toArray(new Double[]{}));
		double zstd=Utilities.Maths.stddev(zs.toArray(new Double[]{}));
		
		Atom a = new AtomImpl();
		a.setX(xstd);
		a.setY(ystd);
		a.setZ(zstd);
		return a;
	}
	
	/**
	 * Returns an AVeraged out atom coordinate.
	 * @param allAtoms
	 * @return
	 */
	public static Atom getAverage(Atom[] allAtoms)
	{

		Vector<Double> xs=new Vector<Double>();
		Vector<Double> ys=new Vector<Double>();
		Vector<Double> zs=new Vector<Double>();

		for(Atom a : allAtoms)
		{
			xs.add(a.getX());
			ys.add(a.getX());
			zs.add(a.getX());
		}
		
		double xstd=Utilities.Maths.average(xs);
		double ystd=Utilities.Maths.average(ys);
		double zstd=Utilities.Maths.average(zs);
		
		Atom a = new AtomImpl();
		a.setX(xstd);
		a.setY(ystd);
		a.setZ(zstd);
		return a;
	
	}
	
	public static double getDistance(Chain c, int res1, String name1, int res2, String name2) throws Exception
	{
		Group g1=GPdbUtils.getGroupAt(c, res1-1);
		Group g2=GPdbUtils.getGroupAt(c, res2-1);
		//System.out.println("Getting atoms " + name1 + " " + name2);
		//System.out.println("Getting resude " + g1 + " / " + g2);

		return Calc.getDistance(g1.getAtom(name1), g2.getAtom(name2));
	}
	/**
	 * Returns the atom array corresponding to the input enum.  Just a thin wrapper to biojava STructureTools.
	 * @param s
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static Atom[] getAtoms(Structure s, ATOM type) throws Exception
	{
		return StructureTools.getAtomArray(s, type.atoms);
	}
	
	public static Logger lg = Logger.getLogger("Pdb Utils");
	static
	{
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout( ));
		lg.addAppender(appender);
	}
	public static String cfg = "Gly=G:Ala=A:Val=V:Leu=L:Ile=I:Met=M:Phe=F:Trp=W:Pro=P:Ser=S:Thr=T:Cys=C:Tyr=Y:Asn=N:Gln=Q:Asp=D:Glu=E:Lys=K:Arg=R:His=H";

	static String PDBURL="http://www.rcsb.org/pdb/files/<0>.<1>";

	/**
	 * Parses a structure from a string.  
	 * Adds spaces to the lines to be greater than DBREF.
	 * @param contents
	 * @return
	 */
	public static Structure parseStructure(String contents)
	{
		try
		{
			lg.info("parsing structure of length " + contents.length() +" characters");
			
			 StringBuffer fixed = new StringBuffer();
			 for(String line : contents.split(newline))
			 {
				 if(line.contains("DBREF") && line.length()<68)
				 {
					 String line2 = line+"     ";
					 lg.info("fixing dbref line : (" + line + ") to be longer (" + line2+")");
					 fixed.append(line2+newline);
				 }
				 else
					 fixed.append(line+newline);
			 }
			 System.out.println("Starting Structure Read ...");
			 Structure s =  new org.biojava.bio.structure.io.PDBFileParser() .parsePDBFile(new BufferedReader(new StringReader(fixed.toString())));
			 System.out.println("Ending structure read, "+s.getPDBCode());
			 return s;
		}
		catch(Throwable e)
		{
			System.out.println("GPDBUtils Exception : "+e.getMessage());
			//e.printStackTrace();
		}
		return null;
	}

	public static Structure getModel(Structure s1, int m)
	{
		Structure s = new StructureImpl();
		s.addModel(s1.getModel(m));
		return s;
	}
	
	/**
	 * This method may be unnecessary for cyana, since in cyana the minimized structure 
	 * appears  to generally by model 0.  Just use the "getModel(0)" method.
	 * 
	 * Gets an average coordinate structure, and picks the structure which is closest
	 * to that average structure.
	 * 
	 * untested.
	 * @param nmr
	 * @return
	 * @throws Exception
	 */
	public static Structure getMinimizedMeanStructure(Structure nmr) throws Exception
	{
		System.out.println("Starting minimized mean calcultaion on bundle with model count = " + nmr.nrModels());
		Structure mean = getAverage(nmr);

		System.out.println("MEan calculated, models should be 1 .... " + mean.nrModels());

		Structure minimizedMean=null;
		double lowestRmsd = Double.MAX_VALUE;

		for(int i =0 ; i < nmr.nrModels(); i++)
		{
			//System.out.println("Scanning structures for minimal mean " + i + " of " + nmr.nrModels() +", lowest rmsd so far " + lowestRmsd );

			Structure modelI = getModel(nmr,i);
			//calculate the rmsd and , if its lower from the mean then the last rmsd, 
			//set model(i) as the current minimized Mean.
			//use CA since its just a quick alignment to find a mean structure, RMSD precision is unimportant)
			double irmsd = StructureFilter.alignCE(modelI,mean,ATOM.CA);
			if(irmsd < lowestRmsd)
			{
				minimizedMean=modelI;
				lowestRmsd = irmsd;
				System.out.println("new minimized mean : model # = " + i+", rmsd is "+irmsd);
			}
			//else
				//System.out.println("model # rmsd is higher than current . " + i + " " + irmsd);
		}
		return minimizedMean;
		
	}
	
	/**
	 * 
	 * Provides average for a single chain NMR structure.
	 * Untested and appears somewhat unreliable. 
	 * Takes a file because multiple reads must be done, and 
	 * I didnt want to have to clone the structure over and over since
	 * clone .
	 * @param s
	 * @return
	 */
	public static Structure getAverage(Structure original) 
	{
		Structure copy = original.clone();
		Chain c1 = new ChainImpl();
		for(Group g11 : copy.getModel(0).get(0).getAtomGroups())
		{
			c1.addGroup(g11);
		}
		//System.out.println("Start calculate average ");
		
		for(int gI=0; gI < copy.getChain(0).getAtomLength() ; gI++)
		{
			for(int atomI=0; atomI< c1.getAtomGroup(gI).getAtoms().size(); atomI++)
			{
				Vector<Double> x=new Vector<Double>();
				Vector<Double> y=new Vector<Double>();
				Vector<Double> z=new Vector<Double>();

				for(int m = 0 ; m < copy.nrModels(); m++)
				{
					//System.out.println("calculate average, model " + m);
					try
					{
						Atom a = copy.getModel(m).get(0).getAtomGroup(gI).getAtom(atomI);
						//System.out.println("Setting atom " + a);
						x.add(a.getX());
						y.add(a.getY());
						z.add(a.getZ());
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				Double x0,y0,z0;
				x0=Utilities.Maths.average(x);
				y0=Utilities.Maths.average(y);
				z0=Utilities.Maths.average(z);

				//System.out.println("final x y z = " + x0 + " " + y0 + " " + z0);
				c1.getAtomGroup(gI).getAtoms().get(atomI).setX(x0);
				c1.getAtomGroup(gI).getAtoms().get(atomI).setY(y0); 
				c1.getAtomGroup(gI).getAtoms().get(atomI).setZ(z0); 
				//System.out.println(c1.getAtomGroup(gI).getAtoms().get(atomI));
				//System.out.println("Done , final " + x+" " + y+" "+z);
			}
		}
		//System.out.println("Done calculating average ");
		Structure sNew = new StructureImpl();
		sNew.addChain(c1);
		return sNew;
	}
	
	public static Character getChar(Group g)
	{
		//System.out.println("pdbutils: get char " + g.getPDBName());
		return getCharForAminoAcid(g.getPDBName());
	}
	
	static long lastskipwarn=System.currentTimeMillis();
	public static Vector<AminoAcidGroup> getGroupsInOrder(List<Group> groups){
		Vector<AminoAcidGroup> aig = new Vector<AminoAcidGroup>();
		int skips = 0;
		for(int i =0;i<groups.size();i++)
		{
			Group g = groups.get(i);
			Character c = GPdbUtils.getChar(g);
			if(c != null)
			{
				aig.add(new AminoAcidGroup(i,g,c));
			}
			else
				skips++;
			//skip
			//else
				//System.err.println("\t unknown aa group " + g);
		}
		if(skips>0 && System.currentTimeMillis()-lastskipwarn>1000)
		{
			lg.info("skipped " + skips + " atom groups ....");
			lastskipwarn=System.currentTimeMillis();
		}
		return getGroupsInOrder(aig);
	}
	
	public static Vector<AminoAcidGroup> getGroupsInOrder(Collection<AminoAcidGroup> groups)
	{
		Vector<AminoAcidGroup> a = new Vector<AminoAcidGroup> ();
		a.addAll(groups);
		Collections.sort(a,new Comparator()
		{
			public int compare(Object arg0, Object arg1) 
			{
				AminoAcidGroup a1 = (AminoAcidGroup) arg0;
				AminoAcidGroup a2 = (AminoAcidGroup) arg1;
				Integer a1Location = Integer.parseInt(a1.g.getPDBCode());
				Integer a2Location = Integer.parseInt(a2.g.getPDBCode());
				return a1Location.compareTo(a2Location);
			}
		});
		return a;
	}
	
	/**
	 * Solves the notorious PDB numbering problem... residues are numbered starting at 0.
	 * @param c
	 * @param index
	 * @return
	 */
	public static Group getGroupAt(Chain c, int index)
	{
		Iterator<AminoAcidGroup> g= getGroupsInOrder(c.getAtomGroups()).iterator();
		while(g.hasNext())
		{
			AminoAcidGroup gi = g.next();
			if(gi.index==index)
			{
				return gi.g;
			}
		}
		System.err.println(index + " is out of range for "+c.getAtomLength()+ " groups in amino acid chain "+ c.getName());
		return null;
	}
	
	
	public static Character getCharForAminoAcid(String aa)
	{
		String[] residues = cfg.toUpperCase().split(":");
		assert residues.length > 19;
		for(String x : residues)
		{
			
			if(StringUtils.contains(x.toLowerCase(),aa.toLowerCase()))
			{
				return x.split("=")[1].charAt(0);
			}
		}
		
		return null;
	}
	

	public static class AtomDistance implements Comparable<AtomDistance>
	{
		public String a1;
		public String a2;
		public String r1Name;
		public String r2Name;
		public int r1;
		public int r2;
		public double std;
		public double avg;
		public AtomDistance(String a1, String a2, String r1Name, String r2Name,
				int r1, int r2,double avg, double stddev) {
			super();
			this.a1 = a1;
			this.a2 = a2;
			this.r1Name = r1Name;
			this.r2Name = r2Name;
			this.r1 = r1;
			this.r2 = r2;
			this.avg=avg;
			std=stddev;
		}
		Vector<Double> distances=new Vector<Double>();
		public void add(Atom a, Atom b) throws Exception
		{
			distances.add(Calc.getDistance(a, b));
		}
		public void finalize()
		{
			avg=Maths.average(distances);
			std=Maths.stddev(distances.toArray(new Double[]{}));
		}
		public String toString()
		{
			return StringUtils.join(new Object[]{r1,r1Name,r2,r2Name,a1,a2,avg,new DecimalFormat("###.###").format(std)}," ") +" -> " + significance();
		}

		/**
		 * A Working mans definition of restraint significance : positively associated with
		 * linear residue distance.  Negatively associated with magnitude and standard devitaion. 
		 * 
		 * So for example, Alanaine 1 and Glycine 80 are 2 angstroms apart would  be a very significant restraint.
		 * ALA 1 and MET 2 are 3 angstroms apart would be trivial.
		 * @return
		 */
		public Double significance()
		{
			if(Math.abs(r1-r2)<2)
				return Double.NEGATIVE_INFINITY;
			else
				return (double) ((r1-r2)/avg) / (double)(std);	
		}
		
		public int compareTo(AtomDistance o) 
		{
			// TODO Auto-generated method stub
			return o.significance().compareTo(this.significance());
		}
		
		public VennRestraint getRestraint()
		{
			VennRestraint r = new VennRestraint();
			r.setRes1atom(this.a1);
			r.setRes2atom(this.a2);
			r.setRes1Num(this.r1);
			r.setRes2Num(this.r2);
			r.setRes1Name(this.r1Name);
			r.setRes2name(this.r2Name);
			//scaled by .5 angstroms
			r.setUdl((float)avg);
				
			return r;
		}
	}
	public static String newline = System.getProperty("line.separator");

	/**
	 * Distance matrix representation of a protein.
	 * works okay to generate restraints from CA.  but ATOM.HEAVY is better.
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static Matrix getDistanceMatrix(Chain c, ATOM a) throws Exception
	{
		double[][] m = new double[c.getAtomGroups().size()][c.getAtomGroups().size()];
		
		Atom[] ar1 =GPdbUtils.getAtoms(GPdbUtils.getStructure(c), a);
		Atom[] ar2 =GPdbUtils.getAtoms(GPdbUtils.getStructure(c), a);
		
		for(int i = 0 ; i < m.length; i++)
		{
			for(int j = 0; j < m.length; j++)
			{
				m[i][j]=Calc.getDistance(ar1[i],ar2[j]);
			}
		}
		Matrix ma = new Matrix(m);

		return ma;
	}
	/**
	 * Distance matrix representation of a protein.
	 * works okay to generate restraints from CA.  but ATOM.HEAVY is better.
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static Matrix getDistanceMatrix(Chain c, String a, String b) throws Exception
	{
		double[][] m = new double[c.getAtomGroups().size()][c.getAtomGroups().size()];
		
		Atom[] ar1 =StructureTools.getAtomArray(GPdbUtils.getStructure(c), new String[]{a});
		Atom[] ar2 =StructureTools.getAtomArray(GPdbUtils.getStructure(c), new String[]{b});
		
		for(int i = 0 ; i < m.length; i++)
		{
			for(int j = 0; j < m.length; j++)
			{
				m[i][j]=Calc.getDistance(ar1[i],ar2[j]);
			}
		}
		Matrix ma = new Matrix(m);

		return ma;
	}
	
	/**
	 * This method calculates an "AtomDistance" summary object, to be used
	 * for predicting restraints.  Atom distances are numberd from 1.
	 * @param chains
	 * @return
	 * @throws Exception
	 */
	public static Vector<AtomDistance> getSyntheticRestraintsFromBundle(List<Chain> chains) throws Exception
	{ 
		lg.info("Chains to process = " + chains.size());
		Matrix[] distances= new Matrix[chains.size()];
		
		Vector<AtomDistance> atomDistances = new Vector<AtomDistance>();

		for(String a : ATOM.HEAVY.atoms)
			for(String b : ATOM.HEAVY.atoms)
			{
				for(int c = 0; c < chains.size(); c++)
				{
					//hard coded for CA
					distances[c] = GPdbUtils.getDistanceMatrix(chains.get(c),a,b);
				}
				List<Group> modelSequence = chains.get(0).getAtomGroups();
				//now do cross matrix statistics.
				for(int i = 0; i < modelSequence.size(); i++)
				{
					for(int j = 0 ; j < modelSequence.size() ; j++)
					{
						if(i>=j)//only half the matrix 
						{
							Vector<Double> distancesIJ = new Vector<Double>();
							//get the ith/jth distance from each matrix, and add it to the vector 
							//so you can get the mean interatomic distance between atom i/j;
							for(Matrix m : distances)
							{
								distancesIJ.add(m.get(i, j));
							}
							double stddev=Utilities.Maths.stddev(distancesIJ.toArray(new Double[]{}));
							double avg=Utilities.Maths.average(distancesIJ);
							//hard coded for CA.
							AtomDistance ad = new AtomDistance(a, b, modelSequence.get(i).getPDBName(), modelSequence.get(j).getPDBName(), i+1,j+1,avg,stddev);
							atomDistances.add(ad);
						}
					}
				}
			}
		Collections.sort(atomDistances);
		lg.info("Total restraints : " +atomDistances.size());
		return atomDistances;
	}

	/**
	public static Structure getStructureCIF(String pdbId)
	{
		lg.info("now getting pdbid " + pdbId);
		if(pdbId==null)
			Utilities.breakException("pdbid was null ! ");
		try
		{
			//if file looks like a path...
			//else, get it from rcsb.
			MMcifParser r = new SimpleMMcifParser();

			String url = GPdbUtils.PDBURL.replaceAll("<0>",pdbId).replaceAll("<1>", "cif");
			
			SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

			r.addMMcifConsumer(consumer);
			r.parse(new BufferedReader(new InputStreamReader(getStream(url))));
			
			Structure s = consumer.getStructure();
			
			return s;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	**/
	public static void main(String[] args)
	{
		/**
		//This example minimizes a structure, writes it to a file, and prints the path of the file out.
		Structure s = GPdbUtils.getStructure(new File("/raid/Users/vyas/nmr/cyana/cyana_exec/16790.pdb"));
		try
		{
			File view = new File("temp.pdb");
			GPdbUtils.getAtoms(s, ATOM.ALL);
			Structure minmean = GPdbUtils.getMinimizedMeanStructure(s);
			if(minmean==null)
				System.err.println("Failed, null output from getMinimizedMean");
			FileUtils.writeStringToFile(view, minmean.toPDB());
			System.out.println(view.getAbsolutePath());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		**/
		
		//This example goes to rcsb, gets a file, and prints contents to System.out
		 Structure s = GPdbUtils.getStructure("1K6Y");
		 GPdbUtils.parseStructure(s.toString());
		//System.out.println(s.toPDB() );
		//System.out.println(parseStructure(s.toPDB()));
		
		//Another remote example.....
		//System.out.println(s.getChains());
		//remoteFetch(new File("newPDB"),"1EFX",PDB_FILE.cif,true);
		//System.out.println(PDB.getCharForAminoAcid("TYR"));
		//System.out.println(PdbUtils.getAtomPrefixString("3ERR"));

		
	}


	public static Structure getStructure(File f,int model)
	{
		 PDBFileReader pdbreader = new PDBFileReader();
		 try
		 {
		     Structure struc = pdbreader.getStructure(f);
		     List<Chain> a1=struc.clone().getModel(model);
		     Structure s2 = new StructureImpl();
		     s2.addModel(a1);
		     return s2;
		 } 
		 catch (Exception e)
		 {
		     e.printStackTrace();
		 }
		 System.err.println("GPDBUtils Couldnt read file " + f.getAbsolutePath());
		 return null;
	}
	
	public static Structure getStructure(Chain c)
	{
		Structure s2 = new StructureImpl();
		s2.addModel(Arrays.asList(c));
		return s2;
	}
	
	public static Structure getStructure(File f)
	{
		 
		 PDBFileReader pdbreader = new PDBFileReader();
		 
		 try{
		     Structure struc = pdbreader.getStructure(f);
		     return struc;
		 } catch (Exception e){
		     e.printStackTrace();
		 }
		 System.err.println("GPDBUtils Couldnt read file " + f.getAbsolutePath());
		 return null;
	}
	/**
	 * Takes in pdbcontents, pdbid, or pdbpath .
	 * Works with 1bpy.
	 * @param pdbid
	 * @param f
	 * @return
	 */
	public static Structure getStructure(String pdbid )
	{
		System.out.println("Going to PDB for pdbid: '"+pdbid+"'");
		if(pdbid==null)
			Utilities.breakException("pdbid was null ! ");
		try
		{
			String file1=null;
			
			//if file looks like a path...
			//else, get it from rcsb.
			PDBFileParser r = new PDBFileParser();
			String url = GPdbUtils.PDBURL.replaceAll("<0>",pdbid).replaceAll("<1>", "pdb");
			Structure s= r.parsePDBFile(getStream(url));
			return s;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static InputStream getStream(String address) 
	{
		StringBuffer b = new StringBuffer();
		URLConnection conn = null;
		InputStream  in = null;
		try 
		{
			URL url = new URL(address);
			conn = url.openConnection();
			in = conn.getInputStream();
			return in;
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	
	
	public static int getMinIndex(Character chain,Structure s)
	{
		try{
		List<Group> c = s.getChainByPDB(chain+"").getAtomGroups();
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		for(Group gg : c)
		{
			//System.out.println(gg.getPDBCode() + " " + gg.getPDBName());
			int ii=Integer.parseInt(gg.getPDBCode());
			if(ii< min)
				min=ii;
			if(ii>max)
				max=ii;
		}
		return min;
		}
		catch(Exception e)
		{e.printStackTrace();}
		return Integer.MAX_VALUE;
	}
	 
	 
}
