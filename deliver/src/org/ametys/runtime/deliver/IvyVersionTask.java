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
package org.ametys.runtime.deliver;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Simple task for creating IVY version depending on:
 * <ul>
 * <li>build version
 * <li>current SVN revision
 * <li>IVY status
 * <li>build timestamp
 * </ul>
 * 
 * The resulting IVY version is:
 * <dl>
 * <dt>release</dt>
 * <dd>[major].[minor].[patch]</dd>
 * <dt>integration</dt>
 * <dd>[major].[minor].[patch]-[svn.revision]-dev</dd>
 * <dt>development</dt>
 * <dd>[major].[minor].[patch]-[svn.revision]-dev-[timestamp]</dd>
 * </dl>
 * 
 * The resolver to use (depending on the status) is also exported.
 */
public class IvyVersionTask extends Task
{
    private String _versionOutputProperty;
    private String _resolverOutputProperty;
    private String _buildVersion;
    private String _svnRevision;
    private String _status;
    private String _timestamp;

    @Override
    public void execute() throws BuildException
    {
        String resolver = "ametys-dev";
        StringBuilder ivyVersion = new StringBuilder();

        ivyVersion.append(_buildVersion);

        if (!"release".equals(_status))
        {
            ivyVersion.append("-dev-");
            ivyVersion.append(_svnRevision);

            if (!"integration".equals(_status))
            {
                ivyVersion.append("-");
                ivyVersion.append(_timestamp);
            }
        }
        else
        {
            resolver = "ametys-release";
        }

        getProject().setProperty(_versionOutputProperty, ivyVersion.toString());
        getProject().setProperty(_resolverOutputProperty, resolver);
    }

    /**
     * Set the property to populate for IVY version value.
     * @param name the property name.
     */
    public void setVersionOutputProperty(String name)
    {
        _versionOutputProperty = name;
    }

    /**
     * Set the property to populate for resolver value.
     * @param name the property name.
     */
    public void setResolverOutputProperty(String name)
    {
        _resolverOutputProperty = name;
    }

    /**
     * Set the build version.
     * @param version the build version.
     */
    public void setBuildRevision(String version)
    {
        _buildVersion = version;
    }

    /**
     * Set the SVN revision.
     * @param revision the SVN revision.
     */
    public void setSvnRevision(String revision)
    {
        _svnRevision = revision;
    }

    /**
     * Set the IVY status.
     * @param status the IVY status.
     */
    public void setStatus(String status)
    {
        _status = status;
    }

    /**
     * Set the build timestamp.
     * @param timestamp the build timestamp.
     */
    public void setTimestamp(String timestamp)
    {
        _timestamp = timestamp;
    }
}
