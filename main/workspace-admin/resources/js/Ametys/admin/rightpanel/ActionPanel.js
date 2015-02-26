/*
 *  Copyright 2012 Anyware Services
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
 * This action provides a contextual panel for link actions.<br/>Use the <code>addAction</code> to add a new link action to this panel.
 */

Ext.define('Ametys.admin.rightpanel.ActionPanel', {
	extend: 'Ametys.admin.rightpanel.TextPanel', 
		
	autoDestroy: false,
	collapsible: false,
	cls: 'admin-panel action-panel',
	
	/** @ignore */
	border: false,
	titleAlign: 'right',
	bbar: [],
	header: {
		height: 40
	},
	
	/**
	 * Adds an action (see <code>org.ametys.Action</code> to this panel.  
	 * @param {String} text The text of the action
	 * @param {String} desc The description of the action
	 * @param {String} icon The absolute url of the icon. Can be null
	 * @param {Function} act The function to be called on click event 
	 */
	addAction: function (text, desc, icon, act) 
	{
		var span = document.createElement("span");
		if (icon)
	    {
			var image = document.createElement("img");
			image.src = icon;
			image.className = "icon";
	        span.appendChild (image);
	    }
		
		var tooltip = null;
		if (tooltip)
		{
			var toolTipTpl = new Ext.XTemplate(
				'<tpl for=".">',
					 '<div class="action-tooltip">',
					 	'<div class="action-title">{text}</div>',
					 	'<div class="action-desc">{desc}</div>',
					 '</div>',
				'</tpl>'
			);
			tooltip = toolTipTpl.apply({text: text, desc: desc, icon: icon});
		}

		var link = document.createElement("a");
	    link.innerHTML = text;
	    link.href = "javascript: void(0)";
	    link.className = "link"
	    span.appendChild (link);

	    var action = new Ametys.admin.rightpanel.ActionPanel.Action ({ 
			border: false,
			html : span.innerHTML,
			tooltip: tooltip,
			listeners: {"click" : act},
			icon : icon,
			iconOver : icon.substring(0, icon.indexOf('.')) +  '-over.png'
		});
	    
		return this.add(action);
	},
	
	/**
	 * Hide the action to the position argument
	 * @param position The position of the action to hide in the panel
	 */
	hideElt: function (position) 
	{ 
		var act = this.items.get(position);
		if(act != null)
		{
			act.setVisible(false);
		}
	},

	/**
	 * Show the action to the position argument
	 *  @param position The position of the action to show in the panel
	 */
	showElt: function (position) 
	{ 
		var act = this.items.get(position);
		if(act != null)
		{
			act.setVisible(true);
		}
	}
});
