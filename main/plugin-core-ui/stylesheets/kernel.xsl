<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2015 Anyware Services

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
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:exslt="http://exslt.org/common"
    xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper">
    
	<xsl:import href="kernel-browsers.xsl"/>
	
    <xsl:variable name="theme" select="ametys:workspaceTheme()"/>
    <xsl:variable name="themeURL" select="ametys:workspaceThemeURL()"/>
    <xsl:variable name="uxtheme">neptune</xsl:variable>

	<xsl:variable name="language-code" select="ametys:translate('plugin.core-ui:PLUGINS_CORE_UI_LANGUAGE_CODE')"/>
    <xsl:variable name="rtl" select="ametys:translate('plugin.core-ui:PLUGINS_CORE_UI_LANGUAGE_RTL') = 'true'"/>
    <xsl:variable name="debug-mode" select="ametys:isDeveloperMode()"/>
	
    <!-- +
         | Load and initialize all scripts for UI
         | @param {String} plugins-direct-prefix=/plugins Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/plugins' 
         | @param {String} plugins-wrapped-prefix=/_plugins Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/_plugins'
         | @param {String} authorized-browsers Optionnaly, you can provide such a string :
		 | 					{
  		 |                     'supported': { 'ie': '8-0', 'ff': '12-0', 'sa': '6-0', 'ch': '18-0', 'op': '12-0', 'ed': '12-0' },     // List of supported browsers with their versions
		 | 					   'not-supported': { 'ie': '0-7', 'ff': '0-11', 'sa': '0-5', 'ch': '0-17', 'op': '0-11' },	              // List of non supported browsers with their versions
 		 | 					   'warning-redirection': "browser-warning.html",			                                              // Redirection when the browser may be supported
		 | 					   'failure-redirection': "browser-failure.html"                                                          // Redirection when the browser is not supported 
		 |                  }			
		 |					If window.ametys_authorized_browsers is empty, no browser test will be done.<br/>
 		 | 					When a supported browser is detected, everything goes on normally.<br/>
		 | 					When a not-supported browser is detected, the application is redirected to the failure rediction url. Where a message should indicates to use a supported browser.<br/>
		 | 					When another browser is detected, the application is redirected to the warning redirection url. Where a message should indicated to use a supported browser, but the user could enforce the navigation (setting a cookie 'ametys.accept.non.supported.navigators' to 'on').<br/>
		 |					Keys for browsers are 'ie' (Microsoft Internet Explorer), 'ff' (Mozilla Firefox), 'ch' (Google Chrome), 'sa' (Apple Safari), 'op' (Opera) and 'ed' (Microsoft Edge).<br/>
		 |					Versions can be a single number '3' or '3.5', or can be an interleave where 0 is the infinity '0-6' means all versions before 6 and 6 included, '6-0' means all versions after 6 and 6 included.
		 |                  The default value displayed above is the requirements of ExtJS version.
         |
         | @param {String} theme=neptune The ExtJS theme to load
         + -->
    <xsl:template name="kernel-base-js">
        <xsl:param name="plugins-direct-prefix">/plugins</xsl:param>
        <xsl:param name="plugins-wrapped-prefix">/_plugins</xsl:param>
		<xsl:param name="authorized-browsers">
		     {
		          'supported': { ch: '18-0', 'ed': '12-0', 'ie': '10-0', 'ff': '12-0' },
		          'not-supported': { 'ie': '0-9.99', 'ff': '0-11.99', 'sa': '0-5.99', 'ch': '0-17.99', 'op': '0-11.99' },
		  		  'failure-redirection': "/_admin/public/browser-unsupported.html"
		     }
		</xsl:param>
        
		<xsl:variable name="context-path" select="ametys:uriPrefix(false())"/>
		<xsl:variable name="workspace-name" select="ametys:workspaceName()"/>
		<xsl:variable name="workspace-prefix" select="ametys:workspacePrefix()"/>
		<xsl:variable name="max-upload-size" select="ametys:config('runtime.upload.max-size')"/>

		<xsl:call-template name="kernel-browsers">
			<xsl:with-param name="authorized-browsers" select="$authorized-browsers"/>
		</xsl:call-template>
	
		<script type="text/javascript">
			// Theses options are here to initialize the Ametys object.
			// Do not use theses since their are removed during Ametys initialization process
			window.ametys_opts = {
				"plugins-direct-prefix": "<xsl:value-of select='$plugins-direct-prefix'/>",
				"plugins-wrapped-prefix": "<xsl:value-of select='$plugins-wrapped-prefix'/>",
				"debug-mode": <xsl:value-of select='$debug-mode'/>,
				"context-path": "<xsl:value-of select='$context-path'/>",
				"workspace-name": "<xsl:value-of select='$workspace-name'/>",
				"workspace-prefix": "<xsl:value-of select='$workspace-prefix'/>",
				"max-upload-size": "<xsl:value-of select='$max-upload-size'/>",
				"language-code": "<xsl:copy-of select='$language-code'/>"
			}
        </script>
        
       	<xsl:variable name="scripts">
       		<script debug="false" rtl="false">/plugins/extjs6/resources/ext-all.js</script>
       		<script debug="true" rtl="false">/plugins/extjs6/resources/ext-all-debug.js</script>
       		<script debug="false" rtl="true">/plugins/extjs6/resources/ext-all-rtl.js</script>
       		<script debug="true" rtl="true">/plugins/extjs6/resources/ext-all-rtl-debug.js</script>
            <script lang="true" debug="false">
                <lang code="en" default="true">/plugins/extjs6/resources/classic/locale/locale-en.js</lang>
                <lang code="fr">/plugins/extjs6/resources/classic/locale/locale-fr.js</lang>
                <lang code="es">/plugins/extjs6/resources/classic/locale/locale-es.js</lang>
            </script>
            <script lang="true" debug="true">
                <lang code="en" default="true">/plugins/extjs6/resources/classic/locale/locale-en-debug.js</lang>
                <lang code="fr">/plugins/extjs6/resources/classic/locale/locale-fr-debug.js</lang>
                <lang code="es">/plugins/extjs6/resources/classic/locale/locale-es-debug.js</lang>
            </script>
            
            <xsl:call-template name="theme-scripts" />
            
            <script debug="false">/plugins/extjs6/resources/packages/ux/classic/ux.js</script>
            <script debug="true">/plugins/extjs6/resources/packages/ux/classic/ux-debug.js</script>
            
			<script>/plugins/core-ui/resources/js/Ext.fixes.js</script>
            <script>/plugins/core-ui/resources/js/Ext.enhancements.js</script>
            
            <script>/plugins/core-ui/resources/js/lunr/lunr.min.js</script>
	        <script>/plugins/core-ui/resources/js/lunr/lunr-ametys.js</script>
            <script lang="true">
                <lang code="en" default="true">/plugins/core-ui/resources/js/lunr/lunr-ametys-en.js</lang>
                <lang code="fr">/plugins/core-ui/resources/js/lunr/lunr-ametys-fr.js</lang>
                <lang code="es">/plugins/core-ui/resources/js/lunr/lunr-ametys-es.js</lang>
            </script>
            <script lang="true">
                <lang code="es">/plugins/core-ui/resources/js/lunr/lunr.stemmer.support.min.js</lang>
                <lang code="fr">/plugins/core-ui/resources/js/lunr/lunr.stemmer.support.min.js</lang>
            </script>
            <script lang="true">
                <lang code="es">/plugins/core-ui/resources/js/lunr/lunr.es.min.js</lang>
                <lang code="fr">/plugins/core-ui/resources/js/lunr/lunr.fr.min.js</lang>
            </script>

			<xsl:call-template name="ametys-scripts"/>
	    </xsl:variable>

        <xsl:call-template name="kernel-load-js">
            <xsl:with-param name="scripts" select="exslt:node-set($scripts)/*"/>
        </xsl:call-template>
    </xsl:template>
            
    <xsl:template name="kernel-base-css">
		<xsl:variable name="css">
            <xsl:call-template name="theme-styles" />
            
            <css>/plugins/codemirror/resources/css/codemirror.css</css>
            <css>/plugins/core-ui/resources/font/ametys/AmetysIcon.css</css>
		</xsl:variable>

		<xsl:call-template name="kernel-load-css">
			<xsl:with-param name="css" select="exslt:node-set($css)/*"/>
		</xsl:call-template>
    </xsl:template>
    
    <xsl:template name="theme-scripts">
        <script debug="false"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>.js</script>
        <script debug="true"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>-debug.js</script>
    </xsl:template>
    
    <xsl:template name="ametys-scripts">
        <script>/plugins/core-ui/resources/js/Ametys.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/mask/GlobalLoadMask.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/log/Logger.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/log/Logger/Entry.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/log/LoggerFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/log/ErrorDialog.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/window/DialogBox.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/window/MessageBox.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/form/AbstractField.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/AbstractFieldsWrapper.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/AbstractQueryableComboBox.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/DateTime.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/StringTime.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/Password.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/ChangePassword.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/ReferencedNumberField.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/RichText.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/RichText/SplitterTracker.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/TextArea.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/ColorSelector.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/SelectUserDirectory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/SelectGroupDirectories.js</script>
        <script debug="true">/plugins/tiny_mce/resources/js/tinymce.js</script>
        <script debug="false">/plugins/tiny_mce/resources/js/tinymce.min.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/field/Code.js</script>
        <script>/plugins/codemirror/resources/js/codemirror.js</script>
        <script>/plugins/codemirror/resources/js/addon/edit/matchbrackets.js</script>
        <script>/plugins/codemirror/resources/js/addon/selection/active-line.js</script>
        <script>/plugins/codemirror/resources/js/mode/xml/xml.js</script>
        <script>/plugins/codemirror/resources/js/mode/javascript/javascript.js</script>
        <script>/plugins/codemirror/resources/js/mode/css/css.js</script>
        <script>/plugins/codemirror/resources/js/mode/htmlmixed/htmlmixed.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/Repeater.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/FieldChecker.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/FieldCheckersManager.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/TableOfContents.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/SaveHelper.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/SaveHelper/SaveErrorDialog.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/form/SaveHelper/SaveBeforeQuitDialog.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/data/ServerCaller.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/data/ServerComm.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/data/ServerCommProxy.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/data/ServerComm/TimeoutDialog.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/grid/plugin/Multisort.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/tip/Tooltip.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/misc/Badge.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Title.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroupContainer.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroup.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/MessageContainer.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/SearchMenu.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator/Notification.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator/Toast.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/TabPanel.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Panel.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Group.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/GroupScale.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/GroupScalePart.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/RibbonButtonMixin.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/Button.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/SplitButton.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/Toolbar.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/gallery/MenuPanel.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/ToolPanel.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/ToolsLayout.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/ZoneTabsToolsPanel.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/PlaceHolder.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/Container.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/ZonedTabsDD.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/FloatingSplitterTracker.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/message/Message.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/message/MessageBus.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/message/MessageTarget.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/message/MessageTargetHelper.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/message/MessageTargetFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/message/factory/DefaultMessageTargetFactory.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/relation/RelationManager.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/RelationHandler.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/RelationPoint.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/Relation.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysDropZone.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysViewDragZone.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDragDrop.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDragZone.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDropZone.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysGridViewDragDrop.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysGridViewDropZone.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/RibbonManager.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/RibbonElementController.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/RibbonTabController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/tab/TabController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/tab/EditionTabController.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/RibbonUIController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/CommonController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/FieldController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/ButtonController.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/button/OpenToolButtonController.js</script>
    
        <script>/plugins/core-ui/resources/js/Ametys/userprefs/UserPrefsDAO.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/userprefs/UserPrefsDAOStateProvider.js</script>

        <script>/plugins/core-ui/resources/js/Ametys/tool/ToolsManager.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/ToolFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/factory/BasicToolFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/factory/UniqueToolFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/ToolMessageTargetFactory.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/Tool.js</script>
        <script>/plugins/core-ui/resources/js/Ametys/tool/SelectionTool.js</script>
        
        <script>/plugins/core-ui/resources/js/Ametys/form/WidgetManager.js</script>
    </xsl:template>
    
    <xsl:template name="theme-styles">
        <css data-donotminimize="true" debug="false" rtl="false"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>-all.css</css>
        <css data-donotminimize="true" debug="false" rtl="true"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>-all-rtl.css</css>
        <css data-donotminimize="true" debug="true" rtl="false"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>-all-debug.css</css>
        <css data-donotminimize="true" debug="true" rtl="true"><xsl:value-of select="$themeURL"/>/theme-<xsl:value-of select="$theme"/>-all-rtl-debug.css</css>
        <css debug="false" rtl="false">/plugins/extjs6/resources/packages/ux/classic/<xsl:value-of select="$uxtheme"/>/resources/ux-all.css</css>
        <css debug="false" rtl="true">/plugins/extjs6/resources/packages/ux/classic/<xsl:value-of select="$uxtheme"/>/resources/ux-all-rtl.css</css>
        <css debug="true" rtl="false">/plugins/extjs6/resources/packages/ux/classic/<xsl:value-of select="$uxtheme"/>/resources/ux-all-debug.css</css>
        <css debug="true" rtl="true">/plugins/extjs6/resources/packages/ux/classic/<xsl:value-of select="$uxtheme"/>/resources/ux-all-rtl-debug.css</css>
        <css debug="false" rtl="false">/plugins/extjs6/resources/packages/charts/classic/<xsl:value-of select="$uxtheme"/>/resources/charts-all.css</css>
        <css debug="false" rtl="true">/plugins/extjs6/resources/packages/charts/classic/<xsl:value-of select="$uxtheme"/>/resources/charts-all-rtl.css</css>
        <css debug="true" rtl="false">/plugins/extjs6/resources/packages/charts/classic/<xsl:value-of select="$uxtheme"/>/resources/charts-all-debug.css</css>
        <css debug="true" rtl="true">/plugins/extjs6/resources/packages/charts/classic/<xsl:value-of select="$uxtheme"/>/resources/charts-all-rtl-debug.css</css>
    </xsl:template>
 
    <!-- +
         | Load CSS and JS files. This template will ensure that each file is called once only.
         | @param {Node} scripts JS files to load . The node is a list of nodes with file url as text value. The url is relative to the server and should not contains context path.
         | @param {Node} css The same as scripts but for css files.
         + -->
    <xsl:template name="kernel-load">
        <xsl:param name="scripts"/>
        <xsl:param name="css"/>
        
        <xsl:call-template name="kernel-load-css">
            <xsl:with-param name="css" select="$css" />
        </xsl:call-template>
        
        <xsl:call-template name="kernel-load-js">
            <xsl:with-param name="scripts" select="$scripts" />
        </xsl:call-template>
    </xsl:template>
        
    <xsl:template name="kernel-load-js">
        <xsl:param name="scripts"/>

        <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
        
		<!-- Load scripts -->
		<xsl:if test="$scripts">
	        <xsl:for-each select="$scripts">
                <xsl:if test="(not(@debug) or ($debug-mode and @debug = 'true') or (not($debug-mode) and @debug = 'false')) and (not(@rtl) or ($rtl and @rtl = 'true') or (not($rtl) and @rtl = 'false'))">
                    
					<xsl:variable name="position" select="position()"/>
                
                    <xsl:variable name="value">
                        <xsl:choose>
                            <xsl:when test="@lang = 'true'">
                                <xsl:choose>
                                    <xsl:when test="lang[@code = $language-code]">
                                        <xsl:value-of select="lang[@code = $language-code]"/>    
                                    </xsl:when>
                                    <xsl:when test="lang[@default = 'true']">
                                        <!-- Fallback to the default language if the user language was not found -->
                                        <xsl:value-of select="lang[@default = 'true']"/>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                    <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    
                    
                    <xsl:if test="$value != ''">
        	            <!-- check that the src was not already loaded (by another plugin for example) -->
        	            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
                            <script type="text/javascript">
                                <xsl:attribute name="src">
                                    <xsl:choose>
                                        <xsl:when test="@absolute='true' or contains($value, '://')"><xsl:value-of select="$value"/></xsl:when>
                                        <xsl:otherwise><xsl:value-of select="concat($contextPath, $value)"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:if test="@data-donotminimize">
                                    <xsl:attribute name="data-donotminimize"><xsl:value-of select="@data-donotminimize" /></xsl:attribute>
                                </xsl:if>
                            </script>
        	            </xsl:if>
                    </xsl:if>
                </xsl:if>
	        </xsl:for-each>
	    </xsl:if>
        
    </xsl:template>
    
    <xsl:template name="kernel-load-css">
        <xsl:param name="css"/>
        
		<!-- Load css -->
        <xsl:if test="$css">
	        <xsl:for-each select="$css">
                <xsl:if test="(not(@debug) or ($debug-mode and @debug = 'true') or (not($debug-mode) and @debug = 'false')) and (not(@rtl) or ($rtl and @rtl = 'true') or (not($rtl) and @rtl = 'false'))">
                
                    <xsl:variable name="position" select="position()"/>

                    <xsl:variable name="value">
                        <xsl:choose>
                            <xsl:when test="@lang and @lang = 'true'">
                                <xsl:choose>
                                    <xsl:when test="lang[@code = $language-code]">
                                        <xsl:value-of select="lang[@code = $language-code]"/>    
                                    </xsl:when>
                                    <xsl:when test="lang[@default = 'true']">
                                        <!-- Fallback to the default language if the user language was not found -->
                                        <xsl:value-of select="lang[@default = 'true']"/>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
	            
                    <xsl:if test="$value != ''">
        	            <!-- check that the src was not already loaded (by another plugin for example) -->
        	            <xsl:if test="not($css[position() &lt; $position and . = $value])">
        	                <link rel="stylesheet" type="text/css">
                                <xsl:attribute name="href">
                                    <xsl:choose>
                                        <xsl:when test="@absolute='true' or contains($value, '://')"><xsl:value-of select="$value"/></xsl:when>
                                        <xsl:otherwise><xsl:value-of select="concat($contextPath, $value)"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:if test="@data-donotminimize">
                                    <xsl:attribute name="data-donotminimize"><xsl:value-of select="@data-donotminimize" /></xsl:attribute>
                                </xsl:if>
                            </link>
        	            </xsl:if>
                    </xsl:if>
                </xsl:if>
	        </xsl:for-each>
	   </xsl:if>
    </xsl:template>
</xsl:stylesheet>
