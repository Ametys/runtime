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
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.SourceValidity;

/**
 * Default resource handler
 */
public class DefaultResourceHandler extends AbstractResourceHandler implements Component
{
    /** last modified parameter name for resources parameters */
    public static final String LAST_MODIFIED = "lastModified";

    
    @Override
    public void generateResource(OutputStream out) throws IOException, ProcessingException
    {
        IOUtils.copy(_inputSource.getInputStream(), out);
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
        return _inputSource.getURI();
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
