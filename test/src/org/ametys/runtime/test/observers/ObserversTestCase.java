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
package org.ametys.runtime.test.observers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.observation.AsyncObserver;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.observation.Observer;
import org.ametys.core.observation.ObserverExtensionPoint;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the observers
 */
public class ObserversTestCase extends AbstractRuntimeTestCase
{
    /** Observation manager */
    protected ObservationManager _observationManager;
    
    /** Observation extension point */
    protected ObserverExtensionPoint _observerExtPt;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        _observerExtPt  = (ObserverExtensionPoint) Init.getPluginServiceManager().lookup(ObserverExtensionPoint.ROLE);
        _observationManager = (ObservationManager) Init.getPluginServiceManager().lookup(ObservationManager.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _observationManager = null;
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * General tests on events and observers
     */
    public void testObservers()
    {
        // Test observers registration
        // Take only test observers into account
        Set<String> testObserverIds = new HashSet<>();
        CollectionUtils.select(_observerExtPt.getExtensionsIds(), s -> StringUtils.startsWith((CharSequence) s, "org.ametys.runtime.test.observers"), testObserverIds);
        
        // number of registered observers
        assertEquals("Invalid number of registered observers", 66, testObserverIds.size());
        assertTrue("AcceptObserver is not registered", testObserverIds.contains(AcceptObserver.class.getName()));
        
        // Send a basic event
        Event event = new Event("basic", new UserIdentity("john", "pop"), new HashMap<String, Object>());
        _observationManager.notify(event);
        
        assertTrue("Event args should contains a success key with a true value", (boolean) event.getArguments().get("success"));
        
        // Test priority
        event = new Event("priority", new UserIdentity("john", "pop"), new HashMap<String, Object>());
        _observationManager.notify(event);
        
        assertTrue("Event args should contains a success key with a true value", (boolean) event.getArguments().get("success"));
    }
    
    /**
     * Test asynchronous observers
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void testAsyncObservers() throws InterruptedException
    {
        int asyncObsTotal = 3;
        
        // Use CountDownLatch to signal start and end of tasks between threads
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(asyncObsTotal);
        
        // Send a basic event
        Map<String, Object> params = new HashMap<>();
        params.put("startLatch", startLatch);
        params.put("doneLatch", doneLatch);
        
        AtomicInteger asyncCount = new AtomicInteger();
        params.put("asyncCount", asyncCount);
        
        Event event = new Event("async", new UserIdentity("john", "pop"), params);
        _observationManager.notify(event);
        
        // AcceptObserver should already have been observed, but not the async observers
        assertTrue("Event args should contains a success key with a true value", (boolean) params.get("success"));
        assertEquals("Asynchronous observers should not have been processed yet.", 0, asyncCount.get());
        
        // start signal
        startLatch.countDown();
        
        // wait for end of jobs (timeout 100ms)
        assertTrue("test async observers has timed out", doneLatch.await(100, TimeUnit.MILLISECONDS));
        assertEquals("Asynchronous observers should have been processed.", 3, asyncCount.get());
    }
    
    /**
     * Test non-parallelizable asynchronous observers
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws TimeoutException on barrier timeout exception
     * @throws BrokenBarrierException on barrier broken exception
     */
    public void testNonParallelizableAsyncObservers() throws InterruptedException, BrokenBarrierException, TimeoutException
    {
        // Use cycle barrier to be able to make assert between observers
        CyclicBarrier barrier = new CyclicBarrier(2); // 2 parties: the main test threads + one async observer
        
        // Send a basic event
        Map<String, Object> params = new HashMap<>();
        params.put("barrier", barrier);
        
        AtomicInteger asyncCount = new AtomicInteger();
        params.put("asyncCount", asyncCount);
        
        Event event = new Event("non-parallelizable-async", new UserIdentity("john", "pop"), params);
        _observationManager.notify(event);
        
        // AcceptObserver should already have been observed, but not the async observers
        assertTrue("Event args should contains a success key with a true value", (boolean) params.get("success"));
        assertEquals("Asynchronous observers should not have been processed yet.", 0, asyncCount.get());
        
        // wait for first observer
        barrier.await(100, TimeUnit.MILLISECONDS);
        
        // first observer start process
        // wait for end process of first observer
        barrier.await(100, TimeUnit.MILLISECONDS);
        assertEquals("High observer should have been processed", 1, asyncCount.get());
        
        // process similarly for mid and low observer
        barrier.await(100, TimeUnit.MILLISECONDS);
        barrier.await(100, TimeUnit.MILLISECONDS);
        assertEquals("Mid observer should have been processed", 2, asyncCount.get());
        
        barrier.await(100, TimeUnit.MILLISECONDS);
        barrier.await(100, TimeUnit.MILLISECONDS);
        assertEquals("Low observer should have been processed", 3, asyncCount.get());
        
        // barrier should still be valid (to ensure that synchronization used for the test was working as expected)
        assertFalse("barrier should still be valid", barrier.isBroken());
    }
    
    /**
     * Run a lot of event and observers at the same time
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws TimeoutException on barrier timeout exception
     * @throws BrokenBarrierException on barrier broken exception
     */
    public void testObserverPressure() throws InterruptedException, BrokenBarrierException, TimeoutException
    {
        // async total = 100 event * (20 async non-parallel + 30 async parallel) = 5000
        int asyncObsTotal = 5000;
        
        // done latch used to wait for termination of all async observers.
        CountDownLatch doneLatch = new CountDownLatch(asyncObsTotal);
        
        // populate params
        Map<String, Object> params = new HashMap<>();
        params.put("doneLatch", doneLatch);
        
        // async count
        AtomicInteger asyncCount = new AtomicInteger();
        params.put("asyncCount", asyncCount);
        
        for (int i = 0; i < 100; i++)
        {
            // per event sync count
            AtomicInteger syncCount = new AtomicInteger();
            params.put("syncCount", syncCount);
            
            Event event = new Event("pressure", new UserIdentity("john", "pop"), params);
            _observationManager.notify(event);
            
            assertTrue("Event args should contains a success key with a true value", (boolean) params.get("success"));
            assertEquals("All pressure synchronous observers should have been processed.", 6, syncCount.get());
        }
        
        // 2 minutes timeout (should be way more than necessary)
        assertTrue("observer pressure test has timed out before all async observers were processed", doneLatch.await(2, TimeUnit.MINUTES));
        assertEquals("All pressure asynchronous observers should have been processed now.", 5000, asyncCount.get());
    }
    
    /**
     * Abstract test observer
     */
    abstract static class AbstractTestObserver implements Observer
    {
        public boolean supports(Event event)
        {
            // support all by default
            return true;
        }

        public int getPriority(Event event)
        {
            return Integer.MAX_VALUE;
        }
        
        public void observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            assertTrue("Issuer must be john", new UserIdentity("john", "pop").equals(event.getIssuer()));
            assertNotNull("Transient vars cannot be null", transientVars);
            
            // visited test map
            _updateVisited(transientVars);
            
            // real observer job
            _observe(event, transientVars);
        }
        
        @SuppressWarnings("unchecked")
        /**
         * Get visited observers from transient vars
         * @param transientVars The transient vars 
         * @return The list of visited observers
         */
        public List<Class> getVisited(Map<String, Object> transientVars)
        {
            return (List<Class>) transientVars.get("visited");
        }
        
        public void _updateVisited(Map<String, Object> transientVars)
        {
            List<Class> visited = getVisited(transientVars);
            
            if (visited == null)
            {
                visited = new LinkedList<>();
                transientVars.put("visited", visited);
            }
            
            visited.add(this.getClass());
        }
        
        /**
         * Observe method to be implemented in each observer
         * @param event The event
         * @param transientVars transient vars
         * @throws Exception on error
         */
        public abstract void _observe(Event event, Map<String, Object> transientVars) throws Exception;
    }
    
