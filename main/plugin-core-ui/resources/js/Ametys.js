/*
 *  Copyright 2016 Anyware Services
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
		CONTEXT_PATH: ametys_opts["context-path"] || "",
	
		/**
		 * The name of the current ametys workspace. Cannot be empty. E.g. 'admin'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_NAME: ametys_opts["workspace-name"] || "",
	
		/**
		 * The prefix of the current workspace (so not starting with the context path). If the workspace is the default one, this can be empty. E.g. '', '/_MyWorkspace'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_PREFIX: ametys_opts["workspace-prefix"] || "",
	           	
		/**
		 * The URI to go the current workspace (starting with the context path). If the context path is ROOT and the workspace is the default one, this can be empty. E.g. '', '/MyContext', '/MyContext/_MyWorkspace', '/_MyWorkspace'
		 * @type String
		 * @readonly
		 */
		WORKSPACE_URI: (ametys_opts["context-path"] || "") + (ametys_opts["workspace-prefix"]  || ""),
	           	
		/**
		 * The parametrized max size for upadloding file to the server. In Bytes. E.g. 10670080 for 10 MB. Can be empty if unknown.
		 * @type String
		 * @readonly
		 */
		MAX_UPLOAD_SIZE: ametys_opts["max-upload-size"] || 1000000,
	            
		/**
	     * Load JS files in debug mode when available.
		 * @type Boolean
		 * @readonly
		 */
		DEBUG_MODE: ametys_opts["debug-mode"] || "",
		
		/**
		 * The language code supported when loading the application. E.g. 'en' or 'fr'
		 * @type String
		 * @readonly
		 */
		LANGUAGE_CODE: ametys_opts["language-code"] || "",
		
		/**
		 * Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins'
		 * @type String
		 * @readonly
		 * @private
		 */
		PLUGINS_DIRECT_PREFIX: ametys_opts["plugins-direct-prefix"] || "",
	                
		/**
		 * Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/plugins'
		 * @type String
		 * @readonly
		 * @private
		 */
		PLUGINS_WRAPPED_PREFIX: ametys_opts["plugins-wrapped-prefix"] || "",
		
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
		 * @return {Object} The value associated to the parameter name, or undefined if it does not exist.
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
		 * @param {String} [media] The css media to set. Such as 'screen', 'print'... or a comma separated list.
		 */
		loadStyle: function (url, media)
		{
			var head = document.getElementsByTagName("head")[0];
			var link = document.createElement("link");
			link.rel = "stylesheet";
			link.href = url;
			link.type = "text/css";
			if (media)
			{
				link.media = media;
			}
			head.appendChild(link);
		},
		
		/**
		 * Load a js file
		 * @param {String} url The url of the js file to load
		 * @param {Function} onLoad The callback to call on successful load.
		 * @param {Function} onError The callback to call on failure to load.
		 */
		loadScript: function (url, onLoad, onError)
		{
			var me = this;
			function internalOnError (msg)
			{
				var message = "{{i18n PLUGINS_CORE_UI_LOADSCRIPT_ERROR}}" + url;
				me.getLogger().error(message);
				
				if (Ext.isFunction (onError))
				{
					onError.call (null, msg);
				}
			}
			
			function internalOnLoad (msg)
			{
				if (me.getLogger().isInfoEnabled())
				{
					var message = "{{i18n PLUGINS_CORE_UI_LOADSCRIPT_SUCCESS}}" + url;
					me.getLogger().info(message);
				}
				
				if (Ext.isFunction (onLoad))
				{
					onLoad.call ();
				}
			}
			
			// Disable cache to avoid URL ends with '?_dc=1379928434854'
			Ext.Loader.setConfig ({
				disableCaching : false
			});
			
			Ext.Loader.loadScript({
				url: url,	
				onLoad: internalOnLoad,
				onError: internalOnError
			});
			
			Ext.Loader.setConfig ({
				disableCaching : true
			});
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
		 * @return {Object} The result of the function
		 * @throws an error if the function is undefined
		 */
		executeFunctionByName: function(name, context, scope/*, args*/)
		{
			var fn = Ametys.getFunctionByName(name, context, scope);
			if (fn == null)
			{
				var message = "Can not execute undefined function '" + name + "'.";
				this.getLogger().error(message);
				throw new Error (message);
			}
			
			var args = Array.prototype.slice.call(arguments, 3);
			
			return fn.apply(null, args);
		},

        /**
         * Open a popup window with http POST or GET data
         * @param {String} url the window url
         * @param {Object} [data] the request parameters
         * @param {String} [method="POST"] the request method for data, 'GET' or 'POST', defaults to "POST"
         * @param {String} [target="_blank"] an optional opening target (a name, or "_self"), defaults to "_blank"
         */
        openWindow: function (url, data, method, target)
        {
            var form = document.createElement("form");
            form.action = url;
            form.method = method || "POST";
            form.target = target || "_blank";
           
            if (data) 
            {
                for (var key in data) 
                {
                    var input = document.createElement("input");
                    input.type = 'hidden';
                    input.name = key;
                    input.value = typeof data[key] === "object" ? Ext.JSON.encode(data[key]) : data[key];
                    form.appendChild(input);
                }
            }
           
            form.style.position = "absolute";
            form.style.left = "-10000px";
            
            document.body.appendChild(form);
            form.submit();
            
            // delete form
            document.body.removeChild(form);
        },

		/**
		 * Shutdown all the application and display a message. A restart link is automatically added.<br/>
		 * Will cut all known crons, requests...<br/>
		 * Do not do anything after this call.
		 * @param {String} [title] A title
		 * @param {String} [message] A html message to display.
         * @param {String} [action] A text message to set on the action button.
		 */
		shutdown: function(title, message, action) 
		{
			title = title || "{{i18n PLUGINS_CORE_UI_SHUTDOWN_DEFAULTTITLE}}";
			message = message || "{{i18n PLUGINS_CORE_UI_SHUTDOWN_DEFAULTTEXT}}";
            action = action || "{{i18n PLUGINS_CORE_UI_SHUTDOWN_DEFAULTACTION}}";
			
			if (window.console)
			{
				console.info("Shutting down Ametys '" + title + "' - " + message);
			}
			
			// Shutdown request
			Ametys.data.ServerComm._shutdown();
			
			// Close error dialogs
			Ametys.log.ErrorDialog._okMessages();
			Ametys.log.ErrorDialog = { display: function() {}, _okMessages: function() {} };
			
			// Display error message
			
			// remove all css
			var links = document.body.getElementsByTagName("link");
			for (var i = links.length - 1; i >= 0; i--)
			{
				if (links[i].type == "text/css" && links[i].media != 'hide')
				{
					links[i].parentNode.removeChild(links[i]);
				}
			}
            
			var styles = document.body.getElementsByTagName("style");
            for (var i = styles.length - 1; i >= 0; i--)
			{
				if (styles[i].type == "text/css")
				{
					styles[i].parentNode.removeChild(styles[i]);
				}
			}
            
            var vps = Ext.ComponentQuery.query('viewport'); 
            if (vps.length > 0)
            {
                try
                {
                    vps[0].destroy();
                }
                catch(e)
                {
                    if (window.console)
                    {
                        console.error(e);
                    }
                    vps[0].getElementTarget().remove();
                }
            }

			
			// Destroy all components at once (to avoid errors due to removal order)
			Ext.ComponentManager.each(function(id, item, size) {
				if (item.ownerCt)
				{
					try
					{
						item.ownerCt.removeAll();
					}
					catch (e)
					{
                        if (window.console)
                        {
						      window.console.warn(e);
                        }
					}
				}
			});
			// Some special components are still there
			Ext.ComponentManager.each(function(id, item, size) {
				if (item.ownerCt)
				{
					try
					{
						item.ownerCt.remove(item, true); 
					}
					catch (e)
					{
                        if (window.console)
                        {
    						window.console.warn(e);
                        }
					}
				}
			});
			// Some components are still there, but does not seems to have effets
			Ext.app.Application.instance.suspendEvents(false);
            
            // Display slashscreen back if any
            if (Ext.getBody().child("div.head"))
            {
                document.body.className = "";
                
                Ext.get(document.body.parentNode).addCls("ametys-common");
                Ext.getBody().child("div.head").dom.style.display = '';
                Ext.getBody().child("div.foot").dom.style.display = '';
                Ext.getBody().child("div.main").dom.style.display = '';
                
                var splashScreen = document.getElementById('splashscreen');
                splashScreen.className = "text";
                splashScreen.innerHTML = "<h1>" + title + "</h1>" 
                                        + "<p class='text'>" + message + "</p>"
                                        + "<p class='additionnal'><a href='javascript:Ametys.reload();' onclick=\"this.parentNode.remove(this); document.body.className = 'done'; document.getElementById('pulse').style.display = '';\">" + action + "</a></p>"
                                        + "<div class=\"la-ball-pulse la-dark la-2x\" id=\"pulse\" style=\"display: none\">"
                                        + "<div></div>"
                                        + "<div></div>"
                                        + "<div></div>"
                                        + "</div>";
            }
			else
            {
    			document.body.innerHTML = "<h1>" + title + "</h1>" 
                                        + "<p>" + message + "</p>"
                                        + "<p><a href='javascript:Ametys.reload();'>" + action + "</a></p>";
            }

            if (window.console)
			{
				console.info("Ametys shutdown");
			}
		},
		
		/**
		 * Reload the application. Current parameters will be replaced. A 'foo' parameter will be added to avoid cache.
		 * @param {String} [params] The params to add to the url. e.g. "myparam=value1&amp;myotherparam=value2" 
		 */
		reload: function(params)
		{
			// Shutdown request
			Ametys.data.ServerComm._shutdown();
			
			// Close error dialogs
			Ametys.log.ErrorDialog._okMessages();
			Ametys.log.ErrorDialog = { display: function() {}, _okMessages: function() {} };

			// Compute new url
			var href = window.location.href;

    		var i1 = href.indexOf('?');
    		if (i1 >= 0)
    		{
    			href = href.substring(0, i1);
    		}

    		var i2 = href.indexOf('#');
    		if (i2 >= 0)
    		{
    			href = href.substring(0, i2);
    		}

    		var i3 = href.indexOf(';');
    		if (i3 >= 0)
    		{
    			href = href.substring(0, i3);
    		}
    		
			// open new url
    		window.location.href = href + "?" + (params || "") + "&foo=" + Math.random();		
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
/**
 * @member Ext.Date
 * @property {Object} patterns=undefined Predefined internationalized patterns 
 * @property {String} patterns.ISO8601DateTime Date using the ISO 8601 standard for date and time. E.g. 2013-02-28T17:13:50.000+02:00
 * @property {String} patterns.ISO8601Date Simplier format of #patterns.ISO8601Date with date only date. E.g 2013-02-28
 * @property {String} patterns.ShortDate A short date depending on language. Can be 02/28/13 in english.
 * @property {String} patterns.LongDate A long date depending on language. Can be 02/28/2013 in english.
 * @property {String} patterns.FullDate A full readable date depending on language. Can be Thursday, February 2, 2013 in english.
 * @property {String} patterns.VeryShortTime A short time depending on language. Can be 03:15 AM in english. 
 * @property {String} patterns.ShortTime A short time depending on language. Can be 03:15:50 AM in english. 
 * @property {String} patterns.LongTime A long time depending on language. Can be 03:15:35+02:00 AM in english.
 * @property {String} patterns.ShortDateTime A combinaison of #patterns.ShortDate and #patterns.ShortTime. Can be 02/28/13 03:15:50 AM in english.
 * @property {String} patterns.FriendlyDateTime A readable #patterns.LongDate, a keyword (such as 'at') and very short time. Can be 02/28/2013 at 03:15 AM in english.
 * @property {String} patterns.LongDateTime A combinaison of #patterns.LongDate and #patterns.LongTime. Can be 02/28/2013 03:15:35+02:00 AM in english.
 * @property {String} patterns.FullDateTime A combinaison of #patterns.FullDate, a keyword (such as 'at') and a long readable time. Can be Thursday, February 28, 2013 at 05:13:50 GMT PM in english.
 * @since Ametys Runtime 3.9
 * @ametys
 */
Ext.Date.patterns = {
	ISO8601DateTime:"Y-m-d\\TH:i:s.uP",
	ISO8601Date:"Y-m-d",
	ShortDate: "{{i18n PLUGINS_CORE_UI_DATETIME_SHORTDATE}}",
	LongDate: "{{i18n PLUGINS_CORE_UI_DATETIME_LONGDATE}}",
	FullDate: "{{i18n PLUGINS_CORE_UI_DATETIME_FULLDATE}}",
    VeryShortTime: "{{i18n PLUGINS_CORE_UI_DATETIME_VERYSHORTTIME}}",
	ShortTime: "{{i18n PLUGINS_CORE_UI_DATETIME_SHORTTIME}}",
	LongTime: "{{i18n PLUGINS_CORE_UI_DATETIME_LONGTIME}}",
	ShortDateTime: "{{i18n PLUGINS_CORE_UI_DATETIME_SHORTDATETIME}}",
	FriendlyDateTime: "{{i18n PLUGINS_CORE_UI_DATETIME_FRIENDLYDATETIME}}",
	LongDateTime: "{{i18n PLUGINS_CORE_UI_DATETIME_LONGDATETIME}}",
	FullDateTime: "{{i18n PLUGINS_CORE_UI_DATETIME_FULLDATETIME}}"
};

/*
 * Changing default ajax method to POST and timeout to a long time
 */
Ext.Ajax.setTimeout(365*24*60*60*1000);
Ext.Ajax.setMethod('POST');

/*
 * Load localization for extjs
 */
(function ()
{
    // This is to change default value
    Ext.define("Ext.locale.i18n.LoadMask", {
        override: "Ext.LoadMask",
        msg: "{{i18n PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}"
    });
})();

/*
 * Remove ametys_opts object
 */
window.ametys_opts = undefined;
