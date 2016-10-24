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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;

import org.ametys.plugins.core.ui.minimize.MinimizeTransformer.FileData;

/**
 * This generator generates a single CSS file to load all ui items css files.
 * Can generates a list of imports of directly intergrates all css files.
 */
public class MinimizeCSSReader extends AbstractMinimizeReader
{
    private static final Pattern CSS_URL_PATTERN_SRC = Pattern.compile("src\\b\\s*=\\s*['\"](.*?)['\"]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_URL_PATTERN_IMPORT_URL = Pattern.compile("(?:@import\\s*)?\\burl\\b\\s*\\(\\s*['\"]?(.*?)['\"]?\\s*\\)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_URL_PATTERN_IMPORT = Pattern.compile("@import\\b\\s['\"](.*?)['\"]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("(?:^|;)\\s*@import\\b\\s*(?:(?:url)?\\(?\\s*[\"']?)([^)\"']*)[\"']?\\)?[ \\t]*([^;\\r\\n]*)?(?:;\\s*\\r?\\n)?", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    @Override
    public String getMimeType()
    {
        return "text/css";
    }

    @Override
    protected String _handleFile(FileData file, String contextPath)
    {
        return _handleFile(file.getUri(), file.getMedia(), "", contextPath);
    }

    private String _handleFile(String fileUri, String media, String nestedParentFilesName, String contextPath)
    {
        StringBuffer sb = new StringBuffer();
        Source cssSource = null;
        try
        {
            URI uri = new URI(fileUri);
            String uriToResolve = uri.isAbsolute() ? fileUri : "cocoon:/" + org.apache.cocoon.util.NetUtils.normalize(fileUri);
            cssSource = _resolver.resolveURI(uriToResolve);
            
            String s0;
            try (InputStream is = cssSource.getInputStream())
            {
                s0 = IOUtils.toString(is, "UTF-8");
            }
            
            String s1 = _remplaceRelativeUri(s0, contextPath + fileUri, CSS_URL_PATTERN_SRC);
            String s2 = _remplaceRelativeUri(s1, contextPath + fileUri, CSS_URL_PATTERN_IMPORT_URL);
            String s3 = _remplaceRelativeUri(s2, contextPath + fileUri, CSS_URL_PATTERN_IMPORT);
            
            String s4 = __removeComment(s3);
            
            String s5 = _resolveImportUrl(s4.toString(), nestedParentFilesName + fileUri + " > ", contextPath);
            
            String s6 = s5.replaceAll("\r?\n|\t", " ")
                          .replaceAll("\\s\\s+", " ")
                          .replaceAll("\\{ ", "{")
                          .replaceAll(": ", ":")
                          .replaceAll(" ;", ";")
                          .replaceAll("; ", ";");
            
            sb.append("/*! File : " + nestedParentFilesName + fileUri + " */\n");
            
            if (StringUtils.isNotEmpty(media))
            {
                sb.append("@media ");
                sb.append(media);
                sb.append(" {");
            }
            
            sb.append(s6.trim());
            
            if (StringUtils.isNotEmpty(media))
            {
                sb.append("}");
            }
            
            sb.append("\n");
            
            if (StringUtils.isNotEmpty(nestedParentFilesName))
            {
                sb.append("/*! File end : " + nestedParentFilesName + fileUri + " */\n");
            }
        }
        catch (Exception e)
        {
            getLogger().error("Cannot open CSS for aggregation " + fileUri, e);
            sb.append("/** ERROR " + e.getMessage() + "*/");
        }
        finally
        {
            _resolver.release(cssSource);
        }
        
        return sb.toString();
    }

    private String _resolveImportUrl(String content, String nestedParentFilesName, String contextPath) throws URISyntaxException
    {
        StringBuffer sb = new StringBuffer();
        
        Matcher importMatcher = IMPORT_PATTERN.matcher(content);

        while (importMatcher.find()) 
        {
            String cssUrl = importMatcher.group(1);
            String media = importMatcher.group(2);
            
            if (cssUrl != null && !cssUrl.startsWith("http://") && !cssUrl.startsWith("https"))
            {
                URI uri = new URI(cssUrl);
                String importedContent = _handleFile(uri.isAbsolute() ? cssUrl : StringUtils.removeStart(cssUrl, contextPath), media, nestedParentFilesName, contextPath);
                
                importMatcher.appendReplacement(sb, importedContent);
            }
        }
        
        importMatcher.appendTail(sb);
        
        return sb.toString();
    }

    private String _remplaceRelativeUri(String content, String fileUri, Pattern pattern) throws URISyntaxException
    {
        Matcher urlMatcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        
        while (urlMatcher.find()) 
        {
            String fullMatch = urlMatcher.group();
            String cssUrl = urlMatcher.group(1);
            
            if (cssUrl != null && !cssUrl.startsWith("data:"))
            {
                URI uri = new URI(cssUrl);
                if (!uri.isAbsolute())
                {
                    String fullPathUrl = FilenameUtils.normalize(FilenameUtils.concat(FilenameUtils.getFullPath(fileUri), cssUrl), true);
                    urlMatcher.appendReplacement(sb, fullMatch.replace(cssUrl, fullPathUrl));
                }
            }
        }
        
        urlMatcher.appendTail(sb);
        
        return sb.toString();
    }
    
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

}
