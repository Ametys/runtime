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
package org.ametys.core.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.core.datasource.ConnectionHelper;

/**
 * Example of simple use: 
 * SQLScriptHelper.createTableIfNotExists(dataSourceId, "QRTZ_JOB_DETAILS", "plugin:core://scripts/%s/quartz.sql", _sourceResolver);
 * Will test if table QRTZ_JOB_DETAILS exits in database from datasource dataSourceId. If not, the script plugin:core://scripts/%s/quartz.sql will be resolved and executed (where %s is replaced by the database type 'mysql', 'derby'...)
 * 
 * Tools to run SQL scripts.<p>
 * Default separator for isolating statements is the semi colon
 * character: <code>;</code>.<br>
 * It can be changed by using a comment like the following snippet
 * for using the string <code>---</code>:<br>
 * <code>-- _separator_=---<br>
 * begin<br>
 * &nbsp;&nbsp;execute immediate 'DROP TABLE MYTABLE';<br>
 * &nbsp;&nbsp;Exception when others then null;<br>
 * end;<br>
 * ---<br>
 * -- _separator_=;<br>
 * CREATE TABLE MYTABLE;<br>
 * ...</code><br>
 * Note that the command must be placed at the end of the comment.<br><br>
 * The runner can be configured to ignore SQLExceptions. This can be useful
 * to execute DROP statements when it's unknown if the tables exist:<br>
 * <code>--_ignore_exceptions_=on<br>
 * DROP TABLE MYTABLE;<br>
 * --_ignore_exceptions_=off</code>
 */
public final class SQLScriptHelper
{
    /** Default separator used for isolating statements. */
    public static final String DEFAULT_SEPARATOR = ";";
    /** Command to ignore sql exceptions. */
    public static final String IGNORE_EXCEPTIONS_COMMAND = "_ignore_exceptions_=";
    /** Command to change the separator. */
    public static final String CHANGE_SEPARATOR_COMMAND = "_separator_=";
    /** Logger available to subclasses. */
    protected static final Logger __LOGGER = LoggerFactory.getLogger(SQLScriptHelper.class);
    
    private SQLScriptHelper()
    {
        // Nothing to do
    }

    /**
     * This method will test if a table exists, and if not will execute a script to create it
     * @param datasourceId The data source id to open a connection to the database
     * @param tableNameToCheck The name of the table that will be checked
     * @param location The source location where to find the script to execute to create the table. This string will be format with String.format with the dbType as argument.
     * @param sourceResolver The source resolver
     * @return true if the table was created, false otherwise
     * @throws SQLException If an error occurred while executing SQL script, or while testing table existence
     * @throws IOException If an error occurred while getting the script file, or if the url is malformed
     */
    public static boolean createTableIfNotExists(String datasourceId, String tableNameToCheck, String location, SourceResolver sourceResolver) throws SQLException, IOException
    {
        return createTableIfNotExists(datasourceId, tableNameToCheck, location, sourceResolver, null);
    }

