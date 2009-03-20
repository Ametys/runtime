package org.ametys.runtime.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.ametys.runtime.util.ConnectionHelper;
import org.ametys.runtime.util.LoggerFactory;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.io.IOUtils;

/**
 * Tool to run SQL scripts.
 */
public abstract class ScriptRunner
{
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
        StringBuilder command = new StringBuilder();
        
        try
        {
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = lineReader.readLine()) != null)
            {
                String trimmedLine = line.trim();
                if (trimmedLine.length() == 0 || trimmedLine.startsWith("//") || trimmedLine.startsWith("--"))
                {
                    // Ignore empty lines and comments
                }
                else if (trimmedLine.endsWith(";"))
                {
                    command.append(line.substring(0, line.lastIndexOf(";")));
                    
                    Statement statement = connection.createStatement();
                    
                    if (__LOGGER.isDebugEnabled())
                    {
                        __LOGGER.debug(String.format("Executing SQL command: '%s'", command));
                    }

                    try
                    {
                        statement.execute(command.toString());
                    }
                    catch (SQLException e)
                    {
                        String message = String.format("Unable to execute SQL: '%s' at line %d", command, lineReader.getLineNumber());
                        SQLException wrappedException = new SQLException(message);
                        wrappedException.setNextException(e);
                        throw e;
                    }
                    finally
                    {
                        ConnectionHelper.cleanup(statement);
                    }

                    // Clear command
                    command.setLength(0);
                }
                else
                {
                    // Append current command to the buffer
                    command.append(line);
                    command.append(" ");
                }
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
}
