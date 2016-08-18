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
package org.ametys.core.group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;

import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryFactory;
import org.ametys.core.group.directory.GroupDirectoryModel;
import org.ametys.core.ui.Callable;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.Status;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.util.AmetysHomeHelper;

/**
 * DAO for accessing {@link GroupDirectory}
 */
public class GroupDirectoryDAO extends AbstractLogEnabled implements Component, Initializable, Serviceable, Disposable
{
    /** Avalon Role */
    public static final String ROLE = GroupDirectoryDAO.class.getName();
    
    /** The path of the XML file containing the group directories */
    private static final File __GROUP_DIRECTORIES_FILE = new File(AmetysHomeHelper.getAmetysHome(), "config" + File.separator + "group-directories.xml");
    
    /** The regular expression for an id of a group directory */
    private static final String __ID_REGEX = "^[a-z][a-z0-9_-]*";
    
    /** The date of the last update of the XML file */
    private long _lastUpdate;
    
    /** The whole group directories of the application */
    private Map<String, GroupDirectory> _groupDirectories;
    
    /** The factory for group directories */
    private GroupDirectoryFactory _groupDirectoryFactory;
    
    @Override
    public void initialize()
    {
        _groupDirectories = new LinkedHashMap<>();
        _lastUpdate = 0;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _groupDirectoryFactory = (GroupDirectoryFactory) manager.lookup(GroupDirectoryFactory.ROLE);
    }
    
    /**
     * Gets all the group directories to JSON format
     * @return A list of object representing the {@link GroupDirectory GroupDirectories}
     */
    public List<Object> getGroupDirectories2Json()
    {
        List<Object> result = new ArrayList<>();
        for (GroupDirectory groupDirectory : getGroupDirectories())
        {
            result.add(getGroupDirectory2Json(groupDirectory));
        }
        return result;
    }
    
