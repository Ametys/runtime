package org.ametys.runtime.cocoon;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

/**
 * Use ametys resolver
 */
@SuppressWarnings("deprecation")
public class TraxTransformer extends org.apache.cocoon.transformation.TraxTransformer
{
    @Override
    public void setup(SourceResolver resolver, Map cocoonObjectModel, String src, Parameters par) throws SAXException, ProcessingException, IOException
    {
        org.apache.excalibur.source.SourceResolver runtimeResolver;
        try
        {
            runtimeResolver = (org.apache.excalibur.source.SourceResolver) manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        }
        catch (ServiceException e)
        {
            String errorMessage = "The runtime resource reader cannot be setup : the runtime source resolver cannot be retrived";
            getLogger().error(errorMessage);
            throw new ProcessingException(errorMessage, e);
        }
        
        super.setup(new SourceResolverWrapper(runtimeResolver), cocoonObjectModel, src, par);
    }
    
    class SourceResolverWrapper implements org.apache.cocoon.environment.SourceResolver
    {
        org.apache.excalibur.source.SourceResolver _res;
        
        /**
         * Create a wrapper
         * @param res The resolver to wrap
         */
        public SourceResolverWrapper(org.apache.excalibur.source.SourceResolver res)
        {
            _res = res;
        }

        public Source resolve(String systemID) throws ProcessingException, SAXException, IOException
        {
            throw new ProcessingException("resolve not handled");
        }

        public void release(org.apache.excalibur.source.Source eSource)
        {
            _res.release(eSource);
        }

        public org.apache.excalibur.source.Source resolveURI(String location) throws MalformedURLException, IOException
        {
            return _res.resolveURI(location);
        }

        public org.apache.excalibur.source.Source resolveURI(String location, String base, Map sParameters) throws MalformedURLException, IOException
        {
            return _res.resolveURI(location, base, sParameters);
        }
    } 
}
