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
package org.ametys.core.right;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.datasource.ConnectionHelper;

/**
 * A profile is a set of Rights.
 */
public class Profile
{
    private final String _id;
    private final String _name;
    private final String _context;
    private Connection _currentConnection;
    private boolean _supportsBatch;
    private PreparedStatement _batchStatement;
    
    private String _dataSourceId;
    private String _tableProfile;
    private String _tableProfileRights;
    
    /**
     * Constructor.
     * @param id the unique id of this profile
     * @param name the name of this profile
     * @param dataSourceId The id of the datasource where the profile is stored
     * @param tableProfile The name of the table of the profiles
     * @param tableProfileRights The name of tha table for the associations profile/rights
     */
    public Profile(String id, String name, String dataSourceId, String tableProfile, String tableProfileRights)
    {
        this(id, name, null, dataSourceId, tableProfile, tableProfileRights);
    }

    /**
     * Constructor.
     * @param id the unique id of this profile
     * @param name the name of this profile
     * @param context the context
     * @param dataSourceId The id of the datasource where the profile is stored
     * @param tableProfile The name of the table of the profiles
     * @param tableProfileRights The name of tha table for the associations profile/rights
     */
    public Profile(String id, String name, String context, String dataSourceId, String tableProfile, String tableProfileRights)
    {
        _id = id;
        _name = name;
        _context = context;
        _currentConnection = null;
        
        _dataSourceId = dataSourceId;
        _tableProfile = tableProfile;
        _tableProfileRights = tableProfileRights;
    }
    
    /**
     * Returns the unique Id of this profile
     * @return the unique Id of this profile
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the name of this profile
     * @return the name of this profile
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Returns the context of this profile
     * @return the context of this profile. Can be null.
     */
    public String getContext()
    {
        return _context;
    }
    
    /**
     * Adds a Right to this Profile
     * @param rightId the Right to add to this profile
     */
    public void addRight(String rightId)
    {
        Connection connection = _getConnection();

        try
        {
            PreparedStatement statement = _getAddStatement(connection);
            statement.setString(1, _id);
            statement.setString(2, rightId);

            if (_isUpdating() && _supportsBatch)
            {
                statement.addBatch();
            }
            else
            {
                statement.executeUpdate();
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            if (!_isUpdating())
            {
                ConnectionHelper.cleanup(connection);
            }
        }
    }
    
    /**
     * Renames this Profile
     * @param newName the new label of this Profile
     */
    public void rename(String newName)
    {
        Connection connection = _getSQLConnection();

        try
        {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + _tableProfile + " SET label = ? WHERE Id = ?");
            statement.setString(1, newName);
            statement.setString(2, _id);
            statement.executeUpdate();
        }
        catch (NumberFormatException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * Returns the set of Rights of this profile
     * @return the set of Rights of this profile
     */
    public Set<String> getRights()
    {
        Set<String> rights = new HashSet<>();

        Connection connection = _getSQLConnection();

        try
        {
            PreparedStatement statement = connection.prepareStatement("SELECT Right_Id FROM " + _tableProfileRights + " WHERE profile_Id = ?");
            statement.setString(1, _id);
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                String id = rs.getString("Right_Id");
                rights.add(id);
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }

        return rights;
    }
    
    /**
     * Removes associated rights
     */
    public void removeRights()
    {
        Connection connection = _getConnection();

        try
        {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
            statement.setString(1, _id);
            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            if (!_isUpdating())
            {
                ConnectionHelper.cleanup(connection);
            }
        }
    }
    
    /**
     * Start update mode: subsequent calls to removeRights and addRight must be
     * enclosed in a transaction until endUpdate is called. 
     */
    public void startUpdate()
    {
        _currentConnection = _getSQLConnection();

        try
        {
            _supportsBatch = _currentConnection.getMetaData().supportsBatchUpdates();
            _currentConnection.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * End update mode: make effective the changes since startUpdate was called.
     * ("commit" the transaction).
     */
    public void endUpdate()
    {
        try
        {
            if (_isUpdating() && _supportsBatch && _batchStatement != null)
            {
                _batchStatement.executeBatch();
            }
            _supportsBatch = false;
            _currentConnection.commit();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(_batchStatement);
            _batchStatement = null;

            ConnectionHelper.cleanup(_currentConnection);
            _currentConnection = null;
        }
    }
    
    /**
     * Get the current connection or create a new one.
     * @return the current connection if in "update" mode, a new one otherwise.
     */
    private Connection _getConnection()
    {
        if (_isUpdating())
        {
            return _currentConnection;
        }
        else
        {
            return _getSQLConnection();
        }
    }
    
    private Connection _getSQLConnection ()
    {
        return ConnectionHelper.getConnection(_dataSourceId);
    }

    /**
     * Get a prepared statement to add a profile in the DBMS.
     * @param connection the connection.
     * @return a prepared statement.
     */
    private PreparedStatement _getAddStatement(Connection connection)
    {
        try
        {
            String query = "INSERT INTO " + _tableProfileRights + " (Profile_Id, Right_Id) VALUES(?, ?)";

            if (_isUpdating() && _supportsBatch)
            {
                if (_batchStatement == null)
                {
                    _batchStatement = connection.prepareStatement(query);
                }

                return _batchStatement;
            }
            else
            {
                return connection.prepareStatement(query);
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Test if we are in "update" mode.
     * @return true if we are "update" mode, false otherwise.
     */
    private boolean _isUpdating()
    {
        boolean updating = false;

        try
        {
            updating = _currentConnection != null && !_currentConnection.isClosed();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        return updating;
    }
    
    /**
     * SAXes a representation of this Profile
     * @param handler the ContentHandler receiving SAX events
     * @throws SAXException if a probleme occurs while SAXing events
     */
    @Deprecated
    public void toSAX(ContentHandler handler) throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "id", "id", "CDATA", _id);
        XMLUtils.startElement(handler, "profile", atts);

        XMLUtils.createElement(handler, "label", _name);

        String context = getContext();
        if (context != null)
        {
            XMLUtils.createElement(handler, "context", context);
        }

        handler.startElement("", "rights", "rights", new AttributesImpl());

        for (String right : getRights())
        {
            AttributesImpl attsRight = new AttributesImpl();
            attsRight.addAttribute("", "id", "id", "CDATA", right);
            XMLUtils.createElement(handler, "right", attsRight);
        }

        XMLUtils.endElement(handler, "rights");
        XMLUtils.endElement(handler, "profile");
    }
    
    /**
     * Get the JSON representation of this Profile
     * @return The profile's properties
     */
    public Map<String, Object> toJSON()
    {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("id", _id);
        profile.put("label", _name);
        profile.put("context", getContext());
        profile.put("rights", getRights());
        
        return profile;
    }
    
    
    @Override
    public boolean equals(Object another)
    {
        if (another == null || !(another instanceof Profile))
        {
            return false;
        }

        Profile otherProfile = (Profile) another;

        return _id != null  || _id.equals(otherProfile.getId());
    }

    @Override
    public int hashCode()
    {
        return _id.hashCode();
    }
}
