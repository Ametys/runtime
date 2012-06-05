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
 * 
 */
Ext.define('Ametys.workspace.admin.NavigationPanel', {
	extend: 'Ext.panel.Panel',
	
	autoDestroy: false,
	collapsible: false,
	cls: 'navigation-panel',
	border: false,
	titleAlign: 'right',
	bbar: [],
	header: {
		height: 40
	},
	navitems : [],
	
	/**
	 * Adds an item of navigation to this panel (see {@link Ametys.workspace.admin.NavigationItem}).  
	 * @param {String} text The text of the item
	 * @param {Function} act The function to call on click event
	 */
	addItems: function (text, act) 
	{ 
		var span = document.createElement("span");
		
		var link = document.createElement("a");
	    link.innerHTML = text;
	    link.href = "#";
	    link.className = "link"
	    span.appendChild (link);

	    var navitem = new Ametys.workspace.admin.NavigationItem ({ 
			border: false,
			html : span.innerHTML,
			listeners: {"click" : act}
		});
	    
		this.add(navitem);
		this.navitems.push(navitem);
	}
});
