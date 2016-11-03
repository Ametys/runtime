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
package org.ametys.plugins.core.impl.right;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.ibatis.session.SqlSession;

import org.ametys.core.datasource.AbstractMyBatisDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.ModifiableProfileAssignmentStorage;
import org.ametys.core.right.ProfileAssignmentStorage;
import org.ametys.core.user.UserIdentity;

/**
 * Jdbc implementation of {@link ProfileAssignmentStorage} which stores profile assignments in database.
 * This only supports String objects as contexts.
 */
public class JdbcProfileAssignmentStorage extends AbstractMyBatisDAO implements ModifiableProfileAssignmentStorage
{
    /** The handled context */
    protected String _supportedContext;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        _supportedContext = configuration.getChild("context").getValue();
    }
    
    /* -------------- */
    /* HAS PERMISSION */
    /* -------------- */
    
    @Override
    public boolean hasPermission(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds)
    {
        if (profileIds.isEmpty())
        {
            return false;
        }
        
        String prefix = getPrefix();
        
        // 1.1) Search at least one profile in "allowed-anonymous-profiles", if found return true
        if (_hasAnonymousAllowedProfile(profileIds, prefix))
        {
            return true;
        }
        
        // 2.1) Search at least one profile in "denied-profiles" for user, if found return false
        if (_hasDeniedProfile(user, profileIds, prefix))
        {
            return false;
        }
        
        // 2.2) Search at least one profile in "allowed-profiles" for user, if found return true
        if (_hasAllowedProfile(user, profileIds, prefix))
        {
            return true;
        }
        
        // 3.1) Search at least one profile in "denied-profiles" for groups, if found return false
        for (GroupIdentity group : userGroups)
        {
            if (_hasDeniedProfile(group, profileIds, prefix))
            {
                return false;
            }
        }
        
        // 3.2) Search at least one profile in "allowed-profiles" for groups, if found return true
        for (GroupIdentity group : userGroups)
        {
            if (_hasAllowedProfile(group, profileIds, prefix))
            {
                return true;
            }
        }
        
        // 4.1) Search at least one profile in "denied-any-connected-profiles", if found return false
        if (_hasAnyConnectedDeniedProfile(profileIds, prefix))
        {
            return false;
        }
            
        // 4.2) Search at least one profile in "allowed-any-connected-profiles", if found return true
        if (_hasAnyConnectedAllowedProfile(profileIds, prefix))
        {
            return true;
        }
        
        // 5.1) Search at least one profile in "denied-any-connected-profiles", if found return false
        if (_hasAnonymousDeniedProfile(profileIds, prefix))
        {
            return false;
        }
        
        // 5) Not found, return false
        return false;
    }
    
    /**
     * Get the object context with prefix if necessary
     * @param context The context object
     * @return The prefixed object
     */
    protected Object getObjectWithPrefix (Object context)
    {
        return context;
    }
    
    /**
     * Get the prefix for object context 
     * @return The prefix. Can be null if no prefix is necessary
     */
    protected String getPrefix ()
    {
        return null;
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for the user
     * @param user The user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for the user
     */
    protected boolean _hasDeniedProfile(UserIdentity user, Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
            return !deniedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for the user
     * @param user The user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for the user
     */
    protected boolean _hasAllowedProfile(UserIdentity user, Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
            return !allowedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for the group
     * @param group The group
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for the group
     */
    protected boolean _hasDeniedProfile(GroupIdentity group, Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("groupId", group.getId());
            parameters.put("groupDirectory", group.getDirectoryId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getGroupDeniedProfiles", parameters);
            return !deniedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for the group
     * @param group The group
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for the group
     */
    protected boolean _hasAllowedProfile(GroupIdentity group, Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("groupId", group.getId());
            parameters.put("groupDirectory", group.getDirectoryId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getGroupAllowedProfiles", parameters);
            return !allowedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for any connected user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for any connected user
     */
    protected boolean _hasAnyConnectedDeniedProfile(Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedDeniedProfiles", parameters);
            return !deniedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for any connected user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for any connected user
     */
    protected boolean _hasAnyConnectedAllowedProfile(Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedAllowedProfiles", parameters);
            return !allowedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for anonymous
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for anonymous
     */
    protected boolean _hasAnonymousDeniedProfile(Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnonymousDeniedProfiles", parameters);
            return !deniedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for anonymous
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for anonymous
     */
    protected boolean _hasAnonymousAllowedProfile(Set<String> profileIds, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            parameters.put("profileIds", profileIds);
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnonymousAllowedProfiles", parameters);
            return !allowedProfiles.isEmpty();
        }
    }
    
    /**
     * Returns the allowed profiles for the user on any context object (and not denied on the same object)
     * @param user The user
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return the allowed profiles for the user on any context object (and not denied on the same object)
     */
    protected Set<String> _getAllowedProfiles(UserIdentity user, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Set<String>> profilesByContext = new HashMap<>();
            
            // First get allowed profiles on context
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                String context = allowedProfile.get("context");
                String profileId = allowedProfile.get("profileId");
                
                if (!profilesByContext.containsKey(context))
                {
                    profilesByContext.put(context, new HashSet<>());
                }
                
                profilesByContext.get(context).add(profileId);
            }
            
            // Then remove the denied profiles one same context
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                String context = deniedProfile.get("context");
                String profileId = deniedProfile.get("profileId");
                
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).remove(profileId);
                    
                    if (profilesByContext.get(context).size() == 0)
                    {
                        profilesByContext.remove(context);
                    }
                }
            }
            
            // Return remaining profiles ignoring their context object
            return profilesByContext.entrySet().stream().flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        }
    }
    
    /**
     * Returns the allowed profiles for the group on any context object (and not denied on the same object)
     * @param group The group
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return the allowed profiles for the group on any context object (and not denied on the same object)
     */
    protected Set<String> _getAllowedProfiles(GroupIdentity group, String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Set<String>> profilesByContext = new HashMap<>();
            
            // First get allowed profiles on context
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("groupId", group.getId());
            parameters.put("groupDirectory", group.getDirectoryId());
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getGroupAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                String context = allowedProfile.get("context");
                String profileId = allowedProfile.get("profileId");
                
                if (!profilesByContext.containsKey(context))
                {
                    profilesByContext.put(context, new HashSet<>());
                }
                
                profilesByContext.get(context).add(profileId);
            }
            
            // Then remove the denied profiles one same context
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getGroupDeniedProfiles", parameters);
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                String context = deniedProfile.get("context");
                String profileId = deniedProfile.get("profileId");
                
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).remove(profileId);
                    
                    if (profilesByContext.get(context).size() == 0)
                    {
                        profilesByContext.remove(context);
                    }
                }
            }
            
            // Return remaining profiles ignoring their context object
            return profilesByContext.entrySet().stream().flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        }
    }
    
    /**
     * Returns the allowed profiles for any connected user on any context object (and not denied on the same object)
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return the allowed profiles for any connected user on any context object (and not denied on the same object)
     */
    protected Set<String> _getAnyConnectedAllowedProfiles(String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Set<String>> profilesByContext = new HashMap<>();
            
            // First get allowed profiles on context
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                String context = allowedProfile.get("context");
                String profileId = allowedProfile.get("profileId");
                
                if (!profilesByContext.containsKey(context))
                {
                    profilesByContext.put(context, new HashSet<>());
                }
                
                profilesByContext.get(context).add(profileId);
            }
            
            // Then remove the denied profiles one same context
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedDeniedProfiles", parameters);
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                String context = deniedProfile.get("context");
                String profileId = deniedProfile.get("profileId");
                
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).remove(profileId);
                    
                    if (profilesByContext.get(context).size() == 0)
                    {
                        profilesByContext.remove(context);
                    }
                }
            }
            
            // Return remaining profiles ignoring their context object
            return profilesByContext.entrySet().stream().flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        }
    }
    
    /**
     * Returns the allowed profiles for anonymous on any context object (and not denied on the same object)
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return the allowed profiles for anonymous on any context object (and not denied on the same object)
     */
    protected Set<String> _getAnonymousAllowedProfiles(String prefix)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Set<String>> profilesByContext = new HashMap<>();
            
            // First get allowed profiles on context
            Map<String, Object> parameters = new HashMap<>();
            if (prefix != null)
            {
                parameters.put("contextPrefix", prefix);
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnonymousAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                String context = allowedProfile.get("context");
                String profileId = allowedProfile.get("profileId");
                
                if (!profilesByContext.containsKey(context))
                {
                    profilesByContext.put(context, new HashSet<>());
                }
                
                profilesByContext.get(context).add(profileId);
            }
            
            // Then remove the denied profiles one same context
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnonymousDeniedProfiles", parameters);
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                String context = deniedProfile.get("context");
                String profileId = deniedProfile.get("profileId");
                
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).remove(profileId);
                    
                    if (profilesByContext.get(context).size() == 0)
                    {
                        profilesByContext.remove(context);
                    }
                }
            }
            
            // Return remaining profiles ignoring their context object
            return profilesByContext.entrySet().stream().flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        }
    }
    
    /* --------------------------------------- */
    /* ALLOWED PROFILES FOR ANY CONNECTED USER */
    /* --------------------------------------- */
    
    @Override
    public Set<String> getAllowedProfilesForAnyConnectedUser(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedAllowedProfiles", parameters);
            return allowedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public boolean isAnyConnectedUserAllowed(Object object, String profileId)
    {
        return getAllowedProfilesForAnyConnectedUser(object).contains(profileId);
    }
    
    @Override
    public void addAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            for (String profileId : profileIds)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedAllowedProfiles", parameters);
                
                if (allowedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addAllowedAnyConnected", parameters);
                }
                else
                {
                    getLogger().debug("Profile {} is already allowed for anyconnected on context {}", profileId, prefixedObject);
                }
                
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession(true))
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("context", object);
            parameters.put("profileIds", profileIds);
            session.delete("ProfilesAssignment.deleteAllowedAnyConnected", parameters);
        }
    }
    
    
    /* -------------------------------------- */
    /* DENIED PROFILES FOR ANY CONNECTED USER */
    /* -------------------------------------- */
    
    @Override
    public Set<String> getDeniedProfilesForAnyConnectedUser(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedDeniedProfiles", parameters);
            return deniedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public boolean isAnyConnectedUserDenied(Object object, String profileId)
    {
        return getDeniedProfilesForAnyConnectedUser(object).contains(profileId);
    }
    
    @Override
    public void addDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            
            for (String profileId : profileIds)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnyConnectedDeniedProfiles", parameters);
                
                if (deniedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addDeniedAnyConnected", parameters);
                }
                else
                {
                    getLogger().debug("Profile {} is already denied for anyconnected on context {}", profileId, prefixedObject);
                }
                
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession(true))
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("context", getObjectWithPrefix(object));
            parameters.put("profileIds", profileIds);
            session.delete("ProfilesAssignment.deleteDeniedAnyConnected", parameters);
        }
    }
    
    
    /* ------------------------------ */
    /* ALLOWED PROFILES FOR ANONYMOUS */
    /* ------------------------------ */
    
    @Override
    public Set<String> getAllowedProfilesForAnonymous(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnonymousAllowedProfiles", parameters);
            return allowedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public boolean isAnonymousAllowed(Object object, String profileId)
    {
        return getAllowedProfilesForAnonymous(object).contains(profileId);
    }
    
    @Override
    public void addAllowedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            
            for (String profileId : profileIds)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getAnonymousAllowedProfiles", parameters);
                
                if (allowedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addAllowedAnonymous", parameters);
                }
                else
                {
                    getLogger().debug("Profile {} is already allowed for anonymous on context {}", profileId, prefixedObject);
                }
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession(true))
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("context", getObjectWithPrefix(object));
            parameters.put("profileIds", profileIds);
            session.delete("ProfilesAssignment.deleteAllowedAnonymous", parameters);
        }
    }
    
    
    /* ----------------------------- */
    /* DENIED PROFILES FOR ANONYMOUS */
    /* ----------------------------- */
    
    @Override
    public Set<String> getDeniedProfilesForAnonymous(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnonymousDeniedProfiles", parameters);
            return deniedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public boolean isAnonymousDenied(Object object, String profileId)
    {
        return getDeniedProfilesForAnonymous(object).contains(profileId);
    }
    
    @Override
    public void addDeniedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            for (String profileId : profileIds)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getAnonymousDeniedProfiles", parameters);
                
                if (deniedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addDeniedAnonymous", parameters);
                }
                else
                {
                    getLogger().debug("Profile {} is already denied for anonymous on context {}", profileId, prefixedObject);
                }
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        try (SqlSession session = getSession(true))
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("context", getObjectWithPrefix(object));
            parameters.put("profileIds", profileIds);
            session.delete("ProfilesAssignment.deleteDeniedAnonymous", parameters);
        }
    }
    
    
    /* --------------------------- */
    /* MANAGEMENT OF ALLOWED USERS */
    /* --------------------------- */
    @Override
    public Set<String> getAllowedProfilesForUser(UserIdentity user, Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
            return allowedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public Map<UserIdentity, Set<String>> getAllowedProfilesForUsers(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<UserIdentity, Set<String>> profiledByUsers = new HashMap<>();
            
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                UserIdentity userIdentity = new UserIdentity(allowedProfile.get("login"), allowedProfile.get("population"));
                String profileId = allowedProfile.get("profileId");
                
                if (!profiledByUsers.containsKey(userIdentity))
                {
                    profiledByUsers.put(userIdentity, new HashSet<>());
                }
                profiledByUsers.get(userIdentity).add(profileId);
            }
            
            return profiledByUsers;
        }
    }
    
    @Override
    public Set<UserIdentity> getAllowedUsers(Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Set<UserIdentity> users = new HashSet<>();
            
            Map<String, Object> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", getObjectWithPrefix(object));
            }
            parameters.put("profileIds", Arrays.asList(profileId));
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                users.add(new UserIdentity(allowedProfile.get("login"), allowedProfile.get("population")));
            }
            
            return users;
        }
    }
    
    @Override
    public void addAllowedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getUserAllowedProfiles", parameters);
                
                if (allowedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addAllowedUser", parameters);
                }
                else
                {
                    getLogger().debug("Login {} has already profile {} on context {}", userIdentity, profileId, prefixedObject);
                }
                
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                parameters.put("profileIds", Arrays.asList(profileId));
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteAllowedUser", parameters);
            }
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedUsers(Set<UserIdentity> users, Object object)
    {
        try (SqlSession session = getSession())
        {
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteAllowedUser", parameters);
            }
            session.commit();
        }
    }
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF ALLOWED GROUPS */
    /* ---------------------------- */
    
    @Override
    public Map<GroupIdentity, Set<String>> getAllowedProfilesForGroups(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<GroupIdentity, Set<String>> profiledByGroups = new HashMap<>();
            
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getGroupAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                GroupIdentity gpIdentity = new GroupIdentity(allowedProfile.get("groupId"), allowedProfile.get("groupDirectory"));
                String profileId = allowedProfile.get("profileId");
                
                if (!profiledByGroups.containsKey(gpIdentity))
                {
                    profiledByGroups.put(gpIdentity, new HashSet<>());
                }
                profiledByGroups.get(gpIdentity).add(profileId);
            }
            
            return profiledByGroups;
        }
    }
    
    @Override
    public Set<GroupIdentity> getAllowedGroups(Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Set<GroupIdentity> groups = new HashSet<>();
            
            Map<String, Object> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", getObjectWithPrefix(object));
            }
            parameters.put("profileIds", Arrays.asList(profileId));
            
            List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getGroupAllowedProfiles", parameters);
            
            for (Map<String, String> allowedProfile : allowedProfiles)
            {
                groups.add(new GroupIdentity(allowedProfile.get("groupId"), allowedProfile.get("groupDirectory")));
            }
            
            return groups;
        }
    }
    
    @Override
    public void addAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> allowedProfiles = session.selectList("ProfilesAssignment.getGroupAllowedProfiles", parameters);
                
                if (allowedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addAllowedGroup", parameters);
                }
                else
                {
                    getLogger().debug("Group {} is already allowed for profile {} on context {}", group, profileId, prefixedObject);
                }
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                parameters.put("profileIds", Arrays.asList(profileId));
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteAllowedGroup", parameters);
            }
            session.commit();
        }
    }
    
    @Override
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object)
    {
        try (SqlSession session = getSession())
        {
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteAllowedGroup", parameters);
            }
            session.commit();
        }
    }
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF DENIED USERS */
    /* ---------------------------- */
    @Override
    public Set<String> getDeniedProfilesForUser(UserIdentity user, Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
            return deniedProfiles.stream().map(profile -> profile.get("profileId")).collect(Collectors.toSet());
        }
    }
    
    @Override
    public Map<UserIdentity, Set<String>> getDeniedProfilesForUsers(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<UserIdentity, Set<String>> profiledByUsers = new HashMap<>();
            
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
            
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                UserIdentity userIdentity = new UserIdentity(deniedProfile.get("login"), deniedProfile.get("population"));
                String profileId = deniedProfile.get("profileId");
                
                if (!profiledByUsers.containsKey(userIdentity))
                {
                    profiledByUsers.put(userIdentity, new HashSet<>());
                }
                profiledByUsers.get(userIdentity).add(profileId);
            }
            
            return profiledByUsers;
        }
    }
    
    @Override
    public Set<UserIdentity> getDeniedUsers(Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Set<UserIdentity> users = new HashSet<>();
            
            Map<String, Object> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", getObjectWithPrefix(object));
            }
            parameters.put("profileIds", Arrays.asList(profileId));
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
            
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                users.add(new UserIdentity(deniedProfile.get("login"), deniedProfile.get("population")));
            }
            
            return users;
        }
    }
    
    @Override
    public void addDeniedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getUserDeniedProfiles", parameters);
                
                if (deniedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addDeniedUser", parameters);
                }
                else
                {
                    getLogger().debug("Login {} is already denied for profile {} on context {}", userIdentity, profileId, prefixedObject);
                }
                
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                parameters.put("profileIds", Arrays.asList(profileId));
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteDeniedUser", parameters);
            }
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedUsers(Set<UserIdentity> users, Object object)
    {
        try (SqlSession session = getSession())
        {
            for (UserIdentity userIdentity : users)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("login", userIdentity.getLogin());
                parameters.put("population", userIdentity.getPopulationId());
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteDeniedUser", parameters);
            }
            session.commit();
        }
    }
    
    
    /* ----------------------------- */
    /* MANAGEMENT OF DENIED GROUPS */
    /* ----------------------------- */
    
    @Override
    public Map<GroupIdentity, Set<String>> getDeniedProfilesForGroups(Object object)
    {
        try (SqlSession session = getSession())
        {
            Map<GroupIdentity, Set<String>> profiledByGroups = new HashMap<>();
            
            Map<String, String> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", (String) getObjectWithPrefix(object));
            }
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getGroupDeniedProfiles", parameters);
            
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                GroupIdentity gpIdentity = new GroupIdentity(deniedProfile.get("groupId"), deniedProfile.get("groupDirectory"));
                String profileId = deniedProfile.get("profileId");
                
                if (!profiledByGroups.containsKey(gpIdentity))
                {
                    profiledByGroups.put(gpIdentity, new HashSet<>());
                }
                profiledByGroups.get(gpIdentity).add(profileId);
            }
            
            return profiledByGroups;
        }
    }
    
    @Override
    public Set<GroupIdentity> getDeniedGroups(Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Set<GroupIdentity> groups = new HashSet<>();
            
            Map<String, Object> parameters = new HashMap<>();
            if (object != null)
            {
                parameters.put("context", getObjectWithPrefix(object));
            }
            parameters.put("profileIds", Arrays.asList(profileId));
            
            List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getGroupDeniedProfiles", parameters);
            
            for (Map<String, String> deniedProfile : deniedProfiles)
            {
                groups.add(new GroupIdentity(deniedProfile.get("groupId"), deniedProfile.get("groupDirectory")));
            }
            
            return groups;
        }
    }
    
    @Override
    public void addDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            Object prefixedObject = getObjectWithPrefix(object);
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                parameters.put("context", prefixedObject);
                parameters.put("profileIds", Arrays.asList(profileId));
                
                List<Map<String, String>> deniedProfiles = session.selectList("ProfilesAssignment.getGroupDeniedProfiles", parameters);
                
                if (deniedProfiles.isEmpty())
                {
                    parameters.put("profileId", profileId);
                    session.insert("ProfilesAssignment.addDeniedGroup", parameters);
                }
                else
                {
                    getLogger().debug("Group {} is already denied for profile {} on context {}", group, profileId, prefixedObject);
                }
            }
            
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        try (SqlSession session = getSession())
        {
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                parameters.put("profileIds", Arrays.asList(profileId));
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteDeniedGroup", parameters);
            }
            session.commit();
        }
    }
    
    @Override
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object)
    {
        try (SqlSession session = getSession())
        {
            for (GroupIdentity group : groups)
            {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("groupId", group.getId());
                parameters.put("groupDirectory", group.getDirectoryId());
                if (object != null)
                {
                    parameters.put("context", getObjectWithPrefix(object));
                }
                
                session.delete("ProfilesAssignment.deleteDeniedGroup", parameters);
            }
            session.commit();
        }
    }
    
    
    /* ------ */
    /* REMOVE */
    /* ------ */
    
    @Override
    public void removeProfile(String profileId)
    {
        try (SqlSession session = getSession())
        {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("profileIds", Arrays.asList(profileId));
            
            session.delete("ProfilesAssignment.deleteAllowedUser", parameters);
            session.delete("ProfilesAssignment.deleteDeniedUser", parameters);
            session.delete("ProfilesAssignment.deleteAllowedGroup", parameters);
            session.delete("ProfilesAssignment.deleteDeniedGroup", parameters);
            session.delete("ProfilesAssignment.deleteAllowedAnonymous", parameters);
            session.delete("ProfilesAssignment.deleteDeniedAnonymous", parameters);
            session.delete("ProfilesAssignment.deleteAllowedAnyConnected", parameters);
            session.delete("ProfilesAssignment.deleteDeniedAnyConnected", parameters);
            
            session.commit();
        }
    }
    
    @Override
    public void removeUser(UserIdentity user)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("login", user.getLogin());
            parameters.put("population", user.getPopulationId());
            
            session.delete("ProfilesAssignment.deleteAllowedUser", parameters);
            session.delete("ProfilesAssignment.deleteDeniedUser", parameters);
            
            session.commit();
        }
    }
    
    @Override
    public void removeGroup(GroupIdentity group)
    {
        try (SqlSession session = getSession())
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("groupId", group.getId());
            parameters.put("groupDirectory", group.getDirectoryId());
            
            session.delete("ProfilesAssignment.deleteAllowedGroup", parameters);
            session.delete("ProfilesAssignment.deleteDeniedGroup", parameters);
            
            session.commit();
        }
    }
    
    /* ------------------------------ */
    /* SUPPORT OF OBJECT AND PRIORITY */
    /* ------------------------------ */

    @Override
    public boolean isSupported(Object object)
    {
        return object instanceof String && _supportedContext.equals(object);
    }

    @Override
    public int getPriority()
    {
        return ProfileAssignmentStorage.MIN_PRIORITY;
    }
}
