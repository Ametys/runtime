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

package org.ametys.runtime.plugins.core.ui.minimize;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.mozilla.javascript.EvaluatorException;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.cocoon.InvalidSourceValidity;

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
        boolean importMode = parameters.getParameterAsBoolean("import", false);
        return source + "-" + importMode;
    }
    
    @Override
    public SourceValidity getValidity()
    {
        boolean importMode = parameters.getParameterAsBoolean("import", false);

        if (importMode)
        {
            return new InvalidSourceValidity();
        }
        else
        {
            return new NOPValidity();
        }
    }
    
    @Override
    public long getLastModified()
    {
        return 0;
    }
    
    /**
     * Should return the session attribute suffix ($js or $css for example)
     * @return The code
     */
    protected abstract String getListCode();
    
    private List<String> _getFileList()
    {
        Session session = ObjectModelHelper.getRequest(objectModel).getSession(true);
        
        Map<Integer, List<String>> codesAndFiles = (Map<Integer, List<String>>) session.getAttribute(MinimizeTransformer.class.getName() + getListCode());
        if (codesAndFiles == null)
        {
            throw new IllegalStateException("No files list register in that user's session (" + getListCode() + ")");
        }
        
        int id = Integer.parseInt(source);
        if (!codesAndFiles.containsKey(id))
        {
            throw new IllegalStateException("No files list using code '" + source + "' register in that user's session (" + getListCode() + ")");
        }
        return codesAndFiles.get(id);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        StringBuffer sb = new StringBuffer("");
        
        List<String> files = _getFileList();
        if (files != null) 
        {
            for (String file : files)
            {
                sb.append(_handleFile(file, ObjectModelHelper.getRequest(objectModel).getContextPath()));
            }
        } 
             
        IOUtils.write(sb.toString(), out);
        IOUtils.closeQuietly(out);
    }
    
    /**
     * Implement to import a file
     * @param file The file to import
     * @param contextPath The context path
     * @return The imported file
     */
    protected abstract String _handleFileImport(String file, String contextPath);
    /**
     * Implement to include a file
     * @param file The file to include
     * @param contextPath The context path
     * @return The included file
     */
    protected abstract String _handleFileDirect(String file, String contextPath);
    
    private String _handleFile(String file, String contextPath)
    {
        boolean importMode = parameters.getParameterAsBoolean("import", false);

        if (importMode)
        {
            return _handleFileImport(file, contextPath);
        }
        else
        {
            return _handleFileDirect(file, contextPath);
        }
    }
    
    /**
     * Error reporter in a logger 
     */
    public class LoggerErrorReporter implements org.mozilla.javascript.ErrorReporter
    {
        private Logger _logger;
        
        /** 
         * Create the a reporter based uppon a logger
         * @param logger The logger
         */
        public LoggerErrorReporter(Logger logger)
        {
            _logger = logger;
        }
        
        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
        {
            _logger.warn(message + " " + sourceName + " line " + line + " " + lineSource + " col " + lineOffset);
        }
        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
        {
            _logger.error(message + " " + sourceName + " line " + line + " " + lineSource + " col " + lineOffset);
        }
        
        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset)
        {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }
}
