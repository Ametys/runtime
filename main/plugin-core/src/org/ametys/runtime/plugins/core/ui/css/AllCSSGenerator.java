/*
 *  Copyright 2010 Anyware Services
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

package org.ametys.runtime.plugins.core.ui.css;

import java.io.IOException;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * This generator generates a single CSS file to load all ui items css files.
 * Can generates a list of imports of directly intergrates all css files.
 */
public class AllCSSGenerator extends ServiceableGenerator
{
    private AllCSSComponent _allCSSComponent;
//    private SourceResolver _resolver;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _allCSSComponent = (AllCSSComponent) smanager.lookup(AllCSSComponent.ROLE);
//        _resolver = (SourceResolver) smanager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        StringBuffer sb = new StringBuffer("/* automatically generated file with hascode '" + source + "' */\n");
        
        Request request = ObjectModelHelper.getRequest(objectModel);
//        boolean importMode = parameters.getParameterAsBoolean("import", false);

        List<String> cssFiles = _allCSSComponent.getCSSFilesList(source);
        if (cssFiles != null) 
        {
            for (String cssFile : cssFiles)
            {
                sb.append("@import \"");
                sb.append(request.getContextPath());
                sb.append(cssFile);
                sb.append("\";\n");
// SEE RUNTIME-419
//                if (importMode)
//                {
//                    sb.append("@import \"");
//                    sb.append(request.getContextPath());
//                    sb.append(cssFile);
//                    sb.append("\";\n");
//                }
//                else
//                {
//                    sb.append("\n/* import ");
//                    sb.append(cssFile);
//                    sb.append(" */\n"); 
//                    
//                    Source csssource = null;
//                    InputStream is = null;
//                    try
//                    {
//                        csssource = _resolver.resolveURI("cocoon://" + cssFile);
//                        is = csssource.getInputStream();
//                        
//                        String s = IOUtils.toString(is).replaceAll("/\\*([^*]|\\*[^/])*\\*/", "").replaceAll("\r?\n", " ").trim();
//                        sb.append(s);
//                        sb.append("\n");
//                    }
//                    catch (Exception e)
//                    {
//                        sb.append("/** " + e.getMessage() + "*/");
//                    }
//                    finally
//                    {
//                        IOUtils.closeQuietly(is);
//                        _resolver.release(csssource);
//                    }
//                }
            }
        } 
                
        contentHandler.startDocument();
        XMLUtils.createElement(contentHandler, "css", sb.toString());
        contentHandler.endDocument();
    }
}
