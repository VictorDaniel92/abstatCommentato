package it.unimib.disco.summarization.dataset;

import java.util.HashMap;

public class AKPDatatypeCount implements NTripleAnalysis{

	private MinimalTypes types;
	private HashMap<String, Long> akps;  //mappa gli AKPs di tutte le triple che questo oggetto riceve con un contatore di occorrenze


	public AKPDatatypeCount(InputFile minimalTypes) throws Exception {
		this.types = new PartitionedMinimalTypes(minimalTypes); // passa il file _minType.txt. //l'oggetto types contiene un'hasmap che mappa le entity dellr triple nel file con i suoi minimal types
		this.akps = new HashMap<String, Long>();
	}

	public HashMap<String, Long> counts() {
		return akps;
	}
	//Salva in akps gli AKP ottenuti e occorrenze combinando tutti i minimal types del soggetto, la property della tripla e il datatype dell'oggetto di triple. 
	public AKPDatatypeCount track(NTriple triple) {
		String datatype = triple.dataType();   //ritorna il datatype del'oggetto della tripla: essendo dataTypreRelationAssertions l'oggetto può essere un literal oppure un double,float,date,eccc)
		String subject = triple.subject().toString();
		String property = triple.property().toString();
		
		for(String type : types.of(subject)){  //per ogni String:type dell'insieme di tipi minimi di subject
			String key = type + "##" + property + "##" + datatype; //costruisco AKP
			if(!akps.containsKey(key)) akps.put(key, 0l); 
			akps.put(key, akps.get(key) + 1); //aggiunge all'hashmap akps key(AKP corrente) e aumentail contatore
		}
		return this;
	}
}
