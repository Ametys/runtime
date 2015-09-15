/*
 *  Copyright 2015 Anyware Services
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.plugin.PluginIssue.PluginIssueCode;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * The PluginManager is in charge to load and initialize plugins. <br>
 * It gives access to the extension points.
 */
public final class PluginsManager
{
    /** The regexp to determine if a plugin name is ignored (CVS or .* or *.bak or *.old)*/
    public static final String PLUGIN_NAMES_IGNORED = "^CVS|\\..+|.*\\.bak|.*\\.old$";
    
    /** The regexp to determine if a plugin name is correct (add ^ and $ as delimiters if this is your only test) */
    public static final String PLUGIN_NAME_REGEXP = "[a-zA-Z0-9](?:[a-zA-Z0-9-_\\.]*[a-zA-Z0-9])?";
    
    /** Separator between pluginName and featureName */
    public static final String FEATURE_ID_SEPARATOR = "/";

    /** Plugin filename */
    public static final String PLUGIN_FILENAME = "plugin.xml";

    // shared instance
    private static PluginsManager __instance;
    
    // safe mode flag
    private boolean _safeMode;

    // associations plugins/resourcesURI
    private Map<String, String> _resourceURIs;
    
    // plugins/locations association
    private Map<String, File> _locations;
    
    // All readable plugins
    private Map<String, Plugin> _allPlugins;

    // Active plugins
    private Map<String, Plugin> _plugins;

    // Loaded features
    private Map<String, Feature> _features;

    // All declared features
    private Map<String, InactivityCause> _inactiveFeatures;

    // All declared extension points
    private Map<String, ExtensionPointDefinition> _extensionPoints;
    
    // Declared components, stored by role
    private Map<String, ComponentDefinition> _components;
    
    // Declared extension, grouped by extension point
    private Map<String, Map<String, ExtensionDefinition>> _extensions;
    
    // status after initialization
    private Status _status;
    
    // Logger for traces
    private Logger _logger = LoggerFactory.getLogger(PluginsManager.class);

    // Errors collected during system initialization
    private Collection<PluginIssue> _errors = new ArrayList<>();

    private PluginsManager()
    {
        // empty constructor
    }
    
    /**
     * Returns the shared instance of the <code>PluginManager</code>
     * @return the shared instance of the PluginManager
     */
    public static PluginsManager getInstance()
    {
        if (__instance == null)
        {
            __instance = new PluginsManager();
        }

        return __instance;
    }
    
    /**
     * Returns true if the safe mode is activated. 
     * @return true if the safe mode is activated.
     */
    public boolean isSafeMode()
    {
        return _safeMode;
    }
    
    /**
     * Returns errors gathered during plugins loading.
     * @return errors gathered during plugins loading.
     */
    public Collection<PluginIssue> getErrors()
    {
        return _errors;
    }

    /**
     * Returns the names of the plugins
     * @return the names of the plugins
     */
    public Set<String> getPluginNames()
    {
        return Collections.unmodifiableSet(_plugins.keySet());
    }
    
    /**
     * Returns a String array containing the names of the plugins bundled in jars
     * @return a String array containing the names of the plugins bundled in jars
     */
    public Set<String> getBundledPluginsNames()
    {
        return Collections.unmodifiableSet(_resourceURIs.keySet());
    }

    /**
     * Returns active plugins declarations.
     * @return active plugins declarations.
     */
    public Map<String, Plugin> getPlugins()
    {
        return Collections.unmodifiableMap(_plugins);
    }

    /**
     * Returns all existing plugins definitions.
     * @return all existing plugins definitions.
     */
    public Map<String, Plugin> getAllPlugins()
    {
        return Collections.unmodifiableMap(_allPlugins);
    }
    
    /**
     * Returns loaded features declarations. <br>They may be different than active feature in case of safe mode.
     * @return loaded features declarations.
     */
    public Map<String, Feature> getFeatures()
    {
        return Collections.unmodifiableMap(_features);
    }
    
    /**
     * Returns inactive features id and cause of deactivation.
     * @return inactive features id and cause of deactivation.
     */
    public Map<String, InactivityCause> getInactiveFeatures()
    {
        return Collections.unmodifiableMap(_inactiveFeatures);
    }

