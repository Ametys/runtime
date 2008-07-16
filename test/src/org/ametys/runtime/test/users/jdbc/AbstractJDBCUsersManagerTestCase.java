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
package org.ametys.runtime.test.users.jdbc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.UsersManager;

/**
 * Reset the jdbc user db and load the user manager 
 */
public class AbstractJDBCUsersManagerTestCase extends AbstractJDBCTestCase
{
    /** the user manager */
    protected UsersManager _usersManager;
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @throws Exception
     */
    protected void _resetDB(String runtimeFilename, String configFileName) throws Exception
    {
        _configureRuntime("test/environments/runtimes/" + runtimeFilename);
        Config.setFilename("test/environments/configs/" + configFileName);
        super.setUp();
        
        _startCocoon("test/environments/webapp1");

        List<File> scripts = new ArrayList<File>();
        scripts.add(new File("main/plugin-core/scripts/mysql/jdbc_users.sql"));
        _setDatabase(scripts);

        _usersManager = (UsersManager) Init.getPluginServiceManager().lookup(UsersManager.ROLE);
    }
    

}
