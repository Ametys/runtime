/*
 *  Copyright 2011 Anyware Services
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
 * Class to handle system start time checker.
 * This constructor sends a check request, that will send another one on callback.
 * @private
 */
Ext.define('Ametys.plugins.coreui.system.StartTimeChecker', {
    singleton: true,
    
    /**
     * @private
     * @property {Number} _lastModification The date (as long) or 0 if not applicable, of the known system statup time.
     */
    _lastModification: 0,
    
    /**
     * @private
     * @property {String} _lastVersion The CMS version returned by the last request.
     */
    _lastVersion: '',
    
    /**
     * @private
     * @property {String[]} __lastStatus The CMS status returned by the last request.
     */
    _lastStatus: [],
    
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
                plugin: 'core-ui',
                url: 'system-startuptime.xml',
                priority: Ametys.data.ServerComm.PRIORITY_MINOR,
                callback: {
                    handler: this._messageCallback,
                    scope: this
                }
            });
        }
    },
    
    /**
     * Callback function called when the message is returned.
     * @param {Object} response The XML response provided by the {@link Ametys.data.ServerComm}.
     * @param {Object} args The callback parameters passed to the {@link Ametys.data.ServerComm#send} method.
     * @private
     */
    _messageCallback: function(response, args)
    {
        this._sendCheckMessage();
        
        if (Ametys.data.ServerComm.handleBadResponse("{{i18n PLUGINS_CORE_UI_TOOL_STARTUPTIME_ERROR_TITLE}}", response, 'Ametys.plugins.coreui.system.StartTimeChecker'))
        {
            return;
        }
        
        var startupNode = Ext.dom.Query.selectNode('> startup-time', response);
        var lastModification = Ext.dom.Query.selectNumber('', startupNode);
        var lastVersion = startupNode.getAttribute('version');
        
        var newStatus = Ext.Array.clean((startupNode.getAttribute('status') || '').split(',')),
            notChanged = Ext.Array.intersect(this._lastStatus, newStatus);
            removed = Ext.Array.difference(this._lastStatus, notChanged),
            added = Ext.Array.difference(newStatus, notChanged),
            hasDiff = removed.length > 0 || added.length > 0;
            
        if (lastModification > this._lastModification && this._lastModification > 0)
        {
            Ametys.Msg.show({
                   title: (lastVersion == this._lastVersion) ? "{{i18n PLUGINS_CORE_UI_TOOL_STARTUPTIME_TITLE}}" : "{{i18n PLUGINS_CORE_UI_TOOL_STARTUPTIME_TITLE2}}",
                   msg: (lastVersion == this._lastVersion) ? "{{i18n PLUGINS_CORE_UI_TOOL_STARTUPTIME_MESSAGE}}" : "{{i18n PLUGINS_CORE_UI_TOOL_STARTUPTIME_MESSAGE2}}",
                   buttons: Ext.Msg.OK,
                   icon: Ext.MessageBox.INFO
            });
        }
        
        if (hasDiff)
        {
            Ext.create("Ametys.message.Message", {
                type: Ametys.message.Message.MODIFIED,
                targets: {
                    id: Ametys.message.MessageTarget.APPLICATION,
                    parameters: {
                        status: {
                            current: newStatus,
                            added: added,
                            removed: removed
                        }
                    }
                }
            });
        }
        
        this._lastStatus = newStatus;
        this._lastModification = lastModification;
        this._lastVersion = lastVersion;
    }
    
});

// Start the checker.
Ametys.plugins.coreui.system.StartTimeChecker._sendCheckMessage();

Ext.define("Ametys.message.ApplicationMessageTarget", {
    override: "Ametys.message.MessageTarget",
    statics:  {
        /**
         * @member Ametys.message.MessageTarget
         * @static
         * @readonly
         * @property {String} APPLICATION The target application. 
         * Parameters are:
         * * {Object} APPLICATION.status An object containing the system status
         * * {String[]} APPLICATION.status.current The current system status
         * * {String[]} APPLICATION.status.added The system status that have just been added
         * * {String[]} APPLICATION.status.removed The system status that have just been removed
         */
        APPLICATION: "application"
    }
});
