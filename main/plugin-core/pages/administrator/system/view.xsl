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
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/System">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/system.css" type="text/css"/>                  
            </head>
            
	            <script>
	            	<script type="text/javascript">
		               		function goBack()
		                    {
		                        document.location.href = context.workspaceContext;
		                    }   
		                    
		                    function save()
		                    {
		                    	var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/system/update";
                        		var args = "";
                        
                       			// args += "&amp;maintenance=" + (document.getElementById('maintenance').checked ? "true" : "false");
                        		args += "&amp;announcement=" + (fieldset.checkbox.dom.checked ? "true" : "false");
                        
                        		var elmts = listview.getElements();
		                        for (var i = 0; i &lt; elmts.length; i++)
		                        {
		                            var element = elmts[i];
		                            args += "&amp;lang=" + element.get('lang');
		                            args += "&amp;message_" + element.get('lang') + "=" + encodeURIComponent(Tools.textareaToHTML(element.get('message')));
		                        }
                        
		                        var result = Tools.postFromUrl(url, args);
		                        if (result == null)
		                        {
		                        	Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ERROR_SAVE"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        	});
		                            return;
		                        }
                        
                        		goBack();
		                    }
		                    
		                    function announcement_add() 
		                    {
		                    	RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = true;
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act();
		                        
		                        var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("languageCode");
		                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("message");
		                        
		                        langCodeInput.setDisabled(false);
		                        langCodeInput.setValue("");
		                        messageTextarea.setValue("");
		                        try {
		                            langCodeInput.focus();
		                        } catch(e) {}
		                    }
		                    
		                    function announcement_edit()
		                    {
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = false;
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act();
		                        
		                        var element = listview.getSelection()[0];
		                        
		                       	var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("languageCode");
		                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("message");
		                        
		                        langCodeInput.setDisabled(element.get('lang') == "*");
		                        langCodeInput.setValue(element.get('lang'));
		                        messageTextarea.setValue(Tools.htmlToTextarea(element.get('message')));
		                        try {
		                            messageTextarea.focus();
		                            messageTextarea.select();
		                        } catch(e) {}
		                    }
		                    
		                    function announcement_remove()
		                    {
		                    	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE_CONFIRM"/>", removeCb);
		                    }
		                    function removeCb (btn)
		                    {
		                    	if (btn == 'yes')
		                    	{
		                            var element = listview.getSelection()[0];
		                            listview.removeElement(element);	
		                    	}
		                    }
		                    
		                    
		                    var RUNTIME_Plugin_Runtime_Administrator_System_Announcement = {};
                    		RUNTIME_Plugin_Runtime_Administrator_System_Announcement.plugin = "<xsl:value-of select="$pluginName"/>";
                    		RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = true;
                    
                    		function _escape(s)
							{
								s = s.replace(/&lt;br\/&gt;/g, "\n");
								return s;
							}

		                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialize = function ()
		                    {
		                    	var langCodeInput = new org.ametys.form.TextField ({
									fieldLabel: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>',
									desc: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_LANG_HELP"/>",
							        name: 'languageCode',
							        msgTarget: 'side',
									anchor:'90%'
								});	
								
								var message = new org.ametys.form.TextAreaField ({
									fieldLabel :'<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>',
									desc: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_MESSAGE_HELP"/>",
									name: 'message',
									anchor:'90%',
									msgTarget: 'side',
							        height: 80
								});	
								
								RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form = new Ext.FormPanel( {
									id : 'form-announcement',
									labelWidth :70,
									width :350,
									border :false,
									bodyStyle :'padding:10px 10px 0',
									items : [ langCodeInput, message ]
								});
								
								
                            	RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box = new org.ametys.DialogBox({
										title :'<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION"/>',
										layout :'fit',
										width :380,
										height :200,
										icon: getPluginResourcesUrl('core') + '/img/administrator/system/announce_16.png',
										items : [ RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form ],
										closeAction: 'close',
										buttons : [ {
											text :'<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_OK"/>',
											handler : RUNTIME_Plugin_Runtime_Administrator_System_Announcement.ok
										}, {
											text :'<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CANCEL"/>',
											handler : RUNTIME_Plugin_Runtime_Administrator_System_Announcement.cancel
										} ]
									});
                    		}
                    
		                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act = function ()
		                    {
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialize();
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.show();
		                    }
                    
		                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.ok = function ()
		                    {
		                        var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("languageCode");
		                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.form.getForm().findField("message");
		                    
		                        if (langCodeInput.disabled != true)
		                        {
		                            if (!/[a-z][a-z]/i.test(langCodeInput.getValue()))
		                            {
		                            	langCodeInput.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_LANG"/>");
		                                return;
		                            }
		                        }
		                        
		                        if (messageTextarea.getValue() == '')
		                        {
		                            messageTextarea.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_MESSAGE"/>");
		                            return;
		                        }
		                    
		                        var lang = langCodeInput.getValue();
		                        var message = messageTextarea.getValue().replace(/\r/g, "");
		                    
		                    	if (RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd)
		                    	{
		                    		listview.addElement(null, 
			                                           	{ lang : lang,
			                                              message : message});
		                    	}
		                    	else
		                    	{
		                    		var element = listview.getSelection()[0];  
		                    		element.set('lang',lang);        
		                    		element.set('message',message);      
		                    	}
		                        
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.close();
		                    }
                    
		                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.cancel = function ()
		                    {
		                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.close();
		                    }
		                    
		                    function onSelectAnnouncement()
							{
								handleAnnouncement.showElt(3);
								handleAnnouncement.showElt(4);		                    
			                }
		                    
							// Actions
							var handle = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE"/>'});
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_SAVE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/save.png", save);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/quit.png", goBack);
								
							// Annonces
							var handleAnnouncement = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT"/>'});
							handleAnnouncement.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_ADD"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/add.png", announcement_add);
							handleAnnouncement.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_EDIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/edit.png", announcement_edit);
							handleAnnouncement.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/delete.png", announcement_remove);
								
							handleAnnouncement.hideElt(3);
							handleAnnouncement.hideElt(4);
								
							// Aide
							var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP"/>'});
							help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP_TEXT"/>");
	            			
							var listview = new org.ametys.ListView({
									listeners: {'rowclick': onSelectAnnouncement},
									autoScroll: true,	
									viewConfig: {
								        forceFit: true
								    },
								    store : new Ext.data.SimpleStore({
											id:0,
									        fields: [
									           {name: 'lang'},
									           {name: 'message'}
									        ]
									}),
								    columns: [
								        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>", width : 80, menuDisabled : true, sortable: true, dataIndex: 'lang', defaultSortable : true},
								        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>", width : 400, menuDisabled : true, sortable: true, dataIndex: 'message'}
								    ],
									id: 'list-view-announce',
									region: 'center',
									baseCls : 'list-view',
								    width : 500,
								    height: 400
								});		
								
							<xsl:choose>
		                        <xsl:when test="/System/announcements">
		                            <xsl:for-each select="/System/announcements/announcement">
		                                listview.addElement(null, 
		                                                    { lang : "<xsl:choose><xsl:when test="@lang"><xsl:value-of select="@lang"/></xsl:when><xsl:otherwise>*</xsl:otherwise></xsl:choose>",
		                                                      message : "<xsl:value-of select="."/>"});
		                            </xsl:for-each>
		                        </xsl:when>
		                        <xsl:otherwise>
		                            listview.addElement(null, { lang : "*", message : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_DEFAULTMESSAGE"/>"});
		                        </xsl:otherwise>
		                    </xsl:choose>
                    
							listview.getStore().sort('lang', 'ASC');
								
							var	fieldset = new Ext.form.FieldSet({
								region:'center',
								title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_CHECK"/>",
								checkboxToggle:true,
								<xsl:if test="announcements/@state != 'on'">collapsed: true,</xsl:if>
								items : [ listview ]
							});
								
							var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									width: 277,
									cls: 'admin-right-panel',
								    items: [handle, handleAnnouncement, help]
							});
							
							function _getAdminPanel ()
							{
								return new Ext.Panel({
									region: 'center',
									baseCls: 'transparent-panel',
									border: false,
									layout: 'border',
									autoScroll: true,
									items: [fieldset, rightPanel]
								});
							}
	            	</script>
	            </script>	
            <body>

            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>