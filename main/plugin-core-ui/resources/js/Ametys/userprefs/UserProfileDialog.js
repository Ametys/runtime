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
 * A dialog box allowing the user to set its user profile preferences
 */
Ext.define('Ametys.userprefs.UserProfileDialog', {
    singleton: true,
    
    /**
     * @property {String} USERPREF_CONTEXT The user pref context for user profile preferences
     * @readonly
     */
    USERPREF_CONTEXT: '/profile',
    
    /**
     * @property {String} USERPREF_PROFILE_IMAGE The user pref id for the user profile image
     * @readonly
     */
    USERPREF_PROFILE_IMAGE: 'profile-image',
    
    /**
     * @property {Boolean} _initialized True if the dialog box was already initialized
     * @private
     */
    _initialized: false,
    
    /**
     * @property {Ametys.window.DialogBox} _box The dialog box
     * @private
     */
    /**
     * @property {Function} _cbFn Optional callback
     * @private
     */
    /**
     * @property {Object} _cbScope Callback scope
     * @private
     */
    
    /**
     * Open the dialog box
     * @param {Function} callback The callback function to called after validating dialog box
     */
    open: function (callback, scope)
    {
        this._cbFn = callback || Ext.emptyFn;
        this._cbScope = scope || null;
        
        this._createDialogBox();
        
        this._box.show();
    },
    
    /**
     * Creates the dialog box
     * @private
     */
    _createDialogBox: function ()
    {
        if (!this._initialized)
        {
            this._box = Ext.create('Ametys.window.DialogBox', {
                title: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_TITLE'/>",
                icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/user-profiles/edit-profile.png', // FIXME better icon
                
                width: 700,
                
                layout: 'anchor',
                defaults: {
                    anchor: '100%'
                },
                
                items : [{
                    xtype: 'component',
                    cls: 'a-text',
                    html: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_INTRO'/>",
                }, this._createProfileImageField()],
                            
                closeAction: 'destroy',
                buttons : [{
                        text: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_OK'/>",
                        handler : Ext.bind(this._ok, this)
                    }, {
                        text: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_CANCEL'/>",
                        handler: Ext.bind(function() {this._box.close();}, this)
                    } 
                ]
            });
        }
    },
    
    /**
     * Create the profile image field, which display the available images to the user
     * @return {Ametys.userprefs.UserProfileDialog.ProfileImageField} The profile image field
     */
    _createProfileImageField: function()
    {
        return Ext.create('Ametys.userprefs.UserProfileDialog.ProfileImageField', {
            itemId: 'image-field',
            allowBlank: false,
            
            labelAlign: 'top',
            msgTarget: 'side',
            
            height: 182
        });
    },
    
    /**
     * Function called when clicking on 'Ok' button.
     * Calls the callback function passed in {@link #method-open} and hide the dialog box.
     * @private
     */
    _ok: function()
    {
        // Save user pref
        var imageField = this._box.down('#image-field');
        if (!imageField.isValid())
        {
            return;
        }
        
        var value = imageField.getValue();
        
        if (value.source == 'userpref') 
        {
            // if userpref, the value has not changed, nothing to do
            
            // User callback
            if (Ext.isFunction(this._cbFn))
            {
                this._cbFn.call(this._cbScope);
            }
            
            this._box.close();
        }
        else
        {
            var userPrefs = {};
            
            userPrefs[Ametys.userprefs.UserProfileDialog.USERPREF_PROFILE_IMAGE] = Ext.JSON.encode(value);
            
            Ametys.userprefs.UserPrefsDAO.saveValues(
                userPrefs,
                Ext.bind(this._saveProfileCb, this),
                Ametys.userprefs.UserProfileDialog.USERPREF_CONTEXT,
                null, // default priority
                null, // no cancel code
                'core-ui', // plugin
                'user-profile/save.xml' // url
            );
            
            // User callback
            if (Ext.isFunction(this._cbFn))
            {
                this._cbFn.call(this._cbScope);
            }
        }
    },
    
    /**
     * Callback called once the profile userprefs have been saved
     * @param {Boolean} callback.success Has the save operation been successful
     * @param {Object} callback.errors The key is the preference name, and the value an error message. Can be empty event is success is false on server exception: in that cas the user is already notified.
     */
    _saveProfileCb: function(success, errors)
    {
        // Handling errors.
        if (success)
        {
            Ametys.Msg.show({
                title: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_SUCCESS_TITLE'/>",
                msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_SUCCESS_DESC'/>",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.INFO
            });
            
            this._box.close();
        }
        else if (errors)
        {
            var details = '';
            for (var error in errors)
            {
                details += error + ": " + errors[error] + "\n";
            }
            
            Ametys.log.ErrorDialog.display({
                title: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_FAILURE_TITLE'/>",
                text: "<i18n:text i18n:key='PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_FAILURE_DESC'/>",
                details: details,
                category: this.self.getName() 
            });
        }
    }
});

