package br.edu.iff.dbpedia_spotlight.service;
 
import java.util.Objects;

public class DBpediaResource 
{
	private String URI;
	private String enSameAs;
	private String enLabel;
	private String enComment;
	private String langLabel;
	private String langComment;
	private String lang;
	
	public DBpediaResource(String URI, 
			               String enSameAs,
			               String enLabel, 
			               String enComment,
			               String langLabel,
			               String langComment,
			               String lang) 
	{
		if (URI == null || URI.trim().isEmpty())
			throw new IllegalArgumentException("URI is mandatory.");
		
		this.URI = URI.trim();
		this.enSameAs = enSameAs != null ? enSameAs.trim() : "";
		this.enLabel = enLabel != null ? enLabel : "";
		this.enComment = enComment != null ? enComment : "";
		this.langLabel = langLabel != null ? langLabel : "";
		this.langComment = langComment != null ? langComment : "";
		this.lang = lang != null ? lang : "";
	}

	public String URI() 
	{
		return this.URI;
	}
	
	public boolean hasEnSameAs()
	{
		return !this.enSameAs().isEmpty();
	}
	
	public String enSameAs() 
	{
		return this.enSameAs;
	}
	
	public String enLabel() 
	{
		return this.enLabel;
	}

	public String enComment() 
	{
		return this.enComment;
	}
	
	public String langLabel() 
	{
		return this.langLabel;
	}

	public String langComment() 
	{
		return this.langComment;
	}
	
	public String lang()
	{
		return this.lang;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.URI,
				            this.enSameAs,
				            this.enLabel, 
				            this.enComment, 
				            this.langLabel,
				            this.langComment, 
				            this.lang);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (!(obj instanceof DBpediaResource)) return false;
		if (this == obj) return true;
		
		DBpediaResource other = (DBpediaResource) obj;
		
		return this.URI.equals(other.URI) &&
			   this.enSameAs.equals(other.enSameAs) &&	
			   this.enLabel.equals(other.enLabel) &&
			   this.enComment.equals(other.enComment) &&
			   this.langLabel.equals(other.langLabel) &&
			   this.langComment.equals(other.langComment) &&
			   this.lang.equals(other.lang);
		
	}

	@Override
	public String toString() 
	{
		String result = "";
		result += "\nURI:\n" + this.URI();
		result += 
		   this.langLabel().isEmpty() ? "" : "\nLabel in "+ this.lang() + ":\n" + this.langLabel();
		result += 
		   this.langComment().isEmpty() ? "" : "\nComment in "+ this.lang() + ":\n" + this.langComment();
		result += 
				this.enSameAs().isEmpty() ? "" : "\nsameAs:\n" + this.enSameAs();
		if (!this.lang().equalsIgnoreCase("en"))
		{	
			result += 
			   this.enLabel().isEmpty() ? "" : "\nLabel in en:\n" + this.enLabel();
			result += 
			   this.enComment().isEmpty() ? "": "\nComment in en:\n" + this.enComment();
		}
		
		return result;
	}
	
	

}
