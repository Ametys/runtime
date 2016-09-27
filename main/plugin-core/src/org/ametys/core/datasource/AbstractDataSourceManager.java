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
package org.ametys.core.datasource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ObservationConstants;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.util.StringUtils;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Abstract component to handle data source
 */
public abstract class AbstractDataSourceManager extends AbstractLogEnabled implements Component, Initializable, Serviceable
{
    /** The suffix of any default data source */
    public static final String DEFAULT_DATASOURCE_SUFFIX = "default-datasource";
    
    /** The observation manager */
    protected ObservationManager _observationManager;
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;
    
    /** The data source definitions */
    protected Map<String, DataSourceDefinition> _dataSourcesDef;

    private long _lastUpdate;

    private DataSourceConsumerExtensionPoint _dataSourceConsumerEP;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _dataSourceConsumerEP = (DataSourceConsumerExtensionPoint) serviceManager.lookup(DataSourceConsumerExtensionPoint.ROLE);
        _observationManager = (ObservationManager) serviceManager.lookup(ObservationManager.ROLE);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }
    
    @Override
    public void initialize() throws Exception
    {
        _dataSourcesDef = new HashMap<>();
        
        readConfiguration();
        
        // Check parameters and create data source
        for (DataSourceDefinition def : _dataSourcesDef.values())
        {
            // Validate the used data sources if not already in safe mode
            boolean isInUse = _dataSourceConsumerEP.isInUse(def.getId()) || (def.isDefault() && _dataSourceConsumerEP.isInUse(getDefaultDataSourceId()));
            if (!PluginsManager.getInstance().isSafeMode() && isInUse)
            {
                checkParameters (def.getParameters());
            }
            
            createDataSource (def);
        }

        if (getDefaultDataSourceDefinition() == null)
        {
            // Force a default data source at start-up if not present
            internalSetDefaultDataSource();
        }
        
        checkDataSources();
    }
    
    /**
     * Get the file configuration of data sources
     * @return the file
     */
    public abstract File getFileConfiguration();
    
    /**
     * Get the prefix for data source identifier
     * @return the id prefix
     */
    protected abstract String getDataSourcePrefixId();
    
    /**
     * Checks the parameters of a data source
     * @param rawParameters the parameters of the data source
     * @throws ParameterCheckerTestFailureException if the test failed
     */
    public abstract void checkParameters(Map<String, String> rawParameters) throws ParameterCheckerTestFailureException;
    
    /**
     * Creates a data source from its configuration
     * @param dataSource the data source configuration
     */
    protected abstract void createDataSource(DataSourceDefinition dataSource);
    
    /**
     * Edit a data source from its configuration
     * @param dataSource the data source configuration
     */
    protected abstract void editDataSource(DataSourceDefinition dataSource);
    
    /**
     * Deletes a data source
     * @param dataSource the data source configuration
     */
    protected abstract void deleteDataSource(DataSourceDefinition dataSource);
    
    /**
     * Set a default data source internally
     */
    protected abstract void internalSetDefaultDataSource();
    
    /**
     * Get the data source definitions 
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources. Not used by default.
     * @param includeDefault true to include an additional data source definition for each default data source
     * @return the data source definitions
     */
    public Map<String, DataSourceDefinition> getDataSourceDefinitions(boolean includePrivate, boolean includeInternal, boolean includeDefault)
    {
        readConfiguration();
        
        Map<String, DataSourceDefinition> dataSourceDefinitions = new HashMap<> ();
        if (includeDefault)
        {
            DataSourceDefinition defaultDataSourceDefinition = getDefaultDataSourceDefinition();
            if (defaultDataSourceDefinition != null)
            {
                dataSourceDefinitions.put(getDefaultDataSourceId(), defaultDataSourceDefinition);
            }
        }
        
        if (includePrivate)
        {
            dataSourceDefinitions.putAll(_dataSourcesDef);
            return dataSourceDefinitions;
        }
        else
        {
            Map<String, DataSourceDefinition> publicDatasources = new HashMap<>();
            for (DataSourceDefinition definition : _dataSourcesDef.values())
            {
                if (!definition.isPrivate())
                {
                    publicDatasources.put(definition.getId(), definition);
                }
            }
            
            dataSourceDefinitions.putAll(publicDatasources);
            return dataSourceDefinitions;
        }
    }
    
    /**
     * Get the data source definition or null if not found
     * @param id the id of data source
     * @return the data source definition or null if not found
     */
    public DataSourceDefinition getDataSourceDefinition(String id)
    {
        readConfiguration();
        
        if (getDefaultDataSourceId().equals(id))
        {
            return getDefaultDataSourceDefinition();
        }
        
        return _dataSourcesDef.get(id);
    }
    
    /**
     * Add a data source
     * @param name the name
     * @param description the description 
     * @param parameters the parameters
     * @param isPrivate true if private
     * @return the created data source definition
     */
    public DataSourceDefinition add(I18nizableText name, I18nizableText description, Map<String, Object> parameters, boolean isPrivate)
    {
        readConfiguration();
        
        Map<String, String> rawParameters = new HashMap<>();
        for (String paramName : parameters.keySet())
        {
            rawParameters.put(paramName, ParameterHelper.valueToString(parameters.get(paramName)));
        }
        
        String id = getDataSourcePrefixId() + StringUtils.generateKey();
        DataSourceDefinition ds = new DataSourceDefinition(id, name, description, rawParameters, isPrivate, false);
        _dataSourcesDef.put(id, ds);
        
        saveConfiguration();
        
        createDataSource(ds);
        
        if (getDataSourceDefinitions(true, true, false).size() == 1)
        {
            internalSetDefaultDataSource();
        }
        
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(ObservationConstants.ARGS_DATASOURCE_IDS, Collections.singletonList(ds.getId()));
        _observationManager.notify(new Event(ObservationConstants.EVENT_DATASOURCE_ADDED, _currentUserProvider.getUser(), eventParams));
        
        return ds;
    }
    
    /**
     * Edit a data source
     * @param id the id
     * @param name the name
     * @param description the description 
     * @param parameters the parameters
     * @param isPrivate true if private
     * @return the edited data source definition
     */
    public DataSourceDefinition edit(String id, I18nizableText name, I18nizableText description, Map<String, Object> parameters, boolean isPrivate)
    {
        readConfiguration();
        
        if (_dataSourcesDef.containsKey(id))
        {
            Map<String, String> rawParameters = new HashMap<>();
            for (String paramName : parameters.keySet())
            {
                rawParameters.put(paramName, ParameterHelper.valueToString(parameters.get(paramName)));
            }
            
            boolean isDefault = _dataSourcesDef.get(id).isDefault();
            DataSourceDefinition ds = new DataSourceDefinition(id, name, description, rawParameters, isPrivate, isDefault);
            _dataSourcesDef.put(id, ds);
            
            saveConfiguration();
            
            editDataSource(ds);
            
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(ObservationConstants.ARGS_DATASOURCE_IDS, Collections.singletonList(ds.getId()));
            _observationManager.notify(new Event(ObservationConstants.EVENT_DATASOURCE_UPDATED, _currentUserProvider.getUser(), eventParams));
            
            return ds;
        }
        
        throw new RuntimeException("The data source with id '" + id + "' was not found. Unable to edit it.");
    }
    
    /**
     * Delete data sources
     * @param dataSourceIds the ids of the data sources to delete
     */
    public void delete(List<String> dataSourceIds)
    {
        readConfiguration();
        
        for (String id : dataSourceIds)
        {
            DataSourceDefinition dataSourceDef = _dataSourcesDef.get(id);
            if (_dataSourceConsumerEP.isInUse(id) || (dataSourceDef.isDefault() && _dataSourceConsumerEP.isInUse(getDefaultDataSourceId())))
            {
                throw new IllegalStateException("The data source '" + id + "' is currently in use. The deletion process has been aborted.");
            }
            
            if (id.equals(SQLDataSourceManager.AMETYS_INTERNAL_DATASOURCE_ID))
            {
                throw new IllegalStateException("The data source '" + id + "' is an internal data source. The deletion process has been aborted.");
            }
            
            deleteDataSource (dataSourceDef);
            _dataSourcesDef.remove(id);
        }
        
        saveConfiguration();
        
        if (getDataSourceDefinitions(true, true, false).size() == 1)
        {
            internalSetDefaultDataSource();
        }
        
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(ObservationConstants.ARGS_DATASOURCE_IDS, dataSourceIds);
        _observationManager.notify(new Event(ObservationConstants.EVENT_DATASOURCE_DELETED, _currentUserProvider.getUser(), eventParams));
    }
    
    /**
     * Set the data source with the given id as the default data source
     * @param id the id of the data source
     * @return the {@link DataSourceDefinition} of the data source set as default 
     */
    public DataSourceDefinition setDefaultDataSource(String id)
    {
        readConfiguration();
        
        if (!id.startsWith(getDataSourcePrefixId()))
        {
            throw new RuntimeException("The data source with id '" + id + "' is not of the appropriate type to set is as default.");
        }

        // Remove the default attribute from the previous default data source (if any)
        DataSourceDefinition oldDefaultDataSource = getDefaultDataSourceDefinition();
        if (oldDefaultDataSource != null)
        {
            oldDefaultDataSource.setDefault(false);
            _dataSourcesDef.put(oldDefaultDataSource.getId(), oldDefaultDataSource);
            
            saveConfiguration();
            editDataSource(oldDefaultDataSource);
        } 
        
        if (_dataSourcesDef.containsKey(id))
        {
            // Set the data source as the default one
            DataSourceDefinition newDefaultDataSource = getDataSourceDefinition(id);
            newDefaultDataSource.setDefault(true);
            _dataSourcesDef.put(id, newDefaultDataSource);
            
            saveConfiguration();
            editDataSource(newDefaultDataSource);
            
            return newDefaultDataSource;
        }
        
        throw new RuntimeException("The data source with id '" + id + "' was not found. Unable to set it as the default data source.");
    }
    
    /**
     * Get the default data source for this type
     * @return the definition object of the default data source. Can return null if no datasource is defined. 
     */
    public DataSourceDefinition getDefaultDataSourceDefinition()
    {
        List<DataSourceDefinition> defaultDataSourceDefinitions = new ArrayList<> ();
        for (DataSourceDefinition definition : _dataSourcesDef.values())
        {
            if (definition.getId().startsWith(getDataSourcePrefixId()) && definition.isDefault())
            {
                defaultDataSourceDefinitions.add(definition);
            }
        }
        
        if (defaultDataSourceDefinitions.isEmpty())
        {
            return null;
        }
        else if (defaultDataSourceDefinitions.size() > 1)
        {
            throw new IllegalStateException("Found more than one default data source definition.");
        }
        else
        {
            return defaultDataSourceDefinitions.get(0);
        }
    }
    
    /**
     * Get the id of the default data source 
     * @return the id of the default data source
     */
    public String getDefaultDataSourceId()
    {
        return getDataSourcePrefixId() + DEFAULT_DATASOURCE_SUFFIX;
    }
    
    /**
     * Read and update the data sources configuration
     */
    protected void readConfiguration()
    {
        File file = getFileConfiguration();
        if (file.exists() && file.lastModified() > _lastUpdate)
        {
            _lastUpdate = new Date().getTime();
            _dataSourcesDef = readDataSourceDefinition(file);
        }
    }
    
    /**
     * Read the read source definitions 
     * @param file The configuration file
     * @return the data source definitions
     */
    public static Map<String, DataSourceDefinition> readDataSourceDefinition (File file)
    {
        Map<String, DataSourceDefinition> definitions = new HashMap<>();
        
        try
        {
            if (file.exists())
            {
                Configuration configuration = new DefaultConfigurationBuilder().buildFromFile(file);
                for (Configuration dsConfig : configuration.getChildren("datasource"))
                {
                    String id = dsConfig.getAttribute("id");
                    
                    I18nizableText name = I18nizableText.parseI18nizableText(dsConfig.getChild("name"), "plugin.core");
                    I18nizableText description = I18nizableText.parseI18nizableText(dsConfig.getChild("description"), "plugin.core", "");
                    
                    boolean isPrivate = dsConfig.getAttributeAsBoolean("private", false);
                    boolean isDefault = dsConfig.getAttributeAsBoolean("default", false);
                    
                    Map<String, String> parameters = new HashMap<>();
                    
                    Configuration[] paramsConfig = dsConfig.getChild("parameters").getChildren();
                    for (Configuration paramConfig : paramsConfig)
                    {
                        String value = paramConfig.getValue("");
                        parameters.put(paramConfig.getName(), value);
                    }
                    
                    DataSourceDefinition dataSource = new DataSourceDefinition(id, name, description, parameters, isPrivate, isDefault);
                    definitions.put(id, dataSource);
                }
            }
            
            return definitions;
        }
        catch (IOException | ConfigurationException | SAXException e)
        {
            throw new RuntimeException("Unable to parse datasource configuration file.", e);
        }
    }
    
    /**
     * Save the configured data sources 
     */
    protected void saveConfiguration()
    {
        File file = getFileConfiguration();
        try
        {
            // Create file if it does not already exist
            if (!file.exists())
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();

            // create the result where to write
            try (OutputStream os = new FileOutputStream(file))
            {
                StreamResult sResult = new StreamResult(os);
                th.setResult(sResult);
    
                // create the format of result
                Properties format = new Properties();
                format.put(OutputKeys.METHOD, "xml");
                format.put(OutputKeys.INDENT, "yes");
                format.put(OutputKeys.ENCODING, "UTF-8");
                th.getTransformer().setOutputProperties(format);
    
                // Send SAX events
                th.startDocument();
                XMLUtils.startElement(th, "datasources");
                
                for (DataSourceDefinition datasource : _dataSourcesDef.values())
                {
                    saxDataSource(th, datasource);
                }
                
                XMLUtils.endElement(th, "datasources");
                th.endDocument();
            }
        }
        catch (SAXException | IOException | TransformerConfigurationException e)
        {
            throw new RuntimeException("Unable to save the configuration of data sources", e);
        }
    }
    
    /**
     * SAX an instance of data source
     * @param handler the content handler to sax into
     * @param dataSource the data source
     * @throws SAXException if an error occurred while SAXing
     */
    protected void saxDataSource(ContentHandler handler, DataSourceDefinition dataSource) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        
        attrs.addCDATAAttribute("id", dataSource.getId());
        attrs.addCDATAAttribute("private", String.valueOf(dataSource.isPrivate()));
        attrs.addCDATAAttribute("default", String.valueOf(dataSource.isDefault()));
        
        XMLUtils.startElement(handler, "datasource", attrs);
        
        dataSource.getName().toSAX(handler, "name");
        dataSource.getDescription().toSAX(handler, "description");
        
        XMLUtils.startElement(handler, "parameters");
        Map<String, String> parameters = dataSource.getParameters();
        for (String paramName : parameters.keySet())
        {
            String value = parameters.get(paramName);
            XMLUtils.createElement(handler, paramName, value != null ? value : "");
        }
        XMLUtils.endElement(handler, "parameters");
        
        XMLUtils.endElement(handler, "datasource");
    }
    
    /**
     * Check that the used data sources are indeed available 
     */
    protected void checkDataSources()
    {
        Set<String> usedDataSourceIds = _dataSourceConsumerEP.getUsedDataSourceIds();
        for (String dataSourceId : usedDataSourceIds)
        {
            if (dataSourceId != null && dataSourceId.startsWith(getDataSourcePrefixId()) && getDataSourceDefinition(dataSourceId) == null  && !PluginsManager.getInstance().isSafeMode())
            {
                throw new UnknownDataSourceException("The data source '" + dataSourceId + "' was not found in the available data sources.");                
            }
        }
    }
    
    /**
     * This class represents the definition of a data source
     */
    public static class DataSourceDefinition implements Cloneable
    {
        private String _id;
        private I18nizableText _name;
        private I18nizableText _description;
        private Map<String, String> _parameters;
        private boolean _isPrivate;
        private boolean _isDefault;
        
        /**
         * Constructor
         * @param id the id
         * @param name the name
         * @param description the description
         * @param parameters the parameters
         * @param isPrivate true if the data source is a private data source
         * @param isDefault true if the data source is a default data source
         */
        public DataSourceDefinition(String id, I18nizableText name, I18nizableText description, Map<String, String> parameters, boolean isPrivate, boolean isDefault)
        {
            _id = id;
            _name = name;
            _description = description;
            _parameters = parameters;
            _isPrivate = isPrivate;
            _isDefault = isDefault;
        }
        
        /**
         * The id of the data source
         * @return the id of the data source
         */
        public String getId()
        {
            return _id;
        }
        
        /**
         * Get the name of the data source
         * @return the name of the data source
         */
        public I18nizableText getName()
        {
            return _name;
        }
        
        /**
         * Get the description of the data source
         * @return the description of the data source
         */
        public I18nizableText getDescription()
        {
            return _description;
        }
        
        /**
         * Returns true if this data source instance is private
         * @return true if is private
         */
        public boolean isPrivate()
        {
            return _isPrivate;
        }
        
        /**
         * Returns true if this is a default data source
         * @return true if this is a default data source
         */
        public boolean isDefault()
        {
            return _isDefault;
        }
        
        /**
         * Set default or not this data source 
         * @param isDefault true to set this data source as the default one, false otherwise 
         */
        public void setDefault(boolean isDefault)
        {
            _isDefault = isDefault;
        }
        
        /**
         * Get the parameters of the data source definition
         * @return the parameters
         */
        public Map<String, String> getParameters()
        {
            return _parameters;
        }
        
        @Override
        public DataSourceDefinition clone()
        {
            return new DataSourceDefinition(_id, _name, _description, new HashMap<>(_parameters), _isPrivate, _isDefault);
        }
    }
}
