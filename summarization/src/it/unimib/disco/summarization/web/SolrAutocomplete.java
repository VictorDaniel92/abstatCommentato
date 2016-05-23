package it.unimib.disco.summarization.web;

import java.io.InputStream;

public class SolrAutocomplete implements Api{
	
	private Connector connector;
	private String suggestionService;

	public SolrAutocomplete(Connector connector, String service) {
		this.connector = connector;
		this.suggestionService = service;
	}

	@Override
	public InputStream get(RequestParameters request) throws Exception {  //request è anche un oggetto HttpParameters
		QueryString queryString = new QueryString()    //è un oggetto che conterrà i parametri della richiesta HTTP
									.addParameter("q", "URI_ngram", request.get("q"))  //aggiunge il parametro 1
									.addParameter("fq", "dataset", request.get("dataset"));   //aggiunge il parametro 2
		
		return connector.query("/solr/indexing/" + suggestionService, queryString);
	}
}
