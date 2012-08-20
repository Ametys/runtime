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
package org.ametys.runtime.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.jaxp.JAXPConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.StringUtils;

/**
 * The PluginManager is in charge to load and initialize plugins. <br>
 * It gives access to the extension points.
 */
public final class PluginsManager
{
    /** Separator between pluginName and featureName */
    public static final String FEATURE_ID_SEPARATOR = "/";
    
    /** The regexp to determine if a plugin name is correct (add ^ and $ as delimiters if this is your only test) */
    public static final String PLUGIN_NAME_REGEXP = "\\w[\\w-_\\.]*\\w?";
    
    private static final Pattern __FEATURE_ID_PATTERN = Pattern.compile("([^/]*/)?[^/]*");

    // shared instance
    private static PluginsManager __manager;

    // Plugin filename
    private static final String __PLUGIN_FILENAME = "plugin.xml";

    // plugins
    private Set<String> _pluginNames;
    
    // associations plugins/resourcesURI
    private Map<String, String> _baseURIs;
    
    // associations plugins/locations
    private Map<String, String> _locations;
    
    // Active plugins list
    private Map<String, ActiveFeature> _activeFeatures;

    // Inactive plugins list
    private Map<String, InactiveFeature> _inactiveFeatures;
    
    // Single extension points' roles
    private Collection<String> _singleExtensionPointsRoles;

    // Extension points' roles
    private Collection<String> _extensionPointsRoles;
    
    // Entity resolver for resolving XML schemas
    private LocalEntityResolver _entityResolver;

    // Logger for traces
    private Logger _logger = LoggerFactory.getLoggerFor(PluginsManager.class);


    private PluginsManager()
    {
        // empty constructor
    }
    
    /**
     * Returns the names of the plugins
     * @return the names of the plugins
     */
    public Set<String> getPluginNames()
    {
        return Collections.unmodifiableSet(_pluginNames);
    }
    
    /**
     * Returns a String array containing the ids of the plugins embedded in jars
     * @return a String array containing the ids of the plugins embedded in jars
     */
    public Set<String> getEmbeddedPluginsIds()
    {
        return Collections.unmodifiableSet(_baseURIs.keySet());
    }

    /**
     * Returns the shared instance of the <code>PluginManager</code>
     * @return the shared instance of the PluginManager
     */
    public static PluginsManager getInstance()
    {
        if (__manager == null)
        {
            __manager = new PluginsManager();
        }

        return __manager;
    }

    /**
     * Returns all active features.
     * @return a never null map containing all active features
     */
    public Map<String, ActiveFeature> getActiveFeatures()
    {
        return Collections.unmodifiableMap(_activeFeatures);
    }
    
    private ActiveFeature _getActiveFeature(String pluginName, String featureName)
    {
        String featureId = pluginName + FEATURE_ID_SEPARATOR + featureName;
        if (_activeFeatures.containsKey(featureId))
        {
            return _activeFeatures.get(featureId);
        }
        else
        {
            ActiveFeature feature = new ActiveFeature(pluginName, featureName);
            _activeFeatures.put(featureId, feature);
            return feature;
        }
    }

    /**
     * Returns all inactive features.
     * @return a never null Collection containing all inactive features
     */
    public Map<String, InactiveFeature> getInactiveFeatures()
    {
        return Collections.unmodifiableMap(_inactiveFeatures);
    }
    
    /**
     * Returns the roles of the loaded single extensions points
     * @return the roles of the loaded single extensions points
     */
    public Collection<String> getSingleExtensionPoints()
    {
        return Collections.unmodifiableCollection(_singleExtensionPointsRoles);
    }
    
    /**
     * Returns the roles of the loaded extensions points
     * @return the roles of the loaded extensions points
     */
    public Collection<String> getExtensionPoints()
    {
        return Collections.unmodifiableCollection(_extensionPointsRoles);
    }
    
    /**
     * Returns the base URI for the given plugin resources, or null if default one (ie in the file system)
     * @param pluginName the name of the plugin
     * @return the base URI for the given plugin resources, or null if default one (ie in the file system)
     */
    public String getBaseURI(String pluginName)
    {
        String pluginUri = _baseURIs.get(pluginName);
        if (pluginUri == null)
        {
            return null;
        }
        
        return "resource:/" + pluginUri; 
    }
    
    /**
     * Returns the plugin location for the given plugin, ie its base path relative to the contextPath, or null if the plugin is loaded from the classpath.
     * @param pluginName the plugin name
     * @return the plugin location for the given plugin, ie its base path relative to the contextPath
     */
    public String getPluginLocation(String pluginName)
    {
        return _locations.get(pluginName);
    }
    
    // Look for plugins embedded in jars
    // They have a META-INF/runtime-plugin plain text file containing plugin name and path to plugin.xml
    private void _initBaseURIs() throws IOException
    {
        Enumeration<URL> pluginResources = getClass().getClassLoader().getResources("META-INF/runtime-plugin");
        
        while (pluginResources.hasMoreElements())
        {
            URL pluginResource = pluginResources.nextElement();
            BufferedReader br = new BufferedReader(new InputStreamReader(pluginResource.openStream(), "UTF-8"));
            
            try
            {
                String pluginName = br.readLine();            
                String pluginResourceURI = br.readLine();
                
                if (getClass().getResource(pluginResourceURI + "/" + __PLUGIN_FILENAME) != null)
                {
                    _baseURIs.put(pluginName, pluginResourceURI);
                }
                else if (_logger.isWarnEnabled())
                {
                    _logger.warn("A plugin '" + pluginName + "' is declared in a library, but no file '" + __PLUGIN_FILENAME + "' can be found at '" + pluginResourceURI + "'. It will be ignored.");
                }

            }
            finally
            {
                IOUtils.closeQuietly(br);
            }
        }
    }
    
