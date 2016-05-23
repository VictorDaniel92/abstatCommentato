package it.unimib.disco.summarization.dataset;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

public class ConceptCount {

	HashMap<String, Long> conceptCounts = new HashMap<String, Long>();
	
	public ConceptCount process(File file) throws Exception {
		InputFile counts = new TextInput(new FileSystemConnector(file));
		while(counts.hasNextLine()){        //itera sulle righe del file
			String[] splitted = counts.nextLine().split("##");   //separa il concetto dal conteggio
			String concept = splitted[0];
			Long count = Long.parseLong(splitted[1]);
			if(!conceptCounts.containsKey(concept)) conceptCounts.put(concept, 0l);  //se il concetto non è registrato in conceptCounts lo aggiungo (con contatore=0)
			conceptCounts.put(concept, conceptCounts.get(concept) + count);  //aggiorno contatore concetto
		}
		return this;
	}

	public ConceptCount writeResultsTo(File results) throws Exception {
		BulkTextOutput output = new BulkTextOutput(new FileSystemConnector(results), 10000);
		for(Entry<String, Long> count : conceptCounts.entrySet()){
			if(count.getValue() > 0) output.writeLine(count.getKey() + "##" + count.getValue());
		}
		output.close();
		return this;
	}
}
