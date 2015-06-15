/*
 *  Copyright 2013 Anyware Services
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
 * This is a DAO to manage current user preferences.
 * 
 * Remember to call #load to get the preferences you need
 */
Ext.define('Ametys.userprefs.UserPrefsDAO', {
	singleton: true,
	
	/**
	 * @readonly
	 * @private
	 * @property {String} _PLUGIN_NAME The plugin to use for requests
	 */
	_PLUGIN_NAME: 'core',
		
	/**
	 * @readonly
	 * @private
	 * @property {String} _URL_VALUES Url to get the user prefs values. The url is relative to the plugin #_PLUGIN_NAME.
	 */
	_URL_VALUES: 'userprefs/values.xml',
	
	/**
	 * @readonly
	 * @private
	 * @property {String} _URL_SAVE Url to save the user prefs values. The url is relative to the plugin #_PLUGIN_NAME.
	 */
	_URL_SAVE: 'userprefs/save.xml',		

	/**
	 * @property {String} _defaultPrefContext="/runtime" The default context
	 * @private
	 */
	_defaultPrefContext: '/runtime',
	
	/**
	 * @private
	 * @property {Object} _cache The cache of user prefs values. The key is a prefContext and the value a cache where the key is the user pref identifier and the value is the array of associated values
	 */
	_cache: {},
	
	/**
	 * Change the pref context
	 */
	setDefaultPrefContext: function(context)
	{
		this._defaultPrefContext = context;
	},
	
	/**
	 * Get the default pref context
	 * @return {String} the default pref context
	 */
	getDefaultPrefContext: function ()
	{
		return this._defaultPrefContext;
	},
	
	/**
	 * This method reset the local cache (so future reading of preferences will do a server connection)
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 */
	resetCache: function(prefContext)
	{
		this._cache[prefContext || this._defaultPrefContext] = null;
	},
	
	/**
	 * Replace the cache with the given object
	 * @param {Object} cache The cache of user prefs values. The key is the user pref identifier and the value is the array of associated values
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 */
	preload: function(cache, prefContext)
	{
		this._cache[prefContext || this._defaultPrefContext] = cache;
	},
	
	/**
	 * This method is preloading several preferences in the local cache.
	 * @param {Function} callback A callback function that will be called when prepareCache is done.
	 * @param {Boolean} callback.success True if the load was successful
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 */
	load: function(callback, prefContext)
	{
		prefContext = prefContext || this._defaultPrefContext;
		
		if (this._cache[prefContext] != null)
		{
			// nothing to do ! cache is already complete
			callback(true);
			return;
		}
		
		Ametys.data.ServerComm.send({
			plugin: Ametys.userprefs.UserPrefsDAO._PLUGIN_NAME,
			url: Ametys.userprefs.UserPrefsDAO._URL_VALUES,
			parameters: {
				'prefContext': prefContext
			}, 
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
			callback: {
				handler: this._cachePrepared,
				scope: this,
				arguments: [callback, prefContext]
			}
		});
	},

	/**
	 * @private
	 * This method is callback when the prepare cache is over 
	 * @param {Object} response The xml http response.
	 * @param {Object[]} args The sent args
	 * @param {Function} args.0 A callback method. Can be null.
	 * @param {String} args.1 The used pref context
	 */
	_cachePrepared: function(response, args)
	{
		var callback = args[0];
		var prefContext = args[1];
		
		if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_ERROR'/>", response, "Ametys.userprefs.UserPrefsDAO._cachePrepared"))
		{
			callback(false);
			return;
		}	
		
		this._cache[prefContext] = {};
		
		var nodes =  Ext.dom.Query.select('> userprefs > *', response);
		for (var i = 0; i < nodes.length; i++)
		{
			this._cache[prefContext][nodes[i].nodeName] = [];
		}
		for (var i = 0; i < nodes.length; i++)
		{
			this._cache[prefContext][nodes[i].nodeName].push(Ext.dom.Query.selectValue("", nodes[i]));
		}

		callback(true);
	},
	
	/**
	 * Build an array with all know cache keys
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 * @return {String[]} Known keys. Can be null if cache is empty.
	 */
	keys: function(prefContext)
	{
		prefContext = prefContext || this._defaultPrefContext;
		
		if (this._cache[prefContext] == null)
		{
			return null;
		}
		
		var array = [];
		for (var i in this._cache[prefContext])
		{
			array.push(i);
		}
		return array;
	},
	
	/**
	 * This method reads a preference.
	 * @param {String} preference The preference name to load. Cannot be null or empty.
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 * @return {Object} The value of the preference. Can be null if cache is empty.
	 */
	getValue: function(preference, prefContext)
	{
		prefContext = prefContext || this._defaultPrefContext;

		if (this._cache[prefContext] == null)
		{
			return null;
		}

		var values = this.getValues(preference, prefContext);

		return (values == null || values.length == 0) ? null : values[0]
	},
	
	/**
	 * This method reads a preference.
	 * @param {String} preference The preference name to load. Cannot be null or empty.
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 * @return {Object[]} The values of the preference. Can be null if cache is empty.
	 */
	getValues: function(preference, prefContext)
	{
		prefContext = prefContext || this._defaultPrefContext;

		if (this._cache[prefContext] == null)
		{
			return null;
		}

		return this._cache[prefContext][preference];
	},


	/**
	 * Change the values
	 * @param {Object} params User prefs: key and values
	 * @param {Function} callback A callback when finished
	 * @param {Boolean} callback.success Has the save operation been successful?
	 * @param {Object} [callback.errors] The key is the preference name, and the value an error message. Can be empty event is success is false on server exception: in that cas the user is already notified. 
	 * @param {String} [prefContext] The pref context to use. Switch to default context if missing. See #setDefaultPrefContext.
	 * @param {Number} [priority] The priority of the Ametys.data.ServerComm#send call: default is Ametys.data.ServerComm#PRIORITY_MAJOR. Set to Ametys.data.ServerComm#PRIORITY_MINOR to save less important preferences. 
	 * @param {String} [cancelCode] A cancel code to prevent many successive save process. See Ametys.data.ServerComm#send for more information.   
	 */
	saveValues: function(params, callback, prefContext, priority, cancelCode)
	{
		params["prefContext"] = prefContext || this._defaultPrefContext;
		params["submit"] = 'true';
		
		Ametys.data.ServerComm.send({
			plugin: Ametys.userprefs.UserPrefsDAO._PLUGIN_NAME,
			url: Ametys.userprefs.UserPrefsDAO._URL_SAVE,
			parameters: params, 
			priority: priority, 
			cancelCode: cancelCode,
			callback: {
				handler: this._valuesSaved,
				scope: this,
				arguments: [callback, params["prefContext"]]
			}
		});
	},

	/**
	 * @private
	 * This method is callback when the save process is over 
	 * @param {Object} response The xml http response.
	 * @param {Object[]} args Arguments send
	 * @param {Function} args.0 callback A callback method.
	 * @param {String}  args.1 prefContext The used pref context.
	 */
	_valuesSaved: function(response, args)
	{
		var callback = args[0];
		var prefContext = args[1];

		if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_ERROR_SAVE'/>", response, "Ametys.userprefs.UserPrefsDAO._valuesSaved"))
		{
			callback(false, null);
			return;
		}	
		
		var errorNodes = Ext.dom.Query.select('> xml > errors > field', response);
		this.getLogger().debug("User preferences saved with " + errorNodes.length + " error(s)");

		if (errorNodes.length > 0)
		{
			var errors = {};
			for (var i=0; i < errorNodes.length; i++)
			{
				var fieldId = errorNodes[i].getAttribute("id");
				var label = Ext.dom.Query.selectValue('> error', errorNodes[i]);
				errors[fieldId] = label;
			}
			
			if (callback != null)
			{
				callback(false, errors);
			}
			return;
		}
		
		// success, refill cache
		var cb = null;
		if (callback != null)
		{
			cb = function() { callback(true, null); };
		}
		this._cachePrepared(Ext.dom.Query.selectNode('xml', response), [cb, prefContext])
	}
});

Ext.define("Ametys.message.UserPrefsMessageTarget",
        {
            override: "Ametys.message.MessageTarget",
            
            statics: 
            {
                /**
                 * @member Ametys.message.MessageTarget
                 * @readonly
                 * @property {String} USER_PREFS The target type is user preferences. 
                 */
                USER_PREFS: "userPrefs"
            }
        }
);
