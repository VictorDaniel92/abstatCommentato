package it.unimib.disco.summarization.dataset;

import java.util.HashMap;

public class AKPObjectCount implements NTripleAnalysis{

	private MinimalTypes types;
	private HashMap<String, Long> akps;

	public AKPObjectCount(MinimalTypes minimalTypes) throws Exception {
		this.types = minimalTypes;
		this.akps = new HashMap<String, Long>();
	}

	public HashMap<String, Long> counts() {
		return akps;
	}

	public AKPObjectCount track(NTriple triple) {
		String object = triple.object().toString();
		String subject = triple.subject().toString();
		String property = triple.property().toString();

		for (String subjectType : types.of(subject)) {  //per ogni tipo minimo del sogg
			for(String objectType : types.of(object)){   //per ogni tipo minimo dell'ogg
				String key = subjectType + "##" + property + "##" + objectType;  //costruisco AKP
				if (!akps.containsKey(key))
					akps.put(key, 0l);
				akps.put(key, akps.get(key) + 1); //aggiunge all'hashmap akps key(AKP corrente) e aumentail contatore
			}
		}
		return this;
	}
}
