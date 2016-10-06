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
 * This class provides a widget to set coordinates (longitude, latitude) by pointing a marker on a GoogleMaps popup window.<br>
 * See {@link Ametys.helper.ChooseLocation}<br>
 * 
 * This widget is the default widget registered for fields of type Ametys.form.WidgetManager#TYPE_GEOCODE.<br>
 * It does NOT handle multiple values.<br>
 * 
 * Use {@link #cfg-initialAddressFormFields} to initialize the address to center the map.<br>
 */
Ext.define('Ametys.form.widget.GeoCode', {
	extend : 'Ametys.form.AbstractField',
	
	/**
	 * @cfg {String} initialAddressFormFields The relative path of form fields composing the initial address, separated by comma.
	 */
	/**
	 * @cfg {String} chooseLocationWindowTitle Title of the dialog box to choose location. See {@link Ametys.helper.ChooseLocation#open}.
	 */
	/**
	 * @cfg {String} chooseLocationWindowIcon The full icon path the dialog box to choose location. See {@link Ametys.helper.ChooseLocation#open}. 
	 */
	/**
	 * @cfg {String} chooseLocationHelpMessage The help message to display on top of dialog box to choose location. See {@link Ametys.helper.ChooseLocation#open}. 
	 */
	/**
	 * @cfg {Number} [chooseLocationDefaultLatitude=0] Initial latitude where to center the map when no address nor coordinates are setted
	 */
	/**
	 * @cfg {Number} [chooseLocationDefaultLongitude=0] Initial longitude where to center the map when no address nor coordinates are setted
	 */
	/**
	 * @cfg {Number} [chooseLocationDefaultZoomLevel] Initial zomm level where to center the map when no address nor coordinates are setted
	 */
	
	/**
	 * @cfg {String} buttonIcon The full path to the button icon (in 16x16 pixels)
	 */
	buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/geolocation/geolocation_16.png',
	/**
	 * @cfg {String} buttonTooltipText The button tooltip text
	 */	
	buttonTooltipText : "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_SHOW_MAP_BUTTON_TOOLTIP}}",
	/**
	 * @cfg {String} deleteButtonIcon The full path to the delete button icon (in 16x16 pixels)
	 */
	deleteButtonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/file_delete_16.png', 
	/**
	 * @cfg {String} deleteTooltipText The delete button tooltip text
	 */	
	deleteTooltipText : "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_DELETE_BUTTON}}",
	/**
	 * @cfg {String} deletePopupTitle The title of the delete confirmation popup 
	 */	
	deleteTitle : "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_DELETE_CONFIRM_TITLE}}",
	/**
	 * @cfg {String} deleteConfirm The text of the delete confirmation popup 
	 */	
	deleteConfirm : "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_DELETE_CONFIRM_CONTENT}}",	
	/**
	 * @cfg {String} emptyText The text for empty field
	 */
	emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_NO_COORDINATES}}",
	
	/**
	 * @property {Object[]} _formFieldsBuildingTheAddress Form fields building the initialAddress  
	 * @private
	 */
	_formFieldsBuildingTheAddress: [],
	
	/**
	 * @property {String[]} _addressFieldName The name or path of fields composing the address
	 * @private
	 */
	_addressFieldNames: [],
	
	/**
	 * @inheritdoc
	 * Initializes the longitude/latitude fields, and the showOnMap button
	 */
	initComponent : function() 
	{
		var me = this;
				
	    // Latitude/longitude field.
		var latitudeLongitudeConfig = Ext.applyIf(this.latitudeLongitudeConfig || {}, {
			cls: Ametys.form.AbstractField.READABLE_TEXT_CLS,
			html: '',
			flex: 1
		});
		this.latitudeLongitudeField = Ext.create('Ext.Component', latitudeLongitudeConfig);

		// Button to open the map popup.
		var mapPopupConfig = Ext.applyIf(this.mapPopupConfig || {}, {			
			icon: this.buttonIcon,
			tooltip: this.buttonTooltipText,
			handler : this._showMapPopup,
			scope : this
		});
		this._mapPopupButton = Ext.create('Ext.button.Button', mapPopupConfig);
		
		// Button which deletes the value.
		var deleteButtonConfig = Ext.applyIf(this.deleteButtonConfig || {}, {
			icon: this.deleteButtonIcon,          
			tooltip: this.deleteTooltipText,
			handler: this._deleteValue,
			scope: this,
			hidden: true
		});
		this._deleteButton = Ext.create('Ext.button.Button', deleteButtonConfig);
      
		this.items = [ this.latitudeLongitudeField, this._mapPopupButton, this._deleteButton ];			

		this.layout = 'hbox';
		this.cls = this.emptyCls;
		
		this.callParent(arguments);
	},	
	
	constructor: function (config)
	{
		this.callParent(arguments);
		
		this._addressFieldNames = config.initialAddressFormFields ? config.initialAddressFormFields.split(',') : [];
		
		if (this._addressFieldNames.length > 0)
		{
			this.form.executeFormReady(this._getAddressFormFields, this);
		}
	},
	
	/**
	 * Get the fields composing the address search input
	 * @return {Ext.form.Field[]} The fields
	 * @private
	 */
	_getAddressFormFields: function ()
	{
		this._addressFields = {};
		
		if (this._addressFieldNames.length > 0)
		{
			var me = this,
				form = me.form,
				position = 0;
			
			Ext.each(this._addressFieldNames, function(fieldName)
			{
				var prefix = me.name.substring(0, me.name.lastIndexOf('.'));
				
				var path = fieldName.split('/');
				for (var i=0; i < path.length; i++)
				{
					if (path[i] == '..')
					{
						prefix = prefix.substring(0, prefix.lastIndexOf('.'));
						fieldName = fieldName.substring(3);
					}
				}
				
				// Separator in composites path is '/' whereas javascript path separator must be '.'
				fieldName = fieldName.replace(/\//g, '.');
						
				var field = form.getField(prefix + '.' + fieldName);
				var remotePath = '';
				while (field == null && fieldName.length > 0)
				{
					var index = fieldName.lastIndexOf('.');
					if (index != -1)
					{
						remotePath = fieldName.substring(index + 1) + (remotePath.length > 0 ? '.' : '') + remotePath;
						fieldName = fieldName.substring(0, index);
						field = form.getField(prefix + '.' + fieldName);
					}
					else
					{
						fieldName = '';
					}
				}
				
				if (!field)
				{
					var message = "{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_UNKNOWN_FIELD}}" + prefix + '.' + fieldName;
					me.getLogger().error(message);
				}
				else
				{
					field.on('change', Ext.bind(me._onInitialAddressChange, me));
					
					if (Ext.isEmpty(remotePath))
					{
						me._addressFields[position] = {fieldName: field.getName()};
					}
					else
					{
						me._addressFields[position] = {fieldName: field.getName(), remotePath: remotePath};
					}
					position++;
				}
			});
		}
	},
	
	/**
	 * Compile the address search input
	 * @return {String} The address
	 * @private
	 */
	_compileAddress: function (defaultLatLng, defaultZoomLevel)
	{
		this._fieldValues = {};
		
		var needServerCall = false;
		
		Ametys.data.ServerComm.suspend();
		
		var position = 0;
		for (var i in this._addressFields)
		{
			var field = this.form.getField(this._addressFields[i].fieldName);
			
			if (field.getValue())
			{
				var remotePath = this._addressFields[i].remotePath;
				if (!Ext.isEmpty(remotePath))
				{
					var contentId = field.getValue();
					if (Ext.isArray(contentId))
					{
						contentId = contentId[0];
					}
					
					this._fieldValues[position] = null;
					
					Ametys.data.ServerComm.callMethod({
						role: "org.ametys.cms.content.ContentHelper", 
						methodName: 'getMetadataValues', 
						parameters: [contentId, [remotePath]],
						callback: {
							handler: this._compileAddressCb,
							scope: this,
							arguments: {
								position: position,
								remotePath: remotePath,
								defaultLatLng: defaultLatLng,
								defaultZoomLevel: defaultZoomLevel
							}
						}
					});
					
					needServerCall = true;
				}
				else
				{
					this._fieldValues[position] = field.getValue();
				}
				
				position++;
			}
		}
		
		Ametys.data.ServerComm.restart();
		
		if (!needServerCall)
		{
			this._openGoogleMap(defaultLatLng, defaultZoomLevel);
		}
	},

	/**
	 * @private
	 * Callback function after retrieving remote fields
	 * @param {Object} result The JSON result
	 * @param {Object} args The callback arguments
	 */
	_compileAddressCb: function (result, args)
	{
		if (result)
		{
			this._fieldValues[args.position] = result[args.remotePath] || '';
		}
		else
		{
			this._fieldValues[args.position] = '';
		}
		
		var addressComplete = true;
		for (var i in this._fieldValues)
		{
			if (this._fieldValues[i] == null)
			{
				addressComplete = false;
				break;
			}
		}
		
		if (addressComplete)
		{
			this._openGoogleMap(args.defaultLatLng, args.defaultZoomLevel);
		}
	},
	
	/**
	 * @private
	 * Open the Google map in a dialog box
	 * @param {Object} defaultLatLng The default latitude and longitude to center the map
	 * @param {Object} defaultLatLng.latitude The latitude
	 * @param {Object} defaultLatLng.longitude The longitude
	 * @param {String} defaultZoomLevel The default zoom level of the map
	 */
	_openGoogleMap: function (defaultLatLng, defaultZoomLevel)
	{
		var values = [];
		for (var i in this._fieldValues)
		{
			if (!Ext.isEmpty(this._fieldValues[i]))
			{
				values.push(this._fieldValues[i]);
			}
		}
		
		var config = {
			initialLatLng : this.value,
			defaultLatLng: defaultLatLng,
			initialAddress : values.length > 0 ? values.join(", ") : '',
			title: this.chooseLocationWindowTitle,
			icon: this.chooseLocationWindowIcon,
			helpMessage: this.chooseLocationHelpMessage
		};
		
		if (defaultZoomLevel)
		{
			config.defaultZoomLevel = parseInt(defaultZoomLevel);
		}
			
		// Launching the GoogleMaps popup window. 
		Ametys.helper.ChooseLocation.open(config, Ext.bind(this._chooseLocationCallback, this));
	},
	
	getErrors: function (value)
	{
		value = value || this.getValue();
		
		var errors = this.callParent(arguments);
   	
	   	if (!this.allowBlank && value && (!value.latitude || !value.longitude))
	   	{
	   		errors.push(this.blankText);
	   	}
   	
	   	return errors;
	},  
   
	isEqual: function (value1, value2)
	{   	    
      	if (value1 != null && value2 != null)
      	{
      		return value1.longitude === value2.longitude && value1.latitude === value2.latitude;      	
      	} 
      	else if (value1 == null && value2 == null) 
      	{
      		return true;	
      	} 
      	else 
      	{
      		return false;
      	}   	      	
	}, 	
	
	getSubmitValue: function ()
    {
    	return !this.value ? null : Ext.JSON.encode(this.value);
    },
    
	getReadableValue: function ()
	{
		if (this.value && this.value.latitude)
		{
			return parseFloat(this.value.latitude).toFixed(4) + "°N , " + parseFloat(this.value.longitude).toFixed(4) + "°E"
		}
		else
		{
			return this.emptyText;
		}
	},
	
	/**
	 * @inheritdoc
     * Sets a data value into the field and updates the display field
     * @param {Object} value The value to set.
     * @param {Number} value.latitude The latitude to set
     * @param {Number} value.longitude The longitude to set
     */
	setValue: function (value) 
	{	
		this.callParent([value]);
		this._updateUI();
	},
	
	/**
	 * @inheritdoc
	 */
	afterRender: function()
    {
    	this.callParent(arguments);
    	this._updateUI();
    },
	
	/**
	 * Update UI
	 * @private
	*/
	_updateUI: function()
	{	
		var value = this.value;
		
		if (!this.rendered)
    	{
    		return;
    	}
		
		if (!value || !value.latitude)
		{
            this._deleteButton.hide();
		}
		else
		{
            this._deleteButton.show();
		}
		
		this._updateDisplayField();
	},	  
	
	/**
	 * Update the display field as a understanding value for the end user
	 * @private
	*/
	_updateDisplayField: function()
	{
		if (!this.rendered)
    	{
    		return;
    	}
		
		this.latitudeLongitudeField.update(this.getReadableValue());
	},
	
	/**
    * Delete the coordinates.
    * @private
    */
	_deleteValue: function()
	{
       // Show the confirmation dialog.
       Ametys.Msg.confirm (this.deleteTitle, this.deleteConfirm,
           function (btn) {
               if (btn == 'yes')
               {
                   this.setValue();
                   this.clearWarning();
               }
           },
           this
       );
	},
	
	/**
	 * @private
	 * Deals with changes in the form fields building the initial address  
	 */
	_onInitialAddressChange: function()
	{
		if (this.getValue())
		{
			this.markWarning("{{i18n PLUGINS_CORE_UI_WIDGET_GEOCODE_ADDRESS_CHANGE}}");
		} 
	},
	
	/**
	 * @private
	 * The launcher for the GoogleMaps popup window.
	 */
	_showMapPopup : function() 
	{
		var defaultLatLng = null;
		if (this.chooseLocationDefaultLatitude && this.chooseLocationDefaultLongitude)
		{
			defaultLatLng = {
				latitude : this.chooseLocationDefaultLatitude,
				longitude : this.chooseLocationDefaultLongitude
			};
		};
		
		this._compileAddress(defaultLatLng, this.chooseLocationDefaultZoomLevel);
		
	},
	
	/**
	 * @private
	 * Callback function called after choosing location.<br>
	 * Update the field value.
	 * @param {Object} location The selected coordinates :
	 * @param {Number} location.latitude The latitude
	 * @param {Number} location.longitude The latitude
	*/
	_chooseLocationCallback: function (location)
	{
		if (location)
		{
			this.setValue(location);
			this.clearWarning();
		}
	}
});
