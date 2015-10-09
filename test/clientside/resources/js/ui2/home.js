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
var tabHome = {
	title: 'Accueil',
 	items: 
	[
			{
				id:'boost',
				title: 'Boutons',
				smallItems:
					[
						{
							align: 'top',
							items:
							[
								{
									xtype: 'ametys.ribbon-button',
									scale: 'small',
									
									handler: function() { tinyMCE.activeEditor.execCommand('Bold'); },
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Gras',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: 'small',
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'G',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                                    
                                    listeners: {
                                        'render': function() {
                                            this.refreshing();
                                        }
                                    }
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: 'small',
					                
									icon: 'resources/img/editpaste_16.gif',
					                text: 'Raut de page',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
								}
							]
						},

						{
							align: 'middle',
							items:
							[
								{
									xtype: 'ametys.ribbon-button',
									scale: 'small',
					               
									icon: 'resources/img/editpaste_16.gif',
					                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                                                                        
                                    listeners: {
                                        'render': function() {
                                            this.refreshing();
                                        }
                                    }
								},
								{
									xtype: 'ametys.ribbon-button',
									scale: 'small',
					                
									icon: 'resources/img/editpaste_16.gif',
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
									scale: 'small',

									icon: 'resources/img/editpaste_16.gif',
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

						{
							align: 'top',
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
								}
							]
						},

						{
							align: 'top',
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
								}
							]
						}
				],
				items:
				[
					{
						xtype: 'ametys.ribbon-button',
						scale: 'large',
						
						handler: function() { tinyMCE.activeEditor.execCommand('Bold'); },
		                
						icon: 'resources/img/editpaste_32.gif',
		                text: 'Gras',
		                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
					},
					{
						xtype: 'ametys.ribbon-button',
						scale: 'large',
		                
						icon: 'resources/img/editpaste_32.gif',
		                text: 'G',
		                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                        
                        listeners: {
                            'render': function() {
                                this.refreshing();
                            }
                        }
					},
					{
						xtype: 'ametys.ribbon-button',
						scale: 'large',
		                
						icon: 'resources/img/editpaste_32.gif',
		                text: 'Saut de page',
		                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
					},

					{
						align: 'middle',
						items:
						[
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				               
								icon: 'resources/img/editpaste_16.gif',
				                text: 'Test',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},

                                listeners: {
                                    'render': function() {
                                        this.refreshing();
                                    }
                                }
							},
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
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
								xtype: 'ametys.ribbon-button',
								scale: 'small',

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
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
								icon: 'resources/img/editpaste_16.gif',
				                text: 'Saut',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
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
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
								icon: 'resources/img/editpaste_16.gif',
				                text: 'Saut',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
								icon: 'resources/img/editpaste_16.gif',
				                text: 'Saut',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
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
								xtype: 'ametys.ribbon-button',
								scale: 'small',
				                
								icon: 'resources/img/editpaste_16.gif',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true},
                                
                                listeners: {
                                    'render': function() {
                                        this.refreshing();
                                    }
                                }                                
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
				title: 'Test layout',
				priority: 1,
				smallItems: [
					{
						xtype: 'ametys.ribbon-button',
						scale: 'small',
						colspan: 2,

		                text: "Test de bouton long",
						icon: 'resources/img/editpaste_16.gif',
		                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
					}
				],
				items:
				[
					{
						align: 'top',
						columns: 2,
						items:
						[
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',
								colspan: 2,

				                text: "Test de bouton long",
								icon: 'resources/img/editpaste_16.gif',
				                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
							},
							{
								xtype: 'ametys.ribbon-button',
								scale: 'small',

				                text: "bouton court",
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
				                
				                text: "bouton court2",
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
                                        "-",
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
										},
										{
											xtype: 'ametys.ribbon-button',
											scale: 'small',
							                
											icon: 'resources/img/editpaste_16.gif',
							                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
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
						align: 'top',
						items:
						[
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
											scale: 'small',
								                
											icon: 'resources/img/editpaste_16.gif',
							                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
										{
											xtype: 'ametys.ribbon-button',
											scale: 'small',
							                
											icon: 'resources/img/editpaste_16.gif',
							                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
										}
									]
								},
								{
									items:
									[
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
				title: 'Boutons grisés',
				dialogBoxLauncher: true,
				items:
				[
						{
							xtype: 'ametys.ribbon-button',

			                disabled: true,
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Saut de page',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						},
						{
							xtype: 'ametys.ribbon-button',
			                
			                disabled: true,
							icon: 'resources/img/editpaste_32.gif',
			                text: 'Graphique',
			                tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
						}
				]
			},
            {
                title: 'Outils',
                items:
                [
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Tous les outils',
                            handler: function () { openTool1(); openTool2(); openTool3(); openTool4(); openTool5(); openTool6(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 1',
                            handler: function () { openTool1(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 2',
                            handler: function () { openTool2(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 3',
                            handler: function () { openTool3(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 4',
                            handler: function () { openTool4(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 5',
                            handler: function () { openTool5(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        },
                        {
                            xtype: 'ametys.ribbon-button',

                            icon: 'resources/img/editpaste_32.gif',
                            text: 'Outils 6',
                            handler: function () { openTool6(); },
                            tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Cliquez ici pour avoir accès aux fonctions générales de l\'application ou pour vous déconnecter.', footertext: 'Voir l\'aide pour plus de détails', inribbon: true}
                        }
                ]
            }

	]	
};