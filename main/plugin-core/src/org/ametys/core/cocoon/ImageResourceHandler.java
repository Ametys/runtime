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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * Resource handler for images
 */
public class ImageResourceHandler extends AbstractResourceHandler implements Component
{
    /** last modified parameter name for resources parameters */
    public static final String LAST_MODIFIED = "lastModified";
    
    private static final Pattern _SIZE_PATTERN = Pattern.compile("^(.*)(?:_(max)?([0-9]+)x([0-9]+))(\\.[^./]+)$");

    private Collection<String> _allowedFormats = Arrays.asList(new String[]{"png", "gif", "jpg", "jpeg"});

    private SourceResolver _sourceResolver;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _resolver = (SourceResolver) serviceManager.lookup(SourceResolver.ROLE);
        _sourceResolver = (SourceResolver) serviceManager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void setup(org.apache.cocoon.environment.SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        _objectModel = cocoonObjectModel;
        _source = src;
        _parameters = par;
        
        try 
        {
            _inputSource = _sourceResolver.resolveURI(src);
        } 
        catch (SourceException e) 
        {
            _inputSource = null;
        }
        
        // Compute the locale
        if (_inputSource == null || !_inputSource.exists())
        {
            Matcher sizeMatcher = _SIZE_PATTERN.matcher(src);
            if (sizeMatcher.matches())
            {
                _source = sizeMatcher.group(1) + sizeMatcher.group(5); 
                _inputSource = _sourceResolver.resolveURI(_source);
                if (!_inputSource.exists())
                {
                    throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
                }
                
                boolean isMaxSize = sizeMatcher.group(2) != null;
                String height = sizeMatcher.group(3);
                String width = sizeMatcher.group(4);

                _parameters.setParameter("height", isMaxSize ? "0" : height);
                _parameters.setParameter("width", isMaxSize ? "0" : width);
                _parameters.setParameter("maxHeight", isMaxSize ? height : "0");
                _parameters.setParameter("maxWidth", isMaxSize ? width : "0");
            }
            else
            {
                throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
            }
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) cocoonObjectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        if (params != null)
        {
            params.put(ImageResourceHandler.LAST_MODIFIED, _inputSource.getLastModified());
        }
    }

    @Override
    public void generateResource(OutputStream out) throws IOException, ProcessingException
    {
        String format = StringUtils.substringAfterLast(_inputSource.getURI(), ".").toLowerCase();
        format = _allowedFormats.contains(format) ? format : "png";
        InputStream generatedImage = ImageResourceHelper.generateImage(_inputSource.getInputStream(), _parameters, format);
        IOUtils.copy(generatedImage, out);
    }

    @Override
    public String getMimeType()
    {
        Context context = ObjectModelHelper.getContext(_objectModel);
        if (context != null) 
        {
            final String mimeType = context.getMimeType(_source);
            
            if (mimeType != null) 
            {
                return mimeType;
            }
        }
        return _inputSource.getMimeType();
    }

    @Override
    public Serializable getKey()
    {
        return ImageResourceHelper.getSerializableKey(_inputSource.getURI(), _parameters);
    }

    @Override
    public SourceValidity getValidity()
    {
        return _inputSource.getValidity();
    }

    @Override
    public long getSize()
    {
        return _inputSource.getContentLength();
    }

    @Override
    public long getLastModified()
    {
        return _inputSource.getLastModified();
    }

}
