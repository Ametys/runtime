/*
 *  Copyright 2012 Anyware Services
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
 * This panel displays a full screen with the list of supported browsers.
 * 	
 */
Ext.define('Ametys.public.UnsupportedBrowserScreen', {
    extend: 'Ametys.public.RedirectActionScreen',
    
    /**
     * @cfg {Object} supported The supported browsers with their supported versions
     */
    supported: '',
    
    /**
     * @cfg {String} contextPath The context path
     */
    contextPath: '',
    
    /**
     * @property {Ext.Template} _browsersTpl Global template to display supported browsers 
     * @private
     */
    _browsersTpl: Ext.create('Ext.Template', '<div class="browsers">{browsers}</div>'),
    
    /**
     * @property {Ext.Template} _browserTpl Template to display a supported browser
     * @private
     */
    _browserTpl: Ext.create('Ext.Template', 
    		'<div class="browser">',
    		 	'<div class="browser-name">',
    		 		'<img src="{icon}"/>{name}',
    		 	'</div>',
    		 	'<div class="browser-version">{versions}</div>',
    		 	'<div class="browser-end"></div>',
    		 '</div>'
    ),
    
    getDescription: function ()
    {
    	var html = '<div>' + this.description + '</div>';
    	html += this._browsersTpl.apply({browsers: this._getSupportedBrowsers()});
    	
    	return {
	    	xtype : "label",
	    	cls : "ametys-public-page-desc",
	    	html : html
	    };
    },
    
    /**
     * @private
     * Get the HTML string for supported browsers
     * @return {String} formatted supported browsers
     */
    _getSupportedBrowsers: function ()
    {
    	var html = '';
    	
    	var me = this;
    	Ext.Object.each(this.supported, function (browser, version) {
    		
    		var data = {
    			name: me._getBrowserName(browser),
    			icon: me.contextPath + "/plugins/core-ui/resources/img/browsers/" + me._getBrowserIcon(browser),
    			versions: me._getBrowserVersions(version)
    		}
    		html += me._browserTpl.apply(data)
    	});

		return html;
    },
    
    /**
     * @private
     * Get the browser full name from its code
     * @param {String} code The browser's code
     * @return the browser name
     */
    _getBrowserName: function (code)
    {
    	switch (code) {
	    	case 'ff':
	    		return 'Mozilla Firefox';
	    	case 'ch':
				return 'Google Chrome';
	    	case 'ie':
				return 'Internet Explorer';
			case 'op':
				return 'Opera';
			case 'sa':
				return 'Apple Safari';
			default:
				return "<i18n:text i18n:key='PLUGINS_CORE_UI_SUPPORTED_BROWSER_UNKNOWN'/>";
				break;
		}
    },
    
    /**
     * @private
     * Get the icon from its code
     * @param {String} code The browser's code
     * @return the browser icon name
     */
    _getBrowserIcon: function (code)
    {
    	switch (code) {
	    	case 'ff':
	    		return 'firefox_48.png';
	    	case 'ch':
				return 'chrome_48.png';
	    	case 'ie':
				return 'ie_48.png';
			case 'op':
				return 'opera_48.png';
			case 'sa':
				return 'safari_48.png';
			default:
				return '';
				break;
		}
    },
    
    /**
     * @private
     * Get supported versions as a user friendly text
     * @param {String} versions The supported browser's versions
     * @return supported versions as a user friendly text
     */
    _getBrowserVersions: function (versions)
    {
    	if (version == '0-0')
    	{
    		return "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ALL' i18n:catalogue='plugin.core-ui'/>";
    	}
    	else if (Ext.String.startsWith(version, '0-'))
    	{
    		return "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS' i18n:catalogue='plugin.core-ui'/>" + version.substring('0-'.length) + "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_LOWER' i18n:catalogue='plugin.core-ui'/>";
    	}
    	else if (Ext.String.endsWith(version, '-0'))
    	{
    		var i = version.indexOf('-0');
    		return "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS' i18n:catalogue='plugin.core-ui'/>" + version.substring(0, i) + "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ABOVE' i18n:catalogue='plugin.core-ui'/>";
    	}
    	else 
    	{
    		var i = version.indexOf('-');
    		var after = version.substring(0, i);
    		var upTo = version.substring(i + 1);
    		return "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS' i18n:catalogue='plugin.core-ui'/>" + after + "<i18n:key i18n:text='PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_TO' i18n:catalogue='plugin.core-ui'/>" + upTo; 
    	}
    	
    }
});

