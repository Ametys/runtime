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
package org.ametys.plugins.core.ui.system;

import java.io.IOException;
import java.util.Locale;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.admin.system.SystemHelper;

/**
 * Generates the system announcement
 */
public class SystemAnnouncementGenerator extends ServiceableGenerator implements Contextualizable
{
    private org.apache.cocoon.environment.Context _environmentContext;
    private SystemHelper _systemHelper;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _systemHelper = (SystemHelper) smanager.lookup(SystemHelper.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "SystemAnnounce");
        
        String contextPath = _environmentContext.getRealPath("/");
        boolean isAvailable = _systemHelper.isSystemAnnouncementAvailable(contextPath);
        
        XMLUtils.createElement(contentHandler, "IsAvailable", String.valueOf(isAvailable));
        if (isAvailable)
        {
            Locale locale = I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);
            
            XMLUtils.createElement(contentHandler, "LastModification", String.valueOf(_systemHelper.getSystemAnnoucementLastModificationDate(contextPath))); 
            XMLUtils.createElement(contentHandler, "Message", _systemHelper.getSystemAnnouncement(locale.getLanguage(), contextPath));
        }
        
        XMLUtils.endElement(contentHandler, "SystemAnnounce");
        contentHandler.endDocument();
    }
}
