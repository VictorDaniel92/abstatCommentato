package it.unimib.disco.summarization.export;

import it.unimib.disco.summarization.dataset.OverallObjectRelationsCounting;
import it.unimib.disco.summarization.dataset.ParallelProcessing;

import java.io.File;

public class ProcessObjectRelationAssertions {
	
	public static void main(String[] args) throws Exception {
		
		Events.summarization();
		
		File sourceDirectory = new File(args[0]);    //../data/datasets/system-test/organized-splitted-deduplicated 
		File minimalTypesDirectory = new File(args[1]);    //../data/summaries/system-test/min-types/min-type-results
		File properties = new File(new File(args[2]), "count-object-properties.txt");   //..//patterns/count-datatype-properties.txt
		File akps = new File(new File(args[2]), "object-akp.txt");    //..//patterns/datatype-akp.txt
		
		OverallObjectRelationsCounting counts = new OverallObjectRelationsCounting(properties, akps, minimalTypesDirectory);
		
		new ParallelProcessing(sourceDirectory, "_obj_properties.nt").process(counts);//alla fine counts conterrà info(conteggio properties e AKPs) sulle triple di tutti gli _obj_properties.nt files di sourceDirectory. input: object relational assertions
	    
	    counts.endProcessing();  //scrive le info sui file properties e akps
	}	
}
