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
package org.ametys.runtime.cocoon;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ResourceReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.ImageHelper;

/**
 * Resource reader but where the source resolver is the runtime one. 
 */
@SuppressWarnings("deprecation")
public class RuntimeResourceReader extends ResourceReader implements Serviceable
{
    /** the avalon service manager */
    protected ServiceManager _manager;
    
    private int _width;
    private int _height;
    private int _maxWidth;
    private int _maxHeight;
    
    private Collection<String> _allowedFormats = Arrays.asList(new String[]{"png", "gif", "jpg", "jpeg"});

    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    @Override
    public void setup(SourceResolver initialResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        org.apache.excalibur.source.SourceResolver runtimeResolver;
        try
        {
            runtimeResolver = (org.apache.excalibur.source.SourceResolver) _manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        }
        catch (ServiceException e)
        {
            String errorMessage = "The runtime resource reader cannot be setup : the runtime source resolver cannot be retrived";
            getLogger().error(errorMessage);
            throw new ProcessingException(errorMessage, e);
        }
                
        _width = par.getParameterAsInteger("width", 0);
        _height = par.getParameterAsInteger("height", 0);
        _maxWidth = par.getParameterAsInteger("maxWidth", 0);
        _maxHeight = par.getParameterAsInteger("maxHeight", 0);
        
        super.setup(new SourceResolverWrapper(runtimeResolver), cocoonObjectModel, src, par);
    }
    
    @Override
    public Serializable getKey() 
    {
        return inputSource.getURI() + "###" + _width + "x" + _height + "x" + _maxWidth + "x" + _maxHeight;
    }
    
    @Override
    public void generate() throws IOException, ProcessingException
    {
        if (!inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + inputSource.getURI());
        }
         
        if (_width == 0 && _height == 0 && _maxWidth == 0 && _maxHeight == 0)
        {
            super.generate();
        }
        else
        {
            InputStream is = inputSource.getInputStream();

            try
            {
                // it's an image, which must be resized
                String format = StringUtils.substringAfterLast(inputSource.getURI(), ".").toLowerCase();
                format = _allowedFormats.contains(format) ? format : "png";
                
                ImageHelper.generateThumbnail(is, out, format, _height, _width, _maxHeight, _maxWidth);
            }
            finally
            {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(out);
            }
        }
        
        
    }
    
    class SourceResolverWrapper implements org.apache.cocoon.environment.SourceResolver
    {
        org.apache.excalibur.source.SourceResolver _res;
        
        /**
         * Create a wrapper
         * @param res The resolver to wrap
         */
        public SourceResolverWrapper(org.apache.excalibur.source.SourceResolver res)
        {
            _res = res;
        }

        public Source resolve(String systemID) throws ProcessingException, SAXException, IOException
        {
            throw new ProcessingException("resolve not handled");
        }

        public void release(org.apache.excalibur.source.Source eSource)
        {
            _res.release(eSource);
        }

        public org.apache.excalibur.source.Source resolveURI(String location) throws MalformedURLException, IOException
        {
            return _res.resolveURI(location);
        }

        public org.apache.excalibur.source.Source resolveURI(String location, String base, Map sParameters) throws MalformedURLException, IOException
        {
            return _res.resolveURI(location, base, sParameters);
        }
    } 
}
