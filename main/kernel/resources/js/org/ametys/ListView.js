/*
 *  Copyright 2009 Anyware Services
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

// Ametys Namespace
Ext.namespace('org.ametys');

/**
 * org.ametys.ListView
 *
 * @class This class extends the GridPanel to provide functions to simply add or remove records
 * @extends Ext.GridPanel
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.ListView = function(config) 
{
	config.sm = new Ext.grid.RowSelectionModel({singleSelect: true});
	org.ametys.ListView.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.ListView, Ext.grid.GridPanel, 
{
	cls: 'list-view',
	columnmove : false,
	frame: true // rendu avec bords arrondis
});

/**
 * Set the multiple selection to true or false
 * @param multiple true for multiple selection
 */
org.ametys.ListView.prototype.setMultipleSelection = function (multiple)
{
	this.getSelectionModel().singleSelect = !multiple;
}

/**
 * Add a new record in the grid
 * @param idElmt The record id. Can be null to be generated
 * @param elmtMap The record element in a map {columnid: value, columnid: value, ...}
 * @param sorted true to insert element basing on the current sort information
 */
org.ametys.ListView.prototype.addElement = function (idElmt, elmtMap, sorted)
{
	var record = this.store.recordType;
	var newEntry;
	if (idElmt == null)
	{
		newEntry = new record(elmtMap);
	}
	else
	{
		newEntry = new record(elmtMap, idElmt);
	}
	
	if (sorted)
	{
		this.store.addSorted(newEntry);
	}
	else
	{
		this.store.add([newEntry]);
	}
}

/**
 * Remove an existing record in the grid by its position
 * @param position The element position in the grid
 */
org.ametys.ListView.prototype.removeElementByPosition = function (position)
{
	var elmt = this.store.getAt(position);
	if (elmt != null)
	{
		this.store.remove(elmt);
	}
}

/**
 * Remove an existing element in the grid
 * @param id The element's id
 */
org.ametys.ListView.prototype.removeElementById = function (id)
{
	var elmt = this.store.getById(id);
	if (elmt != null)
	{
		this.store.remove(elmt);
	}
}

/**
 * Remove an element
 * @param elmt The element to remove
 */
org.ametys.ListView.prototype.removeElement = function (elmt)
{
	if (elmt != null)
	{
		this.store.remove(elmt);
	}
}

/**
 * Get the selected elements in the list
 * @returns The selected elements in an Array
 */
org.ametys.ListView.prototype.getSelection = function ()
{
	var selections = [];
	var map = this.getSelectionModel().selections.map;
	for (i in map)
	{
		selections.push(map[i]);
	}
	return selections;
}


/**
 * Get the record in the list
 * @returns The elements in an Array
 */
org.ametys.ListView.prototype.getElements = function ()
{
	return this.getStore().data.items;
}

org.ametys.ListView.prototype.onRender = function(ct, position)
{
	org.ametys.ListView.superclass.onRender.call(this, ct, position);
	this.body.addClass(this.cls + "-body");
	if (this.extraCls)
	{
		this.body.addClass(this.extraCls + "-body");
	}
}
