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
 * This helper is used to choose a color. See #open method.
 */
Ext.define("Ametys.helper.ChooseColor", {
    singleton: true,

    /**
     * @private
     * @property {Function} _cbFn Callback function to execute after the user validates the dialog
     */

    /**
     * @private
     * @property {Ametys.window.DialogBox} _box The dialog box
     */

    /**
     * @private
     * @property {Ext.form.FormPanel} _colorSelector The color selector
     */

    /**
     * Open the dialog box for selecting a color
     * @param {String} [color] The current color value, in hex6 format, with no leading '#'. Default value is "000000".
     * @param {Function} [callback] The function to call when successful. This function receive the new color as argument, in hex6 format.
     */
    open: function (color, callback)
    {
        this._cbFn = callback;

        this._delayedInitialize();

        this._box.show();
        this._initDialog(color || "000000");
    },

    /**
     * Creates the dialog box
     * @private
     */
    _delayedInitialize: function ()
    {
        if (this._initialized)
        {
            return;
        }

        this._colorSelector = Ext.create({
            xtype: 'colorselector',
			showPreviousColor: false,
			showOkCancelButtons: false
        });

        this._box = Ext.create('Ametys.window.DialogBox', {
            title: "{{i18n PLUGINS_CORE_UI_COLORSELECTOR_OTHERS_COLORS}}",
            width: 600,
            scrollable: false,

            items: [this._colorSelector],

            closeAction: 'hide',
            
            referenceHolder: true,
            defaultButton: 'validate',
            defaultButtonTarget: 'el',
            
            buttons: [{
            	reference: 'validate',
            	text: "{{i18n PLUGINS_CORE_UI_HELPER_CHOOSECOLOR_OK}}",
                handler: Ext.bind(this._validate, this)
            }, {
                text: "{{i18n PLUGINS_CORE_UI_HELPER_CHOOSECOLOR_CANCEL}}",
                handler: Ext.bind(this._cancel, this)
            }]
        });

        this._initialized = true;
    },


    /**
     * Initialize the dialog
     * @param {String} [color] The current color value.
     * @private
     */
    _initDialog: function (color)
    {
        this._colorSelector.setValue(color);
    },

   /**
    * Handler for the 'Ok' button of the dialog box.
    * Return the current color value to the callback
    * @private
    */
    _validate: function ()
    {
        var color = this._colorSelector.getValue();

        this._box.hide();
        if (Ext.isFunction(this._cbFn))
        {
            this._cbFn(color);
        }
    },

    /**
     * Handler for the "cancel" button of the dialog. Hide the dialog.
     * @private
     */
    _cancel: function ()
    {
        this._box.hide();
    }
});
