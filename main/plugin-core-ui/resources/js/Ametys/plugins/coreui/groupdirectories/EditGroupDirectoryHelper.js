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
 * This class is a singleton class to open a dialog box for adding or edting a group directory
 */
Ext.define('Ametys.plugins.coreui.groupdirectories.EditGroupDirectoryHelper', {
    singleton: true,
    
    /**
     * @private
     * @property {String} _mode The mode. Can only be 'add' or 'edit.
     */
    
    /**
     * @private
     * @property {String} _groupDirectoryId If in edition mode, the id of the group directory being edited.
     */
    
    /**
     * @private
     * @property {Function} _callback The callback function called when the group directory is created/modified.
     */
    
    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The dialog box.
     */
    
    /**
     * @private
     * @property {String[]} _knownIds The array containing the ids of the existing group directories. Thus, a new directory cannot have an id contained in this array.
     */
    
    /**
     * @private
     * @property {String} _separator The separator for the {@link Ametys.form.ConfigurableFormPanel}s of the dialog box
     */
    _separator: '/',
    
    /**
     * @private
     * @property {RegExp} _idRegex The regular expression the id of a group directory must match
     */
    _idRegex: /^[a-z][a-z0-9_-]*$/,
    
    /**
     * Open the helper for creating/editing a group directory
     * @param {Object} [valuesToFill] If in edit mode, an object containing the data to fill the form
     * @param {String} [mode] The edition mode for the dialog box. Can only be 'add' (for creation) or 'edit' (for edition).
     * @param {String} [groupDirectoryId] If in edition mode, the id of the group directory being edited.
     * @param {Function} [callback] A callback function called when the group directory is created/modified.
     */
    open: function(valuesToFill, mode, groupDirectoryId, callback)
    {
        this._mode = mode || 'add';
        this._groupDirectoryId = groupDirectoryId;
        this._callback = callback;
        if (mode == 'add')
        {
            // We need to know the existing ids for checking the id given by the user is not used
            this._getKnownIds();
        }
        else
        {
            // We need to know fields needed for the creation of a directory, draw and show the box
            this._getFields(null, [valuesToFill]);
        }
    },
    
    /**
     * @private
     * Asks the server the ids of the existing group directories
     */
    _getKnownIds: function()
    {
        Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.getGroupDirectoriesIds([], this._getFields, {scope: this});
    },
    
    /**
     * @private
     * Asks the server the data needed for creating/editing a group directory.
     * @param {String[]} response The server response. It is an array of ids of the directories of the application. Can be null in 'edit' mode.
     * @param {Object[]} arguments The callback arguments
     */
    _getFields: function(response, arguments)
    {
        if (this._mode == 'add')
        {
            this._knownIds = response;
            Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.getEditionConfiguration([], this._getFieldsCb, {scope: this});
        }
        else
        {
            var valuesToFill = arguments[0];
            Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.getEditionConfiguration([], this._getFieldsCb, {scope: this, arguments: [valuesToFill]});
        }
    },
    
    /**
     * @private
     * After retrieving from server the fields needed for the creation of a group directory, draw and show the box
     * @param {Object} response The server response
     * @param {Object[]} arguments The callback arguments
     */
    _getFieldsCb: function(response, arguments)
    {
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: this._mode == 'add' ? "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_EDIT_TITLE}}",
            iconCls: 'ametysicon-multiple25 decorator-ametysicon-agenda3',
            
            width: 650,
            height: 570,
            layout: {
                type: "vbox",
                align: "stretch"
            },
            
            defaultFocus: 'label',
            items: this._getItems(response.groupDirectoryModels),
            
            referenceHolder: true,
            defaultButton: 'validate',
            
            buttons:  [{
            	reference: 'validate',
            	itemId: 'button-validate',
                text: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ACTIONS_VALIDATE}}",
                handler: Ext.bind(this.validate, this)
            }, {
                itemId: 'button-cancel',
                text: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ACTIONS_CANCEL}}",
                handler: Ext.bind(this.cancel, this)
            }]
        });
        
        this._box.show();
        
        if (this._mode == 'edit')
        {
            var valuesToFill = arguments[0];
            this._fillFields(valuesToFill);
        }
    },
    
    /**
     * @private
     * Gets the items of the box
     * @param {Object} groupDirectoryModels An object containing information about group directory creation
     * @return {Ext.Component[]} The items of the dialog box
     */
    _getItems: function(groupDirectoryModels)
    {
        var chooseModelFieldId = "modelId";
        
        var data = {
            label: {
                label: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_LABEL_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_LABEL_DESC}}",
                type: 'STRING',
                validation: {
                    mandatory: true
                },
                'widget-params': {
                    listeners: {
                        'change': Ext.bind(this._updateIdField, this)
                    }
                }
            },
            id: {
                label: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ID_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ID_DESC}}",
                type: 'STRING',
                validation: {
                    mandatory: true,
                    regexp: this._idRegex,
                    regexText: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ID_INVALID}}"
                },
                'widget-params': {
                    disabled: this._mode == 'edit',
                    validator: Ext.bind(this._validateIdField, this)
                }
            }
        };
        data[chooseModelFieldId] = {
            label: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_MODEL_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_MODEL_DESCRIPTION}}",
            multiple: false,
            type: 'STRING',
            enumeration: [],
            validation: {
                mandatory: true
            }
        };
        this._createData(data, groupDirectoryModels, chooseModelFieldId);
        
        var formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
            itemId: 'form',
            defaultPathSeparator: this._separator,
            hideDisabledFields: true,
            scrollable: true,
            flex: 1
        });
        formPanel.configure(data);
        
        return [
            {
	            xtype: "component",
	            html: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_HINT}}"
	        },
	        formPanel
        ];
    },
    
    /**
     * @private
     * Action called when the value of the label field changed in order to modify the id field in 'add' mode
     * @param {Ext.form.field.Text} labelField The label field which changed
     */
    _updateIdField: function(labelField)
    {
        if (this._mode == 'add')
        {
            var idField = this._box.getComponent('form').getField('id');
            var idValue = this._labelToId(labelField.getValue());
            idField.setValue(idValue);
        }
    },
    
    /**
     * @private
     * Transforms a label into a valid id
     * @param {String} value The label value to transform
     * @return {String} The computed id value
     */
    _labelToId: function(value)
    {
        // toLowerCase -> trim -> deemphasize -> all non valid characters become '-' 
        // -> multiple '-' replaced by one -> do not start with '-' 
        value = Ext.String.deemphasize(value.toLowerCase().trim()).replace(/\W/g, "-").replace(/-+/g, "-").replace(/^-/g, "");
        
        var i = 2;
        var suffixedValue = value;
        while (Ext.Array.contains(this._knownIds, suffixedValue))
        {
            suffixedValue = value + i;
            i++;
        }
        
        return suffixedValue;
    },
    
    /**
     * @private
     * Validator function for the directory id field.
     * @param {String} val
     * @return {Boolean/String} true if the value is valid, an error message otherwise.
     */
    _validateIdField: function(val)
    {
        if (this._mode == 'edit' || !Ext.Array.contains(this._knownIds, val))
        {
            return true;
        }
        else
        {
            return "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_DIALOG_ID_EXISTING}}";
        }
    },
    
    /**
     * @private
     * Create data for the configurable form panel
     * @param {Object} data The data object to modify for configuring the {@link Ametys.form.ConfigurableFormPanel}
     * @param {Object[]} models An array of object containing the information for the fields
     * @param {String} chooseModelFieldId The name of the field for choosing the type (some fields are disabled depending on the value of this field)
     */
    _createData: function(data, models, chooseModelFieldId)
    {
        Ext.Array.forEach(models, function(model) {
            // Add an entry into the combobox for selecting the model
            data[chooseModelFieldId].enumeration.push({
                label: model.label,
                value: model.id
            });
            
            // Add the fields for each parameter
            Ext.Object.each(model.parameters, function(parameterId, parameter) {
                // Add a disable condition
                parameter['disableCondition'] = this._generateDisableCondition(chooseModelFieldId, model.id);
                
                // The field is ready
                data[parameterId] = parameter;
            }, this);
            
        }, this);
    },
    
    /**
     * @private
     * Create the object for a 'neq' disable condition for a field
     * @param {String} fieldId The id of the reference field
     * @param {String} value The value for the reference field to check for the condition
     * @return {Object} The object for a disable condition
     */
    _generateDisableCondition: function(fieldId, value)
    {
        return {
            condition: [{
                id: fieldId,
                operator: "neq",
                value: value
            }]
        };
    },
    
    /**
     * Handler when clicking on 'cancel' button
     */
    cancel: function()
    {
        this._box.hide();
    },

    /**
     * Handler for 'Ok' action
     */
    validate: function()
    {
        var isValidForm = this._getFormPanel().isValid();
        if (!isValidForm)
        {
            return;
        }
        
        var values = this._getFormValues();
        if (this._mode == 'add')
        {
            Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.add([values.id, values.label, values.modelId, values.params], this._validateCb, {scope: this});
        }
        else
        {
            Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.edit([this._groupDirectoryId, values.label, values.modelId, values.params], this._validateCb, {scope: this});
        }
    },
    
    /**
     * @private
     * Gets the configurable form panel of the dialog box.
     * @return {Ametys.form.ConfigurableFormPanel} The configurable form panel
     */
    _getFormPanel: function()
    {
        return this._box.items.getByKey('form');
    },
    
    /**
     * @private
     * Gets the values of the form
     * @return {Object} The form values
     */
    _getFormValues: function()
    {
        var result = {};
        
        var values = this._getFormPanel().getValues();
        
        // Extract label, id and model id
        result['label'] = values.label;
        result['id'] = values.id;
        result['modelId'] = values.modelId;
        
        // The parameters are the remaining entries
        delete values['label'];
        delete values['id'];
        delete values['modelId'];
        result['params'] = values;
        
        return result;
    },
    
    /**
     * @private
     * Callback function called after the 'Ok' action is processed. Close the dialog box if no error.
     * @param {Object} response The server response
     */
    _validateCb: function(response)
    {
        if (!response.error)
        {
            this._box.close();
            if (Ext.isFunction(this._callback))
            {
                this._callback(response);
            }
        }
    },
    
    /**
     * @private
     * Fills the forms of the dialog box with values.
     * @param {Object} valuesToFill The data to fill
     */
    _fillFields: function(valuesToFill)
    {
        var values = {};
        var modelId = valuesToFill['modelId'];
        values['label'] = valuesToFill['label'];
        values['id'] = valuesToFill['id'];
        values['modelId'] = modelId;
        Ext.Object.each(valuesToFill['params'], function(paramName, paramValue) {
            values[paramName] = paramValue;
        }, this);
        
        this._getFormPanel().setValues({values: values});
    }
});
