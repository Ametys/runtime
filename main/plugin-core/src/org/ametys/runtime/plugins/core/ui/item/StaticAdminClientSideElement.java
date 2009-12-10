package org.ametys.runtime.plugins.core.ui.item;

import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.ui.impl.StaticClientSideElement;

/**
 * Creates from static configuration
 */
public class StaticAdminClientSideElement extends StaticClientSideElement implements AdminClientSideElement
{
    /** The regexp url */
    protected Pattern _url;
    
    /** The prefix url (/CONTEXTPATH/_WORKSPACEPATH/ */
    protected String _prefix = "/([^/]*)?(/_[^/]*)?/";
    
    @Override
    public Pattern getUrl()
    {
        return _url;
    }

    @Override
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

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        String pluginName = configuration.getChild("Url").getAttribute("plugin", _pluginName);
        String url = configuration.getChild("Url").getValue(null);
        if (StringUtils.isEmpty(url))
        {
            setUrl(null);
        }
        else
        {
            setUrl("_plugins/" + pluginName + "/" + url);
        }
    }
}
