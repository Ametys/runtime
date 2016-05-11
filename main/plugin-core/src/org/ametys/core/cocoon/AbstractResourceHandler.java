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
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Abstract resource handler that supports the "suffixes" configuration
 */
public abstract class AbstractResourceHandler extends AbstractLogEnabled implements ResourceHandler, Serviceable
{
    /** The input source */
    protected Source _inputSource;

    /** The object model */
    protected Map _objectModel;

    /** The source */
    protected String _source;
    
    /** The parameters */
    protected Parameters _parameters;

    /** The source resolver */
    protected SourceResolver _resolver;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _resolver = (SourceResolver) serviceManager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void setup(org.apache.cocoon.environment.SourceResolver resolver, Map objectModel, String source, Parameters par) throws IOException, ProcessingException, SAXException
    {
        _objectModel = objectModel;
        _source = source;
        _parameters = par;
        
        _inputSource = _resolver.resolveURI(source);

        if (!_inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
        }
    }
}
