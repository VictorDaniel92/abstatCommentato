package it.unimib.disco.summarization.dataset;

import it.unimib.disco.summarization.export.Events;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class NTripleFile {

	private NTripleAnalysis[] analyzers;

	public NTripleFile(NTripleAnalysis... analyzers) {
		this.analyzers = analyzers;
	}

	public void process(InputFile file) throws Exception {//riceve un file con triple nella forma: sogg#pred##ogg
		while(file.hasNextLine()){         //per ogni tripla di file
			String line = file.nextLine();
			try{
				String[] splitted = line.split("##");
				String subject = splitted[0];
				String property = splitted[1];
				String object = splitted[2];
				String datatype = "";
				
				if(splitted.length > 3){
					datatype = "^^<" + splitted[3] + ">";
				}
	
				if(!object.startsWith("\"")){
					object = "<" + object + ">";
				}
				
				line = "<" + subject + "> <" + property + "> " + object + datatype + " .";    //ricostruisce la tripla in formato N-triples
				
				Model model = ModelFactory.createDefaultModel();
				model.read(IOUtils.toInputStream(line) ,null, "N-TRIPLES");  //agguinge rdf statements(in questo caso la tripla line) in formato N-triplrs al model
				Statement statement = model.listStatements().next();  //ottengo l'oggetto statement con la tripla corrente
				
				
				NTriple triple = new NTriple(statement);
				for(NTripleAnalysis analysis : analyzers){  //per ogni oggetto di tipo NTripleAnalysis traccia la tripla. //NtripleAnalysis è un'interfaccia implementata da DatatypeCount, PropertyCount e AKPDatatypeCount
					analysis.track(triple);
				}	
			}catch(Exception e){
				Events.summarization().error("error processing " + line + " from " + file.name(), e);
			}
		}
	}

}