    /**
     * Returns the extensions points and their extensions
     * @return the extensions points and their extensions
     */
    public Map<String, Collection<String>> getExtensionPoints()
    {
        Map<String, Collection<String>> result = new HashMap<>();
        
        for (String point : _extensions.keySet())
        {
            result.put(point, _extensions.get(point).keySet());
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Returns the components roles.
     * @return the components roles.
     */
    public Collection<String> getComponents()
    {
        return Collections.unmodifiableCollection(_components.keySet());
    }
    
    /**
     * Returns the base URI for the given plugin resources, or null if the plugin does not exist or is located in the file system.
     * @param pluginName the name of the plugin
     * @return the base URI for the given plugin resources, or null if the plugin does not exist or is located in the file system.
     */
    public String getResourceURI(String pluginName)
    {
        String pluginUri = _resourceURIs.get(pluginName);
        if (pluginUri == null || !_plugins.containsKey(pluginName))
        {
            return null;
        }
        
        return "resource:/" + pluginUri; 
    }
    
    /**
     * Returns the plugin filesystem location for the given plugin or null if the plugin is loaded from the classpath.
     * @param pluginName the plugin name
     * @return the plugin location for the given plugin
     */
    public File getPluginLocation(String pluginName)
    {
        return _locations.get(pluginName);
    }
    
    /**
     * Returns the status after initialization.
     * @return the status after initialization.
     */
    public Status getStatus()
    {
        return _status;
    }
    
    /**
     * Initialization of the plugin manager
     * @param parentCM the parent {@link ComponentManager}.
     * @param context the Avalon context
     * @param contextPath the Web context path on the server filesystem
     * @return the {@link PluginsComponentManager} containing loaded components.
     * @throws Exception if something wrong occurs during plugins loading
     */
    public PluginsComponentManager init(ComponentManager parentCM, Context context, String contextPath) throws Exception
    {
        _resourceURIs = new HashMap<>();
        _locations = new HashMap<>();
        _errors = new ArrayList<>();
        
        _safeMode = false;

        // Bundled plugins locations
        _initResourceURIs();
        
        // Additional plugins 
        Map<String, File> externalPlugins = RuntimeConfig.getInstance().getExternalPlugins();
        
        // Check external plugins
        for (File plugin : externalPlugins.values())
        {
            if (!plugin.exists() || !plugin.isDirectory())
            {
                throw new RuntimeException("The configured external plugin is not an existing directory: " + plugin.getAbsolutePath());
            }
        }
        
        // Plugins root directories (directories containing plugins directories)
        Collection<String> locations = RuntimeConfig.getInstance().getPluginsLocations();
        
        // List of chosen components
        Map<String, String> componentsConfig = RuntimeConfig.getInstance().getComponents();
        
        // List of manually excluded plugins
        Collection<String> excludedPlugins = RuntimeConfig.getInstance().getExcludedPlugins();
        
        // List of manually excluded features
        Collection<String> excludedFeatures = RuntimeConfig.getInstance().getExcludedFeatures();
        
        // Parse all plugin.xml
        _allPlugins = _parsePlugins(contextPath, locations, externalPlugins, excludedPlugins);

        if (RuntimeConfig.getInstance().isSafeMode())
        {
            _status = Status.RUNTIME_NOT_LOADED;
            PluginsComponentManager safeManager = _enterSafeMode(parentCM, context, contextPath);
            return safeManager;
        }

        // Get active feature list
        PluginsInformation info = computeActiveFeatures(contextPath, excludedPlugins, excludedFeatures, componentsConfig);
        
        Map<String, Plugin> plugins = info.getPlugins();
        Map<String, Feature> features = info.getFeatures();
        _errors.addAll(info.getErrors());
        
        // At this point, extension points, active and inactive features are known
        if (_logger.isDebugEnabled())
        {
            _logger.debug("All declared plugins : \n\n" + dump(info.getInactiveFeatures()));
        }
        
        if (!_errors.isEmpty())
        {
            _status = Status.WRONG_DEFINITIONS;
            PluginsComponentManager manager = _enterSafeMode(parentCM, context, contextPath);
            return manager;
        }

        // Create the ComponentManager
        PluginsComponentManager manager = new PluginsComponentManager(parentCM);
        manager.setLogger(LoggerFactory.getLogger("org.ametys.runtime.plugin.manager"));
        manager.contextualize(context);
        
        // Config loading
        ConfigManager configManager = ConfigManager.getInstance();
        
        configManager.contextualize(context);
        configManager.service(new WrapperServiceManager(manager));
        configManager.initialize();
        
        // Global config parameter loading
        for (String pluginName : plugins.keySet())
        {
            Plugin plugin = plugins.get(pluginName);
            configManager.addGlobalConfig(pluginName, plugin.getConfigParameters(), plugin.getParameterCheckers());
        }
        
        // "local" config parameter loading
        for (String featureId : features.keySet())
        {
            Feature feature = features.get(featureId);
            configManager.addConfig(feature.getFeatureId(), feature.getConfigParameters(), feature.getConfigParametersReferences(), feature.getParameterCheckers());
        }
        
        // check if the config is complete and valid
        configManager.validate();
        
        if (!configManager.isComplete())
        {
            _status = Status.CONFIG_INCOMPLETE;
            PluginsComponentManager safeManager = _enterSafeMode(parentCM, context, contextPath);
            return safeManager;
        }
        
        // Components and single extension point loading
        Collection<PluginIssue> errors = new ArrayList<>();
        _loadExtensionsPoints(manager, info.getExtensionPoints(), info.getExtensions(), contextPath, errors);
        _loadComponents(manager, info.getComponents(), contextPath, errors);
        _loadRuntimeInit(manager, errors);
        
        _errors.addAll(errors);
        
        if (!errors.isEmpty())
        {
            _status = Status.NOT_INITIALIZED;
            PluginsComponentManager safeManager = _enterSafeMode(parentCM, context, contextPath);
            return safeManager;
        }

        _plugins = plugins;
        _features = features;
        _inactiveFeatures = info.getInactiveFeatures();
        _extensionPoints = info.getExtensionPoints();
        _extensions = info.getExtensions();
        _components = info.getComponents();
        
        try
        {
            manager.initialize();
        }
        catch (Exception e)
        {
            _logger.error("Caught an exception loading components.", e);
            
            _status = Status.NOT_INITIALIZED;

            // Dispose the first ComponentManager
            manager.dispose();
            manager = null;
            
            // Then enter safe mode with another ComponentManager
            PluginsComponentManager safeManager = _enterSafeMode(parentCM, context, contextPath);
            return safeManager;
        }
        
        _status = Status.OK;
        
        return manager;
    }
    
    /**
     * Outputs the structure of the plugins.
     * @param inactiveFeatures id and cause of inactive features
     * @return a String representation of all existing plugins and features.
     */
    public String dump(Map<String, InactivityCause> inactiveFeatures)
    {
        Collection<String> excludedPlugins = RuntimeConfig.getInstance().getExcludedPlugins();
        StringBuilder sb = new StringBuilder();
        
        for (String pluginName : _allPlugins.keySet())
        {
            Plugin plugin = _allPlugins.get(pluginName);
            sb.append(_dumpPlugin(plugin, excludedPlugins, inactiveFeatures));
        }
        
        if (!_errors.isEmpty())
        {
            sb.append("\nErrors :\n");
            _errors.forEach(issue -> sb.append(issue.toString()).append('\n'));
        }
        
        return sb.toString();
    }
    
    private String _dumpPlugin(Plugin plugin, Collection<String> excludedPlugins, Map<String, InactivityCause> inactiveFeatures)
    {
        StringBuilder sb = new StringBuilder();
        
        String pluginName = plugin.getName();
        sb.append("Plugin ").append(pluginName);
        
        if (excludedPlugins.contains(pluginName))
        {
            sb.append("   *** excluded ***");
        }
        
        sb.append('\n');
        
        Collection<String> configParameters = plugin.getConfigParameters().keySet();
        if (!CollectionUtils.isEmpty(configParameters))
        {
            sb.append("  Config parameters : \n");
            configParameters.forEach(param -> sb.append("    ").append(param).append('\n'));
        }
        
        Collection<String> paramCheckers = plugin.getParameterCheckers().keySet();
        if (!CollectionUtils.isEmpty(paramCheckers))
        {
            sb.append("  Parameters checkers : \n");
            paramCheckers.forEach(param -> sb.append("    ").append(param).append('\n'));
        }
        
        Collection<String> extensionPoints = plugin.getExtensionPoints();
        if (!CollectionUtils.isEmpty(extensionPoints))
        {
            sb.append("  Extension points : \n");
            extensionPoints.forEach(point -> sb.append("    ").append(point).append('\n'));
        }
        
        Map<String, Feature> features = plugin.getFeatures();
        for (String featureId : features.keySet())
        {
            Feature feature = features.get(featureId);
            sb.append(_dumpFeature(feature, inactiveFeatures));
        }
        
        sb.append('\n');
        
        return sb.toString();
    }
    
    private String _dumpFeature(Feature feature, Map<String, InactivityCause> inactiveFeatures)
    {
        StringBuilder sb = new StringBuilder();
        String featureId = feature.getFeatureId();
        
        sb.append("  Feature ").append(featureId);
        if (feature.isPassive())
        {
            sb.append(" (passive)");
        }

        if (feature.isSafe())
        {
            sb.append(" (safe)");
        }
        
        if (inactiveFeatures != null && inactiveFeatures.containsKey(featureId))
        {
            sb.append("   *** inactive (").append(inactiveFeatures.get(featureId)).append(") ***");
        }
        
        sb.append('\n');
        
        Collection<String> featureConfigParameters = feature.getConfigParameters().keySet();
        if (!CollectionUtils.isEmpty(featureConfigParameters))
        {
            sb.append("    Config parameters : \n");
            featureConfigParameters.forEach(param -> sb.append("      ").append(param).append('\n'));
        }
        
        Collection<String> configParametersReferences = feature.getConfigParametersReferences();
        if (!CollectionUtils.isEmpty(configParametersReferences))
        {
            sb.append("    Config parameters references : \n");
            configParametersReferences.forEach(param -> sb.append("      ").append(param).append('\n'));
        }
        
        Collection<String> featureParamCheckers = feature.getParameterCheckers().keySet();
        if (!CollectionUtils.isEmpty(featureParamCheckers))
        {
            sb.append("    Parameters checkers : \n");
            featureParamCheckers.forEach(param -> sb.append("    ").append(param).append('\n'));
        }
        
        Map<String, String> componentsIds = feature.getComponentsIds();
        if (!componentsIds.isEmpty())
        {
            sb.append("    Components : \n");
            
            for (String role : componentsIds.keySet())
            {
                String id = componentsIds.get(role);
                sb.append("      ").append(role).append(" : ").append(id).append('\n');
            }
            
            sb.append('\n');
        }

        Map<String, Collection<String>> extensionsIds = feature.getExtensionsIds();
        if (!extensionsIds.isEmpty())
        {
            sb.append("    Extensions : \n");
            
            for (Entry<String, Collection<String>> extensionEntry : extensionsIds.entrySet())
            {
                String point = extensionEntry.getKey();
                Collection<String> ids = extensionEntry.getValue();
                
                sb.append("      ").append(point).append(" :\n");
                ids.forEach(id -> sb.append("        ").append(id).append('\n'));
            }
            
            sb.append('\n');
        }
        
        return sb.toString();
    }
    
    // Look for plugins bundled in jars
    // They have a META-INF/ametys-plugins plain text file containing plugin name and path to plugin.xml
    private void _initResourceURIs() throws IOException
    {
        Enumeration<URL> pluginResources = getClass().getClassLoader().getResources("META-INF/ametys-plugins");
        
        while (pluginResources.hasMoreElements())
        {
            URL pluginResource = pluginResources.nextElement();
            
            try (InputStream is = pluginResource.openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")))
            {
                String plugin;
                while ((plugin = br.readLine()) != null)
                {
                    int i = plugin.indexOf(':');
                    if (i != -1)
                    {
                        String pluginName = plugin.substring(0, i);       
                        String pluginResourceURI = plugin.substring(i + 1);
                    
                        _resourceURIs.put(pluginName, pluginResourceURI);
                    }
                }
            }
        }
    }
    
    private Map<String, Plugin> _parsePlugins(String contextPath, Collection<String> locations, Map<String, File> externalPlugins, Collection<String> excludedPlugins) throws IOException
    {
        Map<String, Plugin> plugins = new HashMap<>();
        
        // Bundled plugins configurations loading
        for (String pluginName : _resourceURIs.keySet())
        {
            String resourceURI = _resourceURIs.get(pluginName) + "/" + PLUGIN_FILENAME;
            
            if (getClass().getResource(resourceURI) == null)
            {
                _pluginError(pluginName, "A plugin '" + pluginName + "' is declared in a jar, but no file '" + PLUGIN_FILENAME + "' can be found at '" + resourceURI + "'.", PluginIssueCode.BUNDLED_PLUGIN_NOT_PRESENT, excludedPlugins, null);
            }
            else if (!pluginName.matches("^" + PLUGIN_NAME_REGEXP + "$"))
            {
                _pluginError(pluginName, pluginName + " is an incorrect plugin name.", PluginIssueCode.PLUGIN_NAME_INVALID, excludedPlugins, null);
            }
            else if (plugins.containsKey(pluginName))
            {
                _pluginError(pluginName, "The plugin " + pluginName + " at " + resourceURI + " is already declared.", PluginIssueCode.PLUGIN_NAME_EXIST, excludedPlugins, null);
            }

            _logger.debug("Reading plugin configuration at {}", resourceURI);

            Configuration configuration = null;
            try (InputStream is = getClass().getResourceAsStream(resourceURI))
            {
                configuration = _getConfigurationFromStream(pluginName, is, "resource:/" + resourceURI, excludedPlugins);
            }

            if (configuration != null)
            {
                Plugin plugin = new Plugin(pluginName);
                plugin.configure(configuration);
                plugins.put(pluginName, plugin);

                _logger.info("Plugin '{}' found at path 'resource:/{}'", pluginName, resourceURI);
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
                    _addPlugin(plugins, pluginDir.getName(), pluginDir, excludedPlugins);
                }
            }
        }
        
        // external plugins
        for (String externalPlugin : externalPlugins.keySet())
        {
            File pluginDir = externalPlugins.get(externalPlugin);

            if (pluginDir.exists() && pluginDir.isDirectory())
            {
                _addPlugin(plugins, externalPlugin, pluginDir, excludedPlugins);
            }
        }
        
        return plugins;
    }
    
    private void _addPlugin(Map<String, Plugin> plugins, String pluginName, File pluginDir, Collection<String> excludedPlugins) throws IOException
    {
        if (pluginName.matches(PLUGIN_NAMES_IGNORED))
        {
            _logger.debug("Skipping directory {} ...", pluginDir.getAbsolutePath());
            return;
        }
        
        if (!pluginName.matches("^" + PLUGIN_NAME_REGEXP + "$"))
        {
            _logger.warn("{} is an incorrect plugin directory name. It will be ignored.", pluginName);
            return;
        }
        
        File pluginFile = new File(pluginDir, PLUGIN_FILENAME);
        if (!pluginFile.exists())
        {
            _logger.warn("There is no file named {} in the directory {}. It will be ignored.", PLUGIN_FILENAME, pluginDir.getAbsolutePath());
            return;
        }

        if (plugins.containsKey(pluginName))
        {
            _pluginError(pluginName, "The plugin " + pluginName + " at " + pluginFile.getAbsolutePath() + " is already declared.", PluginIssueCode.PLUGIN_NAME_EXIST, excludedPlugins, null);
            return;
        }
        
        _logger.debug("Reading plugin configuration at {}", pluginFile.getAbsolutePath());

        Configuration configuration = null;
        try (InputStream is = new FileInputStream(pluginFile))
        {
            configuration = _getConfigurationFromStream(pluginName, is, pluginFile.getAbsolutePath(), excludedPlugins);
        }

        if (configuration != null)
        {
            Plugin plugin = new Plugin(pluginName);
            plugin.configure(configuration);
            plugins.put(pluginName, plugin);
            
            _locations.put(pluginName, pluginDir);
            _logger.info("Plugin '{}' found at path '{}'", pluginName, pluginFile.getAbsolutePath());
        }
    }

    private Configuration _getConfigurationFromStream(String pluginName, InputStream is, String path, Collection<String> excludedPlugins)
    {
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaURL = getClass().getResource("plugin-4.0.xsd");
            Schema schema = schemaFactory.newSchema(schemaURL);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);
            
            return confBuilder.build(is, path);
        }
        catch (Exception e)
        {
            _pluginError(pluginName, "Unable to access to plugin '" + pluginName + "' at " + path, PluginIssueCode.CONFIGURATION_UNREADABLE, excludedPlugins, e);
            return null;
        }
    }
    
