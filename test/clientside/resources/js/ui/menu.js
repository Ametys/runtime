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
var tabMenu = {
	title: 'Menu',
 	items: 
	[
			{
				title: 'Boutons',
				smallItems:
				[
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: { items: [{ text: 'hey !', menu: { items: [{ text: 'hey !' }]} }]},
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						}
				],
				items:
				[
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
                            menu: { items: [{ text: 'hey !', menu: { items: [{ text: 'hey !' }]} }]},
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: { items: [{ text: 'hey !', handler: function() { tinyMCE.focus(); /*tinyMCE.activeEditor.execCommand('Bold');*/ } }]},
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique sur deux lignes',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: { items: [{ text: 'hey !', handler: function() { tinyMCE.focus(); /*tinyMCE.activeEditor.execCommand('Bold');*/ } }]},
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: { items: [{ text: 'hey !', handler: function() { tinyMCE.focus(); /*tinyMCE.activeEditor.execCommand('Bold');*/ } }]},
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'very-small',
			                
			                menu: { items: [{ text: 'hey !', handler: function() { tinyMCE.focus(); /*tinyMCE.activeEditor.execCommand('Bold');*/ } }]},
			                
			                icon: 'resources/img/editpaste_16.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'small',
			                
			                menu: { items: [{ text: 'hey !', handler: function() { tinyMCE.focus(); /*tinyMCE.activeEditor.execCommand('Bold');*/ } }]},
			                
			                icon: 'resources/img/editpaste_16.gif',
			                text: 'Graphique',
							tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}		
						}
				]
			}
	]	
};