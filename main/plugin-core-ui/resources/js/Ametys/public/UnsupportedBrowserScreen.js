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
 * This panel display a full screen for an action of redirection.
 * This is an example of use:
 * 
 * 
 * 		Ext.create('Ametys.public.RedirectActionScreen', {
 *             text: "My action title",
 *             description: "My action description",
 *             
 *             image: "/my/action/image.png",
 *             
 *             btnText: "Action",
 *             redirectUrl: "/my/action/url"
 *       });
 * 	
 */
Ext.define('Ametys.public.UnsupportedBrowserScreen', {
    extend: 'Ametys.public.RedirectActionScreen',
    
    /**
     * @cfg {Object} supported The supported browser
     */
    supported: '',
    
    
    contextPath: '',
    
    _browsersTpl: Ext.create('Ext.Template', '<div class="browsers">{browsers}</div>'),
    
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
    	html += this._browsersTpl.apply({browsers: this.getSupportedBrowser()});
    	
    	return {
	    	hidden: Ext.isEmpty(this.description),
	    	xtype : "label",
	    	cls : "ametys-public-page-desc",
	    	html : html
	    };
    },
    
    getSupportedBrowser: function ()
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
				return 'Unknown';
				break;
		}
    },
    
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
				return 'Unknown';
				break;
		}
    },
    
    _getBrowserVersions: function (version)
    {
    	if (version == '0-0')
    	{
    		return "<i18n:key i18n:text='WORKSPACE_ADMIN_NAVIGATOR_VERSION_ALL'/>";
    	}
    	else if (Ext.String.startsWith(version, '0-'))
    	{
    		return "Version " + version.substring('0-'.length) + " et inférieures";
    	}
    	else if (Ext.String.endsWith(version, '-0'))
    	{
    		var i = version.indexOf('-0');
    		return "Version " + version.substring(0, i) + " et supérieures";
    	}
    	else 
    	{
    		var i = version.indexOf('-');
    		var after = version.substring(0, i);
    		var upTo = version.substring(i + 1);
    		return "Versions " + after + " à " + upTo; 
    	}
    	
    }
});

