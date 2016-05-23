package it.unimib.disco.summarization.dataset;

import it.unimib.disco.summarization.ontology.ConceptExtractor;
import it.unimib.disco.summarization.ontology.Concepts;
import it.unimib.disco.summarization.ontology.OntologyDomainRangeExtractor;
import it.unimib.disco.summarization.ontology.OntologySubclassOfExtractor;
import it.unimib.disco.summarization.ontology.Properties;
import it.unimib.disco.summarization.ontology.PropertyExtractor;
import it.unimib.disco.summarization.ontology.TypeGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.OWL;

public class MinimalTypesCalculation implements Processing{

	private TypeGraph graph;
	private Concepts concepts;
	private List<String> subclassRelations;  //ogni stringa conterrà un "concetto ## supertipo"
	private File targetDirectory;

	public MinimalTypesCalculation(OntModel ontology, File targetDirectory) throws Exception {
		Concepts concepts = extractConcepts(ontology);
		
		this.targetDirectory = targetDirectory;
		this.concepts = concepts;
		this.graph = new TypeGraph(concepts, subclassRelations);  //torna il typeGraph(vertici=concetti, archi=reazioni di subClassOf)
	}

	@Override
	public void endProcessing() throws Exception {}
	
	@Override
	public void process(InputFile types) throws Exception {  //il parametro ricevuto è uno dei tanti file _types.nt che contengono i type assertion
		HashMap<String, Integer> conceptCounts = buildConceptCountsFrom(concepts); //costrisce l'hashmap <concetto, occorrenza> con occorrenza=0 per tutti
		List<String> externalConcepts = new ArrayList<String>();   //per ogni lista avremo un astringa tipo: entity + "##" + external concept
		HashMap<String, HashSet<String>> minimalTypes = new HashMap<String, HashSet<String>>(); //è un'hashMap che mappa ogni valore con un insieme. Le chiavi saranno le entity mentre i valori sarà un set(credo insieme di minimal types)
		
		while(types.hasNextLine()){      //per ogni riga del file _type.nt che sarà una tripla con predicato type
			String line = types.nextLine();   //mi salvo la tripla corrente
			String[] resources = line.split("##");  //creo un array di risorse (s, p, o sono risorse)
			
			String entity = resources[0];
			String concept = resources[2];
			if(!concept.equals(OWL.Thing.getURI())){  //se il concetto non è Thing
				trackConcept(entity, concept, conceptCounts, externalConcepts);   
				trackMinimalType(entity, concept, minimalTypes);  //si valuta se concept è minialType di entity. Se si lo aggiunge ed evenutalmente elimina concetti che non sono più minimaltype. Tutto va nell'HashMap minimalTypes.tipo trackMinimalTypr("fido", "Cane", minimalType)
			}
		}
		
		String prefix = new Files().prefixOf(types);
		writeConceptCounts(conceptCounts, targetDirectory, prefix);    //si segna il conteggio di ogni concetto
		writeExternalConcepts(externalConcepts, targetDirectory, prefix);   //scrive su file gli externalConcepts
		writeMinimalTypes(minimalTypes, targetDirectory, prefix);    //scrive su file i tipi minimi 
	}

	//Se minimalTypes non ha registrato entity, lo inserisce. Dopodichè confronta i minimalTypes di entity con concept. Se esiste un arco da minimalType a concept allora concept non è un minimal tpe ->esco da metodo.
	//in tutti gli altri casi concept è minimal type(se  esiste un arco da concept a un minimalType oppure se entity no ha ancora minimal types). 
	private void trackMinimalType(String entity, String concept, HashMap<String, HashSet<String>> minimalTypes) {
		if(!minimalTypes.containsKey(entity)) minimalTypes.put(entity, new HashSet<String>());  //se l'entity non è stato ancora "registrata", allora aggiungi <entity, hashSet>
		for(String minimalType : new HashSet<String>(minimalTypes.get(entity))){//per ogni minimalType, dell'insieme di minimalTypes(valore) di entity(chiave)
			if(!graph.pathsBetween(minimalType, concept).isEmpty()){ //ad esempio se esiste un arco tra pet appatenente a{pet, amico} e cane (non è questo il caso)
				return; // esci del metodo  e non registri cane(concetto) perhcè non è un tipo minimo(poichè
			}
			if(!graph.pathsBetween(concept, minimalType).isEmpty()){ //es se esiste un path tra cane e mammifero
				minimalTypes.get(entity).remove(minimalType);   //rimuovi mammifero perhcè non è più tipo minimo
			}
		}
		minimalTypes.get(entity).add(concept);  
	}

