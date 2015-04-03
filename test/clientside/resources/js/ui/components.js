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
var tabComponents = {
	title: 'Mixed components',
 	items: 
	[
			{
				title: 'Test components',
				items:
				[
					{
						align: 'middle',
						columns: 2,
						items:
						[
								{
									colspan: 2,
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
							new Ext.form.ComboBox
							({
            		        	store: [['id1', 'Titre 1'], ['id2', 'Titre 2'], ['id3', 'Titre 3'], ['id4', 'Paragraphe']],
            		        	value: 'id4',
            		        	mode: 'local',
            		        	triggerAction: 'all',
            		        	disableKeyFilter: true,
            		        	editable: false,
            		        	width: 80
							}),
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								}
						]
					},
					{
						align: 'middle',
						columns: 2,
						items:
						[
								{
									colspan: 2,
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
							new Ext.form.ComboBox
							({
            		        	store: [['id1', 'Titre 1'], ['id2', 'Titre 2'], ['id3', 'Titre 3'], ['id4', 'Paragraphe']],
            		        	value: 'id4',
            		        	mode: 'local',
            		        	width: 50,
            		        	triggerAction: 'all',
            		        	disableKeyFilter: true,
            		        	editable: false
							}),
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								}
						]
					},
					{
						align: 'middle',
						columns: 2,
						items:
						[
								{
									colspan: 2,
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
							new Ext.form.Checkbox
							({
								boxLabel: 'Claquiste ta mère'
							}),
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-button',
												scale: 'small',
								                
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								}
						]
					}						
				]
			},
			{
				title: 'Forms',
				items:
				[
				 	{
				 		xtype: 'textarea',
				 		height: 66,
				 		width: 260
				 	},
				 	{
						align: 'top',
						columns: 1,
						items: [
				 		        {
							 		xtype: 'textfield',
                                    name: 'a',
							 		width: 100,
							 		value: 'aa'
				 		        },
				 		        {
							 		xtype: 'numberfield',
                                    name: 'b',
							 		width: 100,
							 		value: 'aa'
				 		        },
				 		        {
							 		xtype: 'textfield',
                                    name: 'c',
							 		width: 100,
							 		emptyText: 'Fill here'
				 		        }
				 		]
				 	},
				 	{
						align: 'top',
						columns: 1,
						items: [
								{
									xtype: 'ametys.ribbon-button',
										scale: 'small',
						                
										icon: 'resources/img/editpaste_16.gif',
						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
										scale: 'small',
						                
										icon: 'resources/img/editpaste_16.gif',
						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
										scale: 'small',
						                
										icon: 'resources/img/editpaste_16.gif',
						                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								}
				 		]
				 	}				 	
				]
			},
			{
				title: 'Test gallery',
				items:
				[
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: 
			                { 
								items: 
								[
								 	{
								 		xtype: 'ametys.ribbon-menugallery',
							 			items: 
							 			[
							 			 	{
							 			 		title: 'Prédéfini',
							 			 		items:
							 			 		[
							 			 		 	{
							 			 		 		enableToggle: true,
							 			 		 		allowDepress: false,
							 			 		 		pressed: true,
							 			 		 		toggleGroup: 'vazy',
							 			 		 		
							 			 		 		icon: 'resources/img/editpaste_32.gif',	
							 			 		 		text: 'Vasy rené la taupe'
							 			 		 	},
							 			 		 	{
							 			 		 		enableToggle: true,
							 			 		 		allowDepress: false,
							 			 		 		toggleGroup: 'vazy',
							 			 		 		
							 			 		 		icon: 'resources/img/editpaste_32.gif',	
							 			 		 		text: 'Vasyerzerzerzerzerze'
							 			 		 	},
							 			 		 	{
							 			 		 		enableToggle: true,
							 			 		 		allowDepress: false,
							 			 		 		toggleGroup: 'vazy',
							 			 		 		
							 			 		 		icon: 'resources/img/editpaste_32.gif',	
							 			 		 		text: 'Vasyzerzerzerzerzerzerer zerzer'
							 			 		 	}
							 			 		]
							 			 	},
							 			 	{
							 			 		title: 'Manuels',
							 			 		items:
								 			 		[
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy zerzerzerzerzerzerzerzerze'
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	}
								 			 		]
							 			 	}
							 			]
								 	},
								 	{ text: 'hey !' }
								]
			                },
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Gallerie d\'icones',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: 
			                { 
								items: 
								[
								 	{
								 		xtype: 'ametys.ribbon-menugallery',
							 			items: 
							 			[
							 			 	{
							 			 		items:
								 			 		[
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildGalleryText("Normal", "font-scale: 12px; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildGalleryText("Titre 1", "font-scale: 16px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildGalleryText("Titre 2", "font-scale: 14px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildGalleryText("Titre 3", "font-scale: 12px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildGalleryText("Titre 4", "font-scale: 12px; color: red; line-height: 46px;")
								 			 		 	}
								 			 		]
							 			 	}
							 			]
								 	}
								]
			                },
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Gallerie d\'html',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},					
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: 
			                { 
								items: 
								[
								 	{
								 		xtype: 'ametys.ribbon-menugallery',
								 		inribbon: true,
							 			cls: 'x-item-inribbon',
							 			items: 
							 			[
							 			 	{
							 			 		items:
								 			 		[
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildInribbonGalleryText("Normal", "font-scale: 12px; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildInribbonGalleryText("Titre 1", "font-scale: 16px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildInribbonGalleryText("Titre 2", "font-scale: 14px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildInribbonGalleryText("Titre 3", "font-scale: 12px; color: red; font-weight: bold; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		text: buildInribbonGalleryText("Titre 4", "font-scale: 12px; color: red; line-height: 46px;")
								 			 		 	},
								 			 		 	{
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',
								 			 		 		text: 'test of a long doc'
								 			 		 	}
								 			 		]
							 			 	}
							 			]
								 	}
								]
			                },
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Gallerie inribbon',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						}
				]
			}
	]	
};

function buildGalleryText(title, style, sample)
{
	if (sample == null)
	{
		sample = "AaBbCc";
	}
	
	return "<div style='width: 66px; height: 63px; overflow: hidden; background-color: #ffffff;'><div style='height: 46px; vertical-align: middle; " + style + "'>" + sample + "</div><div style='padding-bottom: 5px;'>" + title + "</div></div>";
}
function buildInribbonGalleryText(title, style, sample)
{
	if (sample == null)
	{
		sample = "AaBbCc";
	}
	return "<div style='width: 66px; height: 50px; overflow: hidden;  background-color: #ffffff;'><div style='height: 33px; vertical-align: middle; " + style + "'>" + sample + "</div><div style='padding-bottom: 5px;'>" + title + "</div></div>";
}
