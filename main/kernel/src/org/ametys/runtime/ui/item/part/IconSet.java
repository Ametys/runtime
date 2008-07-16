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
package org.ametys.runtime.ui.item.part;

/**
 * This class handle an icon set.<br/>
 * A icon set, is the same icon in 3 different size.
 */
public class IconSet
{
    /** The small size icon path */
    protected String _iconSmall;
    /** The medium size icon path */
    protected String _iconMedium;
    /** The large size icon path */
    protected String _iconLarge;
    
    /**
     * Create an iconset
     * @param basePath The common base path. May not be null or empty. The / is automatically added and should not terminate the base path.
     * @param smallIconPath The small sized icon path. Small size is 16x16 pixels 
     * @param mediumIconPath The medium sized icon path. Medium size is 32x32 pixels 
     * @param largeIconPath The large sized icon path Large size is 50x50 pixels 
     */
    public IconSet(String basePath, String smallIconPath, String mediumIconPath, String largeIconPath)
    {
        _iconSmall = basePath + '/' + smallIconPath;
        _iconMedium = basePath + '/' + mediumIconPath;
        _iconLarge = basePath + '/' + largeIconPath;
    }
    
    /**
     * Creates a plugin related IconSet with a single icon
     * @param pluginName the name of the plugin
     * @param iconPath the path to the unique icon
     * @return an IconSet with a single icon
     */
    public static IconSet createPluginIconSet(String pluginName, String iconPath)
    {
        return new IconSet("/plugins/" + pluginName + "/resources", iconPath, iconPath, iconPath);
    }
    
    /**
     * Creates a plugin related IconSet
     * @param pluginName the name of the plugin
     * @param smallIconPath The small sized icon path. Small size is 16x16 pixels 
     * @param mediumIconPath The medium sized icon path. Medium size is 32x32 pixels 
     * @param largeIconPath The large sized icon path Large size is 50x50 pixels 
     * @return the corrsponding icon set
     */
    public static IconSet createPluginIconSet(String pluginName, String smallIconPath, String mediumIconPath, String largeIconPath)
    {
        return new IconSet("/plugins/" + pluginName + "/resources", smallIconPath, mediumIconPath, largeIconPath);
    }
    
    /**
     * Get the path to small version icon of the interaction. 
     * @return The path from the root url context.
     */
    public String getSmallIconPath()
    {
        return _iconSmall;
    }

    /**
     * Get the path to medium version icon of the interaction. 
     * @return The path from the root url context.
     */
    public String getMediumIconPath()
    {
        return _iconMedium;
    }

    /**
     * Get the path to large version icon of the interaction. 
     * @return The path from the root url context.
     */
    public String getLargeIconPath()
    {
        return _iconLarge;
    }
}
