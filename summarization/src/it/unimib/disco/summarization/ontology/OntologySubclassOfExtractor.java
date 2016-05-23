package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * OntologySubclassOfExtractor: Extract SubClass of Concept and update the list of Concept
 */
//Itera su ogni concetto finora trovato. Se tale concetto è definito nella nostra ontologia, trova i supertipi(se trova dei concetti nuovi li aggiunge a Concepts)
// dopodichè salva le relazioni di subClassOf nell'oggetto ConceptsSubclassOf, aggiorno il Counter di Concepts.
//Se il concetto no è definito nell'ontologia facciamo una query per trovare le superclassi, dopodichè salviamo le relazioni di subClassOf. Se troviamo concetti nuovi
//li aggiungiamo a Concepts.
public class OntologySubclassOfExtractor {
	
	private SubClassOf ConceptsSubclassOf = new SubClassOf();
	
	public void setConceptsSubclassOf(Concepts Concepts, OntModel ontologyModel){  //riceve i concetti dell'ontologia e l'ontologia e l'ontologia stessa
		//Extract SubClassOf Relation
		Iterator<OntResource> IteratorExtractedConcepts = Concepts.getExtractedConcepts().iterator(); //iteratore sui concetti finora estratti
		
		List<OntResource> AddConcepts = new ArrayList<OntResource>();
		
		while(IteratorExtractedConcepts.hasNext()) {  //ciclo sui concetti dell'ontologia
			OntClass concept = (OntClass) IteratorExtractedConcepts.next();
			String URI = concept.getURI();
			
			if( URI!=null ){
				
				try{ //If Concept is Defined inside Ontology
					
					//Get List Of All Directed SuperClasses
					ExtendedIterator<OntClass> itSup = concept.listSuperClasses(true);  //iteratore su tutte le superclassi DIRETTE di concept
					
					while(itSup.hasNext()) {  //ciclio sui superconcetti del concetto corrente
						
						OntClass conceptSup = itSup.next();
						String URISUP = conceptSup.getURI();
						
						if( URISUP!=null ){
							
							//Save SubClassOf Relation (concept SubClassOf conceptSup)
							ConceptsSubclassOf.addSubClassOfRelation(concept, conceptSup);  //la relazione di sottoclasse viene registrata nell'oggetto ConceptsSubClassOff
							
							if(Concepts.getConcepts().get(URISUP) == null){ //If is a New Concept save It
								Concepts.getConcepts().put(URISUP,conceptSup.getLocalName());
								Concepts.setNewObtainedBy(URISUP, concept.getLocalName() + " - SubClassOf");
								AddConcepts.add(conceptSup);    //lista dei concetti aggiunti in questa fase che prima non erano stati trovati
							}
							
							//Count Presence of Class as SubClassOf (# Of Subclasses)
							Concepts.updateCounter(URISUP, "SubClassOf (# Of Subclasses)");
							//Count Presence of Class as SubClassOf (# Of Superclasses)
							Concepts.updateCounter(URI, "SubClassOf (# Of Superclasses)");
						}
						
					}
				}
				catch(ConversionException e){ //if Concept id Defined Outside Ontology
					
					//SPARQL Query for SubClasses
					//Questa query chiede di chi è sottoclasse concept (non lo sappiamo dato che qeusto concept non è definito nell'ontologia)
					String queryString = "PREFIX rdfs:<" + RDFS.getURI() + ">" +       
										 "PREFIX ont:<" + concept.getNameSpace() + ">" + 
										 "SELECT ?obj " +
										 "WHERE {" +
										 "      ont:" + concept.getLocalName() + " rdfs:subClassOf ?obj" +
										 "      }";
					
					//Execute Query
					Query query = QueryFactory.create(queryString) ;
					QueryExecution qexec = QueryExecutionFactory.create(query, ontologyModel) ;
					
					try {
					    
						ResultSet results = qexec.execSelect();   //result è un iteratore sulla risposta alla query
					    
					    //Temporary Model in Order to Construct Node for External Concept
					    OntModel ontologyTempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null); 
					    
					    //Extract SubCallOf Relation
					    for ( ; results.hasNext() ; )
					    {
					      QuerySolution soln = results.nextSolution() ;

					      Resource obj = soln.getResource("obj");
					      String URIObj = obj.getURI();
					      
					      //Get SubClassOf all Class different from the current one
						  if( URIObj!=null && concept.getURI()!=URIObj ){
							  
							  OntClass conceptSup = ontologyTempModel.createClass(URIObj);  //dico all'ontologia temporanea che le risorse nella risposta sono sue classi
							  
							  //Save SubClassOf Relation (concept SubClassOf conceptSub)
							  ConceptsSubclassOf.addSubClassOfRelation(concept, conceptSup);
								
							  if(Concepts.getConcepts().get(URIObj) == null)  {//If is a New Concept save It //se nell'output della query ci sono concetti nuovi li aggiungo al mio elenco di concetti//CREDO CHE QUESTI NON SIANO DA CONSIDERARE PARTE DELL'ONTOLOGIA
								  Concepts.getConcepts().put(URIObj,conceptSup.getLocalName());
								  Concepts.setNewObtainedBy(URIObj, concept.getLocalName() + " - SubClassOf");
								  AddConcepts.add(conceptSup);
							  }

							  //Count Presence of Class as SubClassOf (# Of Subclasses)
							  Concepts.updateCounter(URIObj, "SubClassOf (# Of Subclasses)");
							  //Count Presence of Class as SubClassOf (# Of Superclasses)
							  Concepts.updateCounter(URI, "SubClassOf (# Of Superclasses)");
						  }

					    }

					} finally { qexec.close() ; }
				}
				
			}
			
		}
		
		Concepts.getExtractedConcepts().addAll(AddConcepts);  //aggiunge alla lista ExtractedConcepts di Concepts, tutti i nuovi concetti "scoperti" in questa fase
	}
	
	public SubClassOf getConceptsSubclassOf(){
		return ConceptsSubclassOf;
	}

}
