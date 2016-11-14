/*
 *  Copyright 2016 Anyware Services
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
 * This UI helper provides a dialog box for choosing one or more group directory(ies)
 * See {@link #open} method.
 * 
 *          Ametys.plugins.coreui.groupdirectories.ChooseGroupDirectoriesHelper.open({
 *              title: "The title of my dialog box",
 *              hintText: "Please select the group directories you want...",
 *              okAction:  Ext.bind(this._chooseCb, this)
 *          });
 * @private
 */
Ext.define('Ametys.plugins.coreui.groupdirectories.ChooseGroupDirectoriesHelper', {
    singleton: true,
    
    /**
     * Opens a dialog box helper for choosing one or more group directory(ies).
     * @param {Object} config The configuration object
     * @param {String} config.title The title of the dialog box
     * @param {String} config.hintText The text of the hint.
     * @param {Function} config.okAction The action to perform when clicking on the 'ok' button
     * @param {String[]} [config.selectedIds] The ids of the group directories which have to be selected at opening
     * @apram {Boolean} [config.allowCreation=false] Set tot true to allow creation
     */
    open: function(config)
    {
        var selectGroupDirectoriesWidget = Ext.create('Ametys.form.field.SelectGroupDirectories', {
            itemId: 'select-group-directories-widget',
            multiple: true,
            value: config.selectedIds || [],
            allowCreation: config.allowCreation,
            stacked: "true",
            flex: 1,
            scrollable: true,
            
            fieldLabel: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_HELPER_DIRECTORIES_LABEL}}",
            ametysDescription: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_HELPER_DIRECTORIES_DESCRIPTION}}",
            cls: 'ametys',
            style: "margin-top: 10px;",
            labelSeparator: '',
            labelAlign: 'right',
            labelWidth: 150,
            width: '100%',
            msgTarget: 'side'
        });
        
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: config.title || '',
            iconCls: 'ametysicon-multiple25',
            
            layout: {
                type: "vbox",
                align: "stretch"
            },
            width: 500,
            height: 210,
            scrollable: true,
            
            closeAction: 'destroy',
            items: [{
                xtype: "component",
                html: config.hintText || ''
            }, 
                selectGroupDirectoriesWidget
            ],
            
            defaultFocus: 'select-group-directories-widget',

            referenceHolder: true,
            defaultButton: 'buttonOk',
            
            buttons: [{
                itemId: 'button-ok',
                reference: 'buttonOk',
                text: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_HELPER_OK_BUTTON}}",
                handler: Ext.bind(this._validate, this, [config.okAction]),
                scope: this
            }, {
                itemId: 'button-cancel',
                text: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_HELPER_CANCEL_BUTTON}}",
                handler: function() {this._box.close();},
                scope: this
            }]
        });
        
        this._box.show();
    },
    
    /**
     * @private
     * Action to perform when the 'ok' button is clicked
     * @param {Function} callback The function to call
     */
    _validate: function(callback)
    {
        var ids = this._box.items.get('select-group-directories-widget').getValue();
        if (Ext.isFunction(callback))
        {
            callback(ids);
        }
        
        this._box.close();
    }
    
});