/**
 * 
 */
package services;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import legacy.bioinformatics.BlastBean;
import legacy.bioinformatics.GPdbUtils;
import legacy.bioinformatics.UniProtBlast;

import org.apache.commons.beanutils.BeanUtils;
import org.biojava.bio.structure.Structure;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.OrderByConstants;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

import uk.ac.ebi.kraken.model.blast.parameters.BlastVersionOption;
import uk.ac.ebi.kraken.model.blast.parameters.DatabaseOptions;
import uk.ac.ebi.kraken.model.blast.parameters.MaxNumberResultsOptions;

/**
 * Getters get from the database, and can return null;
 * Importers go to external databases, and populate the database.
 * The flow of logic that a data accessor would follow would be 
 * 
 *   try to get something...
 *   
 *   	is it null
 *   
 *   		import it
 *   
 *   then try to get it again.
 * 
 * Currently only supports ONE user.  This can easily be modified
 * by having different db files for different users, and farming the homolog
 * engine out to a relational system which is more transactional.
 * 

 * @author jayunit100
 *
 */
public class VennStructureDAO 
{
	static final Logger lg = java.util.logging.Logger.getLogger("Venn Structure DAO");
	//TODO Somehow, windows interprets "/" as "C:/" and so this works ! At least in java 1.6
	//but this is a little bit of a hack so It should be changed to a relative path  ; some day. 
	public static final String databasePath="venn6";

	static String status = "";
	//inherits a status field by stealing unipor
	
	
	public VennStructureDAO()
	{
		status ="initialized";
		lg.info("1 create dao ---> database exists "+new File(databasePath).exists());
		lg.info("2 busy ---> " +this.busy);
		lg.info("3 test startup");
		startup();
		shutdown();
		lg.info("4 done testing.");
		status ="init complete";
	}
	
	/**
	 * Truncates the homolog objects.  This should be done every once in a while
	 * and also , it allows us to test blasts integration with the web app.
	 */
	public void clearHomologs()
	{
		startup( );
		Objects<VennHomologSet> o = odb.getObjects(VennHomologSet.class);
		for(Object oo : o )
		{
			lg.info("Deleting " + oo);
			
			odb.delete(oo);
		}
		shutdown();
	}
 
