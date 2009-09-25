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
    
	<xsl:template match="/UsersView">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_USERS_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/users/view.css" type="text/css"/>
            </head>
            
            <script>
            	<script type="text/javascript" src="{$resourcesPath}/js/users/user.i18n.js"><xsl:comment>//empty</xsl:comment></script>
            	<script type="text/javascript">
            			RUNTIME_Plugin_Runtime_EditUser.initialize("<xsl:value-of select="$pluginName"/>", <xsl:value-of select="count(Model/*)+count(Model/*[type='password'])"/>);
            			
	               		function goBack()
	                    {
	                        document.location.href = context.workspaceContext;
	                    }
	                   
	                  	var formInputs = [];
	                  	<xsl:for-each select="Model/node()">
								var input;
								var type = "<xsl:value-of select="type"/>";
								if (type == 'string')
								{
									input = new org.ametys.form.TextField ({
								        fieldLabel: "<i18n:text i18n:key="{label}"/>",
								        desc: "<i18n:text i18n:key="{description}"/>",
								        name: "field_<xsl:value-of select="local-name()"/>",
								        width: 200
									});
								}
								else if (type == 'password')
								{
									input = new org.ametys.form.PasswordField ({
								        fieldLabel: "<i18n:text i18n:key="{label}"/>",
								        desc: "<i18n:text i18n:key="{description}"/>",
								        name: "field_<xsl:value-of select="local-name()"/>",
								        inputType:"password",
								        width: 200
									});
								}
								else if (type == 'date')
								{
									input = new org.ametys.form.DateField ({
								        fieldLabel: "<i18n:text i18n:key="{label}"/>",
								        desc: "<i18n:text i18n:key="{description}"/>",
								        name: "field_<xsl:value-of select="local-name()"/>",
								        width: 200
									});
								}
								else if (type == 'boolean')
								{
									input = new org.ametys.form.BooleanField ({
								        fieldLabel: "<i18n:text i18n:key="{label}"/>",
								        desc: "<i18n:text i18n:key="{description}"/>",
								        name: "field_<xsl:value-of select="local-name()"/>",
								        width: 200
									});
								}
								else
								{
									input = new org.ametys.form.TextField ({
								        fieldLabel: "<i18n:text i18n:key="{label}"/>",
								        desc: "<i18n:text i18n:key="{description}"/>",
								        name: "field_<xsl:value-of select="local-name()"/>",
								        width: 180
									});
								}	
								formInputs.push(input);																								
						</xsl:for-each>		
	                  	
						function userNew()
		                {
		                    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "new"});
		                }
		                
		                function userEdit()
		                {
		                    var elt = listView.getSelection()[0];
		                    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "edit", "login" : elt.data.login});
		                }
		                
		                function userDelete()
	                    {
	                    	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_PROMPT"/>", user_delete_confirm);
	                    }
	                    function user_delete_confirm (button)
	                    {
	                    	if (button == 'yes')
	                    	{
			                    var elt = listView.getSelection()[0];              
			                    var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/users/delete";
			                    var args = "login=" + encodeURIComponent(elt.data.login);
			                    
			                    if (200 != Tools.postUrlStatusCode(url, args))
			                    {
			                    	Ext.Msg.show ({
			                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
			                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_DELETE_ERROR"/>",
			                        		buttons: Ext.Msg.OK,
						   					icon: Ext.MessageBox.ERROR
			                        });
			                        return;
			                    }
			                    
			                    listView.removeElement(elt);    
			                }                
						}
						
						function addElement(firstname, lastname, login, email)
						{
							listView.addElement(login, {
									'login': login,
									'display': firstname + " " + lastname + " (" + login + ")",
									'firstname': firstname,
									'lastname': lastname,
									'email': email 
								});	
						}
						
						function updateElement (element, login, firstname, lastname, email)
						{
							element.set('firstname', firstname);
							element.set('lastname', lastname);
							element.set('email', email);
							element.set('display', firstname + ' ' + lastname + ' (' + login + ')');
						}

						function users_search ()
		                {
		                	// Effacer tout
		                	listView.getStore().removeAll();
               				
               				<xsl:if test="Model/@Modifiable = 'true'">
               					handle.hideElt(2);
								handle.hideElt(3);
							</xsl:if>
								
               				//Lancer la recherche
		                	var searchValue = searchForm.getForm().findField("searchField").getValue();
			                
			                var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/users/search.xml";
			                var arg = "criteria=" + encodeURIComponent(searchValue);
			                
			                var result = Tools.postFromUrl(url, arg);
			                if (result == null)
			                {
			                	Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_ERROR"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        });
			                    return;
			                }
			
			                // Afficher les resultats
			                var nodes = result.selectNodes("/Search/users/user");
			                for (var i = 0; i &lt; nodes.length; i++)
			                {
			                    var firstnameNode = nodes[i].selectSingleNode("firstname");
			                    var firstname = firstnameNode != null ? firstnameNode[Tools.xmlTextContent] : "";
			                    
			                    var lastname = nodes[i].selectSingleNode("lastname")[Tools.xmlTextContent];
			                    var login = nodes[i].getAttribute("login");
			                    var email = nodes[i].selectSingleNode("email")[Tools.xmlTextContent];
			                    
			                    addElement(firstname, lastname, login, email);
			                }
			                
			                if (nodes.length == 0)
			                {
			                	Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_NORESULT"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.INFO
		                        });
			                    return;
			                }
						}
											
            			//Recherche
						var search = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH"/>'});

						var searchField = new Ext.form.TextField ({
							hideLabel: true,
					        name: 'searchField',
					        anchor:'100%',
					        value: 'Nom ou identifiant',
					        onFocus : function () {this.setValue('');}
						});
					
						var searchForm = new Ext.form.FormPanel({
					        items: [searchField],
					       	baseCls: 'search',
					       	buttonAlign: 'right',
					        buttons: [{
					            text: '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_BUTTON"/>',
								handler : users_search	            
					        }]
    					});					        
				    	
						search.add(searchForm);
						search.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/users/quit.png", goBack);
				    
				    	<xsl:if test="Model/@Modifiable = 'true'">
							// Actions
							var handle = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE"/>'});
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>", "<xsl:value-of select="$resourcesPath"/>/img/users/add_user.png", userNew);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_EDIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/users/edit_user.png", userEdit);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL"/>", "<xsl:value-of select="$resourcesPath"/>/img/users/delete.png", userDelete);
							
							handle.hideElt(2);
							handle.hideElt(3);
						</xsl:if>
										
						// Aide
						var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP"/>'});
						help.addText("<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP_TEXT_READ"/>");
					
						var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									cls: 'admin-right-panel',
									width: 277,
								    items: [search, <xsl:if test="Model/@Modifiable = 'true'">handle, </xsl:if> help]
						});
							
						var store = new Ext.data.SimpleStore({
									id:0,
							        fields: [
							           {name: 'login'},
							           {name: 'display'},
							           {name: 'firstname'},
							           {name: 'lastname'},
							           {name: 'email'}
							        ]});
							        
						function onSelectRow (listview, rowindex, e)
						{
							handle.showElt(2);
							handle.showElt(3);
						}
						
						function onUnselectRow (e)
						{
							handle.hideElt(2);
							handle.hideElt(3);
						}
					     
						// Grid
						var listView = new org.ametys.ListView({
							<xsl:if test="Model/@Modifiable = 'true'">listeners: {'rowclick': onSelectRow, 'mouseup' : onUnselectRow},</xsl:if>
							baseCls : 'user-list',
							viewConfig: {
						        forceFit: true
						    },
						    store : store,
						    columns: [
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_NAME"/>", width : 200, menuDisabled : true, sortable: true, dataIndex: 'display'},
						        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_EMAIL"/>", width : 240, menuDisabled : true, sortable: true, dataIndex: 'email'}
						    ],
							id: 'detailView',
							region: 'center'
						});							

						org.ametys.runtime.administrator.Panel.createPanel = function () 
						{
							return new Ext.Panel({
								region: 'center',
								baseCls: 'transparent-panel',
								border: false,
								layout: 'border',
								autoScroll: true,
								items: [listView, rightPanel]
							});
						}
            	</script>
            </script>
            <body>
				
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>