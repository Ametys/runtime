<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>

    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>    
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>    
    
	<xsl:template match="/GroupsManager">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/groups/groups.css" type="text/css"/>      
                <link rel="stylesheet" href="{$resourcesPath}/css/users/selectuser.css" type="text/css"/>                     
            </head>
            
            <script>
            	<script type="text/javascript" src="{$resourcesPath}/js/users/selectuser.i18n.js"><xsl:comment>//emty</xsl:comment></script>
            	<script type="text/javascript">
            		RUNTIME_Plugin_Runtime_SelectUser.initialize("<xsl:value-of select="$pluginName"/>");
            		
            		<xsl:if test="AdministratorUI = 'true'">
	               		function goBack()
	                    {
	                        document.location.href = context.workspaceContext;
	                    }     
					</xsl:if>
					
					//Current selected group
	            	var selectedElmt = null;
					//If has changes
	            	var hasChanges = false;
	            		
					<xsl:if test="Modifiable = 'true'">
						function menu_new() 
						{
							var record = listviewG.getStore().recordType;
							var newEntry = new record({
													'name': "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEWGROUP"/>",
													'id': "new"
													});
							listviewG.getStore().add([newEntry]);
							listviewU.getStore().removeAll();
							if(listviewG.getStore().getCount() &gt; 0)
							{
								listviewG.getSelectionModel().select(listviewG.getStore().getCount() -1, 0);
							}
							else
							{
								listviewG.getSelectionModel().select(0, 0);
							}
							menu_rename ();
						}
						
						function menu_rename ()
    					{
    						var cell = listviewG.getSelectionModel().getSelectedCell();
    						listviewG.startEditing(cell[0],cell[1])
    					}
    					
    					function menu_remove() 
    					{
    						Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETE"/>", "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_CONFIRM"/>", menu_remove_confirm);
    					}
    					function menu_remove_confirm (button)
		                {
		                	if (button == 'yes')
		                	{
		                		var elt = listviewG.getSelection()[0];
		                    	if (200 == Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/delete", "id=" + elt.get('id')))
    							{
    								listviewG.removeElement(elt);
    							}
    							else
    							{
    								Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_ERROR"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        	});
    							}
    							
    							listviewU.getStore().removeAll();
	    						<xsl:if test="Modifiable = 'true'">
	    							handleUsers.setVisible(false);
	    							handleGroups.showElt(0);
									handleGroups.hideElt(1);
									handleGroups.hideElt(2);
	    						</xsl:if>	
		                    }
						}
					</xsl:if>	
					
					/*********************************/
					/*		CONTEXTUAL PANEL	     */
					/*********************************/
					
					// Menu gestion des groupes
					<xsl:if test="Modifiable = 'true' or AdministratorUI = 'true'">
						var handleGroups = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CATEGORY"/>'});
					</xsl:if>
					<xsl:if test="Modifiable = 'true'">
						handleGroups.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CREATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/add_group.png", menu_new);
						handleGroups.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_RENAME"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/rename.png", menu_rename);
						handleGroups.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/delete.png", menu_remove);
					</xsl:if>
					<xsl:if test="AdministratorUI = 'true'">
						handleGroups.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/quit.png", goBack);
					</xsl:if>
					
					// Menu gestion des utilisateurs
					<xsl:if test="Modifiable = 'true'">
						var handleUsers = new org.ametys.ActionsPanel({title: 'Utilisateurs' });
						handleUsers.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_ADDUSER"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/add_user.png", add_user);
						handleUsers.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETEUSER"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/delete.png", delete_user);
						handleUsers.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_VALIDATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/groups/validate.png", save_objects);
					</xsl:if>
					
					//Aide
					var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HELP_CATEGORY"/>'});
					help.addText("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_HELP_HINT"/>");	
					
					
					<xsl:if test="Modifiable = 'true'">
						handleGroups.showElt(0);
						handleGroups.hideElt(1);
						handleGroups.hideElt(2);
						handleUsers.setVisible(false);
                    </xsl:if>
                    
                    
                    <xsl:if test="AdministratorUI != 'true'">
                    	handleGroups.hideElt(3);
                    </xsl:if>
                        
					
					/*********************************/
					/*		LISTE DES GROUPES	     */
					/*********************************/
					
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
					function onSelectGroup (grid, rowindex, e)
					{
						if (selectedElmt != null &amp;&amp; hasChanges)
						{
							Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_CONFIRM"/>", function (button) {save_object_confirm (button, selectedElmt)});
						}
								
						<xsl:if test="Modifiable = 'true'">
							handleGroups.showElt(1);
							handleGroups.showElt(2);
							handleUsers.setVisible(true);
							handleUsers.showElt(4);
							handleUsers.showElt(5);
							handleUsers.hideElt(6);
						</xsl:if>
						
						listviewU.getStore().removeAll();
						
						var group = grid.getStore().getAt(rowindex);
						selectedElmt = group; 
				        var groupID = group.get('id');
						var result = Tools.postFromUrl (getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/members", "groupID=" + groupID + "&amp;sitename=" + context.siteName);
						if (result != null)
						{
							var members = result.selectNodes("GroupMembers/User");
							for (var i=0; i &lt; members.length; i++)
							{
								var fullname = members[i].selectSingleNode("FullName")[Tools.xmlTextContent];
								var login =  members[i].getAttribute("login");
								listviewU.addElement(login, {user: fullname + "(" + login + ")"});
							}
						}
					}
					
					function validateEdit (e)
					{
						return true;
					}
					function editLabel (store, record, operation)
					{
						if (operation == Ext.data.Record.EDIT)
                    	{
							if (record.get('id') == "new")
							{
								// CREER
								var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/create", "name=" + encodeURIComponent(record.get('name')));
								if (result == null)
								{
									Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEW_ERROR"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        	});
									return false;
								}
								else
								{
									record.set('id', Tools.getFromXML(result, "id"));
									record.commit();
								}
							}
							else
							{
								// RENOMMER
								var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/rename", "id=" + record.data.id + "&amp;name=" + encodeURIComponent(record.get('name')));
								if (result == null)
								{
									Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_ERROR"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        	});
								}
								else
								{
									var state = Tools.getFromXML(result, "message"); 
									if (state != null &amp;&amp; state == "missing")
									{
										Ext.Msg.show ({
			                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
			                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_MISSING_ERROR"/>",
			                        		buttons: Ext.Msg.OK,
						   					icon: Ext.MessageBox.ERROR
			                        	});
										listviewG.removeElement(record);
									}
								}
								record.commit();
							}
						}
					}
					
					 var cm = new Ext.grid.ColumnModel([{
				           id:'name',
				           header: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/>",
				           dataIndex: 'name',
				           width: 500,
				           editor: new Ext.form.TextField({
				               allowBlank: false
				           })
				        }]);
				                
					var listviewG = new org.ametys.EditorListView({
							title : "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/>",
							listeners: {'rowclick': onSelectGroup, 'validateedit': validateEdit},						
						    store : new Ext.data.SimpleStore({
						    	listeners: {'update': editLabel},
								id:0,
						        fields: [
						        	{name: 'id'},
						        	{name: 'name'}
						        ]
							}),
							sm: new Ext.grid.CellSelectionModel({singleSelect:true}),
							height: 200,
							minSize: 100,
    						maxSize: 310,
						   	cm: cm,
						   	clicksToEdit:2,
							id: 'list-view-groups',
							region: 'north',
							hideHeaders: true,
							baseCls: 'group-list',
							autoScroll: true
						});	
						
					<xsl:for-each select="groups/group">
                        listviewG.addElement("<xsl:value-of select="@id"/>", 
                                {
                                    id: "<xsl:value-of select="@id"/>",
                                    name : "<xsl:value-of select="label"/>"
                                });
                    </xsl:for-each>	
						
					var listviewU = new org.ametys.ListView({
							title : "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_COLUMN"/>",
						    store : new Ext.data.SimpleStore({
									id:0,
						        	fields: [
						           	{name: 'user'},
						           	{name: 'type'}
						        ]
							}),
							hideHeaders: true,
						    columns: [
						        {width : 500, menuDisabled : true, sortable: true, dataIndex: 'user'}
						    ],
							id: 'list-view-user',
							region: 'center',
							baseCls: 'user-list',
							autoScroll: true
						});						
					listviewU.setMultipleSelection(true);
					
					function delete_user() 
					{
					    var elts = listviewU.getSelection();
						for (var i = 0; i &lt; elts.length; i++)
						{
							listviewU.removeElement(elts[i]);
						}
						needSave();    
					}																		
					
					
					function add_user() 
					{
						function cb (users)
						{
							function seek (arr, id)
							{
								for (var i=0; i&lt;arr.length; i++)
								{
									if (arr[i].id == id)
										return arr[i];
								}
								return null;
							}
						
							var selectedElements = new Array();
							var existingElements = listviewU.getElements();
							
							for (var i in users)
							{
								var e = seek(existingElements, i);
								
								if (e == null)
									e = listviewU.addElement(i, {"user": users[i], "type": "user"});
								selectedElements.push(e);
							}
							needSave();
						}
						RUNTIME_Plugin_Runtime_SelectUser.act(cb);							
					}

					function save_objects(group)
					{
						if (group == null)
						{
							group = listviewG.getSelection()[0];
						}
						var objects = "";
						var users = listviewU.getStore().data.items;
						for (var i=0; i &lt; users.length; i++)
						{
							var objectId = users[i].id;
							objects += objectId + '/';
						}
						
						// Envoie ça sur le serveur
						var ok = false;
						while (!ok)
						{
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/modify", "id=" +group.id + "&amp;objects=" + objects);
							if (result == null)
							{
								Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_ERROR"/>", function (button) { if (button != 'yes') ok =true});
							}
							else 
							{
								var state = Tools.getFromXML(result, "message"); 
								if (state != null &amp;&amp; state == "missing")
								{
									Ext.Msg.show ({
			                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
			                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_MISSING_ERROR"/>",
			                        		buttons: Ext.Msg.OK,
						   					icon: Ext.MessageBox.ERROR
			                        });
									listviewG.removeElement(group);
								}
								else
								{
									ok = true;
									handleUsers.hideElt(6);
								}
							}
							
						}
						
						hasChanges = false;
					}
					
					function needSave (field, newValue, oldValue)
		            {
		            	handleUsers.showElt(6);
		            	hasChanges = true;
		            }
						
					function checkBeforeQuit ()
					{
						if (selectedElmt != null &amp;&amp; hasChanges)
						{
							Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_CONFIRM"/>", function (button) {save_object_confirm (button, selectedElmt)});
						}
					}
													
					var rightPanel = new org.ametys.HtmlContainer({
								region:'east',
								border: false,
								width: 277,
								cls: 'admin-right-panel',
							    items: [<xsl:if test="Modifiable = 'true'">handleGroups, </xsl:if>
						    	<xsl:if test="Modifiable = 'true'">handleUsers, </xsl:if> help]
					});
					
					var centerPanel = new Ext.Panel({
								defaults: {
								    split: true
								},
								region: 'center',
								layout: 'border',
								baseCls: 'transparent-panel',
								border: false,
								autoScroll: false,
								margins: '0 20 0 0',
								items: [listviewG, listviewU]
							});

			
					function _getAdminPanel()
					{
						return new Ext.Panel({
							region: 'center',
							baseCls: 'transparent-panel',
							border: false,
							layout: 'border',
							autoScroll: false,
							items: [centerPanel, rightPanel]
						});
					}
            			
            		Ext.onReady( function() {
						window.onbeforeunload = checkBeforeQuit
					});	
            	</script>
            </script>
            <body>
				
            </body>
        </html>
    </xsl:template>
        
</xsl:stylesheet>