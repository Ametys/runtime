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
        },

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
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FILE_BUTTON}}",
    			buttonMenuIconCls: 'flaticon-document9',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_FILE_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FILE_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FILE_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_FILE}}"
    		},
    		
    		image: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_IMAGE_BUTTON}}",
				buttonMenuIconCls: 'editor-image2',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_IMAGE_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_IMAGE_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_IMAGE_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_IMAGE}}"
    		},
    		
    		multimedia: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_MULTIMEDIA_BUTTON}}",
    			buttonMenuIconCls: 'editor-movie16',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_MULTIMEDIA_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_MULTIMEDIA_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_MULTIMEDIA_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_MULTIMEDIA}}"
    		},
    		
    		video: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_VIDEO_BUTTON}}",
    			buttonMenuIconCls: 'editor-movie16',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_VIDEO_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_VIDEO_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_VIDEO_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_VIDEO}}"
    		},
    		
    		flash: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_FLASH_BUTTON}}",
    			buttonMenuIconCls: 'editor-flash',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_FLASH_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FLASH_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_FLASH_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_FLASH}}"
    		},
    		
    		audio: {
    			buttonMenuText: "",
    			buttonMenuTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_SELECT_SOUND_BUTTON}}",
    			buttonMenuIconCls: 'flaticon-music168',
				
    			emptyText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_NO_SOUND_SELECTED}}",
    			
    			deleteText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_SOUND_BUTTON}}",
    			deleteTextConfirm: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DELETE_SOUND_CONFIRM}}",
    			
    			downloadText: "{{i18n PLUGINS_CORE_UI_WIDGET_RESOURCES_PICKER_DOWNLOAD_SOUND}}"
    		}
    	}
    },
    
    /**
     * @cfg {String} filter=none The filter to use for accepted files amongst: 'none', 'image', 'audio', 'video', 'multimedia' or 'flash'. Can be null to accept all files (equivalent to 'none').
     */
    /**
     * @cfg {String|String[]} allowSources=external The allowed file sources in a Array or separated by coma. Default to 'external'.
     */
    
    /**
     * @cfg {Number} imagePreviewMaxHeight=100 The maximum height for image preview. This is only used with filter 'image' (see #cfg-fileFilter).
     */
    imagePreviewMaxHeight: 100,
    /**
     * @cfg {Number} imagePreviewMaxWidth=100 The maximum width for image preview. This is only used with filter 'image' (see #cfg-fileFilter).
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
	 * @cfg {String} [deleteButtonIcon] The full path to the delete button icon (in 16x16 pixels). Can be null to use #cfg-deleteButtonIconCls instead
	 */
     /**
     * @cfg {String} [deleteButtonIconCls=flaticon-remove11] The CSS class to apply to delete button icon
     */
    deleteButtonIconCls: 'flaticon-remove11', 
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
    
    /**
     * @cfg {String} imgCls The CSS to use for file's glyph. Only used if the file filter is not #IMAGE_FILTER
     */
    glyphCls: 'a-form-file-widget-glyph',
    
    /**
     * @cfg {String} imgCls The CSS to use for image preview. Only used if the file filter is #IMAGE_FILTER
     */
    imgCls: 'a-form-file-widget-img',
    
    
    /**
     * @cfg {String} imgCls The CSS to use for image preview. Only used if the file filter is #IMAGE_FILTER
     */
    imgWithBorderCls: 'a-form-file-widget-img-border',
    
    initComponent: function()
    {
        this.cls = this.emptyCls;
        this.layout = {
            type: 'hbox',
            //align: 'stretch'
        };
        
        this.items = [];
        
        if (this.fileFilter == this.self.IMAGE_FILTER)
        {
            // Preview image
            this.img = Ext.create('Ext.Img', {
                autoEl: 'div', 
                hidden: true,
                width: this.imagePreviewMaxWidth + 6, // image width + padding
                height: this.imagePreviewMaxHeight + 6, // image height + padding
                cls: this.imgWithBorderCls,
                listeners: {
                    'afterrender': {
	                    fn: function() { 
	                        this.img.el.dom.style.lineHeight = (this.imagePreviewMaxHeight - 2) + "px"; 
	                    }, 
	                    scope: this, 
	                    options: { single: true } 
	                }
                }
            });
            
            this.items.push(this.img);
        }
        else
        {
            // File glyph
        	this.fileGlyph = Ext.create('Ext.Component', {
	           height: 48,
	           width: 48,
	           cls: this.glyphCls
	        });
            
            this.items.push(this.fileGlyph);
        }
        
        // Display field.
        var textConfig = Ext.applyIf(this.textConfig || {}, {
            html: '',
            flex: 1,
            cls: Ametys.form.AbstractField.READABLE_TEXT_CLS
        });
        this.displayField = Ext.create('Ext.Component', textConfig);
        this.items.push(this.displayField);
        
        if (!this.readOnly)
        {
            // Button which opens the upload dialog box.
            this.button = Ext.create('Ext.button.Button', this.getBtnConfig());
            
            // Button which deletes the selected file.
            var deleteButtonConfig = Ext.applyIf(this.deleteButtonConfig || {}, {
                icon: this.deleteButtonIcon,
                iconCls: this.deleteButtonIcon ? null : this.deleteButtonIconCls,
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
        
        // File location
        if (Ext.isEmpty(config['allowSources']))
        {
        	this.fileSources = [Ametys.form.widget.File.External.SOURCE];
        }
        else
        {
        	this.fileSources = Ext.isArray(config['allowSources']) ? config['allowSources'] : config['allowSources'].split(',');
        }
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
                iconCls: this.getInitialConfig('buttonIconCls')  || this.self.filters[this.fileFilter].buttonMenuIconCls,
                
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
    	
    	var errors = this.callParent(arguments);
    	
    	if (!this.allowBlank && (value && !value.id))
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
    		this._showHideImgPreview(false);
    		// Update the file description.
            this.displayField.setHeight(21); // Let's freeze height to avoid useless layout afterward
            
            if (!this.readOnly)
            {
                this.deleteButton.hide();
                this.button.show();
            }
            
            this.displayField.update(this.getInitialConfig('emptyText') || this.self.filters[this.fileFilter].emptyText);
    	}
    	else
    	{
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
     * @private
     * Show or hide the image preview or fiele glyph
     * @param {Boolean} show true to show
     */
    _showHideImgPreview : function (show)
    {
        if (this.fileFilter == this.self.IMAGE_FILTER)
        {
            this.img.setVisible(show);
        }
        else
        {
            this.fileGlyph.setVisible(show);
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
    	if (this.fileFilter == this.self.IMAGE_FILTER)
    	{
    		var imgSrc = viewHref + '&maxWidth=' + this.imagePreviewMaxWidth + '&maxHeight=' + this.imagePreviewMaxHeight + '&foo=' + Math.random();
            this.img.setSrc(imgSrc);
    	}
        else
        {
            var iconGlyph = Ametys.explorer.tree.ExplorerTree.getFileIconGlyph(fileName);
            this.fileGlyph.update('<span class="' + iconGlyph + '"></span>');
        }
    	
        this._showHideImgPreview(true);
        
        // Do not write the file name in the link just now.
        var text = '<a href="' + downloadHref + '" title="' + (this.getInitialConfig('downloadText') || this.self.filters[this.fileFilter].downloadText) + '">' + fileName + '</a>';
        
        if (fileSize)
        {
            text += '<br/><span class="ametys-field-hint">' + Ext.util.Format.fileSize(fileSize) + '</span>';
            this.displayField.setHeight(34); // Let's freeze height to avoid useless layout afterward
        }
        else
        {
            this.displayField.setHeight(21); // Let's freeze height to avoid useless layout afterward
        }
        
        this.displayField.update(text);
    }
});
