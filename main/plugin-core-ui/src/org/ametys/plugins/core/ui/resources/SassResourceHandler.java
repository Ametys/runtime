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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.xml.sax.SAXException;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;

/**
 * Reader for SASS files, compile them on the fly into CSS files.
 */
public class SassResourceHandler extends AbstractCompiledResourceHandler
{
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*@import\\s+(?:(?:url)?\\(?\\s*[\"']?)([^)\"']+)[\"']?\\)?\\s*;?\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private Compiler _jsassCompiler;

    private AmetysSASSHelper _ametysSASSHelper;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        
        _ametysSASSHelper = (AmetysSASSHelper) serviceManager.lookup(AmetysSASSHelper.ROLE);
    }
    
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, IOException, SAXException
    {
        super.setup(initalResolver, cocoonObjectModel, src, par);
        
        _jsassCompiler = new Compiler();
    }

    @Override
    public String compileResource(Source resource) throws IOException, ProcessingException
    {
        Output compiledString = null;
        Options options = new Options();
        AmetysSassImporter sassImporter = new AmetysSassImporter(_sourceResolver);
        options.getImporters().add(sassImporter);
        options.getFunctionProviders().add(_ametysSASSHelper);
        
        try (InputStream is = resource.getInputStream())
        {
            URI uri = new URI(_uri);
            sassImporter.registerValidURI(uri.toString());
            String sassContent = IOUtils.toString(is);
            compiledString = _jsassCompiler.compileString(sassContent, uri, uri, options);
        }
        catch (CompilationException | URISyntaxException e)
        {
            throw new ProcessingException("Unable to compile the SASS file: " + _uri, e);
        }
        
        return compiledString.getCss();
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
                    if (!StringUtils.endsWith(cssUrl, ".css") && !StringUtils.endsWith(cssUrl, ".scss") && !StringUtils.endsWith(cssUrl, ".sass"))
                    {
                        cssUrl += ".scss";
                    }
                    
                    result.add(cssUrl);
                }
            }
        }
        catch (IOException e)
        {
            getLogger().warn("Invalid " + inputSource.getURI(), e);
        }
        
        return result;
    }
    
    @Override
    public String getMimeType()
    {
        return "text/css";
    }

    /**
     * Sass Importer which can resolve Ametys resources
     */
    private class AmetysSassImporter implements Importer 
    {
        private SourceResolver _sResolver;
        
        private Map<String, String> _validURIsRegistered;
    
        /**
         * Default constructor for the Ametys SassImporter. Provides information for resolving imported resources.
         * @param sourceResolver The source resolver
         */
        public AmetysSassImporter(SourceResolver sourceResolver)
        {
            _sResolver = sourceResolver;
            _validURIsRegistered = new HashMap<>();
        }
        
        /**
         * Because Libsass does not fully respect URI schemes and can sometimes corrupt URIs, this method can be used to provide a URI known as valid.
         * When resolving the URI received from Libsass, it will first be checked against valid URIs, to prevent common corruption.
         * Example of known URI corruption : scheme of URI "plugin:*://" will be transformed into "plugin:*:/"  
         * @param validUri A valid URI
         */
        public void registerValidURI(String validUri)
        {
            if (StringUtils.contains(validUri, "://"))
            {
                String schema =  validUri.substring(0, validUri.indexOf("://"));
                String corruptedUri = schema + ":/" + StringUtils.removeStart(validUri, schema + "://");
                _validURIsRegistered.put(corruptedUri, validUri);
            }
        }
        
        public Collection<Import> apply(String url, Import previous)
        {
            List<Import> list = new LinkedList<>();
            URI currentUri = null;
            
            try
            {
                URI importUrl = new URI(url);
                if (importUrl.isAbsolute())
                {
                    currentUri = importUrl;
                    this.registerValidURI(currentUri.toString());
                }
                else
                {
                    if (_validURIsRegistered.containsKey(previous.getAbsoluteUri().toString()))
                    {
                        // URI replaced with the registered matching valid URI, because of potential URI corruption.
                        currentUri = new URI(FilenameUtils.getFullPath(_validURIsRegistered.get(previous.getAbsoluteUri().toString())) + url);
                    }
                    else
                    {
                        currentUri = new URI(FilenameUtils.getFullPath(previous.getAbsoluteUri().toString()) + url);
                    }
                }
                
                Source importSource = _getImportSource(currentUri.toString());
    
                if (importSource.getURI().endsWith(".scss") || importSource.getURI().endsWith(".sass"))
                {
                    String importText = IOUtils.toString(importSource.getInputStream(), "UTF-8");
                    list.add(new Import(currentUri, currentUri, importText));
                }
                else
                {
                    // returning null keeps the original @import mention
                    return null;
                }
            }
            catch (URISyntaxException e) 
            {
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
    
            return list;
        }

        private Source _getImportSource(String currentUri) throws URISyntaxException, IOException
        {
            List<String> uriMatching = new ArrayList<>();
            uriMatching.add(currentUri);
            
            // extension is optional
            uriMatching.add(currentUri + ".scss");
            uriMatching.add(currentUri + ".sass");
            
            // add an underscore prefix to the file name for sass partial imports
            String name = FilenameUtils.getName(currentUri);
            String partialUri = currentUri.substring(0, currentUri.length() - name.length()) + "_" + name;
            uriMatching.add(partialUri);
            uriMatching.add(partialUri + ".scss");
            uriMatching.add(partialUri + ".sass");
            
            for (String uri : uriMatching)
            {
                try
                {
                    Source importSource = _sResolver.resolveURI(uri);
                    if (importSource.exists())
                    {
                        return importSource;
                    }
                }
                catch (SourceNotFoundException e)
                {
                    // source does not exists. Do nothing
                }
            }
            
            throw new URISyntaxException(currentUri, "Unable to resolve SASS import, no matching source found");
        }
    }
}
