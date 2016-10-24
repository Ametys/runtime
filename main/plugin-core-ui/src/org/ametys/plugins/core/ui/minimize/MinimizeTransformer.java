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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.transformation.ServiceableTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.ametys.core.DevMode;
import org.ametys.core.cocoon.ImageResourceHandler;

/**
 * This transformer will minimize every scripts together
 */
public class MinimizeTransformer extends ServiceableTransformer implements Contextualizable, Configurable
{
    private static final String DEFAULT_URI_PATTERN = "^/plugins/[^/]+/resources/";
    private static final Pattern IMPORT_WITHOUT_MEDIA_PATTERN = Pattern.compile("^@import\\b\\s*(?:(?:url)?\\(?\\s*[\"']?)([^)\"']*)[\"']?\\)?\\s*;?$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTERNAL_URL = Pattern.compile("^(http[s]?://[^/]+)(/.*)?$");
    
    /* Hash cache */
    private static Map<String, List<FileData>> _hashCache = new HashMap<>();

    /* Cocoon Source Resolver */
    private SourceResolver _resolver;

    /* Configuration */
    private Boolean _debugMode;
    private Context _context;
    private List<Pattern> _patterns;
    private String _locale;
    private Boolean _inlineCssMedias;
    private String _defaultPluginCoreUrl;
    private String _currentContextPath;
    
    /* Dependencies cache */
    private Map<String, Long> _dependenciesCacheValidity;
    private Map<String, List<String>> _dependenciesCache;

    /* Current queue being minified */
    private Map<String, Map<String, String>> _filesQueue;
    private String _queueTag;
    private Set<String> _queueMedia;
    private boolean _isCurrentTagQueued;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _defaultPluginCoreUrl = configuration.getChild("plugin-core-url").getValue("");
        
        Configuration paternsConfig = configuration.getChild("patterns");
        
        _patterns = new ArrayList<>();
        for (Configuration paternConfig : paternsConfig.getChildren("pattern"))
        {
            Pattern pattern = Pattern.compile(paternConfig.getValue());
            _patterns.add(pattern);
        }
        
        if (_patterns.isEmpty())
        {
            _patterns.add(Pattern.compile(DEFAULT_URI_PATTERN));
        }
        
        _inlineCssMedias = Boolean.valueOf(configuration.getChild("inline-css-medias").getValue("false"));
        
        _dependenciesCacheValidity = new HashMap<>();
        _dependenciesCache = new HashMap<>();
    }
    
    @Override
    public void setup(org.apache.cocoon.environment.SourceResolver res, Map obj, String src, Parameters par) throws ProcessingException, SAXException, IOException 
    {
        super.setup(res, obj, src, par);
        
        Request request = ContextHelper.getRequest(_context);
        _locale = request.getLocale().getLanguage();
        _debugMode = DevMode.isDeveloperMode(request);
        _currentContextPath = request.getContextPath();
    }
    
    @Override
    public void startDocument() throws SAXException
    {
        _filesQueue = new LinkedHashMap<>();
        _queueTag = "";
        _isCurrentTagQueued = false;
        _queueMedia = new HashSet<>();
        
        super.startDocument();
    }
    
    @Override
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException
    {
        if (!_debugMode)
        {
            if (_isMinimizable(StringUtils.lowerCase(loc), a))
            {
                _isCurrentTagQueued = true;
                _addToQueue(StringUtils.lowerCase(loc), a);
            }
            else
            {
                _isCurrentTagQueued = false;
                
                if (_filesQueue.size() > 0)
                {
                    _processQueue();
                }
                
                super.startElement(uri, loc, raw, a);
            }
        }
        else
        {
            super.startElement(uri, loc, raw, a);
        }
    }
    
    @Override
    public void endElement(String uri, String loc, String raw) throws SAXException
    {
        if (!_debugMode)
        { 
            if (_isCurrentTagQueued)
            {
                _isCurrentTagQueued = false;
            }
            else
            {
                if (_filesQueue.size() > 0)
                {
                    _processQueue();
                }

                super.endElement(uri, loc, raw);
            }
        }
        else
        {
            super.endElement(uri, loc, raw);
        }
    }
    
    /**
     * Check if the current tag can be minimized by this transformer
     * @param tagName The tag name
     * @param attrs The attribute
     * @return True if minimizable
     */
    private boolean _isMinimizable(String tagName, Attributes attrs)
    {
        if ("true".equals(attrs.getValue("data-donotminimize")))
        {
            return false;
        }
        
        String uri = null;
        
        if ("script".equals(tagName))
        {
            String type = attrs.getValue("type");
            if (StringUtils.isNotEmpty(type) && !"text/javascript".equals(type) && !"application/javascript".equals(type))
            {
                // unsupported script type
                return false;
            }
            
            uri = attrs.getValue("src");
        }
        
        if ("link".equals(tagName))
        {
            String type = attrs.getValue("type");
            if (StringUtils.isNotEmpty(type) && !"text/css".equals(type))
            {
                // unsupported script type
                return false;
            }
            
            String rel = attrs.getValue("rel");
            if (!"stylesheet".equals(rel))
            {
                // unsupported relation type
                return false;
            }
            
            uri = attrs.getValue("href");
        }
        
        if (StringUtils.isNotEmpty(uri))
        {
            uri = FilenameUtils.normalize(uri, true);
            if (StringUtils.startsWith(uri, _currentContextPath))
            {
                uri = StringUtils.removeStart(uri, _currentContextPath);
            }
            
            for (Pattern pattern : _patterns)
            {
                Matcher matcher = pattern.matcher(uri);
                if (matcher.find())
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Add a new tag to the current queue. If the new tag is not compatible with the current queue, it is processed first and emptied before adding the new type of tag.
     * @param tagName The tag name
     * @param attrs The tag attribute
     * @throws SAXException If an error occurred
     */
    private void _addToQueue(String tagName, Attributes attrs) throws SAXException
    {
        Map<String, String> fileOptions = new HashMap<>();
        
        if (_filesQueue.size() > 0)
        {
            // Check if can be added to current queue. If not, process queue before adding.
            boolean sameAsCurrentQueue = tagName.equals(_queueTag);
            
            if (sameAsCurrentQueue && "link".equals(tagName))
            {
                String media = attrs.getValue("media");
                Set<String> medias = _filterMediaValue(media);
                
                if (_inlineCssMedias)
                {
                    // allow different medias in queue, but store the value as file option
                    fileOptions.put("media", StringUtils.join(medias, ","));
                }
                else
                {
                    // media must be the same as current queue
                    sameAsCurrentQueue = medias.equals(_queueMedia);
                }
            }
            
            if (!sameAsCurrentQueue)
            {
                _processQueue();
            }
        }
        
        String uri = FilenameUtils.normalize("script".equals(tagName) ? attrs.getValue("src") : attrs.getValue("href"), true);
        
        if (StringUtils.isNotEmpty(uri))
        {
            if (StringUtils.startsWith(uri, _currentContextPath))
            {
                uri = StringUtils.removeStart(uri, _currentContextPath);
            }
            
            fileOptions.put("tag", tagName);
            _filesQueue.put(uri, fileOptions);
            if (_filesQueue.size() == 1)
            {
                // first item in queue, set the queue attributes for further additions compatibility checks.
                _queueTag = tagName;
                
                if (!_inlineCssMedias && "link".equals(tagName))
                {
                    String media = attrs.getValue("media");
                    _queueMedia = _filterMediaValue(media);
                }
            }
        }
    }
    
    /**
     * Process the current files queue by saxing it as one file, which name is the hash of the sum of files. 
     * This hash is cached, to allow the retrieval of the real files from the hash value.
     * The files parameters, such as the media value of css files, are also stored in the cache.
     * @throws SAXException If an error occurred
     */
    private void _processQueue() throws SAXException
    {
        String hash = _getQueueHash();
        
        AttributesImpl attrs = new AttributesImpl();
        
        if ("script".equals(_queueTag))
        {
            attrs.addCDATAAttribute("type", "text/javascript");
            attrs.addCDATAAttribute("src", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/plugins/core-ui/resources-minimized/" + hash + ".js");
        }
        
        if ("link".equals(_queueTag))
        {
            attrs.addCDATAAttribute("type", "text/css");
            attrs.addCDATAAttribute("rel", "stylesheet");
            attrs.addCDATAAttribute("href", StringUtils.defaultIfEmpty(source, _defaultPluginCoreUrl) + "/plugins/core-ui/resources-minimized/" + hash + ".css");
            if (!_inlineCssMedias && !_queueMedia.isEmpty())  
            {
                String medias = StringUtils.join(_queueMedia, ",");
                if (StringUtils.isNotEmpty(medias))
                {
                    attrs.addCDATAAttribute("media", medias);
                }
            }

        }
        
        super.startElement("", _queueTag, _queueTag, attrs);
        super.endElement("", _queueTag, _queueTag);
        
        _queueTag = null;
        _filesQueue.clear();
    }
    
    private Set<FileData> _getFileDependencies(String cssUri, String media, String tag) throws SAXException
    {
        Set<FileData> dependencies = new LinkedHashSet<>();
        
        Source fileSource;
        Map<String, Object> resolveParameters = new HashMap<>();
        
        try
        {
            String uriToResolve = cssUri;
            URI uri = new URI(cssUri);
            if (!uri.isAbsolute())
            {
                uriToResolve = "cocoon:/" + uriToResolve;
            }
            fileSource = _resolver.resolveURI(uriToResolve, null, resolveParameters);
        }
        catch (Exception e)
        {
            throw new SAXException("Unable to resolve the dependencies of specified uri", e);
        }
        long fileLastModified = resolveParameters.get(ImageResourceHandler.LAST_MODIFIED) != null ? (long) resolveParameters.get("lastModified") : -1;
         
        FileData fileInfos = new FileData(cssUri);
        fileInfos.setLastModified(fileLastModified);
        fileInfos.setMedia(media);
        dependencies.add(fileInfos);
        
        Long validity = _dependenciesCacheValidity.get(cssUri);
        if (validity == null || validity != fileLastModified)
        {
            // cache is outdated
            List<String> dependenciesCache = new ArrayList<>();
            
            if ("link".equals(tag))
            {
                for (FileData cssDependency : _getCssFileDependencies(cssUri, fileSource, tag))
                {
                    if (!dependenciesCache.contains(cssDependency.getUri()))
                    {
                        dependenciesCache.add(cssDependency.getUri());
                        dependencies.add(cssDependency);
                    }
                }
            }
            
            _dependenciesCache.put(cssUri, dependenciesCache);
            _dependenciesCacheValidity.put(cssUri, fileLastModified);
        }
        else
        {
            // cache is up to date
            for (String dependencyCached : _dependenciesCache.get(cssUri))
            {
                dependencies.addAll(_getFileDependencies(dependencyCached, null, tag));
            }
        }
        
        return dependencies;
    }
    
    private List<FileData> _getCssFileDependencies(String uri, Source cssSource, String tag) throws SAXException
    {
        List<FileData> cssDependencies = new ArrayList<>();
        
        String fileContent;
        try (InputStream is = cssSource.getInputStream())
        {
            fileContent = IOUtils.toString(is);
        }
        catch (IOException e)
        {
            throw new SAXException("Unable to retrieve css file dependencies", e);
        }
        
        Matcher urlMatcher = IMPORT_WITHOUT_MEDIA_PATTERN.matcher(fileContent);
        
        while (urlMatcher.find()) 
        {
            String cssUrl = urlMatcher.group(1);
            
            Matcher externalMatcher = EXTERNAL_URL.matcher(cssUrl);
            
            if (!externalMatcher.find())
            {
                try
                {
                    URI cssUri = new URI(cssUrl);
                    if (!cssUri.isAbsolute())
                    {
                        cssUri = new URI(FilenameUtils.getFullPath(uri) + cssUrl);
                    }
                    cssDependencies.addAll(_getFileDependencies(cssUri.normalize().toString(), null, tag));
                }
                catch (URISyntaxException e)
                {
                    // Invalid URI inside a file, but should not be blocking
                    getLogger().warn("Invalid URI in a file, could not calculate dependancies for file : " + uri , e);
                }
            }
        }
        
        return cssDependencies;
    }
    
    private String _getQueueHash() throws SAXException
    {
        List<FileData> hashCache = new ArrayList<>();
        
        for (Entry<String, Map<String, String>> entry : _filesQueue.entrySet())
        {
            String fileUri = entry.getKey();
            Map<String, String> fileOptions = entry.getValue();
            String media = fileOptions.get("media");
            String tag = fileOptions.get("tag");
            
            Set<FileData> fileDependencies = _getFileDependencies(fileUri, media, tag);
            if (fileDependencies != null)
            {
                hashCache.addAll(fileDependencies);
            }
        }
        
        String hash = Base64.getEncoder().encodeToString((String.valueOf(hashCache.hashCode()) + _locale).getBytes());
        _hashCache.put(hash, hashCache);
        return hash;
    }
    
    private Set<String> _filterMediaValue(String mediaValue)
    {
        Set<String> result = new HashSet<>();
        
        if (mediaValue != null)
        {
            for (String media : StringUtils.split(mediaValue, ','))
            {
                result.add(media.trim());
            }
            
            // test for default value
            if (result.size() == 2 && result.contains("print") && result.contains("screen"))
            {
                result.clear();
            }
        }
        
        return result;
    }
    
    /**
     * Retrieve the files list for a given hash
     * @param hash The hash
     * @return The list of files informations
     */
    public static List<FileData> getFilesForHash(String hash)
    {
        return _hashCache.get(hash);
    }
    
    /**
     * The description of a file
     */
    public class FileData
    {
        private String _uri;
        private Long _lastModified;
        private String _media;
        
        /**
         * Default constructor for a file data
         * @param uri The uri locating the file
         */
        public FileData(String uri)
        {
            _uri = uri;
        }
        
        /**
         * Set the last modified value
         * @param lastModified the lastModified to set
         */
        public void setLastModified(Long lastModified)
        {
            this._lastModified = lastModified;
        }

        /**
         * set the medias value
         * @param media the medias to set
         */
        public void setMedia(String media)
        {
            this._media = media;
        }

        /**
         * Get the file uri
         * @return the uri
         */
        public String getUri()
        {
            return _uri;
        }
        
        /**
         * Get the file last modified date
         * @return the lastModified
         */
        public Long getLastModified()
        {
            return _lastModified;
        }
        
        /**
         * Get the file medias
         * @return the medias
         */
        public String getMedia()
        {
            return _media;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof FileData)
            {
                FileData fObj = (FileData) obj;
                return StringUtils.equals(_uri, fObj._uri)
                        && (_lastModified == null ? fObj._lastModified == null : _lastModified.equals(fObj._lastModified)) 
                        && StringUtils.equals(_media, fObj._media);
            }
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(_uri, _lastModified, _media);
        }
        
        @Override
        public String toString()
        {
            if (_media != null)
            {
                return _uri + "#" + _media + " (" + _lastModified + ")";
            }
            else
            {
                return _uri + " (" + _lastModified + ")";
            }
        }
    }
}
