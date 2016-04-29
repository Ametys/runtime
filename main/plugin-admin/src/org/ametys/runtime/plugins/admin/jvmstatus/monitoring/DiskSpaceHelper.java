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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring;

import org.apache.avalon.framework.component.Component;
import org.apache.commons.io.FileUtils;

import org.ametys.runtime.util.AmetysHomeHelper;

/**
 * Helper for getting information about the use of the disk space of Ametys home directory
 */
public class DiskSpaceHelper implements Component
{
    /** Avalon Role */
    public static final String ROLE = DiskSpaceHelper.class.getName();
    
    /**
     * Gets the space used by the ametys home directory
     * @return The space used by the ametys home directory in bytes
     */
    public long getUsedSpace()
    {
        return FileUtils.sizeOfDirectory(AmetysHomeHelper.getAmetysHome());
    }
    
    /**
     * Gets the free space remaining on the disk of Ametys home directory
     * @return The free space remaining on the disk of Ametys home directory in bytes
     */
    public long getAvailableSpace()
    {
        return AmetysHomeHelper.getAmetysHome().getFreeSpace();
    }

}
