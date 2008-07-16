package org.ametys.runtime.plugins.core.sqlmap.dao;

/**
 * Exception thrown when an attempt to insert or update data results in
 * violation of an integrity constraint.
 */
public class DataIntegrityViolationException extends DataAccessException
{
    /**
     * Constructs a new data integrity violation exception with the specified
     * detail message.
     * @param message The detail message. 
     */
    public DataIntegrityViolationException(String message)
    {
        super(message);
    }
    
    /**
     * Constructs a new data integrity violation exception with the specified
     * detail message and cause.
     * @param message The detail messag.
     * @param cause The cause.
     */
    public DataIntegrityViolationException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * Constructs a new data integrity violation exception with the specified
     * cause.
     * @param cause The cause.
     */
    public DataIntegrityViolationException(Throwable cause)
    {
        super(cause);
    }
}
