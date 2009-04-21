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
Ext.namespace('Ext.ametys');

/**
 * Ext.ametys.Form
 *
 * @class Ext.ametys.Form
 * @extends Ext.form.FormPanel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.Form = function(config) 
{
	Ext.ametys.Form.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.Form, Ext.form.FormPanel, 
{
	labelAlign: 'top',
    frame :true, // arrondi
    bodyStyle:'padding:5px 5px 0'
});


