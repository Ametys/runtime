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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.xml.sax.SAXException;

import org.ametys.core.cocoon.AbstractResourceHandler;
import org.ametys.core.cocoon.ImageResourceHandler;

/**
 * Abstract reader for resources compiled during runtime, such as SASS or LESS files compiled into CSS.
 */
public abstract class AbstractCompiledResourceHandler extends AbstractResourceHandler implements Component
{
    /* Dependencies cache */
    private static Map<String, Long> _dependenciesCacheValidity = new HashMap<>();
    private static Map<String, List<String>> _dependenciesCache = new HashMap<>();
    
    /** The initial source resolver */
    protected SourceResolver _sourceResolver;
    
    /** The uri of the file */
    protected String _uri;
    
    /**
     * Compile the current resource, and returns its value.
     * @param resource The current resource
     * @return The compiled value
     * @throws IOException If an IO error occurs
     * @throws ProcessingException If a processing error occurs 
     */
    public abstract String compileResource(Source resource) throws IOException, ProcessingException;

    /**
     * Calculate the list of dependencies for the given source, for validity calculations.
     * @param inputSource The source
     * @return The list of uri
     */
    protected abstract List<String> getDependenciesList(Source inputSource);
    
    /**
     * Initialize the resource handler
     * @param initalResolver The resolver
     * @param cocoonObjectModel The object model
     * @param src The source
     * @throws ProcessingException If an error occurs
     * @throws IOException If an error occurs
     */
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws IOException, ProcessingException, SAXException
    {
        _sourceResolver = initalResolver;
        _uri = src;
        
        try 
        {
            _inputSource = initalResolver.resolveURI(src);
        } 
        catch (SourceException e) 
        {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }
        
        if (!_inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
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
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            String compiledResource = compileResource(_inputSource);
            IOUtils.write(compiledResource, out, "UTF-8");
        }
    }
    
    public Serializable getKey()
    {
        return getDependenciesKeys(_inputSource, _uri, FilenameUtils.normalize(_inputSource.getURI()), _inputSource.getLastModified(), new HashMap<String, String>());
    }

    public SourceValidity getValidity()
    {
        Long lastModified = getCalculatedLastModified(_inputSource, _uri, _inputSource.getLastModified(), new HashMap<String, String>());
        return lastModified != null ? new TimeStampValidity(lastModified) : null;
    }

    private Long getCalculatedLastModified(Source inputSource, String sourceUri, long lastModified, HashMap<String, String> knowDependencies)
    {
        long result = lastModified;
        List<String> dependencies = _getDependencies(inputSource, sourceUri, lastModified);
        
        for (String dependency : dependencies)
        {
            if (dependency != null && !StringUtils.startsWith(dependency, "http://") && !StringUtils.startsWith(dependency, "https://"))
            {
                try
                {
                    String uriToResolve = _getDependencyURI(sourceUri, dependency);
                    
                    HashMap<String, Object> params = new HashMap<>();
                    Source dependencySource = _sourceResolver.resolveURI(uriToResolve, null, params);
                    String fsURI = FilenameUtils.normalize(dependencySource.getURI());
                    if (knowDependencies.containsKey(fsURI))
                    {
                        // error already logged by the getKey()
                        return null;
                    }
                    knowDependencies.put(fsURI, sourceUri);
                    
                    Long calculatedLastModified = getCalculatedLastModified(dependencySource, uriToResolve, dependencySource.getLastModified(), knowDependencies);
                    if (calculatedLastModified != null && calculatedLastModified > result)
                    {
                        result = calculatedLastModified;
                    }
                }
                catch (Exception e)
                {
                    getLogger().warn("Unable to resolve the following uri : '" + dependency + "' while calculating dependencies for " + _inputSource.getURI(), e);
                    return null;
                }
            }
        }
        
        return result;
    }
    
    private String getDependenciesKeys(Source inputSource, String sourceUri, String fileURI, long lastModified, HashMap<String, String> knowDependencies)
    {
        String result = fileURI;
        List<String> dependencies = _getDependencies(inputSource, sourceUri, lastModified);
        
        for (String dependency : dependencies)
        {
            if (dependency != null && !StringUtils.startsWith(dependency, "http://") && !StringUtils.startsWith(dependency, "https://"))
            {
                try
                {
                    String uriToResolve = _getDependencyURI(sourceUri, dependency);
                    
                    HashMap<String, Object> params = new HashMap<>();
                    Source dependencySource = _sourceResolver.resolveURI(uriToResolve, null, params);
                    String fileDependencyURI = FilenameUtils.normalize(dependencySource.getURI());
                    if (knowDependencies.containsKey(fileDependencyURI))
                    {
                        getLogger().error("A loop import was detected in a SASS file : '" + sourceUri + "' tried to import '" + fileDependencyURI 
                                + "' but it was already previously imported by '" + knowDependencies.get(fileDependencyURI) + "'.");
                        return null;
                    }
                    knowDependencies.put(fileDependencyURI, sourceUri);
                    String dependenciesKeys = getDependenciesKeys(dependencySource, uriToResolve, fileDependencyURI, dependencySource.getLastModified(), knowDependencies);
                    if (dependenciesKeys != null)
                    {
                        result = result + "#" + dependenciesKeys;
                    }
                }
                catch (Exception e)
                {
                    getLogger().warn("Unable to resolve the following uri : '" + dependency + "' while calculating dependencies for " + _inputSource.getURI(), e);
                    return null;
                }
            }
        }
        
        return result;
    }
    
    private String _getDependencyURI(String sourceUri, String dependency) throws URISyntaxException
    {
        URI uri = new URI(dependency);
        String uriToResolve = uri.isAbsolute() ? dependency : FilenameUtils.getFullPath(sourceUri) + dependency;
        
        // Don't normalize the schema part of the uri
        String schema = StringUtils.contains(uriToResolve, "://") ? uriToResolve.substring(0, uriToResolve.indexOf("://") + 3) : "";                    
        uriToResolve = schema + FilenameUtils.normalize(StringUtils.removeStart(uriToResolve, schema));
        return uriToResolve;
    }

    private List<String> _getDependencies(Source inputSource, String sourceUri, long lastModified)
    {
        Long cachedValidity = _dependenciesCacheValidity.get(sourceUri);
        
        List<String> dependencies;
        
        if (cachedValidity == null || !cachedValidity.equals(lastModified))
        {
            // Cache is out of date
            dependencies = getDependenciesList(inputSource);
            _dependenciesCacheValidity.put(sourceUri, lastModified);
            _dependenciesCache.put(sourceUri, dependencies);
        }
        else
        {
            dependencies = _dependenciesCache.get(sourceUri);
        }
        return dependencies;
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
