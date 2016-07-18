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
 * Tool showing the script text input to type into.
 */
Ext.define('Ametys.plugins.coreui.script.ScriptTool',
{
    extend: 'Ametys.tool.Tool',
    
    /**
     * The number of lines in the editor footer, currently forced at 3. 
     * @private
     */
    _footerLineCount: 3,
    
    createPanel: function()
    {
        this._helpTooltipBtn = Ext.create("Ext.Component", {
            cls: 'uitool-script-help-tooltip ametysicon-question13'
        });
        
        this._initConsoleTooltip();
        
        var footerLine1 = "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_FOOTER_HINT_LINE1}}".replace(/\r\n|\r|\n/, "");
        var footerLine2 = "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_FOOTER_HINT_LINE2}}".replace(/\r\n|\r|\n/, "");
        
        this.scriptEditor = Ext.create('Ametys.form.field.Code', {
            mode: 'javascript',
            stateful: true,
            stateId: this.getId() + "$code",
            cls: "uitool-admin-script-editor",
            
            value: "function main() { \n  \n  \n  \n  \n} \n" + footerLine1 + "\n" + footerLine2,
            
            cmParams: {
                extraKeys: {
                    "Ctrl-A": Ext.bind(this._selectAll, this),
                    "Cmd-A": Ext.bind(this._selectAll, this)
                }
            },

            listeners: {
                beforechange: {
                    fn: function (cm, change) {
                        if (change.from.line == 0 || change.to.line >= cm.doc.lineCount() - this._footerLineCount)
                        {
                            change.cancel();
                        }
                    },
                    scope: this
                }
            }
        });
        
        this._panel = Ext.create('Ext.panel.Panel', {
            layout: 'fit',
            scrollable: false,
            cls: 'uitool-script',
            
            items: this.scriptEditor,
            
            listeners: {
                'afterrender': Ext.bind(function (panel) {
                    var cmpId = panel.getId() + '-' + Ext.id();
                    
                    panel.getEl().insertFirst({
                        id: cmpId,
                        tag: 'div', 
                        cls: "uitool-script-help-tooltip-container"
                    });
                    
                    this._helpTooltipBtn.render(cmpId);
                }, this)
            }
        });
        
        return this._panel;
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
    },
    
    getType: function()
    {
        return Ametys.tool.Tool.TYPE_REPOSITORY;  
    },
    
    /**
     * Initialize the console help tooltip
     * @private
     */
    _initConsoleTooltip: function()
    {
        Ametys.data.ServerComm.callMethod({
            role: 'org.ametys.plugins.core.ui.script.ScriptHandler',
            methodName: 'getScriptBindingDescription',
            callback: {
                scope: this,
                handler: this._initConsoleTooltipCb
            },
            errorMessage: {
                msg: "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_SCRIPT_HINT_ERROR}}"
            }
        });
    },
    
    /**
     * Callback after retrieving the list of variables and functions available to the console
     * @private 
     */
    _initConsoleTooltipCb: function(result)
    {
        var tpl = Ext.create('Ext.XTemplate', 
            "<p>{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_SCRIPT_HINT}}</p>",
            "<tpl if='variables'>",
                "<p>{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_SCRIPT_HINT_VARIABLES}}<ul>",
                "<tpl foreach='variables'>",
                    "<li><b>{$}</b> : {.}</li>",
                "</tpl></ul></p>",
            "</tpl>",
            "<tpl if='functions'>",
                "<p>{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_SCRIPT_HINT_FUNCTIONS}}<ul>",
                "<tpl foreach='functions'>",
                    "<li><b>{$}</b> : {.}</li>",
                "</tpl></ul></p>",
            "</tpl>",
            "<p>{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_SCRIPT_HINT_ADDITIONAL_SCRIPT}}</p>");
        
        Ext.create('Ext.tip.ToolTip', {
            target: this._helpTooltipBtn.getId(),
            title: "{{i18n PLUGINS_CORE_UI_TOOLS_SCRIPT_HELP_TITLE}}",
            html: tpl.applyTemplate(result),
            cls: 'x-fluent-tooltip',
            showDelay: 0,
            dismissDelay: 0
        });
    },
    
    /**
     * Called by the key binding Ctrl-A, overrides the default selection to only the editable lines.
     * @private
     */
    _selectAll: function(cm)
    {
        var lastLine = cm.doc.lineCount() - this._footerLineCount - 1;
        var lastLineLength = cm.doc.getLine(lastLine).length;
        cm.setSelection({line: 1, ch: 0}, {line: lastLine, ch: lastLineLength}, {bias: 1});
    }
});
