package it.unimib.disco.summarization.dataset;

import java.io.File;
import java.util.Vector;

public class OverallDatatypeRelationsCounting implements Processing{
	
	private Vector<NTripleAnalysis> datatypesCount;
	private Vector<NTripleAnalysis> propertiesCount;
	private Vector<NTripleAnalysis> akpCounts;
	private File datatypeCountsResults;
	private File propertyFile;
	private File akps;
	private File minimalTypes;
	
	public OverallDatatypeRelationsCounting(File datatypeFile, File propertyFile, File akps, File minimalTypes) {
		this.datatypesCount = new Vector<NTripleAnalysis>();
		this.propertiesCount = new Vector<NTripleAnalysis>();
		this.akpCounts = new Vector<NTripleAnalysis>();
		this.datatypeCountsResults = datatypeFile;
		this.propertyFile = propertyFile;
		this.akps = akps;
		this.minimalTypes = minimalTypes;
	}
	
	
	@Override
	public void process(InputFile file) throws Exception {   //riceve file ".._dt_properties.nt"
		DatatypeCount analysis = new DatatypeCount();
		PropertyCount propertyCount = new PropertyCount();
		
		String prefix = new Files().prefixOf(file);  //tipo se passo x_dt_properties.nt returns "x"
		File minimalTypesFile = new File(minimalTypes, prefix + "_minType.txt");  //è un file tipo"x_minType.txt"
		AKPDatatypeCount akpCount = new AKPDatatypeCount(new TextInput(new FileSystemConnector(minimalTypesFile))); //akpCount  è un oggetto che contiene un hashmap che mappa le entity nel file con i suoi minimal types
		
		new NTripleFile(analysis, propertyCount, akpCount).process(file); //file, costituito da triple viene analizzato da 3 diversi analyzers e le info prodotte salvate in ogni analyzer.
		
		//Copio i risultati delle analisi in questo oggetto
		datatypesCount.add(analysis);
		propertiesCount.add(propertyCount);
		akpCounts.add(akpCount);
	}
	
	public void endProcessing() throws Exception {
		new AggregatedCount(datatypesCount).writeTo(datatypeCountsResults); //scrive il conteggio dei datatypes in datatypeCountsResults
		new AggregatedCount(propertiesCount).writeTo(propertyFile);  //scriveil conteggio delle properties in propertiesCount
		new AggregatedCount(akpCounts).writeTo(akps); //scrive conteggio AKPs in akpCounts
	}
}