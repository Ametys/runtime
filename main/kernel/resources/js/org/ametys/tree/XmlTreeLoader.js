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
org.ametys.tree.XmlTreeLoader = Ext.extend(Ext.ux.tree.XmlTreeLoader, {});

org.ametys.tree.XmlTreeLoader.prototype.processAttributes = function(attr, node)
{
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
