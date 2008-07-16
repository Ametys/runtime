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
    
	<xsl:template match="/UsersView">
        <html>
            <head>
                <title>
                    <i18n:text i18n:key="PLUGINS_CORE_USERS_TITLE"/>
                </title>
            </head>
            <body>
				<table id="fulltable" cellspacing="0" cellpadding="0" style="display: none;">
                    <colgroup>
                        <col width="231px"/>
                        <col/>
                    </colgroup>
					<tr> 
						<td id="actionset"/>
						<td>
							<div id="listview"/>
						</td>
					</tr>
				</table>
				
				<script>
					Tools.loadScript (document, getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/js/users/user.js.i18n", function () {RUNTIME_Plugin_Runtime_EditUser.initialize("<xsl:value-of select="$pluginName"/>", <xsl:value-of select="count(Model/*)+count(Model/*[type='password'])"/>);});
				
                    function listener() {}
                </script>
                        
                <xsl:call-template name="categories"/>
    
                <script>
					var slistview = new SListView("listview", null, listener);
					slistview.setView("detail");
					slistview.setMultipleSelection(false);
					
					slistview.addColumn (null, "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_NAME"/>", null, STools.is_ie ? "190px" : "175px", null);
					slistview.addColumn ("id", "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_ID"/>", null, STools.is_ie ? "100px" : "85px", null);
					slistview.addColumn ("email", "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_EMAIL"/>", null, STools.is_ie ? "160px" : "145px", null);
					
					slistview.paint();
					
					function addElement(firstname, lastname, login, email)
					{
						var name = lastname + (firstname == null || firstname == '' ? '' : (' ' + firstname));
						var elt = slistview.addElement (name, 
												getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/icon_small.gif", "/img/users/icon_medium.gif", "/img/users/icon_large.gif", 
												{
													"id": login,
													"email": email
												});
						return elt;
					}
                    <xsl:if test="Model/@Modifiable = 'true'">
					function updateElement(elt, firstname, lastname, email)
					{
						elt.name = lastname + (firstname == null || firstname == '' ? '' : (' ' + firstname));
						elt.properties.email = email;
					}
                    </xsl:if>

                    sfulltable_onresize = function (fulltable)
                    {
                        var div = document.getElementById("listview");
                        div.style.width = fulltable._width - 231;
                        div.style.height = fulltable._height;
                        
                        var table = document.getElementById("fulltable");
                        table.style.width = fulltable._width;
                        table.style.height = fulltable._height;
                        table.style.display = "";
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

    <xsl:template name="categories">
        <script>
            var sp = new SContextualPanel("actionset");  
        </script>
        
        <xsl:call-template name="search-category"/>
        <xsl:call-template name="action-category"/>
        <xsl:call-template name="help-category"/>
        
        <script>
            sp.paint();
        </script>

        <xsl:call-template name="search-category-after"/>
        <xsl:call-template name="action-category-after"/>
        <xsl:call-template name="help-category-after"/>
    </xsl:template>

    <xsl:template name="search-category">
        <script>
            var search = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH"/>");
            search.addElement("");
            
            <xsl:if test="AdministratorUI = 'true'">
                function goBack()
                {
                    document.location.href = context.workspaceContext;
                }
                
                search.addElement("&#160;");
                search.addLink("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>",getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/quit.gif", goBack);
            </xsl:if>
        </script>
    </xsl:template>                        

    <xsl:template name="search-category-after">
        <xsl:call-template name="search-category-after-html"/>
        <xsl:call-template name="search-category-after-script"/>
    </xsl:template>
    
    <xsl:template name="search-category-after-html">
        <table id="search-table" style="border-collapse: collapse; width: 181px;">
            <tr>
                <td style="font-size: 11px; color: #000000;">
                    <i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>
                </td>
            </tr>
            <tr>
                <td><input type="text" id="searchCrit" name="search" style="border: 1px solid #7f9db9; width: 181px; font-size: 11px;"/></td>
            </tr>
            <tr>
                <td style="text-align: center; padding-top: 10px">
                    <button onclick="users_search(); return false;" style="border: 1px solid #cdcdcd; text-align: center; font-size: 11px;"><i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_BUTTON"/></button>
                </td>
            </tr>
        </table>
    </xsl:template>
        
    <xsl:template name="search-category-after-script">
        <script>
            search.table.rows[1].cells[0].innerHTML = "";
            search.table.rows[1].cells[0].appendChild(document.getElementById("search-table"));

            function users_search()
            {
                // Effacer tout
                while (slistview.getElements().length &gt; 0)
                    slistview.getElements()[0].remove();

                // Relancer la recherche
                var searchValue = document.getElementById('searchCrit').value;
                
                var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/users/search.xml";
                var arg = "criteria=" + encodeURIComponent(searchValue);
                
                var result = Tools.postFromUrl(url, arg);
                if (result == null)
                {
                    alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_ERROR"/>")
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
                    alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_NORESULT"/>")
                    return;
                }
                slistview.paint();
            }
        </script>
    </xsl:template>
    
    <xsl:template name="action-category">
        <xsl:if test="Model/@Modifiable = 'true'">
            <script>
                function userNew()
                {
                    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "new"});
                }
                
                function userEdit()
                {
                    var elt = slistview.getSelection()[0];
                    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "edit", "login" : elt.properties.id});
                }
                
                function userDelete()
                {
                    if (!confirm("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_PROMPT"/>"))
                        return;
                
                    var elt = slistview.getSelection()[0];
                    
                    var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/users/delete";
                    var args = "login=" + encodeURIComponent(elt.properties.id);
                    
                    if (200 != Tools.postUrlStatusCode(url, args))
                    {
                        alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_DELETE_ERROR"/>");
                        return;
                    }
                    
                    elt.remove();
                }
    
                var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE"/>")
                    handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/add_user.gif", userNew);
                    handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_EDIT"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/icon_small.gif", userEdit);
                    handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/users/delete.gif", userDelete);
            </script>
        </xsl:if>
    </xsl:template>

    <xsl:template name="action-category-after">
        <xsl:if test="Model/@Modifiable = 'true'">
            <script>
                handle.showHideElement(1, false);
                handle.showHideElement(2, false);

                listener.onSelect = function(element)
                {
                    handle.showHideElement(1, true);
                    handle.showHideElement(2, true);
                return true;
                }
                listener.onUnselect = function(element)
                {
                    handle.showHideElement(1, false);
                    handle.showHideElement(2, false);
                }
            </script>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="help-category">
        <script>
            var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP"/>");
            <xsl:choose>
                <xsl:when test="Model/@Modifiable = 'true'">
                    help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP_TEXT_MODIFY"/>&lt;/div&gt;");
                </xsl:when>
                <xsl:otherwise>
                    help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP_TEXT_READ"/>&lt;/div&gt;");
                </xsl:otherwise>
            </xsl:choose>
        </script>
    </xsl:template>
        
    <xsl:template name="help-category-after">
    </xsl:template>
        
</xsl:stylesheet>