package org.ametys.runtime.util.cocoon;

import org.apache.excalibur.source.SourceValidity;

/**
 * A never valid {@link SourceValidity}.
 */
public class InvalidSourceValidity implements SourceValidity
{
    @Override
    public int isValid(SourceValidity newValidity)
    {
        return -1;
    }
    
    @Override
    public int isValid()
    {
        return -1;
    }
}
