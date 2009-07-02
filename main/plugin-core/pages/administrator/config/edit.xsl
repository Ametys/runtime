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
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>
    
    <xsl:template match="/">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/config.css" type="text/css"/>
            </head>
            
            <script>
               
               <script type="text/javascript">
               		function goBack()
                    {
                        document.location.href = context.workspaceContext;
                    }
                    
                    function save()
                    {
                    	if (!centerPanel.getForm().isValid())
                    		return;
                        
                        var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/config/set";
                        var args = Tools.buildQueryString(document.getElementById("save-config"), "");

                        var result = Tools.postFromUrl(url, args);
                        if (result == null)
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR"/>");
                            return;
                        }
                        
                        var error = Tools.getFromXML(result, "error");
                        if (error != null &amp;&amp; error != "")
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR"/>" + error)
                            return;
                        }
                        
                        alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK"/>");
                        goBack();
                    }
                    
                   	function bindScroll()
					{
						bound = true;
					}
					function unbindScroll()
					{
						bound = false;
					}
					function calcScrollPosition()
					{
						if (!bound)
							return;
							 
						var last;
						var anchors = ct.select('a[name]', true);
						var min = 0;
						var max = centerPanel.getEl().child('div:first').dom.scrollHeight - centerPanel.getInnerHeight();
						var scrollPosition = centerPanel.getEl().child('div:first').dom.scrollTop;
						var p = (scrollPosition - min) / (max - min);
						p = p * centerPanel.getInnerHeight();
						
						var a0 = anchors.elements[0].getTop();
						
						for (var i=0;  i &lt; anchors.elements.length; i++)
						{
							var anchor = anchors.elements[i];
							if (i > 0) {
								last = anchors.elements[i-1];
							}
							else {
								last = anchor;
							}
							var posY = anchor.getTop() - a0;
							if(posY >= scrollPosition + p)
							{
								activateItemMenu(last.dom.name);
								return;
							}
						
						}
						activateItemMenu(anchors.elements[anchors.elements.length - 1].dom.name);
					}
					function activateItemMenu (id)
					{
						var btn = Ext.getCmp("a" + id);
						if	(btn != null)
						{	
							Ext.getCmp("a" + id).toggle(true);
						}
					}
					
					// NAVIGATION
						var navigation = new Ext.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
						<xsl:for-each select="config/categories/category">
							var item = new Ext.ametys.NavigationItem ({
								text: "<xsl:copy-of select="label/node()"/>",
								divToScroll: "<xsl:value-of select="generate-id()"/>",
								ctToScroll:  'config-inner',
								bindScroll: bindScroll,
								unbindScroll: unbindScroll,
								toggleGroup : 'config-menu',
								id : "a" + "<xsl:value-of select="generate-id()"/>"
							});
							navigation.add(item);
						</xsl:for-each>
						
						// ACTIONS
						var handle = new Ext.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE"/>"});
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/config/save.png", save);
						handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/config/quit.png", goBack);
	
						// AIDE
						var help = new Ext.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP"/>"});
						help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT"/>");
						
						//Create the contextual panel
						var rightPanel = new Ext.ametys.HtmlContainer({
								cls : 'admin-right-panel',
								region:'east',
								border: false,
								width: 277,
							    items: [navigation, handle, help]
						});
						
						var centerPanel = new Ext.form.FormPanel({
								region:'center',
								baseCls: 'transparent-panel',
								border: false,
								autoScroll : true,
								id : 'config-inner',
								formId : 'save-config',
								labelWidth :230,
								bodyStyle: 'position:relative;'
						});
						
						<xsl:for-each select="config/categories/category">
							var fieldset = new Ext.ametys.Fieldset({
								title : "<xsl:copy-of select="label/node()"/>",
								id : "<xsl:value-of select="generate-id()"/>",
								layout: 'form'
							});
							<xsl:for-each select="groups/group">
								var group = new Ext.ametys.HtmlContainer ({
									html : "<xsl:copy-of select="label/node()"/>",
									cls: 'ametys-subcategory'
								});
								fieldset.add(group);
								<xsl:for-each select="parameters/*">
									<xsl:sort select="order"/>
									
									var input;
									
									var type = "<xsl:value-of select="type"/>";
									var inputType;
									if (type == 'double')
									{
										input = new Ext.ametys.form.DoubleField ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        msgTarget: 'under', // position du message d'erreur
									        width: 250
										});
									}
									else if (type == 'long')
									{
										input = new Ext.ametys.form.LongField ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        msgTarget: 'under', // position du message d'erreur 
									        width: 250
										});
									}
									else if (type == 'password')
									{
										input = new Ext.ametys.form.PasswordWidget ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        labelWidth :230
										});
									}
									else if (type == 'date')
									{
										input = new Ext.ametys.form.DateField ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        msgTarget: 'under', // position du message d'erreur 
									        width: 250
										});
									}
									else if (type == 'boolean')
									{
										input = new Ext.ametys.form.BooleanField ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        msgTarget: 'under' // position du message d'erreur 
										});
									}
									else
									{
										input = new Ext.ametys.form.TextField ({
									        fieldLabel: "<xsl:copy-of select="label/node()"/>",
									        desc: "<xsl:copy-of select="description/node()"/>",
									        name: "<xsl:value-of select="local-name()"/>",
									        value: "<xsl:value-of select="value"/>",
									        msgTarget: 'under', // position du message d'erreur 
									        width: 250
										});
									}
									fieldset.add(input);
								</xsl:for-each>
								centerPanel.add(fieldset);
							</xsl:for-each>
						</xsl:for-each>
						
					function _getAdminPanel () 
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
					
                    var ct, bound;
                    function onready() 
                    {
	               		ct = Ext.getCmp("config-inner").getEl().child("div:first").child("*:first");
						bound = true;
						ct.on('scroll', calcScrollPosition);
						calcScrollPosition();
					
					}
					Ext.onReady(onready);
					
               </script>
			</script>
			
			<body>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>