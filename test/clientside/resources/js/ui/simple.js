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
var allsize = 'small';
var tabContextual4 = new Ametys.ui.fluent.ribbon.Panel({
	title: 'Accueil',
	contextualTab: 4,
	contextualLabel: 'Outil de test',
	items: 
	[
			{
				title: 'Boutons',
				items:
				[
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
							
							handler: function() {this.refreshing()},
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Propriétés',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
					{
						align: 'top',
						items:
						[
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Historique',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
							{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Prévisualisation PDF',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					}
				]
			},
			{
				title: 'Boutons',
				items:
				[
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
							
							handler: function() {this.refreshing()},
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Métadonnées',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Editer le contenu',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
					{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Contenu verouillé',
			                diabled: true,
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
					}
				]
			},
			{
				title: 'Boutons',
				items:
				[
					{
						align: 'top',
						items:
						[
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
									
									menu : new Ext.menu.Menu
									({
										items: 
										[
										 	new Ext.menu.Item ({ text: "Validation" }),
										 	new Ext.menu.Item ({ text: "Refus" }),
										 	new Ext.menu.Item ({ text: "Raoul" })
										]
									}),
					                pressed : true,
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis crée',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									menu : new Ext.menu.Menu
									({
										items: 
										[
										 	new Ext.menu.Item ({ text: "Validation" }),
										 	new Ext.menu.Item ({ text: "Refus" }),
										 	new Ext.menu.Item ({ text: "Raoul" })
										]
									}),

									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis à valider',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
							{
									xtype: 'ametys.ribbon-button',
									scale: allsize,

									menu : new Ext.menu.Menu
									({
										items: 
										[
										 	new Ext.menu.Item ({ text: "Validation" }),
										 	new Ext.menu.Item ({ text: "Refus" }),
										 	new Ext.menu.Item ({ text: "Raoul" })
										]
									}),

									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis prêt à publier',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},
					{
						align: 'top',
						items:
						[
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
									
									handler: function() {this.refreshing()},
					                disabled : true,
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis envoyé à la CCI',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									icon: 'resources/img/editpaste_32.gif',
									disabled: true,
					                text: 'Avis publié (CCI)',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
							{
								xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									icon: 'resources/img/editpaste_32.gif',
									disabled: true,
					                text: 'Avis rejeté',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},
					{
						align: 'top',
						items:
						[
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
									
									menu : new Ext.menu.Menu
									({
										items: 
										[
										 	new Ext.menu.Item ({ text: "Validation" }),
										 	new Ext.menu.Item ({ text: "Refus" }),
										 	new Ext.menu.Item ({ text: "Raoul" })
										]
									}),
									disabled : true,
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis en instance',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: allsize,
					                
									icon: 'resources/img/editpaste_32.gif',
					                text: 'Avis retourné (CCI) avec erreur',
					                disabled : true,
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								}
						]
					}					
				]
			}
	]	
});
var tabContextual5 = new Ametys.ui.fluent.ribbon.Panel({
		title: 'Accueil',
		contextualTab: 5,
		contextualLabel: 'Outil de test',
		items: 
	 		[
	 				{
	 					title: 'Boutons',
	 					items:
	 					[
	 							{
	 								xtype: 'ametys.ribbon-button',
	 								scale: 'large',
	 								
	 								handler: function() {this.refreshing()},
	 				                
	 								icon: 'resources/img/editpaste_32.gif',
	 				                text: 'Propriétés',
	 				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 							},
	 						{
	 							align: 'top',
	 							items:
	 							[
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Historique',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 								{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Prévisualisation PDF',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 								}
	 							]
	 						}
	 					]
	 				},
	 				{
	 					title: 'Boutons',
	 					items:
	 					[
	 							{
	 								xtype: 'ametys.ribbon-button',
	 								scale: 'large',
	 								
	 								handler: function() {this.refreshing()},
	 				                
	 								icon: 'resources/img/editpaste_32.gif',
	 				                text: 'Métadonnées',
	 				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 							},
	 							{
	 								xtype: 'ametys.ribbon-button',
	 								scale: 'large',
	 				                
	 								icon: 'resources/img/editpaste_32.gif',
	 				                text: 'Editer le contenu',
	 				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 							},
	 						{
	 								xtype: 'ametys.ribbon-button',
	 								scale: 'large',
	 				                
	 								icon: 'resources/img/editpaste_32.gif',
	 				                text: 'Contenu verouillé',
	 				                diabled: true,
	 				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 						}
	 					]
	 				},
	 				{
	 					title: 'Boutons',
	 					items:
	 					[
	 						{
	 							align: 'top',
	 							items:
	 							[
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 										
	 										menu : new Ext.menu.Menu
	 										({
	 											items: 
	 											[
	 											 	new Ext.menu.Item ({ text: "Validation" }),
	 											 	new Ext.menu.Item ({ text: "Refus" }),
	 											 	new Ext.menu.Item ({ text: "Raoul" })
	 											]
	 										}),
	 						                pressed : true,
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis crée',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										menu : new Ext.menu.Menu
	 										({
	 											items: 
	 											[
	 											 	new Ext.menu.Item ({ text: "Validation" }),
	 											 	new Ext.menu.Item ({ text: "Refus" }),
	 											 	new Ext.menu.Item ({ text: "Raoul" })
	 											]
	 										}),

	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis à valider',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 								{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,

	 										menu : new Ext.menu.Menu
	 										({
	 											items: 
	 											[
	 											 	new Ext.menu.Item ({ text: "Validation" }),
	 											 	new Ext.menu.Item ({ text: "Refus" }),
	 											 	new Ext.menu.Item ({ text: "Raoul" })
	 											]
	 										}),

	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis prêt à publier',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 								}
	 							]
	 						},
	 						{
	 							align: 'top',
	 							items:
	 							[
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 										
	 										handler: function() {this.refreshing()},
	 						                disabled : true,
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis envoyé à la CCI',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										icon: 'resources/img/editpaste_32.gif',
	 										disabled: true,
	 						                text: 'Avis publié (CCI)',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 								{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										icon: 'resources/img/editpaste_32.gif',
	 										disabled: true,
	 						                text: 'Avis rejeté',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 								}
	 							]
	 						},
	 						{
	 							align: 'top',
	 							items:
	 							[
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 										
	 										menu : new Ext.menu.Menu
	 										({
	 											items: 
	 											[
	 											 	new Ext.menu.Item ({ text: "Validation" }),
	 											 	new Ext.menu.Item ({ text: "Refus" }),
	 											 	new Ext.menu.Item ({ text: "Raoul" })
	 											]
	 										}),
	 										disabled : true,
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis en instance',
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									},
	 									{
	 										xtype: 'ametys.ribbon-button',
	 										scale: allsize,
	 						                
	 										icon: 'resources/img/editpaste_32.gif',
	 						                text: 'Avis retourné (CCI) avec erreur',
	 						                disabled : true,
	 						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
	 									}
	 							]
	 						}					
	 					]
	 				}
	 		]		
	});