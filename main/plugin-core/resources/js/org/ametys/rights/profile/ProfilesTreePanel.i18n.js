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

Ext.namespace('org.ametys.rights.profile');

org.ametys.rights.profile.ProfilesTreePanel = function(config) 
{
	this._context = config.context;
	
	var loader = new org.ametys.rights.profile.ProfilesXmlLoader ({
        dataUrl: getPluginDirectUrl('core') + '/rights/profiles-context.xml',
        syncMode: true // synchronous mode for filters
    });
	loader.addListener('beforeload', this._onBeforeLoad, this);
	loader.addListener('load', this._onLoad, this);
    
	config.loader = loader;
	
	config.rootVisible = false,
	config.root = new Ext.tree.AsyncTreeNode({
    	id: config.rootID,
    	text: ' &lt;b&gt;' + "<i18n:text i18n:key="PROFILES_TREEPANEL_ROOT_NODE"/>" + '&lt;/b&gt;',
    	icon: getPluginResourcesUrl('core') + "/img/profiles/profile_16.png"
    });
	
	// Filter
	config.filter = new Ext.tree.TreeFilter (this, {
		clearBlank: true,
		autoClear: true
	});
	
	this._filterInput = new Ext.form.TextField ({
		id: 'filter',
		
		bodyStyle: 'margin-right: 10px;',
		width: 250,
        emptyText: "<i18n:text i18n:key="PROFILES_TREEPANEL_FILTER_BY_NAME"/>",

		enableKeyEvents:true
	});
	this._filterInput.addListener ('keyup', this._filterByName, this);
	
	// Helper
	this._contextHelperPanel = new org.ametys.HtmlContainer ({
		html: '',
		cls: 'context-helper-text'
	});
	
	config.tbar = [
	    this._filterInput,
	    '-',
	    ' ',
		new Ext.Button({
			   tooltip: "<i18n:text i18n:key="PROFILES_TREEPANEL_BTN_INHERITANCE_TOOLTIP"/>",
			   enableToggle: true,
			   toggleHandler: this._seeInheritance,
			   pressed: true,
			   scope: this,
			   icon: getPluginResourcesUrl('core') + '/img/profiles/actions/inheritance_16.gif',
			   cls: 'x-btn-text-icon'
		}),
		new Ext.Button({
     	   tooltip: "<i18n:text i18n:key="PROFILES_TREEPANEL_BTN_UPTOCONTEXT_TOOLTIP"/>",
     	   handler: this._upToContext,
     	   disabled : true,
     	   scope: this,
     	   icon: getPluginResourcesUrl('core') + '/img/profiles/actions/up_16.png',
     	   cls: 'x-btn-text-icon'
        }),
		'-',
       new Ext.Button({
   	    	tooltip: "<i18n:text i18n:key="PROFILES_TREEPANEL_BTN_EXPAND_TOOTIP"/>",
   	    	handler: function () {this.expandAll() },
   	    	scope: this,
   	    	icon: getPluginResourcesUrl('core') + '/img/profiles/actions/expand-all.gif',
   	    	cls: 'x-btn-text-icon'
       }),
       new Ext.Button({
    	   tooltip: "<i18n:text i18n:key="PROFILES_TREEPANEL_BTN_COLLAPSE_TOOTIP"/>",
    	   handler: function () {this.collapseAll() },
    	   scope: this,
    	   icon: getPluginResourcesUrl('core') + '/img/profiles/actions/collapse-all.gif',
    	   cls: 'x-btn-text-icon'
       }),
       '-',
       this._contextHelperPanel
	];
	
    org.ametys.rights.profile.ProfilesTreePanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.rights.profile.ProfilesTreePanel, Ext.tree.TreePanel, {
	
	autoScroll:true,
    animate:true,
    enableDD:false,
    containerScroll: true,
    border: true,
    
    /**
     * @cfg {Function} upToContextFn The function to call for up to context.
     */
	upToContextFn : null,
	 /**
     * @cfg {String} context The initial context. Defaults to '/resources'
     */
	context: '/resources',
	 /**
     * @cfg {String} rootID The resources root id
     */
	rootID: null
});

/**
 * The current context
 */
org.ametys.rights.profile.ProfilesTreePanel.prototype._context;
org.ametys.rights.profile.ProfilesTreePanel.prototype._loadParams;
org.ametys.rights.profile.ProfilesTreePanel.prototype.updateContext = function (ctx, params, init, callback)
{
	this._context = ctx;
	this._loadParams = params;
	
	// Reload tree
	this.reloadTree.defer (500, this, [callback]);
	
	if (this._context != ctx)
	{
		// this.reloadTree.defer (500, this);
	}
}
org.ametys.rights.profile.ProfilesTreePanel.prototype.updateContextHelperText = function (text)
{
	if (this._contextHelperPanel)
	{
		this._contextHelperPanel.update(text);
	}
}

