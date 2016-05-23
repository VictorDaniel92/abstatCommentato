package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.HashMap;

import com.hp.hpl.jena.ontology.OntResource;

/**
 * Provides datatype for store DomainRange infos
 */
public class DomainRange {
	
	private HashMap<String, ArrayList<OntResource>> DRRelation = new HashMap<String, ArrayList<OntResource>>(); //ogni property viene mappato con una lista tipo: lista[0]=domain, lista[1]=range
	private HashMap<String,String> ObtainedBy = new HashMap<String,String>();
	private HashMap<String,String> PropertyType = new HashMap<String,String>(); //associa a ogni property il suo type

	public HashMap<String, ArrayList<OntResource>> getDRRelation() {
		return DRRelation;
	}

	public void setDRRelation(HashMap<String, ArrayList<OntResource>> dRRelation) {
		DRRelation = dRRelation;
	}
	
	public HashMap<String,String> getObtainedBy() {
		return ObtainedBy;
	}

	public void setNewObtainedBy(String URIProperty, String obtainedBy) {
		getObtainedBy().put(URIProperty, obtainedBy);
	}

	public HashMap<String,String> getPropertyType() {
		return PropertyType;
	}

	public void setPropertyType(String URIProperty, String propertyType) {
		getPropertyType().put(URIProperty, propertyType);
	}
}
