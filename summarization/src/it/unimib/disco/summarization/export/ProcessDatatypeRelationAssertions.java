package it.unimib.disco.summarization.export;
import it.unimib.disco.summarization.dataset.OverallDatatypeRelationsCounting;
import it.unimib.disco.summarization.dataset.ParallelProcessing;

import java.io.File;

public class ProcessDatatypeRelationAssertions {

	public static void main(String[] args) throws Exception {
		
		Events.summarization();
		
		File sourceDirectory = new File(args[0]);  //../data/datasets/system-test/organized-splitted-deduplicated 
		File minimalTypesDirectory = new File(args[1]);    //../data/summaries/system-test/min-types/min-type-results
		File datatypes = new File(new File(args[2]), "count-datatype.txt");   //..//patterns/count-datatype.txt
		File properties = new File(new File(args[2]), "count-datatype-properties.txt");   //..//patterns/count-datatype-properties.txt
		File akps = new File(new File(args[2]), "datatype-akp.txt");   //..//patterns/datatype-akp.txt
		
		OverallDatatypeRelationsCounting counts = new OverallDatatypeRelationsCounting(datatypes, properties, akps, minimalTypesDirectory);
		
		new ParallelProcessing(sourceDirectory, "_dt_properties.nt").process(counts);//alla fine counts conterrà info(conteggio datatypes, properties e AKPs) sulle triple di tutti i _dt_properties.nt files di sourceDirectory. input: datatype relational assertions
	    
	    counts.endProcessing();  //scrive le info sui file dataTypes, properties e akps
	}	
}
