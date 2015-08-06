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
 * Field that displays a textarea field with a character counter bar 
 */
 Ext.define('Ametys.form.field.TextArea', {
 	extend: 'Ext.form.field.TextArea',
 	alias: ['widget.ametystextarea'],
    
 	constructor: function(config)
 	{
        // The layout is made in CSS only and me need to override the counter and the wraper with a div so CSS calculations are fine (display: table-cell does not seems to be a good parent).
        config.beforeBodyEl = "<div>";
        config.afterBodyEl = "</div>";
        
 		config.afterSubTpl = config.afterSubTpl || Ext.create('Ext.XTemplate', [
 			'<div id="{id}-counter" style="padding: 2px; font-size: 0.8em; background-color: #CCCCCC; clear: both;">',
	 			'<div style="padding-left: 5px; padding-right: 5px; color: #222222;">',
	 				'<i18n:text i18n:key="PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_1"/> ',
	 				'<span id="{id}-counter-val">0</span>',
	 				(config.maxLength == null ? '' : (' <i18n:text i18n:key="PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_2"/> ' + config.maxLength)),
	 			'</div>',
 			'</div>'
 		]);
        
 		this.callParent(arguments);
        
 		this.on({
 			'change': this._updateCharCounter,
 			'keyup': this._updateCharCounter
 		});
 		
 	},
    
    /**
     * @private
     * Updates the char counter under the textarea field.
     */
    _updateCharCounter: function()
    {
        var count = this.getValue().length;
        
        var counter = document.getElementById(this.id + "-counter-val"); 
        if (counter != null)
        {
            counter.innerHTML = count;
        }
        
        // is there a maxlength ?
        if (this.maxLength != Number.MAX_VALUE)
        {
            if (count > this.maxLength)
            {
                counter.style.color = "#ff0000";
            }
            else
            {
                counter.style.color = "";
            }
        }
    }
 	
 });