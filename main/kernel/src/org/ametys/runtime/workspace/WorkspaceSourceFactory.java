/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.workspace;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.FileSource;

/**
 * SourceFactory handling resources URIs for workspaces.
 * Workspace resources can be found of File System, in a JAR, ...
 */
public class WorkspaceSourceFactory implements SourceFactory, Contextualizable, Serviceable
{
    private static final String __PROTOCOL_SEPARATOR = "://"; 
    
    private String _path;
    
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
            return new FileSource("file://" + _path + "workspaces/" + workspaceName + path);
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

    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _path = cocoonContext.getRealPath("/");
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
}
