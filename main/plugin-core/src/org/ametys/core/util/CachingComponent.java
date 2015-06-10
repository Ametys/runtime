/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Simple memory cache with a thread clearing the cache every day.
 * @param <T> the type of objects cached by this component.
 */
public class CachingComponent<T> extends AbstractLogEnabled implements Initializable
{
    private Map<String, T> _objects = new HashMap<>();
    
    public void initialize() throws Exception
    {
        if (isCacheEnabled())
        {
            Timer timer = new Timer("CachingComponent", true);
            
            long period = 1000 * 60 * 60 * 24; // one day
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    clearCache();
                }
            }, period, period);
        }
    }
    
    /**
     * Returns an object from the cache, correspondong to the specified key, or null if none.
     * @param key the object's key.
     * @return the object from cache.
     */
    protected T getObjectFromCache(String key)
    {
        T object = _objects.get(key);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Getting object" + object + "from cache for key " + key);
        }
        
        return object;
    }
    
    /**
     * Adds a key/object pair in the cache.
     * @param key the object's key.
     * @param object the object to be cached.
     */
    protected void addObjectInCache(String key, T object)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding object " + object + "in cache for key " + key);
        }
        
        _objects.put(key, object);
    }
    
    /**
     * Removes a key/object pair in the cache.
     * @param key the object's key.
     */
    protected void removeObjectFromCache(String key)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Removing object in cache for key " + key);
        }
        
        _objects.remove(key);
    }
    
    /**
     * Removes all entries from the cache.
     */
    protected void clearCache()
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Clearing cache");
        }
        
        _objects.clear();
    }
    
    /**
     * Returns true if the cache is enabled.
     * @return true if the cache is enabled.
     */
    protected boolean isCacheEnabled()
    {
        return true;
    }
}
