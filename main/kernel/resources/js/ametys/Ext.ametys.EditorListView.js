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
Ext.namespace('Ext.awt');

/**
 * Ext.ametys.EditorListView
 *
 * @class Ext.ametys.EditorListView
 * @extends Ext.EditorGridPanel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.EditorListView = function(config) 
{
	config.sm = new Ext.grid.CellSelectionModel({singleSelect: true});
	Ext.ametys.EditorListView.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.EditorListView, Ext.grid.EditorGridPanel, 
{
	iconCls: 'icon-list',
	cls: 'list-view',
	columnmove : false,
	frame: true, // rendu avec bords arrondis
	/**
	 * Set the multiple selection to true or false
	 * @param multiple true for multiple selection
	 */
	setMultipleSelection : function (multiple)
	{
		this.getSelectionModel().singleSelect = !multiple;
	},
	/**
	 * Add a new entry in the <code>ListView</code>
	 * @param elmtMap The record element in a map {columnid: value, columnid: value, ...}
	 */
	addElement : function (elmtMap)
	{
		var record = this.store.recordType;
		var newEntry = new record(elmtMap);
		this.store.add([newEntry]);
	},
	/**
	 * Add a new entry in the <code>ListView</code>
	 * @param elmtMap The record element in a map {columnid: value, columnid: value, ...}
	 */
	addElement : function (idElmt, elmtMap)
	{
		var record = this.store.recordType;
		var newEntry = new record(elmtMap, idElmt);
		this.store.add([newEntry]);
	},
	/**
	 * Remove an existing entry in the <code>ListView</code> by its position
	 * @param position The element position in the list
	 */
	removeElementByPosition : function (position)
	{
		var elmt = this.store.getAt(position);
		if (elmt != null)
		{
			this.store.remove(elmt);
		}
	},
	/**
	 * Remove an existing element in the <code>ListView</code>
	 * @param id The element's id
	 */
	removeElementById : function (id)
	{
		var elmt = this.store.getById(id);
		if (elmt != null)
		{
			this.store.remove(elmt);
		}
	},
	/**
	 * Remove an element
	 * @param elmt The element to remove
	 */
	removeElement : function (elmt)
	{
		if (elmt != null)
		{
			this.store.remove(elmt);
		}
	},
	/**
	 * Get the selected elements in the list
	 * @returns The selected elements in an Array
	 */
	getSelection : function ()
	{
		var selections = [];
		var record = this.getSelectionModel().selection.record;
		selections.push(record);
		
		return selections;
	},
	/**
	 * Get the elements in the list
	 * @returns The elements in an Array
	 */
	getElements : function ()
	{
		return this.getStore().data.items;
	},
	onRender : function(ct, position)
	{
		Ext.ametys.EditorListView.superclass.onRender.call(this, ct, position);
		this.body.addClass(this.cls + "-body");
	}
});