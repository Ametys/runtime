/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.StringUtils;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.xml.sax.XMLReader;

/**
 * The PluginManager is in charge to load and initialize plugins. <br>
 * It gives access to the extension points.
 */
public final class PluginsManager
{
    /** Separator between pluginName and featureName */
    public static final String FEATURE_ID_SEPARATOR = "/";
    
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
    
    // Recherche les plugins embarqués dans les jar
    // Ils possèdent un fichier text META-INF/runtime-plugin
    // contenant le nom du plugin et le chemin d'accès au plugins.xml
    private void _initBaseURIs() throws IOException
    {
        Enumeration<URL> pluginResources = getClass().getClassLoader().getResources("META-INF/runtime-plugin");
        
        while (pluginResources.hasMoreElements())
        {
            URL pluginResource = pluginResources.nextElement();
            BufferedReader br = new BufferedReader(new InputStreamReader(pluginResource.openStream(), "UTF-8"));
            
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
            
            br.close();
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
        
        // Localisation des plugins embarqués dans des librairies
        try
        {
            _initBaseURIs();
        }
        catch (IOException e)
        {
            _logger.error("Unable to locate embedded plugins", e);
            throw new RuntimeException("Unable to locate embedded plugins", e);
        }
        
        // Les emplacements de plugins (ie les répertoires contenant des répertoires de plugins)
        Collection<String> locations = RuntimeConfig.getInstance().getPluginsLocations();
        
        // Les configurations de tous les plugin.xml du système
        Map<String, Configuration> pluginsConfigurations = _getConfigurations(contextPath, locations);
        
        _pluginNames = pluginsConfigurations.keySet();
        
        // La liste des points d'extensions singles déclarés par les plugins
        Map<String, SingleExtensionPointInformation> singleExtensionsPoints = _getSingleExtensionsPoints(pluginsConfigurations);
        _singleExtensionPointsRoles = singleExtensionsPoints.keySet();
        
        // La liste des extensions choisies parmi les singles
        Map<String, String> extensionsConfig = RuntimeConfig.getInstance().getExtensionsPoints();
        
        _checkDefaultSingleExtensionsPoints(singleExtensionsPoints, extensionsConfig);
        
        // La liste des features désactivés manuellement
        Collection<String> excludedFeatures = RuntimeConfig.getInstance().getExcludedFeatures();
        
        // Les configurations des plugins actifs
        Map<String, FeatureInformation> featuresInformations = _getActiveFeaturesInformations(pluginsConfigurations, singleExtensionsPoints, extensionsConfig, excludedFeatures);
        
        // Résolution des dépendences
        _checkDependencies(featuresInformations);
        
        // Chargement de la config
        ConfigManager configManager = ConfigManager.getInstance();
        
        try
        {
            configManager.contextualize(context);
            configManager.service(new WrapperServiceManager(manager));
            configManager.initialize();
        }
        catch (Exception ex)
        {
            _logger.error("Exception while setting up ConfigManager", ex);
            throw new RuntimeException("Exception while setting up ConfigManager", ex);
        }
        
        try
        {
            // Chargement des configurations "globales"
            for (String pluginName : pluginsConfigurations.keySet())
            {
                Configuration conf = pluginsConfigurations.get(pluginName);
                configManager.addGlobalConfig(pluginName, conf);
            }
            
            // Chargement des configurations "locales"
            for (String featureId : featuresInformations.keySet())
            {
                FeatureInformation info = featuresInformations.get(featureId);
                configManager.addConfig(info.getPluginName(), info.getFeatureName(), info.getConfiguration());
            }
        }
        catch (ConfigurationException ex)
        {
            _logger.error("Exception while reading Config configuration", ex);
            throw new RuntimeException("Exception while reading Config configuration", ex);
        }
        
        configManager.validate();
        
        if (!configManager.isComplete())
        {
            return null;
        }
        
        // Chargement des composants Avalon et des points d'extensions single
        try
        {
            _loadComponents(manager, featuresInformations, contextPath);
            Collection<String> loadedSingleExtensionsPoints = _loadSingleExtensionsPoints(manager, featuresInformations, singleExtensionsPoints, extensionsConfig, contextPath);
            _checkSingleExtensionsPoints(loadedSingleExtensionsPoints, singleExtensionsPoints, extensionsConfig);
            _loadRuntimeInit(manager);
        }
        catch (ConfigurationException e)
        {
            _logger.error("Exception while loading components", e);
            throw new RuntimeException("Exception while loading components", e);
        }
        
        // La liste des points d'extensions déclarés par les plugins
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
        
        // Chargement des features actives
        _loadFeatures(extPoints, info, contextPath);
    }
    
    private Map<String, Configuration> _getConfigurations(String contextPath, Collection<String> locations)
    {
        Map<String, Configuration> pluginsConfigurations = new HashMap<String, Configuration>();
        
        // Récupération des configurations des plugins embarqués
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
        
        // Récupération des configurations des plugins de tous les emplacements de plugins
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
                
                for (int i = 0; i < pluginDirs.length; i++)
                {
                    File pluginDir = pluginDirs[i];
                    File pluginFile = new File(pluginDir, __PLUGIN_FILENAME);
                    
                    String pluginName = pluginDir.getName();

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
                        if (!pluginName.matches("[a-zA-Z0-9]|([a-zA-Z0-9][a-zA-Z0-9-_.]*[a-zA-Z0-9])"))
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
                                // Normalement, ca ne peut pas arriver, puisqu'on teste l'existence avant
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
            }
        }
        
        return pluginsConfigurations;
    }
    
