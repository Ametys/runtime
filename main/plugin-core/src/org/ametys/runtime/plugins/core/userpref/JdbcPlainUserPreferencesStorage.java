/*
 *  Copyright 2013 Anyware Services
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * This class is a JDBC implementation of {@link UserPreferencesStorage},
 * which stores preferences in database, one preference by column.<br>
 * Currently, it only supports storing in string-typed columns (VARCHAR, TEXT, ...),
 * but allows all preference types by casting them from/to strings when retrieving/getting them.<br>
 * This component does not impose a table structure. This is a configuration example:<br>
 * <pre>
 * &lt;component role="com.mydomain.test.MyDatabaseUserPreferencesStorage"<br>
 *            class="org.ametys.runtime.plugins.core.userpref.JdbcPlainUserPreferencesStorage"&gt;<br>
 *     &lt;pool&gt;com.mydomain.test.MyPool&lt;/pool&gt;<br>
 *     &lt;table&gt;MyUserPreferences&lt;/table&gt;<br>
 *     &lt;loginColumn&gt;user&lt;/loginColumn&gt;<br>
 *     &lt;contextColumn&gt;context&lt;/contextColumn&gt;<br>
 *     &lt;columnPattern&gt;^(mypref_\w+)$&lt;/columnPattern&gt;<br>
 *     &lt;mappings&gt;<br>
 *         &lt;mapping prefId="lastname" column="mypref_lastname"/&gt;<br>
 *         &lt;mapping prefId="firstname" column="mypref_firstname"/&gt;<br>
 *         &lt;mapping prefId="email" column="mypref_email_address"/&gt;<br>
 *     &lt;/mappings&gt;<br>
 * &lt;/component&gt;<br>
 * </pre><br>
 * Column names must be configured lowercase, both when setting login and context columns and when setting mapping columns.
 * <br><br>
 * This class differs from {@link JdbcXmlUserPreferencesStorage} as it does not implement {@link DefaultUserPreferencesStorage},
 * and because the latter imposes the DB table structure and stores the preferences as an XML binary.
 */
public class JdbcPlainUserPreferencesStorage extends AbstractLogEnabled implements UserPreferencesStorage, ThreadSafe, Configurable
{
    
    /** Connection pool name. */
    protected String _poolName;
    
    /** The database table in which the preferences are stored. */
    protected String _databaseTable;
    
    /** The login column, cannot be null. */
    protected String _loginColumn;
    
    /** The context column, can be null if the database is not context-dependent. */
    protected String _contextColumn;
    
    /** A pattern to filter the columns which correspond to preferences. */
    protected Pattern _columnPattern;
    
    /** Mapping from preference id to column name. */
    protected Map<String, String> _prefIdToColumn;
    
    /** Mapping from column name to preference id. */
    protected Map<String, String> _columnToPrefId;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        // Default to the core pool.
        _poolName = configuration.getChild("pool").getValue(ConnectionHelper.CORE_POOL_NAME);
        // The table configuration is mandatory.
        _databaseTable = configuration.getChild("table").getValue();
        // Default to "login".
        _loginColumn = configuration.getChild("loginColumn").getValue("login").toLowerCase();
        // Default to null (no context column).
        _contextColumn = configuration.getChild("contextColumn").getValue(null);
        if (_contextColumn != null)
        {
            _contextColumn = _contextColumn.toLowerCase();
        }
        
