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
 * State provider that stores values in a single binary userpref.
 * 
 * 		var upP = Ext.create("Ametys.userprefs.UserPrefsDAOStateProvider", {
 * 			preference: "workspace"
 * 		});
 * 		Ext.state.Manager.setProvider(upP);
 * 
 * Based upon Ametys.userprefs.UserPrefsDAO that should be already loaded.
 */
Ext.define('Ametys.userprefs.UserPrefsDAOStateProvider', {
	extend: "Ext.state.Provider",
	
	statics: {
		/**
		 * @property {Number} __SAVE_TIMEOUT The time in ms before we wait before saving state. If in between another saveState appears, it will cancel the previous call.
		 * @readonly
		 * @private
		 */
		__SAVE_TIMEOUT: 100
	},
	
	/**
	 * @cfg {String} preference The name of the preference to use to store.
	 */

	/**
	 * @cfg {String} prefContext The preference context to use. Use the default one if not specified. See Ametys.userprefs.UserPrefsDAO#setDefaultPrefContext 
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		this.state = this.decodeValue(Ametys.userprefs.UserPrefsDAO.getValue(this.preference, this.prefContext)) || {};
		
		window.onbeforeunload = Ext.bind(this._onUnload, this);
	},
	
	/**
	 * @private
	 * Listener when the window is unload to save unsaved preferences on the fly
	 * @param {Object} eOpts The options passed to the addListener
	 */
	_onUnload: function(eOpts)
	{
		this._saveState(Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS);
	},
	
    set : function(name, value){
        this.callParent(arguments);
        this._saveState();
    },
    
    clear : function(name){
        this.callParent(arguments);
        this._saveState();
    },
    
    /**
     * @private
     * Do effectively the save
     * @param {String} [priority=Ametys.data.ServerComm#PRIORITY_MINOR] The Ametys.userprefs.UserPrefsDAO#saveValues priority to use.
     */
    _saveState: function(priority)
    {
    	priority = priority || Ametys.data.ServerComm.PRIORITY_MINOR
    	
    	var save = {};
    	save[this.preference] = this.encodeValue(this.state);
    	
    	Ametys.userprefs.UserPrefsDAO.saveValues(save, Ext.bind(this._saveStateCB, this), this.prefContext, priority, this.self.getName() + "$saving");
    },
    
    /**
     * @private
     * Callback after a save process
     * @param {Boolean} success True is save worked fine.
     * @param {Object} errors Association (preference name, error message)
     */
    _saveStateCB: function(success, errors)
    {
    	if (success == false && errors != null)
    	{
    		var details = "";
    		for (var error in errors)
    		{
    			details += error + ": " + errors[error] + "\n";
    		}
    		
    	    Ametys.log.ErrorDialog.display({
    	        title: "<i18n:text i18n:key='KERNEL_USER_PREFERENCES_STATE_PROVIDER_SAVE_FAILURE_TITLE'/>",
    	        text: "<i18n:text i18n:key='KERNEL_USER_PREFERENCES_STATE_PROVIDER_SAVE_FAILURE_DESC'/>",
    	        details: details,
    	        category: this.self.getName() 
    	    });
    	}
    }
});