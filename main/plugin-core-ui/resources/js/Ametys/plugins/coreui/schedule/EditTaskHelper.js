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
 * This class is a singleton class to open a dialog box for adding or edting a task
 */
Ext.define('Ametys.plugins.coreui.schedule.EditTaskHelper', {
    singleton: true,
    
    /**
     * @private
     * @property {String} _mode The mode. Can only be 'add' or 'edit.
     */
    
    /**
     * @private
     * @property {String} _taskId If in edition mode, the id of the task being edited.
     */
    
    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The dialog box.
     */
    
    /**
     * @private
     * @property {String} _separator The separator for the {@link ConfigurableFormPanel}s of the dialog box
     */
    _separator: '/',
    
    /**
     * @private
     * @property {RegExp} _cronRegex The regular expression for cron validation
     */
    /* Found on https://regex101.com/ , see also http://stackoverflow.com/questions/2362985/verifying-a-cron-expression-is-valid-in-java */
    _cronRegex: /^\s*($|#|\w+\s*=|(\?|\*|(?:[0-5]?\d)(?:(?:-|\/|\,)(?:[0-5]?\d))?(?:,(?:[0-5]?\d)(?:(?:-|\/|\,)(?:[0-5]?\d))?)*)\s+(\?|\*|(?:[0-5]?\d)(?:(?:-|\/|\,)(?:[0-5]?\d))?(?:,(?:[0-5]?\d)(?:(?:-|\/|\,)(?:[0-5]?\d))?)*)\s+(\?|\*|(?:[01]?\d|2[0-3])(?:(?:-|\/|\,)(?:[01]?\d|2[0-3]))?(?:,(?:[01]?\d|2[0-3])(?:(?:-|\/|\,)(?:[01]?\d|2[0-3]))?)*)\s+(\?|\*|(?:0?[1-9]|[12]\d|3[01])(?:(?:-|\/|\,)(?:0?[1-9]|[12]\d|3[01]))?(?:,(?:0?[1-9]|[12]\d|3[01])(?:(?:-|\/|\,)(?:0?[1-9]|[12]\d|3[01]))?)*)\s+(\?|\*|(?:[1-9]|1[012])(?:(?:-|\/|\,)(?:[1-9]|1[012]))?(?:L|W)?(?:,(?:[1-9]|1[012])(?:(?:-|\/|\,)(?:[1-9]|1[012]))?(?:L|W)?)*|\?|\*|(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:,(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)*)\s+(\?|\*|(?:[0-6])(?:(?:-|\/|\,|#)(?:[0-6]))?(?:L)?(?:,(?:[0-6])(?:(?:-|\/|\,|#)(?:[0-6]))?(?:L)?)*|\?|\*|(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?(?:,(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?)*)(|\s)+(\?|\*|(?:|\d{4})(?:(?:-|\/|\,)(?:|\d{4}))?(?:,(?:|\d{4})(?:(?:-|\/|\,)(?:|\d{4}))?)*))$/,
    
    /**
     * Open the helper for creating/editing a task
     * @param {Object} [valuesToFill] If in edit mode, an object containing the data to fill the form
     * @param {String} mode The edition mode for the dialog box. Can only be 'add' (for creation) or 'edit' (for edition).
     * @param {String} [taskId] If in edition mode, the id of the task being edited.
     */
    open: function(valuesToFill, mode, taskId)
    {
        this._mode = mode || 'add';
        this._taskId = taskId;
        Ametys.plugins.core.schedule.Scheduler.getEditionConfiguration([], this._getFieldsCb, {scope: this, arguments: [valuesToFill]});
    },
    
    /**
     * @private
     * After retrieving from server the fields needed for the creation of a task, draw and show the box
     * @param {Object} response The server response
     * @param {Object[]} arguments The callback arguments
     */
    _getFieldsCb: function(response, arguments)
    {
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: this._mode == 'add' ? "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_EDIT_TITLE}}",
            iconCls: 'flaticon-gear39',
            
            width: 600,
            maxHeight: 600,
            layout: {
                type: "vbox",
                align: "stretch"
            },
            
            defaultFocus: 'label',
            items: this._getItems(response.schedulables),
            
            buttons:  [{
                itemId: 'button-validate',
                text: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_ACTIONS_VALIDATE}}",
                handler: Ext.bind(this.validate, this)
            }, {
                itemId: 'button-cancel',
                text: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_ACTIONS_CANCEL}}",
                handler: Ext.bind(this.cancel, this)
            }],
            
            validateAction: function()
            {
                this.down('#button-validate').btnEl.dom.click();
            }
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
     * @param {Object} schedulables An object containing information about task creation
     * @return {Ext.Component[]} The items of the dialog box
     */
    _getItems: function(schedulables)
    {
        var chooseSchedulableId = "schedulableId";
        
        var data = {
            label: {
                label: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_LABEL_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_LABEL_DESCRIPTION}}",
                validation: {
                    mandatory: true
                },
                type: "STRING"
            },
            description: {
                label: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_DESCRIPTION_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_DESCRIPTION_DESCRIPTION}}",
                type: "STRING"
            },
            runAtStartup: {
                label: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_RUN_AT_STARTUP_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_RUN_AT_STARTUP_DESCRIPTION}}",
                type: "BOOLEAN"
            },
            cron: {
                label: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_CRON_LABEL}}",
                description: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_CRON_DESCRIPTION}}",
                validation: {
                    mandatory: true,
                    regexp: this._cronRegex,
                    invalidText: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_CRON_INVALID}}"
                },
                type: "STRING",
                disableCondition: {
                    condition: [{
                        id: "runAtStartup",
                        operator: "eq",
                        value: true
                    }]
                }
            }
        };
        data[chooseSchedulableId] = {
            label: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_SCHEDULABLE_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_TASKS_DIALOG_SCHEDULABLE_DESCRIPTION}}",
            multiple: false,
            type: 'STRING',
            enumeration: [],
            validation: {
                mandatory: true
            },
            'can-not-write': this._mode == 'edit'
        };
        this._addParametersToData(data, schedulables, chooseSchedulableId);
        
        var formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
            itemId: 'form',
            defaultPathSeparator: this._separator,
            hideDisabledFields: true,
            scrollable: true,
            flex: 1
        });
        formPanel.configure(data);
        
        return [formPanel];
    },
    
    /**
     * @private
     * Create data for the configurable form panel
     * @param {Object} data The data object to modify for configuring the {@link ConfigurableFormPanel}
     * @param {Object[]} schedulables An array of object containing the information for the fields
     * @param {String} chooseSchedulableId The name of the field for choosing the task type (some fields are disabled depending on the value of this field)
     */
    _addParametersToData: function(data, schedulables, chooseSchedulableId)
    {
        Ext.Array.forEach(schedulables, function(schedulable) {
            // Add an entry into the combobox for selecting the schedulable
            data[chooseSchedulableId].enumeration.push({
                label: schedulable.label,
                value: schedulable.id
            });
            
            // Add the fields for each parameter
            Ext.Object.each(schedulable.parameters, function(parameterId, parameter) {
                // Add a disable condition
                parameter['disableCondition'] = this._generateDisableCondition(chooseSchedulableId, schedulable.id);
                
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
            Ametys.plugins.core.schedule.Scheduler.add([values.label, values.description, values.runAtStartup, values.cron, values.schedulableId, values.params], this._validateCb, {scope: this});
        }
        else
        {
            Ametys.plugins.core.schedule.Scheduler.edit([this._taskId, values.label, values.description, values.runAtStartup, values.cron, values.params], this._validateCb, {scope: this});
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
        result['description'] = values.description;
        result['runAtStartup'] = values.runAtStartup == "true";
        result['cron'] = values.cron;
        result['schedulableId'] = values.schedulableId;
        
        // The parameters are the remaining entries
        delete values['label'];
        delete values['description'];
        delete values['runAtStartup'];
        delete values['cron'];
        delete values['schedulableId'];
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
        var schedulableId = valuesToFill['schedulableId'];
        values['label'] = valuesToFill['label'];
        values['description'] = valuesToFill['description'];
        values['runAtStartup'] = valuesToFill['runAtStartup'];
        values['cron'] = valuesToFill['cron'];
        values['schedulableId'] = schedulableId;
        Ext.Object.each(valuesToFill['params'], function(paramName, paramValue) {
            values[paramName] = paramValue;
        }, this);
        
        this._getFormPanel().setValues({values: values});
    }
});
