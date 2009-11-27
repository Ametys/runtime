package org.ametys.runtime.plugins.core.administrator.system;

import java.io.IOException;
import java.util.Locale;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * Generates the system announcement
 */
public class SystemAnnouncementGenerator extends AbstractGenerator implements Contextualizable
{
    private org.apache.cocoon.environment.Context _environmentContext;

    public void contextualize(Context context) throws ContextException
    {
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "SystemAnnounce");
        
        String contextPath = _environmentContext.getRealPath("");
        boolean isAvailable = SystemHelper.isSystemAnnouncementAvailable(contextPath);
        
        XMLUtils.createElement(contentHandler, "IsAvailable", String.valueOf(isAvailable));
        if (isAvailable)
        {
            Locale locale = I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);
            
            XMLUtils.createElement(contentHandler, "LastModification", String.valueOf(SystemHelper.getSystemAnnoucementLastModificationDate(contextPath))); 
            XMLUtils.createElement(contentHandler, "Message", SystemHelper.getSystemAnnouncement(locale.getLanguage(), contextPath));
        }

        
        
        XMLUtils.endElement(contentHandler, "SystemAnnounce");
        contentHandler.endDocument();
    }
}
