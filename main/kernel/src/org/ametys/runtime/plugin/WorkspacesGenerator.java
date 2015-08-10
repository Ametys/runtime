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
package org.ametys.runtime.plugin;

import java.io.IOException;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.workspace.WorkspaceManager;
import org.ametys.runtime.workspace.WorkspaceManager.InactivityCause;

/**
 * SAXes the data for the workspace tree
 */
public class WorkspacesGenerator extends AbstractGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        _saxWorkspaces();
        
        contentHandler.endDocument();
    }
    
    private void _saxWorkspaces() throws SAXException
    {
        String defaultWorkspace = RuntimeConfig.getInstance().getDefaultWorkspace();
        AttributesImpl attrs2 = new AttributesImpl();
        attrs2.addCDATAAttribute("default", defaultWorkspace);
        XMLUtils.startElement(contentHandler, "workspaces", attrs2);
        
        for (String workspaceName : WorkspaceManager.getInstance().getWorkspaceNames())
        {
            XMLUtils.createElement(contentHandler, "workspace", workspaceName);
        }
        
        Map<String, InactivityCause> inactiveWorkspaces = WorkspaceManager.getInstance().getInactiveWorkspaces();
        for (String workspace : inactiveWorkspaces.keySet())
        {
            InactivityCause inactivityCause = inactiveWorkspaces.get(workspace);
            
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("inactive", "true");
            attrs.addCDATAAttribute("cause", inactivityCause.toString());
            XMLUtils.createElement(contentHandler, "workspace", attrs, workspace);
        }
        
        XMLUtils.endElement(contentHandler, "workspaces");
    }

}
