/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.ui.item;

import java.util.regex.Pattern;

import org.ametys.runtime.ui.ClientSideElement;

/**
 * Client side element for administration workspace handle urls to be able to tell which component is currently used in the left bar
 */
public interface AdminClientSideElement extends ClientSideElement
{
    /**
     * Get the url.
     * @return the regexp url. Can be null or empty.
     */
    public Pattern getUrl();
    
    /**
     * Set the url
     * @param url The regexp url
     */
    public void setUrl(String url);
}
