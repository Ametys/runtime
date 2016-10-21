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
package org.ametys.core.authentication;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * This class representes a model for a {@link CredentialProvider}
 */
public interface CredentialProviderModel
{
    /**
     * Get the id of this credential provider
     * @return the id of this credential provider
     */
    public String getId();
    
    /**
     * Get the label of the credential provider.
     * @return the label of the credential provider
     */
    public I18nizableText getLabel();
    
    /**
     * Get the description text of the credential provider.
     * @return the description of the credential provider
     */
    public I18nizableText getDescription();
    
    /**
     * Get the label for the connection screen of the credential provider
     * @return the label for the connection screen of the credential provider
     */
    public I18nizableText getConnectionLabel();
    
    /**
     * Get the CSS class for the glyph icon
     * @return the CSS class for the glyph icon
     */
    public String getIconGlyph();
    
    /**
     * Get the CSS class for the glyph decorator icon
     * @return the CSS class for the glyph decorator icon
     */
    public String getIconDecorator();
    
    /**
     * Get the path of the small icon resource
     * @return the path of the small icon resource
     */
    public String getIconSmall();
    
    /**
     * Get the path of the small icon resource
     * @return the path of the small icon resource
     */
    public String getIconMedium();
    
    /**
     * Get the path of the small icon resource
     * @return the path of the small icon resource
     */
    public String getIconLarge();
    
    /**
     * Get the color of the credential provider
     * @return the color of the credential provider
     */
    public String getColor();
    
    /**
     * Get the configuration parameters
     * @return The configuration parameters
     */
    public Map<String, ? extends Parameter<ParameterType>> getParameters();
    
    /**
     * Get the configuration parameter checkers
     * @return The configuration parameter checkers
     */
    public Map<String, ? extends ParameterCheckerDescriptor> getParameterCheckers();
    
    /**
     * Returns the plugin name of declaration (for debug purpose)
     * @return the plugin name
     */
    public String getPluginName();
    
    /**
     * Get the credential provider class
     * @return the credential provider class
     */
    public Class<CredentialProvider> getCredentialProviderClass();
    
    /**
     * Get the additional configuration for the implementation of {@link CredentialProvider}
     * @return the additional configuration.
     */
    public Configuration getCredentialProviderConfiguration ();
}
