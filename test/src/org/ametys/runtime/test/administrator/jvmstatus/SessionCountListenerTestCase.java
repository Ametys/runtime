/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.test.administrator.jvmstatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.ametys.runtime.plugins.admin.jvmstatus.SessionCountListener;

/**
 * Test {@link SessionCountListener} in a massive threaded context.
 */
public class SessionCountListenerTestCase extends TestCase
{
    /**
     * Create the test case.
     * @param name the test case name.
     */
    public SessionCountListenerTestCase(String name)
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
        final SessionCountListener sessionCountListener = new SessionCountListener();

        // Compteur initial
        assertEquals(0, SessionCountListener.getSessionCount());

        // Vérifier que le compteur ne passe pas dans les négatifs
        sessionCountListener.sessionDestroyed(null);
        assertEquals(0, SessionCountListener.getSessionCount());
        
        // Tester 3 fois de suite
        for (int i = 0; i < 3; i++)
        {
            // Tester la création
            _testCount(sessionCountListener, threadCount, true);
            assertEquals(threadCount, SessionCountListener.getSessionCount());
            
            // Tester la desctruction
            _testCount(sessionCountListener, threadCount, false);
            assertEquals(0, SessionCountListener.getSessionCount());
        }
    }

    private void _testCount(final SessionCountListener sessionCountListener, final int threadCount, final boolean create) throws Exception
    {
        final Random random = new Random(System.currentTimeMillis());
        List<Thread> threads = new ArrayList<>();
        
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
                        sessionCountListener.sessionCreated(null);
                    }
                    else
                    {
                        sessionCountListener.sessionDestroyed(null);
                    }
                }
            });
        }

        // Lancer les threads
        for (Thread thread : threads)
        {
            thread.start();
        }

        // Attendre la fin des threads
        for (Thread thread : threads)
        {
            thread.join();
        }
    }
}
