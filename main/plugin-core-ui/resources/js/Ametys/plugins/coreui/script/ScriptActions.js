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
 * Actions on the ConsoleTool and ConsoleResultTool
 * @private
 */
Ext.define('Ametys.plugins.coreui.script.ScriptActions',
{
    singleton: true,
    
    /**
     * Handles the execute script button ("play").
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The button controller.
     */
    executeScript: function(controller)
    {
        var tool = Ametys.tool.ToolsManager.getFocusedTool();
        if (tool != null)
        {
            var script = tool.scriptEditor.getValue();
            
            Ametys.data.ServerComm.callMethod({
                role: 'org.ametys.plugins.core.ui.script.ScriptHandler',
                methodName: 'executeScript',
                parameters: [script],
                callback: {
                    handler: this._executeScriptCb,
                    scope: this
                },
                waitMessage: "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_WAITING_MESSAGE}}",
                errorMessage: "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_EXECUTE_ERROR}}"
            });
        }
    },
    
    /**
     * Callback fired when the script execution has finished: update the result panels.
     * @param {Object} result The script execution result.
     * @param {Array} args The callback arguments.
     * @private
     */
    _executeScriptCb: function(result, args)
    {
        Ametys.tool.ToolsManager.openTool('uitool-admin-console');
        
        Ext.create('Ametys.message.Message', {
            type: 'console-script-executed',
            parameters: {
                result: {
                    'output': result.output,
                    'result': result.result,
                    'error': result.error,
                    'start': Ext.Date.parse(result.start, Ext.Date.patterns.ISO8601DateTime),
                    'end': Ext.Date.parse(result.end, Ext.Date.patterns.ISO8601DateTime)
                }
            }
        });
    },
    
    /**
     * Handles the clear result button.
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The button controller.
     */
    clearResults: function(controller)
    {
        var tool = Ametys.tool.ToolsManager.getTool("uitool-admin-console");
        if (tool != null)
        {
            tool.getContentPanel().update('');
        }
    }
    
});
