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
 * The controller launching tests on a configurable form panel
 * @private
 */
Ext.define('Ametys.plugins.coreui.configurableformpanel.TestsController', {
	extend: 'Ametys.ribbon.element.ui.ButtonController',
	
    statics: {
        /**
         * Check the elements attached to a parameter checker
         * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
         */
        check: function(controller)
        {
            var target = controller.getMatchingTargets()[0];
            if (target != null)
            {
                var mode = controller.getInitialConfig().mode;
                var form = target.getParameters().object.owner;
                
                var activeParamCheckers = form._paramCheckersDAO._paramCheckers.filter(function (el) { return el.isActive; });
                form._paramCheckersDAO.check(activeParamCheckers, true, Ext.emptyFn , mode == 'all');
            }
        }
    },
    
	/**
	 * @property {String} _mode the check mode (can be "all" or "missed")
	 * @private
	 */
	constructor: function(config)
	{
		this.callParent(arguments);
		this._mode = config.mode;
		
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
	},
	
	/**
	 * Listener when the test results have been modified
	 * Will update the state of the button
	 * @param {Ametys.message.Message} message The modified message.
	 * @protected
	 */
	_onModified: function(message)
	{
		var targets = message.getTargets();
		for (var i=0; i < targets.length; i++)
		{
			var target = targets[i];
			if (target.getType() == Ametys.message.MessageTarget.CONFIGURATION)
			{
				var subtarget = target.getSubtargets()[0]; 
				if (subtarget.getType() == Ametys.message.MessageTarget.FORM)
				{
					this._updateDescriptions(subtarget.getParameters()['test-results']);
					return;
				}
			}
		}
	},
	
	/**
	 * @private
	 * Updates the values inside the tooltips
	 * @param {Object} testResults the results of the tests
	 * @param {Number} testResults.notTested the number of non-performed tests
	 * @param {Number} testResults.successes the number of successful tests
	 * @param {Number} testResults.failures the number of failed tests
	 */
	_updateDescriptions: function(testResults)
	{
		var html = [],
			tpl = new Ext.Template(
				"<div class='test-results'>" + 
					"<span><i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_TEXT'/></span>" +
					'<ul>' + 
						"<li> {successes} <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_SUCCESSES'/></li>" +
						"<li> {failures} <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_FAILURES'/></li>" +
						"<li> {notTested} <i18n:text i18n:key='PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_NOT_TESTED'/></li>" +
					'</ul>' +
				"</div>"
			);
		
		this.setDescription(tpl.applyOut(testResults, html)[0]);
		this.setDisabled(this._mode == 'all' ? testResults.successes + testResults.failures + testResults.notTested == 0 : testResults.failures + testResults.notTested == 0);
	}
});