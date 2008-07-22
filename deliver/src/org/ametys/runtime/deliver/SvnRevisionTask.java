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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Simple task for retrieving current SVN revision.
 */
public class SvnRevisionTask extends Task
{
    private String _property;
    
    @Override
    public void execute() throws BuildException
    {
        try
        {
        	Process process = Runtime.getRuntime().exec("svnversion .");
        	
        	int statusCode = process.waitFor();
        	String revision = "unknown";
        	
        	if (statusCode != 0)
        	{
        		
        	}
        	else
        	{
        		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        		revision = reader.readLine();
        		int index = revision.indexOf(":");

        		if (index != -1)
        		{
        			revision = revision.substring(0, index);
        		}
        	}
        	
        	getProject().setProperty(_property, revision);
        }
        catch (Exception ex)
        {
        	getProject().log(ex.getMessage());
        	getProject().setProperty(_property, "unknown");
        }
    }
    
    /**
     * Set the property to populate.
     * @param name the property name.
     */
    public void setProperty(String name)
    {
        _property = name;
    }
}