    /**
     * gets a group directory to JSON format
     * @param groupDirectory The group directory to get
     * @return An object representing a {@link GroupDirectory}
     */
    public Map<String, Object> getGroupDirectory2Json(GroupDirectory groupDirectory)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", groupDirectory.getId());
        result.put("label", groupDirectory.getLabel());
        String modelId = groupDirectory.getGroupDirectoryModelId();
        GroupDirectoryModel model = _groupDirectoryFactory.getExtension(modelId);
        result.put("modelLabel", model.getLabel());
        return result;
    }
    
    /**
     * Gets all the group directories of this application
     * @return A list of {@link GroupDirectory GroupDirectories}
     */
    public List<GroupDirectory> getGroupDirectories()
    {
        // Don't read in safe mode, we know that only the admin population is needed in this case and we want to prevent some warnings in the logs for non-safe features not found
        if (Status.OK.equals(PluginsManager.getInstance().getStatus()))
        {
            _read(false);
            return new ArrayList<>(_groupDirectories.values());
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * Gets a group directory by its id.
     * @param id The id of the group directory
     * @return The {@link GroupDirectory}, or null if not found
     */
    public GroupDirectory getGroupDirectory(String id)
    {
        _read(false);
        return _groupDirectories.get(id);
    }
    
    /**
     * Gets the list of the ids of all the group directories of the application
     * @return The list of the ids of all the group directories
     */
    @Callable
    public Set<String> getGroupDirectoriesIds()
    {
        _read(false);
        return _groupDirectories.keySet();
    }
    
    /**
     * Gets the configuration for creating/editing a group directory.
     * @return A map containing information about what is needed to create/edit a group directory
     * @throws Exception If an error occurs.
     */
    @Callable
    public Map<String, Object> getEditionConfiguration() throws Exception
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        List<Object> groupDirectoryModels = new ArrayList<>();
        for (String extensionId : _groupDirectoryFactory.getExtensionsIds())
        {
            GroupDirectoryModel model = _groupDirectoryFactory.getExtension(extensionId);
            Map<String, Object> gdMap = new LinkedHashMap<>();
            gdMap.put("id", extensionId);
            gdMap.put("label", model.getLabel());
            gdMap.put("description", model.getDescription());
            
            Map<String, Object> params = new LinkedHashMap<>();
            for (String paramId : model.getParameters().keySet())
            {
                // prefix in case of two parameters from two different models have the same id which can lead to some errorsin client-side
                params.put(extensionId + "$" + paramId, ParameterHelper.toJSON(model.getParameters().get(paramId)));
            }
            gdMap.put("parameters", params);
            
            groupDirectoryModels.add(gdMap);
        }
        result.put("groupDirectoryModels", groupDirectoryModels);
        
        return result;
    }
    
    /**
     * Gets the values of the parameters of the given group directory
     * @param id The id of the group directory
     * @return The values of the parameters
     */
    @Callable
    public Map<String, Object> getGroupDirectoryParameterValues(String id)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        _read(false);
        GroupDirectory gd = _groupDirectories.get(id);
        
        if (gd == null)
        {
            getLogger().error("The GroupDirectory of id '{}' does not exist.", id);
            result.put("error", "unknown");
            return result;
        }
        
        result.put("label", gd.getLabel());
        result.put("id", gd.getId());
        String modelId = gd.getGroupDirectoryModelId();
        result.put("modelId", modelId);
        Map<String, Object> params = new HashMap<>();
        for (String key : gd.getParameterValues().keySet())
        {
            params.put(modelId + "$" + key, gd.getParameterValues().get(key));
        }
        result.put("params", params);
        
        return result;
    }
    
    /**
     * Adds a new group directory
     * @param id The unique id of the group directory
     * @param label The label of the group directory
     * @param modelId The id of the group directory model
     * @param params The parameters of the group directory
     * @return The id of the created group directory, or null if an error occured
     */
    @Callable
    public String add(String id, String label, String modelId, Map<String, String> params)
    {
        _read(false);
        
        if (!_isCorrectId(id))
        {
            return null;
        }
        
        GroupDirectory gd = _createGroupDirectory(id, label, modelId, params);
        
        _groupDirectories.put(id, gd);
        if (_write())
        {
            return null;
        }
        
        return id;
    }
    
    private boolean _isCorrectId(String id)
    {
        if (_groupDirectories.get(id) != null)
        {
            getLogger().error("The id '{}' is already used for a group directory.", id);
            return false;
        }
        
        if (!Pattern.matches(__ID_REGEX, id))
        {
            getLogger().error("The id '{}' is not a correct id for a group directory.", id);
            return false;
        }
        
        return true;
    }
    
    /**
     * Edits the given group directory
     * @param id The id of the group directory to edit
     * @param label The label of the group directory
     * @param modelId The id of the group directory model
     * @param params The parameters of the group directory
     * @return A map containing the id of the edited group directory
     */
    @Callable
    public Map<String, Object> edit(String id, String label, String modelId, Map<String, String> params)
    {
        _read(false);
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        GroupDirectory gd = _groupDirectories.get(id);
        if (gd == null)
        {
            getLogger().error("The GroupDirectory with id '{}' does not exist, it cannot be edited.", id);
            result.put("error", "unknown");
            return result;
        }
        else
        {
            _groupDirectories.remove(id);
        }
        
        GroupDirectory newGd = _createGroupDirectory(id, label, modelId, params);
        _groupDirectories.put(id, newGd);
        if (_write())
        {
            return null;
        }
        
        result.put("id", id);
        return result;
    }
    
    private GroupDirectory _createGroupDirectory(String id, String label, String modelId, Map<String, String> params)
    {
        Map<String, Object> typedParams = _getTypedParams(params, modelId);
        return _groupDirectoryFactory.createGroupDirectory(id, new I18nizableText(label), modelId, typedParams);
    }
    
    private Map<String, Object> _getTypedParams(Map<String, String> params, String modelId)
    {
        Map<String, Object> resultParameters = new LinkedHashMap<>();
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _groupDirectoryFactory.getExtension(modelId).getParameters();
        for (String paramNameWithPrefix : params.keySet())
        {
            String[] splitStr = paramNameWithPrefix.split("\\$", 2);
            String prefix = splitStr[0];
            String paramName = splitStr[1];
            if (prefix.equals(modelId) && declaredParameters.containsKey(paramName))
            {
                String originalValue = params.get(paramNameWithPrefix);
                
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
     * Removes the given group directory
     * @param id The id of the group directory to remove
     * @return A map containing the id of the removed group directory
     */
    @Callable
    public Map<String, Object> remove(String id)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        _read(false);
        if (_groupDirectories.remove(id) == null)
        {
            getLogger().error("The GroupDirectory with id '{}' does not exist, it cannot be removed.", id);
            result.put("error", "unknown");
            return result;
        }
        if (_write())
        {
            return null;
        }
        
        result.put("id", id);
        return result;
    }
    
    /**
     * If needed, reads the config file representing the group directories and then
     * reinitializes and updates the internal representation of the group directories.
     * @param forceRead True to avoid the use of the cache and force the reading of the file
     */
    private void _read(boolean forceRead)
    {
        try
        {
            if (!__GROUP_DIRECTORIES_FILE.exists())
            {
                _createDirectoriesFile(__GROUP_DIRECTORIES_FILE);
            }
            
            if (forceRead || __GROUP_DIRECTORIES_FILE.lastModified() > _lastUpdate)
            {
                _lastUpdate = new Date().getTime();
                _groupDirectories = new LinkedHashMap<>();
                
                Configuration cfg = new DefaultConfigurationBuilder().buildFromFile(__GROUP_DIRECTORIES_FILE);
                for (Configuration childCfg : cfg.getChildren("groupDirectory"))
                {
                    try
                    {
                        _configureGroupDirectory(childCfg);
                    }
                    catch (ConfigurationException e)
                    {
                        getLogger().error("Error configuring the group directory '" + childCfg.getAttribute("id", "") + "'. The group directory will be ignored.", e);
                    }
                }
            }
        }
        catch (IOException | TransformerConfigurationException | ConfigurationException | SAXException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error retrieving group directories with the configuration file " + __GROUP_DIRECTORIES_FILE, e);
            }
        }
    }
    
    private void _createDirectoriesFile(File file) throws IOException, TransformerConfigurationException, SAXException
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
            XMLUtils.createElement(th, "groupDirectories");
            th.endDocument();
        }
    }
    
    private void _configureGroupDirectory(Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        String modelId = configuration.getAttribute("modelId");
        I18nizableText label = new I18nizableText(configuration.getChild("label").getValue());
        Map<String, Object> paramValues = _getParametersFromConfiguration(configuration.getChild("params"), modelId);
        if (paramValues != null)
        {
            GroupDirectory gd = _groupDirectoryFactory.createGroupDirectory(id, label, modelId, paramValues);
            if (gd != null)
            {
                _groupDirectories.put(id, gd);
            }
        }
    }
    
    private Map<String, Object> _getParametersFromConfiguration(Configuration conf, String modelId)
    {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        if (!_groupDirectoryFactory.hasExtension(modelId))
        {
            getLogger().warn("The model id '{}' is referenced in the file containing the group directories but seems to not exist.", modelId);
            return null;
        }
        
        Map<String, ? extends Parameter<ParameterType>> declaredParameters = _groupDirectoryFactory.getExtension(modelId).getParameters();
        for (Configuration paramConf : conf.getChildren())
        {
            String paramName = paramConf.getName();
            if (declaredParameters.containsKey(paramName))
            {
                String valueAsString = paramConf.getValue("");
                
                Parameter<ParameterType> parameter = declaredParameters.get(paramName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(valueAsString, type);
                parameters.put(paramName, typedValue);
            }
            else
            {
                getLogger().warn("The parameter '{}' is not declared in extension '{}'. It will be ignored", paramName, modelId);
            }
        }
        
        return parameters;
    }
    
    /**
     * Erases the config file representing the group directories and rebuild it 
     * from the internal representation of the group directories.
     * @return True if an error occured
     */
    private boolean _write()
    {
        File backup = new File(__GROUP_DIRECTORIES_FILE.getPath() + ".tmp");
        boolean errorOccured = false;
        
        // Create a backup file
        try
        {
            Files.copy(__GROUP_DIRECTORIES_FILE.toPath(), backup.toPath());
        }
        catch (IOException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when creating backup '" + __GROUP_DIRECTORIES_FILE + "' file", e);
            }
        }
        
        // Do writing
        try (OutputStream os = new FileOutputStream(__GROUP_DIRECTORIES_FILE))
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
                    getLogger().error("Error when saxing the groupDirectories", e);
                }
                errorOccured = true;
            }
        }
        catch (IOException | TransformerConfigurationException | TransformerFactoryConfigurationError e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when trying to modify the group directories with the configuration file " + __GROUP_DIRECTORIES_FILE, e);
            }
        }
        
        // Restore the file if an error previously occured
        try
        {
            if (errorOccured)
            {
                // An error occured, restore the original
                Files.copy(backup.toPath(), __GROUP_DIRECTORIES_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Force to reread the file
                _read(true);
            }
            Files.deleteIfExists(backup.toPath());
        }
        catch (IOException e)
        {
            if (getLogger().isErrorEnabled())
            {
                getLogger().error("Error when restoring backup '" + __GROUP_DIRECTORIES_FILE + "' file", e);
            }
        }
        
        return errorOccured;
    }
    
    private void _toSAX(TransformerHandler handler) throws SAXException
    {
        handler.startDocument();
        XMLUtils.startElement(handler, "groupDirectories");
        for (GroupDirectory gd : _groupDirectories.values())
        {
            _saxGroupDirectory(gd, handler);
        }
        
        XMLUtils.endElement(handler, "groupDirectories");
        handler.endDocument();
    }
    
    private void _saxGroupDirectory(GroupDirectory groupDirectory, TransformerHandler handler) throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addCDATAAttribute("id", groupDirectory.getId());
        atts.addCDATAAttribute("modelId", groupDirectory.getGroupDirectoryModelId());
        XMLUtils.startElement(handler, "groupDirectory", atts);
        
        groupDirectory.getLabel().toSAX(handler, "label");
        
        XMLUtils.startElement(handler, "params");
        Map<String, Object> paramValues = groupDirectory.getParameterValues();
        for (String paramName : paramValues.keySet())
        {
            Object value = paramValues.get(paramName);
            XMLUtils.createElement(handler, paramName, ParameterHelper.valueToString(value));
        }
        XMLUtils.endElement(handler, "params");
        
        XMLUtils.endElement(handler, "groupDirectory");
    }
    
    @Override
    public void dispose()
    {
        for (GroupDirectory gd : _groupDirectories.values())
        {
            LifecycleHelper.dispose(gd);
        }
    }
}