    // Look for XML schemas embedded in jars
    // They have a META-INF/runtime-plugin plain text file containing schema identifier and path to the actual XSD file
    private void _initSchemas(String contextPath, Collection<String> locations) throws IOException
    {
        // Embedded schemas
        Enumeration<URL> shemasResources = getClass().getClassLoader().getResources("META-INF/runtime-schema");
        while (shemasResources.hasMoreElements())
        {
            URL shemasResource = shemasResources.nextElement();
            BufferedReader br = new BufferedReader(new InputStreamReader(shemasResource.openStream(), "UTF-8"));
            
            try
            {
                String systemId = br.readLine();            
                String schemaResourceURI = br.readLine();
                
                if (getClass().getResource(schemaResourceURI) != null)
                {
                    _entityResolver.addEmbeddedSchema(systemId, schemaResourceURI);
                }
                else if (_logger.isWarnEnabled())
                {
                    _logger.warn("A schema is declared in a library, but no file can be found at '" + schemaResourceURI + "'. It will be ignored.");
                }
            }
            finally
            {
                IOUtils.closeQuietly(br); 
            }
        }

        // Local schemas
        File kernelBase = new File(contextPath, "kernel");
        if (kernelBase.exists() && kernelBase.isDirectory())
        {
            _findAndAddSchema(kernelBase);
        }
        
        for (String location : locations)
        {
            File locationBase = new File(contextPath, location);

            if (locationBase.exists() && locationBase.isDirectory())
            {
                File[] pluginDirs = locationBase.listFiles(new FileFilter() 
                {
                    public boolean accept(File pathname)
                    {
                        return pathname.isDirectory();
                    }
                });
                
                for (File pluginDir : pluginDirs)
                {
                    _findAndAddSchema(pluginDir);
                }
            }
        }
    }

    /**
     * Search in root files of the given directory. If a .xsd file is found it will be added to known schemas using the namespace http://www.ametys.org/schema/FILENAME.xsd
     * @param dir The directory to inspect.
     */
    private void _findAndAddSchema(File dir)
    {
        File[] schemaFiles = dir.listFiles(new FileFilter() 
        {
            public boolean accept(File pathname)
            {
                return pathname.isFile() && pathname.getName().endsWith(".xsd");
            }
        });
        
        if (schemaFiles != null && schemaFiles.length > 0)
        {
            for (File schemaFile : schemaFiles)
            {
                // Local schema convention is to put a myschema-1.0.xsd file into
                // the plugin directory in order to declare the schema of namespace
                // http://www.ametys.org/schema/myschema with the schema location
                // http://www.ametys.org/schema/myschema-1.0.xsd
                _entityResolver.addLocalSchema("http://www.ametys.org/schema/" + schemaFile.getName(), schemaFile);
            }
        }
    }
    
