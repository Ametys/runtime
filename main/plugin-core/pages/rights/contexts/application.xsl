<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

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
    
    <xsl:template match="/Application">
        <Context id="Application">
            <Button>
                <Icon><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/contexts/application.gif</Icon>
                <Label><i18n:text i18n:key="PLUGINS_CORE_RIGHTS_CONTEXTS_APPLICATION_LABEL" i18n:catalogue="plugin.{$pluginName}"/></Label>
                <Description><i18n:text i18n:key="PLUGINS_CORE_RIGHTS_CONTEXTS_APPLICATION_DESCRIPTION" i18n:catalogue="plugin.{$pluginName}"/></Description>
            </Button>
            <Content>
                <table id="application-fulltable">
                    <colgroup>
                        <col width="233px"/>
                        <col/>
                    </colgroup>
                    <tr>
                        <td id="affect-appli-panel" style="width: 233px; overflow: auto; border-right: 1px inset #ece9d8;"/>
                        <td>
                            <table id="application-innertable" cellspacing="0" cellpadding="0" border="0">
                                <colgroup>
                                    <col width="10px"/>
                                    <col/>
                                    <col width="10px"/>
                                </colgroup>
                                <tr>
                                    <td colspan="3" style="height: 5px">
                                        <div style="margin-left:10px; font-size: 10px"><i18n:text i18n:key="PLUGINS_CORE_RIGHTS_USERGROUP_LABEL" i18n:catalogue="plugin.{$pluginName}"/></div>                                    
                                    </td>
                                </tr>
                                <tr>
                                    <td/>
                                    <td>
                                        <div id="list-view-appli"/>
                                    </td>
                                    <td/>
                                </tr>
                                <tr>
                                    <td colspan="3" style="height: 5px"></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td id="profil-appli-panel" style="height: 185px; width: 233px; overflow: auto;  border-right: 1px inset #ece9d8;"/>
                        <td style="height: 185px; text-align: center; vertical-align: middle;">
                            <xsl:call-template name="double-liste">
                                <xsl:with-param name="profilCategory">profilAppliCategory</xsl:with-param>
                            </xsl:call-template>
                        </td>
                    </tr>
                </table>
                
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/rights/rights.i18n.js">// empty</script>
                <script>
                        onresize.push(function (width, height)
                        {
                            var appTable = document.getElementById("application-fulltable");
                            appTable.style.width = width;
                            appTable.style.height = height;
                            
                            var appTable = document.getElementById("application-innertable");
                            appTable.style.width = width - 231;
                            appTable.style.height = height - 185;
                            
                            var list = document.getElementById("list-view-appli");
                            list.style.height = height - 185;
                            list.style.width = width - 231 - 20;
                            
                            //list-view-left-appli
                            //list-view-right-appli
                        });
                
                        <xsl:if test="AdministratorUI = 'true'">
                            function goBack()
                            {
                                document.location.href = context.workspaceContext;
                            }
                        </xsl:if>
                    
                        var applicationPlugin = "<xsl:value-of select="$pluginName"/>";
                        
                        function RUNTIME_Plugin_Runtime_SelectUserCallBack_application (users)
                        {
                            RUNTIME_Plugin_Runtime_SelectUserCallBack(users, slistview_application);
                        }
                        function RUNTIME_Plugin_Runtime_SelectGroupCallBack_application (groups)
                        {
                            RUNTIME_Plugin_Runtime_SelectGroupCallBack(groups, slistview_application);
                        }
                        function RUNTIME_Plugin_Runtime_Save_Assignment_For_Application(force)
                        {
                            if (force == false)
                            {
                            	var save = false;
                            	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_VALID_CONFIRM"/>", function (button) { if (button != 'yes') save = true});
                                if ((profilAppliCategory.table.rows[0+1].style.display == "" &amp;&amp; !save) || profilAppliCategory.table.rows[0+1].style.display != "")
                                {
                                    profilAppliCategory.showHideElement(0, false);
                                    return;                             
                                }
                            }
    
                            RUNTIME_Plugin_Runtime_Save_Assignment (slistview_application, slistview_right_appli, "/application");
                            profilAppliCategory.showHideElement(0, false);
                        }
                        function RUNTIME_Plugin_Runtime_Remove_Assignment_For_Application()
                        {
                            RUNTIME_Plugin_Runtime_Remove_Assignment(slistview_application, '/application');
                        }
                    
                        var sactionpanelappli1 = new SContextualPanel ("affect-appli-panel");
                        var actAppliCategory = sactionpanelappli1.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_CATEGORY" i18n:catalogue="plugin.{$pluginName}"/>");
                            actAppliCategory.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_ADDUSER" i18n:catalogue="plugin.{$pluginName}"/>", "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/action_add_user.gif", function(){RUNTIME_Plugin_Runtime_SelectUser.act(RUNTIME_Plugin_Runtime_SelectUserCallBack_application)});
                            actAppliCategory.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_ADDGROUP" i18n:catalogue="plugin.{$pluginName}"/>", "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/action_add_group.gif", function(){RUNTIME_Plugin_Runtime_SelectGroup.act(RUNTIME_Plugin_Runtime_SelectGroupCallBack_application)});
                            actAppliCategory.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_REMOVE" i18n:catalogue="plugin.{$pluginName}"/>", "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/action_delete.gif", RUNTIME_Plugin_Runtime_Remove_Assignment_For_Application);
                    <xsl:if test="AdministratorUI = 'true'">
                            actAppliCategory.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_QUIT" i18n:catalogue="plugin.{$pluginName}"/>", getPluginResourcesUrl("<xsl:value-of select="$pluginName"/>") + "/img/rights/quit.gif", goBack);
                    </xsl:if>
                            actAppliCategory.addElement("&lt;br/&gt;&lt;div style='color: #000000; font-size: 10px; text-align: left;'&gt;<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_CONTEXTS_APPLICATION_HELP_AFFECT" i18n:catalogue="plugin.{$pluginName}"/>&lt;/div&gt;")
                    
                        var sactionpanelappli2 = new SContextualPanel ("profil-appli-panel");
                        var profilAppliCategory = sactionpanelappli2.addCategory ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFIL_CATEGORY" i18n:catalogue="plugin.{$pluginName}"/>");
                            profilAppliCategory.addLink("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_VALIDATE" i18n:catalogue="plugin.{$pluginName}"/>", "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/action_valid.gif", RUNTIME_Plugin_Runtime_Save_Assignment_For_Application);
                            profilAppliCategory.addElement("&lt;div style='color: #000000; font-size: 10px; text-align: left;'&gt;<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_CONTEXTS_APPLICATION_HELP_PROFIL" i18n:catalogue="plugin.{$pluginName}"/>&lt;/div&gt;")
                    
                        sactionpanelappli1.paint();
                        sactionpanelappli2.paint();
                        
                        actAppliCategory.showHide(true);
                        actAppliCategory.showHideElement(2, false);
                        profilAppliCategory.showHide(false);
                        
    
                        /* ************************************
                           *  TABULATION A DROITE
                           ************************************ */
                        
                        
                        var listener_application = {};
                        
                        listener_application.onSelect = function(element)
                        {
                            actAppliCategory.showHideElement(2, true);
                            profilAppliCategory.showHide(true);
                            profilAppliCategory.showHideElement(0, false);
                            
                            slistview_left_appli.elements = new Array();
                            slistview_left_appli.selection = new Array();
                            slistview_right_appli.elements = new Array();
                            slistview_right_appli.selection = new Array();
    
                            // afficher les profils dans la double liste
                            for (var i in all_profiles)
                            {
                                // qd plusieurs éléments sont sélectionnés, on affiche tout à gauche de toute manière
                                if (element.properties[i] == null || slistview_application.getSelection().length &gt;= 1) 
                                {
                                    slistview_left_appli.addElement(all_profiles[i], "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/profiles/list16.gif", "", "", {"id": i});
                                }
                                else
                                {
                                    slistview_right_appli.addElement(all_profiles[i], "<xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources/img/rights/profiles/list16.gif", "", "", {"id": i});
                                }
                            }
    
                            slistview_left_appli.paint();
                            slistview_right_appli.paint();
    
                            return true;
                        }
                        
                        listener_application.onUnselect = function(element)
                        {
                            RUNTIME_Plugin_Runtime_Save_Assignment_For_Application(false);
                        
                            profilAppliCategory.showHide(false);
                            actAppliCategory.showHideElement(2, false);
                            
                            slistview_left_appli.elements = new Array();
                            slistview_left_appli.selection = new Array();
                            slistview_left_appli.paint();
                            
                            slistview_right_appli.elements = new Array();
                            slistview_right_appli.selection = new Array();
                            slistview_right_appli.paint();
                        }
                        
                        
                        var slistview_application = new SListView ("list-view-appli", null, listener_application);
                        slistview_application.setView("icon");
                        slistview_application.paint();
                        
                        RUNTIME_Plugin_Runtime_Get_UsersAndGroups("/application", slistview_application);
                </script>   
            </Content>
        </Context>
    </xsl:template>
    
    <xsl:template name="double-liste">
        <xsl:param name="profilCategory"/>
        
        <script>
        var slistview_right_appli;
        var slistview_left_appli;
        
        function doubleList_appli (lv1, lv2, mode)
        {
            var eltsToTransit;
            if (mode == 'all')
                          eltsToTransit = lv1.getElements();
            else
                          eltsToTransit = lv1.getSelection();

            var newElements = new Array();

            for (var i=0; i &lt; eltsToTransit.length; i++)
            {
                profilAppliCategory.showHideElement (0, true);
                var elt = eltsToTransit[i];

                newElements.push (lv2.addElement(elt.name, elt.icon16, elt.icon32, elt.icon50, elt.properties));
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
            <table cellpadding="0" cellspacing="0" style="height: 185px; margin: 10px; margin-top: 5px; margin-bottom: 5px; table-layout: fixed">
                <colgroup>
                    <col width="50%"/>
                    <col width="50px"/>
                    <col width="50%"/>
                </colgroup>
                <tr>
                    <td>
                        <div id="list-view-left-appli"/>
                    </td>
                    <td style="text-align: center; vertical-align: middle">
                        <img onclick="doubleList_appli(slistview_right_appli, slistview_left_appli, 'all');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/dbl_list_rewind.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 1px; margin-top: 0px; padding: 0px;" i18n:attr="title" title="plugin.{$pluginName}:PLUGINS_CORE_RIGHTS_DBL_LIST_REWIND"/>
                        <img onclick="doubleList_appli(slistview_right_appli, slistview_left_appli, 'selection');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/dbl_list_previous.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 1px; padding: 0px;" i18n:attr="title" title="plugin.{$pluginName}:PLUGINS_CORE_RIGHTS_DBL_LIST_PREV"/>
                        <img onclick="doubleList_appli(slistview_left_appli, slistview_right_appli, 'selection');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/dbl_list_next.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 1px; padding: 0px;" i18n:attr="title" title="plugin.{$pluginName}:PLUGINS_CORE_RIGHTS_DBL_LIST_NEXT"/>
                        <img onclick="doubleList_appli(slistview_left_appli, slistview_right_appli, 'all');" src="{$contextPath}/plugins/{$pluginName}/resources/img/rights/dbl_list_forward.gif" style="width: 25px; height: 25px; text-align: center; display: block; margin: 1px; padding: 0px;" i18n:attr="title" title="plugin.{$pluginName}:PLUGINS_CORE_RIGHTS_DBL_LIST_FORWARD"/>
                    </td>
                    <td>
                        <div id="list-view-right-appli"/>
                    </td>
                </tr>
            </table>
        <script>
            slistview_left_appli = new SListView ("list-view-left-appli" , null, null);
            slistview_right_appli = new SListView ("list-view-right-appli", null, null);
    
            slistview_left_appli.addColumn (null, "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGN_LEFTCOLUMN" i18n:catalogue="plugin.{$pluginName}"/>", null, "185px", null);
            slistview_right_appli.addColumn (null, "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGN_RIGHTCOLUMN" i18n:catalogue="plugin.{$pluginName}"/>", null, "185px", null);
    
            slistview_left_appli.setView("detail");
            slistview_right_appli.setView("detail");
    
            slistview_left_appli.paint();
            slistview_right_appli.paint();
        </script>
    </xsl:template>
</xsl:stylesheet>