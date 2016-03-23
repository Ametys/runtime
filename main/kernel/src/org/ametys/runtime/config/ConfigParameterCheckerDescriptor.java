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
package org.ametys.runtime.config;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;

/**
 * Descriptor for a configuration parameter checker
 */
public class ConfigParameterCheckerDescriptor extends ParameterCheckerDescriptor
{
    /** The path of the configuration parameter to attach this parameter parameter checker to */
    protected String _uiRefParamPath;
    
    /** The configuration group of the checked parameter */
    protected I18nizableText _uiRefGroup;
    
    /** The configuration category of the checked parameter */
    protected I18nizableText _uiRefCategory;

    /**
     * Retrieves the id of the parameter the parameter checker is attached to
     * @return _uiRefParamId the id of the parameter
     */
    public String getUiRefParamId()
    {
        return _uiRefParamPath;
    }
    
    /**
     * Sets the path of the parameter the parameter checker is attached to
     * @param uiRefParamPath the id of the parameter
     */
    public void setUiRefParamPath(String uiRefParamPath)
    {
        _uiRefParamPath = uiRefParamPath;
    }
    
    /**
     * Retrieves the text of the configuration group the parameter checker is attached to
     * @return _uiRefGroup the text of the configuration group
     */
    public I18nizableText getUiRefGroup()
    {
        return _uiRefGroup;
    }
    
    /**
     * Sets the text of the configuration group the parameter checker is attached to
     * @param uiRefGroup the text of the configuration group
     */
    public void setUiRefGroup(I18nizableText uiRefGroup)
    {
        _uiRefGroup = uiRefGroup;
    }
    
    /**
     * Retrieves the text of the configuration category the parameter checker is attached to
     * @return _uiRefCategory the text of the configuration group
     */
    public I18nizableText getUiRefCategory()
    {
        return _uiRefCategory;
    }
    
    /**
     * Sets the text of the configuration category the parameter checker is attached to
     * @param uiRefCategory the text of the configuration group
     */
    public void setUiRefCategory(I18nizableText uiRefCategory)
    {
        _uiRefCategory = uiRefCategory;
    }
}
