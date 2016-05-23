package it.unimib.disco.summarization.dataset;

import it.unimib.disco.summarization.export.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.vocabulary.OWL;


public class PartitionedMinimalTypes implements MinimalTypes {

	private HashMap<String, List<String>> types;

	public PartitionedMinimalTypes(InputFile types) throws Exception {
		this.types = buildMinimalTypes(types);  //trasforma le righe del file _minType.txt nel contenuto dell'HashMaps types
	}

	@Override
	public List<String> of(String entity) {
		List<String> result = types.get(entity);
		if(result == null){
			result = new ArrayList<String>();
			result.add(OWL.Thing.toString());
		}
		return result;
	}
	
	private HashMap<String, List<String>> buildMinimalTypes(InputFile types) throws Exception {   // types è un file _minType.txt
		HashMap<String, List<String>> minimalTypes = new HashMap<String, List<String>>();
		while(types.hasNextLine()){
			String nextLine = types.nextLine();  //per ogni riga del file _minType.txt
			try{
				List<String> line = Arrays.asList(nextLine.replace("#-#", "##").split("##"));
				minimalTypes.put(line.get(1), line.subList(2, line.size()));//inserisce nell'HashMap minimaltypes l'entity(line.get(1) e il resto che sono i suoi tipi minimi
			}catch(Exception e){
				Events.summarization().error("processing line " + nextLine + " - " + types.name(), e);	
			}
		}
		return minimalTypes;
	}
}
