package org.ametys.runtime.plugins.core.ui.item;

import java.util.regex.Pattern;

import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.util.I18nizableText;

/**
 * This interaction also handle a regexp url
 */
public class AdminInteractionImpl extends RightInteraction implements AdminInteraction
{
    /** The regexp url */
    protected Pattern _url;
    
    /** The prefix url */
    protected String _prefix = "(/_[^/]*)?/";
    
    /**
     * Create an interaction
     * @param label The label of the interaction. Cannot be null.
     * @param description The description. Cannot be null.
     * @param iconSet The set of icon. Cannot be null.
     */
    public AdminInteractionImpl(I18nizableText label, I18nizableText description, IconSet iconSet)
    {
        super(label, description, iconSet);
    }
    
    public Pattern getUrl()
    {
        return _url;
    }
    
    public void setUrl(String url)
    {
        if (url != null && url.length() > 0)
        {
            _url = Pattern.compile(_prefix + url);
        }
        else
        {
            _url = null;
        }
    }

}
