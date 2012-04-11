package legacy.bioinformatics;
import java.util.ArrayList;
import java.util.List;

import legacy.util.Utilities;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import uk.ac.ebi.kraken.interfaces.uniref.UniRefEntry;
import uk.ac.ebi.kraken.model.blast.JobStatus;
import uk.ac.ebi.kraken.model.blast.parameters.BlastVersionOption;
import uk.ac.ebi.kraken.model.blast.parameters.DatabaseOptions;
import uk.ac.ebi.kraken.model.blast.parameters.MaxNumberResultsOptions;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniRefQueryService;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastData;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastHit;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastInput;

/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class UniProtBlast {

	public static Logger lg = Logger.getLogger(UniProtBlast.class);
	
	public static void main(String[] args)
	{
		 new UniProtBlast
		 (
				 "jaypro3",
				 "VPLRPMTYKAAVDLSHFLKEKGGLEGLIHSQRRQDILDLWIYHTQGYFPDWQNYTPGPGVRYPLTFGWCYKLVPVEVLEWRFDSRLAFHHVARELHPEYF",
				 DatabaseOptions.UNIREF_50,
				 BlastVersionOption.BLASTP,
				 MaxNumberResultsOptions.FIFTY
		 );
		 //lg.info(c.size());
	}

	String s,n;
	List<BlastBean> beans;
	public UniProtBlast(String n, String seq, MaxNumberResultsOptions num)
	{
		this.n=n;
		this.s=seq;
		this.num=num;
		beans=this.getBlastHits();
	}

	public UniProtBlast(String n, String seq, DatabaseOptions db, BlastVersionOption v, MaxNumberResultsOptions num)
	{
		this.n=n;
		this.s=seq;
		this.num=num;
		this.blastType=v;
		this.db=db;
		beans=this.getBlastHits();
	}

	DatabaseOptions db = DatabaseOptions.UNIREF_90;
    uk.ac.ebi.kraken.model.blast.parameters.BlastVersionOption blastType = BlastVersionOption.BLASTP;
    uk.ac.ebi.kraken.model.blast.parameters.MaxNumberResultsOptions num = MaxNumberResultsOptions.FIVE_HUNDRED;

    public static String testConn() throws Exception
    {
    	DatabaseOptions db = DatabaseOptions.UNIREF_90;
        uk.ac.ebi.kraken.model.blast.parameters.BlastVersionOption blastType = BlastVersionOption.BLASTP;
        uk.ac.ebi.kraken.model.blast.parameters.MaxNumberResultsOptions num = MaxNumberResultsOptions.FIVE;
        String s=BioinformaticsUtilities.NEF;

        lg.info("Starting blast test on hiv nef " + BioinformaticsUtilities.NEF);
    	 //Get the UniProt Service. This is how to access the blast service
    	//you can use uniref,uniprot,etc....
    	UniRefQueryService service = UniProtJAPI.factory.getUniRefQueryService();

    	if(s==null)
    		Utilities.breakException("null sequence");
    	System.out.println("uniprotblast : input " + db + " " + s + " " + blastType + " " + num + " ; ");
    	//Create a blast input with a Database and sequence (adds the local version, max number options).
        BlastInput input = new BlastInput(db, s,blastType,num);
        try
        {
	        	//Submitting the input to the service will return a job id
	        String jobid = service.submitBlast(input);
	        return "Job id succesfully acquired " + jobid+" status of submission @ "+Utilities.getTimestamp() +" = "+service.checkStatus(jobid);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return e.getMessage();
        }
    }
	/**
	 * 
	 * MaxNumberResultsOptions size = FIVE_HUNDRED; 
	   ExpectedThreshold threshold = TEN ;
	   are good defaults to add to the ENUM array.  
	   
	    See BioinformaticsUtilities. for api to use this.  Typically
	    this method should only be called from that class.
	 * 
	 * @param name
	 * @param sequence1
	 * @param e
	 * @return
	 */
    public List<BlastBean> getBlastHits( ) 
    {
    	lg.info("Starting blast...");
    	ArrayList<BlastBean> beans = new ArrayList<BlastBean>();
    	 //Get the UniProt Service. This is how to access the blast service
    	//you can use uniref,uniprot,etc....
    	UniRefQueryService service = UniProtJAPI.factory.getUniRefQueryService();

    	if(s==null)
    		Utilities.breakException("null sequence");
    	System.out.println("uniprotblast : input " + db + " " + s + " " + blastType + " " + num + " ; ");
    	//Create a blast input with a Database and sequence (adds the local version, max number options).
        BlastInput input = new BlastInput(db, s,blastType,num);
        
        try{
	        	//Submitting the input to the service will return a job id
	        String jobid = service.submitBlast(input);
	        JobStatus status  =service.checkStatus(jobid);
	        System.out.println("Job id = '" + jobid + "'");
	        if(jobid==null || jobid.trim().length()<2)
	        	System.err.println("Warning... invalid or null uniprot job id ! " + jobid);
	        //Use this jobid to check the service to see if the job is complete
	        int i =0;
	        
	        while (status != JobStatus.FINISHED && status != JobStatus.FAILED) 
	        {
	        	System.out.println("(sleep waiting for homologs) " +i);
	        	Thread.sleep(6000);
	              lg.info("Checking Blast for job id " + jobid + " - " + i++ );
	              int seconds = i*6;
	              if(seconds > 20*60)
	            	  throw new Exception("Uniprot took too over seconds..." + seconds + " gave up.");
	        	status = service.checkStatus(jobid);
	        	System.out.println("job status is  " + status.name());
	        }
	        
	        BlastData<UniRefEntry> blastResult = service.getResults(jobid);
	    	List<BlastHit<UniRefEntry>> hits = blastResult.getBlastHits();
	    	
	    	   for(BlastHit<UniRefEntry> h : hits)
	    	   {
	    		   System.out.print(" Uniprot Blast hit : " + h.getEntry());
	    		   BlastBean b = BioinformaticsUtilities.getUniref(h.getHit().getHitId());
	    		   if(b != null)
	    		   {
	    			   if(b.getSequence()==null)
	    				   System.err.println("error , blast bean has null sequence.");
	        		   System.out.print(" 1 comparing sequence " + b.getSequence());

	    			   b.setScore(Utilities.StringUt.similarity(s, b.getSequence()));
	    			   lg.info("Blast for polymer complete, federating homologs from Uniprot... - last hit = " + b);
	
	        		   System.out.print(" 2 " );
	    			   
		    		   if(h.getEntry().getRepresentativeMember() != null && h.getEntry().getRepresentativeMember().getNCBITaxonomy() != null)
		    		   {
		    			   String tx = h.getEntry().getRepresentativeMember().getNCBITaxonomy().getValue();
		    			   b.setTaxid(Integer.parseInt(tx));
		    		   }
	
	        		   System.out.print(" 3 " );
		    		   
		    		   beans.add(b);
	    		   
		    		   System.out.print(" UniProtBlast:"+BeanUtils.describe(b) + " \n");
	    		   }
	    		 }
	    	   System.out.println("Total so far = " +  beans.size());
	   }
       catch(Exception ex)
       {
    	   ex.printStackTrace();
       }
        return beans;
    }
    
}