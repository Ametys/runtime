<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   -->
<xsl:stylesheet version="1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   	
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
        
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>   
    
    <xsl:template match="/ProfilesManager">
        
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_LABELLONG"/></title>
	            <link rel="stylesheet" href="{$resourcesPath}/css/rights/profiles.css" type="text/css"/>
            </head>
            
				<script>
					<script src="{$resourcesPath}/js/org/ametys/rights/CheckRightEntry.js"/>
					<script src="{$resourcesPath}/js/org/ametys/rights/RightEntry.js"/>
	            	<script type="text/javascript">
	            			//Current selected profile
	            			var selectedElmt = null;
	            			//If has changes
	            			var hasChanges = false;
	            			
	            			var MAP_RIGHTS = function (config) { Ext.apply(this, config); } 
							MAP_RIGHTS.prototype.toString = function() 
							{ 
								var str = ""
								for (var i in this) 
								{ 
									str += i + "-";
								}
								return str;
							};
								
		               		function goBack()
		                    {
		                        document.location.href = context.workspaceContext;
		                    }   
		                    
		                   	function menu_new() 
							{
								var record = listview.getStore().recordType;
								var newEntry = new record({
														'name': "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEWPROFILE"/>",
														'id': "new",
                                                        'rights': {}
														});
                                
                                var serverMessage = new org.ametys.servercomm.ServerMessage("<xsl:value-of select="$pluginName"/>", "/rights/profiles/create", {name: newEntry.get('name') }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
                                var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
                                
                                if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>", result, "editLabel"))
                                {
                                    return;
                                }
                                else
                                {
                                    newEntry.set('id', org.ametys.servercomm.ServerComm.handleResponse(result, 'id'));
                                    newEntry.commit();
                                }
                                
								listview.getStore().add([newEntry]);
                                
								if(listview.getStore().getCount() &gt; 0)
								{
									listview.getSelectionModel().select(listview.getStore().getCount() -1, 0);
								}
								else
								{
									listview.getSelectionModel().select(0, 0);
								}
                                selectedElmt = newEntry;
								
								menu_rename ();
							}
							
							function menu_rename ()
	    					{
	    						var cell = listview.getSelectionModel().getSelectedCell();
	    						listview.startEditing(cell[0],cell[1]);
	    					}
							
		                    function menu_remove ()
							{
								Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_CONFIRM"/>", menu_remove_confirm);
							}
							function menu_remove_confirm (button)
							{
								if (button == 'yes')
								{
									var elt = listview.getSelection()[0];
									if (200 == org.ametys.servercomm.DirectComm.getInstance().sendSynchronousRequest(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/delete", "id=" + elt.get('id')).status)
    									{
    										listview.removeElement(elt);
    									}
    									else
    									{
    										Ext.Msg.show ({
    			                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
    			                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_ERROR"/>",
    			                        		buttons: Ext.Msg.OK,
    						   					icon: Ext.MessageBox.ERROR
    			                        	});
    									}
                                    }
								}
		                    
		                    function save_objects(element)
		                    {
								if (element == null)
								{
									element = listview.getSelection()[0];
								}
												
								// Met à jour le noeud ! et liste les droits à envoyer
								var objects = [];
								
								var newRights = new MAP_RIGHTS({});
								
								for (var i=0; i &lt; RIGHTS_ID.length; i++)
								{
									var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
									if (rightElmt.getValue())
									{
										newRights[rightElmt.getName()] = "";
										objects.push(rightElmt.getName());
									}
								}
								element.set('rights', newRights);
							
								
								// Envoie ça sur le serveur
								var ok = false;
								while (!ok)
								{
									var serverMessage = new org.ametys.servercomm.ServerMessage("<xsl:value-of select="$pluginName"/>", "/rights/profiles/modify", { id: element.get('id'), objects: objects }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
									var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
								
								    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_ERROR"/>", result, "save_objects"))
								    {
										return;
								    }
								    else
									{
										var state = org.ametys.servercomm.ServerComm.handleResponse(result, "message"); 
										if (state != null &amp;&amp; state == "missing")
										{
											Ext.Msg.show ({
				                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
				                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_MISSING_ERROR"/>",
				                        		buttons: Ext.Msg.OK,
							   					icon: Ext.MessageBox.ERROR
				                        	});
											listview.removeElement(element);
										}
										else
										{
											ok = true;
											_Category.hideElt(2);
										}
									}
									
								}
								hasChanges = false;
								selectLayout(); // to update view if saving at layout time
		                    }
		                    
		                    function needSave (field, newValue, oldValue)
		                    {
		                    	_Category.showElt(2);
		                    	hasChanges = true;
		                    }
		                    
		                    function save_object_confirm (button, selectedElmt)
							{
								if (button == 'yes')
								{
									save_objects(selectedElmt);
								}
								else
								{
									hasChanges = false;
								}
							}
					
		                    function checkBeforeQuit ()
							{
								if (selectedElmt != null &amp;&amp; hasChanges)
								{
									if (confirm ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
									{
										save_object_confirm ('yes', selectedElmt);
										
									}
								}
							}
							
							function select_all (id)
							{
								if (id != null)
								{
									var fd = Ext.getCmp(id);
									fd.items.each (function (item) {item.setValue(true)});
								}
								else
								{
									for (var i=0; i &lt; RIGHTS_ID.length; i++)
									{
										var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
										rightElmt.setValue(true);
									}
								}
							}
							
							function unselect_all (id)
							{
								if (id != null)
								{
									var fd = Ext.getCmp(id);
									fd.items.each (function (item) {item.setValue(false)});
								}
								else
								{
									for (var i=0; i &lt; RIGHTS_ID.length; i++)
									{
										var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
										rightElmt.setValue(false);
									}
								}
							}
							
							function onSelectProfil (grid, rowindex, e)
							{
								if (selectedElmt != null &amp;&amp; hasChanges)
								{
									Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>", function (button) {save_object_confirm (button, selectedElmt)});
								}
								
								_Category.showElt(1);
								_Category.hideElt(2);
								_Category.showElt(3);
								
								var elmt = listview.getSelection()[0];
								selectedElmt = elmt; 
								var rights = elmt.get('rights') || {};
								
								for (var i=0; i &lt; RIGHTS_CATEGORY.length; i++)
								{
									Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
								}
								
								for (var i=0; i &lt; RIGHTS_ID.length; i++)
								{
									//Mise à jour de l'écran d'édition
									var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
									rightElmt.removeListener('check', needSave);
									var id = rightElmt.getName();
									rightElmt.setDisabled(false);
									rightElmt.setValue(rights[id] != null);
									rightElmt.addListener('check', needSave);
									
									//Mise à jour de l'écran de visualisation
									var cat = Ext.getCmp("cat_" + rightElmt.category + "_read");
									var display = rights[id] != null;
									if (display)
									{
										cat.show();
									}
									if (Ext.get(id + '_read') != null)
									{
										Ext.get(id + '_read').dom.style.display = display ? "" : "none";
									}
								}
							}
							
							function beforeEditLabel (e)
							{
								var record = e.record;
							}
							
							function validateEdit (e)
							{
								var record = e.record;
								if (!/^[a-z|A-Z|0-9| |-|_]*$/.test(e.value))
		                        {
		                        	Ext.Msg.show ({
				                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
				                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NAMING_ERROR"/>",
				                        		buttons: Ext.Msg.OK,
							   					icon: Ext.MessageBox.ERROR
				                        	});
		                            return false;
		                        }
                    		}
                    		function editLabel (store, record, operation)
                    		{
                    			if (operation == Ext.data.Record.EDIT)
                    			{
									if (record.get('id') == "new")
									{
									
										// CREER
										var serverMessage = new org.ametys.servercomm.ServerMessage("<xsl:value-of select="$pluginName"/>", "/rights/profiles/create", {name: record.get('name') }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
										var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
									
									    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>", result, "editLabel"))
									    {
									       return;
									    }
										else
										{
											record.set('id', org.ametys.servercomm.ServerComm.handleResponse(result, "id"));
											record.commit();
										}
									}
									else
									{
										// RENOMMER
										var serverMessage = new org.ametys.servercomm.ServerMessage("<xsl:value-of select="$pluginName"/>", "/rights/profiles/rename", { id: record.get('id'), name: record.get('name') }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
										var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
									
									    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_ERROR"/>", result, "editLabel"))
									    {
									       return;
									    }
										else 
										{
											var state = org.ametys.servercomm.ServerComm.handleResponse(result, "message"); 
											if (state != null &amp;&amp; state == "missing")
											{
												new org.ametys.msg.ErrorDialog ("<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_MISSING_ERROR"/>", "state is missing", "editLabel");
												listview.removeElement(record);
												return;
											}
										}
										record.commit();
									}
								}
							}
							
							// Onglets
							var _Navigation = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
							var item1 = new org.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_READ"/>",
								handlerFn: selectLayout,
								activeItem: 0,
								cardLayout: 'profile-card-panel',
								toggleGroup : 'profile-menu',
								pressed: true
							})
							_Navigation.add(item1);
							var item2 = new org.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_EDIT"/>",
								handlerFn: selectLayout,
								activeItem: 1,
								cardLayout: 'profile-card-panel',
								toggleGroup : 'profile-menu'
							}) 
							_Navigation.add(item2);
							
							
							// Gestion des profils
							var _Category = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CATEGORY"/>'});
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CREATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/new.png", menu_new);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_RENAME"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/rename.png", menu_rename);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_VALIDATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/validate.png", save_objects);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/delete.png", menu_remove);
							
							<xsl:if test="AdministratorUI = 'true'">
								_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/quit.png", goBack);
							</xsl:if>	
							
							_Category.hideElt(1);
							_Category.hideElt(2);
							_Category.hideElt(3);
							
							// Utils
							var _utilsCategory = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_SELECT_HANDLE_CATEGORY"/>'});
							_utilsCategory.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_SELECT_ALL"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/select_all.png", select_all);
							_utilsCategory.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_UNSELECT_ALL"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/unselect_all.png", unselect_all);
							_utilsCategory.setVisible(false);
							
							// Help
							var helpCategory = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HELP_CATEGORY"/>'});
							helpCategory.addText("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_HELP_HINT"/>");
							
							//Profils existants
							var profileData = [];
							
							<xsl:for-each select="profiles/profile">
								var rights = new MAP_RIGHTS({});
								<xsl:for-each select="rights/right">
									rights['<xsl:value-of select="@id"/>'] = "";
								</xsl:for-each>
								profileData.push(['<xsl:value-of select="@id" />', '<xsl:value-of select="label" />', rights]);
							</xsl:for-each>						
						    
						   	var store = new Ext.data.SimpleStore({
						   				listeners: {'update': editLabel},
										id:0,
								        fields: [
								           {name: 'id'},
								           {name: 'name'},
								           {name: 'rights'}
								        ]
									});
						   	store.loadData(profileData);
						   	
						   	var cm = new Ext.grid.ColumnModel([{
						           id:'name',
						           header: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE"/>",
						           dataIndex: 'name',
						           width: 540,
						           editor: new Ext.form.TextField({
						               allowBlank: false
						           })
						        }]);
				        	
						   	var listview = new org.ametys.EditorListView({
						   		title : '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_LIST"/>',
								listeners: {'rowclick': onSelectProfil, 'beforeedit': beforeEditLabel, 'validateedit': validateEdit},						
							    store : store,
								sm: new Ext.grid.CellSelectionModel({singleSelect:true}),
								height: 150,
								anchor: '0.5', //ascenseur
							   	cm: cm,
							   	clicksToEdit:2,
								region: 'center',
								id: 'profile-list-view',
								baseCls: 'profile-list-view',
								region: 'north',
								hideHeaders: true,
								minSize: 75,
    							maxSize: 500,
    							split: true,
    							border: true
    							
							});	
                            
							//Panel for edit rights profils
							var editRights = new Ext.Panel({
								title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
								autoScroll:true,
								border: false,
								id: 'profile-edit-panel',
								baseCls: 'profile-edit-panel'
							});  
							//Panel for read rights profils
							var readRights = new Ext.Panel({ 
								title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
								autoScroll:true,
								id: 'profile-read-panel',
								baseCls: 'profile-read-panel'
							});
							var cardPanel = new Ext.Panel ({
								id:'profile-card-panel',
								region:'center',
								border: false,
								layout:'card',
								activeItem:0,
								minSize: 75,
    							maxSize: 500,
    							baseCls: 'card-panel',
								items:[readRights, editRights]
							});
							
							var RIGHTS_ID = [];
							var RIGHTS_CATEGORY = [];
							<xsl:for-each select="rights/right[not(category/@id = preceding-sibling::right/category/@id)]">
								<xsl:variable name="category" select="category/@id"/>
								<xsl:variable name="categoryKey" select="category"/>
								RIGHTS_CATEGORY.push('<xsl:value-of select="$category"/>');
								var cat_<xsl:value-of select="$category"/> = new org.ametys.TBarFieldset({
										title : "<xsl:copy-of select="$categoryKey/*"/>",
										layout: 'form',
										id: "cat_<xsl:value-of select="$category"/>_edit",
										bbar: [new Ext.Button({
											icon: '<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/select_16.png',
											tooltip: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_CATEGORY_SELECT_ALL"/>',
											handler: function (){select_all ('cat_<xsl:value-of select="$category"/>_edit')}
									    }), new Ext.Button({
											icon: '<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/unselect_16.png',
											tooltip: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_CATEGORY_UNSELECT_ALL"/>',
											handler: function (){unselect_all ('cat_<xsl:value-of select="$category"/>_edit')}
									    })]
								});
								editRights.add(cat_<xsl:value-of select="$category"/>);	
								var cat_<xsl:value-of select="$category"/>_read = new org.ametys.Fieldset({
									title : "<xsl:copy-of select="$categoryKey/*"/>",
									layout: 'form',
									id: "cat_<xsl:value-of select="$category"/>_read",
									cls: 'fieldset'
								});
								readRights.add(cat_<xsl:value-of select="$category"/>_read);
								<xsl:for-each select="../right[category/@id = $category]">
									var input = new org.ametys.rights.CheckRightEntry ({
										listeners: {'check': needSave},
										width: 175,
										text : "<xsl:copy-of select="label/*"/>",
								        name: "<xsl:value-of select="@id"/>",
								        id: "<xsl:value-of select="@id"/>",
								        description: "<xsl:copy-of select="description/*"/>",
								        category: "<xsl:value-of select="$category"/>",
								        hideLabel : true,
								        disabled: true
									});	
									RIGHTS_ID.push("<xsl:value-of select="@id"/>");	
									cat_<xsl:value-of select="$category"/>.add(input);
									
									var profileText = new org.ametys.rights.RightEntry({
										id : "<xsl:value-of select="@id"/>_read", 
										width: 175,
										text : "<xsl:copy-of select="label/*"/>", 
										description: "<xsl:copy-of select="description/*"/>"
									});
									cat_<xsl:value-of select="$category"/>_read.add(profileText);
								</xsl:for-each>
								cat_<xsl:value-of select="$category"/>_read.hide();
							</xsl:for-each> 
							
							var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									width: 277,
									baseCls: 'admin-right-panel',
								    items: [_Navigation, _Category, _utilsCategory, helpCategory]
							});
							
							var centerPanel = new Ext.Panel({
								defaults: {
								    split: true
								},
								region:'center',
								baseCls: 'transparent-panel',
								border: false,
								layout: 'border',
								autoScroll : true,
								height: 'auto',
								items: [listview, cardPanel]
							});
							
							org.ametys.runtime.administrator.Panel.createPanel = function () 
							{
								return new Ext.Panel({
									region: 'center',
									baseCls: 'transparent-panel',
									border: false,
									layout: 'border',
									autoScroll: true,
									items: [centerPanel, rightPanel]
								});
							}								
	            			
	            			function selectLayout()
							{
								if (selectedElmt != null &amp;&amp; hasChanges &amp;&amp; item1.pressed)
								{
									Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>", function (button) {save_object_confirm (button, selectedElmt)});
								}
								var rights = selectedElmt != null ? selectedElmt.get('rights') : {};
								
								// Mise à jour de la vue en fonction du profil sélectionné
								if (item1.pressed)
								{
									cardPanel.getLayout().setActiveItem(0);
									for (var i=0; i &lt; RIGHTS_CATEGORY.length; i++)
									{
										Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
									}
									for (var i=0; i &lt; RIGHTS_ID.length; i++)
									{
										//Mise à jour de l'écran de visualisation
										var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
										var id = rightElmt.getName();
										var cat = Ext.getCmp("cat_" + rightElmt.category + "_read");
										var display = rights[id] != null;
										if (display)
										{
											cat.show();
										}
										if (Ext.get(id + '_read') != null)
										{
											Ext.get(id + '_read').dom.style.display = display ? "" : "none";
										}
									}
									_utilsCategory.setVisible(false);
								}
								else
								{	
									cardPanel.getLayout().setActiveItem(1);
									for (var i=0; i &lt; RIGHTS_ID.length; i++)
									{
										//Mise de l'écran d'édition
										var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
										rightElmt.removeListener('check', needSave);
										var id = rightElmt.getName();
										rightElmt.setDisabled(selectedElmt == null);
										rightElmt.setValue(rights[id] != null);
										rightElmt.addListener('check', needSave);
									}
									_utilsCategory.setVisible(true);
								}
							}
							
	            			Ext.onReady( function () {
								window.onbeforeunload = checkBeforeQuit
							});
	            	</script>
	           </script>
	           
	
        </html>
    </xsl:template>
    
</xsl:stylesheet>