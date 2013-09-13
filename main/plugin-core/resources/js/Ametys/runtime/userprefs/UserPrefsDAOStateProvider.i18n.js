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
 * 		var upP = Ext.create("Ametys.runtime.userprefs.UserPrefsDAOStateProvider", {
 * 			preference: "workspace"
 * 		});
 * 		Ext.state.Manager.setProvider(upP);
 * 
 * Based upon Ametys.runtime.userprefs.UserPrefsDAO that should be already loaded.
 */
Ext.define('Ametys.runtime.userprefs.UserPrefsDAOStateProvider', {
	extend: "Ext.state.Provider",
	
	/**
	 * @cfg {String} preference The name of the preference to use to store.
	 */

	/**
	 * @cfg {String} prefContext The preference context to use. Use the default one if not specified. See Ametys.runtime.userprefs.UserPrefsDAO#setDefaultPrefContext 
	 */

	constructor: function(config)
	{
		this.callParent(arguments);
		
		this.state = this.decodeValue(Ametys.runtime.userprefs.UserPrefsDAO.getValue(this.preference, this.prefContext));
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
     * Launch a save on the users pref DAO
     */
    _saveState: function()
    {
    	var save = {};
    	save[this.preference] = this.encodeValue(this.state);
    	Ametys.runtime.userprefs.UserPrefsDAO.saveValues(save, Ext.bind(this._saveSateCB, this), this.prefContext);
    },
    
    /**
     * @private
     * Callback after a save process
     * @param {Boolean} success True is save worked fine.
     * @param {Object} errors Association (preference name, error message)
     */
    _saveSateCB: function(success, errors)
    {
    	if (success == false && errors != null)
    	{
    		var details = "";
    		for (var error in errors)
    		{
    			details += error + ": " + errors[error] + "\n";
    		}
    		
    	    Ametys.log.ErrorDialog.display({
    	        title: "<i18n:text i18n:key="PLUGINS_CORE_USER_PREFERENCES_STATE_PROVIDER_SAVE_FAILURE_TITLE"/>",
    	        text: "<i18n:text i18n:key="PLUGINS_CORE_USER_PREFERENCES_STATE_PROVIDER_SAVE_FAILURE_DESC"/>",
    	        details: details,
    	        category: this.self.getName() 
    	    });
    	}
    }
});