    /**
     * A dummy observer that accepts every events
     */
    public static class AcceptObserver extends AbstractTestObserver
    {
        @Override
        public void _observe(Event event, Map<String, Object> transientVars)
        {
            Map<String, Object> arguments = event.getArguments();
            arguments.put("success", true);
            
            if (arguments.containsKey("syncCount"))
            {
                // increment sync count if needed
                ((AtomicInteger) arguments.get("syncCount")).incrementAndGet();
            }
        }
    }
    
    /**
     * A dummy observer that deny every events
     */
    public static class DenyObserver extends AbstractTestObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return false;
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars)
        {
            fail("Deny observer should never observe an event");
        }
    }
    
    /**
     * An observer with max priority
     */
    public static class HighPriorityObserver extends AbstractTestObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "priority".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 0; // max priority
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("High priority observer should be the first to be observed", HighPriorityObserver.class.equals(visited.get(0)));
        }
    }
    
    /**
     * An observer with a medium priority
     */
    public static class MidPriorityObserver extends AbstractTestObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "priority".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 1000;
        }

        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("Mid priority observer should be observed after High priority", MidPriorityObserver.class.equals(visited.get(1)));
        }
    }
    
    /**
     * An observer with a low priority
     */
    public static class LowPriorityObserver extends AbstractTestObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "priority".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 5000;
        }

        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("Low priority observer should be observed after High and Mid priority", LowPriorityObserver.class.equals(visited.get(2)));
        }
    }
    
    /**
     * A basic asynchronous observer
     */
    public static class BasicAsyncObserver extends AbstractTestObserver implements AsyncObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "async".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 0; // max priority to show that non-async observer will still be executed before.
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            Map<String, Object> arguments = event.getArguments();
            CountDownLatch startLatch = (CountDownLatch) arguments.get("startLatch");
            CountDownLatch doneLatch = (CountDownLatch) arguments.get("doneLatch");
            
            // wait for start signal
            assertTrue("Basic async observer has timed out", startLatch.await(100, TimeUnit.MILLISECONDS));
            
            // increment async count
            ((AtomicInteger) arguments.get("asyncCount")).incrementAndGet();
            
            // signal end of the process
            doneLatch.countDown();
        }
    }
    
    /**
     * A non-parallelizable async observer with max priority
     */
    public static class HighPriorityAsyncObserver extends AbstractTestObserver implements AsyncObserver
    {
        @Override
        public boolean parallelizable()
        {
            return false;
        }
        
        @Override
        public boolean supports(Event event)
        {
            return "non-parallelizable-async".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 0; // max priority
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("High priority observer should be the first to be observed (not taking AcceptObserver into account)", HighPriorityAsyncObserver.class.equals(visited.get(1)));
            
            Map<String, Object> arguments = event.getArguments();
            CyclicBarrier barrier = (CyclicBarrier) arguments.get("barrier");
            
            // wait for start of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
            
            // increment async count
            ((AtomicInteger) arguments.get("asyncCount")).incrementAndGet();
            
            // end of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * A non-parallelizable async observer with mid priority
     */
    public static class MidPriorityAsyncObserver extends AbstractTestObserver implements AsyncObserver
    {
        @Override
        public boolean parallelizable()
        {
            return false;
        }
        
        @Override
        public boolean supports(Event event)
        {
            return "non-parallelizable-async".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 1000;
        }

        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("Mid priority observer should be observed after High priority (not taking AcceptObserver into account)", MidPriorityAsyncObserver.class.equals(visited.get(2)));
            
            Map<String, Object> arguments = event.getArguments();
            CyclicBarrier barrier = (CyclicBarrier) arguments.get("barrier");
            
            // wait for start of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
            
            // increment async count
            ((AtomicInteger) arguments.get("asyncCount")).incrementAndGet();
            
            // end of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * A non-parallelizable async observer with mid priority
     */
    public static class LowPriorityAsyncObserver extends AbstractTestObserver implements AsyncObserver
    {
        @Override
        public boolean parallelizable()
        {
            return false;
        }
        
        @Override
        public boolean supports(Event event)
        {
            return "non-parallelizable-async".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 5000;
        }

        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            List<Class> visited = getVisited(transientVars);
            assertTrue("Low priority observer should be observed after High and Mid priority (not taking AcceptObserver into account)", LowPriorityAsyncObserver.class.equals(visited.get(3)));
            
            Map<String, Object> arguments = event.getArguments();
            CyclicBarrier barrier = (CyclicBarrier) arguments.get("barrier");
            
            // wait for start of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
            
            // increment async count
            ((AtomicInteger) arguments.get("asyncCount")).incrementAndGet();
            
            // end of processing
            barrier.await(100, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Synchronous observer used for the pressure test
     */
    public static class PressureSyncObserver extends AbstractTestObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "pressure".equals(event.getId());
        }
        
        @Override
        public int getPriority(Event event)
        {
            return 1000; // higher priority than accept observer 
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws InterruptedException
        {
            // 10ms sleep
            Thread.sleep(10);
            
            // increment sync count
            ((AtomicInteger) event.getArguments().get("syncCount")).incrementAndGet();
        }
    }
    
    /**
     * A parallelizable async observer used for the pressure test
     */
    public static class PressureParallelizableAsyncObserver extends AbstractTestObserver implements AsyncObserver
    {
        @Override
        public boolean supports(Event event)
        {
            return "pressure".equals(event.getId());
        }
        
        @Override
        public void _observe(Event event, Map<String, Object> transientVars) throws Exception
        {
            _sleep();
            
            // increment async count
            Map<String, Object> arguments = event.getArguments();
            ((AtomicInteger) arguments.get("asyncCount")).incrementAndGet();
            
            // signal that observer has been processed
            CountDownLatch doneLatch = (CountDownLatch) arguments.get("doneLatch");
            doneLatch.countDown();
        }
        
        /**
         * Sleep the observer thread for some time
         * @throws InterruptedException on thread interruption
         */
        protected void _sleep() throws InterruptedException
        {
            // 1ms sleep
            Thread.sleep(1);
        }
    }
    
    /**
     * A non-parallelizable async observer used for the pressure test
     */
    public static class PressureNonParallelizableAsyncObserver extends PressureParallelizableAsyncObserver
    {
        @Override
        public boolean parallelizable()
        {
            return false;
        }
        
        @Override
        protected void _sleep() throws InterruptedException
        {
            // 5ms sleep
            Thread.sleep(5);
        }
    }
}