    private void _pluginError(String pluginName, String message, PluginIssueCode code, Collection<String> excludedPlugins, Exception e)
    {
        // ignore errors for manually excluded plugins
        if (!excludedPlugins.contains(pluginName))
        {
            PluginIssue issue = new PluginIssue(null, null, code, null, message, e);
            _errors.add(issue);
            _logger.error(message, e);
        }
    }
    
    /**
     * Computes the actual plugins and features to load, based on values selected by the administrator.<br>
     * This method don't actually load nor execute any Java code. It reads plugins definitions, selects active features and get components and extensions definitions.
     * @param contextPath the application context path.
     * @param excludedPlugins manually excluded plugins.
     * @param excludedFeatures manually excluded features.
     * @param componentsConfig chosen components, among those with the same role.
     * @return all informations gathered during plugins reading.
     */
    private PluginsInformation computeActiveFeatures(String contextPath, Collection<String> excludedPlugins, Collection<String> excludedFeatures, Map<String, String> componentsConfig)
    {
        Map<String, Feature> initialFeatures = new HashMap<>();
        Map<String, ExtensionPointDefinition> extensionPoints = new HashMap<>();
        Map<String, InactivityCause> inactiveFeatures = new HashMap<>();
        
        Collection<PluginIssue> errors = new ArrayList<>();

        // Get actual plugin list, corresponding extension points and initial feature list
        Map<String, Plugin> plugins = _computeActivePlugins(excludedPlugins, initialFeatures, inactiveFeatures, extensionPoints, errors);

        // Compute incoming deactivations
        Map<String, Collection<String>> incomingDeactivations = _computeIncomingDeactivations(initialFeatures);
        
        // First remove user-excluded features
        Set<String> ids = initialFeatures.keySet();
        Iterator<String> it = ids.iterator();
        while (it.hasNext())
        {
            String id = it.next();
            
            if (excludedFeatures.contains(id))
            {
                _logger.debug("Remove excluded feature '{}'", id);
                it.remove();
                inactiveFeatures.put(id, InactivityCause.EXCLUDED);
            }
        }
        
        // Then remove deactivated features
        // Also remove feature containing inactive components
        _removeInactiveFeatures(initialFeatures, inactiveFeatures, incomingDeactivations, componentsConfig);
        
        ids = initialFeatures.keySet();
        it = ids.iterator();
        while (it.hasNext())
        {
            String id = it.next();
            Feature feature = initialFeatures.get(id);
            Map<String, Collection<String>> extensionsIds = feature.getExtensionsIds();
            boolean hasBeenRemoved = false;
            for (String point : extensionsIds.keySet())
            {
                if (!extensionPoints.containsKey(point))
                {
                    String message = "In feature '" + id + "' an extension references the non-existing point '" + point + "'.";
                    _logger.error(message);
                    PluginIssue issue = new PluginIssue(feature.getPluginName(), feature.getFeatureName(), PluginIssueCode.INVALID_POINT, null, message);
                    errors.add(issue);
                    if (!hasBeenRemoved)
                    {
                        it.remove();
                        inactiveFeatures.put(id, InactivityCause.INVALID_POINT);
                        hasBeenRemoved = true;
                    }
                }
            }
        }
        
        // Process outgoing dependencies
        Map<String, Feature> features = _processOutgoingDependencies(initialFeatures, inactiveFeatures, errors);

        // Compute incoming dependencies
        Map<String, Collection<String>> incomingDependencies = _computeIncompingDependencies(features);
        
        // Finally remove unused passive features
        ids = features.keySet();
        it = ids.iterator();
        while (it.hasNext())
        {
            String id = it.next();
            Feature feature = features.get(id);
            
            if (feature.isPassive() && !incomingDependencies.containsKey(id))
            {
                _logger.debug("Remove passive feature '{}'", id);
                it.remove();
                inactiveFeatures.put(id, InactivityCause.PASSIVE);
            }
        }
        
        // Check uniqueness of extensions and components
        Map<String, Map<String, ExtensionDefinition>> extensions = _computeExtensions(features, errors);
        Map<String, ComponentDefinition> components = _computeComponents(features, componentsConfig, errors);
        
        return new PluginsInformation(plugins, features, inactiveFeatures, extensionPoints, extensions, components, errors);
    }
    
