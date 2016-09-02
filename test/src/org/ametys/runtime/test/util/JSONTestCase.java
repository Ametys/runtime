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
package org.ametys.runtime.test.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.core.ui.RibbonControlsManager;
import org.ametys.core.util.JSONUtils;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

import junit.framework.TestCase;

/**
 * {@link TestCase} for converting or parsing JSON string
 */
public class JSONTestCase extends AbstractRuntimeTestCase
{
    JSONUtils _jsonUtils;
    RibbonControlsManager _ribbonManager;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        
        ServiceManager manager = Init.getPluginServiceManager();
        _jsonUtils = (JSONUtils) manager.lookup(JSONUtils.ROLE);
        _ribbonManager = (RibbonControlsManager) manager.lookup("ribbonManagerTest");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test JSON to Map conversion
     */
    public void testJsonToMap()
    {
        String jsonString = "{\"label\": \"label\", \"default-description\": \"description\", \"icon-small\": \"plugins/core/resources/img/icon_small.gif\", \"icon-medium\": \"plugins/core/resources/img/icon_medium.gif\", \"icon-large\": \"plugins/core/resources/img/icon_large.gif\", \"menu-item\": [{\"id\": \"menu-item-1\", \"label\": \"label\"}, {\"id\": \"menu-item-2\", \"label\": \"label\"}]}";
        Map<String, Object> map = new HashMap<>();
        try
        {
            map = _jsonUtils.convertJsonToMap(jsonString);
        }
        catch (Exception e)
        {
            // Nothing
        }
        
        assertTrue(map.size() != 0);
        assertEquals(map.get("label"), "label");
        assertEquals(((List) map.get("menu-item")).size(), 2);
        assertEquals(((Map) ((List) map.get("menu-item")).get(0)).get("id"), "menu-item-1");
        
    }
    
    /**
     * Test Map to JSON conversion
     */
    public void testMapToJson ()
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        ClientSideElement itemFactory3 = _ribbonManager.getExtension("staticuiitemfactorytest.5");
        Map<String, Object> parameters = null;
        for (Script script : itemFactory3.getScripts(null))
        {
            parameters = script.getParameters();
        }
        
        String expectedJson = "{\"label\":\"label\",\"default-description\":\"description\",\"icon-small\":\"/plugins/core/resources/img/icon_small.gif\",\"icon-medium\":\"/plugins/core/resources/img/icon_medium.gif\",\"icon-large\":\"/plugins/core/resources/img/icon_large.gif\",\"child\":{\"id\":\"child-1\",\"label\":\"label\"}}";
        String json = _jsonUtils.convertObjectToJson(parameters);
        assertNotNull(json);
        assertEquals(expectedJson, json);
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
}
