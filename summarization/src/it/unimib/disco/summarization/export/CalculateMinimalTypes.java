package it.unimib.disco.summarization.export;

import it.unimib.disco.summarization.dataset.MinimalTypesCalculation;
import it.unimib.disco.summarization.dataset.ParallelProcessing;
import it.unimib.disco.summarization.ontology.Model;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.ontology.OntModel;

public class CalculateMinimalTypes {

	public static void main(String[] args) throws Exception {
		
		Events.summarization();
		
		File folder = new File(args[0]);   //folder rappresenta la cartella che contiene lontologia
		Collection<File> listOfFiles = FileUtils.listFiles(folder, new String[]{"owl"}, false); //listFiles() trova tutti i file in folder che hanno l'estensione .owl
		File ontology = listOfFiles.iterator().next();
		File typesDirectory = new File(args[1]);   //cartella che contiene i type e relation assertions del dataset. Praticamente l'output degli script awk
		File targetDirectory = new File(args[2]);  //cartella che conterrà i tipi minimi (CREDO)
		
		OntModel ontologyModel = new Model(ontology.getAbsolutePath(),"RDF/XML").getOntologyModel();  //rappresenta l'ontologia
		
		MinimalTypesCalculation minimalTypes = new MinimalTypesCalculation(ontologyModel, targetDirectory);  //ora minimalTypes rappresenta il type graph, inoltre viene calcolato il dominio e codominio di ogni property, ma questo non so dove veine salvato nell'oggetto
		
		new ParallelProcessing(typesDirectory, "_types.nt").process(minimalTypes);  //Scrive su abstat/data/summaries/system-test/min-types/min-type-results i file del conteggio di ogni concetto, dei concetti esterni, dei minimal types di ogni concetto. Notare che MinimalTypesCalcutaion implementa Processing
		
	}
}
