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
package org.ametys.plugins.core.impl.version;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.core.version.Version;
import org.ametys.core.version.VersionsHandler;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.MapHandler;


/**
 * Default implementation of a VersionHandler returning exactly two versions : that of the Runtime kernel and that of the running application.<br>
 * Applications may subclass this default implementation 
 */
public class DefaultVersionsHandler extends AbstractLogEnabled implements VersionsHandler, ThreadSafe
{
    private Version _ametysVersion;
    private Version _applicationVersion;
    
    @Override
    public final Collection<Version> getVersions()
    {
        ArrayList<Version> versions = new ArrayList<>();
        
        if (_applicationVersion == null)
        {
            _applicationVersion = _getApplicationVersion();
        }
        
        if (_ametysVersion == null)
        {
            _ametysVersion = _getVersionFromClasspath("/org/ametys/runtime/kernel/version.xml", "Ametys");
        }
        
        versions.add(_applicationVersion);
        
        Collection<Version> additionalVersions = getAdditionalVersions();
        
        if (additionalVersions != null)
        {
            versions.addAll(additionalVersions);
        }
        
        versions.add(_ametysVersion);

        return versions;
    }
    
    /**
     * Returns any additional versions informations displayable in the administrator area.<br>
     * <i>Note: This implementation returns null.</i>
     * @return any additional versions informations displayable in the administrator area.<br>
     */
    protected Collection<Version> getAdditionalVersions()
    {
        return null;
    }
    
    private Version _getApplicationVersion()
    {
        RuntimeConfig config = RuntimeConfig.getInstance();
        return new Version("Application", config.getApplicationVersion(), config.getApplicationBuildDate());
    }
    
    /**
     * Helper for getting Version from an XML file in the classpath.<br>
     * The file must be formed like this :<br>
     * &lt;version&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;<i>Version name</i>&lt;/version&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;date&gt;<i>Version date in format "yyyyMMdd'T'HHmmz"</i>&lt;/date&gt;<br>
     * &lt;/version&gt;<br>
     * @param path a path in the classpath poiting to an XML file
     * @param versionName the name of the Version to create
     * @return the created Version
     */
    protected final Version _getVersionFromClasspath(String path, String versionName)
    {
        Map<String, String> config = new HashMap<>();
        Date date = null;
        
        try (InputStream is = getClass().getResourceAsStream(path))
        {
            if (is == null)
            {
                getLogger().warn(versionName + " version is unavailable");
                return new Version(versionName, null, null);
            }
            
            SAXParserFactory.newInstance().newSAXParser().parse(is, new MapHandler(config));
            
            String strDate = config.get("date");
            date = new SimpleDateFormat("yyyyMMdd'T'HHmmz").parse(strDate);
        }
        catch (Exception ex)
        {
            getLogger().warn("Unable to get version number for " + versionName, ex);
        }
        
        return new Version(versionName, config.get("version"), date);
    }
}