    private Configuration _getConfigurationFromStream(InputStream is, String path)
    {
        InputStream xsd = null;
        
        try
        {
            // Validation du plugin.xml sur le schéma plugin.xsd
            xsd = getClass().getResourceAsStream("/org/ametys/runtime/plugin/plugin.xsd");
            Schema schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsd));
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            
            DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);
            
            return confBuilder.build(is, path);
        }
        catch (Exception ex)
        {
            _logger.error("Unable to access to plugin at " + path, ex);
            throw new RuntimeException("Unable to access to plugin at " + path, ex);
        }
        finally
        {
            if (xsd != null)
            {
                try
                {
                    xsd.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Exception closing stream : " + path, e);
                }
            }
            
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
                        String errorMessage = "Unable to load class '" + clazz + "' for extension point '" + id + "' in plugin " + pluginName;
                        _logger.error(errorMessage, e);
                        throw new IllegalArgumentException(errorMessage, e);
                    }
                    catch (ConfigurationException e)
                    {
                        String errorMessage = "Unable to load configuration for extension point '" + id + "' in plugin " + pluginName;
                        _logger.error(errorMessage, e);
                        throw new IllegalArgumentException(errorMessage, e);
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
                        
                        extPoints.put(id, new SingleExtensionPointInformation(id, c, defaultExtensionId));
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
        
        // Map interne à cette méthode permettant de vérifier l'unicité des id des extensions
        Map<String, Collection<String>> extensionsIds = new HashMap<String, Collection<String>>();
        
        for (String pluginName : pluginsConfigurations.keySet())
        {
            Configuration[] pluginsConf = pluginsConfigurations.get(pluginName).getChildren("feature");
            
            // Boucle sur la configuration de chaque plugin
            for (Configuration conf : pluginsConf)
            {
                String name = conf.getAttribute("name", null);
                
                if (name == null)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The plugin '" + pluginName + "' defines a feature without the mandatory \"name\" attribute. It will be ignored.");
                    }
                }
                else if (!name.matches("[a-zA-Z0-9]|([a-zA-Z0-9][a-zA-Z0-9-_.]*[a-zA-Z0-9])"))
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn(name + " is an incorrect feature name. It will be ignored.");
                    }
                }
                else if (featuresInformations.containsKey(pluginName + FEATURE_ID_SEPARATOR + name))
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The feature " + name + " in the plugin " + pluginName + " is already declared. It will be ignored.");
                    }
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
    
    private void _checkDependencies(Map<String, FeatureInformation> featuresInformations)
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
        
        // On supprime finalement les plugins ignorés
        for (String featureToRemove : featuresToRemove)
        {
            FeatureInformation info = featuresInformations.remove(featureToRemove);
            _inactiveFeatures.put(info.getFeatureId(), new InactiveFeature(info.getPluginName(), info.getFeatureName(), InactivityCause.DEPENDENCY));
        }
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
                // On ne génère pas de warnings ici, ça sera fait dans la passe de chargement des extensions
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
                // On ne génère pas de warnings ici, ça sera fait dans la passe suivante de chargement des extensions
                if (singleExtensionsPoints.containsKey(point))
                {
                    // C'est bien une extension single
                    // On la compare avec le paramétrage
                    if (!id.equals(extensionsConfig.get(point)))
                    {
                        // Cette extension n'est pas sélectionnée
                        // On invalide le plugin, ie on ne le charge pas
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

            // Boucle sur chaque extension
            for (Configuration extConf : extsConf)
            {
                _loadExtensions(feature, extConf, extPoints, contextPath);
            }
        }
        
        // Initialisation finale des points d'extension
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

    private void _loadComponents(PluginsComponentManager manager, Map<String, FeatureInformation> featuresInformations, String contextPath) throws ConfigurationException, ComponentException
    {
        for (String featureId : featuresInformations.keySet())
        {
            FeatureInformation info = featuresInformations.get(featureId);
            Configuration conf = info.getConfiguration();
            
            // Chargement des composants Avalon
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
    
    private Collection<String> _loadSingleExtensionsPoints(PluginsComponentManager manager, Map<String, FeatureInformation> featuresInformations, Map<String, SingleExtensionPointInformation> singleExtensionsPoints, Map<String, String> extensionsConfig, String contextPath) throws ConfigurationException, ComponentException
    {
        Collection<String> loadedSingleExtensionsPoints = new ArrayList<String>();
        for (String featureId : featuresInformations.keySet())
        {
            FeatureInformation info = featuresInformations.get(featureId);
            Configuration conf = info.getConfiguration();
            
            // Chargement des points d'extensions
            Configuration[] extsConf = conf.getChild("extensions").getChildren("extension");
            
            // Boucle sur chaque extension
            for (Configuration extConf : extsConf)
            {
                String id = extConf.getAttribute("id", null);
                String point = extConf.getAttribute("point", null);
                
                // On vérifie qu'il s'agit d'un point d'extension single (sinon, ca ne nous concerne pas pour l'instant)
                // et que l'extension courante soit bien celle qui est sélectionnée pour le point en question.
                // A noter que cette dernière vérification doit être inutile, puisque si l'id ne correspondait pas, le plugin ne serait pas chargé.
                // ... Mais deux vérifications valent mieux qu'une.
                if (id != null && point != null && singleExtensionsPoints.containsKey(point) && id.equals(extensionsConfig.get(point)))
                {
                    // On ne génère pas de warnings ici, ça sera fait dans la passe suivante de chargement des extensions
                    SingleExtensionPointInformation extInfo = singleExtensionsPoints.get(point);
                    Class<?> extInterface = extInfo.getExtensionPointClass();
                    
                    String clazz = extConf.getAttribute("class", null);
                    
                    // clazz n'est pas nul, sinon, il y aurait eu une IllegalArgumentException dans _includePlugin()
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
                    _logger.info("Loading init class ");
                }
                
                Class<?> initClass = Class.forName(className);
                
                if (initClass.isAssignableFrom(Init.class))
                {
                    throw new IllegalArgumentException("Provided init class " + initClass + " does not implement org.ametys.runtime.plugin.Init");
                }
                
                manager.addComponent(null, null, Init.ROLE, (Class<Object>) initClass, new DefaultConfiguration("component"));
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
            _logger.info("No init class used");
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
                    // chemin absolu
                    File configFile = new File(contextPath, config);
                    configPath = configFile.getAbsolutePath();
                    is = new FileInputStream(configFile);
                }
                else
                {
                    // chemin relatif
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
                        is = new FileInputStream(configFile);
                    }
                    else
                    {
                        String path = baseUri + "/" + config;
                        configPath = "resource:/" + path;
                        is = getClass().getResourceAsStream(path);
                        
                        if (is == null)
                        {
                            throw new IllegalArgumentException("The config file '" + configPath + "' does not exist");
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
        private String _id;
        private Class _clazz;
        private String _defaultExtensionId;
        
        SingleExtensionPointInformation(String id, Class clazz, String defaultExtensionId)
        {
            _id = id;
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

        String getId()
        {
            return _id;
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
