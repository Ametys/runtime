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
			return appParameters[name];
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
		 * Shutdown all the application and display a standard error message.<br/>
		 * Will cut all known crons, requests...<br/>
		 * Do not do anything after this call.
		 */
		shutdown: function() {
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
			document.body.innerHTML = "<h1>" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1'/>" + "</h1> <p>" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2'/>" +"</p><p><a href='javascript:location.reload(true);'>" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_3'/>" + "</a></p>"
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
Ext.BLANK_IMAGE_URL = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

/*
 * Initialize ssl empty file url
 */
Ext.SSL_SECURE_URL = Ext.BLANK_IMAGE_URL;

/*
 * Load localization for extjs
 */
(function() 
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
	
	// Override SortType to add support for accentued characters
	Ext.define("Ext.locale.data.SortTypes", {
        override: "Ext.data.SortTypes",
        
        /**
         * @member Ext.data.SortTypes
         * @method asNonAccentedUCString 
         * @since Ametys Runtime 3.7
         * @ametys
         * Case insensitive string (which takes accents into account)
         * @param {Object} s The value being converted
         * @return {String} The comparison value
         */
    	asNonAccentedUCString: function (s)
    	{
    		s = s.toLowerCase();
    		
    		s = s.replace(new RegExp(/[àáâãäå]/g),"a");
    		s = s.replace(new RegExp(/æ/g),"ae");
    		s = s.replace(new RegExp(/ç/g),"c");
    		s = s.replace(new RegExp(/[èéêë]/g),"e");
    		s = s.replace(new RegExp(/[ìíîï]/g),"i");
    		s = s.replace(new RegExp(/ñ/g),"n");
    		s = s.replace(new RegExp(/[òóôõö]/g),"o");
    		s = s.replace(new RegExp(/œ/g),"oe");
    		s = s.replace(new RegExp(/[ùúûü]/g),"u");
    		s = s.replace(new RegExp(/[ýÿ]/g),"y");

    		return Ext.data.SortTypes.asUCString(s);
    	}
    });
})();

/*
 * Supports for ametysDescription on fields and fields containers
 */
/**
 * @member Ext.form.field.Base
 * @ametys
 * @since Ametys Runtime 3.7
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
/**
 * @member Ext.form.FieldContainer
 * @ametys
 * @since Ametys Runtime 3.7
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
(function ()
{
	function getLabelableRenderData () 
	{
		var data = this.callParent(arguments);
		data.ametysDescription = this.ametysDescription;
		
		this.getInsertionRenderData(data, this.labelableInsertions);
		
		return data;
	}
	function onRender ()
	{
		this.callParent(arguments); 
		var td = this.el.query(".ametys-description")[0];
		if (td != null)
		{
			td.parentNode.appendChild(td); // move it as last
		}
	}
	
	var afterSubTpl = [ '<tpl if="ametysDescription">',
	                    	'</td>',
	                    	'<td class="ametys-description" data-qtip="{ametysDescription}">',
	                    	'</tpl>'
	];

	Ext.define("Ametys.form.Labelable", { override: "Ext.form.field.Base", afterSubTpl: afterSubTpl, getLabelableRenderData: getLabelableRenderData, onRender: onRender });
	Ext.define("Ametys.form.FieldContainer", { override: "Ext.form.FieldContainer", afterSubTpl: afterSubTpl, getLabelableRenderData: getLabelableRenderData, onRender: onRender });
})();

/*
 * Support for optionnal label on files to indicate max allowed size 
 */
(function() 
{
    Ext.define("Ametys.form.field.File", {
        override: "Ext.form.field.File",
        
        getTriggerMarkup: function() {
        	var result = this.callParent(arguments);
        	
        	/**
        	 * @member Ext.form.field.File
        	 * @ametys
        	 * @since Ametys Runtime 3.7
        	 * @cfg {Boolean} ametysShowMaxUploadSizeHint false to hide to max size hint under the field. true by default
        	 */
        	if (Ametys.MAX_UPLOAD_SIZE != undefined && Ametys.MAX_UPLOAD_SIZE != '' && this.ametysShowMaxUploadSizeHint !== false)
        	{
        		result += '</tr><tr id="' + this.id + '-uploadsize" class="ametys-file-hint"><td colspan="2">'
        		    + '(<i18n:text i18n:key="KERNEL_UPLOAD_HINT"/>'
        		    + Ext.util.Format.fileSize(Ametys.MAX_UPLOAD_SIZE)
        			+ ')</td>';
        	}
        	
        	return result;
        }
    });
})();

/*
 * Support for background animation 
 */
(function () 
{
    /**
     * @member Ext.dom.Element
     * @method animate 
     * @since Ametys Runtime 3.7
     * @ametys
     * @inheritdoc Ext.dom.Element_anim#animate
     * Ametys do additionnaly handle `background-position` to animate a background-image and `background-position-step` to step this animation.
     * Both args are array of numbers with unit.
     * To right align, use '100%'.
     * 
     * The following example will animate the background image of the element to the coordinates 0,0. 
     * The animation will be "normal" on the x axis but will only use 256 multiples on the y axis.
     * 
     *     el.animate({ 
     * 			to: { 
     * 				'background-position': ['0px', '0px'], 
     * 				'background-position-step': ['1px', '256px'] 
     * 			}, 
     * 			duration: 500, 
     * 			easing: 'linear' 
     *     });
     */
	Ext.define('Ametys.fx.target.Element', {
		override: 'Ext.fx.target.Element',
		
	    getElVal: function(element, attr, val) 
	    {
	        if (val == undefined && attr === 'background-position') 
	        {
	        	var bgPos = element.getStyle("background-position");
	    		/^([^ ]*) ([^ ]*)$/.exec(bgPos);
	    		val = [ RegExp.$1, RegExp.$2 ];
	    		
	    		return val;
	        }
	        return this.callParent(arguments);
	    },
	    
	    setElVal: function(element, attr, value)
	    {
	        if (attr === 'background-position') 
	        {
	        	var anim = element.getActiveAnimation();
	        	var to = anim.to['background-position'];
	        	var by = anim.to['background-position-step']
	        	if (by == null)
	        	{
	        		by = [1, 1];
	        	}

	        	var roundedVal = [
	        	 	Math.round((parseInt(value[0]) - parseInt(to[0])) / parseInt(by[0])) * parseInt(by[0]) + parseInt(to[0]),
	        	 	Math.round((parseInt(value[1]) - parseInt(to[1])) / parseInt(by[1])) * parseInt(by[1]) + parseInt(to[1])
	        	];
	        	var units = [
	        	    value[0].replace(/[-.0-9]*/, ''),
	        	    value[1].replace(/[-.0-9]*/, ''),
	        	];

	        	element.setStyle("background-position", roundedVal[0] + units[0] + ' ' + roundedVal[1] + units[1]);
	        } 
	        else 
	        {
	        	this.callParent(arguments);
	        }
	    }
	});
})();

/*
 * Changing default ajax method to POST
 */
Ext.Ajax.setOptions({method: 'POST', timeout: 0})

/*
 * Remove ametys_opts object
 */
window.ametys_opts = undefined;
