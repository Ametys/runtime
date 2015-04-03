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
var ribbon, layout;
function onreadyfunction() {
    ribbon = Ext.create("Ametys.ui.fluent.ribbon.Ribbon", {
                    applicationTitle: 'Ametys Test Tools',
                    items: [
                    ],
                    help: {
                        handler: function() { alert('help'); },
                        tooltip: "A little bit of help?"
                    },
                    user: {
                        text: 'Raphaël Franchet',
                        menu: { items: [ {text: 'raphael' } ]}
                    },
                    menu:
                    {
                        icon: 'resources/img/ametys.gif',
                        tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                        items:
                        [
                            {
                                text: 'TEST'
                            }
                        ]
                    }
                }, {
                    region : 'north'
                });
                
    Ametys.tool.ToolsManager.setToolsLayout("Ametys.ui.tool.layout.ZonedTabsToolsLayout", { initialized: false });
    
	Ext.application({
		requires : ['Ext.container.Viewport'],
		name : 'AM',

		appFolder : 'app',
		enableQuickTips : false,
		launch : function() {
			Ext.create('Ext.container.Viewport', {
				layout : 'border',
				items : [ ribbon, Ametys.tool.ToolsManager.getToolsLayout().createLayout() ]
			});
            
            Ext.create("Ametys.message.Message", {
                type: Ametys.message.Message.SELECTION_CHANGED,
                targets: [],
                callback: function()
                {
                    Ext.suspendLayouts();  
                
                    Ametys.tool.ToolsManager.init({
                        autoRefreshingFactories: [ ],
                        autoOpenedTools: [ ]
                    });
                    
                    // Ametys.tool.ToolsManager.openTool("...", { .. });
                                
                    Ametys.tool.ToolsManager.getToolsLayout().setAsInitialized();
                    
                    Ext.resumeLayouts(true);
                }
            });            
		}
	});
}
Ext.onReady(onreadyfunction);
