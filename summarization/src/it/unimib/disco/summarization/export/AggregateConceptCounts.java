package it.unimib.disco.summarization.export;

import it.unimib.disco.summarization.dataset.ConceptCount;
import it.unimib.disco.summarization.dataset.Files;

import java.io.File;

public class AggregateConceptCounts {

	public static void main(String[] args) throws Exception {
		
		Events.summarization();
		
		File sourceDirectory = new File(args[0]);          // = data/summaries/system-test/min-types/min-type-results 
		File targetFile = new File(args[1], "count-concepts.txt");   // = abstat/data/summaries/system-test/patterns/count-concepts.txt
		
		ConceptCount counts = new ConceptCount();
		for(File file : new Files().get(sourceDirectory, "_countConcepts.txt")){ //per ogni file in ../min-types/min-type-results che finisce con countConcepts.txt
			try{
				counts.process(file);   //processa il file: per ogni concetto registra il numero di volte che occorre nel file
			}catch(Exception e){
				Events.summarization().error("processing " + file, e);
			}
		}
		counts.writeResultsTo(targetFile);
	}
}