    private Map<String, Plugin> _computeActivePlugins(Collection<String> excludedPlugins, Map<String, Feature> initialFeatures, Map<String, InactivityCause> inactiveFeatures, Map<String, ExtensionPointDefinition> extensionPoints, Collection<PluginIssue> errors)
    {
        Map<String, Plugin> plugins = new HashMap<>();
        for (String pluginName : _allPlugins.keySet())
        {
            if (!excludedPlugins.contains(pluginName))
            {
                Plugin plugin = _allPlugins.get(pluginName);
                plugins.put(pluginName, plugin);
                _logger.info("Plugin '{}' loaded", pluginName);

                // Check uniqueness of extension points
                Map<String, ExtensionPointDefinition> extPoints = plugin.getExtensionPointDefinitions();
                for (String point : extPoints.keySet())
                {
                    ExtensionPointDefinition definition = extPoints.get(point);
                    
                    if (!_safeMode || definition._safe)
                    {
                        if (extensionPoints.containsKey(point))
                        {
                            // It is an error to have two extension points with the same id, but we should not interrupt when in safe mode, so just ignore it
                            String message = "The extension point '" + point + "', defined in the plugin '" + pluginName + "' is already defined in aother plugin. ";
                            PluginIssue issue = new PluginIssue(pluginName, null, PluginIssue.PluginIssueCode.EXTENSIONPOINT_ALREADY_EXIST, definition._configuration.getLocation(), message);
    
                            if (!_safeMode)
                            {
                                _logger.error(message);
                                errors.add(issue);
                            }
                            else
                            {
                                _logger.debug("[Safe mode] {}", message);
                            }
                        }
                        else
                        {
                            extensionPoints.put(point, definition);
                        }
                    }
                }
                
                Map<String, Feature> features = plugin.getFeatures();
                for (String id : features.keySet())
                {
                    Feature feature = features.get(id);
                    
                    if (!_safeMode || feature.isSafe())
                    {
                        initialFeatures.put(id, feature);
                    }
                    else
                    {
                        inactiveFeatures.put(id, InactivityCause.NOT_SAFE);
                    }
                }
            }
            else
            {
                _logger.debug("Plugin '{}' is excluded", pluginName);
            }
        }

        return plugins;
    }
    
