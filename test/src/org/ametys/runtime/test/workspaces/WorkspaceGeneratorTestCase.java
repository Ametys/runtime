/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.test.workspaces;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Test case for the workspace generator
 */
public class WorkspaceGeneratorTestCase extends AbstractRuntimeTestCase
{
    /**
     * This test verify that the omitted groups are correctly generated
     * @throws Exception If an error occurs
     */
    public void testRibbonGroupsGeneration() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Document document = _getWorkspaceDocument("ribbon-test-groups.xml");

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        
        // Generate medium from <large> group
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/large/control[@id='org.ametys.plugins.workspace-generator-test.control1']", document, XPathConstants.NODESET)).getLength());
        assertEquals(1, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/medium/control[@id='org.ametys.plugins.workspace-generator-test.control1']", document, XPathConstants.NODESET)).getLength());
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/small/control[@id='org.ametys.plugins.workspace-generator-test.control1']", document, XPathConstants.NODESET)).getLength());
        
        // Generate medium from <group> 
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/large/control[@id='org.ametys.plugins.workspace-generator-test.control2']", document, XPathConstants.NODESET)).getLength());
        assertEquals(1, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/medium//control[@id='org.ametys.plugins.workspace-generator-test.control2']", document, XPathConstants.NODESET)).getLength());
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/small/control[@id='org.ametys.plugins.workspace-generator-test.control2']", document, XPathConstants.NODESET)).getLength());

        // Generate <medium> and <small> with enough elements to create layouts
        assertEquals(8, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/large/control[@id='org.ametys.plugins.workspace-generator-test.control3']", document, XPathConstants.NODESET)).getLength());
        assertEquals(8, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/medium//control[@id='org.ametys.plugins.workspace-generator-test.control3']", document, XPathConstants.NODESET)).getLength());
        assertEquals(8, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/small//control[@id='org.ametys.plugins.workspace-generator-test.control3']", document, XPathConstants.NODESET)).getLength());
        
        // Confirm normal ribbon groups generation
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/large/control[@id='org.ametys.plugins.workspace-generator-test.control4']", document, XPathConstants.NODESET)).getLength());
        assertEquals(1, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/medium/control[@id='org.ametys.plugins.workspace-generator-test.control4']", document, XPathConstants.NODESET)).getLength());
        assertEquals(0, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group/small/control[@id='org.ametys.plugins.workspace-generator-test.control4']", document, XPathConstants.NODESET)).getLength());

        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }

    private Document _getWorkspaceDocument(String ribbonFile) throws ServiceException, MalformedURLException, IOException, ParserConfigurationException, SAXException, SourceNotFoundException
    {
        SourceResolver resolver = (SourceResolver) Init.getPluginServiceManager().lookup(SourceResolver.ROLE);
        Source source = resolver.resolveURI("cocoon://_workspace-test3/plugins/workspace-generator-test/workspace-generator/" + ribbonFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(source.getInputStream());
        return document;
    }
    
    /**
     * This test verify that we can create controls on the fly
     * @throws Exception If an error occurs
     */
    public void testRibbonControlsOnTheFly() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Document document = _getWorkspaceDocument("ribbon-test-onthefly.xml");

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // Declare SimpleMenu on the fly
        String menuId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control/@id", document, XPathConstants.STRING);
        assertTrue(menuId != null);
        String menuConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + menuId + "']/action", document, XPathConstants.STRING);
        assertTrue(menuConfig != null && menuConfig.contains("primary-menu-item-id") && menuConfig.contains("menu-items"));

        // Create controls on the fly
        String controlId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/control[1]/@id", document, XPathConstants.STRING);
        assertTrue(controlId != null);
        String controlConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + controlId + "']/action", document, XPathConstants.STRING);
        assertTrue(controlConfig != null && controlConfig.contains("On the fly, no id"));
        
        controlId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/control[2]/@id", document, XPathConstants.STRING);
        assertEquals("test-onthefly", controlId);
        controlConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + controlId + "']/action", document, XPathConstants.STRING);
        assertTrue(controlConfig != null && controlConfig.contains("On the fly, with id"));
        
        controlId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/control[3]/@id", document, XPathConstants.STRING);
        assertTrue(controlId != null);
        controlConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + controlId + "']/action", document, XPathConstants.STRING);
        assertTrue(controlConfig != null && controlConfig.contains("On the fly, by ref-id"));
        
        controlId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/control[4]/@id", document, XPathConstants.STRING);
        assertEquals("test-onthefly2", controlId);
        controlConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + controlId + "']/action", document, XPathConstants.STRING);
        assertTrue(controlConfig != null && controlConfig.contains("On the fly, id and ref-id"));
        
        controlId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/control[5]/@id", document, XPathConstants.STRING);
        assertTrue(controlId != null);
        controlConfig = (String) xpath.evaluate("/workspace/ribbon/controls/control[@id='" + controlId + "']/action", document, XPathConstants.STRING);
        assertTrue(controlConfig != null && controlConfig.contains("On the fly, ref-id of a ref-id"));

        // Create tabs on the fly
        String tabId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab[@label='application:TAB_LABEL2']/@controlId", document, XPathConstants.STRING);
        assertTrue(tabId != null);
        String tabConfig = (String) xpath.evaluate("/workspace/ribbon/tabsControls/tab[@id='" + tabId + "']/action", document, XPathConstants.STRING);
        assertTrue(tabConfig != null && tabConfig.contains("test-tab1"));

        tabId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab[@label='application:TAB_LABEL3']/@controlId", document, XPathConstants.STRING);
        assertEquals("tab-onthefly1", tabId);
        tabConfig = (String) xpath.evaluate("/workspace/ribbon/tabsControls/tab[@id='" + tabId + "']/action", document, XPathConstants.STRING);
        assertTrue(tabConfig != null && tabConfig.contains("test-tab2"));

        tabId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab[@label='application:TAB_LABEL4']/@controlId", document, XPathConstants.STRING);
        assertTrue(tabId != null);
        tabConfig = (String) xpath.evaluate("/workspace/ribbon/tabsControls/tab[@id='" + tabId + "']/action", document, XPathConstants.STRING);
        assertTrue(tabConfig != null && tabConfig.contains("test-tab3"));
        
        tabId = (String) xpath.evaluate("/workspace/ribbon/tabs/tab[@label='application:TAB_LABEL5']/@controlId", document, XPathConstants.STRING);
        assertTrue(tabId != null);
        tabConfig = (String) xpath.evaluate("/workspace/ribbon/tabsControls/tab[@id='" + tabId + "']/action", document, XPathConstants.STRING);
        assertTrue(tabConfig != null && tabConfig.contains("test-tab4"));

        
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }
    
    /**
     * This test verify that we can inject controls into an existing tab
     * @throws Exception If an error occurs
     */
    public void testRibbonControlInjection() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Document document = _getWorkspaceDocument("ribbon-test-injectcontrol.xml");

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        
        assertEquals("Overriding tabs must not be saxed", 1, ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab", document, XPathConstants.NODESET)).getLength());
        
        // injected group order is correct
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW2' and position() = 1]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW3' and position() = 2]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL' and position() = 3]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2' and position() = 4]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW6' and position() = 5]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW8' and position() = 6]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW7' and position() = 7]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW1' and position() = 8]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL3' and position() = 9]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL4' and position() = 10]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW' and position() = 11]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW4' and position() = 12]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong group order on injection", 1 == ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL_NEW5' and position() = 13]", document, XPathConstants.NODESET)).getLength());
        
        // inject control order is correct
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_9' and position() = 1]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_10' and position() = 2]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_3' and position() = 3]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_4' and position() = 4]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='org.ametys.plugins.workspace-generator-test.control1' and position() = 5]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='org.ametys.plugins.workspace-generator-test.control2' and position() = 6]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_7' and position() = 7]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_8' and position() = 8]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_11' and position() = 9]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_12' and position() = 10]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_15' and position() = 11]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_16' and position() = 12]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_13' and position() = 13]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_14' and position() = 14]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='org.ametys.plugins.workspace-generator-test.control3' and position() = 15]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='org.ametys.plugins.workspace-generator-test.control4' and position() = 16]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_1' and position() = 17]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_2' and position() = 18]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_5' and position() = 19]", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong control order on injection", 0 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL']/medium/control[@id='INJECT_CONTROl_6' and position() = 20]", document, XPathConstants.NODESET)).getLength());
       
        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }
    
    /**
     * This test verify that we can inject controls into an existing tab
     * @throws Exception If an error occurs
     */
    public void testRibbonMergeLayout() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");

        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Document document = _getWorkspaceDocument("ribbon-test-mergelayouts.xml");

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        
        assertTrue("Wrong number of controls in layout merge", 2 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL1']/medium/layout[0]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 3 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL1']/medium/layout[1]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 2 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL1']/medium/layout[2]/control", document, XPathConstants.NODESET)).getLength());

        assertTrue("Wrong number of controls in layout merge", 5 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/layout[0]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 6 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/layout[1]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 1 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL2']/medium/layout[2]/control", document, XPathConstants.NODESET)).getLength());

        assertTrue("Wrong number of controls in layout merge", 3 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL3']/medium/layout[0]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 4 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL3']/medium/layout[1]/control", document, XPathConstants.NODESET)).getLength());
        assertTrue("Wrong number of controls in layout merge", 5 != ((NodeList) xpath.evaluate("/workspace/ribbon/tabs/tab/groups/group[@label='application:RIBBON_GROUP_LABEL3']/medium/layout[2]/control", document, XPathConstants.NODESET)).getLength());

        _cocoon._leaveEnvironment(environmentInformation);
        cocoon.dispose();
    }  

}
