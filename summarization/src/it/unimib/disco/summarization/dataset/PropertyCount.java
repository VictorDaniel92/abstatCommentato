package it.unimib.disco.summarization.dataset;

import java.util.HashMap;

public class PropertyCount implements NTripleAnalysis{

	private HashMap<String, Long> counts;   //mappa i roperties di tutte le triple che questo oggetto riceve con un contatore di occorrenze

	public PropertyCount() {
		counts = new HashMap<String, Long>();
	}
	
	public HashMap<String, Long> counts() {
		return counts;
	}
	
	//aggiunge(o aggiorna il contatore) ilpredicato della tripla all'hashMap counts
	public PropertyCount track(NTriple triple) {
		String property = triple.property().asResource().getURI();
		if(!counts.containsKey(property)) counts.put(property, 0l);
		counts.put(property, counts.get(property) + 1);
		return this;
	}
}
