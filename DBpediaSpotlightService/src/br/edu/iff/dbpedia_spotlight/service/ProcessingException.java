package br.edu.iff.dbpedia_spotlight.service;

public class ProcessingException extends Exception 
{
	private static final long serialVersionUID = 2935981735233863571L;

	public ProcessingException() 
	{
		super();
	}

	public ProcessingException(String message, 
			                   Throwable cause, 
			                   boolean enableSuppression, 
			                   boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessingException(String message, Throwable cause) 
	{
		super(message, cause);
	}

	public ProcessingException(String message) 
	{
		super(message);
	}

	public ProcessingException(Throwable cause) 
	{
		super(cause);
	}
	
}
