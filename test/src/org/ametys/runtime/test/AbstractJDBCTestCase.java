/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;

import org.ametys.runtime.util.ConnectionHelper;

/**
 * Abstract test case for jdbc Runtime test cases.
 */
public abstract class AbstractJDBCTestCase extends AbstractRuntimeTestCase
{
    /**
     * Reset the database and insert the scripts. Needs the Init class.
     * @param scripts A list of script to play on db. Scripts should reset tables.
     * @throws Exception
     */
    protected void _setDatabase(List<File> scripts) throws Exception
    {
        Connection connection = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME);
            
            for (File script : scripts)
            {
                ScriptRunner.runScript(connection, new FileInputStream(script));
            }
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
}
