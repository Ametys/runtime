/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.test.administrator.jvmstatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.RequestCountListener;

/**
 * Test {@link RequestCountListener} in a massive threaded context.
 */
public class RequestCountListenerTestCase extends TestCase
{
    /**
     * Create the test case.
     * @param name the test case name.
     */
    public RequestCountListenerTestCase(String name)
    {
        super(name);
    }
    
    /**
     * Test count in a massive threaded context.
     * @throws Exception if an error occurs.
     */
    public void testCount() throws Exception
    {
        final int threadCount = 10000;
        final RequestCountListener requestCountListener = new RequestCountListener();

        // Compteurs initiaux
        assertEquals(0, RequestCountListener.getCurrentRequestCount());
        assertEquals(0, RequestCountListener.getTotalRequestCount());
        
        // Tester 3 fois de suite
        for (int i = 0; i < 3; i++)
        {
            // Tester la création
            _testCount(requestCountListener, threadCount, true);
            assertEquals(threadCount, RequestCountListener.getCurrentRequestCount());
            assertEquals((i + 1) * threadCount, RequestCountListener.getTotalRequestCount());
            
            // Tester la desctruction
            _testCount(requestCountListener, threadCount, false);
            assertEquals(0, RequestCountListener.getCurrentRequestCount());
            assertEquals((i + 1) * threadCount, RequestCountListener.getTotalRequestCount());
        }
    }

    private void _testCount(final RequestCountListener requestCountListener, final int threadCount, final boolean create) throws Exception
    {
        final Random random = new Random(System.currentTimeMillis());
        List<Thread> threads = new ArrayList<Thread>();
        
        for (int i = 0; i < threadCount; i++)
        {
            threads.add(new Thread()
            {
                
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(Math.abs(random.nextInt()) % 1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    
                    if (create)
                    {
                        requestCountListener.requestInitialized(null);
                    }
                    else
                    {
                        requestCountListener.requestDestroyed(null);
                    }
                }
            });
        }

        // Lancer les threads
        for (Thread thread : threads)
        {
            thread.setDaemon(true);
            thread.start();
        }

        // Attendre la fin des threads
        for (Thread thread : threads)
        {
            thread.join();
        }
    }
}
