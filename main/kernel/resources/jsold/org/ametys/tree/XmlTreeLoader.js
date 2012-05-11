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

Ext.namespace('org.ametys.tree');

/**
 * @class org.ametys.web.XmlTreeLoader
 * @extends Ext.ux.tree.XmlTreeLoader
 */
org.ametys.tree.XmlTreeLoader = function (config)
{
	if (config.baseParams == null)
	{
		config.baseParams = {};
	}
	
	if (context.parameters)
	{
		for (var i in context.parameters)
		{
			config.baseParams[i] = encodeURIComponent(context.parameters[i]);
		}
	}

	org.ametys.tree.XmlTreeLoader.superclass.constructor.call(this, config);
}

Ext.extend(org.ametys.tree.XmlTreeLoader, Ext.ux.tree.XmlTreeLoader, {});

org.ametys.tree.XmlTreeLoader.prototype._nodeTags;

org.ametys.tree.XmlTreeLoader.prototype.createNode = function(node)
{
	this._currentXMLNode = node;
	
	var treeNode = org.ametys.tree.XmlTreeLoader.superclass.createNode.call(this, node);
	
	delete this._currentXMLNode;
	
	return treeNode;
};

org.ametys.tree.XmlTreeLoader.prototype.processAttributes = function(attr)
{
	var node = this._currentXMLNode;
	
	Ext.each(node.childNodes, 
		function(n) 
		{
			if (this._nodeTags != null && this._nodeTags[n.tagName] == null)
			{
				if (n.childNodes[0] && n.childNodes[0].nodeValue)
				{
					attr[n.nodeName] = n.childNodes[0].nodeValue.trim();
				}
			}
		}, 
		this);
}

org.ametys.tree.XmlTreeLoader.prototype.parseXml = function(node) 
{
	var nodes = [];
	
	Ext.each(
		node.childNodes, 
		function(n) 
		{
			if (n.nodeType == this.XML_NODE_ELEMENT && (this._nodeTags == null || (this._nodeTags != null && this._nodeTags[n.tagName] != null))) 
			{
				var treeNode = this.createNode(n);
				if (n.childNodes.length > 0) 
				{
					var child = this.parseXml(n);
					if (typeof child == 'string') 
					{
						treeNode.attributes.innerText = child;
					}
					else 
					{
						treeNode.appendChild(child);
					}
				}
				nodes.push(treeNode);
			} 
			else if (n.nodeType == this.XML_NODE_TEXT) 
			{
				var text = n.nodeValue.trim();
				if (text.length > 0) 
				{
					return nodes = text;
				}
			}
		}, 
		this);

	return nodes;
}
