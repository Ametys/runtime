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
 * @private
 * Singleton class defining the actions related to the super user
 */
Ext.define('Ametys.plugins.admin.superuser.SuperUserActions', {
    singleton: true,
    
    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The dialog box
     */
    
    /**
     * @private
     * @property {Ext.form.Panel} _form The form panel of the box
     */
    
    /**
     * Affects a user to an existing profile or a new super profile on an empty context or on the context defined by controller
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    act: function(controller)
    {
        this.affectSuperUser(controller, ["/application"], {});
    },
    
    /**
     * Affects a user to an existing profile or a new super profile on an empty context or on the context defined by controller
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     * @param {String[]} populationContexts The contexts for the populations where to retrieve the users for edition.user widget
     * @param {Object} additionalParameters The additional parameters for the server call
     */
    affectSuperUser: function(controller, populationContexts, additionalParameters)
    {
        this._createAndShow(controller, populationContexts, additionalParameters);
    },
    
    /**
     * Creates the dialog box
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     * @param {String[]} populationContexts The contexts for the populations where to retrieve the users
     * @param {Object} additionalParameters The additional parameters
     */
    _createAndShow: function(controller, populationContexts, additionalParameters)
    {
        this._form = Ext.create('Ext.form.Panel', {
            name: 'form',
            items: this._getItemsCfg(populationContexts)
        });
        
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_TITLE}}",
            iconCls: 'ametysicon-tie6',
            
            width: 480,
            height: 350,
            layout: {
                type: "vbox",
                align: "stretch"
            },
            
            defaultFocus: 'form',
            items: this._form,
            closeAction: 'destroy',
            
            referenceHolder: true,
            defaultButton: 'validate',
            
            buttons:  [{
                reference: 'validate',
                itemId: 'button-validate',
                text: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_OK_BUTTON}}",
                handler: Ext.bind(this._validate, this, [controller, additionalParameters], 0)
            }, {
                itemId: 'button-cancel',
                text: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_CANCEL_BUTTON}}",
                handler: Ext.bind(this._cancel, this)
            }]
        });
        
        
        this._box.show();
    },
    
    /**
     * @private
     * Gets the configuration of items of the form panel
     * @param {String[]} populationContexts The contexts for the populations where to retrieve the users
     * @return {Object} The configuration for items
     */
    _getItemsCfg: function(populationContexts)
    {
        return [
            {
                xtype: "component",
                html: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_HINT_1}}",
                style: {
                    marginBottom: '10px'
                }
            }, {
                xtype: "edition.user",
                name: 'users',
                itemId: 'users',
                contexts: populationContexts,
                fieldLabel: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_USER}}",
                multiple: false,
                allowBlank: false,
                style: {
                    marginBottom: '20px'
                }
            }, {
                xtype: "component",
                html: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_HINT_2}}"
            }, {
                xtype: 'radiofield',
                boxLabel: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_CHOOSE_PROFILE}}",
                name: 'radio-profile',
                itemId: 'radio-profile-choose',
                checked: true,
                inputValue: 'choose',
                handler: this._onRadioCheck,
                scope: this,
                style: {
                    marginLeft: '5px'
                }
            }, {
                xtype: "combobox",
                name: 'profile',
                itemId: 'profile',
                fieldLabel: "Profil",
                valueField: 'id',
                displayField: 'label',
                store: {
                    autoDestroy: true,
                    proxy: {
                        type: 'ametys',
                        plugin: 'core',
                        url: 'rights/profiles.json',
                        reader: {
                            type: 'json',
                            rootProperty: 'profiles'
                        }
                     },
                     sorters: [{property: 'label', direction: 'ASC'}],
                     fields: [
                         {name: 'id'},
                         {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}
                     ]
                },
                allowBlank: false,
                style: {
                    marginBottom: '10px'
                }
            }, {
                xtype: 'radiofield',
                boxLabel: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_CREATE_PROFILE}}",
                name: 'radio-profile',
                itemId: 'radio-profile-create',
                inputValue: 'create',
                handler: this._onRadioCheck,
                scope: this,
                style: {
                    marginLeft: '5px'
                }
            }, {
                xtype: 'textfield',
                name: 'name',
                itemId: 'name',
                fieldLabel: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_PROFILE_NAME}}",
                value: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_PROFILE_NAME_DEFAULT_VALUE}}",
                allowBlank: false,
                disabled: true
            }
        ];
    },
    
    /**
     * @private
     * Function called when the radio button changed
     * @param {Ext.form.field.Checkbox} checkbox The Checkbox being toggled.
     * @param {boolean} checked The new checked state of the checkbox.
     */
    _onRadioCheck: function(checkbox, checked)
    {
        if (checkbox.getItemId() == 'radio-profile-choose')
        {
            this._form.items.get('profile').setDisabled(!checked);
        }
        else if (checkbox.getItemId() == 'radio-profile-create')
        {
            this._form.items.get('name').setDisabled(!checked);
        }
    },
    
    /**
     * @private
     * Function called when OK button is pressed
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller which called the action
     * @param {Object} additionalParameters The additional parameters
     */
    _validate: function(controller, additionalParameters)
    {
        var form = this._form.getForm();
        if (!form.isValid())
        {
            return;
        }
        
        var mode = form.findField("radio-profile");
        
        var opts = {
            errorMessage: { 
                msg: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_ERROR}}", 
                category: Ext.getClassName(this)
            },
            waitMessage: true
        };
        
        var user = form.findField("users").getValue();
        
        if (mode == 'create')
        {
        	var name = form.findField("name").getValue();
            var params = [user, name, additionalParameters];
            controller.serverCall('affectUserToNewProfile', params, this._affectUserToNewProfileCb, opts);
        }
        else if (mode == 'choose')
        {
        	var profile = form.findField("profile").getValue();
            var params = [user, profile, additionalParameters];
            opts.arguments = [profile];
            controller.serverCall('affectUserToProfile', params, this._affectUserToProfileCb, opts);
        }
        
        this._box.close();
    },
    
    /**
     * @private
     * Function called when cancel button is pressed
     */
    _cancel: function()
    {
        this._box.close();
    },
    
    /**
     * @private
     * Callback function called after affecting a user to a new profile
     * @param {String} profileId The server response: the id of the created profile
     * @param {Object} args the callback arguments
     */
    _affectUserToNewProfileCb: function(profileId, args)
    {
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.CREATED,
            targets: [{
                id: Ametys.message.MessageTarget.PROFILE,
                parameters: {id: profileId}
            }]
        });
        
        Ametys.notify({
            title: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_TITLE}}",
            description: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_SUCCESS}}"
        });
    },
    
    /**
     * @private
     * Callback function called after affecting a user to an existing profile
     * @param {Object} response The server response
     * @param {Object} args the callback arguments
     */
    _affectUserToProfileCb: function(response, args)
    {
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.MODIFIED,
            targets: [{
                id: Ametys.message.MessageTarget.PROFILE,
                parameters: {id: args[0]}
            }]
        });
        
        Ametys.notify({
            title: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_TITLE}}",
            description: "{{i18n PLUGINS_ADMIN_SUPERUSER_AFFECT_SUCCESS}}"
        });
    }
});
