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
package org.ametys.runtime.plugins.core.administrator.system;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return false;
            }
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(systemFile);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String state = xpath.evaluate("/announcements/@state", document);
            
            return "on".equals(state);
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
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return null;
            }
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(systemFile);
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
    }
}
