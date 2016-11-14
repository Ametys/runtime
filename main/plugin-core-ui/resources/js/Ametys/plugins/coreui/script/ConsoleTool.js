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
 * Tool showing the script tool results.
 * @private
 */
Ext.define('Ametys.plugins.coreui.script.ConsoleTool',
{
    extend: 'Ametys.tool.Tool',
    
    /**
     * @private
     * @property {Ext.Template} _scriptDateTpl HTML fragment template used for displaying script duration and datetime
     */
    _scriptDateTpl: Ext.create('Ext.Template', '<div class="result-header"><span>',
            "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_RESULT_MSG}}",
            '{date} ({duration}',
            " {{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_RESULT_MSG_SEC}}",
            ')</span></div>'),
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        // Bus messages listeners
        Ametys.message.MessageBus.on('console-script-executed', this._onScriptExecution, this);
    },
    
    createPanel: function()
    {
        return Ext.create('Ext.panel.Panel', {
            layout: 'fit',
            cls: 'coreui-script-result-tool',
            scrollable: true,
            html: ''
        });
    },
    
    getMBSelectionInteraction: function()
    {
        return Ametys.tool.Tool.MB_TYPE_LISTENING;
    },
    
    getType: function()
    {
        return Ametys.tool.Tool.TYPE_REPOSITORY;  
    },
    
    /**
     * Called when a script has been executed.
     * @param {Ametys.message.Message} message The bus message.
     * @private
     */
    _onScriptExecution: function(message)
    {
        this.addResult(message.getParameters().result);
    },
    
    /**
     * Add a script execution result to the panel.
     * @param {Object} result The script execution result. 
     */
    addResult: function(result)
    {
        var html = "";
        
        var duration = (result.end.getTime() - result.start.getTime()) / 1000.0;
        
        html += this._scriptDateTpl.apply ({date: Ext.Date.format(result.end, Ext.Date.patterns.ShortTime), duration: duration});
        
        if (!Ext.isEmpty(result.output))
        {
            html += '<pre>' + result.output + '</pre>';
        }
        if (!Ext.isEmpty(result.result))
        {
            html += Ext.JSON.prettyEncode(result.result, 0, function(value, offset) {
                if (Ext.isObject(value) && value.type == 'node' && value.path != null)
                {
                    return value.path;
                }
                if (Ext.isObject(value) && value.type == 'ametys-object' && value.path != null)
                {
                    return value.path + " [" + value.id + "]";
                }
            });
        }
        if (!Ext.isEmpty(result.error))
        {
            html += '<div class="error">' + result.error + '</div>';
        }
        if (Ext.isEmpty(result.output) && Ext.isEmpty(result.result) && Ext.isEmpty(result.error))
        {
            html += "<div class='no-result'>{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_RESULTS_NO_RESULT}}</div>";
        }
        
        html += '</div>';
        
        this.getContentPanel().update(html + this.getContentPanel().body.getHtml());
        this.getContentPanel().scrollTo(0, 0, true);
    }
    
});
