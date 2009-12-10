/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

Ext.namespace('org.ametys.runtime');

/**
  * This class allow a plugin to define a simple context path  
  * relative url by emulating a class around it
  */

org.ametys.runtime.Link = function (plugin, parameters)
{
  	var link = parameters['Link'];
  	var mode = parameters['Mode'];
  	
  	runtimeRedirectTo(link, mode, plugin);
}
