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

package org.ametys.plugins.core.ui.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

/**
 * Reader for LESS files, compile them on the fly into CSS files.
 */
public class LessResourceHandler extends AbstractCompiledResourceHandler
{
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^@import\\b\\s*(?:(?:url)?\\(?\\s*[\"']?)([^)\"']*)[\"']?\\)?\\s*;?$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private DefaultLessCompiler _defaultLessCompiler;
    
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, IOException, SAXException
    {
        super.setup(initalResolver, cocoonObjectModel, src, par);
        
        _defaultLessCompiler = new DefaultLessCompiler();
    }
        
    @Override
    public String compileResource(Source resource) throws IOException, ProcessingException
    {
        CompilationResult result = null;
        
        try (InputStream is = resource.getInputStream())
        {
            URI uri = new URI(_uri);
            String lessContent = IOUtils.toString(is);
            AmetysLessSource stringSource = new AmetysLessSource(_sourceResolver, lessContent, uri);
            result = _defaultLessCompiler.compile(stringSource);
        }
        catch (Less4jException e)
        {
            throw new ProcessingException("Unable to compile the LESS file : " + _uri, e);
        }
        catch (URISyntaxException e)
        {
            throw new ProcessingException("Unable to process LESS File, invalid uri : " + _uri, e);
        }
        
        return result == null ? null : result.getCss();
    }
    
    @Override
    protected List<String> getDependenciesList(Source inputSource)
    {
        List<String> result = new ArrayList<>();
        
        try (InputStream is = inputSource.getInputStream())
        {
            String content = IOUtils.toString(is);
            
            Matcher matcher = IMPORT_PATTERN.matcher(content);
            
            while (matcher.find())
            {
                String cssUrl = matcher.group(1);
                
                if (!StringUtils.contains(cssUrl, "http://") && !StringUtils.contains(cssUrl, "https://")) 
                {
                    if (!StringUtils.endsWith(cssUrl, ".css") && !StringUtils.endsWith(cssUrl, ".less"))
                    {
                        cssUrl += ".less";
                    }
                    
                    result.add(cssUrl);
                }
            }
        }
        catch (IOException e)
        {
            getLogger().warn("Invalid content when listing dependencies for file " + _uri, e);
        }
        
        return result;
    }


    /**
     * LessSource definition for Ametys Resources
     */
    private static class AmetysLessSource extends LessSource
    {
        private String _lessContent;
        private String _name;
        private URI _sourceUri;
        private SourceResolver _sResolver;
    
        /**
         * Default constructor for Ametys less source
         * @param sourceResolver The Source Resolver
         * @param lessContent The content of the less source
         * @param uri The uri of the less source
         */
        public AmetysLessSource(SourceResolver sourceResolver, String lessContent, URI uri)
        {
            _sResolver = sourceResolver;
            _lessContent = lessContent;
            _sourceUri = uri;
        }
    
        @Override
        public LessSource relativeSource(String relativePath) throws FileNotFound, CannotReadFile, StringSourceException
        {
            try
            {
                URI relativeSourceUri = new URI(relativePath);
                if (!relativeSourceUri.isAbsolute())
                {
                    relativeSourceUri = new URI(FilenameUtils.getFullPath(_sourceUri.toString()) + relativePath);
                }
                
                // SASS files can be .sass or .scss
                Source importSource = null;
                
                importSource = _sResolver.resolveURI(relativeSourceUri.toString());
    
                String importText = IOUtils.toString(importSource.getInputStream(), "UTF-8");
                return new AmetysLessSource(_sResolver, importText, relativeSourceUri);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Unable to process LESS File : " + _sourceUri + ", invalid import : " + relativePath, e);
            }
        }
    
        @Override
        public String getContent() throws FileNotFound, CannotReadFile
        {
            return _lessContent;
        }
    
        @Override
        public byte[] getBytes() throws FileNotFound, CannotReadFile
        {
            try
            {
                return _lessContent.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new CannotReadFile();
            }
        }
        
        @Override
        public URI getURI()
        {
            return _sourceUri;
        }
        
        @Override
        public String getName()
        {
            return _name;
        }
    
    }

    @Override
    public String getMimeType()
    {
        return "text/css";
    }

}
