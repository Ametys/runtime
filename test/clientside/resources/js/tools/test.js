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
 
var factory = Ext.create("Ametys.tool.factory.UniqueToolFactory", {
    "role": "uitool-tool1",
    "toolClass": "Ametys.test.tool.Tool1", 
    id: "tool1", 
    pluginName: "test",
    
    'title': "Tool n°1",
    'description': "This is the tool number one",
    'icon-small': '/test/resources/img/editpaste_16.gif',
    'icon-medium': '/test/resources/img/editpaste_32.gif',
    'icon-large': '/test/resources/img/editpaste_48.gif'
});
Ametys.tool.ToolsManager.addFactory(factory);

Ext.define('Ametys.test.tool.Tool1', {
    extend: "Ametys.tool.Tool",
    
    setParams: function (params)
    {
        this.callParent(arguments);
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
    },
    
    createPanel: function ()
    {
        return Ext.create("Ext.panel.Panel", {
            items: [
                {
                    xtype: 'component',
                    html: 'This is tool one'
                },
                {
                    xtype: 'button',
                    text: 'open a tool n°2',
                    handler: function()
                    {
                        Ametys.tool.ToolsManager.openTool("uitool-tool2", { id: "t" + Math.random() })
                    }
                }
            ]
        });
    },
    
    getType: function()
    {
        return 20;
    }
});

var factory = Ext.create("Ametys.tool.factory.BasicToolFactory", {
    "role": "uitool-tool2",
    "toolClass": "Ametys.test.tool.Tool2", 
    id: "tool2", 
    pluginName: "test",
    
    'title': "Tool n°2",
    'description': "This is the tool number two",
    'icon-small': '/test/resources/img/editpaste_16.gif',
    'icon-medium': '/test/resources/img/editpaste_32.gif',
    'icon-large': '/test/resources/img/editpaste_48.gif'
});
Ametys.tool.ToolsManager.addFactory(factory);

Ext.define('Ametys.test.tool.Tool2', {
    extend: "Ametys.tool.Tool",
    
    setParams: function (params)
    {
        this.callParent(arguments);
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
    },
    
    createPanel: function ()
    {
        return Ext.create("Ext.panel.Panel", {
            html: 'This is tool one'
        });
    }
});


var control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", {
    'id':               "button1",
    'opentool-id':    "uitool-tool1",
    'label':            "Tool one",
    'description':      "Open tool number one",
    'icon-small' :      "/test/resources/img/editpaste_16.gif",
    'icon-medium' :     "/test/resources/img/editpaste_32.gif",
    'icon-large' :      "/test/resources/img/editpaste_48.gif"
});
Ametys.ribbon.RibbonManager.registerUI(control);

var tab1 = Ext.create("Ametys.ui.fluent.ribbon.Panel", {
    title: "Home",
    items: [
        {
            title: 'Tools',
            priority: -10,
            smallItems: undefined,
            items: [
                Ametys.ribbon.RibbonManager.getUI("button1").addUI("large")
            ],
            largeItems: undefined
        }
    ]
});
ribbonItems.push(tab1);
