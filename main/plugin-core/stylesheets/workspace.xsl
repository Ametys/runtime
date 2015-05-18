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
                xmlns:ametys="org.ametys.runtime.plugins.core.ui.AmetysXSLTHelper"
                extension-element-prefixes="exslt">

    <xsl:import href="kernel://stylesheets/kernel.xsl"/>    
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    <xsl:param name="max-upload-size"/>
    <xsl:param name="debug-mode-config">2</xsl:param>
    <xsl:param name="debug-mode-request"/>
    <xsl:param name="splashscreen"/>
    <xsl:param name="skip-navigator-compatibility">false</xsl:param>
    
    <xsl:variable name="callback"><script type="text/javascript">window.setTimeout(_updateProgressBar, 1);</script></xsl:variable>

	<xsl:variable name="debug-mode">
		<xsl:choose>
			<xsl:when test="$debug-mode-request = ''"><xsl:value-of select="$debug-mode-config"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="$debug-mode-request"/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>    
    <xsl:variable name="workspace-resources" select="concat($contextPath, $workspaceURI, '/resources')"/>

    <xsl:template name="title">
    	<title><i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/></title>
    </xsl:template>
    <xsl:template name="applicationTitle">
    	<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
    </xsl:template>
    <xsl:template name="productTitle">
    	<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
    </xsl:template>
    
    
    
    <xsl:template name="uicall">
	    <xsl:call-template name="kernel-base">
            <xsl:with-param name="theme">gray</xsl:with-param>
	        <xsl:with-param name="load-cb" select="$callback"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="workspace">
    	<html>
			<head>
				<meta http-equiv="X-UA-Compatible" content="IE=10" />
				<xsl:call-template name="title"/>
				
				<link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
        		<link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
				
				<xsl:if test="$splashscreen != 'no'">
					<style type="text/css" media="screen">
				        .splashscreen-center {float:left; width: 430px; height: 28px; background: url("<xsl:value-of select="$workspace-resources"/>/img/splashscreen/bar-available.png") repeat-x scroll left top transparent; margin-right: auto; margin-left: auto; -webkit-border-radius: 10px; -moz-border-radius: 10px; padding: 0;}
				        .splashscreen-progress {height: 28px; background: url("<xsl:value-of select="$workspace-resources"/>/img/splashscreen/bar-loaded.png") repeat-x scroll left top transparent; -webkit-border-radius: 10px; -moz-border-radius: 10px;}
				        .splashscreen-loading {text-align: right; font-family: arial,sans-serif; font-style: italic; color: #404040; font-size: 11px; margin:0; padding-right: 30px;} 
			      
			      		.splashscreen { width: 500px; height: 400px; background-image: url("<xsl:value-of select="$workspace-resources"/>/img/splashscreen/splashscreen.png"); color: #fff; margin-right: auto; margin-left: auto; margin-top: 200px;} 
				        .splashscreen-version {text-align: right; padding-top: 260px; margin-right: 30px; color: #1C58A0; font-family: arial,sans-serif; letter-spacing: 1px;}
				        .splashscreen-version .app-version {font-size: 0.7em; color:#7F7F7F;}
			       		.splashscreen-progressbar {margin-top: 20px !important; margin-left: 20px; margin-right: 20px;}
			       		.splashscreen-img {padding: 2px; background: url("<xsl:value-of select="$workspace-resources"/>/img/splashscreen/loading.gif") no-repeat scroll left top transparent;}
			       	 </style> 
				</xsl:if>

			</head>
			
			<body>
				<noscript><i18n:text i18n:key="WORKSPACE_CMS_MAIN_ERROR_NOJS" i18n:catalogue="workspace.cms"/></noscript>
		        
				<xsl:if test="$splashscreen != 'no'">
                    <xsl:call-template name="app-splashscreen"/>
                </xsl:if>
                
				<xsl:call-template name="uicall"/>                

                <xsl:call-template name="ui-apptools-load"/>
               
                <xsl:call-template name="ui-extension-load"/>
                    
				<!-- UITools -->
                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="static-imports/import/scripts/file"/>
                      <xsl:with-param name="css" select="static-imports/import/css/file"/>
                      <xsl:with-param name="load-cb" select="$callback"/>
                </xsl:call-template>                

                <xsl:call-template name="kernel-load">
                      <xsl:with-param name="scripts" select="ribbon/controls/control/scripts/file | ribbon/tabsControls/tab/scripts/file | uitools-factories/uitool-factory/scripts/file | messagetarget-factories/messagetarget-factory/scripts/file | relations-handlers/relation-handler/scripts/file | widgets/widget-wrapper/widget/scripts/file | app-menu/item/scripts/file"/>
                      <xsl:with-param name="css" select="ribbon/controls/control/css/file | ribbon/tabsControls/tab/css/file | uitools-factories/uitool-factory/css/file | messagetarget-factories/messagetarget-factory/css/file | relations-handlers/relation-handler/css/file | widgets/widget-wrapper/widget/css/file | app-menu/item/css/file"/>
                      <xsl:with-param name="load-cb" select="$callback"/>
                </xsl:call-template>                

				<script type="text/javascript">
                    Ametys.setAppParameter("debug.ui", "<xsl:value-of select="$debug-mode"/>");        
                    
                    Ametys.setAppParameter("user", {
                    	login: "<xsl:value-of select="user/@login"/>",
                    	firstname: "<xsl:value-of select="user/firstname"/>",
                    	lastname: "<xsl:value-of select="user/lastname"/>",
                    	fullname: "<xsl:value-of select="user/firstname"/><xsl:text> </xsl:text><xsl:value-of select="user/lastname"/>" ,
                    	email:  "<xsl:value-of select="user/email"/>",
                    	locale: "<i18n:text i18n:key='KERNEL_LANGUAGE_CODE' catalogue='kernel'/>"
                    });
                            
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
                                menu: {<xsl:text/>
                                    icon: '<xsl:value-of select="$workspace-resources"/>/img/ametys.gif',
                                    items: menuItems.length == 0 ? null : menuItems
                                },<xsl:text/>
                                <xsl:text/>items: ribbonItems<xsl:text/>
	                            /*, help: {
	                                handler: function() { alert('help'); },
	                                tooltip: "A little bit of help?"
	                            },
	                            user: {
	                                text: "<xsl:value-of select="user/firstname"/>&#160;<xsl:value-of select="user/lastname"/>",
	                                menu: { 
	                                   items: [ 
	                                       {text: "<xsl:value-of select="user/firstname"/>&#160;<xsl:value-of select="user/lastname"/> (<xsl:value-of select="user/@login"/>)" }
	                                       <xsl:if test="user/email">, {text: "<xsl:value-of select="user/email"/>" }</xsl:if>
	                                   ]
	                                }
	                            },*/
                            <xsl:text/>}, {region: 'north'});<xsl:text/>
					
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
                           Ametys.runtime.form.WidgetManager._defaultWidgets = <xsl:value-of select="widgets/@default-widgets"/>;
                           Ametys.runtime.form.WidgetManager._defaultWidgetsForEnumeration = <xsl:value-of select="widgets/@default-widgets-enumerated"/>;
                           
                           var widgetEnumerated, widgetMultiple;
                    <xsl:for-each select="widgets/widget-wrapper">
                           widgetEnumerated = [<xsl:if test="@supports-enumerated='true'">true</xsl:if><xsl:if test="@supports-enumerated='true' and @supports-non-enumerated='true'">, </xsl:if><xsl:if test="@supports-non-enumerated='true'">false</xsl:if>]; 
                           widgetMultiple = [<xsl:if test="@supports-multiple='true'">true</xsl:if><xsl:if test="@supports-multiple='true' and @supports-non-multiple='true'">, </xsl:if><xsl:if test="@supports-non-multiple='true'">false</xsl:if>]; 
                           widgetFTypes = "<xsl:value-of select="@ftypes"/>".split(",");
                           
                           <xsl:value-of select="widget/action/@class"/>.xtype = "widget.<xsl:value-of select="widget/@id"/>";
                           for (var i = 0; i &lt; widgetFTypes.length; i++)
                           {
                                for (var j = 0; j &lt; widgetMultiple.length; j++)
                                {
	                                for (var k = 0; k &lt; widgetEnumerated.length; k++)
	                                {
                                       Ametys.runtime.form.WidgetManager.register("<xsl:value-of select="widget/@id"/>", widgetFTypes[i], widgetEnumerated[k], widgetMultiple[j]);
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
				                        Ext.create('Ext.container.Viewport', {
						                    hidden: true,
	                                        hideMode: 'offsets',
				                            layout: 'border',
				                            items: [
				                                ribbon,
				                                Ametys.tool.ToolsManager.getToolsLayout().createLayout()
				                            ],
                                            listeners: {'afterrender': function() { <xsl:if test="$splashscreen != 'no'">hideSplashscreen(); </xsl:if>this.show()}}
				                        });

		                                /** Empty selection */
		                                Ext.create("Ametys.message.Message", {
		                                    type: Ametys.message.Message.SELECTION_CHANGED,
		                                    targets: [],
		                                    callback: function()
		                                    {
		                                        Ext.suspendLayouts();  
		                                    
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
		                                        
		                                        Ext.resumeLayouts(true);
		                                    }
		                                });
				                    }
				                });
                            });
                    })();
				</script>
			</body>
		</html>
    </xsl:template>
    
    <xsl:template name="app-splashscreen">
    	<div id="splashscreen" class="splashscreen">
        	<p class="splashscreen-version">
        		<xsl:call-template name="productTitle"/>
        		<xsl:if test="/Ametys/Versions/Component[Name = 'CMS']/Version"><br/><span class="app-version"><i18n:text i18n:key="WORKSPACE_CMS_SPLASHSCREEN_APP_AMETYS_VERSION" catalogue="workspace.cms"/><xsl:value-of select="/Ametys/Versions/Component[Name = 'CMS']/Version"/></span></xsl:if>
        	</p>
        	
        	<div class="splashscreen-progressbar">
	        	<img id="bar-left" height="28px" width="14px" style="float:left;" src="{$workspace-resources}/img/splashscreen/bar-left.png" alt=""/>
	        	<div class="splashscreen-center"><div class="splashscreen-progress" id="progress" style="width:0px"></div></div>
	        	<img id="bar-right" height="28px" width="14px" src="{$workspace-resources}/img/splashscreen/bar-right.png" alt=""/>
        	</div>
        	
	       	<p class="splashscreen-loading">
	       		<span class="splashscreen-img">&#160;&#160;&#160;&#160;&#160;&#160;</span>
	       		<i18n:text i18n:key="WORKSPACE_CMS_SPLASHSCREEN_LOADING" catalogue="workspace.cms"/>
	       		<span id="loaded" class="splashscreen-loaded">1%</span>
	       	</p>
        </div>
		        
        <script type="text/javascript">
        	var splashscreen; 
        	function showSplashscreen()
	        {
	        	document.body.style.background = 'url("<xsl:value-of select="$contextPath"/>/kernel/resources/img/home/bg.gif") repeat-x left top #033059';
	        	splashscreen = document.getElementById('splashscreen'); 
	        	if (splashscreen)
	            { 
	         		splashscreen.style.display='block';
	         	} 
	        }
	        showSplashscreen();
	        function hideSplashscreen()
	        { 
	            try
	            { 
	                if (splashscreen)
	                { 
	                    splashscreen.style.display = 'none'; 
	                    while (splashscreen.hasChildNodes()) splashscreen.removeChild(splashscreen.lastChild); 
	                    splashscreen.parentNode.removeChild(splashscreen); 
	                } 
	                
	               document.body.style.background = "none";
	            }
	            catch(e){} 
	            finally{ splashscreen = null;} 
	        } 
        </script>

        <script type="text/javascript">
        	var count = 0; // scripts and css loaded
            var toLoad = 1 + 20; // scripts and css to load (css is only one file)
            // FIXME TODO 20 is false
            
			function _updateProgressBar()
			{
				count++;
				count = Math.min(count, toLoad);
				var width = Math.floor(count * 430 / toLoad);
				document.getElementById('progress').style.width = width + "px";
				
				var pc = Math.floor(count * 100 / toLoad);
				if (pc >= 100)
				{
					pc = 100;
				}
				
				document.getElementById('loaded').innerHTML = pc + '%';
				if (pc == 100)
				{
					document.getElementById('bar-right').src = "<xsl:value-of select="$workspace-resources"/>/img/splashscreen/bar-right-loaded.png";
				}
			}
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
			
			<!-- JS -->
