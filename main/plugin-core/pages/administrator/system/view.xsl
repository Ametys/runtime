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
    
    <xsl:template match="/">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_TITLE"/></title>
                <link rel="stylesheet" href="{$workspaceContext}/resources/css/homepage.css" type="text/css"/>
            </head>
            
            <body>
                <style>
                     div.checkzone
                     {
                        margin: 15px;
                        height: 20px;
                     }
                     div.checkzone input.checkbox
                     {
                        vertical-align: middle;
                     }
                     div.checkzone label
                     {
                        vertical-align: middle;
                     }
                     
                     hr.separator
                     {
                        margin-left: 15px;
                        margin-right: 15px;
                        border-top: 1px solid #84827b;
                        border-bottom: 1px solid #fffbf7;
                        margin-top: 10px;
                     }
                </style>
            
                <table class="admin_index_main_table" style="width: 710px; height: 440px;">
                    <tr>
                        <td id="actionset"/>
						<td style="background-color: #efebde; padding-top: 10px">
                        <!-- MAINTENANCE DESACTIVE POUR LE MOMENT
                            <div class="checkzone">
                                <input type="checkbox" class="checkbox" id="maintenance" style="margin-right: 5px;">
                                    <xsl:if test="/System/maintenance/@state='on'">
                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                    </xsl:if>
                                </input>
                                <label for="maintenance"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_MAINTENANCE"/></label>
                            </div>
                            
                            <div style="height: 20px;"> <hr class="separator"/> </div>
                        -->    
                            <div class="checkzone">
                                <input type="checkbox" class="checkbox" id="announcement" style="margin-right: 5px;" onclick="onAnnouncementStateChange(this)" onchange="onAnnouncementStateChange(this)">
                                    <xsl:if test="/System/announcements/@state='on'">
                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                    </xsl:if>
                                </input>
                                <label for="announcement"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_CHECK"/></label>
                            </div>
                            <div style="padding-left: 40px; margin-right: 20px; height: 300px; width: 420px;">
                                <div style="height: 300px; width: 420px;" id="listview"></div>
                            </div>
                        </td>
                    </tr>
                </table>
                
                <table id="RUNTIME_Plugin_Runtime_Administrator_System_Announcement" style="display: none">
                    <tr>
                        <td class="dialog">

                            <form method="POST" style="padding: 0px; margin: 0px;">
                                <table cellspacing="0" cellpadding="0" width="355px">
                                    <colgroup>
                                        <col width="90px"/>
                                        <col width="20px"/>
                                        <col/>
                                    </colgroup>
                                    <tbody>
                                        <tr>
                                            <td>
                                                <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>
                                            </td>
                                            <td>
                                                <img src="{$resourcesPath}/img/administrator/system/help.gif" title="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_LANG_HELP" i18n:attr="title"/>
                                            </td>
                                            <td>
                                                <input id="languageCode" type="text" maxlength="2" style="width: 50px; font-size: 10px; font-family: verdana;"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td valign="top">
                                                <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>
                                            </td>
                                            <td valign="top">
                                                <img src="{$resourcesPath}/img/administrator/system/help.gif" title="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_MESSAGE_HELP" i18n:attr="title"/>
                                            </td>
                                            <td>
                                                <textarea id="message" style="width: 250px; height: 80px; font-size: 10px; font-family: verdana;"></textarea>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="3" style="height: 30px; text-align: center; vertical-align: bottom; ">
                                                <button onclick="parent.RUNTIME_Plugin_Runtime_Administrator_System_Announcement.ok(); return false;">
                                                    <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_OK" />
                                                </button>
                                                &#160;&#160;
                                                <button onclick="parent.RUNTIME_Plugin_Runtime_Administrator_System_Announcement.cancel(); return false;">
                                                    <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CANCEL" />
                                                </button>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </form>
                                    
                        </td>
                    </tr>
                </table>
                
                <script>
                    function goBack()
                    {
                        document.location.href = context.workspaceContext;
                    }
                    
                    function save()
                    {
                        var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/system/update";
                        var args = "";
                        
                       // args += "&amp;maintenance=" + (document.getElementById('maintenance').checked ? "true" : "false");
                        args += "&amp;announcement=" + (document.getElementById('announcement').checked ? "true" : "false");
                        
                        for (var i = 0; i &lt; listview.elements.length; i++)
                        {
                            var element = listview.elements[i];
                            args += "&amp;lang=" + element.name;
                            args += "&amp;message_" + element.name + "=" + encodeURIComponent(Tools.textareaToHTML(element.properties.message));
                        }
                        
                        var result = Tools.postFromUrl(url, args);
                        if (result == null)
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ERROR_SAVE"/>")
                            return;
                        }
                        
                        goBack();
                    }
                
                    var sp = new SContextualPanel("actionset");
                    var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE"/>");
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_SAVE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/save.gif", save);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/nosave.gif", goBack);
                    var handleAnnouncement = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT"/>");
                        handleAnnouncement.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_ADD"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/add.gif", announcement_add);
                        handleAnnouncement.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_EDIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/edit.gif", announcement_edit);
                        handleAnnouncement.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/delete.gif", announcement_remove);
                    var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP"/>");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_HELP_TEXT"/>&lt;/div&gt;");
                    sp.paint();
                    
                    var listview_listener = {};
                    listview_listener.onSelect = function(element)
                    {
                        handleAnnouncement.showHideElement(1, true);
                        handleAnnouncement.showHideElement(2, element.name != '*');
                        return true;
                    }
                    
                    listview_listener.onUnselect = function(element)
                    {
                        handleAnnouncement.showHideElement(1, false);
                        handleAnnouncement.showHideElement(2, false);
                    }
                    
                    var listview = new SListView("listview", null, listview_listener);
                    listview.setView("detail");
                    listview.sort(true, null, true);
                    listview.setMultipleSelection(false);
                    listview.addColumn(null, "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG"/>", null, "60px");
                    listview.addColumn("message", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE"/>", null, "300px");

                    /** ------------------------------------------- */

                    <xsl:choose>
                        <xsl:when test="/System/announcements">
                            <xsl:for-each select="/System/announcements/announcement">
                                listview.addElement("<xsl:choose><xsl:when test="@lang"><xsl:value-of select="@lang"/></xsl:when><xsl:otherwise>*</xsl:otherwise></xsl:choose>", 
                                                    "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/announcement_small.gif", "", "", 
                                                    { message : "<xsl:value-of select="."/>",
                                                      messageDisplay : "<xsl:value-of select="."/>".replace(/&lt;br\/&gt;/g, " ")});
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            listview.addElement("*", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/announcement_small.gif", "", "", { message : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_DEFAULTMESSAGE"/>"});
                        </xsl:otherwise>
                    </xsl:choose>

                    listview.paint();
                    
                    function announcement_add()
                    {
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = true;
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act();
                        
                        var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("languageCode");
                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("message");
                        
                        langCodeInput.disabled = false;
                        langCodeInput.value = "";
                        messageTextarea.value = "";
                        try {
                            langCodeInput.focus();
                        } catch(e) {}
                    }
                    function announcement_edit()
                    {
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = false;
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act();
                        
                        var element = listview.getSelection()[0];
                        
                        var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("languageCode");
                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("message");
                        
                        langCodeInput.disabled = element.name == "*";
                        langCodeInput.value = element.name;
                        messageTextarea.value = Tools.htmlToTextarea(element.properties.message);
                        try {
                            messageTextarea.focus();
                            messageTextarea.select();
                        } catch(e) {}
                    }
                    function announcement_remove()
                    {
                        if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DELETE_CONFIRM"/>"))
                        {
                            var element = listview.getSelection()[0];
                            element.remove();
                        }
                    }
                
                    function onAnnouncementStateChange(input)
                    {
                        handleAnnouncement.showHide (input.checked);
                        document.getElementById("listview").style.display = input.checked ? "" : "none";
                    }

                    var RUNTIME_Plugin_Runtime_Administrator_System_Announcement = {};
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.plugin = "<xsl:value-of select="$pluginName"/>";
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd = true;
                    
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialize = function ()
                    {
                        if (RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialized)
                            return;
                            
                        var config = new SDialog.Config()
                        config.innerTableClass = "dialog";
                      
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box = new SDialog("RUNTIME_Plugin_Runtime_Administrator_System_Announcement", 
                                                    "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION"/>", 
                                                    getPluginResourcesUrl(RUNTIME_Plugin_Runtime_Administrator_System_Announcement.plugin) + "/img/administrator/system/announcement_small.gif", 
                                                    380, 175, config, RUNTIME_Plugin_Runtime_Administrator_System_Announcement);
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.paint();
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.ui.iframe.contentWindow.document;
                        Tools.loadStyle(RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document, context.contextPath + "/kernel/resources/css/dialog.css");
                            
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialized = true;
                    }
                    
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.act = function ()
                    {
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.initialize();
                        
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.showModal();
                    }
                    
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.ok = function ()
                    {
                        var langCodeInput = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("languageCode");
                        var messageTextarea = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.document.getElementById("message");
                    
                        if (langCodeInput.disabled != true)
                        {
                            if (!/[a-z][a-z]/i.test(langCodeInput.value))
                            {
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_LANG"/>");
                                return;
                            }
                        }
                        
                        if (messageTextarea.value == '')
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_MESSAGE"/>");
                            return;
                        }
                    
                        var lang = langCodeInput.value;
                        var message = messageTextarea.value.replace(/\r/g, "");
                    
                        var element = RUNTIME_Plugin_Runtime_Administrator_System_Announcement.modeAdd ? 
                                    listview.addElement("", "<xsl:value-of select="$resourcesPath"/>/img/administrator/system/announcement_small.gif", "", "", {}) : 
                                    listview.getSelection()[0];                        
                        element.name = lang;
                        element.properties.message = message;
                        element.properties.messageDisplay = message.replace(/"&lt;br\/&gt;"/g, " ");
                        listview.paint();
                        element.select();
                        
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.close();
                    }
                    
                    RUNTIME_Plugin_Runtime_Administrator_System_Announcement.cancel = function ()
                    {
                        RUNTIME_Plugin_Runtime_Administrator_System_Announcement.box.close();
                    }
                    
                    onAnnouncementStateChange(document.getElementById('announcement'));
                    listview_listener.onUnselect(null);
                </script>
            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>