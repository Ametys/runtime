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
var factory = Ext.create("Ametys.tool.factory.BasicToolFactory", {
    "role": "uitool-tool2",
    "toolClass": "Ametys.test.tool.Tool2", 
    id: "tool2", 
    pluginName: "test",
    
    'title': "Tool n°2",
    'description': "This is the tool number two",
    'icon-small': 'resources/img/editpaste_16.gif',
    'icon-medium': 'resources/img/editpaste_32.gif',
    'icon-large': 'resources/img/editpaste_48.gif'
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

   