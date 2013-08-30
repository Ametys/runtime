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
  * Root for all Ametys objects and methods. Also contains application parameters.<br/>
  * <br/>
  * Before loading this class you have to initialize the window.ametys_opt variable:
  * 
  * <pre><code>		
  * 	// Theses options are here to initialize the Ametys object.
  * 	// Do not use these since they are removed during Ametys initialization process
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
  * </code></pre>
  * 
  * <br/>
  * Loading this class also initialize extjs with the following elements :
  * <ul>
  * <li>Test for authorized browser (redirecting if the browser is not supported),</li>
  * <li>Removing window.ametys_authorized_browsers,</li>
  * <li>Ext.Date.patterns are initialize to suitable values,</li>
  * <li>Ext.BLANK_IMAGE_URL is associated to the correct url,</li>
  * <li>Ext.SSL_SECURE_URL is associated to the correct url,</li>
  * <li>Removing window.ametys_opts from global scope,</li>
  * <li>Adding support for 'ametysDescription' config on fields and field container (that draws a ? image on the right of the fields),</li>
  * <li>Doing some localization work,</li>
  * <li>Display an hint under File field to display max upload size,</li>
  * <li>Changing Ext.Ajax default request method from GET to POST, and setting timeout to 0.</li>
  * </ul>
  */

