/*
 *  Copyright 2015 Anyware Services
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

/**
 * @private
 * This class controls a ribbon button representing the lock state of a content
 */
Ext.define('Ametys.plugins.coreui.profiles.controller.ViewModeController', {
	extend: 'Ametys.ribbon.element.ui.ButtonController',

	areSameTargets: function(target1, target2)
	{
		var areSameTargets = this.callParent(arguments);
		return areSameTargets && target1.getSubtarget(Ametys.message.MessageTarget.FORM) == target2.getSubtarget(Ametys.message.MessageTarget.FORM);
	},
	
	updateState: function()
	{
		this.enable();
		
		var targets = this.getMatchingTargets();
		if (targets.length > 0)
		{
			var profileTarget = targets[0];
			var formTarget = profileTarget.getSubtarget(Ametys.message.MessageTarget.FORM);
			
			this.toggle(formTarget != null);
		}
	}
});
