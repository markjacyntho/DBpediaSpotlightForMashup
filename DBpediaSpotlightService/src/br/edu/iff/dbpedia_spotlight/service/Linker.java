package br.edu.iff.dbpedia_spotlight.service;
 
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Linker 
{
	//Singleton
	private static Linker soleInstance = null;
	public static Linker soleInstance()
	{
		if (Linker.soleInstance == null)
			Linker.soleInstance = new Linker();
		
		return Linker.soleInstance;
	}
	
	private Linker() {}
	
	//without approval
	public void execute(Model input, 
					    Model output,
					    String language,
					    double confidence) 
					    		throws ProcessingException
	{
		this.execute(input, 
				            output, 
				            language, 
				            confidence, 
				            new Approver() 
							{

								@Override
								public boolean execute(Resource subject, 
										           DBpediaResource current,
										           int currentPosition,
										           int numberOfDBpediaResources) 
								{
									return true;
								}
			
							});
	}
	
	//with approval
	public void execute(Model input, 
					    Model output,
					    String language,
					    double confidence, 
					    Approver approver) 
					    		throws ProcessingException
	{
		//set namespace prefixes in output model
		output.setNsPrefixes(input);
		
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
			                		   throws ProcessingException
	{
		String texto = triple.getString();
		List<DBpediaResource> resources = 
				Extractor.soleInstance().execute(texto, 
						                             language, 
						                             confidence);
		int total = resources.size();
		for (int i = 0; i < total; i++)
		{
			DBpediaResource current = resources.get(i);
			
			if (approver.execute(triple.getSubject(), current, i+1, total))
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
		public boolean execute(Resource subject, 
				               DBpediaResource current,
				               int currentPosition,
				               int numberOfDBpediaResources);
	}
	
}
