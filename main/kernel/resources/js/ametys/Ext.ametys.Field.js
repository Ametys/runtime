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
 * Ext.ametys.Field
 *
 * @class Ext.ametys.Field
 * @extends Ext.form.Field
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.Field = function(config) 
{
	Ext.ametys.Field.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.Field, Ext.form.Field, 
{
	msgTarget: 'side'
});


