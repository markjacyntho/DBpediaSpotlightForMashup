package br.edu.iff.dbpedia_spotlight.service;
 
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class GraphProcessor 
{
	//Singleton
	private static GraphProcessor soleInstance = null;
	public static GraphProcessor soleInstance()
	{
		if (GraphProcessor.soleInstance == null)
			GraphProcessor.soleInstance = new GraphProcessor();
		
		return GraphProcessor.soleInstance;
	}
	
	private GraphProcessor() {}
	
	//sem aprovação
	public void processTriples(Model input, 
					           Model output,
					           String language,
					           double confidence)
	{
		this.processTriples(input, 
				            output, 
				            language, 
				            confidence, 
				            new Approver() 
							{

								@Override
								public boolean approve(Resource subject, 
										           DBpediaResource current,
										           int currentPosition,
										           int numberOfDBpediaResources) 
								{
									return true;
								}
			
							});
	}
	
	//com aprovação
	public void processTriples(Model input, 
					           Model output,
					           String language,
					           double confidence, 
					           Approver approver)
	{
		for (StmtIterator it = input.listStatements(); it.hasNext();)
		{
			Statement triple = it.nextStatement();
			processTriple(output, triple, language, confidence, approver);
		}
	
	}

	private void processTriple(Model output, 
			                   Statement triple,
			                   String language,
			                   double confidence, 
			                   Approver approver)
	{
		String texto = triple.getString();
		List<DBpediaResource> resources = 
				TextProcessor.soleInstance().extract(texto, 
						                             language, 
						                             confidence);
		int total = resources.size();
		for (int i = 0; i < total; i++)
		{
			DBpediaResource current = resources.get(i);
			
			if (approver.approve(triple.getSubject(), current, i+1, total))
			{
				output.add(triple.getSubject(), 
						   triple.getPredicate(), 
						   output.createResource(current.URI()));
				if (current.hasEnSameAs())
					output.add(triple.getSubject(), 
							   triple.getPredicate(), 
							   output.createResource(current.enSameAs()));
				
			}
		}
		
	}

	

	public static interface Approver
	{
		public boolean approve(Resource subject, 
				               DBpediaResource current,
				               int currentPosition,
				               int numberOfDBpediaResources);
	}
	
}
