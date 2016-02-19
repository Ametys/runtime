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

import org.apache.log4j.lf5.Log4JLogRecord;

/**
 *  Log record for the Memory Appender storage needs.
 */
public class MemoryLogRecord extends Log4JLogRecord
{
    private String _user;
    private String _requestURI;

    /**
     * Get the user associated with this LogRecord.
     * @return The user of this record.
     * @see #setUser(String)
     */
    public String getUser()
    {
      return (_user);
    }

    /**
     * Set the user associated with this LogRecord.
     * @param user The user.
     * @see #getUser()
     */
    public void setUser(String user)
    {
        _user = user;
    }
    
    /**
     * Get the request URI associated with this LogRecord.
     * @return The request URI of this record.
     * @see #setRequestURI(String)
     */
    public String getRequestURI()
    {
        return (_requestURI);
    }
    
    /**
     * Set the request URI associated with this LogRecord.
     * @param requestURI The request URI.
     * @see #getRequestURI()
     */
    public void setRequestURI(String requestURI)
    {
        _requestURI = requestURI;
    }
}
