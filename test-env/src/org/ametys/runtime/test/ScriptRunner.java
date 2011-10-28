/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.io.IOUtils;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.util.LoggerFactory;

/**
 * Tool to run SQL scripts.<p>
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
public abstract class ScriptRunner
{
    /** Default separator used for isolating statements. */
    public static final String DEFAULT_SEPARATOR = ";";
    /** Command to ignore sql exceptions. */
    public static final String IGNORE_EXCEPTIONS_COMMAND = "_ignore_exceptions_=";
    /** Command to change the separator. */
    public static final String CHANGE_SEPARATOR_COMMAND = "_separator_=";
    /** Logger available to subclasses. */
    protected static final Logger __LOGGER = LoggerFactory.getLoggerFor(ScriptRunner.class);
    
    private ScriptRunner()
    {
        // Nothing to do
    }
    
    /**
     * Run a SQL script using a connection from the core pool.
     * @param is the input stream containing the script data.
     * @throws IOException if an error occurs while reading the script.
     * @throws SQLException if an error occurs while executing the script.
     */
    public static void runScript(InputStream is) throws IOException, SQLException
    {
        Connection connection = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME);
            runScript(connection, is);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
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
        String separator = DEFAULT_SEPARATOR;
        boolean ignoreExceptions = false;
        StringBuilder command = new StringBuilder();
        
        try
        {
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = lineReader.readLine()) != null)
            {
                if (__LOGGER.isDebugEnabled())
                {
                    __LOGGER.debug(String.format("Reading line: '%s'", line));
                }
                
                boolean processCommand = false;
                String trimmedLine = line.trim();
                if (trimmedLine.length() == 0 || trimmedLine.startsWith("//") || trimmedLine.startsWith("--"))
                {
                    String currentSeparator = separator;
                    
                    // Search if the separator needs to be changed
                    if (trimmedLine.contains(CHANGE_SEPARATOR_COMMAND))
                    {
                        // New separator
                        separator = trimmedLine.substring(trimmedLine.indexOf(CHANGE_SEPARATOR_COMMAND)
                                    + CHANGE_SEPARATOR_COMMAND.length()).trim();
                        
                        if (__LOGGER.isDebugEnabled())
                        {
                            __LOGGER.debug(String.format("Changing separator to: '%s'", separator));
                        }
                    }
                    else if (trimmedLine.contains(IGNORE_EXCEPTIONS_COMMAND))
                    {
                        String ignoreStr = trimmedLine.substring(trimmedLine.indexOf(IGNORE_EXCEPTIONS_COMMAND)
                                    + IGNORE_EXCEPTIONS_COMMAND.length()).trim();
                        
                        ignoreExceptions = "on".equals(ignoreStr);
                        
                        if (__LOGGER.isDebugEnabled())
                        {
                            __LOGGER.debug(String.format("Ignore exceptions: '%s'", ignoreExceptions ? "on" : "off"));
                        }
                    }
                    
                    if (trimmedLine.contains(currentSeparator))
                    {
                        if (command.length() > 0)
                        {
                            // End of command but do not use current line
                            processCommand = true;
                        }
                    }
                        
                }
                else if (trimmedLine.endsWith(separator))
                {
                    // End of command and use current line
                    processCommand = true;
                    command.append(line.substring(0, line.lastIndexOf(separator)));
                }
                else
                {
                    // Append current command to the buffer
                    command.append(line);
                    command.append(" ");
                }
                
                if (processCommand)
                {
                    _process(connection, command, lineReader.getLineNumber(), ignoreExceptions);
                }
            }
            
            if (command.length() > 0)
            {
                _process(connection, command, lineReader.getLineNumber(), ignoreExceptions);
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
            
            IOUtils.closeQuietly(is);
        }
    }
    
    private static void _process(Connection connection, StringBuilder command, int lineNumber, boolean ignoreExceptions) throws SQLException
    {
        if (__LOGGER.isInfoEnabled())
        {
            __LOGGER.info(String.format("Executing SQL command: '%s'", command));
        }
        
        _execute(connection, command.toString(), lineNumber, ignoreExceptions);

        // Clear command
        command.setLength(0);
    }
    
    private static void _execute(Connection connection, String command, int lineNumber, boolean ignoreExceptions) throws SQLException
    {
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
            statement.execute(command);
        }
        catch (SQLException e)
        {
            if (!ignoreExceptions)
            {
                String message = String.format("Unable to execute SQL: '%s' at line %d", command, lineNumber);
                __LOGGER.error(message, e);
                
                throw new SQLException(message);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(statement);
        }

    }
}
