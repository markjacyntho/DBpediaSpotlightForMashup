package br.edu.iff.dbpedia_spotlight.main; 

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import br.edu.iff.dbpedia_spotlight.service.DBpediaResource;
import br.edu.iff.dbpedia_spotlight.service.Linker;
import br.edu.iff.dbpedia_spotlight.service.Linker.Approver;

public class Main 
{ 
   
	private static String outputSyntax(String outputFile)
	{
		String result;
		
		outputFile = outputFile.toLowerCase();
		if (outputFile.endsWith(".ttl"))
			result = "TURTLE";
		else if (outputFile.endsWith(".nt"))
			result = "N-TRIPLE";
		else
			result = "RDF/XML";
		
		return result;
	}
	
	public static void main(String[] args) 
	{
		if (args.length != 5)
		{	
			System.out.println(
				   "All the following command line arguments must be provided,"
				 + " in this order, separated by space:\n"
				 + "\n1) with approval (y) or without approval (n);"
				 + "\n2) language code (one of: en, pt, es, de, nl, fr); "
				 + "\n3) confidence value in interval [0.0, 1.0];"
				 + "\n4) complete input RDF file path (local path or HTTP URL);"
				 + "\n5) complete output RDF file path (local path).");
			return; //exit program
		}	
		
		boolean approve = args[0].toLowerCase().charAt(0) == 'y';
		String language = args[1];
		double confidence = Double.valueOf(args[2]);
		String inputFile = args[3];
		String outputFile = args[4];
		
		Linker.Approver approver =
				new Approver()
				{
					@Override
					public boolean execute(Resource subject, 
							               DBpediaResource current,
							               int currentPosition,
							               int numberOfDBpediaResources) 
					{
						boolean result = true;
						
						if (approve)
						{
							System.out.println("\nResource:"+subject.getURI());
							System.out.println("\nDBpedia resource " + 
								                currentPosition + " of " + 
									            numberOfDBpediaResources + ":");
							System.out.println(current);
							System.out.print("Approve (y/n)?: ");
							@SuppressWarnings("resource")
							Scanner sc = new Scanner(System.in);
							char answer = sc.nextLine().toLowerCase().charAt(0);
							result = (answer == 'y');
						}
						
						return result;
					}
				};
				
		try 
		{ 
			System.out.println("Reading file " + inputFile);
			
			//read from input file to input model
			Model input = ModelFactory.createDefaultModel();
			input.read(inputFile);
			
			System.out.println("Processing...");
			
			//output model
			Model output = ModelFactory.createDefaultModel();	
			
			Linker.soleInstance().execute(input, 
						                  output, 
						                  language, 
						                  confidence, 
						                  approver);

			System.out.println("Generating file " + outputFile);
			
			//write to output file
			OutputStream out = new FileOutputStream(outputFile);
			output.write(out, outputSyntax(outputFile));
			
			System.out.println("\nDone!\n\nCheers,\n     "
					+ "Prof. Mark Douglas de Azevedo Jacyntho");
		} 
		catch (Exception e)
		{
			System.out.println("\nThe following problem occurred:\n" + 
		                        e.getMessage());
		}
		
	}

}
