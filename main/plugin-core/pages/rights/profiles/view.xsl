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
   	
    <xsl:import href="plugin:core://stylesheets/widgets.xsl"/>

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
					<script src="{$resourcesPath}/js/ametys/rights/Ext.ametys.CheckRightEntry.js"/>
					<script src="{$resourcesPath}/js/ametys/rights/Ext.ametys.RightEntry.js"/>
	            	<script type="text/javascript">
	            			//Current selected profil
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
														'id': "new"
														});
								listview.getStore().add([newEntry]);
								if(listview.getStore().getCount() &gt; 0)
								{
									listview.getSelectionModel().select(listview.getStore().getCount() -1, 0);
								}
								else
								{
									listview.getSelectionModel().select(0, 0);
								}
								
								menu_rename ();
							}
							
							function menu_rename ()
	    					{
	    						var cell = listview.getSelectionModel().getSelectedCell();
	    						listview.startEditing(cell[0],cell[1]);
	    					}
							
		                    function menu_remove ()
							{
								var elt = listview.getSelection()[0];		      
								if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_CONFIRM"/>"))
								{
									if (200 == Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/delete", "id=" + elt.get('id')))
									{
										listview.removeElement(elt);
									}
									else
										alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_ERROR"/>")
								}
							}
		                    
		                    function save_objects(element)
		                    {
								if (element == null)
								{
									element = listview.getSelection()[0];
								}
												
								// Met à jour le noeud ! et liste les droits à envoyer
								var objects = "";
								
								var newRights = new MAP_RIGHTS({});
								
								for (var i=0; i &lt; RIGHTS_ID.length; i++)
								{
									var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
									if (rightElmt.getValue())
									{
										newRights[rightElmt.getName()] = "";
										objects += rightElmt.getName() + '/';
									}
								}
								element.set('rights', newRights);
							
								
								// Envoie ça sur le serveur
								var ok = false;
								while (!ok)
								{
									var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/modify", "id=" + element.get('id') + "&amp;objects=" + objects);
									if (result == null)
									{
										if (!confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_ERROR"/>"))
										{
											ok = true;
										}
									}
									else 
									{
										var state = Tools.getFromXML(result, "message"); 
										if (state != null &amp;&amp; state == "missing")
										{
											alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_MISSING_ERROR"/>");
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
		                    }
		                    
		                    function needSave (field, newValue, oldValue)
		                    {
		                    	_Category.showElt(2);
		                    	hasChanges = true;
		                    }
		                    
		                    function checkBeforeQuit ()
							{
								if (selectedElmt != null &amp;&amp; hasChanges)
								{
									if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
									{
										save_objects(selectedElmt);
										//return false; //Pour rester sur la page
									}
								}
							}
							
							function onSelectProfil (grid, rowindex, e)
							{
								if (selectedElmt != null &amp;&amp; hasChanges)
								{
									if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
									{
										save_objects(selectedElmt);
									}
									else
									{
										hasChanges = false;
									}
								}
								
								_Category.showElt(1);
								_Category.hideElt(2);
								_Category.showElt(3);
								
								var elmt = listview.getSelection()[0];
								selectedElmt = elmt; 
								var rights = elmt.get('rights');
								
								for (var i=0; i &lt; RIGHTS_CATEGORY.length; i++)
								{
									Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
								}
								
								for (var i=0; i &lt; RIGHTS_ID.length; i++)
								{
									//Mise de l'écran d'édition
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
									Ext.get(id + '_read').dom.style.display = display ? "" : "none";
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
		                            alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NAMING_ERROR"/>")
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
										var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/create", "name=" + encodeURIComponent(record.get('name')));
										if (result == null)
										{
											alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>")
											return;
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
										var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/rename", "id=" + record.get('id') + "&amp;name=" + encodeURIComponent(record.get('name')));
										if (result == null)
										{
											alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_ERROR"/>");
										}
										else 
										{
											var state = Tools.getFromXML(result, "message"); 
											if (state != null &amp;&amp; state == "missing")
											{
												alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_MISSING_ERROR"/>");
												listview.removeElement(record);
												return;
											}
										}
										record.commit();
									}
								}
							}
							
							// Onglets
							var _Navigation = new Ext.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
							var item1 = new Ext.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_READ"/>",
								handlerFn: selectLayout,
								toggleGroup : 'profile-menu',
								pressed: true
							})
							_Navigation.add(item1);
							var item2 = new Ext.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_EDIT"/>",
								handlerFn: selectLayout,
								toggleGroup : 'profile-menu'
							}) 
							_Navigation.add(item2);
							
							
							// Gestion des profils
							var _Category = new Ext.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CATEGORY"/>'});
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CREATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/new.gif", menu_new);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_RENAME"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/rename.gif", menu_rename);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_VALIDATE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/valid.gif", save_objects);
							_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/delete.png", menu_remove);
							
							<xsl:if test="AdministratorUI = 'true'">
								_Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/profiles/quit.png", goBack);
							</xsl:if>	
							
							_Category.hideElt(1);
							_Category.hideElt(2);
							_Category.hideElt(3);
							
							
							// Help
							var helpCategory = new Ext.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HELP_CATEGORY"/>'});
							helpCategory.addText("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_HELP_HINT"/>");
							
							//Profils existants
							var dummyData = [];
							
							<xsl:for-each select="profiles/profile">
								var rights = new MAP_RIGHTS({});
								<xsl:for-each select="rights/right">
									rights['<xsl:value-of select="@id"/>'] = "";
								</xsl:for-each>
								dummyData.push(['<xsl:value-of select="@id" />', '<xsl:value-of select="label" />', rights]);
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
						   	store.loadData(dummyData);	
						   	
						   	var cm = new Ext.grid.ColumnModel([{
						           id:'name',
						           header: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE"/>",
						           dataIndex: 'name',
						           width: 540,
						           editor: new Ext.form.TextField({
						               allowBlank: false
						           })
						        }]);
				        	
						   	var listview = new Ext.ametys.EditorListView({
						   		title : 'Liste des profils',
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
								title: 'Droits associés',
								autoScroll:true,
								border: false,
								id: 'profile-edit-panel',
								baseCls: 'profile-edit-panel'
							});  
							//Panel for read rights profils
							var readRights = new Ext.Panel({ 
								title: 'Droits associés',
								autoScroll:true,
								id: 'profile-read-panel',
								baseCls: 'profile-read-panel'
							});
							var cardPanel = new Ext.Panel ({
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
							<xsl:for-each select="rights/right[not(category = preceding-sibling::right/category)]">
								<xsl:variable name="category" select="category"/>
								RIGHTS_CATEGORY.push('<xsl:value-of select="$category"/>');
								var cat_<xsl:value-of select="$category"/> = new Ext.ametys.Fieldset({
										title : "<i18n:text i18n:key="{$category}" i18n:catalogue="{@catalogue}"/>",
										layout: 'form',
										id: "cat_<xsl:value-of select="$category"/>_edit",
										cls: 'fieldset'
								});
								editRights.add(cat_<xsl:value-of select="$category"/>);	
								var cat_<xsl:value-of select="$category"/>_read = new Ext.ametys.Fieldset({
									title : "<i18n:text i18n:key="{$category}" i18n:catalogue="{@catalogue}"/>",
									layout: 'form',
									id: "cat_<xsl:value-of select="$category"/>_read",
									cls: 'fieldset'
								});
								readRights.add(cat_<xsl:value-of select="$category"/>_read);
								<xsl:for-each select="../right[category = $category]">
									var input = new Ext.ametys.CheckRightEntry ({
										listeners: {'check': needSave},
										width: 190,
										boxLabel : "<i18n:text i18n:key="{label}" i18n:catalogue="{@catalogue}"/>",
								        name: "<xsl:value-of select="@id"/>",
								        id: "<xsl:value-of select="@id"/>",
								        desc: "<i18n:text i18n:key="{description}" i18n:catalogue="{@catalogue}"/>",
								        category: "<xsl:value-of select="$category"/>",
								        hideLabel : true,
								        disabled: true
									});	
									RIGHTS_ID.push("<xsl:value-of select="@id"/>");	
									cat_<xsl:value-of select="$category"/>.add(input);
									
									var profileText = new Ext.ametys.RightEntry({
										id : "<xsl:value-of select="@id"/>_read", 
										width: 190,
										text : "<i18n:text i18n:key="{label}" i18n:catalogue="{@catalogue}"/>", 
										desc: "<i18n:text i18n:key="{description}" i18n:catalogue="{@catalogue}"/>"
									});
									cat_<xsl:value-of select="$category"/>_read.add(profileText);
								</xsl:for-each>
								cat_<xsl:value-of select="$category"/>_read.hide();
							</xsl:for-each> 
							
							var rightPanel = new Ext.ametys.HtmlContainer({
									region:'east',
									border: false,
									width: 277,
									baseCls: 'admin-right-panel',
								    items: [_Navigation, _Category, helpCategory]
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
							
							function _getAdminPanel ()
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
									if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
									{
										save_objects(selectedElmt);
									}
									else
									{
										hasChanges = false;
									}
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
										Ext.get(id + '_read').dom.style.display = display ? "" : "none";
									}
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