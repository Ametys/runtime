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
 * This UI helper provides a dialog to select coordinates on a Google Map.<br>
 * This dialog box embeds an address search bar and a GoogleMaps.<br>
 * This creates a marker on the GoogleMaps. That marker is draggable at will.<br>
 * An initial address can be provided - to initialize the searrch field - in the configuration parameter of the #open method.
 */
Ext.define('Ametys.helper.ChooseLocation', {
	
	singleton: true,	
	
	/**
	 * The default zoom level
	 * @private
	 * @readonly
	 */
	__DEFAULT_ZOOM_LEVEL: 17,
	
	/**
	 * Allow the user to setup a GoogleMaps marker
	 * @param {Object} [config] The initial parameters of the widget.
	 * @param {Object} [config.zoomLevel] The default zoom level.
	 * @param {String} [config.mapTypeId=google.maps.MapTypeId#HYBRID] The map type. Defaults to hybrid (roadmap + satellite)
	 * @param {String} [config.zoomControlStyle=google.maps.ZoomControlStyle.LARGE] The zoom control style.
	 * @param {Object} [config.initialLatLng] The initial position of the marker :
	 * @param {Number} [config.initialLatLng.latitude] The initial latitude of the marker.
	 * @param {Number} [config.initialLatLng.longitude] The initial longitude of the marker.
	 * @param {String} [config.initialAddress] The initial value of the address textfield. If config.initialLatLng is not provided in the config object, then this string also setups the initial position of the marker.
	 * @param {String} [config.helpMessage] The help message to display at the top of the window
	 * @param {String} [config.icon] The full icon path of the dialog box
	 * @param {String} [config.title] The title of the dialog box
	 * @param {Function} [callback] The method that will be called when the dialog box is closed. The method signature is :
	 * @param {Object} [callback.latlng] The chosen latitude and longitude
	 * @param {Number} [callback.latlng.latitude] The chosen latitude
	 * @param {Number} [callback.latlng.longitude] The chosen longitude
	 */
	open: function (config, callback)
	{
		this._cbFn = callback;
		
		// Fetch the API key for google maps and load the script
		this._getAPIKey(config);
	},
	
	/**
	 * @private
	 * Get the configured google maps API key
	 * @param {Object} [config] The initial parameters of the widget.
	 */
	_getAPIKey: function(config)
	{
        Ametys.data.ServerComm.send({
        	plugin: 'core', 
        	url: 'google-api-key/get', 
        	parameters: {}, 
        	priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
        	callback: {
                handler: this._getAPIKeyCb,
                scope: this,
                arguments: {
                	config: config
                }
            },
            errorMessage: true,
            waitMessage: true
        });
	},
	
	/**
	 * @private
	 * Callback invoked once the api key is retrieved
	 * @param {Object} response the server's response
	 * @param {Object} args The callback arguments
	 */
	_getAPIKeyCb: function(response, args)
	{
		var apiKey = Ext.dom.Query.selectValue('ActionResult/apiKey', response);
		Ametys.helper.ChooseLocation.LoadGoogleMaps.loadScript(apiKey, 
				Ext.bind(
						function(){
							this._delayedInitialize(args.config, apiKey), 
							this._gmapwindow.show();
							this._gmapwindow.down('#geo-search-textfield').focus();
						}, 
				this)
		);
	},
	
	/**
	 * Creates the dialog box if it is not already created and initialize it
	 * @param {Object} [config] The dialog box configuration object
	 * @param {Object} [config.initialLatLng] The initial position of the marker.
	 * @param {Object} [config.defaultLatLng] The default position of the marker when there is no initial address.
	 * @param {Number} [config.defaultZoomLevel=6] The default zoom level when for default latitude/longitude
	 * @param {Number} config.initialLatLng.latitude The initial latitude of the marker.
	 * @param {Number} config.initialLatLng.longitude The initial longitude of the marker.
	 * @param {String} [config.initialAddress] The initial value of the address textfield. If config.initialLatLng is not provided in the config object, then this string also setups the initial position of the marker. 
	 * @param {String} [config.helpMessage] The help message to display at the top of the window
	 * @param {String} [config.icon] The relative path of the window icon
	 * @param {String} [config.title] The title of the window
	 * @param {String} The Google Api key
	 */
	_delayedInitialize: function(config, apiKey) 
	{
		// Initial position of marker
		var initialLatLng = config.initialLatLng ? new google.maps.LatLng(config.initialLatLng.latitude, config.initialLatLng.longitude) : null;
		var defaultLatLng = config.defaultLatLng ? new google.maps.LatLng(config.defaultLatLng.latitude, config.defaultLatLng.longitude) : new google.maps.LatLng(0.0, 0.0);
		var initialAddress = config.initialAddress;
		
		// Zoom level
		var zoomLevel = initialLatLng || initialAddress ? (config.zoomLevel || this.__DEFAULT_ZOOM_LEVEL) : (defaultLatLng ? (config.defaultZoomLevel || 6) : 1);
		
		// Create the GMap panel
		this._gmapPanel = Ext.create('Ext.ux.GMapPanel', {
			name : 'gmappanel',
			flex: 1,
			mapOptions: {
				mapTypeId: config.mapTypeId || google.maps.MapTypeId.HYBRID,
				zoom: zoomLevel,
				zoomControl: true,
				zoomControlOptions: {
				    style: config.zoomControlStyle || google.maps.ZoomControlStyle.LARGE
				}
			},
			center : new google.maps.LatLng(0.0, 0.0),
			listeners: {
				mapready: Ext.bind (this._pinInitialMarker, this, [initialLatLng, config.initialAddress, defaultLatLng], 2)
			}
		});		
		
		var me = this;
		this._searchErrorMsg = '';
		
		this._gmapwindow = Ext.create("Ametys.window.DialogBox",{
			title: config.title || "{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_DEFAULT_TITLE}}",
			icon: config.icon || Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/geolocation/geolocation_16.png', 
			
			closeAction : 'destroy',
			width : 550,
			height : 450,
			layout: 'fit',
			
			items: {
				xtype: 'panel',
				layout: {
				    type: 'vbox',
				    align : 'stretch',
				    pack  : 'start'
				},
				border: false,
				items : [{
							xtype: 'component',
							cls: 'a-text-warning',
							html: "{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_MISSING_API_KEY}}",
							hidden: !Ext.isEmpty(apiKey)
						},
				        {
							xtype: 'component',
							cls: 'a-text',
							html: config.helpMessage || "{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_HELP_MSG_1}}"
						},
						{
							// Search address bar
							xtype: 'panel',
							layout: 'hbox',
							height: 26,
							border: false,
							items: [
							        {
							        	xtype: 'textfield',
							        	id: 'geo-search-textfield',
							        	fieldLabel: "{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_SEARCH_LABEL}}",
							        	labelAlign: 'right',
							        	labelSeparator: '',
							        	labelWidth: 80,
							        	value: config.initialAddress,
							        	inputAttrTpl: 'donotsubmitonenter="true"', // Adds the attribute 'donotsubmitonenter="true"' to the html code of that component
							        	flex: 1,
							        	listeners: {
							        		specialkey: Ext.bind(this._pinAddressOnEnterKeyPress, this)
							        	} 
							        },
							        {
							        	xtype: 'button',
							        	icon: Ametys.getPluginResourcesPrefix('cms') + '/img/search/search_16.png',
							        	tooltip : "{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_SEARCH}}",				
							        	handler : Ext.bind(this._pinAtTextfieldAddress, this)
							        }
							]
						},
						{
							xtype: 'component',
							cls: 'a-text-error',
							itemId: 'geo-search-textfield-error',
							html: me._searchErrorMsg || ''
						},
						this._gmapPanel,
						{
							xtype: 'component',
							cls: 'a-text',
							html: config.bottomText || ""
						}
				]
			},
			
			// Buttons
			buttons : [{
					text :"{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_OK}}",
					handler: Ext.bind(this._ok, this)
				}, {
					text :'{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_CANCEL}}',
					handler: Ext.bind(this._cancel, this)
				}
			]
		});
		
		return true;
	},
	
	/**
	 * Set the error message under the search field
	 * @param {String} [msg] The message to display, or undefined/null to remove the message.
	 * @private
	 */
	_setErrorSearchMessage: function(msg)
	{
		this._searchErrorMsg = msg || '';
		var searchErrorCmp = this._gmapwindow.down('#geo-search-textfield-error');
		
		if (searchErrorCmp)
		{
			searchErrorCmp.update(this._searchErrorMsg);
		}
	},

	/**
	 * Remove the error message under the search field
	 * @private
	 */
	_cleanErrorSearchMessage: function()
	{
		this._setErrorSearchMessage();
	},
	
	/**
	 * Search for the address on the map whenever you type Enter in the search address textfield
	 * @param {Ext.form.field.Text} input The input text
	 * @param {Ext.event.Event} e The event object
	 * @private
	 */
	_pinAddressOnEnterKeyPress: function(input, e)
	{
		if (e.getKey() == e.ENTER) 
		{
			e.preventDefault();
			e.stopPropagation();
			this._pinAtTextfieldAddress();
		}	
	},
	
	
	/**
	 * @private 
	 * Pin the marker to the initial position.
	 * @param {Ext.ux.GMapPanel} gmapPanel The GMap panel
	 * @param {google.maps.Map} gmap The Google Map
	 * @param {google.maps.LatLng} initialLatLng The initial position of the marker. Can be null to center the map to the initial address.
	 * @param {String} initialAddress The (postal) address to center the map. Can be null to center the map to the default position.
	 * @param {google.maps.LatLng} defaultLatLng The default position of the marker if initialLatLng and initialAddress are empty (default to : 0°N 0°E)
	 */
	_pinInitialMarker: function (gmapPanel, gmap, initialLatLng, initialAddress, defaultLatLng)
	{
		if (initialLatLng)
		{
			this._pinAtLatLng(initialLatLng);
		} 
		else if (initialAddress)
		{
			this.pinAtAddress(initialAddress);
		}
		else
		{
			this._pinAtLatLng(defaultLatLng);
		}
	},
	
	/**
	 * @private 
	 * Moves the marker at the location described in the address search bar
	 */
	_pinAtTextfieldAddress: function() 
	{
		var textfieldAddress = this._gmapwindow.down('#geo-search-textfield').getValue();
		if (textfieldAddress)
		{
			this.pinAtAddress(textfieldAddress);
			this._gmapPanel.gmap.setZoom(this.__DEFAULT_ZOOM_LEVEL);
		}
	},

	/**
	 * Moves the marker at a given latitude/longitude
	 * @param {Number} latitude The latitude to setup the marker
	 * @param {Number} longitude The longitude to setup the marker
	 */
	pinAtLatitudeLongitude: function(latitude, longitude)
	{
		this._pinAtLatLng(new google.maps.LatLng(latitude, longitude));
	},
	
	
	/**
	 * Moves the marker at a given (postal) address
	 * @param {String} anAddress A string representing a (postal) address
	 */
	pinAtAddress: function(anAddress) 
	{
		// GoogleMaps method to convert an address into a location
		var geocoder = new google.maps.Geocoder();
		var geocoderRequest = {
			address : anAddress
		};
		
		var me = this;
		geocoder.geocode(geocoderRequest, function (geocoderResults, geocoderStatus){
			if (geocoderStatus === 'OK')
			{
				// On a successful address->location translation, move the marker to that location
				var location = geocoderResults[0].geometry.location;
				var latLng = new google.maps.LatLng(location.lat(), location.lng());
				me._pinAtLatLng(latLng);
				me._cleanErrorSearchMessage();
			}
			else
			{
				me._setErrorSearchMessage("{{i18n PLUGINS_CORE_UI_GEOCODE_GMAP_DIALOG_SEARCH_NOT_FOUND}}" + anAddress);
			}
		});
	},
	
	/**
	 * Center the map at a given latLng
	 * @param {google.maps.LatLng} latLng The new location of the center of the map
	 */	
	centerMap: function(latLng)
	{
		if (this._gmapPanel.gmap)
		{
			if(latLng)
			{
				this._gmapPanel.gmap.setCenter(latLng);
			} 
			else 
			{
				var defaultLocation = new google.maps.LatLng(0, 0);
				this._gmapPanel.gmap.setCenter(defaultLocation);
			}
		}
	},
	
	/**
	 * @private 
	 * Move the marker at a given latLng and center the map
	 * @param {google.maps.LatLng} latLng The new location of the marker
	 */	
	_pinAtLatLng: function(latLng) 
	{
		this._removeExistingMarker();
		this.centerMap(latLng);
		this._newPinAt(latLng, Ext.bind(this._saveLatLng, this));				
	},
	
	
	/**
	 * @private 
	 * Save a latLng in the internal _currentLatLng
	 * @param {google.maps.LatLng} latLng The location to save
	 */	
	_saveLatLng: function (latLng)
	{
		this._currentLatLng = latLng;
	},
	

	/**
	 * @private 
	 * Remove the existing marker from the map.
	 */	
	_removeExistingMarker: function() 
	{
		if (this.locationMarker)
		{
			this.locationMarker.setMap(null);			
		}
	},
	
	/**
	 * @private 
	 * Adds a marker at a given location on the map. The marker is draggable.
	 * @param {google.maps.LatLng} latLng The location of the newly created marker.
	 * @param {Function} onMove The callback after a marker's drag/drop.
	 */	
	_newPinAt: function(latLng, onMove)
	{		
		function onDragEnd (event) 
		{
			// Save position on move
			this._saveLatLng (event.latLng);
		};
		
		// Setup marker at position 'latLng'
		var locationMarker = new google.maps.Marker({
			position : latLng,
			title : "",
			draggable : true,
			listeners : {
				dragend : Ext.bind(onDragEnd, this)
			}
		});
		
		// Add marker to map and save it
		this.locationMarker = this._gmapPanel.addMarker(locationMarker);
		
		// Save position
		this._saveLatLng(latLng)
	},
	
	
	/**
	 * @private 
	 * Function called when pressing the 'ok' button of the dialog box.<br>
	 * Calls the user-defined callback and closes the dialog box.
	 */
	_ok: function()
	{
		// Call the callback with the current latLng
		if (Ext.isFunction (this._cbFn) && this._currentLatLng) 
		{
			var value = {
					latitude: this._currentLatLng.lat(), 
					longitude: this._currentLatLng.lng()
			};
			
			this._cbFn(value);
		}
		
		if (this.locationMarker)
		{
			// Forget about the location marker
			this.locationMarker.setMap(null);
			this.locationMarker = null;
		}
		
		// Close the window
		this._gmapwindow.close();
	},
	
	/**
	 * @private 
	 * Function called when pressing the 'cancel' button of the dialog box.<br>
	 * Calls the user-defined callback with no arguements and closes the dialog box.
	 */
	_cancel: function()
	{
		// Call the callback with no value
		if (Ext.isFunction (this._cbFn))
		{
			this._cbFn();
		}	
		
		if (this.locationMarker)
		{
			// Forget about the location marker
			this.locationMarker.setMap(null);
			this.locationMarker = null;
		}
		
		// Close the window
		this._gmapwindow.close();
	}
});