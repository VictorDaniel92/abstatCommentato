package it.unimib.disco.summarization.dataset;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class NTriple{
	
	private Statement nodes;

	public NTriple(Statement triple){
		this.nodes = triple;
	}
	
	public RDFNode subject(){
		return nodes.getSubject();
	}
	
	public RDFNode property(){
		return nodes.getPredicate();
	}
	
	public RDFNode object(){
		return nodes.getObject();
	}
	
	public String dataType() {
		String datatype = this.object().asLiteral().getDatatypeURI();
		if(datatype == null) datatype = RDFS.Literal.getURI();  //se l'oggetto della tripla è tipo una stringa(tipo in <../resorce/Barack_Obama><foaf/0.1/name> "Barack Obama")
		//allora this.object().asLiteral().getDatatypeURI() torna l'URI dlla costante Literal. Se invece è un tipo un numero intero , allora la variabile datatype conterrà l'URI 
		//del datatype (es:http://www.w3.org/2001/XMLSchema#float)
		
		return datatype;
	}
}