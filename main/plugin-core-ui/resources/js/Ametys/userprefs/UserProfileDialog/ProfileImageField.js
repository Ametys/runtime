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
 * An abstract field displaying the various profile images availables
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
            
            store: this._getImageStoreCfg(),
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
    
    _getImageStoreCfg: function()
    {
        return Ext.create('Ext.data.Store', {
            autoLoad: true,
            model: 'Ametys.userprefs.UserProfileDialog.ProfileImageModel',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'images/user-profile/list.json',
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
    
    _getViewTpl: function()
    {
        return new Ext.XTemplate(
            '<tpl for=".">',
                '<div class="profile-image{[values.source == "upload-image" ? " unselectable" : ""]}">',
                    '<img src="{viewUrl}" data-qtip="{description}">',
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
    
    /**
     * Handler called when the store has loaded
     * @param {Ext.data.Store} this
     * @param {Ext.data.Model[]} records An array of records
     * @param {Boolean} successful True if the operation was successful.
     */
    _onLoad: function(store, records, successful)
    {
        if (successful)
        {
            // Select the userpref record
            var userPrefRecord = this._getUserPrefRecord();
            if (userPrefRecord)
            {
                this._imagesView.getSelectionModel().select([userPrefRecord]);
            }
            
            // Insert the upload icon
            var store = this._imagesView.getStore();
            
            store.insert(0, {
                source: 'upload-image'
            });
        }
    },
    
    /**
     * Retrieves the userpref record.
     * @return {Ametys.userprefs.UserProfileDialog.ProfileImageModel} The userpref record or null
     */
    _getUserPrefRecord: function()
    {
        var store = this._imagesView.getStore(),
            userPrefRecord;
        
        store.findBy(function(record) {
            if (record.get('source') == 'userpref')
            {
                userPrefRecord = record;
                return true;
            }
        });
        
        return userPrefRecord;
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
            Ametys.helper.FileUpload.open(
                Ametys.getPluginResourcesPrefix('core-ui') + '/img/user-profiles/upload-image-icon.png',
                "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_UPLOAD_TITLE'/>",
                "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_UPLOAD_HINT'/>",
                Ext.bind(this._fileUploadCb, this)
            );
            
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
    }
});
