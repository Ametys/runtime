package org.ametys.runtime.plugins.core.sqlmap.dao;

/**
 * Error on accessing data.
 */
public class DataAccessException extends RuntimeException
{
    /**
     * Constructs a new data access exception with the specified detail message.
     * @param message The detail message. 
     */
    public DataAccessException(String message)
    {
        super(message);
    }
    
    /**
     * Constructs a new data access exception with the specified detail message and
     * cause.
     * @param message The detail messag.
     * @param cause The cause.
     */
    public DataAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * Constructs a new data access exception with the specified cause.
     * @param cause The cause.
     */
    public DataAccessException(Throwable cause)
    {
        super(cause);
    }
}
