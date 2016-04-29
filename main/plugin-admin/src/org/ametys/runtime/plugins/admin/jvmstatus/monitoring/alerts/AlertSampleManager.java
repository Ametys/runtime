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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts;

import java.util.Map;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;

/**
 * Interface to be implemented for monitoring samples of data able to send system alerts when reaching threshold.
 */
public interface AlertSampleManager extends SampleManager
{
    /**
     * Provides the threshold for each datasource.
     * @return the threshold for each datasource. The key of the map is the datasource name for
     * which you want to create an alert.
     */
    public Map<String, Threshold> getThresholdValues();
    
    /**
     * Represents a threshold
     */
    public class Threshold
    {
        /** The possible types of operators */
        public static enum Operator
        {
            /** Alert will be triggered when the value is less or equal to the threshold */
            LEQ,
            /** Alert will be triggered when the value is greater or equal to the threshold */
            GEQ,
        }

        private Operator _operator;
        private String _dsName;
        private Object _value;
        private I18nizableText _mailSubject;
        private I18nizableText _mailBody;
        
        /**
         * Creates a threshold.
         * @param operator The kind of operator
         * @param datasourceName The id of the datasource
         * @param value The value of the threshold. Can be null to disable the alert.
         * @param mailSubject The subject of the potential mail to send.
         * @param mailBody The body of the potential mail to send.
         */
        public Threshold(Operator operator, String datasourceName, Object value, I18nizableText mailSubject, I18nizableText mailBody)
        {
            _operator = operator;
            _dsName = datasourceName;
            _value = value;
            _mailSubject = mailSubject;
            _mailBody = mailBody;
        }
        
        /**
         * Gets the datasource name of this threshold.
         * @return the datasource name.
         */
        public String getDatasourceName()
        {
            return _dsName;
        }
        
        /**
         * Gets the value of the threshold.
         * @return the value of the threshold.
         */
        public Object getValue()
        {
            return _value;
        }
        
        /**
         * Gets the subject of the mail to send.
         * @return the subject of the mail to send.
         */
        public I18nizableText getMailSubject()
        {
            return _mailSubject;
        }
        
        /**
         * Gets the body of the mail to send.
         * @return the body of the mail to send.
         */
        public I18nizableText getMailBody()
        {
            return _mailBody;
        }
        
        /**
         * Tests if the given value exceeds the threshold.
         * @param comparedTo The value to test against the threshold.
         * @return true if it exceeded, false otherwise.
         */
        public boolean isExceeded(Object comparedTo)
        {
            if (_value == null)
            {
                return false;
            }
            
            // integer values
            if (_value instanceof Long)
            {
                Long longComparedTo;
                if (comparedTo instanceof Long)
                {
                    longComparedTo = (Long) comparedTo;
                }
                else if (comparedTo instanceof Integer)
                {
                    longComparedTo = new Long((Integer) comparedTo);
                }
                else
                {
                    return false; // The provided object cannot be cast => log ?
                }
                
                if (_operator.equals(Operator.GEQ))
                {
                    return longComparedTo >= ((Long) _value);
                }
                else if (_operator.equals(Operator.LEQ))
                {
                    return longComparedTo <= ((Long) _value);
                }
            }
            
            // floating values
            if (_value instanceof Double)
            {
                Double doubleComparedTo;
                if (comparedTo instanceof Double)
                {
                    doubleComparedTo = (Double) comparedTo;
                }
                else if (comparedTo instanceof Float)
                {
                    doubleComparedTo = new Double((Float) comparedTo);
                }
                else
                {
                    return false;  // The provided object cannot be cast => log ?
                }
                
                if (_operator.equals(Operator.GEQ))
                {
                    return doubleComparedTo >= ((Double) _value);
                }
                else if (_operator.equals(Operator.LEQ))
                {
                    return doubleComparedTo <= ((Double) _value);
                }
            }
            
            return false;
        }
    }
}
