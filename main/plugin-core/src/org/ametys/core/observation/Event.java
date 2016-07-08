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

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.ametys.core.user.UserIdentity;

/**
 * Abstraction of an event.
 */
public class Event
{
    private UserIdentity _issuer;
    private String _id;
    private Map<String, Object> _args;

    /**
     * Creates an event.
     * @param eventId the event identifier.
     * @param issuer the issuer responsible of this event.
     * @param args the event arguments.
     */
    public Event(String eventId, UserIdentity issuer, Map<String, Object> args)
    {
        _issuer = issuer;
        _id = eventId;
        _args = args;
    }
    
    /**
     * Retrieves the issuer responsible of this event.
     * @return the issuer.
     */
    public UserIdentity getIssuer()
    {
        return _issuer;
    }
    
    /**
     * Retrieves the event identifier.
     * @return the event identifier.
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the arguments.
     * @return the arguments.
     */
    public Map<String, Object> getArguments()
    {
        return _args;
    }
    
    @Override
    public String toString()
    {
        StringBuilder event = new StringBuilder();
        
        event.append("event[id: ");
        event.append(_id);
        event.append(", issuer: ");
        event.append(_issuer);
        event.append(", args: [");
        
        Iterator<String> it = _args.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            
            event.append(key);
            event.append(" = ");
            event.append(Objects.toString(_args.get(key)));
            
            if (it.hasNext())
            {
                event.append(", ");
            }
        }
        
        event.append("]]");
        
        return event.toString();
    }
}
