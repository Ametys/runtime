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
 * This action provide a contextual text panel.<br/>Use the <code>addText</code> to add html text to this panel.
 */
Ext.define('Ametys.admin.rightpanel.TextPanel', {
	extend: 'Ext.panel.Panel',
	
	autoDestroy: false,
	collapsible: false,
	cls: 'admin-panel text-panel',
	bbar: [],
	border: false,
	titleAlign: 'right',
	header: {
		height: 40
	},
	
	/**
	 * Adds a text to this panel.  
	 * @param {String} text The html text
	 */
	addText: function (text)
	{
		this.add(Ext.Component({html: text,  border: false, cls: 'text-panel' }));
	}
});
