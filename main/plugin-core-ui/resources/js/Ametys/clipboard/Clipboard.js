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
 * Singleton class defining the Clipboard
 */
Ext.define('Ametys.clipboard.Clipboard', {
	singleton: true,
	
	/**
	 * @property {String} _type The type of stored data.
	 * @private
	 */
	_type: null,
	
	/**
	 * @property {Object[]} _data The stored data.
	 * @private
	 */
	_data: [],
	
	/**
	 * Store a object into the clipboard
	 * @return {Object[]} A array of data stored into the clipboard. Can be empty if there is no data into the clipboard.
	 */
	getData: function ()
	{
		return this._data || [];
	},
	
	/**
	 * Get the type of stored data
	 * @return {String} The type
	 */
	getType: function ()
	{
		return this._type;
	},
	
	/**
	 * Store data into clipboard. 
	 * The given data will replace the preceding stored data.
	 * @param {String} type The type of stored data. It is recommended to use a message type defined in {@link Ametys.message.Message}.
	 * @param {Object/Object[]} data The data to store into the clipboard
	 */
	setData: function (type, data)
	{
		this._type = type;
		if (Ext.isArray (data))
		{
			this._data = data;
		}
		else
		{
			this._data = [data];
		}
		
		Ext.create("Ametys.message.Message", {
			type: Ametys.message.Message.MODIFIED,
			targets: {
				type: Ametys.message.MessageTarget.CLIPBOARD,
				parameters: {}
			}
		});
	}
});

Ext.define("Ametys.message.ClipboardMessageTarget",
	{
		override: "Ametys.message.MessageTarget",
		
		statics: 
		{
			/**
			 * @member Ametys.message.MessageTarget
			 * @readonly
			 * @property {String} CLIPBOARD The target type is the clipboard.
			 */
			CLIPBOARD: "clipboard"
		}
	}
);