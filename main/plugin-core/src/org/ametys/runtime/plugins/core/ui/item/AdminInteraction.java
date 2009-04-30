package org.ametys.runtime.plugins.core.ui.item;

import java.util.regex.Pattern;

/**
 * This interface encapsulate all interactions for admin 
 */
public interface AdminInteraction
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
