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

// Ametys Namespace
Ext.namespace('org.ametys');

/**
 * org.ametys.DesktopCategory
 *
 * @class This class represents a category of {@link org.ametys.DesktopItem}
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.DesktopCategory = function(config) 
{
	org.ametys.DesktopCategory.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.DesktopCategory, Ext.BoxComponent, 
{
	cls: 'desktop-category-title',
	border: false
});

org.ametys.DesktopCategory.prototype.onRender = function(ct, position)
{
	org.ametys.DesktopCategory.superclass.onRender.call(this, ct, position);
	
	if (this.text)
	{
		this.el.update(this.text);
	}
}