/**
 * Reload the all tree
 * @param callback the callback function
 */
org.ametys.rights.profile.ProfilesTreePanel.prototype.reloadTree = function (callback)
{
	var rootNode = this.getRootNode();
	if (rootNode != null)
	{
		this.getRootNode().reload(callback);
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._onBeforeLoad = function (loader, node, cb)
{
	if (!this.rendered)
		return;
	
	if (node != null &amp;&amp; node.id != this.getRootNode().id)
	{
		loader.baseParams.profile = node.attributes.id.substring('profile-'.length);
	}
	else
	{
		loader.baseParams.profile = null;
	}
	loader.baseParams.context = this._context;
	
	if (this._loadParams != null)
	{
		for (var i in this._loadParams)
		{
			loader.baseParams[i] = this._loadParams[i];
		}
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._onLoad = function (loader, node, response)
{
	this._setTooltips(node);
	
	// select first node
	if (node.childNodes[0])
	{
		node.childNodes[0].select();
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._setTooltips = function (node)
{
	var childNodes = node.childNodes;
	for (var i=0; i &lt; childNodes.length; i++)
	{
		this._setTooltip (childNodes[i]);
		this._setTooltips (childNodes[i]);
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._setTooltip = function (node)
{
	// Remove if exists
	var tooltip = Ext.getCmp('tooltip-' + node.id);
	if (tooltip)
		tooltip.destroy();
	
	// Create tooltip
	new Ext.ToolTip({
		id: 'tooltip-' + node.id,
	    target: node.getUI().getTextEl(),
	    html: this._createTooltip (node.attributes),
	    dismissDelay: 0
	});
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._createTooltip = function (attributes)
{
	if (attributes.type == 'user' || attributes.type == 'group')
	{
		var name = attributes.type == 'user' ? attributes.name : attributes.label
		var html = "<div class='right-assign-tooltip'>";
		html += "&lt;b&gt;" + name + "&lt;/b&gt;&lt;br/&gt;"
		html += "&lt;u&gt;" + "<i18n:text i18n:key="PROFILES_TREEPANEL_CONTEXT_TOOLTIP"/>" + "&lt;/u&gt; : " + attributes.context;
		html += "</div>";
		
		return html;
	}
	else
	{
		var html = "";
		
		var serverMessage = new org.ametys.servercomm.ServerMessage('core', '/rights/profile.xml', {id: attributes.id.substring('profile-'.length)}, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
		var response = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage); 
		
		var nodes = response.selectNodes ("profile/category");
		for (var i=0; i &lt; nodes.length; i++)
		{
			var label = nodes[i].selectSingleNode ("label")[org.ametys.servercomm.ServerComm.xmlTextContent];
			html += '&lt;b&gt;' + label + '&lt;/b&gt;&lt;br/&gt;';
			
			var rights = nodes[i].selectNodes ("right");
			for (var j=0; j &lt; rights.length; j++)
			{
				var label = rights[j].selectSingleNode ("label")[org.ametys.servercomm.ServerComm.xmlTextContent];
				html += label;
				if (j != rights.length - 1)
				{
					html += ', ';
				}
			}
			
			html += '&lt;br/&gt;';
		}
		
		return html;
	}
}

/**
 * @private Clear the filters applied to the tree nodes
 */
org.ametys.rights.profile.ProfilesTreePanel.prototype._initFilters = function ()
{
	this.filter.clear();
	this._filterInput.setValue('');
	
	if (!this.getTopToolbar().items.get(3).pressed)
	{
		this.filter.filterBy( function (node){
			return node.attributes.inherit != 'true';
		});
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._filterByName = function (field, event)
{
	this.filter.clear();
	var val = field.getRawValue()
	var re = new RegExp('.*' + val + '.*', 'i');
	
	this.filter.filterBy( function (node){
		return re.test(node.text) || node.attributes.type == 'profile';
	});
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._seeInheritance = function (button, state)
{
	if (state)
	{
		this.filter.clear();
	}
	else
	{
		this.filter.filterBy( function (node){
			return node.attributes.inherit != 'true';
		});
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype._upToContext = function (button, state)
{
	var node = this.getSelectionModel().getSelectedNode();
	if (node != null)
	{
		var context = node.attributes.context;
		if (typeof this.upToContextFn == 'function')
		{
			this.upToContextFn (context);
		}
	}
}

org.ametys.rights.profile.ProfilesTreePanel.prototype.onRender = function(ct, position)
{
	org.ametys.rights.profile.ProfilesTreePanel.superclass.onRender.call(this, ct, position);

	this.getRootNode().expand();
}