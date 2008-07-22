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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Simple task writing two lines in a file :<br>
 * - the resource name<br>
 * - the resource URI
 */
public class ResourceFileTask extends Task
{
    private String _name;
    private String _uri;
    private File _destFile;

    @Override
    public void execute() throws BuildException
    {
        try
        {
            _destFile.getParentFile().mkdirs();

            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(_destFile)));

            pw.println(_name);
            pw.println(_uri);

            pw.close();
        }
        catch (IOException ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Set the resource name
     * 
     * @param name
     *            the resource name
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Set the resource URI
     * 
     * @param uri
     *            the resource URI
     */
    public void setURI(String uri)
    {
        _uri = uri;
    }

    /**
     * Set the file to write
     * 
     * @param destFile
     *            the file to write
     */
    public void setDestFile(File destFile)
    {
        _destFile = destFile;
    }
}