    /**
     * Initialization of the plugin manager
     * @param manager the PluginsServiceManager dedicated to manage plugins-defined components
     * @param context the Avalon context
     * @param contextPath the Web context path on the server filesystem
     * @return all relevant information about loaded features or null if the application is not correctly configured
     * @throws ComponentException if something wrong occurs during loading of a component
     */
    public Map<String, FeatureInformation> init(PluginsComponentManager manager, Context context, String contextPath) throws ComponentException
    {
        _baseURIs = new HashMap<String, String>();
        _inactiveFeatures = new HashMap<String, InactiveFeature>();
        _activeFeatures = new HashMap<String, ActiveFeature>();
        _locations = new HashMap<String, String>();
        _entityResolver = new LocalEntityResolver();
        
        // Embedded plugins locations
        try
        {
            _initBaseURIs();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to locate embedded plugins", e);
        }
        
        // Plugins root directories (directories containing plugins directories)
        Collection<String> locations = RuntimeConfig.getInstance().getPluginsLocations();
        
        // Schemas locations
        try
        {
            _initSchemas(contextPath, locations);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to locate XML schemas", e);
        }
        
        // All plugin.xml Configurations
        Map<String, Configuration> pluginsConfigurations = _getConfigurations(contextPath, locations);
        
        _pluginNames = pluginsConfigurations.keySet();
        
        // Single extension points declared by plugins
        Map<String, SingleExtensionPointInformation> singleExtensionsPoints = _getSingleExtensionsPoints(pluginsConfigurations);
        _singleExtensionPointsRoles = singleExtensionsPoints.keySet();
        
        // List of chosen extension points among single extension points
        Map<String, String> extensionsConfig = RuntimeConfig.getInstance().getExtensionsPoints();
        
        // Check if all single extension point have a valid extension
        _checkDefaultSingleExtensionsPoints(singleExtensionsPoints, extensionsConfig);
        
        // List of manually excluded features
        Collection<String> excludedFeatures = RuntimeConfig.getInstance().getExcludedFeatures();

        // Active features configurations
        Map<String, FeatureInformation> featuresInformations = _getActiveFeaturesInformations(pluginsConfigurations, singleExtensionsPoints, extensionsConfig, excludedFeatures);
        
        // Handling of features dependencies
        _checkFeaturesDependencies(featuresInformations);
        featuresInformations = _computeFeaturesDependencies(featuresInformations);
        
        // Config loading
        ConfigManager configManager = ConfigManager.getInstance();
        
        try
        {
            configManager.service(new WrapperServiceManager(manager));
            configManager.initialize();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception while setting up ConfigManager", e);
        }
        
        try
        {
            // Global config parameter loading
            for (String pluginName : pluginsConfigurations.keySet())
            {
                Configuration conf = pluginsConfigurations.get(pluginName);
                configManager.addGlobalConfig(pluginName, conf);
            }
            
            // "local" config parameter loading
            for (String featureId : featuresInformations.keySet())
            {
                FeatureInformation info = featuresInformations.get(featureId);
                configManager.addConfig(info.getPluginName(), info.getFeatureName(), info.getConfiguration());
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException("Exception while reading Config configuration", e);
        }
        
        // check if the config is complete and valid
        configManager.validate();
        
        if (!configManager.isComplete())
        {
            return null;
        }
        
        // Avalon components and single extension point loading
        try
        {
            _loadComponents(manager, featuresInformations, contextPath);
            Collection<String> loadedSingleExtensionsPoints = _loadSingleExtensionsPoints(manager, featuresInformations, singleExtensionsPoints, extensionsConfig, contextPath);
            _checkSingleExtensionsPoints(loadedSingleExtensionsPoints, singleExtensionsPoints, extensionsConfig);
            _loadRuntimeInit(manager);
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException("Exception while loading components", e);
        }
        
        // List of declared extension points
        _extensionPointsRoles = _getExtensionsPoints(pluginsConfigurations, manager, contextPath);
        
        return featuresInformations;
    }
    
    /**
     * Second and last part of initialization : loading of extensions.<br>
     * @param manager the PluginsServiceManager used to register new components
     * @param info all relevant information about features, provided by init()
     * @param contextPath the Web context path on the server filesystem
     * @throws Exception if something wrong occurs 
     */
    public void initExtensions(PluginsComponentManager manager, Map<String, FeatureInformation> info, String contextPath) throws Exception
    {
        Map<String, ExtensionPoint> extPoints = new HashMap<String, ExtensionPoint>();
        for (String role : _extensionPointsRoles)
        {
            ExtensionPoint extPoint = (ExtensionPoint) manager.lookup(role);
            extPoints.put(role, extPoint);
        }
        
        // Active features loading
        _loadFeatures(extPoints, info, contextPath);
    }
    
    private Map<String, Configuration> _getConfigurations(String contextPath, Collection<String> locations)
    {
        Map<String, Configuration> pluginsConfigurations = new HashMap<String, Configuration>();
        
        // Embedded plugins configurations loading
        for (String pluginName : _baseURIs.keySet())
        {
            String resourceURI = _baseURIs.get(pluginName) + "/" + __PLUGIN_FILENAME;
            InputStream is = getClass().getResourceAsStream(resourceURI);
            Configuration configuration = _getConfigurationFromStream(is, "resource:/" + resourceURI);
            
            pluginsConfigurations.put(pluginName, configuration);
            
            if (_logger.isInfoEnabled())
            {
                _logger.info("Plugin '" + pluginName + "' added at path 'resource:/" + resourceURI + "'");
            }
        }
        
        // Other plugins configuration loading
        for (String location : locations)
        {
            File locationBase = new File(contextPath, location);

            if (locationBase.exists() && locationBase.isDirectory())
            {
                File[] pluginDirs = locationBase.listFiles(new FileFilter() 
                {
                    public boolean accept(File pathname)
                    {
                        return pathname.isDirectory();
                    }
                });
                
                for (File pluginDir : pluginDirs)
                {
                    _addPluginConfiguration(pluginsConfigurations, location, pluginDir);
                }
            }
        }
        
        return pluginsConfigurations;
    }
    
    private void _addPluginConfiguration(Map<String, Configuration> pluginsConfigurations, String location, File pluginDir)
    {
        String pluginName = pluginDir.getName();
        File pluginFile = new File(pluginDir, __PLUGIN_FILENAME);
        
        if (!pluginFile.exists())
        {
            // Ignore CVS and .svn
            if (pluginDir.getName().equals("CVS") || pluginDir.getName().equals(".svn"))
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("There is no file named " + __PLUGIN_FILENAME + " in the directory " + pluginDir.getAbsolutePath() + ". It will be ignored.");
                }
            }
            else
            {
                if (_logger.isWarnEnabled())
                {
                    _logger.warn("There is no file named " + __PLUGIN_FILENAME + " in the directory " + pluginDir.getAbsolutePath() + ". It will be ignored.");
                }
            }
        }
        else
        {
            if (!pluginName.matches("^" + PLUGIN_NAME_REGEXP + "$"))
            {
                if (_logger.isWarnEnabled())
                {
                    _logger.warn(pluginName + " is an incorrect plugin directory name. It will be ignored.");
                }
            }
            else if (pluginsConfigurations.containsKey(pluginName))
            {
                if (_logger.isWarnEnabled())
                {
                    _logger.warn("The plugin " + pluginName + " at " + pluginFile.getAbsolutePath() + " is already declared. It will be ignored.");
                }
            }
            else
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("Reading plugin configuration at " + pluginFile.getAbsolutePath());
                }

                InputStream is = null;
                
                try
                {
                    is = new FileInputStream(pluginFile);
                }
                catch (FileNotFoundException e)
                {
                    // Should not happen, as existence is checked before
                    throw new IllegalStateException("File not found", e);
                }
                
                Configuration configuration = _getConfigurationFromStream(is, pluginFile.getAbsolutePath());

                pluginsConfigurations.put(pluginName, configuration);
                
                _locations.put(pluginName, location);
                
