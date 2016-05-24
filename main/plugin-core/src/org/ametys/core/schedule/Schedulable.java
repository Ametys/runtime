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
package org.ametys.core.schedule;

import java.util.Map;

import org.quartz.Job;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * This interface represents a 'job' which can be perform and schedule.
 *
 */
public interface Schedulable extends Job
{
    /**
     * Returns the label
     * @return the i18n label
     */
    public I18nizableText getLabel ();
    
    /**
     * Return the description
     * @return the i18n description
     */
    public I18nizableText getDescription ();
    
    /**
     * Returns the glyph icon
     * @return the glyph icon
     */
    public String getIconGlyph();
    
    /**
     * Returns the path to the small icon in 16x16 pixels
     * @return the path to the 16x16 icon
     */
    public String getIconSmall();
    
    /**
     * Returns the path to the medium icon in 32x32 pixels
     * @return the path to the 32x32 icon
     */
    public String getIconMedium();
    
    /**
     * Returns the path to the large icon in 48x48 pixels
     * @return the path to the 48x48 icon
     */
    public String getIconLarge();
    
    /**
     * Get the parameters for job execution
     * @return the parameters
     */
    public Map<String, Object> getParameters();
}
