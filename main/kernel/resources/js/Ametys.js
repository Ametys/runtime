/*
 *  Copyright 2012 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
  * Root for all Ametys objects and methods.<br/>
  * <br/>
  * Before loading this class you have to initialize the window.ametys_opt variable:
  * <code>		
  * 	// Theses options are here to initialize the Ametys object.
  * 	// Do not use theses since their are removed during Ametys initialization process
  * 	window.ametys_opts = {
  * 		"plugins-direct-prefix": '/_plugins', 	// See PLUGINS_DIRECT_PREFIX for details (private)
  * 		"plugins-wrapped-prefix": '/plugins', 	// See PLUGINS_WRAPPED_PREFIX for details (private)
  * 		"debug-mode": true, 					// See DEBUG_MODE for details
  * 		"context-path": '', 					// See CONTEXT_PATH for details
  * 		"workspace-name": 'admin', 				// See WORKSPACE_NAME for details
  * 		"workspace-prefix": '/_admin', 			// See WORKSPACE_PREFIX for details
  * 		"max-upload-size": '10670080', 			// See MAX_UPLOAD_SIZE for details
  * 		"language-code": 'fr' 					// See LANGUAGE_CODE for details
  * 	}
  * </code>
  * <br/>
  * Loading this class also initialize extjs with the following elements :
  * <ul>
  * <li>Test for authorized browser (redirecting if the browser is not supported),</li>
  * <li>Removing window.ametys_authorized_browsers,</li>
  * <li>Ext.Date.patterns are initialize to suitable values,</li>
  * <li>Ext.BLANK_IMAGE_URL is associated to the correct url,</li>
  * <li>Ext.SSL_SECURE_URL is associated to the correct url,</li>
  * <li>Removing window.ametys_opts from global scope</li>
  * <li>Adding support for 'description' config on fields and field container (that draws a ? image on the right of the fields)</li>
  * </ul>
  */

Ext.define(
	"Ametys", 
	{
		singleton: true,
	
		/**
		 * The application context path. Can be empty for the root context path. E.g. '/MyContext'.
		 * @type String
		 */
		CONTEXT_PATH: ametys_opts["context-path"],
	
		/**
		 * The name of the current ametys workspace. Cannot be empty. E.g. 'admin'
		 * @type String
		 */
		WORKSPACE_NAME: ametys_opts["workspace-name"],
	
		/**
		 * The prefix of the current workspace (so not starting with the context path). If the workspace is the default one, this can be empty. E.g. '', '/_MyWorkspace'
		 * @type String
		 */
		WORKSPACE_PREFIX: ametys_opts["workspace-prefix"],
	           	
		/**
		 * The URI to go the current workspace (starting with the context path). If the context path is ROOT and the workspace is the default one, this can be empty. E.g. '', '/MyContext', '/MyContext/_MyWorkspace', '/_MyWorkspace'
		 * @type String
		 */
		WORKSPACE_URI: ametys_opts["context-path"] + ametys_opts["workspace-prefix"],
	           	
		/**
		 * The parametrized max size for upadloding file to the server. In Bytes. E.g. 10670080 for 10 MB. Can be empty if unknown.
		 * @type String
		 */
		MAX_UPLOAD_SIZE: ametys_opts["max-upload-size"],
	            
		/**
	     * Load JS files in debug mode when available.
		 * @type Boolean
		 */
		DEBUG_MODE: ametys_opts["debug-mode"],
		
		/**
		 * The language code supported when loading the application. E.g. 'en' or 'fr'
		 * @type String
		 */
		LANGUAGE_CODE: ametys_opts["language-code"],
		
		/**
		 * Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins'
		 * @type String
		 * @private
		 */
		PLUGINS_DIRECT_PREFIX: ametys_opts["plugins-direct-prefix"],
	                
		/**
		 * Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/plugins'
		 * @type String
		 * @private
		 */
		PLUGINS_WRAPPED_PREFIX: ametys_opts["plugins-wrapped-prefix"],
	                
		/**
		 * Get the url prefix for direct connection to a plugin (e.g. for ajax connections)
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing directly plugins (e.g. for ajax connections). E.g. '/MyContext/_plugins/MyPlugin'
		 */
		getPluginDirectPrefix: function (plugin) { return Ametys.CONTEXT_PATH + Ametys.PLUGINS_DIRECT_PREFIX + '/' + plugin;},
	            
		/**
		 * Get the url prefix for wrapped connection to a plugin (e.g. for redirections to this plugin)
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing directly plugins (e.g. for ajax connections). E.g. '/MyContext/plugins/MyPlugin'
		 */
		getPluginWrapperPrefix: function(plugin) { return Ametys.CONTEXT_PATH + Ametys.PLUGINS_WRAPPED_PREFIX + '/' + plugin;},
	            
		/**
		 * Get the url prefix for downloading resource file of a plugin. The use of Ametys.getPluginResourcesPrefix('test') + '/img/image.png' will return '/MyContext/plugins/test/resources/img/image.png' 
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing plugin resources (e.g. js, css or images files). E.g. '/MyContext/plugins/MyPlugin/resources'
		 */
		getPluginResourcesPrefix: function (plugin) { return Ametys.CONTEXT_PATH + "/plugins/" + plugin + "/resources"; }
   	}
);

/*
 * Initialize Ext.Date with suitable patterns
 */
Ext.Date.patterns = {
    ISO8601Long:"Y-m-d\\TH:i:s.uP",
    ISO8601Short:"Y-m-d",
    ShortDate: "n/j/Y",
    LongDate: "l, F d, Y",
    FullDateTime: "l, F d, Y g:i:s A",
    MonthDay: "F d",
    ShortTime: "g:i A",
    LongTime: "g:i:s A",
    SortableDateTime: "Y-m-d\\TH:i:s",
    UniversalSortableDateTime: "Y-m-d H:i:sO",
    YearMonth: "F, Y"
};

/*
 * Initialize blank image url
 */
Ext.BLANK_IMAGE_URL = Ametys.getPluginResourcesPrefix('extjs4') + "/images/default/s.gif";

/*
 * Initialize ssl empty file url
 */
Ext.SSL_SECURE_URL = Ext.BLANK_IMAGE_URL;

/*
 * Load localization for extjs
 */
{
	var link = document.createElement("script");
		link.src = Ametys.getPluginResourcesPrefix('extjs4') + "/js/locale/ext-lang-" + Ametys.LANGUAGE_CODE + ".js";
		link.charset = "UTF-8";
		link.type = "text/javascript";
	document.getElementsByTagName("head")[0].appendChild(link);
}

/*
 * Support for description on all fields
 */
Ext.form.field.Base.prototype._onRender = Ext.form.field.Base.prototype.onRender;
Ext.form.FieldContainer.prototype._onRender = Ext.form.FieldContainer.prototype.onRender;

Ext.form.FieldContainer.prototype.onRender = 
Ext.form.field.Base.prototype.onRender = 
function()
{
	this._onRender(arguments);
	
	/**
	 * @class Ext.form.field.Base
	 * @cfg {String} ametysDescription A help image is added with the given description as tooltip
	 */
	if (this.ametysDescription)
	{
				var helpEl = this.bodyEl.insertSibling({
					tag: 'td',
					cls: 'ametys-description'
				}, 'after');
				helpEl;
				
				var tooltip = new Ext.ToolTip({
					        target: helpEl,
					        html: this.ametysDescription,
					        dismissDelay: 0 // disable automatic hiding
	    		});
	}
}		


/*
 * Remove ametys_opts object
 */
window.ametys_opts = undefined;
