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
package org.ametys.plugins.core.ui.about;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;

import org.ametys.core.ui.Callable;
import org.ametys.core.util.I18nUtils;
import org.ametys.core.version.Version;
import org.ametys.core.version.VersionsHandler;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Helper providing information (versions, licenses) on running application for "About Ametys" feature.
 */
public class AboutInfoProvider extends AbstractLogEnabled implements Serviceable, Component, Contextualizable
{
    /** The Avalon role */
    public static final String ROLE = AboutInfoProvider.class.getName();
    
    /** The name of the license text file */
    private static final String NOTICE_FILE_PATH = "/NOTICE.txt";
    /** The path of the Application logo */
    private static final String LOGO_FILE_PATH = "/app_logo.png";
    
    /** The versions handler */
    private VersionsHandler _versionsHandler;
    /** The cocoon context */
    private org.apache.cocoon.environment.Context _cocoonContext;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _versionsHandler = (VersionsHandler) manager.lookup(VersionsHandler.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    /**
     * Gets the information for "About Ametys" feature
     * @param lang the lang
     * @return A map of information needed for "About Ametys" feature.
     * Contains the application name, the versions of the application and the license text.
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Callable
    public Map<String, Object> getInfo(String lang) throws FileNotFoundException, IOException
    {
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("applicationLogo", getApplicationLogo());
        result.put("applicationName", getApplicationName(lang));
        result.put("versions", getVersions());
        result.put("licenseText", getLicenseText());
        
        return result;
    }
    
    /**
     * Get the path of the application logo
     * @return The path to the Ametys application logo.
     */
    @Callable
    public String getApplicationLogo ()
    {
        String path = _cocoonContext.getRealPath(LOGO_FILE_PATH);
        File imgFile = new File(path);
        
        if (imgFile.exists() && imgFile.isFile())
        {
            return _cocoonContext.getRealPath(LOGO_FILE_PATH);
        }
        return null;
    }
    
    /**
     * Gets the application name
     * @param lang The lang
     * @return The name of the application.
     */
    @Callable
    public String getApplicationName(String lang)
    {
        return I18nUtils.getInstance().translate(new I18nizableText("application", "APPLICATION_PRODUCT_LABEL"), lang);
    }
    
    /**
     * Gets the available versions of the application.
     * @return The list of versions
     */
    @Callable
    public List<Map<String, Object>> getVersions()
    {
        List<Map<String, Object>> versions = new ArrayList<>();
        
        Iterator<Version> it = _versionsHandler.getVersions().iterator();
        while (it.hasNext())
        {
            Version version = it.next();

            Map<String, Object> versionInfos = new LinkedHashMap<>();
            versionInfos.put("name", version.getName());
            versionInfos.put("version", version.getVersion());
            versionInfos.put("date", version.getDate() != null ? ParameterHelper.valueToString(version.getDate()) : null);
            
            versions.add(versionInfos);
        }
        
        return versions;
    }
    
    /**
     * Gets the content of the license text.
     * @return The content of the license text.
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Callable
    public String getLicenseText() throws FileNotFoundException, IOException
    {
        String path = _cocoonContext.getRealPath(NOTICE_FILE_PATH);
        File licenseFile = new File(path);
        
        if (licenseFile.exists() && licenseFile.isFile())
        {
            StringBuffer sb = new StringBuffer();
            try ( BufferedReader reader = new BufferedReader(new FileReader(licenseFile)) )
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                    sb.append("<br/>");
                }
            }
            
            return sb.toString();
        }
        else
        {
            getLogger().warn("License file {} is not present at {}", NOTICE_FILE_PATH, path);
            return "";
        }
    }

}
