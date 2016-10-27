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
package org.ametys.runtime.log;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Wrapper for a log queue with an expiration date. 
 * When accessing a specific key, the expiration timer is checked.
 * When using operations accessing the whole queue, it is fully checked for expiration.
 * @param <T> The type of object managed by the queue
 */
public class ExpiringSortedSetQueue<T>
{
    private long _timeToLiveMillis;
    private Map<T, Long> _expirationDate;
    private SortedSet<T> _queue;
    
    /**
     * Default constructor for initializing a sorted set queue with an expiration timer
     * @param expiringTime The time limit, in milliseconds, that the logs are kept in the queue.
     * @param comparator The comparator used to sort the queue
     */
    public ExpiringSortedSetQueue(final long expiringTime, Comparator<T> comparator)
    {
        _timeToLiveMillis = expiringTime > 0 ? expiringTime : 0;
        _queue = Collections.synchronizedSortedSet(new TreeSet<>(comparator));
        _expirationDate = Collections.synchronizedMap(new HashMap<T, Long>());
    }
    
    /**
     * Put an object in the queue
     * @param object The object
     */
    public void put(T object)
    {
        put(object, now());
    }
    
    /**
     * Put an object in the queue, and specify the current time for the object.
     * @param object The object to queue
     * @param objectTime The time associated with the object
     */
    public void put(T object, long objectTime)
    {
        Long keepUntil = objectTime + _timeToLiveMillis;
        
        _queue.add(object);
        _expirationDate.put(object, keepUntil);
    }
    
    /**
     * Add a collection of objects to the queue
     * @param setToCopy The collection of objects
     */
    public void putAll(Set<? extends T> setToCopy)
    {
        for (T entry : setToCopy)
        {
            put(entry);
        }
    }
    
    /**
     * Clear the content of the queue
     */
    public void clear()
    {
        _queue.clear();
        _expirationDate.clear();
    }
    
    /**
     * Check if the queue contains the specified key
     * @param key The key
     * @return True if the key was found.
     */
    public boolean contains(final T key)
    {
        removeIfExpired(key, now());
        return _queue.contains(key);
    }
    
    /**
     * Returns a view of the portion of this set whose elements are greater than or equal to fromElement.
     * @param fromElement low endpoint (inclusive) of the returned set
     * @return a view of the portion of this set whose elements are greater than or equal to fromElement
     */
    public SortedSet<T> tailSet(T fromElement)
    {
        synchronized (_queue)
        {
            removeAllExpired(now());
            return new TreeSet<>(_queue.tailSet(fromElement));
        }
    }
    
    /**
     * Returns a view of the portion of this set whose elements are greater than or equal to fromElement.
     * @param toElement high endpoint (exclusive) of the returned set
     * @return a view of the portion of this set whose elements are strictly less than toElement
     */
    public SortedSet<T> headSet(T toElement)
    {
        synchronized (_queue)
        {
            removeAllExpired(now());
            return new TreeSet<>(_queue.headSet(toElement));
        }
    }
    
    /**
     * Returns a view of the portion of this set whose elements range from fromElement, inclusive, to toElement, exclusive.
     * @param fromElement low endpoint (inclusive) of the returned set
     * @param toElement high endpoint (exclusive) of the returned set
     * @return a view of the portion of this set whose elements range from fromElement, inclusive, to toElement, exclusive
     */
    public SortedSet<T> subSet(T fromElement, T toElement)
    {
        synchronized (_queue)
        {
            removeAllExpired(now());
            return new TreeSet<>(_queue.subSet(fromElement, toElement));
        }
    }
    

    private Long now()
    {
        return System.currentTimeMillis();
    }

    private void removeAllExpired(final long now)
    {
        final Iterator<Entry<T, Long>> iterator = _expirationDate.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<T, Long> next = iterator.next();
            if (isExpired(now, next.getValue()))
            {
                _queue.remove(next.getKey());
                iterator.remove();
            }
        }
    }
    
    private void removeIfExpired(final T key, final Long now)
    {
        Long expirationTime = _expirationDate.get(key);
        if (isExpired(now, expirationTime))
        {
            _queue.remove(key);
            _expirationDate.remove(key);
        }
    }
    
    private boolean isExpired(final long now, final Long expirationTime)
    {
        if (expirationTime != null)
        {
            final long expirationValue = expirationTime.longValue();
            return expirationValue >= 0 && now >= expirationValue;
        }
        return false;
    }
}
