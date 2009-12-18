/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.FileSource;


/**
 * SourceFactory handling resources URIs for plugins.
 * Plugin resources can be found of File System, in a JAR, ...
 */
public class PluginSourceFactory extends AbstractLogEnabled implements SourceFactory, Contextualizable, Serviceable
{
    private static final Pattern __SOURCE_PATTERN = Pattern.compile("^[\\w]+:(" + PluginsManager.PLUGIN_NAME_REGEXP + ")://(.*)$");
    
    private SourceResolver _resolver;
    
    private org.apache.cocoon.environment.Context _cocoonContext;
    
    public Source getSource(String location, Map parameters) throws IOException
    {
        Matcher m = __SOURCE_PATTERN.matcher(location);
        if (!m.matches())
        {
            throw new MalformedURLException("URI must be like protocol:<plugin name>://path/to/resource. Location was '" + location + "'");
        }
        
        String pluginName = m.group(1);
        String path = "/" + m.group(2); 
        
        String resourceURI = PluginsManager.getInstance().getBaseURI(pluginName);
        
        if (resourceURI == null)
        {
            String pluginLocation = PluginsManager.getInstance().getPluginLocation(pluginName);
            if (pluginLocation == null)
            {
                String errorMessage = "The plugin '" + pluginName + "' does not exists.";
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn(errorMessage);
                }
                throw new SourceNotFoundException(errorMessage);
            }
             
            if (!pluginLocation.endsWith("/"))
            {
                pluginLocation += '/';
            }
            
            return new FileSource("file://" + _cocoonContext.getRealPath(pluginLocation + pluginName + path));
        }
        else
        {
            return _resolver.resolveURI(resourceURI + path);
        }
    }
    
    public void release(Source source)
    {
        // empty method
    }

    public void contextualize(Context context) throws ContextException
    {
        _cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
}
