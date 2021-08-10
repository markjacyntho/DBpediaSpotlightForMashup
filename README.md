# DBpedia Spotlight For Linked Data Mashup
Using DBpedia Spotlight (https://www.dbpedia-spotlight.org/), a tool for automatic extraction of DBpedia (https://www.dbpedia.org/) resources from text, this project offers the following services:

1. DBpedia resources extraction from text

   This service is straightforward. The operation *List<DBpediaResource> execute(String text, String language, double confidence)*, in the class *br.edu.iff.dbpedia_spotlight.service.Extractor*, receives the text, the language (en, de, nl, fr, es, pt), the confidence threshold (a value between 0.0 and 1.0), and returns the list of DBpedia resources (class *br.edu.iff.dbpedia_spotlight.service.DBpediaResource*) automatically extracted from the text. Each DBpedia resource object contains the resource URI, rdfs:label, rdfs:comment, and owl:sameAs related English resource URI. It is noteworthy that this service works with long texts.

2. Linking to DBpedia resources for Linked Data mashup
    
    This service uses the first one to extract DBpedia resources from textual values related to specified resources and then links the extracted DBpedia resources to the specified resources, using a specified property (e.g. rdfs:seeAlso). The operation *void execute(Model input, Model output, String language, double confidence, Approver approver)*, in the class *br.edu.iff.dbpedia_spotlight.service.Linker*, receives an Apache Jena model (RDF graph) as input and fills in the Jena output model with the extracted DBpedia resources. The optional *approver* is an implementation of the nested interface *Linker.Approver*, which contains the logic to approve each automatically extracted DBpedia resource. 
    
    The RDF triples in the input model must be in accordance to the following template: 
        
        <resource> <linking property> <textual value>  
  
    Each triple from the input model will be processed to extract DBpedia resources based on its textual value, and then, for each extracted DBpedia resource, a corresponding triple will be inserted in the output model using the following template:
        
        <resource> <linking property> <DBpedia resource>

    To generate the input model from your RDF dataset, a suggestion is to use SPARQL construct queries. After the processing, the resulting output model can be inserted into your dataset establishing Linked Data mashup with DBpedia.
    
    For convenience, the class *br.edu.iff.dbpedia_spotlight.main.Main* provides a command-line application that uses this service. This application must receive the following command-line arguments, in this order, separated by space:
    1. with approval (y) or without approval (n);
    2. language code (en, pt, es, de, nl, fr);
    3. confidence value in interval [0.0, 1.0];
    4. complete input RDF file path (local path or HTTP URL);
    5. complete output RDF file path (local path only).
    
    With approval (y) means the user will be asked for approving (y) or not (n) each extracted DBpedia resource, improving the accuracy. Without approval (n) means the extraction and linking will be completely automatic (some wrong DBpedia resources may appear). 
    The syntax of the input RDF file is determined by the content type (if an HTTP request), then the file extension if there is no content type. The permitted syntaxes and corresponding file extensions are: RDF/XML (.rdf), TURTLE (.ttl), and N-TRIPLES (.nt). Other file extensions will raise an error with the message "Content is not allowed in prolog".
    The syntax of the output RDF file is also determined by the same file extension correspondence. Nevertheless, if an unknown file extension is used, the syntax will be RDF/XML.
    A Windows executable file (spotlightLinker.exe) and runnable JAR (JavaSE-11) file  (spotlightLinker.jar) are provided in the releases of the application.
    
    To illustrate the use of the application, two examples are available:
    
    1. English example (en)
        * command line: spotlightLinker n en 0.5 input.ttl output.ttl
        * input RDF file in TURTLE: [input.ttl](https://drive.google.com/file/d/1w4tX9odNk3uS6f5rfGvTwhwdnR5pEVRR/view?usp=sharing "input RDF file")
        * output RDF file in TURTLE: [output.ttl](https://drive.google.com/file/d/1zLFmR9dd5b5LD8XGFwQ0_QpkmzwOYyuB/view?usp=sharing "output RDF file")
        
    3. Portuguese example (pt)
        * command line: spotlightLinker n pt 0.5 input.ttl output.rdf
        * input RDF file in TURTLE: [input.ttl](https://drive.google.com/file/d/1uHIshFqx1og1jq42fr89xislH0bQIbiH/view?usp=sharing "input RDF file")
        * output RDF file in RDF/XML: [output.rdf](https://drive.google.com/file/d/1ewDWZwB9ONuIGNCSCf9i8tV1LJ0We9j9/view?usp=sharing "output RDF file")
    
    
    Sincerely,<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Prof. Mark Douglas de Azevedo Jacyntho.
