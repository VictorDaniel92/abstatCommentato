package it.unimib.disco.summarization.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class QueryString {

	private ArrayList<String> parameters;

	public QueryString() {
		this.parameters = new ArrayList<String>();
	}
	
	public QueryString addParameter(String queryParameter, String solrParameter, String value) throws UnsupportedEncodingException {
		parameters.add(queryParameter + "=" + solrParameter + ":" + encode(value));
		return this;
	}

	private String encode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}
	//trasforma i parametri in una stringa
	public String build() {
		return "?" + StringUtils.join(parameters, "&");  //mette in un'unica stringa i parametri dell'arrayList parameters preceduto da "?"
	}

}
