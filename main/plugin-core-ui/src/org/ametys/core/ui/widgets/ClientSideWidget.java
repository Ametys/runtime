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
package org.ametys.core.ui.widgets;

import java.util.Map;

import org.ametys.core.ui.ClientSideElement;

/**
 * This class is a particular client side element that stands for widgets 
 */
public interface ClientSideWidget extends ClientSideElement
{
    /**
     * Returns the supported types
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return An non null and non empty list of supported types
     */
    public String[] getFormTypes(Map<String, Object> contextParameters);
    /**
     * Determine if the widget can handle enumerated values
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return true if it does
     */
    public boolean supportsEnumerated(Map<String, Object> contextParameters);
    /**
     * Determine if the widget can handle non-enumerated values
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return true if it does
     */
    public boolean supportsNonEnumerated(Map<String, Object> contextParameters);
    /**
     * Determine if the widget can handle multiple values
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return true if it does
     */
    public boolean supportsMultiple(Map<String, Object> contextParameters);
    /**
     * Determine if the widget can handle non-multiple values
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return true if it does
     */
    public boolean supportsNonMultiple(Map<String, Object> contextParameters);
}
