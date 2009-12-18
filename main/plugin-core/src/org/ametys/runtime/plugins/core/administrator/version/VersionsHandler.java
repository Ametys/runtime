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
package org.ametys.runtime.plugins.core.administrator.version;

import java.util.Collection;

/**
 * Administrator component able to determine the content of the "Versions" panel in the administrator area.<br>
 * It may includes plugins versions, Runtime version, application version, ...
 */
public interface VersionsHandler
{
    /** Avalon Role */
    public static final String ROLE = VersionsHandler.class.getName();
    
    /**
     * Returns all Versions to be displayed in the administrator area
     * @return all Versions to be displayed in the administrator area
     */
    public Collection<Version> getVersions();
}
