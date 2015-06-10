/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.core.cocoon;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.serialization.TextSerializer;

/**
 * Serializer CSV
 */
public class CSVSerializer extends TextSerializer
{
    @Override
    public void configure(Configuration conf) throws ConfigurationException 
    {
        super.configure(conf);
        format.put(OutputKeys.ENCODING, "UTF-8");
        this.format.put(OutputKeys.METHOD, "text");
    }
    
    @Override
    public String getMimeType()
    {
        return "text/csv";
    }
    
    @Override
    public void setOutputStream(OutputStream out) throws IOException
    {
        byte[] enc = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}; 
        out.write(enc);
        super.setOutputStream(out);
    }
}
