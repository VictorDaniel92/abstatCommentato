package it.unimib.disco.summarization.dataset;

import it.unimib.disco.summarization.export.Events;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelProcessing{
	
	private File sourceDirectory;
	private String suffix;

	public ParallelProcessing(File directory, String suffix) { //riceve cartella che contiene type relational assertions  e il suffix "_types.nt"
		this.sourceDirectory = directory;
		this.suffix = suffix;
	}
	
	public void process(final Processing processing) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for(final File file : new Files().get(sourceDirectory, suffix)){  //per ogni file con suffisso sufix (es: "_types.nt", "_dt_properties.nt", "_obj_properties.nt")
			executor.execute(new Runnable() {   //esegui con un pool di 10 threads
				@Override
				public void run() {
					try {
						processing.process(new TextInput(new FileSystemConnector(file)));  //a ogni iterazione, processing si arrichirà di info(conteggio datatypes, properties e AKPs) sulle triple del file corrente
					} catch (Exception e) {
						Events.summarization().error(file, e);
					}
				}
			});
		}
		executor.shutdown();
	    while(!executor.isTerminated()){}
	}
}