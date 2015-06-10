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
 * This class provides a widget to select a file.<br>
 * 
 * This widget is the default widget registered for fields of type Ametys.form.WidgetManager#TYPE_FILE and Ametys.form.WidgetManager#TYPE_BINARY.<br>
 * 
 * This widget is configurable:<br>
 * - Use {@link #cfg-filter} to restrict files by types. The available filters are 'image', 'video', 'multimedia', 'audio' or 'flash'.<br>
 * - Use {@link #cfg-allowSources} to specify the authorized file sources (eg: 'external' for local resource, 'resource' for shared resource). Separated by comma.<br>
 * See the subclasses of {@link Ametys.form.widget.File.FileSource} for the available sources.<br>
 */
Ext.define('Ametys.form.widget.File', {
    
    extend: 'Ametys.form.AbstractField',
    alias: ['widget.edition.file'], //TODO: remove all aliases in this file
  
    statics: {
    	
    	/**
         * @property {Object} _fileSources The accepted file types (external, resource, ..)
         * @private
         */
        _fileSources: [],
        
        /**
         * Register a new file type
         * @param {String} source The file source as such 'external', 'resource', ...
         * @param {Ametys.form.widget.File.FileSource} className The object class associated with this source
         */
    	registerFileSource: function (source, className)
        {
        	this._fileSources[source] = className;
        },
        
        /**
         * Get the object class associated with the given file type
         * @param {String} source The file source as such 'external', 'resource', ...
         * @return {Ametys.form.widget.File.FileSource} The file source class
         */
        getFileSource: function (source)
        {
        	return this._fileSources[source];
        }
    },
    
    inheritableStatics: {
    	/**
		 * @property {String} IMAGE_FILTER Filter for image files
		 * @readonly
		 */
    	IMAGE_FILTER: 'image',
    	/**
		 * @property {String} MULTIMEDIA_FILTER Filter for multimedia files
		 * @readonly
		 */
    	MULTIMEDIA_FILTER: 'multimedia',
    	/**
		 * @property {String} VIDEO_FILTER Filter for video files
		 * @readonly
		 */
    	VIDEO_FILTER: 'video',
    	/**
		 * @property {String} VIDEO_FILTER Filter for audio files
		 * @readonly
		 */
    	AUDIO_FILTER: 'audio',
    	/**
		 * @property {String} FLASH_FILTER Filter for flash files
		 * @readonly
		 */
    	FLASH_FILTER: 'flash',
    	
    	/**
    	 * The filters configuration
    	 * @private
    	 */
    	filters: {
    		none: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FILE_BUTTON'/>",
    			buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/file_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_FILE_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FILE_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FILE_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_FILE'/>"
    		},
    		
    		image: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_IMAGE_BUTTON'/>",
				buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/image_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_IMAGE_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_IMAGE_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_IMAGE_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_IMAGE'/>"
    		},
    		
    		multimedia: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_MULTIMEDIA_BUTTON'/>",
    			buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_MULTIMEDIA_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_MULTIMEDIA_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_MULTIMEDIA_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_MULTIMEDIA'/>"
    		},
    		
    		video: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_VIDEO_BUTTON'/>",
    			buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_VIDEO_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_VIDEO_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_VIDEO_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_VIDEO'/>"
    		},
    		
    		flash: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FLASH_BUTTON'/>",
    			buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/multimedia_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_FLASH_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FLASH_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FLASH_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_FLASH'/>"
    		},
    		
    		audio: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_SOUND_BUTTON'/>",
    			buttonMenuIcon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/sound_local_16.png',
				
    			emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_SOUND_SELECTED'/>",
    			
    			deleteText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_SOUND_BUTTON'/>",
    			deleteTextConfirm: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_SOUND_CONFIRM'/>",
    			
    			downloadText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_SOUND'/>"
    		}
    	}
    },
    
    /**
     * @cfg {String} filter=none The filter to use for accepted files among: 'none', 'image', 'audio', 'video', 'multimedia' or 'flash'. Can be null to accept all files (equivalent to 'none').
     */
    /**
     * @cfg {String} allowSources=external The allowed file sources separated by coma. Default to 'external'.
     */
    
    /**
     * @cfg {Number} imagePreviewMaxHeight=100 The maximun height for image preview. This is only used with filter 'image' (see #cfg-fileFilter).
     */
    imagePreviewMaxHeight: 100,
    /**
     * @cfg {Number} imagePreviewMaxWidth=100 The maximun width for image preview. This is only used with filter 'image' (see #cfg-fileFilter).
     */
    imagePreviewMaxWidth: 100,
    
    /**
     * @cfg {String} buttonText The button text to display on the upload button.
     */
    /**
     * @cfg {String} buttonIcon The button icon path for the upload button.
     */
    
    /**
     * @cfg {Object} textConfig The configuration object for the display field. Note that many configuration can be set directly here and will we broadcasted to underlying field (allowBlank...). 
     */
    /**
     * @cfg {String} emptyText The default text to place into an empty field.
     */
    /**
     * @cfg {String} deleteText The text to display on delete button tooltip.
     */
    /**
     * @cfg {String} deleteTextConfirm The text to display when deleting a file.
     */
    
    /**
     * @cfg {Boolean} buttonOnly True to display the file upload field as a button with no visible
     * text field (defaults to true).  If true, all inherited TextField members will still be available.
     */
    buttonOnly: false,
    /**
     * @cfg {Number} buttonOffset The number of pixels of space reserved between the button and the text field
     * (defaults to 3).  Note that this only applies if {@link #buttonOnly} = false.
     */
    buttonOffset: 5,
    
    initComponent: function()
    {
    	// File icon or image
        var imgCfg = {autoEl: 'div', hidden: true};
        if (this.fileFilter == this.self.IMAGE_FILTER)
        {
            imgCfg.width = this.imagePreviewMaxWidth + 6; // image width + padding
            imgCfg.height = this.imagePreviewMaxHeight + 6;
            imgCfg.cls = 'x-form-file-widget-img';
            imgCfg.listeners = { 
                'afterrender': {
                    fn: function() { 
                        this.img.el.dom.style.lineHeight = (this.imagePreviewMaxHeight - 2) + "px"; 
                    }, 
                    scope: this, 
                    options: { single: true } 
                }
            }
        }
        else
        {
            imgCfg.width = 32; // image width + padding
            imgCfg.height = 32;
        }
    	this.img = Ext.create('Ext.Img', imgCfg);
    	
        // Display field.
        var textConfig = Ext.applyIf(this.textConfig || {}, {
            html: '',
            flex: 1,
            cls: 'x-form-file-widget-text x-form-file-widget-text-empty'
        });
        this.displayField = Ext.create('Ext.Component', textConfig);
        
        this.cls = 'x-form-file-widget';
        this.layout = 'hbox';
        this.items = [
            this.img,
            this.displayField
        ];
            
        if (!this.readOnly)
        {
            // Button which opens the upload dialog box.
            this.button = Ext.create('Ext.button.Button', this.getBtnConfig());
            
            // Button which deletes the selected file.
            var deleteButtonConfig = Ext.applyIf(this.deleteButtonConfig || {}, {
                icon: Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/resources-picker/file_delete_16.png',
                cls: 'x-form-file-widget-delete-btn x-btn-icon',
                
                disabled: this.disabled,
                
                tooltip: this.deleteText || this.self.filters[this.fileFilter].deleteText,
                handler: this.deleteFile,
                scope: this,
                hidden: true
            });
        
            this.deleteButton = Ext.create('Ext.button.Button', deleteButtonConfig);
            
            this.items.push(this.button);
            this.items.push(this.deleteButton);
        }
    
        this.callParent(arguments);
    },
    
    constructor: function (config)
    {
    	// Filter
        this.fileFilter = config['filter'] || 'none';
        delete config.filter;
        
        // File localtion
        this.fileSources = config['allowSources'] ? config['allowSources'].split(',') : [Ametys.form.widget.File.External.SOURCE];
        delete config.allowSources;
        
        this.callParent(arguments);
    },
    
    /**
     * Get the configuration of the button to pick/upload a resource
     * @return {Object} The button configuration
     */
    getBtnConfig: function ()
    {
    	if (this.fileSources.length > 1)
    	{
    		var menu = [];
    		
    		for (var i=0; i < this.fileSources.length; i++)
    		{
    			var source = Ametys.form.widget.File.getFileSource(this.fileSources[i]);
    			var itemCfg = source.getMenuItemConfig (this.getInitialConfig(), this.fileFilter);
    			
    			menu.push(Ext.apply(itemCfg, {
    				xtype: 'menuitem',
                    handler: this._selectFile,
                    scope: this
                }))
    		}
    		
    		return {
    			text: this.getInitialConfig('buttonText') || this.self.filters[this.fileFilter].buttonMenuText, 
                tooltip: this.getInitialConfig('buttonTooltip') || this.self.filters[this.fileFilter].buttonMenuTooltip, 
                icon: this.getInitialConfig('buttonIcon')  || this.self.filters[this.fileFilter].buttonMenuIcon,
                
                menu: menu,
                
                disabled: this.disabled
            }
    	}
    	else
    	{
    		var source = Ametys.form.widget.File.getFileSource(this.fileSources[0]);
        	var btnCfg = source.getBtnConfig (this.getInitialConfig(), this.fileFilter);
        	
    		return Ext.apply(btnCfg, {
                handler: this._selectFile,
                scope: this,
                
                disabled: this.disabled
            });
    	}
    },
    
    /**
     * Select a file.
     * @param {Ext.Button} btn The button calling this function
     */
    _selectFile: function (btn)
    {
    	var source = Ametys.form.widget.File.getFileSource(btn.source);
    	source.handler (this.getInitialConfig(), this.fileFilter, Ext.bind(this._insertResourceCb, this, [source.getFileType()], true));
    },
    
    /**
     * Callback function, called when a resource is uploaded in the dialog.
     * @param {String} id The uploaded file id.
     * @param {String} filename The uploaded file name.
     * @param {Number} fileSize The uploaded file size in bytes.
     * @param {Number} viewHref A URL to view the file.
     * @param {Number} downloadHref A URL to download the file.
     * @param {String} type The type of the resource. Can be null.
     * @private
     */
    _insertResourceCb: function(id, filename, fileSize, viewHref, downloadHref, type)
    {
        if (id == null)
        {
            return;
        }
        
        this.setValue({
        	id: id,
        	filename: filename,
        	size: fileSize,
        	viewUrl: viewHref,
        	downloadUrl: downloadHref,
        	type: type
        });
    },
    
    /**
     * Delete the file.
     * @private
     */
    deleteFile: function()
    {
        // Show the confirmation dialog.
        Ametys.Msg.confirm(
        	this.getInitialConfig('deleteText') || this.self.filters[this.fileFilter].deleteText, 
        	this.getInitialConfig('deleteTextConfirm') || this.self.filters[this.fileFilter].deleteTextConfirm,
            function (btn) {
                if (btn == 'yes')
                {
                    this.setValue();
                }
            },
            this
        );
    },
    
    getErrors: function (value)
    {
    	value = value || this.getValue();
    	
    	var errors = [];
    	
    	if (!this.allowBlank && (!value || !value.id))
    	{
    		errors.push(this.blankText);
    	}
    	
    	return errors;
    },
    
    /**, 
     * Sets a data value into the field.
     * @param {Object} value The value to set.
     * @param {String} [value.id] The file identifier.
     * @param {String} value.filename The file name (if applicable).
     * @param {String} value.type The file object type ('metadata').
     * @param {Number} value.size The file size in bytes.
     * @param {Number} value.lastModified The file's last modification date.
     * @param {Number} value.viewUrl A URL to view the file.
     * @param {Number} value.downloadUrl A URL to download the file.
     */
    setValue: function (value)
    {
    	if (value && !Ext.Object.isEmpty(value))
    	{
    		value = Ext.applyIf (value, {
        		id: 'untouched',
        		type: 'external'
        	});
    	}
    	else
    	{
    		value = {};
    	}
    	
    	this.callParent([value]);

        Ext.suspendLayouts();
    	this._updateUI();
        Ext.resumeLayouts(true);
    },
    
    isEqual: function(value1, value2) 
    {
    	return value1 != null && value2 != null && value1.id === value2.id;
    }, 
    
    getSubmitData: function ()
    {
    	var data = {};
    	data[this.name] = Ext.encode(this.value);
        return data;
    },
    
    getReadableValue: function ()
    {
    	return this.value ? this.value.filename : '';
    },
    
    /**
     * Update UI
     * @private
     */
    _updateUI: function()
    {
    	var value = this.value;
    	
    	if (!value || !value.id)
    	{
    		this.img.hide();
    		// Update the file description.
            this.displayField.addCls('x-form-file-widget-text-empty');
            this.displayField.setHeight(21); // Let's freeze height to avoid useless layout afterward
            
            if (!this.readOnly)
            {
                this.deleteButton.hide();
                this.button.show();
            }
            
            this.displayField.update('<span class="empty">' + (this.getInitialConfig('emptyText') || this.self.filters[this.fileFilter].emptyText) + '</span>');
    	}
    	else
    	{
        	this.displayField.removeCls('x-form-file-widget-text-empty');
            
            if (!this.readOnly)
            {
                this.deleteButton.show();
                this.button.hide();
            }
            
            // Update file description after resizing the fields to be able to fit the file name to the available space.
            this._updateFileDescription(value.id, value.filename, value.size, value.viewUrl, value.downloadUrl);
        }
    },
    
    /**
     * Construct the file description (icon, name, size and link to download it) and update the display field.
     * @param {String} id The file id.
     * @param {String} fileName The file name
     * @param {Number} fileSize The file size in bytes.
     * @param {Number} viewHref A URL to view the file.
     * @param {Number} downloadHref A URL to download the file.
     * @return The HTML described file
     * @private
     */
    _updateFileDescription: function(id, fileName, fileSize, viewHref, downloadHref)
    {
    	var iconPath;
    	if (this.fileFilter == this.self.IMAGE_FILTER)
    	{
    		iconPath = viewHref + '&maxWidth=' + this.imagePreviewMaxWidth + '&maxHeight=' + this.imagePreviewMaxHeight + '&foo=' + Math.random();
    	}
    	else
    	{
    		iconPath = this._getIconPath(fileName);
    	}
    	
    	this.img.setSrc(iconPath);
    	this.img.show();
    	
        // Do not write the file name in the link just now.
        var text = '<a href="' + downloadHref + '" title="' + (this.getInitialConfig('downloadText') || this.self.filters[this.fileFilter].downloadText) + '">' + fileName + '</a>';
        
        if (fileSize)
        {
            text += '<br/><span class="filesize">' + Ext.util.Format.fileSize(fileSize) + '</span>';
            this.displayField.setHeight(34); // Let's freeze height to avoid useless layout afterward
        }
        else
        {
            this.displayField.setHeight(21); // Let's freeze height to avoid useless layout afterward
        }
        
        this.displayField.update(text);
    },
    
    /**
     * Get the icon path of the file from its name.
     * @param {String} fileName The file name
     * @return The icon path
     * @private
     */
    _getIconPath: function(fileName)
    {
        var extension = 'unknown';
        if (fileName)
        {
            var index = fileName.lastIndexOf('.');
            if (index > 0)
            {
                extension = fileName.substring(index + 1).toLowerCase();
            }
        }
        return Ametys.getPluginResourcesPrefix('explorer') + '/img/resources/icon-medium/' + extension + '.png';
    }
});
