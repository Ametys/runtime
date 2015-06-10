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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring;

/**
 * Monitoring constants.
 */
public interface MonitoringConstants
{
    /** Webapp path for storing RRD files. */
    public static final String RRD_STORAGE_PATH = "/WEB-INF/data/monitoring";
    
    /** Extension of RRD files. */
    public static final String RRD_EXT = ".rrd";
    
    /** Data are fed each minute. */
    public static final int FEEDING_PERIOD = 60;
    
    /** Period to archive and to render. */
    public enum Period
    {
        /** Last hour. */
        LAST_HOUR
        {
            @Override
            public String toString()
            {
                return "hour";
            }
        },
        /** Last 24 hours. */
        LAST_DAY
        {
            @Override
            public String toString()
            {
                return "day";
            }
        },
        /** Last week. */
        LAST_WEEK
        {
            @Override
            public String toString()
            {
                return "week";
            }
        },
        /** Last month. */
        LAST_MONTH
        {
            @Override
            public String toString()
            {
                return "month";
            }
        },
        /** Last year. */
        LAST_YEAR
        {
            @Override
            public String toString()
            {
                return "year";
            }
        };
        
        /**
         * Returns the time in seconds for current period.
         * @return the time.
         */
        public long getTime()
        {
            int days = 1;
            int hours = 1;
            
            if (this == LAST_YEAR)
            {
                days = 365;
                hours = 24;
            }
            else if (this == LAST_MONTH)
            {
                days = 31;
                hours = 24;
            }
            else if (this == LAST_WEEK)
            {
                days = 7;
                hours = 24;
            }
            else if (this == LAST_DAY)
            {
                hours = 24;
            }
            
            return 60 * 60 * hours * days;
        }

    }
}
