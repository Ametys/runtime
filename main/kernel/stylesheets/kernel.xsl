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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:exslt="http://exslt.org/common"
    xmlns:csscomponent="org.ametys.runtime.plugins.core.ui.css.AllCSSComponent"
    xmlns:jscomponent="org.ametys.runtime.plugins.core.ui.js.AllJSComponent">
	
	<xsl:import href="kernel-browsers.xsl"/>
	
    <!-- +
         | Load and initialize all scripts for UI
         | @param {String} plugins-direct-prefix Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins' 
         | @param {String} plugins-wrapped-prefix Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/plugins'
         | @param {Boolean} debug-mode Load JS files in debug mode when available.
         | @param {String} context-path The application context path. Can be empty for ROOT context path or should begin with / in other cases. E.g. '/MyContext'
         | @param {String} workspace-name The name of the current ametys workspace. Cannot be empty. E.g. 'admin'
         | @param {String} workspace-prefix The prefix of the current workspace (so not starting with the context path). If the workspace is the default one, this can be empty. E.g. '', '/_MyWorkspace'
         | @param {String} max-upload-size The parametrized max size for upadloding file to the server. In Bytes. E.g. 10670080 for 10 MB. Can be empty if unknown.
         | @param {String} language-code The language code supported when loading the application. E.g. 'en' or 'fr'. Defaults to the i18n value for key kernel:KERNEL_LANGUAGE_CODE.
         | @param {String} authorized-browsers Optionnaly, you can provide such a string :
		 | 					{
  		 |                     'supported': { 'ie' : '7-9'}, 					// List of supported browsers with their versions
		 | 					   'not-supported': { 'ie' : '0-6', ch : '10-0'},	// List of non supported browsers with their versions
 		 | 					   'warning-redirection': "warning.html",			// Redirection when the browser may be supported
		 | 					   'failure-redirection': "failure.html"			// Redirection when the browser is not supported
		 |					If window.ametys_authorized_browsers is undefined, no browser test will be done.<br/>
 		 | 					When a supported browser is detected, everything goes on normally.<br/>
		 | 					When a not-supported browser is detected, the application is redirected to the failure rediction url. Where a message should indicates to use a supported browser.<br/>
		 | 					When another browser is detected, the application is redirected to the warning redirection url. Where a message should indicated to use a supported browser, but the user could enforce the navigation (setting a cookie 'ametys.accept.non.supported.navigators' to 'on').<br/>
		 |					Keys for browsers are 'ie' (Microsoft Internet Explorer), 'ff' (Mozilla Firefox), 'ch' (Google Chrome), 'sa' (Apple Safari) and 'op' (Opera).<br/>
		 |					Versions can be a single number '3' or '3.5', or can be an interleave where 0 is the infinity '0-6' means all versions before 6 and 6 included, '6-0' means all versions after 6 and 6 included.
         + -->
    <xsl:template name="kernel-base">
        <xsl:param name="plugins-direct-prefix"/>
        <xsl:param name="plugins-wrapped-prefix"/>
        <xsl:param name="debug-mode" select="false()"/>
		<xsl:param name="context-path"/>
		<xsl:param name="workspace-name"/>
		<xsl:param name="workspace-prefix"/>
		<xsl:param name="max-upload-size"/>
		<xsl:param name="language-code"><i18n:text i18n:key='KERNEL_LANGUAGE_CODE' i18n:catalogue='kernel'/></xsl:param>
		<xsl:param name="authorized-browsers">undefined</xsl:param>

        <xsl:param name="load-cb"/>
        <xsl:param name="use-css-component">true</xsl:param>
        <xsl:param name="reuse-css-component">false</xsl:param>
        <xsl:param name="use-js-component">true</xsl:param>
        <xsl:param name="reuse-js-component">false</xsl:param>

	
		<xsl:call-template name="kernel-browsers">
			<xsl:with-param name="authorized-browsers" select="$authorized-browsers"/>
			<xsl:with-param name="context-path" select="$context-path"/>
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
        
       	<xsl:variable name="scripts-to-load-raw">
       		<script>/plugins/extjs4/resources/js/ext-all<xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
       		<script>/plugins/extjs4/resources/ux/js/form/MultiSelect.js</script>
       		<script>/plugins/extjs4/resources/ux/js/form/ItemSelector.js</script>

			<script>/kernel/resources/js/Ametys.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/window/DialogBox.js</script>
	        <script>/kernel/resources/js/Ametys/form/AbstractFieldWrapper.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/DateTime.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/ChangePassword.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/data/ServerComm.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/data/ServerComm/TimeoutDialog.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/log/Logger.js</script>
	        <script>/kernel/resources/js/Ametys/log/Logger/Entry.js</script>
	        <script>/kernel/resources/js/Ametys/log/ErrorDialog.i18n.js</script>

<!-- File pour ajouter la limite d'upload -->

<!-- 	        <script>/kernel/resources/js/org/ametys/ListView.js</script> -->
<!-- 	        <script>/kernel/resources/js/org/ametys/EditorListView.js</script> -->
<!-- 	        <script>/kernel/resources/js/org/ametys/Tree.js</script> -->

<!-- 	 		<script>/plugins/extjs/resources/ux/js/XmlTreeLoader.js</script> -->
<!-- 	 		<script>/kernel/resources/js/org/ametys/tree/XmlTreeLoader.js</script> -->
	 		
<!-- 			<script>/kernel/resources/js/org/ametys/servercomm/ServerComm.i18n.js</script> -->
<!-- 			<script>/kernel/resources/js/org/ametys/servercomm/ServerMessage.js</script> -->
<!-- 			<script>/kernel/resources/js/org/ametys/log/LoggerManager.js</script> -->
<!-- 			<script>/kernel/resources/js/org/ametys/log/LoggerEntry.js</script> -->
<!-- 			<script>/kernel/resources/js/org/ametys/msg/ErrorDialog.i18n.js</script> -->

	    </xsl:variable>
		<xsl:variable name="scripts-to-load" select="exslt:node-set($scripts-to-load-raw)"/>
		
		<xsl:variable name="css-to-load-raw">
			<css>/kernel/resources/css/gray/all.css</css>
		</xsl:variable>
		<xsl:variable name="css-to-load" select="exslt:node-set($css-to-load-raw)"/>

		<!-- LOAD JS -->
		<xsl:choose>
			<xsl:when test="$use-css-component != 'true'">
					<xsl:for-each select="$scripts-to-load/script">
						<script type="text/javascript" src="{$context-path}{.}"/>
						<xsl:copy-of select="$load-cb"/>
			      	</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
					<xsl:if test="$reuse-js-component = 'false'">
						<xsl:value-of select="jscomponent:resetJSFilesList()"/>
					</xsl:if>
					
					<xsl:for-each select="$scripts-to-load/script">
			      		<xsl:value-of select="jscomponent:addJSFile(.)"/>
			      	</xsl:for-each>

					<xsl:if test="$reuse-js-component = 'false'">
				        <script type="text/javascript" src="{$context-path}{$workspace-prefix}/plugins/core/jsfilelist/{jscomponent:getHashCode()}-{$debug-mode}.js"></script>
						<xsl:copy-of select="$load-cb"/>
					</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		
		<!--  LOAD CSS -->		
		<xsl:choose>
			<xsl:when test="$use-css-component != 'true'">
					<xsl:for-each select="$css-to-load/css">
						<link rel="stylesheet" href="{$context-path}{.}" type="text/css"/>
						<xsl:copy-of select="$load-cb"/>
					</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
					<xsl:if test="$reuse-css-component = 'false'">
						<xsl:value-of select="csscomponent:resetCSSFilesList()"/>
					</xsl:if>

					<xsl:for-each select="$css-to-load/css">
			      		<xsl:value-of select="csscomponent:addCSSFile(.)"/>
			      	</xsl:for-each>
			      	
					<xsl:if test="$reuse-css-component = 'false'">
						<xsl:call-template name="ui-load-css">
							<xsl:with-param name="load-cb" select="$load-cb"/>
							<xsl:with-param name="debug-mode" select="$debug-mode"/>
						</xsl:call-template>
					</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		
		<script type="text/javascript">
		
		var states = Ext.create('Ext.data.Store', {
    fields: ['abbr', 'name'],
    data : [
        {"abbr":"AL", "name":"Alabama"},
        {"abbr":"AK", "name":"Alaska"},
        {"abbr":"AZ", "name":"Arizona"}
        //...
    ]
});
		
		
		
		
			Ext.application({
			    name: 'AM',
			
			    appFolder: 'app',
			
			    launch: function() {
			        Ext.create('Ext.container.Viewport', {
			            layout: 'fit',
			            items: [
		                    
			            ]
			        });
			        
			        Ext.create("Ametys.window.DialogBox", 
			        	{
			        		'title': 'toto', 
			        		'icon': '<xsl:value-of select="$context-path"/>/kernel/resources/img/error_16.gif',
			        		'layout': 'anchor',
			        		id: 'blues',
			        		defaultButton: 'a',
			        		'items' : [
				        		 new Ext.form.Panel({ id: 'tata', defaults: {labelSeparator: ''}, items: [
				        			new Ext.Component({ html: 'test' }),
				        			new Ext.form.field.Text({fieldLabel: 'hello1', id: 'a', width: 350}),
				        			new Ext.form.field.Number({fieldLabel: 'hello2', id:'toto', ametysDescription: '', width: 350 }),
									
									Ext.create('Ametys.form.field.DateTime', {
									    fieldLabel: 'Choose State',
									    id: 'toto2',
									    name: 'toto2',
									    width: 350,
									    allowBlank: false,
								    	ametysDescription: 'test'
									}),


									Ext.create('Ametys.form.field.ChangePassword', {
									    fieldLabel: 'Choose State2',
									    width: 350,
									    allowBlank: false,
									    ametysDescription: 'test'
									}),

				        			new Ext.form.field.Text({fieldLabel: 'hello3', ametysDescription: 'test', allowBlank: false, width: 350}),
				        			
				        			Ext.create('Ext.form.field.File', {
									    fieldLabel: 'Choose State 5',
									    width: 350,
									    ametysDescription: 'test'
									}),

				        			Ext.create('Ametys.form.field.ChangePassword', {
									    fieldLabel: 'Choose State4',
									    width: 350,
									    allowBlank: false,
									    value: 'password',
									    ametysDescription: 'test'
									})
				        			
				        		 ]})
			        		],
			        		width: 500,
			        		height: 400,
			        		buttons: [ 
			        			{
			        				text:'OK', 
			        				handler: function() {
				        				var form = Ext.getCmp('tata').getForm()
				        				
				        				console.log(form.getFieldValues());
				        				
				        				if (form.isValid())
				        				{
				        				form.getFields().each(function(t) {console.log(t.getSubmitData())})
				        					this.up('dialog').hide();
				        				} 
			        				} 
			        			},
			        			{
			        				text:'Clear', 
			        				handler: function() {
				        				var form = Ext.getCmp('tata').getForm()
				        					form.clearInvalid();
			        				} 
			        			},
			        			{
			        				text:'Mask', 
			        				handler: function() {
			        				
 										Ext.create("Ametys.data.ServerComm.TimeoutDialog", 'a', 0);
 
				        			//	var a = Ext.Ajax.request({url: "_sites.xml", params: "toto=1", async: false, method: "GET"});
				        			//	var a = Ext.Ajax.request({url: "_sites.xml", params: "toto=1", async: false});
			        				} 
			        			},
			        			{
			        				text:'Enable/Disable', 
			        				handler: function() {
				        				var form = Ext.getCmp('tata').getForm()
				        				form.getFields().each(function (field) {
				        					if (field.isDisabled())
				        					{
				        						field.enable();
				        					}
				        					else
				        					{
				        						field.disable();
				        					}
				        				});
			        				} 
			        			}
			        		]
			        	}
			        ).show();
			    }
			});
			
			Ametys.setAppParameter('jojo', 'estpasbo');
		</script>
    </xsl:template>
 

    <xsl:template name="ui-load-css">
        <xsl:param name="load-cb"/>
        <xsl:param name="debug-mode"/>
        
        	<xsl:choose>
        		<xsl:when test="$debug-mode = 'true'">
        			<xsl:variable name="hashcode" select="csscomponent:getHashCode()"/>
        		
				    <xsl:call-template name="ui-load-css-recurse">
					    <xsl:with-param name="max" select="csscomponent:getNumberOfParts($hashcode)"/>
					    <xsl:with-param name="hashcode" select="$hashcode"/>
				    </xsl:call-template>
				    
					<xsl:copy-of select="$load-cb"/>
        		</xsl:when>
        		<xsl:otherwise>
		        	<link rel="stylesheet" type="text/css" href="{$contextPath}{$workspaceURI}/plugins/core/cssfilelist/{csscomponent:getHashCode()}.css"/>
					<xsl:copy-of select="$load-cb"/>
        		</xsl:otherwise>
        	</xsl:choose>
	</xsl:template>

    <xsl:template name="ui-load-css-recurse">
        <xsl:param name="num" select="0"/>
        <xsl:param name="max"/>
        <xsl:param name="hashcode"/>
    
    	<xsl:if test="$num &lt; $max">
        	<link rel="stylesheet" type="text/css" href="{$contextPath}{$workspaceURI}/plugins/core/cssfilelist/debug/{$hashcode}/{$num}.css"/>
		    <xsl:call-template name="ui-load-css-recurse">
			    <xsl:with-param name="num" select="$num + 1"/>
			    <xsl:with-param name="max" select="$max"/>
			    <xsl:with-param name="hashcode" select="$hashcode"/>
		    </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>