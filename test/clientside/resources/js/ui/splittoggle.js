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
var tagSplitToggle = {
	title: 'Split and Toggle',
 	items: 
	[
			{
				title: 'Boutons',
				items:
				[
						{
							xtype: 'ametys.ribbon-splitbutton',
							scale: 'large',
							enableToggle: true,
			                menu: { items: [{ text: 'hey !' }]},
							
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
						{
							xtype: 'ametys.ribbon-splitbutton',
							scale: 'large',
							enableToggle: true,
			                menu: { items: [{ text: 'hey !' }]},
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'G',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
					{
							xtype: 'ametys.ribbon-splitbutton',
							scale: 'large',
							enableToggle: true,
			                menu: { items: [{ text: 'hey !' }]},
			                
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Saut de page',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
					},
					{
						align: 'middle',
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
					               
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Test',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Test long',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},

					{
						align: 'top',
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},

									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},
					{
						align: 'top',
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},
					{
						align: 'top',
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
       
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
     
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
   
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Saut',
					                								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					},
					{
						align: 'top',
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
  
									icon: 'resources/img/editpaste_16.gif',
					                								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
   
									icon: 'resources/img/editpaste_16.gif',
					                								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
     
									icon: 'resources/img/editpaste_16.gif',
					                								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							}
						]
					}
				]
			},
			{
				title: 'Test layout',
				items:
				[
					{
						align: 'top',
						columns: 2,
						items:
						[
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									colspan: 2,
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},

					                text: "Test de bouton long",
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},

					                text: "bouton court",
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
   
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
   
					                text: "bouton court2",
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-splitbutton',
									scale: 'small',
									enableToggle: true,
					                menu: { items: [{ text: 'hey !' }]},
    
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
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
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
												toggleGroup: 1,
												allowDepress: false,
												pressed: true,
								                menu: { items: [{ text: 'hey !' }]},
          
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
												toggleGroup: 1,
												allowDepress: false,
								                menu: { items: [{ text: 'hey !' }]},
           
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
												toggleGroup: 1,
												allowDepress: false,
								                menu: { items: [{ text: 'hey !' }]},
        
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
												toggleGroup: 1,
												allowDepress: false,
								                menu: { items: [{ text: 'hey !' }]},

												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
												toggleGroup: 1,
												allowDepress: false,
								                menu: { items: [{ text: 'hey !' }]},
    
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
   
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
   
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
  
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
      
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										},
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},

												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								}
						]
					},
					{
						align: 'top',
						items:
						[
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
 
												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},

												icon: 'resources/img/editpaste_16.gif',
								                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-splitbutton',
												scale: 'small',
												enableToggle: true,
								                menu: { items: [{ text: 'hey !' }]},
 
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
				title: 'Boutons grisés',
				dialogBoxLauncher: true,
				items:
				[
						{
							xtype: 'ametys.ribbon-splitbutton',
							scale: 'large',
							enableToggle: true,

			                disabled: true,
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Saut de page',
			                 tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
						{
							xtype: 'ametys.ribbon-splitbutton',
							scale: 'large',
							enableToggle: true,
			                
			                disabled: true,
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						}
				]
			}
	]	
};
