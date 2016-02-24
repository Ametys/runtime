package org.ametys.core.datasource;

/**
 * Exception representing the absence of a data source
 */
public class UnknownDataSourceException extends RuntimeException
{
    /**
     * Constructor without arguments
     */
    public UnknownDataSourceException() 
    {
        super();
    }
    
    /**
     * Constructor with a message
     * @param msg The exception message
     */
    public UnknownDataSourceException(String msg) 
    {
        super(msg);
    }
    
    /**
     * Constructor with the message and the cause
     * @param cause the cause of the exception
     */
    public UnknownDataSourceException(Throwable cause) 
    {
        super(cause);
    }
    
    /**
     * Constructor with the message and the cause
     * @param msg the exception message 
     * @param cause the cause of the exception
     */
    public UnknownDataSourceException(String msg, Throwable cause) 
    {
        super(msg, cause);
    }
}
