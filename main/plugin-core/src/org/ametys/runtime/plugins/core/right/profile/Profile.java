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
     * SAXes a representation of this Profile
     * @param handler the ContentHandler receiving SAX events
     * @throws SAXException if a probleme occurs while SAXing events
     */
    public void toSAX(ContentHandler handler) throws SAXException;
}
