<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2013 Anyware Services

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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:exslt="http://exslt.org/common" 
                xmlns:escaper="org.apache.commons.lang.StringEscapeUtils"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper"
                xmlns:digest="org.apache.commons.codec.digest.DigestUtils"
                extension-element-prefixes="exslt">

    <xsl:import href="kernel.xsl"/>
    <xsl:import href="../pages/common.xsl"/>    
    
    <xsl:param name="splashscreen"/>
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspaceURI" select="ametys:workspacePrefix()"/>
    <xsl:variable name="debug-mode" select="ametys:isDeveloperMode()"/>
    
    <xsl:template name="head-title"><i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/></xsl:template>
    <xsl:template name="applicationTitle"><i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/></xsl:template>
    <xsl:template name="ribbonTitle"/>
    <xsl:template name="css-file"><xsl:value-of select="$themeURL"/>/sass/special/splashscreen.scss</xsl:template>
    
    <xsl:template name="uicall-js">
        <xsl:call-template name="kernel-base-js"/>
    </xsl:template>
    
    <xsl:template name="uicall-css">
        <xsl:call-template name="kernel-base-css"/>
    </xsl:template>
    
    <xsl:template name="head-css">
        <xsl:if test="$splashscreen != 'no'">
            <xsl:call-template name="head-css-impl"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="body">
        <xsl:if test="$splashscreen != 'no'">
            <xsl:call-template name="body-impl"/>
            <script type="text/javascript" id="script-loader">
                function destroyLoader()
                { 
                    Ext.getBody().child("div.head").dom.style.display = 'none';
                    Ext.getBody().child("div.foot").dom.style.display = 'none';
                    Ext.getBody().child("div.main").dom.style.display = 'none';
                    Ext.get("script-loader").remove();
                    Ext.get(document.body.parentNode).removeCls("ametys-common");
                }
            </script>
        </xsl:if>
    </xsl:template>

    <xsl:template name="main">
        <noscript><div class="splashscreen noscript"><div class="msg"><i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></div></div></noscript>
    
        <div id="splashscreen" class="splashscreen" style="display: none">
            <div class="msg">
                <span id="load-msg">
                    <xsl:choose>
                        <xsl:when test="/Ametys/workspace/safe-mode != 'CONFIG_INCOMPLETE'">
                            <i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_LOADING_SAFEMODE" i18n:catalogue="plugin.core-ui"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_LOADING" i18n:catalogue="plugin.core-ui"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </div>
            
            <xsl:comment>[if lte IE 9]>
            &lt;style type="text/css">
                .la-ball-spin-clockwise {
                    background-image: url(<xsl:value-of select="$contextPath"/>/plugins/extjs6/resources/classic/theme-neptune/resources/images/loadmask/loading.gif);
                    background-position: center;
                    background-repeat: no-repeat;
                    background-size: 30%;
                }
                .la-ball-spin-clockwise div {
                    display: none;
                }
            &lt;/style>
            &lt;![endif]</xsl:comment>
                  
            <div class="la-ball-spin-clockwise la-dark la-3x">
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
            </div>
        </div>
        
        <script type="text/javascript">
            document.getElementById('splashscreen').style.display = "";
        </script>
    </xsl:template>

    <xsl:template name="body-start">
        <script type="text/javascript">
            window.onerror = function(e) {
                window.errorMode = true;
                
                document.body.parentNode.classList.add("error");
                <xsl:choose>
                    <xsl:when test="$splashscreen != 'no'">
                       document.getElementById('load-msg').innerHTML = "<i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_LOAD_FAIL" i18n:catalogue="plugin.core-ui"/>";
                    </xsl:when>
                    <xsl:otherwise>
                       document.body.innerHTML = "<i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_LOAD_FAIL" i18n:catalogue="plugin.core-ui"/>";
                    </xsl:otherwise>
                </xsl:choose>

                // Remove conflicting CSS
                var links = document.body.getElementsByTagName("link");
                for (var i = 0; i &lt; links.length; i++)
                {
                    links[i].parentNode.removeChild(links[i]);
                }
            }
        </script>
    </xsl:template>
    
    <xsl:template name="body-end">
        <xsl:apply-templates select="/Ametys/workspace"/>
        
        <!-- Thoses fake inputs are here to calm down browsers completion -->
        <input style="display: none" name="username" type="text" id="fake_username" onchange="this.value=''"/>
        <input style="display: none" name="password" type="password" id="fake_password" onchange="this.value=''"/>
    </xsl:template>
    
    <xsl:template match="workspace">
                <noscript><i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></noscript>
                
                <xsl:call-template name="uicall-css"/>                
                <xsl:call-template name="kernel-load-css">
                      <xsl:with-param name="css" select="static-imports/import/css/file | ribbon/controls/control/css/file | ribbon/tabsControls/tab/css/file | uitools-factories/uitool-factory/css/file | messagetarget-factories/messagetarget-factory/css/file | relations-handlers/relation-handler/css/file | widgets/widget-wrapper/widget/css/file"/>
                </xsl:call-template>
                                
                <xsl:call-template name="uicall-js"/>                
                
                <xsl:call-template name="ui-apptools-load"/>
               
                <xsl:call-template name="ui-extension-load"/>
                    
                
                <script type="text/javascript">
                    Ametys.setAppParameter("debug.mode", "<xsl:value-of select="$debug-mode"/>");        
                    Ametys.setAppParameter("context", "<xsl:call-template name="app-context"/>");

                    Ametys.setAppParameter("user", {
                        login: "<xsl:value-of select="user/@login"/>",
                        population: "<xsl:value-of select="user/population"/>",
                        firstname: "<xsl:value-of select="user/firstname"/>",
                        lastname: "<xsl:value-of select="user/lastname"/>",
                        fullname: "<xsl:value-of select="user/fullname"/>",
                        email:  "<xsl:value-of select="user/email"/>",
                        locale: Ametys.LANGUAGE_CODE
                    });
                </script>
                
                <!-- UITools -->

                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="static-imports/import/scripts/file"/>
                </xsl:call-template>                
                
                <xsl:call-template name="ui-extension-after-static-load"/>
                
                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="(ribbon/controls/control/scripts/file | ribbon/tabsControls/tab/scripts/file | uitools-factories/uitool-factory/scripts/file | messagetarget-factories/messagetarget-factory/scripts/file | relations-handlers/relation-handler/scripts/file | widgets/widget-wrapper/widget/scripts/file)[not(. = current()/static-imports/import/scripts/file)]"/>
                </xsl:call-template>                

                <script type="text/javascript">
                    (function() {
                               /** Tools factories */<xsl:text/>
                               var factory;
                    <xsl:for-each select="uitools-factories/uitool-factory">
                               factory = Ext.create("<xsl:value-of select="action/@class"/>", Ext.apply(<xsl:value-of select="action"/>, {id: "<xsl:value-of select="@id"/>", pluginName: "<xsl:value-of select="@plugin"/>"}));
                               Ametys.tool.ToolsManager.addFactory(factory);
                    </xsl:for-each>
                    
                               /** Controls creation */<xsl:text/>
                               var control;
                    <xsl:for-each select="ribbon/controls/control">
                               control = Ext.create("<xsl:value-of select="action/@class"/>", Ext.apply(<xsl:value-of select="action"/>, {id: "<xsl:value-of select="@id"/>", serverId: "<xsl:value-of select="@serverId"/>", pluginName: "<xsl:value-of select="@plugin"/>"}));
                               Ametys.ribbon.RibbonManager.registerUI(control);
                    </xsl:for-each>

                                /** Searchmenu items creation */                    
                                var searchMenuItems = [];          
                                
                                // create the lunr index
								var index = lunr(function(){
									var lang = Ametys.getAppParameter("user").locale;
									if (lang != 'en')
									{
										// use the language
										var lunrUse = "lunr." + lang;
		    							this.use(eval(lunrUse));
		    						}
    
								    this.field('title', {boost: 1});
								    this.field('keywords', {boost: 100});
								    this.field('description', {boost: 10});
								    // the id
								    this.ref('id');
								    
								    this.pipeline.add(lunr.elision);
								    this.pipeline.add(lunr.deemphasize);
								});
								
                    <xsl:for-each select="ribbon/tabs/tab/groups/group/medium//control">
                            <xsl:variable name="id" select="@id"/>
                               try { 
                               		var control = Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="@id"/>");
                                    var tabLabel = "<xsl:value-of select="ancestor::tab/@label"/>";
                               		var menuItem = control.addMenuItemUI();
                                    menuItem.text = '&lt;span class="a-ribbon-searchmenu-item-category"&gt;' + tabLabel + '&lt;/span&gt;' + menuItem.text;
                               		searchMenuItems.push(menuItem);
                               		index.add({
									    id: menuItem.id,
									    title: control.getInitialConfig('label'),
									    keywords: control.getInitialConfig('keywords') || "",
									    description: control._description,
									});
                               	} catch (e) { /* some controls cannot be used into a menu */ }   
                    </xsl:for-each>
                    		lunr.controllersIndex = index;

                            var ribbonItems = [];         

                    <xsl:for-each select="ribbon/tabs/tab">
                        <xsl:sort select="not(not(@contextualColor))"/>
                        
                        <xsl:variable name="tabPos"><xsl:value-of select="position()"/></xsl:variable>
                        
                            /** Tab <xsl:value-of select="$tabPos"/><xsl:text/> */
                            var tab_<xsl:value-of select="$tabPos"/> = Ext.create("Ametys.ui.fluent.ribbon.Panel", {<xsl:text/>
                            <xsl:text/>title: "<xsl:value-of select="@label"/>",<xsl:text/>
                            <xsl:if test="@contextualColor"><xsl:text/>contextualTab: "<xsl:value-of select="@contextualColor"/>",<xsl:text/></xsl:if>
                            <xsl:if test="@contextualGroup"><xsl:text/>contextualGroup: "<xsl:value-of select="@contextualGroup"/>",<xsl:text/></xsl:if>
                            <xsl:if test="@contextualLabel"><xsl:text/>contextualLabel: "<xsl:value-of select="@contextualLabel"/>",<xsl:text/></xsl:if>
                            <xsl:text/>items: []<xsl:text/>
                            <xsl:text>});</xsl:text>
                        
                            <!-- Ribbon tabs group -->
                        <xsl:for-each select="groups/group[medium//control/@id = /Ametys/workspace/ribbon/controls/control/@id]">
                        
                            <xsl:variable name="groupPos"><xsl:value-of select="position()"/></xsl:variable>

                            var fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_small = [];<xsl:text/>
                            var fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_medium = [];<xsl:text/>
                            var fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_large = [];<xsl:text/>

                            <xsl:for-each select="medium/*|small/*|large/*">
                                <xsl:choose>
                                    <xsl:when test="local-name()='control' and @id = /Ametys/workspace/ribbon/controls/control/@id">
                                        <xsl:variable name="id" select="@id"/>
                            fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_<xsl:value-of select="local-name(..)"/>.push(<xsl:text/>
                                   <xsl:text/>Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$id"/>").addUI("large")<xsl:text/> 
                                <xsl:text/>);<xsl:text/>
                                    </xsl:when>
                                    <xsl:when test="local-name()='layout' and (*[@id = /Ametys/workspace/ribbon/controls/control/@id] or toolbar/*[@id = /Ametys/workspace/ribbon/controls/control/@id])">
                                        <xsl:variable name="layoutPos"><xsl:value-of select="position()"/></xsl:variable>
                            fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_<xsl:value-of select="local-name(..)"/>.push({<xsl:text/>
                                    columns: <xsl:value-of select="@cols"/>,
                                    align: '<xsl:value-of select="@align"/>',
                                    items: [
                                        <xsl:for-each select="*[@id = /Ametys/workspace/ribbon/controls/control/@id] | toolbar"><xsl:if test="position() != 1">, </xsl:if>
                                            <xsl:choose>
                                                <xsl:when test="local-name()='control' and @id = /Ametys/workspace/ribbon/controls/control/@id">
                                                    <xsl:variable name="id" select="@id"/>
                                       <xsl:text/>Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$id"/>").addUI("<xsl:value-of select="../@size"/>", <xsl:value-of select="@colspan"/>)<xsl:text/> 
                                                </xsl:when>
                                                <xsl:when test="local-name()='toolbar' and *[@id = /Ametys/workspace/ribbon/controls/control/@id]">
                                                <xsl:text/>{
                                                    colspan: <xsl:value-of select="@colspan"/>,
                                                    items: [<xsl:text/>
                                                <xsl:for-each select="*[@id = /Ametys/workspace/ribbon/controls/control/@id]"><xsl:if test="position() != 1">, </xsl:if>
                                                    <xsl:variable name="id" select="@id"/>
                                            <xsl:text/>Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$id"/>").addUI("very-small", <xsl:value-of select="@colspan"/>)<xsl:text/> 
                                                </xsl:for-each>
                                                    ]
                                                }
                                                </xsl:when>
                                             </xsl:choose>
                                        </xsl:for-each>
                                    ]
                                });
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:for-each>
                            
                                var fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/> = {<xsl:text/>
                                    <xsl:text/>title: '<xsl:value-of select="@label"/>'<xsl:text/>,
                                    <xsl:text/>priority: <xsl:value-of select="@priority"/><xsl:text/>,
                                    <xsl:text/>smallItems: fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_small,
                                    <xsl:text/>items: fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_medium,
                                    <xsl:text/>largeItems: fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>_large
                                <xsl:text/>};<xsl:text/>
                            
                                // Dialog box launcher
                                <xsl:variable name="dialogBoxLauncher" select="@dialog-box-launcher"/>
                                <xsl:if test="@dialog-box-launcher and /Ametys/workspace/ribbon/controls/control[@id = $dialogBoxLauncher]/action">
                                    var controlCfg = <xsl:value-of select="/Ametys/workspace/ribbon/controls/control[@id = $dialogBoxLauncher]/action"/>;
                                    if (controlCfg['dialog-box-launcher'])
                                    {
                                        fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>.dialogBoxLauncher = true;
                                        fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>.listeners = {'dialogboxlaunch': Ext.bind(Ametys.executeFunctionByName, Ametys, [controlCfg['dialog-box-launcher'], null, null, Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$dialogBoxLauncher"/>")], false)};
                                    }
                                </xsl:if>
                                
                                tab_<xsl:value-of select="$tabPos"/>.add(fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>);<xsl:text/>
                            </xsl:for-each>
                            
                            ribbonItems.push(tab_<xsl:value-of select="$tabPos"/>);<xsl:text/>
                    </xsl:for-each>

                            /** Ribbon creation */
                            var appMenuItems = [];
                            <xsl:for-each select="ribbon/app-menu/*">
                                <xsl:choose>
                                    <xsl:when test="local-name()='control' and @id = /Ametys/workspace/ribbon/controls/control/@id">
                                        <xsl:variable name="id" select="@id"/>
                                        appMenuItems.push(Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$id"/>").addMenuItemUI());
                                    </xsl:when>
                                    <xsl:when test="local-name()='separator'">
                                        appMenuItems.push('-');
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:for-each>
                            
                            <xsl:if test="user">
                                var userMenuItems = [];
                                <xsl:if test="ribbon/user-menu/*">
                                    userMenuItems.push('-');
                                </xsl:if>
                                <xsl:for-each select="ribbon/user-menu/*">
                                    <xsl:choose>
                                        <xsl:when test="local-name()='control' and @id = /Ametys/workspace/ribbon/controls/control/@id">
                                            <xsl:variable name="id" select="@id"/>
                                            userMenuItems.push(Ametys.ribbon.RibbonManager.getUI("<xsl:value-of select="$id"/>").addMenuItemUI());
                                        </xsl:when>
                                        <xsl:when test="local-name()='separator'">
                                            userMenuItems.push('-');
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:if>
                            
                            var ribbon = Ext.create("Ametys.ui.fluent.ribbon.Ribbon", {<xsl:text/>
                                <xsl:text/>applicationTitle: "<xsl:call-template name="applicationTitle"/>",
                                title: "<xsl:call-template name="ribbonTitle"/>",
                                
                                id: 'ribbon',
                                
                                mainButton: {
                                    xtype: 'button',
                                    iconCls: 'a-mainbutton',
                                    arrowVisible: false,
                                    /*tooltip: {
                                         title: "", 
                                         image: "", 
                                         text: "", 
                                         inribbon: true
                                    },*/
                                    menu: appMenuItems.length == 0 ? null : {
                                         ui: 'ribbon-menu',
                                         items: appMenuItems
                                    }
                                },
                                
                                <xsl:text/>items: ribbonItems,<xsl:text/>
                                
                                message: [
                                <xsl:if test="safe-mode">
                                    {
                                        title: "<i18n:text i18n:key="PLUGINS_CORE_UI_SAFE_MODE_BANNER_TITLE_{safe-mode}" i18n:catalogue="plugin.core-ui"/>",
                                        text: "<i18n:text i18n:key="PLUGINS_CORE_UI_SAFE_MODE_BANNER_TEXT_{safe-mode}" i18n:catalogue="plugin.core-ui"/>",
                                        <xsl:choose>
                                            <xsl:when test="safe-mode = 'CONFIG_INCOMPLETE'">
                                                closable: true,
                                                type: "info"
                                            </xsl:when>
                                            <xsl:otherwise>
                                                closable: false,
                                                type: "warning"
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    }
                                </xsl:if>
                                ],
                                
                                help: {
                                    tooltip:{ 
                                        inribbon: true, 
                                        text: "<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_PLACEHOLDER' i18n:catalogue='plugin.core-ui'/>",
                                    },
                                    handler: function() { this.previousSibling().focus(); }
                                },
                                notification: {
                                    tooltip:{ 
                                        inribbon: true, 
                                        title: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_RIBBON_TIP_TITLE' i18n:catalogue='plugin.core-ui'/>", 
                                        text: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_RIBBON_TIP_DESCRIPTION' i18n:catalogue='plugin.core-ui'/>",
                                        image: Ametys.getPluginResourcesPrefix("core-ui") + "/img/uitool-notification/notification_48.png"
                                    },
                                    /* uitool-notification dependency is hard-coded in WorkspaceGenerator class */
                                    handler: function() { Ametys.tool.ToolsManager.openTool('uitool-notification'); }
                                },
                                searchMenu: {
                                    allowSearch: true,
                                    items: searchMenuItems
                                },
                                
                                <xsl:if test="user">
                                user: {
                                    fullName: "<xsl:value-of select="user/fullname"/>",
                                    login: "<xsl:value-of select="user/@login"/>",
                                    email: "<xsl:value-of select="user/email"/>",
                                    menu: { 
                                        items: userMenuItems
                                    }
                                    <xsl:if test="string(user/@login) != ''">,
                                        smallPhoto: Ametys.getPluginDirectPrefix('core-ui') + '/current-user/image_16',
                                        mediumPhoto: Ametys.getPluginDirectPrefix('core-ui') + '/current-user/image_32',
                                        largePhoto: Ametys.getPluginDirectPrefix('core-ui') + '/current-user/image_48',
                                        extraLargePhoto: Ametys.getPluginDirectPrefix('core-ui') + '/current-user/image_64'
                                    </xsl:if>
                                }
                                </xsl:if>
                            <xsl:text/>});<xsl:text/>
                    
                            /** Contextual tabs creation */<xsl:text/>
                            var tab;
                    <xsl:variable name="tabcount" select="count(ribbon/tabs/tab)"/>
                    <xsl:for-each select="ribbon/tabs/tab[not(preceding-sibling::*/@controlId = @controlId)]">
                        <xsl:variable name="tabController" select="../../tabsControls/tab[@id = current()/@controlId]"/>
                        
                        <xsl:if test="$tabController">
                            tab = Ext.create("<xsl:value-of select="$tabController/action/@class"/>", Ext.apply(<xsl:value-of select="$tabController/action"/>, {id: "<xsl:value-of select="$tabController/@id"/>", pluginName: "<xsl:value-of select="$tabController/@plugin"/>"}));
                            Ametys.ribbon.RibbonManager.registerTab(tab);
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:for-each select="ribbon/tabs/tab">
                        <xsl:sort select="not(not(@contextualColor))" order="descending"/>
                        <xsl:sort select="position()" data-type="number" order="descending"/>

                        <xsl:variable name="tabPos"><xsl:value-of select="$tabcount - position() + 1"/></xsl:variable>
                        <xsl:variable name="tabController" select="../../tabsControls/tab[@id = current()/@controlId]"/>
                        
                        <xsl:if test="$tabController">
                            <xsl:text />Ametys.ribbon.RibbonManager.getTab("<xsl:value-of select="$tabController/@id"/>").attachTab(tab_<xsl:value-of select="$tabPos"/>);
                        </xsl:if>
                    </xsl:for-each>                            
                            
                            /** Message target factories */<xsl:text/>
                            var mtFactory;
                            mtFactory = Ext.create("Ametys.message.factory.DefaultMessageTargetFactory", {pluginName: "core", id: "*"});
                            Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);
                            mtFactory = Ext.create("Ametys.tool.ToolMessageTargetFactory", {pluginName: "core", id: "tool"});
                            Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);
                    <xsl:for-each select="messagetarget-factories/messagetarget-factory">
                            mtFactory = Ext.create("<xsl:value-of select="action/@class"/>",  Ext.apply(<xsl:value-of select="action"/>, {pluginName: "<xsl:value-of select="@plugin"/>", id: "<xsl:value-of select="@id"/>"}));
                            Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);
                    </xsl:for-each>
                    
                           /** Relations handlers */<xsl:text/>
                           var relationHandler;
                    <xsl:for-each select="relations-handlers/relation-handler">
                           relationHandler = Ext.create("<xsl:value-of select="action/@class"/>",  Ext.apply(<xsl:value-of select="action"/>, {pluginName: "<xsl:value-of select="@plugin"/>", id: "<xsl:value-of select="@id"/>"}));
                           Ametys.relation.RelationManager.register(relationHandler);
                    </xsl:for-each>
                           
                           /** Widgets */<xsl:text/>
                           Ametys.form.WidgetManager._defaultWidgets = <xsl:value-of select="widgets/@default-widgets"/>;
                           Ametys.form.WidgetManager._defaultWidgetsForEnumeration = <xsl:value-of select="widgets/@default-widgets-enumerated"/>;
                           
                           var widgetEnumerated, widgetMultiple;
                    <xsl:for-each select="widgets/widget-wrapper">
                           widgetEnumerated = [<xsl:if test="@supports-enumerated='true'">true</xsl:if><xsl:if test="@supports-enumerated='true' and @supports-non-enumerated='true'">, </xsl:if><xsl:if test="@supports-non-enumerated='true'">false</xsl:if>]; 
                           widgetMultiple = [<xsl:if test="@supports-multiple='true'">true</xsl:if><xsl:if test="@supports-multiple='true' and @supports-non-multiple='true'">, </xsl:if><xsl:if test="@supports-non-multiple='true'">false</xsl:if>]; 
                           widgetFTypes = "<xsl:value-of select="@ftypes"/>".split(",");
                           
                           // Dynamically attach the xtype
                           Ext.ClassManager.aliasToName["widget.<xsl:value-of select="widget/@id"/>"] = "<xsl:value-of select="widget/action/@class"/>";
                           Ext.ClassManager.nameToAliases["<xsl:value-of select="widget/action/@class"/>"] = Ext.Array.from(Ext.ClassManager.nameToAliases["<xsl:value-of select="widget/action/@class"/>"]);
                           Ext.ClassManager.nameToAliases["<xsl:value-of select="widget/action/@class"/>"].push("widget.<xsl:value-of select="widget/@id"/>");
                           
                           for (var i = 0; i &lt; widgetFTypes.length; i++)
                           {
                                for (var j = 0; j &lt; widgetMultiple.length; j++)
                                {
                                    for (var k = 0; k &lt; widgetEnumerated.length; k++)
                                    {
                                       Ametys.form.WidgetManager.register("<xsl:value-of select="widget/@id"/>", widgetFTypes[i], widgetEnumerated[k], widgetMultiple[j]);
                                    }
                                }
                           }
                    </xsl:for-each>
                           
                            Ext.onReady(function ()
                            {
                                Ext.application({
                                    requires: ['Ext.container.Viewport'],
                                    name: 'AM',
                
                                    appFolder: 'ametys',
                                    enableQuickTips: false,
                                    launch: function() {
                                        if (window.errorMode) { return; }
                                        
                                        Ametys.tool.ToolsManager.getToolsLayout().setToolPolicy(Ametys.userprefs.UserPrefsDAO.getValue("tab-policy"));
                                        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, function(message) {
                                            if (message.getTargets(Ametys.message.MessageTarget.USER_PREFS).length > 0)
                                            {
                                                Ametys.tool.ToolsManager.getToolsLayout().setToolPolicy(Ametys.userprefs.UserPrefsDAO.getValue("tab-policy"));
                                            }
                                        }, window);
                                        
                                        Ext.create('Ext.container.Viewport', {
                                            hidden: true,
                                            hideMode: 'offsets',
                                            layout:  { type: 'vbox', align: 'stretch' },
                                            items: [
                                                ribbon,
                                                Ext.apply(Ametys.tool.ToolsManager.getToolsLayout().createLayout(), {flex: 1})
                                            ],
                                            listeners: {
                                                'afterrender': function() { 
                                                    if (window.errorMode) { return; }  
                                                    window.onerror = null;
                                                    <xsl:if test="$splashscreen != 'no'">destroyLoader();</xsl:if>
                                                    this.show(); 
                                                    
                                                    <xsl:call-template name="after-ametys-loaded-hook-js"/>
                                                } 
                                            }
                                        });

                                        /** Empty selection */
                                        Ext.create("Ametys.message.Message", {
                                            type: Ametys.message.Message.SELECTION_CHANGED,
                                            targets: [],
                                            callback: function()
                                            {
                                                if (window.errorMode) { return; }
                                                
                                                try
                                                {
                                                    Ametys.tool.ToolsManager.init({
                                                        autoRefreshingFactories: [ <xsl:for-each select="uitools-factories/refresh/uitool-factory">
                                                             <xsl:text/>"<xsl:value-of select="@id"/>"<xsl:if test="position() != last()">,</xsl:if>
                                                        </xsl:for-each> ],
                                                        autoOpenedTools: [ <xsl:for-each select="uitools-factories/default/uitool-factory">
                                                             <xsl:text/>{ toolId: "<xsl:value-of select="@id"/>", toolParams: {<xsl:value-of select="."/>} }<xsl:if test="position() != last()">,</xsl:if>
                                                        </xsl:for-each> ]
                                                    });
                                                    
                                                    <xsl:for-each select="uitools-factories/additionnal/uitool-factory">
                                                    Ametys.tool.ToolsManager.openTool("<xsl:value-of select="@id"/>", {<xsl:value-of select="."/>});
                                                    </xsl:for-each>
                                                                
                                                    Ametys.tool.ToolsManager.getToolsLayout().setAsInitialized();
                                                }
                                                catch (e)
                                                {
                                                    Ametys.userprefs.UserPrefsDAO.saveValues( { "workspace": {} }, function() { }, undefined, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS);
                                                    Ametys.shutdown("<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_FAIL_AFTERLOAD_TITLE' i18n:catalogue="plugin.core-ui"/>", "<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_FAIL_AFTERLOAD_TEXT' i18n:catalogue="plugin.core-ui"/>", "<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_FAIL_AFTERLOAD_ACTION' i18n:catalogue="plugin.core-ui"/>");
                                                }
                                            }
                                        });
                                    }
                                });
                            });
                    })();
                </script>
                
                <xsl:call-template name="after-workspace-load-hook"/>
    </xsl:template>

    <xsl:template name="ui-apptools-load">
        <!-- Ametys interface -->
            <!-- UserPrefs -->
            <script type="text/javascript">
                  <xsl:call-template name="ui-apptools-load-PrefContext"/>
            
                  var userPrefs = {};<xsl:text/>
                  
                  <xsl:for-each select="/Ametys/userprefs/*">
                          <xsl:variable name="name" select="local-name()"/>
                          <xsl:if test="count(preceding-sibling::*[local-name() = $name]) = 0 and not(ametys:requestParameter('ignore') = $name)">
                      userPrefs["<xsl:value-of select="$name"/>"] = [<xsl:text/>
                                  <xsl:for-each select="/Ametys/userprefs/*[local-name() = $name]">
                                    <xsl:text/>"<xsl:value-of select="."/>"<xsl:if test="position() != last()">,</xsl:if>
                                  </xsl:for-each>
                              <xsl:text/>];<xsl:text/>
                          </xsl:if>
                      </xsl:for-each><xsl:text/>
                  Ametys.userprefs.UserPrefsDAO.preload(userPrefs);
                  var upp = Ext.create('Ametys.userprefs.UserPrefsDAOStateProvider', { preference: 'workspace' })
                  Ext.state.Manager.setProvider(upp);
            </script>
            
            <xsl:call-template name="ui-apptools-load-toolsmanager"/>
    </xsl:template>

    <xsl:template name="ui-apptools-load-PrefContext">
                Ametys.userprefs.UserPrefsDAO.setDefaultPrefContext("<xsl:value-of select="/Ametys/userprefs/@prefContext"/>");
    </xsl:template>    
    
    <xsl:template name="ui-apptools-load-toolsmanager">
            <script type="text/javascript">
                Ametys.tool.ToolsManager.setToolsLayout("Ametys.ui.tool.layout.ZonedTabsToolsLayout", { 
                    initialized: false,
                    notoolselected: function () {
                        Ext.create("Ametys.message.Message", { type: Ametys.message.Message.SELECTION_CHANGED, targets: [] });
                    },
                    titleChangedCallback: function(title) {
                        // Ext.getCmp("ribbon").setTitle(title);
                    } 
                });
            </script>
    </xsl:template>

    <xsl:template name="ui-extension-load">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>

    <xsl:template name="ui-extension-after-static-load">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>
    <xsl:template name="after-workspace-load-hook">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>
    <xsl:template name="after-ametys-loaded-hook-js">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>
    
    
    <xsl:template name="app-context">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>
    
<!--     <xsl:template name="theme-scripts"> -->
<!--         <script absolute="true">/~cmd/extensions/sencha-fashion/fashion/fashion.js</script> -->
<!--         <script absolute="true">/~cmd/extensions/sencha-fashion/sass-compiler.js</script> -->
        
<!--         <script absolute="true">/ext/build/classic/theme-neptune/theme-neptune-debug.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroup.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/GroupScale.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/TabPanel.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ametys/ui/tool/layout/ZonedTabsToolsLayout.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ametys/grid/plugin/Multisort.js</script> -->

<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ext/tree/Panel.js</script> -->
<!--         <script absolute="true">/packages/local/theme-ametys-base/overrides/Ext/resizer/Splitter.js</script> -->
<!--     </xsl:template> -->
<!--     <xsl:template name="theme-styles"/>      -->
</xsl:stylesheet>
