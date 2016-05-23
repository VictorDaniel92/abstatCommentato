package it.unimib.disco.summarization.ontology;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Model {
	
	OntModel ontologyModel;
	
	public Model(String OwlBaseFile, String FileType){
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null); //ontologyModel ora  è un modello di ontologia(ancora  vuoto) d'accoordo con la specifica
		ontologyModel.read(OwlBaseFile, FileType);   //riempie ontologyModel con  gli statement dall'input OwlBaseFile(file ontologia) di tipo FileType(RDF/XML)
	}
	
	public OntModel getOntologyModel(){   //ritorna il modello di ontologia
		
		return ontologyModel;
	}
}
