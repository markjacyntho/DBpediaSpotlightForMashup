package br.edu.iff.dbpedia_spotlight.service;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 

public class Extractor 
{
	
	private static final String SERVER = "https://api.dbpedia-spotlight.org";
	private static final int SUPPORT = 0;
	private static final String DBPEDIA_SPARQL_ENDPOINT = "dbpedia.org/sparql";
			
	
	//Singleton
	private static Extractor soleInstance = null;
	public static Extractor soleInstance()
	{
		if (Extractor.soleInstance == null)
			Extractor.soleInstance = new Extractor();
		
		return Extractor.soleInstance;
	}
	
	private Extractor() {}
	
	private List<DBpediaResource> createResources(JSONArray entities, 
			                                      String language) 
			                                          throws ProcessingException
	{
		List<DBpediaResource> result = new ArrayList<DBpediaResource>();
		int total = entities.length();
		if (total == 0) return result; //nothing to do
		
		String resourceVar = "resource";
		String enSameAsVar = "enSameAs";
 		String enLabelVar = "en_label";
		String enCommentVar = "en_comment";
		String langLabelVar = "lang_label";
		String langCommentVar = "lang_comment";
		
		String queryString = ""
		+ " PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
		+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"		
		+ " SELECT  DISTINCT"
		+ " ?" + resourceVar 
		+ " ?" + enSameAsVar 
		+ " ?" + enLabelVar 
		+ " ?" + enCommentVar 
		+ " ?" + langLabelVar 
		+ " ?" + langCommentVar 
		+ " \n WHERE \n"
		+ " { \n"
		+ "  VALUES ?" + resourceVar +"\n"
		+ "  { \n";
		
		Set<String> visitedURIs = new HashSet<String>();
		for(int i = 0; i < total; i++) 
		{
			try 
			{
				JSONObject entity = entities.getJSONObject(i);
				String URI = entity.getString("@URI");
				URI = URI.trim();
				if (!visitedURIs.contains(URI)) //ignorar URIs duplicadas
				{	
					queryString += "     <" + URI + ">" + "\n";
					visitedURIs.add(URI);
				}
			} 
			catch (JSONException e) 
			{
				throw new RuntimeException("JSON exception: "+e);
            }
			
		}
		
		
		queryString += 
		  "  } \n"
        + "  OPTIONAL \n"
		+ "  { \n"
		+ "  	?" + resourceVar + " owl:sameAs ?" + enSameAsVar + " . \n"
		+ "     FILTER regex(str(?" + enSameAsVar + "), \"http://dbpedia.org/resource\", \"i\") . \n"
		+ "  } \n"	
		+ "  OPTIONAL \n"
		+ "  { \n"
		+ "  	?" + enSameAsVar + " owl:sameAs ?" + resourceVar + " .  \n"
		+ "     FILTER regex(str(?" + enSameAsVar + "), \"http://dbpedia.org/resource\", \"i\") . \n"
		+ "  } \n"
		+ "  OPTIONAL \n"
		+ "  { \n"
		+ "	    ?" + resourceVar + " rdfs:label ?"+enLabelVar+" . \n"
		+ "  	FILTER ( lang(?"+enLabelVar+") = \"en\") . \n"
		+ "  } \n"
		+ "  OPTIONAL \n"
		+ "  { \n"
		+ "     ?" + resourceVar + " rdfs:comment ?"+enCommentVar+" . \n"
		+ "     FILTER ( lang(?"+enCommentVar+" ) = \"en\") . \n"
		+ "  } \n"
		+ "  OPTIONAL \n"
		+ "  { \n"
		+ "	    ?" + resourceVar + " rdfs:label ?"+langLabelVar+" . \n"
		+ "  	FILTER ( lang(?"+langLabelVar+") = \"" + language + "\") . \n"
		+ "  } \n"
		+ "  OPTIONAL \n"
		+ "  { \n"
		+ "     ?" + resourceVar + " rdfs:comment ?"+langCommentVar+" . \n"
		+ "     FILTER ( lang(?"+langCommentVar+" ) = \"" + language + "\") . \n"
		+ "  } \n"
		+ " } \n"
		+ " ORDER BY ?"+langLabelVar+" ?"+enLabelVar+" \n";
		
		//System.out.println(queryString);
		
		String sparqlEndpoint;
		if (language.equals("en")) 
			sparqlEndpoint = "https://" + DBPEDIA_SPARQL_ENDPOINT;
		else if (language.equals("es"))
			sparqlEndpoint = "https://"+language+"."+ DBPEDIA_SPARQL_ENDPOINT;
		else
			sparqlEndpoint = "http://"+language+"."+ DBPEDIA_SPARQL_ENDPOINT;
		
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = 
				QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		
		try
		{
			ResultSet results = queryExecution.execSelect();
			while (results.hasNext())
			{
				QuerySolution solution = results.next();
				String URI = solution.getResource(resourceVar).getURI();
				Resource resource = solution.getResource(enSameAsVar);
				String enSameAs = resource != null ? resource.getURI() : "";
				Literal literal = solution.getLiteral(enLabelVar);
				String enLabel = 
						literal != null ? literal.getLexicalForm() : "";
				literal = solution.getLiteral(enCommentVar);
				String enComment = 
						literal != null ? literal.getLexicalForm() : "";
				literal = solution.getLiteral(langLabelVar);
				String langLabel = 
						literal != null ? literal.getLexicalForm() : "";
				literal = solution.getLiteral(langCommentVar);
				String langComment = 
						literal != null ? literal.getLexicalForm() : "";
				
				DBpediaResource current = 
						new DBpediaResource(URI,
								            enSameAs,
								            enLabel, 
								            enComment,
											langLabel,
											langComment,
											language);
				result.add(current);
			}
		}
		catch (Exception e)
		{
			throw new ProcessingException(e);
		}
		finally
		{
			queryExecution.close();
		}
		return result;
	}
	
