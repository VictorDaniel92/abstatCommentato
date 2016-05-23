package it.unimib.disco.summarization.web;

import java.util.HashMap;

public class Routing{
	
	private HashMap<String, Response> mappings;
	
	public Routing(){
		this.mappings = new HashMap<String, Response>();
	}		
	
	public Routing mapFile(String route, String file){
		map(route, new FileResponse(file));
		return this;
	}
	
	public Routing mapText(String route, String message){
		map(route, new TextualResponse(message));
		return this;
	}
	
	public Routing mapJson(String route, Api api) {
		map(route, new JsonResponse(api));
		return this;
	}
	
	public Response routeTo(String path){  //path è il target della request
		Response response = mappings.get(path);  //cerca il target tra le rotte e prende il response
		if(response == null) response = new NotFound();
		return response;
	}

	private void map(String route, Response response) {
		mappings.put(route, response);
	}
}