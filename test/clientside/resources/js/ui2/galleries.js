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
var tabGalleries = {
	title: 'Galeries',
 	items: 
	[
			{
				title: 'Test gallery',
				items:
				[
                    
                        {
                            xtype: 'ametys.ribbon-button',
                            scale: 'large',
                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Galerie big menu',
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                            
                            menu: {
                                items: [
                                
                                    {
                                        text: 'Element 1'
                                    },
                                    {
                                        text: 'Element 2'
                                    }
                                
                                ]
                            }
                        },
                        
						{
							xtype: 'ametys.ribbon-button',
							scale: 'large',
			                
			                menu: 
			                { 
								items: 
								[
								 	{
							 			title: 'Prédéfini',
								 		xtype: 'ametys.ribbon-menupanel',
                                        layout: 'auto',
                                        width: 400,
                                        defaults: { width: 100 },
							 			items: 
							 			[
							 			 		 	{
                                                        xtype: 'ametys.ribbon-button',
							 			 		 		enableToggle: true,
							 			 		 		allowDepress: false,
							 			 		 		pressed: true,
							 			 		 		toggleGroup: 'vazy',

                                                        icon: 'resources/img/editpaste_32.gif',	
							 			 		 		text: 'Vasy rené la taupe'
							 			 		 	},
							 			 		 	{
                                                        xtype: 'ametys.ribbon-button',
							 			 		 		enableToggle: true,
							 			 		 		allowDepress: false,
							 			 		 		toggleGroup: 'vazy',
							 			 		 		
							 			 		 		icon: 'resources/img/editpaste_32.gif',	
							 			 		 		text: 'Vasyerzerzerzerzerze'
							 			 		 	},
							 			 		 	{
                                                        xtype: 'ametys.ribbon-button',
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
                                        xtype: 'ametys.ribbon-menupanel',
                                        layout: 'auto',
                                        width: 400,
                                        defaults: { width: 100 },
                                        items: 
								 			 		[
								 			 		 	{
                                                            xtype: 'ametys.ribbon-button',
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy zerzerzerzerzerzerzerzerze'
								 			 		 	},
								 			 		 	{
                                                            xtype: 'ametys.ribbon-button',
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
                                                            xtype: 'ametys.ribbon-button',
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
                                                            xtype: 'ametys.ribbon-button',
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	},
								 			 		 	{
                                                            xtype: 'ametys.ribbon-button',
								 			 		 		enableToggle: true,
								 			 		 		allowDepress: false,
								 			 		 		toggleGroup: 'vazy',
								 			 		 		
								 			 		 		icon: 'resources/img/editpaste_32.gif',	
								 			 		 		text: 'Vasy'
								 			 		 	}
								 			 		]
								 	},
                                    '-',
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
								 		xtype: 'ametys.ribbon-menupanel',
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
			                },
			                
			                icon: 'resources/img/editpaste_32.gif',
			                text: 'Gallerie d\'html',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						}/*,					
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
						}*/
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