<!-- 			<script type="text/javascript" src="{$workspace-resources}/js/Ametys/cms/form/WidgetManager.js"/> -->
			<script type="text/javascript" src="{$contextPath}/plugins/core/resources/js/Ametys/runtime/form/WidgetManager.js"/>
			<script type="text/javascript" src="{$workspace-resources}/js/Ametys/cms/form/FormEditionPanel.js"/>
			<script type="text/javascript" src="{$workspace-resources}/js/Ametys/cms/form/Repeater.js"/>
            <script type="text/javascript" src="{$workspace-resources}/js/Ametys/cms/form/widget/RichTextConfiguration.js"/>

			<xsl:call-template name="ui-apptools-load-toolsmanager"/>
    </xsl:template>

    <xsl:template name="ui-apptools-load-PrefContext">
                  Ametys.userprefs.UserPrefsDAO.setDefaultPrefContext("/ametys");
    </xsl:template>    
    
    <xsl:template name="ui-apptools-load-toolsmanager">
            <script type="text/javascript">
                Ametys.tool.ToolsManager.setToolsLayout("Ametys.ui.tool.layout.ZonedTabsToolsLayout", { initialized: false });
            </script>
    </xsl:template>
    
    <xsl:template name="ui-extension-load">
    	<!-- Keep empty. Here for inheritance purpose. -->
	</xsl:template>

</xsl:stylesheet>
