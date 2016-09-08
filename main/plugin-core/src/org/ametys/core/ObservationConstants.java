/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.core;

/**
 * Interface for observers
 *
 */
public interface ObservationConstants
{
    /** Event id when a profile is delete. */
    public static final String EVENT_PROFILE_ADDED = "profile.added";
    
    /** Event id when a profile is delete. */
    public static final String EVENT_PROFILE_UPDATED = "profile.updated";
    
    /** Event id when a profile is delete. */
    public static final String EVENT_PROFILE_DELETED = "profile.deleted";
    
    /** Event id when a user is added. */
    public static final String EVENT_USER_ADDED = "user.added";
    
    /** Event id when a user is updated. */
    public static final String EVENT_USER_UPDATED = "user.updated";
    
    /** Event id when a user is deleted. */
    public static final String EVENT_USER_DELETED = "user.deleted";
    
    /** Event id when a group is added. */
    public static final String EVENT_GROUP_ADDED = "group.added";
    
    /** Event id when a group is updated. */
    public static final String EVENT_GROUP_UPDATED = "group.updated";
    
    /** Event id when a group is deleted. */
    public static final String EVENT_GROUP_DELETED = "group.deleted";
    
    /** Argument name for processed profile */
    public static final String ARGS_PROFILE = "profile";
    
    /** Argument name for processed user */
    public static final String ARGS_USER = "user";
    
    /** Argument name for processed group */
    public static final String ARGS_GROUP = "group";
}