        // Default to null: all columns except the login column and the context column (if any) are preferences.
        String regex = configuration.getChild("columnPattern").getValue(null);
        _columnPattern = StringUtils.isBlank(regex) ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        
        // Configure the preference-column mappings.
        configureMappings(configuration.getChild("mappings"));
    }
    
    /**
     * Configure the mappings from preference ID to column name.
     * @param configuration the mapping configuration root.
     * @throws ConfigurationException if an error occurs.
     */
    public void configureMappings(Configuration configuration) throws ConfigurationException
    {
        // Store the mappings in both directions.
        _prefIdToColumn = new HashMap<>();
        _columnToPrefId = new HashMap<>();
        
        for (Configuration mappingConf : configuration.getChildren("mapping"))
        {
            String prefId = mappingConf.getAttribute("prefId");
            String column = mappingConf.getAttribute("column").toLowerCase();
            
            _prefIdToColumn.put(prefId, column);
            _columnToPrefId.put(column, prefId);
        }
    }
    
    @Override
    public Map<String, String> getUnTypedUserPrefs(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Map<String, String> prefs = new HashMap<>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM ").append(_databaseTable).append(" WHERE ").append(_loginColumn).append(" = ?");
            if (StringUtils.isNotBlank(_contextColumn))
            {
                query.append(" AND ").append(_contextColumn).append(" = ?");
            }
            
            stmt = connection.prepareStatement(query.toString());
            
            stmt.setString(1, login);
            if (StringUtils.isNotBlank(_contextColumn))
            {
                stmt.setString(2, storageContext);
            }
            
            rs = stmt.executeQuery();
            
            if (rs.next())
            {
                ResultSetMetaData metaData = rs.getMetaData();
                
                int colCount = metaData.getColumnCount();
                
                for (int col = 1; col <= colCount; col++)
                {
                    String name = metaData.getColumnName(col).toLowerCase();
                    
                    if (isColumnValid(name))
                    {
                        int type = metaData.getColumnType(col);
                        String value = getPreferenceValue(rs, col, type);
                        
                        if (value != null)
                        {
                            String prefId = _columnToPrefId.containsKey(name) ? _columnToPrefId.get(name) : name;
                            prefs.put(prefId, value);
                        }
                    }
                }
            }
            
            return prefs;
        }
        catch (SQLException e)
        {
            String message = "Database error trying to access the preferences of user '" + login + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    @Override
    public void removeUserPreferences(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            StringBuilder query = new StringBuilder();
            query.append("DELETE FROM ").append(_databaseTable).append(" WHERE ").append(_loginColumn).append(" = ?");
            if (StringUtils.isNotBlank(_contextColumn))
            {
                query.append(" AND ").append(_contextColumn).append(" = ?");
            }
            
            stmt = connection.prepareStatement(query.toString());
            
            stmt.setString(1, login);
            if (StringUtils.isNotBlank(_contextColumn))
            {
                stmt.setString(2, storageContext);
            }
            
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            String message = "Database error trying to remove preferences for login '" + login + "' in context '" + storageContext + "'.";
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
    public void setUserPreferences(String login, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException
    {
        Connection connection = null;
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            // Test if the preferences already exist.
            if (dataExists(connection, login, storageContext))
            {
                updatePreferences(connection, preferences, login, storageContext);
            }
            else
            {
                insertPreferences(connection, preferences, login, storageContext);
            }
        }
        catch (SQLException e)
        {
            String message = "Database error trying to set the preferences of user '" + login + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
    
    @Override
    public String getUserPreferenceAsString(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        String value = null;
        
        try
        {
            String column = _prefIdToColumn.containsKey(id) ? _prefIdToColumn.get(id) : id;
            
            if (isColumnValid(column))
            {
                StringBuilder query = new StringBuilder();
                query.append("SELECT ? FROM ").append(_databaseTable).append(" WHERE ").append(_loginColumn).append(" = ?");
                if (StringUtils.isNotBlank(_contextColumn))
                {
                    query.append(" AND ").append(_contextColumn).append(" = ?");
                }
                
                connection = ConnectionHelper.getConnection(_poolName);
                
                statement = connection.prepareStatement(query.toString());
                statement.setString(1, column);
                statement.setString(2, login);
                if (StringUtils.isNotBlank(_contextColumn))
                {
                    statement.setString(3, storageContext);
                }
                
                rs = statement.executeQuery();
                
                if (rs.next())
                {
                    value = rs.getString(1);
                }
            }
        }
        catch (SQLException e)
        {
            String message = "Database error trying to get the preferences of user '" + login + "' in context '" + storageContext + "'.";
            getLogger().error(message, e);
            throw new UserPreferencesException(message, e);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(connection);
        }
        
        return value;
    }
    
    @Override
    public Long getUserPreferenceAsLong(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        // TODO Single select
        Long value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Long) ParameterHelper.castValue(values.get(id), ParameterType.LONG);
        }
        
        return value;
    }
    
    @Override
    public Date getUserPreferenceAsDate(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        // TODO Single select
        Date value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Date) ParameterHelper.castValue(values.get(id), ParameterType.DATE);
        }
        
        return value;
    }
    
    @Override
    public Boolean getUserPreferenceAsBoolean(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        // TODO Single select
        Boolean value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Boolean) ParameterHelper.castValue(values.get(id), ParameterType.BOOLEAN);
        }
        
        return value;
    }
    
    @Override
    public Double getUserPreferenceAsDouble(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        // TODO Single select
        Double value = null;
        
        Map<String, String> values = getUnTypedUserPrefs(login, storageContext, contextVars);
        if (values.containsKey(id))
        {
            value = (Double) ParameterHelper.castValue(values.get(id), ParameterType.DOUBLE);
        }
        
        return value;
    }
    
    /**
     * Test if the given column corresponds to a preference value.
     * @param name the column name.
     * @return true if the column corresponds to a preference value, false otherwise.
     */
    protected boolean isColumnValid(String name)
    {
        // Do not return the login column, the context column (if applicable)
        // and columns not matching the pattern.
        return !_loginColumn.equalsIgnoreCase(name)
            && (_contextColumn == null || !_contextColumn.equalsIgnoreCase(name))
            && (_columnPattern == null || _columnPattern.matcher(name).matches());
    }
    
    /**
     * Get a preference value as a String.
     * @param rs The result set, must be set on the right record.
     * @param columnIndex The column index.
     * @param jdbcType The JDBC type.
     * @return The preference value as a String, can be null.
     * @throws SQLException if an error occurs.
     */
    protected String getPreferenceValue(ResultSet rs, int columnIndex, int jdbcType) throws SQLException // $CHECKSTYLE:cyclomaticcomplexity
    {
        String value = null;
        
        // TODO Cast if necessary.
        switch (jdbcType)
        {
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                value = rs.getString(columnIndex);
                break;
            default:
                break;
        }
        
        return value;
    }
    
    /**
     * Test if a record exists for this user and context.
     * @param connection The database connection.
     * @param login The user login.
     * @param storageContext The storage context.
     * @return true if data exists, false otherwise.
     * @throws SQLException if an error occurs.
     */
    protected boolean dataExists(Connection connection, String login, String storageContext) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            StringBuilder query = new StringBuilder();
            query.append("SELECT count(*) FROM ").append(_databaseTable).append(" WHERE ").append(_loginColumn).append(" = ?");
            if (StringUtils.isNotBlank(_contextColumn))
            {
                query.append(" AND ").append(_contextColumn).append(" = ?");
            }
            
            stmt = connection.prepareStatement(query.toString());
            
            stmt.setString(1, login);
            if (StringUtils.isNotBlank(_contextColumn))
            {
                stmt.setString(2, storageContext);
            }
            
            rs = stmt.executeQuery();
            rs.next();
            
            return rs.getInt(1) > 0;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
        }
    }
    
    /**
     * Insert preferences into the database.
     * @param connection The database connection.
     * @param preferences The preference values, indexed by preference id.
     * @param login The user login.
     * @param storageContext The preference storage context.
     * @throws SQLException if an error occurs.
     */
    protected void insertPreferences(Connection connection, Map<String, String> preferences, String login, String storageContext) throws SQLException
    {
        PreparedStatement stmt = null;
        
        try
        {
            StringBuilder query = new StringBuilder();
            StringBuilder values = new StringBuilder();
            query.append("INSERT INTO ").append(_databaseTable).append("(").append(_loginColumn);
            values.append('?');
            if (StringUtils.isNotBlank(_contextColumn))
            {
                query.append(", ").append(_contextColumn);
                values.append(", ?");
            }
            
            List<String> valuesToSet = new ArrayList<>();
            
            int validPrefCount = 0;
            for (String prefId : preferences.keySet())
            {
                String column = _prefIdToColumn.containsKey(prefId) ? _prefIdToColumn.get(prefId) : prefId;
                
                if (isColumnValid(column))
                {
                    valuesToSet.add(preferences.get(prefId));
                    
                    query.append(", ").append(column);
                    values.append(", ?");
    
                    validPrefCount++;
                }
            }
            
            if (validPrefCount > 0)
            {
                query.append(") VALUES (").append(values).append(')');
                
                int i = 1;
                
                stmt = connection.prepareStatement(query.toString());
                
                stmt.setString(i++, login);
                if (StringUtils.isNotBlank(_contextColumn))
                {
                    stmt.setString(i++, storageContext);
                }
                
                for (String value : valuesToSet)
                {
                    stmt.setString(i, value);
                    i++;
                }
                
                stmt.executeUpdate();
            }
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
        }
    }
    
    /**
     * Update existing preferences.
     * @param connection The database connection.
     * @param preferences The preference values, indexed by preference id.
     * @param login The user login.
     * @param storageContext The preference storage context.
     * @throws SQLException if an error occurs.
     */
    protected void updatePreferences(Connection connection, Map<String, String> preferences, String login, String storageContext) throws SQLException
    {
        PreparedStatement stmt = null;
        
        try
        {
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ").append(_databaseTable).append(" SET ");
            
            List<String> valuesToSet = new ArrayList<>();
            
            int validPrefCount = 0;
            for (String prefId : preferences.keySet())
            {
                String column = _prefIdToColumn.containsKey(prefId) ? _prefIdToColumn.get(prefId) : prefId;
                
                if (isColumnValid(column))
                {
                    valuesToSet.add(preferences.get(prefId));
                    
                    if (validPrefCount > 0)
                    {
                        query.append(", ");
                    }
                    
                    query.append(" ").append(column).append(" = ?");
                    
                    validPrefCount++;
                }
            }
            
            query.append(" WHERE ").append(_loginColumn).append(" = ?");
            if (StringUtils.isNotBlank(_contextColumn))
            {
                query.append(" AND ").append(_contextColumn).append(" = ?");
            }
            
            if (validPrefCount > 0)
            {
                stmt = connection.prepareStatement(query.toString());
                
                int i = 1;
                for (String value : valuesToSet)
                {
                    stmt.setString(i, value);
                    i++;
                }
                
                stmt.setString(i++, login);
                if (StringUtils.isNotBlank(_contextColumn))
                {
                    stmt.setString(i++, storageContext);
                }
                
                stmt.executeUpdate();
            }
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
        }
    }
    
}
