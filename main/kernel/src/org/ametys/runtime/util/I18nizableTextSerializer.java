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
package org.ametys.runtime.util;

import java.io.IOException;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * Serializer for {@link I18nizableText} objects.
 * Returns the translated message for JSON value.
 */
public class I18nizableTextSerializer extends SerializerBase<I18nizableText> implements Component, Serviceable
{
    /** The Avalon Role */
    public static final String ROLE = I18nizableTextSerializer.class.getName();
    
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    /**
     * Constructor
     */
    public I18nizableTextSerializer() { super(I18nizableText.class); }
    
    @Override
    public void serialize(I18nizableText value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
    {
        if (value.isI18n())
        {
            jgen.writeString(_i18nUtils.translate(value));
        }
        else
        {
            jgen.writeString(value.getLabel());
        }
    }
}
