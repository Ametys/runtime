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
package org.ametys.core.user.population;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * This class represents a population of users.
 */
public class UserPopulation
{
    /** The id */
    protected String _id;
    
    /** The label */
    protected I18nizableText _label;
    
    /** The enabled nature of the population. True if enabled, false otherwise. */
    protected boolean _enabled;
    
    /** The list of the user directories */
    protected List<UserDirectory> _userDirectories;
    
    /** The list of the credential providers */
    protected List<CredentialProvider> _credentialProviders;
    
    /**
     * Default constructor
     */
    public UserPopulation()
    {
        super();
        _enabled = true;
        _userDirectories = new ArrayList<>();
        _credentialProviders = new ArrayList<>();
    }
    
    /**
     * Get the label of the population.
     * @return the label of the population
     */
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    /**
     * Set the label of the population.
     * @param label the label
     */
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    /**
     * Tells if the population is enabled.
     * @return True if the population is enabled, false otherwise
     */
    public boolean isEnabled()
    {
        return _enabled;
    }

    /**
     * Enables/disables a population.
     * @param enabled True to enable the population, false to disable it.
     */
    public void enable(boolean enabled)
    {
        this._enabled = enabled;
    }

    /**
     * Get the id of the population.
     * @return the id of the population
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Set the id of the population.
     * @param id the id
     */
    public void setId(String id)
    {
        _id = id;
    }
    
    /**
     * Get the associated {@link UserDirectory}s
     * @return The associated user directories
     */
    public List<UserDirectory> getUserDirectories()
    {
        return _userDirectories;
    }
    
    
    /**
     * Get the select UserDirectory
     * @param id The id of the directory to get
     * @return The associated user directory or null
     */
    public UserDirectory getUserDirectory(String id)
    {
        Optional<UserDirectory> findAny = _userDirectories.stream().filter(ud -> StringUtils.equals(ud.getId(), id)).findAny();
        if (findAny.isPresent())
        {
            return findAny.get();
        }
        return null;
    }
    
    /**
     * Set the user directories
     * @param userDirectories The list of {@link UserDirectory}s to set.
     */
    public void setUserDirectories(List<UserDirectory> userDirectories)
    {
        resetUserDirectories();
        _userDirectories = userDirectories;
    }
    
    /**
     * Reset the user directories, i.e. remove all the user directories linked to this population.
     */
    public void resetUserDirectories()
    {
        _userDirectories.clear();
    }

    /**
     * Get the associated {@link CredentialProvider}s
     * @return The associated credential providers
     */
    public List<CredentialProvider> getCredentialProviders()
    {
        return _credentialProviders;
    }
    
    /**
     * Set the credential providers
     * @param credentialProviders The list of {@link CredentialProvider}s to set
     */
    public void setCredentialProvider(List<CredentialProvider> credentialProviders)
    {
        resetCredentialProviders();
        _credentialProviders = credentialProviders;
    }
    
    /**
     * Reset the credential providers, i.e. remove all the credential providers linked to this population.
     */
    public void resetCredentialProviders()
    {
        _credentialProviders.clear();
    }
    
    /**
     * Dispose the user directories and credential providers of this population.
     */
    public void dispose()
    {
        for (UserDirectory ud : _userDirectories)
        {
            LifecycleHelper.dispose(ud);
        }
        resetUserDirectories();
        
        for (CredentialProvider cp : _credentialProviders)
        {
            LifecycleHelper.dispose(cp);
        }
        resetCredentialProviders();
    }
    
    @Override
    public String toString()
    {
        return super.toString() + "[" + _id + "]";
    }
}