Ext.define(
	"Ametys", 
	{
		singleton: true,
	
		/**
		 * The application context path. Can be empty for the root context path. E.g. '/MyContext'.
		 * @type String
		 * @readonly
		 */
		CONTEXT_PATH: ametys_opts["context-path"],
	
		/**
		 * The name of the current ametys workspace. Cannot be empty. E.g. 'admin'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_NAME: ametys_opts["workspace-name"],
	
		/**
		 * The prefix of the current workspace (so not starting with the context path). If the workspace is the default one, this can be empty. E.g. '', '/_MyWorkspace'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_PREFIX: ametys_opts["workspace-prefix"],
	           	
		/**
		 * The URI to go the current workspace (starting with the context path). If the context path is ROOT and the workspace is the default one, this can be empty. E.g. '', '/MyContext', '/MyContext/_MyWorkspace', '/_MyWorkspace'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_URI: ametys_opts["context-path"] + ametys_opts["workspace-prefix"],
	           	
		/**
		 * The parametrized max size for upadloding file to the server. In Bytes. E.g. 10670080 for 10 MB. Can be empty if unknown.
		 * @type String
		 * @readonly
		 */
		MAX_UPLOAD_SIZE: 1000000,//ametys_opts["max-upload-size"],
	            
		/**
	     * Load JS files in debug mode when available.
		 * @type Boolean
		 * @readonly
		 */
		DEBUG_MODE: ametys_opts["debug-mode"],
		
		/**
		 * The language code supported when loading the application. E.g. 'en' or 'fr'
		 * @type String
		 * @readonly
		 */
		LANGUAGE_CODE: ametys_opts["language-code"],
		
		/**
		 * Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins'
		 * @type String
		 * @readonly
		 * @private
		 */
		PLUGINS_DIRECT_PREFIX: ametys_opts["plugins-direct-prefix"],
	                
		/**
		 * Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/plugins'
		 * @type String
		 * @readonly
		 * @private
		 */
		PLUGINS_WRAPPED_PREFIX: ametys_opts["plugins-wrapped-prefix"],
		
		/**
		 * Application parameters. Theses parameters are set by the application, and are added to all requests.
		 * They can also be used for local purposes.
		 * @type Object
		 * @private
		 */
		appParameters: {},
		
		/**
		 * Add an application parameter. Theses parameters are automatically added to all request, but are of course also added for local purposes.
		 * @param {String} name The key of the parameter
		 * @param {Object} value The value of the parameter. The object have to be encodable using Ext.JSON.encode
		 */
		setAppParameter: function(name, value) {
			this.appParameters[name] = value;
			Ext.Ajax.extraParams = "context.parameters=" + encodeURIComponent(Ext.JSON.encode(this.appParameters));
		},
		
		/**
		 * Get an application parameter.
		 * @param {String} name The key of the parameter
		 * @return {Object} The value associated to the parameter name, or undefined if it does not exists.
		 */
		getAppParameter: function(name) {
			return this.appParameters[name];
		},
		
		/**
		 * Get every application parameters.
		 * @return {Object} The application parameters.
		 */
		getAppParameters: function() {
			return Ext.clone(this.appParameters);
		},
		
		/**
		 * Get the url prefix for direct connection to a plugin (e.g. for ajax connections)
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing directly plugins (e.g. for ajax connections). E.g. '/MyContext/_MyWorkspace/_plugins/MyPlugin'
		 */
		getPluginDirectPrefix: function (plugin) { return Ametys.WORKSPACE_URI + Ametys.PLUGINS_DIRECT_PREFIX + '/' + plugin;},
	            
		/**
		 * Get the url prefix for wrapped connection to a plugin (e.g. for redirections to this plugin)
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing plugins for displaying (e.g. to display a page rendered by the plugin). E.g. '/MyContext/_MyWorkspace/plugins/MyPlugin'
		 */
		getPluginWrapperPrefix: function(plugin) { return Ametys.WORKSPACE_URI + Ametys.PLUGINS_WRAPPED_PREFIX + '/' + plugin;},
	            
		/**
		 * Get the url prefix for downloading resource file of a plugin. The use of Ametys.getPluginResourcesPrefix('test') + '/img/image.png' will return '/MyContext/plugins/test/resources/img/image.png' 
		 * @param {String} plugin The plugin name. Cannot be null or empty.
		 * @return {String} The url prefix for accessing plugin resources (e.g. js, css or images files). E.g. '/MyContext/plugins/MyPlugin/resources'
		 */
		getPluginResourcesPrefix: function (plugin) { return Ametys.CONTEXT_PATH + "/plugins/" + plugin + "/resources"; },
		
		/**
		 * Get the url prefix for accessing a workspace resource file. The use of Ametys.getWorkspaceResourcesPrefix('wsp') + '/img/image.png' will return '/MyContext/_wsp/resources/img/image.png' 
		 * @param {String} workspace The workspace name. If omitted, the current workspace will be used.
		 * @return {String} The url prefix for accessing workspace resources (e.g. js, css or images files). E.g. '/MyContext/_wsp/resources'
		 */
		getWorkspaceResourcesPrefix: function(workspace)
		{
		    if (workspace == null)
	        {
		        return Ametys.WORKSPACE_URI + '/resources';
	        }
		    else
	        {
                return Ametys.CONTEXT_PATH + '/_' + workspace + '/resources';
	        }
	    },
		
		/**
		 * Convert html tags to textare.
		 * @param {String} s The string to convert containing text with some br tags
		 * @return {String} The textare compliant string with 0x13 characters
		 */
		convertHtmlToTextarea: function(s)
		{
		    s = s.replace(/<br\/>/g, "\r\n");
		    s = s.replace(/&#034;/g, "\"");
		    s = s.replace(/&#039;/g, "'");
		    s = s.replace(/&lt;/g, "<");
		    s = s.replace(/>/g, ">");
		    s = s.replace(/&amp;/g, "&");
		    return s;
		},

		/**
		 * The opposite of {@link #convertHtmlToTextarea}
		 * @param {String} s The string to convert containing text with some 0x13 characters
		 * @return {String} The html compliant string with br tags
		 */
		convertTextareaToHtml: function (s)
		{
		    s = s.replace(/\r?\n/g, "<br/>");
		    s = s.replace(/"/g, "&#034;");
		    s = s.replace(/'/g, "&#039;");
		    return s;
		},

		/**
		 * Load a css file
		 * @param {String} url The url of the css file to load
		 */
		loadStyle: function (url)
		{
			var head = document.getElementsByTagName("head")[0];
			var link = document.createElement("link");
			link.rel = "stylesheet";
			link.href = url;
			link.type = "text/css";
			head.appendChild(link);
		},
		
		/**
		 * Selects the direct child DOM nodes
		 * @param {HTMLElement} [node=document] The start of the query.
		 * @return {HTMLElement[]} An array of DOM elements
		 */
		selectDirectChildElements: function (node)
		{
			var childNodes = Ext.dom.Query.select('> *', node);
			var elements = [];
			for (var i = 0; i < childNodes.length; i++)
			{
				// Test if Node.ELEMENT_NODE
				if (childNodes[i].nodeType == 1)
				{
					elements.push(childNodes[i]);
				}
			}
			return elements;
		},
		
		/**
		 * Retrieve an object by its name
		 * @param {String} name The name of the object
		 * @param {Object} [context=window] The search context (relative to the object name).
		 * @return {Object} The desired object, or null if it does not exist.
		 */
		getObjectByName: function(name, context)
		{
			context = context || window;
			
			var namespaces = name.split(".");
			var obj = context;
			var prop;
			
			for (var i = 0; i < namespaces.length; i++)
			{
				prop = namespaces[i];
				if (prop in obj)
				{
					obj = obj[prop];
				}
				else
				{
					return null;
				}
			}
			
			return obj;
		},
		
		/**
		 * Create an object by supplying its constructor name
		 * @param {String} name The name of the constructor
		 * @param {Object} [context=window] The search context (relative to the constructor name).
		 * @param {Object...} [args] Optional function arguments.
		 * @return {Object} The object returned by the constructor call.
		 */
		createObjectByName: function(name, context/*, args*/)
		{
			var fn = Ametys.getObjectByName(name, context);
			if (!fn) return null; // return null if fn is falsy.
			
			var args = Array.prototype.slice.call(arguments, 2);
			
			// new operator must be call on a wrapped function like this one.
			var wrappedCtor = function(args)
			{
				var ctor = function() {
					// Here 'this' is set to the new keyword.
					fn.apply(this, args);
				}
				// Inherits the prototype.
				ctor.prototype = fn.prototype;
				return ctor;
			};
			
			var ctor = wrappedCtor(args);
			return new ctor();
		},
		
		/**
		 * Get a function by supplying its name
		 * @param {String} name The name of the object
		 * @param {Object} [context=window] The search context (relative to the object name).
		 * @param {Object} [scope=context] The scope for the function call.
		 * @return {Object} The function, or null if the function does not exist.
		 */
		getFunctionByName: function(name, context, scope)
		{
			var ctx = context || window;
			
			var namespaces = name.split(".");
			var prop;
			
			for (var i = 0; i < namespaces.length-1; i++)
			{
				prop = namespaces[i];
				if (prop in ctx)
				{
					ctx = ctx[prop];
				}
				else
				{
					return null;
				}
			}
			
			var fn = ctx[namespaces.pop()];
			if (!fn) return null; // return null if fn is falsy.
			
			var scp = scope || context || ctx;
			
			return Ext.bind(fn, scp);
		},
		
		/**
		 * Executes a function by supplying its name
		 * @param {String} name The name of the object
		 * @param {Object} [context=window] The search context (relative to the object name).
		 * @param {Object} [scope=context] The scope for the function call.
		 * @param {Object...} [args] Optional function arguments.
		 * @return {Object} The result of the function, or null if the function does not exist.
		 */
		executeFunctionByName: function(name, context, scope/*, args*/)
		{
			var fn = Ametys.getFunctionByName(name, context, scope);
			if (fn == null)
			{
				return null;
			}
			
			var args = Array.prototype.slice.call(arguments, 3);
			
			return fn.apply(null, args);
		},

		/**
		 * Shutdown all the application and display a standard error message.<br/>
		 * Will cut all known crons, requests...<br/>
		 * Do not do anything after this call.
		 */
		shutdown: function() 
		{
			// Shutdown request
			Ametys.data.ServerComm._shutdown();
			
			// Close error dialogs
			Ametys.log.ErrorDialog._okMessages();
			Ametys.log.ErrorDialog = { display: function() {} };
			
			// Display error message
			
			// remove all css
			var links = document.getElementsByTagName("link");
			for (var i = 0; i < links.length; i++)
			{
				if (links[i].type == "text/css")
				{
					links[i].parentNode.removeChild(links[i]);
				}
			}
			var styles = document.getElementsByTagName("style");
			for (var i = 0; i < styles.length; i++)
			{
				if (styles[i].type == "text/css")
				{
					styles[i].parentNode.removeChild(styles[i]);
				}
			}
			
			document.body.setAttribute('style', "background-image: none !important; background-color: #FFFFFF !important; color: #000000 !important; padding: 20px !important;");
			document.body.innerHTML = "<h1><i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1'/></h1> <p><i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2'/></p><p><a href='javascript:location.reload(true);'><i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_3'/></a></p>"
		},
		
		/**
		 * Close the application by exiting
		 * If an app param "back-url" is available, current window will be redirected to it (String).
		 * Else If an app param "callback" is available, it will be called (Function).
		 * Else the window will be closed
		 */
		exit: function() 
		{
			if (this.getAppParameter('back-url') != null)
			{
				window.location.href = this.getEnvParams('back-url');
			}
			if (typeof(this.getAppParameter('callback')) == 'function')
			{
				this.getEnvParams('callback')();
			}
			else
			{
				window.close();
			}
		}
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
Ext.BLANK_IMAGE_URL = Ametys.getPluginResourcesPrefix('extjs4') + "/themes/images/default/tree/s.gif";

/*
 * Initialize ssl empty file url
 */
Ext.SSL_SECURE_URL = Ext.BLANK_IMAGE_URL;

/*
 * Changing default ajax method to POST
 */
Ext.Ajax.setOptions({method: 'POST', timeout: 0});

/*
 * Load localization for extjs
 */
(function ()
{
    // This is to change default value
    Ext.define("Ext.locale.fr.LoadMask", {
        override: "Ext.LoadMask",
        msg: "<i18n:text i18n:key='KERNEL_LOADMASK_DEFAULT_MESSAGE' i18n:catalogue='kernel'/>"
    });

    // Load localized extjs
	var link = document.createElement("script");
		link.src = Ametys.getPluginResourcesPrefix('extjs4') + "/js/locale/ext-lang-" + Ametys.LANGUAGE_CODE + ".js";
		link.charset = "UTF-8";
		link.type = "text/javascript";
	document.getElementsByTagName("head")[0].appendChild(link);
})();

/*
 * Remove ametys_opts object
 */
window.ametys_opts = undefined;

/*
 * Create a dedicated Ametys server proxy that sends a request through the Ametys ServerComm
 */
(function () 
{
	/**
	 * @since Ametys Runtime 3.7
	 * @ametys
	 * Defines a dedicated proxy that use the {@link Ametys.data.ServerComm} to communicate with the server.
	 * 
	 * Example of configuration in a store:
	 * 
	 * 		Ext.create('Ext.data.Store', {
	 * 			model: 'my.own.Model',
	 * 			proxy: {
	 * 				type: 'ametys',
	 * 				plugin: 'projects',
	 * 				url: 'administrator/projects/list.xml',
	 * 				reader: {
	 * 					type: 'xml',
	 * 					record: 'element',
	 * 					root: 'root'
	 * 				}
	 * 			}
	 * 		});
	 */
	Ext.define('Ametys.data.ServerCommProxy', {
		alias: 'proxy.ametys',
		extend: 'Ext.data.proxy.Server',
		
		noCache: false,
		
		plugin: null,
		workspace: null,
		errorMessage: "<i18n:text i18n:key="KERNEL_SERVERCOMMPROXY_ERROR_MSG"/>",
		
		reader2ResponseTypes: {
			xml: 'xml',
			json: 'text'
		},
		
		doRequest: function(operation, callback, scope)
		{
			var writer  = this.getWriter();
			var request = this.buildRequest(operation);
			
			if (operation.allowWrite()) {
				request = writer.write(request);
			}
			
			Ametys.data.ServerComm.send({
				priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
				plugin: this.plugin,
				workspace: this.workspace,
				url: request.url,
				parameters: request.params,
				responseType: this.getResponseType(),
				callback: {
					handler: this.createRequestCallback,
					scope: this,
					arguments: {
						request: request,
						operation: operation,
						callback: callback,
						scope: scope
					}
				}
			});
		},
		
		getResponseType: function()
		{
			return this.reader2ResponseTypes[this.getReader().type || 'xml'];
		},
		
		createRequestCallback: function(response, arguments)
		{
			var failure = Ametys.data.ServerComm.handleBadResponse(this.errorMessage, 
																	response, 
																	Ext.getClassName(this) + '.createRequestCallback');
			
			this.processResponse(!failure, arguments.operation, arguments.request.options, response, arguments.callback, arguments.scope);
		},
		
		extractResponseData: function(response)
		{
			var responseType = this.getResponseType();
			
			// Return first child which is the expected root node
			if (responseType == 'xml')
			{
				return Ext.dom.Query.selectNode('*', response)
			}
			else
			{
				return response;
			}
		}
	});
})();
