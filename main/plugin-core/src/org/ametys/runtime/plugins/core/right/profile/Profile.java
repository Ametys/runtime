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
package org.ametys.runtime.plugins.core.right.profile;

import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A profile is a set of Rights.
 */
public interface Profile
{
    /**
     * Returns the unique Id of this profile
     * @return the unique Id of this profile
     */
    public String getId();
    
    /**
     * Returns the name of this profile
     * @return the name of this profile
     */
    public String getName();
    
    /**
     * Returns the context of this profile
     * @return the context of this profile. Can be null.
     */
    public String getContext();
    
    /**
     * Adds a Right to this Profile
     * @param rightId the Right to add to this profile
     */
    public void addRight(String rightId);
    
    /**
     * Renames this Profile
     * @param newName the new label of this Profile
     */
    public void rename(String newName);
    
    /**
     * Returns the set of Rights of this profile
     * @return the set of Rights of this profile
     */
    public Set<String> getRights();
    
    /**
     * Removes associated rights
     */
    public void removeRights();
    
    /**
     * Remove the right from database
     */
    public void remove();
    
    /**
     * Start update mode: subsequent calls to removeRights and addRight must be
     * enclosed in a transaction until endUpdate is called. 
     */
    public void startUpdate();
    
    /**
     * End update mode: make effective the changes since startUpdate was called.
     * ("commit" the transaction).
     */
    public void endUpdate();
    
    /**
     * SAXes a representation of this Profile
     * @param handler the ContentHandler receiving SAX events
     * @throws SAXException if a probleme occurs while SAXing events
     */
    public void toSAX(ContentHandler handler) throws SAXException;
}
