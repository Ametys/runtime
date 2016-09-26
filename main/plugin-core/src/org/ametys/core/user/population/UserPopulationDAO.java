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
package org.ametys.core.user.population;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;

import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.authentication.CredentialProviderFactory;
import org.ametys.core.authentication.CredentialProviderModel;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.core.user.directory.UserDirectoryModel;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.Status;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.util.AmetysHomeHelper;

/**
 * DAO for accessing {@link UserPopulation}
 */
public class UserPopulationDAO extends AbstractLogEnabled implements Component, Serviceable, Initializable, Disposable
{
    /** Avalon Role */
    public static final String ROLE = UserPopulationDAO.class.getName();
    
    /** The id of the "admin" population */
    public static final String ADMIN_POPULATION_ID = "admin_population";
    
    /** The path of the XML file containing the user populations */
    private static final File __USER_POPULATIONS_FILE = new File(AmetysHomeHelper.getAmetysHome(), "config" + File.separator + "user-populations.xml");
    
    /** The regular expression for an id of a user population */
    private static final String __ID_REGEX = "^[a-z][a-z0-9_-]*";
    
    /** The date of the last update of the XML file */
    private long _lastUpdate;
    
    /** The whole user populations of the application */
    private Map<String, UserPopulation> _userPopulations;
    /** The misconfigured user populations */
    private Set<String> _misconfiguredUserPopulations;
    
    /** The list of population ids which are declared in the user population file but were not instanciated since their configuration led to an error */
    private Set<String> _ignoredPopulations;
    
    /** The population admin */
    private UserPopulation _adminUserPopulation;

    /** The user directories factory  */
    private UserDirectoryFactory _userDirectoryFactory;

    /** The credential providers factory  */
    private CredentialProviderFactory _credentialProviderFactory;
    
    /** The extension point for population consumers */
    private PopulationConsumerExtensionPoint _populationConsumerEP;

    @Override
    public void initialize()
    {
        _userPopulations = new LinkedHashMap<>();
        _misconfiguredUserPopulations = new HashSet<>();
        _ignoredPopulations = new HashSet<>();
        _lastUpdate = 0;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userDirectoryFactory = (UserDirectoryFactory) manager.lookup(UserDirectoryFactory.ROLE);
        _credentialProviderFactory = (CredentialProviderFactory) manager.lookup(CredentialProviderFactory.ROLE);
        _populationConsumerEP = (PopulationConsumerExtensionPoint) manager.lookup(PopulationConsumerExtensionPoint.ROLE);
    }
    
    /**
     * Gets all the populations to JSON format
     * @param withAdmin True to include the "admin" population
     * @return A list of object representing the {@link UserPopulation}s
     */
    public List<Object> getUserPopulationsAsJson(boolean withAdmin)
    {
        return getUserPopulations(withAdmin).stream().map(this::getUserPopulationAsJson).collect(Collectors.toList());
    }
    
    /**
     * Gets a population to JSON format
     * @param userPopulation The user population to get
     * @return An object representing a {@link UserPopulation}
     */
    public Map<String, Object> getUserPopulationAsJson(UserPopulation userPopulation)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", userPopulation.getId());
        result.put("label", userPopulation.getLabel());
        result.put("enabled", userPopulation.isEnabled());
        result.put("valid", isValid(userPopulation.getId()));
        result.put("isInUse", _populationConsumerEP.isInUse(userPopulation.getId()));
        
        List<Object> userDirectories = new ArrayList<>();
        int directoryIndex = 0;
        for (UserDirectory ud : userPopulation.getUserDirectories())
        {
            String udModelId = ud.getUserDirectoryModelId();
            UserDirectoryModel udModel = _userDirectoryFactory.getExtension(udModelId);
            
            Map<String, Object> directory = new HashMap<>();
            directory.put("index", directoryIndex++);
            directory.put("label", udModel.getLabel());
            directory.put("modifiable", ud instanceof ModifiableUserDirectory);
            userDirectories.add(directory);
        }
        result.put("userDirectories", userDirectories);
        
