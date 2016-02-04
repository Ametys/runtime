/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.util;

import java.io.File;

import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Helper for Ametys home locations
 *
 */
public final class AmetysHomeHelper
{
    /** The path to the Ametys home data directory */
    public static String AMETYS_HOME_DATA_DIR = "data";
    /** The path to the Ametys home config directory */
    public static String AMETYS_HOME_CONFIG_DIR = "config";
    /** The path to the Ametys home temporary directory */
    public static String AMETYS_HOME_TMP_DIR = "tmp";
    
    private AmetysHomeHelper()
    {
        // Helper class, never to be instantiated.
    }
    
    /**
     * Returns the Ametys home directory. Cannot be null.
     * @return The Ametys home directory.
     */
    public static File getAmetysHome()
    {
        return RuntimeConfig.getInstance().getAmetysHome();
    }
    
    /**
     * Returns the Ametys home data directory. Cannot be null.
     * @return The Ametys home data directory.
     */
    public static File getAmetysHomeData()
    {
        return new File(RuntimeConfig.getInstance().getAmetysHome(), AMETYS_HOME_DATA_DIR);
    }
    
    /**
     * Returns the Ametys home config directory. Cannot be null.
     * @return The Ametys home config directory.
     */
    public static File getAmetysHomeConfig()
    {
        return new File(RuntimeConfig.getInstance().getAmetysHome(), AMETYS_HOME_CONFIG_DIR);
    }
    
    /**
     * Returns the Ametys temporary directory. Cannot be null.
     * @return The Ametys temporary directory.
     */
    public static File getAmetysHomeTmp()
    {
        return new File(RuntimeConfig.getInstance().getAmetysHome(), AMETYS_HOME_TMP_DIR);
    }
    
}
