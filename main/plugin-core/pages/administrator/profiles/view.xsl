<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2012 Anyware Services

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
	            <link rel="stylesheet" href="{$resourcesPath}/css/administrator/profiles.css" type="text/css"/>
            </head>
            
            <script>
				<script type="text/javascript" src="{$resourcesPath}/js/Ametys/plugins/core/administration/Profiles.i18n.js"/>
				<script type="text/javascript">
				        Ametys.plugins.core.administration.Profiles.initialize ("<xsl:value-of select="$pluginName"/>");
				
				        // Existing profiles
				        var profileData = [
                            <xsl:for-each select="profiles/profile">
    				            <xsl:if test="position() != 1"><xsl:text>, </xsl:text></xsl:if>
    				            <xsl:text/>
    				            { id: '<xsl:value-of select="@id" />', name: '<xsl:value-of select="label" />', rights: {<xsl:text/>
    				                <xsl:for-each select="rights/right">
                                        <xsl:if test="position() != 1"><xsl:text>, </xsl:text></xsl:if>
    				                    <xsl:text/>'<xsl:value-of select="@id"/>': ""
    				                </xsl:for-each>
    				            <xsl:text>} }</xsl:text>
    				        </xsl:for-each>
				        ];
				        
						var RIGHTS_ID = [];
						var RIGHTS_CATEGORY = [];
						
						var readItems = [];
                        var editItems = [];
                        
						<xsl:for-each select="rights/right[not(category/@id = preceding-sibling::right/category/@id)]">
							<xsl:variable name="category" select="category/@id"/>
							<xsl:variable name="categoryKey" select="category"/>
							
							RIGHTS_CATEGORY.push('<xsl:value-of select="$category"/>');
							
							// EDIT ITEM
							var cat_<xsl:value-of select="$category"/> = new Ext.form.FieldSet({
									title : "<xsl:copy-of select="$categoryKey/*"/>",
									layout: 'form',
									
 	                                collapsible: true,
	                                titleCollapse: true,
	                                hideCollapseTool: true,
	                                
	                                border: false,
	                                shadow: false,
                                
									id: "cat_<xsl:value-of select="$category"/>_edit",
									cls : "ametys-tbar-fieldset",
									
									listeners: {'afterrender': function() {
									   this.legend.add(this.items.get(0));
									}},
									
									items: [
									   new Ext.Container({items: [
									           new Ext.Button({
			                                        icon: '<xsl:value-of select="$resourcesPath"/>/img/administrator/profiles/select_16.png',
			                                        tooltip: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_CATEGORY_SELECT_ALL"/>',
			                                        border: false,
			                                        handler: function (){ Ametys.plugins.core.administration.Profiles.selectAll ('cat_<xsl:value-of select="$category"/>_edit'); }
			                                    }), 
			                                    new Ext.Button({
			                                        icon: '<xsl:value-of select="$resourcesPath"/>/img/administrator/profiles/unselect_16.png',
			                                        tooltip: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_CATEGORY_UNSELECT_ALL"/>',
                                                    border: false,
			                                        handler: function (){ Ametys.plugins.core.administration.Profiles.unselectAll ('cat_<xsl:value-of select="$category"/>_edit'); }
			                                    })
									   ]})
									]
							});
							editItems.push(cat_<xsl:value-of select="$category"/>);	
							
							// READ ITEM
							var cat_<xsl:value-of select="$category"/>_read = new Ext.form.FieldSet({
								title : "<xsl:copy-of select="$categoryKey/*"/>",
					            collapsible: true,
					            titleCollapse: true,
					            hideCollapseTool: true,
					            
					            border: false,
					            shadow: false,
								id: "cat_<xsl:value-of select="$category"/>_read",
								cls: 'fieldset'
							});
							readItems.push(cat_<xsl:value-of select="$category"/>_read);
							
							<xsl:for-each select="../right[category/@id = $category]">
								var input = new Ametys.plugins.core.administration.Profiles.CheckRightEntry ({
									listeners: {'change': Ext.bind(Ametys.plugins.core.administration.Profiles.needSave, Ametys.plugins.core.administration.Profiles)},
									width: 175,
									boxLabel : "<xsl:copy-of select="label/*"/>",
							        name: "<xsl:value-of select="@id"/>",
							        id: "<xsl:value-of select="@id"/>",
							        description: "<xsl:copy-of select="description/*"/>",
							        category: "<xsl:value-of select="$category"/>",
							        hideLabel : true,
							        disabled: true
								});	
								RIGHTS_ID.push("<xsl:value-of select="@id"/>");	
								cat_<xsl:value-of select="$category"/>.add(input);
								
								var profileText = new Ametys.plugins.core.administration.Profiles.RightEntry({
									id : "<xsl:value-of select="@id"/>_read", 
									width: 180,
									text : "<xsl:copy-of select="label/*"/>", 
									description: "<xsl:copy-of select="description/*"/>"
								});
								cat_<xsl:value-of select="$category"/>_read.add(profileText);
							</xsl:for-each>
							cat_<xsl:value-of select="$category"/>_read.hide();
						</xsl:for-each> 
						
						createPanel = function () 
						{
							return Ametys.plugins.core.administration.Profiles.createPanel(readItems, editItems);
						}								
                </script>
	        </script>
	           
	        <body>
            </body>
        </html>
    </xsl:template>
    
</xsl:stylesheet>