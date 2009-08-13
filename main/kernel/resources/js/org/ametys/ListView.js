/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
 */
org.ametys.ListView.prototype.addElement = function (idElmt, elmtMap)
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
	this.store.add([newEntry]);
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