        List<I18nizableText> credentialProviders = new ArrayList<>();
        for (CredentialProvider cp : userPopulation.getCredentialProviders())
        {
            String cpModelId = cp.getCredentialProviderModelId();
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cpModelId);
            credentialProviders.add(cpModel.getLabel());
        }
        result.put("credentialProviders", credentialProviders);
        
        return result;
    }
    
    /**
     * Gets all the populations of this application
     * @param includeAdminPopulation True to include the "admin" population
     * @return A list of {@link UserPopulation}
     */
    public List<UserPopulation> getUserPopulations(boolean includeAdminPopulation)
    {
        List<UserPopulation> result = new ArrayList<>();
        if (includeAdminPopulation)
        {
            result.add(getAdminPopulation());
        }
        
        // Don't read in safe mode, we know that only the admin population is needed in this case and we want to prevent some warnings in the logs for non-safe features not found
        if (Status.OK.equals(PluginsManager.getInstance().getStatus()))
        {
            _readPopulations(false);
            result.addAll(_userPopulations.values());
        }
        
        return result;
    }
    
    /**
     * Gets all the enabled populations of this application
     * @param withAdmin True to include the "admin" population
     * @return A list of enabled {@link UserPopulation}
     */
    public List<UserPopulation> getEnabledUserPopulations(boolean withAdmin)
    {
        return getUserPopulations(withAdmin).stream().filter(UserPopulation::isEnabled).collect(Collectors.toList());
    }
    
    /**
     * Gets a population with its id.
     * @param id The id of the population
     * @return The {@link UserPopulation}, or null if not found
     */
    public UserPopulation getUserPopulation(String id)
    {
        if (ADMIN_POPULATION_ID.equals(id))
        {
            return getAdminPopulation();
        }
        
        _readPopulations(false);
        return _userPopulations.get(id);
    }
    
    /**
     * Gets the list of the ids of all the population of the application
     * @return The list of the ids of all the populations
     */
    @Callable
    public List<String> getUserPopulationsIds()
    {
        _readPopulations(false);
        return new ArrayList<>(_userPopulations.keySet());
    }
    
    /**
     * Returns the id of population which have a fatal invalid configuration.
     * These populations can NOT be used by application.
     * @return The ignored populations
     */
    public Set<String> getIgnoredPopulations()
    {
        _readPopulations(false);
        return _ignoredPopulations;
    }
    
    /**
     * Returns the id of population which have at least one user directory or one credential provider misconfigured
     * These populations can be used by application.
     * @return The misconfigured populations.
     */
    public Set<String> getMisconfiguredPopulations()
    {
        _readPopulations(false);
        return _misconfiguredUserPopulations;
    }
    
    /**
     * Return the stream of the configuration file
     * @return The input stream to be closed.
     */
    public InputStream getConfigurationFile()
    {
        try
        {
            return new FileInputStream(__USER_POPULATIONS_FILE);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("Cannot get the configuration file", e);
        }
    }
    
    /**
     * Gets the configuration for creating/editing a user population.
     * @return A map containing information about what is needed to create/edit a user population
     * @throws Exception If an error occurs.
     */
    @Callable
    public Map<String, Object> getEditionConfiguration() throws Exception
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        List<Object> userDirectoryModels = new ArrayList<>();
        for (String extensionId : _userDirectoryFactory.getExtensionsIds())
        {
            UserDirectoryModel udModel = _userDirectoryFactory.getExtension(extensionId);
            Map<String, Object> udMap = new LinkedHashMap<>();
            udMap.put("id", extensionId);
            udMap.put("label", udModel.getLabel());
            udMap.put("description", udModel.getDescription());
            
            Map<String, Object> params = new LinkedHashMap<>();
            for (String paramId : udModel.getParameters().keySet())
            {
                // prefix in case of two parameters from two different models have the same id which can lead to some errorsin client-side
                params.put(extensionId + "$" + paramId, ParameterHelper.toJSON(udModel.getParameters().get(paramId)));
            }
            udMap.put("parameters", params);
            
            Map<String, Object> paramCheckers = new LinkedHashMap<>();
            for (String paramCheckerId : udModel.getParameterCheckers().keySet())
            {
                ParameterCheckerDescriptor paramChecker = udModel.getParameterCheckers().get(paramCheckerId);
                paramCheckers.put(extensionId + "$" + paramCheckerId, paramChecker.toJSON());
            }
            udMap.put("parameterCheckers", paramCheckers);
            
            userDirectoryModels.add(udMap);
        }
        result.put("userDirectoryModels", userDirectoryModels);
        
        List<Object> credentialProviderModels = new ArrayList<>();
        for (String extensionId : _credentialProviderFactory.getExtensionsIds())
        {
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(extensionId);
            Map<String, Object> cpMap = new LinkedHashMap<>();
            cpMap.put("id", extensionId);
            cpMap.put("label", cpModel.getLabel());
            cpMap.put("description", cpModel.getDescription());
            
            Map<String, Object> params = new LinkedHashMap<>();
            for (String paramId : cpModel.getParameters().keySet())
            {
                // prefix in case of two parameters from two different models have the same id which can lead to some errorsin client-side
                params.put(extensionId + "$" + paramId, ParameterHelper.toJSON(cpModel.getParameters().get(paramId)));
            }
            cpMap.put("parameters", params);
            
            credentialProviderModels.add(cpMap);
        }
        result.put("credentialProviderModels", credentialProviderModels);
        
        return result;
    }
    
    /**
     * Gets the values of the parameters of the given population
     * @param id The id of the population
     * @return The values of the parameters
     */
    @Callable
    public Map<String, Object> getPopulationParameterValues(String id)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        _readPopulations(false);
        UserPopulation up = _userPopulations.get(id);
        
        if (up == null)
        {
            getLogger().error("The UserPopulation of id '{}' does not exists.", id);
            result.put("error", "unknown");
            return result;
        }
        
        // Population Label
        result.put("label", up.getLabel());
        result.put("id", up.getId());
        
        // User Directories
        List<Map<String, Object>> userDirectories = new ArrayList<>();
        
        for (UserDirectory ud : up.getUserDirectories())
        {
            Map<String, Object> ud2json = new HashMap<>();
            String udModelId = ud.getUserDirectoryModelId();
            ud2json.put("udModelId", udModelId);
            Map<String, Object> params = new HashMap<>();
            for (String key : ud.getParameterValues().keySet())
            {
                params.put(udModelId + "$" + key, ud.getParameterValues().get(key));
            }
            ud2json.put("params", params);
            userDirectories.add(ud2json);
        }
        result.put("userDirectories", userDirectories);
        
        // Credential Providers
        List<Map<String, Object>> credentialProviders = new ArrayList<>();
        
        for (CredentialProvider cp : up.getCredentialProviders())
        {
            Map<String, Object> cp2json = new HashMap<>();
            String cpModelId = cp.getCredentialProviderModelId();
            cp2json.put("cpModelId", cpModelId);
            Map<String, Object> params = new HashMap<>();
            for (String key : cp.getParameterValues().keySet())
            {
                params.put(cpModelId + "$" + key, cp.getParameterValues().get(key));
            }
            cp2json.put("params", params);
            credentialProviders.add(cp2json);
        }
        result.put("credentialProviders", credentialProviders);
        
        return result;
    }
    
    /**
     * Gets the "admin" population
     * @return The "admin" population
     */
    public UserPopulation getAdminPopulation()
    {
        if (_adminUserPopulation != null)
        {
            return _adminUserPopulation;
        }
        
        _adminUserPopulation = new UserPopulation();
        _adminUserPopulation.setId(ADMIN_POPULATION_ID);
        
        Map<String, String> userDirectory = new HashMap<>();
        String udModelId = "org.ametys.plugins.core.user.directory.Jdbc";
        userDirectory.put("udModelId", udModelId);
        userDirectory.put(udModelId + "$" + "runtime.users.jdbc.datasource", SQLDataSourceManager.AMETYS_INTERNAL_DATASOURCE_ID);
        userDirectory.put(udModelId + "$" + "runtime.users.jdbc.table", "AdminUsers");
        
        Map<String, String> credentialProvider = new HashMap<>();
        String cpModelId = "org.ametys.core.authentication.Basic";
        credentialProvider.put("cpModelId", cpModelId); // TODO replace Basic by Form (need Form to be safe) RUNTIME-1764
        credentialProvider.put(cpModelId + "$" + "runtime.authentication.basic.realm", "Ametys workspace admin");
        
        _fillUserPopulation(_adminUserPopulation, "Admin Population", Collections.singletonList(userDirectory), Collections.singletonList(credentialProvider));
        
        return _adminUserPopulation;
    }
    
    /**
     * Adds a new population
     * @param id The unique id of the population
     * @param label The label of the population
     * @param userDirectories A list of user directory parameters
     * @param credentialProviders A list of credential provider parameters
     * @return The id of the created population or null if an error occured
     */
    @Callable
    public String add(String id, String label, List<Map<String, String>> userDirectories, List<Map<String, String>> credentialProviders)
    {
        _readPopulations(false);
        
        if (!_isCorrectId(id))
        {
            return null;
        }
        
        UserPopulation up = new UserPopulation();
        up.setId(id);
        
        _fillUserPopulation(up, label, userDirectories, credentialProviders);
        
        _userPopulations.put(id, up);
        if (_writePopulations())
        {
            return null;
        }
        
        return id;
    }
    
    private boolean _isCorrectId(String id)
    {
        if (_userPopulations.get(id) != null || ADMIN_POPULATION_ID.equals(id))
        {
            getLogger().error("The id '{}' is already used for a population.", id);
            return false;
        }
        
        if (!Pattern.matches(__ID_REGEX, id))
        {
            getLogger().error("The id '{}' is not a correct id for a user population.", id);
            return false;
        }
        
        return true;
    }
    
    /**
     * Edits the given population.
     * @param id The id of the population to edit
     * @param label The label of the population
     * @param userDirectories A list of user directory parameters
     * @param credentialProviders A list of credential provider parameters
     * @return A map containing the id of the edited population
     */
    @Callable
    public Map<String, Object> edit(String id, String label, List<Map<String, String>> userDirectories, List<Map<String, String>> credentialProviders)
    {
        _readPopulations(false);
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        UserPopulation up = _userPopulations.get(id);
        if (up == null)
        {
            getLogger().error("The UserPopulation with id '{}' does not exist, it cannot be edited.", id);
            result.put("error", "unknown");
            return result;
        }
        
        _fillUserPopulation(up, label, userDirectories, credentialProviders);
        
        if (_writePopulations())
        {
            result.put("error", "server");
            return result;
        }
        
        result.put("id", id);
        return result;
    }
    
    private void _fillUserPopulation(UserPopulation up, String label, List<Map<String, String>> userDirectories, List<Map<String, String>> credentialProviders)
    {
        up.setLabel(new I18nizableText(label));
        
        // Create the user directories
        List<UserDirectory> uds = new ArrayList<>();
        for (Map<String, String> userDirectoryParameters : userDirectories)
        {
            String modelId = userDirectoryParameters.remove("udModelId");
            Map<String, Object> typedParamValues = _getTypedUDParameters(userDirectoryParameters, modelId);
            uds.add(_userDirectoryFactory.createUserDirectory(modelId, typedParamValues, up.getId()));
        }
        up.setUserDirectory(uds);
        
        // Create the credential providers
        List<CredentialProvider> cps = new ArrayList<>();
        for (Map<String, String> credentialProviderParameters : credentialProviders)
        {
            String modelId = credentialProviderParameters.remove("cpModelId");
            Map<String, Object> typedParamValues = _getTypedCPParameters(credentialProviderParameters, modelId);
            cps.add(_credentialProviderFactory.createCredentialProvider(modelId, typedParamValues));
        }
        up.setCredentialProvider(cps);
    }
    
    private Map<String, Object> _getTypedUDParameters(Map<String, String> parameters, String modelId)
    {
        Map<String, Object> resultParameters = new LinkedHashMap<>();
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _userDirectoryFactory.getExtension(modelId).getParameters();
        for (String paramNameWithPrefix : parameters.keySet())
        {
            String[] splitStr = paramNameWithPrefix.split("\\$", 2);
            String prefix = splitStr[0];
            String paramName = splitStr[1];
            if (prefix.equals(modelId) && declaredParameters.containsKey(paramName))
            {
                String originalValue = parameters.get(paramNameWithPrefix);
                
                Parameter<ParameterType> parameter = declaredParameters.get(paramName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(originalValue, type);
                resultParameters.put(paramName, typedValue);
            }
            else if (prefix.equals(modelId))
            {
                getLogger().warn("The parameter {} is not declared in extension {}. It will be ignored", paramName, modelId);
            }
        }
        
        return resultParameters;
    }
    
    private Map<String, Object> _getTypedCPParameters(Map<String, String> parameters, String modelId)
    {
        Map<String, Object> resultParameters = new LinkedHashMap<>();
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _credentialProviderFactory.getExtension(modelId).getParameters();
        for (String paramNameWithPrefix : parameters.keySet())
        {
            String[] splitStr = paramNameWithPrefix.split("\\$", 2);
            String prefix = splitStr[0];
            String paramName = splitStr[1];
            if (prefix.equals(modelId) && declaredParameters.containsKey(paramName))
            {
                String originalValue = parameters.get(paramNameWithPrefix);
                
                Parameter<ParameterType> parameter = declaredParameters.get(paramName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(originalValue, type);
                resultParameters.put(paramName, typedValue);
            }
            else if (prefix.equals(modelId))
            {
                getLogger().warn("The parameter {} is not declared in extension {}. It will be ignored", paramName, modelId);
            }
        }
        
        return resultParameters;
    }
    
    /**
     * Removes the given population.
     * @param id The id of the population to remove
     * @return A map containing the id of the removed population
     */
    @Callable
    public Map<String, Object> remove(String id)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // Check if the population is not the admin population
        if (ADMIN_POPULATION_ID.equals("id"))
        {
            return null;
        }
        
        // Check if the population is used
        if (_populationConsumerEP.isInUse(id))
        {
            getLogger().error("The UserPopulation with id '{}' is used, it cannot be removed.", id);
            result.put("error", "used");
            return result;
        }
        
        _readPopulations(false);
        if (_userPopulations.remove(id) == null)
        {
            getLogger().error("The UserPopulation with id '{}' does not exist, it cannot be removed.", id);
            result.put("error", "unknown");
            return result;
        }
        if (_writePopulations())
        {
            result.put("error", "server");
            return result;
        }
        
        result.put("id", id);
        return result;
    }
    
    /**
     * Enables/Disables the given population
     * @param populationId The id of the population to enable/disable
     * @param enabled True to enable the population, false to disable it.
     * @return A map containing the id of the enabled/disabled population, or with an error.
     */
    @Callable
    public Map<String, Object> enable(String populationId, boolean enabled)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        UserPopulation population = getUserPopulation(populationId);
        if (population != null)
        {
            population.enable(enabled);
            result.put("id", populationId);
        }
        else
        {
            getLogger().error("The UserPopulation with id '{}' does not exist, it cannot be enabled/disabled.", populationId);
            result.put("error", "unknown");
        }
        
        if (_writePopulations())
        {
            result.put("error", "server");
            return result;
        }
        
        return result;
    }
    
    /**
     * Determines if a population has a valid configuration
     * @param populationId The id of the population to retrieve state
     * @return A map, with the response as a boolean, or an error.
     */
    @Callable
    public boolean isValid(String populationId)
    {
        return !_misconfiguredUserPopulations.contains(populationId);
    }
    
    /**
     * Returns the enabled state of the given population
     * @param populationId The id of the population to retrieve state
     * @return A map, with the response as a booolean, or an error.
     */
    @Callable
    public Map<String, Object> isEnabled(String populationId)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        UserPopulation population = getUserPopulation(populationId);
        if (population != null)
        {
            result.put("enabled", population.isEnabled());
        }
        else
        {
            result.put("error", "unknown");
        }
        
        return result;
    }
    
    /**
     * If needed, reads the config file representing the populations and then
     * reinitializes and updates the internal representation of the populations.
     * @param forceRead True to avoid the use of the cache and force the reading of the file
     */
    private void _readPopulations(boolean forceRead)
    {
        try
        {
            if (!__USER_POPULATIONS_FILE.exists())
            {
                _createPopulationsFile(__USER_POPULATIONS_FILE);
            }
            
            if (forceRead || __USER_POPULATIONS_FILE.lastModified() > _lastUpdate)
            {
                _lastUpdate = new Date().getTime();
                _userPopulations = new LinkedHashMap<>();
                _ignoredPopulations = new HashSet<>();
                _misconfiguredUserPopulations = new HashSet<>();
                
                Configuration cfg = new DefaultConfigurationBuilder().buildFromFile(__USER_POPULATIONS_FILE);
                for (Configuration childCfg : cfg.getChildren("userPopulation"))
                {
                    try
                    {
                        _configurePopulation(childCfg);
                    }
                    catch (ConfigurationException e)
                    {
                        getLogger().error("Fatal configuration error for population of id '{}'. The population will be ignored.", childCfg.getAttribute("id", ""), e);
                        _ignoredPopulations.add(childCfg.getAttribute("id", ""));
                    }
                }
            }
        }
        catch (Exception e)
        {
            getLogger().error("Failed to retrieve user populations from the configuration file " + __USER_POPULATIONS_FILE, e);
        }
    }
    
    private void _createPopulationsFile(File file) throws IOException, TransformerConfigurationException, SAXException
    {
        file.createNewFile();
        try (OutputStream os = new FileOutputStream(file))
        {
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
            
            StreamResult result = new StreamResult(os);
            th.setResult(result);

            // create the format of result
            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.INDENT, "yes");
            format.put(OutputKeys.ENCODING, "UTF-8");
            format.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
            th.getTransformer().setOutputProperties(format);
            th.startDocument();
            XMLUtils.createElement(th, "userPopulations");
            th.endDocument();
        }
    }
    
    private void _configurePopulation(Configuration configuration) throws ConfigurationException
    {
        UserPopulation up = new UserPopulation();
        
        String upId = configuration.getAttribute("id");
        up.setId(upId);
        up.setLabel(new I18nizableText(configuration.getChild("label").getValue()));
        
        List<UserDirectory> userDirectories = _configureUserDirectories(configuration, upId);
        up.setUserDirectory(userDirectories);
        
        List<CredentialProvider> credentialProviders = _configureCredentialProviders(configuration, upId);
        up.setCredentialProvider(credentialProviders);
        
        boolean enabled = configuration.getAttributeAsBoolean("enabled", true) && userDirectories.size() > 0 && credentialProviders.size() > 0;
        up.enable(enabled);
        
        _userPopulations.put(upId, up);
    }
    
    private List<UserDirectory> _configureUserDirectories(Configuration configuration, String upId) throws ConfigurationException
    {
        List<UserDirectory> userDirectories = new ArrayList<>();
        
        Configuration[] userDirectoriesConf = configuration.getChild("userDirectories").getChildren("userDirectory");
        for (Configuration userDirectoryConf : userDirectoriesConf)
        {
            String modelId = userDirectoryConf.getAttribute("modelId");
            
            try
            {
                Map<String, Object> paramValues = _getUDParametersFromConfiguration(userDirectoryConf, modelId, upId);
                UserDirectory ud = _userDirectoryFactory.createUserDirectory(modelId, paramValues, upId);
                if (ud != null)
                {
                    userDirectories.add(ud);
                }
            }
            catch (Exception e)
            {
                getLogger().warn("The population of id '" + upId + "' declares a user directory with an invalid configuration", e);
                _misconfiguredUserPopulations.add(upId);
            }
        }

        if (userDirectories.isEmpty())
        {
            _misconfiguredUserPopulations.add(upId);
            getLogger().warn("The population of id '" + upId + "' does not have user directory with a valid configuration. It will be disabled until it will be fixed.");
        }
        
        return userDirectories;
    }
    
    private List<CredentialProvider> _configureCredentialProviders(Configuration configuration, String upId) throws ConfigurationException
    {
        List<CredentialProvider> credentialProviders = new ArrayList<>();
        
        Configuration[] credentialProvidersConf = configuration.getChild("credentialProviders").getChildren("credentialProvider");
        for (Configuration credentialProviderConf : credentialProvidersConf)
        {
            String modelId = credentialProviderConf.getAttribute("modelId");
            
            try
            {
                Map<String, Object> paramValues = _getCPParametersFromConfiguration(credentialProviderConf, modelId, upId);
                CredentialProvider cp = _credentialProviderFactory.createCredentialProvider(modelId, paramValues);
                if (cp != null)
                {
                    credentialProviders.add(cp);
                }
            }
            catch (Exception e)
            {
                getLogger().warn("The population of id '" + upId + "' declares a credential provider with an invalid configuration", e);
                _misconfiguredUserPopulations.add(upId);
            }
        }
        
        if (credentialProviders.isEmpty())
        {
            _misconfiguredUserPopulations.add(upId);
            getLogger().warn("The population of id '" + upId + "' does not have credential provider with a valid configuration. It will be disabled until it will be fixed.");
        }
        
        return credentialProviders;
    }
    
    /**
     * Get the typed parameters of a user directory used by a population
     * @param conf The user directory's configuration.
     * @param modelId The id of user directory model
     * @param populationId The id of population
     * @return The typed parameters
     * @throws IllegalArgumentException if the configured user directory references a non-existing user directory model
     * @throws ConfigurationException if a parameter is missing
     */
    private Map<String, Object> _getUDParametersFromConfiguration(Configuration conf, String modelId, String populationId) throws ConfigurationException, IllegalArgumentException
    {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        if (!_userDirectoryFactory.hasExtension(modelId))
        {
            throw new IllegalArgumentException(String.format("The population of id '%s' declares a non-existing user directory model with id '%s'. It will be ignored.", populationId, modelId));
        }
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _userDirectoryFactory.getExtension(modelId).getParameters();
        
        for (String udParamName : declaredParameters.keySet())
        {
            Configuration paramConf = conf.getChild(udParamName, false);
            if (paramConf == null)
            {
                throw new ConfigurationException(String.format("The population of id '%s' declares a user directory model with id '%s' but the parameter '%s' is missing. This user directory will be ignored.", populationId, modelId, udParamName));
            }
            else
            {
                String valueAsString = paramConf.getValue("");
                
                Parameter<ParameterType> parameter = declaredParameters.get(udParamName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(valueAsString, type);
                parameters.put(udParamName, typedValue);
            }
        }
        
        return parameters;
    }
    
    /**
     * Get the typed parameters of a credential provider used by a population
     * @param conf The credential provider's configuration.
     * @param modelId The id of credential provider model
     * @param populationId The id of population
     * @return The typed parameters
     * @throws IllegalArgumentException if the configured credential provider references a non-existing credential provider model
     * @throws ConfigurationException if a parameter is missing
     */
    private Map<String, Object> _getCPParametersFromConfiguration(Configuration conf, String modelId, String populationId) throws ConfigurationException, IllegalArgumentException
    {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        if (!_credentialProviderFactory.hasExtension(modelId))
        {
            throw new IllegalArgumentException(String.format("The population of id '%s' declares a non-existing credential provider model with id '%s'. It will be ignored.", populationId, modelId));
        }
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _credentialProviderFactory.getExtension(modelId).getParameters();
        
        for (String cpParamName : declaredParameters.keySet())
        {
            Configuration paramConf = conf.getChild(cpParamName, false);
            if (paramConf == null)
            {
                throw new ConfigurationException(String.format("The population of id '%s' declares a credential provider model with id '%s' but the parameter '%s' is missing. This credential provider will be ignored.", populationId, modelId, cpParamName));
            }
            else
            {
                String valueAsString = paramConf.getValue("");
                
                Parameter<ParameterType> parameter = declaredParameters.get(cpParamName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(valueAsString, type);
                parameters.put(cpParamName, typedValue);
                
            }
        }

        return parameters;
    }
    
    /**
     * Erases the config file representing the populations and rebuild it 
     * from the internal representation of the populations.
     * @return True if an error occured
     */
    private boolean _writePopulations()
    {
        File backup = new File(__USER_POPULATIONS_FILE.getPath() + ".tmp");
        boolean errorOccured = false;
        
        // Create a backup file
        try
        {
            Files.copy(__USER_POPULATIONS_FILE.toPath(), backup.toPath());
        }
        catch (IOException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when creating backup '" + __USER_POPULATIONS_FILE + "' file", e);
            }
        }
        
        // Do writing
        try (OutputStream os = new FileOutputStream(__USER_POPULATIONS_FILE))
        {
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
            
            StreamResult result = new StreamResult(os);
            th.setResult(result);

            // create the format of result
            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.INDENT, "yes");
            format.put(OutputKeys.ENCODING, "UTF-8");
            format.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
            th.getTransformer().setOutputProperties(format);

            // sax the config
            try
            {
                _toSAX(th);
            }
            catch (Exception e)
            {
                if (getLogger().isErrorEnabled())
                {
                    getLogger().error("Error when saxing the userPopulations", e);
                }
                errorOccured = true;
            }
        }
        catch (IOException | TransformerConfigurationException | TransformerFactoryConfigurationError e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when trying to modify the user populations with the configuration file " + __USER_POPULATIONS_FILE, e);
            }
        }
        
        // Restore the file if an error previously occured
        try
        {
            if (errorOccured)
            {
                // An error occured, restore the original
                Files.copy(backup.toPath(), __USER_POPULATIONS_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Force to reread the file
                _readPopulations(true);
            }
            Files.deleteIfExists(backup.toPath());
        }
        catch (IOException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when restoring backup '" + __USER_POPULATIONS_FILE + "' file", e);
            }
        }
        
        return errorOccured;
    }
    
    private void _toSAX(TransformerHandler handler)
    {
        try
        {
            handler.startDocument();
            XMLUtils.startElement(handler, "userPopulations");
            for (UserPopulation up : _userPopulations.values())
            {
                _saxUserPopulation(up, handler);
            }
            
            XMLUtils.endElement(handler, "userPopulations");
            handler.endDocument();
        }
        catch (SAXException e)
        {
            getLogger().error("Error when saxing the userPopulations", e);
        }
    }
    
    private void _saxUserPopulation(UserPopulation userPopulation, TransformerHandler handler)
    {
        try
        {
            AttributesImpl atts = new AttributesImpl();
            atts.addCDATAAttribute("id", userPopulation.getId());
            atts.addCDATAAttribute("enabled", Boolean.toString(userPopulation.isEnabled()));
            XMLUtils.startElement(handler, "userPopulation", atts);
            
            userPopulation.getLabel().toSAX(handler, "label");
            
            // SAX user directories
            XMLUtils.startElement(handler, "userDirectories");
            for (UserDirectory ud : userPopulation.getUserDirectories())
            {
                AttributesImpl attr = new AttributesImpl();
                attr.addCDATAAttribute("modelId", ud.getUserDirectoryModelId());
                XMLUtils.startElement(handler, "userDirectory", attr);
                
                Map<String, Object> paramValues = ud.getParameterValues();
                for (String paramName : paramValues.keySet())
                {
                    Object value = paramValues.get(paramName);
                    XMLUtils.createElement(handler, paramName, ParameterHelper.valueToString(value));
                }
                XMLUtils.endElement(handler, "userDirectory");
            }
            XMLUtils.endElement(handler, "userDirectories");
            
            // SAX credential providers
            XMLUtils.startElement(handler, "credentialProviders");
            for (CredentialProvider cp : userPopulation.getCredentialProviders())
            {
                AttributesImpl attr = new AttributesImpl();
                attr.addCDATAAttribute("modelId", cp.getCredentialProviderModelId());
                XMLUtils.startElement(handler, "credentialProvider", attr);
                
                Map<String, Object> paramValues = cp.getParameterValues();
                for (String paramName : paramValues.keySet())
                {
                    Object value = paramValues.get(paramName);
                    XMLUtils.createElement(handler, paramName, ParameterHelper.valueToString(value));
                }
                XMLUtils.endElement(handler, "credentialProvider");
            }
            XMLUtils.endElement(handler, "credentialProviders");
            
            XMLUtils.endElement(handler, "userPopulation");
        }
        catch (SAXException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when saxing the userPopulation " + userPopulation, e);
            }
        }
    }
    
    @Override
    public void dispose()
    {
        for (UserPopulation up : _userPopulations.values())
        {
            up.dispose();
        }
    }
}
