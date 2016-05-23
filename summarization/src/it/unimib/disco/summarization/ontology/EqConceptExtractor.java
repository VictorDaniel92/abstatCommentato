package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * ConceptExtractor: Extract EquivalentProperty Defined inside Ontology
 */
public class EqConceptExtractor {
	
	private HashMap<OntResource,List<OntResource>> ExtractedEquConcept = new HashMap<OntResource,List<OntResource>>(); //credo mappi ogni concetto che ha concetti equivalenti con i suoi concetti equivalenti
	private ArrayList<String> equConcept = new ArrayList<String>();    //credo contenga tutti i concetti  equivalenti

	public void setEquConcept(Concepts AllConcepts, OntModel ontologyModel) {

		Iterator<OntResource> itC = AllConcepts.getExtractedConcepts().iterator();
		
		while(itC.hasNext()) {  //finchè ci sono concetti su cui iterare
			OntResource concept = itC.next();
			
			List<OntResource> equConc = new ArrayList<OntResource>();  //conterrà gli eventuali concetti equivalenti del concept corrente
			
			String queryString = "PREFIX owl:<" + OWL.getURI() + ">" + 
								 "SELECT ?obj " +
								 "WHERE {" +
								 "      <" + concept.getURI() + "> owl:equivalentClass ?obj" +
								 "      }";
			
			Query query = QueryFactory.create(queryString) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, ontologyModel) ;   //questa query trova per ogni concetto le classi equivalenti
			
			try {
			    
				ResultSet results = qexec.execSelect();      //insieme di risultati alal query
			    
			    //Temporary Model in Order to Construct Node for External Concept
			    OntModel ontologyTempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null); 
			    
			    //Extract EquConcept Relation
			    for ( ; results.hasNext() ; )   //per ogni risposta mi ricavo la risorsa associata  e poi l'URI. Successivamente registro i concetti equivalenti a concept in ExtractedEquConcept 
			    {
			      QuerySolution soln = results.nextSolution() ;

			      Resource obj = soln.getResource("obj");
			      String URIObj = obj.getURI();
			      
			      //Get EquClass all Concept different from the current one
				  if( URIObj!=null && concept.getURI()!=URIObj ){
					  
					  OntResource EquConc = ontologyTempModel.createOntResource(URIObj);
					  
					  //Save EquC
					  if( URIObj!=null ){
						equConc.add(EquConc);
						
						equConcept.add(EquConc.getURI());
						
						//Count Presence of Class as EquConcept
						AllConcepts.updateCounter(concept.getURI(), "Equivalent Class");
					}

					}
					
					ExtractedEquConcept.put(concept, equConc);  //per ogni concetto aggiunge i suoi concetti equivalenti
				  }


			} finally { qexec.close() ; }
			
		}

	}
	
	public  HashMap<OntResource, List<OntResource>> getExtractedEquConcept() {
		return ExtractedEquConcept;
	}

	public ArrayList<String> getEquConcept() {
		return equConcept;
	}

	public void setEquConcept(ArrayList<String> equConcept) {
		this.equConcept = equConcept;
	}
	
}
