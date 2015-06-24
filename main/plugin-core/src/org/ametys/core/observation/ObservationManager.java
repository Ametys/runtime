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
package org.ametys.core.observation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for dispatching {@link Event} instances to {@link Observer}s.
 */
public class ObservationManager extends AbstractLogEnabled implements Component, Serviceable
{
    /** Avalon ROLE. */
    public static final String ROLE = ObservationManager.class.getName();
    
    private static final Logger __ALL_EVENTS_LOGGER = LoggerFactory.getLogger("org.ametys.runtime.observation.AllEvents");
    
    private ObserverExtensionPoint _observerExtPt;
    private Collection<Observer> _registeredObservers = new ArrayList<>();
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _observerExtPt  = (ObserverExtensionPoint) manager.lookup(ObserverExtensionPoint.ROLE);
    }
    
    /**
     * Notify of a event which will be dispatch to registered
     * observers.
     * @param event the event to notify.
     */
    public void notify(final Event event)
    {
        try
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Receiving " + event);
            }
            
            if (__ALL_EVENTS_LOGGER.isInfoEnabled())
            {
                __ALL_EVENTS_LOGGER.info("Receiving " + event);
            }
            
            List<Observer> supportedObservers = new ArrayList<>();
            
            // Retrieve supported observers
            for (String observerId : _observerExtPt.getExtensionsIds())
            {
                Observer observer = _observerExtPt.getExtension(observerId);
                
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Checking support for event: " + event + " and observer: " + observer);
                }
                
                if (observer.supports(event))
                {
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("Event: " + event + " supported for observer: " + observer);
                    }
                    
                    supportedObservers.add(observer);
                }
            }
            
            // Order observers (0 is first, Integer.MAX_INT is last)
            Collections.sort(supportedObservers, new Comparator<Observer>()
            {
                @Override
                public int compare(Observer o1, Observer o2)
                {
                    return new Integer(o1.getPriority(event)).compareTo(new Integer(o2.getPriority(event)));
                }
        
            });
            
            Map<String, Object> transientVars = new HashMap<>();
            for (Observer supportedObserver : supportedObservers)
            {
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Notifying observer: " + supportedObserver + " for event: " + event);
                }
             
                // Observe current event
                supportedObserver.observe(event, transientVars);
            }
        }
        catch (Exception e)
        {
            // Observers should never fail, so just log the error
            getLogger().error("Unable to dispatch event: " + event + " to observers", e);
        }
    }
    
    /**
     * Registers an {@link Observer}.
     * @param observer the {@link Observer}.
     */
    public void registerObserver(Observer observer)
    {
        _registeredObservers.add(observer);
    }
    
    /**
     * Unregisters an {@link Observer}.
     * @param observer the {@link Observer}.
     */
    public void unregisterObserver(Observer observer)
    {
        _registeredObservers.remove(observer);
    }
}
