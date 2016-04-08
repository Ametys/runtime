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
package org.ametys.core.group.directory;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * This class represents a model for a {@link GroupDirectory}
 */
public interface GroupDirectoryModel
{
    /**
     * Get the id of this user directory
     * @return the id of this user directory
     */
    public String getId();
    
    /**
     * Get the label of the directory.
     * @return the label of the directory
     */
    public I18nizableText getLabel();
    
    /**
     * Get the description text of the directory.
     * @return the description of the directory
     */
    public I18nizableText getDescription();
    
    /**
     * Get the configuration parameters
     * @return The configuration parameters
     */
    public Map<String, ? extends Parameter<ParameterType>> getParameters();
    
    /**
     * Returns the plugin name of declaration (for debug purpose)
     * @return the plugin name
     */
    public String getPluginName();
    
    /**
     * Get the group directory class
     * @return the group directory class
     */
    public Class<GroupDirectory> getGroupDirectoryClass();
    
    /**
     * Get the additional configuration for the implementation of {@link GroupDirectory}
     * @return the additional configuration.
     */
    public Configuration getGroupDirectoryConfiguration();
}
