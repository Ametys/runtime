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

package org.ametys.plugins.core.ui.minimize;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.mozilla.javascript.EvaluatorException;
import org.xml.sax.SAXException;

import org.ametys.plugins.core.ui.minimize.MinimizeTransformer.FileData;

/**
 * This generator generates a single file to load all ui items files.
 * Can generates a list of imports of directly intergrates all files.
 */
public abstract class AbstractMinimizeReader extends ServiceableReader implements CacheableProcessingComponent
{
    /** The source resolver */
    protected SourceResolver _resolver;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }

    @Override
    public Serializable getKey()
    {
        return source;
    }

    @Override
    public SourceValidity getValidity()
    {
        return new NOPValidity();
    }

    @Override
    public long getLastModified()
    {
        return 0;
    }
    
    @Override
    public void setup(org.apache.cocoon.environment.SourceResolver res, java.util.Map obj, String src, org.apache.avalon.framework.parameters.Parameters par) throws ProcessingException, SAXException, IOException 
    {
        super.setup(res, obj, src, par);
        Response response = ObjectModelHelper.getResponse(objectModel);
        response.setDateHeader("Expires", System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
    }

    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        StringBuffer sb = new StringBuffer("");

        List<FileData> files = MinimizeTransformer.getFilesForHash(source);
        if (files != null) 
        {
            for (FileData file : files)
            {
                sb.append(_handleFile(file, ObjectModelHelper.getRequest(objectModel).getContextPath()));
            }
        } 

        IOUtils.write(sb.toString(), out);
        IOUtils.closeQuietly(out);
    }

    /**
     * Implement to include a file
     * @param file The file to include
     * @param contextPath The context path
     * @return The included file
     */
    protected abstract String _handleFile(FileData file, String contextPath);
}
