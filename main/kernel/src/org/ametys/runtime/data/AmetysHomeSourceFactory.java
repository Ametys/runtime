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
package org.ametys.runtime.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.impl.FileSource;

import org.ametys.runtime.servlet.RuntimeConfig;


/**
 * SourceFactory handling URIs in Ametys home.
 */
public class AmetysHomeSourceFactory extends AbstractLogEnabled implements SourceFactory
{
    private static final String __AMETYS_HOME_SOURCE_PREFIX = "ametys-home://";
    
    public Source getSource(String location, Map parameters) throws IOException
    {
        if (!StringUtils.startsWith(location, __AMETYS_HOME_SOURCE_PREFIX))
        {
            throw new MalformedURLException("URI must be like ametys-home://path/to/resource. Location was '" + location + "'");
        }
        
        File ametysHomeDir = RuntimeConfig.getInstance().getAmetysHome();
        String childLocation = StringUtils.removeStart(location, __AMETYS_HOME_SOURCE_PREFIX);
        return new FileSource("file", new File(ametysHomeDir, childLocation));
    }
    
    public void release(Source source)
    {
        // empty method
    }
}
