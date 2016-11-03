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
 * This class is a singleton class to open a dialog box for adding or edting a user population
 */
Ext.define('Ametys.plugins.coreui.populations.EditPopulationHelper', {
    singleton: true,
    
    /**
     * @private
     * @property {String} _mode The mode. Can only be 'add' or 'edit.
     */
    
    /**
     * @private
     * @property {String} _populationId If in edition mode, the id of the population being edited.
     */
    
    /**
     * @private
     * @property {Function} _callback The callback function called when the population is created/modified.
     */
    
    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The dialog box.
     */
    
    /**
     * @private
     * @property {Ext.container.Container[]} _cards The array containing the cards of the dialog box.
     */
    
    /**
     * @private
     * @property {String[]} _knownIds The array containing the ids of the existing populations. Thus, a new population cannot have an id contained in this array.
     */
    
    /**
     * @private
     * @property {String} _separator The separator for the {@link ConfigurableFormPanel}s of the dialog box
     */
    _separator: '/',
    
    /**
     * @private
     * @property {RegExp} _idRegex The regular expression the id of a population must match
     */
    _idRegex: /^[a-z][a-z0-9_-]*$/,
    
    /**
     * Open the helper for creating/editing a user population
     * @param {Object} [valuesToFill] If in edit mode, an object containing the data to fill the form
     * @param {String} [mode='add'] The edition mode for the dialog box. Can only be 'add' (for creation) or 'edit' (for edition).
     * @param {String} [populationId] If in edition mode, the id of the population being edited.
     * @param {Function} [callback] A callback function called when the population is created/modified.
     */
    open: function(valuesToFill, mode, populationId, callback)
    {
        this._mode = mode || 'add';
        this._populationId = populationId;
        this._callback = callback;
        if (mode == 'add')
        {
            // We need to know the existing ids for checking the id given by the user is not used
            this._getKnownIds();
        }
        else
        {
            // We need to know fields needed for the creation of a population, draw and show the box
            this._getFields(null, [valuesToFill]);
        }
    },
    
    /**
     * Handler when clicking on 'next' button.
     * Determines if we must do 'next' action or 'validate' action
     */
    nextOrValidateAction: function()
    {
        var isValidForm = this._getFormPanel(this.getCurrentCard()).isValid();
        if (!isValidForm)
        {
            return;
        }
        
        if ( this.isLastCard(this.getCurrentCardIndex()) )
        {
            this.validate();
        }
        else
        {
            this.next();
        }
    },
    
    /**
     * Handler when clicking on 'previous' button
     */
    previous: function()
    {
        var currentCard = this.getCurrentCard();

        this.goToPreviousCard();
    },

    /**
     * Handler for 'next' action
     */
    next: function()
    {
        var currentCard = this.getCurrentCard();
        
        this.goToNextCard();
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
        var values = this._getFormValues();
        if (this._mode == 'add')
        {
	        Ametys.plugins.core.populations.UserPopulationDAO.add([values.id, values.label, values.userDirectories, values.credentialProviders], this._validateCb, {scope: this});
        }
        else
        {
	        Ametys.plugins.core.populations.UserPopulationDAO.edit([this._populationId, values.label, values.userDirectories, values.credentialProviders], this._validateCb, {scope: this});
        }
    },
    
    /**
     * Get card to the specified index
     * @param {Number} index The card index
     * @return {Ext.container.Container} the card if found.
     */
    getCard: function(index)
    {
        return this._cards[index];
    },
    
    /**
     * Get the number of cards
     * @return {Number} the number of cards
     */
    getCardsCount: function()
    {
        return this._cards.length;
    },
    
    /**
     * Get the index of the current active card
     * @return {Number} the index
     */
    getCurrentCardIndex: function()
    {
        return this._box.items.indexOf(this._box.getLayout().activeItem);
    },
    
    /**
     * Return true if the card is the last one
     * @param {Number} cardIndex The index
     * @return {Boolean} True if the card is the last one
     */
    isLastCard: function(cardIndex)
    {
        var lastIndex = this.getCardsCount() - 1;
        return (lastIndex == cardIndex);
    },
    
    /**
     * Get the current active card
     * @return {Ext.container.Container} the active card
     */
    getCurrentCard: function()
    {
        var currentCardIndex = this.getCurrentCardIndex();
        return this.getCard(currentCardIndex);
    },

    /**
     * Go to the previous card
     */
    goToPreviousCard: function()
    {
        var previousCardIndex = this._getPreviousCardIndex();

        this._showCard(previousCardIndex);
    },
    
    /**
     * Go to the next card
     */
    goToNextCard: function()
    {
        var nextCardIndex = this._getNextCardIndex();

        this._showCard(nextCardIndex);
    },
    
    /**
     * @private
     * Show card to the specified index
     * @param {Number} index The index of card to activate
     */
    _showCard: function(index)
    {
        this._box.getLayout().setActiveItem(index); 
        this._updateButtons();
    },
    
    /**
     * @private
     * Update dialog box buttons
     */
    _updateButtons: function()
    {
        // PREVIOUS BUTTON
        var hasAPreviousCard = this._getPreviousCardIndex() != -1;
        
        // APPLY
        var nextButton = this._box.down("button[itemId='button-next']");
        var previousButton = this._box.down("button[itemId='button-previous']");
        
        previousButton.setDisabled(!hasAPreviousCard);
                
        // Maybe update the text of the button
        if ( this.isLastCard(this.getCurrentCardIndex()) )
        {
            nextButton.setText("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ACTIONS_VALIDATE}}");
        }
        else
        {
            nextButton.setText("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ACTIONS_NEXT}}");
        }
    },
    
    /**
     * @private
     * Get the index of previous card
     * @return the index of previous card or -1 if there is no previous card
     */
    _getPreviousCardIndex: function()
    {
        var currentCardIndex = this._box.items.indexOf(this._box.getLayout().activeItem);
        return currentCardIndex - 1;
    },
    
    /**
     * @private
     * Get the index of next card
     * @return the index of next card or -1 if there is no previous card
     */
    _getNextCardIndex: function()
    {
        var currentCardIndex = this._box.items.indexOf(this._box.getLayout().activeItem);
        if (this.isLastCard(currentCardIndex))
        {
            return -1
        }
        else
        {
	        return currentCardIndex + 1;
        }
    },
    
    /**
     * @private
     * Asks the server the ids of the existing populations
     */
    _getKnownIds: function()
    {
        Ametys.plugins.core.populations.UserPopulationDAO.getUserPopulationsIds([], this._getFields, {scope: this});
    },
    
    /**
     * @private
     * Asks the server the data needed for creating/editing a user population.
     * @param {String[]} response The server response. It is an array of ids of the populations of the application. Can be null in 'edit' mode.
     * @param {Object[]} arguments The callback arguments
     */
    _getFields: function(response, arguments)
    {
        if (this._mode == 'add')
        {
            this._knownIds = response;
            Ametys.plugins.core.populations.UserPopulationDAO.getEditionConfiguration([], this._getFieldsCb, {scope: this});
        }
        else
        {
	        var valuesToFill = arguments[0];
            Ametys.plugins.core.populations.UserPopulationDAO.getEditionConfiguration([], this._getFieldsCb, {scope: this, arguments: [valuesToFill]});
        }
    },
    
    /**
     * @private
     * After retrieving from server the fields needed for the creation of a population, draw and show the box
     * @param {Object} response The server response
     * @param {Object[]} arguments The callback arguments
     */
    _getFieldsCb: function(response, arguments)
    {
        this._cards = [this._createFirstCard(), this._createSecondCard(response['userDirectoryModels']), this._createThirdCard(response['credentialProviderModels'])];
            
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: this._mode == 'add' ? "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_EDIT_TITLE}}",
            iconCls: 'ametysicon-multiple25',
            
            width: 650,
            height: 610,
            layout: {
                type: 'card'
            },
            
            defaultFocus: 'label',
            items: this._cards,
        	
            referenceHolder: true,
            defaultButton: 'next',
            
            buttons: [{
                itemId: 'button-previous',
                text: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ACTIONS_PREVIOUS}}",
                disabled: true,
                handler: Ext.bind(this.previous, this)
            }, {
            	reference: 'next',
                itemId: 'button-next',
                text: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ACTIONS_NEXT}}",
                handler: Ext.bind(this.nextOrValidateAction, this)
            }, {
                itemId: 'button-cancel',
                text: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ACTIONS_CANCEL}}",
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
     * Create the first card (for the label of the population)
     * @return {Ext.form.Panel} The panel of the first card
     */
    _createFirstCard: function()
    {
        return Ext.create('Ext.form.Panel', {
            scrollable: true,
            defaults: {
                cls: 'ametys',
                labelSeparator: '',
                labelAlign: 'top',
                labelStyle: 'font-weight: bold',
                labelWidth: 120,
                width: '100%',
                msgTarget: 'side'
            },
            
            items: [{
                xtype: "component",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_HINT}}"
            }, {
                xtype: "component",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_HINT2}}"
            }, {
                xtype: "component",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_HINT3}}"
            }, {
                xtype: "textfield",
                name: "label",
                itemId: "label",
                fieldLabel: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_LABEL_LABEL}}" + " *",
                ametysDescription: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_LABEL_DESC}}",
                allowBlank: false,
                listeners: {
                    'change': Ext.bind(this._updateIdField, this)
                }
            }, {
                xtype: "button",
                text: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_ADVANCED_BUTTON_COLLAPSED}}",
                width: "20%",
                style: {
                    marginTop: '12px'
                },
                handler: Ext.bind(function(button, event) {
                    if (this.getCard(0).getComponent('id').isHidden())
                    {
                        this._getFormPanel(this.getCard(0)).getComponent('idHint').setVisible(true);
                        this._getFormPanel(this.getCard(0)).getComponent('id').setVisible(true);
                        button.setText("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_ADVANCED_BUTTON_EXPANDED}}");
                    }
                    else
                    {
                        this._getFormPanel(this.getCard(0)).getComponent('idHint').setHidden(true);
                        this._getFormPanel(this.getCard(0)).getComponent('id').setHidden(true);
                        button.setText("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_ADVANCED_BUTTON_COLLAPSED}}");
                    }
                }, this)
            }, 
            {
                xtype: "component",
                hidden: true,
                itemId: "idHint",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_HINT4}}",
                style: {
                    marginTop: '12px'
                }
            }, {
                xtype: "textfield",
                hidden: true,
                name: "id",
                itemId: "id",
                fieldLabel: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_ID_LABEL}}" + " *",
                ametysDescription: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_ID_DESC}}",
                allowBlank: false,
                regex: this._idRegex,
                regexText: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_ID_INVALID}}",
                disabled: this._mode == 'edit',
                validator: Ext.bind(this._validateIdField, this)
            }
            ]
        });
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
	        var idField = this._getFormPanel(this.getCard(0)).getComponent('id');
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
     * Validator function for the population id field.
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
            return "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_FIRST_CARD_POPULATION_ID_EXISTING}}";
        }
    },
    
    /**
     * @private
     * Create the second card (for the user directories of the population)
     * @param {Object} userDirectoryModels An object containing information about user directory creation
     * @return {Ext.container.Container} The container of the second card
     */
    _createSecondCard: function(userDirectoryModels)
    {
        var fieldName = "userDirectories";
        var chooseModelFieldId = "udModelId";
        var labelFieldId = "label";

        var composition = {};
        var data = {};
        data[fieldName] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_ENTRY_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_ENTRY_DESCRIPTION}}",
            multiple: false,
            repeater: {
                'header-label': '{' + chooseModelFieldId + '} {' + labelFieldId + '}',
                'add-label': "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ADD_USER_DIRECTORY_LABEL}}",
                'del-label': "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_DELETE_USER_DIRECTORY_LABEL}}",
                'min-size': 1,
                composition: composition
            },
            type: 'COMPOSITE'
        };
        composition[chooseModelFieldId] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_TYPE_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_TYPE_DESCRIPTION}}",
	        multiple: false,
	        type: 'STRING',
	        enumeration: [],
	        validation: {
	            mandatory: true
	        }
        };
        composition['id'] = {
            label: "id",
            multiple: false,
            type: 'STRING',
            hidden: true,
            validation: {
                mandatory: false
            }
        };
        composition[labelFieldId] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_LABEL_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_USER_DIRECTORY_LABEL_DESCRIPTION}}",
            multiple: false,
            type: 'STRING',
            validation: {
                mandatory: false
            }
        };
        this._createRepeaterData(data, fieldName, userDirectoryModels, chooseModelFieldId);
        
        var formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
            defaultPathSeparator: this._separator,
            hideDisabledFields: true,
            scrollable: true,
            flex: 1,
            testURL: Ametys.getPluginDirectPrefix('core') + '/userdirectory/test',
            testHandler: this._testHandler
        });
        formPanel.configure(data);
        formPanel.setValues({});
        
        return Ext.create('Ext.container.Container', {
            layout: {
                type: "vbox",
                align: "stretch"
            },
            items: [{
                xtype: "component",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_SECOND_CARD_HINT}}"
            },
            formPanel]
        });
    },
    
    /**
     * @private
     * Create the third card (for the credential providers of the population)
     * @param {Object} credentialProviderModels An object containing information about credential provider creation
     * @return {Ext.container.Container} The container of the third card
     */
    _createThirdCard: function(credentialProviderModels)
    {
        var me = this;
        var fieldName = "credentialProviders";
        var chooseModelFieldId = "cpModelId";
        var labelFieldId = "label";
        
        var composition = {};
        var data = {};
        data[fieldName] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_ENTRY_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_ENTRY_DESCRIPTION}}",
            multiple: false,
            repeater: {
                'header-label': '{' + chooseModelFieldId + '} {' + labelFieldId + '}',
                'add-label': "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_ADD_CREDENTIAL_PROVIDER_LABEL}}",
                'del-label': "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_DELETE_CREDENTIAL_PROVIDER_LABEL}}",
                'min-size': 1,
                composition: composition
            },
            type: 'COMPOSITE'
        };
        composition[chooseModelFieldId] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_TYPE_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_TYPE_DESCRIPTION}}",
            multiple: false,
            type: 'STRING',
            enumeration: [],
            validation: {
                mandatory: true
            }
        };
        composition['id'] = {
            label: "id",
            multiple: false,
            type: 'STRING',
            hidden: true,
            validation: {
                mandatory: false
            }
        };        
        composition[labelFieldId] = {
            label: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_LABEL_LABEL}}",
            description: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_CREDENTIAL_PROVIDER_LABEL_DESCRIPTION}}",
            multiple: false,
            type: 'STRING',
            validation: {
                mandatory: false
            }
        };
        this._createRepeaterData(data, fieldName, credentialProviderModels, chooseModelFieldId);
        
        var formPanel = Ext.create('Ametys.form.ConfigurableFormPanel', {
            defaultPathSeparator: this._separator,
            hideDisabledFields: true,
            scrollable: true,
            flex: 1,
            testURL: Ametys.getPluginDirectPrefix('core') + '/credentialprovider/test',
            testHandler: this._testHandler
        });
        formPanel.configure(data);
        formPanel.setValues({});
        
        return Ext.create('Ext.container.Container', {
            layout: {
                type: "vbox",
                align: "stretch"
            },
            items: [{
                xtype: "component",
                html: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DIALOG_THIRD_CARD_HINT}}"
            },
            formPanel]
        });
    },
    
    /**
     * @private
     * The test handler for the cp and ud forms
     * @param {String[]} testParamsNames The array of param names
     * @param {Object[]} rawTestValues The array of param values
     * @return {Object} The modified fieldCheckersInfo
     */
    _testHandler: function(fieldCheckers, fieldCheckersInfo)
    {
        // WARNING execution scope is the ConfigurableFormPanel
        
        // The population id
        fieldCheckersInfo._user_population_id = me._cards[0].form.findField("id").getValue();
        
        // For each running test
        for (var i = 0; i < fieldCheckers.length; i++)
        {
            // Let's add the identifier of the credential provider tested
            fieldCheckersInfo[fieldCheckers[i].id].testParamsNames.push(fieldCheckers[0].fieldCheckerPrefix + "id");
            fieldCheckersInfo[fieldCheckers[i].id].rawTestValues.push(this.getField(fieldCheckers[0].fieldCheckerPrefix + "id").getValue());
        }
        return fieldCheckersInfo;
    },
    
    /**
     * @private
     * Create data for a repeater
     * @param {Object} data The object to modify for adding repeater data for configuring the {@link ConfigurableFormPanel}
     * @param {String} fieldName The name of the field where to insert the repeater in object data
     * @param {Object[]} models An array of object containing the information for the fields of the composite repeater
     * @param {String} chooseModelFieldId The name of the field for choosing the model (some fields are disabled depending on the value of this field)
     */
    _createRepeaterData: function(data, fieldName, models, chooseModelFieldId)
    {
        data[fieldName].repeater['field-checker'] = [];
        Ext.Array.forEach(models, function(model) {
            // Add an entry into the combobox for selecting the model
            data[fieldName].repeater.composition[chooseModelFieldId].enumeration.push({
                label: model.label,
                value: model.id
            });
            
            // Add the fields for each parameter
            Ext.Object.each(model.parameters, function(parameterId, parameter) {
                // Add a disable condition
                parameter['disableCondition'] = this._generateDisableCondition(chooseModelFieldId, model.id);
                
                // The field is ready
                data[fieldName].repeater.composition[parameterId] = parameter;
            }, this);
            
            // Add the parameter checkers
            Ext.Object.each(model.parameterCheckers, function(parameterCheckerId, parameterChecker) {
                data[fieldName].repeater['field-checker'].push(parameterChecker);
            }, this);
            
        }, this);
    },
    
    /**
     * @private
     * Create the object for a 'neq' disable condition for a repeater field
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
     * @private
     * From a container, child of the box, returns its form panel (can be itself)
     * @param {Ext.container.Container} card The card
     * @return {Ext.form.Panel} The form panel, or null
     */
    _getFormPanel: function(card)
    {
        switch (Ext.getClassName(card)) {
            case "Ext.form.Panel":
            case "Ametys.form.ConfigurableFormPanel":
                return card;
                break;
            case "Ext.container.Container":
                var foundChild;
                card.items.each(function(child) {
                    var childFormPanel = this._getFormPanel(child);
                    if (childFormPanel != null)
                    {
                        foundChild = childFormPanel;
                        return false
                    }
                }, this);
                return foundChild;
            default:
                return null;
        }
    },
    
    /**
     * @private
     * Gets the whole values of the form (from all the cards of the dialog box)
     * @return {Object} The form values
     */
    _getFormValues: function()
    {
        var values = {};
        // First card: label of the population
        Ext.apply(values, this._getFormPanel(this._cards[0]).getValues());
        // Second card: userDirectories
        values['userDirectories'] = this._getRepeaterValues(this._getFormPanel(this._cards[1]).getValues(), 'userDirectories');
        // Third card: credentialProviders
        values['credentialProviders'] = this._getRepeaterValues(this._getFormPanel(this._cards[2]).getValues(), 'credentialProviders');
        
        return values;
    },
    
    /**
     * @private
     * Gets the values from a repeater
     * @param {Object} values The object containing all the raw values of all the repeater entries
     * @param {String} fieldName The name of the field
     * @return {Object[]} An array containing the values of each entry of the repeater
     */
    _getRepeaterValues: function(values, fieldName)
    {
        var result = [];
        var count = values['_' + fieldName + '.size'];
        for (var index = 1; index <= count; index++) { //the repeater begins its counter at 1
            // Retrieve parameters about the entry number 'index'
            var parameters = this._filterParametersByPrefix(values, fieldName + this._separator + index + this._separator);
            result.push(parameters);
        }
        
        return result;
    },
    
    /**
     * @private
     * Returns an object containing only the items of parameters with a key which has the prefix.
     * @param {Object} parameters The object to filter
     * @param {String} prefix The prefix for filtering
     * @return {Object} The filtered object, with indexes without the prefix
     */
    _filterParametersByPrefix: function(parameters, prefix)
    {
        var keys = Ext.Object.getKeys(parameters);
        
        // Filter the keys which match the prefix
        var resultKeys = Ext.Array.filter(keys, function(key) {
            return Ext.String.startsWith(key, prefix);
        }, this);
        // Create simplier keys, without the prefix
        Ext.Array.forEach(resultKeys, function(key, index, allKeys) {
            allKeys[index] = key.split(prefix)[1];
        }, this);
        // Create the result map
        var result = {};
        Ext.Array.forEach(resultKeys, function(key) {
            result[key] = parameters[prefix + key];
        }, this);
        
        return result;
    },
    
    /**
     * @private
     * Callback function called after the 'Ok' action is processed. Close the dialog box if no error.
     * @param {Object} response The server response
     */
    _validateCb: function(response)
    {
        if (response && !response.error)
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
        // First card, just the label
        this._getFormPanel(this._cards[0]).getForm().setValues({
            label: valuesToFill['label'],
            id: valuesToFill['id']
        });
        
        // Second card
        var fieldName = "userDirectories";
        var udData = this._getJsonForFillingForm(valuesToFill[fieldName], fieldName, ["udModelId", "label", "id"]);
        this._getFormPanel(this._cards[1]).setValues(udData);
        
        // Third card
        fieldName = "credentialProviders";
        var cpData = this._getJsonForFillingForm(valuesToFill[fieldName], fieldName, ["cpModelId", "label", "id"]);
        this._getFormPanel(this._cards[2]).setValues(cpData);
    },
    
    /**
     * @private
     * Gets data in good JSON format for filling a {@link ConfigurableFormPanel}
     * @param {Object[]} inputData An array of objects containing a map of parameters (key of the field/value to fill)
     * @param {String} fieldName The name of the composite field
     * @param {String/String[]} idName The name of the id field
     * @return {Object} The object for filling the form
     */
    _getJsonForFillingForm: function(inputData, fieldName, idName)
    {
        idName = Ext.Array.from(idName);
        
        var values = {};
        Ext.Array.forEach(inputData, function(object, index) {
            var realIndex = index + 1; // indexes for repeater begin at 1
            var parameters = object.params;
            
            for (var i = 0; i < idName.length; i++)
            {
                var id = idName[i];
                values[fieldName + this._separator + realIndex + this._separator + id] = object[id]; // for special id field
            }
            
            Ext.Object.each(parameters, function(paramName, paramValue) { // for parameter fields
                values[fieldName + this._separator + realIndex + this._separator + paramName] = paramValue;
            }, this);
        }, this);
        
        var outputData = {
            values: values,
            repeaters: [{
                name: fieldName,
                prefix: "",
                count: inputData.length
            }]
        };
        
        return outputData;
    }
});