	public void seeHomologs()
	{
		startup();
		try 
		{
			Objects<VennHomologSet> o = odb.getObjects(VennHomologSet.class);
			lg.info(o.size()+"");
			for(VennHomologSet oo : o )
			{
				for(BlastBean b : oo.getChildren())
					lg.info(BeanUtils.describe(b)+"");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutdown();
		}
	}
	
	/**
	 * list of the structures in the db so far.
	 * @return
	 */
	public Iterator<VennStructure> seeStructures()
	{
		List<VennStructure> iter =null;
		startup();
		try 
		{
			Objects<VennStructure> o = odb.getObjects(VennStructure.class);
			lg.info(o.size()+"");
			Iterator<VennStructure> vs=o.iterator(OrderByConstants.ORDER_BY_ASC);
			iter=new ArrayList<VennStructure>();
			while(vs.hasNext())
				iter.add(vs.next());
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutdown();
		}
		return iter.iterator();
	}
	/**
	 * Imports Homologs from EBI via blast
	 * @param sequence
	 */
	public synchronized void importHomologsFromEBI(String sequence,boolean full)
	{
		lg.info("invoking ebi blast on sequence " + sequence);
		if(sequence.length()<4)
		{
			System.err.println("Not blasting short sequence.");
		}
		List<BlastBean> beans;
	
		if(! full)
		{
			beans = new UniProtBlast("j", sequence, MaxNumberResultsOptions.TEN).getBlastHits();
		}
		else
		{
			beans = new UniProtBlast("j",sequence,DatabaseOptions.UNIREF_90, BlastVersionOption.BLASTP,MaxNumberResultsOptions.FIVE_HUNDRED) .getBlastHits();
		}
		importHomologsDirectly(sequence,beans);
	}
	/**
	 * Delete all homolgs for a sequence.
	 * Presumeable there will be only one homolog set....
	 * but just to be sure assumes a whole collection of homolog sets.
	 * @param sequence
	 */
	public synchronized void clearHomologs(String sequence)
	{
		startup();
		Objects<VennHomologSet> o = odb.getObjects(VennHomologSet.class);
		lg.info(o.size()+"");
		//delete homolog set if it matches the input set.
		try
		{
			for(VennHomologSet oo : o )
			{
				if(oo.getParent().equals(sequence))
					odb.delete(oo);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			shutdown();
		}
	}
	
	/**
	 * Core import method.  Used by the ebi importer as well.
	 * @param sequence
	 * @param beans
	 */
	public synchronized void importHomologsDirectly(String sequence, List<BlastBean> beans)
	{
		status ="importing from EBI";

		try
		{
			//do we already have this homolog set ? 
			VennHomologSet set = this.getHomologs(sequence);
			
			this.clearHomologs(sequence);
			startup();
			if(set == null)
			{	
				set = new VennHomologSet(sequence,beans);
				odb.store(set);
			}
			else
			{
				set.children.addAll(beans);
				odb.store(set);
			}
			odb.commit();
				
			lg.info(sequence +"\n now has " + set.children.size() +" homologs");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutdown();
		}
		lg.info("Done importing homologs.... ");
		status ="DONE importing from EBI";
	}
	
	/**
	 * Returns all homologs in the db4o set for a sequence.
	 * @param sequence
	 * @return
	 */
	public synchronized VennHomologSet getHomologs(String sequence)
	{
		lg.info("Getting homologs.");
		startup();
		VennHomologSet r=null;
		try 
		{
			 VennHomologSet p = new VennHomologSet(sequence,null);
			
			 IQuery query = new CriteriaQuery(VennHomologSet.class, Where.equal("parent", sequence));
	         Objects<VennHomologSet> s =  odb.getObjects(query);

			if(s.hasNext())
				r=s.next();
			else
				System.err.println("Warning.. .could not obtain homologs from database query for sequence = ' " + sequence + " ' ");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			shutdown();
		}
		lg.info("Returning homolog set " + r );
		return r;
	}
	
	public synchronized void importSript(String structure, String script, List<BlastBean> homo)
	{
		new VennScript(structure,script,homo);
	}
	
	
	/**
	 * Import a structure from the pdb into db4o
	 * @param pdbid
	 */
	public synchronized Long importStructure(String pdbid)
	{
		startup();
		Long vi=null;
		try
		{
			String contents = GPdbUtils.getStructure(pdbid).toPDB();
			lg.info("Contents for pdb structure acquired " + contents.substring(0,10)+" now storing.");
			VennStructure r = new VennStructure(contents,pdbid,null);
			odb.store(r);
			vi=r.vennId;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutdown();
		}
		return vi;
	}	
	
	/**
	 * Import a structure from the pdb into db4o, a biojava structure (not nec a pdb !)
	 * Returns the unique long timestamp key.
	 * will return null if something goes wrong and no vennid is created.
	 * 
	 * Importantly, the constructor takes all null arguments.  This way the object 
	 * itself generates the Long key.  Then, the key is obtained from the object directly
	 * after it is serialized.	 That way, if something goes wrong in creating the object,
	 * a null key is returned.  
	 * 
	 * This is done because it would be dangerous to return a key to an object which has not been properly imported 
	 * into the venn database.
	 * @param pdbid
	 */
	public synchronized Long importStructure(Structure st)
	{
		startup();
		Long vid=null;
		try
		{
			VennStructure r = new VennStructure(st.toPDB(),null,null);
			odb.store(r);
			vid=r.vennId;
			lg.info("Stored with unique id " + vid);
		}
		finally
		{
			shutdown();
		}
		return vid;
	}	
	

	//place holder that stores wether the file is busy or taken at the moment.
	public static boolean busy=false;
	public ODB odb;
	/**
	 * Startup and shutdown open and close the database file
	 * to help avoid the dreaded file lock problem.
	 * @return
	 */
	private void startup()
	{
		lg.info(" attempting DB STARTUP " + databasePath);
		//wait to get a connection if the database is "busy".... 
		//neodatis is not a concurrent environment.
		while(busy)
		{
			lg.info("Venn Structure DAO is waiting to get a connection....");
			try
			{
				Thread.sleep(1);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		//now that the connection is available, open it and set busy to true.
		if(odb==null || odb.isClosed())
		{
			odb = ODBFactory.open( databasePath);
			busy=true;
		}
	}

	/**
	 * It is important to call this method after any and every query.  
	 * Without shutting down the connection, the database will be frozen and 
	 * the application will be completely impotent !  
	 * Neodatis only supports one concurrent connection.
	 */
	private void shutdown()
	{
		lg.info("DB SHUTDOWN");
		if(! odb.isClosed())
		{
			odb.close();
		}
		busy = false;
	}
	
	/**
	 * Returns null if the object doesnt exist in the db4o space.
	 * @param pdbid
	 * @param vennId
	 * @return
	 */
	public synchronized VennStructure getStructure(Long vennId)
	{
		VennStructure r=null;
		startup();
		try 
		{
			//create a prototype, to find the pdb by id in the database.
			//assumes that user was smart enough to not enter 0 !
			//0 will just give the first pdb structure in the db by order.
			lg.info("Searching for structure with venn id " + vennId);
				IQuery q = new CriteriaQuery(VennStructure.class,Where.equal("vennId", vennId));
				Objects<VennStructure> s = odb.getObjects(q);
			lg.info(s.size() +" structures found (hopefully?1) ! ");
				r= s.getFirst();

		} 
		finally 
		{
			shutdown();
		}
		return r;
	}
	
	/**
	 * For REMOTE RCSB Lookups, first call "importStructure" and then call this method.
	 * Looks up using pdbid. 
	 * The pdbID for custom structures is 'VENN', and 
	 * their true id is, of course, the vennid, which is a long;
	 * @param pdbId
	 * @return
	 */
	public synchronized VennStructure getStructure(String pdbId)
	{
		VennStructure r=null;
		startup();
		try 
		{
		
			IQuery q = new CriteriaQuery(VennStructure.class,Where.and().add(Where.equal("pdbid", pdbId)));
			Objects<VennStructure> s = odb.getObjects(q);
			if(s.hasNext())
				r = s.getFirst();
		} 
		finally 
		{
			shutdown();
		}
		
		return r;
		
	}
	
	public static void main(String[] args)
	{
		//new VennStructureDAO().importStructure("1AVZ");
		//Structure bioj=new VennStructureDAO().getStructure("1AVZ" ).getStructure();
		//lg.info(bioj.getChains().size());
		lg.info(System.currentTimeMillis()+"");	
		new VennStructureDAO( ).seeHomologs();
		//VennStructureDAO.clearHomologs();
	}
}
