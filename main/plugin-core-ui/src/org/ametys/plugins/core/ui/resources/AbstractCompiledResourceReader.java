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
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.xml.sax.SAXException;

import org.ametys.core.cocoon.RuntimeResourceReader;

/**
 * Abstract reader for resources compiled during runtime, such as SASS or LESS files compiled into CSS.
 */
public abstract class AbstractCompiledResourceReader extends AbstractReader implements CacheableProcessingComponent
{
    /* Dependencies cache */
    private static Map<String, Long> _dependenciesCacheValidity = new HashMap<>();
    private static Map<String, List<String>> _dependenciesCache = new HashMap<>();
    
    /** The initial source resolver */
    protected SourceResolver _resolver;
    
    /** The uri of the file */
    protected String _uri;
    
    private Source _inputSource;
    
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
    
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        _resolver = initalResolver;
        _uri = src;
        
        try 
        {
            _inputSource = initalResolver.resolveURI(src);
        } 
        catch (SourceException e) 
        {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }
        
        super.setup(initalResolver, cocoonObjectModel, src, par);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        if (params != null)
        {
            params.put(RuntimeResourceReader.LAST_MODIFIED, _inputSource.getLastModified());
        }
    }
    
    @Override
    public void generate() throws IOException, ProcessingException
    {
        if (!_inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
        }
        
        try
        {
            String compiledResource = compileResource(_inputSource);
            IOUtils.write(compiledResource, out, "UTF-8");
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
    
    public Serializable getKey()
    {
        return _inputSource != null ? _inputSource.getURI() : null;
    }

    public SourceValidity getValidity()
    {
        Long lastModified = getCalculatedLastModified(_inputSource, _uri, _inputSource.getLastModified());
        return lastModified != null ? new TimeStampValidity(lastModified) : null;
    }

    private Long getCalculatedLastModified(Source inputSource, String sourceUri, long lastModified)
    {
        long result = lastModified;
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
        
        for (String dependency : dependencies)
        {
            if (dependency != null && !StringUtils.startsWith(dependency, "http://") && !StringUtils.startsWith(dependency, "https://"))
            {
                try
                {
                    URI uri = new URI(dependency);
                    String uriToResolve = uri.isAbsolute() ? dependency : FilenameUtils.getFullPath(sourceUri) + dependency;
                    
                    // Don't normalize the schema part of the uri
                    String schema = StringUtils.contains(uriToResolve, "://") ? uriToResolve.substring(0, uriToResolve.indexOf("://") + 3) : "";                    
                    uriToResolve = schema + FilenameUtils.normalize(StringUtils.removeStart(uriToResolve, schema));
                    
                    HashMap<String, Object> params = new HashMap<>();
                    Source dependencySource = _resolver.resolveURI(uriToResolve, null, params);
                    Long calculatedLastModified = getCalculatedLastModified(dependencySource, uriToResolve, dependencySource.getLastModified());
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
}
