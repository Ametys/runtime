/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.cocoon;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * Default resource reader, that handle different resources type using the ResourcesExtensionPoint.
 */
public class ResourceReader extends ServiceableReader implements CacheableProcessingComponent
{
    private ResourceHandlerExtensionPoint _resourcesHandlerEP;
    private ResourceHandler _resourceHandler;

    @Override
    public void service(ServiceManager sManager) throws ServiceException
    {
        super.service(sManager);
        _resourcesHandlerEP = (ResourceHandlerExtensionPoint) sManager.lookup(ResourceHandlerExtensionPoint.ROLE);
    }
    
    @Override
    public void setup(SourceResolver res, Map objModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        super.setup(res, objModel, src, par);
        
        _resourceHandler = _resourcesHandlerEP.getExtensionBySuffix(source);
        
        if (_resourceHandler == null)
        {
            throw new ProcessingException("No resource handler for current resource '" + this.source + "'");
        }
        
        _resourceHandler.setup(resolver, objectModel, source, parameters);
    }

    @Override
    public void generate() throws IOException, ProcessingException
    {
        _resourceHandler.generateResource(out);
        out.flush();
    }

    @Override
    public Serializable getKey()
    {
        return _resourceHandler != null ? _resourceHandler.getKey() : null;
    }
    
    @Override
    public SourceValidity getValidity()
    {
        return _resourceHandler != null ? _resourceHandler.getValidity() : null;
    }

    @Override
    public void recycle()
    {
        super.recycle();
        _resourceHandler = null;
    }
    
    @Override
    public String getMimeType()
    {
        return _resourceHandler != null ? _resourceHandler.getMimeType() : super.getMimeType();
    }
    
    @Override
    public long getLastModified()
    {
        return _resourceHandler != null ? _resourceHandler.getLastModified() : super.getLastModified();
    }
}