	//Cerca il concetp nel conceptCounts, se lo trova aggiorna il contatore, se non c'è l'aggiungo agli externalConcepts
	private void trackConcept(String entity, String concept, HashMap<String, Integer> counts, List<String> externalConcepts) {
		if(counts.containsKey(concept))	{               //se il concetto si trova in counts(che è l'hashmap di conteggio dei concetti)
			counts.put(concept, counts.get(concept) + 1);  //lo conti
		}else{
			externalConcepts.add(entity + "##" + concept);  //altrimenti inserisco concept negli externalConcepts
		}
	}
	
	private void writeExternalConcepts(List<String> externalConcepts, File directory, String prefix) throws Exception {
		BulkTextOutput externalConceptFile = connectorTo(directory, prefix, "uknHierConcept");
		for(String line : externalConcepts){
			externalConceptFile.writeLine(line);
		}
		externalConceptFile.close();		
	}
	
	//Scrive per ogni concetto il conteggio
	private void writeConceptCounts(HashMap<String, Integer> conceptCounts, File directory, String prefix) throws Exception {
		BulkTextOutput countConceptFile = connectorTo(directory, prefix, "countConcepts");
		for(Entry<String, Integer> concept : conceptCounts.entrySet()){
			countConceptFile.writeLine(concept.getKey() + "##" + concept.getValue());   
		}
		countConceptFile.close();
	}

	private void writeMinimalTypes(HashMap<String, HashSet<String>> minimalTypes, File directory, String prefix) throws Exception {
		BulkTextOutput connector = connectorTo(directory, prefix, "minType");
		for(Entry<String, HashSet<String>> entityTypes : minimalTypes.entrySet()){
			ArrayList<String> types = new ArrayList<String>(entityTypes.getValue());
			Collections.sort(types);
			connector.writeLine(types.size() + "##" + entityTypes.getKey() + "##" + StringUtils.join(types, "#-#"));
		}
		connector.close();
	}
	
	//Questo metodo usa concepts per costruire un a HashMap che contiene i concetti come chiave e come valore la loro freq(per ora sono tutti 0)
	private HashMap<String, Integer> buildConceptCountsFrom(Concepts concepts) throws Exception {
		HashMap<String, Integer> conceptCounts = new HashMap<String, Integer>();
		for(String concept : concepts.getConcepts().keySet()){  //per ogni concetto dell'insieme dei concetti
			conceptCounts.put(concept, 0);
		}
		return conceptCounts;
	}

	private BulkTextOutput connectorTo(File directory, String prefix, String name) {
		return new BulkTextOutput(new FileSystemConnector(new File(directory, prefix + "_" + name + ".txt")), 1000);
	}

	private Concepts extractConcepts(OntModel ontology) {
		PropertyExtractor pExtract = new PropertyExtractor();
		pExtract.setProperty(ontology);
		//estraggo properties
		Properties properties = new Properties();
		properties.setProperty(pExtract.getProperty());
		properties.setExtractedProperty(pExtract.getExtractedProperty());
		properties.setCounter(pExtract.getCounter());
		//estraggo concetti
		ConceptExtractor cExtract = new ConceptExtractor();
		cExtract.setConcepts(ontology);
		
		Concepts concepts = new Concepts();   
		concepts.setConcepts(cExtract.getConcepts());
		concepts.setExtractedConcepts(cExtract.getExtractedConcepts());
		concepts.setObtainedBy(cExtract.getObtainedBy());
		//estraggo relazioni di subClassOf
		OntologySubclassOfExtractor extractor = new OntologySubclassOfExtractor();
		extractor.setConceptsSubclassOf(concepts, ontology);
		
		this.subclassRelations = new ArrayList<String>();   //è un arraylist di stringhe del tipo_ concetto##supertipoDiConcetto
		for(List<OntClass> subClasses : extractor.getConceptsSubclassOf().getConceptsSubclassOf()){
			this.subclassRelations.add(subClasses.get(0) + "##" + subClasses.get(1));
		}
		
		OntologyDomainRangeExtractor DRExtractor = new OntologyDomainRangeExtractor();
		DRExtractor.setConceptsDomainRange(concepts, properties); //estae info su  domain e range di ogni property(e per ogni subproperty e property inversa)
		return concepts;
	}
}

