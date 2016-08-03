/*
 *  Copyright 2015 Anyware Services
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
 * An abstract field displaying the various available profile images 
 */
Ext.define('Ametys.userprefs.UserProfileDialog.ProfileImageField', {
    extend: 'Ametys.form.AbstractField',
    
    /**
     * @property {Ext.view.View} _imagesView The view containing the image entries
     * @private
     */
    
    layout: 'fit',
    
    initComponent: function() 
    {
        this._imagesView = Ext.create('Ext.view.View', Ext.applyIf(this.imagesGridCfg || {}, {
            selModel: {
                mode: 'SINGLE'
            },
            
            overItemCls: 'x-view-over',
            ui: 'view',
            
            scrollable: true,
            
            store: this._getImageStore(),
            tpl: this._getViewTpl(),
            itemSelector: 'div.profile-image',
            border: true,
            style: {borderStyle: 'solid'},
            listeners: {
                beforeselect: {fn: this._onBeforeSelect, scope: this}
            }
        }));
        
        this.items = [this._imagesView];
        
        this.callParent(arguments);
    },
    
    /**
     * @private
     * Get the store used in the images view
     * @return {Ext.data.Store} the store
     */
    _getImageStore: function()
    {
        return Ext.create('Ext.data.Store', {
            model: 'Ametys.userprefs.UserProfileDialog.ProfileImageModel',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'user-profile/images/list.json',
                reader: {
                    type: 'json',
                    rootProperty: 'images'
                }
            },
            listeners: {
                load: {fn: this._onLoad, scope: this}
            }
        });
    },
    
    /**
     * Public function used to request a load of the store
     * @param {Object} loadArgs arguments to be passed to the load function of the image store
     */
    loadStore: function(loadArgs)
    {
        this._imagesView.getStore().load(loadArgs || {});
    },
    
    _getViewTpl: function()
    {
        return new Ext.XTemplate(
            '<tpl for=".">',
                '<div class="profile-image{[values.source == "upload-image" ? " unselectable" : ""]}">',
                    '<img src="{viewUrl}" data-qtip="{description}" style="width:64px; height:64px;">',
                '</div>',
            '</tpl>'
        );
    },
    
    getValue: function()
    {
        var selected = this._imagesView.getSelectionModel().getSelection()[0];
        if (selected)
        {
            var value = {
                source: selected.get('source')
            };
            
            var parameters = selected.get('parameters');
            if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters))
            {
                value.parameters = parameters;
            }
            
            return value;
        }
        else
        {
            return null;
        }
    },
    
    setValue: function(value)
    {
        // retrieves the value object
        value = value || null;
        if (value)
        {
            if (Ext.isString(value))
            {
                value = Ext.JSON.decode(value);
            }
            
            // value should be an object at this point
            if (!Ext.isObject(value))
            {
                value = null;
            }
        }
        
        var store = this._imagesView.getStore();
        if (!store.isLoaded())
        {
            this._valueToSet = value;
        }
        else
        {
            var index = this._findByValue(value);
            if (index >= 0)
            {
                this._imagesView.getSelectionModel().select(index);

                var record = store.getAt(index);
                var node = this._imagesView.getNode(record);
                node.click();
            }
        }
    },
    
    /**
     * Handler called when the store has loaded
     * @param {Ext.data.Store} store the field's store
     * @param {Ext.data.Model[]} records An array of records
     * @param {Boolean} successful True if the operation was successful.
     */
    _onLoad: function(store, records, successful)
    {
        if (successful)
        {
            // Set value if needed
            if (this._valueToSet)
            {
                var index = this._findByValue(this._valueToSet);
                if (index >= 0)
                {
                    this._imagesView.getSelectionModel().select(index);
                    
                    var record = store.getAt(index);
                    var node = this._imagesView.getNode(record);
                    node.click();
                }
                
                this._valueToSet = null;
            }
            
            // Insert the upload icon
            var store = this._imagesView.getStore();
            
            store.insert(0, {
                source: 'upload-image'
            });
        }
    },
    
    /**
     * Retrieves the index of a record corresponding to a value object
     * @return {Object} The value corresponding to a record (key are source and parameters)
     */
    _findByValue: function(value)
    {
        var store = this._imagesView.getStore();
        
        return store.findBy(function(record) {
            var source = record.get('source');
            
            if (source == 'userpref' && value.source == 'base64')
            {
                return true; // special case when value is stored in the userpref
            }
            
            if (source == value.source)
            {
                var recordParams = record.get('parameters') || {},
                    valueParams = value.parameters || {};
                
                return Ext.Object.equals(recordParams, valueParams);
            }
        });
    },
    
    /**
     * @param {Ext.selection.DataViewModel} model The data view model
     * @param {Ext.data.Model} record The selected record.
     * @param {Number} index The index within the store of the selected record.
     */
    _onBeforeSelect: function(model, record, index)
    {
        if (record.get('source') == 'upload-image')
        {
            Ametys.helper.FileUpload.open({
                iconCls: 'ametysicon-image2',
                title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_UPLOAD_TITLE}}",
                helpmessage: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_UPLOAD_HINT}}",
                callback: Ext.bind(this._fileUploadCb, this),
                filter: Ametys.helper.FileUpload.IMAGE_FILTER
            });
            
            return false;
        }
    },
    
    /**
     * Callback function to be executed after a file has been uploaded.
     * @param {String} id The file id.
     * @param {String} fileName The file name
     * @param {Number} fileSize The file size in bytes.
     * @return The HTML described file
     */
    _fileUploadCb: function(id, fileName, fileSize)
    {
        // Insert just after the upload icon.
        var index = 1;
        var inserted = this._imagesView.getStore().insert(1, {
            source: 'upload',
            parameters: {
                id: id
            }
        });
        
        this._imagesView.getSelectionModel().select(inserted[0]);
        
        var record = this._imagesView.getStore().getAt(1);
        var node = this._imagesView.getNode(record);
        node.click();
    }
});
