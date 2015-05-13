/*
 *  Copyright 2010 Anyware Services
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
 * Class to handle system announce.
 * This constructor sends a check request, that will send another one on callback.
 * @private
 */
Ext.define('Ametys.runtime.system.Announce', {
    singleton: true,
    
    /**
     * @private
     * @property {Number} _lastModification The date (as long) or 0 if not applicable, of the known system statup time.
     */
    _lastModification: 0,
    
    /**
     * @private
     * Send a check request.
     */
    _sendCheckMessage: function()
    {
        // Define a 'no.system.message' application parameter to 'true' to disable ping.
        if (Ametys.getAppParameter('no.system.message') != true)
        {
            Ametys.data.ServerComm.send({
                plugin: 'core',
                url: 'system-announcement/view.xml',
                priority: Ametys.data.ServerComm.PRIORITY_MINOR,
                callback: {
                    handler: this._messageCallback,
                    scope: this
                }
            });
        }
    },
    
    /**
     * Callback function invoked when the message is returned.
     * @param {Object} response The XML response provided by the {@link Ametys.data.ServerComm}.
     * @param {Object} args The callback parameters passed to the {@link Ametys.data.ServerComm#send} method.
     * @private
     */
    _messageCallback: function(response, args)
    {
        this._sendCheckMessage();
        
        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CMS_TOOL_ANNOUNCE_ERROR_TITLE'/>", response, 'Ametys.runtime.system.Announce'))
        {
            return;
        }
        
        var isAvailable = Ext.dom.Query.selectValue('> SystemAnnounce > IsAvailable', response, 'false') === 'true';
        if (isAvailable)
        {
            var message = Ext.dom.Query.selectValue('> SystemAnnounce > Message', response);
            var lastModification = Ext.dom.Query.selectNumber('> SystemAnnounce > LastModification', response);
            
            if (lastModification > this._lastModification)
            {
                this._lastModification = lastModification;
                Ametys.Msg.show({
                    title: "<i18n:text i18n:key='PLUGINS_CMS_TOOL_ANNOUNCE_TITLE'/>",
                    msg: message,
                    buttons: Ext.Msg.OK,
                    icon: Ext.MessageBox.INFO
                });
            }
        }
    }
    
});

// Start the announce checker.
Ametys.runtime.system.Announce._sendCheckMessage();
