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

Ext.namespace('org.ametys.administration.Config');

org.ametys.administration.Config = function ()
{
}

/**
* The configuration fields
* @private
 */
org.ametys.administration.Config._fields = [];

/**
 * {Ext.form.BasicForm} The configuration form
 */
org.ametys.administration.Config._form;

/**
 * {Object []} The navigation items
 * @private
 */
org.ametys.administration.Config._navItems;

org.ametys.administration.Config.initialize = function (pluginName)
{
	org.ametys.administration.Config.pluginName = pluginName;
}

org.ametys.administration.Config.createPanel = function ()
{
	// The form
	org.ametys.administration.Config._form  = new Ext.form.FormPanel({
		region:'center',
		
		baseCls: 'transparent-panel',
		labelWidth :230,
		bodyStyle: 'position:relative;',
			
		border: false,
		autoScroll : true,
		
		id : 'config-inner',
		formId : 'save-config',

		html: ''
	});
	
	org.ametys.administration.Config._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Config._drawNavigationPanel (),
		        org.ametys.administration.Config._drawHandlePanel (),
		        org.ametys.administration.Config._drawHelpPanel ()]
	});
	
	return new Ext.Panel({
		region: 'center',
		
		autoScroll: false,
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		items: [org.ametys.administration.Config._form , 
		        org.ametys.administration.Config._contextualPanel]
	});
}

org.ametys.administration.Config.createFieldSet = function (id, label)
{
	return new org.ametys.Fieldset({
		id : id,
		title : label,
		
		layout: 'form',
		
		width: 595
	});
}

org.ametys.administration.Config.addGroupCategory = function (fd, name)
{
	fd.add (new org.ametys.HtmlContainer ({
		html: name,
		cls: 'ametys-subcategory'
	}));
}

org.ametys.administration.Config.addInputField = function (ct, type, name, value, label, description, enumeration, widget, mandatory, regexp, invalidText)
{
    var field = null;
    
	if (enumeration != null)
	{
	    field = org.ametys.administration.Config._createTextField (name, value, label, description, enumeration, mandatory == 'true', null);
	}
	else
	{
		switch (type) 
		{
			case 'double':
				field = org.ametys.administration.Config._createDoubleField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
				break;
			case 'long':
				field = org.ametys.administration.Config._createLongField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
				break;
			case 'password':
				field = org.ametys.administration.Config._createPasswordField (name, value, label, description);
				break;
			case 'date':
				field = org.ametys.administration.Config._createDateField (name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
				break;
			case 'boolean':
				field = org.ametys.administration.Config._createBooleanField (name, value, label, description);
				break;
			default:
				if (widget != '')
				{
					 field = org.ametys.administration.Config._createWidgetField(widget, name, value, label, description, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
				}
				else
				{
					field = org.ametys.administration.Config._createTextField (name, value, label, description, null, mandatory == 'true', regexp != '' ? new RegExp (regexp) : null, invalidText);
				}
				break;
		}
	}
	
	if (field != null)
    {
	    ct.add(field);
    }
	
	org.ametys.administration.Config._fields.push(name);
	
	return field;
}

org.ametys.administration.Config._widgets = [];
org.ametys.administration.Config.registerWidget = function (widgetId, widgetJSClass)
{
	org.ametys.administration.Config._widgets[widgetId] = widgetJSClass;
}

org.ametys.administration.Config.getInputHeight = function (input)
{
	if (typeof input == 'org.ametys.form.PasswordCreationField')
	{
		return 75;
	}
	else if (typeof input == 'org.ametys.form.BooleanField')
	{
		return 22;
	}
	else if (typeof input == 'org.ametys.form.TextAreaField')
	{
	    return 90;
	}
	else
	{
		return 36;
	}
		
}

org.ametys.administration.Config._createDoubleField = function (name, value, label, description, mandatory, regexp, invalidText)
{
	return new org.ametys.form.DoubleField ({
		name: name,
        fieldLabel: label,
        desc: description,
        
        value: value,
        allowBlank: !mandatory,
        validatorRegexp: regexp,
        validator: function(value)
		{
			if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return true;
			}
			else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return this.blankText;
			}
			else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
			{
				return this.invalidText != null ? this.invalidText : this.regexText;
			}
			else
			{
				return true;
			}
		},
		regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
		invalidText: invalidText != null ? invalidText : null,
        msgTarget: 'side',
        
        width: 250
	});
}

