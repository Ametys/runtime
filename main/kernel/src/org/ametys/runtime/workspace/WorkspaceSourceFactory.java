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
package org.ametys.runtime.workspace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.FileSource;

/**
 * SourceFactory handling resources URIs for workspaces.
 * Workspace resources can be found of File System, in a JAR, ...
 */
public class WorkspaceSourceFactory implements SourceFactory, Serviceable
{
    private static final String __PROTOCOL_SEPARATOR = "://"; 
    
    private SourceResolver _resolver;
    
    public Source getSource(String location, Map parameters) throws IOException
    {
        int protocolIndex = location.indexOf(__PROTOCOL_SEPARATOR);
        
        if (protocolIndex == -1)
        {
            throw new MalformedURLException("URI must be like workspace:<workspace name>://path/to/resource. Location was '" + location + "'");
        }
        
        int index = location.indexOf(':');
        
        if (index == protocolIndex)
        {
            throw new MalformedURLException("URI must be like workspace:<workspace name>://path/to/resource. Location was '" + location + "'");
        }
        
        // 2 = __PROTOCOL_SEPARATOR.length - 1 (pour garder le / du début de chaine)
        String path = location.substring(protocolIndex + 2); 
        
        String workspaceName = location.substring(index + 1, protocolIndex);
        
        String resourceURI = WorkspaceManager.getInstance().getBaseURI(workspaceName);
        
        if (resourceURI == null)
        {
            File pluginLocation = WorkspaceManager.getInstance().getLocation(workspaceName);
            return new FileSource("file", new File(pluginLocation, path));
        }
        else
        {
            return _resolver.resolveURI("resource:/" + resourceURI + path);
        }
    }
    
    public void release(Source source)
    {
        // empty method
    }

    public void service(ServiceManager manager) throws ServiceException
    {
        _resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
}
