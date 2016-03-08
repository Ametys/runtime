/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.core.util.cocoon;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;

/**
 * This action simply checks to see if a resource identified by the <code>src</code>
 * sitemap attribute exists or not. The action returns empty <code>Map</code> if
 * resource does not exist, <code>null</code> otherwise.
 */
public class ResourceNotExistsAction extends ServiceableAction implements ThreadSafe 
{
    private org.apache.excalibur.source.SourceResolver _srcResolver;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _srcResolver = (org.apache.excalibur.source.SourceResolver) smanager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception 
    {
        Source source = null;
        try 
        {
            source = _srcResolver.resolveURI(src);
            if (!source.exists()) 
            {
                return EMPTY_MAP;
            }
        } 
        catch (SourceNotFoundException e) 
        {
            // Do not log
            return EMPTY_MAP;
        } 
        catch (Exception e) 
        {
            getLogger().warn("Exception resolving resource " + src, e);
        } 
        finally 
        {
            if (source != null) 
            {
                resolver.release(source);
            }
        }
        return null;
    }
}
