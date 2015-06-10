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
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FILE_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/file_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_FILE'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/file_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FILE'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FILE_HINT'/>",
			    
				filter: null
			},
			
			image: {
				buttonText: "",
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_IMAGE_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/image_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_IMAGE'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/image_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_IMAGE'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_IMAGE_HINT'/>",
				
				filter: Ametys.helper.FileUpload.IMAGE_FILTER
			},
			
			multimedia: {
				buttonText: "",
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_MULTIMEDIA_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_MULTIMEDIA'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_MULTIMEDIA'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_MULTIMEDIA_HINT'/>",
				
				filter: Ametys.helper.FileUpload.MULTIMEDIA_FILTER
			},
			
			video: {
				buttonText: "",
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_VIDEO_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_VIDEO'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_VIDEO'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_VIDEO_HINT'/>",
				
				filter: Ametys.helper.FileUpload.VIDEO_FILTER
			},
			
			flash: {
				buttonText: "",
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FLASH_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_FLASH'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FLASH'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_FLASH_HINT'/>",
				
				filter: Ametys.helper.FileUpload.FLASH_FILTER
			},
			
			audio: {
				buttonText: "",
				buttonTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_SOUND_BUTTON'/>",
				buttonIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/sound_local_16.png',
				
				menuItemText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_EXTERNAL_SOUND'/>",
				menuItemIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/sound_local_16.png',
				
				dialogTitle: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_SOUND'/>",
				dialogHint: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_INSERT_LOCAL_SOUND_HINT'/>",
				
				filter: Ametys.helper.FileUpload.SOUND_FILTER
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
            icon: config.buttonIcon || this.self.filters[filter].buttonIcon,
            source: this.self.SOURCE
        };
	},
	
	getMenuItemConfig: function (config, filter)
	{
		filter = filter || 'none';
		
		return {
			text: this.self.filters[filter].menuItemText, 
	   		icon: this.self.filters[filter].menuItemIcon, 
	   		source: this.self.SOURCE
		}
	},
	
	/**
	 * @inheritdoc
     * Opens a dialog to upload a file from the local hard drive
     */
	handler: function (config, filter, callback)
    {
		filter = filter || 'none';
		
        Ametys.helper.FileUpload.open(
        	config.buttonIcon || this.self.filters[filter].buttonIcon, 
        	config.dialogTitle || this.self.filters[filter].dialogTitle, 
        	config.dialogHint || this.self.filters[filter].dialogHint,
        	callback,
            this.self.filters[filter].filter
        );
    }
});

Ametys.form.widget.File.registerFileSource (Ametys.form.widget.File.External.SOURCE, Ext.create('Ametys.form.widget.File.External', {}));
