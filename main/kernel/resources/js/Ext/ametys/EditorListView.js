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
 * org.ametys.EditorListView
 *
 * @class This class extends the EditorGridPanel to provide functions to simply add or remove records
 * @extends Ext.EditorGridPanel
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.EditorListView = function(config) 
{
	config.sm = new Ext.grid.CellSelectionModel({singleSelect: true});
	org.ametys.EditorListView.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.EditorListView, Ext.grid.EditorGridPanel, 
{
	iconCls: 'icon-list',
	cls: 'list-view',
	columnmove : false,
	frame: true // rendu avec bords arrondis
});

/**
 * Set the multiple selection to true or false
 * @param multiple true for multiple selection
 */
org.ametys.EditorListView.prototype.setMultipleSelection = function (multiple)
{
	this.getSelectionModel().singleSelect = !multiple;
}


/**
 * Add a new record in the grid
 * @param idElmt The record id. Can be null to be generated
 * @param elmtMap The record element in a map {columnid: value, columnid: value, ...}
 */
org.ametys.EditorListView.prototype.addElement = function (idElmt, elmtMap)
{
	var record = this.store.recordType;
	var newEntry = new record(elmtMap, idElmt);
	this.store.add([newEntry]);
}

/**
 * Remove an existing record in the grid by its position
 * @param position The element position in the grid
 */
org.ametys.EditorListView.prototype.removeElementByPosition = function (position)
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
org.ametys.EditorListView.prototype.removeElementById = function (id)
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
org.ametys.EditorListView.prototype.removeElement = function (elmt)
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
org.ametys.EditorListView.prototype.getSelection = function ()
{
	var selections = [];
	var record = this.getSelectionModel().selection.record;
	selections.push(record);
	
	return selections;
}

/**
 * Get the record in the list
 * @returns The elements in an Array
 */
org.ametys.EditorListView.prototype.getElements = function ()
{
	return this.getStore().data.items;
}

org.ametys.EditorListView.prototype.onRender = function(ct, position)
{
	org.ametys.EditorListView.superclass.onRender.call(this, ct, position);
	this.body.addClass(this.cls + "-body");
}
