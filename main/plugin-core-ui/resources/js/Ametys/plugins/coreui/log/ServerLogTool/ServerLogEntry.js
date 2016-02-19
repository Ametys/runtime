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
 * This class is the model for entries in the grid of the server logs tool
 * @private
 */
Ext.define("Ametys.plugins.coreui.log.ServerLogTool.ServerLogEntry", {
    extend: 'Ext.data.Model',
    
    fields: [
             {name: 'id'},
             {name: 'timestamp', type: 'int'},
             {name: 'user'},
             {name: 'requestURI'},
             {name: 'level'},
             {name: 'category'},
             {name: 'message'},
             {name: 'thread'},
             {name: 'location'},
             {name: 'callstack'},
             {
                 name: 'levelCode',
                 convert: function(value, record)
                 {
                     switch (record.getData().level)
                    {
                    case "DEBUG":
                        return 0;
                    case "INFO":
                        return 1;
                    case "ERROR":
                        return 3;
                    case "FATAL":
                        return 4;
                    default:
                        return 2; // WARN
                    }
                 }
             }
    ]
});