    private void _removeInactiveFeatures(Map<String, Feature> initialFeatures, Map<String, InactivityCause> inactiveFeatures, Map<String, Collection<String>> incomingDeactivations, Map<String, String> componentsConfig)
    {
        Iterator<String> it = initialFeatures.keySet().iterator();
        while (it.hasNext())
        {
            String id = it.next();
            Feature feature = initialFeatures.get(id);
            
            if (incomingDeactivations.containsKey(id) && !incomingDeactivations.get(id).isEmpty())
            {
                String deactivatingFeature = incomingDeactivations.get(id).iterator().next();
                _logger.debug("Removing feature {} deactivated by feature {}.", id, deactivatingFeature);
                it.remove();
                inactiveFeatures.put(id, InactivityCause.DEACTIVATED);
                continue;
            }
            
            Map<String, String> components = feature.getComponentsIds();
            for (String role : components.keySet())
            {
                String componentId = components.get(role);
                String selectedId = componentsConfig.get(role);
                
                // remove the feature if the user asked for a specific id and the declared component has not that id 
                if (selectedId != null && !selectedId.equals(componentId))
                {
                    _logger.debug("Removing feature '{}' as it contains the component id '{}' for role '{}' but the user selected the id '{}' for that role.", id, componentId, role, selectedId);
                    it.remove();
                    inactiveFeatures.put(id, InactivityCause.COMPONENT);
                    continue;
                }
            }
        }
    }
    
    private Map<String, Collection<String>> _computeIncomingDeactivations(Map<String, Feature> features)
    {
        Map<String, Collection<String>> incomingDeactivations = new HashMap<>();
        
        for (String id : features.keySet())
        {
            Feature feature = features.get(id);
            Collection<String> deactivations = feature.getDeactivations();
            
            for (String deactivation : deactivations)
            {
                Collection<String> deps = incomingDeactivations.get(deactivation);
                if (deps == null)
                {
                    deps = new ArrayList<>();
                    incomingDeactivations.put(deactivation, deps);
                }
                
                deps.add(id);
            }
        }
        
        return incomingDeactivations;
    }
    
