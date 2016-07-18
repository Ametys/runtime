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
package org.ametys.plugins.core.impl.userpref;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.userpref.DefaultUserPreferencesStorage;
import org.ametys.core.userpref.UserPreferencesException;
import org.ametys.core.userpref.UserPrefsHandler;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * The JDBC implementation of {@link DefaultUserPreferencesStorage}.
 * This implementation stores preferences in a database, as an XML file.
 */
public class JdbcXmlUserPreferencesStorage extends AbstractLogEnabled implements DefaultUserPreferencesStorage, ThreadSafe, Configurable, Serviceable
{
    /** A SAX parser. */
    protected SAXParser _saxParser;
    
    /** The id of the data source used. */
    protected String _dataSourceId;
    
    /** The database table in which the preferences are stored. */
    protected String _databaseTable;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
     // Data source id
        Configuration dataSourceConf = configuration.getChild("datasource", false);
        if (dataSourceConf == null)
        {
            throw new ConfigurationException("The 'datasource' configuration node must be defined.", dataSourceConf);
        }
        
        String dataSourceConfParam = dataSourceConf.getValue();
        String dataSourceConfType = dataSourceConf.getAttribute("type", "config");
        
        if (StringUtils.equals(dataSourceConfType, "config"))
        {
            _dataSourceId = Config.getInstance().getValueAsString(dataSourceConfParam);
        }
        else // expecting type="id"
        {
            _dataSourceId = dataSourceConfParam;
        }
        
