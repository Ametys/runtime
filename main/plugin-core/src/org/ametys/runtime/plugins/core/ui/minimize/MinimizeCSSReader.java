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

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;

/**
 * This generator generates a single CSS file to load all ui items css files.
 * Can generates a list of imports of directly intergrates all css files.
 */
public class MinimizeCSSReader extends AbstractMinimizeReader
{
    private static final String[] __START_OF_IMAGE_FILE = new String[] {"(", "'", ":", " "};
    private static final String[] __END_OF_IMAGE_FILE = new String[] {".gif", ".jpg", ".jpeg", ".png"};
    private static final Pattern __EXTERNAL_URL = Pattern.compile("^(http[s]?://[^/]+)(/.*)?$");
    
    @Override
    public String getMimeType()
    {
        return "text/css";
    }

    @Override
    protected String getListCode()
    {
        return "$css";
    }

    @Override
    protected String _handleFileDirect(String file, String contextPath)
    {
        StringBuffer sb = new StringBuffer();
        
        Source csssource = null;
        InputStream is = null;
        try
        {
            String cssPathForImages = "/";
            String cssPathForCSS = "/";
            String externalFile = StringUtils.startsWith(file, "~") ? org.apache.cocoon.util.NetUtils.normalize(file.substring(1)) : file;
            if (externalFile.lastIndexOf("/") != -1)
            {
                cssPathForImages = externalFile.substring(0, externalFile.lastIndexOf("/") + 1);
                cssPathForCSS = file.substring(0, file.lastIndexOf("/") + 1);
            }

            csssource = _resolver.resolveURI(StringUtils.startsWith(file, "~") ? "cocoon:/" + org.apache.cocoon.util.NetUtils.normalize(file.substring(1)) : file);
            is = csssource.getInputStream();
            
            // Initial file
            String s0 = IOUtils.toString(is);
            // Removing comments
            String s1 = __removeComment(s0);
            // Resolve images
            String s2 = __resolveImages(_getRootRelativePath(cssPathForImages), cssPathForImages, s1);
            // resolving internal imports
            String s3 = __resolveImports(_getRootRelativePath(cssPathForCSS), cssPathForCSS, s2);
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
            getLogger().error("Cannot open CSS for aggregation " + file, e);
            sb.append("/** ERROR " + e.getMessage() + "*/");
        }
        finally
        {
            IOUtils.closeQuietly(is);
            _resolver.release(csssource);
        }
        
        return sb.toString();
    }
    
    @Override
    protected String _handleFileImport(String file, String contextPath)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("@import \"");
        sb.append(StringUtils.startsWith(file, "~") ? contextPath + org.apache.cocoon.util.NetUtils.normalize(file.substring(1)) : file);
        sb.append("\";\n");
        
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
            fileToImport = fileToImport.replaceAll("[(');\t ]", "");
            if (!fileToImport.startsWith("/"))
            {
                fileToImport = currentPath + fileToImport;
            }
            
            return initialString.substring(0, i) + "\n" + _handleFileDirect(fileToImport, rootRelativePath) + "\n" + (eof ? "" : __resolveImports(rootRelativePath, currentPath, endOfString.substring(j + 1)));
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
                String f = initialString.substring(j + 1, i + 4);
                if (!f.startsWith("/"))
                {
                    f = currentPath + f;
                }
                return initialString.substring(0, j + 1) + org.apache.cocoon.util.NetUtils.normalize(rootRelativePath + f) + __resolveImages(rootRelativePath, currentPath, initialString.substring(i + 4));
            }
        }
    }
    
    private String _getRootRelativePath(String currentFile)
    {
        Matcher m = __EXTERNAL_URL.matcher(currentFile);
        if (m.matches())
        {
            return m.group(1);
        }
        else
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
}
