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
 * org.ametys.DesktopPanel
 * @class This class provides a container for {@link org.ametys.DesktopItem}
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.DesktopPanel = function(config) 
{
	org.ametys.DesktopPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.DesktopPanel, Ext.Container, 
{
	cls: 'desktop',
	border: false,
	autoscroll: true
});

org.ametys.DesktopPanel.prototype.onRender = function(ct, position)
{
	org.ametys.DesktopPanel.superclass.onRender.call(this, ct, position);
}


