package org.ametys.runtime.config;

/**
 * A disable condition for config parameters 
 */
public class DisableCondition
{
    /**
     * The available operators
     */
    public enum OPERATOR
    {
        /** Equals */
        EQ,
        /** Non equals */
        NEQ,
        /** Greater than */
        GT,
        /** Greater or equals */
        GEQ,
        /** Less or equals */ 
        LEQ,
        /** Less than */
        LT
    }
    
    private final String _id;
    private final OPERATOR _operator;
    private final String _value;
    
    /**
     * Creates a condition
     * @param id The parameter id
     * @param operator comparison operator of the condition ('eq'...)
     * @param value value to compare to
     */
    public DisableCondition(String id, OPERATOR operator, String value)
    {
        _id = id;
        _operator = operator;
        _value = value;
    }

    /**
     * Get the id
     * @return the parameter identifier
     */
    public String getId()
    {
        return _id;
    }

    /**
     * Get the operator
     * @return The comparison operator
     */
    public OPERATOR getOperator()
    {
        return _operator;
    }

    /**
     * Get the value
     * @return The value to compare to
     */
    public String getValue()
    {
        return _value;
    }
}