                if (_logger.isInfoEnabled())
                {
                    _logger.info("Plugin '" + pluginName + "' added at path '" + pluginFile.getAbsolutePath() + "'");
                }
            }
        }
    }

    private Configuration _getConfigurationFromStream(InputStream is, String path)
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.W3C_XML_SCHEMA);
            reader.setEntityResolver(_entityResolver);
            DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);
            
            return confBuilder.build(is, path);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to access to plugin at " + path, e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Exception closing stream : " + path, e);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> _getExtensionsPoints(Map<String, Configuration> pluginsConfigurations, PluginsComponentManager manager, String contextPath) throws ComponentException
    {
        Collection<String> extPoints = new ArrayList<String>();
        
        for (String pluginName : pluginsConfigurations.keySet())
        {
            Configuration configuration = pluginsConfigurations.get(pluginName);
            Configuration extPointsConf = configuration.getChild("extension-points");
            
            Configuration[] extPointConf = extPointsConf.getChildren("extension-point");
            
            for (Configuration conf : extPointConf)
            {
                String id = conf.getAttribute("id", null);
                String clazz = conf.getAttribute("class", null);
                
                if (id == null)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("In plugin '" + pluginName + "', an extension point has no \"id\" attribute. It will be ignored.");
                    }
                }
                else if (clazz == null)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("In plugin '" + pluginName + "', the extension point '" + id + "' miss the \"class\" attribute. It will be ignored.");
                    }
                }
                else if (_singleExtensionPointsRoles.contains(id) || extPoints.contains(id))
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The extension point '" + id + "' (in the plugin '" + pluginName + "') is already defined. It will be ignored.");
                    }
                }
                else
                {
                    try
                    {
                        Configuration realComponentConf = _getComponentConfiguration(conf, contextPath, pluginName);
                        
                        Class c = Class.forName(clazz);
                        manager.addComponent(pluginName, null, id, c, realComponentConf);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new IllegalArgumentException("Unable to load class '" + clazz + "' for extension point '" + id + "' in plugin " + pluginName, e);
                    }
                    catch (ConfigurationException e)
                    {
                        throw new IllegalArgumentException("Unable to load configuration for extension point '" + id + "' in plugin " + pluginName, e);
                    }
                    
                    extPoints.add(id);
                }
            }
        }
        
        return extPoints;
    }
    
    private Map<String, SingleExtensionPointInformation> _getSingleExtensionsPoints(Map<String, Configuration> pluginsConfigurations)
    {
        try
        {
            Map<String, SingleExtensionPointInformation> extPoints = new HashMap<String, SingleExtensionPointInformation>();
            
            for (String pluginName : pluginsConfigurations.keySet())
            {
                Configuration configuration = pluginsConfigurations.get(pluginName);
                Configuration extPointsConf = configuration.getChild("extension-points");
                
                Configuration[] extPointConf = extPointsConf.getChildren("single-extension-point");
                
                for (Configuration conf : extPointConf)
                {
                    String id = conf.getAttribute("id", null);
                    String clazz = conf.getAttribute("class", null);
                    String defaultExtensionId = conf.getAttribute("default-extension-id", null);
                    
                    if (id == null)
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("In plugin '" + pluginName + "', a single extension point miss the \"id\" attribute. It will be ignored.");
                        }
                    }
                    else if (clazz == null)
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("In plugin '" + pluginName + "', the single extension point '" + id + "' miss the \"class\" attribute. It will be ignored.");
                        }
                    }
                    else if (extPoints.containsKey(id))
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("The single extension point '" + id + "' (in the plugin '" + pluginName + "') is already defined. It will be ignored.");
                        }
                    }
                    else 
                    {
                        Class c = Class.forName(clazz);
                        
                        extPoints.put(id, new SingleExtensionPointInformation(c, defaultExtensionId));
                    }
                }
            }
            
            return extPoints;
        }
        catch (ClassNotFoundException e)
        {
            _logger.error("Unable to load a single extension point class", e);
            throw new IllegalArgumentException("Unable to load a single extension point class", e);
        }
    }
    
    private void _checkDefaultSingleExtensionsPoints(Map<String, SingleExtensionPointInformation> singleExtensionsPoints, Map<String, String> extensionsConfig)
    {
        for (String extensionPointRole : singleExtensionsPoints.keySet())
        {
            if (!extensionsConfig.containsKey(extensionPointRole))
            {
                SingleExtensionPointInformation info = singleExtensionsPoints.get(extensionPointRole);
                String defaultExtensionid = info.getDefaultExtensionId();
                
                if (defaultExtensionid != null)
                {
                    extensionsConfig.put(extensionPointRole, defaultExtensionid);
                }
                else
                {
                    String errorMessage = "No extension available for the single extension point '" + extensionPointRole + "' : none has been selected in the WEB-INF/param/runtime.xml, and the extension point does not have a default extension id.";
                    _logger.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
            }
        }
    }
    
    private Map<String, FeatureInformation> _getActiveFeaturesInformations(Map<String, Configuration> pluginsConfigurations, Map<String, SingleExtensionPointInformation> extensionsPoints, Map<String, String> extensionsConfig, Collection<String> excludedFeatures)
    {
        Map<String, FeatureInformation> featuresInformations = new HashMap<String, FeatureInformation>();
        
        // internal Map to check unicity of extensions ids
        Map<String, Collection<String>> extensionsIds = new HashMap<String, Collection<String>>();
        
        for (String pluginName : pluginsConfigurations.keySet())
        {
            Configuration[] pluginsConf = pluginsConfigurations.get(pluginName).getChildren("feature");
            
            // Loop on each plugin Configuration
            for (Configuration conf : pluginsConf)
            {
                String name = conf.getAttribute("name", null);
                
                if (name == null)
                {
                    _logger.error("The plugin '" + pluginName + "' defines a feature without the mandatory \"name\" attribute. It will be ignored.");
                }
                else if (!name.matches("[a-zA-Z0-9]|([a-zA-Z0-9][a-zA-Z0-9-_.]*[a-zA-Z0-9])"))
                {
                    _logger.error(name + " is an incorrect feature name. It will be ignored.");
                }
                else if (featuresInformations.containsKey(pluginName + FEATURE_ID_SEPARATOR + name))
                {
                    _logger.error("The feature " + name + " in the plugin " + pluginName + " is already declared. It will be ignored.");
                }
                else
                {
                    _checkExtensionsIds(extensionsIds, conf, pluginName);
                    
                    String featureId = pluginName + FEATURE_ID_SEPARATOR + name;
                    
                    if (excludedFeatures.contains(featureId))
                    {
                        _inactiveFeatures.put(featureId, new InactiveFeature(pluginName, name, InactivityCause.EXCLUDED));
                    }
                    else if (!_includePlugin(conf, extensionsPoints, extensionsConfig))
                    {
                        _inactiveFeatures.put(featureId, new InactiveFeature(pluginName, name, InactivityCause.SINGLE));
                    }
                    else
                    {
                        FeatureInformation info = new FeatureInformation(pluginName, name, conf);
                        featuresInformations.put(info.getFeatureId(), info);
                    }
                }
            }
        }
        
        return featuresInformations;
    }
    
    private void _checkFeaturesDependencies(Map<String, FeatureInformation> featuresInformations)
    {
        boolean process = true;
        Set<String> featuresToRemove = new HashSet<String>();
        
        while (process)
        {
            process = false;
            
            for (String featureId : featuresInformations.keySet())
            {
                FeatureInformation info = featuresInformations.get(featureId);
                Configuration conf = info.getConfiguration();
                String depends = conf.getAttribute("depends", "");
                Collection<String> features = StringUtils.stringToCollection(depends);
                
                for (String feature : features)
                {
                    Matcher featureIdMatcher = __FEATURE_ID_PATTERN.matcher(feature);
                    if (featureIdMatcher.matches())
                    {
                        String dependingFeatureId = feature;
                        
                        String prefix = featureIdMatcher.group(1);
                        if (prefix == null || prefix.length() == 0)
                        {
                            dependingFeatureId = info.getPluginName() + FEATURE_ID_SEPARATOR + feature;
                        }
                        
                        if (!featuresInformations.containsKey(dependingFeatureId) && !featuresToRemove.contains(featureId))
                        {
                            if (_logger.isWarnEnabled())
                            {
                                _logger.warn("The feature '" + featureId + "' depends on '" + dependingFeatureId + "' which is not present. It will be ignored.");
                            }
                            
                            featuresToRemove.add(featureId);
                            
                            process = true;
                        }
                    }
                    else
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("The feature '" + featureId + "' depends on '" + feature + "' which is not a valid feature id. This dependency will be ignored.");
                        }
                    }
                }
            }
        }
        
        // Finally remove ignored features
        for (String featureToRemove : featuresToRemove)
        {
            FeatureInformation info = featuresInformations.remove(featureToRemove);
            _inactiveFeatures.put(info.getFeatureId(), new InactiveFeature(info.getPluginName(), info.getFeatureName(), InactivityCause.DEPENDENCY));
        }
    }
    
    private Map<String, FeatureInformation> _computeFeaturesDependencies(Map<String, FeatureInformation> featuresInformations)
    {
        LinkedHashMap<String, FeatureInformation> result = new LinkedHashMap<String, FeatureInformation>();
        
        for (String featureId : featuresInformations.keySet())
        {
            _computeFeaturesDependencies(featureId, featuresInformations, result, featureId);
        }
        
        return result;
    }
    
    private void _computeFeaturesDependencies(String featureId, Map<String, FeatureInformation> featuresInformations, Map<String, FeatureInformation> result, String initialFeatureId)
    {
        FeatureInformation info = featuresInformations.get(featureId);
        Configuration conf = info.getConfiguration();
        String depends = conf.getAttribute("depends", "");
        Collection<String> features = StringUtils.stringToCollection(depends);
        
        for (String feature : features)
        {
            Matcher featureIdMatcher = __FEATURE_ID_PATTERN.matcher(feature);
            if (featureIdMatcher.matches())
            {
                String dependingFeatureId = feature;
                
                String prefix = featureIdMatcher.group(1);
                if (prefix == null || prefix.length() == 0)
                {
                    dependingFeatureId = info.getPluginName() + FEATURE_ID_SEPARATOR + feature;
                }

                if (initialFeatureId.equals(dependingFeatureId))
                {
                    throw new RuntimeException("Circular dependency detected for feature: " + feature);
                }
                
                // do not process the feature if it has already been processed
                if (!result.containsKey(dependingFeatureId))
                {
                    _computeFeaturesDependencies(dependingFeatureId, featuresInformations, result, initialFeatureId);
                }
            }
            else
            {
                if (_logger.isWarnEnabled())
                {
                    _logger.warn("The feature '" + featureId + "' depends on '" + feature + "' which is not a valid feature id. This dependency will be ignored.");
                }
            }
        }
        
        result.put(featureId, info);
    }
    
    private void _checkExtensionsIds(Map<String, Collection<String>> extensionsIds, Configuration conf, String pluginName)
    {
        String featureName = conf.getAttribute("name", null);
        
        Configuration[] extsConf = conf.getChild("extensions").getChildren("extension");
        
        // Boucle sur chaque extension
        for (Configuration extConf : extsConf)
        {
            String id = extConf.getAttribute("id", null);
            String point = extConf.getAttribute("point", null);
            
            if (id != null && point != null)
            {
                // No warnings are generated here, it's already done during extensions loading phase
                Collection<String> ids = extensionsIds.get(point);
                
                if (ids == null)
                {
                    ids = new ArrayList<String>();
                    ids.add(id);
                    extensionsIds.put(point, ids);
                }
                else if (!ids.contains(id))
                {
                    ids.add(id);
                }
                else
                {
                    throw new IllegalArgumentException("In feature " + pluginName + FEATURE_ID_SEPARATOR + featureName + ", the extension " + id + " to point " + point + " is already declared.");
                }
            }
        }
    }
    
    private boolean _includePlugin(Configuration conf, Map<String, SingleExtensionPointInformation> singleExtensionsPoints, Map<String, String> extensionsConfig)
    {
        Configuration[] extsConf = conf.getChild("extensions").getChildren("extension");
        
        // Boucle sur chaque extension
        for (Configuration extConf : extsConf)
        {
            String id = extConf.getAttribute("id", null);
            String point = extConf.getAttribute("point", null);
            
            if (id != null && point != null)
            {
                // No warnings are generated here, it's already done during extensions loading phase
                if (singleExtensionsPoints.containsKey(point))
                {
                    // It's a single extension point, compare it with the parameterized value
                    if (!id.equals(extensionsConfig.get(point)))
                    {
                        // This extension is not selected, the feature is invalidated
                        return false;
                    }
                    else if (extConf.getAttribute("class", null) == null)
                    {
                        throw new IllegalArgumentException("The extension '" + id + "' to the single extension point '" + point + "' defined at " + extConf.getLocation() + " does not contains the mandatory attribute \"class\".");
                    }
                }
            }
        }
        
        return true;
    }
    
    private void _loadFeatures(Map<String, ExtensionPoint> extPoints, Map<String, FeatureInformation> featuresInformations, String contextPath) throws Exception
    {
        for (String featureId : featuresInformations.keySet())
        {
            FeatureInformation info = featuresInformations.get(featureId);
            
            Configuration conf = info.getConfiguration();
            
            Configuration[] extsConf = conf.getChild("extensions").getChildren("extension");
            
            ActiveFeature feature = _getActiveFeature(info.getPluginName(), info.getFeatureName());

            // Loop on each extension
            for (Configuration extConf : extsConf)
            {
                _loadExtensions(feature, extConf, extPoints, contextPath);
            }
        }
        
        // final initialization of extension points
        for (ExtensionPoint extPoint : extPoints.values())
        {
            extPoint.initializeExtensions();
        }
    }
    
    private void _loadExtensions(ActiveFeature feature, Configuration extConf, Map<String, ExtensionPoint> extPoints, String contextPath) throws ConfigurationException
    {
        String id = extConf.getAttribute("id", null);
        String point = extConf.getAttribute("point", null);
        
        String featureId = feature.getPluginName() + FEATURE_ID_SEPARATOR + feature.getFeatureName();
        
        if (id == null)
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("The feature '" + featureId + "' defines an extension without the mandatory \"id\" attribute. It will be ignored.");
            }
        }
        else if (point == null)
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("The extension '" + id + "' in the feature '" + featureId + "' defines an extension without the mandatory \"point\" attribute. It will be ignored.");
            }
        }
        else if (!_extensionPointsRoles.contains(point) && !_singleExtensionPointsRoles.contains(point))
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("The extension '" + id + "' in the feature '" + featureId + "' defines an extension to the non-existing extension point '" + point + "'. It will be ignored.");
            }
        }
        else
        {
            ExtensionPoint extPoint = extPoints.get(point);
            
            if (extPoint != null)
            {
                Configuration realComponentConf = _getComponentConfiguration(extConf, contextPath, feature.getPluginName());

                extPoint.addExtension(feature.getPluginName(), feature.getFeatureName(), realComponentConf);
                feature.addExtension(point, id);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void _loadComponents(PluginsComponentManager manager, Map<String, FeatureInformation> featuresInformations, String contextPath) throws ConfigurationException, ComponentException
    {
        for (String featureId : featuresInformations.keySet())
        {
            FeatureInformation info = featuresInformations.get(featureId);
            Configuration conf = info.getConfiguration();
            
            // Avalon components loading
            Configuration[] componentsConf = conf.getChild("components").getChildren("component");
            
            for (Configuration componentConf : componentsConf)
            {
                String role = componentConf.getAttribute("role", null);
                String clazz = componentConf.getAttribute("class", null);
                
                if (clazz == null)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The feature " + featureId + " defines a component without a class. It will be ignored.");
                    }
                }
                else
                {
                    if (role == null)
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("The feature " + featureId + " defines a component without a role. The class name " + clazz + " will be used.");
                        }
                        role = clazz;
                    }
                    
                    Configuration realComponentConf = _getComponentConfiguration(componentConf, contextPath, info.getPluginName());
                    
                    try
                    {
                        Class c = Class.forName(clazz);
                        
                        manager.addComponent(info.getPluginName(), info.getFeatureName(), role, c, realComponentConf);
                    }
                    catch (ClassNotFoundException ex)
                    {
                        throw new ConfigurationException("Unable to load class " + clazz, componentConf, ex);
                    }
                    
                    ActiveFeature feature = _getActiveFeature(info.getPluginName(), info.getFeatureName());
                    feature.addComponent(role);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> _loadSingleExtensionsPoints(PluginsComponentManager manager, Map<String, FeatureInformation> featuresInformations, Map<String, SingleExtensionPointInformation> singleExtensionsPoints, Map<String, String> extensionsConfig, String contextPath) throws ConfigurationException, ComponentException
    {
        Collection<String> loadedSingleExtensionsPoints = new ArrayList<String>();
        for (String featureId : featuresInformations.keySet())
        {
            FeatureInformation info = featuresInformations.get(featureId);
            Configuration conf = info.getConfiguration();
            
            // extension points loading
            Configuration[] extsConf = conf.getChild("extensions").getChildren("extension");
            
            // loop on each extension
            for (Configuration extConf : extsConf)
            {
                String id = extConf.getAttribute("id", null);
                String point = extConf.getAttribute("point", null);
                
                // Verify that it is a single extension point (else it'll  be handled later) and that the current extension is the selected one
                // Note that this last check should be useless, as the feature would have not been loaded if ids doesn't match
                if (id != null && point != null && singleExtensionsPoints.containsKey(point) && id.equals(extensionsConfig.get(point)))
                {
                    // No warnings are generated here, it's already done during extensions loading phase
                    SingleExtensionPointInformation extInfo = singleExtensionsPoints.get(point);
                    Class<?> extInterface = extInfo.getExtensionPointClass();
                    
                    String clazz = extConf.getAttribute("class", null);
                    
                    // clazz is not null, else an IllegalArgumentException would have been raised in _includePlugin()
                    try
                    {
                        Class c = Class.forName(clazz);
                        
                        if (!extInterface.isAssignableFrom(c))
                        {
                            throw new ConfigurationException("The class " + clazz + " for extension '" + id + "' does not extends or implements the class " + extInterface.getName() + " declared for the single extension point '" + point + "'", extConf);
                        }
                        
                        Configuration realComponentConf = _getComponentConfiguration(extConf, contextPath, info.getPluginName());
                        
                        manager.addComponent(info.getPluginName(), info.getFeatureName(), point, c, realComponentConf);
                        loadedSingleExtensionsPoints.add(point);
                        
                        ActiveFeature feature = _getActiveFeature(info.getPluginName(), info.getFeatureName());
                        feature.addExtension(point, id);
                    }
                    catch (ClassNotFoundException ex)
                    {
                        throw new ConfigurationException("Unable to load class " + clazz, extConf, ex);
                    }
                }
            }
        }
        
        return loadedSingleExtensionsPoints;
    }
    
    private void _checkSingleExtensionsPoints(Collection<String> loadedSingleExtensionsPoints, Map<String, SingleExtensionPointInformation> singleExtensionsPoints, Map<String, String> extensionsConfig)
    {
        for (String point : singleExtensionsPoints.keySet())
        {
            if (!loadedSingleExtensionsPoints.contains(point))
            {
                String id = extensionsConfig.get(point);
                
                String errorMessage = "The extension '" + id +  "' for the extension point '" + point + "' is not loaded. It may be misspelled, or declared in an inactivated feature.";
                _logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }
    }

    private void _loadRuntimeInit(PluginsComponentManager manager)
    {
        String className = RuntimeConfig.getInstance().getInitClassName();

        if (className != null)
        {
            try
            {
                if (_logger.isInfoEnabled())
                {
                    _logger.info("Loading init class '" + className + "' for application");
                }
                
                Class<?> initClass = Class.forName(className);
                
                if (!Init.class.isAssignableFrom(initClass))
                {
                    throw new IllegalArgumentException("Provided init class " + initClass + " does not implement org.ametys.runtime.plugin.Init");
                }
                
                manager.addComponent(null, null, Init.ROLE, initClass, new DefaultConfiguration("component"));
                if (_logger.isInfoEnabled())
                {
                    _logger.info("Init class " + className + " loaded");
                }
            }
            catch (ClassNotFoundException e)
            {
                _logger.error("Exception loading init class", e);
                throw new IllegalArgumentException("Exception loading init class", e);
            }
            catch (ComponentException e)
            {
                _logger.error("Exception loading component", e);
                throw new IllegalArgumentException("Exception loading component", e);
            }
        }
        else if (_logger.isInfoEnabled())
        {
            _logger.info("No init class configured");
        }
    }
    
    private Configuration _getComponentConfiguration(Configuration initialConfiguration, String contextPath, String pluginName) throws ConfigurationException
    {
        String config = initialConfiguration.getAttribute("config", null);
        
        if (config != null)
        {
            InputStream is = null;
            String configPath = null;
            
            try
            {
                // Si l'attribut config est présent, c'est soit un chemin relatif au plugin, soit absolu depuis la racine du contexte web, vers un fichier de conf
                if (config.startsWith("/"))
                {
                    // absolute path
                    File configFile = new File(contextPath, config);
                    configPath = configFile.getAbsolutePath();
                    
                    if (!configFile.exists() || configFile.isDirectory())
                    {
                        if (_logger.isInfoEnabled())
                        {
                            _logger.info("No config file was found at " + configPath + ". Using internally declared config.");
                        }
                        return initialConfiguration;
                    }
                    
                    is = new FileInputStream(configFile);
                }
                else
                {
                    // relative path
                    String baseUri = _baseURIs.get(pluginName);
                    if (baseUri == null)
                    {
                        String pluginLocation = getPluginLocation(pluginName);
                        
                        if (!pluginLocation.endsWith("/"))
                        {
                            pluginLocation += '/';
                        }
                        
                        String path = pluginLocation + pluginName + "/" + config;
                        File configFile = new File(contextPath, path);
                        configPath = configFile.getAbsolutePath();

                        if (!configFile.exists() || configFile.isDirectory())
                        {
                            if (_logger.isInfoEnabled())
                            {
                                _logger.info("No config file was found at " + configPath + ". Using internally declared config.");
                            }
                            return initialConfiguration;
                        }

                        is = new FileInputStream(configFile);
                    }
                    else
                    {
                        String path = baseUri + "/" + config;
                        configPath = "resource:/" + path;
                        is = getClass().getResourceAsStream(path);
                        
                        if (is == null)
                        {
                            if (_logger.isInfoEnabled())
                            {
                                _logger.info("No config file was found at " + configPath + ". Using internally declared config.");
                            }
                            return initialConfiguration;
                        }
                    }
                }
                
                return new DefaultConfigurationBuilder().build(is, configPath);
            }
            catch (Exception ex)
            {
                String errorMessage = "Unable to load external configuration defined in the plugin " + pluginName;
                throw new ConfigurationException(errorMessage, initialConfiguration, ex);
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException ex)
                    {
                        String errorMessage = "Unable to close stream";
                        _logger.error(errorMessage, ex);
                        throw new RuntimeException(errorMessage, ex);
                    }
                }
            }
        }
        
        return initialConfiguration;
    }

    /**
     * Active feature
     */
    public class ActiveFeature
    {
        private String _pluginName;
        private String _featureName;
        private Map<String, Collection<String>> _extensions;
        private Collection<String> _components;
        
        ActiveFeature(String pluginName, String featureName)
        {
            _pluginName = pluginName;
            _featureName = featureName;
            _extensions = new HashMap<String, Collection<String>>();
            _components = new ArrayList<String>();
        }
        
        /**
         * Returns the declaring plugin name
         * @return the declaring plugin name
         */
        public String getPluginName()
        {
            return _pluginName;
        }
        
        /**
         * Returns this feature name
         * @return this feature name
         */
        public String getFeatureName()
        {
            return _featureName;
        }
        
        /**
         * Returns the extensions declared within this feature, stored by extension point
         * @return the extensions declared within this feature, stored by extension point
         */
        public Map<String, Collection<String>> getExtensions()
        {
            return _extensions;
        }
        
        /**
         * Returns the roles of the components declared within this feature
         * @return the roles of the components declared within this feature
         */
        public Collection<String> getComponents()
        {
            return _components;
        }
        
        void addExtension(String point, String id)
        {
            Collection<String> extensions = _extensions.get(point);
            
            if (extensions == null)
            {
                extensions = new ArrayList<String>();
                _extensions.put(point, extensions);
            }
            
            extensions.add(id);
        }
        
        void addComponent(String role)
        {
            _components.add(role);
        }
        
        @Override
        public String toString()
        {
            return _pluginName + FEATURE_ID_SEPARATOR + _featureName;
        }
    }
    
    /**
     * Cause of the deactivation of a feature
     */
    public enum InactivityCause
    {
        /**
         * Constant for manually deactivated plugins
         */
        EXCLUDED,
        
        /**
         * Constant for plugins disabled due to not choosen single extension
         */
        SINGLE,
        
        /**
         * Constant for plugins disabled due to missing dependencies
         */
        DEPENDENCY
    }
    
    private static class LocalEntityResolver implements EntityResolver
    {
        private Map<String, String> _embeddedSchemas = new HashMap<String, String>();
        private Map<String, File> _localSchemas = new HashMap<String, File>();
        
        LocalEntityResolver()
        {
            // Nothing to do
        }
        
        public void addEmbeddedSchema(String systemId, String resourceUri)
        {
            _embeddedSchemas.put(systemId, resourceUri);
        }
        
        public void addLocalSchema(String systemId, File schemaFile)
        {
            _localSchemas.put(systemId, schemaFile);
        }
        
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
        {
            if (systemId.endsWith(".xsd"))
            {
                // Check first into local schemas
                File localSchema = _localSchemas.get(systemId);
                
                if (localSchema != null)
                {
                    return new InputSource(new FileInputStream(localSchema));
                }

                // Then into embedded schemas
                String resourceUri = _embeddedSchemas.get(systemId);

                if (resourceUri != null)
                {
                    InputStream is = getClass().getResourceAsStream(resourceUri);
                    
                    if (is != null)
                    {
                        return new InputSource(is);
                    }
                }
            }
            
            return null;
        }
    }
    
    /**
     * Represents an inactive feature for the current runtime.
     */
    public class InactiveFeature
    {
        private InactivityCause _cause;
        private String _pluginName;
        private String _featureName;
        
        InactiveFeature(String pluginName, String featureName, InactivityCause cause)
        {
            _pluginName = pluginName;
            _featureName = featureName;
            _cause = cause;
        }
        
        /**
         * Returns the declaring plugin name
         * @return the declaring plugin name
         */
        public String getPluginName()
        {
            return _pluginName;
        }
        
        /**
         * Returns this feature name
         * @return this feature name
         */
        public String getFeatureName()
        {
            return _featureName;
        }
        
        /**
         * Returns the cause of the deactivation of this feature
         * @return the cause of the deactivation of this feature
         */
        public InactivityCause getCause()
        {
            return _cause;
        }
        
        @Override
        public String toString()
        {
            return _pluginName + FEATURE_ID_SEPARATOR + _featureName;
        }
    }
    
    private class SingleExtensionPointInformation
    {
        private Class _clazz;
        private String _defaultExtensionId;
        
        SingleExtensionPointInformation(Class clazz, String defaultExtensionId)
        {
            _clazz = clazz;
            _defaultExtensionId = defaultExtensionId;
        }

        Class getExtensionPointClass()
        {
            return _clazz;
        }

        String getDefaultExtensionId()
        {
            return _defaultExtensionId;
        }
    }
    
    /**
     * Helper class containing all relevant feature informations needed at startup
     */
    public class FeatureInformation
    {
        private String _pluginName;
        private String _featureName;
        private Configuration _configuration;
        
        FeatureInformation(String pluginName, String featureName, Configuration configuration)
        {
            _pluginName = pluginName;
            _featureName = featureName;
            _configuration = configuration;
        }
        
        String getPluginName()
        {
            return _pluginName;
        }
        
        String getFeatureName()
        {
            return _featureName;
        }
        
        String getFeatureId()
        {
            return _pluginName + FEATURE_ID_SEPARATOR + _featureName;
        }
        
        Configuration getConfiguration()
        {
            return _configuration;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            ActiveFeature feature = (ActiveFeature) obj;
            return _pluginName.equals(feature.getPluginName()) && _featureName.equals(feature.getFeatureName());
        }
        
        @Override
        public int hashCode()
        {
            return (_pluginName + FEATURE_ID_SEPARATOR + _featureName).hashCode();
        }
    }
}
