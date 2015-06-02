/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.logs;

import java.util.Comparator;

import org.apache.log4j.Logger;

/**
 * Comparator to compare two loggers by their name 
 */
public class LoggerComparator implements Comparator<Logger>
{
    public int compare(Logger o1, Logger o2)
    {
        String[] o1NameParts = o1.getName().split("\\.");
        String[] o2NameParts = o2.getName().split("\\.");
        int i = 0;
        
        for (; i < o1NameParts.length && i < o2NameParts.length; i++)
        {
            int compare = o1NameParts[i].compareTo(o2NameParts[i]);
            
            if (compare != 0)
            {
                return compare;
            }
            
            // Same category, continue
        }
        
        if (i == o1NameParts.length)
        {
            if (i != o2NameParts.length)
            {
                // o2 has a longer category
                return 1;
            }
        }
        else
        {
            // o1 has a longer category
            return -1;
        }
        
        return o1.getName().compareTo(o2.getName());
    }
}
