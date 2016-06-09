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
  * This class is the representation of a task
  */
Ext.define('Ametys.plugins.coreui.schedule.Task', {
    config: {
        /**
         * @cfg {String} id The unique id of the task
         */
        /**
         * @method getId Get the #cfg-id
         * @return {String} The id
         */
        /** @ignore */
        id: null,
        /**
         * @cfg {Boolean} modifiable The modifiable status
         */
        /**
         * @method getModifiable Get the #cfg-modifiable
         * @return {Boolean} The modifiable status
         */
        /** @ignore */
        modifiable: false,
        /**
         * @cfg {Boolean} removable The removable status
         */
        /**
         * @method getRemovable Get the #cfg-removable
         * @return {Boolean} The removable status
         */
        /** @ignore */
        removable: false,
        /**
         * @cfg {Boolean} deactivatable The deactivatable status
         */
        /**
         * @method getDeactivatable Get the #cfg-deactivatable
         * @return {Boolean} The deactivatable status
         */
        /** @ignore */
        deactivatable: false
    },
    
    /**
     * Creates a task instance
     * @param {Object} config See configuration doc.
     */
    constructor: function(config)
    {
        this.initConfig(config);
    },
    
    /**
     * Gets the task properties
     * @param {Object} initialProperty The initial properties
     * @return {Object} The task properties
     */
    getProperties: function(initialProperty)
    {
        return Ext.apply({
            id: this._id,
            modifiable: this._modifiable,
            removable: this._removable,
            deactivatable: this._deactivatable
        }, initialProperty);
    }
});