    /**
     * This method will test if a table exists, and if not will execute a script to create it
     * @param datasourceId The data source id to open a connection to the database
     * @param tableNameToCheck The name of the table that will be checked
     * @param location The source location where to find the script to execute to create the table. This string will be format with String.format with the dbType as argument.
     * @param sourceResolver The source resolver
     * @param replace The map of string to replace. Key is the regexp to seek, value is the replacing string.
     * @return true if the table was created, false otherwise
     * @throws SQLException If an error occurred while executing SQL script, or while testing table existence
     * @throws IOException If an error occurred while getting the script file, or if the url is malformed
     */
    public static boolean createTableIfNotExists(String datasourceId, String tableNameToCheck, String location, SourceResolver sourceResolver, Map<String, String> replace) throws SQLException, IOException
    {
        Connection connection = null;
        try
        {
            connection = ConnectionHelper.getConnection(datasourceId);
            
            return createTableIfNotExists(connection, tableNameToCheck, location, sourceResolver, replace);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * This method will test if a table exists, and if not will execute a script to create it
     * @param connection The database connection to use
     * @param tableNameToCheck The name of the table that will be checked
     * @param location The source location where to find the script to execute to create the table. This string will be format with String.format with the dbType as argument.
     * @param sourceResolver The source resolver
     * @return true if the table was created, false otherwise
     * @throws SQLException If an error occurred while executing SQL script, or while testing table existence
     * @throws IOException If an error occurred while getting the script file, or if the url is malformed
     */
    public static boolean createTableIfNotExists(Connection connection, String tableNameToCheck, String location, SourceResolver sourceResolver) throws SQLException, IOException
    {
        return createTableIfNotExists(connection, tableNameToCheck, location, sourceResolver, null);
    }
    
    /**
     * This method will test if a table exists, and if not will execute a script to create it
     * @param connection The database connection to use
     * @param tableNameToCheck The name of the table that will be checked
     * @param location The source location where to find the script to execute to create the table. This string will be format with String.format with the dbType as argument.
     * @param sourceResolver The source resolver
     * @param replace The map of string to replace. Key is the regexp to seek, value is the replacing string.
     * @return true if the table was created, false otherwise
     * @throws SQLException If an error occurred while executing SQL script, or while testing table existence
     * @throws IOException If an error occurred while getting the script file, or if the url is malformed
     */
    public static boolean createTableIfNotExists(Connection connection, String tableNameToCheck, String location, SourceResolver sourceResolver, Map<String, String> replace) throws SQLException, IOException
    {
        if (tableExists(connection, tableNameToCheck))
        {
            return false;
        }
        
        String finalLocation = String.format(location, ConnectionHelper.getDatabaseType(connection));
        
        Source source = null;
        try
        {
            source = sourceResolver.resolveURI(finalLocation);
            
            try (InputStream is = source.getInputStream())
            {
                String script = IOUtils.toString(is, "UTF-8");

                if (replace != null)
                {
                    for (String replaceKey : replace.keySet())
                    {
                        script = script.replaceAll(replaceKey, replace.get(replaceKey));
                    }
                }
                
                SQLScriptHelper.runScript(connection, script);
            }
        }
        finally
        {
            if (source != null)
            {
                sourceResolver.release(source);
            }
        }
        
        return true;
    }
    
    /**
     * Checks whether the given table exists in the database.
     * @param connection The database connection
     * @param tableName the name of the table
     * @return true is the table exists
     * @throws SQLException In an SQL exception occurs
     */
    public static boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        ResultSet rs = null;
        boolean schemaExists = false;
        
        String name = tableName;
        DatabaseMetaData metaData = connection.getMetaData();
        
        if (metaData.storesLowerCaseIdentifiers())
        {
            name = tableName.toLowerCase();
        }
        else if (metaData.storesUpperCaseIdentifiers())
        {
            name = tableName.toUpperCase();
        }
        
        try
        {
            rs = metaData.getTables(null, null, name, null);
            schemaExists = rs.next();
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
        }
        
        return schemaExists;
    }

    /**
     * Run a SQL script using the connection passed in.
     * @param connection the connection to use for the script
     * @param script the script data.
     * @throws IOException if an error occurs while reading the script.
     * @throws SQLException if an error occurs while executing the script.
     */
    public static void runScript(Connection connection, String script) throws IOException, SQLException
    {
        ScriptContext scriptContext = new ScriptContext();
        StringBuilder command = new StringBuilder();
        
        try
        {
            LineNumberReader lineReader = new LineNumberReader(new StringReader(script));
            String line = null;
            while ((line = lineReader.readLine()) != null)
            {
                if (__LOGGER.isDebugEnabled())
                {
                    __LOGGER.debug(String.format("Reading line: '%s'", line));
                }
                
                boolean processCommand = false;
                String trimmedLine = line.trim();
                
                if (trimmedLine.length() > 0)
                {
                    processCommand = processScriptLine(trimmedLine, command, scriptContext);
                    
                    if (processCommand)
                    {
                        _processCommand(connection, command, lineReader.getLineNumber(), scriptContext);
                    }
                }
            }
            
            // If the entire file was processed and the command buffer is not empty, execute the current buffer.
            if (command.length() > 0)
            {
                _processCommand(connection, command, lineReader.getLineNumber(), scriptContext);
            }
            
            if (!connection.getAutoCommit())
            {
                connection.commit();
            }
        }
        finally
        {
            if (!connection.getAutoCommit())
            {
                try
                {
                    // Fermer la connexion à la base
                    connection.rollback();
                }
                catch (SQLException s)
                {
                    __LOGGER.error("Error while rollbacking connection", s);
                }
            }
        }
    }
    
    /**
     * Run a SQL script using the connection passed in.
     * @param connection the connection to use for the script
     * @param is the input stream containing the script data.
     * @throws IOException if an error occurs while reading the script.
     * @throws SQLException if an error occurs while executing the script.
     */
    public static void runScript(Connection connection, InputStream is) throws IOException, SQLException
    {
        try
        {
            String script = IOUtils.toString(is, "UTF-8");
            runScript(connection, script);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Process a script line.
     * @param line the line to process.
     * @param commandBuffer the command buffer.
     * @param scriptContext the script execution context.
     * @return true to immediately process the command (a separator was found), false to process it later.
     */
    protected static boolean processScriptLine(String line, StringBuilder commandBuffer, ScriptContext scriptContext)
    {
        boolean processCommand = false;
        
        if (line.startsWith("//") || line.startsWith("--"))
        {
            String currentSeparator = scriptContext.getSeparator();
            
            // Search if the separator needs to be changed
            if (line.contains(CHANGE_SEPARATOR_COMMAND))
            {
                // New separator
                String newSeparator = line.substring(line.indexOf(CHANGE_SEPARATOR_COMMAND)
                            + CHANGE_SEPARATOR_COMMAND.length()).trim();
                
                scriptContext.setSeparator(newSeparator);
                
                if (__LOGGER.isDebugEnabled())
                {
                    __LOGGER.debug(String.format("Changing separator to: '%s'", newSeparator));
                }
            }
            else if (line.contains(IGNORE_EXCEPTIONS_COMMAND))
            {
                String ignoreStr = line.substring(line.indexOf(IGNORE_EXCEPTIONS_COMMAND)
                            + IGNORE_EXCEPTIONS_COMMAND.length()).trim();
                
                boolean ignoreExceptions = "on".equals(ignoreStr);
                
                scriptContext.setIgnoreExceptions(ignoreExceptions);
                
                if (__LOGGER.isDebugEnabled())
                {
                    __LOGGER.debug(String.format("Ignore exceptions: '%s'", ignoreExceptions ? "on" : "off"));
                }
            }
            
            if (line.contains(currentSeparator))
            {
                if (commandBuffer.length() > 0)
                {
                    // End of command but do not use current line
                    processCommand = true;
                }
            }
        }
        else if (line.endsWith(scriptContext.getSeparator()))
        {
            // End of command and use current line
            processCommand = true;
            commandBuffer.append(line.substring(0, line.lastIndexOf(scriptContext.getSeparator())));
        }
        else
        {
            // Append current command to the buffer
            commandBuffer.append(line);
            commandBuffer.append(" ");
        }
        
        return processCommand;
    }
    
    private static void _processCommand(Connection connection, StringBuilder command, int lineNumber, ScriptContext scriptContext) throws SQLException
    {
        if (__LOGGER.isInfoEnabled())
        {
            __LOGGER.info(String.format("Executing SQL command: '%s'", command));
        }
        
        _execute(connection, command.toString(), lineNumber, scriptContext);

        // Clear command
        command.setLength(0);
    }
    
    private static void _execute(Connection connection, String command, int lineNumber, ScriptContext scriptContext) throws SQLException
    {
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
            statement.execute(command);
        }
        catch (SQLException e)
        {
            if (!scriptContext.ignoreExceptions())
            {
                String message = String.format("Unable to execute SQL: '%s' at line %d", command, lineNumber);
                __LOGGER.error(message, e);
                
                throw new SQLException(message, e);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(statement);
        }
    }
    
    /**
     * Script execution context.
     */
    protected static class ScriptContext
    {
        
        /** The current script execution block separator. */
        protected String _separator;
        
        /** True to ignore sql exceptions. */
        protected boolean _ignoreExceptions;
        
        /**
         * Default ScriptContext object.
         */
        public ScriptContext()
        {
            this(DEFAULT_SEPARATOR, false);
        }
        
        /**
         * Build a ScriptContext object.
         * @param separator the separator
         * @param ignoreExceptions true to ignore exceptions.
         */
        public ScriptContext(String separator, boolean ignoreExceptions)
        {
            this._separator = separator;
            this._ignoreExceptions = ignoreExceptions;
        }
        
        /**
         * Get the separator.
         * @return the separator
         */
        public String getSeparator()
        {
            return _separator;
        }
        
        /**
         * Set the separator.
         * @param separator the separator to set
         */
        public void setSeparator(String separator)
        {
            this._separator = separator;
        }
        
        /**
         * Get the ignoreExceptions.
         * @return the ignoreExceptions
         */
        public boolean ignoreExceptions()
        {
            return _ignoreExceptions;
        }
        
        /**
         * Set the ignoreExceptions.
         * @param ignoreExceptions the ignoreExceptions to set
         */
        public void setIgnoreExceptions(boolean ignoreExceptions)
        {
            this._ignoreExceptions = ignoreExceptions;
        }
        
    }
    
}
