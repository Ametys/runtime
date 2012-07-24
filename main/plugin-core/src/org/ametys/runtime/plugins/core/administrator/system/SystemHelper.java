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
package org.ametys.runtime.plugins.core.administrator.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

/**
 * Helper class for accessing the the System parameters defined in the 'admin' section (system announcements, ...)
 */
public final class SystemHelper
{
    /** The relative path to the file where system information are saved (announcement, maintenance...) */
    public static final String ADMINISTRATOR_SYSTEM_FILE = "WEB-INF/data/administrator/system.xml";

    private SystemHelper()
    {
        // empty constructor
    }
    
    /**
     * Tests if system announcements are active.
     * @param contextPath the webapp context path
     * @return true if system announcements are active.
     */
    public static boolean isSystemAnnouncementAvailable(String contextPath)
    {
        InputStream is = null;
        
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return false;
            }
            
            is = new FileInputStream(systemFile);
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String state = xpath.evaluate("/announcements/@state", document);
            
            return "on".equals(state);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get system announcements", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }
    
    /**
     * Return the date of the last modification of the annonce
     * @param contextPath the webapp context path
     * @return The date of the last modification or 0 if there is no announce file
     */
    public static long getSystemAnnoucementLastModificationDate(String contextPath)
    {
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return 0;
            }
            
            return systemFile.lastModified();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get system announcements", e);
        }
    }

    /**
     * Returns the system announcement for the given language code, or for the default language code if there is no specified announcement for the given language code.<br>
     * Returns null if the system announcements are not activated.
     * @param languageCode the desired language code of the system announcement
     * @param contextPath the webapp context path
     * @return the system announcement in the specified language code, or in the default language code, or null if announcements are not active.
     */
    public static String getSystemAnnouncement(String languageCode, String contextPath)
    {
        InputStream is = null;
        
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return null;
            }
            
            is = new FileInputStream(systemFile);
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String state = xpath.evaluate("/announcements/@state", document);
            
            if (!"on".equals(state))
            {
                return null;
            }
            
            String announcement = xpath.evaluate("/announcements/announcement[@lang='" + languageCode + "']", document);
            
            if (announcement == null || announcement.length() == 0)
            {
                String defaultAnnouncement = xpath.evaluate("/announcements/announcement[not(@lang)]", document);
                
                if (defaultAnnouncement == null || defaultAnnouncement.length() == 0)
                {
                    throw new IllegalStateException("There must be a default announcement.");
                }
                
                return defaultAnnouncement;
            }
            else
            {
                return announcement;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get system announcements", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }
}
