package it.unimib.disco.summarization.dataset;

import java.util.HashMap;

public class DatatypeCount implements NTripleAnalysis {

	private HashMap<String, Long> counts;  //mappa i datatypes di tutte le triple che questo oggetto riceve con un contatore di occorrenze

	public DatatypeCount() {
		counts = new HashMap<String, Long>();
	}
	
	public HashMap<String, Long> counts() {
		return counts;
	}

	@Override
	//aggiunge(o aggiorna) il datatype dell'ogg della tripla all'hashMap counts
	public NTripleAnalysis track(NTriple triple) {
		String datatype = triple.dataType(); //ritorna il datatype del'oggetto della tripla: essendo dataTypreRelationAssertions l'oggetto può essere un literal oppure un double,float,date,ecc)
		if(!counts.containsKey(datatype)) counts.put(datatype, 0l); //se il datatype non è registratato. aggiungilo
		counts.put(datatype, counts.get(datatype) + 1);  //aggiorna contatore
		return this;
	}
}