	public List<DBpediaResource> execute(String text, 
			                             String language,
			                             double confidence) 
			                            		 throws ProcessingException  
	{
		if (text == null || text.trim().isEmpty())
			throw new IllegalArgumentException("text is mandatory.");
		if (language == null || language.trim().isEmpty())
		    throw new IllegalArgumentException("language is mandatory.");
		if (confidence < 0 || confidence > 1)
			throw new IllegalArgumentException(
					"confidence must be in interval [0,1].");
		
		language = language.trim().toLowerCase();
		if (!(language.equals("en") || //English
			  language.equals("de") || //German
			  language.equals("nl") || //Dutch
			  language.equals("fr") || //French
			  //language.equals("it") || //Italian - no sparql endpoint
			  //language.equals("ru") || //Russian - no sparql endpoint
			  language.equals("es") || //Spanish
			  language.equals("pt")    //|| //Portuguese
			  //language.equals("hu") || //Hungarian - no sparql endpoint
			  //language.equals("tr")    //Turkish - no sparql endpoint
			  ))  
			throw new IllegalArgumentException("The language "+ language + 
					" is not supported.");
		
		text = text.trim();
		
		try {
			
			String API_URL = SERVER + "/" + language + "/annotate";
			String parameters = "confidence=" + confidence
		                        + "&support=" + SUPPORT
		                        + "&text=" + URLEncoder.encode(text, "utf-8");
			
			//HTTP GET
			//HttpGet request = new HttpGet(API_URL + "?" + parameters);
			//HTTP GET
			
			//HTTP POST
			HttpPost request = new HttpPost(API_URL);
			request.addHeader("content-type", 
					          "application/x-www-form-urlencoded");
			request.setEntity(new StringEntity(parameters));
			//HTTP POST
			
			request.addHeader("accept", "application/json");
			
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(request);  
			
			if (response.getStatusLine().getStatusCode() != 200) 
			{
	            throw new ProcessingException("HTTP error code : "
	               + response.getStatusLine().getStatusCode());
	        }
			
			InputStream inputStream = response.getEntity().getContent();
			
			String spotlightResponse = new String(inputStream.readAllBytes(), 
							               StandardCharsets.UTF_8);
			
			JSONObject resultJSON = new JSONObject(spotlightResponse);
			JSONArray entities = resultJSON.getJSONArray("Resources");
			
			return this.createResources(entities, language);
		} 
		catch (Exception e) 
		{
			throw new ProcessingException(e);
		} 
		
	}

	

}
