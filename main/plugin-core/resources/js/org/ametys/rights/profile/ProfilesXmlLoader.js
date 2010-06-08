/*
 *  Copyright 2010 Anyware Services
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

// Ametys Namespace
Ext.namespace('org.ametys.rights.profile');

org.ametys.rights.profile.ProfilesXmlLoader = Ext.extend(org.ametys.tree.XmlTreeLoader, {});

org.ametys.rights.profile.ProfilesXmlLoader.prototype.processResponse = function(response, node, callback) 
{
	this._nodeTags = {};
	this._nodeTags['profile'] = '';
	this._nodeTags['user'] = '';
	this._nodeTags['group'] = '';
	
	org.ametys.rights.profile.ProfilesXmlLoader.superclass.processResponse.call(this, response, node, callback);
}

org.ametys.rights.profile.ProfilesXmlLoader.prototype.processAttributes = function(attr)
{
	org.ametys.rights.profile.ProfilesXmlLoader.superclass.processAttributes.call(this, attr);
	
	if (attr.tagName == "profile")
    { 
        attr.text = " <b>" + attr.label + "</b>";
        attr.type = "profile";
        attr.leafSort = false; // attribute for sorting
        attr.icon = getPluginResourcesUrl('core') + '/img/profiles/profile_16.png';

        if (this.syncMode)
        {
       	 // Override these values for our folder nodes because we are loading all data at once.  If we were
            // loading each node asynchronously (the default) we would not want to do this:
            attr.expanded = true;
        	attr.loaded = true;
        }
    }
    else if (attr.tagName == "user")
    { 
        attr.text = ' ' + attr.name + ' (' + attr.login + ')';
        attr.type = "user";
        attr.leafSort = true; // attribute for sorting
        attr.icon = getPluginResourcesUrl('core') + '/img/profiles/user_16.png';
        
        if (attr.inherit == 'true')
        {
        	attr.cls = 'inherit';
        	// attr.disabled = true;
        }
        
        // Tell the tree this is a leaf node.  This could also be passed as an attribute in the original XML,
        // but this example demonstrates that you can control this even when you cannot dictate the format of
        // the incoming source XML:
        attr.leaf = true;
    }
    else if (attr.tagName == "group")
    { 
        attr.text = ' ' + attr.label;
        attr.type = "group";
        attr.leafSort = true; // attribute for sorting
        attr.icon = getPluginResourcesUrl('core') + '/img/profiles/group_16.png';
        
        if (attr.inherit == 'true')
        {
        	attr.cls = 'inherit';
        	//attr.disabled = true;
        }
        
        // Tell the tree this is a leaf node.  This could also be passed as an attribute in the original XML,
        // but this example demonstrates that you can control this even when you cannot dictate the format of
        // the incoming source XML:
        attr.leaf = true;
    }
}

org.ametys.rights.profile.ProfilesXmlLoader.prototype.createNode = function(node) 
{
	var attr = {
		tagName :node.tagName
	};

	Ext.each(node.attributes, function(a) {
		attr[a.nodeName] = a.nodeValue;
	});
	
	this._currentXMLNode = node;
	this.processAttributes(attr);
	delete this._currentXMLNode;
	
	if (this.baseAttrs)
    {
        Ext.applyIf(attr, this.baseAttrs);
    }
    if (this.applyLoader !== false)
    {
        attr.loader = this;
    }
    if (typeof attr.uiProvider == 'string')
    {
       attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
    }
    
    if (attr.tagName == "profile")
    {
    	return new Ext.tree.AsyncTreeNode(attr);
    }
    else
    {
    	return new Ext.tree.TreeNode(attr);
    }
}
