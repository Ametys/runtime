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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.core.engine.BackgroundEngineHelper;

/**
 * Manager for dispatching {@link Event} instances to {@link Observer}s.
 */
public class ObservationManager extends AbstractLogEnabled implements Component, Serviceable, Contextualizable, Disposable
{
    /** Avalon ROLE. */
    public static final String ROLE = ObservationManager.class.getName();
    
    private static final Logger __ALL_EVENTS_LOGGER = LoggerFactory.getLogger("org.ametys.cms.observation.AllEvents");
    
    /**
     * The executor service managing the single thread pool. This threads is
     * used to run non-parallelizable observers.
     */
    private static ExecutorService __SINGLE_THREAD_EXECUTOR;
    
    /**
     * The executor service managing the thread pool of asynchronous observers
     * allowed to run in parallel.
     */
    private static ExecutorService __PARALLEL_THREAD_EXECUTOR;
    
    /** Cocoon context */
    protected Context _context;
    
    /** Service manager */
    protected ServiceManager _manager;
    
    private ObserverExtensionPoint _observerExtPt;
    private Collection<Observer> _registeredObservers = new ArrayList<>();
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _observerExtPt  = (ObserverExtensionPoint) manager.lookup(ObserverExtensionPoint.ROLE);
    }
    
    @Override
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException
    {
        _context = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
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
            
            // Observes the event and prepares the asynchronous observes.
            _observesEvent(event, supportedObservers);
        }
        catch (Exception e)
        {
            // Observers should never fail, so just log the error
            getLogger().error("Unable to dispatch event: " + event + " to observers", e);
        }
    }
    
    /**
     * Observes the event
     * @param event The event
     * @param supportedObservers list of observers
     * @throws Exception on error
     */
    protected void _observesEvent(final Event event, List<Observer> supportedObservers) throws Exception
    {
        List<AsyncObserver> parallelizableAsyncObservers = new ArrayList<>();
        List<AsyncObserver> nonParallelizableAsyncObservers = new ArrayList<>();
        
        Map<String, Object> transientVars = new HashMap<>();
        for (Observer supportedObserver : supportedObservers)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Notifying observer: " + supportedObserver + " for event: " + event);
            }
            
            // Observe current event
            if (supportedObserver instanceof AsyncObserver)
            {
                AsyncObserver asyncObserver = (AsyncObserver) supportedObserver;
                boolean parallelizable = asyncObserver.parallelizable();
                
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug(String.format("Adding %s asynchronous observer: '%s' for event '%s' to the async queue.",
                            parallelizable ? "parallelizable" : "non-parallelizable", asyncObserver, event));
                }
                if (__ALL_EVENTS_LOGGER.isDebugEnabled())
                {
                    __ALL_EVENTS_LOGGER.debug(String.format("Adding %s asynchronous observer: '%s' for event '%s' to the async queue.",
                            parallelizable ? "parallelizable" : "non-parallelizable", asyncObserver, event));
                }
                
                if (parallelizable)
                {
                    parallelizableAsyncObservers.add(asyncObserver);
                }
                else
                {
                    nonParallelizableAsyncObservers.add(asyncObserver);
                }
            }
            else
            {
                supportedObserver.observe(event, transientVars);
            }
        }
        
        // Observe for async observer.
        if (!parallelizableAsyncObservers.isEmpty() || !nonParallelizableAsyncObservers.isEmpty())
        {
            _asyncObserve(parallelizableAsyncObservers, nonParallelizableAsyncObservers, event, transientVars);
        }
    }

    /**
     * Async observe through a thread pool
     * @param parallelObservers parallelizable asynchronous observers
     * @param nonParallelObservers non parallelizable asynchronous observers
     * @param event The event to observe
     * @param transientVars The observer transient vars
     */
    private void _asyncObserve(Collection<AsyncObserver> parallelObservers, List<AsyncObserver> nonParallelObservers, Event event, Map<String, Object> transientVars)
    {
        if (__SINGLE_THREAD_EXECUTOR == null)
        {
            AsyncObserveThreadFactory threadFactory = new AsyncObserveThreadFactory();
            
            __SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor(threadFactory);
            
            // 10 threads per core
            __PARALLEL_THREAD_EXECUTOR = new ThreadPoolExecutor(0, 10 * Runtime.getRuntime().availableProcessors(), 60L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), threadFactory);
        }
        
        if (!parallelObservers.isEmpty())
        {
            for (AsyncObserver observer :  parallelObservers)
            {
                __PARALLEL_THREAD_EXECUTOR.execute(new ParallelAsyncObserve(observer, event, transientVars, getLogger(), __ALL_EVENTS_LOGGER));
            }
        }
        
        if (!nonParallelObservers.isEmpty())
        {
            __SINGLE_THREAD_EXECUTOR.execute(new NonParallelAsyncObserve(nonParallelObservers, event, transientVars, getLogger(), __ALL_EVENTS_LOGGER));
        }
    }
    
    /**
     * Runnable to be used for asynchronous calls 
     */
    abstract class AbstractAsyncObserve implements Runnable
    {
        /** event to observe */
        protected final Event _event;
        protected final Map<String, Object> _transientVars;
        protected final org.apache.avalon.framework.logger.Logger _logger;
        protected final Logger _allEventLogger;
        
        AbstractAsyncObserve(Event event, Map<String, Object> transientVars, org.apache.avalon.framework.logger.Logger logger, Logger allEventLogger)
        {
            _event = event;
            _transientVars = transientVars;
            _logger = logger;
            _allEventLogger = allEventLogger;
        }
        
        public void run()
        {
            Map<String, Object> environmentInformation = null;
            try
            {
                // Create the environment.
                environmentInformation = BackgroundEngineHelper.createAndEnterEngineEnvironment(_manager, _context, _logger);
                
                _observe();
            }
            catch (Exception e)
            {
                // Observer must never fail, so just log the error
                _logger.error("Unable to dispatch event: " + _event + " to asynchronous observers", e);
            }
            finally
            {
                // FIXME close jcr-sessions? (as in JCRSessionDispatchRequestProcess)
                
                // Leave the environment.
                if (environmentInformation != null)
                {
                    BackgroundEngineHelper.leaveEngineEnvironment(environmentInformation);
                }
            }
        }
        
        /**
         * Abstract observe method where the observation should be done
         * @throws Exception on error
         */
        protected abstract void _observe() throws Exception;
    }
    
    /**
     * Runnable for parallel observers 
     */
    class ParallelAsyncObserve extends AbstractAsyncObserve
    {
        private final AsyncObserver _observer;
        
        ParallelAsyncObserve(AsyncObserver observer, Event event, Map<String, Object> transientVars, org.apache.avalon.framework.logger.Logger logger, Logger allEventLogger)
        {
            super(event, transientVars, logger, allEventLogger);
            _observer = observer;
        }
        
        @Override
        protected void _observe() throws Exception
        {
            if (_logger.isDebugEnabled())
            {
                _logger.debug("Observing the asynchronous observer: " + _observer + " for event: " + _event + ".");
            }
            if (_allEventLogger.isDebugEnabled())
            {
                _allEventLogger.debug("Observing the asynchronous observer: " + _observer + " for event: " + _event + ".");
            }
            
            _observer.observe(_event, _transientVars);
        }
    }
    
    /**
     * Runnable for non parallel observers 
     */
    class NonParallelAsyncObserve extends AbstractAsyncObserve
    {
        private final Collection<AsyncObserver> _observers;
        
        NonParallelAsyncObserve(Collection<AsyncObserver> observers, Event event, Map<String, Object> transientVars, org.apache.avalon.framework.logger.Logger logger, Logger allEventLogger)
        {
            super(event, transientVars, logger, allEventLogger);
            _observers = observers;
        }
        
        @Override
        protected void _observe() throws Exception
        {
            for (AsyncObserver observer : _observers)
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("Observing the asynchronous observer: " + observer + " for event: " + _event + ".");
                }
                if (_allEventLogger.isDebugEnabled())
                {
                    _allEventLogger.debug("Observing the asynchronous observer: " + observer + " for event: " + _event + ".");
                }
                
                observer.observe(_event, _transientVars);
            }
        }
    }
    
    /**
     * Thread factory for async observers.
     * Set the thread name format and marks the thread as daemon. 
     */
    static class AsyncObserveThreadFactory implements ThreadFactory
    {
        private static ThreadFactory _defaultThreadFactory;
        private static String _nameFormat;
        private static AtomicLong _count;
        
        public AsyncObserveThreadFactory()
        {
            _defaultThreadFactory = Executors.defaultThreadFactory();
            _nameFormat = "ametys-async-observe-%d";
            _count = new AtomicLong(0);
        }
        
        public Thread newThread(Runnable r)
        {
            Thread thread = _defaultThreadFactory.newThread(r);
            thread.setName(String.format(_nameFormat, _count.getAndIncrement()));
            thread.setDaemon(true);
            
            return thread;
        }
    }
    
    @Override
    public void dispose()
    {
        if (__SINGLE_THREAD_EXECUTOR != null)
        {
            __SINGLE_THREAD_EXECUTOR.shutdownNow();
            __SINGLE_THREAD_EXECUTOR = null;
        }
        if (__PARALLEL_THREAD_EXECUTOR != null)
        {
            __PARALLEL_THREAD_EXECUTOR.shutdownNow();
            __PARALLEL_THREAD_EXECUTOR = null;
        }
        
        _context = null;
        _manager = null;
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
