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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/plugins.xsl"/>
    <xsl:import href="core/template.xsl"/>
    
    <xsl:param name="workspaceName"/>
	
    <xsl:template name="workspace-title"/>
    
    <xsl:template name="workspace-head">
	   <LINK rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/homepage.css" type="text/css"/>
    </xsl:template>

    <xsl:template name="workspace-body">
        <!-- loading plugins -->
        <xsl:call-template name="plugins-load">
            <xsl:with-param name="scripts" select="/Admin/Desktop/category/UIItem/Action/Imports/Import"/>
            <xsl:with-param name="actions" select="/Admin/Desktop/category/UIItem/Action/ClassName"/>
        </xsl:call-template>

        <table class="admin_index_main_table">
            <tr>
                <td id="actionset"/>
                <td>
                    <div id="listview" style="width: 500px"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template name="workspace-script">
        <script>
            var sp = new SContextualPanel("actionset")
            var c1 = sp.addCategory("<i18n:text i18n:key="WORKSPACE_ADMIN_HELP_LABEL" i18n:catalogue="workspace.{$workspaceName}"/>", true); 
                c1.addElement("&lt;div style='color: #000000; text-indent: 20px; font-size: 11px; text-align: left;'&gt;<i18n:text i18n:key="WORKSPACE_ADMIN_HELP" i18n:catalogue="workspace.{$workspaceName}"/>&lt;/div&gt;")

            <xsl:if test="/Admin/Versions/Component">
                var c2 = sp.addCategory("<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_LABEL" i18n:catalogue="workspace.{$workspaceName}"/>", true); 
                <xsl:for-each select="/Admin/Versions/Component">
                        c2.addElement("&lt;span style='font-size: 11px; font-weight: bold'&gt;<xsl:value-of select="Name"/>&#160;:&lt;/span&gt;&#160;<xsl:value-of select="Version"/>");
                        <xsl:if test="Date">
                            c2.addElement("<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATED" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<i18n:date src-pattern="dd/MM/yyyy" pattern="medium" value="{Date}"/>&#160;<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATEDTIME" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<i18n:time src-pattern="HH:mm" pattern="short" value="{Time}"/>&#160;GMT");
                        </xsl:if>
                        <xsl:if test="position() != last()">
                            c2.addElement("&#160;");
                        </xsl:if>
                </xsl:for-each>
            </xsl:if>
            
            sp.paint();
            
            function slistview_listener() {}
            slistview_listener.onSelect = function (element) {
                if (element.properties.actionFunction == null)
                    return false;
                    
                window.setTimeout( function() { element.properties.actionFunction(element.properties.plugin, element.properties.actionParams) }, 10);
                window.setTimeout( function() { slistview.unselect() }, 200);
                return true;
            }
            
            SListView.mosaicViewTrunc = 50;

            var slistview = new SListView("listview", null, slistview_listener);
            slistview.setView("mozaic");
            slistview.sort(false);
            slistview.setGroup("categoryDisplay");
            slistview.showGroups(true);
            slistview.setMultipleSelection(false);
            slistview.addColumn(null, "", true);
            slistview.addColumn("description", "", true);

            <xsl:for-each select="/Admin/Desktop/category">
                <xsl:for-each select="UIItem">
                    slistview.addElement("<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Label"/></xsl:call-template>",
                            "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Small"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Medium"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Large"/>",
                            {
                                "description" : "<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Description"/></xsl:call-template>",
                                "category" : "<xsl:value-of select="../@name"/>",
                                "categoryDisplay" : "<i18n:text i18n:key="{../@name}" i18n:catalogue="application"/>"
                                <xsl:if test="not(@disabled)">
                                    , 
                                    "plugin" : "<xsl:value-of select="Action/@plugin"/>",
                                    "actionFunction" : <xsl:value-of select="Action/ClassName"/>.act,
                                    "actionParams" : {<xsl:for-each select="Action/Parameters/*">
                                                        <xsl:text>"</xsl:text><xsl:value-of select="local-name()"/>" : "<xsl:value-of select="."/><xsl:text>"</xsl:text>
                                                        <xsl:if test="position() != last()">, </xsl:if>
                                                    </xsl:for-each>}
                                </xsl:if>
                            });
                </xsl:for-each>
            </xsl:for-each>

            slistview.paint();
        </script>
    </xsl:template>
    
</xsl:stylesheet>
