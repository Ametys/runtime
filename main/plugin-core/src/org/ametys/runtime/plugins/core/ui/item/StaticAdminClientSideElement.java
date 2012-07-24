/*
 *  Copyright 2012 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
