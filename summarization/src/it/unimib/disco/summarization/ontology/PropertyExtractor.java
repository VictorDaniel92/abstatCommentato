package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * ConceptExtractor: Extract Property Defined inside Ontology
 */
public class PropertyExtractor {
	
	private HashMap<String,String> Property = new HashMap<String,String>();   //mappa l'URI di una property col suo nome
	private List<OntProperty> ExtractedProperty = new ArrayList<OntProperty>();  //è una lista delle proprierties dell'ontologia
	private HashMap<String,HashMap<String,Integer>> Counter = new HashMap<String,HashMap<String,Integer>>(); //Property -> Context(Relation Type), Total


	public PropertyExtractor setProperty(OntModel ontologyModel) {
		
		enrichWithImplicitPropertyDeclarations(ontologyModel);
		ExtendedIterator<OntProperty> TempExtractedPropery = ontologyModel.listAllOntProperties(); //= iteratore su tutte le properties nell'ontologia
		while(TempExtractedPropery.hasNext()) {    //per ogni property dell'ontologia chiama add()
			OntProperty property = TempExtractedPropery.next();
			add(property); //Aggiunge la property a ExtractedProperty, Property e a Counter
		}
		
		TempExtractedPropery.close();
		return this;
	}

	private void enrichWithImplicitPropertyDeclarations(OntModel ontologyModel) {
		StmtIterator domainStatements = ontologyModel.listStatements(new SimpleSelector(){ //domainStatements è un iteratore sull'insieme {tutti gli statement dell'ontologia che superano selects()}
			//quindi domainStatments itera solo sugli statements che hanno come predicato "domain" o "range", vale a dire gli statements che descrivono proprietà. Gli statement che descrivono Classi o altro verranno saltati
			@Override
			public boolean selects(Statement s) {   //ritorna true se lo statement ha come predicato "domain" o "range"
				Property predicate = s.getPredicate();
				return predicate.equals(RDFS.domain) || predicate.equals(RDFS.range);
			}
		});
		HashSet<String> implicitProperties = new HashSet<String>();//conterrà quelle che sono definite come "proprietà" nell'ontologia
		while(domainStatements.hasNext()){
			implicitProperties.add(domainStatements.next().getSubject().getURI()); //aggiunge l'URI del sogg. di uno statement che descrive una proprietà, quindi aggiunge implicitamente una proprietà. //Ricorda che l'ontologia descrive classi, proprietà,ecc, a noi per ora interessa sapere quali sono le proprietà descritte
		}
		domainStatements.close();
		for(String property : implicitProperties){       //credo che in questo modo, ontologyModel sa quali sono le proprietà
			ontologyModel.createOntProperty(property);  
		}
	}
    //Aggiunge la property a ExtractedProperty, Property e a Counter
	private void add(OntProperty property) {
		String URI = property.getURI();

		if( URI!=null ){
			ExtractedProperty.add(property);
			Property.put(URI,property.getLocalName());
			//Count direct presence
			HashMap<String,Integer> count = new HashMap<String,Integer>();
			count.put("Direct",1);
			getCounter().put(URI,count);
		}
	}
	
	public List<OntProperty> getExtractedProperty() {
		return ExtractedProperty;
	}
	
	public HashMap<String, String> getProperty() {
		return Property;
	}
	public void updateCounter(String URI, String Context){
		//Se la proprietà non � presente l'aggiungo
		if(Counter.get(URI)==null){
			//Count direct presence
			HashMap<String,Integer> count = new HashMap<String,Integer>();
			count.put(Context, new Integer(1));
			Counter.put(URI, count);
		}
		//Se il contesto non � presente l'aggiungo 
		else if(Counter.get(URI).get(Context)==null){
			Counter.get(URI).put(Context, new Integer(1));
		}
		//Se sia proprietà che contesto sono presenti aggiorno il contatore
		else{
			Counter.get(URI).put(Context, new Integer(Integer.valueOf(Counter.get(URI).get(Context)) + 1));
		}
	}
	public HashMap<String,HashMap<String,Integer>> getCounter() {
		return Counter;
	}

	public void setCounter(HashMap<String,HashMap<String,Integer>> counter) {
		Counter = counter;
	}
	
}
