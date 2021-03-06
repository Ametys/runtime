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
package org.ametys.runtime.test.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.test.AbstractRuntimeTestCase;


/**
 * {@link TestCase} for formatting and parsing {@link Date}.
 */
public class DateConversionTestCase extends AbstractRuntimeTestCase
{
    /**
     * Create the test case.
     * @param name the test case name.
     */
    public DateConversionTestCase(String name)
    {
        super(name);
    }
    
    /**
     * Tests ISO8601 conversion.
     * @throws Exception if an error occurs.
     */
    public void testDateConversion() throws Exception
    {
        String dateValue = "2007-04-17T15:19:21.618+08:00";
        Date date = new DateTime(dateValue).toDate();
        
        // Date parsing
        Object dateConverted = ParameterHelper.castValue(dateValue, ParameterType.DATE);
        assertEquals(date, dateConverted);
        
        DateTimeZone defaultTZ = DateTimeZone.getDefault();
        // Use singapore time zone
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(8));
        // Date formatting
        assertEquals(dateValue, ParameterHelper.valueToString(date));
        DateTimeZone.setDefault(defaultTZ);

        // Date formatting using current time zone
        assertEquals(ISODateTimeFormat.dateTime().print(new DateTime(dateValue)), ParameterHelper.valueToString(date));
    }
    
    /**
     * Tests value to string conversion for a recent date
     * @throws Exception if an error occurs.
     */
    public void testRecentDate() throws Exception
    {
        String expected = ISODateTimeFormat.dateTime().print(new DateTime(2012, 3, 2, 18, 30, 22, 666));
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2012, 2, 2, 18, 30, 22);
        cal.set(Calendar.MILLISECOND, 666);
        Date date = cal.getTime();
        
        // Date parsing
        String actual = ParameterHelper.valueToString(date);
        assertEquals(expected, actual);
    }
    
    /**
     * Tests value to string conversion for date older than 1900
     * @throws Exception if an error occurs.
     */
    public void testOldDate() throws Exception
    {
        String expected = ISODateTimeFormat.dateTime().print(new DateTime(1899, 6, 18, 0, 0, 0, 0));
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1899, 5, 18, 0, 0, 0);
        Date date = cal.getTime();
        
        // Date parsing
        String actual = ParameterHelper.valueToString(date);
        assertEquals(expected, actual);
    }
}
