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
 * Ext.ametys.ListView
 *
 * @class Ext.ametys.ListView
 * @extends Ext.GridPanel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.ListView = function(config) 
{
	config.sm = new Ext.grid.RowSelectionModel({singleSelect: true});
	Ext.ametys.ListView.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.ListView, Ext.grid.GridPanel, 
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
	 * @param idElmt The element id. Can be null to be generated
	 * @param elmtMap The record element in a map {columnid: value, columnid: value, ...}
	 */
	addElement : function (idElmt, elmtMap)
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
		var map = this.getSelectionModel().selections.map;
		for (i in map)
		{
			selections.push(map[i]);
		}
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
		Ext.ametys.ListView.superclass.onRender.call(this, ct, position);
		this.body.addClass(this.cls + "-body");
		if (this.extraCls)
		{
			this.body.addClass(this.extraCls + "-body");
		}
	}
});