        _databaseTable = configuration.getChild("table").getValue();
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
    }
    
    @Override
    public Map<String, String> getUnTypedUserPrefs(UserIdentity user, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Map<String, String> prefs = new HashMap<>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        @SuppressWarnings("resource") InputStream dataIs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_dataSourceId);
            String dbType = ConnectionHelper.getDatabaseType(connection);
            
            stmt = connection.prepareStatement("SELECT * FROM " + _databaseTable + " WHERE login = ? AND population = ? AND context = ?");
            
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            stmt.setString(3, storageContext);
            
            rs = stmt.executeQuery();
            
            if (rs.next())
            {
                if (ConnectionHelper.DATABASE_POSTGRES.equals(dbType))
                {
                    dataIs = rs.getBinaryStream("data");
                }
                else
                {
                    Blob data = rs.getBlob("data");
                    dataIs = data.getBinaryStream();
                }
                
                // Create the handler and fill the Map by parsing the configuration.
                UserPrefsHandler handler = new UserPrefsHandler(prefs);
                _saxParser.parse(new InputSource(dataIs), handler);
            }
            
            return prefs;
        }
        catch (SQLException e)
        {
            String message = "Database error trying to access the preferences of user '" + user + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        catch (SAXException | IOException e)
        {
            String message = "Error parsing the preferences of user '" + user + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            IOUtils.closeQuietly(dataIs);
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    @Override
    public void removeUserPreferences(UserIdentity user, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_dataSourceId);
            
            stmt = connection.prepareStatement("DELETE FROM " + _databaseTable + " WHERE login = ? AND population = ? AND context = ?");
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            stmt.setString(3, storageContext);
            
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            String message = "Database error trying to remove preferences for login '" + user + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    @Override
    public void setUserPreferences(UserIdentity user, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException
    {
        byte[] prefBytes = _getPreferencesXmlBytes(preferences);
        Connection connection = null;
        
        try (InputStream dataIs = new ByteArrayInputStream(prefBytes);)
        {
            
            connection = ConnectionHelper.getConnection(_dataSourceId);
            
            String dbType = ConnectionHelper.getDatabaseType(connection);
            
            // Test if the preferences already exist.
            boolean dataExists;
            try (PreparedStatement stmt = connection.prepareStatement("SELECT count(*) FROM " + _databaseTable + " WHERE login = ? AND population = ? AND context = ?"))
            {
                stmt.setString(1, user.getLogin());
                stmt.setString(2, user.getPopulationId());
                stmt.setString(3, storageContext);
                
                try (ResultSet rs = stmt.executeQuery())
                {
                    rs.next();
                    dataExists = rs.getInt(1) > 0;
                }
            }
            
            if (dataExists)
            {
                // If there's already a record, update it with the new data.
                try (PreparedStatement stmt = connection.prepareStatement("UPDATE " + _databaseTable + " SET data = ? WHERE login = ? AND population = ? AND context = ?"))
                {
                    if (ConnectionHelper.DATABASE_POSTGRES.equals(dbType) || ConnectionHelper.DATABASE_ORACLE.equals(dbType))
                    {
                        stmt.setBinaryStream(1, dataIs, prefBytes.length);
                    }
                    else
                    {
                        stmt.setBlob(1, dataIs, prefBytes.length);
                    }
                    
                    stmt.setString(2, user.getLogin());
                    stmt.setString(3, user.getPopulationId());
                    stmt.setString(4, storageContext);
                    
                    stmt.executeUpdate();
                }
            }
            else
            {
                // If not, insert the data.
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + _databaseTable + "(login, population, context, data) VALUES(?, ?, ?, ?)"))
                {
                    stmt.setString(1, user.getLogin());
                    stmt.setString(2, user.getPopulationId());
                    stmt.setString(3, storageContext);
                    if (ConnectionHelper.DATABASE_POSTGRES.equals(dbType) || ConnectionHelper.DATABASE_ORACLE.equals(dbType))
                    {
                        stmt.setBinaryStream(4, dataIs, prefBytes.length);
                    }
                    else
                    {
                        stmt.setBlob(4, dataIs);
                    }
                    
                    stmt.executeUpdate();
                }
            }
            
        }
        catch (SQLException | IOException e)
        {
            String message = "Database error trying to access the preferences of user '" + user + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public String getUserPreferenceAsString(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        String value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(user, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = values.get(id);
        }
        
        return value;
    }
    
    @Override
    public Long getUserPreferenceAsLong(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        Long value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(user, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Long) ParameterHelper.castValue(values.get(id), ParameterType.LONG);
        }
        
        return value;

    }
    
    @Override
    public Date getUserPreferenceAsDate(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        Date value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(user, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Date) ParameterHelper.castValue(values.get(id), ParameterType.DATE);
        }
        
        return value;
    }
    
    @Override
    public Boolean getUserPreferenceAsBoolean(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        Boolean value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(user, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Boolean) ParameterHelper.castValue(values.get(id), ParameterType.BOOLEAN);
        }
        
        return value;
    }
    
    @Override
    public Double getUserPreferenceAsDouble(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        Double value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(user, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Double) ParameterHelper.castValue(values.get(id), ParameterType.DOUBLE);
        }
        
        return value;
    }
    
    /**
     * Write a Map of preferences as XML and return an InputStream on this XML.
     * @param preferences the preferences Map.
     * @return an InputStream on the preferences as XML.
     * @throws UserPreferencesException if an error occurred
     */
    protected byte[] _getPreferencesXmlBytes(Map<String, String> preferences) throws UserPreferencesException
    {
        try
        {
            SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            TransformerHandler handler = factory.newTransformerHandler();
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Result result = new StreamResult(bos);
            
            handler.setResult(result);
            
            handler.startDocument();
            
            AttributesImpl attr = new AttributesImpl();
            attr.addCDATAAttribute("version", "2");
            XMLUtils.startElement(handler, "UserPreferences", attr);
            
            for (Entry<String, String> preference : preferences.entrySet())
            {
                String value = preference.getValue();
                if (value != null)
                {
                    attr.clear();
                    attr.addCDATAAttribute("id", preference.getKey());
                    XMLUtils.createElement(handler, "preference", attr, preference.getValue());
                }
            }
            
            XMLUtils.endElement(handler, "UserPreferences");
            handler.endDocument();
            
            return bos.toByteArray();
        }
        catch (TransformerException e)
        {
            throw new UserPreferencesException("Error writing the preferences as XML.", e);
        }
        catch (SAXException e)
        {
            throw new UserPreferencesException("Error writing the preferences as XML.", e);
        }
    }
    
}
