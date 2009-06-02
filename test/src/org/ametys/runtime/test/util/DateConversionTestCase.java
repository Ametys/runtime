package org.ametys.runtime.test.util;

import java.util.Date;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;


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
}
