/*
 *  Copyright 2016 Anyware Services
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
                form.mask('{{i18n PLUGINS_CORE_UI_LOADMASK_TESTS_RUNNING}}');
                
                var activeFieldCheckers = form._fieldCheckersManager._fieldCheckers.filter(function (el) { return el.isActive; });
                form._fieldCheckersManager.check(activeFieldCheckers, true, function(){form.unmask();}, mode == 'all');
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
	},
	
	updateState: function()
	{
		var formTarget = this.getMatchingTargets()[0];
		var testResults = formTarget.getParameters()['test-results'];
		if (!Ext.Object.isEmpty(testResults))
		{
			this._updateDescriptions(testResults);
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
		var testsRunning = testResults.running > 0;
		if (testsRunning)
		{
			this.refreshing();
		}
		else
		{
			var html = [],
			tpl = new Ext.Template(
					"<div class='test-results'>" + 
					"<span>{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_TEXT}}</span>" +
						'<ul>' + 
							"<li> {successes} {{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_SUCCESSES}}</li>" +
							"<li> {failures} {{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_FAILURES}}</li>" +
							"<li> {notTested} {{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_TEST_CONTROLLER_RESULTS_NOT_TESTED}}</li>" +
						'</ul>' +
					"</div>"
			);
			
			var noFailures = testResults.failures + testResults.notTested == 0;
			var noTests = testResults.successes + testResults.failures + testResults.notTested == 0;
			
			if (this._refreshing)
			{
				this.stopRefreshing();
			}
			
			if (this._mode == 'missed')
			{
				this.setIconDecorator (noFailures ? 'decorator-ametysicon-checked34' : 'decorator-ametysicon-alert9');
			}
			
			this.setDisabled(this._mode == 'all' ? noTests : noFailures);
			this.setDescription(tpl.applyOut(testResults, html)[0]);
		}
	}
});