/*
 *  Copyright 2014 Anyware Services
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
 * This tool display the Ametys documentation inside an Iframe.
 * It also allows the user to open the documentation in a new tab.
 * @private
 */
Ext.define('Ametys.plugins.coreui.help.HelpTool', {
	extend: "Ametys.tool.Tool",
	
	/**
	 * @cfg {Boolean} searchQuery=null If provided, the tool will open on the search page with this query string.
	 * The search URL is defined in WEB-INF/param/help.xml
	 */
	
	/**
	 * @cfg {String} helpId=null  The help identifier used to open the tool on a certain url.
	 * The id / url mapping is defined in WEB-INF/param/help.xml 
	 */
	
	/**
	 * @property {String} _helpUrl=null The url displayed by the help tool.
	 * @private
	 */
	
	/**
	 * @private
	 * @property {Ext.ux.IFrame} _iframe The iframe object
	 */
	
	/**
	 * @private
	 * @property {Ext.container.Container} _iframeCt The iframe container
	 */
	
	/**
	 * @private
	 * @property {Ext.button.Button} _openWindowBtn Button to open help page in a new window
	 */
	
	statics: {
		/**
		 * Open the help tool relative to currently displayed tooltip or to the currently focused tool
		 */
		openTool: function()
		{
			var helpId = undefined,
				qt, ft;
			
			qt = Ext.QuickTips.getQuickTip(); 
			if (qt && qt.isVisible())
			{
				helpId = qt.activeTarget.helpId || null;
			}
			else
			{
				ft = Ametys.tool.ToolsManager.getFocusedTool();
				if (ft)
				{
					helpId = ft.getToolHelpId() || null;
				}
			}
			
			Ametys.tool.ToolsManager.openTool('uitool-help', {helpId: helpId});
		}
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		
		this._helpUrl = null;
		this._initializeDisplay();
		
    	// Register the tool on the history tool
	    var role = this.getFactory().getRole();
	    var toolParams = this.getParams();

	    Ametys.navhistory.HistoryDAO.addEntry({
	        id: this.getId(),
	        label: this.getTitle(),
	        description: this.getDescription(),
	        iconSmall: this.getSmallIcon(),
	        iconMedium: this.getMediumIcon(),
	        iconLarge: this.getLargeIcon(),
	        type: Ametys.navhistory.HistoryDAO.TOOL_TYPE,
	        action: Ext.bind(Ametys.tool.ToolsManager.openTool, Ametys.tool.ToolsManager, [role, toolParams], false)
	    });
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function ()
	{
		this._openWindowBtn = new Ext.button.Button({
			icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/help/new-window_16.png',
			tooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_HELP_OPEN_NEW_WINDOW'/>",
			cls: 'tbar-btn-open-window',
			handler: this._openNewWindow,
			scope: this
		});
		
		this._iframe = Ext.create("Ext.ux.IFrame", {}); 
		
		this._iframeCt = Ext.create("Ext.container.Container", {
	    	items : this._iframe,
	    	layout: 'fit'
	    });
		
		var noHelpPanel = this._createNoHelpPanel();
		
		var me = this;
		
		return Ext.create('Ext.panel.Panel', {
			border: false,
			cls: 'help-tool',
			dockedItems: [noHelpPanel],
			layout: 'fit',
			
			items: this._iframeCt,
			
			listeners: {
				'afterrender': function (panel)
				{
					var cmpId = panel.getId() + '-' + Ext.id();
					
					panel.getEl().insertFirst({
						id: cmpId,
					    tag: 'div', 
						cls: "tbar-btn-open-window-container"
					});
					
					me._openWindowBtn.render(cmpId);
				}
			}
		});
	},
	
	/**
	 * @private 
	 * Create the panel that displays a message when no help page was found.
	 * @return {Ext.Panel} The created panel
	 */
	_createNoHelpPanel: function()
	{
		var button = Ext.create("Ext.Button", {
	    	flex: 1,
	    	textAlign: 'left',
	    	text:"<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_HELP_INFO_NOT_FOUND'/>",
	    	handler: this._showDefaultUrl,
	    	scope: this
	    });
	    
	    this._noHelpPanel = Ext.create("Ext.container.Container", {
	    	cls: 'no-help-btn',
	    	hidden: true,
	    	dock: 'top',
			layout: {
				type: 'hbox',
				pack: 'start',
				align: 'stretch'
			},
	    	items : button
	    });
	    
	    return this._noHelpPanel;
	},
	
	/**
	 * Initialize help tool
	 * @private
	 */
	_initializeDisplay: function()
	{
		var searchQuery = this.getParams().searchQuery;
		var helpId = this.getParams().helpId;
		
		if (searchQuery !== undefined)
		{
			this._getSearchUrl(searchQuery, this._showUrl);
		}
		else if (helpId !== undefined)
		{
			if (helpId)
			{
				this._getHelpUrl(helpId, this._showUrl);
			}
			else
			{
				this._setInNoHelpState();
			}
		}
		else
		{
			// Tool opened from help button, or if no tool was focused and no tooltip active
			// Displaying default help.
			this._getDefaultHelpUrl(this._showUrl);
		}
	},
	
	/**
	 * @private
	 * Load the iframe with given url if not null or empty
	 * @param {String} url The iframe url.
	 */
	_showUrl: function(url)
	{
		if (url)
		{
			this._loadIframe(url);
		}
		else
		{
			this._setInNoHelpState();
		}
	},
	
	/**
	 * @private
	 * Load the iframe with default help url if not null or empty
	 */
	_showDefaultUrl: function()
	{
		if (!this._defaultUrl)
		{
			this._getDefaultHelpUrl(this._showUrl);
		}
		else
		{
			this._showUrl(this._defaultUrl);
		}
	},
	
	/**
	 * @private
	 * Load the iframe with given url
	 * @param {String} url The iframe url.
	 */
	_loadIframe: function(url)
	{
		if (url)
		{
			this._iframe.load(url);
		}
		
		this._noHelpPanel.hide();
		if (this._openWindowBtn)
		{
			this._openWindowBtn.show();
		}
		
		if (this._iframeCt.rendered)
		{
			this._iframeCt.unmask();
		}
		else
		{
			this._iframeCt.on('afterrender', function () {this.unmask()}, this._iframeCt, {single: true});
		}
	},
	
	/**
	 * Set tool in "no help" state
	 * @private
	 */
	_setInNoHelpState: function()
	{
		this._noHelpPanel.show();
		if (this._openWindowBtn)
		{
			this._openWindowBtn.hide();
		}
		
		if (this._iframeCt.rendered)
		{
			this._iframeCt.mask();
		}
		else
		{
			this._iframeCt.on('afterrender', function () {this.mask()}, this._iframeCt, {single: true});
		}
	},
	
	/**
	 * @private
	 * Get the default help url
	 * @param {Function} cb the callback function to call after retrieving default url
	 * @param {String} cb.url The default url
	 */
	_getDefaultHelpUrl: function(cb)
	{
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.ui.help.HelpUrlProvider",
			methodName: "getDefaultHelpUrl",
			callback: {
 				scope: this,
				handler: this._getDefaultHelpUrlCb,
				arguments: [cb]
			},
			waitMessage: true,
			errorMessage: {
				msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_HELP_ERROR_URL_PROVIDER'/>",
				category: Ext.getClassName(this)
			}
		});
	},
	
	/**
	 * @private
	 * Callback function called after retrieving the default url
	 * @param {Object} result The JSON result
	 * @param {Object} args The callback arguments
	 */
	_getDefaultHelpUrlCb: function(result, args)
	{
		var url = result.url;
		if (url)
		{
			this._defaultUrl = url;
		}
		
		var cb = args[0];
		if (Ext.isFunction(cb))
		{
			cb.call(this, this._defaultUrl);
		}
	},
	
	/**
	 * @private
	 * Get the url linked the given help id
	 * @param {String} helpId The identifier for help
	 * @param {Function} cb The callback function
	 * @param {String} cb.url The retrieved url
	 */
	_getHelpUrl: function(helpId, cb)
	{
		this._helpUrl = null;
		
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.ui.help.HelpUrlProvider",
			methodName: "getHelpUrl",
			parameters: [helpId],
			callback: {
 				scope: this,
				handler: this._getHelpUrlCb,
				arguments: [cb]
			},
			waitMessage: true,
			errorMessage: {
				msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_HELP_ERROR_URL_PROVIDER'/>",
				category: Ext.getClassName(this)
			}
		});
	},
	
	/**
	 * @private
	 * Callback function after retrieving the help url
	 * @param {Object} result The JSON result
	 * @param {Object} args The callback arguments
	 */
	_getHelpUrlCb: function(result, args)
	{
		var url = result.url;
		
		this._helpUrl = url || null;
		
		var cb = args[0];
		if (Ext.isFunction(cb))
		{
			cb.call(this, this._helpUrl);
		}
	},
	
	/**
	 * @private
	 * Get the search url to open
	 * @param {String} searchQuery The query for the search
	 * @param {Function} cb The callback function
	 * @param {String} cb.url The retrieved url
	 */
	_getSearchUrl: function(searchQuery, cb)
	{
		this._helpUrl = null;
		
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.ui.help.HelpUrlProvider",
			methodName: "getSearchUrl",
			parameters: [searchQuery || ""],
			callback: {
 				scope: this,
				handler: this._getSearchUrlCb,
				arguments: [cb]
			},
			waitMessage: true,
			errorMessage: {
				msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_HELP_ERROR_URL_PROVIDER'/>",
				category: Ext.getClassName(this)
			}
		});
	},
	
	/**
	 * @private
	 * Callback function after retrieving the search url
	 * @param {Object} result The JSON result
	 * @param {Object} args The callback arguments
	 */
	_getSearchUrlCb: function(result, args)
	{
		var url = result.url;
		
		this._helpUrl = url || null;
		
		var cb = args[0];
		if (Ext.isFunction(cb))
		{
			cb.call(this, this._helpUrl);
		}
	},
	
	/**
	 * @private
	 * Open the current help url in a new window
	 */
	_openNewWindow: function()
	{
		var url = this._helpUrl || this._defaultUrl;
		
		var cb = function(url)
		{
			if (url)
			{
				var win = window.open(url, '_blank');
				win.focus();
				
				// Close tool
				this.close();
			}
		}
		
		if (!url)
		{
			this._getDefaultHelpUrl(Ext.bind(cb, this));
		}
		else
		{
			cb.call(this, url);
		}
	}
	
}, function() {
	
	// Bind Keyboard help feature or F1 to the help tool.
	// Executed when the HelpTool class is created
	if ('onhelp' in window) // IE
	{
		var me = this;
		window.onhelp = function() {
			me.openTool();
			return false;
		}
	}
	else // Others
	{
		new Ext.util.KeyMap({
			target: Ext.getDoc(),
			key: Ext.event.Event.F1,
			defaultEventAction: 'preventDefault',
			handler: this.openTool,
			scope: this
		});
	}
});
