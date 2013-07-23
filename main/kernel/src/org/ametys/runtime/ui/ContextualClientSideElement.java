package org.ametys.runtime.ui;

import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * A client side element that may change its state upon the context
 * @deprecated Use ProcessableClientSideElement instead
 */
@Deprecated
public interface ContextualClientSideElement extends ProcessableClientSideElement
{
    /**
     * This method returns the parameters given to the element script class at a given time.
     * The parameters returns may depend on the current environment.
     * @param parameters The parameters transmitted by the client side script
     * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
     * @deprecated Use process instead.
     */
    @Deprecated
    public Map<String, I18nizableText> getCurrentParameters(Map<String, Object> parameters);
}
