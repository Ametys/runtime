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
                    quickToolbar: { 
                        items: [ 
                            { icon:'resources/img/editpaste_16.gif' } 
                        ] 
                    },
                    title: 'Accueil (fr/index.html)',
                    applicationTitle: 'Ametys Demo (www)',
                    
                    message: [{
                        title: "Warning",
                        text: "You are in safe mode... not everything will be working !",
                        type: 'warning'
                    }],
                    
                    searchMenu: {  // to activate a Tell me what you want to do feature
                        // emptyText: "Specify to replace the default value"
                        searchURL: "http://www.google.com?q={query}", // to search in doc
                        items: [
                            {
                                text: 'A button for print preview',
                                keywords: []
                            },
                            {
                                text: 'A button for print',
                                keywords: []
                            },
                            {
                                text: 'A color button',
                                keywords: []
                            },
                            {
                                text: 'A another color button',
                                keywords: []
                            }
                        ]
                    },
                    
                    help: {
                        handler: function() { alert('help'); },
                        tooltip: { inribbon: true, text: "A little bit of help?" }
                    },

                    
                    mainButton:
                    {
                        xtype: 'button',
                        text: 'Ametys',
                        tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                        menu: {
                            ui: 'ribbon-menu',
                            items: [
                                {
                                    text: 'TEST'
                                }
                            ]
                        }
                    },
                    user: {
                        fullName: 'Raphaël Franchet',
                        login: 'raphael',
                        email: 'raphael.franchet@anyware-services.com',
                        smallPhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=16&d=blank",
                        mediumPhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=32&d=mm",
                        largePhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=48&d=mm",
                        extraLargePhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=64&d=mm",                        
                        menu: { items: [ "-", {text: 'Disconnect' } ]}
                    },
                    notification: {
                          tooltip: 'A descriptive text',
                          handler: function() {  }
                    },
                    
                    items: ribbonItems
                });
                
    Ametys.tool.ToolsManager.setToolsLayout("Ametys.ui.tool.layout.ZonedTabsToolsLayout", { initialized: false });
    
	Ext.application({
		requires : ['Ext.container.Viewport'],
		name : 'AM',

		appFolder : 'app',
		enableQuickTips : false,
		launch : function() {
			Ext.create('Ext.container.Viewport', {
                    layout:  { type: 'vbox', align: 'stretch' },
                    items: [
                        ribbon,
                        Ext.apply(Ametys.tool.ToolsManager.getToolsLayout().createLayout(), {flex: 1})
                    ]
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
