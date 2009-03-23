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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/plugins.xsl"/>
    <xsl:import href="core/template.xsl"/>
    
    <xsl:param name="workspaceName"/>
	
    <xsl:template name="workspace-title"/>
    
    <xsl:template name="workspace-head">
	   <LINK rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/homepage.css" type="text/css"/>
	   
	   <xsl:call-template name="plugins-load">
            <xsl:with-param name="scripts" select="/Admin/Desktop/category/UIItem/Action/Imports/Import"/>
            <xsl:with-param name="actions" select="/Admin/Desktop/category/UIItem/Action/ClassName"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="workspace-script">
    	<script type="text/javascript">
    		function workspaceBody () 
    		{
	    		var items = []
	    		
	    		var links = new Ext.ametys.HtmlContainer ({
	    			contentEl : 'links'
	    		});
	    		items.push(links);
				<xsl:for-each select="/Admin/Desktop/category">
						var category = new Ext.ametys.DesktopCategory ({
								text: "<i18n:text i18n:key="{@name}" i18n:catalogue="application"/>"
						});
						items.push(category);
						
						<xsl:for-each select="UIItem">
							var item = new Ext.ametys.DesktopItem ({
								text: "<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Label"/></xsl:call-template>",
								desc: "<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Description"/></xsl:call-template>",
								icon: "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Large"/>",
								iconOver: "<xsl:value-of select="$contextPath"/><xsl:value-of select="substring-before(Icons/Large, '.')"/>_over.<xsl:value-of select="substring-after(Icons/Large, '.')"/>"
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
							items.push(item);
						</xsl:for-each>
				</xsl:for-each>
				
				return new Ext.ametys.DesktopPanel({
					items: items,
					baseCls : 'desktop',
					autoScroll: true
				});
			}
		</script>
    </xsl:template>
    
    <xsl:template name="workspace-body">
    	<div id="links">
            <div class="link">
            	<div class="label">
            		<div class="left"><xsl:comment></xsl:comment></div>
            		<div class="text">site web</div>
            	</div>
            	<a href="http://www.ametys.org" target="_blank">ametys.org</a>
            </div>
             <div class="link">
             	<div class="label">
             		<div class="left"><xsl:comment></xsl:comment></div>
            		<div class="text">documentation</div>
            	</div>
            	<a href="http://wiki.ametys.org" target="_blank">wiki.ametys.org</a>
            </div>
        </div>
    </xsl:template>
    
</xsl:stylesheet>
