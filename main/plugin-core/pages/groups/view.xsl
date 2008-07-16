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
        
    <xsl:template match="/GroupsManager">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/></title>
            </head>
            <body>
                <table id="fulltable" cellspacing="0" cellpadding="0" style="display: none">
                    <colgroup>
                        <col width="231px"/>
                        <col/>
                    </colgroup>
                    <tr>
                        <td id="action-panel"/>
                        <td>
                            <div id="listview"/>
                        </td>
                    </tr>
                    <tr>
                        <td id="action-panel2" style="height: 180px; overflow: auto;"/>
                         <td style="border-top: 1px solid threedlight; background-color: threedface; vertical-align: top; text-align: center; padding-top: 10px; padding-left: 15px; padding-right: 15px; padding-bottom: 0px;">
                                 <xsl:call-template name="bottom-list"/>
                        </td>
                    </tr>
                </table>
                <script>
                    <xsl:if test="AdministratorUI = 'true'">
                    	function goBack()
    					{
    						document.location.href = context.workspaceContext;
    					}
                    </xsl:if>
					
                    <xsl:if test="Modifiable = 'true'">
                		// Ajoute une alerte sur les menus
    					var f = function (func) {
    						slistview.unselect();
    					}
    					
    					try
    					{
    						onMenuListener.push(f);
    					}
    					catch (e){};
                    
    					/* ************************************
    					   *  ACTION
    					   ************************************ */
    					function menu_new ()
    					{
    						var elt = slistview.addElement("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEWGROUP"/>", 
    											getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_small.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_medium.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_large.gif",
    											{id : "new"});
    						slistview.unselect();
    						slistview.paint();
    						elt.select();
    						window.setTimeout (function () {elt.rename();}, 10);
    					}
    					function menu_rename ()
    					{
    						slistview.getSelection()[0].rename();
    					}
    					function menu_remove ()
    					{
    						if (confirm("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_CONFIRM"/>"))
    						{
    							if (200 == Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/delete", "id=" + slistview.getSelection()[0].properties.id))
    							{
    								slistview.removing = true;
    								slistview.getSelection()[0].remove();
    							}
    							else
    								alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_ERROR"/>")
    						}
    					}
                    </xsl:if>
				
					/* ************************************
					   *  MENU DE GAUCHE
					   ************************************ */
					var sactionpanel = new SContextualPanel ("action-panel");
                    <xsl:if test="Modifiable = 'true' or AdministratorUI = 'true'">
					   var _Category = sactionpanel.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CATEGORY"/>");
                    </xsl:if>
                    <xsl:if test="Modifiable = 'true'">
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CREATE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/new.gif", menu_new);
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_RENAME"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/rename.gif", menu_rename);
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/delete.gif", menu_remove);
                    </xsl:if>
					
					<xsl:if test="AdministratorUI = 'true'">
							_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_QUIT"/>",getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/quit.gif", goBack);
					</xsl:if>
						
					var aideCategory = sactionpanel.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HELP_CATEGORY"/>");
						aideCategory.addElement("&lt;div style='font-size: 11px; color: #000000; text-align: left;'&gt;<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_HELP_HINT"/>&lt;/div&gt;")
					sactionpanel.paint();

  					var sactionpanel2 = new SContextualPanel ("action-panel2");
                    <xsl:if test="Modifiable = 'true'">
    					var _CategoryG = sactionpanel2.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CATEGORY"/>");
    						_CategoryG.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_ADDUSER"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/add_user.gif", add_user);
    						_CategoryG.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETEUSER"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/delete.gif", delete_user);
    						_CategoryG.addLink("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_VALIDATE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/valid.gif", save_objects);
                    </xsl:if>
    					sactionpanel2.paint();
                    <xsl:if test="Modifiable = 'true'">
    					_CategoryG.showHide(false);
                    </xsl:if>

					/* ************************************
					   *  VUE ICONE
					   ************************************ */
					var all = {                 
                        <xsl:for-each select="users/user">
                            <xsl:if test="position() != 1">, </xsl:if>
                            "<xsl:value-of select="@login"/>": "<xsl:if test="firstname"><xsl:value-of select="firstname"/>&#160;</xsl:if><xsl:value-of select="lastname"/> (<xsl:value-of select="@login"/>)"
				        </xsl:for-each>
                    };
					   
					function listener()
					{
					}
					listener.onSelect = function(element)
					{
                        <xsl:if test="Modifiable = 'true'">
    						_Category.showHideElement (1, true);
    						_Category.showHideElement (2, true);
    						_CategoryG.showHide(true);
    						_CategoryG.showHideElement (1, false);
    						_CategoryG.showHideElement (2, false);
                        </xsl:if>

						for (var i in all)
						{
							if (element.properties[i] != null)
							{
								slistview_right.addElement(all[i], getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/icon_small.gif", "", "", {"id": i});
							}
							
							//document.getElementById('doubleliste_right').style.backgroundColor = "";
						}

						slistview_right.paint();
						
						return true;
					}
					listener.onUnselect = function(element)
					{
                        <xsl:if test="Modifiable = 'true'">
						if (element &amp;&amp; slistview.removing != true)
						{
							var savedState = true;
							
							// Compte les objets alloués et vérifie s'ils étaient déjà affectés
							var nbItems = 0;
							var elts = slistview_right.getElements();
							for (var i=0; savedState &amp;&amp; i &lt; elts.length; i++)
							{
								var objectId = elts[i].properties.id;
								
								if (element.properties[objectId] == null)
									savedState = false;
								else
									nbItems++;
							}	
							
							// Si tous les objets étaient déjà affectés, peut-être en a t-on retiré! 
							// =&gt; on vérifie qu'il y en ait le même nombre
							if (savedState)
							{
								var nbProperties = -1; /* -1 pq id ne compte pas comme droit */
								for (var i in element.properties)
									nbProperties ++;
								savedState = (nbItems == nbProperties)
							}																
							
							if (!savedState)
							{
								if (confirm("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_CONFIRM"/>"))
								{
									save_objects();
								}
							}
						}
						
    						_Category.showHideElement (1, false);
    						_Category.showHideElement (2, false);
    						_CategoryG.showHide(false);
                        </xsl:if>
    						
						slistview_right.elements = new Array();
						slistview_right.selection = new Array();
						slistview_right.paint();
						// document.getElementById('doubleliste_right').style.backgroundColor = "threedface";
						
						slistview.removing = false;
					}
                    <xsl:if test="Modifiable = 'true'">
					listener.onCancelEditLabel = function(element)
					{
						if (element.properties.id == "new")
							element.remove();
					}
					listener.onEditLabel = function(element)
					{
						if (element.properties.id == "new")
						{
							// CREER
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/create", "name=" + encodeURIComponent(element.name));
							if (result == null)
							{
								alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEW_ERROR"/>")
								listener.onCancelEditLabel(element);
								return;
							}
							else
							{
								element.properties.id = Tools.getFromXML(result, "id");
							}
						}
						else
						{
							// RENOMMER
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/rename", "id=" + element.properties.id + "&amp;name=" + encodeURIComponent(element.name));
							if (result == null)
							{
								alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_ERROR"/>");
							}
							else
							{
								var state = Tools.getFromXML(result, "message"); 
								if (state != null &amp;&amp; state == "missing")
								{
									alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_MISSING_ERROR"/>");
									element.remove();
								}
							}
						}
						
						element.unselect();
						slistview.paint();
						element.select();
					}
                    </xsl:if>

					var slistview = new SListView ("listview", null, listener);
					slistview.setMultipleSelection(false);
					
                    <xsl:for-each select="groups/group">
                        slistview.addElement("<xsl:value-of select="label"/>", 
                                getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_small.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_medium.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/groups/icon_large.gif",
                                {
                                    id: "<xsl:value-of select="@id"/>"
                                    <xsl:for-each select="users/user">
                                        , "<xsl:value-of select="."/>": ""
                                    </xsl:for-each>
                                });
                    </xsl:for-each>
					
					slistview.setView("icon");
					slistview.paint();
					
					listener.onUnselect();
					
                    <xsl:if test="Modifiable = 'true'">
					function save_objects()
					{
						var element = slistview.getSelection()[0];
						
						// Met à jour le noeud ! et liste les droits à envoyer
						var objects = "";
						var newProperties = {};
						newProperties.id = element.properties.id;
						
						var elts = slistview_right.getElements();
						for (var i=0; i &lt; elts.length; i++)
						{
							var objectId = elts[i].properties.id;
							newProperties[objectId] = "";
							objects += objectId + '/';
						}
						element.properties = newProperties;
						
						// Envoie ça sur le serveur
						var ok = false;
						while (!ok)
						{
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/groups/modify", "id=" + element.properties.id + "&amp;objects=" + objects);
							if (result == null)
							{
								if (!confirm("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_ERROR"/>"))
								{
									ok = true;
								}
							}
							else 
							{
								var state = Tools.getFromXML(result, "message"); 
								if (state != null &amp;&amp; state == "missing")
								{
									alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_MISSING_ERROR"/>");
									if (slistview.removing == false)
									element.remove();
								}
								else
								{
									ok = true;
									_CategoryG.showHideElement (2, false);
								}
							}
							
						}
					}
					
					function add_user()
					{
						function cb (users)
						{
							function seek (arr, id)
							{
								for (var i=0; i&lt;arr.length; i++)
								{
									if (arr[i].properties.id == id)
										return arr[i];
								}
								return null;
							}
						
							var selectedElements = new Array();
							var existingElements = slistview_right.getElements();
							
							for (var i in users)
							{
								var e = seek(existingElements, i);
								
								if (e == null)
									e = slistview_right.addElement(users[i], getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/icon_small.gif", getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/resources/img/users/icon_medium.gif", getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/resources/img/users/icon_large.gif", {"id": i, "type": "user"});
								selectedElements.push(e);
							}
							
							slistview_right.paint();
							slistview_right.unselect();
							for (var i=0; i &lt; selectedElements.length; i++)
							{
								selectedElements[i].select(true);
							}
							_CategoryG.showHideElement (2, true);
						}
						RUNTIME_Plugin_Runtime_SelectUser.act(cb);
					}
					
					function delete_user()
					{
						var s = slistview_right.getSelection()
						for (var i = 0; i &lt; s.length; i++)
						{
							s[i].remove();
						}
						_CategoryG.showHideElement (2, true);
					}
                    </xsl:if>
				</script>
                
                <script>
                    sfulltable_onresize = function (fulltable)
                    {
                        var div = document.getElementById("listview");
                        div.style.width = fulltable._width - 230;
                        div.style.height = fulltable._height - 181;
                        
                        var table = document.getElementById("fulltable");
                        table.style.width = fulltable._width;
                        table.style.height = fulltable._height;
                        table.style.display = "";
                        
                        var bottomList = document.getElementById("bottom-list");
                        bottomList.style.width = fulltable._width - 230 - 2*15;

                        var listviewRight = document.getElementById("list-view-right");
                        listviewRight.style.width = bottomList.style.width;
                    }                    
                    sfulltable_create = function()
                    {
                        var minSize = {width: 720, height: 450};
                        var maxSize = null;
                        var sfulltable = new SFullTable("fulltable", minSize, maxSize);
                        sfulltable.setEventListener("onresize", sfulltable_onresize);
                        sfulltable_onresize(sfulltable);
                    }
                    window.setTimeout("sfulltable_create();", 1);
                </script>
            </body>
        </html>
    </xsl:template>
	
    <xsl:template name="bottom-list">
        <script>
  		var slistview_right;
		</script>
        <table id="bottom-list" cellpadding="0" cellspacing="0" style="height: 170px;">
        	<tr>
        		<td style="font-size: 11px">
                	<i18n:text i18n:key="PLUGINS_CORE_GROUPS_COLUMN"/>
        		</td>
        	</tr>
            <tr>
                <td>
                    <div id="list-view-right" style="height: 154px"/>
                </td>
            </tr>
        </table>
        
		<script src="{$contextPath}/plugins/{$pluginName}/resources/js/users/selectuser.js.i18n"><xsl:comment>empty</xsl:comment></script>
    	<script>
			RUNTIME_Plugin_Runtime_SelectUser.initialize("<xsl:value-of select="$pluginName"/>");

    		var slistview_right_listener = {};
    		slistview_right_listener.onSelect = function (elt)
    		{
				_CategoryG.showHideElement (1, true);
    			return true;
    		}
    		slistview_right_listener.onUnselect = function (elt)
    		{
				_CategoryG.showHideElement (1, false);
    		}
    	
	  		slistview_right = new SListView ("list-view-right", null, slistview_right_listener);
	
	  		slistview_right.addColumn (null, "", null, "200px", null);
	  		slistview_right.showHeaders(false);
	
	  		slistview_right.setView("list");
	
	  		slistview_right.paint();
    	</script>
    </xsl:template>
</xsl:stylesheet>