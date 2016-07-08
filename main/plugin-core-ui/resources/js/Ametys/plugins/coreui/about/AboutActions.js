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
 * This class is a singleton to handle actions on "About Ametys" feature.
 * @private
 */
Ext.define('Ametys.plugins.coreui.about.AboutActions', {
    singleton: true,
    
    /**
     * @private
     * @property {Ametys.window.DialogBox} _aboutBox The "About Ametys" dialog box.
     */
    
    /**
     * @private
     * @property {Boolean} _aboutIsInitialized Indicates if the "About Ametys" dialog box is initialized.
     */
    
    /**
     * Opens the "About Ametys" dialog box
     */
    openAboutDialogBox: function()
    {
        this._delayedInitializeAboutDialog();
        this._aboutBox.show();
    },
    
    /**
     * Opens the details dialog box
     */
    openDetailsDialogBox: function()
    {
        alert('Not yet implemented');
    },
    
    /**
     * @private
     * Creates the "About Ametys" dialog box
     */
    _delayedInitializeAboutDialog: function ()
    {
        if (!this._aboutIsInitialized)
        {
            var itemsCfg = this._createItemsCfg();
            
            this._aboutBox = Ext.create('Ametys.window.DialogBox', {
                title: "{{i18n PLUGINS_CORE_UI_ABOUT_DIALOG_TITLE}}",
                iconCls: "ametysmisc-question13",

                maxHeight: 600,
                width: 500,
                bodyPadding: '10',
                layout: {
                    type: "vbox",
                    align: "middle"
                },
                
                items: itemsCfg,
                bodyCls: 'about-dialog',
                
                closeAction: 'hide',
                buttons: [{
                    text: "{{i18n PLUGINS_CORE_UI_ABOUT_DIALOG_BUTTON_OK}}",
                    handler: function() { this._aboutBox.close(); },
                    scope: this
                }]    
            });
            
            Ametys.data.ServerComm.callMethod({
                role: "org.ametys.plugins.core.ui.about.AboutInfoProvider",
                methodName: "getInfo",
                parameters: [Ametys.getAppParameter("user").locale],
                callback: {
                    handler: this._getInfoCb,
                    scope: this
                },
                waitMessage: false
            });
            
            this._aboutIsInitialized = true;
        }
    },
    
    /**
     * @private
     * Creates the items config of the "about" dialogbox
     * @return {Object[]} The configs of the box items  
     */
    _createItemsCfg: function()
    {
        var itemsCfg = [
	        {
                xtype: "image",
                src: Ametys.getPluginDirectPrefix('core-ui') + '/app_logo.jpg',
                height: 128,
                maxWidth: 400,
                alt: '',
                cls: 'about-logo'
	        },
	        {
	            xtype: "component",
                itemId: "appName",
                cls: 'about-app-name',
                html: "",
                scrollable: false
	        },
	        {
	            xtype: "component",
                itemId: "versions",
                cls: 'about-versions',
                html: "",
                scrollable: false
	        },
	        {
	            xtype: "component",
                itemId: "license",
                flex: 1,
                width: '100%',
                cls: 'about-license',
                html: "",
                hidden: true,
                scrollable: true
	        }
        ];
        return itemsCfg;
    },
    
    /**
     * @private
     * Callback method after the information are retrieved from the server.
     * Updates the texts of the dialog box (app name, versions, license).
     * @param {Object} response The server response
     */
    _getInfoCb: function(response)
    {
        this._updateAppName(response.applicationName);
        this._updateVersions(response.versions);
        this._updateLicenseText(response.licenseText);
    },
    
    /**
     * @private
     * Updates the displayed application name
     * @param {String} appName The application name
     */
    _updateAppName: function(appName)
    {
        var cmp = this._aboutBox.items.get("appName");
        cmp.update(appName);
    },
    
    /**
     * @private
     * @property {Ext.XTemplate} _versionTpl The template for versions
     */
    _versionTpl : Ext.create('Ext.XTemplate', 
        '<tpl for=".">',       
            '<div class="version">{name} - ',
            '<tpl if="version">',
                "{{i18n PLUGINS_CORE_UI_ABOUT_DIALOG_VERSIONS_VERSION_PREFIX}}" + '{version}',
                "{{i18n PLUGINS_CORE_UI_ABOUT_DIALOG_VERSIONS_DATE_PREFIX}}" + '{[Ext.util.Format.date(values.date, Ext.Date.patterns.FriendlyDateTime)]}</div>',
            '</tpl>',
        '</tpl>'
    ),
    
    /**
     * @private
     * Updates the displayed versions
     * @param {Object} versions The versions handled by the application as a map. The key is the version name, and the value is a mpa containing the version number and the date.
     */
    _updateVersions: function(versions)
    {
        var cmp = this._aboutBox.items.get("versions");
        return this._versionTpl.overwrite (cmp.getEl(), versions);
    },
    
    /**
     * @private
     * Updates the license text
     * @param {String} license The license text
     */
    _updateLicenseText: function(license)
    {
        if (!Ext.isEmpty(license))
        {
            var cmp = this._aboutBox.items.get("license");
            cmp.update(license);
            cmp.show();
        }
    }
    
});