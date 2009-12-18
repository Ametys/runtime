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
