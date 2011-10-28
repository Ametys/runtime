/*
 *  Copyright 2011 Anyware Services
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
package org.ametys.runtime.plugins.core.userpref;

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

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.datasource.ConnectionHelper.DatabaseType;
import org.ametys.runtime.util.MapHandler;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * User preference manager: allows to get and set user preferences.
 */
public class UserPreferencesManager extends AbstractLogEnabled implements Component, Configurable, Serviceable
{
    /** The avalon role. */
    public static final String ROLE = UserPreferencesManager.class.getName();
    
    /** The user preferences extensions point. */
    protected UserPreferencesExtensionPoint _userPrefEP;
    
    /** A SAX parser. */
    protected SAXParser _saxParser;
    
    /** The database table in which the preferences are stored. */
    protected String _databaseTable;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _databaseTable = configuration.getChild("table").getValue();
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userPrefEP = (UserPreferencesExtensionPoint) manager.lookup(UserPreferencesExtensionPoint.ROLE);
        _saxParser = (SAXParser) manager.lookup(SAXParser.ROLE);
    }
    
    /**
     * Get a user's preference values (as String) for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @return the user preference values as a Map of String indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, String> getUnTypedUserPrefs(String login, String context) throws UserPreferencesException
    {
        Map<String, String> prefs = new HashMap<String, String>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        InputStream dataIs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME);
            DatabaseType dbType = ConnectionHelper.getDatabaseType(connection);
            
            stmt = connection.prepareStatement("SELECT * FROM " + _databaseTable + " WHERE login = ? AND context = ?");
            
            stmt.setString(1, login);
            stmt.setString(2, context);
            
            rs = stmt.executeQuery();
            
            if (rs.next())
            {
                if (DatabaseType.DATABASE_POSTGRES.equals(dbType))
                {
                    dataIs = rs.getBinaryStream("data");
                }
                else
                {
                    Blob data = rs.getBlob("data");
                    dataIs = data.getBinaryStream();
                }
                
                // Create the handler and fill the Map by parsing the configuration.
                MapHandler handler = new MapHandler(prefs);
                _saxParser.parse(new InputSource(dataIs), handler);
            }
            
            return prefs;
        }
        catch (SQLException e)
        {
            String message = "Database error trying to access the preferences of user '" + login + "' in context '" + context + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        catch (SAXException e)
        {
            String message = "Error parsing the preferences of user '" + login + "' in context '" + context + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        catch (IOException e)
        {
            String message = "Error parsing the preferences of user '" + login + "' in context '" + context + "'.";
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
    
    /**
     * Get a user's preference values cast as their own type for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @return the user preference values as a Map of Object indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, Object> getTypedUserPrefs(String login, String context) throws UserPreferencesException
    {
        Map<String, String> unTypedUserPrefs = getUnTypedUserPrefs(login, context);
        
        return _castValues(unTypedUserPrefs);
    }
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param name the user pref name
     * @param value the user pref value
     * @throws UserPreferencesException
     */
    public void addUserPreference (String login, String context, String name, String value) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, context);
        userPrefs.put(name, value);
        
        setUserPreferences(login, context, userPrefs);
    }
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param values the user prefs to add
     * @throws UserPreferencesException
     */
    public void addUserPreferences (String login, String context, Map<String, String> values) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, context);
        userPrefs.putAll(values);
        
        setUserPreferences(login, context, userPrefs);
    }
    
    /**
     * Remove a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param name the user pref name
     * @throws UserPreferencesException
     */
    public void removeUserPreference (String login, String context, String name) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, context);
        if (userPrefs.containsKey(name))
        {
            userPrefs.remove(name);
        }
        setUserPreferences(login, context, userPrefs);
    }
    
    /**
     * Set a user's preferences for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param preferences a Map of the preference values indexed by ID.
     * @throws UserPreferencesException 
     */
    public void setUserPreferences(String login, String context, Map<String, String> preferences) throws UserPreferencesException
    {
        // Set.
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        InputStream dataIs = null;
        
        try
        {
            byte[] prefBytes = _getPreferencesXmlBytes(preferences);
            dataIs = new ByteArrayInputStream(prefBytes);
            
            connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME);
            DatabaseType dbType = ConnectionHelper.getDatabaseType(connection);
            
            // Test if the preferences already exist.
            stmt = connection.prepareStatement("SELECT count(*) FROM " + _databaseTable + " WHERE login = ? AND context = ?");
            stmt.setString(1, login);
            stmt.setString(2, context);
            rs = stmt.executeQuery();
            rs.next();
            boolean dataExists = rs.getInt(1) > 0;
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            
            if (dataExists)
            {
                // If there's already a record, update it with the new data.
                stmt = connection.prepareStatement("UPDATE " + _databaseTable + " SET data = ? WHERE login = ? AND context = ?");
                
                if (DatabaseType.DATABASE_POSTGRES.equals(dbType))
                {
                    stmt.setBinaryStream(1, dataIs, prefBytes.length);
                }
                else
                {
                    stmt.setBlob(1, dataIs);
                }
                stmt.setString(2, login);
                stmt.setString(3, context);
                
                stmt.executeUpdate();
            }
            else
            {
                // If not, insert the data.
                stmt = connection.prepareStatement("INSERT INTO " + _databaseTable + "(login, context, data) VALUES(?, ?, ?)");
                
                stmt.setString(1, login);
                stmt.setString(2, context);
                if (DatabaseType.DATABASE_POSTGRES.equals(dbType))
                {
                    stmt.setBinaryStream(3, dataIs, prefBytes.length);
                }
                else
                {
                    stmt.setBlob(3, dataIs);
                }
                
                stmt.executeUpdate();
            }
            
        }
        catch (SQLException e)
        {
            String message = "Database error trying to access the preferences of user '" + login + "' in context '" + context + "'.";
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

    /**
     * Get a single string user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a String.
     * @throws UserPreferencesException 
     */
    public String getUserPreferenceAsString(String login, String context, String id) throws UserPreferencesException
    {
        String value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, context);
        if (values.containsKey(id))
        {
            value = values.get(id);
        }
        
        return value;
    }
    
    /**
     * Get a single long user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Long.
     * @throws UserPreferencesException 
     */
    public Long getUserPreferenceAsLong(String login, String context, String id) throws UserPreferencesException
    {
        Long value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, context);
        if (values.containsKey(id))
        {
            value = (Long) ParameterHelper.castValue(values.get(id), ParameterType.LONG);
        }
        
        return value;

    }
    
    /**
     * Get a single date user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Date.
     * @throws UserPreferencesException 
     */
    public Date getUserPreferenceAsDate(String login, String context, String id) throws UserPreferencesException
    {
        Date value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, context);
        if (values.containsKey(id))
        {
            value = (Date) ParameterHelper.castValue(values.get(id), ParameterType.DATE);
        }
        
        return value;
    }
    
    /**
     * Get a single boolean user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Boolean.
     * @throws UserPreferencesException 
     */
    public Boolean getUserPreferenceAsBoolean(String login, String context, String id) throws UserPreferencesException
    {
        Boolean value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, context);
        if (values.containsKey(id))
        {
            value = (Boolean) ParameterHelper.castValue(values.get(id), ParameterType.BOOLEAN);
        }
        
        return value;
    }
    
    /**
     * Get a single double user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Double.
     * @throws UserPreferencesException 
     */
    public Double getUserPreferenceAsDouble(String login, String context, String id) throws UserPreferencesException
    {
        Double value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, context);
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
     * @throws UserPreferencesException
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
            XMLUtils.startElement(handler, "UserPreferences");
            
            for (Entry<String, String> preference : preferences.entrySet())
            {
                String value = preference.getValue();
                if (value != null)
                {
                    XMLUtils.createElement(handler, preference.getKey(), preference.getValue());
                }
            }
            
            XMLUtils.endElement(handler, "UserPreferences");
            handler.endDocument();
            
            return bos.toByteArray();
//            byte[] xmlBuffer = bos.toByteArray();
//            
//            return new ByteArrayInputStream(xmlBuffer);
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

    /**
     * Cast the preference values as their real type.
     * @param untypedValues the untyped user preferences
     * @return typed user preferences
     */
    protected Map<String, Object> _castValues(Map<String, String> untypedValues)
    {
        Map<String, Object> typedValues = new HashMap<String, Object>(untypedValues.size());
        
        for (Entry<String, String> entry : untypedValues.entrySet())
        {
            UserPreference userPref = _userPrefEP.getExtension(entry.getKey());
            if (userPref != null)
            {
                Object value = ParameterHelper.castValue(entry.getValue(), userPref.getType());
                typedValues.put(userPref.getId(), value);
            }
            else
            {
                typedValues.put(entry.getKey(), entry.getValue());
            }
        }
        
        return typedValues;
    }
    
}
