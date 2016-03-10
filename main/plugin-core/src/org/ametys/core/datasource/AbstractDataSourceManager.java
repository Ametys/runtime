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
    /** The data source definitions */
    protected Map<String, DataSourceDefinition> _dataSourcesDef;

    private long _lastUpdate;

    private DataSourceConsumerExtensionPoint _dataSourceConsumerEP;
    
    @Override
    public void initialize() throws Exception
    {
        _dataSourcesDef = new HashMap<>();
        
        readConfiguration(true);
        checkDataSources();
    }
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _dataSourceConsumerEP = (DataSourceConsumerExtensionPoint) serviceManager.lookup(DataSourceConsumerExtensionPoint.ROLE);
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
     * @param dataSource the data source to check
     * @throws ParameterCheckerTestFailureException if parameters test failed
     */
    public abstract void checkParameters(DataSourceDefinition dataSource) throws ParameterCheckerTestFailureException;
    
    /**
     * Checks the parameters of a data source
     * @param rawParameters the data source parameters
     * @throws ParameterCheckerTestFailureException if parameters test failed
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
     * Get the data source definitions 
     * @param includePrivate true to include private data sources
     * @param includeInternal true to include internal data sources. Not used by default.
     * @return the data source definitions
     */
    public Map<String, DataSourceDefinition> getDataSourceDefinitions(boolean includePrivate, boolean includeInternal)
    {
        readConfiguration(false);
        
        if (includePrivate)
        {
            return _dataSourcesDef;
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
            
            return publicDatasources;
        }
    }
    
    /**
     * Get the data source definition or null if not found
     * @param id the id of data source
     * @return the data source definition or null if not found
     */
    public DataSourceDefinition getDataSourceDefinition(String id)
    {
        readConfiguration(false);
        return _dataSourcesDef.get(id);
    }
    
    /**
     * Add a data source
     * @param name the name
     * @param description the description 
     * @param parameters the parameters
     * @param isPrivate true if private
     * @return the created data sourec definition
     */
    public DataSourceDefinition add(I18nizableText name, I18nizableText description, Map<String, Object> parameters, boolean isPrivate)
    {
        readConfiguration(false);
        
        Map<String, String> rawParameters = new HashMap<>();
        for (String paramName : parameters.keySet())
        {
            rawParameters.put(paramName, ParameterHelper.valueToString(parameters.get(paramName)));
        }
        
        String id = getDataSourcePrefixId() + StringUtils.generateKey();
        DataSourceDefinition ds = new DataSourceDefinition(id, name, description, rawParameters, isPrivate);
        _dataSourcesDef.put(id, ds);
        
        saveConfiguration();
        
        createDataSource(ds);
        
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
        readConfiguration(false);
        
        if (_dataSourcesDef.containsKey(id))
        {
            Map<String, String> rawParameters = new HashMap<>();
            for (String paramName : parameters.keySet())
            {
                rawParameters.put(paramName, ParameterHelper.valueToString(parameters.get(paramName)));
            }
            
            DataSourceDefinition ds = new DataSourceDefinition(id, name, description, rawParameters, isPrivate);
            _dataSourcesDef.put(id, ds);
            
            saveConfiguration();
            
            editDataSource(ds);
            
            return ds;
        }
        
        throw new RuntimeException("The data source with id '" + id + "' was not found. Unable to edit it.");
    }
    
    /**
     * Delete data sources
     * @param dataSourceIds the ids of data sources to delete
     */
    public void delete(List<String> dataSourceIds)
    {
        readConfiguration(false);
        
        for (String id : dataSourceIds)
        {
            if (_dataSourceConsumerEP.isInUse(id))
            {
                throw new RuntimeException("The data source '" + id + "' is currently in use. The deletion process has been aborted.");
            }
            deleteDataSource (_dataSourcesDef.get(id));
            _dataSourcesDef.remove(id);
        }
        
        saveConfiguration();
    }
    
    /**
     * Read and update the data sources configuration
     * @param checkParameters true to test parameters while reading configuration.
     */
    protected void readConfiguration(boolean checkParameters)
    {
        try
        {
            File file = getFileConfiguration();
            if (file.exists() && file.lastModified() > _lastUpdate)
            {
                _lastUpdate = new Date().getTime();
                _dataSourcesDef = new HashMap<>();
                
                Configuration configuration = new DefaultConfigurationBuilder().buildFromFile(file);
                for (Configuration dsConfig : configuration.getChildren("datasource"))
                {
                    String id = dsConfig.getAttribute("id");
                    
                    I18nizableText name = I18nizableText.parseI18nizableText(dsConfig.getChild("name"), "plugin.core");
                    I18nizableText description = I18nizableText.parseI18nizableText(dsConfig.getChild("description"), "plugin.core", "");
                    
                    boolean isPrivate = dsConfig.getAttributeAsBoolean("private", false);
                    
                    Map<String, String> parameters = new HashMap<>();
                    
                    Configuration[] paramsConfig = dsConfig.getChild("parameters").getChildren();
                    for (Configuration paramConfig : paramsConfig)
                    {
                        String value = paramConfig.getValue("");
                        parameters.put(paramConfig.getName(), value);
                    }
                    
                    DataSourceDefinition dataSource = new DataSourceDefinition(id, name, description, parameters, isPrivate);
                    _dataSourcesDef.put(id, dataSource);
                    
                    // Validate the used SQL data sources if not already in safe mode
                    if (checkParameters && !PluginsManager.getInstance().isSafeMode() && _dataSourceConsumerEP.isInUse(id))
                    {
                        checkParameters (dataSource);
                    }
                    
                    createDataSource (dataSource);
                }
            }
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
    public class DataSourceDefinition 
    {
        private String _id;
        private I18nizableText _name;
        private I18nizableText _description;
        private Map<String, String> _parameters;
        private boolean _isPrivate;
        
        /**
         * Constructor
         * @param id the id
         * @param name the name
         * @param description the description
         * @param parameters the parameters
         * @param isPrivate true if the data source is a private data source
         */
        public DataSourceDefinition(String id, I18nizableText name, I18nizableText description, Map<String, String> parameters, boolean isPrivate)
        {
            _id = id;
            _name = name;
            _description = description;
            _parameters = parameters;
            _isPrivate = isPrivate;
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
        public boolean isPrivate ()
        {
            return _isPrivate;
        }
        
        /**
         * Get the parameters of the data source definition
         * @return the parameters
         */
        public Map<String, String> getParameters()
        {
            return _parameters;
        }
    }
}