    private Map<String, Feature> _processOutgoingDependencies(Map<String, Feature> initialFeatures, Map<String, InactivityCause> inactiveFeatures, Collection<PluginIssue> errors)
    {
        // Check outgoing dependencies
        boolean processDependencies = true;
        while (processDependencies)
        {
            processDependencies = false;
            
            Collection<String> ids = initialFeatures.keySet();
            Iterator<String> it = ids.iterator();
            while (it.hasNext())
            {
                String id = it.next();
                Feature feature = initialFeatures.get(id);
                Collection<String> dependencies = feature.getDependencies();
                for (String dependency : dependencies)
                {
                    if (!initialFeatures.containsKey(dependency))
                    {
                        _logger.debug("The feature '{}' depends on '{}' which is not present. It will be ignored.", id, dependency);
                        it.remove();
                        inactiveFeatures.put(id, InactivityCause.DEPENDENCY);
                        processDependencies = true;
                    }
                }
            }
        }
        
        // Reorder remaining features, respecting dependencies
        LinkedHashMap<String, Feature> features = new LinkedHashMap<>();
        
        for (String featureId : initialFeatures.keySet())
        {
            _computeFeaturesDependencies(featureId, initialFeatures, features, featureId, errors);
        }
        
        return features;
    }
    
    private Map<String, Collection<String>> _computeIncompingDependencies(Map<String, Feature> features)
    {
        Map<String, Collection<String>> incomingDependencies = new HashMap<>();
        for (String id : features.keySet())
        {
            Feature feature = features.get(id);
            Collection<String> dependencies = feature.getDependencies();
            
            for (String dependency : dependencies)
            {
                Collection<String> deps = incomingDependencies.get(dependency);
                if (deps == null)
                {
                    deps = new ArrayList<>();
                    incomingDependencies.put(dependency, deps);
                }
                
                deps.add(id);
            }
        }
        
        return incomingDependencies;
    }

    private void _computeFeaturesDependencies(String featureId, Map<String, Feature> features, Map<String, Feature> result, String initialFeatureId, Collection<PluginIssue> errors)
    {
        Feature feature = features.get(featureId);
        Collection<String> dependencies = feature.getDependencies();
        
        for (String dependency : dependencies)
        {
            if (initialFeatureId.equals(dependency))
            {
                String message = "Circular dependency detected for feature: " + feature;
                _logger.error(message);
                PluginIssue issue = new PluginIssue(feature.getPluginName(), feature.getFeatureName(), PluginIssueCode.CIRCULAR_DEPENDENCY, null, message);
                errors.add(issue);
            }
            else if (!result.containsKey(dependency))
            {
                // do not process the feature if it has already been processed
                _computeFeaturesDependencies(dependency, features, result, initialFeatureId, errors);
            }
        }
        
        result.put(featureId, feature);
    }
    
    private Map<String, Map<String, ExtensionDefinition>> _computeExtensions(Map<String, Feature> features, Collection<PluginIssue> errors)
    {
        Map<String, Map<String, ExtensionDefinition>> extensionsDefinitions = new HashMap<>();
        for (Feature feature : features.values())
        {
            // extensions
            Map<String, Map<String, ExtensionDefinition>> extensionsConfs = feature.getExtensions();
            for (String point : extensionsConfs.keySet())
            {
                Map<String, ExtensionDefinition> featureExtensions = extensionsConfs.get(point);
                Map<String, ExtensionDefinition> globalExtensions = extensionsDefinitions.get(point);
                if (globalExtensions == null)
                {
                    globalExtensions = new LinkedHashMap<>(featureExtensions);
                    extensionsDefinitions.put(point, globalExtensions);
                }
                else
                {
                    for (String id : featureExtensions.keySet())
                    {
                        if (globalExtensions.containsKey(id))
                        {
                            String message = "The extension '" + id + "' to point '" + point + "' is already defined in another feature.";
                            _logger.error(message);
                            PluginIssue issue = new PluginIssue(feature.getPluginName(), feature.getFeatureName(), PluginIssueCode.EXTENSION_ALREADY_EXIST, null, message);
                            errors.add(issue);
                        }
                        else
                        {
                            ExtensionDefinition definition = featureExtensions.get(id);
                            globalExtensions.put(id, definition);
                        }
                    }
                }
            }
        }
        
        return extensionsDefinitions;
    }
    
    private Map<String, ComponentDefinition> _computeComponents(Map<String, Feature> features, Map<String, String> componentsConfig, Collection<PluginIssue> errors)
    {
        Map<String, ComponentDefinition> components = new HashMap<>();
        
        for (Feature feature : features.values())
        {
            // components
            Map<String, ComponentDefinition> featureComponents = feature.getComponents();
            for (String role : featureComponents.keySet())
            {
                ComponentDefinition definition = featureComponents.get(role);
                ComponentDefinition globalDefinition = components.get(role);
                if (globalDefinition == null)
                {
                    components.put(role, definition);
                }
                else
                {
                    String id = definition.getId();
                    if (id.equals(globalDefinition.getId()))
                    {
                        String message = "The component for role '" + role + "' and id '" + id + "' is defined both in feature '" + definition.getPluginName() + FEATURE_ID_SEPARATOR + definition.getFeatureName() + "' and in feature '" + globalDefinition.getPluginName() + FEATURE_ID_SEPARATOR + globalDefinition.getFeatureName() + "'.";
                        _logger.error(message);
                        PluginIssue issue = new PluginIssue(feature.getPluginName(), feature.getFeatureName(), PluginIssueCode.COMPONENT_ALREADY_EXIST, null, message);
                        errors.add(issue);
                    }
                    else
                    {
                        String message = "The component for role '" + role + "' is defined with id '" + id + "' in the feature '" + definition.getPluginName() + FEATURE_ID_SEPARATOR + definition.getFeatureName() + "' and with id '" + globalDefinition.getId() + "' in the feature '" + globalDefinition.getPluginName() + FEATURE_ID_SEPARATOR + globalDefinition.getFeatureName() + "'. One of them should be chosen in the runtime.xml.";
                        _logger.error(message);
                        PluginIssue issue = new PluginIssue(feature.getPluginName(), feature.getFeatureName(), PluginIssueCode.COMPONENT_ALREADY_EXIST, null, message);
                        errors.add(issue);
                    }
                }
            }
        }
        
        // check that each component choosen in the runtime.xml is actually defined
        for (String role : componentsConfig.keySet())
        {
            String requiredId = componentsConfig.get(role);
            ComponentDefinition definition = components.get(role);
            
            if (definition == null || !definition.getId().equals(requiredId))
            {
                // Due to preceding checks, the definition id should not be different than requiredId, but two checks are always better than one ...
                String message = "The component for role '" + role + "' should point to id '" + requiredId + "' but no component match.";
                _logger.error(message);
                PluginIssue issue = new PluginIssue(null, null, PluginIssueCode.COMPONENT_NOT_DECLARED, null, message);
                errors.add(issue);
            }
        }
        
        return components;
    }
    