org.ametys.administration.Config._createLongField = function (name, value, label, description, mandatory, regexp, invalidText)
{
	return new org.ametys.form.LongField ({
		name: name,
		fieldLabel: label,
        desc: description,
        
        value: value,
        allowBlank: !mandatory,
        validatorRegexp: regexp,
        validator: function(value)
		{
			if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return true;
			}
			else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return this.blankText;
			}
			else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
			{
				return this.invalidText != null ? this.invalidText : this.regexText;
			}
			else
			{
				return true;
			}
		},
		regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
		invalidText: invalidText != null ? invalidText : null,
		msgTarget: 'side',
		
        width: 250
	});
}

org.ametys.administration.Config._createPasswordField = function (name, value, label, description)
{
	return new org.ametys.form.PasswordCreationField ({
		name: name,
		fieldLabel: label,
	    desc: description,
	    
	    value: value,
	    
	    width: 250
	});
}

org.ametys.administration.Config._createDateField = function (name, value, label, description, mandatory, regexp, invalidText)
{
    var dateValue = value;
    if (typeof value == 'string')
    {
        dateValue = Date.parseDate(value, 'c');
    }
    
	return new org.ametys.form.DateField ({
		name: name,
        fieldLabel: label,
        desc: description,
        
        altFormats: 'c',
        value: dateValue,
        allowBlank: !mandatory,
        validatorRegexp: regexp,
        validator: function(value)
		{
			if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return true;
			}
			else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
			{
				return this.blankText;
			}
			else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
			{
				return this.invalidText != null ? this.invalidText : this.regexText;
			}
			else
			{
				return true;
			}
		},
		regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
		invalidText: invalidText != null ? invalidText : null,
		msgTarget: 'side',
		
        width: 250
	});
}

Ext.namespace('org.ametys.administration.widget');

/** ------------------- Hour Widget -------------------------*/
org.ametys.administration.widget.HourWidget = function (config)
{
	org.ametys.administration.widget.HourWidget.superclass.constructor.call (this, config);
}

Ext.extend(org.ametys.administration.widget.HourWidget, org.ametys.form.TimeField, {
	increment: 60,
    editable: false,
	width: 100
});
org.ametys.administration.Config.registerWidget ('hour', 'org.ametys.administration.widget.HourWidget');

/** ------------------- Time Widget -------------------------*/
org.ametys.administration.widget.TimeWidget = function (config)
{
	org.ametys.administration.widget.TimeWidget.superclass.constructor.call (this, config);
}

Ext.extend(org.ametys.administration.widget.TimeWidget, org.ametys.form.TimeField, {
	increment: 60,
	width: 100
});
org.ametys.administration.Config.registerWidget ('time', 'org.ametys.administration.widget.TimeWidget');

/** ------------------- TextArea Widget -------------------------*/
org.ametys.administration.widget.TextAreaWidget = function (config)
{
	org.ametys.administration.widget.TextAreaWidget.superclass.constructor.call (this, config);
}

Ext.extend(org.ametys.administration.widget.TextAreaWidget, org.ametys.form.TextAreaField, {
	width: 250,
    height: 80
});
org.ametys.administration.Config.registerWidget ('textarea', 'org.ametys.administration.widget.TextAreaWidget');


/** ------------------- Boolean Widget -------------------------*/
org.ametys.administration.Config._createBooleanField = function (name, value, label, description)
{
	return new org.ametys.form.BooleanField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        
        checked: (value == "true")
        
	});
}

