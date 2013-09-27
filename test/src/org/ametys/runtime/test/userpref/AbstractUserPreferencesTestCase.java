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
package org.ametys.runtime.test.userpref;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.format.ISODateTimeFormat;

import org.ametys.runtime.plugins.core.userpref.UserPreferencesExtensionPoint;
import org.ametys.runtime.plugins.core.userpref.UserPreferencesManager;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Abstract class to test user preferences.
 */
public abstract class AbstractUserPreferencesTestCase extends AbstractJDBCTestCase
{
    
    private UserPreferencesExtensionPoint _userPrefsEP;
    
    private UserPreferencesManager _userPrefs;
    
    /**
     * Provide the scripts to run before each test invocation.
     * @return the scripts to run.
     */
    protected abstract File[] getScripts();
    
    /**
     * Provide the scripts to run to populate the database.
     * @return the scripts to run.
     */
    protected abstract File[] getPopulateScripts();
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName) throws Exception
    {
        super.setUp();
        
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/webapp1");
        
        _setDatabase(Arrays.asList(getScripts()));

        _userPrefsEP = (UserPreferencesExtensionPoint) Init.getPluginServiceManager().lookup(UserPreferencesExtensionPoint.ROLE);
        _userPrefs = (UserPreferencesManager) Init.getPluginServiceManager().lookup(UserPreferencesManager.ROLE);
    }
    
    /**
     * Check when the db is empty
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        Map<String, String> contextVars = Collections.emptyMap();
        
        // Declared user prefs.
        assertEquals(0, _userPrefsEP.getUserPreferences(contextVars).size());
        
        // User prefs in database.
        assertEquals(0, _userPrefs.getUnTypedUserPrefs("anonymous", "/", contextVars).size());
    }
    
    /**
     * Check when the db is filled
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        Map<String, String> contextVars = Collections.emptyMap();
        
        Map<String, String> prefs;
        
        prefs = _userPrefs.getUnTypedUserPrefs("user", "/empty", contextVars);
        
        assertEquals(0, prefs.size());
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/empty", contextVars, "nopref"));
        
        prefs = _userPrefs.getUnTypedUserPrefs("user", "/one", contextVars);
        
        assertEquals(1, prefs.size());
        assertEquals("one", prefs.get("pref1"));
        
        assertEquals("one", _userPrefs.getUserPreferenceAsString("user", "/one", contextVars, "pref1"));
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/one", contextVars, "nopref"));
        
        prefs = _userPrefs.getUnTypedUserPrefs("user", "/two", contextVars);
        
        assertEquals(2, prefs.size());
        assertEquals("one", prefs.get("pref1"));
        assertEquals("two", prefs.get("pref2"));
        
        assertEquals("one", _userPrefs.getUserPreferenceAsString("user", "/two", contextVars, "pref1"));
        assertEquals("two", _userPrefs.getUserPreferenceAsString("user", "/two", contextVars, "pref2"));
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/two", contextVars, "nopref"));
        
        prefs = _userPrefs.getUnTypedUserPrefs("user", "/all", contextVars);
        
        assertEquals(6, prefs.size());
        assertEquals("one", prefs.get("pref1"));
        assertEquals("two", prefs.get("pref2"));
        assertEquals("27", prefs.get("long"));
        assertEquals("3.14", prefs.get("double"));
        assertEquals("1987-10-09T00:00:00.000+02:00", prefs.get("date"));
        assertEquals("true", prefs.get("boolean"));
        
        Date date = ISODateTimeFormat.dateTime().parseDateTime("1987-10-09T00:00:00.000+02:00").toDate();
        
        assertEquals("one", _userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref1"));
        assertEquals("two", _userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref2"));
        assertEquals(27L, _userPrefs.getUserPreferenceAsLong("user", "/all", contextVars, "long").longValue());
        assertEquals(3.14, _userPrefs.getUserPreferenceAsDouble("user", "/all", contextVars, "double").doubleValue());
        assertEquals(date, _userPrefs.getUserPreferenceAsDate("user", "/all", contextVars, "date"));
        assertEquals(true, _userPrefs.getUserPreferenceAsBoolean("user", "/all", contextVars, "boolean").booleanValue());
        
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "nopref"));
    }
    
    /**
     * Check when the db is filled by the API.
     * @throws Exception if an error occurs
     */
    public void testSet() throws Exception
    {
        Map<String, String> contextVars = Collections.emptyMap();
        
        Map<String, String> prefs;
        
        prefs = _userPrefs.getUnTypedUserPrefs("user", "/all", contextVars);
        
        assertEquals(0, prefs.size());
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/empty", contextVars, "nopref"));
                
        Map<String, String> prefsToSet = new HashMap<String, String>();
        
        prefsToSet.put("pref1", "one");
        prefsToSet.put("pref2", "two");
        prefsToSet.put("long", "27");
        prefsToSet.put("double", "3.14");
        prefsToSet.put("date", "1987-10-09T00:00:00.000+02:00");
        prefsToSet.put("boolean", "true");
        
        _userPrefs.setUserPreferences("user", "/all", contextVars, prefsToSet);
        
        prefsToSet.clear();
        prefsToSet = _userPrefs.getUnTypedUserPrefs("user", "/all", contextVars);
        
        assertEquals(6, prefsToSet.size());
        assertEquals("one", prefsToSet.get("pref1"));
        assertEquals("two", prefsToSet.get("pref2"));
        assertEquals("27", prefsToSet.get("long"));
        assertEquals("3.14", prefsToSet.get("double"));
        assertEquals("1987-10-09T00:00:00.000+02:00", prefsToSet.get("date"));
        assertEquals("true", prefsToSet.get("boolean"));
        
        Date date = ISODateTimeFormat.dateTime().parseDateTime("1987-10-09T00:00:00.000+02:00").toDate();
        
        assertEquals("one", _userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref1"));
        assertEquals("two", _userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref2"));
        assertEquals(27L, _userPrefs.getUserPreferenceAsLong("user", "/all", contextVars, "long").longValue());
        assertEquals(3.14, _userPrefs.getUserPreferenceAsDouble("user", "/all", contextVars, "double").doubleValue());
        assertEquals(date, _userPrefs.getUserPreferenceAsDate("user", "/all", contextVars, "date"));
        assertEquals(true, _userPrefs.getUserPreferenceAsBoolean("user", "/all", contextVars, "boolean").booleanValue());
        
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "nopref"));
        
        // Remove a preference and test if it is null.
        _userPrefs.removeUserPreference("user", "/all", contextVars, "pref2");
        
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref2"));
        
        // Remove all preferences and test if the values are null and the value map is empty.
        _userPrefs.removeAllUserPreferences("user", "/all", contextVars);
        
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "pref1"));
        assertNull(_userPrefs.getUserPreferenceAsString("user", "/all", contextVars, "boolean"));
        assertEquals(0, _userPrefs.getUnTypedUserPrefs("user", "/all", contextVars).size());
    }
    
}
