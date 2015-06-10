/*
 *  Copyright 2013 Anyware Services
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
 * This factory creates Ametys.message.MessageTarget when the target is equals to its parameters (no need to compute anything).
 * This is the implementation for unknown message target type
 * 
 * Parameters of #createTargets can be anything.
 */
Ext.define("Ametys.message.factory.DefaultMessageTargetFactory",
	{
		extend: "Ametys.message.MessageTargetFactory",

		/**
		 * @inheritdoc
		 * @param {Object} parameters Can be anything but require type 
		 * @param {String} parameters.type The message target type
		 * @param {Function} callback The callback function called when the targets are created. Parameters are
		 * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
		 */
		createTargets: function(parameters, callback)
		{
			var target = Ext.create("Ametys.message.MessageTarget", {
				type: parameters.type,
				parameters: parameters
			});		

			callback([target]);
		}
	}
);
