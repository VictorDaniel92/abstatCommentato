package it.unimib.disco.summarization.dataset;

import java.io.File;
import java.util.Vector;

public class OverallObjectRelationsCounting implements Processing{

	private Vector<NTripleAnalysis> propertiesCount;
	private Vector<NTripleAnalysis> akpCounts;
	private File propertyFile;
	private File akps;
	private MinimalTypes minimalTypesOracle;
	
	public OverallObjectRelationsCounting(File propertyFile, File akps, File types) throws Exception {
		this.propertiesCount = new Vector<NTripleAnalysis>();
		this.akpCounts = new Vector<NTripleAnalysis>();
		this.propertyFile = propertyFile;
		this.akps = akps;
		this.minimalTypesOracle = new AllMinimalTypes(types); //contiene un HashMap types che mappa il prefisso di ogni file con un oggetto che trasforma le righe del file _minType.txt nel contenuto dell'HashMaps. Contiene anche others, un oggetto per il file con prefisso "others"
	}
	
	@Override
	public void process(InputFile file) throws Exception {  //riceve file ".._obj_properties.nt"
		PropertyCount propertyCount = new PropertyCount();
		AKPObjectCount akpCount = new AKPObjectCount(minimalTypesOracle);
		
		new NTripleFile(propertyCount, akpCount).process(file); //file, costituito da triple viene analizzato da 2 diversi analyzers e le info prodotte salvate in ogni analyzer.
		
		//Copio i risultati delle analisi in questo oggetto
		propertiesCount.add(propertyCount);
		akpCounts.add(akpCount);
	}
	
	public void endProcessing() throws Exception {
		new AggregatedCount(propertiesCount).writeTo(propertyFile);
		new AggregatedCount(akpCounts).writeTo(akps);
	}
}
