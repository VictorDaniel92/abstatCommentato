package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Extract domain and range relation from an ontology
 * 
 * @author Vincenzo Ferme
 */
public class OntologyDomainRangeExtractor {

	private DomainRange propertyDomainRange = new DomainRange();

	//Questo metodo salva info su domain e range di ogni properties, inoltre per ogni property cerca subproperties e properties 
	//inversa e setta il domain e range di cosneguenza
	public void setConceptsDomainRange(Concepts concepts, Properties properties) { 
		
		if(properties.getExtractedProperty().isEmpty()) return;  //se non ci sono properties esco
		//Used for Dynamic Computation of Domain And Range
		HashMap<String, ArrayList<OntResource>> DRProperty = new HashMap<String, ArrayList<OntResource>>(); //ogni property viene mappato con una lista tipo: lista[0]=domain, lista[1]=range
		
		List<OntResource> conceptsToAdd = new ArrayList<OntResource>();
		
		//Last size of DRProperty Set 
		int lastSize = 0;

		//TODO: Propago le informazioni finch� non crescono pi� (vedere se vale il punto fisso) 
		// - Forse si pu� ottimizzare togliendo dall'insieme delle propriet� quelle per cui ad un passo non si trova nulla

		while(lastSize==0 || DRProperty.size()>lastSize){ //Finch� nuovi elementi vengono aggiunti

			Iterator<OntProperty> itP = properties.getExtractedProperty().iterator();  //iteratore sulle properties

			lastSize = DRProperty.size();

			while(itP.hasNext()) {        //Itera su ogni property
				OntProperty property = itP.next();
				String URIP = property.getURI();  //URI della property corrente
				
				//Inizializzo Domain e Range
				OntResource domain = null;
				OntResource range = null;
				
				////Se  ho gi� salvato dominio e range da precedenti propagazioni
				if( DRProperty.get(property.getURI())!=null ) //cerca tra le key se già esiste questa property. True. Allora estraggo domain e range
				{
					ArrayList<OntResource> data = DRProperty.get(property.getURI());
					domain = data.get(0);
					range = data.get(1);
				}
				else //se non esiste una key per queela property
				{
					domain = property.getDomain();
					range = property.getRange();
				}

				//Se la proprieta' e' --, il suo URI e quello di Domain e Range sono diversi da null e sia Dominio che Range sono --
				if( URIP!=null && property.getDomain()!=null && property.getRange()!=null && domain.isClass()){ 
					//TODO: && cls1.getNameSpace().compareTo(nameSpace)==0 && cls1.getDomain().getNameSpace().compareTo(nameSpace)==0 && cls1.getRange().getNameSpace().compareTo(nameSpace)==0

					//Se non ho gia salvato dominio e range da precedenti propagazioni
					if( DRProperty.get(property.getURI())==null ){

						//Salvo le informazioni in DRPoperty e in propertyDomainRange(tramite setNewObtainedBy(), setPropertyType())
						ArrayList<OntResource> data = new ArrayList<OntResource>();
						data.add(domain);
						data.add(range);
						DRProperty.put(property.getURI(), data);
						propertyDomainRange.setNewObtainedBy(property.getURI(), "DomainRange: " + property.getLocalName());  //salva l'URI della property e una stringa in PropertyDomainRange
						
						//Salvo il tipo della propriet�
						propertyDomainRange.setPropertyType(property.getURI(), property.getRDFType(true).getLocalName());
						
						//If Domain is a New Concept save It
						if(!domain.isAnon() && concepts.getConcepts().get(domain.getURI()) == null)  {
							concepts.getConcepts().put(domain.getURI(),domain.getLocalName());
							concepts.setNewObtainedBy(domain.getURI(), "Domain - " + property.getLocalName() + " (" + property.getRDFType(true).getLocalName() + ")");
							conceptsToAdd.add(domain);
							
						}			
						//Count Presence of Class as Domain
						concepts.updateCounter(domain.getURI(), "Domain");
						
						//If Range is a New Concept save It
						if(!range.isAnon() && range.isClass() && concepts.getConcepts().get(range.getURI()) == null)  {
							concepts.getConcepts().put(range.getURI(),range.getLocalName());
							concepts.setNewObtainedBy(range.getURI(), "Range - " + property.getLocalName() + " (" + property.getRDFType(true).getLocalName() + ")");
							conceptsToAdd.add(range);
						}
							//Count Presence of Class as Range
						if (range.isClass()) concepts.updateCounter(range.getURI(), "Range");
						
					}
				}
				
				//Se ho Dominio e Range, Propago l'informazione alle sottopropriet� e alle propriet� inverse
				if( domain!=null && range!=null ){
					//Ottengo le Sottopropriet�
					ExtendedIterator<? extends OntProperty> itSP = property.listSubProperties();

					while(itSP.hasNext()) {
						OntProperty cls11 = itSP.next();
						String URI11 = cls11.getURI();
						
						//Se la sottopropriet� ha un URI, � un -- e sia Dominio che Range sono --
						if( URI11!=null && domain.isClass() ){ //TODO: && cls1.getNameSpace().compareTo(nameSpace)==0 && cls1.getDomain().getNameSpace().compareTo(nameSpace)==0 && cls1.getRange().getNameSpace().compareTo(nameSpace)==0

							//Se non ho gi� salvato dominio e range della sottoproperty da precedenti propagazioni
							if( DRProperty.get(cls11.getURI())==null ){
								
								//Salvo le informazioni: domain e range di una subProperty sono gli stessi della superProperty
								ArrayList<OntResource> data = new ArrayList<OntResource>();
								data.add(domain);  
								data.add(range);
								DRProperty.put(cls11.getURI(), data);
								propertyDomainRange.setNewObtainedBy(cls11.getURI(), "DomainRange: " + cls11.getLocalName() + " - SubPropertyOf: "+ property.getLocalName());
								
								//Salvo il tipo della propriet�
								propertyDomainRange.setPropertyType(cls11.getURI(), cls11.getRDFType(true).getLocalName());
								
								//Count Presence of Class as Domain
								concepts.updateCounter(domain.getURI(), "Domain - Sub");
								//Count Presence of Class as Range
								if(range.isClass())
									concepts.updateCounter(range.getURI(), "Range - Sub");
							}
							
						}
					}

					//Ottengo le Propriet� Inverse
					ExtendedIterator<? extends OntProperty> itSP1 = property.listInverse();

					while(itSP1.hasNext()) {
						OntProperty cls11 = itSP1.next();
						String URI11 = cls11.getURI();
						
						//Se la propriet� inversa ha un URI, � un -- e sia Dominio che Range sono --
						if( URI11!=null && domain.isClass() ){ //TODO: && cls1.getNameSpace().compareTo(nameSpace)==0 && cls1.getDomain().getNameSpace().compareTo(nameSpace)==0 && cls1.getRange().getNameSpace().compareTo(nameSpace)==0

							//Se non ho gi� salvato dominio e range da precedenti propagazioni
							if( DRProperty.get(cls11.getURI())==null ){

								//Salvo le informazioni: Una property inversa avrà domain e range invertiti di property
								ArrayList<OntResource> data = new ArrayList<OntResource>();
								data.add(range);
								data.add(domain);
								DRProperty.put(cls11.getURI(), data);
								propertyDomainRange.setNewObtainedBy(cls11.getURI(), "DomainRange: " + cls11.getLocalName() + " - InvPropertyOf: "+ property.getLocalName());
								
								//Salvo il tipo della propriet�
								propertyDomainRange.setPropertyType(cls11.getURI(), cls11.getRDFType(true).getLocalName());
								
								//Count Presence of Class as Domain
								if(range.isClass())
									concepts.updateCounter(range.getURI(), "Domain - Inv");
								//Count Presence of Class as Range
								concepts.updateCounter(domain.getURI(), "Range - Inv");
							}
							
						}
					}
				}
			}
		}
		concepts.getExtractedConcepts().addAll(conceptsToAdd);  //aggiungo la lista di nuovi concetti ala lista di tutti i concetti di concepts
		propertyDomainRange.setDRRelation(DRProperty);   //savo domain e range di ogni property in propertyDomainRange
	}

	public DomainRange getPropertyDomainRange() {
		return propertyDomainRange;
	}
}
