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

package org.ametys.runtime.plugins.core.ui.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.cocoon.InvalidSourceValidity;

/**
 * This generator generates a single CSS file to load all ui items css files.
 * Can generates a list of imports of directly intergrates all css files.
 */
public class AllCSSReader extends ServiceableReader implements CacheableProcessingComponent
{
    /** The max number allowed of css files to import */
    public static final int __PACKET_SIZE = 31;

    private static final String[] __START_OF_IMAGE_FILE = new String[] {"(", "'", ":", " "};
    private static final String[] __END_OF_IMAGE_FILE = new String[] {".gif", ".jpg", ".jpeg", ".png"};

    private AllCSSComponent _allCSSComponent;
    private SourceResolver _resolver;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _allCSSComponent = (AllCSSComponent) smanager.lookup(AllCSSComponent.ROLE);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public String getMimeType()
    {
        return "text/css";
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
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        String contextPath = _allCSSComponent.getContextPath();
        if (contextPath == null)
        {
            contextPath = request.getContextPath();
        }
        
        String rootRelativePath = _getRootRelativePath();
        
        StringBuffer sb = new StringBuffer("");
        
        List<String> cssFiles = _allCSSComponent.getCSSFilesList(source);
        if (cssFiles != null) 
        {
            boolean importMode = parameters.getParameterAsBoolean("import", false);

            if (importMode)
            {
                // a sub part of the css file list is required ?
                int packetNum = parameters.getParameterAsInteger("packet-num", 0);
                
                List<String> cssFilesToImport = cssFiles.subList(__PACKET_SIZE * packetNum, Math.min(__PACKET_SIZE * (packetNum + 1), cssFiles.size())); 
                                                
                for (String cssFile : cssFilesToImport)
                {
                    sb.append(_handleFileImport(cssFile, contextPath));
                }
            }
            else
            {
                for (String cssFile : cssFiles)
                {
                    sb.append(_handleFileDirect(rootRelativePath, cssFile));
                }
                
            }
        } 
             
        IOUtils.write(sb.toString(), out);
        IOUtils.closeQuietly(out);
    }
    
    private String _handleFileImport(String cssFile, String contextPath)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("@import \"");
        sb.append(contextPath);
        sb.append(cssFile);
        sb.append("\";\n");
        
        return sb.toString();
    }
    
    private String _handleFileDirect(String rootRelativePath, String cssFile)
    {
        StringBuffer sb = new StringBuffer();
        
        Source csssource = null;
        InputStream is = null;
        try
        {
            String cssPath = "/";
            if (cssFile.lastIndexOf("/") != -1)
            {
                cssPath = cssFile.substring(0, cssFile.lastIndexOf("/") + 1);
            }

            csssource = _resolver.resolveURI("cocoon:/" + org.apache.cocoon.util.NetUtils.normalize(cssFile));
            is = csssource.getInputStream();
            
            // Initial file
            String s0 = IOUtils.toString(is);
            // Removing comments
            String s1 = __removeComment(s0);
            // Resolve images
            String s2 = __resolveImages(rootRelativePath, cssPath, s1);
            // resolving internal imports
            String s3 = __resolveImports(rootRelativePath, cssPath, s2);
            // Removing break line
            String s4 = s3.replaceAll("\r?\n|\t", " ");
            // Removing other
            String s5 = s4.replaceAll("  ", " ");
            String s6 = s5.replaceAll("\\{ ", "{");
            String s7 = s6.replaceAll(" \\}", "}");
            String s8 = s7.replaceAll(" :", ":");
            String s9 = s8.replaceAll(": ", ":");
            String s10 = s9.replaceAll(" ;", ";");
            String s11 = s10.replaceAll("; ", ";");
            
            sb.append(s11.trim());
            sb.append("\n");
        }
        catch (Exception e)
        {
            getLogger().error("Cannot open CSS for aggregation " + cssFile, e);
            sb.append("/** ERROR " + e.getMessage() + "*/");
        }
        finally
        {
            IOUtils.closeQuietly(is);
            _resolver.release(csssource);
        }
        
        return sb.toString();
    }
    
    // Yes I try to code it using regexp to obtain a StackOverflow on big strings
    private String __removeComment(String initialString)
    {
        int i = initialString.indexOf("/*");
        int j = initialString.indexOf("*/");
        if (i == -1 || j == -1 || j < i)
        {
            return initialString;
        }
        else
        {
            return initialString.substring(0, i) + __removeComment(initialString.substring(j + 2)); 
        }
    }
    
    private String __resolveImports(String rootRelativePath, String currentPath, String initialString)
    {
        int i = initialString.indexOf("@import");
        if (i == -1)
        {
            return initialString;
        }
        else
        {
            boolean eof = false;
            String endOfString = initialString.substring(i + 7);
            int j = endOfString.indexOf("\n");
            if (j == -1)
            {
                // We reach the end of file
                j = endOfString.length();
                eof = true;
            }
            
            String fileToImport = endOfString.substring(0, j).trim();
            if (fileToImport.startsWith("url"))
            {
                fileToImport = fileToImport.substring(3);
            }
            fileToImport = currentPath + fileToImport.replaceAll("[(');\t ]", "");
            
            return initialString.substring(0, i) + "\n" + _handleFileDirect(rootRelativePath, fileToImport) + "\n" + (eof ? "" : __resolveImports(rootRelativePath, currentPath, endOfString.substring(j + 1)));
        }
    }
    
    private String __resolveImages(String rootRelativePath, String currentPath, String initialString)
    {
        String initialStringLC = initialString.toLowerCase();

        int i = -1;
        for (String extension : __END_OF_IMAGE_FILE)
        {
            int e = initialStringLC.indexOf(extension);
            if (e != -1 && (e < i || i == -1))
            {
                i = e;
            }
        }
        
        if (i == -1)
        {
            return initialString;
        }
        else
        {
            String untilEndOfFile = initialString.substring(0, i);
            int j = StringUtils.lastIndexOfAny(untilEndOfFile, __START_OF_IMAGE_FILE);
            if (j == -1)
            {
                return initialString;
            }
            else
            {
                return initialString.substring(0, j + 1) + org.apache.cocoon.util.NetUtils.normalize(rootRelativePath + currentPath + initialString.substring(j + 1, i + 4)) + __resolveImages(rootRelativePath, currentPath, initialString.substring(i + 4));
            }
        }
    }
    
    private String _getRootRelativePath ()
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        
        String requestPath = requestURI.substring(contextPath.length());
        int depth = StringUtils.countMatches(requestPath, "/");
        
        String relativePath = "";
        for (int i = 0; i < depth - 1; i++)
        {
            if (i != 0)
            {
                relativePath += "/";
            }
            relativePath += "..";
        }
        
        return relativePath;
    }
}
