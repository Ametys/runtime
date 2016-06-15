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
 * This class is the abstract class for all file sources used in {@link Ametys.form.widget.File} widget.<br>
 * Implement the #getFileType, #getBtnConfig, #getMenuItemConfig and #handler methods in your implementation. 
 * 
 * Do not forgot to register your class in {@link Ametys.form.widget.File} widget as the sample below:
 * 
 * 		Ametys.form.widget.File.registerFileSource ('MyType', MyTypeClass);
 * 
 */
Ext.define('Ametys.form.widget.File.FileSource', {
	
	/**
	 * Get the file type which belongs to this source
	 * @return {String} The file type
	 */
	getFileType: function ()
	{
		throw new Error("The method #getBtnConfig is not implemented in " + this.self.getName());
	},
	
	/**
	 * Get the button configuration
	 * @param {Object} config The widget initial configuration
	 * @param {String} filter The filter name for files
	 */
	getBtnConfig: function (config, filter)
	{
		throw new Error("The method #getBtnConfig is not implemented in " + this.self.getName());
	},
	
	/**
	 * Get the menu item configuration
	 * @param {Object} config The widget initial configuration
	 * @param {String} filter The filter name for files
	 */
	getMenuItemConfig: function (config, filter)
	{
		throw new Error("The method #getMenuItemConfig is not implemented in " + this.self.getName());
	},
	
	/**
	 * Function called when button or menu item is pressed.
	 * @param {Object} config The widget initial configuration
	 * @param {String} filter The filter name for files. Can be null to allow all files or to use file extensions instead
     * @param {String[]} allowedExtensions The allowed file extensions. Can be null to allowed all extensions or use filter instead
	 * @param {Function} callback The callback function. Has the following parameters:
	 * @param {String} callback.id The file id.
     * @param {String} callback.fileName The file name.
     * @param {Number} callback.fileSize The file size in bytes.
     * @param {Number} callback.viewHref A URL to view the file.
     * @param {Number} callback.downloadHref A URL to download the file.
	 */
	handler: function (config, filter, allowedExtensions, callback)
    {
		throw new Error("The method #handler is not implemented in " + this.self.getName());
    }
});

