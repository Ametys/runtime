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
 * This class defines a single mask to hide the overall screen several times.
 * A single message will be displayed by adding serveral masks.
 * 
 * 
 * <pre><code>
 *  var id1 = Ametys.mask.GlobalLoadMask.mask();            // Display a mask all over the screen with a std message
 *  var id2 = Ametys.mask.GlobalLoadMask.mask();            // Nothing change
 *  Ametys.mask.GlobalLoadMask.unmask(id1);                 // Nothing change
 *  Ametys.mask.GlobalLoadMask.mask("my message", "myid");  // The mask default message will have sub message "my message"
 *  Ametys.mask.GlobalLoadMask.unmask("myid");              // The mask come back to its default message
 *  Ametys.mask.GlobalLoadMask.unmask(id2);                 // The mask disappears.
 * </code></pre>
 * 
 * 
 * This mask will be attached to the viewport or to the body.
 */
Ext.define(
    "Ametys.mask.GlobalLoadMask", 
    {
        singleton: true,
        
        /**
         * @property {String} DEFAULT_MESSAGE The message that will always be displayed on the top of the waiting messages.
         */
        DEFAULT_MESSAGE: "<i18n:text i18n:key='PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE'/>",
        
        /** 
         * @private
         * @property {String/Ext.LoadMask} _maskInstance The only mask instance or "body" if the viewport is to display on the body.
         */
        _maskInstance: null,
        
        /** 
         * @private
         * @property {Object} _masks The currently displayed masks. 
         * @property {String} _masks.key The mask identifier 
         * @property {String} _masks.value The message associated. Cannot be null. 
         */ 
        _masks: {},
        
        /**
         * Show a mask if necessary.
         * @param {String} [message] The message to display on the mask (in addition to others messages and the #property-DEFAULT_MESSAGE). Can be null or empty.
         * @param {String} [id] An identifier for the message. If null or empty, an identifier will be generated and returned.
         * @return {String} The identifier of the mask. This will be necessarey to hide the mask with #unmask.
         */
        mask: function(message, id)
        {
            id = id || Ext.id();
            
            this._masks[id] = message ? message : "";
            
            this._update();
            
            return id;
        },
        
        /**
         * Remove the given identifier for the overall mask. If this was the last identifer, the mask will be hidden.
         * @param {String} id The id of the mask to destroy. If the id is unknown, nothing will happen.
         */
        unmask: function(id)
        {
            delete this._masks[id];
            
            this._update();
        },
        
        /**
         * @private
         * Operates visual modifications to display/hide the mask and to create the message.
         */
        _update: function()
        {
            if (Ext.Object.getSize(this._masks) == 0)
            {
                this._hide();
            }
            else
            {
                var msg = this.DEFAULT_MESSAGE;
                Ext.Object.each(this._masks, function(id, message) {
                    if (message)
                    {
                        msg += "<br/>" + message;
                    }
                }, this);
                
                this._show(msg);
            }
        },
        
        /**
         * @private
         * Hide the mask
         */
        _hide: function()
        {
            this._prepareInstance();
            
            if (this._maskInstance === "body")
            {
                Ext.getBody().unmask();
            }
            else
            {
                this._maskInstance.hide();
            }
        },
        
        /**
         * @private
         * Display the mask
         * @param {String} msg The message to display or update
         */
        _show: function(msg)
        {
            this._prepareInstance();
            
            if (this._maskInstance === "body")
            {
                Ext.getBody().mask(msg);
            }
            else
            {
                if (!this._maskInstance.isVisible() || this._maskInstance.msg != msg)
                {
                    this._maskInstance.msg = msg;
                    this._maskInstance.hide();
                    this._maskInstance.show();
                }
            }
        },
        
        /**
         * @private
         * Creates the single mask instance if necessary
         */
        _prepareInstance: function()
        {
            if (this._maskInstance !== null)
            {
                return;
            }
            
            var vps = Ext.ComponentQuery.query('viewport'); 
            if (vps.length == 0)
            {
                this._maskInstance = "body";
            }
            else
            {
                this._maskInstance = Ext.create("Ext.LoadMask", { target: vps[0] });
            }
        }
    }
);
        