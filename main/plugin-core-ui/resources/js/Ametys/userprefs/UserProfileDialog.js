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
     * @private
     * @property {Boolean} _userProfileDialogInitialized True if the dialog box is initialized
     */
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
     * @property {Boolean} _valuesSet indicates if values have been set or not
     */
    
    /**
     * Open the dialog box
     * @param {Function} callback The callback function to called after validating dialog box
     */
    open: function (callback, scope)
    {
        this._valuesSet = false;
        
        this._cbFn = callback || Ext.emptyFn;
        this._cbScope = scope || null;
        
        if (!this._initializeUserProfileDialog())
        {
            return;
        }
        
        this._box.down('#image-field').loadStore({
            callback: this._setValues,
            scope: this
        });
        
        this._box.show();
    },
    
    /**
     * Create and initialize the dialog box
     * @private
     */
    _initializeUserProfileDialog: function ()
    {
        if (this._initialized)
        {
            return true;
        }
        
        // Load profile user prefs
        Ametys.userprefs.UserPrefsDAO.load(Ext.bind(this._loadUserProfilePrefsCb, this), Ametys.userprefs.UserProfileDialog.USERPREF_CONTEXT);
        
        var field = this._createProfileImageField();
        
        // Create dialogbox
        this._box = Ext.create('Ametys.window.DialogBox', {
            title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_TITLE}}",
            icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/user-profiles/edit-profile_16.png',
            
            width: 700,
            
            layout: 'anchor',
            defaults: {
                anchor: '100%'
            },
            
            items: [
                {
	                xtype: 'component',
	                cls: 'a-text',
	                html: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_INTRO}}"
	            }, 
	            field,
	            {
	                xtype: 'component',
	                cls: 'a-text-warning',
	                html: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_HINT}}",
	            }
	        ],
            
	        defaultFocus: field,
	        
	        referenceHolder: true,
	        defaultButton :'a',
	        
            closeAction: 'hide',
            
            buttons : [{
            	reference: 'a',
                    text: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_OK}}",
                    handler : Ext.bind(this._ok, this)
                }, {
                    text: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_CANCEL}}",
                    handler: Ext.bind(function() {this._box.close();}, this)
                } 
            ]
        });
        
        this._initialized = true;
        return true;
    },
    
    /**
     * Load profile user prefs callback
     * @private
     */
    _loadUserProfilePrefsCb: function(success)
    {
        if (success)
        {
            this._setValues();
        }
    },
    
    /**
     * Set the prefs values into the box
     * @private
     */
    _setValues: function()
    {
        if (this._valuesSet)
        {
            return;
        }
        
        // Set value for image field
        var value = Ametys.userprefs.UserPrefsDAO.getValue(Ametys.userprefs.UserProfileDialog.USERPREF_PROFILE_IMAGE, Ametys.userprefs.UserProfileDialog.USERPREF_CONTEXT);
        
        // Value might be null if cache not already prepared (see {@link Ametys.userprefs.UserPrefsDAO})
        if (value)
        {
            imageField = this._box.down('#image-field');
            imageField.setValue(value);
            
            this._valuesSet = true;
        }
    },
    
    /**
     * Create the profile image field, which display the available images to the user
     * @return {Ametys.userprefs.UserProfileDialog.ProfileImageField} The profile image field
     * @private
     */
    _createProfileImageField: function()
    {
        return Ext.create('Ametys.userprefs.UserProfileDialog.ProfileImageField', {
            itemId: 'image-field',
            allowBlank: false,
            
            labelAlign: 'top',
            msgTarget: 'side',
            
            height: 270
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
        
        var value = imageField.getValue(),
            userPrefs = {};
        
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
    },
    
    /**
     * Callback called once the profile userprefs have been saved
     * @param {Boolean} callback.success Has the save operation been successful
     * @param {Object} callback.errors The key is the preference name, and the value an error message. Can be empty event is success is false on server exception: in that cas the user is already notified.
     * @private
     */
    _saveProfileCb: function(success, errors)
    {
        // Handling errors.
        if (success)
        {
            Ametys.notify({
                type: 'info',
                title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_SUCCESS_TITLE}}",
                description: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_SUCCESS_DESC}}"
            });
            
            this._box.close();
            
            // User pref modified message
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.MODIFIED,
                
                targets: {
                    id: Ametys.message.MessageTarget.USER_PREFS,
                    parameters: {
                        context: Ametys.userprefs.UserProfileDialog.USERPREF_CONTEXT
                    }
                }
            });
        }
        else if (errors)
        {
            var details = '';
            for (var error in errors)
            {
                details += error + ": " + errors[error] + "\n";
            }
            
            Ametys.log.ErrorDialog.display({
                title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_FAILURE_TITLE}}",
                text: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_SAVE_FAILURE_DESC}}",
                details: details,
                category: this.self.getName() 
            });
        }
    }
});

