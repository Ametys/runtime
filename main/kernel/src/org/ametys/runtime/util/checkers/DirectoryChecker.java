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
package org.ametys.runtime.util.checkers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

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

import org.ametys.runtime.config.ParameterChecker;
import org.ametys.runtime.config.ParameterCheckerTestFailureException;

/**
 * Checks if the specified repository exists and optionally if the user has the writing rights
 */
public class DirectoryChecker extends AbstractLogEnabled implements Configurable, ParameterChecker, Serviceable
{
    /** The id of the configuration panel's parameter which path is going to be checked */
    private String _pathParameter;
    
    /** Equals true if the directory has to be writable */
    private boolean _checkWrite;
    
    /** The source resolver */
    private SourceResolver _sourceResolver;

    /** Creates the directory if it does not exists already */
    private boolean _createsIfRequired;

    private ServiceManager _sManager;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _createsIfRequired = configuration.getChild("configuration").getChild("creates-if-required", false) != null;
        _checkWrite = configuration.getChild("configuration").getChild("check-write", false) != null;
        
        if (configuration.getChild("linked-params").getChildren().length != 1)
        {
            throw new ConfigurationException("The SqlConnectionChecker should have 1 linked param: directory");
        }

        _pathParameter = configuration.getChild("linked-params").getChild("param-ref").getAttribute("id");
    }
    
    @Override
    public void service(ServiceManager sManager) throws ServiceException
    {
        _sManager = sManager;
    }
    
    @Override
    public void check(Map<String, String> configurationParameters) throws ParameterCheckerTestFailureException
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
        
        String path = configurationParameters.get(_pathParameter);
        _checkDirectory(path);
    }
    
    /**
     * Checks if the source exists and optionally checks if it is writable
     * @param path The path of the source to check
     * @throws ParameterCheckerTestFailureException 
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
