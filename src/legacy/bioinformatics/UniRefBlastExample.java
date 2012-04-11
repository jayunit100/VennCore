package legacy.bioinformatics;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;

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

public class UniRefBlastExample {

    public static void main(String[] args) {// Create UniProt query service
    UniProtQueryService uniProtQueryService = UniProtJAPI.factory.getUniProtQueryService();

    //Create a list of accession numbers (both primary and seconday are acceptable)
    List<String> accList = new ArrayList<String>();
    accList.add("O60243");
    accList.add("Q8IZP7");
    accList.add("P02070");
    //Isoform IDs are acceptable as well 
    accList.add("Q4R572-1");
    //as well as entry IDs 
    accList.add("14310_ARATH");

    Query query = UniProtQueryBuilder.buildIDListQuery(accList);
    
    System.out.println(query.toString());
    
    EntryIterator<UniProtEntry> entries = uniProtQueryService.getEntryIterator(query);
    for (UniProtEntry entry : entries) 
    {
      System.out.println("entry.getUniProtId() = " + entry.getUniProtId());
    }
    }
}