org.ametys.administration.Config._createTextField = function (name, value, label, description, enumeration, mandatory, regexp, invalidText)
{
	if (enumeration != null)
	{
		return new org.ametys.form.ComboField ({
			name: name,
			
	        fieldLabel: label,
	        desc: description,
	        value: value,
	        width: 250,
	        
	        allowBlank: !mandatory,
	        msgTarget: 'side',
	        
	        mode: 'local',
	        editable: false,
	        forceSelection: true,
			triggerAction: 'all',
	        store: new Ext.data.SimpleStore({
	            id: 0,
	            fields: [ 'value', {name: 'text', sortType: Utils.sortAsNonAccentedUCString}],
	            data: enumeration,
	            sortInfo: {field: 'text'} // default order
	        }),
	        valueField: 'value',
	        displayField: 'text'
		});
	}
	else
	{
		return new org.ametys.form.TextField ({
			name: name,
			
	        fieldLabel: label,
	        desc: description,
	        value: value,
	        
	        width: 250,
	        
	        allowBlank: !mandatory,
	        validatorRegexp: regexp,
	        validator: function(value)
			{
				if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return true;
				}
				else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return this.blankText;
				}
				else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
				{
					return this.invalidText != null ? this.invalidText : this.regexText;
				}
				else
				{
					return true;
				}
			},
			regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
			invalidText: invalidText != null ? invalidText : null,
			msgTarget: 'side'
		});
	}
}

org.ametys.administration.Config._createWidgetField = function (widgetId, name, value, label, description, mandatory, regexp, invalidText)
{
	var widgetCfg = {
			name: name,
	        fieldLabel: label,
	        desc: description,
	        
	        value: value,
	        allowBlank: !mandatory,
	        validatorRegexp: regexp,
	        validator: function(value)
			{
				if (this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return true;
				}
				else if (!this.allowBlank &amp;&amp; (value.length &lt; 1 || value === this.emptyText))
				{
					return this.blankText;
				}
				else if (this.validatorRegexp &amp;&amp; !this.validatorRegexp.test(value))
				{
					return this.invalidText != null ? this.invalidText : this.regexText;
				}
				else
				{
					return true;
				}
			},
			regexText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_INVALID_REGEXP"/>" + regexp,
			invalidText: invalidText != null ? invalidText : null,
	        msgTarget: 'side'
	};
	
	var widgetClass = org.ametys.administration.Config._widgets[widgetId];
	return eval('new ' + widgetClass + '(widgetCfg)');
}

/**
 * Draw the navigation panel. This function needs the org.ametys.administration.Config._navItems was filled first.
 * @return {org.ametys.NavigationPanel} The navigation panel
 * @private
 */
org.ametys.administration.Config._drawNavigationPanel = function ()
{
	org.ametys.administration.Config._nav = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
	
	for (var i=0; i &lt; org.ametys.administration.Config._navItems.length; i++)
	{
		var item = new org.ametys.NavigationItem ({
			id : "a" + org.ametys.administration.Config._navItems[i].id,
			text: org.ametys.administration.Config._navItems[i].label,
			
			divToScroll: org.ametys.administration.Config._navItems[i].id,
			ctToScroll:  'config-inner',
			
			bindScroll: org.ametys.administration.Config._bindScroll,
			unbindScroll:  org.ametys.administration.Config._unbindScroll,
			
			toggleGroup : 'config-menu'
		});
		
		org.ametys.administration.Config._nav.add(item);
	}
	
	return org.ametys.administration.Config._nav;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Config._drawHandlePanel = function ()
{
	org.ametys.administration.Config._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE"/>"});
	
	// Save action
	org.ametys.administration.Config._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE"/>", 
					 getPluginResourcesUrl(org.ametys.administration.Config.pluginName) + '/img/administrator/config/save.png',
					 org.ametys.administration.Config.save);
	
	// Quit action
	org.ametys.administration.Config._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT"/>", 
				     getPluginResourcesUrl(org.ametys.administration.Config.pluginName) + '/img/administrator/config/quit.png',
				     org.ametys.administration.Config.goBack);
	
	return org.ametys.administration.Config._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Config._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT"/>");
	
	return helpPanel;
}

/**
 * Quit
 */
org.ametys.administration.Config.goBack = function (mask)
{
	if (mask)
	{
		new org.ametys.msg.Mask();
	}
    document.location.href = context.workspaceContext;
}

/**
 * Save configuration
 */
