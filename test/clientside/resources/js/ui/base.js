/*
 *  Copyright 2013 Anyware Services
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
                    title: 'Accueil (fr/index.html)',
                    applicationTitle: 'Ametys Demo (www)',
                    items: [
                        tabHome,
                        tabToggle,
                        tabMenu,
                        tabSplit,
                        tagSplitToggle,
                        tabComponents,
                        tabContextual1,
                        tabContextual2,
                        tabContextual2bis,
                        tabContextual3,
                        tabContextual4,
                        tabContextual5                        
                    ],
                    help: {
                        handler: function() { alert('help'); },
                        tooltip: "A little bit of help?"
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
                
    layout = Ext.create("Ametys.ui.tool.layout.ZonedTabsToolsLayout");
    
	Ext.application({
		requires : ['Ext.container.Viewport'],
		name : 'AM',

		appFolder : 'app',
		enableQuickTips : false,
		launch : function() {
			Ext.create('Ext.container.Viewport', {
				layout : 'border',
				items : [ ribbon, layout.createLayout() ]
			});
		}
	});
    
    ribbon.getPanel().add(tabContextual6);


	// org.ametys.fluent.Tooltip.init();

	// tinyMCE.init
	// ({
	// // General options
	// mode : "none",
	// theme : "advanced",
	// plugins : "table,paste,tabfocus",
	// doctype: "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"
	// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
	//
	// entity_encoding : 'raw',
	// fix_list_elements : true,
	// fix_table_elements : true,
	// fix_nesting : true,
	// verify_css_classes : false,
	// gecko_spellcheck : true,
	// paste_strip_class_attributes: "mso",
	// paste_remove_styles: true,
	// strict_loading_mode : true,
	//
	// // Theme options
	// theme_advanced_buttons1 : "",
	// theme_advanced_buttons2 : "",
	// theme_advanced_buttons3 : "",
	//				
	// // Theme options
	// theme_advanced_toolbar_location : "none",
	// theme_advanced_statusbar_location : "none",
	//					
	// setup: function(ed)
	// {
	// ed.onNodeChange.add (onRichTextNodeSelected);
	// }
	// });
	// tinyMCE.execCommand("mceAddControl", true, "toto");

}
Ext.onReady(onreadyfunction);

// tinyMCE.focus = function()
// {
// tinyMCE.activeEditor.focus();
// tinyMCE.activeEditor.selection.moveToBookmark(tinyMCE.activeEditor._bookmark);
// }
// onRichTextNodeSelected = function(editor, controlManager, node, isCollapsed)
// {
// editor.lastNode = node;
// if (editor.notFirstCallToOnRichTextNodeSelected)
// {
// // Action
// tinyMCE.activeEditor._bookmark =
// tinyMCE.activeEditor.selection.getBookmark(1);
// }
// else
// {
// editor.onClick.remove(editor.nodeChanged);
// }
// editor.notFirstCallToOnRichTextNodeSelected = true;
// }
