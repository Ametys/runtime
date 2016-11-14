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
 * This UI helper provides a dialog box to upload a file from hard drive
 * See #open method.
 */
Ext.define('Ametys.helper.FileUpload', {
	singleton: true,

	/**
	 * @property _cbFn {Function} The call back function to call after choosing file
	 * @private
	 */
	
	/**
	 * @property _filter {String} The file name filter
	 * @private
	 */
	
	/**
	 * @property _box {Ametys.window.DialogBox} The dialog box
	 * @private
	 */
	
    /**
     * Allow the user to choose a resource file from its local hard drive
     * @param {Object} config The configuration options:
     * @param {String} [config.icon] The full path to icon (16x16) for the dialog box. If null the iconCls will be used
     * @param {String} [config.iconCls=ametysicon-upload119] One or more CSS classes to apply to dialog's icon. Can be null to use the default one.
     * @param {String} config.title The title of the dialog box.
     * @param {String} config.helpmessage The message displayed at the top of the dialog box.
     * @param {Function} config.callback The method that will be called when the dialog box is closed. The method signature is <ul><li>node : The tree node currently selected or null if no selection has been made (cancel)</li></ul> The method can return false to made the dialog box keep open (you should display an error message in this case)
     * @param {Function} [config.filter] The filter for the filename. Choose between constants. Argument is the file name and return a boolean.
     * @param {String[]} [config.allowedExtensions] The allowed extensions for the filename. Can be null. If use the filter function will be forced to Ametys.helper.FileUpload#EXTENSIONS_FILTER
     */
    open: function (config)
    {
        this._cbFn = config.callback;
        this._filter = config.filter;
        this._allowedExtensions = config.allowedExtensions;
        
        if (this._allowedExtensions)
        {
            // Force filter
            this._filter = Ametys.helper.FileUpload.EXTENSION_FILTER;
        }
        
        this._initialize(config.icon, config.iconCls, config.title, config.helpmessage);
        this._box.show();
    },
	
	/**
	 * Initialize the dialog box
	 * @param {String} iconCls One or more CSS classes to apply to dialog's icon
	 * @param {String} title The title of the dialog box.
	 * @param {String} helpmessage The message displayed at the top of the dialog box.
	 * @private
	 */
	_initialize:  function(icon, iconCls, title, helpmessage)
	{
		if (!this._initialized)
		{
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: title,
                icon: icon,
                iconCls: icon ? null : (iconCls || 'ametysicon-upload119'),
				
				width: 430,
				scrollable: true,
				layout: 'fit',
				
				items: [{
							xtype: 'form',
                            layout: 'vbox',
							border: false,
							defaults: {
								cls: 'ametys',
								labelAlign: 'top',
								labelSeparator: ''
							},
							items: [{
										xtype: 'component',
										cls: 'a-text',
										html: helpmessage || ''
									}, 
									{
										xtype: 'filefield',
										name: 'file',
										id: 'fileupload-file',
										emptyText: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_FIELD_EMPTYTEXT}}",
									    fieldLabel: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_FIELD_FIELDLABEL}}",
									    buttonText: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_FIELD_BUTTONTEXT}}",
										width: 370,
										
										listeners: {'change': {fn: this._selectFile, scope: this}}
									}
							]
						}
				],
				
				defaultFocus: 'fileupload-file',
				
				closeAction: 'hide',
				buttons : [{
					text :"{{i18n PLUGINS_CORE_UI_FILEUPLOAD_BOX_OK}}",
					disabled: true,
					handler : Ext.bind(this._submit, this)
				}, {
					text :"{{i18n PLUGINS_CORE_UI_FILEUPLOAD_BOX_CANCEL}}",
					handler: Ext.bind(function() {this._box.hide();}, this) 
				}]
			});
			
			this._initialized = true;
		}
		else
		{
			this._box.down('#fileupload-file').reset();
            if (icon)
            {
                this._box.setIcon(icon);
                this._box.setIconCls(null);
            }
            else
            {
                this._box.setIconCls(iconCls || 'ametysicon-upload119');
                this._box.setIcon(null);
            }
			this._box.setTitle(title);
			this._box.down('form').items.get(0).update(helpmessage);
		}
	},
	
	/**
	 * Filter for image files
	 * @param {String} filename The file name
	 */
	IMAGE_FILTER: function(filename)
	{
		return /\.jpg|\.jpeg|\.gif|\.png$/i.test(filename);
	},
	/**
	 * @property {String} IMAGE_FILTER_LABEL Label for image filter
	 * @readonly
	 */
	IMAGE_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_IMAGEFILTER}}",
	
	/**
	 * Filter for videos and audio files
	 * @param {String} filename The file name
	 */
	VIDEO_FILTER: function(filename)
	{
		return /\.swf|\.flv$/i.test(filename);
	},
	/**
	 * @property {String} VIDEO_FILTER_LABEL Label for video filter
	 * @readonly
	 */
	VIDEO_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_VIDEOFILTER}}",
	
	/**
	 * Filter for videos and audio files
	 * @param {String} filename The file name
	 */
	MULTIMEDIA_FILTER: function(filename)
	{
	    return /\.swf|\.flv|\.mp3$/i.test(filename);
	},
	/**
	 * @property {String} MULTIMEDIA_FILTER_LABEL Label for multimedia filter
	 * @readonly
	 */
	MULTIMEDIA_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_MULTIMEDIAFILTER}}",
	
	/**
	 * Filter for audio files
	 * @param {String} filename The file name
	 */
	SOUND_FILTER: function(filename)
	{
		return /\.mp3$/i.test(filename);
	},
    /**
     * @property {String} SOUND_FILTER_LABEL Label for sound filter
     * @readonly
     */
    SOUND_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_SOUNDFILTER}}",
    
	/**
	 * Filter for SWF files
	 * @param {String} filename The file name
	 */
	FLASH_FILTER: function(filename)
	{
		return /\.swf$/i.test(filename);
	},
	/**
	 * @property {String} FLASH_FILTER_LABEL Label for flash filter
	 * @readonly
	 */
	FLASH_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_FLASHFILTER}}",

    /**
     * Filter for PDF files
     * @param {String} filename The file name
     */
    PDF_FILTER: function(filename)
    {
        return /\.pdf$/i.test(filename);
    },
    /**
     * @property {String} PDF_FILTER_LABEL Label for PDF filter
     * @readonly
     */
    PDF_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_PDFFILTER}}",
    
    /**
     * Filter function to filter files based on the allowed extensions
     * @param {String} filename The file name
     * @return {Boolean} true if the filter match
     */
    EXTENSION_FILTER: function (filename)
    {
        if (!this._allowedExtensions)
        {
            return true;
        }
        
        for (var i = 0; i < this._allowedExtensions.length; i++) 
        {
            if (new RegExp('\\.' + this._allowedExtensions[i] + '$', "i").test(filename))
            {
                return true;
            }
        }
        return false;
    },
    /**
     * @property {String} EXTENSION_FILTER_LABEL Label for PDF filter
     * @readonly
     */
    EXTENSION_FILTER_LABEL: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_EXTENSIONFILTER}}",
    
	/**
	 * Function called when the file input change
	 * @param {Ext.form.field.File} input The file input
	 * @param {String} name The file name
	 * @private
	 */
	_selectFile: function (input, name)
	{
		// _uploading boolean is used to prevent to execution of this code twice, because for some reasons, in IE, the
		// change event is raised again when the form is submitted (with an empty value)
		if (!this._uploading)
		{
			var matchFilter = this._filter == null || this._filter(name);
			this._box.getDockedItems('toolbar[dock="bottom"]')[0].items.get(0).setDisabled(!matchFilter);
			if (!matchFilter)
			{
                var text = "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERRORDIALOG_DESC}} " + Ametys.getObjectByName("Ametys.helper.FileUpload." + this._filter.$name + '_LABEL');
                if (this._allowedExtensions)
                {
                    text += this._getAllowedExtensionsAsText();
                }
				Ametys.log.ErrorDialog.display({
					title: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERRORDIALOG_TEXT}}", 
					text: text,
		    		details: "",
		    		category: "Ametys.helper.FileUpload._selectFile"
				});
			}
		}
	},
    
    /**
     * Get the allowed extension as a text to be displayed for user
     * @return {String} the allowed extension as a text 
     */
    _getAllowedExtensionsAsText: function ()
    {
        if (this._allowedExtensions.length > 2)
        {
            var slicedExtensions = Ext.Array.slice(this._allowedExtensions, 0, this._allowedExtensions.length -1);
            var txt = slicedExtensions.join("{{i18n PLUGINS_CORE_UI_FILEUPLOAD_EXTENSIONFILTER_SEPARATOR}}");
            txt += "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_EXTENSIONFILTER_SEPARATOR_FINAL}}";
            txt += this._allowedExtensions[this._allowedExtensions.length -1];
            return txt;
        }
        else
        {
            return this._allowedExtensions.join("{{i18n PLUGINS_CORE_UI_FILEUPLOAD_EXTENSIONFILTER_SEPARATOR_FINAL}}");
        }
    },
	
	/**
	 * This function submits the form to upload the selected file
	 * @private
	 */
	_submit: function()
	{
		this._uploading = true;
		
		this._box.down('form').getForm().submit({
			url : Ametys.getPluginDirectPrefix('core') + "/upload/store",
			
			waitTitle: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_SUBMITFORM_TITLE}}",
			waitMsg: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_SUBMITFORM_MSG}}",
			
			success: Ext.bind(this._submitSuccess, this),
		    failure: Ext.bind(this._submitFailure, this)
		});
	},
	
	/**
	 * The function to call when {@link #_submit} succeeded.
	 * @param {Ext.form.Basic} form The form that requested the submit action
	 * @param {Ext.form.action.Action} action The Action class.
	 */
	_submitSuccess: function (form, action)
	{
		this._uploading = false;
		
		if (this._cbFn)
		{
			Ext.Function.defer (this._cbFn, 0, null, [action.result.id, action.result.filename || action.result.name, action.result.size, Ametys.CONTEXT_PATH + action.result.viewHref, Ametys.CONTEXT_PATH + action.result.downloadHref]);
		}
		
		this._box.hide();
	},
	
	/**
	 * The function to call when {@link #_submit} failed.
	 * @param {Ext.form.Basic} form The form that requested the submit action
	 * @param {Ext.form.action.Action} action The Action class.
	 */
	_submitFailure: function (form, action)
	{
		this._uploading = true;
		
		if (action.result.error == "rejected")
		{
			Ametys.log.ErrorDialog.display({
				title: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERROR_MSG}}", 
				text: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERROR_FILEREJECTED}}",
	    		details: "",
	    		category: "Ametys.helper.FileUpload"
			});
		}
		else
		{
			Ametys.log.ErrorDialog.display({
				title: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERROR_MSG}}", 
				text: "{{i18n PLUGINS_CORE_UI_FILEUPLOAD_ERROR_ON_SERVER}}",
	    		details: action.result.error ? action.result.error.message + "\n" + action.result.error.stacktrace : "",
	    		category: "Ametys.helper.FileUpload"
			});
		}
	}
});


