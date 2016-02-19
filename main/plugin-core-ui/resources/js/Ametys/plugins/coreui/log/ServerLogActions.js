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
 * This class is a singleton to handle actions on the Server Log tool.
 * @private
 */
Ext.define('Ametys.plugins.coreui.log.ServerLogActions', {
    singleton: true,
    
    /**
     * Action to toggle the pause of the logs' updates
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function.
     */
    pause: function(controller)
    {
        var tool = Ametys.tool.ToolsManager.getFocusedTool();
        var pausing = !controller.isPressed();
        
        if (pausing)
        {
            tool.pauseUpdates(true);
            controller.toggle(true);
            controller.setAdditionalDescription(controller.getInitialConfig("description-play"));
            controller.setGlyphIcon(controller.getInitialConfig("icon-glyph-play"));
        }
        else
        {
            tool.pauseUpdates(false);
            controller.toggle(false);
            controller.setAdditionalDescription("");
            controller.setGlyphIcon(controller.getInitialConfig("icon-glyph"))
        }
    },
    
    /**
     * Action to clear the list of logs.
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function. 
     */
    clear: function(controller)
    {
        var tool = Ametys.tool.ToolsManager.getFocusedTool();
        tool.clearLogs();
    }
});
