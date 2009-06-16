<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
// --------------------------------
function RUNTIME_Plugin_Runtime_SelectUserCallBack (users, sl)
{
	function seek (arr, id)
	{
		for (var i=0; i&lt;arr.length; i++)
		{
			if (arr[i].properties.id == id)
				return arr[i];
		}
		return null;
	}

	var selectedElements = new Array();
	var existingElements = sl.getElements();
	
	for (var i in users)
	{
		var e = seek(existingElements, i);
		
		if (e == null)
			e = sl.addElement(users[i], getPluginResourcesUrl(applicationPlugin) + "/img/rights/users_small.gif", getPluginResourcesUrl(applicationPlugin) + "/img/rights/users_medium.gif", getPluginResourcesUrl(applicationPlugin) + "/img/rights/users_large.gif", {"id": i, "type": "user"});
		selectedElements.push(e);
	}
	
	sl.paint();
	sl.unselect();
	for (var i=0; i &lt; selectedElements.length; i++)
	{
		selectedElements[i].select(true);
	}
}

// --------------------------------
function RUNTIME_Plugin_Runtime_SelectGroupCallBack (groups, sl)
{
	function seek (arr, id)
	{
		for (var i=0; i&lt;arr.length; i++)
		{
			if (arr[i].properties.id == id)
				return arr[i];
		}
		return null;
	}

	var selectedElements = new Array();
	var existingElements = sl.getElements();
	
	for (var i in groups)
	{
		var e = seek(existingElements, i);
		
		if (e == null)
			e = sl.addElement(groups[i], getPluginResourcesUrl(applicationPlugin) + "/img/rights/groups_small.gif", getPluginResourcesUrl(applicationPlugin) + "/img/rights/groups_medium.gif", getPluginResourcesUrl(applicationPlugin) + "/img/rights/groups_large.gif", {"id": i, "type": "group"});
		selectedElements.push(e);
	}
	
	sl.paint();
	sl.unselect();
	for (var i=0; i &lt; selectedElements.length; i++)
	{
		selectedElements[i].select(true);
	}
}
