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
package org.ametys.runtime.test.rights.access.controller;

import org.ametys.core.right.AccessController;
import org.ametys.runtime.test.rights.TestAccessController;

/**
 * Common test class for testing a string based {@link AccessController}
 */
public abstract class AbstractStringBasedAccessControllerTestCase extends AbstractAccessControllerTestCase
{
    @Override
    protected String _getExtensionId()
    {
        return TestAccessController.class.getName();
    }

    @Override
    protected Object _getTest1()
    {
        return "/test";
    }
    
    @Override
    protected Object _getTest2()
    {
        return "/test2";
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime4.xml", "test/environments/configs/config1.xml", null, "test/environments/webapp4");
    }
}