    private void _loadExtensionsPoints(PluginsComponentManager manager, Map<String, ExtensionPointDefinition> extensionPoints, Map<String, Map<String, ExtensionDefinition>> extensionsDefinitions, String contextPath, Collection<PluginIssue> errors)
    {
        for (String point : extensionPoints.keySet())
        {
            ExtensionPointDefinition definition = extensionPoints.get(point);
            Configuration conf = definition._configuration;
            String clazz = conf.getAttribute("class", null);
            String pluginName = definition._pluginName;
            
            try
            {
                Class<? extends Object> c = Class.forName(clazz);

                // check that the class is actually an ExtensionPoint
                if (ExtensionPoint.class.isAssignableFrom(c))
                {
                    Class<? extends ExtensionPoint> extensionClass = c.asSubclass(ExtensionPoint.class);
                    
                    // Load extensions
                    Collection<ExtensionDefinition> extensionDefinitions = new ArrayList<>();
                    Map<String, ExtensionDefinition> initialDefinitions = extensionsDefinitions.get(point);
                    
                    if (initialDefinitions != null)
                    {
                        for (String id : initialDefinitions.keySet())
                        {
                            ExtensionDefinition extensionDefinition = initialDefinitions.get(id);
                            Configuration initialConf = extensionDefinition.getConfiguration();
                            Configuration realExtensionConf = _getComponentConfiguration(initialConf, contextPath, extensionDefinition.getPluginName(), errors);
                            extensionDefinitions.add(new ExtensionDefinition(id, point, extensionDefinition.getPluginName(), extensionDefinition.getFeatureName(), realExtensionConf));
                        }
                    }
                    
                    Configuration realComponentConf = _getComponentConfiguration(conf, contextPath, pluginName, errors);
                    manager.addExtensionPoint(pluginName, point, extensionClass, realComponentConf, extensionDefinitions);
                }
                else
                {
                    String message = "In plugin '" + pluginName + "', the extension point '" + point + "' references class '" + clazz + "' which don't implement " + ExtensionPoint.class.getName();
                    _logger.error(message);
                    PluginIssue issue = new PluginIssue(pluginName, null, PluginIssue.PluginIssueCode.EXTENSIONPOINT_CLASS_INVALID, conf.getLocation(), message);
                    errors.add(issue);
                }
            }
            catch (ClassNotFoundException e)
            {
                String message = "In plugin '" + pluginName + "', the extension point '" + point + "' references the unexisting class '" + clazz + "'.";
                _logger.error(message, e);
                PluginIssue issue = new PluginIssue(pluginName, null, PluginIssue.PluginIssueCode.CLASSNOTFOUND, conf.getLocation(), message);
                errors.add(issue);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void _loadComponents(PluginsComponentManager manager, Map<String, ComponentDefinition> components, String contextPath, Collection<PluginIssue> errors)
    {
        for (String role : components.keySet())
        {
            ComponentDefinition componentDefinition = components.get(role);
            Configuration componentConf = componentDefinition.getConfiguration();
            Configuration realComponentConf = _getComponentConfiguration(componentConf, contextPath, componentDefinition.getPluginName(), errors);

            // XML schema ensures class is not null
            String clazz = componentConf.getAttribute("class", null);
            assert clazz != null;
            
            try
            {
                Class c = Class.forName(clazz);
                manager.addComponent(componentDefinition.getPluginName(), componentDefinition.getFeatureName(), role, c, realComponentConf);
            }
            catch (ClassNotFoundException ex)
            {
                String message = "In feature '" + componentDefinition.getPluginName() + FEATURE_ID_SEPARATOR + componentDefinition.getFeatureName() + "', the component '" + role + "' references the unexisting class '" + clazz + "'.";
                _logger.error(message, ex);
                PluginIssue issue = new PluginIssue(componentDefinition.getPluginName(), componentDefinition.getFeatureName(), PluginIssueCode.CLASSNOTFOUND, componentConf.getLocation(), message);
                errors.add(issue);
            }
        }
    }
    
    private Configuration _getComponentConfiguration(Configuration initialConfiguration, String contextPath, String pluginName, Collection<PluginIssue> errors)
    {
        String config = initialConfiguration.getAttribute("config", null);
        
        if (config != null)
        {
            @SuppressWarnings("resource") InputStream is = null;
            String configPath = null;
            
            try
            {
                // If the config attribute is present, it is either a plugin-relative, or a webapp-relative path (starting with '/')  
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
                    String baseUri = _resourceURIs.get(pluginName);
                    if (baseUri == null)
                    {
                        File pluginLocation = getPluginLocation(pluginName);
                        
                        File configFile = new File(pluginLocation, config);
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
                
                return new DefaultConfigurationBuilder(true).build(is, configPath);
            }
            catch (Exception ex)
            {
                String message = "Unable to load external configuration defined in the plugin " + pluginName;
                _logger.error(message, ex);
                PluginIssue issue = new PluginIssue(pluginName, null, PluginIssueCode.EXTERNAL_CONFIGURATION, initialConfiguration.getLocation(), message);
                errors.add(issue);
            }
            finally
            {
                IOUtils.closeQuietly(is);
            }
        }
        
        return initialConfiguration;
    }

    private void _loadRuntimeInit(PluginsComponentManager manager, Collection<PluginIssue> errors)
    {
        String className = RuntimeConfig.getInstance().getInitClassName();

        if (className != null)
        {
            _logger.info("Loading init class '{}' for application", className);
            
            try
            {
                Class<?> initClass = Class.forName(className);
                if (!Init.class.isAssignableFrom(initClass))
                {
                    String message = "Provided init class " + initClass + " does not implement " + Init.class.getName();
                    _logger.error(message);
                    PluginIssue issue = new PluginIssue(null, null, PluginIssue.PluginIssueCode.INIT_CLASS_INVALID, null, message);
                    errors.add(issue);
                    return;
                }
                
                manager.addComponent(null, null, Init.ROLE, initClass, new DefaultConfiguration("component"));
                _logger.info("Init class {} loaded", className);
            }
            catch (ClassNotFoundException e)
            {
                String message = "The application init class '" + className + "' does not exist.";
                _logger.error(message, e);
                PluginIssue issue = new PluginIssue(null, null, PluginIssueCode.CLASSNOTFOUND, null, message);
                errors.add(issue);
            }
            
        }
        else if (_logger.isInfoEnabled())
        {
            _logger.info("No init class configured");
        }
    }
    
    private PluginsComponentManager _enterSafeMode(ComponentManager parentCM, Context context, String contextPath)
    {
        _logger.info("Entering safe mode due to previous errors ...");
        _safeMode = true;
        
        PluginsInformation info = computeActiveFeatures(contextPath, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP);
        
        _plugins = info.getPlugins();
        _extensionPoints = info.getExtensionPoints();
        _components = info.getComponents();
        _extensions = info.getExtensions();
        _features = info.getFeatures();
        _inactiveFeatures = info.getInactiveFeatures();
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Safe mode : \n\n" + dump(_inactiveFeatures));
        }
        
        Collection<PluginIssue> errors = info.getErrors();
        if (!errors.isEmpty())
        {
            // errors while in safe mode ... 
            throw new PluginException("Errors while loading components in safe mode.", _errors, errors);
        }

        // Create the ComponentManager
        PluginsComponentManager manager = new PluginsComponentManager(parentCM);
        manager.setLogger(LoggerFactory.getLogger("org.ametys.runtime.plugin.manager"));
        manager.contextualize(context);

        errors = new ArrayList<>();
        _loadExtensionsPoints(manager, _extensionPoints, _extensions, contextPath, errors);
        _loadComponents(manager, _components, contextPath, errors);
        
        if (!errors.isEmpty())
        {
            // errors while in safe mode ... 
            throw new PluginException("Errors while loading components in safe mode.", _errors, errors);
        }
        
        try
        {
            manager.initialize();
        }
        catch (Exception e)
        {
            throw new PluginException("Caught exception while starting ComponentManager in safe mode.", e, _errors, null);
        }
        
        return manager;
    }
    
    /**
     * Cause of the deactivation of a feature
     */
    public enum InactivityCause
    {
        /**
         * Constant for excluded features
         */
        EXCLUDED,
        
        /**
         * Constant for features deactivated by other features
         */
        DEACTIVATED,
        
        /**
         * Constant for features disabled due to not choosen component
         */
        COMPONENT,
        
        /**
         * Constant for features disabled due to missing dependencies
         */
        DEPENDENCY,
        
        /**
         * Constant for passive features that are not necessary (nobody depends on it)
         */
        PASSIVE, 
        
        /**
         * Constant for features disabled to wrong referenced extension point
         */
        INVALID_POINT, 
        
        /**
         * Feature is not safe while in safe mode
         */
        NOT_SAFE
    }
    
    /**
     * PluginsManager status after initialization.
     */
    public enum Status
    {
        /**
         * Everything is ok. All features were correctly loaded.
         */
        OK,
        
        /**
         * There was no errors, but the configuration is missing or incomplete.
         */
        CONFIG_INCOMPLETE,
        
        /**
         * Something was wrong when reading plugins definitions.
         */
        WRONG_DEFINITIONS,
        
        /**
         * There were issues during components loading.
         */
        NOT_INITIALIZED, 
        
        /**
         * The runtime.xml could not be loaded.
         */
        RUNTIME_NOT_LOADED
    }
    
    /**
     * Helper class containing all relevant informations after features list computation.
     */
    public static class PluginsInformation
    {
        private Map<String, Plugin> _plugins;
        private Map<String, Feature> _features;
        private Map<String, InactivityCause> _inactiveFeatures;
        private Map<String, ExtensionPointDefinition> _extensionPoints;
        private Map<String, Map<String, ExtensionDefinition>> _extensions;
        private Map<String, ComponentDefinition> _components;
        private Collection<PluginIssue> _errors;
        
        PluginsInformation(Map<String, Plugin> plugins, Map<String, Feature> features, Map<String, InactivityCause> inactiveFeatures, Map<String, ExtensionPointDefinition> extensionPoints, Map<String, Map<String, ExtensionDefinition>> extensions, Map<String, ComponentDefinition> components, Collection<PluginIssue> errors)
        {
            _plugins = plugins;
            _features = features;
            _inactiveFeatures = inactiveFeatures;
            _extensionPoints = extensionPoints;
            _extensions = extensions;
            _components = components;
            _errors = errors;
        }
        
        Map<String, Plugin> getPlugins()
        {
            return _plugins;
        }
        
        Map<String, Feature> getFeatures()
        {
            return _features;
        }
        
        Map<String, InactivityCause> getInactiveFeatures()
        {
            return _inactiveFeatures;
        }
        
        Map<String, ExtensionPointDefinition> getExtensionPoints()
        {
            return _extensionPoints;
        }
        
        Map<String, Map<String, ExtensionDefinition>> getExtensions()
        {
            return _extensions;
        }
        
        Map<String, ComponentDefinition> getComponents()
        {
            return _components;
        }
        
        /**
         * Returns all errors collected during initialization phase.
         * @return all errors collected during initialization phase.
         */
        public Collection<PluginIssue> getErrors()
        {
            return _errors;
        }
    }
}