org.ametys.administration.Config.save = function ()
{
	if (!org.ametys.administration.Config._form.getForm().isValid())
	{
		Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_INVALID"/>", this.el.focus.createCallback(this.el));
		org.ametys.administration.Config._form.getForm().markInvalid();
		return;
    }
    
    org.ametys.administration.Config.save._mask = new org.ametys.msg.Mask();
    org.ametys.administration.Config.save2.defer(1);
}
org.ametys.administration.Config.save2 = function ()
{
    var url = getPluginDirectUrl(org.ametys.administration.Config.pluginName) + "/administrator/config/set";

    var args = "";
    var argsObj = org.ametys.administration.Config._getQueryParameters(org.ametys.administration.Config._form.getForm());
    for (var f in argsObj)
    {
    	if (argsObj[f] == null)
    	{
    		delete argsObj[f];
    	}
    };

    
    for (var i in argsObj)
    {
    	args += "&amp;" + i + "=" + encodeURIComponent(argsObj[i]);
    }

    var result = null;
    var ex = "";
    try
    {
    	result = org.ametys.servercomm.DirectComm.getInstance().sendSynchronousRequest(url, args);
    }
    catch (e)
    {
    	ex = "" + e;
    }
    
    org.ametys.administration.Config.save._mask.hide();

	if (result == null)
    {
    	new org.ametys.msg.ErrorDialog("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", 
    			"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR"/>",
    			ex,
    			"org.ametys.administration.Config.save");
        return;
    }
    result = result.responseXML;
    
    var error = org.ametys.servercomm.ServerComm.handleResponse(result, "error");
    if (error != null &amp;&amp; error != "")
    {
    	Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR"/>",
    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
    	});
        return;
    }
    
    Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK"/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO,
			fn: org.ametys.administration.Config.goBack.createCallback(this, true)
    });
}

org.ametys.administration.Config._getQueryParameters = function (form)
{
	var params = {};
	
	form.items.each( function(item) {
		if (item.xtype == 'datefield' &amp;&amp; item.getValue() != '')
		{
			params[item.getName()] = item.getValue().format(Date.patterns.ISO8601Long);
		}
		else if (item.xtype == 'checkbox')
		{
			params[item.getName()] = item.checked;
		}
		else
		{
			params[item.getName()] = item.getValue();
		}
    }, this);
	
	return params;
}
org.ametys.administration.Config._bound;

org.ametys.administration.Config._bindScroll = function ()
{
	org.ametys.administration.Config._bound = true;
}
org.ametys.administration.Config._unbindScroll = function ()
{
	org.ametys.administration.Config._bound = false;
}

org.ametys.administration.Config._calcScrollPosition = function ()
{
	if (!org.ametys.administration.Config._bound)
		return;
		 
	var last;
	var anchors = org.ametys.administration.Config._ct.select('a[name]', true);
	var min = 0;
	var max = org.ametys.administration.Config._form.getEl().child('form').dom.scrollHeight - org.ametys.administration.Config._form.getEl().child('form').getHeight();
	
	var scrollPosition = org.ametys.administration.Config._form.getEl().child('form').dom.scrollTop;
	var p = (scrollPosition - min) / (max - min);
	p = p * org.ametys.administration.Config._form.getInnerHeight();
	
	var a0 = anchors.elements[0].getTop();
	
	for (var i=0;  i &lt; anchors.elements.length; i++)
	{
		var anchor = anchors.elements[i];
		if (i > 0) {
			last = anchors.elements[i-1];
		}
		else {
			last = anchor;
		}
		var posY = anchor.getTop() - a0;
		if(posY >= scrollPosition + p)
		{
			org.ametys.administration.Config._activateItemMenu(last.dom.name);
			return;
		}
	
	}
	org.ametys.administration.Config._activateItemMenu(anchors.elements[anchors.elements.length - 1].dom.name);
}

org.ametys.administration.Config._activateItemMenu = function (id)
{
	var button = Ext.getCmp("a" + id);
	if	(button != null)
	{	
		Ext.getCmp("a" + id).toggle(true);
	}
}

org.ametys.administration.Config._ct;

org.ametys.administration.Config.onready = function () 
{
	org.ametys.administration.Config._bound = true;
	
	org.ametys.administration.Config._ct = Ext.getCmp("config-inner").getEl().child("div:first").child("*:first");
	
	org.ametys.administration.Config._ct.on('scroll', org.ametys.administration.Config._calcScrollPosition);
	
	org.ametys.administration.Config._calcScrollPosition();

}
Ext.onReady(org.ametys.administration.Config.onready);