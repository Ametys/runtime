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
var monindex = 1;

function openTool1()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°1 - ' + monindex++,
        description: 'This is my tool numero uno',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: 'This is tool one<br/>'
            + '<ul>'
            +       '<li>Contextual tab 1 <a href="javascript:tabContextual1.showContextualTab()">show</a> <a href="javascript:tabContextual1.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2 <a href="javascript:tabContextual2.showContextualTab()">show</a> <a href="javascript:tabContextual2.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2bis <a href="javascript:tabContextual2bis.showContextualTab()">show</a> <a href="javascript:tabContextual2bis.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 3 <a href="javascript:tabContextual3.showContextualTab()">show</a> <a href="javascript:tabContextual3.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 4 <a href="javascript:tabContextual4.showContextualTab()">show</a> <a href="javascript:tabContextual4.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 5 <a href="javascript:tabContextual5.showContextualTab()">show</a> <a href="javascript:tabContextual5.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 6 <a href="javascript:tabContextual6.showContextualTab()">show</a> <a href="javascript:tabContextual6.hideContextualTab()">hide</a></li>'
            + '</ul>',
        closable: true
    });

    layout.addTool(tool, "cl");
    layout.focusTool(tool);
}

function openTool2()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°2 - ' + monindex++,
        description: 'This is my tool numero duo',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        dockedItems: [ createOodPanel() ],
        html: "<b>je suis le contenu du Tool 2</b>"
    });
    
    layout.addTool(tool, "l");
    layout.focusTool(tool);
}

function openTool3()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°3 - ' + monindex++,
        description: 'This is my tool numero trouo',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        scrollable: true,
        layout: 'absolute',
        items: tools3Items
    });
    
    layout.addTool(tool, "cr");
    layout.focusTool(tool);
}


function openTool4()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°4 - ' + monindex++,
        description: 'This is my tool numero quatro',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 4</b><br/>Pensez à me collapser pour tester"
    });
    
    layout.addTool(tool, "r");
    layout.focusTool(tool);
}



function openTool5()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°5 - ' + monindex++,
        description: 'This is my tool numero cinquo',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 5</b><br/>Pensez à me drag'n'droper dans une autre zone et à me ramener ici ensuite pour tester"
    });
    
    layout.addTool(tool, "t");
    layout.focusTool(tool);
}



function openTool6()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°6 - ' + monindex++,
        description: 'This is my tool numero sexto',
        smallIcon: 'resources/img/editpaste_16.gif',
        mediumIcon: 'resources/img/editpaste_32.gif',
        largeIcon: 'resources/img/editpaste_48.gif',
        type: Ametys.ui.tool.ToolPanel.TOOLTYPE_0,
        html: "<b>je suis le contenu du Tool 6</b>"
    });
    
    layout.addTool(tool, "b");
    layout.focusTool(tool);
}


function createOodPanel()
{
    var button = Ext.create("Ext.Button", {
        flex: 1,
        textAlign: 'left',
        text:"Data are not up to date. Click here to refresh this tool.",
        tooltip: {
            title: "Out of date",
            image: Ametys.CONTEXT_PATH + "/kernelresources/img/Ametys/theme/gray/uitool/reload_32.png",
            imageWidth: 32,
            imageHeight: 32,
            text: "Data currently displayed are not up to date.<br/><br/>By clicking on the yellow bar you will refresh the view.<br/><br/><span style='font-style: italic'>The refresh is not automatic for performances purpose.</span>",
            inribbon: false
        },
        handler: Ext.bind(this.refresh, this, [true], false)
    });
    
    this._oodPanel = Ext.create("Ext.container.Container", {
        cls: 'a-tool-outofdate',
        //hidden: true,
        dock: 'top',
        layout: {
            type: 'hbox',
            pack: 'start',
            align: 'stretch'
        },
        items : button
    });
    
    return this._oodPanel;
}