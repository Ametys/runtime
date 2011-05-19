/*
 *  Copyright 2010 Anyware Services
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

package org.ametys.runtime.plugins.core.ui.js;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.mozilla.javascript.EvaluatorException;
import org.xml.sax.SAXException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * This generator generates a single CSS file to load all ui items css files.
 * Can generates a list of imports of directly intergrates all css files.
 */
public class AllJSReader extends ServiceableReader implements CacheableProcessingComponent
{
    private AllJSComponent _allJSComponent;
    private SourceResolver _resolver;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _allJSComponent = (AllJSComponent) smanager.lookup(AllJSComponent.ROLE);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public String getMimeType()
    {
        return "text/javascript;charset=utf-8";
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
            return new SourceValidity() 
                { 
                    @Override
                    public int isValid()
                    {
                        return -1;
                    }
                    @Override
                    public int isValid(SourceValidity newValidity)
                    {
                        return -1;
                    }
                };
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
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        StringBuffer sb = new StringBuffer("");
        
        List<String> jsFiles = _allJSComponent.getJSFilesList(source);
        if (jsFiles != null) 
        {
            for (String jsFile : jsFiles)
            {
                sb.append(_handleFile(jsFile));
            }
        } 
             
        IOUtils.write(sb.toString(), out);
        IOUtils.closeQuietly(out);
    }
    
    private String _handleFile(String jsFile)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        StringBuffer sb = new StringBuffer();
        
        boolean importMode = parameters.getParameterAsBoolean("import", false);

        String s = "";
        
        if (importMode)
        {
            sb.append("document.write(\"<script type='text/javascript' src='" + request.getContextPath() + org.apache.cocoon.util.NetUtils.normalize(jsFile) + "'><!-- import --></script>\");\n");
        }
        else
        {
            Source jssource = null;
            InputStream is = null;
            try
            {
                jssource = _resolver.resolveURI("cocoon://" + org.apache.cocoon.util.NetUtils.normalize(jsFile));
                is = jssource.getInputStream();
                
                s = IOUtils.toString(is);

                Reader r = new StringReader(s);
                JavaScriptCompressor compressor = new JavaScriptCompressor(r, new LoggerErrorReporter(getLogger()));
                r.close();
                
                Writer w = new StringWriter();
                compressor.compress(w, 8000, false, false, true, true);
                
                sb.append(w.toString());
            }
            catch (Exception e)
            {
                getLogger().error("Cannot minimize JS for aggregation " + jsFile, e);
                sb.append("/** ERROR " + e.getMessage() + "*/");
            }
            finally
            {
                IOUtils.closeQuietly(is);
                _resolver.release(jssource);
            }
        }
        
        return sb.toString();
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
