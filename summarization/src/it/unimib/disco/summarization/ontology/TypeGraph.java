package it.unimib.disco.summarization.ontology;

import java.util.ArrayList;
import java.util.List;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class TypeGraph{
	
	private DirectedAcyclicGraph<String, DefaultEdge> graph;

	public TypeGraph(Concepts concepts, List<String> subClasses) throws Exception{ //riceve concepts e una lista di stringe del tipo concept##superConcept
		this.graph = subTypeGraphFrom(concepts, subClasses);  //ritorna un grafo di concetti e archi tra concetti e superconcetti(typeGraph)
	}
	
	public List<List<String>> pathsBetween(String leaf, String root){
		ArrayList<List<String>> paths = new ArrayList<List<String>>();
		if(graph.containsVertex(leaf) && graph.containsVertex(root)){
			inOrderTraversal(leaf, root, new ArrayList<String>(), paths);
		}
		return paths;
	}
	
	private void inOrderTraversal(String leaf, String root, List<String> currentPath, List<List<String>> paths){
		ArrayList<String> path = new ArrayList<String>(currentPath);
		path.add(leaf);
		if(leaf.equals(root)){
			paths.add(path);
		}
		for(DefaultEdge edgeToSuperType : graph.outgoingEdgesOf(leaf)){
			String superType = graph.getEdgeTarget(edgeToSuperType);
			inOrderTraversal(superType, root, path, paths);
		}
	}
	
	//crea vertici per ogni concetto e archi orientati tra concetti e superconcetti
	private DirectedAcyclicGraph<String, DefaultEdge> subTypeGraphFrom(Concepts concepts, List<String> subclassRelations) throws Exception {
		DirectedAcyclicGraph<String, DefaultEdge> typeGraph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);
		
		for(String concept : concepts.getConcepts().keySet()){  //per ogni concetto di concepts, aggiungi un vertice
			typeGraph.addVertex(concept);
		}
		
		for(String line : subclassRelations){
			String[] relation = line.split("##");
			String subtype = relation[0];
			String supertype = relation[1];
			
			if(!typeGraph.containsVertex(subtype)) typeGraph.addVertex(subtype); //se typeGraph non contiene subtype come vertice aggiungilo
			if(!typeGraph.containsVertex(supertype)) typeGraph.addVertex(supertype);//   '                   supertype   ''
			
			typeGraph.addEdge(subtype, supertype);  //aggiungi un arco tra i vertici di subtype  e supertype
		}
		
		return typeGraph;
	}
}