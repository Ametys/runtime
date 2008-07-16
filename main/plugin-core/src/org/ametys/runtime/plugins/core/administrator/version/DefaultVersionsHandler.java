/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.administrator.version;

import java.io.IOException;
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

import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.MapHandler;


/**
 * Default implementation of a VersionHandler returning exactly two versions : that of the Runtime kernel and that of the running application.<br>
 * Applications may subclass this default implementation 
 */
public class DefaultVersionsHandler extends AbstractLogEnabled implements VersionsHandler, ThreadSafe
{
    private Version _runtimeVersion;
    private Version _applicationVersion;
    
    public final Collection<Version> getVersions()
    {
        ArrayList<Version> versions = new ArrayList<Version>();
        
        if (_applicationVersion == null)
        {
            _applicationVersion = _getApplicationVersion();
        }
        
        if (_runtimeVersion == null)
        {
            _runtimeVersion = _getVersionFromCLasspath("/org/ametys/runtime/version.xml", "Runtime");
        }
        
        versions.add(_applicationVersion);
        
        Collection<Version> additionalVersions = getAdditionalVersions();
        
        if (additionalVersions != null)
        {
            versions.addAll(additionalVersions);
        }
        
        versions.add(_runtimeVersion);

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
     * &lt;version><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;version><i>Version name</i>&lt;/version><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;date><i>Version date in format "yyyyMMdd'T'HHmmz"</i>&lt;/date><br>
     * &lt;/version><br>
     * @param path a path in the classpath poiting to an XML file
     * @param versionName the name of the Version to create
     * @return the created Version
     */
    protected final Version _getVersionFromCLasspath(String path, String versionName)
    {
        Map<String, String> config = new HashMap<String, String>();
        Date date = null;
        
        InputStream is = null;
        try
        {
            is = getClass().getResourceAsStream(path);
            SAXParserFactory.newInstance().newSAXParser().parse(is, new MapHandler(config));
            
            String strDate = config.get("date");
            date = new SimpleDateFormat("yyyyMMdd'T'HHmmz").parse(strDate);
        }
        catch (Exception ex)
        {
            getLogger().warn("Unable to load version values", ex);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    // empty
                }
            }
        }
        
        return new Version(versionName, config.get("version"), date);
    }
}
