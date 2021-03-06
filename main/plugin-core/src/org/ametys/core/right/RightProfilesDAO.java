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
package org.ametys.core.right;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.ibatis.session.SqlSession;

import org.ametys.core.ObservationConstants;
import org.ametys.core.datasource.AbstractMyBatisDAO;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.runtime.request.RequestListener;

/**
 * Manages registration of profiles
 */
public class RightProfilesDAO extends AbstractMyBatisDAO implements RequestListener
{
    /** The component role. */
    public static final String ROLE = RightProfilesDAO.class.getName();
    
    /**
     * This cache is for storing the set of profiles a right belongs to.
     * { RightId : {[ProfileIds]}
     */
    private final ThreadLocal<Map<String, Set<String>>> _cacheProfilesTL = new ThreadLocal<>();

    private ServiceManager _smanager;

    private ObservationManager _observationManager;

    private CurrentUserProvider _currentUserProvider;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
        super.service(smanager);
    }
    
    /**
     * Get all existing profiles
     * @return The list for profiles
     */
    public List<Profile> getProfiles()
    {
        try (SqlSession session = getSession())
        {
            return session.selectList("Profiles.getProfiles");
        }
    }
    
    /**
     * Get the profiles on a given context
     * @param context The context. Can be null. If null, the profiles with no context are returned.
     * @return The list for profiles for this context
     */
    public List<Profile> getProfiles(String context)
    {
        try (SqlSession session = getSession())
        {
            if (context == null)
            {
                return session.selectList("Profiles.getProfilesWithNullContext");
            }
            else
            {
                return session.selectList("Profiles.getProfilesByContext");
            }
        }
    }
    
    /**
     * Get the profile with given identifier
     * @param id The id of profile to retrieve
     * @return The profile
     */
    public Profile getProfile(String id)
    {
        try (SqlSession session = getSession())
        {
            return session.selectOne("Profiles.getProfile", id);
        }
    }
    
    /**
     * Get all profiles containing the right wth given id
     * @param rightId The id of right
     * @return The id of profiles with this right
     */
    public Set<String> getProfilesWithRight (String rightId)
    {
        Map<String, Set<String>> cache = _cacheProfilesTL.get();
        
        if (cache == null)
        {
            // Build the cache with only one SQL query
            cache = new HashMap<>();
            
            try (SqlSession session = getSession())
            {
                List<Map<String, String>> profileRights = session.selectList("Profiles.getProfileRights");
                
                for (Map<String, String> profileRight : profileRights)
                {
                    String currentProfileId = profileRight.get("profileId");
                    String currentRightId = profileRight.get("rightId");
                    
                    if (cache.containsKey(currentRightId))
                    {
                        cache.get(currentRightId).add(currentProfileId);
                    }
                    else
                    {
                        Set<String> profiles = new HashSet<>();
                        profiles.add(currentProfileId);
                        cache.put(currentRightId, profiles);
                    }
                }
            }
        }
        
        if (cache.containsKey(rightId))
        {
            return cache.get(rightId);
        }
        else
        {
            return Collections.EMPTY_SET;
        }
    }
    
    /**
     * Creates a new profile with null context. The identifier of the profile will be automatically generated from label.
     * @param label The label of profile
     * @return The create profile
     */
    public Profile addProfile (String label)
    {
        return addProfile(label, null);
    }
    
    /**
     * Creates a new profile. The identifier of the profile will be automatically generated from label.
     * @param label The label of profile
     * @param context The context. Can be null
     * @return The create profile
     */
    public Profile addProfile (String label, String context)
    {
        String id = _generateUniqueId(label);
        Profile profile = new Profile(id, label, context);
        addProfile(profile);
        return profile;
    }
    
    private String _generateUniqueId(String label)
    {
        // Id generated from name lowercased, trimmed, and spaces and underscores replaced by dashes
        String value = label.toLowerCase().trim().replaceAll("[\\W_]", "-").replaceAll("-+", "-").replaceAll("^-", "");
        int i = 2;
        String suffixedValue = value;
        while (getProfile(suffixedValue) != null)
        {
            suffixedValue = value + i;
            i++;
        }
        
        return suffixedValue;
    }
    
    /**
     * Creates a new profile
     * @param id The unique identifier of profile
     * @param label The label of profile
     * @param context The context. Can be null
     * @return The create profile
     */
    public Profile addProfile (String id, String label, String context)
    {
        Profile profile = new Profile(id, label, context);
        addProfile(profile);
        return profile;
    }
    
    /**
     * Add a new profile
     * @param profile The profile to add
     * @param silent Set to true to not notify observer of this update
     */
    public void addProfile (Profile profile, boolean silent)
    {
        try (SqlSession session = getSession(true))
        {
            session.insert("Profiles.addProfile", profile);
            
            if (!silent)
            {
                _notifyEvent(profile, ObservationConstants.EVENT_PROFILE_ADDED);
            }
        }
    }
    
    /**
     * Add a new profile
     * @param profile The profile to add
     */
    public void addProfile (Profile profile)
    {
        addProfile(profile, false);
    }
    
    /**
     * Rename a profile
     * @param profile The profile to rename
     * @param newLabel The updated label
     */
    public void renameProfile (Profile profile, String newLabel)
    {
        renameProfile(profile, newLabel, false);
    }
    
    /**
     * Rename a profile
     * @param profile The profile to rename
     * @param newLabel The updated label
     * @param silent Set to true to not notify observer of this update
     */
    public void renameProfile (Profile profile, String newLabel, boolean silent)
    {
        try (SqlSession session = getSession(true))
        {
            Map<String, Object> params = new HashMap<>();
            params.put("id", profile.getId());
            params.put("label", newLabel);
            session.update("Profiles.renameProfile", params);
            
            if (!silent)
            {
                _notifyEvent(profile, ObservationConstants.EVENT_PROFILE_UPDATED);
            }
        }
    }
    
    /**
     * Get the rights of a profile
     * @param profile The profile
     * @return The rights
     */
    public List<String> getRights (Profile profile)
    {
        try (SqlSession session = getSession())
        {
            return session.selectList("Profiles.getRights", profile.getId());
        }
    }
    
    /**
     * Add a right to a profile
     * @param profile The profile
     * @param rightId The id of right to add
     */
    public void addRight (Profile profile, String rightId)
    {
        try (SqlSession session = getSession(true))
        {
            _addRight (session, profile, rightId);
        }
    }
    
    /**
     * Add a right to a profile
     * @param profile The profile
     * @param rightIds The id of rights to add
     */
    public void addRights (Profile profile, List<String> rightIds)
    {
        try (SqlSession session = getSession())
        {
            for (String rightId : rightIds)
            {
                _addRight (session, profile, rightId);
            }
            
            session.commit();
        }
    }
    
    /**
     * Update the rights of a profile
     * @param profile The profile
     * @param rights The rights of the profile
     */
    public void updateRights (Profile profile, List<String> rights)
    {
        updateRights(profile, rights, false);
    }
    
    /**
     * Update the rights of a profile
     * @param profile The profile
     * @param rights The rights of the profile
     * @param silent Set to true to not notify observer of this update
     */
    public void updateRights (Profile profile, List<String> rights, boolean silent)
    {
        try (SqlSession session = getSession())
        {
            session.delete("Profiles.deleteProfileRights", profile.getId());
            
            if (rights != null)
            {
                for (String rightId : rights)
                {
                    _addRight (session, profile, rightId);
                }
            }
            
            session.commit();
            
            if (!silent)
            {
                _notifyEvent(profile, ObservationConstants.EVENT_PROFILE_UPDATED);
            }
        }
    }
    
    private void _addRight (SqlSession session, Profile profile, String rightId)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("profileId", profile.getId());
        params.put("rightId", rightId);
        
        session.insert("Profiles.addRight", params);
    }
    
    /**
     * Add a right to a profile
     * @param profile The profile
     */
    public void removeRights (Profile profile)
    {
        removeRights(profile, false);
    }
    
    /**
     * Add a right to a profile
     * @param profile The profile
     * @param silent Set to true to not notify observer of this update
     */
    public void removeRights (Profile profile, boolean silent)
    {
        try (SqlSession session = getSession(true))
        {
            session.delete("Profiles.deleteProfileRights", profile.getId());
            
            if (!silent)
            {
                _notifyEvent(profile, ObservationConstants.EVENT_PROFILE_UPDATED);
            }
        }
    }
    
    /**
     * Delete a profile
     * @param profile The profile to delete
     */
    public void deleteProfile (Profile profile)
    {
        deleteProfile(profile, false);
    }
    
    /**
     * Delete a profile
     * @param profile The profile to delete
     * @param silent Set to true to not notify observer of this update
     */
    public void deleteProfile (Profile profile, boolean silent)
    {
        try (SqlSession session = getSession())
        {
            session.delete("Profiles.deleteProfile", profile.getId());
            session.delete("Profiles.deleteProfileRights", profile.getId());
            
            session.commit();
            
            if (!silent)
            {
                _notifyEvent(profile, ObservationConstants.EVENT_PROFILE_DELETED);
            }
        }
    }
    
    @Override
    public void requestStarted(HttpServletRequest req)
    {
        // Nothing to do
        
    }
    
    @Override
    public void requestEnded(HttpServletRequest req)
    {
        if (_cacheProfilesTL.get() != null)
        {
            _cacheProfilesTL.set(null);
        }
        
    }
    
    private void _notifyEvent (Profile profile, String eventId)
    {
        try
        {
            if (_observationManager == null)
            {
                _observationManager = (ObservationManager) _smanager.lookup(ObservationManager.ROLE);
            }
            if (_currentUserProvider == null)
            {
                _currentUserProvider = (CurrentUserProvider) _smanager.lookup(CurrentUserProvider.ROLE);
            }
            
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(ObservationConstants.ARGS_PROFILE, profile);
            _observationManager.notify(new Event(eventId, _currentUserProvider.getUser(), eventParams));
        }
        catch (ServiceException e)
        {
            getLogger().error("Fail to notify observers for event '" + eventId + "'", e);
        }
    }
}
