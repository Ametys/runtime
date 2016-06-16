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
 * This singleton the object class associated to the file type 'external' in {@link Ametys.form.widget.File} widget.
 * It is used to upload a file from the local hard drive.
 * See {@link Ametys.form.widget.File} widget.
 */
Ext.define('Ametys.form.widget.File.External', {
	extend: 'Ametys.form.widget.File.FileSource',
	
	statics: {
		
		/**
		 * @property {String} SOURCE The file source which belongs to this class
		 * @static
		 * @readonly
		 */
		SOURCE: 'external',
		
		/**
		 * @property {String} TYPE The file type which belongs to this class
		 * @static
		 * @readonly
		 */
		TYPE: 'metadata',
		
		/**
		 * The filters configuration
		 * @static
		 * @private
		 */
		filters: {
			none: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FILE_BUTTON}}",
                buttonIconCls: 'flaticon-document9',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_FILE}}",
				menuItemIconCls: 'flaticon-document9',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FILE}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FILE_HINT}}",
			    
				filter: null
			},
			
			image: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_IMAGE_BUTTON}}",
				buttonIconCls: 'editor-image2',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_IMAGE}}",
				menuItemIconCls: 'editor-image2',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_IMAGE}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_IMAGE_HINT}}",
				
				filter: Ametys.helper.FileUpload.IMAGE_FILTER
			},
			
			multimedia: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_MULTIMEDIA_BUTTON}}",
				buttonIconCls: 'editor-movie16',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_MULTIMEDIA}}",
				menuItemIconCls: 'editor-movie16',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_MULTIMEDIA}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_MULTIMEDIA_HINT}}",
				
				filter: Ametys.helper.FileUpload.MULTIMEDIA_FILTER
			},
			
			video: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_VIDEO_BUTTON}}",
				buttonIconCls: 'editor-movie16',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_VIDEO}}",
				menuItemIconCls: 'editor-movie16',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_VIDEO}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_VIDEO_HINT}}",
				
				filter: Ametys.helper.FileUpload.VIDEO_FILTER
			},
			
			flash: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FLASH_BUTTON}}",
				buttonIconCls: 'editor-flash',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_FLASH}}",
				menuItemIconCls: 'editor-flash',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FLASH}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FLASH_HINT}}",
				
				filter: Ametys.helper.FileUpload.FLASH_FILTER
			},
			
			audio: {
				buttonText: "",
				buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_SOUND_BUTTON}}",
				buttonIconCls: 'flaticon-music168',
				
				menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_SOUND}}",
				menuItemIconCls: 'flaticon-music168',
				
				dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_SOUND}}",
				dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_SOUND_HINT}}",
				
				filter: Ametys.helper.FileUpload.SOUND_FILTER
			},
            
            pdf: {
                buttonText: "",
                buttonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_PDF_BUTTON}}",
                buttonIconCls: 'flaticon-pdf17',
                
                menuItemText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_PDF}}",
                menuItemIconCls: 'flaticon-pdf17',
                
                dialogTitle: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_PDF}}",
                dialogHint: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_PDF_HINT}}",
                
                filter: Ametys.helper.FileUpload.PDF_FILTER
            }
		}
	},
	
	getFileType: function ()
	{
		return this.self.TYPE;
	},

	getBtnConfig: function (config, filter)
	{
		filter = filter || 'none';
		
		return {
            text: config.buttonText || this.self.filters[filter].buttonText, 
            tooltip: config.buttonTooltip || this.self.filters[filter].buttonTooltip, 
            iconCls: config.buttonIconCls || this.self.filters[filter].buttonIconCls,
            source: this.self.SOURCE
        };
	},
	
	getMenuItemConfig: function (config, filter)
	{
		filter = filter || 'none';
		
		return {
			text: this.self.filters[filter].menuItemText, 
	   		iconCls: this.self.filters[filter].menuItemIconCls, 
	   		source: this.self.SOURCE
		}
	},
	
	/**
	 * @inheritdoc
     * Opens a dialog to upload a file from the local hard drive
     */
	handler: function (config, filter, allowedExtensions, callback)
    {
		filter = filter || 'none';
        
        Ametys.helper.FileUpload.open({
        	iconCls: config.buttonIconCls || this.self.filters[filter].buttonIconCls, 
        	title: config.dialogTitle || this.self.filters[filter].dialogTitle, 
        	helmessage: config.dialogHint || this.self.filters[filter].dialogHint,
        	callback: callback,
            filter: this.self.filters[filter].filter,
            allowedExtensions: allowedExtensions
        });
    }
});

Ametys.form.widget.File.registerFileSource (Ametys.form.widget.File.External.SOURCE, Ext.create('Ametys.form.widget.File.External', {}));
