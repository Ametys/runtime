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
 * org.ametys.Tree
 *
 * @class This class simply extends the {@link Ext.tree.Panel}
 * @extends Ext.tree.TreePanel
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.Tree = function(config) 
{
	org.ametys.Tree.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.Tree, Ext.tree.TreePanel, {});


