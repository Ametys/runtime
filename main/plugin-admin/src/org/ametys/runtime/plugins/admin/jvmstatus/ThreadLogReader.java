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
package org.ametys.runtime.plugins.admin.jvmstatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.xml.sax.SAXException;

/**
 * Send a report on thread 
 */
public class ThreadLogReader extends AbstractReader
{
    /** The servlet response */
    protected Response _response;
    
    @Override
    public String getMimeType()
    {
        return "text/plain";
    }
    
    @Override
    public void setup(SourceResolver pResolver, Map pObjectModel, String pSrc, Parameters pPar) throws ProcessingException, SAXException, IOException
    {
        super.setup(pResolver, pObjectModel, pSrc, pPar);
        _response = ObjectModelHelper.getResponse(pObjectModel);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator is download deadlocked threads'report");
        }
        
        _response.setHeader("Content-Disposition", "attachment");
        
        long[] deadlockedThreadsIds = ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
        if (deadlockedThreadsIds == null)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Report is empty");
            }
            out.write("No thread in deadlock.\n".getBytes("UTF-8"));
        }
        else
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Report contains " + deadlockedThreadsIds.length + " deadlocked threads");
            }
            out.write((deadlockedThreadsIds.length + " threads in deadlock.\n").getBytes("UTF-8"));
            
            Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
            Map<Long, StackTraceElement[]> stackTraces = new HashMap<>();
            for (Thread thread : threads.keySet())
            {
                long id = thread.getId();
                stackTraces.put(id, threads.get(thread));
            }
            
            ThreadInfo[] deadlockedThread = ManagementFactory.getThreadMXBean().getThreadInfo(deadlockedThreadsIds);
            for (ThreadInfo info : deadlockedThread)
            {
                out.write("\n".getBytes("UTF-8"));
                out.write(("THREAD '" + info.getThreadId() + "' - '" + info.getThreadName() + "'\n").getBytes("UTF-8"));
                out.write(("locked on monitor " + info.getLockName() + " by thread '" + info.getLockOwnerId() + "' - '" + info.getLockOwnerName() + "'\n").getBytes("UTF-8"));

                
                StackTraceElement[] stes = stackTraces.get(info.getThreadId()); // car info.getStackTrace(); renvoie vide
                for (StackTraceElement ste : stes)
                {
                    String line = "at " + ste.getClassName()  + "." + ste.getMethodName() + " (" + ste.getFileName() + ":" + ste.getLineNumber() + ")\n";
                    out.write(line.getBytes("UTF-8"));
                }
            }
        }
    }
}
