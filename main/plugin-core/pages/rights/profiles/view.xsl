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
<xsl:stylesheet version="1.0" xmlns:i18n="http://apache.org/cocoon/i18n/2.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   	
   	<xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    
    <xsl:template match="/ProfilesManager">
        <xsl:param name="initialIcons"/>
        
        <html>
            <head>
                <title>
                    <i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_LABELLONG"/>
                </title>
            </head>
            <body>
                <style>
                    div.SListView table.mosaic {
                        margin: 0px !important;
                    }
                    div.SListView table.detail td table td.selected div {
                        color: #ffffff;
                        padding-right: 5px;
                        background-color: #316ac5;
                    }
                </style>
                <table id="fulltable" cellspacing="0" cellpadding="0" style="display: none;">
                    <colgroup>
                        <col width="231px"/>
                        <col/>
                    </colgroup>
                    <tr>
                        <td id="action-panel" style="overflow: auto;"/>
                        <td>
                            <table id="innertable">
                                <tr>
                                    <td>
                                        <div id="listview"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="height: 235px; border-top: 1px solid threedlight; background-color: threedface; vertical-align: top; text-align: center; padding: 15px; padding-top: 10px; padding-bottom: 0px;">
                                           <xsl:call-template name="double-liste"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <script>
                    SListView.mosaicViewTrunc = 60;
                    SListView.detailViewTrunc = 0.0001;
                    
                    <xsl:if test="AdministratorUI = 'true'">
                    	function goBack()
    					{
    						document.location.href = context.workspaceContext;
    					}
                    </xsl:if>
					
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
						var elt = slistview.addElement("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEWPROFILE"/>", 
											getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_small.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_medium.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_large.gif",
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
						if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_CONFIRM"/>"))
						{
							if (200 == Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/delete", "id=" + slistview.getSelection()[0].properties.id))
							{
								slistview.removing = true;
								slistview.getSelection()[0].remove();
							}
							else
								alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_ERROR"/>")
						}
					}
				
					/* ************************************
					   *  MENU DE GAUCHE
					   ************************************ */
					var sactionpanel = new SContextualPanel ("action-panel");
					var _Category = sactionpanel.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CATEGORY"/>");
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CREATE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") +  "/img/rights/profiles/new.gif", menu_new);
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_RENAME"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/rename.gif", menu_rename);
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_VALIDATE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/valid.gif", save_objects);
						_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/delete.gif", menu_remove);
					
					<xsl:if test="AdministratorUI = 'true'">
							_Category.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_QUIT"/>",getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/quit.gif", goBack);
					</xsl:if>
                    
					var aideCategory = sactionpanel.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HELP_CATEGORY"/>");
						aideCategory.addElement("&lt;div style='color: #000000; text-align: left; font-size: 11px; '&gt;<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_HELP_HINT"/>&lt;/div&gt;")
						
					sactionpanel.paint();

					/* ************************************
					   *  VUE ICONE
					   ************************************ */
					var all = {                 
                        <xsl:for-each select="rights/right">
		                    <xsl:if test="position() != 1">, </xsl:if>
		                    "<xsl:value-of select="@id"/>": {label : "<i18n:text i18n:catalogue="{@catalogue}" i18n:key="{label}"/>", description : "<i18n:text i18n:catalogue="{@catalogue}" i18n:key="{description}"/>", category: "<i18n:text i18n:catalogue="{@catalogue}" i18n:key="{category}"/>"}
						</xsl:for-each>
                    };
                      
					function listener()
					{
					}
					listener.onSelect = function(element)
					{
						_Category.showHideElement (1, true);
						_Category.showHideElement (3, true);

						for (var i in all)
						{
                            var helpImage = getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/help.gif";
                            var label = "&lt;div&gt;&lt;img title=\"" + all[i].description + "\" src='" + helpImage + "'/&gt;&#160;" + all[i].label + "&lt;/div&gt;";
                            
							if (element.properties[i] == null)
							{
								slistview_left.addElement(label, null, null, null, {"id": i, "category": all[i].category}, all[i].label);
							}
							else
							{
								slistview_right.addElement(label, null, null, null, {"id": i, "category": all[i].category}, all[i].label);
							}
						}

						slistview_left.paint();
						slistview_right.paint();
						
						return true;
					}
					listener.onUnselect = function(element)
					{
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
								if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
								{
									save_objects();
								}
							}
						}
						
						_Category.showHideElement (1, false);
						_Category.showHideElement (2, false);
						_Category.showHideElement (3, false);
						
						slistview_left.elements = new Array();
						slistview_left.selection = new Array();
						slistview_left.paint();
						slistview_right.elements = new Array();
						slistview_right.selection = new Array();
						slistview_right.paint();
						// document.getElementById('doubleliste_left').style.backgroundColor = "threedface";
						// document.getElementById('doubleliste_right').style.backgroundColor = "threedface";
						
						slistview.removing = false;
					}
                    listener.onBeforeEditLabel = function(element)
                    {
                        element.oldName = element.name;
                    }
					listener.onCancelEditLabel = function(element)
					{
						if (element.properties.id == "new")
							element.remove();
					}
					listener.onEditLabel = function(element)
					{
                        if (!/^[a-z|A-Z|0-9| |-|_]*$/.test(element.name))
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NAMING_ERROR"/>")
                            element.name = element.oldName;
                            element.paint();
                            return;
                        }
                    
						if (element.properties.id == "new")
						{
							// CREER
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/create", "name=" + encodeURIComponent(element.name));
							if (result == null)
							{
								alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>")
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
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/rename", "id=" + element.properties.id + "&amp;name=" + encodeURIComponent(element.name));
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
									element.remove();
								}
							}
						}
						
						element.unselect();
						slistview.paint();
						element.select();
					}

					var slistview = new SListView ("listview", null, listener);
					slistview.setMultipleSelection(false);
					
					<xsl:for-each select="profiles/profile">
					slistview.addElement("<xsl:value-of select="label"/>", 
							getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_small.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_medium.gif", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/profiles/icon_large.gif",
							{
								id: "<xsl:value-of select="@id"/>"
								<xsl:for-each select="rights/right">
									, "<xsl:value-of select="@id"/>": ""
								</xsl:for-each>
							});
					</xsl:for-each>		
					
					slistview.setView("icon");
					slistview.paint();
					
					listener.onUnselect();
					
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
							var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/profiles/modify", "id=" + element.properties.id + "&amp;objects=" + objects);
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
									if (slistview.removing == false)
									element.remove();
								}
								else
								{
									ok = true;
									_Category.showHideElement (2, false);
								}
							}
							
						}
					}
				</script>
                
                <script>
                    sfulltable_onresize = function (fulltable)
                    {
                        var div = document.getElementById("listview");
                        div.style.width = fulltable._width - 230;
                        div.style.height = fulltable._height - 246;
                        
                        var innertable = document.getElementById("innertable");
                        innertable.style.width = fulltable._width - 230;
                        innertable.style.height = fulltable._height;

                        var table = document.getElementById("fulltable");
                        table.style.width = fulltable._width;
                        table.style.height = fulltable._height;
                        table.style.display = "";
                        
                        var doubleliste = document.getElementById("doubleliste");
                        doubleliste.style.width = fulltable._width - 230 - 2*15;

                        var half = (fulltable._width - 230 - 2*15 - 50) / 2 - 22 + (STools.is_ie ? 0 : - 15);
                        slistview_left.columns = new Array();
                        slistview_left.addColumn (null, "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_LEFTCOLUMN"/>", true, half + "px", null);
            
                        slistview_right.columns = new Array();
                        slistview_right.addColumn (null, "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTCOLUMN"/>", true, half + "px", null);
                        
                        slistview_left.paint();
                        slistview_right.paint();
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
	
    <xsl:template name="double-liste">
    	
        <script>
  		var slistview_left;
  		var slistview_right;

  		function doubleList (lv1, lv2, mode)
  		{
	    	var eltsToTransit;
		    if (mode == 'all')
                          eltsToTransit = lv1.getElements();
            else
                          eltsToTransit = lv1.getSelection();

            var newElements = new Array();

            for (var i=0; i &lt; eltsToTransit.length; i++)
            {
            	_Category.showHideElement (2, true);
              	var elt = eltsToTransit[i];

              	newElements.push (lv2.addElement(elt.name, elt.icon16, elt.icon32, elt.icon50, elt.properties, elt.tooltip));
            }
            for (var i=eltsToTransit.length-1; i &gt;= 0; i--)
            {
              	eltsToTransit[i].remove();
            }

            lv2.unselect();
            lv2.paint();

            for (var i=0; i &lt; newElements.length; i++)
            {
              	newElements[i].select(true);
            }
  		}
		</script>
        <table id="doubleliste" cellpadding="0" cellspacing="0" style="height: 235px; table-layout: fixed">
            <colgroup>
                <col width="50%"/>
                <col width="50px"/>
                <col width="50%"/>
            </colgroup>
            <tr>
                <td style="">
                    <div id="list-view-left" style="height: 235px; overflow: auto"/>
                </td>
                <td style="text-align: center; vertical-align: middle">
                    <img onclick="doubleList(slistview_right, slistview_left, 'all');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/profiles/rewind.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 5px; padding: 0px;" title="Tout passer à gauche"/>
                    <img onclick="doubleList(slistview_right, slistview_left, 'selection');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/profiles/previous.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 5px; padding: 0px;" title="Passer la sélection à gauche"/>
                    <img onclick="doubleList(slistview_left, slistview_right, 'selection');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/profiles/next.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 5px; padding: 0px;" title="Passer la sélection à droite"/>
                    <img onclick="doubleList(slistview_left, slistview_right, 'all');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/profiles/forward.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 5px; padding: 0px;" title="Tout passer à droite"/>
                </td>
                <td style="">
                    <div id="list-view-right" style="height: 235px; overflow: auto"/>
                </td>
            </tr>
        </table>
    	<script>
	  		slistview_left = new SListView ("list-view-left", null, null);
            slistview_left.setGroup("category");
            slistview_left.showGroups(true);
            slistview_left.setView("detail");
            
            slistview_right = new SListView ("list-view-right", null, null);
            slistview_right.setGroup("category");
            slistview_right.showGroups(true);
            slistview_right.setView("detail");
    	</script>
    </xsl:template>
</xsl:stylesheet>