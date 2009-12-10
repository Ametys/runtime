package org.ametys.runtime.plugins.core.ui.item;

import java.util.regex.Pattern;

import org.ametys.runtime.ui.ClientSideElement;

/**
 * Client side element for administration workspace handle urls to be able to tell which component is currently used in the left bar
 */
public interface AdminClientSideElement extends ClientSideElement
{
    /**
     * Get the url.
     * @return the regexp url. Can be null or empty.
     */
    public Pattern getUrl();
    
    /**
     * Set the url
     * @param url The regexp url
     */
    public void setUrl(String url);
}
