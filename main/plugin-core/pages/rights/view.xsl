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
    <xsl:param name="defaultRightContext"/>
    
    <xsl:template match="/ViewRights">
        <html>
            <head>
                <title>
                    <i18n:text i18n:key="PLUGINS_CORE_RIGHTS_LABELLONG" i18n:catalogue="plugin.{$pluginName}"/>
                </title>
                <LINK type="text/css" href="{$contextPath}/plugins/{$pluginName}/resources/css/rights/rights.css" rel="stylesheet"/>
            </head>
            <body>
                <!-- Boite de selection d'un user -->
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/users/selectuser.js.i18n">// emtpy</script>
                <script>
                    RUNTIME_Plugin_Runtime_SelectUser.initialize("<xsl:value-of select="$pluginName"/>");
                </script>
                
                <!-- Boite de selection d'un groupe -->
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/groups/selectgroup.js.i18n">// emtpy</script>
                <script>
                    RUNTIME_Plugin_Runtime_SelectGroup.initialize("<xsl:value-of select="$pluginName"/>");
                </script>
                
                <!-- Mise à disposition des profils -->
                <script>
                     var all_profiles = {
                        <xsl:for-each select="Profiles/profile">
                            <xsl:if test="position() != 1">, </xsl:if>
                            "<xsl:value-of select="@id"/>": "<xsl:value-of select="label"/>"
                        </xsl:for-each>
                    }
                    
                	function RUNTIME_Plugin_Runtime_Get_UsersAndGroups(context, listview)
                    {
                        var result = Tools.postFromUrl(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/assignment.xml", "context=" + context);
                        if (result == null)
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_GETUSERSANDGROUPS_ERROR" i18n:catalogue="plugin.{$pluginName}"/>");
                            return;
                        }

                        var groupsOrUsers = result.selectNodes("/info/*/*");
                        for (var i=0; i &lt; groupsOrUsers.length; i++)
                        {
                            var properties = {}
                            properties.id = groupsOrUsers[i].getAttribute("id");
                            properties.type = groupsOrUsers[i].tagName;

                            var profiles = groupsOrUsers[i].selectNodes("profiles/profile");
                            for (var j=0; j &lt; profiles.length; j++)
                            {
                                properties[profiles[j].getAttribute("id")] = "";
                            }

                            listview.addElement(groupsOrUsers[i].selectSingleNode("label")[Tools.xmlTextContent], 
                                                "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/" + groupsOrUsers[i].tagName + "s_small.gif", 
                                                "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/" + groupsOrUsers[i].tagName + "s_medium.gif", 
                                                "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/" + groupsOrUsers[i].tagName + "s_large.gif", 
                                                properties);                            
                        }
                        listview.paint();
                    }
                    
                
                    
                    function RUNTIME_Plugin_Runtime_Save_Assignment(listview, list, context)
                    {
                        var groups = ""; // groupes à modifier
                        var users = ""; // utilisateurs à modifier
                        var objects = ""; // profils à affecter
                        
                        var selection = listview.getSelection();
                        
                        for (var i=0; i &lt; selection.length; i++)
                        {
                            var element = selection[i];
                            
                            if (element.properties.type == "group")
                                groups += element.properties.id + "/";
                            else
                                users += element.properties.id + "/";
                            
                            var newProperties = {};
                            newProperties.id = element.properties.id;
                            newProperties.type = element.properties.type;

                            var elts = list.getElements();
                            for (var j=0; j &lt; elts.length; j++)
                            {
                                var objectId = elts[j].properties.id;
                                newProperties[objectId] = "";
                                objects += objectId + "/";
                            }
                            element.properties = newProperties;
                        }
                        
                        if (200 != Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/assign", "groups=" + groups + "&amp;users=" + users + "&amp;profiles=" + objects + "&amp;context=" + context))
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_VALID_ERROR" i18n:catalogue="plugin.{$pluginName}"/>");
                        }
                    }
                    
                    function RUNTIME_Plugin_Runtime_Remove_Assignment(listview, context)
                    {
                        if (!confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_REMOVE_CONFIRM" i18n:catalogue="plugin.{$pluginName}"/>"))
                        {
                            return;
                        }

                        var users = "";
                        var groups = "";
                                                            
                        var selection = listview.getSelection();
                        for (var i=0; i &lt; selection.length; i++)
                        {
                            var element = selection[i];
                            
                            if (element.properties.type == "group")
                                groups += element.properties.id + "/";
                            else
                                users += element.properties.id + "/";
                        }
                        
                        if (200 != Tools.postUrlStatusCode(getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/rights/remove", "groups=" + groups + "&amp;users=" + users + "&amp;context=" + context))
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_REMOVE_ERROR" i18n:catalogue="plugin.{$pluginName}"/>");
                            return;
                        }
                        
                        for (var i=0; i &lt; selection.length; i++)
                        {
                            var element = selection[i];
                            element.remove();
                        }
                    }
                    
                    var onresize = new Array();
                </script>
        
                <table id="fulltable" class="assign-rights" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                        <xsl:if test="count(Context) = 1"><xsl:attribute name="style">display: none;</xsl:attribute></xsl:if>
                    
                        <td class="assign-rights-buttons" height="56px" style="overflow: hidden">
                            
                            <!-- BOUTONS D'EN-TETE -->
                            <table height="50px" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                    <xsl:for-each select="Context">
                                        <td height="50px">
                                        
                                            <!-- BOUTON -->
                                            <table onclick="RUNTIME_Rights_Context_SwitchButton('{@id}'); return false;" class="button_off" cellspacing="0" cellpadding="0" id="RUNTIME_Rights_Context_Button_{@id}">
                                                <tr>
                                                    <td width="50px">
                                                        <img src="{Button/Icon}"/>
                                                    </td>
                                                    <td style="padding-left: 10px; vertical-align: middle;">
                                                        <div class="button_label">
                                                            <xsl:copy-of select="Button/Label/node()"/>
                                                        </div>
                                                        <div class="button_desc">
                                                            <xsl:copy-of select="Button/Description/node()"/>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>    
                                                                                            
                                        </td>
                                    </xsl:for-each>
                                </tr>
                            </table>
                        
                        </td>
                    </tr>
                    <xsl:for-each select="Context">
                        <tr id="RUNTIME_Rights_Context_Row_{@id}" style="display: none;">
                            <td class="assign-rights-inner" style="overflow: hidden">
                                <xsl:copy-of select="Content/node()"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>

                <script>
                    function RUNTIME_Rights_Context_SwitchButton (id)
                    {
                        <xsl:for-each select="Context">
                            document.getElementById('RUNTIME_Rights_Context_Button_<xsl:value-of select="@id"/>').className = (id == '<xsl:value-of select="@id"/>' ? "button_on" : "button_off");
                            document.getElementById('RUNTIME_Rights_Context_Row_<xsl:value-of select="@id"/>').style.display = (id == '<xsl:value-of select="@id"/>' ? "" : "none");
                        </xsl:for-each>
                    }
                </script>
                
                <script>
                    sfulltable_onresize = function (fulltable)
                    {
                        var table = document.getElementById("fulltable");
                        table.style.width = fulltable._width;
                        table.style.height = fulltable._height;
                        
                        for (var i = 0; i &lt; onresize.length; i++)
                        {
                            onresize[i](fulltable._width, fulltable._height - (table.rows[0].style.display == 'none' ? 0 : 56));
                        }

                        <!-- default selected button -->
                        <xsl:choose>
                            <xsl:when test="count(Context) = 1">
                                RUNTIME_Rights_Context_SwitchButton('<xsl:value-of select="Context/@id"/>');
                            </xsl:when>
                            <xsl:when test="$defaultRightContext != ''">
                                RUNTIME_Rights_Context_SwitchButton('<xsl:value-of select="$defaultRightContext"/>');
                            </xsl:when>
                            <xsl:otherwise>
                                // no default selection
                            </xsl:otherwise>
                        </xsl:choose>
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
</xsl:stylesheet>