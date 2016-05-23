package it.unimib.disco.summarization.export;

import it.unimib.disco.summarization.dataset.FileDataSupport;
import it.unimib.disco.summarization.ontology.ConceptExtractor;
import it.unimib.disco.summarization.ontology.Concepts;
import it.unimib.disco.summarization.ontology.EqConceptExtractor;
import it.unimib.disco.summarization.ontology.EquivalentConcepts;
import it.unimib.disco.summarization.ontology.Model;
import it.unimib.disco.summarization.ontology.OntologySubclassOfExtractor;
import it.unimib.disco.summarization.ontology.Properties;
import it.unimib.disco.summarization.ontology.PropertyExtractor;
import it.unimib.disco.summarization.ontology.SubClassOf;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.ontology.OntModel;


public class ProcessOntology {

	public static void main(String[] args) throws Exception {
		Events.summarization();
		
		
		String owlBaseFileArg = null;
		String datasetSupportFileDirectory = null;
		
		owlBaseFileArg=args[0];
		datasetSupportFileDirectory=args[1];

		File folder = new File(owlBaseFileArg);  //= directory ../data/datasets/system-test/ontology
		Collection<File> listOfFiles = FileUtils.listFiles(folder, new String[]{"owl"}, false);   //è la collezione di files di folder con estensione .owl
		String fileName = listOfFiles.iterator().next().getName();  //fileName = nome del file dell'ontologia
		
		String owlBaseFile = "file://" + owlBaseFileArg + "/" + fileName; //l'absolute path dell'ontologia

		//Model
		//ontologyModel è' il modello dell'ontologia creato a partire di owlBaseFile con condifica RDF/XML
		OntModel ontologyModel = new Model(owlBaseFile,"RDF/XML").getOntologyModel();  
		
		//Extract Property from Ontology Model
		PropertyExtractor pExtract = new PropertyExtractor();   //pExtract è un oggetto che conterrà info sulle properties dell'ontologia oronizzate in ExtractedProperty, Property e a Counter
		pExtract.setProperty(ontologyModel); //avvia e completa l'estrazione delle propperties, inoltre ora ontologyModel sa quali sono le sue properties
		
		Properties properties = new Properties();      //tutti dati di pExtract vengono copiate in properties
		properties.setProperty(pExtract.getProperty());
		properties.setExtractedProperty(pExtract.getExtractedProperty());
		properties.setCounter(pExtract.getCounter());
		
		//Extract Concept from Ontology Model
		ConceptExtractor cExtract = new ConceptExtractor();
		cExtract.setConcepts(ontologyModel);//avvia e completa l'estrazione dei concetti, inoltre ora ontologyModel sa quali sono i suoi concetti
		
		Concepts concepts = new Concepts();       //tutti dati di cExtract vengono copiate in concepts
		concepts.setConcepts(cExtract.getConcepts());
		concepts.setExtractedConcepts(cExtract.getExtractedConcepts());
		concepts.setObtainedBy(cExtract.getObtainedBy());
		
		//Extract SubClassOf Relation from OntologyModel
		OntologySubclassOfExtractor SbExtractor = new OntologySubclassOfExtractor();
		//The Set of Concepts will be Updated if Superclasses are not in It
		SbExtractor.setConceptsSubclassOf(concepts, ontologyModel);
		SubClassOf SubClassOfRelation = SbExtractor.getConceptsSubclassOf();   //SubClassOfRelation ha tutte le info sulle relazioni di subClassOf di tutti i concetti  trovati
		
		//Extract EquivalentClass from Ontology Model - Qui per considerare tutti i concetti
		EqConceptExtractor equConcepts = new EqConceptExtractor();
		equConcepts.setEquConcept(concepts, ontologyModel); //estrae i concetti equivalenti dei concetti che ho
		
		EquivalentConcepts equConcept = new EquivalentConcepts();   //salva le info in eqConcept
		equConcept.setExtractedEquConcept(equConcepts.getExtractedEquConcept());
		equConcept.setEquConcept(equConcepts.getEquConcept());
		
		concepts.deleteThing();   // Pulisco i concetti da eventuali null e Thing
		SubClassOfRelation.deleteThing();
		
        FileDataSupport writeFileSupp = new FileDataSupport(SubClassOfRelation, datasetSupportFileDirectory + "SubclassOf.txt", datasetSupportFileDirectory + "Concepts.txt");
        
        writeFileSupp.writeSubclass(equConcept);
        writeFileSupp.writeConcept(concepts);
	}

}
