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
 * This helper provides a method to asynchronously load the GoogleMaps lib API.
 * See #loadScript method
 */
Ext.define('Ametys.helper.ChooseLocation.LoadGoogleMaps', {
	
	singleton: true,
	
	/**
	 * @private
	 * The GoogleMaps libs url
	 */
	__GOOGLE_MAPS_URL: '//maps.googleapis.com/maps/api/js',
	
	/**
	 * @property {Boolean} True if Google Map script are loaded
	 * @private
	 */
	_isGoogleMapsLoaded: false,	
	
	/**
	 * @property {Function} _callbackFn The callback function to load when google maps is ready to use. Set by #loadScript.
	 * @private
	 */
	_callbackFn: null,
	
	/**
	 * Load GoogleMap libs if needed then execute the callback function after loading
	 * @param {Function} callback The function requiring GoogleMaps to be loaded to execute after loading
	 */
	loadScript: function (callback)
	{
		if (this._isGoogleMapsLoaded)
		{
			callback();
		}
		else
		{
			// Fetch the API key for google maps and load the script
			this._getAPIKey();
			
			this._callbackFn = callback;
		}
	},
	
	/**
	 * @private
	 * Get the configured googled maps API key
	 */
	_getAPIKey: function()
	{
        Ametys.data.ServerComm.send({
        	plugin: 'core', 
        	url: 'google-api-key/get', 
        	parameters: {}, 
        	priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
        	callback: {
                handler: this._getAPIKeyCb,
                scope: this
            },
            errorMessage: true,
            waitMessage: true
        });
	},
	
	/**
	 * @private
	 * Callback invoked once the api key is retrieved
	 * @param {Object} response the server's response
	 * @param {Object} args the callback arguments
	 * @param {Function} args.callback th callback function
	 */
	_getAPIKeyCb: function(response, args)
	{
		this._loadScript(Ext.dom.Query.selectValue('ActionResult/apiKey', response));
	},
	
	/**
	 * @private
	 * Load GoogleMap lib
	 * @param {String} apiKey the google map api key to use. can be empty but limits functionnality
	 */
	_loadScript: function (apiKey)
	{
		var baseUrl = (document.location.protocol == 'https:' ? 'https:' : 'http:') + this.__GOOGLE_MAPS_URL;
		var url = baseUrl + '?callback=Ametys.helper.ChooseLocation.LoadGoogleMaps._loadScriptCb';
		url += apiKey ? '&key=' + apiKey : '';
		
		Ametys.loadScript (url, null, this._onLoadFailed);
	},
	
	/**
	 * This function is called when loading succeed
	 * @private
	 */
	_loadScriptCb: function ()
	{
		this._isGoogleMapsLoaded = true;
		
		if (Ext.isFunction(this._callbackFn))
		{
			// Execute callback function
			this._callbackFn();
			this._callbackFn = null;
		}
	},
	
	/**
	 * This function is called when loading failed
	 * @private
	 */
	_onLoadFailed: function (e)
	{
		this._isGoogleMapsLoaded = false;
		
		Ametys.log.ErrorDialog.display({
			title : "{{i18n LOAD_GOOGLE_MAPS_ERROR}}",
			text : "{{i18n LOAD_GOOGLE_MAPS_ERROR_DESC}}",
			details : e,
			category : this.self.getName()
		});
	}
});
