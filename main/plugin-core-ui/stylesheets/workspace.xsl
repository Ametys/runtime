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
    <xsl:variable name="debug-mode" select="ametys:config('runtime.debug.ui')"/>
    
    <xsl:template name="head-title"><i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/></xsl:template>
    <xsl:template name="applicationTitle"><i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/></xsl:template>
    <xsl:template name="css-file">/plugins/core-ui/resources/css/special/splashscreen.css</xsl:template>
    
    <xsl:template name="uicall">
        <xsl:call-template name="kernel-base"/>
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
                
                document.body.parentNode.className += " error";
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
    </xsl:template>
    
    <xsl:template match="workspace">
                <noscript><i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></noscript>
                
                <xsl:call-template name="uicall"/>                

                <xsl:call-template name="ui-apptools-load"/>
               
                <xsl:call-template name="ui-extension-load"/>
                    
                <!-- UITools -->
                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="static-imports/import/scripts/file"/>
                      <xsl:with-param name="css" select="static-imports/import/css/file"/>
                </xsl:call-template>                
                
                <script type="text/javascript">
                    Ametys.setAppParameter("debug.ui", "<xsl:value-of select="$debug-mode"/>");        

                    Ametys.setAppParameter("user", {
                        login: "<xsl:value-of select="user/@login"/>",
                        firstname: "<xsl:value-of select="user/firstname"/>",
                        lastname: "<xsl:value-of select="user/lastname"/>",
                        fullname: "<xsl:value-of select="user/firstname"/><xsl:text> </xsl:text><xsl:value-of select="user/lastname"/>" ,
                        email:  "<xsl:value-of select="user/email"/>",
                        locale: Ametys.LANGUAGE_CODE
                    });
                </script>
                
                <xsl:call-template name="ui-extension-after-static-load"/>
                
                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="(ribbon/controls/control/scripts/file | ribbon/tabsControls/tab/scripts/file | uitools-factories/uitool-factory/scripts/file | messagetarget-factories/messagetarget-factory/scripts/file | relations-handlers/relation-handler/scripts/file | widgets/widget-wrapper/widget/scripts/file | app-menu/item/scripts/file)[not(. = current()/static-imports/import/scripts/file)]"/>
                      <xsl:with-param name="css" select="(ribbon/controls/control/css/file | ribbon/tabsControls/tab/css/file | uitools-factories/uitool-factory/css/file | messagetarget-factories/messagetarget-factory/css/file | relations-handlers/relation-handler/css/file | widgets/widget-wrapper/widget/css/file | app-menu/item/css/file)[not(. = current()/static-imports/import/css/file)]"/>
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
                               control = Ext.create("<xsl:value-of select="action/@class"/>", Ext.apply(<xsl:value-of select="action"/>, {id: "<xsl:value-of select="@id"/>", pluginName: "<xsl:value-of select="@plugin"/>"}));
                               Ametys.ribbon.RibbonManager.registerElement(control);
                    </xsl:for-each>

                            var ribbonItems = [];                   

                    <xsl:for-each select="ribbon/tabs/tab[groups/group/medium//control/@id = /Ametys/workspace/ribbon/controls/control/@id]">
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
                                   <xsl:text/>Ametys.ribbon.RibbonManager.getElement("<xsl:value-of select="$id"/>").addUI("large")<xsl:text/> 
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
                                       <xsl:text/>Ametys.ribbon.RibbonManager.getElement("<xsl:value-of select="$id"/>").addUI("<xsl:value-of select="../@size"/>", <xsl:value-of select="@colspan"/>)<xsl:text/> 
                                                </xsl:when>
                                                <xsl:when test="local-name()='toolbar' and *[@id = /Ametys/workspace/ribbon/controls/control/@id]">
                                                <xsl:text/>{
                                                    colspan: <xsl:value-of select="@colspan"/>,
                                                    items: [<xsl:text/>
                                                <xsl:for-each select="*[@id = /Ametys/workspace/ribbon/controls/control/@id]"><xsl:if test="position() != 1">, </xsl:if>
                                                    <xsl:variable name="id" select="@id"/>
                                            <xsl:text/>Ametys.ribbon.RibbonManager.getElement("<xsl:value-of select="$id"/>").addUI("very-small", <xsl:value-of select="@colspan"/>)<xsl:text/> 
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
                                        fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>.listeners = {'dialogboxlaunch': Ext.bind(Ametys.executeFunctionByName, Ametys, [controlCfg['dialog-box-launcher'], null, null, Ametys.ribbon.RibbonManager.getElement("<xsl:value-of select="$dialogBoxLauncher"/>")], false)};
                                    }
                                </xsl:if>
                                
                                tab_<xsl:value-of select="$tabPos"/>.add(fgp_<xsl:value-of select="$tabPos"/>_<xsl:value-of select="$groupPos"/>);<xsl:text/>
                            </xsl:for-each>
                            
                            ribbonItems.push(tab_<xsl:value-of select="$tabPos"/>);<xsl:text/>
                    </xsl:for-each>

                            /** Ribbon creation */
                            var menuItems = [];
                            <xsl:for-each select="app-menu/item">
                                (function () {
                                    var actionParam = <xsl:value-of select="action"/>;
                                    menuItems.push({
                                        text: actionParam['label'],
                                        icon: Ametys.CONTEXT_PATH + actionParam['icon-small'],
                                        handler: Ametys.getFunctionByName(actionParam['action']),
                                        tooltip: {
                                            title: actionParam['label'],
                                            text: actionParam['description'],
                                            image: Ametys.CONTEXT_PATH + actionParam['icon-large'],
                                            inribbon: false
                                        }
                                    });
                                })();
                            </xsl:for-each>
                            
                            var ribbon = Ext.create("Ametys.ui.fluent.ribbon.Ribbon", {<xsl:text/>
                                <xsl:text/>applicationTitle: '&lt;span class="x-fluent-tab-panel-header-title-extension"&gt;<xsl:call-template name="applicationTitle"/>&lt;/span&gt;',<xsl:text/>
                                id: 'ribbon',
                                
                                mainButton: {
			                        xtype: 'button',
			                        text: 'Ametys',
			                        /*tooltip: {
			                             title: "", 
			                             image: "", 
			                             text: "", 
			                             inribbon: true
			                        },*/
			                        menu: menuItems.length == 0 ? null : {
			                             items: menuItems
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
                                
                                /*
                                help: {
                                    handler: function() { alert('help'); },
                                    tooltip:{ inribbon: true, text: "A little bit of help?" }
                                },
                                notification: {
			                          tooltip:{ inribbon: true, text: "A describtive text" }
			                          handler: function() {  }
			                    },
                                */
                                <xsl:if test="user">
                                user: {
                                    fullName: "<xsl:value-of select="user/firstname"/>&#160;<xsl:value-of select="user/lastname"/>",
                                    login: "<xsl:value-of select="user/@login"/>",
                                    email: "<xsl:value-of select="user/email"/>"
                                    <xsl:if test="string-length(user/email) > 1">,
                                        <xsl:variable name="gravatarHash" select="digest:md5Hex(user/email)"/>
                                        smallPhoto: "http://www.gravatar.com/avatar/<xsl:value-of select="$gravatarHash"/>?s=16&amp;d=blank",
                                        mediumPhoto: "http://www.gravatar.com/avatar/<xsl:value-of select="$gravatarHash"/>?s=32&amp;d=mm",
                                        largePhoto: "http://www.gravatar.com/avatar/<xsl:value-of select="$gravatarHash"/>?s=48&amp;d=mm",
                                        extraLargePhoto: "http://www.gravatar.com/avatar/<xsl:value-of select="$gravatarHash"/>?s=64&amp;d=mm"
                                    </xsl:if>
                                }
                                </xsl:if>
                            <xsl:text/>}, {});<xsl:text/>
                    
                            /** Contextual tabs creation */<xsl:text/>
                            var tab;
                    <xsl:variable name="tabcount" select="count(ribbon/tabs/tab[groups/group/medium//control/@id = /Ametys/workspace/ribbon/controls/control/@id])"/>
                    <xsl:for-each select="ribbon/tabs/tab[groups/group/medium//control/@id = /Ametys/workspace/ribbon/controls/control/@id]">
                        <xsl:sort select="not(not(@contextualColor))" order="descending"/>
                        <xsl:sort select="position()" data-type="number" order="descending"/>

                        <xsl:variable name="tabPos"><xsl:value-of select="$tabcount - position() + 1"/></xsl:variable>
                        <xsl:variable name="tabController" select="../../tabsControls/tab[@id = current()/@id]"/>
                        
                        <xsl:if test="$tabController">
                            tab = Ext.create("<xsl:value-of select="$tabController/action/@class"/>", Ext.apply(<xsl:value-of select="$tabController/action"/>, {id: "<xsl:value-of select="$tabController/@id"/>", pluginName: "<xsl:value-of select="$tabController/@plugin"/>", tabPanel: tab_<xsl:value-of select="$tabPos"/>}));
                            Ametys.ribbon.RibbonManager.registerElement(tab);
                        </xsl:if>
                    </xsl:for-each>                            
                            
                            /** Message target factories */<xsl:text/>
                            var mtFactory;
                            mtFactory = Ext.create("Ametys.message.factory.DefaultMessageTargetFactory", Ext.apply({type: "*"}, {pluginName: "core", id: null}));
                            Ametys.message.MessageTargetFactory.registerTargetFactory(mtFactory);
                            mtFactory = Ext.create("Ametys.tool.ToolMessageTargetFactory", Ext.apply({type: "tool"}, {pluginName: "core"}));
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
                           Ext.ClassManager.nameToAliases["<xsl:value-of select="widget/action/@class"/>"] = Ext.Array.from(Ext.ClassManager.nameToAliases['Ametys.runtime.form.widget.Text']).push("widget.<xsl:value-of select="widget/@id"/>");
                           
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
                                                             <xsl:text/>{ role: "<xsl:value-of select="@id"/>", toolParams: {<xsl:value-of select="."/>} }<xsl:if test="position() != last()">,</xsl:if>
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
                Ametys.tool.ToolsManager.setToolsLayout("Ametys.ui.tool.layout.ZonedTabsToolsLayout", { initialized: false });
            </script>
    </xsl:template>

    <xsl:template name="ui-extension-load">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>

    <xsl:template name="ui-extension-after-static-load">
        <!-- Keep empty. Here for inheritance purpose. -->
    </xsl:template>
</xsl:stylesheet>
