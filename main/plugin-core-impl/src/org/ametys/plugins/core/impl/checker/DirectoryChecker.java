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
package org.ametys.plugins.core.impl.checker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.impl.FileSource;

import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Checks if the specified repository exists and optionally if the user has the writing rights
 */
public class DirectoryChecker extends AbstractLogEnabled implements Configurable, ParameterChecker, Serviceable
{
    /** Equals true if the directory has to be writable */
    private boolean _checkWrite;
    
    /** The source resolver */
    private SourceResolver _sourceResolver;

    /** Creates the directory if it does not exist already */
    private boolean _createsIfRequired;

    private ServiceManager _sManager;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _createsIfRequired = configuration.getChild("configuration").getChild("creates-if-required", false) != null;
        _checkWrite = configuration.getChild("configuration").getChild("check-write", false) != null;
        
        if (configuration.getChild("linked-params").getChildren().length != 1)
        {
            throw new ConfigurationException("The Directory Checker should have exactly 1 linked parameter: the directory");
        }
    }
    
    @Override
    public void service(ServiceManager sManager) throws ServiceException
    {
        _sManager = sManager;
    }
    
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        if (_sourceResolver == null)
        {
            try
            {
                _sourceResolver = (SourceResolver) _sManager.lookup(SourceResolver.ROLE);
            }
            catch (ServiceException e)
            {
                throw new ParameterCheckerTestFailureException("The test cannot be tested now", e);
            }
        }
        
        _checkDirectory(values.get(0));
    }
    
    /**
     * Checks if the source exists and optionally checks if it is writable
     * @param path The path of the source to check
     * @throws ParameterCheckerTestFailureException if an error occurred 
     */
    private void _checkDirectory(String path) throws ParameterCheckerTestFailureException
    {
        Source source = null;
        try
        {
            source = _sourceResolver.resolveURI(path, "context://", null); 
            
            boolean removeIt = false;

            if (!source.exists())
            {
                if (!_createsIfRequired)
                {
                    throw new ParameterCheckerTestFailureException("The specified directory at '" + source.getURI() + "' does not exist");
                }
                if (!(source instanceof ModifiableTraversableSource))
                {
                    throw new ParameterCheckerTestFailureException("The specified directory at '" + source.getURI() + "' does not exist and cannot be created");
                }
                
                try
                {
                    ((ModifiableTraversableSource) source).makeCollection();
                    removeIt = true;
                }
                catch (SourceException e)
                {
                    throw new ParameterCheckerTestFailureException("The specified directory at '" + source.getURI() + "' does not exist and cannot be created", e);
                }
            }
            
            if (!(source instanceof TraversableSource) && ((TraversableSource) source).isCollection())
            {
                throw new ParameterCheckerTestFailureException("The specified directory at '" + source.getURI() + "' is not a directory");
            }
            
            if (_checkWrite 
                    && (!(source instanceof ModifiableTraversableSource)
                            || !(source instanceof FileSource)
                            || !((FileSource) source).getFile().canWrite()))
            {
                throw new ParameterCheckerTestFailureException("The specified directory at '" + source.getURI() + "' is not writable");
            }
            
            if (removeIt)
            {
                try
                {
                    ((ModifiableTraversableSource) source).delete();
                }
                catch (SourceException e)
                {
                    getLogger().warn("The specified directory '" + source.getURI() + "' was created for tests purposes but could not be removed", e);
                }
            }
        }
        catch (MalformedURLException e)
        {
            throw new ParameterCheckerTestFailureException("An error occurred while trying to resolve the following path '" + path + "'." , e);
        }
        catch (IOException e)
        {
            throw new ParameterCheckerTestFailureException("An error occurred while trying to resolve the following path '" + path + "'." , e);
        }
    }
}
